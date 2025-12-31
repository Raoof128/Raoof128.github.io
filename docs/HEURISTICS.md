# 🔍 Heuristics Documentation

> **Complete reference for Mehr Guard's 25+ detection rules**

---

## 📋 Overview

Mehr Guard uses a **weighted heuristic scoring system** combined with ML and brand detection. Each heuristic contributes to the overall risk score.

| Scoring Range | Verdict |
|---------------|---------|
| 0-10 | ✅ SAFE |
| 11-49 | ⚠️ SUSPICIOUS |
| 50-100 | 🔴 MALICIOUS |

---

## 🔴 Critical Risk Heuristics (Weight ≥15)

### 1. IP Address as Host

| Property | Value |
|----------|-------|
| **ID** | `RISK_IP_HOST` |
| **Weight** | 25 |
| **Detects** | URLs using IP addresses instead of domains |
| **Examples** | `http://192.168.1.1/login`, `http://10.0.0.1:8080/` |
| **Why Risky** | IP addresses bypass DNS-based blocklists and are common in phishing |
| **False Positives** | Internal corporate tools, localhost development |

### 2. Punycode/IDN Domain

| Property | Value |
|----------|-------|
| **ID** | `RISK_PUNYCODE` |
| **Weight** | 18 |
| **Detects** | Internationalized domain names (xn--) |
| **Examples** | `xn--pypal-4ve.com`, `xn--ggle-55da.com` |
| **Why Risky** | Often used for homograph attacks |
| **False Positives** | Legitimate non-ASCII domains |

### 3. @ Symbol in URL

| Property | Value |
|----------|-------|
| **ID** | `RISK_AT_SYMBOL` |
| **Weight** | 18 |
| **Detects** | `@` character before the host |
| **Examples** | `https://google.com@evil.com/login` |
| **Why Risky** | Tricks users into thinking they're on a trusted domain |
| **False Positives** | Rarely legitimate |

### 4. Suspicious File Extension

| Property | Value |
|----------|-------|
| **ID** | `RISK_SUSPICIOUS_EXT` |
| **Weight** | 25 |
| **Detects** | Executable extensions in URLs |
| **Examples** | `.exe`, `.scr`, `.bat`, `.ps1` |
| **Why Risky** | Direct malware download |
| **False Positives** | Legitimate software downloads |

### 5. Double Extension

| Property | Value |
|----------|-------|
| **ID** | `RISK_DOUBLE_EXT` |
| **Weight** | 30 |
| **Detects** | Multiple extensions hiding true type |
| **Examples** | `document.pdf.exe`, `image.jpg.scr` |
| **Why Risky** | Classic Windows malware hiding technique |
| **False Positives** | Almost none |

### 6. HTTP Without HTTPS

| Property | Value |
|----------|-------|
| **ID** | `RISK_NO_HTTPS` |
| **Weight** | 15 |
| **Detects** | Unencrypted HTTP connections |
| **Examples** | `http://bank-login.com/` |
| **Why Risky** | Credentials can be intercepted |
| **False Positives** | Internal/local services |

---

## 🟡 Medium Risk Heuristics (Weight 8-14)

### 7. URL Shortener

| Property | Value |
|----------|-------|
| **ID** | `RISK_SHORTENER` |
| **Weight** | 10 |
| **Detects** | Known URL shortening services |
| **Examples** | `bit.ly/abc123`, `t.co/xyz`, `tinyurl.com/` |
| **Why Risky** | Hides final destination |
| **False Positives** | Legitimate link sharing |

### 8. Excessive Subdomains

| Property | Value |
|----------|-------|
| **ID** | `RISK_SUBDOMAINS` |
| **Weight** | 12 |
| **Detects** | More than 3 subdomains |
| **Examples** | `login.secure.account.verify.bank.evil.com` |
| **Why Risky** | Attempts to look official with long domains |
| **False Positives** | Some CDNs, enterprise apps |

### 9. Non-Standard Port

| Property | Value |
|----------|-------|
| **ID** | `RISK_PORT` |
| **Weight** | 10 |
| **Detects** | Explicit port numbers |
| **Examples** | `http://example.com:8080/`, `:443`, `:8443` |
| **Why Risky** | May bypass security monitoring |
| **False Positives** | Development servers, webcams |

### 10. High Entropy Domain

| Property | Value |
|----------|-------|
| **ID** | `RISK_HIGH_ENTROPY` |
| **Weight** | 12 |
| **Detects** | Random-looking domain names |
| **Examples** | `xk7f9m2q.com`, `a1b2c3d4e5f6.net` |
| **Why Risky** | Generated domains for phishing campaigns |
| **False Positives** | Hash-based URLs, CDN nodes |

### 11. Credential Keywords

| Property | Value |
|----------|-------|
| **ID** | `RISK_CREDENTIAL_KEYWORDS` |
| **Weight** | 12 |
| **Detects** | Login-related keywords in URL |
| **Examples** | `/login`, `/password`, `/verify`, `/signin` |
| **Why Risky** | Often in combination with other flags |
| **False Positives** | Legitimate login pages |

### 12. Encoded Payload

| Property | Value |
|----------|-------|
| **ID** | `RISK_ENCODED` |
| **Weight** | 10 |
| **Detects** | URL-encoded content |
| **Examples** | `%3Cscript%3E`, `%252e%252e` |
| **Why Risky** | May hide malicious payloads |
| **False Positives** | Normal URL encoding |

---

## 🟢 Low Risk Heuristics (Weight ≤7)

### 13. Long URL

| Property | Value |
|----------|-------|
| **ID** | `RISK_LONG_URL` |
| **Weight** | 5 |
| **Detects** | URLs > 200 characters |
| **Examples** | Very long tracking URLs |
| **Why Risky** | May hide malicious parameters |
| **False Positives** | Analytics, deep links |

### 14. Multiple TLDs in Path

| Property | Value |
|----------|-------|
| **ID** | `RISK_MULTI_TLD` |
| **Weight** | 10 |
| **Detects** | `.com`, `.org`, etc. appearing in path |
| **Examples** | `evil.com/paypal.com/login` |
| **Why Risky** | Attempts to fool users |
| **False Positives** | Documentation sites |

### 15. Numeric Subdomain

| Property | Value |
|----------|-------|
| **ID** | `RISK_NUMERIC_SUBDOMAIN` |
| **Weight** | 8 |
| **Detects** | Numbers in subdomain |
| **Examples** | `123.evil.com`, `192.phishing.net` |
| **Why Risky** | Often auto-generated |
| **False Positives** | Load balancers, CDNs |

---

## 🌐 TLD Risk Scoring

### Free/Abused TLDs

| TLD | Score | Reason |
|-----|-------|--------|
| `.tk` | 18 | Free Tokelau, heavily abused |
| `.ml` | 18 | Free Mali, heavily abused |
| `.ga` | 18 | Free Gabon, heavily abused |
| `.cf` | 18 | Free Central African Republic |
| `.gq` | 18 | Free Equatorial Guinea |

### High-Risk TLDs

| TLD | Score | Reason |
|-----|-------|--------|
| `.xyz` | 12 | Cheap, high abuse rate |
| `.top` | 12 | Cheap, high abuse rate |
| `.work` | 10 | Frequently abused |
| `.click` | 10 | Designed for phishing |
| `.link` | 10 | Common in spam |

### Safe TLDs

| TLD | Score | Reason |
|-----|-------|--------|
| `.com` | 0 | Standard commercial |
| `.org` | 0 | Standard organization |
| `.gov` | 0 | Government (trusted) |
| `.edu` | 0 | Educational (trusted) |

---

## 🎭 Adversarial Detection

### Homograph Attacks

| Attack Type | Example | Detection |
|-------------|---------|-----------|
| Cyrillic 'а' | `pаypal.com` (а vs a) | Unicode script analysis |
| Cyrillic 'е' | `googlе.com` (е vs e) | HomographDetector.kt |
| Greek 'ο' | `micrοsoft.com` (ο vs o) | Character mapping |
| Mixed scripts | `аpple.com` | Script mixing detection |

### Obfuscation Detection

| Technique | Example | Detection |
|-----------|---------|-----------|
| Zero-width chars | `goo​gle.com` (invisible char) | Unicode scanning |
| RTL override | `exemsi.exe` (displays as pdf.exe) | Control char detection |
| Octal IP | `http://0300.0250.0001.0001/` | IP parsing |
| Hex IP | `http://0xC0A80101/` | IP parsing |
| Decimal IP | `http://3232235777/` | IP parsing |
| Double encoding | `%252e%252e` | Recursive decoding |

---

## ⚙️ Configuration

### Weight Customization

Weights are configurable via `HeuristicWeightsConfig`:

```kotlin
data class HeuristicWeightsConfig(
    val httpNotHttps: Int = 15,
    val ipAddress: Int = 25,
    val urlShortener: Int = 10,
    // ... all weights configurable
)
```

### Adding New Heuristics

See [EXTENDING_RULES.md](EXTENDING_RULES.md) for contributor guide.

---

## 🧪 Testing

Each heuristic has dedicated tests in `HeuristicsEngineTest.kt`:

```kotlin
@Test
fun `detects IP address as host`() {
    val result = engine.analyze("http://192.168.1.1/login")
    assertTrue(result.flags.any { it.contains("IP") })
}
```

Total heuristic tests: **100+**

---

## 📊 False Positive Mitigation

| Strategy | Implementation |
|----------|----------------|
| Brand allowlist | 60+ brands with official domains |
| Weight balancing | Low-risk heuristics don't trigger alone |
| Context awareness | Combinations matter more than singles |
| Confidence scoring | Multiple signals = higher confidence |

---

*This document is auto-referenced from heuristic code via KDoc links.*
