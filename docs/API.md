# QR-SHIELD API Reference

> Complete API documentation for QR-SHIELD SDK

## Table of Contents

- [Quick Start](#quick-start)
- [Core API](#core-api)
  - [PhishingEngine](#phishingengine)
  - [RiskAssessment](#riskassessment)
  - [Verdict](#verdict)
- [Detection Engines](#detection-engines)
  - [HeuristicsEngine](#heuristicsengine)
  - [BrandDetector](#branddetector)
  - [TldScorer](#tldscorer)
- [ML Model](#ml-model)
  - [LogisticRegressionModel](#logisticregressionmodel)
  - [FeatureExtractor](#featureextractor)
- [Security Utilities](#security-utilities)
  - [InputValidator](#inputvalidator)
  - [RateLimiter](#ratelimiter)
- [Data Layer](#data-layer)
  - [HistoryRepository](#historyrepository)
- [Scanner Interface](#scanner-interface)
  - [QrScanner](#qrscanner)
  - [ScanResult](#scanresult)
- [Error Handling](#error-handling)

---

## Quick Start

```kotlin
import com.qrshield.core.PhishingEngine
import com.qrshield.model.Verdict

// Create engine instance
val engine = PhishingEngine()

// Analyze a URL
val result = engine.analyze("https://example.com")

// Check verdict
when (result.verdict) {
    Verdict.SAFE -> println("Safe to visit")
    Verdict.SUSPICIOUS -> println("Proceed with caution")
    Verdict.MALICIOUS -> println("Do not visit!")
    Verdict.UNKNOWN -> println("Unable to determine")
}

// Access detailed analysis
println("Score: ${result.score}/100")
println("Confidence: ${(result.confidence * 100).toInt()}%")
println("Risk factors: ${result.flags.joinToString()}")
```

---

## Core API

### PhishingEngine

Main orchestrator for URL phishing analysis.

```kotlin
class PhishingEngine(
    heuristicsEngine: HeuristicsEngine = HeuristicsEngine(),
    brandDetector: BrandDetector = BrandDetector(),
    tldScorer: TldScorer = TldScorer(),
    mlModel: LogisticRegressionModel = LogisticRegressionModel.default(),
    featureExtractor: FeatureExtractor = FeatureExtractor()
)
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `analyze` | `url: String` | `RiskAssessment` | Perform complete phishing analysis |

#### Example

```kotlin
val engine = PhishingEngine()
val result = engine.analyze("https://paypa1.com/login")

if (result.verdict == Verdict.MALICIOUS) {
    showWarning("Phishing detected: ${result.details.brandMatch}")
}
```

---

### RiskAssessment

Complete analysis result.

```kotlin
data class RiskAssessment(
    val score: Int,
    val verdict: Verdict,
    val flags: List<String>,
    val details: UrlAnalysisResult,
    val confidence: Float = 0.8f
)
```

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| `score` | `Int` | Risk score 0-100 |
| `verdict` | `Verdict` | Classification result |
| `flags` | `List<String>` | Human-readable risk factors |
| `details` | `UrlAnalysisResult` | Detailed breakdown |
| `confidence` | `Float` | Confidence level 0.0-1.0 |

#### Computed Properties

```kotlin
val scoreDescription: String  // "Low Risk", "Medium Risk", "High Risk"
val actionRecommendation: String  // User-facing recommendation
```

---

### Verdict

Risk classification enum.

```kotlin
enum class Verdict {
    SAFE,       // Score 0-30: URL appears legitimate
    SUSPICIOUS, // Score 31-70: Proceed with caution
    MALICIOUS,  // Score 71-100: Strong phishing indicators
    UNKNOWN     // Unable to analyze
}
```

---

## Detection Engines

### HeuristicsEngine

Applies 17+ security heuristics to detect phishing patterns.

```kotlin
class HeuristicsEngine(
    urlAnalyzer: UrlAnalyzer = UrlAnalyzer()
)
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `analyze` | `url: String` | `Result` | Run all heuristic checks |

#### Result Type

```kotlin
data class Result(
    val score: Int,           // Total heuristic score
    val flags: List<String>,  // Triggered rules
    val details: Map<String, Int>  // Rule -> weight
)
```

#### Heuristic Rules

| Rule ID | Weight | Trigger Condition |
|---------|--------|-------------------|
| `HTTP_NOT_HTTPS` | 15 | Uses HTTP protocol |
| `IP_ADDRESS_HOST` | 20 | Host is IP address |
| `URL_SHORTENER` | 8 | Known shortener service |
| `EXCESSIVE_SUBDOMAINS` | 10 | >3 subdomain levels |
| `NON_STANDARD_PORT` | 8 | Port not 80/443/8080/8443 |
| `LONG_URL` | 5 | URL >200 characters |
| `HIGH_ENTROPY_HOST` | 12 | Entropy >4.0 |
| `SUSPICIOUS_PATH_KEYWORDS` | 5-20 | login, verify, etc. |
| `CREDENTIAL_PARAMS` | 18 | password= in query |
| `AT_SYMBOL_INJECTION` | 15 | @ symbol in URL path |
| `MULTIPLE_TLD_SEGMENTS` | 10 | Multiple TLD-like segments |
| `PUNYCODE_DOMAIN` | 15 | Contains xn-- |
| `NUMERIC_SUBDOMAIN` | 8 | Digit-only subdomain |
| `RISKY_EXTENSION` | 25 | .exe, .scr, .bat, etc. |
| `DOUBLE_EXTENSION` | 20 | file.pdf.exe pattern |
| `ENCODED_PAYLOAD` | 10 | Base64-like in query |
| `EXCESSIVE_ENCODING` | 8 | >10% URL encoded |

---

### BrandDetector

Detects brand impersonation attacks.

```kotlin
class BrandDetector()
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `detect` | `url: String` | `DetectionResult` | Check for brand impersonation |
| `detectBatch` | `urls: List<String>` | `Map<String, DetectionResult>` | Batch detection |

#### DetectionResult

```kotlin
data class DetectionResult(
    val score: Int,               // Brand impersonation score
    val match: String?,           // Brand name if detected
    val details: BrandMatch?      // Match details
) {
    val isImpersonation: Boolean  // true if match found
    val severity: String          // CRITICAL, HIGH, MEDIUM, LOW, NONE
}
```

#### Match Types

| Type | Score | Description |
|------|-------|-------------|
| `HOMOGRAPH` | 40 | Unicode confusable characters |
| `TYPOSQUAT` | 35 | Character substitution (paypa1) |
| `EXACT_IN_SUBDOMAIN` | 30 | Brand in subdomain |
| `COMBO_SQUAT` | 25 | Brand + keyword (paypal-login) |
| `FUZZY_MATCH` | 20 | Levenshtein distance ≤2 |

#### Supported Brands (30+)

| Category | Brands |
|----------|--------|
| Financial | PayPal, Stripe, CommBank, NAB, Westpac, ANZ, Bendigo |
| Technology | Google, Microsoft, Apple, Amazon |
| Social | Facebook, Instagram, Twitter, LinkedIn, TikTok |
| Entertainment | Netflix, Spotify |
| Logistics | AusPost, DHL, FedEx |
| Government | myGov, ATO |

---

### TldScorer

Scores TLDs based on historical abuse data.

```kotlin
class TldScorer()
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `score` | `url: String` | `TldResult` | Score TLD risk |
| `scoreBatch` | `urls: List<String>` | `Map<String, TldResult>` | Batch scoring |
| `isHighRiskTld` | `tld: String` | `Boolean` | Quick risk check |

#### TldResult

```kotlin
data class TldResult(
    val tld: String,
    val score: Int,
    val isHighRisk: Boolean,
    val riskCategory: RiskCategory
)
```

#### Risk Categories

| Category | Score | Example TLDs |
|----------|-------|--------------|
| `SAFE` | 0 | .com, .org, .edu, .gov |
| `COUNTRY_CODE` | 3 | .au, .uk, .de |
| `MODERATE` | 5 | .io, .co, .me |
| `UNKNOWN` | 8 | New/rare TLDs |
| `HIGH_RISK` | 12 | .xyz, .icu, .club |
| `FREE_TIER` | 18 | .tk, .ml, .ga |

---

## ML Model

### LogisticRegressionModel

On-device ML inference for phishing classification.

```kotlin
class LogisticRegressionModel private constructor(
    weights: FloatArray,
    bias: Float
)
```

#### Factory Methods

```kotlin
// Create with default pre-trained weights
LogisticRegressionModel.default(): LogisticRegressionModel

// Create with custom weights
LogisticRegressionModel.create(
    weights: FloatArray,  // Must have 15 elements
    bias: Float
): LogisticRegressionModel

// Load from JSON
LogisticRegressionModel.fromJson(json: String): LogisticRegressionModel
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `predict` | `features: FloatArray` | `Float` | Probability 0.0-1.0 |
| `predictWithThreshold` | `features: FloatArray, threshold: Float` | `Prediction` | Classification with confidence |

#### Prediction

```kotlin
data class Prediction(
    val isPhishing: Boolean,
    val probability: Float,
    val confidence: Float
) {
    val riskLevel: String  // "Low", "Medium", "High"
}
```

---

### FeatureExtractor

Extracts normalized features from URLs.

```kotlin
class FeatureExtractor()
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `extract` | `url: String` | `FloatArray` | 15-element feature vector |

---

## Security Utilities

### InputValidator

Comprehensive input validation.

```kotlin
object InputValidator
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `validateUrl` | `url: String?` | `ValidationResult<String>` | Validate URL |
| `validateTextInput` | `input: String?, maxLength: Int` | `ValidationResult<String>` | Validate text |
| `validateHostname` | `host: String?` | `ValidationResult<String>` | Validate hostname |
| `containsSqlInjectionPatterns` | `input: String` | `Boolean` | Detect SQLi |
| `containsXssPatterns` | `input: String` | `Boolean` | Detect XSS |

#### ValidationResult

```kotlin
sealed class ValidationResult<out T> {
    data class Valid<T>(val value: T) : ValidationResult<T>()
    data class Invalid(val reason: String, val code: ErrorCode) : ValidationResult<Nothing>()
    
    fun isValid(): Boolean
    fun getOrNull(): T?
}
```

#### Error Codes

| Code | Description |
|------|-------------|
| `EMPTY_INPUT` | Null or empty input |
| `TOO_LONG` | Exceeds maximum length |
| `CONTAINS_NULL_BYTES` | Null byte injection |
| `CONTAINS_CONTROL_CHARS` | Invalid control characters |
| `INVALID_PROTOCOL` | Protocol not allowed |
| `MALFORMED_URL` | Cannot parse URL |
| `INVALID_HOST` | Invalid hostname format |

---

### RateLimiter

Sliding window rate limiter.

```kotlin
class RateLimiter(
    maxRequests: Int,
    windowMs: Long
)
```

#### Factory Methods

```kotlin
RateLimiter.forUi(): RateLimiter       // 60 req/min
RateLimiter.forBatch(): RateLimiter    // 100 req/min
RateLimiter.forApi(): RateLimiter      // 30 req/min
RateLimiter.forSensitive(): RateLimiter // 5 req/5min
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `isAllowed` | - | `Boolean` | Check if request allowed |
| `tryAcquire` | - | `RateLimitResult` | Attempt with metadata |
| `getStatus` | - | `RateLimitStatus` | Current status |
| `reset` | - | `Unit` | Clear limiter |

---

## Data Layer

### HistoryRepository

Persistence interface for scan history.

```kotlin
interface HistoryRepository {
    suspend fun insert(item: ScanHistoryItem): Boolean
    suspend fun getAll(): List<ScanHistoryItem>
    suspend fun getRecent(limit: Int): List<ScanHistoryItem>
    suspend fun getById(id: String): ScanHistoryItem?
    suspend fun delete(id: String): Boolean
    suspend fun clearAll(): Int
    suspend fun count(): Long
    fun observe(): Flow<List<ScanHistoryItem>>
    suspend fun getByVerdict(verdict: Verdict): List<ScanHistoryItem>
}
```

#### ScanHistoryItem

```kotlin
data class ScanHistoryItem(
    val id: String,
    val url: String,
    val score: Int,
    val verdict: Verdict,
    val scannedAt: Long,
    val source: ScanSource
)
```

---

## Scanner Interface

### QrScanner

Platform-agnostic scanning interface.

```kotlin
interface QrScanner {
    fun scanFromCamera(): Flow<ScanResult>
    suspend fun scanFromImage(imageBytes: ByteArray): ScanResult
    fun stopScanning()
    suspend fun hasCameraPermission(): Boolean
    suspend fun requestCameraPermission(): Boolean
}
```

### ScanResult

```kotlin
sealed class ScanResult {
    data class Success(
        val content: String,
        val contentType: ContentType
    ) : ScanResult()
    
    data class Error(
        val message: String,
        val code: ErrorCode
    ) : ScanResult()
    
    data object NoQrFound : ScanResult()
}
```

#### ContentType

```kotlin
enum class ContentType {
    URL, TEXT, WIFI, VCARD, GEO, PHONE, SMS, EMAIL, UNKNOWN
}
```

---

## Error Handling

All APIs use defensive error handling:

```kotlin
// Core analysis never throws
val result = engine.analyze(url)  // Always returns RiskAssessment

// Validation uses sealed results
when (val validation = InputValidator.validateUrl(url)) {
    is ValidationResult.Valid -> process(validation.value)
    is ValidationResult.Invalid -> handleError(validation.reason)
}

// Repositories return success/failure
val success = repository.insert(item)  // Boolean

// Flow-based APIs emit to callbacks
scanner.scanFromCamera().collect { result ->
    when (result) {
        is ScanResult.Success -> analyze(result.content)
        is ScanResult.Error -> showError(result.message)
        is ScanResult.NoQrFound -> continue
    }
}
```

---

## Thread Safety

All public APIs are thread-safe:

- `PhishingEngine`: Stateless, safe for concurrent use
- `BrandDetector`: Immutable database
- `RateLimiter`: Synchronized internal state
- `HistoryRepository`: Mutex-protected operations

---

## Version

API Version: 1.0.0  
Last Updated: 2025-12-13
