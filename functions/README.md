# Hunar Ji Admin email/password + email OTP authentication

The Admin website uses a separate Firebase email/password account and a six-digit email OTP. Google sign-in is not used for Admin access.

## Security model
- The initial Admin account is created only after Firebase email verification and a one-time bootstrap code.
- The bootstrap code is stored in Firebase Functions Secret Manager, never in the website or GitHub.
- Every Admin login requires Firebase email/password authentication followed by a server-generated six-digit email OTP.
- OTP values are HMAC-hashed in Firestore, expire after five minutes, have a five-attempt limit and a one-minute resend cooldown.
- The server assigns the `is_admin` custom claim only after successful OTP verification.
- Admin sessions expire after eight hours. Logout clears the claim and revokes refresh tokens.
- Firestore Admin access is based on the server-issued custom claim only. Client writes to the `admins` collection are denied.
- Never store passwords, OTPs, SMTP passwords, service account JSON, or private tokens in Git.

## One-time setup
1. Enable **Email/Password** in Firebase Console → Authentication → Sign-in method.
2. Confirm the Firebase project has billing enabled for Cloud Functions deployment, and that your selected SMTP provider allows authenticated transactional email.
3. From the repository root install Firebase CLI and log in: `npm install -g firebase-tools`, then `firebase login`.
4. Set the secrets (use a long random bootstrap code and a separate random OTP pepper):
   ```sh
   firebase functions:secrets:set ADMIN_BOOTSTRAP_CODE
   firebase functions:secrets:set ADMIN_OTP_PEPPER
   firebase functions:secrets:set ADMIN_SMTP_HOST
   firebase functions:secrets:set ADMIN_SMTP_PORT
   firebase functions:secrets:set ADMIN_SMTP_USER
   firebase functions:secrets:set ADMIN_SMTP_PASS
   firebase functions:secrets:set ADMIN_SMTP_FROM
   ```
   Enter each value only at the CLI prompt. Do not paste secrets into GitHub.
5. Deploy: `firebase deploy --only functions,firestore:rules`.
6. Open the Admin website. Create the first Admin account using an email address you control, a strong unique password and the one-time setup code. Verify the email using the Firebase verification email.
7. Sign in with the same email/password, request the six-digit OTP, then enter the code delivered to that email.
8. Store the bootstrap code safely offline. The first successful setup consumes it; subsequent Admin accounts must be provisioned by a future secure Admin invitation workflow.

## Important
This repository commit does not deploy Cloud Functions, set SMTP secrets, enable Firebase Email/Password, or create an Admin account. Those steps require the project owner's Firebase console/CLI access. Do not claim production MFA is active until the functions and rules deploy and a real email OTP login is tested.
