# Mehr Guard Roadmap

This document outlines future enhancements planned for Mehr Guard after the KotlinConf 2026 Student Coding Competition.

> **Note:** Items below are planned features. The [README.md](README.md) documents only shipped features.

---

## v2.0 (Post-Competition)

### 🌐 Kotlin/Wasm Target

**Status:** Experimental (not production-ready)

```kotlin
// Planned: webApp/src/wasmJsMain/kotlin/Main.kt
@OptIn(ExperimentalWasmDsl::class)
fun main() { /* Wasm entry point */ }
```

**Why not now:**
- SQLDelight and kotlinx-coroutines don't fully support Kotlin/Wasm yet
- WebAssembly linear memory limits affect large URL processing
- Browser adoption of Wasm GC is still growing

**When ready:** Will enable ~2x faster URL analysis in browsers.

---

### 🧠 Federated Learning

**Status:** Designed, not implemented

**What it is:** Privacy-preserving model improvement where:
- Users report false positives/negatives
- Only encrypted gradients (not URLs) are sent to server
- Model improves without seeing user data

**Components designed:**
- `PrivacyPreservingAnalytics.kt` - Differential privacy scaffolding
- `FeedbackManager.kt` - User feedback collection

**Why not now:**
- Requires backend infrastructure for gradient aggregation
- Privacy guarantees need formal verification
- Not necessary for competition judging (offline-first is the feature)

---

### 🔄 Living Engine (OTA Updates)

**Status:** Placeholder exists

**What it is:** Over-the-air updates for:
- Brand database (new brands discovered)
- Heuristic rules (new attack patterns)
- ML model weights (improved accuracy)

**Why not now:**
- Requires CDN/backend infrastructure
- Checksum verification code exists but no remote config
- Competition entry should demonstrate local capabilities

---

### 🔒 Differential Privacy (Formal)

**Status:** Basic implementation exists

**What's implemented:**
- Gaussian noise injection
- Gradient clipping (L2 norm)
- k-anonymity (timestamp bucketing)

**What's missing for "real" DP:**
- Formal privacy budget accounting
- Integration with federated learning
- Third-party audit of privacy guarantees

---

## v1.7 (Near-term)

### ✅ App Clip / Instant App

Allow scanning without full app installation:
- iOS: App Clip (< 10MB)
- Android: Instant App module

### ✅ Apple Watch / Wear OS

Companion apps showing:
- Last scan result
- Quick stats
- Haptic alerts for MALICIOUS verdicts

### ✅ Browser Extension

Chrome/Firefox extension that:
- Scans links before click
- Shows verdict badge on URLs
- Uses same PhishingEngine (via Kotlin/JS)

---

## Contributing to Roadmap

Have ideas? Open an issue with the `enhancement` label:
- Describe the feature
- Explain the user benefit
- Suggest implementation approach

---

## Current Features (v1.6.2)

These are **shipped and working**:

| Feature | Evidence |
|---------|----------|
| ✅ Ensemble ML (3 models) | `EnsembleModel.kt` |
| ✅ 25+ security heuristics | `HeuristicsEngine.kt` |
| ✅ 500+ brand database | `BrandDetector.kt` |
| ✅ Dynamic brand discovery | `DynamicBrandDiscovery.kt` |
| ✅ 89% test coverage | CI badge |
| ✅ <5ms analysis (P50) | `./judge/verify_performance.sh` |
| ✅ 5-language translations | `Translations.kt` |
| ✅ Real ECDH crypto | `SecureAggregation.kt` |
| ✅ iOS Compose hybrid | `ComposeInterop.swift` |

See [README.md](README.md) for full documentation of shipped features.
