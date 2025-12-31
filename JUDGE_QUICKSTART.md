# 🏆 Judge's Quick Start Guide

> **Start Here** — Verify QR-SHIELD in under 5 minutes.

---

## ⚡ 60-Second Quick Links

| Action | Link |
|--------|------|
| 🌐 **Try Live Demo** | [raoof128.github.io](https://raoof128.github.io) |
| 🎬 **Watch Video Demo** | [docs/VIDEO_DEMO.md](docs/VIDEO_DEMO.md) |
| 📱 **Download Android APK** | [QRShield-1.1.0-release.apk](releases/QRShield-1.1.0-release.apk) |
| 📖 **Read Essay** | [ESSAY.md](ESSAY.md) |
| 🔨 **One-Command Build Test** | `./scripts/judge-smoke.sh` |
| ✅ **Verify All Claims** | `./judge/verify_all.sh` |
| 📊 **View Evidence Pack** | [docs/EVIDENCE.md](docs/EVIDENCE.md) |

---

## 🧪 Test URLs (Copy & Paste)

### Try in Web Demo or Any App

| URL | Expected Verdict | Why |
|-----|------------------|-----|
| `https://google.com` | ✅ SAFE | Legitimate domain |
| `https://paypa1-secure.tk/login` | 🔴 MALICIOUS | Brand impersonation (paypal → paypa1) + risky TLD |
| `https://gооgle.com` | 🔴 MALICIOUS | Homograph attack (Cyrillic 'о') |
| `https://bit.ly/xyz` | ⚠️ SUSPICIOUS | URL shortener hides destination |
| `http://192.168.1.1/login` | ⚠️ SUSPICIOUS | IP address instead of domain |

> **💡 Tip:** In the web demo, paste any URL in the input box and press Enter.

---

## 🔍 Quick Verification Commands

```bash
# Clone the repository
git clone https://github.com/Raoof128/Raoof128.github.io.git qrshield
cd qrshield

# Run ALL verification (2-3 minutes)
./judge/verify_all.sh

# Or run individual checks:
./judge/verify_offline.sh      # Proves zero network calls
./judge/verify_performance.sh  # Proves <5ms latency
./judge/verify_accuracy.sh     # Proves 87% F1 score
./judge/verify_parity.sh       # Proves cross-platform parity
```

**Expected Output:**
```
✅ OFFLINE VERIFICATION PASSED
✅ PERFORMANCE VERIFICATION PASSED
✅ ACCURACY VERIFICATION PASSED
✅ PARITY VERIFICATION PASSED
```

---

## 🔴 Red Team Developer Mode

**Hidden feature for testing attack detection across all platforms.**

| Platform | How to Activate | State |
|----------|----------------|-------|
| **Android** | Settings → 7-tap version number → Toggle "Red Team Mode" | Hidden by default |
| **iOS** | Settings → 7-tap version number → Toggle "Red Team Mode" | Hidden by default |
| **Desktop** | Header bar → Click "🕵️ Judge Mode" toggle (next to "Offline First") | Toggle ON/OFF |
| **Web** | Settings → Security Settings → "Enable Red Team Scenarios" toggle | Toggle ON/OFF |

**What You'll See:**
- 18 curated attack scenarios (homographs, IP obfuscation, TLD abuse, brand impersonation, URL shorteners)
- One-tap testing of detection accuracy (bypasses camera, feeds URL directly to engine)
- Useful for demonstrating engine capabilities to judges without needing QR codes

**Quick Demo Steps:**
1. **Desktop**: Click "Judge Mode" in header → Click any attack chip (e.g., "Cyrillic Apple")
2. **Web**: Go to Settings → Enable "Red Team Scenarios" → Click any chip → Redirects to scanner with analysis
3. **Mobile**: Settings → 7-tap version → Enable Red Team Mode → Chips appear in scanner

---

## 📊 Key Metrics at a Glance

| Metric | Value | Evidence |
|--------|-------|----------|
| **Test Coverage** | 89% | [Kover Report](https://github.com/Raoof128/Raoof128.github.io/actions/workflows/kover.yml) |
| **Total Tests** | 1,248 | `./gradlew :common:desktopTest` |
| **Analysis Latency** | <5ms P95 | [BENCHMARKS.md](docs/BENCHMARKS.md) |
| **Detection Rate** | 87% F1 | [EVALUATION.md](docs/EVALUATION.md) |
| **False Positive Rate** | <5% | [AlexaTop100FPTest.kt](common/src/commonTest/kotlin/com/qrshield/benchmark/AlexaTop100FPTest.kt) |
| **Shared Code** | ~11,000 LOC | [SHARED_CODE_REPORT.md](docs/SHARED_CODE_REPORT.md) |
| **Platforms** | 5 (Android, iOS, Desktop, JS, Wasm) | All build successfully |
| **Languages** | 16 | 629 Android strings, 547 iOS strings |
| **Accessibility** | Full | 197+ content descriptions, VoiceOver labels |
| **App Version** | 1.20.30 | Latest as of Dec 31, 2025 |

---

## 🏗️ Key Files to Review

If you have limited time, focus on these files:

| Priority | File | What It Shows |
|----------|------|---------------|
| ⭐⭐⭐ | [PhishingEngine.kt](common/src/commonMain/kotlin/com/qrshield/core/PhishingEngine.kt) | Core detection logic, KMP architecture |
| ⭐⭐⭐ | [EnsembleModel.kt](common/src/commonMain/kotlin/com/qrshield/ml/EnsembleModel.kt) | 3-model ML ensemble (LR + Boosting + Rules) |
| ⭐⭐ | [HeuristicsEngine.kt](common/src/commonMain/kotlin/com/qrshield/engine/HeuristicsEngine.kt) | 25+ security heuristics |
| ⭐⭐ | [PlatformAbstractions.kt](common/src/commonMain/kotlin/com/qrshield/platform/PlatformAbstractions.kt) | expect/actual pattern |
| ⭐ | [ESSAY.md](ESSAY.md) | Competition essay (322 words) |

---

## ✅ Competition Compliance Checklist

| Requirement | Status | Location |
|-------------|--------|----------|
| ✅ Original work (not pre-existing) | PASS | [CONTEST_START.md](CONTEST_START.md) |
| ✅ Open-source license | PASS | [LICENSE](LICENSE) (Apache 2.0) |
| ✅ README.md with instructions | PASS | [README.md](README.md) |
| ✅ Essay (300+ words) | PASS | [ESSAY.md](ESSAY.md) (322 words) |
| ✅ Public GitHub repository | PASS | [github.com/Raoof128/Raoof128.github.io](https://github.com/Raoof128/Raoof128.github.io) |
| ✅ Not a library-only submission | PASS | Full apps on 5 platforms |
| ✅ Not a template/Hello World | PASS | ~26,000 LOC custom implementation |

---

## 🎯 Scoring Quick Reference

### Creativity & Novelty (40 pts)
- **QRishing detection** addresses a 587% attack increase
- **Offline-first** = privacy is architecture, not feature
- **Ensemble ML** = 3 models combined (not just API calling)
- **Explainable** = tells users WHY, not just WHAT

### Kotlin Multiplatform Usage (40 pts)
- **5 KMP targets**: Android, iOS (3 arch), Desktop, JS, WasmJS
- **100% shared business logic** (~11,000 LOC)
- **Shared Compose UI components**: `CommonBrainVisualizer`, `CameraPermissionScreen`
- **Strategic expect/actual**: 7 platform abstractions
- **Parity tests**: prove identical behavior across all targets

### Kotlin Conventions (20 pts)
- **Sealed classes, data classes, coroutines** throughout
- **Detekt zero-tolerance** in CI
- **89% test coverage**, 1,248 tests
- **KDoc on all public APIs**

---

## ❓ FAQ for Judges

### "Why are web demo scores different from native?"

Web uses optimized ML weights (~200KB vs ~500KB). Detection is still accurate—only thresholds differ slightly. See [Platform Parity Note](docs/PLATFORM_PARITY.md#web-optimization).

### "How do I test iOS without Xcode?"

Use the web demo at [raoof128.github.io](https://raoof128.github.io)—it uses the exact same detection engine compiled to JavaScript.

### "Is this really 100% offline?"

Yes! Run `./judge/verify_offline.sh` to prove zero network calls. The analysis module has no HTTP client dependencies.

### "How long did this take to build?"

26 days (December 5-31, 2025). See [CONTEST_START.md](CONTEST_START.md) for timeline.

---

## 📞 Contact

**Author:** Raouf (رئوف)  
**GitHub:** [@Raoof128](https://github.com/Raoof128)  
**Location:** Sydney, Australia

---

*Thank you for reviewing QR-SHIELD. Every claim in this document is backed by reproducible evidence.*

🛡️ **Scan smart. Stay protected.**
