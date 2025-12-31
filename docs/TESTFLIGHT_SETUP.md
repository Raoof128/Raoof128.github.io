# 📱 TestFlight Setup Guide for Mehr Guard iOS

This guide covers setting up TestFlight for iOS beta testing of Mehr Guard.

---

## 📋 Prerequisites

- [x] Apple Developer Account ($99/year)
- [x] Xcode 15+ with valid signing certificate
- [x] App Store Connect access
- [x] Mehr Guard iOS app project

---

## 🚀 Step 1: App Store Connect Setup

### 1.1 Create App in App Store Connect

1. Log in to [App Store Connect](https://appstoreconnect.apple.com)
2. Navigate to **My Apps** → **+** → **New App**
3. Fill in app details:

| Field | Value |
|-------|-------|
| **Platform** | iOS |
| **Name** | Mehr Guard |
| **Primary Language** | English (U.S.) |
| **Bundle ID** | `com.mehrguard.ios` |
| **SKU** | `mehrguard-ios-001` |
| **User Access** | Full Access |

---

## 🔐 Step 2: Code Signing Setup

### 2.1 Create Certificates & Profiles

1. Open **Xcode** → **Preferences** → **Accounts**
2. Sign in with your Apple Developer account
3. Select your team → **Manage Certificates**
4. Create **Apple Distribution** certificate if not exists

### 2.2 Create App ID

1. Go to [Apple Developer Portal](https://developer.apple.com/account/resources/identifiers/list)
2. Click **+** → **App IDs** → **Continue**
3. Configure:
   - **Description**: Mehr Guard iOS
   - **Bundle ID**: Explicit → `com.mehrguard.ios`
   - **Capabilities**: Enable required capabilities

### 2.3 Create Provisioning Profile

1. Go to **Profiles** → **+**
2. Select **App Store** (Distribution)
3. Choose App ID: `com.mehrguard.ios`
4. Select your Distribution certificate
5. Name: `MehrGuard AppStore Profile`
6. Download and double-click to install

---

## 📦 Step 3: Build for TestFlight

### 3.1 Configure Xcode Project

Open `iosApp/MehrGuard.xcodeproj` in Xcode:

```xcodebuild
# Build Settings
PRODUCT_BUNDLE_IDENTIFIER = com.mehrguard.ios
DEVELOPMENT_TEAM = YOUR_TEAM_ID
CODE_SIGN_IDENTITY = Apple Distribution
PROVISIONING_PROFILE_SPECIFIER = MehrGuard AppStore Profile
```

### 3.2 Update Build Number

In `Info.plist` or Xcode:
```xml
<key>CFBundleShortVersionString</key>
<string>1.0.0</string>
<key>CFBundleVersion</key>
<string>1</string>
```

### 3.3 Archive the App

1. Select **Any iOS Device (arm64)** as destination
2. **Product** → **Archive**
3. Wait for archive to complete
4. **Organizer** window opens automatically

### 3.4 Upload to App Store Connect

1. In **Organizer**, select the archive
2. Click **Distribute App**
3. Choose **App Store Connect**
4. Select **Upload**
5. Review signing options → **Next**
6. Review app content → **Upload**

---

## 🧪 Step 4: Configure TestFlight

### 4.1 Add External Testers Group

1. In App Store Connect → **TestFlight** tab
2. Click **+** next to "External Groups"
3. Create group: `Mehr Guard Beta Testers`

### 4.2 Add Build to Group

1. Select your uploaded build
2. Click **Add to Group**
3. Select `Mehr Guard Beta Testers`
4. Click **Submit for Beta Review**

### 4.3 Configure Beta App Information

| Field | Value |
|-------|-------|
| **Beta App Description** | Mehr Guard scans QR codes and detects phishing attempts using advanced heuristic analysis. Help us test before the public release! |
| **Feedback Email** | beta@mehrguard.app |
| **Marketing URL** | https://github.com/Raoof128/Raoof128.github.io |
| **Privacy Policy URL** | https://github.com/Raoof128/Raoof128.github.io/blob/main/SECURITY.md |

### 4.4 Get Public TestFlight Link

1. After approval, go to **External Groups** → `Mehr Guard Beta Testers`
2. Enable **Public Link**
3. Copy the link: `https://testflight.apple.com/join/XXXXXXXX`

---

## 🔄 Step 5: GitHub Actions CI/CD (Optional)

For automated TestFlight deployments, add this workflow:

### 5.1 Create iOS Release Workflow

Create `.github/workflows/ios-release.yml`:

```yaml
name: iOS TestFlight Release

on:
  push:
    tags:
      - 'v*-ios'
  workflow_dispatch:

jobs:
  build-and-upload:
    runs-on: macos-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build KMP iOS Framework
      run: ./gradlew :common:linkReleaseFrameworkIosArm64 --no-daemon
    
    - name: Install Apple Certificate
      uses: apple-actions/import-codesign-certs@v2
      with:
        p12-file-base64: ${{ secrets.APPLE_CERTIFICATE_P12 }}
        p12-password: ${{ secrets.APPLE_CERTIFICATE_PASSWORD }}
    
    - name: Install Provisioning Profile
      uses: apple-actions/download-provisioning-profiles@v2
      with:
        bundle-id: 'com.mehrguard.ios'
        issuer-id: ${{ secrets.APPSTORE_ISSUER_ID }}
        api-key-id: ${{ secrets.APPSTORE_API_KEY_ID }}
        api-private-key: ${{ secrets.APPSTORE_API_PRIVATE_KEY }}
    
    - name: Build iOS App
      run: |
        cd iosApp
        xcodebuild archive \
          -project MehrGuard.xcodeproj \
          -scheme MehrGuard \
          -archivePath build/MehrGuard.xcarchive \
          -destination generic/platform=iOS \
          CODE_SIGN_STYLE=Manual \
          DEVELOPMENT_TEAM=${{ secrets.APPLE_TEAM_ID }}
    
    - name: Export IPA
      run: |
        cd iosApp
        xcodebuild -exportArchive \
          -archivePath build/MehrGuard.xcarchive \
          -exportPath build/export \
          -exportOptionsPlist ExportOptions.plist
    
    - name: Upload to TestFlight
      uses: apple-actions/upload-testflight-build@v1
      with:
        app-path: 'iosApp/build/export/MehrGuard.ipa'
        issuer-id: ${{ secrets.APPSTORE_ISSUER_ID }}
        api-key-id: ${{ secrets.APPSTORE_API_KEY_ID }}
        api-private-key: ${{ secrets.APPSTORE_API_PRIVATE_KEY }}
```

### 5.2 Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `APPLE_CERTIFICATE_P12` | Base64-encoded .p12 certificate |
| `APPLE_CERTIFICATE_PASSWORD` | Certificate password |
| `APPLE_TEAM_ID` | Your Apple Developer Team ID |
| `APPSTORE_ISSUER_ID` | App Store Connect API Issuer ID |
| `APPSTORE_API_KEY_ID` | App Store Connect API Key ID |
| `APPSTORE_API_PRIVATE_KEY` | App Store Connect API Private Key (.p8) |

### 5.3 Create ExportOptions.plist

Create `iosApp/ExportOptions.plist`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>app-store</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>uploadBitcode</key>
    <false/>
    <key>uploadSymbols</key>
    <true/>
    <key>destination</key>
    <string>upload</string>
</dict>
</plist>
```

---

## 📊 Step 6: Managing Beta Testing

### 6.1 Viewing Crash Reports

1. App Store Connect → **TestFlight** → **Crashes**
2. Review symbolicated crash logs
3. Track issues by build version

### 6.2 Collecting Feedback

1. Testers can submit feedback via TestFlight app
2. Screenshots automatically included
3. Review in App Store Connect → **TestFlight** → **Feedback**

### 6.3 Expiring Builds

- TestFlight builds expire after **90 days**
- Upload new builds regularly
- Previous builds archived automatically

---

## 🔗 Public TestFlight Links

After approval, your TestFlight link will look like:

```
https://testflight.apple.com/join/XXXXXXXX
```

Add this to:
- README.md download badges
- Project website
- Social media

---

## ✅ Checklist

- [ ] Apple Developer Account active
- [ ] App Store Connect app created
- [ ] Bundle ID registered
- [ ] Distribution certificate valid
- [ ] Provisioning profile installed
- [ ] App archived and uploaded
- [ ] Beta app information complete
- [ ] External testers group created
- [ ] Build submitted for beta review
- [ ] Public link enabled
- [ ] README badges updated

---

## 🆘 Troubleshooting

### "No valid signing identity"
- Reinstall certificates from Apple Developer Portal
- Check Xcode → Preferences → Accounts

### "Provisioning profile doesn't match"
- Regenerate profile with correct App ID
- Clean build folder: Product → Clean Build Folder

### "Upload failed"
- Check internet connection
- Verify App Store Connect status
- Try Application Loader as alternative

### "Beta review rejected"
- Review rejection notes in App Store Connect
- Common issues: privacy policy, demo credentials
- Fix and resubmit

---

## 📚 Resources

- [App Store Connect Help](https://developer.apple.com/help/app-store-connect/)
- [TestFlight Documentation](https://developer.apple.com/testflight/)
- [Code Signing Guide](https://developer.apple.com/support/code-signing/)
- [Fastlane (Automation)](https://fastlane.tools/)

---

*Last updated: December 2025*
