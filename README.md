# 🛡️ QR-SHIELD

> **Offline QR Phishing Detection** — Protect users from QRishing attacks with on-device ML, zero data collection, and cross-platform deployment.

<p align="center">
  <a href="https://raoof128.github.io"><img src="https://img.shields.io/badge/🌐_Live_Demo-raoof128.github.io-4F8BFF?style=for-the-badge" alt="Live Demo"></a>
  <a href="releases/QRShield-1.1.0-release.apk"><img src="https://img.shields.io/badge/📱_Android-Download_APK-3DDC84?style=for-the-badge" alt="Download APK"></a>
  <a href="JUDGE_QUICKSTART.md"><img src="https://img.shields.io/badge/🏆_Judges-Quick_Start-gold?style=for-the-badge" alt="Judge Quick Start"></a>
</p>

<!-- Competition Badges -->
<p align="center">
  <img src="https://img.shields.io/badge/KotlinConf-2025--2026-7F52FF?logo=kotlin&logoColor=white" alt="Contest">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF" alt="Kotlin">
  <img src="https://img.shields.io/badge/KMP_Targets-5-orange" alt="Platforms">
  <img src="https://img.shields.io/badge/Offline-100%25-brightgreen" alt="Offline">
  <img src="https://img.shields.io/badge/Privacy-Zero_Data-blue" alt="Privacy">
  <img src="https://img.shields.io/badge/Tests-1,248+-brightgreen" alt="Tests">
  <img src="https://img.shields.io/badge/Coverage-89%25-brightgreen" alt="Coverage">
  <img src="https://img.shields.io/badge/F1_Score-87.1%25-brightgreen" alt="F1 Score">
  <img src="https://img.shields.io/badge/License-Apache_2.0-purple" alt="License">
</p>

---

## ⚡ 10-Second Summary

| Step | Action | Time |
|------|--------|------|
| 📷 **Scan** | Point camera at any QR code | 0ms |
| 🧠 **Analyze** | 25+ heuristics + 3-model ML ensemble | <5ms |
| ✅ **Verdict** | SAFE / SUSPICIOUS / MALICIOUS with reasons | Instant |

**No cloud. No network. No data collection. Ever.**

---

## 🎯 The Problem We Solve

**QRishing attacks increased 587% since 2023.** Users scan parking meters, restaurant menus, payment terminals—trusting the code without checking the URL. When they land on `paypa1-secure.tk`, it's already too late.

**Existing solutions fail because:**
- 🔴 Cloud scanners log every URL you scan (privacy nightmare)
- 🔴 Network-dependent tools don't work in parking garages or planes
- 🔴 Generic "link checkers" miss QR-specific attack patterns

**QR-SHIELD is different:**
- ✅ **100% offline** — URL never leaves your device
- ✅ **Privacy-first architecture** — Can't leak what we don't transmit
- ✅ **<5ms analysis** — Real-time during scanning
- ✅ **Explainable verdicts** — Shows *why* something is risky

---

## 🚀 Try It Now

### 🌐 Web Demo (Instant)
**[👉 raoof128.github.io](https://raoof128.github.io)** — Works in any browser, no install needed

### 📱 All 5 Platforms

| Platform | Target | Download |
|----------|--------|----------|
| **Android** | `androidTarget()` | [📥 Download APK](releases/QRShield-1.1.0-release.apk) |
| **iOS** | `iosArm64/iosX64/iosSimulatorArm64` | `./scripts/run_ios_simulator.sh` |
| **Desktop** | `jvm("desktop")` | `./gradlew :desktopApp:run` |
| **Web (JS)** | `js(IR) { browser {} }` | [raoof128.github.io](https://raoof128.github.io) |
| **Web (Wasm)** | `wasmJs { browser {} }` | `./gradlew :webApp:wasmJsBrowserRun` |

### 🧪 Test These URLs

| URL | Expected Result |
|-----|-----------------|
| `https://paypa1-secure.tk/login` | 🔴 **MALICIOUS** — Brand impersonation + risky TLD |
| `https://google.com` | 🟢 **SAFE** — No threats detected |
| `https://gооgle.com` (Cyrillic 'о') | 🔴 **MALICIOUS** — Homograph attack |
| `https://192.168.1.1/login` | ⚠️ **SUSPICIOUS** — IP-based URL |

---

## 🏗️ Architecture — True 5-Platform KMP

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              5 Platform Apps                                      │
├────────────────┬─────────────┬──────────────┬─────────────┬────────────────────┤
│   androidApp   │   iosApp    │  desktopApp  │  webApp (JS)│  webApp (Wasm)     │
│   Compose UI   │   SwiftUI   │  Compose UI  │  Kotlin/JS  │  Kotlin/WasmJS     │
│   CameraX      │ AVFoundation│    ZXing     │   jsQR      │    jsQR            │
│   ML Kit       │   Vision    │              │             │                    │
└───────┬────────┴──────┬──────┴───────┬──────┴──────┬──────┴─────────┬──────────┘
        │               │              │             │                │
        └───────────────┴──────────────┴─────────────┴────────────────┘
                                       │
                                       ▼
              ┌─────────────────────────────────────────────────────────┐
              │            common (100% Shared Kotlin)                  │
              ├─────────────────────────────────────────────────────────┤
              │  🔧 PhishingEngine      — Main orchestrator             │
              │  🧠 EnsembleModel       — 3-model ML architecture       │
              │  🔍 HeuristicsEngine    — 25+ detection rules           │
              │  🏷️ BrandDetector       — 500+ brands + dynamic         │
              │  ✂️ FeatureExtractor    — URL feature engineering       │
              │  📊 SharedViewModel     — Cross-platform state          │
              │  🎨 CommonBrainVisualizer— Shared Compose UI            │
              └─────────────────────────────────────────────────────────┘
```

### 📊 Code Sharing Metrics

| Category | Lines of Code | Shared % |
|----------|---------------|----------|
| **Business Logic** | ~11,000 | **100%** |
| **ML Engine** | ~1,400 | **100%** |
| **Detection Engine** | ~2,500 | **100%** |
| **UI Components** | ~3,000 | **80%** |
| **Platform-Specific** | ~12,500 | 0% |
| **Total Codebase** | ~26,000 | **~52%** |

> **Key insight:** All security-critical code is shared. Platform code is only for camera access and native UI polish.

---

## 🧠 Detection Engine

### Ensemble ML Architecture

```kotlin
class EnsembleModel {
    private val logisticRegression = LogisticRegressionModel()  // Speed
    private val gradientBoosting = GradientBoostingModel()      // Accuracy
    private val decisionRules = RuleBasedModel()                // Explainability
    
    fun predict(features: FloatArray): Prediction {
        val votes = listOf(
            logisticRegression.predict(features),
            gradientBoosting.predict(features),
            decisionRules.predict(features)
        )
        return Prediction.fromVotes(votes)  // Majority voting
    }
}
```

### 25+ Heuristics

| Attack Type | Detection Method |
|-------------|------------------|
| **Homograph Attacks** | Unicode script mixing (Cyrillic 'а', Greek 'ο') |
| **Typosquatting** | Levenshtein distance for 500+ brands |
| **Brand Impersonation** | Fuzzy matching + dynamic discovery |
| **Suspicious TLDs** | `.tk`, `.ml`, `.ga`, `.cf` (free, abused) |
| **IP-Based URLs** | Standard + obfuscated (octal, hex) |
| **@ Symbol Injection** | `https://google.com@evil.com` patterns |
| **URL Shorteners** | bit.ly, tinyurl flagged for review |
| **Credential Paths** | `/login`, `/signin`, `/account` keywords |
| **RTL Override** | Right-to-left character attacks |
| **Zero-Width Injection** | Hidden characters in URLs |

### Performance

| Metric | Value | Comparison |
|--------|-------|------------|
| **P50 Latency** | <1ms | 200x faster than Google Safe Browsing |
| **P99 Latency** | <5ms | 100x faster than VirusTotal |
| **Memory** | <50MB | Suitable for low-end devices |
| **Bundle Size** | ~2MB (native), ~200KB (web) | Fully offline |

---

## 📊 Accuracy Metrics

| Metric | QR-SHIELD | Target |
|--------|-----------|--------|
| **F1 Score** | 87.1% | 85% |
| **Recall** | 89.1% | 85% |
| **Precision** | 85.2% | 85% |
| **False Positive Rate** | 0% MALICIOUS on Alexa Top 100 | 0% |

### Test Coverage

| Category | Count |
|----------|-------|
| **Unit Tests** | 1,248+ |
| **Architecture Tests** | 9 (Konsist) |
| **Performance Tests** | 15+ with P99 thresholds |
| **Platform Parity Tests** | 5 (all targets identical) |
| **Red Team Corpus** | 60+ adversarial URLs |

---

## 🔒 Privacy Guarantee

| What Cloud Scanners Know | What QR-SHIELD Knows |
|--------------------------|----------------------|
| Which banks you use | **Nothing** |
| Which doctors you visit | **Nothing** |
| Which lawyers you consult | **Nothing** |
| Your location patterns | **Nothing** |

**Why?** We can't leak what we never transmit. Privacy is the **architecture**, not a feature.

### Verification

```bash
# Prove zero network calls
./judge/verify_offline.sh

# Expected output:
# ✅ OFFLINE VERIFICATION PASSED
# Analyzed 27 URLs, made 0 network calls
```

---

## 🌍 Internationalization

**16 Languages** — Reaching 4+ billion speakers worldwide

| Region | Languages |
|--------|-----------|
| 🇬🇧 🇺🇸 | English |
| 🇩🇪 🇦🇹 🇨🇭 | German |
| 🇫🇷 | French |
| 🇪🇸 🇲🇽 | Spanish |
| 🇮🇹 | Italian |
| 🇵🇹 🇧🇷 | Portuguese |
| 🇷🇺 | Russian |
| 🇨🇳 | Chinese (Simplified) |
| 🇯🇵 | Japanese |
| 🇰🇷 | Korean |
| 🇮🇳 | Hindi |
| 🇸🇦 🇦🇪 | Arabic |
| 🇹🇭 | Thai |
| 🇻🇳 | Vietnamese |
| 🇹🇷 | Turkish |
| 🇮🇩 | Indonesian |

---

## 🏆 Competition Compliance

| Criterion | Status | Evidence |
|-----------|--------|----------|
| ✅ Original work | Dec 5-25, 2025 | Git commit history |
| ✅ Apache 2.0 license | [LICENSE](LICENSE) | Full text included |
| ✅ Public repository | GitHub | [Raoof128/Raoof128.github.io](https://github.com/Raoof128/Raoof128.github.io) |
| ✅ Kotlin Multiplatform | **5 targets** | Android, iOS, Desktop, JS, Wasm |
| ✅ README documentation | This file | + [JUDGE_QUICKSTART.md](JUDGE_QUICKSTART.md) |
| ✅ Competition essay | [ESSAY_SUBMISSION.md](ESSAY_SUBMISSION.md) | ~950 words |
| ✅ Static analysis | Detekt | Zero-tolerance (no baseline) |
| ✅ Test coverage | 89% | 1,248+ tests |

---

## 🧑‍⚖️ Judge Quick Verification

```bash
# One command to verify all claims
./judge/verify_all.sh

# Individual verifications
./judge/verify_offline.sh     # Zero network calls
./judge/verify_performance.sh # <5ms P50 latency
./judge/verify_accuracy.sh    # 87% F1 score
./judge/verify_parity.sh      # Identical on all platforms
```

📖 **[Full Evidence Pack →](docs/EVIDENCE.md)** — Every claim linked to artifacts

---

## 📚 Documentation

### For Judges
| Document | Purpose |
|----------|---------|
| **[JUDGE_QUICKSTART.md](JUDGE_QUICKSTART.md)** | 5-minute verification guide |
| **[ESSAY_SUBMISSION.md](ESSAY_SUBMISSION.md)** | Competition essay (~950 words) |
| **[docs/EVIDENCE.md](docs/EVIDENCE.md)** | Claims linked to artifacts |
| **[SUBMISSION_CHECKLIST.md](SUBMISSION_CHECKLIST.md)** | Rule compliance checklist |

### Technical
| Document | Purpose |
|----------|---------|
| **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** | System design |
| **[docs/ML_MODEL.md](docs/ML_MODEL.md)** | ML architecture |
| **[docs/THREAT_MODEL.md](docs/THREAT_MODEL.md)** | Security analysis |
| **[docs/API.md](docs/API.md)** | Developer reference |
| **[docs/SHARED_CODE_REPORT.md](docs/SHARED_CODE_REPORT.md)** | KMP code sharing breakdown |

### Platform Guides
| Document | Purpose |
|----------|---------|
| **[androidApp/README.md](androidApp/README.md)** | Android app guide |
| **[iosApp/INTEGRATION_GUIDE.md](iosApp/INTEGRATION_GUIDE.md)** | iOS integration |
| **[desktopApp/README.md](desktopApp/README.md)** | Desktop app guide |
| **[docs/ICON_INTEGRATION.md](docs/ICON_INTEGRATION.md)** | Icon setup for all platforms |

---

## 🛠️ Build & Run

### Prerequisites
- **JDK 17+** (Temurin/Corretto recommended)
- **Android Studio** (for Android builds)
- **Xcode 15+** (for iOS, macOS only)

### Quick Start

```bash
# Clone
git clone https://github.com/Raoof128/Raoof128.github.io.git
cd Raoof128.github.io

# Run tests
./gradlew test

# Run Android
./gradlew :androidApp:installDebug

# Run Desktop
./gradlew :desktopApp:run

# Run Web (JS)
./gradlew :webApp:jsBrowserDevelopmentRun

# Run Web (Wasm)
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

---

## 📄 License

```
Copyright 2025-2026 QR-SHIELD Contributors
Licensed under the Apache License, Version 2.0
```

See [LICENSE](LICENSE) for full text.

---

<p align="center">
  <img src="qr-shield-iconset/QR-SHIELD.iconset/icon_128x128.png" alt="QR-SHIELD Icon" width="64">
  <br><br>
  <b>🛡️ QR-SHIELD</b><br>
  <i>Kotlin Multiplatform • 5 Platforms • 100% Offline • 87% F1 Score</i><br><br>
  <b>Scan smart. Stay protected.</b>
</p>
