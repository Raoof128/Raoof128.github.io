# 📱 Platform Parity & Known Limitations

> Feature comparison across Android, iOS, Desktop, and Web platforms.

---

## ✅ Feature Parity Table

### Core Detection Features (Shared)

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| **URL Analysis** | ✅ | ✅ | ✅ | ✅ | 100% shared via PhishingEngine |
| **25+ Heuristics** | ✅ | ✅ | ✅ | ✅ | HeuristicsEngine in commonMain |
| **ML Scoring** | ✅ | ✅ | ✅ | ✅ | LogisticRegressionModel shared |
| **Brand Detection (500+)** | ✅ | ✅ | ✅ | ✅ | BrandDetector in commonMain |
| **Typosquat Detection** | ✅ | ✅ | ✅ | ✅ | Levenshtein + fuzzy matching |
| **Homograph Detection** | ✅ | ✅ | ✅ | ✅ | Punycode analysis |
| **Risk Score (0-100)** | ✅ | ✅ | ✅ | ✅ | Identical scoring algorithm |
| **Verdict (SAFE/SUSPICIOUS/MALICIOUS)** | ✅ | ✅ | ✅ | ✅ | Same thresholds all platforms |
| **Explainable Signals** | ✅ | ✅ | ✅ | ✅ | "Why flagged?" details |
| **Counterfactual Hints** | ✅ | ✅ | ✅ | ✅ | "What would reduce risk?" |

### QR Scanning Features

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| **Camera QR Scanning** | ✅ | ✅ | ✅ | ✅ | Platform-native APIs |
| **Image Upload Scanning** | ✅ | ✅ | ✅ | ✅ | File picker + QR decode |
| **Gallery Photo Picker** | ✅ | ✅ | ⚠️ | ✅ | Desktop uses file dialog |
| **Drag & Drop QR Image** | ❌ | ❌ | ✅ | ✅ | Desktop/Web only |
| **Real-time Camera Preview** | ✅ | ✅ | ✅ | ✅ | ML Kit/AVFoundation/ZXing/jsQR |
| **Flash/Torch Control** | ✅ | ✅ | ❌ | ❌ | Mobile only |

### UI/UX Features

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| **Dark Mode** | ✅ | ✅ | ✅ | ✅ | System-aware theme |
| **Light Mode** | ✅ | ✅ | ✅ | ✅ | System-aware theme |
| **Haptic Feedback** | ✅ | ✅ | ❌ | ❌ | Mobile only |
| **Sound Effects** | ✅ | ✅ | ✅ | ⚠️ | Web requires user interaction |
| **Onboarding Tutorial** | ✅ | ✅ | ⚠️ | ✅ | Desktop minimal |
| **Result Animations** | ✅ | ✅ | ✅ | ✅ | Score ring animation |
| **Glassmorphism UI** | ✅ | ✅ | ✅ | ✅ | Consistent premium look |

### History & Storage

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| **Scan History** | ✅ | ✅ | ✅ | ✅ | SQLite on native, localStorage on web |
| **History Persistence** | ✅ | ✅ | ✅ | ✅ | Survives app restart |
| **Clear History** | ✅ | ✅ | ✅ | ✅ | With confirmation dialog |
| **History Search** | ✅ | ✅ | ⚠️ | ❌ | Mobile/Desktop only |
| **Export History** | ❌ | ❌ | ❌ | ❌ | Future roadmap |

### Accessibility

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| **Screen Reader Support** | ✅ | ✅ | ⚠️ | ✅ | TalkBack/VoiceOver tested |
| **Dynamic Text Scaling** | ✅ | ✅ | ⚠️ | ✅ | Respects system settings |
| **Keyboard Navigation** | ⚠️ | ⚠️ | ✅ | ✅ | Full keyboard on Desktop/Web |
| **Reduced Motion** | ✅ | ✅ | ⚠️ | ✅ | Respects prefers-reduced-motion |
| **High Contrast Mode** | ✅ | ✅ | ✅ | ✅ | WCAG 2.1 AA compliant |

### Platform-Specific Features

| Feature | Android | iOS | Desktop | Web | Notes |
|---------|:-------:|:---:|:-------:|:---:|-------|
| **Home Screen Widget** | ✅ | ❌ | ❌ | ❌ | Glance widget (Android 12+) |
| **Share Extension** | ✅ | ⚠️ | ❌ | ❌ | iOS extension pending |
| **Menu Bar App** | ❌ | ❌ | ⚠️ | ❌ | macOS future |
| **PWA Install** | ❌ | ❌ | ❌ | ✅ | Web only |
| **Offline Mode** | ✅ | ✅ | ✅ | ✅ | 100% offline on all platforms |

### Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Fully implemented and tested |
| ⚠️ | Partial implementation or planned |
| ❌ | Not applicable or not planned |

---

## ⚠️ Known Limitations

> Honesty is important. These are the current limitations of QR-SHIELD.

### Detection Limitations

| Limitation | Impact | Mitigation |
|------------|--------|------------|
| **Novel Domains** | Cannot detect brand-new phishing domains with no history | Conservative scoring + user warnings |
| **URL Shorteners** | Cannot expand shortened URLs without network (privacy-first) | Flag as suspicious, recommend caution |
| **Content Analysis** | Does not analyze destination page content | Future: optional page preview API |
| **Language Bias** | Brand database is English-focused (AU/US/UK) | Expanding to other markets |
| **Sophisticated Spear-Phishing** | May miss highly targeted attacks | Combined with user education |

### Platform Limitations

| Platform | Limitation | Workaround |
|----------|------------|------------|
| **iOS** | No home screen widget (unlike Android) | App icon badge (future) |
| **Desktop** | No system tray quick-scan | Keyboard shortcut support |
| **Web** | Camera may be laggy on older devices | Upload image fallback prominent |
| **Web** | No persistent background service | Service Worker for PWA caching |
| **Android** | Requires Android 8.0+ (API 26) | Covers 95%+ of active devices |
| **iOS** | Requires iOS 17+ | Covers ~80% of active devices |

### False Positive Scenarios

| Scenario | Why It Happens | User Action |
|----------|---------------|-------------|
| New legitimate startup domains | Unknown TLDs, new registrations | "Proceed Anyway" with warning |
| Internal company URLs | May match brand patterns | Whitelist feature (future) |
| Legitimate shorteners | bit.ly/t.co flagged as suspicious | Expand URL before clicking |
| Development/staging URLs | Unusual ports, IP addresses | Expected for dev environments |

### Performance Limitations

| Metric | Current | Target | Notes |
|--------|---------|--------|-------|
| Analysis time | <50ms | <20ms | Already very fast |
| App cold start | ~1.5s | <1s | Android cold start optimization |
| Web bundle size | ~2MB | <1MB | Tree-shaking improvements |
| Memory footprint | ~50MB | ~30MB | Brand database optimization |

---

## 🧪 Smoke Test Matrix

> Quick tests to verify each platform works correctly.

### Test URLs

```bash
# SAFE - Should pass
https://google.com
https://github.com
https://apple.com

# MALICIOUS - Should fail
https://paypa1-secure.tk/login
https://gооgle.com/login
https://commbank.secure-verify.ml/account

# SUSPICIOUS - Should warn
https://bit.ly/abc123
https://192.168.1.1:8080/admin
```

### Platform Smoke Tests

#### Android
```bash
# Build and install
./gradlew :androidApp:installDebug

# Run instrumentation tests
./gradlew :androidApp:connectedAndroidTest

# Manual check: Open app → Paste URL → Verify verdict
```

#### iOS (macOS required)
```bash
# Build for simulator
./gradlew :common:linkDebugFrameworkIosSimulatorArm64

# Open in Xcode and run
open iosApp/QRShield.xcodeproj

# Manual check: Open app → Paste URL → Verify verdict
```

#### Desktop
```bash
# Run directly
./gradlew :desktopApp:run

# Run tests
./gradlew :desktopApp:desktopTest

# Manual check: Paste URL → Verify verdict
```

#### Web
```bash
# Start dev server
cd webApp && npm run dev

# Or visit production
open https://raoof128.github.io

# Manual check: Paste URL → Verify verdict
```

### CI Smoke Test Script

```bash
#!/bin/bash
# scripts/smoke_test.sh

echo "🧪 Running QR-SHIELD Smoke Tests..."

# Test common module (covers all detection logic)
echo "Testing common module..."
./gradlew :common:desktopTest --quiet

if [ $? -eq 0 ]; then
    echo "✅ Common module tests passed"
else
    echo "❌ Common module tests failed"
    exit 1
fi

# Test desktop app (JVM verification)
echo "Testing desktop app..."
./gradlew :desktopApp:desktopTest --quiet

if [ $? -eq 0 ]; then
    echo "✅ Desktop app tests passed"
else
    echo "❌ Desktop app tests failed"
    exit 1
fi

echo "✅ All smoke tests passed!"
```

---

## 📊 Test Coverage by Platform

| Platform | Unit Tests | Integration Tests | UI Tests | Coverage |
|----------|:----------:|:-----------------:|:--------:|:--------:|
| **Common** | 849 | N/A | N/A | 89% |
| **Android** | 50+ | 20+ | 10+ | ~75% |
| **iOS** | Via Common | Manual | Manual | ~70%* |
| **Desktop** | Via Common | 10+ | Manual | ~80% |
| **Web** | Via Common | Manual | Manual | ~70%* |

*iOS and Web primarily tested through shared common module tests.

---

## 🔄 Version Compatibility

| Platform | Minimum | Recommended | Latest Tested |
|----------|---------|-------------|---------------|
| **Android** | 8.0 (API 26) | 12+ (API 31) | 15 (API 35) |
| **iOS** | 17.0 | 17.0+ | 18.2 |
| **Desktop** | JVM 17 | JVM 21 | JVM 21 |
| **Web** | Chrome 90+ | Latest | Chrome 131 |

---

*Last updated: December 2025*
