# 🛡️ QR-SHIELD

> **The QR-SHIELD App** — A Kotlin Multiplatform security app that detects QR phishing attacks entirely offline. Scan any QR code and get instant, explainable verdicts without sacrificing your privacy.

[![Test Coverage](https://img.shields.io/badge/coverage-89%25-brightgreen)](https://github.com/Raoof128/Raoof128.github.io/actions/workflows/kover.yml)
[![Build Status](https://img.shields.io/github/actions/workflow/status/Raoof128/Raoof128.github.io/quality-tests.yml?label=tests)](https://github.com/Raoof128/Raoof128.github.io/actions)
[![Performance](https://img.shields.io/github/actions/workflow/status/Raoof128/Raoof128.github.io/performance.yml?label=performance&logo=speedtest&logoColor=white)](https://github.com/Raoof128/Raoof128.github.io/actions/workflows/performance.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-purple)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF)](https://kotlinlang.org)
[![KMP](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-orange)](https://kotlinlang.org/docs/multiplatform.html)

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

```kotlin
// Add to build.gradle.kts
dependencies {
    implementation("io.github.raoof128:qrshield:1.6.3")
}

// Use in your app
val engine = PhishingEngine()
val result = engine.analyzeBlocking(url)

when (result.verdict) {
    Verdict.SAFE -> showGreenCheckmark()
    Verdict.SUSPICIOUS -> showYellowWarning()
    Verdict.MALICIOUS -> blockAndAlert()
}
```

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
