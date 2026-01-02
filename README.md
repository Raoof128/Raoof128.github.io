# 🛡️ Mehr Guard

> **Offline QR & Link Security for the Privacy-Conscious**
>
> **"Mehr"** (Persian: مهر) means *trust, covenant, light* — the foundation of secure scanning.

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/KMP_Targets-5-orange" alt="5 Platforms">
  <img src="https://img.shields.io/badge/Offline-100%25-brightgreen" alt="Offline">
  <img src="https://img.shields.io/badge/Tests-1,248+-brightgreen" alt="Tests">
  <img src="https://img.shields.io/badge/F1_Score-87%25-blue" alt="F1 Score">
  <img src="https://img.shields.io/badge/License-Apache_2.0-purple" alt="License">
</p>

---

## What Is Mehr Guard?

Mehr Guard is a **privacy-first QR code and URL security scanner** built with Kotlin Multiplatform. It detects phishing attacks, brand impersonation, and malicious redirects **entirely on-device** — no cloud APIs, no data collection, no network calls.

**Core Value Proposition:**
- **100% Offline Analysis** — Your scanned URLs never leave your device
- **5 Platform Targets** — Android, iOS, Desktop (JVM), Web (JS), Web (Wasm)
- **87% F1 Score** — Ensemble ML + 25 heuristics detect real-world phishing
- **<5ms Latency** — Real-time analysis during camera scanning

---

## Quick Start

### 🌐 Try Instantly (No Build Required)

**Live Web Demo:** [raoof128.github.io](https://raoof128.github.io)

Paste any URL to test detection:
| URL | Expected Verdict |
|-----|------------------|
| `https://google.com` | ✅ SAFE |
| `https://paypa1-secure.tk/login` | 🔴 MALICIOUS — Brand impersonation |
| `https://gооgle.com` (Cyrillic о) | 🔴 MALICIOUS — Homograph attack |

### 📱 Pre-Built Android APK

**Download:** Available in [GitHub Releases](https://github.com/Raoof128/Raoof128.github.io/releases) after v2.0.36 tag is created

Install on any Android 8+ device. No build required.

---

## Build From Source

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| **JDK** | 17+ | Temurin or Corretto recommended |
| **Gradle** | 8.13 (bundled) | Uses wrapper |
| **Android Studio** | Latest | For Android builds |
| **Xcode** | 15+ | For iOS builds (macOS only) |

### Clone Repository

```bash
git clone https://github.com/Raoof128/Raoof128.github.io.git
cd Raoof128.github.io
```

### Platform Build Commands

#### Android

```bash
# Debug build (installs to connected device/emulator)
./gradlew :androidApp:installDebug

# Release APK
./gradlew :androidApp:assembleRelease
# Output: androidApp/build/outputs/apk/release/androidApp-release.apk
```

**Note:** Release builds require signing. Copy `keystore.properties.template` to `keystore.properties` and configure your keystore, or the build will use debug signing.

#### Desktop (JVM)

```bash
# Run directly
./gradlew :desktopApp:run

# Package for distribution
./gradlew :desktopApp:packageDmg        # macOS
./gradlew :desktopApp:packageMsi        # Windows
./gradlew :desktopApp:packageDeb        # Linux
```

#### iOS (macOS only)

```bash
# Step 1: Build KMP framework
./gradlew :common:linkDebugFrameworkIosSimulatorArm64

# Step 2: Open Xcode project
open iosApp/MehrGuard.xcodeproj

# Step 3: In Xcode
#   - Select iPhone 16 Pro simulator (or any iOS 17+ simulator)
#   - Press ⌘+R to build and run
```

**First-time setup:** Link the framework in Xcode:
1. Select MehrGuard target → General tab
2. Scroll to "Frameworks, Libraries, and Embedded Content"
3. Add `common/build/bin/iosSimulatorArm64/debugFramework/common.framework`
4. Set Embed to "Embed & Sign"

#### Web (Kotlin/JS)

```bash
# Development server with hot reload
./gradlew :webApp:jsBrowserDevelopmentRun
# Opens http://localhost:8080

# Production build
./gradlew :webApp:jsBrowserProductionWebpack
# Output: webApp/build/dist/js/productionExecutable/
```

#### Web (Kotlin/Wasm)

```bash
# Development server
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Production build
./gradlew :webApp:wasmJsBrowserProductionWebpack
```

### Run Tests

```bash
# All shared tests
./gradlew :common:allTests

# Platform-specific
./gradlew :common:desktopTest          # JVM tests
./gradlew :androidApp:testDebugUnitTest # Android unit tests

# Coverage report
./gradlew :common:koverXmlReport
# Output: common/build/reports/kover/xml/report.xml
```

---

## Architecture

### 5-Target Kotlin Multiplatform Structure

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Application Layer                             │
├──────────────┬──────────────┬──────────────┬──────────────┬─────────┤
│  androidApp  │   iosApp     │  desktopApp  │   webApp     │  webApp │
│  Compose UI  │  SwiftUI +   │ Compose      │  Kotlin/JS   │ Kotlin/ │
│  CameraX     │  KMP Bridge  │ Desktop      │  Browser     │ Wasm    │
│  ML Kit      │  Vision      │ ZXing        │  jsQR        │ jsQR    │
└──────┬───────┴──────┬───────┴──────┬───────┴──────┬───────┴────┬────┘
       │              │              │              │            │
       └──────────────┴──────────────┴──────────────┴────────────┘
                                     │
                                     ▼
              ┌───────────────────────────────────────────────┐
              │           common module (100% shared)         │
              │  • PhishingEngine      (orchestration)        │
              │  • EnsembleModel       (3-model ML)           │
              │  • HeuristicsEngine    (25+ rules)            │
              │  • BrandDetector       (60+ brands)           │
              │  • FeatureExtractor    (URL features)         │
              │  • SQLDelight          (scan history)         │
              └───────────────────────────────────────────────┘
```

### Code Sharing Metrics

| Component | Lines | Shared |
|-----------|-------|--------|
| Detection Engine | ~11,000 | **100%** |
| ML Models | ~1,400 | **100%** |
| Heuristics | ~2,500 | **100%** |
| Platform Code | ~12,500 | 0% |
| **Total** | ~26,000 | **52%** |

All security-critical logic lives in `common/src/commonMain/kotlin/`. Platform modules only implement camera/QR scanning and native UI.

---

## Detection Engine

### How It Works

1. **Heuristics Engine** (25+ checks)
   - Homograph detection (Cyrillic, Greek lookalikes)
   - Brand impersonation (typosquatting, fuzzy matching)
   - Suspicious TLDs (.tk, .ml, .ga, .cf)
   - IP obfuscation (decimal, hex, octal)
   - @ symbol injection, URL shorteners, credential paths

2. **Ensemble ML Model**
   - Logistic Regression (speed)
   - Gradient Boosting (accuracy)
   - Decision Rules (explainability)
   - Majority voting for final prediction

3. **Verdict Determination**
   - Each component votes: SAFE / SUSPICIOUS / MALICIOUS
   - 3+ SAFE votes → SAFE
   - 2+ MALICIOUS votes → MALICIOUS
   - Critical escalations override (homograph, @ symbol)

### Performance

| Metric | Value |
|--------|-------|
| P50 Latency | <1ms |
| P99 Latency | <5ms |
| F1 Score | 87% |
| False Positive Rate | <5% on Alexa Top 100 |

---

## Red Team Mode (For Judges)

A hidden developer mode exposes 11 curated attack scenarios for testing.

**How to Enable:**

| Platform | Activation |
|----------|------------|
| Android | Settings → 7-tap version number |
| iOS | Settings → 7-tap version number |
| Desktop | Click "🕵️ Judge Mode" in header |
| Web | Settings → Security → "Enable Red Team Scenarios" |

**Scenarios Include:**
- Cyrillic homographs (аpple.com, раypal.com)
- IP obfuscation (decimal, hex encoding)
- Suspicious TLDs (.tk, .ml with brand keywords)
- Brand impersonation (paypa1.com, netflix.secure-verify.com)
- URL shortener chains

---

## Privacy Guarantee

Mehr Guard makes **zero network calls** for analysis. Verification:

```bash
./judge/verify_offline.sh
# ✅ OFFLINE VERIFICATION PASSED
# Analyzed 27 URLs, made 0 network calls
```

The detection engine has no HTTP client dependencies. Privacy is architectural, not a policy.

---

## Internationalization

18 languages supported across all platforms:

English, German, French, Spanish, Italian, Portuguese, Russian, Chinese, Japanese, Korean, Hindi, Arabic, Thai, Vietnamese, Turkish, Indonesian, Hebrew, Persian

---

## Project Structure

```
├── common/                 # Shared KMP module (detection engine, ML, data)
│   ├── src/commonMain/     # Cross-platform Kotlin
│   ├── src/androidMain/    # Android-specific (SQLite driver)
│   ├── src/iosMain/        # iOS-specific (SQLite driver)
│   ├── src/desktopMain/    # Desktop-specific (SQLite driver)
│   ├── src/jsMain/         # JS-specific (IndexedDB driver)
│   └── src/commonTest/     # Shared tests (1,248+)
├── androidApp/             # Android application (Compose)
├── iosApp/                 # iOS application (SwiftUI + KMP)
├── desktopApp/             # Desktop application (Compose Desktop)
├── webApp/                 # Web application (Kotlin/JS + Wasm)
├── judge/                  # Verification scripts for claims
└── docs/                   # Technical documentation
```

---

## Verification Scripts

All claims are backed by reproducible evidence:

```bash
./judge/verify_all.sh        # Run all verifications
./judge/verify_offline.sh    # Prove zero network calls
./judge/verify_performance.sh # Prove <5ms latency
./judge/verify_accuracy.sh   # Prove 87% F1 score
./judge/verify_parity.sh     # Prove cross-platform parity
```

---

## Documentation

| Document | Purpose |
|----------|---------|
| [JUDGE_QUICKSTART.md](JUDGE_QUICKSTART.md) | 5-minute verification guide |
| [ESSAY.md](ESSAY.md) | Competition essay (300 words) |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design |
| [docs/SHARED_CODE_REPORT.md](docs/SHARED_CODE_REPORT.md) | KMP code sharing breakdown |
| [docs/ML_MODEL.md](docs/ML_MODEL.md) | ML architecture details |

---

## License

```
Copyright 2025-2026 Mehr Guard Contributors
Licensed under the Apache License, Version 2.0
```

See [LICENSE](LICENSE) for full text.

---

## Contact

**Author:** Mohammad Raouf Abedini  
**Email:** raoof.r12@gmail.com | mohammadraouf.abedini@students.mq.edu.au  
**University:** Macquarie University, Sydney, Australia  
**GitHub:** [@Raoof128](https://github.com/Raoof128)

---

<p align="center">
  <b>🛡️ Mehr Guard</b><br>
  <i>Scan smart. Stay protected.</i>
</p>
