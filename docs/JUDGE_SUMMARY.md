# 🧑‍⚖️ QR-SHIELD — Judge Summary Card

> **30-second pitch:** QR-SHIELD is a Kotlin Multiplatform security app that detects phishing in QR codes across Android, iOS, Desktop, and Web—entirely offline, with a single shared detection engine.

---

## ✨ Why This Should Win

| Differentiator | Evidence |
|----------------|----------|
| **Privacy-First** | 100% offline, zero data collection, no cloud API calls |
| **True KMP Usage** | ~80% shared business logic (7,400+ LOC), not just data classes |
| **Real Problem** | QRishing is a growing threat with limited cross-platform tools |
| **Production Quality** | 89% test coverage, 900+ tests, Detekt CI enforcement |

---

## 🚀 Quick Start (Pick One)

```bash
# Option 1: Live demo (no build required)
open https://raoof128.github.io/?demo=true

# Option 2: Desktop app (any OS with JDK 17+)
./gradlew :desktopApp:run

# Option 3: iOS (macOS + Xcode 15+)
./scripts/build_ios_demo.sh

# Option 4: Android (Android Studio)
./gradlew :androidApp:installDebug
```

---

## 📊 Key Metrics

| Metric | Value |
|--------|-------|
| **Shared Code** | ~80% business logic |
| **Platforms** | 4 (Android, iOS, Desktop, Web) |
| **Test Coverage** | 89% |
| **Detection Accuracy** | 87.1% F1 score |
| **Security Heuristics** | 25+ |
| **Unit Tests** | 900+ |

---

## 🧠 Architecture Highlights

- **Shared:** `PhishingEngine`, `HeuristicsEngine`, `BrandDetector`, `LogisticRegressionModel`
- **Platform:** Native UI (Compose/SwiftUI/HTML) + native QR scanning
- **Pattern:** `expect/actual` for database drivers, QR scanners, platform utilities

---

## 🔗 Key Links

| Resource | Link |
|----------|------|
| **Live Demo** | [raoof128.github.io](https://raoof128.github.io) |
| **Full README** | [README.md](../README.md) |
| **Essay** | [ESSAY.md](../ESSAY.md) |
| **ML Model** | [docs/ML_MODEL.md](ML_MODEL.md) |
| **Architecture** | [docs/ARCHITECTURE.md](ARCHITECTURE.md) |

---

*Created for KotlinConf Student Competition 2025-2026*
