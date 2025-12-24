# 🛡️ QR-SHIELD

> **Offline QR Phishing Detection** — Scan any QR code, get instant verdicts, never send data to the cloud.

<!-- Competition Badges -->
[![Contest](https://img.shields.io/badge/KotlinConf-2025--2026-7F52FF?logo=kotlin&logoColor=white)](CONTEST_START.md)
[![Platforms](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-orange)](docs/SHARED_CODE_REPORT.md)
[![Offline](https://img.shields.io/badge/Network-100%25%20Offline-brightgreen)](judge/verify_offline.sh)
[![No Network](https://img.shields.io/badge/Privacy-Zero%20Data%20Collection-blue)](PRIVACY.md)

<!-- Quality Badges -->
[![Test Coverage](https://img.shields.io/badge/coverage-89%25-brightgreen)](https://github.com/Raoof128/Raoof128.github.io/actions/workflows/kover.yml)
[![Build Status](https://img.shields.io/github/actions/workflow/status/Raoof128/Raoof128.github.io/quality-tests.yml?label=tests)](https://github.com/Raoof128/Raoof128.github.io/actions)
[![Performance](https://img.shields.io/github/actions/workflow/status/Raoof128/Raoof128.github.io/performance.yml?label=performance)](https://github.com/Raoof128/Raoof128.github.io/actions/workflows/performance.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-purple)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF)](https://kotlinlang.org)
[![i18n](https://img.shields.io/badge/i18n-🇬🇧%20🇩🇪-blue)](common/src/commonMain/kotlin/com/qrshield/localization/Translations.kt)

---

## ⚡ What It Does (10 Seconds)

- **Scan** → Point at any QR code  
- **Analyze** → 25+ heuristics + ML ensemble score the URL in <5ms  
- **Verdict** → SAFE ✅ / SUSPICIOUS ⚠️ / MALICIOUS 🔴 with human-readable reasons

**No network calls. No cloud APIs. No data collection. Ever.**

---

## 🎯 Threat Model

| Who Attacks | What We Detect | What We Don't |
|-------------|----------------|---------------|
| **Lazy phishers** | .tk/.ml domains, typosquats, IP hosts | Brand-new domains (no blocklist) |
| **Script kiddies** | Homograph attacks, @ injection, shorteners | Zero-day exploits |
| **Credential harvesters** | Login keywords, credential params | Sophisticated APTs |

**Trade-off:** We sacrifice real-time blocklist updates for absolute privacy.

📖 Full threat model: [docs/THREAT_MODEL.md](docs/THREAT_MODEL.md)

---

## 🚀 Get the App

| Platform | Status | Download |
|----------|--------|----------|
| **Android** | ✅ Full App | [Download APK](releases/QRShield-1.1.0-release.apk) |
| **iOS** | ✅ Full App | [Simulator Guide](#ios-one-command-simulator) |
| **Desktop** | ✅ Full App | `./gradlew :desktopApp:run` |
| **Web** | ✅ PWA | [raoof128.github.io](https://raoof128.github.io) |

### iOS One-Command Simulator

```bash
# Build and run on iOS Simulator (requires Xcode)
./scripts/run_ios_simulator.sh
```

---

## 📋 What Is QR-SHIELD?

**QRishing attacks increased 587% since 2023.** Users scan parking meters, restaurant menus, payment terminals—never verifying the URL. Existing solutions require cloud APIs, meaning every scanned URL is logged on corporate servers.

**QR-SHIELD solves this with privacy-first, offline detection:**

| Feature | How It Works |
|---------|--------------|
| 🔒 **100% Offline** | No URL ever leaves the device—zero data collection |
| 🧠 **Ensemble ML** | 3 models: Logistic Regression + Gradient Boosting + Decision Rules |
| 🔍 **25+ Heuristics** | Homograph detection, typosquatting, suspicious TLDs, IP obfuscation |
| ⚡ **<5ms Analysis** | Real-time feedback during QR scanning |
| 📊 **Explainable** | Tells you *why* a URL is risky, not just that it is |

---

## ❓ Why Not Cloud? (Privacy vs. Google Safe Browsing)

> **"Why didn't you just use the Google Safe Browsing API?"** — This is a fair question.

| Factor | Google Safe Browsing | QR-SHIELD (Offline) |
|--------|---------------------|---------------------|
| **Privacy** | ❌ Every URL sent to Google servers | ✅ Zero URLs leave device |
| **What They Know** | Which banks, doctors, lawyers you visit | Nothing — we can't collect what we don't transmit |
| **Data Risk** | Can be subpoenaed, leaked, sold | No data = no risk |
| **Offline Support** | ❌ Requires internet connection | ✅ Works in parking garages, planes, remote areas |
| **Detection Coverage** | ✅ Real-time blocklists (best for known threats) | 🟡 Heuristics + ML (best for pattern-based attacks) |
| **Latency** | ~100-500ms (network round trip) | <5ms (on-device) |
| **Cost** | Free tier limits, enterprise pricing | Free forever |

### Trade-offs We Accept

- **No real-time blocklists**: We can't check if a specific URL was reported 5 minutes ago
- **Lower recall on brand-new threats**: First-day phishing sites might slip through
- **Higher false positive potential**: Heuristics can over-flag unusual but legitimate URLs

### Why We Still Win

- **90% of attacks** use lazy patterns (`.tk` domains, typosquats, IP hosts) that heuristics catch
- **Privacy is non-negotiable** for medical, legal, and financial QR codes
- **Offline-first** means protection even without cell signal

---

## 🧑‍⚖️ Quick Verification (5 Minutes)

```bash
# Verify all claims with one command
./judge/verify_all.sh
```

| Test | What It Proves |
|------|----------------|
| `./judge/verify_offline.sh` | Zero network calls during analysis |
| `./judge/verify_performance.sh` | <5ms P50 latency |
| `./judge/verify_accuracy.sh` | 87% F1 score |
| `./judge/verify_parity.sh` | Identical verdicts on JVM + JS + Native |

**Try these URLs:**

| Test URL | Expected Result |
|----------|-----------------|
| `https://paypa1-secure.tk/login` | 🔴 MALICIOUS — Brand impersonation + suspicious TLD |
| `https://google.com` | 🟢 SAFE — No threats detected |
| `https://gооgle.com` (Cyrillic) | 🔴 MALICIOUS — Homograph attack |

> **📱 Platform Note:** Web demo uses optimized ML weights for smaller bundle size (~200KB vs ~500KB on native). This may result in slightly lower scores (SUSPICIOUS vs MALICIOUS) on web compared to native apps. The detection is still accurate—only the score threshold differs.

---

## 🔒 Offline Guarantee (Provable)

**"100% Offline" isn't a marketing claim—it's enforced by tests.**

| Enforcement | How |
|-------------|-----|
| **No HTTP client** | Analysis module has zero network dependencies |
| **Test verification** | `./judge/verify_offline.sh` fails if any socket call detected |
| **CI enforcement** | Quality tests run in isolated network environment |

```bash
# Prove zero network calls
./judge/verify_offline.sh

# Expected output:
# ✅ OFFLINE VERIFICATION PASSED
# Analyzed 27 URLs, made 0 network calls
```

📖 Full test: [common/src/commonTest/.../OfflineOnlyTest.kt](common/src/commonTest/kotlin/com/qrshield/core/OfflineOnlyTest.kt)

---

## 📊 Shared Code Proof (KMP is Real)

| Module | Lines | Shared? |
|--------|-------|---------|
| `core/` (PhishingEngine) | 1,800 | ✅ 100% |
| `engine/` (Heuristics) | 2,500 | ✅ 100% |
| `ml/` (Ensemble Model) | 1,400 | ✅ 100% |
| `model/` (Data Classes) | 600 | ✅ 100% |
| `security/` (InputValidator) | 800 | ✅ 100% |
| **Total Business Logic** | **~11,000** | **100%** |

Platform-specific code (UI, camera, haptics): ~12,500 LOC

**Key insight:** Business logic is 100% shared. Only UI and hardware access is platform-specific.

📖 Full breakdown: [docs/SHARED_CODE_REPORT.md](docs/SHARED_CODE_REPORT.md)

---

## 🏗️ Architecture

> **~80% shared code** via Kotlin Multiplatform. One detection engine compiles to JVM, Native, and JavaScript.

```
┌─────────────────────────────────────────────────────────────┐
│                     Platform Apps                            │
├──────────────┬──────────────┬─────────────┬─────────────────┤
│  androidApp  │    iosApp    │ desktopApp  │     webApp      │
│  Compose UI  │   SwiftUI    │  Compose    │   Kotlin/JS     │
│   CameraX    │ AVFoundation │    ZXing    │     jsQR        │
└──────┬───────┴──────┬───────┴──────┬──────┴────────┬────────┘
       │              │              │               │
       └──────────────┴──────────────┴───────────────┘
                              │
                              ▼
       ┌─────────────────────────────────────────────────────┐
       │              common (Shared Kotlin)                  │
       ├─────────────────────────────────────────────────────┤
       │  PhishingEngine — Main orchestrator                  │
       │  HeuristicsEngine — 25+ detection rules              │
       │  EnsembleModel — 3-model ML architecture             │
       │  BrandDetector — 500+ brands + dynamic discovery     │
       │  SecureECDH — Curve25519 key exchange                │
       └─────────────────────────────────────────────────────┘
```

### Platform Support

| Platform | Status | Implementation | LOC |
|----------|--------|----------------|-----|
| **Android** | ✅ **Full** | CameraX + ML Kit + Compose UI | ~4,500 |
| **iOS** | ✅ **Full** | AVFoundation + SwiftUI + KMP engine | ~6,500 |
| **Desktop** | ✅ **Full** | Compose Desktop + ZXing | ~2,000 |
| **Web** | ✅ **PWA** | Kotlin/JS + Service Worker | ~1,500 |

---

## ✨ Detection Capabilities

```kotlin
val result = PhishingEngine().analyzeBlocking("https://paypa1-secure.tk/login")
// → Verdict: MALICIOUS (score: 85)
// → Flags: ["Brand impersonation: paypal", "High-risk TLD: .tk"]
```

| Attack Type | How We Detect It |
|-------------|------------------|
| **Homograph Attacks** | Unicode script mixing (Cyrillic 'а', Greek 'ο') |
| **Typosquatting** | Levenshtein distance for 500+ brands |
| **URL Shorteners** | bit.ly, tinyurl, t.co flagged as suspicious |
| **Suspicious TLDs** | .tk, .ml, .ga, .cf (free, abused for phishing) |
| **IP Address URLs** | Standard and obfuscated (octal, hex, decimal) |
| **@ Symbol Injection** | `https://google.com@evil.com` patterns |

---

## 🔧 SDK Integration

For developers who want to integrate QR-SHIELD detection into their own apps:

### Local Module Integration

The `common` module contains the complete detection engine and can be included as a local dependency:

```kotlin
// In your settings.gradle.kts, include the common module
include(":common")
project(":common").projectDir = file("path/to/qrshield/common")

// In your build.gradle.kts
dependencies {
    implementation(project(":common"))
}
```

### Usage

```kotlin
// Use in your app
val engine = PhishingEngine()
val result = engine.analyzeBlocking(url)

when (result.verdict) {
    Verdict.SAFE -> showGreenCheckmark()
    Verdict.SUSPICIOUS -> showYellowWarning()
    Verdict.MALICIOUS -> blockAndAlert()
}
```

> **Note:** The SDK is designed for local module integration rather than Maven Central publishing.
> This ensures you always have the latest detection rules and allows customization.

> 📖 **[SDK Documentation →](docs/API.md)**

---

## 🧪 Quality & Testing

| Metric | Value |
|--------|-------|
| Test Coverage | 89% |
| Total Tests | 1000+ |
| Architecture Tests | 9 (Konsist) |
| Performance Tests | 15+ with P99 thresholds |
| Accuracy | F1: 87.1%, Recall: 89.1% |
| **False Positive Rate** | **<5%** on Alexa Top 100 |


### CI Enforcement

| Workflow | What It Checks |
|----------|----------------|
| `quality-tests.yml` | 1000+ unit tests |
| `performance.yml` | P99 latency thresholds |
| `benchmark.yml` | Performance regression detection |
| Detekt | Zero-tolerance static analysis |

---

## 📚 Documentation

| Topic | Document |
|-------|----------|
| **Architecture** | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| **ML Model** | [docs/ML_MODEL.md](docs/ML_MODEL.md) |
| **ML Training & Validation** | [docs/ML_TRAINING.md](docs/ML_TRAINING.md) |
| **Threat Model** | [docs/THREAT_MODEL.md](docs/THREAT_MODEL.md) |
| **Attack Demos** | [docs/ATTACK_DEMOS.md](docs/ATTACK_DEMOS.md) |
| **Platform Parity** | [docs/PLATFORM_PARITY.md](docs/PLATFORM_PARITY.md) |
| **iOS Integration** | [iosApp/INTEGRATION_GUIDE.md](iosApp/INTEGRATION_GUIDE.md) |
| **API Reference** | [docs/API.md](docs/API.md) |
| **Competition Essay** | [ESSAY_SUBMISSION.md](ESSAY_SUBMISSION.md) |


---

## 🏆 Competition Compliance

| Criterion | Status |
|-----------|--------|
| ✅ Original work | 100% written for this competition |
| ✅ Apache 2.0 license | [LICENSE](LICENSE) |
| ✅ Public repository | github.com/Raoof128/Raoof128.github.io |
| ✅ Kotlin Multiplatform | 4 targets from shared codebase |
| ✅ README documentation | This file |
| ✅ Competition essay | [ESSAY_SUBMISSION.md](ESSAY_SUBMISSION.md) |

---

## 📄 License

```
Copyright 2025-2026 QR-SHIELD Contributors
Licensed under the Apache License, Version 2.0
```

See [LICENSE](LICENSE) for the full text.

---

<p align="center">
  <b>🛡️ QR-SHIELD</b><br>
  Kotlin Multiplatform • Privacy-First • 87% F1 Score • <5ms Analysis<br>
  <i>Protecting users from QR phishing, one scan at a time.</i>
</p>
