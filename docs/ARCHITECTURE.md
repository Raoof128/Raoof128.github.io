# 🏗️ QR-SHIELD Architecture

> High-level architecture overview: what's shared, what's platform-specific, and where expect/actual is used.

---

## 📊 Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           QR-SHIELD Architecture                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                     SHARED LAYER (common module)                     │   │
│  │                        ~80% of business logic                        │   │
│  │   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │   │
│  │   │  Detection  │  │  Ensemble   │  │   Data      │                 │   │
│  │   │   Engine    │  │  ML Model   │  │   Layer     │                 │   │
│  │   ├─────────────┤  ├─────────────┤  ├─────────────┤                 │   │
│  │   │PhishingEngin│  │EnsembleModel│  │HistoryRepo  │                 │   │
│  │   │HeuristicsEng│  │LogisticRegr.│  │SQLDelight   │                 │   │
│  │   │BrandDetector│  │GradientBoost│  │Queries      │                 │   │
│  │   │AdversarialD.│  │DecisionStump│  │             │                 │   │
│  │   └─────────────┘  └─────────────┘  └─────────────┘                 │   │
│  │                                                                      │   │
│  │   ┌───────────────────────────────────────────────────────┐         │   │
│  │   │          EXPECT/ACTUAL DECLARATIONS                   │         │   │
│  │   │  ─────────────────────────────────────────────────── │         │   │
│  │   │  expect class DatabaseDriverFactory                   │         │   │
│  │   │  expect class QrScanner                               │         │   │
│  │   │  expect object PlatformClipboard                      │         │   │
│  │   │  expect object PlatformHaptics                        │         │   │
│  │   └───────────────────────────────────────────────────────┘         │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                      │                                      │
│                    ┌─────────────────┼─────────────────┐                    │
│                    │                 │                 │                    │
│                    ▼                 ▼                 ▼                    │
│  ┌─────────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌────────┐│
│  │   ANDROID LAYER     │ │   IOS LAYER     │ │  DESKTOP LAYER  │ │  WEB   ││
│  │   (androidApp/)     │ │   (iosApp/)     │ │  (desktopApp/)  │ │(webApp)││
│  ├─────────────────────┤ ├─────────────────┤ ├─────────────────┤ ├────────┤│
│  │ Jetpack Compose UI  │ │  SwiftUI UI +   │ │ Compose Desktop │ │HTML/CSS││
│  │ ML Kit Scanner      │ │  SharedResultCd │ │ ZXing Scanner   │ │ jsQR   ││
│  │ Android SQLite      │ │ AVFoundation    │ │ JVM SQLite      │ │IndexedD││
│  │ CameraX             │ │ Vision Kit      │ │ Swing fallback  │ │ Canvas ││
│  └─────────────────────┘ └─────────────────┘ └─────────────────┘ └────────┘│
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Shared Code (common module)

All business logic lives in `common/src/commonMain/kotlin/`:

### Core Detection Engine

| File | Purpose | LOC |
|------|---------|-----|
| `PhishingEngine.kt` | Main analysis orchestrator (suspend + blocking APIs) | ~400 |
| `HeuristicsEngine.kt` | 25+ security heuristics | ~400 |
| `BrandDetector.kt` | Static brand impersonation detection (60+ brands) | ~500 |
| `DynamicBrandDiscovery.kt` | Pattern-based brand detection for unknown brands | ~280 |
| `BrandDatabase.kt` | 500+ brand patterns | ~600 |
| `SecurityConstants.kt` | Centralized thresholds and weights | ~380 |

### Ensemble ML Model

| File | Purpose | LOC |
|------|---------|-----|
| `EnsembleModel.kt` | 3-model ensemble (LR + GB + Rules) | ~380 |
| `LogisticRegressionModel.kt` | On-device ML classifier | ~400 |
| `FeatureExtractor.kt` | URL feature extraction (15 features) | ~300 |

### Data Layer

| File | Purpose | LOC |
|------|---------|-----|
| `HistoryRepository.kt` | Scan history CRUD | ~200 |
| `QRShieldDatabase.sq` | SQLDelight schema | ~50 |

### Shared UI Components

| File | Purpose | LOC |
|------|---------|-----|
| `SharedResultCard.kt` | Compose MP result card (iOS + Desktop + Android) | ~200 |
| `ThreatRadar.kt` | Radar visualization component | ~150 |
| `SharedTextGenerator.kt` | Centralized verdict/explanation text | ~300 |
| `LocalizationKeys.kt` | ~80 localization keys | ~200 |

### Models & Utilities

| File | Purpose | LOC |
|------|---------|-----|
| `RiskAssessment.kt` | Analysis result data class | ~100 |
| `UrlParser.kt` | URL parsing utilities | ~200 |
| `AdversarialDefense.kt` | Obfuscation detection | ~350 |

---

## 📱 Platform-Specific Code

### What's Platform-Specific

| Component | Android | iOS | Desktop | Web |
|-----------|---------|-----|---------|-----|
| **UI Framework** | Jetpack Compose | SwiftUI | Compose Desktop | HTML/JS |
| **QR Scanner** | ML Kit | AVFoundation | ZXing | jsQR |
| **Database Driver** | Android SQLite | Native SQLite | JVM SQLite | IndexedDB |
| **Camera Access** | CameraX | AVCaptureSession | Webcam API | getUserMedia |
| **Haptic Feedback** | Vibrator | UIImpactFeedback | N/A | N/A |

### expect/actual Declarations

Located in `common/src/commonMain/kotlin/com/qrshield/platform/`:

```kotlin
// Database driver - different SQLite implementation per platform
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

// Platform info - OS detection
expect class PlatformInfo {
    val name: String
    val isDebug: Boolean
}

// QR Scanner abstraction
expect class QrScanner {
    suspend fun startScanning(): Flow<String>
    fun stopScanning()
}

// Time utility
expect fun getCurrentTimeMillis(): Long
```

### actual Implementations

| Expect | Android Actual | iOS Actual | Desktop Actual | Web Actual |
|--------|----------------|------------|----------------|------------|
| `DatabaseDriverFactory` | `AndroidSqliteDriver` | `NativeSqliteDriver` | `JdbcSqliteDriver` | `WebWorkerDriver` |
| `getCurrentTimeMillis()` | `System.currentTimeMillis()` | `NSDate` | `System.currentTimeMillis()` | `Date.now()` |
| `PlatformInfo.name` | `"Android"` | `"iOS"` | `"Desktop"` | `"Web"` |

---

## 🔄 Data Flow

```
1. User scans QR code / enters URL
              │
              ▼
2. Platform-specific scanner extracts URL
              │
              ▼
3. URL sent to shared PhishingEngine
              │
              ▼
4. ┌─────────────────────────────────┐
   │   SHARED ANALYSIS PIPELINE      │
   │                                 │
   │  URL → UrlParser.parse()        │
   │     → HeuristicsEngine.analyze()│
   │     → LogisticRegressionModel   │
   │     → BrandDetector.detect()    │
   │     → Score aggregation         │
   │     → RiskAssessment            │
   └─────────────────────────────────┘
              │
              ▼
5. RiskAssessment returned to platform UI
              │
              ▼
6. Platform-specific UI displays result
   • Verdict (SAFE/SUSPICIOUS/MALICIOUS)
   • Score (0-100)
   • Detailed risk signals
   • Counterfactual hints
```

---

## 📊 Code Distribution

```
Total Codebase: ~26,000 LOC

┌────────────────────────────────────────────────────────┐
│ SHARED (commonMain)                                    │
│ ████████████████████████████████░░░░░░░░░░ ~80%       │
│ • Detection engines                                    │
│ • ML model                                            │
│ • Business logic                                      │
│ • Data layer                                          │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│ PLATFORM-SPECIFIC                                      │
│ ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░████████ ~20%         │
│ • UI (Compose, SwiftUI, HTML)                         │
│ • Camera/Scanner (ML Kit, AVFoundation, ZXing)        │
│ • Database drivers                                    │
└────────────────────────────────────────────────────────┘
```

Run `./scripts/loc_report.sh` for exact counts.

---

## 🎯 Why This Architecture

### Benefits of KMP

1. **Single Source of Truth**: Detection logic written once, tested once
2. **Consistent Security**: Same analysis on all platforms
3. **Faster Updates**: Fix a bug in common → fixed everywhere
4. **Shared Tests**: 1,248+ tests run on all targets

### iOS Decision: SwiftUI with KMP Integration

We chose native SwiftUI for iOS with KMP engine integration:

1. **Better UX**: Native animations, gestures, feel
2. **Camera Access**: AVFoundation is more mature than KMP camera libs
3. **App Store Ready**: No experimental Compose iOS issues
4. **Full Platform Parity**: Same analysis results as Android/Desktop/Web
5. **Accessibility**: VoiceOver labels, Reduce Motion support

```swift
// iOS code calling Kotlin shared engine via UnifiedAnalysisService
let service = UnifiedAnalysisService.shared
let result = await service.analyze(url: userUrl)
// Same result as Android, Desktop, Web!

// Features at full parity (v1.20.30):
// - Dynamic analysis breakdowns (16 flag types)
// - Red Team developer mode
// - 547 localized strings (16 languages)
// - VoiceOver accessibility labels
```

---

## 📚 Related Documentation

- [Evaluation Methodology](EVALUATION.md)
- [ML Model Details](ML_MODEL.md)
- [Security Model](../SECURITY_MODEL.md)
- [API Documentation](API.md)

---

*Last updated: December 31, 2025 (v1.20.30)*
