# AI Screenshot Manager – Smart Search

**Developer:** FX Signal Lab  
**Platform:** Android (Jetpack Compose & Kotlin)  
**Target SDK:** Android 14+ (API 34/35)  
**Architecture:** Modern MVVM with Clean Architecture & Material Design 3  

---

## Overview

**AI Screenshot Manager – Smart Search** is an intelligent, privacy-first screenshot management Android application developed by **FX Signal Lab**. It automatically organizes screenshots, extracts searchable text, identifies structured information (prices, dates, emails, phone numbers, order numbers, URLs), finds duplicate and near-duplicate screenshots, and allows users to search their library using natural-language queries.

---

## Key Features

### 1. Natural Language Smart Search
Search through thousands of screenshots using colloquial queries:
- *"Meri flight ticket wali screenshots"*
- *"Show screenshots containing $50"*
- *"Find my shopping screenshots"*
- *"Show screenshots from January"*
- *"Find screenshots containing a phone number"*
- *"Find receipts"*

The on-device NLP search engine translates human queries into local filter criteria, full-text token searches, category mappings, and entity constraints without requiring an internet connection.

### 2. On-Device Text Recognition (OCR) & Entity Extraction
- Extracts all visible text from screenshots using Google ML Kit Vision.
- Identifies actionable entities:
  - **Prices & Currency:** Tap to copy or track.
  - **Phone Numbers:** Direct dial or copy.
  - **Email Addresses:** Send email or copy.
  - **URLs & Links:** Open in browser or copy.
  - **Dates & Times:** Detects calendar dates.
  - **Orders / Tracking / Flight Numbers:** Instant copy.

### 3. Automated Smart Categorization
Screenshots are classified into 14 distinct productivity categories:
- **Receipts**
- **Shopping**
- **Bills**
- **Tickets & Boarding Passes**
- **Travel**
- **Study & Lectures**
- **Notes & Checklists**
- **Documents & Contracts**
- **Social**
- **Entertainment**
- **Maps & Navigation**
- **Finance & Banking**
- **Food & Menus**
- **Other**

### 4. Perceptual Duplicate Detection & Storage Cleaner
- **Difference Hashing (dHash):** Computes a 64-bit perceptual hash to detect near-duplicates, repeated screenshots, and different compressions.
- **SHA-256 File Hashing:** Detects exact byte-for-byte duplicates.
- **Smart Cleanup:** Identifies removable screenshots such as temporary 2FA/OTP verification codes, expired notices, and duplicate clusters.
- **Safe Review:** Never deletes files automatically. Requires explicit user review and confirmation.

### 5. AI Assistant (Optional & Privacy-Controlled)
- **Local Mode (Default):** Synthesizes insights and answers questions using on-device OCR and extracted entities with zero internet required.
- **Cloud AI Mode (Optional):** Users can securely enter a Gemini API key or proxy endpoint in Settings for deep semantic explanations. No keys are hardcoded.

### 6. Monetization & AdMob Integration
- Centralized `AdMobConfig` separating Debug and Release environments.
- Uses official Google test ad unit IDs during development.
- Policy-compliant banner placements and cooldown-protected interstitial transitions.

---

## Build Instructions

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 34 / 35

### Command Line Builds

#### 1. Compile and Run Unit Tests
```bash
gradle testDebugUnitTest
```

#### 2. Build Debug APK
```bash
gradle assembleDebug
```
Output location: `app/build/outputs/apk/debug/app-debug.apk`

#### 3. Build Release APK
```bash
gradle assembleRelease
```
Output location: `app/build/outputs/apk/release/app-release.apk`

#### 4. Build Google Play AAB (Android App Bundle)
```bash
gradle bundleRelease
```
Output location: `app/build/outputs/bundle/release/app-release.aab`

---

## Release Signing Configuration

For automated CI/CD and release builds, configure these environment variables or GitHub Secrets:

| Secret Name | Description |
|---|---|
| `SIGNING_KEYSTORE_BASE64` | Base64-encoded release keystore file |
| `KEYSTORE_PATH` | Path to the `.jks` or `.keystore` file |
| `KEY_ALIAS` | Key alias in the keystore |
| `KEY_PASSWORD` | Password for the key |
| `STORE_PASSWORD` | Password for the keystore |

---

## AdMob Production Setup

Before releasing to Google Play, replace the placeholders with your official AdMob IDs:

1. **App ID in Manifest:** Update `admob_app_id` in `app/src/main/res/values/strings.xml`:
```xml
<string name="admob_app_id">ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX</string>
```

2. **Ad Unit IDs:** Update production constants in `app/src/main/java/com/example/ads/AdMobConfig.kt`:
- `PROD_BANNER_ID`
- `PROD_INTERSTITIAL_ID`
- `PROD_REWARDED_ID`

---

## Google Play Store Metadata & ASO (App Store Optimization)

### App Title (30 characters max - Google Play Policy Compliant)
```
AI Screenshot Manager: Search
```

### Short Description (80 characters max)
```
Smart search, organize, extract text, and clean duplicate screenshots with AI.
```

### Full Description
```
Tired of scrolling through thousands of messy screenshots?

AI Screenshot Manager – Smart Search by FX Signal Lab is your intelligent on-device assistant designed to organize, search, and clean your screenshot gallery in seconds.

🔍 NATURAL LANGUAGE SMART SEARCH
Search your screenshots just like you speak!
• "Show my flight ticket"
• "Screenshots containing $50"
• "Find shopping receipts from this month"
• "Find phone numbers"
Our smart search understands your intent and finds the exact screenshot instantly.

📑 ON-DEVICE OCR & TEXT EXTRACTION
Extract text from images without uploading your photos to the internet.
• Instant copy: Extract prices, dates, tracking numbers, and addresses.
• One-tap actions: Call detected phone numbers, send emails, or open web links directly.

🗂️ 14 AUTOMATIC SMART CATEGORIES
Organize your screenshot library into clean, dedicated categories:
Receipts, Shopping, Bills, Tickets, Travel, Study Notes, Documents, Finance, Food, and more.

🧹 DUPLICATE DETECTOR & STORAGE CLEANER
Recover gigabytes of wasted storage:
• Perceptual Difference Hashing (dHash) detects identical and repeated screenshots.
• Identify temporary OTP codes and verification screenshots that are safe to delete.
• Review and free up storage with full user control.

🔒 100% ON-DEVICE PRIVACY
Your screenshots never leave your phone. All image scanning, text recognition, and organization happen entirely on-device for maximum privacy and security.

Designed with Material Design 3 for a fluid, modern Android experience.
```

### Keywords & Tags
`screenshot manager`, `smart search`, `ocr text scanner`, `duplicate photo cleaner`, `receipt scanner`, `screenshot organizer`, `extract text from image`, `storage cleaner`, `gallery cleaner`, `productivity`

---

## License

Copyright © 2026 FX Signal Lab. All rights reserved.
