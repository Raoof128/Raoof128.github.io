# Red Team Corpus

Adversarial test cases for QR-SHIELD detection engine. These patterns are used by real-world phishing attacks to bypass security filters.

## ⚠️ WARNING

These URLs are **DEFANGED** (modified to be non-functional). They are for testing purposes only (see
[Red Team Corpus Format](#format)).

---

## Format

All URLs use the following defanging format:
- `hxxp://` instead of `http://`
- `hxxps://` instead of `https://`
- `[.]` instead of `.`
- `[at]` instead of `@`

To convert to real URLs for testing, use the helper function in test code.

---

## 1. Mixed Scripts (Homograph Attacks)

| ID | Defanged URL | Attack Type | Target Brand |
|----|--------------|-------------|--------------|
| HG-001 | `hxxps://аpple[.]com/verify` | Cyrillic 'а' (U+0430) | Apple |
| HG-002 | `hxxps://раypal[.]com/login` | Cyrillic 'р' and 'а' | PayPal |
| HG-003 | `hxxps://micrоsоft[.]com/signin` | Cyrillic 'о' (U+043E) | Microsoft |
| HG-004 | `hxxps://Αmazon[.]com/giftcard` | Greek 'Α' (U+0391) | Amazon |
| HG-005 | `hxxps://goοgle[.]com/security` | Greek 'ο' (U+03BF) | Google |
| HG-006 | `hxxps://fаcebооk[.]com/recovery` | Multiple Cyrillic | Facebook |
| HG-007 | `hxxps://whаtsapp[.]com/verify` | Cyrillic 'а' | WhatsApp |
| HG-008 | `hxxps://netflіх[.]com/billing` | Cyrillic 'і' and 'х' | Netflix |
| HG-009 | `hxxps://spоtify[.]com/premium` | Cyrillic 'о' | Spotify |
| HG-010 | `hxxps://drop​box[.]com/share` | Zero-width space U+200B | Dropbox |

---

## 2. Percent-Encoding Abuse

| ID | Defanged URL | Attack Type |
|----|--------------|-------------|
| PE-001 | `hxxps://example[.]com/%2e%2e/admin` | Encoded path traversal |
| PE-002 | `hxxps://example[.]com/%2F%2F%2Fmalware` | Triple-encoded slashes |
| PE-003 | `hxxps://example[.]com/%70%61%79%70%61%6c` | Fully encoded "paypal" |
| PE-004 | `hxxps://example[.]com/%252e%252e/etc/passwd` | Double-encoded dots |
| PE-005 | `hxxps://example[.]com/%00hidden` | Null byte injection |
| PE-006 | `hxxps://login%2Epaypal%2Ecom[.]attacker[.]com` | Encoded subdomain |
| PE-007 | `hxxps://example[.]com/%ef%bb%bf/hidden` | BOM prefix |
| PE-008 | `hxxps://example[.]com/path?url=%68%74%74%70%3a%2f%2f%6d%61%6c%77%61%72%65` | Encoded redirect URL |

---

## 3. Nested Redirects in Parameters

| ID | Defanged URL | Attack Type |
|----|--------------|-------------|
| NR-001 | `hxxps://legit[.]com/redirect?url=hxxps://phishing[.]tk` | Simple redirect |
| NR-002 | `hxxps://legit[.]com/goto?next=hxxps%3A%2F%2Fmalware[.]ml` | Encoded redirect |
| NR-003 | `hxxps://legit[.]com/r?target=hxxps://legit[.]com/r?target=hxxps://malware[.]ru` | Chained redirects |
| NR-004 | `hxxps://legit[.]com/out?url=//phishing[.]tk/login` | Protocol-relative |
| NR-005 | `hxxps://legit[.]com/link?dest=javascript:alert(1)` | JavaScript URI |
| NR-006 | `hxxps://legit[.]com/redir?callback=data:text/html,<script>` | Data URI |
| NR-007 | `hxxps://accounts.google.com.phishing[.]tk/redirect?continue=hxxps://malware[.]xyz` | Brand subdomain + redirect |

---

## 4. Unicode Normalization Edge Cases

| ID | Defanged URL | Attack Type |
|----|--------------|-------------|
| UN-001 | `hxxps://café[.]com/login` | NFC/NFD difference (é) |
| UN-002 | `hxxps://example[.]com/path\u202E/gpj.exe` | RTL override (reverses extension) |
| UN-003 | `hxxps://example[.]com/​hidden` | Zero-width space in path |
| UN-004 | `hxxps://example[.]com/file\u200Bname` | Zero-width space in filename |
| UN-005 | `hxxps://example[.]com/a\u0300bc` | Combining grave accent |
| UN-006 | `hxxps://example[.]com/﷽long﷽` | Arabic ligature (very wide) |
| UN-007 | `hxxps://example[.]com/\u2028newline` | Line separator |

---

## 5. IP Address Obfuscation

| ID | Defanged URL | Attack Type | Decoded IP |
|----|--------------|-------------|------------|
| IP-001 | `hxxp://3232235777/malware` | Decimal (192.168.1.1) | 192.168.1.1 |
| IP-002 | `hxxp://0xC0A80101/payload` | Hex | 192.168.1.1 |
| IP-003 | `hxxp://0300.0250.0001.0001/shell` | Octal | 192.168.1.1 |
| IP-004 | `hxxp://192.168.0x01.1/admin` | Mixed notation | 192.168.1.1 |
| IP-005 | `hxxp://192.168.1.1.xip[.]io/` | IP with DNS rebind | Varies |
| IP-006 | `hxxp://[::ffff:192.168.1.1]/hidden` | IPv6-mapped IPv4 | 192.168.1.1 |
| IP-007 | `hxxp://0[.]0[.]0[.]0[at]192[.]168[.]1[.]1/` | Username as IP | 192.168.1.1 |

---

## 6. WiFi Payload Attacks

| ID | Payload | Attack Type |
|----|---------|-------------|
| WF-001 | `WIFI:T:nopass;S:FreeAirportWifi;;` | Open network phishing |
| WF-002 | `WIFI:T:WEP;S:CorporateNet;P:12345;;` | Weak WEP encryption |
| WF-003 | `WIFI:T:WPA;S:StarbucksWifi;P:coffee123;H:true;;` | Brand impersonation + hidden |
| WF-004 | `WIFI:T:WPA;S:BankOfAmericaFreeWifi;P:boa2024;;` | Financial brand impersonation |
| WF-005 | `WIFI:T:WPA;S:aGVsbG8gd29ybGQ=;P:base64encoded;;` | Base64 in SSID (exfil) |
| WF-006 | `WIFI:T:WPA;S:Update Your Device;P:virus;;` | Social engineering SSID |

---

## 7. SMS/Smishing Attacks

| ID | Payload | Attack Type |
|----|---------|-------------|
| SM-001 | `sms:+1-900-555-0123?body=Subscribe` | Premium rate number |
| SM-002 | `smsto:+447441999999:Your bank account is locked. Visit hxxps://secure-bank[.]tk` | Smishing with URL |
| SM-003 | `sms:+1234567890?body=URGENT: Verify your account at bit[.]ly/verify123` | Shortened URL |
| SM-004 | `sms:+1234567890?body=You won $1000000! Click hxxps://winner[.]ml/claim` | Prize scam |
| SM-005 | `sms:12345?body=download` | Short code subscription |

---

## 8. Cryptocurrency Payment Attacks

| ID | Payload | Attack Type |
|----|---------|-------------|
| CR-001 | `bitcoin:1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2?amount=0.5&label=URGENT%20REFUND` | Social engineering label |
| CR-002 | `bitcoin:1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa?amount=10&label=TechSupport%20Payment` | Support scam |
| CR-003 | `ethereum:0x742d35Cc6634C0532925a3b844Bc9e7595f2bD25?amount=5&label=Prize%20Fee` | Prize fee scam |
| CR-004 | `bitcoin:bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh?amount=0.001` | Dust attack pattern |

---

## 9. vCard Impersonation

| ID | Payload Snippet | Attack Type |
|----|-----------------|-------------|
| VC-001 | `FN:John Smith - CEO Apple\nURL:hxxps://apple-support[.]tk` | Executive + malicious URL |
| VC-002 | `ORG:PayPal Security Team\nEMAIL:security[at]paypa1[.]com` | Brand impersonation |
| VC-003 | `FN:IRS Tax Refund Agent\nTEL:+1-900-555-0199` | Government + premium number |
| VC-004 | `ORG:Bank of America\nURL:hxxps://bofa-verify[.]ml/account` | Financial brand + phishing |

---

## 10. Combination Attacks (Advanced)

| ID | Defanged URL | Attack Types Combined |
|----|--------------|----------------------|
| CA-001 | `hxxps://аррle[.]com\u200B/redirect?url=hxxps%3A%2F%2Fmalware[.]tk` | Homograph + Zero-width + Nested redirect |
| CA-002 | `hxxp://0xC0A80101/раypal/login?next=%68%74%74%70%3a%2f%2f%67%6f%6f%67%6c%65%2e%63%6f%6d` | Hex IP + Homograph + Double encoding |
| CA-003 | `hxxps://secure-login.google.com.account-verify[.]tk:8080/\u202Efdp.exe` | Subdomain confusion + RTL override |
| CA-004 | `hxxps://mіcrоsоft[.]com/%2e%2e/%2e%2e/system32?callback=javascript:` | Multiple Cyrillic + Path traversal + JS |

---

## Usage in Tests

```kotlin
class AdversarialCorpusTest {
    
    @Test
    fun `detect homograph attack HG-001`() {
        val url = refang("hxxps://аpple[.]com/verify")
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.hasObfuscation)
        assertTrue(ObfuscationAttack.MIXED_SCRIPTS in result.detectedAttacks)
    }
    
    private fun refang(defanged: String): String {
        return defanged
            .replace("hxxp://", "http://")
            .replace("hxxps://", "https://")
            .replace("[.]", ".")
            .replace("[at]", "@")
    }
}
```

---

## Contributing

Add new attack patterns by:
1. Creating a test case in `AdversarialCorpusTest.kt`
2. Adding the pattern to this file with a unique ID
3. Ensuring the pattern is properly defanged
4. Including expected detection type and risk level

---

## References

- [Unicode Security Mechanisms](https://unicode.org/reports/tr39/)
- [OWASP URL Encoding](https://cheatsheetseries.owasp.org/cheatsheets/Injection_Prevention_Cheat_Sheet.html)
- [RFC 3987 - Internationalized Resource Identifiers](https://tools.ietf.org/html/rfc3987)
- [IDN Homograph Attack](https://en.wikipedia.org/wiki/IDN_homograph_attack)
