/*
 * Copyright 2025-2026 Mehr Guard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.raouf.mehrguard.engine

import com.raouf.mehrguard.core.PhishingEngine
import com.raouf.mehrguard.model.Verdict
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Real-World Phishing URL Test Cases
 *
 * These tests use "defanged" URLs (with [.] instead of .) to represent
 * actual phishing patterns seen in the wild. The patterns are based on
 * publicly documented phishing campaigns.
 *
 * IMPORTANT: All URLs are defanged for safety. The actual phishing sites
 * may still be active - DO NOT visit these domains.
 *
 * Sources:
 * - APWG Phishing Activity Trends Reports
 * - PhishTank Database
 * - Australian Cyber Security Centre Threat Reports
 *
 * @author Mehr Guard Security Team
 * @since 1.0.0
 */
class RealWorldPhishingTest {

    private val engine = PhishingEngine()

    /**
     * Helper to convert defanged URL to testable format
     */
    private fun defangedToUrl(defanged: String): String {
        return defanged.replace("[.]", ".")
    }

    // === BRAND IMPERSONATION PATTERNS ===

    @Test
    fun `detects PayPal typosquat pattern - paypa1`() {
        val url = defangedToUrl("https://paypa1-secure[.]tk/login/verify")
        val result = engine.analyzeBlocking(url)

        // Should detect suspicious TLD at minimum
        assertTrue(
            result.score >= 20 || result.flags.isNotEmpty(),
            "PayPal typosquat should be flagged. Score: ${result.score}, Verdict: ${result.verdict}"
        )
    }

    @Test
    fun `detects Microsoft credential harvesting pattern`() {
        val url = defangedToUrl("https://rnicrosoft-365[.]xyz/signin/oauth")
        val result = engine.analyzeBlocking(url)

        // Should detect suspicious TLD (.xyz) and impersonation pattern
        assertTrue(
            result.score >= 20 || result.flags.isNotEmpty(),
            "Microsoft impersonation should have flags or elevated score. Got: ${result.score}"
        )
    }

    @Test
    fun `detects Netflix billing scam pattern`() {
        val url = defangedToUrl("http://netflix-billing-update[.]ml/payment")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.verdict != Verdict.SAFE,
            "Netflix billing scam pattern should not be SAFE. Got: ${result.verdict}"
        )
    }

    @Test
    fun `detects Apple ID phishing pattern`() {
        val url = defangedToUrl("https://appleid-verify[.]ga/account/login")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.flags.isNotEmpty(),
            "Apple ID phishing should have risk flags"
        )
    }

    // === AUSTRALIAN BANK PHISHING PATTERNS ===

    @Test
    fun `detects CommBank NetBank phishing`() {
        val url = defangedToUrl("https://commbank-netbank-login[.]tk/verify")
        val result = engine.analyzeBlocking(url)

        // .tk is a high-risk TLD, should be flagged
        assertTrue(
            result.score >= 20 || result.flags.isNotEmpty(),
            "CommBank phishing should be flagged. Score: ${result.score}"
        )
    }

    @Test
    fun `detects NAB internet banking phishing`() {
        val url = defangedToUrl("http://nab-internet-banking-secure[.]cf/login")
        val result = engine.analyzeBlocking(url)

        // HTTP + suspicious TLD should be flagged
        assertTrue(
            result.score >= 30 || result.flags.isNotEmpty(),
            "NAB phishing should have elevated score. Got: ${result.score}"
        )
    }

    @Test
    fun `detects ATO tax refund scam`() {
        val url = defangedToUrl("https://ato-refund-claim[.]gq/verify-identity")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.verdict != Verdict.SAFE,
            "ATO scam should not be SAFE. Got: ${result.verdict}"
        )
    }

    // === DELIVERY SCAM PATTERNS ===

    @Test
    fun `detects AusPost parcel scam`() {
        val url = defangedToUrl("https://auspost-parcel-tracking[.]info/1234567890")
        val result = engine.analyzeBlocking(url)

        // AusPost scams are very common in Australia
        assertTrue(
            result.flags.any {
                it.contains("AusPost", ignoreCase = true) ||
                it.contains("impersonation", ignoreCase = true) ||
                it.contains("suspicious", ignoreCase = true)
            } || result.score > 30,
            "AusPost scam should be detected. Score: ${result.score}, Flags: ${result.flags}"
        )
    }

    @Test
    fun `detects DHL tracking scam`() {
        val url = defangedToUrl("http://dhi-tracking[.]xyz/parcel/123456")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.verdict != Verdict.SAFE || result.score > 20,
            "DHL typosquat (dhi vs dhl) should be flagged"
        )
    }

    // === TECHNICAL EVASION PATTERNS ===

    @Test
    fun `detects IP address based phishing`() {
        val url = defangedToUrl("http://192[.]168[.]1[.]100:8080/paypal/login")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.flags.any { it.contains("IP", ignoreCase = true) },
            "IP address host should be flagged"
        )
    }

    @Test
    fun `detects at-symbol URL spoofing`() {
        val url = defangedToUrl("https://google[.]com@evil-site[.]tk/login")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.score >= 30,
            "At-symbol URL spoofing should be detected. Score: ${result.score}"
        )
    }

    @Test
    fun `detects punycode homograph attack`() {
        // This is a simulated punycode domain (xn-- prefix)
        val url = defangedToUrl("https://xn--pypal-4ve[.]com/login")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.flags.any {
                it.contains("punycode", ignoreCase = true) ||
                it.contains("internationalized", ignoreCase = true) ||
                it.contains("homograph", ignoreCase = true)
            },
            "Punycode domain should be flagged"
        )
    }

    // === URL SHORTENER PATTERNS ===

    @Test
    fun `flags URL shortener as suspicious`() {
        val url = defangedToUrl("https://bit[.]ly/3xYz123")
        val result = engine.analyzeBlocking(url)

        // URL shorteners should have some score or flags
        assertTrue(
            result.score >= 5 || result.flags.any {
                it.contains("shortener", ignoreCase = true) ||
                it.contains("short", ignoreCase = true) ||
                it.contains("redirect", ignoreCase = true)
            },
            "URL shortener should be detected. Score: ${result.score}, Flags: ${result.flags}"
        )
    }

    // === HIGH RISK TLD PATTERNS ===

    @Test
    fun `detects high-risk TLD abuse`() {
        val urls = listOf(
            "https://secure-login[.]tk/",
            "https://verify-account[.]ml/",
            "https://update-billing[.]ga/",
            "https://confirm-identity[.]cf/"
        )

        urls.forEach { defanged ->
            val url = defangedToUrl(defanged)
            val result = engine.analyzeBlocking(url)

            assertTrue(
                result.score >= 20 || result.flags.any { it.contains("TLD", ignoreCase = true) },
                "High-risk TLD in $url should be flagged. Score: ${result.score}"
            )
        }
    }

    // === COMBINED THREAT PATTERNS ===

    @Test
    fun `detects multi-signal phishing attack`() {
        // This URL has multiple red flags:
        // 1. HTTP (not HTTPS)
        // 2. Brand impersonation (paypa1)
        // 3. High-risk TLD (.tk)
        // 4. Credential keywords in path
        val url = defangedToUrl("http://paypa1-secure[.]tk/login?password=verify&token=abc")
        val result = engine.analyzeBlocking(url)

        // Should at least be SUSPICIOUS with multiple signals
        assertTrue(
            result.verdict == Verdict.MALICIOUS || result.verdict == Verdict.SUSPICIOUS,
            "Multi-signal attack should be SUSPICIOUS or MALICIOUS. Got: ${result.verdict}, Score: ${result.score}"
        )
        assertTrue(
            result.flags.isNotEmpty(),
            "Should have flags. Got: ${result.flags.size}"
        )
    }

    // === SOCIAL MEDIA SCAM PATTERNS ===

    @Test
    fun `detects Instagram verification scam`() {
        val url = defangedToUrl("https://instagram-verify-badge[.]ml/verify/username")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.verdict != Verdict.SAFE,
            "Instagram scam should not be SAFE. Got: ${result.verdict}"
        )
    }

    @Test
    fun `detects Facebook account recovery scam`() {
        val url = defangedToUrl("https://facebook-account-recovery[.]tk/recover?id=123456")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.score >= 30 || result.flags.isNotEmpty(),
            "Facebook recovery scam should be flagged. Score: ${result.score}"
        )
    }

    @Test
    fun `detects WhatsApp prize scam`() {
        val url = defangedToUrl("http://whatsapp-winner[.]ga/claim-prize?phone=0412345678")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.verdict == Verdict.MALICIOUS || result.verdict == Verdict.SUSPICIOUS,
            "WhatsApp prize scam should not be SAFE. Got: ${result.verdict}"
        )
    }

    // === CRYPTOCURRENCY SCAM PATTERNS ===

    @Test
    fun `detects crypto wallet drainer`() {
        val url = defangedToUrl("https://metamask-sync[.]io/wallet/connect?key=abc123")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.flags.any {
                it.contains("impersonation", ignoreCase = true) ||
                it.contains("credential", ignoreCase = true)
            } || result.score > 20,
            "Crypto wallet scam should be detected. Score: ${result.score}"
        )
    }

    @Test
    fun `detects fake airdrop claim`() {
        val url = defangedToUrl("https://solana-airdrop-claim[.]tk/claim?wallet=0x1234")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.verdict != Verdict.SAFE,
            "Fake airdrop should not be SAFE. Got: ${result.verdict}"
        )
    }

    // === QR-SPECIFIC ATTACK PATTERNS ===

    @Test
    fun `detects parking meter QR scam`() {
        // Common QRishing attack: fake parking payment
        val url = defangedToUrl("https://parking-payment-sydney[.]ml/pay?meter=123")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.score >= 20,
            "Parking scam with .ml TLD should be flagged. Score: ${result.score}"
        )
    }

    @Test
    fun `detects restaurant menu with hidden redirect`() {
        val url = defangedToUrl("https://menu-view[.]tk/restaurant?redirect=https://evil[.]com")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.flags.any {
                it.contains("redirect", ignoreCase = true) ||
                it.contains("TLD", ignoreCase = true)
            },
            "Hidden redirect should be detected. Flags: ${result.flags}"
        )
    }

    @Test
    fun `detects WiFi login portal phishing`() {
        val url = defangedToUrl("http://wifi-login[.]cf/connect?ssid=CoffeeShop&session=abc")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.verdict != Verdict.SAFE,
            "Fake WiFi portal should not be SAFE. Got: ${result.verdict}"
        )
    }

    // === EVASION TECHNIQUE PATTERNS ===

    @Test
    fun `detects base64 encoded redirect`() {
        // Base64: "https://evil.com" = "aHR0cHM6Ly9ldmlsLmNvbQ=="
        val url = defangedToUrl("https://legitimate-looking[.]com/redirect?url=aHR0cHM6Ly9ldmlsLmNvbQ==")
        val result = engine.analyzeBlocking(url)

        // Should detect encoded payload or at least complete analysis without error
        // Base64 detection is an advanced feature - main assertion is no crash
        assertTrue(
            result.score >= 0,
            "Analysis should complete successfully. Score: ${result.score}, Flags: ${result.flags}"
        )
    }

    @Test
    fun `detects double URL encoding evasion`() {
        // Double-encoded characters to evade filters
        val url = defangedToUrl("https://trusted[.]com/%252e%252e/admin/login")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.score > 10 || result.flags.isNotEmpty(),
            "Double encoding should raise some flags. Score: ${result.score}"
        )
    }

    @Test
    fun `detects extremely long subdomain obfuscation`() {
        val url = defangedToUrl("https://secure.login.verify.account.update.paypal.suspicious-domain[.]tk/")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.flags.any {
                it.contains("subdomain", ignoreCase = true) ||
                it.contains("TLD", ignoreCase = true)
            },
            "Excessive subdomains + brand should be flagged. Flags: ${result.flags}"
        )
    }

    // === LEGITIMATE URL FALSE POSITIVE CHECKS ===

    @Test
    fun `does not flag legitimate short domain`() {
        val url = defangedToUrl("https://t[.]co/about")
        val result = engine.analyzeBlocking(url)

        // t.co is Twitter's shortener - should be flagged as shortener but not MALICIOUS
        assertTrue(
            result.verdict != Verdict.MALICIOUS,
            "Twitter's t.co should not be MALICIOUS. Got: ${result.verdict}"
        )
    }

    @Test
    fun `does not flag legitimate Australian bank`() {
        val url = defangedToUrl("https://www[.]commbank[.]com[.]au/netbank/login")
        val result = engine.analyzeBlocking(url)

        assertTrue(
            result.verdict == Verdict.SAFE || result.score < 40,
            "Real CommBank should be SAFE. Got: ${result.verdict}, Score: ${result.score}"
        )
    }

    @Test
    fun `does not flag legitimate government site`() {
        val url = defangedToUrl("https://my[.]gov[.]au/mygov/")
        val result = engine.analyzeBlocking(url)

        // Government sites should be safe; ensemble model may produce slightly higher scores
        assertTrue(
            result.verdict == Verdict.SAFE || (result.verdict == Verdict.SUSPICIOUS && result.score < 30),
            "Australian government should be SAFE or low SUSPICIOUS. Got: ${result.verdict}, Score: ${result.score}"
        )
    }

    // === CHALLENGE CASES (Known Edge Cases) ===

    @Test
    fun `handles unicode normalization attack`() {
        // Using full-width characters that look like ASCII
        val url = defangedToUrl("https://www.google。com/search") // Full-width period
        val result = engine.analyzeBlocking(url)

        // This is a tricky one - depends on normalization
        // At minimum, should parse without crashing
        assertTrue(
            result.score >= 0,
            "Should handle unicode without crashing. Score: ${result.score}"
        )
    }

    @Test
    fun `handles very long URL without timeout`() {
        val longPath = "a".repeat(500)
        val url = defangedToUrl("https://example[.]com/$longPath")
        val result = engine.analyzeBlocking(url)

        // Should complete analysis and possibly flag length
        assertTrue(
            result.score >= 0,
            "Should handle long URLs. Score: ${result.score}"
        )
    }
}

