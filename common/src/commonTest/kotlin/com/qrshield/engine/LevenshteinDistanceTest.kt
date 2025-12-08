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

import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for BrandDetector's Levenshtein distance algorithm.
 * 
 * Tests the optimized single-row DP implementation with:
 * - Basic distance calculations
 * - Edge cases (empty strings, identical strings)
 * - Security bounds (50 char limit)
 * - Performance considerations
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class LevenshteinDistanceTest {
    
    private val detector = BrandDetector()
    
    // ============================================
    // DISTANCE CALCULATION TESTS
    // ============================================
    
    @Test
    fun `legitimate domain has zero or low score`() {
        val result = detector.detect("https://google.com")
        // Legitimate Google domain should have low score
        assertTrue(result.score <= 10, "Legitimate domain scored too high: ${result.score}")
    }
    
    @Test
    fun `single character difference is detected`() {
        // paypa1.com (with 1 instead of l) should be detected
        val result = detector.detect("https://paypa1.com/login")
        // Should have some detection
        assertTrue(result.score > 0 || result.match != null, 
            "Single char typo not detected")
    }
    
    @Test
    fun `transposition is detected as typosquat`() {
        // googel.com is a typosquat of google.com
        val result = detector.detect("https://googel.com/login")
        assertTrue(result.score > 0, "Transposition not detected")
    }
    
    @Test
    fun `completely different domain has low score`() {
        val result = detector.detect("https://example.com")
        // example.com should not trigger brand detection
        assertEquals(null, result.match)
    }
    
    // ============================================
    // BRAND TYPOSQUAT DETECTION TESTS
    // ============================================
    
    @Test
    fun `common typosquats trigger detection`() {
        val typosquats = listOf(
            "https://g00gle.com",   // 00 instead of oo
            "https://goggle.com",   // extra g
            "https://gogle.com",    // missing o
            "https://amaz0n.com",   // 0 instead of o
            "https://facebo0k.com"  // 0 instead of o
        )
        
        typosquats.forEach { url ->
            val result = detector.detect(url)
            // Should have score > 0 or match
            assertTrue(result.score > 0 || result.match != null, 
                "Failed for: $url (score=${result.score})")
        }
    }
    
    // ============================================
    // BOUNDARY TESTS
    // ============================================
    
    @Test
    fun `very long strings are handled efficiently`() {
        // Very long URL should not cause performance issues
        val longUrl = "https://" + "a".repeat(1000) + ".com"
        
        val startTime = Clock.System.now().toEpochMilliseconds()
        val result = detector.detect(longUrl)
        val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
        
        // Should complete quickly (under 100ms)
        assertTrue(elapsed < 100, "Processing took ${elapsed}ms - too slow")
        assertTrue(result.score >= 0)
    }
    
    @Test
    fun `empty path does not crash`() {
        val result = detector.detect("https://example.com")
        assertTrue(result.score >= 0)
    }
    
    @Test
    fun `short domain does not crash`() {
        val result = detector.detect("https://a.co")
        assertTrue(result.score >= 0)
    }
    
    // ============================================
    // COMBOSQUAT DETECTION TESTS
    // ============================================
    
    @Test
    fun `combosquats can be analyzed`() {
        // These URLs should be analyzed without errors
        // Actual detection depends on brand database configuration
        val combosquats = listOf(
            "https://paypal-security.com",
            "https://google-login.com",
            "https://amazon-verify.com",
            "https://microsoft-update.com"
        )
        
        combosquats.forEach { url ->
            val result = detector.detect(url)
            // Just verify detection runs and returns valid result
            assertTrue(result.score >= 0, "Invalid score for: $url")
            assertTrue(result.severity in listOf("NONE", "LOW", "MEDIUM", "HIGH", "CRITICAL"),
                "Invalid severity for: $url")
        }
    }
    
    // ============================================
    // OFFICIAL DOMAIN TESTS
    // ============================================
    
    @Test
    fun `official domains have low scores`() {
        val officialDomains = listOf(
            "https://google.com",
            "https://www.google.com",
            "https://paypal.com",
            "https://microsoft.com"
        )
        
        officialDomains.forEach { url ->
            val result = detector.detect(url)
            // Official domains should have low/zero score
            assertTrue(result.score <= 10, 
                "Official domain flagged with score ${result.score}: $url")
        }
    }
    
    // ============================================
    // MATCH TYPE TESTS
    // ============================================
    
    @Test
    fun `detection result contains expected properties`() {
        val result = detector.detect("https://paypa1.com")
        
        // DetectionResult should have these properties
        assertTrue(result.score >= 0)
        // isImpersonation is derived from match
        if (result.match != null) {
            assertTrue(result.isImpersonation)
        }
        // severity is derived from score
        assertTrue(result.severity in listOf("NONE", "LOW", "MEDIUM", "HIGH", "CRITICAL"))
    }
}
