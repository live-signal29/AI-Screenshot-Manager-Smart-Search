# Google Play Console & AdMob Complete Setup Guide

Aapke Play Console aur AdMob ke teeno screenshots ka complete solution aur step-by-step guide neeche di gayi hai:

---

## 1. Play Console Issue: "Add the BILLING permission to your APK" (Screenshots 1 & 3)

### Wajah (Why this happened):
Google Play Console par **"One-time products"** aur **"Subscriptions"** tab tab tak lock rehte hain jab tak aapki uploaded APK/AAB file mein Google Play Billing library aur `com.android.vending.BILLING` permission maujood na ho. 

### Humne Kya Fix Kiya (What is now fixed in Code):
1. **Billing Permission Added**: `AndroidManifest.xml` mein `<uses-permission android:name="com.android.vending.BILLING" />` add kar di gayi hai.
2. **Official Google Play Billing Library**: `com.android.billingclient:billing-ktx:7.1.1` integrate kar di gayi hai.
3. **BillingManager & In-App Purchase Flow**: `BillingManager.kt` banaya gaya hai jo purchases, subscriptions, aur purchase restoration (`Restore Purchases`) handle karta hai.
4. **Premium Section UI**: Settings screen par "Upgrade to Pro" card aur interactive paywall modal add kiya gaya hai jisme:
   - Lifetime Access (One-time payment)
   - Monthly Plan (Auto-renewing subscription)
   - Ad-Free experience (Premium users ko saare ads hide ho jate hain)
   - Restore Purchases button

### Ab Play Console par kaise set up karein:
1. Nayi AAB bundle file ko download karke Play Console ke **Internal testing** ya **Production track** par upload karein.
2. Upload hote hi Google Play Console `BILLING` permission detect kar lega aur **One-time products** aur **Subscriptions** ke buttons unlock ho jayenge!
3. **One-time product banane ke steps**:
   - Play Console -> **Monetize with Play** -> **Products** -> **One-time products** par jayein.
   - Click **Create product**.
   - **Product ID**: `lifetime_pro` dalein (ye app code ke sath matched hai).
   - Name dalein: `AI Screenshot Lifetime Pro`.
   - Description dalein: `Ad-free unlimited smart search and cleaner`.
   - Price set karein (e.g. ₹299 / $4.99) aur **Save** & **Activate** karein.
4. **Subscription banane ke steps**:
   - Play Console -> **Monetize with Play** -> **Products** -> **Subscriptions** par jayein.
   - Click **Create subscription**.
   - **Subscription ID**: `pro_monthly` (ya `pro_yearly`).
   - Name dalein: `Pro Monthly Membership`.
   - Add base plan, billing period select karein (Monthly), price set karein aur **Save** & **Activate** karein.

---

## 2. AdMob Issue: "Can't find your app? Double-check your app store details" (Screenshot 2)

### Wajah (Why this happened):
Aapne AdMob par package name `com.aistudio.screenshotmanager.prod` daal kar search kiya. Lekin **Google Play search par app tabhi dikhta hai jab app Play Store par publicly publish aur live ho**. Kyunki app abhi Play Console par review/draft mein hai, isliye AdMob search use nahi dhundh paya.

### AdMob par App Add karne ka Sahi Tareeka (Follow this now):
1. AdMob par **"Set up a new app"** page par jayein.
2. **Step 1: Select app stores**:
   - Platform: **Android** select karein.
   - *Is the app listed on a supported app store?*: **"No"** select karein! (Ye sabse zaroori step hai).
   - Click **Continue**.
3. **Step 2: Add app details**:
   - App name: **AI Screenshot** dalein.
   - User metrics: Enable rehne dein.
   - Click **Add App**.
4. Aapka AdMob App create ho jayega aur aapko **AdMob App ID** mil jayega (Format: `ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy`).
5. Ab **Ad units** par click karke 3 ad units create karein:
   - **Banner** (e.g., `ca-app-pub-.../...`)
   - **Interstitial** (e.g., `ca-app-pub-.../...`)
   - **Rewarded** (e.g., `ca-app-pub-.../...`)
6. **Future Step (Jab app live ho jaye)**:
   Jab Google Play Store par aapka app approved aur live ho jaye, tab aap AdMob mein:
   - **App settings** -> **App store details** par click karke Google Play ke sath link kar sakte hain 1-click mein!

---

## 3. Summary of Changes Done in App

| Feature | Status | Details |
|---|---|---|
| **BILLING Permission** | Added | `<uses-permission android:name="com.android.vending.BILLING" />` in Manifest |
| **Play Billing Library** | Integrated | `com.android.billingclient:billing-ktx:7.1.1` in Gradle |
| **Billing Engine** | Completed | `BillingManager.kt` with In-App & Subs support |
| **Premium UI** | Completed | `PremiumSection.kt` in Settings with Paywall & Restore |
| **Ad Hiding for Pro** | Completed | All Banner & Interstitial ads automatically disabled for Pro users |
| **AAB Release Build** | Ready | Ready for Play Store upload |
