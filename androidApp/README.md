# 📱 Mehr Guard Android App

> **Jetpack Compose implementation for Android 8.0+**

---

## ✨ Features

- **Live QR Scanning** — CameraX + ML Kit for real-time QR detection
- **Gallery Import** — Scan QR codes from saved images
- **Full URL Analysis** — Same detection engine as all platforms
- **Scan History** — Persistent storage with SQLDelight
- **Dark Mode** — System-aware theming with Material 3
- **18 Languages** — Full internationalization support
- **Haptic Feedback** — Vibration on scan results
- **Beat the Bot** — Gamified phishing detection training

---

## 🚀 Quick Start

### Run from Source

```bash
# Prerequisites: Android Studio, JDK 17+
cd mehrguard

# Build and install debug APK
./gradlew :androidApp:installDebug

# Or just build APK
./gradlew :androidApp:assembleDebug
# Output: androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

### Build Release APK

```bash
# Build signed release APK
./gradlew :androidApp:assembleRelease

# Output: androidApp/build/outputs/apk/release/androidApp-release.apk
```

---

## 📋 Requirements

| Requirement | Value |
|-------------|-------|
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 35 (Android 16) |
| **Compile SDK** | 35 |
| **Kotlin** | 2.3.0 |

---

## 📁 Project Structure

```
androidApp/
├── src/main/kotlin/com/mehrguard/android/
│   ├── MainActivity.kt          ← Entry point
│   ├── MehrGuardApp.kt            ← Application class
│   ├── ui/
│   │   ├── screens/              ← Composable screens
│   │   │   ├── DashboardScreen.kt
│   │   │   ├── ScannerScreen.kt
│   │   │   ├── HistoryScreen.kt
│   │   │   ├── SettingsScreen.kt
│   │   │   └── BeatTheBotScreen.kt
│   │   ├── components/           ← Reusable UI components
│   │   ├── theme/                ← Material 3 theming
│   │   └── viewmodels/           ← ViewModels
│   └── data/                     ← Data layer
├── src/main/res/
│   ├── values/                   ← English strings
│   ├── values-ar/                ← Arabic
│   ├── values-de/                ← German
│   ├── values-es/                ← Spanish
│   ├── values-fr/                ← French
│   ├── values-hi/                ← Hindi
│   ├── values-id/                ← Indonesian
│   ├── values-it/                ← Italian
│   ├── values-ja/                ← Japanese
│   ├── values-ko/                ← Korean
│   ├── values-pt/                ← Portuguese
│   ├── values-ru/                ← Russian
│   ├── values-th/                ← Thai
│   ├── values-tr/                ← Turkish
│   ├── values-vi/                ← Vietnamese
│   ├── values-zh/                ← Chinese
│   └── xml/locales_config.xml    ← Locale configuration
└── build.gradle.kts              ← Build configuration
```

---

## 🔧 Key Dependencies

- **Jetpack Compose** — Modern declarative UI
- **CameraX** — Camera access and preview
- **ML Kit** — QR code detection and decoding
- **Material 3** — Material Design components
- **SQLDelight** — Local database (shared with KMP common)
- **Coil** — Image loading

---

## 🌍 Supported Languages (16)

| Language | Code | Status |
|----------|------|--------|
| English | en | ✅ Complete |
| Arabic | ar | ✅ Complete (RTL) |
| Chinese | zh | ✅ Complete |
| French | fr | ✅ Complete |
| German | de | ✅ Complete |
| Hindi | hi | ✅ Complete |
| Indonesian | id | ✅ Complete |
| Italian | it | ✅ Complete |
| Japanese | ja | ✅ Complete |
| Korean | ko | ✅ Complete |
| Portuguese | pt | ✅ Complete |
| Russian | ru | ✅ Complete |
| Spanish | es | ✅ Complete |
| Thai | th | ✅ Complete |
| Turkish | tr | ✅ Complete |
| Vietnamese | vi | ✅ Complete |

---

## 🧪 Testing

```bash
# Run unit tests
./gradlew :androidApp:testDebugUnitTest

# Run instrumentation tests (requires emulator/device)
./gradlew :androidApp:connectedAndroidTest

# Run shared KMP tests (Android target)
./gradlew :common:testDebugUnitTest
```

---

## 📊 Build Metrics

| Metric | Value |
|--------|-------|
| APK Size (Release) | ~29 MB |
| Min SDK | 26 |
| Target SDK | 35 |
| Version Code | 10 |
| Version Name | 1.17.34 |

---

## ⚠️ Permissions

| Permission | Purpose | Required |
|------------|---------|----------|
| `CAMERA` | QR code scanning | Yes |
| `READ_MEDIA_IMAGES` | Gallery import | Optional |
| `VIBRATE` | Haptic feedback | Optional |

---

## 📄 License

Apache 2.0 — See [LICENSE](../LICENSE) in root directory.
