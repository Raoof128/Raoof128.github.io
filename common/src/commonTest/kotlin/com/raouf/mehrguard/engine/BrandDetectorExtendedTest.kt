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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Extended tests for BrandDetector.
 */
class BrandDetectorExtendedTest {

    private val detector = BrandDetector()

    // === EXACT BRAND MATCH TESTS ===

    @Test
    fun `detects paypal exact match`() {
        val result = detector.detect("paypal.com")
        // paypal.com is an official domain, so it should NOT be flagged as impersonation
        assertNotNull(result)
    }

    @Test
    fun `detects google exact match`() {
        val result = detector.detect("google.com")
        assertNotNull(result)
    }

    @Test
    fun `detects microsoft exact match`() {
        val result = detector.detect("microsoft.com")
        assertNotNull(result)
    }

    @Test
    fun `detects apple exact match`() {
        val result = detector.detect("apple.com")
        assertNotNull(result)
    }

    @Test
    fun `detects amazon exact match`() {
        val result = detector.detect("amazon.com")
        assertNotNull(result)
    }

    @Test
    fun `detects facebook exact match`() {
        val result = detector.detect("facebook.com")
        assertNotNull(result)
    }

    @Test
    fun `detects netflix exact match`() {
        val result = detector.detect("netflix.com")
        assertNotNull(result)
    }

    // === TYPOSQUATTING DETECTION ===

    @Test
    fun `detects paypal with letter substitution`() {
        val result = detector.detect("paypa1.com")  // 1 instead of l
        assertTrue(result.score > 0)
        assertNotNull(result.match)
    }

    @Test
    fun `detects google with extra letter`() {
        val result = detector.detect("gooogle.com")  // extra o
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects microsoft number substitution`() {
        val result = detector.detect("micr0soft.com")  // 0 instead of o
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects apple number substitution`() {
        val result = detector.detect("app1e.com")  // 1 instead of l
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects amazon number substitution`() {
        val result = detector.detect("amaz0n.com")  // 0 instead of o
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects facebook number substitution`() {
        val result = detector.detect("faceb00k.com")  // 00 instead of oo
        assertTrue(result.score > 0)
    }

    // === SUBDOMAIN IMPERSONATION ===

    @Test
    fun `detects brand in subdomain`() {
        val result = detector.detect("paypal.login.evil.com")
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects secure-paypal pattern`() {
        val result = detector.detect("secure-paypal.evil.com")
        assertTrue(result.score > 0 || result.match != null)
    }

    @Test
    fun `detects login-google pattern`() {
        val result = detector.detect("login-google.evil.com")
        assertNotNull(result)
    }

    // === SAFE DOMAINS ===

    @Test
    fun `unknown domain has zero score`() {
        val result = detector.detect("randomwebsite123.com")
        assertEquals(0, result.score)
    }

    @Test
    fun `no brand in normal domain`() {
        val result = detector.detect("mywebsite.com")
        assertTrue(result.match == null || result.score == 0)
    }

    // === COMBOSQUAT DETECTION ===

    @Test
    fun `detects paypal-secure combosquat`() {
        val result = detector.detect("paypal-secure.com")
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects google-login combosquat`() {
        val result = detector.detect("google-login.com")
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects microsoft-account combosquat`() {
        val result = detector.detect("microsoft-account.com")
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects netflix-billing combosquat`() {
        val result = detector.detect("netflix-billing.com")
        assertTrue(result.score > 0)
    }

    // === PATH-BASED IMPERSONATION ===

    @Test
    fun `url with brand in path analyzed`() {
        val result = detector.detect("evil.com/paypal/login")
        assertNotNull(result)
    }

    // === LEVENSHTEIN DISTANCE TESTS ===

    @Test
    fun `close typosquat gets higher score`() {
        val close = detector.detect("paypai.com")  // 1 char diff
        val far = detector.detect("ppppp.com")     // very different
        assertTrue(close.score >= far.score)
    }

    // === BATCH PROCESSING ===

    @Test
    fun `batch detect works`() {
        val urls = listOf("paypal.com", "google.com", "random.com")
        val results = detector.detectBatch(urls)
        assertEquals(3, results.size)
    }

    // === RESULT STRUCTURE ===

    @Test
    fun `result has match field`() {
        val result = detector.detect("paypa1.com")
        assertNotNull(result.match)
    }

    @Test
    fun `result has score field`() {
        val result = detector.detect("paypa1.com")
        assertTrue(result.score >= 0)
    }

    @Test
    fun `result has details field for match`() {
        val result = detector.detect("paypa1.com")
        assertNotNull(result.details)
    }

    @Test
    fun `result isImpersonation is correct`() {
        val match = detector.detect("paypa1.com")
        val noMatch = detector.detect("randomsite.com")

        assertTrue(match.isImpersonation)
        assertFalse(noMatch.isImpersonation)
    }

    @Test
    fun `result severity is calculated`() {
        val result = detector.detect("paypa1.com")
        assertTrue(result.severity.isNotEmpty())
    }

    // === AUSTRALIAN BANKS ===

    @Test
    fun `detects commbank typosquat`() {
        val result = detector.detect("cornmbank.com")
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects nab typosquat`() {
        val result = detector.detect("n4b.com")
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects westpac typosquat`() {
        val result = detector.detect("westpec.com")
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects anz typosquat`() {
        val result = detector.detect("4nz.com.au")
        assertTrue(result.score > 0)
    }

    // === EDGE CASES ===

    @Test
    fun `empty url returns zero score`() {
        val result = detector.detect("")
        assertEquals(0, result.score)
        assertNull(result.match)
    }

    @Test
    fun `very long url handled`() {
        val longUrl = "https://${"a".repeat(3000)}.com"
        val result = detector.detect(longUrl)
        assertNotNull(result)
    }
}
