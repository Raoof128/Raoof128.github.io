# QR-SHIELD Architecture

> Comprehensive technical architecture for the Kotlin Multiplatform QRishing Detector

## Table of Contents

- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Module Structure](#module-structure)
- [Data Flow](#data-flow)
- [Security Model](#security-model)
- [Platform Implementations](#platform-implementations)
- [Analysis Pipeline](#analysis-pipeline)
- [ML Model](#ml-model)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)

---

## Overview

QR-SHIELD is a Kotlin Multiplatform (KMP) application designed to detect QRishing (QR code phishing) attacks across Android, iOS, Desktop, and Web platforms with a single shared codebase.

### Design Principles

1. **Offline-First**: All analysis performed locally without network dependency
2. **Privacy-Focused**: No user data leaves the device
3. **Layered Security**: Multiple detection mechanisms combined for robustness
4. **Cross-Platform**: Single codebase for maximum code reuse
5. **Extensible**: Modular design for easy enhancement

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              QR-SHIELD SYSTEM                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   Android   │  │     iOS     │  │   Desktop   │  │     Web     │        │
│  │  (ML Kit)   │  │  (Vision)   │  │   (ZXing)   │  │   (jsQR)    │        │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
│         │                │                │                │               │
│         └────────────────┴────────────────┴────────────────┘               │
│                                   │                                         │
│                    ┌──────────────▼──────────────┐                         │
│                    │     PRESENTATION LAYER      │                         │
│                    │   (Compose Multiplatform)   │                         │
│                    │   ┌───────────────────┐     │                         │
│                    │   │  SharedViewModel  │     │                         │
│                    │   │   ┌──────────┐    │     │                         │
│                    │   │   │ UiState  │    │     │                         │
│                    │   │   └──────────┘    │     │                         │
│                    │   └───────────────────┘     │                         │
│                    └──────────────┬──────────────┘                         │
│                                   │                                         │
│                    ┌──────────────▼──────────────┐                         │
│                    │        DOMAIN LAYER         │                         │
│                    │                             │                         │
│                    │  ┌───────────────────────┐  │                         │
│                    │  │    PhishingEngine     │  │ ◄── Main Orchestrator   │
│                    │  │    ────────────────   │  │                         │
│                    │  │  • analyze(url)       │  │                         │
│                    │  │  • calculateScore()   │  │                         │
│                    │  │  • determineVerdict() │  │                         │
│                    │  └───────────┬───────────┘  │                         │
│                    │              │              │                         │
│                    │   ┌──────────┼──────────┐   │                         │
│                    │   ▼          ▼          ▼   │                         │
│                    │ ┌────┐   ┌────┐   ┌────┐    │                         │
│                    │ │Heur│   │ ML │   │Brand│   │                         │
│                    │ │isti│   │Mode│   │Detec│   │                         │
│                    │ │cs  │   │l   │   │tor  │   │                         │
│                    │ └────┘   └────┘   └────┘    │                         │
│                    │   17+     15       30+      │                         │
│                    │  rules  features  brands    │                         │
│                    └──────────────┬──────────────┘                         │
│                                   │                                         │
│                    ┌──────────────▼──────────────┐                         │
│                    │         DATA LAYER          │                         │
│                    │                             │                         │
│                    │  ┌─────────────────────┐    │                         │
│                    │  │  HistoryRepository  │    │                         │
│                    │  │  (SQLDelight)       │    │                         │
│                    │  └─────────────────────┘    │                         │
│                    │                             │                         │
│                    └─────────────────────────────┘                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Module Structure

```
qrshield/
├── common/                      # Shared KMP module
│   └── src/
│       ├── commonMain/          # Platform-agnostic code
│       │   └── kotlin/com/qrshield/
│       │       ├── core/        # Business logic
│       │       ├── data/        # Data layer
│       │       ├── engine/      # Detection engines
│       │       ├── ml/          # Machine learning
│       │       ├── model/       # Data models
│       │       ├── scanner/     # QR scanner interface
│       │       ├── security/    # Security utilities
│       │       ├── ui/          # Shared UI components
│       │       └── utils/       # Utilities
│       ├── commonTest/          # Shared tests
│       ├── androidMain/         # Android-specific
│       ├── iosMain/             # iOS-specific
│       ├── desktopMain/         # Desktop-specific
│       └── jsMain/              # Web-specific
│
├── androidApp/                  # Android application
├── desktopApp/                  # Desktop application
└── docs/                        # Documentation
```

### Package Responsibilities

| Package | Responsibility |
|---------|---------------|
| `core` | PhishingEngine, UrlAnalyzer, RiskScorer, VerdictEngine |
| `engine` | HeuristicsEngine, BrandDetector, TldScorer, HomographDetector |
| `ml` | LogisticRegressionModel, FeatureExtractor |
| `security` | InputValidator, RateLimiter |
| `data` | HistoryRepository, ScanHistoryManager |
| `model` | RiskAssessment, Verdict, ScanResult |
| `scanner` | QrScanner interface + platform implementations |
| `ui` | SharedViewModel, UiState, theme components |

---

## Data Flow

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           DATA FLOW DIAGRAM                              │
└──────────────────────────────────────────────────────────────────────────┘

  📷 QR Code                                                   📊 Result
      │                                                            │
      ▼                                                            ▲
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐   │
│  Camera  │────▶│  Scanner │────▶│   URL    │────▶│ Analysis │───┘
│  Input   │     │  (ML Kit │     │Extracted │     │ Pipeline │
│          │     │  Vision) │     │          │     │          │
└──────────┘     └──────────┘     └────┬─────┘     └──────────┘
                                       │
                                       ▼
                              ┌────────────────┐
                              │ InputValidator │
                              │ ──────────────│
                              │ • Length check │
                              │ • Null bytes   │
                              │ • Protocol     │
                              └───────┬────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    ▼                 ▼                 ▼
            ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
            │  Heuristics  │  │   ML Model   │  │    Brand     │
            │   Engine     │  │  Inference   │  │   Detector   │
            │  ──────────  │  │  ──────────  │  │  ──────────  │
            │  17+ rules   │  │  15 features │  │  30+ brands  │
            │  scored      │  │  probability │  │  fuzzy match │
            └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
                   │                 │                 │
                   └─────────────────┼─────────────────┘
                                     ▼
                           ┌─────────────────┐
                           │   RiskScorer    │
                           │   ───────────   │
                           │ Combined Score  │
                           │    (0-100)      │
                           └────────┬────────┘
                                    │
                                    ▼
                           ┌─────────────────┐
                           │ Verdict Engine  │
                           │ ─────────────── │
                           │ SAFE/SUSPICIOUS │
                           │   /MALICIOUS    │
                           └────────┬────────┘
                                    │
                                    ▼
                           ┌─────────────────┐
                           │ RiskAssessment  │
                           │ ─────────────── │
                           │ • score         │
                           │ • verdict       │
                           │ • flags[]       │
                           │ • confidence    │
                           └─────────────────┘
```

---

## Security Model

### Threat Defense Matrix

| Attack Vector | Detection Method | Engine |
|---------------|------------------|--------|
| Typosquatting | Character substitution patterns | BrandDetector |
| Homograph | Unicode confusable detection | HomographDetector |
| Combosquatting | Brand + keyword patterns | BrandDetector |
| Subdomain abuse | Brand in subdomain check | BrandDetector |
| Credential harvesting | Query param inspection | HeuristicsEngine |
| URL obfuscation | Encoding analysis | HeuristicsEngine |
| IP address hosting | IP vs domain check | HeuristicsEngine |
| Risky TLDs | TLD abuse database | TldScorer |

### Input Security

```kotlin
// All inputs validated before processing
InputValidator.validateUrl(url) returns:
  - ValidationResult.Valid(sanitizedUrl)
  - ValidationResult.Invalid(reason, ErrorCode)

// Validation checks:
- Length: max 2048 characters
- Null bytes: rejected
- Control characters: rejected  
- Protocol: http/https only
- Dangerous schemes: javascript:, data:, vbscript: blocked
```

### Privacy Guarantees

1. **No Network Calls**: Analysis performed entirely on-device
2. **No Telemetry**: Zero tracking or analytics
3. **Local Storage**: History encrypted with platform keystore
4. **No Cloud**: ML model embedded, no external inference

---

## Platform Implementations

### QrScanner Interface

```kotlin
interface QrScanner {
    fun scanFromCamera(): Flow<ScanResult>
    suspend fun scanFromImage(imageBytes: ByteArray): ScanResult
    fun stopScanning()
    suspend fun hasCameraPermission(): Boolean
    suspend fun requestCameraPermission(): Boolean
}
```

### Platform Implementation Matrix

| Platform | Scanner Library | Camera API | Storage |
|----------|-----------------|------------|---------|
| Android | Google ML Kit | CameraX | SQLDelight Android |
| iOS | Vision Framework | AVFoundation | SQLDelight Native |
| Desktop | ZXing | OpenCV | SQLDelight JVM |
| Web | jsQR | MediaDevices | SQLDelight Web |

---

## Analysis Pipeline

### Scoring Formula

```
Final Score = (
    Heuristic Score × 0.40 +
    ML Model Score × 0.35 +
    Brand Score × 0.15 +
    TLD Score × 0.10
) × 100
```

### Verdict Thresholds

| Score Range | Verdict | Action |
|-------------|---------|--------|
| 0-30 | SAFE | URL appears legitimate |
| 31-70 | SUSPICIOUS | Proceed with caution |
| 71-100 | MALICIOUS | Do not visit |

### Heuristic Rules (17+)

| Rule | Weight | Description |
|------|--------|-------------|
| HTTP_NOT_HTTPS | 15 | No TLS encryption |
| IP_ADDRESS_HOST | 20 | IP instead of domain |
| URL_SHORTENER | 8 | Redirect service |
| EXCESSIVE_SUBDOMAINS | 10 | >3 subdomain levels |
| CREDENTIAL_PARAMS | 18 | Password in query |
| AT_SYMBOL_INJECTION | 15 | URL spoofing |
| PUNYCODE_DOMAIN | 15 | IDN homograph risk |
| RISKY_EXTENSION | 25 | .exe, .scr, etc. |

---

## ML Model

### Architecture

```
Logistic Regression (Binary Classification)
├── Input: 15 normalized features
├── Weights: Trained on phishing URL dataset
├── Output: Probability [0, 1]
└── Inference: ~1ms on-device
```

### Feature Vector

| Index | Feature | Normalization |
|-------|---------|---------------|
| 0 | URL Length | /500, max 1.0 |
| 1 | Host Length | /100, max 1.0 |
| 2 | Path Length | /200, max 1.0 |
| 3 | Subdomain Count | /5, max 1.0 |
| 4 | Has HTTPS | 0 or 1 |
| 5 | IP Host | 0 or 1 |
| 6 | Domain Entropy | /5, max 1.0 |
| 7 | Path Entropy | /5, max 1.0 |
| 8 | Query Param Count | /10, max 1.0 |
| 9 | Has @ Symbol | 0 or 1 |
| 10 | Dot Count | /10, max 1.0 |
| 11 | Dash Count | /10, max 1.0 |
| 12 | Has Port | 0 or 1 |
| 13 | Shortener Domain | 0 or 1 |
| 14 | Suspicious TLD | 0 or 1 |

---

## Database Schema

### SQLDelight Schema

```sql
-- Scan History Table
CREATE TABLE ScanHistory (
    id TEXT NOT NULL PRIMARY KEY,
    url TEXT NOT NULL,
    score INTEGER NOT NULL,
    verdict TEXT NOT NULL,
    scanned_at INTEGER NOT NULL,
    source TEXT NOT NULL
);

CREATE INDEX idx_scanned_at ON ScanHistory(scanned_at DESC);
```

---

## API Reference

### Core Classes

#### PhishingEngine

```kotlin
class PhishingEngine {
    fun analyze(url: String): RiskAssessment
}
```

#### RiskAssessment

```kotlin
data class RiskAssessment(
    val score: Int,              // 0-100
    val verdict: Verdict,        // SAFE, SUSPICIOUS, MALICIOUS, UNKNOWN
    val flags: List<String>,     // Risk factors detected
    val details: UrlAnalysisResult,
    val confidence: Float        // 0.0-1.0
)
```

See [API.md](API.md) for complete API documentation.

---

## References

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [Google ML Kit](https://developers.google.com/ml-kit/vision/barcode-scanning)
