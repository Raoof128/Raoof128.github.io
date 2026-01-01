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

package com.raouf.mehrguard.adversarial

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

/**
 * Adversarial Robustness Tests
 *
 * Tests the detection engine against the red-team corpus of obfuscation attacks.
 * These tests verify that Mehr Guard can detect and defend against real-world
 * evasion techniques used by attackers.
 *
 * See: data/red_team_corpus.md for full corpus documentation.
 *
 * @author Mehr Guard Security Team
 * @since 1.2.0
 */
class AdversarialRobustnessTest {
    
    // ==================== Homograph Attack Tests ====================
    
    @Test
    fun `HG-001 detect Cyrillic 'a' in apple domain`() {
        // Cyrillic 'а' (U+0430) instead of Latin 'a'
        val url = "https://аpple.com/verify"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.hasObfuscation, "Should detect obfuscation")
        assertTrue(
            ObfuscationAttack.MIXED_SCRIPTS in result.detectedAttacks,
            "Should detect mixed scripts attack"
        )
        assertTrue(result.riskScore >= 40, "Risk score should be high")
    }
    
    @Test
    fun `HG-002 detect Cyrillic 'p' and 'a' in paypal domain`() {
        // Cyrillic 'р' (U+0440) and 'а' (U+0430)
        val url = "https://раypal.com/login"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.hasObfuscation)
        assertTrue(ObfuscationAttack.MIXED_SCRIPTS in result.detectedAttacks)
    }
    
    @Test
    fun `HG-003 detect Cyrillic 'o' in microsoft domain`() {
        // Cyrillic 'о' (U+043E) instead of Latin 'o'
        val url = "https://micrоsоft.com/signin"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.hasObfuscation)
        assertTrue(ObfuscationAttack.MIXED_SCRIPTS in result.detectedAttacks)
    }
    
    @Test
    fun `HG-004 detect Greek Alpha in Amazon domain`() {
        // Greek 'Α' (U+0391) instead of Latin 'A'
        val url = "https://Αmazon.com/giftcard"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.hasObfuscation)
        assertTrue(ObfuscationAttack.MIXED_SCRIPTS in result.detectedAttacks)
    }
    
    @Test
    fun `HG-005 detect Greek omicron in google domain`() {
        // Greek 'ο' (U+03BF) instead of Latin 'o'
        val url = "https://goοgle.com/security"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.hasObfuscation)
        assertTrue(ObfuscationAttack.MIXED_SCRIPTS in result.detectedAttacks)
    }
    
    @Test
    fun `HG-010 detect zero-width space in domain`() {
        // Zero-width space (U+200B) in "dropbox"
        val url = "https://drop\u200Bbox.com/share"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.hasObfuscation)
        assertTrue(ObfuscationAttack.ZERO_WIDTH_CHARACTERS in result.detectedAttacks)
        assertFalse(result.normalizedUrl.contains('\u200B'), "Should remove zero-width space")
    }
    
    // ==================== Percent Encoding Tests ====================
    
    @Test
    fun `PE-001 detect encoded path traversal`() {
        val url = "https://example.com/%2e%2e/admin"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.normalizedUrl.contains(".."), "Should decode path traversal")
    }
    
    @Test
    fun `PE-004 detect double-encoded path traversal`() {
        // %25 = %, so %252e = %2e = .
        val url = "https://example.com/%252e%252e/etc/passwd"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.DOUBLE_ENCODING in result.detectedAttacks)
    }
    
    @Test
    fun `PE-006 detect encoded subdomain dots`() {
        // login%2Epaypal%2Ecom = login.paypal.com
        val url = "https://login%2Epaypal%2Ecom.attacker.com"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.normalizedUrl.contains("login.paypal.com"))
    }
    
    @Test
    fun `PE-007 detect BOM prefix in path`() {
        // BOM (U+FEFF) in path
        val url = "https://example.com/\uFEFFhidden"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.ZERO_WIDTH_CHARACTERS in result.detectedAttacks)
    }
    
    // ==================== Nested Redirect Tests ====================
    
    @Test
    fun `NR-001 detect simple redirect parameter`() {
        val url = "https://legit.com/redirect?url=https://phishing.tk"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.nestedUrls.isNotEmpty(), "Should detect nested URL")
        assertTrue(ObfuscationAttack.NESTED_REDIRECTS in result.detectedAttacks)
    }
    
    @Test
    fun `NR-002 detect encoded redirect URL`() {
        val url = "https://legit.com/goto?next=https%3A%2F%2Fmalware.ml"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.nestedUrls.isNotEmpty())
        assertTrue(ObfuscationAttack.NESTED_REDIRECTS in result.detectedAttacks)
    }
    
    @Test
    fun `NR-005 detect JavaScript URI in redirect`() {
        val url = "https://legit.com/link?dest=javascript:alert(1)"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.nestedUrls.isNotEmpty())
    }
    
    @Test
    fun `NR-006 detect data URI in redirect`() {
        val url = "https://legit.com/redir?callback=data:text/html,<script>"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.nestedUrls.isNotEmpty())
    }
    
    // ==================== Unicode Normalization Tests ====================
    
    @Test
    fun `UN-002 detect RTL override in filename`() {
        // RTL override (U+202E) to reverse "exe.jpg" to appear as "gpj.exe"
        val url = "https://example.com/path\u202E/gpj.exe"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.RTL_OVERRIDE in result.detectedAttacks)
        assertFalse(result.normalizedUrl.contains('\u202E'), "Should remove RTL override")
    }
    
    @Test
    fun `UN-003 detect zero-width space in path`() {
        val url = "https://example.com/\u200Bhidden"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.ZERO_WIDTH_CHARACTERS in result.detectedAttacks)
    }
    
    @Test
    fun `UN-005 detect combining grave accent`() {
        // 'a' + combining grave accent (U+0300)
        val url = "https://example.com/a\u0300bc"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.COMBINING_MARKS in result.detectedAttacks)
    }
    
    // ==================== IP Obfuscation Tests ====================
    
    @Test
    fun `IP-001 detect decimal IP address`() {
        // 3232235777 = 192.168.1.1
        val url = "http://3232235777/malware"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.DECIMAL_IP in result.detectedAttacks)
    }
    
    @Test
    fun `IP-002 detect hexadecimal IP address`() {
        val url = "http://0xC0A80101/payload"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.HEX_IP in result.detectedAttacks)
    }
    
    @Test
    fun `IP-003 detect octal IP address`() {
        val url = "http://0300.0250.0001.0001/shell"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.OCTAL_IP in result.detectedAttacks)
    }
    
    @Test
    fun `IP-004 detect mixed IP notation`() {
        val url = "http://192.168.0x01.1/admin"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.MIXED_IP_NOTATION in result.detectedAttacks)
    }
    
    // ==================== Punycode Tests ====================
    
    @Test
    fun `detect punycode domain`() {
        // xn--pple-43d.com is Cyrillic apple lookalike in punycode
        val url = "https://xn--pple-43d.com/verify"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.PUNYCODE_DOMAIN in result.detectedAttacks)
    }
    
    // ==================== Combination Attack Tests ====================
    
    @Test
    fun `CA-001 detect homograph + zero-width + nested redirect`() {
        // Cyrillic 'а' + zero-width space + encoded redirect
        val url = "https://аррle.com\u200B/redirect?url=https%3A%2F%2Fmalware.tk"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.hasObfuscation)
        assertTrue(result.detectedAttacks.size >= 2, "Should detect multiple attack types")
        assertTrue(result.riskScore >= 50, "Combined risk should be high")
    }
    
    @Test
    fun `CA-003 detect subdomain + RTL override`() {
        val url = "https://secure-login.google.com.account-verify.tk/\u202Efdp.exe"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(ObfuscationAttack.RTL_OVERRIDE in result.detectedAttacks)
    }
    
    // ==================== Clean URL Tests ====================
    
    @Test
    fun `legitimate URL should have no obfuscation`() {
        val url = "https://google.com/search?q=kotlin"
        val result = AdversarialDefense.normalize(url)
        
        assertFalse(result.hasObfuscation, "Legitimate URL should not flag obfuscation")
        assertEquals(0, result.riskScore, "Risk score should be zero")
    }
    
    @Test
    fun `legitimate international URL should not false positive`() {
        // Real Japanese URL
        val url = "https://日本語.jp/test"
        val result = AdversarialDefense.normalize(url)
        
        // Should NOT flag as mixed scripts (it's genuinely international)
        // May flag punycode if it gets converted
        assertEquals(url, result.normalizedUrl, "Should preserve legitimate i18n URL")
    }
    
    @Test
    fun `URL with legitimate query parameters should not flag as redirect`() {
        val url = "https://example.com/search?q=how+to+make+redirect"
        val result = AdversarialDefense.normalize(url)
        
        assertFalse(
            ObfuscationAttack.NESTED_REDIRECTS in result.detectedAttacks,
            "Should not flag legitimate 'redirect' keyword"
        )
    }
    
    // ==================== Edge Case Tests ====================
    
    @Test
    fun `empty URL should not crash`() {
        val result = AdversarialDefense.normalize("")
        assertFalse(result.hasObfuscation)
    }
    
    @Test
    fun `very long URL should be handled`() {
        val url = "https://example.com/" + "a".repeat(10000)
        val result = AdversarialDefense.normalize(url)
        // Should complete without exception
        assertTrue(result.normalizedUrl.isNotEmpty())
    }
    
    @Test
    fun `URL with all attack types combined`() {
        // Construct a URL with multiple obfuscation techniques
        val url = "https://аpple\u200B.com\u202E/%252e%252e?url=https://evil.com&x=\u0300"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.detectedAttacks.size >= 3, "Should detect multiple attack types")
        assertTrue(result.riskScore >= 70, "Maximum obfuscation should have high risk")
    }
    
    // ==================== Risk Score Validation ====================
    
    @Test
    fun `risk score should be bounded 0-100`() {
        // URL with many obfuscation techniques
        val url = "https://xn--pple-43d.com/%252e%252e\u200B\u202E?url=http://evil.com"
        val result = AdversarialDefense.normalize(url)
        
        assertTrue(result.riskScore in 0..100, "Risk score should be 0-100")
    }
    
    @Test
    fun `single attack should have proportional risk`() {
        val urlWithOneAttack = "https://drop\u200Bbox.com"
        val result = AdversarialDefense.normalize(urlWithOneAttack)
        
        assertTrue(result.riskScore > 0, "Should have some risk")
        assertTrue(result.riskScore < 50, "Single attack should not max out risk")
    }
}
