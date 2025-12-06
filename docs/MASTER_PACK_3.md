# QR-SHIELD Master Pack - Part 3

---

# 8. DEMO VIDEO SCRIPT

## Shot-by-Shot Storyboard

### Scene 1: Hook (0:00 - 0:05)
**Visual**: Close-up of phone scanning QR code at coffee shop
**Text on Screen**: "587% increase in QR phishing attacks"
**Narration**: "QR codes are everywhere. So are the threats."

### Scene 2: Problem Statement (0:05 - 0:15)
**Visual**: Montage of QR codes on menus, posters, emails
**Text on Screen**: "QRishing: Phishing via QR Codes"
**Narration**: "Attackers embed malicious URLs in QR codes. 71% of users never verify before scanning."

### Scene 3: App Launch (0:15 - 0:20)
**Visual**: QR-SHIELD app icon, animated launch screen
**Text on Screen**: "Introducing QR-SHIELD"
**Narration**: "QR-SHIELD protects you instantly."

### Scene 4: Safe Scan Demo (0:20 - 0:30)
**Visual**: User scans legitimate Google QR code
**Text on Screen**: "✅ SAFE - Risk Score: 5"
**Narration**: "Scan any QR code. Get instant analysis. This official Google link? Safe."

### Scene 5: Suspicious Scan Demo (0:30 - 0:40)
**Visual**: User scans shortened URL QR code
**Text on Screen**: "⚠️ SUSPICIOUS - Risk Score: 45"
**Narration**: "A shortened URL? QR-SHIELD flags it, shows potential risks."

### Scene 6: Malicious Scan Demo (0:40 - 0:50)
**Visual**: User scans phishing PayPal QR code
**Text on Screen**: "❌ MALICIOUS - Risk Score: 87"
**Narration**: "This fake PayPal login? Instantly blocked with detailed warnings."

### Scene 7: Cross-Platform (0:50 - 1:00)
**Visual**: Split screen showing Android, iOS, Desktop, Web
**Text on Screen**: "One codebase. Every platform."
**Narration**: "Kotlin Multiplatform. Protection everywhere you scan."

### Scene 8: Features Montage (1:00 - 1:10)
**Visual**: Quick cuts of features
**Text on Screen**: 
- "25+ Security Heuristics"
- "ML-Powered Scoring"
- "Brand Impersonation Detection"
- "100% Offline & Private"

### Scene 9: Closing (1:10 - 1:20)
**Visual**: QR-SHIELD logo animation
**Text on Screen**: "QR-SHIELD: Scan Smart. Stay Protected."
**Narration**: "Download QR-SHIELD. Because every scan matters."
**Call to Action**: GitHub link, download badges

---

# 9. THREAT MODEL & SECURITY NOTES

## QRishing Taxonomy

```
┌─────────────────────────────────────────────────────────────────┐
│                     QRishing Attack Categories                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. CREDENTIAL HARVESTING                                       │
│     └─ Fake login pages for banking, email, social media        │
│                                                                 │
│  2. MALWARE DISTRIBUTION                                        │
│     └─ Links to APK downloads, exploit kits                     │
│                                                                 │
│  3. PAYMENT FRAUD                                               │
│     └─ Modified payment QR codes at retail locations            │
│                                                                 │
│  4. SOCIAL ENGINEERING                                          │
│     └─ Fake surveys, prize claims, tech support scams           │
│                                                                 │
│  5. DATA EXFILTRATION                                           │
│     └─ Links that capture device info, location, contacts       │
│                                                                 │
│  6. SESSION HIJACKING                                           │
│     └─ QR code login abuse (WhatsApp Web, Discord)              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Attack Vectors (Synthetic Examples)

| Attack Type | Example URL | Risk Signals |
|-------------|-------------|--------------|
| Brand Spoof | `https://paypa1-secure.com/login` | Typosquat, login keyword |
| IP Host | `http://192.168.1.1:8080/update` | IP address, HTTP, port |
| Homograph | `https://gооgle.com` (Cyrillic 'o') | Unicode lookalike |
| Shortener Chain | `http://bit.ly/3xYz123` | Redirect, HTTP |
| Subdomain Abuse | `https://secure.login.verify.amazon.fakesite.tk` | Excessive subdomains |

## Defenses Implemented

1. **URL Parsing**: Robust extraction of all URL components
2. **TLD Verification**: High-risk TLD flagging (.tk, .ml, .ga)
3. **Brand Database**: 500+ brands with typosquat variants
4. **Homograph Detection**: Punycode and Unicode analysis
5. **ML Classification**: Trained on phishing URL datasets
6. **Entropy Analysis**: Random subdomain detection

## Limitations & Transparency

1. **Novel Attacks**: May not catch zero-day phishing domains
2. **URL Shorteners**: Cannot expand without network call
3. **Legitimate New Domains**: May false-positive on new sites
4. **Language Bias**: Brand database English-focused
5. **No Content Analysis**: Doesn't scan destination page

## Future Enhancements

- **LLM Integration**: Natural language URL reasoning
- **OCR Analysis**: Detect hidden text in QR images
- **Domain Age API**: Check WHOIS registration date
- **Community Reporting**: Crowdsourced threat intel
- **Browser Extension**: Pre-scan URLs before click

---

# 10. COMPLIANCE MAPPING

## ACSC Essential Eight Alignment

| Control | QR-SHIELD Implementation |
|---------|-------------------------|
| Application Control | Analyzes URLs before user action |
| Patch Applications | N/A (user app) |
| Configure MS Office Macros | N/A |
| User App Hardening | Blocks known malicious patterns |
| Restrict Admin Privileges | No elevated permissions required |
| Patch OS | N/A |
| Multi-Factor Auth | N/A |
| Regular Backups | Local history backup capability |

## Australian Privacy Act Alignment

| Principle | Compliance |
|-----------|------------|
| APP 1: Open and transparent | Open source, clear privacy policy |
| APP 3: Collection of solicited personal info | Zero data collection |
| APP 6: Use or disclosure | No disclosure, purely local |
| APP 11: Security of personal info | AES-256 encrypted storage |

## ISO 27001 Controls

| Control | Mapping |
|---------|---------|
| A.8.2.1 Classification of information | URL risk classification system |
| A.12.2.1 Controls against malware | Proactive phishing detection |
| A.13.1.1 Network controls | URL validation before access |

## NIST AI RMF Alignment

| Function | Implementation |
|----------|----------------|
| Govern | Open source governance, community oversight |
| Map | ML model clearly defined, scope documented |
| Measure | Risk scoring with confidence intervals |
| Manage | User control over decisions (proceed anyway) |

---

# 11. TEST CASE SUITE

## Unit Test Plan

```kotlin
class PhishingEngineTest {
    @Test
    fun `safe URL returns low risk score`()
    
    @Test
    fun `HTTP URL adds risk points`()
    
    @Test
    fun `IP address host flagged as suspicious`()
    
    @Test
    fun `brand impersonation detected in subdomain`()
    
    @Test
    fun `homograph attack detected`()
    
    @Test
    fun `excessive subdomains increase score`()
    
    @Test
    fun `URL shortener flagged`()
    
    @Test
    fun `combined score calculated correctly`()
}

class BrandDetectorTest {
    @Test
    fun `exact brand in subdomain detected`()
    
    @Test
    fun `typosquat variant detected`()
    
    @Test
    fun `official domain not flagged`()
}

class MLModelTest {
    @Test
    fun `feature extraction produces correct dimensions`()
    
    @Test
    fun `sigmoid output in valid range`()
    
    @Test
    fun `known phishing URL produces high score`()
}
```

## Sample QR Payloads

```json
{
  "test_payloads": [
    {
      "id": "safe_001",
      "url": "https://www.google.com/search?q=kotlin",
      "expected_verdict": "SAFE",
      "expected_score_range": [0, 20]
    },
    {
      "id": "suspicious_001",
      "url": "http://bit.ly/3xYz123",
      "expected_verdict": "SUSPICIOUS",
      "expected_score_range": [30, 60]
    },
    {
      "id": "malicious_001",
      "url": "https://paypa1-secure-login.com/verify",
      "expected_verdict": "MALICIOUS",
      "expected_score_range": [70, 100]
    },
    {
      "id": "malicious_002",
      "url": "http://192.168.1.100:8080/banking/login.php",
      "expected_verdict": "MALICIOUS",
      "expected_score_range": [75, 100]
    }
  ]
}
```

---

# 12. FUTURE ROADMAP

## Phase 1: Foundation (Current)
- ✅ Core phishing engine
- ✅ Cross-platform QR scanning
- ✅ Heuristics-based analysis
- ✅ Basic ML scoring

## Phase 2: Enhanced Intelligence (Q2 2025)
- [ ] Graph-based domain reputation
- [ ] Real-time threat feed integration
- [ ] Advanced homograph detection
- [ ] Multi-language brand database

## Phase 3: Advanced ML (Q3 2025)
- [ ] Vision Transformer QR anomaly detection
- [ ] On-device OCR for hidden text
- [ ] URL embedding models
- [ ] Federated learning for privacy-preserving updates

## Phase 4: Ecosystem (Q4 2025)
- [ ] Browser extension
- [ ] Email plugin (Outlook, Gmail)
- [ ] Enterprise MDM integration
- [ ] API for third-party integration

## Phase 5: Intelligence Network (2026)
- [ ] Crowdsourced threat reporting
- [ ] Anonymous telemetry (opt-in)
- [ ] Regional threat intelligence
- [ ] LLM-powered explanations
