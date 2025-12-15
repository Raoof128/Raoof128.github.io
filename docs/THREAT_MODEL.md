# QR-SHIELD Threat Model

## 1. QRishing Taxonomy

### Attack Categories

```
┌─────────────────────────────────────────────────────────────────┐
│                    QRishing Attack Taxonomy                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  CREDENTIAL     │  │    MALWARE      │  │    PAYMENT      │  │
│  │  HARVESTING     │  │  DISTRIBUTION   │  │     FRAUD       │  │
│  │                 │  │                 │  │                 │  │
│  │ • Fake logins   │  │ • APK downloads │  │ • Modified QR   │  │
│  │ • OAuth phish   │  │ • Exploit kits  │  │ • Fake invoices │  │
│  │ • MFA bypass    │  │ • Ransomware    │  │ • Charity scams │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │    SOCIAL       │  │     DATA        │  │    SESSION      │  │
│  │  ENGINEERING    │  │  EXFILTRATION   │  │   HIJACKING     │  │
│  │                 │  │                 │  │                 │  │
│  │ • Prize scams   │  │ • Device info   │  │ • WhatsApp Web  │  │
│  │ • Tech support  │  │ • Location      │  │ • Discord login │  │
│  │ • Romance scams │  │ • Contacts      │  │ • Email access  │  │
│  │                 │  │ • Base64 data   │  │                 │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Attack Vectors

### 2.1 Typosquatting

**Description**: Domain names that mimic legitimate brands using character substitution.

| Technique | Example | Detection |
|-----------|---------|-----------|
| Number substitution | paypa1.com | Character analysis |
| Letter substitution | arnazon.com (rn→m) | Visual similarity check |
| TLD abuse | paypal.co (not .com) | TLD verification |

### 2.2 Homograph Attacks

**Description**: Using Unicode characters that visually resemble ASCII characters.

| Legit | Attack | Unicode Used |
|-------|--------|--------------|
| apple.com | аpple.com | Cyrillic 'а' (U+0430) |
| google.com | gооgle.com | Cyrillic 'о' (U+043E) |

**Detection**: Punycode detection, IDN analysis

### 2.3 Subdomain Abuse

**Description**: Placing brand names in subdomains of attacker-controlled domains.

```
Legit:    https://www.paypal.com/login
Attack:   https://paypal.com.secure-login.attacker.com/login
                 ↑ brand here     ↑ actual domain
```

### 2.4 URL Shorteners

**Description**: Using shortening services to obscure malicious destinations.

| Service | Risk | Example |
|---------|------|---------|
| bit.ly | Medium | bit.ly/3xYz123 → malicious.com |
| t.co | Medium | Twitter shortener |
| Custom | High | company-short.ly → phishing.com |

### 2.5 Data Exfiltration via QR

**Description**: QR codes can encode URLs that exfiltrate data via query parameters.

#### Attack Patterns

| Pattern | Example | Detection Method |
|---------|---------|------------------|
| Base64 Payload | `?data=SGVsbG9Xb3JsZA==` | Long alphanumeric sequences |
| Encoded Credentials | `?u=YWRtaW4=&p=cGFzc3dvcmQ=` | Credential param names |
| Device Info | `?uid=ABC123&model=iPhone` | Device-related params |
| Location Data | `?lat=40.7128&lng=-74.0060` | Geo coordinate patterns |

#### QR-SHIELD Detection

The HeuristicsEngine specifically looks for exfiltration indicators:

```
ENCODED_PAYLOAD Detection:
├── Consecutive alphanumeric run ≥ 50 characters
├── Base64 character set (A-Z, a-z, 0-9, +, /, =)
├── Multiple encoded parameters
└── Penalty: +10 points per indicator

CREDENTIAL_PARAMS Detection:
├── password, pwd, token, session, auth, secret
├── api_key, apikey, access_token, jwt, oauth
└── Penalty: +18 points if found
```

#### Example Attack

```
https://tracking.malicious.site/collect?
  uid=a1b2c3d4e5f6&
  data=eyJ1c2VybmFtZSI6ImFkbWluIiwicGFzc3dvcmQiOiJzZWNyZXQifQ==&
  device=iPhone14Pro

├── Very long URL with encoded data
├── Base64 payload in query (50+ chars)
├── Device fingerprinting parameter
└── Risk Score: 55 (SUSPICIOUS)
```

---

## 3. Synthetic Attack Examples

**⚠️ These are synthetic examples for testing only**

### Credential Harvesting

```
https://secure-netf1ix-billing.tk/update-payment
├── Brand: Netflix (typosquat)
├── TLD: .tk (high-risk free domain)
├── Keywords: secure, billing, payment
└── Risk Score: 85 (MALICIOUS)
```

### Payment Fraud

```
http://192.168.1.100:8080/invoice/pay?amount=500
├── Protocol: HTTP (insecure)
├── Host: IP address
├── Port: Non-standard
└── Risk Score: 78 (MALICIOUS)
```

### Shortener Abuse

```
https://bit.ly/free-gift-claim
├── Shortened: Unknown destination
├── Keywords: free, gift
├── Risk Score: 45 (SUSPICIOUS)
```

---

## 4. Defense Mechanisms

### Implemented Defenses

| Defense | Implementation | Effectiveness |
|---------|---------------|---------------|
| Protocol Check | Verify HTTPS | High |
| IP Detection | Flag IP hosts | High |
| Brand Database | 500+ brands | High |
| Typosquat Detection | Fuzzy matching | Medium-High |
| Homograph Detection | Punycode analysis | High |
| TLD Risk Scoring | Threat intel data | Medium |
| ML Classification | 15-feature model | Medium |
| Path Analysis | Keyword detection | Medium |
| **Adversarial Defense (NEW)** | **13 obfuscation types** | **High** |
| **Payload Analysis (NEW)** | **15+ payload types** | **High** |
| **Policy Engine (NEW)** | **Enterprise policies** | **High** |

### Defense Layers

```
┌─────────────────────────────────────────────────┐
│ Layer 0: Policy Enforcement (NEW v1.2.0)        │
│   • Org allowlists • TLD blocking • HTTPS req   │
├─────────────────────────────────────────────────┤
│ Layer 1: Adversarial Defense (NEW v1.2.0)       │
│   • Homograph • RTL override • Encoding abuse   │
├─────────────────────────────────────────────────┤
│ Layer 2: Payload Type Analysis (NEW v1.2.0)     │
│   • WiFi config • SMS • vCard • Crypto payments │
├─────────────────────────────────────────────────┤
│ Layer 3: Structural Analysis                    │
│   • Protocol • Host type • Port • Domain depth  │
├─────────────────────────────────────────────────┤
│ Layer 4: Heuristic Rules                        │
│   • 25+ individual checks • Weighted scoring    │
├─────────────────────────────────────────────────┤
│ Layer 5: Brand Detection                        │
│   • Exact match • Typosquat • Homograph         │
├─────────────────────────────────────────────────┤
│ Layer 6: ML Classification                      │
│   • Feature extraction • Logistic regression    │
├─────────────────────────────────────────────────┤
│ Layer 7: Combined Scoring                       │
│   • Weighted aggregation • Confidence scoring   │
└─────────────────────────────────────────────────┘
```

### New in v1.2.0: Adversarial Attack Defenses

| Attack Type | Unicode/Encoding Trick | Detection Method |
|-------------|------------------------|------------------|
| **Homograph** | Cyrillic 'а' (U+0430) | Script mixing detection |
| **RTL Override** | U+202E reverses text | RTL character removal |
| **Double Encoding** | %25 → % | Iterative decoding |
| **Zero-Width** | U+200B invisible | Zero-width removal |
| **Decimal IP** | 3232235777 | Decimal pattern match |
| **Punycode** | xn--pple-43d | IDN domain detection |

### New in v1.2.0: QR Payload-Specific Threats

| Payload Type | Attack Vector | Detection |
|--------------|---------------|-----------|
| **WiFi** | Rogue access points, WEP | Open network alerts, SSID analysis |
| **SMS** | Smishing, premium numbers | URL extraction, number validation |
| **vCard** | Executive impersonation | Title/org analysis, embedded URLs |
| **Bitcoin** | Address swapping, scam labels | Irreversibility warnings |
| **UPI** | Large amount fraud | Payee verification prompts |

### New in v1.2.0: Enterprise Policy Defenses

| Policy Feature | Use Case |
|----------------|----------|
| **Domain Allowlists** | Skip scanning for internal domains |
| **TLD Blocking** | Block all .tk, .ml, .ga organization-wide |
| **HTTPS Enforcement** | Reject all HTTP URLs |
| **Shortener Blocking** | Block all URL shorteners |
| **Custom Thresholds** | Adjust risk tolerance per department |


---

## 5. Limitations

### Known Limitations

1. **Novel Domains**: Cannot detect brand-new phishing domains with no history
2. **URL Shorteners**: Cannot expand without network request
3. **Legitimate New Sites**: May false-positive on new legitimate domains
4. **Content Analysis**: Does not analyze destination page content
5. **Language Bias**: Brand database is English-focused
6. **Sophisticated Attacks**: May miss highly targeted spear-phishing

### Mitigation Strategies

| Limitation | Mitigation |
|------------|------------|
| Novel domains | Conservative scoring + user warnings |
| Shorteners | Flag as suspicious, recommend caution |
| False positives | "Proceed Anyway" option with warning |
| Content analysis | Future roadmap item |

---

## 6. Future Enhancements

### Short Term (2025 H1)
- [ ] Real-time domain reputation API (opt-in)
- [ ] Multi-language brand database
- [ ] OCR for text in QR images

### Medium Term (2025 H2)
- [ ] LLM-powered URL reasoning
- [ ] Browser extension
- [ ] Enterprise MDM integration

### Long Term (2026+)
- [ ] Vision Transformer anomaly detection
- [ ] Crowdsourced threat reporting
- [ ] Federated learning updates
