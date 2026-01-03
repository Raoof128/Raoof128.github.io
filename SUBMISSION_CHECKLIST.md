# 📋 Submission Checklist (Judge-Facing)

> **For Judges:** This checklist maps every KotlinConf 2025-2026 competition requirement to its evidence.
>
> **Submission Deadline:** January 12, 2026 (23:59:00 CET)

---

## ✅ Mandatory Requirements (PASS/FAIL)

| Requirement | Status | Evidence |
|-------------|--------|----------|
| Uses Kotlin Multiplatform | ✅ PASS | `common/` module, 5 targets |
| Runs on at least 2 platforms | ✅ PASS | 5 platforms: Android, iOS, Desktop, Web (JS), Web (Wasm) |
| Created specifically for this contest | ✅ PASS | [CONTEST_START.md](CONTEST_START.md) - Started Dec 5, 2025 |
| NOT a template or "Hello World" | ✅ PASS | 26,000+ LOC, substantial security app |
| NOT a library | ✅ PASS | Standalone application with full UI |
| GitHub repository | ✅ PASS | [github.com/Raoof128/Raoof128.github.io](https://github.com/Raoof128/Raoof128.github.io) |
| Open-source license (Apache 2.0/MIT/BSD) | ✅ PASS | [LICENSE](LICENSE) (Apache 2.0) |
| README with installation instructions | ✅ PASS | [README.md](README.md) - All 5 platforms |
| README with feature descriptions | ✅ PASS | [README.md](README.md) - Detection engine, ML, Red Team |
| Short screencast (3-5 min) | ✅ PASS | [Demo Video](https://youtu.be/n8bheouj4jM) |
| All content in English | ✅ PASS | Docs, code, comments all English |
| 300-word Essay | ✅ PASS | [ESSAY.md](ESSAY.md) (327 words) |
| Original work (100% owned) | ✅ PASS | Git history shows original development |

---

## 🎯 Judging Criteria Breakdown

### Creativity & Novelty (40%)

| Criterion | Evidence | Score Target |
|-----------|----------|--------------|
| **Unique Problem** | QR phishing detection—587% attack increase since 2023 | +10 |
| **Offline-First Architecture** | Zero network calls, [verify_offline.sh](judge/verify_offline.sh) | +10 |
| **Ensemble ML** | 3 models combined ([EnsembleModel.kt](common/src/commonMain/kotlin/com/raouf/mehrguard/ml/EnsembleModel.kt)) | +8 |
| **Dynamic Brand Discovery** | Detects unknown brands via patterns | +5 |
| **Adversarial Robustness** | Homographs, punycode, RTL override detection | +5 |
| **Beat the Bot Gamification** | Security training game mode | +2 |
| **TOTAL** | | **40/40** |

### Kotlin Multiplatform Usage (40%)

| Criterion | Evidence | Score Target |
|-----------|----------|--------------|
| **5 Platform Targets** | Android, iOS, Desktop, Web (JS), Web (Wasm) | +10 |
| **~52% Shared Code** | [SHARED_CODE_REPORT.md](docs/SHARED_CODE_REPORT.md) - 13,500 LOC shared | +10 |
| **Strategic expect/actual** | 7 platform abstractions (Clipboard, Haptics, Logger, etc.) | +8 |
| **Shared Business Logic** | 100% of detection engine in commonMain | +5 |
| **Native Integration** | SwiftUI + Kotlin bridge, Compose MP | +5 |
| **Platform Parity Tests** | [verify_parity.sh](judge/verify_parity.sh) | +2 |
| **TOTAL** | | **40/40** |

### Kotlin Coding Conventions (20%)

| Criterion | Evidence | Score Target |
|-----------|----------|--------------|
| **Idiomatic Kotlin** | sealed classes, data classes, coroutines, Flow | +5 |
| **Detekt Static Analysis** | [detekt.yml](detekt.yml), CI enforced | +5 |
| **KDoc Documentation** | All public APIs documented | +3 |
| **Test Coverage** | 89% coverage, 1,248+ tests | +3 |
| **Property-Based Tests** | Randomized input testing | +2 |
| **Architecture Tests** | Konsist rules enforced | +2 |
| **TOTAL** | | **20/20** |

---

## 🏃 Quick Verification (5 Minutes)

```bash
# Clone and verify
git clone https://github.com/Raoof128/Raoof128.github.io.git mehrguard
cd mehrguard

# Run all verification
./judge/verify_all.sh
```

| Command | What It Proves | Expected Time |
|---------|----------------|---------------|
| `./judge/verify_offline.sh` | Zero network calls | 30s |
| `./judge/verify_performance.sh` | <5ms P50 latency | 30s |
| `./judge/verify_accuracy.sh` | 87 F1 score | 30s |
| `./judge/verify_parity.sh` | JVM+JS+Native identical | 2m |

---

## 📁 Key Files for Review

| Category | File | Why It's Important |
|----------|------|--------------------|
| **Core Engine** | [PhishingEngine.kt](common/src/commonMain/kotlin/com/mehrguard/core/PhishingEngine.kt) | Main orchestrator, shows KMP design |
| **ML Model** | [EnsembleModel.kt](common/src/commonMain/kotlin/com/mehrguard/ml/EnsembleModel.kt) | Ensemble architecture |
| **Platform Abstraction** | [PlatformAbstractions.kt](common/src/commonMain/kotlin/com/mehrguard/platform/PlatformAbstractions.kt) | expect/actual pattern |
| **iOS Integration** | [ComposeInterop.swift](iosApp/MehrGuard/ComposeInterop.swift) | SwiftUI + Compose hybrid |
| **Tests** | [VerifyMlMathTest.kt](common/src/commonTest/kotlin/com/mehrguard/ml/VerifyMlMathTest.kt) | Proves ML is real |
| **Security Constants** | [SecurityConstants.kt](common/src/commonMain/kotlin/com/mehrguard/core/SecurityConstants.kt) | Centralized, documented |

---

## 📊 What's Original Here

| What | Why It Matters |
|------|----------------|
| **Offline-first security scanner** | Existing solutions require cloud APIs |
| **Ensemble ML for URL classification** | Most scanners use single-model or blocklists |
| **Dynamic brand discovery** | Detects unknown brands without database |
| **Explainable verdicts** | Users understand WHY something is risky |
| **True 5-platform KMP** | Same engine on Android, iOS, Desktop, Web (JS + Wasm) |
| **Production-quality CI** | Detekt, Kover, Konsist, performance CI |

---

## 🔗 All Documentation

| Document | Purpose |
|----------|---------|
| [README.md](README.md) | Quick start, architecture overview |
| [ESSAY.md](ESSAY.md) | Competition essay (327 words) |
| [CONTEST_START.md](CONTEST_START.md) | Contest timeline, original work proof |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Technical architecture deep-dive |
| [docs/THREAT_MODEL.md](docs/THREAT_MODEL.md) | Who attacks, what we detect |
| [docs/EVALUATION.md](docs/EVALUATION.md) | Accuracy metrics, methodology |
| [docs/HEURISTICS.md](docs/HEURISTICS.md) | All detection rules documented |
| [docs/SHARED_CODE_REPORT.md](docs/SHARED_CODE_REPORT.md) | Module-by-module shared code % |
| [SECURITY.md](SECURITY.md) | Security policy, disclosure |
| [PRIVACY.md](PRIVACY.md) | What data we store/don't store |
| [docs/VIDEO_DEMO.md](docs/VIDEO_DEMO.md) | Demo video and script |

---

## 🎬 Demo Video

**YouTube Link:** [https://youtu.be/n8bheouj4jM](https://youtu.be/n8bheouj4jM)

The screencast demonstrates:
- Real-time QR scanning on all platforms
- Phishing detection with explainable verdicts
- Red Team developer mode with 19 attack scenarios
- Beat the Bot gamification feature
- Cross-platform UI consistency

---

## ⏰ Submission Timeline

| Date | Event |
|------|-------|
| September 15, 2025 | Contest opens |
| December 5, 2025 | Repository created |
| January 4, 2026 | Current version: v2.0.36 |
| **January 12, 2026** | **Submission deadline (23:59 CET)** |
| January 22, 2026 | Winners announced |

---

*This checklist is designed for judges to verify every claim in under 10 minutes.*
*Version: 2.0.36 | Last Updated: January 4, 2026*
