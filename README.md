# 🛡️ QR-SHIELD

> **Kotlin Multiplatform security app detecting QR phishing attacks entirely offline.** Protects users' privacy (no URL ever leaves device) while achieving 87% F1 score on real phishing samples. One shared detection engine across Android, iOS, Desktop, and Web.

[![Test Coverage](https://img.shields.io/badge/coverage-89%25-brightgreen)](https://github.com/Raoof128/Raoof128.github.io/actions/workflows/kover.yml)
[![Build Status](https://img.shields.io/github/actions/workflow/status/Raoof128/Raoof128.github.io/quality-tests.yml?label=tests)](https://github.com/Raoof128/Raoof128.github.io/actions)
[![Performance](https://img.shields.io/github/actions/workflow/status/Raoof128/Raoof128.github.io/performance.yml?label=performance&logo=speedtest&logoColor=white)](https://github.com/Raoof128/Raoof128.github.io/actions/workflows/performance.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-purple)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF)](https://kotlinlang.org)
[![KMP](https://img.shields.io/badge/Platforms-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-orange)](https://kotlinlang.org/docs/multiplatform.html)

---

## 📋 Executive Summary

### The Problem

**QRishing attacks increased 587% since 2023.** Users scan parking meters, restaurant menus, payment terminals—never verifying the URL. Existing solutions require cloud APIs, meaning every scanned URL is logged on corporate servers. **Privacy is sacrificed for protection.**

### The Solution

**Privacy-first phishing detection with zero network requests:**

| Feature | Implementation |
|---------|----------------|
| 🔒 **100% Offline** | No URL ever leaves the device—zero data collection |
| 🧠 **Ensemble ML** | 3 models: Logistic Regression + Gradient Boosting + Decision Rules |
| 🔍 **25+ Heuristics** | Homograph detection, typosquatting, suspicious TLDs, IP obfuscation |
| 🏢 **500+ Brand Database** | Plus dynamic pattern discovery for unknown brands |
| ⚡ **<5ms Analysis** | Real-time feedback during QR scanning |

### Why This Wins

| Criterion | Evidence |
|-----------|----------|
| ✅ **Real Impact** | Protects users from $12B+ annual QRishing losses |
| ✅ **Privacy Architecture** | No cloud API = no data collection |
| ✅ **Technical Depth** | Ensemble ML, adversarial robustness, ECDH secure aggregation |
| ✅ **Production Quality** | 89% test coverage, 1000+ tests, zero-tolerance Detekt CI |
| ✅ **Kotlin Showcase** | Coroutines, sealed classes, null safety, expect/actual |

---

## 🧑‍⚖️ Quick Start for Judges

```bash
# One-command verification (5 minutes)
./judge/verify_all.sh

# Or try the live demo (no build required)
# → https://raoof128.github.io/?demo=true
```

| Test URL | Expected Result |
|----------|-----------------|
| `https://paypa1-secure.tk/login` | 🔴 MALICIOUS — Brand impersonation + suspicious TLD |
| `https://google.com` | 🟢 SAFE — No threats detected |
| `https://gооgle.com` | 🔴 MALICIOUS — Homograph attack (Cyrillic 'о') |

### Pre-Built Downloads

| Platform | Download |
|----------|----------|
| **Android** | [`QRShield-1.1.0-release.apk`](releases/QRShield-1.1.0-release.apk) |
| **Web Demo** | [raoof128.github.io](https://raoof128.github.io) |
| **Desktop** | `./gradlew :desktopApp:run` |
| **iOS** | Coming to TestFlight |

> 📝 **[Competition Essay →](ESSAY_SUBMISSION.md)** (550 words covering background and approach)

---

## 🏗️ Architecture Overview

> **~80% of code is shared via Kotlin Multiplatform.** The detection engine is written once and compiles to JVM, Native, and JavaScript.

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
       │  FeatureExtractor — 15 URL features for ML           │
       └─────────────────────────────────────────────────────┘
```

### Platform Support

| Platform | Status | Implementation | LOC |
|----------|--------|----------------|-----|
| **Android** | ✅ **Full** | CameraX + ML Kit + Compose UI | ~4,500 |
| **iOS** | ✅ **Full** | AVFoundation + SwiftUI + KMP engine | ~6,500 |
| **Desktop** | ✅ **Full** | Compose Desktop + ZXing | ~2,000 |
| **Web** | ⚠️ **Demo** | Kotlin/JS proof-of-concept | ~500 |

> 📖 **[Full Architecture Documentation →](docs/ARCHITECTURE.md)**

---

## ✨ Key Features

### Detection Engine

```kotlin
// One call, same behavior on all platforms
val result = PhishingEngine().analyzeBlocking("https://paypa1-secure.tk/login")
// → Verdict: MALICIOUS (score: 85)
// → Flags: ["Brand impersonation: paypal", "High-risk TLD: .tk"]
```

### What We Detect

| Attack Type | Detection Method |
|-------------|------------------|
| **Homograph Attacks** | Unicode script mixing detection (Cyrillic 'а', Greek 'ο') |
| **Typosquatting** | Levenshtein distance for 500+ brands |
| **URL Shorteners** | bit.ly, tinyurl, t.co flagged as suspicious |
| **Suspicious TLDs** | .tk, .ml, .ga, .cf freely abused for phishing |
| **IP Address URLs** | Both standard and obfuscated (octal, hex, decimal) |
| **@ Symbol Injection** | `https://google.com@evil.com` patterns |

### Novelty Features

| Feature | What It Does |
|---------|--------------|
| 🎮 **Beat the Bot** | Adversarial testing game mode |
| 🔍 **Dynamic Brand Discovery** | Pattern-based detection for unknown brands |
| 🏢 **Local Policy Engine** | Enterprise allow/block lists |
| 🔐 **ECDH Secure Aggregation** | Privacy-preserving analytics foundation |

> 📖 **[ML Model Details →](docs/ML_MODEL.md)** | **[Attack Demos →](docs/ATTACK_DEMOS.md)** | **[Threat Model →](docs/THREAT_MODEL.md)**

---

## 🔧 SDK Integration

```kotlin
// Add to build.gradle.kts
dependencies {
    implementation("io.github.raoof128:qrshield:1.6.2")
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

> 📖 **[Full SDK Documentation →](docs/API.md)**

---

## 🧪 Quality & Testing

### Test Coverage

| Metric | Value |
|--------|-------|
| Test Coverage | 89% |
| Total Tests | 1000+ |
| Architecture Tests | 9 (Konsist) |
| Performance Tests | 15+ with P99 thresholds |
| Accuracy Tests | F1: 87.1%, Recall: 89.1% |

### CI/CD Enforcement

| Workflow | What It Checks |
|----------|----------------|
| `quality-tests.yml` | 1000+ unit tests |
| `performance.yml` | P99 latency thresholds |
| `benchmark.yml` | Performance regression detection |
| Detekt | Zero-tolerance static analysis |

> 📖 **[Testing Strategy →](docs/EVALUATION.md)** | **[Performance Benchmarks →](docs/JUDGE_SUMMARY.md)**

---

## 📊 Verification Scripts

**All claims are reproducible:**

```bash
./judge/verify_all.sh  # Runs all 4 verification suites
```

| Test | Claim Verified |
|------|----------------|
| `./judge/verify_offline.sh` | Zero network calls during analysis |
| `./judge/verify_performance.sh` | <5ms P50 latency |
| `./judge/verify_accuracy.sh` | 87% F1 score |
| `./judge/verify_parity.sh` | Identical verdicts across platforms |

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
| **Style Guide** | [docs/KOTLIN_STYLE_GUIDE.md](docs/KOTLIN_STYLE_GUIDE.md) |
| **Roadmap** | [ROADMAP.md](ROADMAP.md) |

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

## 🔗 Links

- **Live Demo:** [raoof128.github.io](https://raoof128.github.io)
- **Competition Essay:** [ESSAY_SUBMISSION.md](ESSAY_SUBMISSION.md)
- **Full Documentation:** [README_FULL.md](README_FULL.md) (17,000 words)
- **GitHub Repository:** [github.com/Raoof128/Raoof128.github.io](https://github.com/Raoof128/Raoof128.github.io)

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
