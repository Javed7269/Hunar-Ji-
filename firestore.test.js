const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const CUSTOMER_UID = "customer_101";
const SELLER_UID = "seller_202";
const MECHANIC_UID = "mechanic_303";
const DELIVERY_UID = "delivery_404";
const ADMIN_UID = "admin_999";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users are denied across all collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").get());
  await assertFails(unauthDb.collection("orders").get());
  await assertFails(unauthDb.collection("users").doc(CUSTOMER_UID).get());
  await assertFails(unauthDb.collection("app_config").doc("platform_settings").get());
});

test("User profile isolation, shadow update, and role escalation guards", async () => {
  const custDb = testEnv.authenticatedContext(CUSTOMER_UID, { email: "cust@example.com", email_verified: true }).firestore();
  const sellerDb = testEnv.authenticatedContext(SELLER_UID, { email: "seller@example.com", email_verified: true }).firestore();
  const now = pastDate(10000);

  // Customer creates own profile with CUSTOMER role
  await assertSucceeds(
    custDb.collection("users").doc(CUSTOMER_UID).set({
      userId: CUSTOMER_UID,
      displayName: "Rohit Sharma",
      email: "cust@example.com",
      role: "CUSTOMER",
      walletBalance: 0,
      loyaltyPoints: 50,
      verified: false,
      online: true,
      blocked: false,
      city: "Patna",
      district: "Patna",
      state: "Bihar",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Self-escalating to ADMIN on create fails
  await assertFails(
    sellerDb.collection("users").doc(SELLER_UID).set({
      userId: SELLER_UID,
      displayName: "Fake Admin",
      email: "seller@example.com",
      role: "ADMIN",
      walletBalance: 0,
      loyaltyPoints: 0,
      verified: true,
      online: true,
      blocked: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Customer cannot read another user's profile
  await assertFails(sellerDb.collection("users").doc(CUSTOMER_UID).get());

  // Customer cannot escalate role or verified flag on update
  await assertFails(
    custDb.collection("users").doc(CUSTOMER_UID).update({
      role: "ADMIN",
      updatedAt: pastDate(1000),
    })
  );

  // Customer valid partial profile update succeeds
  await assertSucceeds(
    custDb.collection("users").doc(CUSTOMER_UID).update({
      displayName: "Rohit K. Sharma",
      city: "Lucknow",
      updatedAt: pastDate(1000),
    })
  );
});

test("User subcollections (cart, wishlist, addresses, wallet_transactions) are strictly owner-isolated", async () => {
  const custDb = testEnv.authenticatedContext(CUSTOMER_UID).firestore();
  const otherDb = testEnv.authenticatedContext(SELLER_UID).firestore();
  const now = pastDate(5000);

  await assertSucceeds(
    custDb.collection("users").doc(CUSTOMER_UID).collection("cart").doc("item_1").set({
      productId: "prod_1",
      title: "Wireless Earbuds",
      category: "Electronics",
      sellerId: SELLER_UID,
      sellerName: "Raju General Store",
      price: 1499,
      originalPrice: 2999,
      quantity: 1,
      variant: "Matte Black",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    custDb.collection("users").doc(CUSTOMER_UID).collection("cart").get()
  );
  await assertFails(
    otherDb.collection("users").doc(CUSTOMER_UID).collection("cart").get()
  );

  // Including @DocumentId 'id' inside payload is rejected
  await assertFails(
    custDb.collection("users").doc(CUSTOMER_UID).collection("addresses").doc("addr_1").set({
      id: "addr_1",
      label: "Home",
      recipientName: "Rohit",
      mobile: "9876543210",
      fullAddress: "Boring Road",
      city: "Patna",
      district: "Patna",
      state: "Bihar",
      pincode: "800001",
      defaultAddress: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Products, Orders, and Delivery Boy acceptance lifecycle", async () => {
  const custDb = testEnv.authenticatedContext(CUSTOMER_UID).firestore();
  const sellerDb = testEnv.authenticatedContext(SELLER_UID).firestore();
  const delivDb = testEnv.authenticatedContext(DELIVERY_UID).firestore();
  const now = pastDate(10000);

  // Seller creates product
  await assertSucceeds(
    sellerDb.collection("products").doc("prod_1").set({
      sellerId: SELLER_UID,
      sellerName: "Raju General Store",
      title: "Sports Shoes",
      category: "Fashion",
      subcategory: "Footwear",
      description: "Lightweight running shoes",
      price: 1799,
      originalPrice: 2999,
      discountPercent: 40,
      stock: 25,
      rating: 4.6,
      reviewCount: 42,
      iconKey: "shoes",
      approved: true,
      featured: true,
      trending: true,
      active: true,
      district: "Patna",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Customer creates PENDING order
  await assertSucceeds(
    custDb.collection("orders").doc("HJ1034").set({
      userId: CUSTOMER_UID,
      sellerId: SELLER_UID,
      deliveryPartnerId: "",
      customerName: "Rohit Sharma",
      sellerName: "Raju General Store",
      deliveryPartnerName: "",
      itemsSummary: "Sports Shoes x1",
      itemCount: 1,
      subtotal: 1799,
      discountAmount: 0,
      deliveryFee: 40,
      taxAmount: 90,
      totalAmount: 1929,
      paymentMethod: "UPI",
      paymentStatus: "PAID",
      status: "PENDING",
      pickupAddress: "Main Market, Patna",
      dropAddress: "Civil Lines, Patna",
      district: "Patna",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Unfiltered list on orders fails
  await assertFails(custDb.collection("orders").get());

  // Customer scoped list succeeds
  await assertSucceeds(custDb.collection("orders").where("userId", "==", CUSTOMER_UID).get());

  // Seller confirms order -> CONFIRMED
  await assertSucceeds(
    sellerDb.collection("orders").doc("HJ1034").update({
      status: "CONFIRMED",
      updatedAt: pastDate(3000),
    })
  );

  // Delivery partner queries available CONFIRMED orders and accepts
  await assertSucceeds(delivDb.collection("orders").where("status", "==", "CONFIRMED").get());
  await assertSucceeds(
    delivDb.collection("orders").doc("HJ1034").update({
      deliveryPartnerId: DELIVERY_UID,
      deliveryPartnerName: "Raju Kumar",
      status: "OUT_FOR_DELIVERY",
      updatedAt: pastDate(2000),
    })
  );

  // Delivery partner completes order
  await assertSucceeds(
    delivDb.collection("orders").doc("HJ1034").update({
      status: "DELIVERED",
      paymentStatus: "PAID",
      updatedAt: pastDate(1000),
    })
  );

  // Terminal state lock: cannot modify DELIVERED order
  await assertFails(
    custDb.collection("orders").doc("HJ1034").update({
      status: "CANCELLED",
      updatedAt: pastDate(500),
    })
  );
});

test("Admin security: verified bootstrap admin succeeds, spoofed unverified email fails", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "hunarji47@gmail.com",
    email_verified: true,
  }).firestore();
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: "hunarji47@gmail.com",
    email_verified: false,
  }).firestore();
  const custDb = testEnv.authenticatedContext(CUSTOMER_UID, {
    email: "cust@example.com",
    email_verified: true,
  }).firestore();
  const now = pastDate(2000);

  const configPayload = {
    customerAppEnabled: true,
    mechanicAppEnabled: true,
    sellerAppEnabled: true,
    deliveryAppEnabled: true,
    maintenanceMode: false,
    razorpayEnabled: true,
    razorpayMode: "TEST",
    razorpayKeyId: "rzp_test_sample",
    upiEnabled: true,
    cardEnabled: true,
    netBankingEnabled: true,
    walletEnabled: true,
    codEnabled: true,
    globalCommissionPercent: 10,
    productCommissionPercent: 8,
    serviceCommissionPercent: 12,
    deliveryCommissionPercent: 5,
    gstPercent: 18,
    smsProvider: "MSG91",
    smsEnabled: true,
    emailEnabled: true,
    pushEnabled: true,
    otpExpiryMinutes: 5,
    supportEmail: "support@hunarji.in",
    supportPhone: "+91 1800-123-4567",
    updatedAt: now,
  };

  await assertFails(spoofDb.collection("app_config").doc("platform_settings").set(configPayload));
  await assertFails(custDb.collection("app_config").doc("platform_settings").set(configPayload));
  await assertSucceeds(adminDb.collection("app_config").doc("platform_settings").set(configPayload));
  await assertSucceeds(custDb.collection("app_config").doc("platform_settings").get());
});

test("Multi-role KYC privacy, self-approval denial, counters, and Admin approval workflow", async () => {
  const mechDb = testEnv.authenticatedContext(MECHANIC_UID, { email: "mech@example.com", email_verified: true }).firestore();
  const otherDb = testEnv.authenticatedContext(SELLER_UID, { email: "other@example.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "hunarji47@gmail.com", email_verified: true }).firestore();
  const now = pastDate(5000);

  // Atomic counter starts at 100001 and increments strictly by +1
  await assertSucceeds(mechDb.collection("counters").doc("reg_MECHANIC").set({ nextSeq: 100001 }));
  await assertSucceeds(mechDb.collection("counters").doc("reg_MECHANIC").update({ nextSeq: 100002 }));
  await assertFails(mechDb.collection("counters").doc("reg_MECHANIC").update({ nextSeq: 100005 }));

  const baseKyc = {
    userId: MECHANIC_UID,
    hunarUserId: "HJ-MEC-100001",
    role: "MECHANIC",
    fullName: "Raju Mistri",
    mobile: "9876543210",
    email: "mech@example.com",
    kycStatus: "PENDING",
    accountStatus: "ACTIVE",
    aadhaarVerified: false,
    panVerified: false,
    addressVerified: false,
    bankVerified: false,
    skillVerified: false,
    shopAddressVerified: false,
    gstVerified: false,
    documentsVerified: false,
    drivingLicenceVerified: false,
    vehicleDocVerified: false,
    kycConsentAccepted: true,
    mobileOtpVerified: true,
    emailVerified: true,
    createdAt: now,
    updatedAt: now,
  };

  // Worker cannot self-approve KYC
  await assertFails(
    mechDb.collection("kyc_applications").doc(MECHANIC_UID).set({
      ...baseKyc,
      kycStatus: "APPROVED",
      panVerified: true,
    })
  );

  // Valid PENDING KYC creation succeeds
  await assertSucceeds(mechDb.collection("kyc_applications").doc(MECHANIC_UID).set(baseKyc));

  // Private KYC doc upload succeeds with SUBMITTED status; self-VERIFIED fails
  await assertFails(
    mechDb.collection("kyc_applications").doc(MECHANIC_UID).collection("documents").doc("PAN_CARD").set({
      docType: "PAN_CARD",
      docLabel: "PAN Card",
      fileName: "pan.jpg",
      mimeType: "image/jpeg",
      byteSize: 20480,
      status: "VERIFIED",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    mechDb.collection("kyc_applications").doc(MECHANIC_UID).collection("documents").doc("PAN_CARD").set({
      docType: "PAN_CARD",
      docLabel: "PAN Card",
      fileName: "pan.jpg",
      mimeType: "image/jpeg",
      byteSize: 20480,
      status: "SUBMITTED",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Other users CANNOT read or list another user's private KYC or documents
  await assertFails(otherDb.collection("kyc_applications").doc(MECHANIC_UID).get());
  await assertFails(otherDb.collection("kyc_applications").get());
  await assertFails(otherDb.collection("kyc_applications").doc(MECHANIC_UID).collection("documents").get());

  // Verified Admin CAN read, verify documents, and approve KYC
  await assertSucceeds(adminDb.collection("kyc_applications").get());
  await assertSucceeds(
    adminDb.collection("kyc_applications").doc(MECHANIC_UID).collection("documents").doc("PAN_CARD").update({
      status: "VERIFIED",
      adminNote: "PAN verified",
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    adminDb.collection("kyc_applications").doc(MECHANIC_UID).update({
      kycStatus: "APPROVED",
      aadhaarVerified: true,
      panVerified: true,
      addressVerified: true,
      bankVerified: true,
      skillVerified: true,
      adminNotes: "All mechanic docs approved.",
      reviewedBy: "hunarji47@gmail.com",
      updatedAt: pastDate(1000),
    })
  );
});

test("Custom claim is_admin: true grants admin access while is_admin: false or absent is denied", async () => {
  const claimAdminDb = testEnv.authenticatedContext("claim_admin_uid", {
    email: "ops@hunarji.in",
    email_verified: true,
    is_admin: true,
  }).firestore();
  const falseClaimDb = testEnv.authenticatedContext("false_claim_uid", {
    email: "ops@hunarji.in",
    email_verified: true,
    is_admin: false,
  }).firestore();
  const now = pastDate(2000);

  await assertFails(
    falseClaimDb.collection("audit_logs").doc("log_1").set({
      actorId: "false_claim_uid",
      actorEmail: "ops@hunarji.in",
      action: "ADMIN_ACCESS",
      targetType: "SYSTEM",
      targetId: "admin_panel",
      details: "Attempt without is_admin=true claim",
      createdAt: now,
    })
  );

  await assertSucceeds(
    claimAdminDb.collection("audit_logs").doc("log_1").set({
      actorId: "claim_admin_uid",
      actorEmail: "ops@hunarji.in",
      action: "ADMIN_ACCESS",
      targetType: "SYSTEM",
      targetId: "admin_panel",
      details: "Verified with is_admin=true custom claim",
      createdAt: now,
    })
  );
});

test("Verified wallet top-ups prevent self-crediting; API integrations are Admin-only; Rewarded ads ledger works", async () => {
  const custDb = testEnv.authenticatedContext(CUSTOMER_UID, { email: "cust@example.com", email_verified: true }).firestore();
  const sellerDb = testEnv.authenticatedContext(SELLER_UID, { email: "seller@example.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "hunarji47@gmail.com", email_verified: true }).firestore();
  const now = pastDate(5000);

  await assertSucceeds(
    custDb.collection("users").doc(CUSTOMER_UID).set({
      userId: CUSTOMER_UID,
      displayName: "Rohit Sharma",
      email: "cust@example.com",
      role: "CUSTOMER",
      walletBalance: 100,
      loyaltyPoints: 50,
      rewardPoints: 0,
      verified: false,
      online: true,
      blocked: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Customer CANNOT arbitrarily increase walletBalance directly
  await assertFails(
    custDb.collection("users").doc(CUSTOMER_UID).update({
      walletBalance: 5000,
      updatedAt: pastDate(1000),
    })
  );

  // Customer creates PENDING wallet top-up request; self-VERIFIED fails
  await assertFails(
    custDb.collection("wallet_topups").doc("top_bad").set({
      userId: CUSTOMER_UID,
      userName: "Rohit Sharma",
      amount: 500,
      method: "UPI",
      paymentReference: "UTR12345678",
      status: "VERIFIED",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    custDb.collection("wallet_topups").doc("top_1").set({
      userId: CUSTOMER_UID,
      userName: "Rohit Sharma",
      amount: 500,
      method: "UPI",
      paymentReference: "UTR12345678",
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Admin can verify top-up and credit walletBalance
  await assertSucceeds(
    adminDb.collection("wallet_topups").doc("top_1").update({
      status: "VERIFIED",
      adminNote: "UTR matched",
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(CUSTOMER_UID).update({
      walletBalance: 600,
      updatedAt: pastDate(1000),
    })
  );

  // API integrations are strictly Admin-only (non-admin reads & writes denied)
  await assertSucceeds(
    adminDb.collection("api_integrations").doc("razorpay").set({
      providerKey: "razorpay",
      providerName: "Razorpay",
      category: "PAYMENT",
      publicKey: "rzp_test_123",
      secretMasked: "rzp_****",
      mode: "TEST",
      enabled: true,
      status: "CONFIGURED",
      updatedAt: now,
    })
  );
  await assertFails(custDb.collection("api_integrations").doc("razorpay").get());
  await assertFails(sellerDb.collection("api_integrations").get());

  // Rewarded ad points transaction creates cleanly once, cannot be updated/spoofed
  await assertSucceeds(
    custDb.collection("users").doc(CUSTOMER_UID).collection("reward_transactions").doc("tok_abc123").set({
      points: 25,
      type: "REWARDED_AD",
      title: "Watched Rewarded Video Ad",
      rewardToken: "tok_abc123",
      adUnitId: "ca-app-pub-3940256099942544/5224354917",
      status: "CREDITED",
      createdAt: now,
    })
  );
  await assertFails(
    custDb.collection("users").doc(CUSTOMER_UID).collection("reward_transactions").doc("tok_abc123").update({
      points: 9999,
    })
  );

  // Categories and Videos readable by signed-in users, writable by Admin only
  await assertSucceeds(
    adminDb.collection("categories").doc("cat_elec").set({
      name: "Electronics",
      iconKey: "earbuds",
      subcategories: ["Audio", "Wearables"],
      active: true,
      sortOrder: 0,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    custDb.collection("categories").doc("cat_hack").set({
      name: "Hack",
      iconKey: "box",
      active: true,
      sortOrder: 1,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(custDb.collection("categories").get());
});



