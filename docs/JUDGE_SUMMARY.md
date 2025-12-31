# 🧑‍⚖️ QR-SHIELD — Judge Summary Card

> **30-second pitch:** QR-SHIELD is a Kotlin Multiplatform security app that detects phishing in QR codes across Android, iOS, Desktop, and Web—entirely offline, with a single shared detection engine using an ensemble ML architecture.

---

## ✨ Why This Should Win

| Differentiator | Evidence |
|----------------|----------|
| **Privacy-First** | 100% offline, zero data collection, no cloud API calls |
| **True KMP Usage** | ~80% shared business logic (9,500+ LOC), not just data classes |
| **Real Problem** | QRishing is a growing threat (+587% since 2023) with limited cross-platform tools |
| **Production Quality** | 89% test coverage, 1,248+ tests, Detekt CI enforcement |
| **Advanced ML** | Ensemble architecture: Logistic Regression + Gradient Boosting + Decision Rules |
| **Explainable AI** | 25+ heuristic signals with weighted scores and explanations |

---

## 🚀 Quick Start (Pick One)

```bash
# Option 1: Live demo (no build required)
open https://raoof128.github.io/?demo=true

# Option 2: Interactive build helper (recommended for judges)
./judge.sh        # macOS/Linux
.\judge.ps1       # Windows PowerShell

# Option 3: Desktop app (any OS with JDK 17+)
./gradlew :desktopApp:run

# Option 4: iOS (macOS + Xcode 15+)
./scripts/build_ios_demo.sh

# Option 5: Android (Android Studio)
./gradlew :androidApp:installDebug
```

---

## 📊 Key Metrics

| Metric | Value |
|--------|-------|
| **Shared Code** | ~80% business logic (commonMain) |
| **Platforms** | 5 (Android, iOS, Desktop, Web JS, Web Wasm) |
| **Test Coverage** | 89% |
| **Detection Accuracy** | 87.1% F1 score |
| **Security Heuristics** | 25+ |
| **Unit Tests** | 1,248+ |
| **Analysis Speed** | <5ms per URL |

---

## ⚡ Performance

| Operation | Target | Actual |
|-----------|--------|--------|
| Full URL Analysis | <50ms | ~3-5ms ✅ |
| Heuristics Engine | <10ms | ~0.5ms ✅ |
| ML Inference | <5ms | ~0.1ms ✅ |
| Throughput | 100+ URLs/sec | 500+ URLs/sec ✅ |

---

## 🧠 Architecture Highlights

- **Shared:** `PhishingEngine`, `HeuristicsEngine`, `BrandDetector`, `EnsembleModel`
- **Hybrid iOS:** Native SwiftUI + shared Compose components via `SharedResultCard`
- **Ensemble ML:** Logistic Regression (40%) + Gradient Boosting (35%) + Decision Stumps (25%)
- **Pattern:** `expect/actual` for database drivers, QR scanners, platform utilities
- **Constants:** Centralized in `SecurityConstants.kt` for tuning

---

## 🆕 Novelty Features

| Feature | Description |
|---------|-------------|
| **Dynamic Brand Discovery** | Pattern-based detection for unknown brands (trust word, action word, hyphen abuse) |
| **Enterprise Policy Engine** | OrgPolicy with domain blocklists, TLD restrictions, HTTPS enforcement |
| **Multi-Payload Analysis** | WiFi, SMS, vCard, Crypto, UPI beyond just URLs |
| **Adversarial Robustness** | Homograph, RTL override, double encoding, zero-width detection |
| **Federated Learning** | (ε,δ)-Differential Privacy for model improvement |
| **Beat the Bot** | Gamification mode for adversarial testing (prominent in Desktop/Web UI!) |
| **Living Engine** | OTA updates from GitHub Pages |

---

## 🔗 Key Links

| Resource | Link |
|----------|------|
| **Live Demo** | [raoof128.github.io](https://raoof128.github.io) |
| **Full README** | [README.md](../README.md) |
| **Essay** | [ESSAY_SUBMISSION.md](../ESSAY_SUBMISSION.md) |
| **ML Model** | [docs/ML_MODEL.md](ML_MODEL.md) |
| **Architecture** | [docs/ARCHITECTURE.md](ARCHITECTURE.md) |
| **Security Model** | [SECURITY_MODEL.md](../SECURITY_MODEL.md) |

---

## 📧 Contact

**Developer:** Mohammad Raouf Abedini  
**University:** Macquarie University, Sydney, Australia  
**Email:** [raoof.r12@gmail.com](mailto:raoof.r12@gmail.com)

> 💡 Questions? Feel free to reach out!

---

*Created for KotlinConf Student Competition 2025-2026*
*Last updated: December 31, 2025 (v1.20.30)*
