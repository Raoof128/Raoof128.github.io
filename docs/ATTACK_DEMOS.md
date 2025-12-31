# 🎭 Attack Demo Scenarios

> Curated attack demonstrations for judges: Real detection signals, mitigations, and user guidance.

---

## 🚀 Quick Demo: Visit the Web App

```
https://raoof128.github.io/?demo=true
```

This auto-loads 5 perfect examples in history covering all attack types!

---

## 🔴 Attack Scenario 1: Homograph Attack

### The Attack

A phishing URL that uses **Cyrillic characters** to visually impersonate Google:

```
Legitimate: https://google.com
Attack:     https://gооgle.com  ← Cyrillic 'о' (U+043E)
```

**Why It's Dangerous:** These look identical in most fonts and browser address bars!

### Mehr Guard Detection

```
URL Analyzed: https://gооgle.com/login

┌─────────────────────────────────────────────────┐
│ 🔴 VERDICT: MALICIOUS (Score: 88/100)           │
├─────────────────────────────────────────────────┤
│ 🚨 Detection Signals:                           │
│                                                 │
│ ⚠️  HOMOGRAPH_ATTACK (+35 points)               │
│     "Punycode detected: xn--ggle-55da.com"      │
│     Contains non-ASCII characters mimicking     │
│     standard domain characters                  │
│                                                 │
│ ⚠️  BRAND_IMPERSONATION (+25 points)            │
│     "Impersonating: google"                     │
│     Match type: HOMOGRAPH                       │
│                                                 │
│ ⚠️  SUSPICIOUS_PATH (+10 points)                │
│     "Path contains credential harvesting        │
│     keyword: /login"                            │
│                                                 │
│ ✅ Counterfactual hint:                         │
│    "Using https://google.com directly would     │
│    reduce this risk by ~60 points"              │
└─────────────────────────────────────────────────┘
```

### User Guidance

> ⚠️ **This URL uses Unicode characters that look like ASCII but aren't!**
> 
> **Safe Action:** Navigate to google.com directly — never click suspicious links.

---

## 🔴 Attack Scenario 2: Typosquatting

### The Attack

A fake PayPal domain using **character substitution** (1 instead of l):

```
Legitimate: https://paypal.com/login
Attack:     https://paypa1.com/login  ← Number '1' instead of 'l'
```

**Why It's Dangerous:** Easy to miss at a glance, especially in QR codes!

### Mehr Guard Detection

```
URL Analyzed: https://paypa1-secure.tk/login

┌─────────────────────────────────────────────────┐
│ 🔴 VERDICT: MALICIOUS (Score: 92/100)           │
├─────────────────────────────────────────────────┤
│ 🚨 Detection Signals:                           │
│                                                 │
│ ⚠️  TYPOSQUAT_DETECTED (+30 points)             │
│     "Domain 'paypa1' matches brand 'paypal'"    │
│     Levenshtein distance: 1 character           │
│                                                 │
│ ⚠️  SUSPICIOUS_TLD (+20 points)                 │
│     ".tk is a high-risk free TLD commonly       │
│     used for phishing"                          │
│                                                 │
│ ⚠️  COMBOSQUATTING (+15 points)                 │
│     "Domain contains brand + '-secure'"         │
│     Common phishing pattern                     │
│                                                 │
│ ⚠️  CREDENTIAL_PATH (+10 points)                │
│     "Path '/login' indicates credential form"   │
│                                                 │
│ ✅ Counterfactual hint:                         │
│    "Using paypal.com would eliminate this risk" │
└─────────────────────────────────────────────────┘
```

### User Guidance

> ⚠️ **This domain impersonates PayPal with a subtle character swap!**
> 
> **Safe Action:** Always type paypal.com directly or use your saved bookmark.

---

## 🟡 Attack Scenario 3: URL Shortener

### The Attack

A shortened URL that **hides the true destination**:

```
Visible:     https://bit.ly/3xYz123
Destination: https://malicious-phishing-site.xyz/steal-data
```

**Why It's Dangerous:** You can't see where you're going until it's too late!

### Mehr Guard Detection

```
URL Analyzed: https://bit.ly/3xYz123

┌─────────────────────────────────────────────────┐
│ 🟡 VERDICT: SUSPICIOUS (Score: 38/100)          │
├─────────────────────────────────────────────────┤
│ ⚠️ Detection Signals:                           │
│                                                 │
│ ⚠️  URL_SHORTENER (+20 points)                  │
│     "bit.ly is a URL shortening service"        │
│     True destination cannot be verified offline │
│                                                 │
│ ⚠️  DESTINATION_UNKNOWN (+10 points)            │
│     "Cannot analyze final destination without   │
│     network request (privacy-preserving mode)"  │
│                                                 │
│ ℹ️  Note:                                       │
│     Mehr Guard flags shorteners as suspicious    │
│     because they obscure the true destination   │
│                                                 │
│ ✅ Counterfactual hint:                         │
│    "If you know the intended site, visit it     │
│    directly instead of using the short URL"    │
└─────────────────────────────────────────────────┘
```

### User Guidance

> ⚠️ **This shortened URL hides the real destination!**
> 
> **Safe Action:** Ask the sender for the full URL, or use a URL expander service.

---

## 🔴 Attack Scenario 4: Brand Impersonation (Bank)

### The Attack

A fake CommBank page hidden in a subdomain:

```
Legitimate: https://www.commbank.com.au/netbank
Attack:     https://commbank.secure-verify.ml/account
                   ↑ brand in subdomain    ↑ attacker's domain
```

**Why It's Dangerous:** The attacker puts the brand FIRST to fool you!

### Mehr Guard Detection

```
URL Analyzed: https://commbank.secure-verify.ml/account

┌─────────────────────────────────────────────────┐
│ 🔴 VERDICT: MALICIOUS (Score: 85/100)           │
├─────────────────────────────────────────────────┤
│ 🚨 Detection Signals:                           │
│                                                 │
│ ⚠️  BRAND_IN_SUBDOMAIN (+25 points)             │
│     "Brand 'commbank' found in subdomain but    │
│     actual domain is 'secure-verify.ml'"        │
│                                                 │
│ ⚠️  SUSPICIOUS_TLD (+20 points)                 │
│     ".ml (Mali) is a high-risk TLD"             │
│                                                 │
│ ⚠️  COMBOSQUATTING (+15 points)                 │
│     "Domain contains 'secure-verify' pattern"   │
│     Common in phishing campaigns                │
│                                                 │
│ ⚠️  FINANCIAL_BRAND (+10 points)                │
│     "Impersonating financial institution"       │
│     Category: BANK (AU)                         │
│                                                 │
│ ✅ Counterfactual hint:                         │
│    "Visit commbank.com.au directly"             │
└─────────────────────────────────────────────────┘
```

### User Guidance

> ⚠️ **This URL puts the brand in a subdomain but links to an attacker's domain!**
> 
> **Safe Action:** Always check the REAL domain (the part before the first `/`).

---

## 🔴 Attack Scenario 5: Redirect Chain

### The Attack

A URL that passes through **multiple redirects** to evade detection:

```
Initial:    https://click.tracking.xyz/r?url=...
Redirect 1: https://bit.ly/abc123
Redirect 2: https://tracking-pixel.net/bounce?to=...
Final:      https://paypa1-phishing.tk/login
```

**Why It's Dangerous:** Each redirect buys the attacker detection evasion time!

### Mehr Guard Detection

```
URL Analyzed: https://redirect-chain.xyz/r?url=aHR0cHM6Ly9waGlzaGluZy5jb20=

┌─────────────────────────────────────────────────┐
│ 🔴 VERDICT: MALICIOUS (Score: 75/100)           │
├─────────────────────────────────────────────────┤
│ 🚨 Detection Signals:                           │
│                                                 │
│ ⚠️  REDIRECT_PATTERN (+20 points)               │
│     "URL contains redirect indicator: /r?"      │
│                                                 │
│ ⚠️  ENCODED_PAYLOAD (+20 points)                │
│     "Base64 content detected in query params"   │
│     Decoded: "https://phishing.com"             │
│                                                 │
│ ⚠️  SUSPICIOUS_TLD (+15 points)                 │
│     ".xyz is associated with higher phishing"   │
│                                                 │
│ ⚠️  HIGH_ENTROPY_PARAMS (+10 points)            │
│     "Query parameters appear randomized"        │
│                                                 │
│ ✅ Counterfactual hint:                         │
│    "Visit the intended site directly without    │
│    using redirect/tracking links"               │
└─────────────────────────────────────────────────┘
```

### User Guidance

> ⚠️ **This URL contains hidden redirects and encoded destinations!**
> 
> **Safe Action:** Never click tracking links — go directly to the site you want.

---

## 🟢 Safe Example: Legitimate URL

### The URL

```
https://www.google.com/search?q=kotlin+multiplatform
```

### Mehr Guard Detection

```
URL Analyzed: https://www.google.com/search?q=kotlin+multiplatform

┌─────────────────────────────────────────────────┐
│ 🟢 VERDICT: SAFE (Score: 5/100)                 │
├─────────────────────────────────────────────────┤
│ ✅ Security Signals:                            │
│                                                 │
│ ✅ HTTPS (+0 points, expected)                  │
│    "Secure connection verified"                 │
│                                                 │
│ ✅ OFFICIAL_DOMAIN (+0 points)                  │
│    "google.com is a verified legitimate domain" │
│                                                 │
│ ✅ NO_SUSPICIOUS_PATTERNS                       │
│    "URL structure is normal and expected"       │
│                                                 │
│ ℹ️  Why it's safe:                              │
│    • Official domain owned by Google            │
│    • Standard URL structure                     │
│    • No encoding, obfuscation, or redirects     │
│    • Path matches expected service (/search)    │
└─────────────────────────────────────────────────┘
```

---

## 📊 Detection Signal Reference

| Signal | Points | Description |
|--------|--------|-------------|
| `HOMOGRAPH_ATTACK` | +35 | Unicode characters mimicking ASCII |
| `TYPOSQUAT_DETECTED` | +30 | Character substitution matching brand |
| `BRAND_IN_SUBDOMAIN` | +25 | Brand name in subdomain, not domain |
| `BRAND_IMPERSONATION` | +25 | Generic brand matching |
| `IP_ADDRESS_HOST` | +25 | Numeric IP instead of domain |
| `SUSPICIOUS_TLD` | +20 | High-risk TLD (.tk, .ml, .xyz) |
| `URL_SHORTENER` | +20 | Link shortening service |
| `NO_HTTPS` | +18 | Unencrypted connection |
| `COMBOSQUATTING` | +15 | Brand + keyword pattern |
| `CREDENTIAL_PATH` | +10 | Path contains /login, /signin, etc. |
| `HIGH_ENTROPY` | +10 | Randomized domain/path |
| `HTTPS_PRESENT` | -10 | Encrypted connection (protective) |

---

## 🔬 Adversarial Test Corpus

We publish our adversarial test corpus for the security research community:

**📂 Download:** [`data/adversarial_corpus.json`](../data/adversarial_corpus.json)

| Metric | Value |
|--------|-------|
| Total URLs | 100 |
| Legitimate | 50 |
| Phishing | 50 |
| Attack Categories | 12 |

**Categories include:**
- Typosquatting (character substitution)
- Subdomain abuse
- Australian bank phishing
- High-risk TLDs (.tk, .ml, .ga)
- Cryptocurrency scams
- Social media verification scams
- Delivery/package scams
- IP address hosts
- @ symbol injection
- Punycode/homograph attacks
- QR-specific attacks (parking, menus)

**Usage:**
```kotlin
// Load corpus for testing
val corpus = loadAdversarialCorpus("data/adversarial_corpus.json")
corpus.urls.forEach { entry ->
    val result = PhishingEngine().analyzeBlocking(entry.url)
    assert(result.verdict == expectedVerdict(entry.label))
}
```

---

## 🧪 Try It Yourself

### Web Demo
```bash
open https://raoof128.github.io/?demo=true
```

### Desktop App
```bash
./gradlew :desktopApp:run
```

### Test All Examples
```bash
./scripts/eval.sh
```

---

*Last updated: December 2025*
