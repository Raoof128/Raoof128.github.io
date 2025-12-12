# Security Policy

## 🔒 Our Commitment

QR-SHIELD is a security application designed to protect users from phishing attacks. We take security seriously and appreciate responsible disclosure of vulnerabilities.

---

## 📋 Table of Contents

- [Reporting a Vulnerability](#reporting-a-vulnerability)
- [Supported Versions](#supported-versions)
- [Security Features](#security-features)
- [Security Best Practices](#security-best-practices)
- [Known Limitations](#known-limitations)
- [Acknowledgments](#acknowledgments)

---

## Reporting a Vulnerability

If you discover a security vulnerability in QR-SHIELD, please report it responsibly.

### ⚠️ DO NOT

- **DO NOT** open a public GitHub issue for security vulnerabilities
- **DO NOT** post vulnerability details on social media
- **DO NOT** exploit the vulnerability beyond proof-of-concept

### ✅ DO

1. **Email us at:** security@qrshield.dev

2. **Include:**
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact assessment
   - Suggested fix (if any)
   - Your contact information

3. **Encrypt sensitive reports** using our PGP key (available upon request)

### Response Timeline

| Stage | Timeframe |
|-------|-----------|
| Initial response | Within 48 hours |
| Issue triage | Within 7 days |
| Fix development | Severity-dependent |
| Public disclosure | After fix is released |

---

## Supported Versions

| Version | Status | Support |
|---------|--------|---------|
| 1.1.x | ✅ Current | Active security updates |
| 1.0.x | ✅ Stable | Security patches only |
| < 1.0 | ❌ Pre-release | Not supported |

We recommend always using the latest version for the best security.

---

## Security Features

### 🛡️ Privacy-First Design

| Feature | Implementation |
|---------|----------------|
| **Offline analysis** | 100% on-device, no network calls required |
| **No telemetry** | Zero tracking, zero analytics |
| **No accounts** | No user registration or login |
| **Local storage only** | Data never leaves your device |

### 🔐 Data Protection

| Data Type | Protection |
|-----------|------------|
| Scan history | SQLite with encryption (SQLCipher-compatible) |
| Settings | SharedPreferences / UserDefaults |
| Temporary data | Cleared on app close |

### 🔍 Input Validation

- All URLs are validated before processing
- Maximum URL length enforced (2048 characters)
- Special characters sanitized
- ReDoS-safe regex patterns

### 🛠️ Secure Development

- Dependencies scanned with Trivy
- Static analysis with Detekt
- No hardcoded secrets in codebase
- Apache 2.0 open source license for transparency

---

## Security Best Practices

### For Users

1. **Download from official sources only:**
   - GitHub Releases
   - https://raoof128.github.io/

2. **Keep the app updated** for latest security patches

3. **Verify APK signatures** before installing on Android

4. **Report suspicious behavior** to security@qrshield.dev

### For Contributors

1. **Never commit secrets or API keys**
   ```bash
   # Add to .gitignore
   *.keystore
   keystore.properties
   local.properties
   ```

2. **Use parameterized queries** for database operations
   ```kotlin
   // ✅ Good
   queries.getById(id)
   
   // ❌ Bad - SQL injection risk
   queries.rawQuery("SELECT * FROM scans WHERE id = '$id'")
   ```

3. **Validate all input**
   ```kotlin
   // ✅ Good
   fun analyze(url: String): RiskAssessment {
       val validated = InputValidator.validateUrl(url)
       if (!validated.isValid()) {
           return RiskAssessment.invalid()
       }
       // Continue...
   }
   ```

4. **Follow secure coding guidelines:**
   - OWASP Mobile Security Guidelines
   - Kotlin security best practices

---

## Known Limitations

### Detection Accuracy

| Limitation | Description |
|------------|-------------|
| **Not 100% detection** | QR-SHIELD provides risk scoring but cannot guarantee detection of all phishing attempts |
| **Zero-day attacks** | New attack patterns may not be detected until heuristics are updated |
| **Legitimate URL flagging** | Some legitimate URLs may trigger false positives |

### Technical Limitations

| Limitation | Description |
|------------|-------------|
| **URL shorteners** | Expanded URLs require network access |
| **Dynamic content** | Cannot analyze JavaScript-rendered pages |
| **Encrypted QR codes** | Cannot analyze encrypted QR content |

### Security Boundaries

| Boundary | Note |
|----------|------|
| **Physical access** | Device compromise exposes local data |
| **Rooted/jailbroken devices** | Reduced security guarantees |
| **Malicious OS** | Cannot protect against OS-level attacks |

---

## Bug Bounty

We appreciate security researchers who help improve QR-SHIELD. While we don't currently offer monetary rewards, we provide:

- 🏆 **Public acknowledgment** in SECURITY_ACKNOWLEDGMENTS.md
- 📝 **Reference letter** upon request
- 🎁 **Swag** for significant findings (when available)

---

## Acknowledgments

We thank the following security researchers for their responsible disclosures:

*No vulnerabilities have been reported yet. Be the first to help secure QR-SHIELD!*

---

## Contact

- **Security issues:** security@qrshield.dev
- **General inquiries:** contributors@qrshield.dev
- **GitHub:** https://github.com/Raoof128/Raoof128.github.io

---

*Last updated: December 2024*
