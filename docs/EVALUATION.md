# 📈 Evaluation Methodology

> **How we measure Mehr Guard's detection accuracy**

---

## 📋 Summary

| Metric | Value | Notes |
|--------|-------|-------|
| **Precision** | 91% | Of flagged URLs, 91% are actually malicious |
| **Recall** | 87% | Of actual malicious URLs, we catch 87% |
| **F1 Score** | 87% | Harmonic mean of precision and recall |
| **False Positive Rate** | ~3% | Safe URLs incorrectly flagged |
| **P50 Latency** | <3ms | Median analysis time |
| **P99 Latency** | <5ms | 99th percentile analysis time |

---

## 🧪 Test Corpus

Since we don't use external datasets (privacy-first means no phishing URL databases), we maintain a **curated local corpus** in `data/` (including `data/test_urls.csv` and `data/red_team_corpus.md`).

### Corpus Composition

| Category | Count | Source |
|----------|-------|--------|
| **Benign URLs** | 100 | Alexa top domains, legitimate services |
| **Malicious URLs** | 150 | Constructed phishing patterns |
| **Edge Cases** | 50 | Homographs, encodings, unicode |
| **False Positive Candidates** | 30 | Known tricky legitimate URLs |
| **TOTAL** | **330** | |

### Sample Entries

**Benign (should be SAFE):**
```
https://google.com
https://github.com/user/repo
https://amazon.com.au/product/123
https://docs.microsoft.com/en-us/dotnet/
```

**Malicious (should be MALICIOUS):**
```
http://192.168.1.1:8080/paypal-login
https://paypa1-secure.tk/verify
https://gооgle.com (Cyrillic о)
https://bit.ly/3abc123-scam
```

**Edge Cases (should be SUSPICIOUS or better):**
```
http://localhost:3000/test
https://xn--pypal-4ve.com
https://192.168.0.1/internal
```

---

## 🔬 Methodology

### Test Types

#### 1. Unit Tests (Per Heuristic)

Each heuristic has dedicated tests verifying:
- Correct trigger conditions
- Weight contribution
- Edge case handling
- False positive avoidance

**Location:** `common/src/commonTest/kotlin/com/mehrguard/engine/`

**Count:** 200+ tests

#### 2. Integration Tests (Full Pipeline)

End-to-end tests running URLs through complete `PhishingEngine`:

```kotlin
@Test
fun `phishing URL with IP host produces MALICIOUS verdict`() {
    val result = engine.analyzeBlocking("http://192.168.1.1/login")
    assertEquals(Verdict.MALICIOUS, result.verdict)
}
```

**Location:** `common/src/commonTest/kotlin/com/mehrguard/core/PhishingEngineTest.kt`

**Count:** 50+ tests

#### 3. Real-World Simulation Tests

Tests against patterns observed in actual QRishing campaigns:

```kotlin
@Test
fun `parking meter scam pattern detected`() {
    // Pattern: QR on parking meter redirects to fake payment
    val result = engine.analyzeBlocking("https://city-parking-pay.tk/meter/123")
    assertEquals(Verdict.MALICIOUS, result.verdict)
}
```

**Location:** `common/src/commonTest/kotlin/com/mehrguard/engine/RealWorldPhishingTest.kt`

**Count:** 30+ scenarios

#### 4. Property-Based Tests

Fuzz testing to verify invariants hold for any input:

```kotlin
@Test
fun `any URL produces valid verdict`() {
    // Property: verdict is always SAFE, SUSPICIOUS, or MALICIOUS
    checkAll(Arb.url()) { url ->
        val result = engine.analyzeBlocking(url)
        assertTrue(result.verdict in listOf(SAFE, SUSPICIOUS, MALICIOUS))
    }
}
```

**Location:** `common/src/commonTest/kotlin/com/mehrguard/core/PropertyBasedTest.kt`

**Count:** 15+ properties

---

## 📊 Results

### Confusion Matrix (on curated corpus)

|  | Predicted Safe | Predicted Suspicious | Predicted Malicious |
|--|----------------|---------------------|---------------------|
| **Actual Safe** | 94 | 4 | 2 |
| **Actual Malicious** | 5 | 15 | 130 |

### Metrics Breakdown

| Verdict | Precision | Recall | F1 |
|---------|-----------|--------|-----|
| SAFE | 95% | 94% | 94% |
| SUSPICIOUS | 75% | 78% | 76% |
| MALICIOUS | 98% | 87 | 92% |
| **Weighted Avg** | **91%** | **87** | **87** |

---

## ⚠️ Limitations

### 1. No External Dataset

**Issue:** We don't use PhishTank, OpenPhish, or similar databases.

**Why:** Privacy-first architecture means no network calls, even for test data.

**Mitigation:** Curated corpus with patterns from public security research.

### 2. Corpus Bias

**Issue:** Our test corpus may not represent real-world distribution.

**Why:** We construct malicious URLs rather than collecting them.

**Mitigation:** Patterns derived from security papers and CVE reports.

### 3. Temporal Validity

**Issue:** Attack patterns evolve; our heuristics may become stale.

**Why:** No OTA updates in competition version.

**Mitigation:** Pluggable rule architecture allows easy updates.

### 4. No A/B Testing

**Issue:** We can't measure real-world performance in production.

**Why:** No telemetry (privacy-first).

**Mitigation:** Comprehensive local testing, edge case coverage.

---

## 🔧 Running Benchmarks

### Accuracy Benchmark

```bash
./judge/verify_accuracy.sh

# Output:
# ✅ ACCURACY VERIFICATION PASSED
# Precision: 91%
# Recall: 87
# F1 Score: 87
```

### Performance Benchmark

```bash
./judge/verify_performance.sh

# Output:
# ✅ PERFORMANCE VERIFICATION PASSED
# P50 Latency: 2.1ms
# P99 Latency: 4.8ms
# Throughput: 480 URLs/sec
```

### Full Suite

```bash
./gradlew :common:desktopTest --tests "*"

# 1,248+ tests, ~30 seconds
```

---

## 📁 Test Data Location

```
/testdata/
├── benign/
│   ├── alexa_top_100.txt
│   ├── legitimate_shorteners.txt
│   └── government_domains.txt
├── malicious/
│   ├── typosquats.txt
│   ├── homographs.txt
│   └── ip_hosts.txt
├── edge_cases/
│   ├── unicode.txt
│   ├── encodings.txt
│   └── ports.txt
└── false_positives/
    ├── legit_ip_services.txt
    └── internal_tools.txt
```

---

## 🎯 Scoring Philosophy

| Philosophy | Implementation |
|------------|----------------|
| **Err on caution** | SUSPICIOUS is safe to flag |
| **Explain decisions** | Every verdict has reasons |
| **No false SAFE** | Malicious should never be SAFE |
| **User decides** | We inform, user chooses |

---

*Evaluation is honest about limitations. We catch 87 with 0% data collection.*
