# 📐 Kotlin Code Style Guide

> QR-SHIELD follows official Kotlin conventions with security-focused additions.

---

## Quick Reference

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `PhishingEngine`, `UrlAssessment` |
| Functions | camelCase | `analyzeUrl()`, `detectBrand()` |
| Properties | camelCase | `riskScore`, `isSecure` |
| Constants | SCREAMING_SNAKE | `MAX_SCORE`, `DEFAULT_THRESHOLD` |
| Packages | lowercase | `com.qrshield.engine` |

---

## 1. Naming Conventions

### Classes and Interfaces

```kotlin
// ✅ Good - descriptive, PascalCase
class PhishingEngine
class BrandDetector
interface UrlAnalyzer
sealed class ScanResult

// ❌ Bad - unclear, wrong case
class phishing_engine
class Detector  // Too vague
class IProcessor  // Hungarian notation
```

### Functions

```kotlin
// ✅ Good - verb phrases, descriptive
fun analyzeUrl(url: String): RiskAssessment
fun detectBrandImpersonation(host: String): BrandMatch?
fun calculateWeightedScore(signals: List<Signal>): Int

// ❌ Bad - abbreviations, unclear
fun a(u: String)  // What does this do?
fun proc()        // Process what?
fun doIt()        // Too vague
```

### Properties and Variables

```kotlin
// ✅ Good - descriptive nouns
val riskScore: Int
val isSecure: Boolean
val detectionResult: DetectionResult
private val _mutableState = MutableStateFlow<State>()
val publicState: StateFlow<State> = _mutableState

// ❌ Bad - single letters, unclear types
val x: Int
val flag: Boolean  // Flag for what?
val data: Any      // Type is too broad
```

### Constants

```kotlin
// ✅ Good - SCREAMING_SNAKE_CASE in companion object
companion object {
    const val MAX_URL_LENGTH = 2048
    const val DEFAULT_THRESHOLD = 0.5f
    const val SUSPICIOUS_SCORE_THRESHOLD = 30
    
    // Collections that act as constants
    val RISKY_TLDS = setOf("tk", "ml", "ga", "cf", "gq")
}

// ❌ Bad - wrong case, not in companion
val max_url_length = 2048  // Should be const
const val maxLength = 2048  // Wrong case
```

---

## 2. Documentation (KDoc)

### Class Documentation

```kotlin
/**
 * Phishing detection engine for QR-SHIELD.
 *
 * Orchestrates URL analysis using multiple detection strategies:
 * - Heuristic rule matching
 * - Machine learning scoring
 * - Brand impersonation detection
 *
 * ## Usage
 * ```kotlin
 * val engine = PhishingEngine()
 * val result = engine.analyze("https://suspicious.tk/login")
 * when (result.verdict) {
 *     Verdict.SAFE -> // Allow
 *     Verdict.MALICIOUS -> // Block
 * }
 * ```
 *
 * ## Thread Safety
 * This class is thread-safe. All internal state is immutable.
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 * @see HeuristicsEngine
 * @see BrandDetector
 */
class PhishingEngine { /* ... */ }
```

### Function Documentation

```kotlin
/**
 * Analyze a URL for phishing indicators.
 *
 * @param url The URL to analyze (must be valid HTTP/HTTPS)
 * @return [RiskAssessment] containing score, verdict, and flags
 * @throws IllegalArgumentException if URL exceeds [MAX_URL_LENGTH]
 *
 * @sample com.qrshield.samples.EngineUsageSample.analyzeUrl
 */
fun analyze(url: String): RiskAssessment
```

---

## 3. Null Safety

```kotlin
// ✅ Good - explicit null handling
fun extractHost(url: String): String? {
    return try {
        URL(url).host
    } catch (e: MalformedURLException) {
        null
    }
}

// Using the nullable result
val host = extractHost(url)
val result = host?.let { analyzeHost(it) } ?: FallbackResult

// ✅ Good - require for preconditions
fun analyze(url: String): RiskAssessment {
    require(url.length <= MAX_URL_LENGTH) {
        "URL exceeds maximum length of $MAX_URL_LENGTH"
    }
    // ...
}

// ❌ Bad - null assertion without reason
val host = extractHost(url)!!  // Will crash if null
```

---

## 4. Coroutines and Async

```kotlin
// ✅ Good - proper dispatcher usage
class ScannerViewModel(
    private val engine: PhishingEngine,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    fun analyze(url: String) {
        viewModelScope.launch {
            _state.value = ScanState.Loading
            
            val result = withContext(defaultDispatcher) {
                engine.analyze(url)
            }
            
            _state.value = ScanState.Success(result)
        }
    }
}

// ❌ Bad - blocking on main thread
fun analyze(url: String) {
    val result = runBlocking { engine.analyze(url) }  // Blocks UI!
}
```

---

## 5. Data Classes

```kotlin
// ✅ Good - immutable with meaningful defaults
data class RiskAssessment(
    val url: String,
    val score: Int,
    val verdict: Verdict,
    val flags: List<String> = emptyList(),
    val confidence: Float = 0.0f,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    init {
        require(score in 0..100) { "Score must be 0-100, got $score" }
    }
    
    val isHighRisk: Boolean
        get() = score >= 70
}

// ❌ Bad - mutable, no validation
data class Result(
    var score: Int,        // Mutable!
    var flags: MutableList<String>  // Mutable collection!
)
```

---

## 6. Sealed Classes for State

```kotlin
// ✅ Good - exhaustive state modeling
sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Analyzing(val progress: Float) : ScanState()
    data class Success(val result: RiskAssessment) : ScanState()
    data class Error(val message: String, val retryable: Boolean) : ScanState()
}

// Usage with when - compiler enforces exhaustiveness
fun render(state: ScanState) = when (state) {
    is ScanState.Idle -> showIdleUI()
    is ScanState.Scanning -> showCamera()
    is ScanState.Analyzing -> showProgress(state.progress)
    is ScanState.Success -> showResult(state.result)
    is ScanState.Error -> showError(state.message, state.retryable)
}
```

---

## 7. Extension Functions

```kotlin
// ✅ Good - focused, well-named extensions
fun String.containsHomographCharacters(): Boolean {
    val cyrillicRange = '\u0400'..'\u04FF'
    return any { it in cyrillicRange }
}

fun String.extractTld(): String? {
    val lastDot = lastIndexOf('.')
    return if (lastDot > 0) substring(lastDot + 1) else null
}

// Usage
if (url.containsHomographCharacters()) {
    addFlag("HOMOGRAPH_DETECTED")
}

// ❌ Bad - too broad, modifies unrelated types
fun Any.toJson(): String  // Too broad
fun String.process(): String  // What does "process" mean?
```

---

## 8. Scope Functions

```kotlin
// ✅ Good - appropriate scope function usage

// let - for null checks and transformations
val host = extractHost(url)?.let { normalizeHost(it) }

// apply - for object configuration
val engine = PhishingEngine().apply {
    enableStrictMode()
    setThreshold(0.7f)
}

// also - for side effects (logging, analytics)
val result = engine.analyze(url).also { 
    logger.info("Analyzed: ${it.verdict}")
}

// run - for scoped computation
val assessment = run {
    val heuristics = heuristicsEngine.analyze(url)
    val ml = mlModel.predict(features)
    combineResults(heuristics, ml)
}

// ❌ Bad - nested scope functions (hard to read)
url?.let { u ->
    extractHost(u)?.let { h ->
        analyzeHost(h)?.let { r ->
            // 3 levels deep - use early returns instead
        }
    }
}
```

---

## 9. Error Handling

```kotlin
// ✅ Good - Result type for expected failures
fun parseUrl(url: String): Result<ParsedUrl> {
    return try {
        Result.success(ParsedUrl.from(url))
    } catch (e: MalformedURLException) {
        Result.failure(e)
    }
}

// ✅ Good - sealed class for domain errors
sealed class AnalysisError {
    data class InvalidUrl(val reason: String) : AnalysisError()
    data class TooLong(val length: Int) : AnalysisError()
    object RateLimited : AnalysisError()
}

// ❌ Bad - using exceptions for control flow
fun analyze(url: String): RiskAssessment {
    if (!isValid(url)) throw InvalidUrlException()  // Don't throw for expected cases
}
```

---

## 10. Testing Style

```kotlin
class HeuristicsEngineTest {
    
    private lateinit var engine: HeuristicsEngine
    
    @BeforeTest
    fun setup() {
        engine = HeuristicsEngine()
    }
    
    // ✅ Good - descriptive test names with backticks
    @Test
    fun `should flag HTTP URLs as insecure`() {
        val result = engine.analyze("http://example.com")
        
        assertTrue(result.flags.any { it.contains("HTTP") })
    }
    
    @Test
    fun `should return score of 0 for perfectly safe URL`() {
        val result = engine.analyze("https://www.google.com")
        
        assertEquals(0, result.score)
    }
    
    // ❌ Bad - unclear test names
    @Test
    fun test1() { /* What does this test? */ }
    
    @Test  
    fun testHttp() { /* What aspect of HTTP? */ }
}
```

---

## Static Analysis

We enforce style with **Detekt**:

```bash
# Check style
./gradlew detekt

# View report
open build/reports/detekt/detekt.html
```

Key rules enforced:
- `MagicNumber` - Use named constants
- `MaxLineLength` - 120 characters max
- `WildcardImport` - No `import package.*`
- `UnnecessaryLet` - Avoid redundant scope functions

---

*Following these conventions keeps QR-SHIELD's codebase readable, maintainable, and professional.*
