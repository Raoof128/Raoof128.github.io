# QR-SHIELD iOS App - App Store Review Guide

## Overview

**QR-SHIELD** is a privacy-focused QR code scanner that detects phishing (QRishing) attempts in real-time using advanced heuristic analysis. Built with Kotlin Multiplatform (KMP) for cross-platform security logic.

## App Store Submission Information

### App Details
- **Bundle ID**: com.qrshield.ios (configure in Xcode)
- **Version**: 1.0.0
- **Build**: 1
- **Minimum iOS**: 17.0
- **Architectures**: arm64

### Privacy Information
- **No tracking**: The app does not track users in any way
- **No data collection**: No user data is collected or transmitted
- **Local only**: All scan history is stored locally on device
- **Camera only**: Camera access is used solely for QR code scanning
- **Photo library**: Photo access is used only to scan QR codes from saved images

### Required Permissions
1. **Camera** - Required for scanning QR codes
2. **Photo Library** - Optional, for scanning QR codes from saved images
3. **Notifications** - Optional, for security alerts

## Demo Instructions for Reviewers

### Testing the Scanner
1. Launch the app and complete onboarding (swipe through pages)
2. Grant camera permission when prompted
3. Point the camera at any QR code to scan
4. The app will analyze the URL and display:
   - **SAFE** (green) - URL appears legitimate
   - **SUSPICIOUS** (orange) - URL has some risk indicators
   - **MALICIOUS** (red) - URL appears to be a phishing attempt

### Testing with Safe URLs
Scan QR codes containing these URLs:
- `https://www.apple.com`
- `https://www.google.com`
- `https://www.github.com`

### Testing Suspicious Detection
The app will flag suspicious URLs that contain:
- Unusual character combinations
- Misleading domain names
- URL shorteners
- Non-HTTPS protocols

### Testing Features
1. **History**: Tap the History tab to view scan history
2. **Settings**: Tap Settings to configure:
   - Haptic feedback on/off
   - Sound effects on/off
   - Auto-scan on/off
   - Dark mode toggle
   - Clear history
3. **Photo Gallery**: Use the gallery button to scan QR codes from saved images
4. **Flash**: Toggle the flashlight for scanning in low light

## Technical Details

### Frameworks Used
- SwiftUI (iOS 17+ features)
- AVFoundation (Camera capture)
- Vision (QR code detection from images)
- UserNotifications (Security alerts)
- PhotosUI (Image picker)

### Architecture
- MVVM with @Observable (iOS 17+)
- Singleton view models for state persistence
- UserDefaults for settings/history storage

## Support Information

- **Email**: support@qrshield.app
- **Website**: https://qrshield.app
- **GitHub**: https://github.com/Raoof128/QDKMP-KotlinConf-2026-

## App Store Review Notes

1. This app requires a physical device with a camera for full functionality
2. In the iOS Simulator, camera features won't work but UI can be reviewed
3. The app does not require an internet connection for basic scanning
4. No login or account is required to use the app
5. The app is free with no in-app purchases

## Export Compliance

The app uses `ITSAppUsesNonExemptEncryption = false` as it:
- Does not use any custom encryption
- Only uses standard Apple frameworks for HTTPS
- Does not transmit encrypted data
