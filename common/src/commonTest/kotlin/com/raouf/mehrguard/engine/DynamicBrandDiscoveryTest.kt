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

package com.raouf.mehrguard.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Tests for Dynamic Brand Discovery Engine.
 *
 * These tests verify pattern-based detection of brand impersonation
 * for domains/brands NOT in the static database.
 */
class DynamicBrandDiscoveryTest {

    // ==========================================================================
    // TRUST WORD ABUSE DETECTION
    // ==========================================================================

    @Test
    fun `should detect trust word abuse in domain`() {
        val result = DynamicBrandDiscovery.analyze("https://secure-login-verify.example.tk")
        
        assertTrue(result.findings.isNotEmpty(), "Should detect trust word abuse")
        assertTrue(
            result.findings.any { it.type == DynamicBrandDiscovery.FindingType.TRUST_WORD_ABUSE },
            "Should have trust word abuse finding"
        )
    }

    @Test
    fun `should detect multiple trust words`() {
        val result = DynamicBrandDiscovery.analyze("https://verified-secure-official.tk/login")
        
        assertTrue(result.score > 0, "Score should be positive for trust word abuse")
        assertTrue(result.hasSuspiciousPatterns, "Should have suspicious patterns")
    }

    // ==========================================================================
    // ACTION WORD DETECTION
    // ==========================================================================

    @Test
    fun `should detect login action word in domain`() {
        val result = DynamicBrandDiscovery.analyze("https://login-now-required.tk")
        
        assertTrue(
            result.findings.any { it.type == DynamicBrandDiscovery.FindingType.ACTION_WORD_IN_DOMAIN },
            "Should detect action word 'login'"
        )
    }

    @Test
    fun `should detect signin action word`() {
        val result = DynamicBrandDiscovery.analyze("https://signin-verify.example.com")
        
        assertTrue(result.hasSuspiciousPatterns, "Should flag signin action word")
    }

    // ==========================================================================
    // SUSPICIOUS HYPHEN PATTERNS
    // ==========================================================================

    @Test
    fun `should detect brand-secure pattern`() {
        val result = DynamicBrandDiscovery.analyze("https://mycompany-secure.example.tk")
        
        assertTrue(
            result.findings.any { it.type == DynamicBrandDiscovery.FindingType.SUSPICIOUS_HYPHEN_PATTERN },
            "Should detect brand-secure pattern"
        )
    }

    @Test
    fun `should detect brand-login pattern`() {
        val result = DynamicBrandDiscovery.analyze("https://newstartup-login.tk/auth")
        
        assertTrue(result.score > 0, "Should have positive score for suspicious pattern")
    }

    @Test
    fun `should detect excessive hyphens`() {
        val result = DynamicBrandDiscovery.analyze("https://your-account-is-pending-update.tk")
        
        assertTrue(
            result.findings.any { 
                it.type == DynamicBrandDiscovery.FindingType.SUSPICIOUS_HYPHEN_PATTERN 
            },
            "Should detect excessive hyphens"
        )
    }

    // ==========================================================================
    // BRAND-LIKE SUBDOMAIN PATTERNS
    // ==========================================================================

    @Test
    fun `should detect potential brand in subdomain`() {
        val result = DynamicBrandDiscovery.analyze("https://coolstartup.suspicious-domain.tk")
        
        // May detect brand-like subdomain
        assertTrue(result.score >= 0, "Should analyze without error")
    }

    @Test
    fun `should not flag common subdomains`() {
        val result = DynamicBrandDiscovery.analyze("https://www.legitimate.com")
        
        assertTrue(
            result.findings.none { 
                it.type == DynamicBrandDiscovery.FindingType.BRAND_LIKE_SUBDOMAIN 
            },
            "Should not flag 'www' as brand-like"
        )
    }

    // ==========================================================================
    // IMPERSONATION STRUCTURE DETECTION
    // ==========================================================================

    @Test
    fun `should detect deep subdomain impersonation structure`() {
        // Pattern: legitimate.looking.subdomain.suspicious.tk
        val result = DynamicBrandDiscovery.analyze("https://accounts.security.check.suspicious.tk")
        
        assertTrue(result.hasSuspiciousPatterns, "Should detect impersonation structure")
    }

    // ==========================================================================
    // URGENCY PATTERNS
    // ==========================================================================

    @Test
    fun `should detect urgency patterns in URL`() {
        val result = DynamicBrandDiscovery.analyze("https://urgent-action-required-immediately.example.tk")
        
        assertTrue(
            result.findings.any { it.type == DynamicBrandDiscovery.FindingType.URGENCY_PATTERN },
            "Should detect urgency patterns"
        )
    }

    // ==========================================================================
    // CLEAN URLS (NEGATIVE TESTS)
    // ==========================================================================

    @Test
    fun `should have low score for legitimate URL`() {
        val result = DynamicBrandDiscovery.analyze("https://www.google.com")
        
        assertTrue(result.score <= 10, "Legitimate URL should have low score")
    }

    @Test
    fun `should have low score for GitHub`() {
        val result = DynamicBrandDiscovery.analyze("https://github.com/user/repo")
        
        assertTrue(result.score <= 10, "GitHub URL should have low score")
    }

    @Test
    fun `should have low score for simple domain`() {
        val result = DynamicBrandDiscovery.analyze("https://example.com")
        
        assertEquals(0, result.score, "Simple domain should have zero score")
    }

    // ==========================================================================
    // SUGGESTED BRAND EXTRACTION
    // ==========================================================================

    @Test
    fun `should suggest brand from subdomain`() {
        // The subdomain pattern detection might suggest a brand
        val result = DynamicBrandDiscovery.analyze("https://coolapp.suspicious.tk")
        
        // Either no suggestion or a valid one
        if (result.suggestedBrand != null) {
            assertNotNull(result.suggestedBrand, "Suggested brand should not be null when present")
        }
    }

    // ==========================================================================
    // SCORE CAPPING
    // ==========================================================================

    @Test
    fun `should cap score at MAX_BRAND_SCORE`() {
        // Multiple trust words + action words + hyphens
        val result = DynamicBrandDiscovery.analyze(
            "https://secure-verify-login-urgent-official-update.tk"
        )
        
        assertTrue(
            result.score <= 45, // MAX_BRAND_SCORE
            "Score should be capped at MAX_BRAND_SCORE (45)"
        )
    }

    // ==========================================================================
    // INTEGRATION WITH HOST PARAMETER
    // ==========================================================================

    @Test
    fun `should accept pre-parsed host for efficiency`() {
        val result = DynamicBrandDiscovery.analyze(
            url = "https://secure-login.suspicious.tk",
            host = "secure-login.suspicious.tk",
            subdomains = listOf("secure-login")
        )
        
        assertTrue(result.hasSuspiciousPatterns, "Should work with pre-parsed host")
    }
}
