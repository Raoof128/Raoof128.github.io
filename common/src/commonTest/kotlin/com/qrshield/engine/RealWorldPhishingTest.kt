/*
 * Copyright 2024 QR-SHIELD Contributors
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

package com.qrshield.engine

import com.qrshield.core.PhishingEngine
import com.qrshield.model.Verdict
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
 * @author QR-SHIELD Security Team
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
        val result = engine.analyze(url)
        
        // Should detect suspicious TLD at minimum
        assertTrue(
            result.score >= 20 || result.flags.isNotEmpty(),
            "PayPal typosquat should be flagged. Score: ${result.score}, Verdict: ${result.verdict}"
        )
    }
    
    @Test
    fun `detects Microsoft credential harvesting pattern`() {
        val url = defangedToUrl("https://rnicrosoft-365[.]xyz/signin/oauth")
        val result = engine.analyze(url)
        
        // Should detect suspicious TLD (.xyz) and impersonation pattern
        assertTrue(
            result.score >= 20 || result.flags.isNotEmpty(),
            "Microsoft impersonation should have flags or elevated score. Got: ${result.score}"
        )
    }
    
    @Test
    fun `detects Netflix billing scam pattern`() {
        val url = defangedToUrl("http://netflix-billing-update[.]ml/payment")
        val result = engine.analyze(url)
        
        assertTrue(
            result.verdict != Verdict.SAFE,
            "Netflix billing scam pattern should not be SAFE. Got: ${result.verdict}"
        )
    }
    
    @Test
    fun `detects Apple ID phishing pattern`() {
        val url = defangedToUrl("https://appleid-verify[.]ga/account/login")
        val result = engine.analyze(url)
        
        assertTrue(
            result.flags.isNotEmpty(),
            "Apple ID phishing should have risk flags"
        )
    }
    
    // === AUSTRALIAN BANK PHISHING PATTERNS ===
    
    @Test
    fun `detects CommBank NetBank phishing`() {
        val url = defangedToUrl("https://commbank-netbank-login[.]tk/verify")
        val result = engine.analyze(url)
        
        // .tk is a high-risk TLD, should be flagged
        assertTrue(
            result.score >= 20 || result.flags.isNotEmpty(),
            "CommBank phishing should be flagged. Score: ${result.score}"
        )
    }
    
    @Test
    fun `detects NAB internet banking phishing`() {
        val url = defangedToUrl("http://nab-internet-banking-secure[.]cf/login")
        val result = engine.analyze(url)
        
        // HTTP + suspicious TLD should be flagged
        assertTrue(
            result.score >= 30 || result.flags.isNotEmpty(),
            "NAB phishing should have elevated score. Got: ${result.score}"
        )
    }
    
    @Test
    fun `detects ATO tax refund scam`() {
        val url = defangedToUrl("https://ato-refund-claim[.]gq/verify-identity")
        val result = engine.analyze(url)
        
        assertTrue(
            result.verdict != Verdict.SAFE,
            "ATO scam should not be SAFE. Got: ${result.verdict}"
        )
    }
    
    // === DELIVERY SCAM PATTERNS ===
    
    @Test
    fun `detects AusPost parcel scam`() {
        val url = defangedToUrl("https://auspost-parcel-tracking[.]info/1234567890")
        val result = engine.analyze(url)
        
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
        val result = engine.analyze(url)
        
        assertTrue(
            result.verdict != Verdict.SAFE || result.score > 20,
            "DHL typosquat (dhi vs dhl) should be flagged"
        )
    }
    
    // === TECHNICAL EVASION PATTERNS ===
    
    @Test
    fun `detects IP address based phishing`() {
        val url = defangedToUrl("http://192[.]168[.]1[.]100:8080/paypal/login")
        val result = engine.analyze(url)
        
        assertTrue(
            result.flags.any { it.contains("IP", ignoreCase = true) },
            "IP address host should be flagged"
        )
    }
    
    @Test
    fun `detects @ symbol URL spoofing`() {
        val url = defangedToUrl("https://google[.]com@evil-site[.]tk/login")
        val result = engine.analyze(url)
        
        assertTrue(
            result.score >= 30,
            "@ symbol URL spoofing should be detected. Score: ${result.score}"
        )
    }
    
    @Test
    fun `detects punycode homograph attack`() {
        // This is a simulated punycode domain (xn-- prefix)
        val url = defangedToUrl("https://xn--pypal-4ve[.]com/login")
        val result = engine.analyze(url)
        
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
        val result = engine.analyze(url)
        
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
            val result = engine.analyze(url)
            
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
        val result = engine.analyze(url)
        
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
}
