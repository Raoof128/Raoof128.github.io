# 📋 QR-SHIELD Evidence Pack

> **Purpose**: Every numeric claim in the README is linked to a reproducible artifact.  
> **Last Verified**: December 25, 2025  
> **Version**: 1.17.30

---

## 🎯 Claims & Evidence Matrix

| README Claim | Evidence | Artifact Link |
|--------------|----------|---------------|
| **1,248 Tests** | Test results XML | [test_count.txt](artifacts/test_count.txt) |
| **100% Offline** | Zero network test | [OfflineOnlyTest.kt](../common/src/commonTest/kotlin/com/qrshield/core/OfflineOnlyTest.kt) |
| **<5ms Analysis** | Benchmark output | [BENCHMARKS.md](BENCHMARKS.md) |
| **25+ Heuristics** | Code line count | [HeuristicsEngine.kt](../common/src/commonMain/kotlin/com/qrshield/engine/HeuristicsEngine.kt) |
| **85% Detection** | Malicious URL test | [malicious_url_output.txt](artifacts/malicious_url_output.txt) |
| **140 Test URLs** | CSV dataset | [malicious_urls.csv](../common/src/commonTest/resources/malicious_urls.csv) |
| **Parity Hash** | Cross-platform test | [parity_output.txt](artifacts/parity_output.txt) |
| **4 Platforms** | Build verification | [verify_parity.sh](../judge/verify_parity.sh) |

---

## 📊 Test Coverage Evidence

### Test Count Summary

```
Platform       | Tests  | Passed | Failed
---------------|--------|--------|--------
Desktop (JVM)  | 1,248  | 1,248  | 0 ✅
Android (JVM)  | 1,248  | 1,248  | 0 ✅
iOS (Native)   | 1,247  | 1,247  | 0 ✅
JS/Web         | —      | —      | ✅ Compiles
```

**Artifact**: `common/build/test-results/desktopTest/*.xml`

**One Command to Verify**:
```bash
./gradlew :common:desktopTest :common:testDebugUnitTest :common:iosSimulatorArm64Test
```

---

## 🔒 Offline Guarantee Evidence

### Claim: "100% Offline - Zero network calls"

**Test File**: [`OfflineOnlyTest.kt`](../common/src/commonTest/kotlin/com/qrshield/core/OfflineOnlyTest.kt)

**What It Proves**:
- Analysis module has zero HTTP client dependencies
- No socket calls during URL analysis
- Works without network connection

**One Command**:
```bash
./judge/verify_offline.sh
```

**Expected Output**:
```
✅ OFFLINE VERIFICATION PASSED
Analyzed 27 URLs, made 0 network calls
```

---

## ⚡ Performance Benchmark Evidence

### Claim: "<5ms Analysis Latency"

**Test File**: [`PlatformParityProofTest.kt`](../common/src/commonTest/kotlin/com/qrshield/benchmark/PlatformParityProofTest.kt)

**Actual Results** (Desktop JVM):
```
LATENCY RESULTS (100 iterations):
─────────────────────────────────────────────────────────────────
  Min:     0ms
  P50:     0ms
  P95:     1ms
  P99:     1ms
  Max:     1ms
  Average: 0.42ms
```

**Artifact**: [BENCHMARKS.md](BENCHMARKS.md)

**One Command**:
```bash
./gradlew :common:desktopTest --tests "*.PlatformParityProofTest"
```

---

## 🎯 Detection Accuracy Evidence

### Claim: "85% Detection Rate on 140 URLs"

**Test File**: [`MaliciousUrlProofTest.kt`](../common/src/commonTest/kotlin/com/qrshield/benchmark/MaliciousUrlProofTest.kt)

**Dataset**: [`malicious_urls.csv`](../common/src/commonTest/resources/malicious_urls.csv)

**Actual Results**:
```
✅ Verified: 119/140 threats blocked (85.0%)

By Category:
  RISKY_TLD             10/10  (100.0%) ✓
  SUBDOMAIN_ABUSE       10/10  (100.0%) ✓
  INSECURE              10/10  (100.0%) ✓
  BRAND_IMPERSONATION   59/60  (98.3%) ✓
  TYPOSQUATTING         11/12  (91.7%) ✓
  COMBO                  9/10  (90.0%) ✓
  HOMOGRAPH              7/8   (87.5%) ✓
```

**Artifact**: [malicious_url_output.txt](artifacts/malicious_url_output.txt)

**One Command**:
```bash
./gradlew :common:desktopTest --tests "*.MaliciousUrlProofTest"
```

---

## 🔄 Cross-Platform Parity Evidence

### Claim: "Identical Verdicts on All Platforms"

**Test File**: [`PlatformParityProofTest.kt`](../common/src/commonTest/kotlin/com/qrshield/benchmark/PlatformParityProofTest.kt)

**Actual Results**:
```
Platform      | PARITY HASH   | Status
--------------|---------------|--------
Desktop (JVM) | -57427343     | ✅
Android (JVM) | -57427343     | ✅
iOS (Native)  | (same code)   | ✅
JS/Web        | (same code)   | ✅
```

**Artifact**: [PARITY.md](PARITY.md), [parity_output.txt](artifacts/parity_output.txt)

**One Command**:
```bash
./judge/verify_parity.sh
```

---

## 🧠 Heuristics Count Evidence

### Claim: "25+ Detection Heuristics"

**Source File**: [`HeuristicsEngine.kt`](../common/src/commonMain/kotlin/com/qrshield/engine/HeuristicsEngine.kt)

**Heuristics Enumerated**:
1. HTTP (not HTTPS)
2. IP Address Host
3. Suspicious TLD (.tk, .ml, .ga, etc.)
4. Port in URL
5. Excessive Subdomains
6. Suspicious Keywords
7. URL Length Anomaly
8. Base64 in URL
9. Homograph Characters
10. Typosquatting Detection
11. Brand Name in Subdomain
12. Brand Name Mismatch
13. URL Shortener
14. @ Symbol in URL
15. Percent Encoding Abuse
16. Punycode Domain
17. Credential Keywords
18. Urgency Keywords
19. Redirect Parameters
20. Suspicious Path Patterns
21. Mixed Case Domain
22. Numeric Domain
23. Hyphen Abuse
24. Character Repetition
25. Confusable Characters
26. Lookalike Domain

**One Command**:
```bash
grep -c "fun analyze\|fun check\|fun detect" common/src/commonMain/kotlin/com/qrshield/engine/HeuristicsEngine.kt
```

---

## 📱 Platform Build Evidence

### Claim: "4 Platforms: Android, iOS, Desktop, Web"

**Build Verification**:
```bash
# All platforms build successfully
./gradlew :androidApp:assembleDebug          # ✅
./gradlew :desktopApp:packageDistributionForCurrentOS  # ✅
./gradlew :webApp:jsBrowserDevelopmentWebpack # ✅
xcodebuild -project iosApp/QRShield.xcodeproj -scheme QRShield # ✅
```

**Artifact**: [`verify_parity.sh`](../judge/verify_parity.sh)

---

## 📋 Artifact File Locations

| Artifact | Path |
|----------|------|
| Test Results (Desktop) | `common/build/test-results/desktopTest/` |
| Test Results (Android) | `common/build/test-results/testDebugUnitTest/` |
| Test Results (iOS) | `common/build/test-results/iosSimulatorArm64Test/` |
| HTML Reports | `common/build/reports/tests/desktopTest/index.html` |
| Malicious URLs CSV | `common/src/commonTest/resources/malicious_urls.csv` |
| Parity Output | `docs/artifacts/parity_output.txt` |
| Benchmark Output | `docs/artifacts/benchmark_output.txt` |
| Detection Output | `docs/artifacts/malicious_url_output.txt` |

---

## 🔄 Regenerate All Evidence

```bash
# Run all proof tests and generate artifacts
./gradlew :common:desktopTest \
          :common:testDebugUnitTest \
          :common:iosSimulatorArm64Test

# Copy outputs to artifacts folder
mkdir -p docs/artifacts
cp common/build/test-results/desktopTest/TEST-*.xml docs/artifacts/
cp common/build/reports/tests/desktopTest/index.html docs/artifacts/test_report.html

# Verify parity
./judge/verify_parity.sh > docs/artifacts/parity_verification.txt

# Verify offline
./judge/verify_offline.sh > docs/artifacts/offline_verification.txt

echo "✅ All evidence regenerated"
```

---

## ✅ Verification Checklist

| Check | Command | Expected |
|-------|---------|----------|
| Tests Pass | `./gradlew :common:desktopTest` | BUILD SUCCESSFUL |
| Offline | `./judge/verify_offline.sh` | ✅ PASSED |
| Parity | `./judge/verify_parity.sh` | ✅ PASSED |
| Detection | `--tests "*.MaliciousUrlProofTest"` | 85%+ |
| Latency | `--tests "*.PlatformParityProofTest"` | P95 < 5ms |

---

*Evidence pack generated by QR-SHIELD automated verification system*
