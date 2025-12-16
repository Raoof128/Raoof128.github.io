# 🔧 How to Extend Detection Rules

> A guide for contributors on adding new phishing detection heuristics.

---

## 📋 Overview

QR-SHIELD's detection engine uses a pluggable heuristics system. This guide shows you how to:

1. Add new detection rules
2. Tune existing rule weights
3. Add brands to the database
4. Write tests for new rules

---

## 🧠 Architecture Quick Reference

```
common/src/commonMain/kotlin/com/qrshield/
├── core/
│   ├── PhishingEngine.kt      ← Main orchestrator
│   └── SecurityConstants.kt   ← Centralized thresholds
├── engine/
│   ├── HeuristicsEngine.kt    ← 25+ detection rules
│   ├── BrandDetector.kt       ← Static brand impersonation (500+ brands)
│   ├── DynamicBrandDiscovery.kt ← Pattern-based brand detection
│   └── BrandDatabase.kt       ← 500+ brand patterns
├── ml/
│   ├── EnsembleModel.kt       ← 3-model ensemble
│   ├── LogisticRegressionModel.kt  ← ML scoring
│   └── FeatureExtractor.kt         ← Feature engineering
└── model/
    └── RiskAssessment.kt      ← Result data class
```

---

## ➕ Adding a New Heuristic

### Step 1: Define the Heuristic

Open `common/src/commonMain/kotlin/com/qrshield/engine/HeuristicsEngine.kt`:

```kotlin
/**
 * Detects suspicious patterns in URL query parameters.
 * Example: ?password=xxx or ?token=xxx
 */
private fun checkSuspiciousQueryParams(url: String): HeuristicResult {
    val suspiciousParams = listOf("password", "pwd", "token", "secret", "apikey")
    val query = url.substringAfter("?", "").lowercase()
    
    val found = suspiciousParams.filter { it in query }
    
    return if (found.isNotEmpty()) {
        HeuristicResult(
            name = "SUSPICIOUS_QUERY_PARAMS",
            score = 18,  // Weight: how risky is this signal?
            description = "URL contains sensitive parameter names: ${found.joinToString()}"
        )
    } else {
        HeuristicResult.NONE
    }
}
```

### Step 2: Register the Heuristic

Add your check to the `analyze()` function:

```kotlin
fun analyze(url: String): List<HeuristicResult> {
    return listOfNotNull(
        checkProtocol(url),
        checkIpAddress(url),
        checkSuspiciousTld(url),
        checkSuspiciousQueryParams(url),  // ← Add your new rule here
        // ... other checks
    ).filter { it != HeuristicResult.NONE }
}
```

### Step 3: Add Tests

Create tests in `common/src/commonTest/kotlin/com/qrshield/engine/`:

```kotlin
@Test
fun `detects password in query params`() {
    val result = heuristicsEngine.analyze("https://evil.com?password=secret123")
    
    assertTrue(result.any { it.name == "SUSPICIOUS_QUERY_PARAMS" })
    assertTrue(result.sumOf { it.score } >= 18)
}

@Test
fun `ignores safe query params`() {
    val result = heuristicsEngine.analyze("https://example.com?page=1&sort=asc")
    
    assertFalse(result.any { it.name == "SUSPICIOUS_QUERY_PARAMS" })
}
```

### Step 4: Run Tests

```bash
./gradlew :common:desktopTest --tests "*HeuristicsEngine*"
```

---

## ⚖️ Tuning Rule Weights

Weights determine how much each signal contributes to the total score.

### Centralized Constants (v1.6.1+)

All tunable constants are now centralized in `SecurityConstants.kt`:

```kotlin
// common/src/commonMain/kotlin/com/qrshield/core/SecurityConstants.kt
object SecurityConstants {
    // Score thresholds
    const val SAFE_THRESHOLD: Int = 30       // URLs below this are SAFE
    const val MALICIOUS_THRESHOLD: Int = 70  // URLs at/above this are MALICIOUS
    
    // Component weights (must sum to 1.0)
    const val HEURISTIC_WEIGHT: Float = 0.50f
    const val ML_WEIGHT: Float = 0.25f
    const val BRAND_WEIGHT: Float = 0.15f
    const val TLD_WEIGHT: Float = 0.10f
    
    // TLD risk scores
    const val FREE_TLD_SCORE: Int = 18       // .tk, .ml, .ga
    const val HIGH_RISK_TLD_SCORE: Int = 12  // .xyz, .icu
    // ... more constants
}
```

### Weight Guidelines

| Severity | Score Range | Use Case |
|----------|-------------|----------|
| 🟢 Low | 5-10 | Minor concern, edge case |
| 🟡 Medium | 15-25 | Clear warning sign |
| 🔴 High | 30-50 | Strong phishing indicator |
| 🚨 Critical | 50+ | Almost certainly malicious |

### Current Weight Reference

| Heuristic | Weight | Rationale |
|-----------|--------|-----------|
| `IP_ADDRESS_HOST` | 25 | Direct IPs are rarely used by legitimate sites |
| `SUSPICIOUS_TLD` | 20 | .tk/.ml/.xyz have high phishing rates |
| `NO_HTTPS` | 15 | Security issue but not definitive |
| `URL_SHORTENER` | 20 | Cannot verify destination |
| `BRAND_IMPERSONATION` | 35 | Strong phishing indicator |
| `CREDENTIAL_PATH` | 10 | Common but not conclusive |

### Adjusting Weights

**Preferred:** Edit values in `SecurityConstants.kt` for centralized tuning.

**Alternative:** Override locally in the heuristic (document why it differs):

```kotlin
private fun checkSuspiciousTld(url: String): HeuristicResult {
    // Using SecurityConstants for consistency
    val score = SecurityConstants.FREE_TLD_SCORE
    return HeuristicResult(
        name = "SUSPICIOUS_TLD",
        score = score,
        description = "..."
    )
}
```

---

## 🏢 Adding Brands to the Database

### Step 1: Edit BrandDatabase.kt

Open `common/src/commonMain/kotlin/com/qrshield/engine/BrandDatabase.kt`:

```kotlin
private val brands = listOf(
    Brand(
        name = "Afterpay",
        domains = listOf("afterpay.com", "afterpay.com.au"),
        category = "FINANCE",
        typoPatterns = listOf("aftepay", "afterpa1", "after-pay"),
        keywords = listOf("bnpl", "payment", "installment")
    ),
    // ... existing brands
)
```

### Step 2: Brand Data Structure

```kotlin
data class Brand(
    val name: String,           // Display name
    val domains: List<String>,  // Official domains
    val category: String,       // FINANCE, TECH, RETAIL, GOV, etc.
    val typoPatterns: List<String> = emptyList(),  // Common typos
    val keywords: List<String> = emptyList()        // Related words
)
```

### Step 3: Add Brand Tests

```kotlin
@Test
fun `detects Afterpay typosquat`() {
    val result = brandDetector.detect("https://aftepay-verify.tk/login")
    
    assertEquals("Afterpay", result.brandName)
    assertEquals(MatchType.TYPOSQUAT, result.matchType)
}
```

---

## 🧪 Testing Best Practices

### 1. Test Both Detection and Non-Detection

```kotlin
@Test
fun `detects malicious pattern`() {
    assertTrue(result.score > 60)
}

@Test
fun `does not flag legitimate pattern`() {
    assertTrue(result.score < 30)
}
```

### 2. Use Real-World Examples

```kotlin
// Good: Real attack pattern (defanged)
val phishUrl = "https://commbank-verify[.]ml/login"

// Bad: Synthetic pattern that may not reflect reality
val phishUrl = "https://evil-url-12345.com"
```

### 3. Test Edge Cases

```kotlin
@Test
fun `handles empty URL`() {
    val result = engine.analyze("")
    assertNotNull(result)
}

@Test
fun `handles very long URL`() {
    val result = engine.analyze("https://x.com/" + "a".repeat(2000))
    assertTrue(result.score <= 100)
}
```

---

## 🔄 PR Checklist for New Rules

Before submitting a PR with new detection rules:

- [ ] **Unit tests pass**: `./gradlew :common:desktopTest`
- [ ] **Detekt passes**: `./gradlew detekt`
- [ ] **No false positives on safe URLs**: Test against google.com, github.com, etc.
- [ ] **KDoc documented**: What does the rule detect? Why is it risky?
- [ ] **Weight justified**: Why this score value?
- [ ] **Edge cases handled**: Empty input, malformed URLs, etc.

---

## 📊 Validating New Rules

### Run Full Evaluation

```bash
./scripts/eval.sh
```

### Check for Regression

Compare precision/recall before and after your change:

| Metric | Before | After | Change |
|--------|--------|-------|--------|
| Precision | 85.2% | ? | |
| Recall | 89.1% | ? | |
| F1 Score | 87.1% | ? | |

A good rule should:
- ✅ Increase or maintain recall (catch more phishing)
- ✅ Maintain or only slightly decrease precision (few false positives)
- ❌ Avoid large precision drops (causes user fatigue)

---

## 📚 Further Reading

- [ARCHITECTURE.md](ARCHITECTURE.md) — System design overview
- [EVALUATION.md](EVALUATION.md) — How we measure accuracy
- [THREAT_MODEL.md](THREAT_MODEL.md) — Attack taxonomy
- [ATTACK_DEMOS.md](ATTACK_DEMOS.md) — Curated attack examples

---

## 🤝 Getting Help

- **Questions?** Open a GitHub Discussion
- **Bug found?** Open a GitHub Issue
- **Ready to PR?** Follow the checklist above!

---

*Last updated: December 2025*
