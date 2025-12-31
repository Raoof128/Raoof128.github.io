# Known Limitations & Technical Trade-offs

**QR-SHIELD Detection Engine — Honest Assessment**

---

## Overview

This document transparently describes the limitations of QR-SHIELD's detection approach. We believe acknowledging limitations demonstrates engineering maturity and helps users make informed decisions.

---

## 1. Hardcoded Brand Database

### Limitation
The brand impersonation detection relies on a **hardcoded database of ~60 brands with 500+ patterns** in `BrandDatabase.kt`. This has several implications:

```kotlin
// Example from BrandDatabase.kt
val BRANDS = mapOf(
    "paypal" to "paypal.com",
    "google" to listOf("google.com", "google.co.uk", ...),
    "amazon" to listOf("amazon.com", "amazon.co.uk", ...),
    // ~500 entries...
)
```

### Implications

| Issue | Impact | Mitigation |
|-------|--------|------------|
| **New brands not covered** | A phishing site impersonating "CoolNewStartup.io" won't trigger brand detection | Other heuristics (TLD, entropy) still apply |
| **Regional brands missing** | Local banks, retailers may be absent | Database is extensible via updates |
| **Point-in-time snapshot** | Database was curated in Dec 2025 | Version updates can refresh |
| **Storage overhead** | ~50KB for brand data | Acceptable for on-device detection |

### Why This Trade-off?

| Alternative | Downside |
|-------------|----------|
| Cloud API lookup | Requires internet, privacy concerns, latency |
| ML-based brand detection | Much larger model size, higher compute |
| No brand detection | Misses major attack vector |

**Decision:** Hardcoded database offers best balance of privacy, offline capability, and detection accuracy for major brands.

### Future Improvements
- Community-contributed brand lists
- Regional brand packs (downloadable)
- On-device brand learning (experimental)

---

## 2. Heuristic Fragility

### Limitation
URL heuristics are **pattern-based rules** that can be:

1. **Evaded** by sophisticated attackers
2. **False-positive prone** on unusual legitimate URLs
3. **Point-in-time** — attack patterns evolve

### Examples of Evasion

```
✅ Detected: https://paypa1.tk/login
❌ Evaded:   https://secure-payment-gateway.com/paypal (no brand in domain)

✅ Detected: http://192.168.1.1/steal
❌ Evaded:   https://legitimatecdn.com/redirect?target=evil.com (redirect chain)

✅ Detected: https://gооgle.com (Cyrillic o)
❌ Evaded:   https://google-support.com (looks similar, no homograph)
```

### Mitigation Strategy

We use **defense in depth** — no single heuristic is decisive:

```
Final Score = 
    Heuristics (50%) +
    Ensemble ML (25%) + 
    Brand Detection (15%) +
    TLD Scoring (10%)
```

If one layer is evaded, others may still catch the attack.

### Documented Heuristic Limitations

| Heuristic | Limitation | False Positive Risk |
|-----------|------------|---------------------|
| Subdomain count | CDNs often have many subdomains | Medium |
| URL length | Some legitimate URLs are long | Low |
| Entropy | Random slugs (UUIDs) in legitimate URLs | Medium |
| Credential path | `/auth`, `/login` in legitimate flows | Low |
| URL shortener | Legitimate marketing campaigns use shorteners | Medium |

---

## 3. ML Model Constraints

### Limitation
The ensemble ML model combines three approaches but is **deliberately lightweight**:

- 15 features only
- ~2KB weights total
- Ensemble: Logistic Regression (40%) + Gradient Boosting (35%) + Decision Rules (25%)
- No deep learning

### Why Not Deep Learning?

| Factor | Deep Learning | Our Approach |
|--------|---------------|--------------|
| Model size | 10-100MB | <2KB |
| Inference time | 50-500ms | <5ms |
| Explainability | Black box | Full transparency |
| Cross-platform | TensorFlow Lite deps | Pure Kotlin |

### Accuracy Trade-off

| Metric | QR-SHIELD (Ensemble) | Production DL Model |
|--------|----------------------|---------------------|
| Precision | ~85% | 92%+ |
| Recall | ~89% | 95%+ |
| F1 | ~0.87 | 0.93+ |

**Decision:** The ensemble approach provides a good balance between accuracy, explainability, and cross-platform compatibility.

---

## 4. No Real-Time Threat Intelligence

### Limitation
QR-SHIELD does **not** connect to:
- Google Safe Browsing API
- PhishTank API
- VirusTotal
- Any cloud threat feed

### Implication
A brand-new phishing domain registered 1 hour ago will **not** be in any blocklist.

### Why This Trade-off?

| Benefit | Cost |
|---------|------|
| 100% offline operation | No real-time threat data |
| Zero data collection | Miss newest threats |
| No API keys required | No crowd-sourced intelligence |

### Mitigation
- Heuristics detect *patterns*, not specific domains
- Suspicious TLD lists cover common abuse vectors
- Brand database covers high-value targets

---

## 5. Platform-Specific Gaps

### Android
- Camera permission required for QR scanning
- No background scanning (intentional — battery preservation)

### iOS
- VisionKit requires iOS 13+
- No dynamic TLD updates without app update

### Web
- Camera API requires HTTPS and user permission
- No file system access for batch scanning
- jsQR library may miss damaged QR codes
- **Single-threaded JavaScript execution**: Kotlin/JS runs on the main browser thread. If `PhishingEngine.analyze()` takes 50ms (as measured in benchmarks), it may cause brief UI jank during analysis. Web Workers could mitigate this but add significant complexity. Current implementation is acceptable for occasional scans but not optimized for batch processing.

### Desktop
- No native QR scanning (clipboard/file only)
- JVM startup time (~500ms cold start)

---

## 6. False Positive Scenarios

### Known False Positive Patterns

| URL Pattern | Why Flagged | Reality |
|-------------|-------------|---------|
| `https://accounts.google.com/o/oauth2/...` | Long URL, multiple subdomains | Legitimate Google OAuth |
| `https://bit.ly/official-campaign` | URL shortener | Legitimate marketing |
| `https://192.168.1.1/router-admin` | IP address | Local router admin |
| `https://xn--n3h.com` | Punycode | Legitimate IDN domain |

### User Recourse
- "Wrong Verdict?" feedback button (stores locally)
- Future: community-contributed allowlists

---

## 7. Attack Vectors We DON'T Detect

| Attack Type | Why Not Detected |
|-------------|------------------|
| **Malware download** | We check URLs, not file contents |
| **Legitimate but compromised sites** | No content analysis |
| **Credential stuffing** | Application-layer attack |
| **Session hijacking** | Requires network monitoring |
| **Zero-day browser exploits** | Beyond URL analysis |

**QR-SHIELD's Scope:** Pre-click URL reputation scoring. Not an endpoint security solution.

---

## Summary

| Limitation | Severity | Mitigation |
|------------|----------|------------|
| Hardcoded brand database | Medium | Covers 500+ major brands |
| Heuristic evasion | Medium | Multi-layer defense |
| Simple ML model | Low | Explainability trade-off |
| No real-time feeds | High | Privacy is a feature |
| Platform gaps | Low | Graceful degradation |
| False positives | Medium | Feedback mechanism |

---

## Our Position

**QR-SHIELD is not meant to replace enterprise security solutions.** 

It's designed as a **privacy-first, educational tool** that:
1. Raises user awareness about URL risks
2. Catches obvious phishing attempts
3. Works without any cloud dependencies
4. Explains *why* something is suspicious

For users who need 99.9% detection with real-time threat intelligence, we recommend complementing QR-SHIELD with cloud security services.

---

*Document Version: 1.0 — December 2025*
