/*
 * Copyright 2025-2026 QR-SHIELD Contributors
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

package com.raouf.mehrguard.policy

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Organization Policy Engine Tests
 *
 * Tests the policy enforcement engine for enterprise deployments.
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */
class OrgPolicyTest {
    
    // ==================== Domain Allowlist Tests ====================
    
    @Test
    fun `allowed domain bypasses all checks`() {
        val policy = OrgPolicy(
            allowedDomains = setOf("internal.company.com")
        )
        
        val result = policy.evaluate("https://internal.company.com/any/path")
        
        assertIs<PolicyResult.Allowed>(result)
        assertTrue(result.reason.contains("allowlist"))
    }
    
    @Test
    fun `wildcard domain pattern matches subdomains`() {
        val policy = OrgPolicy(
            allowedDomains = setOf("*.company.com")
        )
        
        val result1 = policy.evaluate("https://internal.company.com/page")
        val result2 = policy.evaluate("https://api.company.com/v1/users")
        val result3 = policy.evaluate("https://company.com/home")
        
        assertIs<PolicyResult.Allowed>(result1)
        assertIs<PolicyResult.Allowed>(result2)
        assertIs<PolicyResult.Allowed>(result3, "Should match base domain too")
    }
    
    @Test
    fun `non-allowed domain is not bypassed`() {
        val policy = OrgPolicy(
            allowedDomains = setOf("company.com")
        )
        
        val result = policy.evaluate("https://phishing.tk/fake")
        
        assertIs<PolicyResult.PassedPolicy>(result)
    }
    
    // ==================== Domain Blocklist Tests ====================
    
    @Test
    fun `blocked domain is rejected`() {
        val policy = OrgPolicy(
            blockedDomains = setOf("malware.tk")
        )
        
        val result = policy.evaluate("https://malware.tk/payload")
        
        assertIs<PolicyResult.Blocked>(result)
        assertEquals(BlockReason.DOMAIN_BLOCKED, result.blockReason)
    }
    
    @Test
    fun `wildcard blocklist catches subdomains`() {
        val policy = OrgPolicy(
            blockedDomains = setOf("*.badsite.com")
        )
        
        val result = policy.evaluate("https://evil.badsite.com/phish")
        
        assertIs<PolicyResult.Blocked>(result)
    }
    
    // ==================== TLD Blocking Tests ====================
    
    @Test
    fun `blocked TLD is rejected`() {
        val policy = OrgPolicy(
            blockedTlds = setOf("tk", "ml", "ga")
        )
        
        val resultTk = policy.evaluate("https://example.tk/page")
        val resultMl = policy.evaluate("https://phishing.ml/login")
        val resultGa = policy.evaluate("https://scam.ga/prize")
        
        assertIs<PolicyResult.Blocked>(resultTk)
        assertIs<PolicyResult.Blocked>(resultMl)
        assertIs<PolicyResult.Blocked>(resultGa)
        assertEquals(BlockReason.TLD_BLOCKED, resultTk.blockReason)
    }
    
    @Test
    fun `legitimate TLD is not blocked`() {
        val policy = OrgPolicy(
            blockedTlds = setOf("tk", "ml")
        )
        
        val result = policy.evaluate("https://google.com/search")
        
        assertIs<PolicyResult.PassedPolicy>(result)
    }
    
    // ==================== HTTPS Requirement Tests ====================
    
    @Test
    fun `HTTP is blocked when HTTPS required`() {
        val policy = OrgPolicy(requireHttps = true)
        
        val result = policy.evaluate("http://example.com/insecure")
        
        assertIs<PolicyResult.Blocked>(result)
        assertEquals(BlockReason.HTTPS_REQUIRED, result.blockReason)
    }
    
    @Test
    fun `HTTPS is allowed when HTTPS required`() {
        val policy = OrgPolicy(requireHttps = true)
        
        val result = policy.evaluate("https://example.com/secure")
        
        assertIs<PolicyResult.PassedPolicy>(result)
    }
    
    // ==================== IP Address Blocking Tests ====================
    
    @Test
    fun `IPv4 address is blocked when policy enabled`() {
        val policy = OrgPolicy(blockIpAddresses = true)
        
        val result = policy.evaluate("http://192.168.1.1/admin")
        
        assertIs<PolicyResult.Blocked>(result)
        assertEquals(BlockReason.IP_ADDRESS, result.blockReason)
    }
    
    @Test
    fun `IPv6 address is blocked when policy enabled`() {
        val policy = OrgPolicy(blockIpAddresses = true)
        
        val result = policy.evaluate("http://[::1]/admin")
        
        assertIs<PolicyResult.Blocked>(result)
    }
    
    @Test
    fun `domain is allowed when IP blocking enabled`() {
        val policy = OrgPolicy(blockIpAddresses = true)
        
        val result = policy.evaluate("https://example.com/page")
        
        assertIs<PolicyResult.PassedPolicy>(result)
    }
    
    // ==================== URL Shortener Blocking Tests ====================
    
    @Test
    fun `URL shorteners are blocked when policy enabled`() {
        val policy = OrgPolicy(blockShorteners = true)
        
        val shorteners = listOf(
            "https://bit.ly/abc123",
            "https://t.co/xyz789",
            "https://tinyurl.com/short",
            "https://goo.gl/maps/abc"
        )
        
        shorteners.forEach { url ->
            val result = policy.evaluate(url)
            assertIs<PolicyResult.Blocked>(result, "Should block: $url")
            assertEquals(BlockReason.SHORTENER, result.blockReason)
        }
    }
    
    // ==================== URL Length Tests ====================
    
    @Test
    fun `URL exceeding max length is blocked`() {
        val policy = OrgPolicy(maxUrlLength = 100)
        
        val longUrl = "https://example.com/" + "a".repeat(200)
        val result = policy.evaluate(longUrl)
        
        assertIs<PolicyResult.Blocked>(result)
        assertEquals(BlockReason.LENGTH_EXCEEDED, result.blockReason)
    }
    
    @Test
    fun `URL within max length is allowed`() {
        val policy = OrgPolicy(maxUrlLength = 100)
        
        val shortUrl = "https://example.com/page"
        val result = policy.evaluate(shortUrl)
        
        assertIs<PolicyResult.PassedPolicy>(result)
    }
    
    // ==================== Pattern Tests ====================
    
    @Test
    fun `allowed pattern bypasses checks`() {
        val policy = OrgPolicy(
            allowedPatterns = listOf(""".*\.company\.com/api/.*""")
        )
        
        val result = policy.evaluate("https://any.company.com/api/v1/users")
        
        assertIs<PolicyResult.Allowed>(result)
    }
    
    @Test
    fun `blocked pattern is rejected`() {
        val policy = OrgPolicy(
            blockedPatterns = listOf(""".*phish.*""", """.*malware.*""")
        )
        
        val result1 = policy.evaluate("https://phishing-site.com/login")
        val result2 = policy.evaluate("https://download-malware.tk/virus")
        
        assertIs<PolicyResult.Blocked>(result1)
        assertIs<PolicyResult.Blocked>(result2)
        assertEquals(BlockReason.PATTERN_MATCH, result1.blockReason)
    }
    
    // ==================== Strict Mode Tests ====================
    
    @Test
    fun `strict mode flags excessive subdomains`() {
        val policy = OrgPolicy(strictMode = true)
        
        val result = policy.evaluate("https://a.b.c.d.e.f.example.com/page")
        
        assertIs<PolicyResult.RequiresReview>(result)
        assertTrue(result.reason.contains("subdomain"))
    }
    
    // ==================== Payload Type Tests ====================
    
    @Test
    fun `blocked payload type is rejected`() {
        val policy = OrgPolicy(
            allowedPayloadTypes = setOf(QrPayloadType.URL_HTTPS, QrPayloadType.TEXT)
        )
        
        val result = policy.evaluatePayload("WIFI:T:WPA;S:Test;P:pass;;", QrPayloadType.WIFI)
        
        assertIs<PolicyResult.Blocked>(result)
        assertEquals(BlockReason.PAYLOAD_TYPE_BLOCKED, result.blockReason)
    }
    
    @Test
    fun `allowed payload type passes`() {
        val policy = OrgPolicy(
            allowedPayloadTypes = setOf(QrPayloadType.URL_HTTPS, QrPayloadType.TEXT)
        )
        
        val result = policy.evaluatePayload("https://example.com", QrPayloadType.URL_HTTPS)
        
        assertIs<PolicyResult.PassedPolicy>(result)
    }
    
    // ==================== SMS Smishing Detection Tests ====================
    
    @Test
    fun `SMS with blocked URL is flagged as smishing`() {
        val policy = OrgPolicy(
            blockedDomains = setOf("malware.tk")
        )
        
        val smsPayload = "sms:+1234567890?body=Click%20here%20https://malware.tk/prize"
        val result = policy.evaluatePayload(smsPayload, QrPayloadType.SMS)
        
        assertIs<PolicyResult.Blocked>(result)
        assertEquals(BlockReason.SMISHING_DETECTED, result.blockReason)
    }
    
    // ==================== JSON Parsing Tests ====================
    
    @Test
    fun `policy loads from JSON`() {
        val json = """
        {
            "version": "1.0",
            "orgId": "test-org",
            "orgName": "Test Organization",
            "strictMode": true,
            "blockedTlds": ["tk", "ml"],
            "requireHttps": true,
            "blockShorteners": true
        }
        """
        
        val policy = OrgPolicy.fromJson(json)
        
        assertEquals("1.0", policy.version)
        assertEquals("test-org", policy.orgId)
        assertTrue(policy.strictMode)
        assertTrue("tk" in policy.blockedTlds)
        assertTrue("ml" in policy.blockedTlds)
        assertTrue(policy.requireHttps)
        assertTrue(policy.blockShorteners)
    }
    
    @Test
    fun `policy exports to JSON`() {
        val policy = OrgPolicy(
            orgId = "export-test",
            strictMode = true,
            blockedTlds = setOf("tk")
        )
        
        val json = policy.toJson()
        
        assertTrue(json.contains("export-test"))
        assertTrue(json.contains("strictMode"))
        assertTrue(json.contains("tk"))
    }
    
    // ==================== Preset Policy Tests ====================
    
    @Test
    fun `ENTERPRISE_STRICT policy blocks high-risk TLDs`() {
        val policy = OrgPolicy.ENTERPRISE_STRICT
        
        val result = policy.evaluate("https://scam.tk/phish")
        
        assertIs<PolicyResult.Blocked>(result)
    }
    
    @Test
    fun `FINANCIAL policy has crypto blocking`() {
        val policy = OrgPolicy.FINANCIAL
        
        assertTrue("crypto" in policy.blockedCategories)
        assertTrue("gambling" in policy.blockedCategories)
        assertTrue(policy.strictMode)
    }
    
    // ==================== Priority Tests ====================
    
    @Test
    fun `allowed pattern takes priority over blocked TLD`() {
        val policy = OrgPolicy(
            blockedTlds = setOf("tk"),
            allowedPatterns = listOf(""".*trusted\.tk.*""")
        )
        
        val result = policy.evaluate("https://trusted.tk/safe-page")
        
        assertIs<PolicyResult.Allowed>(result)
    }
    
    @Test
    fun `allowed domain takes priority over shortener block`() {
        val policy = OrgPolicy(
            blockShorteners = true,
            allowedDomains = setOf("bit.ly")  // Company uses bit.ly for marketing
        )
        
        val result = policy.evaluate("https://bit.ly/company-link")
        
        assertIs<PolicyResult.Allowed>(result)
    }
}
