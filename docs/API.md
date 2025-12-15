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
- **[Policy Engine (NEW)](#policy-engine)**
  - [OrgPolicy](#orgpolicy)
  - [PolicyResult](#policyresult)
- **[Payload Analyzer (NEW)](#payload-analyzer)**
  - [QrPayloadType](#qrpayloadtype)
  - [QrPayloadAnalyzer](#qrpayloadanalyzer)
- **[Adversarial Defense (NEW)](#adversarial-defense)**
  - [AdversarialDefense](#adversarialdefense)
  - [ObfuscationAttack](#obfuscationattack)
- **[Shared UI Components (NEW)](#shared-ui-components)**
  - [SharedTextGenerator](#sharedtextgenerator)
  - [LocalizationKeys](#localizationkeys)
  - [SharedViewModel](#sharedviewmodel)
- **[Platform Abstractions (NEW)](#platform-abstractions)**
  - [PlatformClipboard](#platformclipboard)
  - [PlatformHaptics](#platformhaptics)
  - [PlatformLogger](#platformlogger)
  - [PlatformTime](#platformtime)
  - [PlatformShare](#platformshare)
  - [PlatformSecureRandom](#platformsecurerandom)
  - [PlatformUrlOpener](#platformurlopener)
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

## Policy Engine

> **NEW in v1.2.0** — Enterprise policy enforcement for organizational deployments.

### OrgPolicy

Organization-specific security policy.

```kotlin
data class OrgPolicy(
    val version: String = "1.0",
    val orgId: String = "",
    val orgName: String = "",
    val strictMode: Boolean = false,
    val allowedDomains: Set<String> = emptySet(),
    val blockedDomains: Set<String> = emptySet(),
    val blockedTlds: Set<String> = emptySet(),
    val blockedCategories: Set<String> = emptySet(),
    val allowedBrands: Set<String> = emptySet(),
    val sensitiveBrands: Set<String> = emptySet(),
    val safeThreshold: Int? = null,
    val suspiciousThreshold: Int? = null,
    val requireHttps: Boolean = false,
    val blockIpAddresses: Boolean = false,
    val blockShorteners: Boolean = false,
    val maxUrlLength: Int? = null,
    val blockedPatterns: List<String> = emptyList(),
    val allowedPatterns: List<String> = emptyList(),
    val allowedPayloadTypes: Set<QrPayloadType>? = null
)
```

#### Factory Methods

```kotlin
// Load from JSON
OrgPolicy.fromJson(json: String): OrgPolicy

// Preset policies
OrgPolicy.DEFAULT          // Minimal restrictions
OrgPolicy.ENTERPRISE_STRICT // Block risky TLDs, require HTTPS
OrgPolicy.FINANCIAL        // Block crypto, gambling, strict mode
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `evaluate` | `url: String` | `PolicyResult` | Evaluate URL against policy |
| `evaluatePayload` | `payload: String, type: QrPayloadType` | `PolicyResult` | Evaluate non-URL payload |
| `toJson` | - | `String` | Export policy to JSON |

#### Usage Example

```kotlin
val policy = OrgPolicy(
    orgName = "Acme Corp",
    strictMode = true,
    blockedTlds = setOf("tk", "ml", "ga"),
    allowedDomains = setOf("*.acme.com"),
    requireHttps = true,
    blockShorteners = true
)

when (val result = policy.evaluate("https://suspicious.tk/phish")) {
    is PolicyResult.Allowed -> proceedDirectly()
    is PolicyResult.Blocked -> showBlockedMessage(result.reason)
    is PolicyResult.RequiresReview -> flagForReview(result.reason)
    is PolicyResult.PassedPolicy -> performFullAnalysis()
}
```

---

### PolicyResult

Sealed class for policy evaluation results.

```kotlin
sealed class PolicyResult {
    data class Allowed(val reason: String) : PolicyResult()
    data class Blocked(
        val blockReason: BlockReason,
        val details: String = ""
    ) : PolicyResult()
    data class RequiresReview(val reason: String) : PolicyResult()
    object PassedPolicy : PolicyResult()
}
```

#### BlockReason Enum

| Reason | Description |
|--------|-------------|
| `DOMAIN_BLOCKED` | Domain in blocklist |
| `TLD_BLOCKED` | TLD is blocked |
| `HTTPS_REQUIRED` | HTTP when HTTPS required |
| `IP_ADDRESS` | IP address when blocked |
| `SHORTENER` | URL shortener when blocked |
| `LENGTH_EXCEEDED` | URL too long |
| `PATTERN_MATCH` | Matched blocked pattern |
| `PAYLOAD_TYPE_BLOCKED` | Payload type not allowed |
| `SMISHING_DETECTED` | SMS contains blocked URL |

---

## Payload Analyzer

> **NEW in v1.2.0** — Analysis for non-URL QR payloads (WiFi, SMS, vCard, crypto).

### QrPayloadType

Enum of supported QR payload types.

```kotlin
enum class QrPayloadType(
    val displayName: String,
    val riskLevel: RiskLevel
) {
    // URLs
    URL("Generic URL", RiskLevel.MEDIUM),
    URL_HTTP("HTTP URL", RiskLevel.HIGH),
    URL_HTTPS("HTTPS URL", RiskLevel.LOW),
    
    // Communication
    SMS("SMS Message", RiskLevel.HIGH),
    PHONE("Phone Call", RiskLevel.MEDIUM),
    EMAIL("Email", RiskLevel.LOW),
    
    // Contact/Calendar
    VCARD("vCard Contact", RiskLevel.MEDIUM),
    MECARD("MeCard Contact", RiskLevel.MEDIUM),
    VEVENT("Calendar Event", RiskLevel.LOW),
    
    // Network
    WIFI("WiFi Config", RiskLevel.HIGH),
    
    // Location
    GEO("Geographic Location", RiskLevel.LOW),
    
    // Payments - Crypto
    BITCOIN("Bitcoin Payment", RiskLevel.CRITICAL),
    ETHEREUM("Ethereum Payment", RiskLevel.CRITICAL),
    CRYPTO_OTHER("Crypto Payment", RiskLevel.CRITICAL),
    
    // Payments - Traditional
    UPI("UPI Payment", RiskLevel.HIGH),
    PAYPAL("PayPal Payment", RiskLevel.HIGH),
    WECHAT_PAY("WeChat Pay", RiskLevel.HIGH),
    ALIPAY("Alipay", RiskLevel.HIGH),
    
    // Other
    TEXT("Plain Text", RiskLevel.LOW),
    UNKNOWN("Unknown", RiskLevel.MEDIUM)
}
```

#### Static Methods

```kotlin
QrPayloadType.detect(content: String): QrPayloadType
```

---

### QrPayloadAnalyzer

Analyzes non-URL QR payloads for security risks.

```kotlin
object QrPayloadAnalyzer {
    fun analyze(content: String): PayloadAnalysisResult
    fun analyzeWifi(content: String): PayloadAnalysisResult
    fun analyzeSms(content: String): PayloadAnalysisResult
    fun analyzeContact(content: String, type: QrPayloadType): PayloadAnalysisResult
    fun analyzeCrypto(content: String, type: QrPayloadType): PayloadAnalysisResult
}
```

#### PayloadAnalysisResult

```kotlin
data class PayloadAnalysisResult(
    val payloadType: QrPayloadType,
    val riskScore: Int,              // 0-100
    val signals: List<PayloadSignal>,
    val parsedData: Map<String, String>,
    val recommendation: String
) {
    val verdict: PayloadVerdict  // SAFE, CAUTION, SUSPICIOUS, DANGEROUS
}
```

#### PayloadSignal

```kotlin
data class PayloadSignal(
    val name: String,
    val description: String,
    val riskPoints: Int
)
```

#### Usage Example

```kotlin
val result = QrPayloadAnalyzer.analyze("WIFI:T:nopass;S:Free Airport Wifi;;")

println("Type: ${result.payloadType}")        // WIFI
println("Risk: ${result.riskScore}")          // 65
println("Verdict: ${result.verdict}")         // SUSPICIOUS

result.signals.forEach { signal ->
    println("- ${signal.name}: +${signal.riskPoints}")
}
// Output:
// - Open Network: +35
// - Suspicious SSID: free: +15
// - Suspicious SSID: airport: +15
```

---

## Adversarial Defense

> **NEW in v1.2.0** — Detection of URL obfuscation attacks.

### AdversarialDefense

Detects and normalizes obfuscated URLs.

```kotlin
object AdversarialDefense {
    fun normalize(url: String): NormalizationResult
}
```

#### NormalizationResult

```kotlin
data class NormalizationResult(
    val originalUrl: String,
    val normalizedUrl: String,
    val detectedAttacks: List<ObfuscationAttack>,
    val nestedUrls: List<String>,
    val riskScore: Int
) {
    val hasObfuscation: Boolean
    val attackSummary: String
}
```

---

### ObfuscationAttack

Enum of detectable obfuscation techniques.

```kotlin
enum class ObfuscationAttack(
    val displayName: String,
    val description: String,
    val riskScore: Int
) {
    ZERO_WIDTH_CHARACTERS("Zero-Width Characters", "...", 30),
    RTL_OVERRIDE("RTL Override", "...", 40),
    DOUBLE_ENCODING("Double Encoding", "...", 35),
    UNNECESSARY_ENCODING("Unnecessary Encoding", "...", 15),
    MIXED_CASE_ENCODING("Mixed Case Encoding", "...", 10),
    MIXED_SCRIPTS("Mixed Scripts (Homograph)", "...", 45),
    COMBINING_MARKS("Combining Marks", "...", 25),
    PUNYCODE_DOMAIN("Punycode Domain", "...", 20),
    NESTED_REDIRECTS("Nested Redirects", "...", 30),
    UNICODE_NORMALIZATION("Unicode Normalization", "...", 15),
    DECIMAL_IP("Decimal IP Address", "...", 25),
    OCTAL_IP("Octal IP Address", "...", 30),
    HEX_IP("Hexadecimal IP Address", "...", 30),
    MIXED_IP_NOTATION("Mixed IP Notation", "...", 35)
}
```

#### Usage Example

```kotlin
val result = AdversarialDefense.normalize("https://аpple.com/verify")

if (result.hasObfuscation) {
    println("Attacks detected: ${result.attackSummary}")
    // "Mixed Scripts (Homograph)"
    
    println("Risk score: ${result.riskScore}")  // 45
    
    result.detectedAttacks.forEach { attack ->
        println("- ${attack.displayName}: +${attack.riskScore}")
    }
}
```

---

## Shared UI Components

> **NEW in v1.2.0** — Shared text generation and localization for platform parity.

### SharedTextGenerator

Centralized text generation ensuring identical messaging across all platforms.

```kotlin
object SharedTextGenerator {
    // Verdict text
    fun getVerdictTitle(verdict: Verdict): String
    fun getVerdictDescription(verdict: Verdict): String
    fun getVerdictAccessibilityLabel(verdict: Verdict, score: Int): String

    // Score text
    fun getScoreDescription(score: Int): String
    fun getScoreRangeLabel(score: Int): String

    // Risk explanations
    fun getRiskExplanation(assessment: RiskAssessment): String
    fun getShortRiskSummary(assessment: RiskAssessment): String

    // Action recommendations
    fun getRecommendedAction(verdict: Verdict): String
    fun getActionGuidance(verdict: Verdict): List<String>

    // Signal explanations
    val signalExplanations: Map<String, SignalExplanation>
    fun getSignalExplanation(signalId: String): SignalExplanation?

    // Export formats
    fun generateShareText(url: String, assessment: RiskAssessment): String
    fun generateJsonExport(url: String, assessment: RiskAssessment, timestamp: Long): String
}
```

#### Usage Example

```kotlin
val explanation = SharedTextGenerator.getRiskExplanation(assessment)
val shareText = SharedTextGenerator.generateShareText(url, assessment)
```

---

### LocalizationKeys

~80 centralized localization keys for all platforms.

```kotlin
object LocalizationKeys {
    val APP_NAME = LocalizedKey("app_name", "QR-SHIELD")
    val VERDICT_SAFE = LocalizedKey("verdict_safe", "Safe")
    val VERDICT_SUSPICIOUS = LocalizedKey("verdict_suspicious", "Suspicious")
    val VERDICT_MALICIOUS = LocalizedKey("verdict_malicious", "Dangerous")
    // ...~80 more keys for tabs, scanner, results, actions, history, settings
}

data class LocalizedKey(val key: String, val defaultText: String)
```

#### Key Categories

| Category | Examples |
|----------|----------|
| **App** | `APP_NAME`, `APP_TAGLINE` |
| **Tabs** | `TAB_SCAN`, `TAB_HISTORY`, `TAB_SETTINGS` |
| **Scanner** | `SCANNER_TITLE`, `SCANNER_INSTRUCTION` |
| **Verdicts** | `VERDICT_SAFE`, `VERDICT_SUSPICIOUS`, `VERDICT_MALICIOUS` |
| **Actions** | `ACTION_COPY_URL`, `ACTION_SHARE`, `ACTION_REPORT` |
| **Errors** | `ERROR_INVALID_URL`, `ERROR_NO_QR_FOUND` |
| **Accessibility** | `A11Y_RISK_SCORE_LABEL`, `A11Y_SCAN_BUTTON` |

---

### SharedViewModel

Shared state machine for UI across all platforms.

```kotlin
class SharedViewModel(
    phishingEngine: PhishingEngine,
    historyRepository: HistoryRepository,
    coroutineScope: CoroutineScope
) {
    val uiState: StateFlow<UiState>
    val history: StateFlow<List<ScanHistoryItem>>
    val settings: StateFlow<AppSettings>

    fun processScanResult(result: ScanResult, source: ScanSource)
    fun analyzeUrl(url: String, source: ScanSource = ScanSource.CLIPBOARD)
    fun clearHistory()
    fun deleteScan(id: String)
    fun generateShareContent(): ShareContent?
}
```

---

## Platform Abstractions

> **NEW in v1.2.0** — Strategic expect/actual declarations with documented native boundaries.

All platform abstractions follow this pattern:
- `commonMain`: expect declaration with WHY documentation
- `androidMain`, `iosMain`, `desktopMain`, `jsMain`: actual implementations

### PlatformClipboard

System clipboard operations.

```kotlin
expect object PlatformClipboard {
    fun copyToClipboard(text: String): Boolean
    fun getClipboardText(): String?
    fun hasText(): Boolean
}
```

**Why Native:** ClipboardManager (Android), UIPasteboard (iOS), AWT (Desktop), navigator.clipboard (Web)

---

### PlatformHaptics

Tactile feedback.

```kotlin
expect object PlatformHaptics {
    fun light()   // Button taps
    fun medium()  // Confirmations
    fun heavy()   // Important actions
    fun success() // Safe verdict
    fun warning() // Suspicious verdict
    fun error()   // Malicious verdict
}
```

**Why Native:** Vibrator (Android), UIImpactFeedbackGenerator (iOS), no-op (Desktop)

---

### PlatformLogger

Platform-appropriate logging.

```kotlin
expect object PlatformLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}
```

**Why Native:** Logcat (Android), OSLog (iOS), java.util.logging (Desktop), console (Web)

---

### PlatformTime

High-resolution time operations.

```kotlin
expect object PlatformTime {
    fun currentTimeMillis(): Long
    fun nanoTime(): Long
    fun formatTimestamp(millis: Long): String
    fun formatRelativeTime(millis: Long): String
}
```

**Why Native:** System.nanoTime (JVM), CFAbsoluteTimeGetCurrent (iOS), performance.now (Web)

---

### PlatformShare

System share sheet integration.

```kotlin
expect object PlatformShare {
    fun shareText(text: String, title: String = ""): Boolean
    fun isShareSupported(): Boolean
}
```

**Why Native:** Intent.ACTION_SEND (Android), UIActivityViewController (iOS), Web Share API

---

### PlatformSecureRandom

Cryptographically secure random generation.

```kotlin
expect object PlatformSecureRandom {
    fun nextBytes(size: Int): ByteArray
    fun randomUUID(): String
}
```

**Why Native:** SecureRandom (JVM), SecRandomCopyBytes (iOS), crypto.getRandomValues (Web)

---

### PlatformUrlOpener

Open URLs in default browser.

```kotlin
expect object PlatformUrlOpener {
    fun openUrl(url: String): Boolean
    fun canOpenUrl(url: String): Boolean
}
```

**Why Native:** Intent.ACTION_VIEW (Android), UIApplication.openURL (iOS), Desktop.browse (JVM)

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

API Version: 1.2.0  
Last Updated: 2025-12-15
