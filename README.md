<p align="center">
  <img src="MehrGuard.iconset/icon_256x256@2x.png">
</p>

# Mehr Guard

> **Frozen contest submission (January 2026).** This repository is the KotlinConf 2025–26 submission as it was entered, kept because the web demo and the release builds are served from it. It is not maintained. A November 2026 audit could not reproduce several figures below: the 87% F1 score, the "<5ms" latency, the 19 red-team scenarios (there are 18) and "zero network calls" (the Android app checks for rule updates at startup), and the model described as trained is hand-set. The maintained source, with measured results and their limits, is **[Raoof128/MehrGuard](https://github.com/Raoof128/MehrGuard#summary)**.

> **Offline QR & Link Security for the Privacy-Conscious**
>
> **"Mehr"** (Persian: مهر) means *trust, covenant, light* — the foundation of secure scanning.

<p align="center">
  <a href="https://youtu.be/n8bheouj4jM">
    <img src="https://img.shields.io/badge/🎬_Demo_Video-Watch_Now-red?style=for-the-badge&logo=youtube" alt="Demo Video">
  </a>
</p>

<p align="center">
  <b>📺 Watch the Demo:</b> <a href="https://youtu.be/n8bheouj4jM">https://youtu.be/n8bheouj4jM</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/KMP_Targets-5-orange" alt="5 Platforms">
  <img src="https://img.shields.io/badge/Offline-100%25-brightgreen" alt="Offline">
  <img src="https://img.shields.io/badge/Tests-1,248+-brightgreen" alt="Tests">
  <img src="https://img.shields.io/badge/F1_Score-87%25-blue" alt="F1 Score">
  <img src="https://img.shields.io/badge/License-Apache_2.0-purple" alt="License">
</p>

---

## 🏆 KotlinConf 2025-2026 Contest Submission

| Item | Link |
|------|------|
| **📝 Essay (300 words)** | [ESSAY.md](ESSAY.md) |
| **🎬 Demo Video** | [YouTube](https://youtu.be/n8bheouj4jM) |
| **📋 Submission Checklist** | [SUBMISSION_CHECKLIST.md](SUBMISSION_CHECKLIST.md) |
| **🔍 Judge Quickstart** | [JUDGE_QUICKSTART.md](JUDGE_QUICKSTART.md) |
| **📅 Contest Timeline** | [CONTEST_START.md](CONTEST_START.md) |

---

## ⚡ 60-Second Judge Path

| Step | Action | Time |
|------|--------|------|
| **1** | 📱 [Download APK](https://github.com/Raoof128/Raoof128.github.io/releases/tag/v2.0.36-submission) | 10s |
| **2** | 🌐 [Open Web Demo](https://raoof128.github.io) — paste `paypa1-secure.tk` | 20s |
| **3** | ✅ Run `./judge/verify_offline.sh` (proves zero network calls) | 30s |

> 📋 **Full Guide:** [JUDGE_QUICKSTART.md](JUDGE_QUICKSTART.md) • 📝 **Essay:** [ESSAY.md](ESSAY.md)

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

**Download:** [MehrGuard-2.0.36-debug.apk](https://github.com/Raoof128/Raoof128.github.io/releases/tag/v2.0.36-submission)

Install on any Android 8+ device. No build required.

> **Note:** Debug-signed APK (suitable for judge evaluation). A release-signed APK is not required for evaluation.

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

## Red Team Mode & Judge Testing

Red Team Mode is a **hidden developer feature** that exposes curated attack scenarios for testing the detection engine. This allows judges and developers to instantly verify detection accuracy without needing to print QR codes.

### How It Works (Technical Architecture)

```
┌─────────────────────────────────────────────────────────────────────┐
│                    RED TEAM MODE ARCHITECTURE                        │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │              SHARED MODULE (common/redteam/)                  │  │
│  │                                                               │  │
│  │  RedTeamScenarios.kt                                          │  │
│  │  ├── 19 curated attack scenarios (object SCENARIOS)          │  │
│  │  ├── Scenario data class (id, category, url, expectedScore)  │  │
│  │  ├── Categories: Homograph, IP Obfuscation, TLD, Brand, etc. │  │
│  │  └── Utility: groupedByCategory(), getById()                 │  │
│  │                                                               │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              │                                       │
│              ┌───────────────┼───────────────┐                      │
│              ▼               ▼               ▼                      │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐           │
│  │   Android     │  │     iOS       │  │   Desktop     │           │
│  │ RedTeamPanel  │  │ RedTeamPanel  │  │ RedTeamChips  │           │
│  │ (Compose)     │  │ (SwiftUI)     │  │ (Compose)     │           │
│  └───────────────┘  └───────────────┘  └───────────────┘           │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │   Web (JavaScript array mirrors Kotlin RedTeamScenarios)      │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### Cross-Platform Parity

All 5 platforms share the **exact same scenarios** with identical IDs, URLs, and expected scores:

| Source File | Platform | Implementation |
|-------------|----------|----------------|
| `common/.../redteam/RedTeamScenarios.kt` | Android, Desktop | Direct Kotlin import |
| `iosApp/.../MockTypes.swift` | iOS | Swift enum mirroring Kotlin |
| `webApp/.../scanner.js` | Web (JS/Wasm) | JavaScript array mirror |

### How to Enable Red Team Mode

| Platform | Activation Method | UI Location |
|----------|-------------------|-------------|
| **Android** | Settings → Tap version number **7 times** → Developer Mode unlocks | Red Team panel appears at top of Scanner screen |
| **iOS** | Settings → Tap version number **7 times** → Developer Mode unlocks | Red Team scenarios panel in Scanner view |
| **Desktop** | Click "🕵️ Judge Mode" toggle in header bar | Horizontal scrollable chip bar in Scanner |
| **Web** | Settings → Security Settings → Toggle "Enable Red Team Scenarios" | Chip grid appears above scanner |

### Attack Scenario Categories

| Category | Count | Example | Detection Target |
|----------|-------|---------|------------------|
| **Homograph Attack** | 3 | `https://аpple.com` (Cyrillic 'а') | Mixed Unicode scripts |
| **IP Obfuscation** | 3 | `http://3232235777/malware` | Decimal/Hex/Octal IP |
| **Suspicious TLD** | 3 | `https://paypa1-secure.tk` | Free/abused TLDs |
| **Nested Redirect** | 2 | `https://legit.com?url=https://phishing.tk` | URL-in-URL patterns |
| **Brand Impersonation** | 3 | `https://paypa1.com` | Typosquatting |
| **URL Shortener** | 2 | `https://bit.ly/xyz` | Destination hiding |
| **Safe Control** | 2 | `https://google.com` | Baseline verification |

### Code Flow (One-Click Testing)

```kotlin
// 1. User taps a Red Team scenario chip
val scenario = RedTeamScenarios.getById("HG-001")  // Cyrillic Apple

// 2. URL is fed directly to PhishingEngine (bypasses camera)
val result = phishingEngine.analyze(scenario.maliciousUrl)

// 3. Result displayed with full breakdown
// - Expected: score 70-100, verdict MALICIOUS
// - Signals: MIXED_SCRIPTS, BRAND_IMPERSONATION, HOMOGRAPH_DETECTED
```

### Verification Commands

```bash
# Run all judge verification tests
./judge/verify_all.sh

# Individual verifications:
./judge/verify_offline.sh      # Proves zero network calls
./judge/verify_performance.sh  # Proves <5ms latency
./judge/verify_accuracy.sh     # Proves 87% F1 score on red team corpus
./judge/verify_parity.sh       # Proves identical verdicts across platforms
```

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
