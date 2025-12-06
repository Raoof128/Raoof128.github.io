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

### Defense Layers

```
┌─────────────────────────────────────────────────┐
│ Layer 1: Structural Analysis                    │
│   • Protocol • Host type • Port • Domain depth  │
├─────────────────────────────────────────────────┤
│ Layer 2: Heuristic Rules                        │
│   • 25+ individual checks • Weighted scoring    │
├─────────────────────────────────────────────────┤
│ Layer 3: Brand Detection                        │
│   • Exact match • Typosquat • Homograph         │
├─────────────────────────────────────────────────┤
│ Layer 4: ML Classification                      │
│   • Feature extraction • Logistic regression    │
├─────────────────────────────────────────────────┤
│ Layer 5: Combined Scoring                       │
│   • Weighted aggregation • Confidence scoring   │
└─────────────────────────────────────────────────┘
```

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
