# Hunar Ji Admin Control Center

This is a static Firebase-backed Admin website. It uses the existing named Firestore database configured by the Android app and does not contain service-account credentials or payment secrets.

## Deploy to Netlify

1. Connect this GitHub repository to Netlify.
2. Keep the base directory empty.
3. Leave the build command empty.
4. Set the publish directory to `admin-web` (also configured in the root `netlify.toml`).
5. Deploy the `main` branch.

## Firebase setup required

1. In Firebase Console, open Authentication → Sign-in method and enable Google.
2. In Authentication → Settings → Authorized domains, add the Netlify domain used by this site.
3. Confirm that the Firestore database ID is `ai-studio-android-hunarji-1c6adcb8-e8ca-431c-87d6-1a54bee28f29`.
4. Deploy the repository's `firestore.rules` to that same named database.
5. The bootstrap Admin account must be `hunarji47@gmail.com` with verified email, or another account must have the server-issued `is_admin: true` custom claim.

## Current scope

The website provides secure Admin sign-in, overview counts, and read-only browsing for users, KYC applications, orders, bookings, partners, products, services, withdrawals, wallet top-ups, API integration records, banners, videos, and support tickets. Sensitive key-like fields are redacted in the browser.

Payment execution, payout approval, KYC document download, SMS sending, and API-secret changes must be implemented through trusted server functions and strict rules before production use. Do not paste payment secrets into this static website.
