"use strict";

const crypto = require("node:crypto");
const nodemailer = require("nodemailer");
const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore, Timestamp, FieldValue } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();
const auth = getAuth();

const BOOTSTRAP_CODE = defineSecret("ADMIN_BOOTSTRAP_CODE");
const OTP_PEPPER = defineSecret("ADMIN_OTP_PEPPER");
const SMTP_HOST = defineSecret("ADMIN_SMTP_HOST");
const SMTP_PORT = defineSecret("ADMIN_SMTP_PORT");
const SMTP_USER = defineSecret("ADMIN_SMTP_USER");
const SMTP_PASS = defineSecret("ADMIN_SMTP_PASS");
const SMTP_FROM = defineSecret("ADMIN_SMTP_FROM");

const mailSecrets = [OTP_PEPPER, SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASS, SMTP_FROM];
const bootstrapSecrets = [BOOTSTRAP_CODE];

function safeEqual(a, b) {
  const left = Buffer.from(String(a || ""));
  const right = Buffer.from(String(b || ""));
  return left.length === right.length && crypto.timingSafeEqual(left, right);
}
function hashOtp(uid, otp) {
  return crypto.createHmac("sha256", OTP_PEPPER.value()).update(uid + ":" + otp).digest("hex");
}
function requireVerifiedUser(request) {
  if (!request.auth || !request.auth.uid) throw new HttpsError("unauthenticated", "Sign in first.");
  if (request.auth.token.email_verified !== true || !request.auth.token.email) {
    throw new HttpsError("failed-precondition", "Verify your email address before continuing.");
  }
  return { uid: request.auth.uid, email: String(request.auth.token.email).toLowerCase() };
}
function requireAdmin(request) {
  const user = requireVerifiedUser(request);
  if (request.auth.token.is_admin !== true) throw new HttpsError("permission-denied", "Administrator access is required.");
  return user;
}
function mailer() {
  const port = Number(SMTP_PORT.value() || 587);
  return nodemailer.createTransport({
    host: SMTP_HOST.value(),
    port,
    secure: port === 465,
    auth: { user: SMTP_USER.value(), pass: SMTP_PASS.value() },
    tls: { minVersion: "TLSv1.2" }
  });
}

exports.createFirstAdminAccount = onCall({ secrets: bootstrapSecrets, enforceAppCheck: false }, async (request) => {
  const user = requireVerifiedUser(request);
  const supplied = String(request.data?.setupCode || "");
  const expected = BOOTSTRAP_CODE.value();
  if (!expected || !safeEqual(supplied, expected)) throw new HttpsError("permission-denied", "Invalid one-time administrator setup code.");
  const bootstrapRef = db.doc("system_security/admin_bootstrap");
  const adminRef = db.doc("admins/" + user.uid);
  await db.runTransaction(async (tx) => {
    const [bootstrapSnap, adminSnap] = await Promise.all([tx.get(bootstrapRef), tx.get(adminRef)]);
    if (bootstrapSnap.exists && bootstrapSnap.get("consumed") === true) {
      throw new HttpsError("failed-precondition", "Initial administrator setup has already been used. Ask an existing admin to invite another admin.");
    }
    if (adminSnap.exists && adminSnap.get("status") === "ACTIVE") return;
    tx.set(bootstrapRef, { consumed: true, consumedBy: user.uid, consumedAt: FieldValue.serverTimestamp() });
    tx.set(adminRef, { uid: user.uid, email: user.email, status: "ACTIVE", createdAt: FieldValue.serverTimestamp(), createdBy: "bootstrap" });
  });
  await auth.setCustomUserClaims(user.uid, { ...(await auth.getUser(user.uid)).customClaims, is_admin: false });
  return { success: true, message: "Admin account registered. Sign in again and request the email OTP." };
});

exports.requestAdminEmailOtp = onCall({ secrets: mailSecrets, enforceAppCheck: false }, async (request) => {
  const user = requireVerifiedUser(request);
  const adminSnap = await db.doc("admins/" + user.uid).get();
  if (!adminSnap.exists || adminSnap.get("status") !== "ACTIVE" || adminSnap.get("email") !== user.email) {
    throw new HttpsError("permission-denied", "This email is not registered as an active Hunar Ji administrator.");
  }
  const challengeRef = db.doc("admin_login_challenges/" + user.uid);
  const now = Date.now();
  const otp = String(crypto.randomInt(100000, 1000000));
  await db.runTransaction(async (tx) => {
    const snap = await tx.get(challengeRef);
    const lastSent = snap.exists ? snap.get("sentAt")?.toMillis?.() || 0 : 0;
    if (now - lastSent < 60_000) throw new HttpsError("resource-exhausted", "Please wait one minute before requesting another OTP.");
    tx.set(challengeRef, {
      otpHash: hashOtp(user.uid, otp),
      expiresAt: Timestamp.fromMillis(now + 5 * 60_000),
      sentAt: Timestamp.fromMillis(now),
      attempts: 0
    });
  });
  try {
    await mailer().sendMail({
      from: SMTP_FROM.value(),
      to: user.email,
      subject: "Your Hunar Ji Admin login code",
      text: "Your Hunar Ji Admin verification code is " + otp + ". It expires in 5 minutes. Do not share this code with anyone.",
      html: "<div style=\"font-family:Arial,sans-serif;max-width:520px;margin:auto;padding:24px\"><h2>Hunar Ji Admin security check</h2><p>Your one-time login code is:</p><div style=\"font-size:32px;font-weight:800;letter-spacing:8px;padding:16px;background:#f1f5f9;border-radius:12px\">" + otp + "</div><p>This code expires in 5 minutes. Never share it with anyone.</p><p>If you did not request this code, secure your account immediately.</p></div>"
    });
  } catch (error) {
    await challengeRef.delete().catch(() => {});
    console.error("Admin OTP email delivery failed", error);
    throw new HttpsError("unavailable", "The verification email could not be sent. Check the configured mail service.");
  }
  return { success: true, expiresInSeconds: 300 };
});

exports.verifyAdminEmailOtp = onCall({ secrets: [OTP_PEPPER], enforceAppCheck: false }, async (request) => {
  const user = requireVerifiedUser(request);
  const otp = String(request.data?.otp || "").trim();
  if (!/^\d{6}$/.test(otp)) throw new HttpsError("invalid-argument", "Enter the six-digit email code.");
  const adminRef = db.doc("admins/" + user.uid);
  const challengeRef = db.doc("admin_login_challenges/" + user.uid);
  const [adminSnap, challengeSnap] = await Promise.all([adminRef.get(), challengeRef.get()]);
  if (!adminSnap.exists || adminSnap.get("status") !== "ACTIVE" || adminSnap.get("email") !== user.email) {
    throw new HttpsError("permission-denied", "Administrator account is not active.");
  }
  if (!challengeSnap.exists) throw new HttpsError("deadline-exceeded", "Request a new verification code.");
  const expiry = challengeSnap.get("expiresAt")?.toMillis?.() || 0;
  const attempts = Number(challengeSnap.get("attempts") || 0);
  if (Date.now() > expiry || attempts >= 5) {
    await challengeRef.delete();
    throw new HttpsError("deadline-exceeded", "The code expired or too many attempts were made. Request a new code.");
  }
  const matches = safeEqual(challengeSnap.get("otpHash"), hashOtp(user.uid, otp));
  if (!matches) {
    await challengeRef.update({ attempts: FieldValue.increment(1) });
    throw new HttpsError("permission-denied", "Incorrect verification code.");
  }
  await challengeRef.delete();
  const current = await auth.getUser(user.uid);
  const expiresAt = Math.floor(Date.now() / 1000) + 8 * 60 * 60;
  await auth.setCustomUserClaims(user.uid, {
    ...(current.customClaims || {}),
    is_admin: true,
    admin_session_expires_at: expiresAt,
    admin_mfa_at: Math.floor(Date.now() / 1000)
  });
  await db.collection("audit_logs").add({
    actorId: user.uid,
    action: "ADMIN_MFA_LOGIN",
    targetType: "admin",
    targetId: user.uid,
    details: { email: user.email },
    createdAt: FieldValue.serverTimestamp()
  });
  return { success: true, sessionExpiresAt: expiresAt };
});

exports.endAdminSession = onCall({ enforceAppCheck: false }, async (request) => {
  const user = requireAdmin(request);
  const current = await auth.getUser(user.uid);
  const claims = { ...(current.customClaims || {}) };
  delete claims.is_admin;
  delete claims.admin_session_expires_at;
  delete claims.admin_mfa_at;
  await auth.setCustomUserClaims(user.uid, claims);
  await auth.revokeRefreshTokens(user.uid);
  await db.doc("admin_login_challenges/" + user.uid).delete().catch(() => {});
  return { success: true };
});
