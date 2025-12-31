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
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Comprehensive tests for StaticRedirectPatternAnalyzer.
 *
 * Tests the offline redirect chain detection capability.
 */
class StaticRedirectPatternAnalyzerTest {

    private val simulator = StaticRedirectPatternAnalyzer()

    // === URL SHORTENER DETECTION ===

    @Test
    fun detectsBitlyShortener() {
        val result = simulator.analyze("https://bit.ly/3xYz123")

        assertTrue(result.hasRedirectIndicators, "Should detect bit.ly as redirect")
        assertTrue(result.score > 0, "Should have positive score")
        assertTrue(result.chain.any { it.type == StaticRedirectPatternAnalyzer.HopType.SHORTENER })
    }

    @Test
    fun detectsTcoShortener() {
        val result = simulator.analyze("https://t.co/abc123")

        assertTrue(result.hasRedirectIndicators, "Should detect t.co as redirect")
    }

    @Test
    fun detectsTinyurlShortener() {
        val result = simulator.analyze("https://tinyurl.com/y2abc")

        assertTrue(result.hasRedirectIndicators)
        assertTrue(result.warnings.any { it.contains("shortener", ignoreCase = true) })
    }

    @Test
    fun detectsMultipleShorteners() {
        val shorteners = listOf(
            "https://goo.gl/abc",
            "https://ow.ly/xyz",
            "https://is.gd/123",
            "https://cutt.ly/abc",
            "https://rb.gy/short"
        )

        shorteners.forEach { url ->
            val result = simulator.analyze(url)
            assertTrue(result.hasRedirectIndicators, "Should detect $url as shortener")
        }
    }

    // === EMBEDDED URL DETECTION ===

    @Test
    fun detectsEmbeddedUrl() {
        val result = simulator.analyze(
            "https://evil.com/redirect?url=https://victim-bank.com/login"
        )

        assertTrue(result.hasRedirectIndicators)
        assertTrue(result.chain.any { it.type == StaticRedirectPatternAnalyzer.HopType.EMBEDDED })
        assertTrue(result.score >= 20, "Embedded URL should add 20+ points")
    }

    @Test
    fun detectsEncodedEmbeddedUrl() {
        val result = simulator.analyze(
            "https://evil.com/redirect?url=https%3A%2F%2Fvictim.com%2Flogin"
        )

        assertTrue(result.hasRedirectIndicators)
    }

    // === REDIRECT PARAMETER DETECTION ===

    @Test
    fun detectsRedirectParam() {
        val result = simulator.analyze("https://login.evil.com/?redirect=https://bank.com")

        assertTrue(result.warnings.any { it.contains("redirect parameter", ignoreCase = true) })
    }

    @Test
    fun detectsCommonRedirectParams() {
        val params = listOf("redirect", "url", "goto", "next", "return_url", "callback")

        params.forEach { param ->
            val result = simulator.analyze("https://example.com/?$param=https://target.com")
            assertTrue(
                result.warnings.any { it.contains(param, ignoreCase = true) },
                "Should detect $param parameter"
            )
        }
    }

    // === DOUBLE ENCODING DETECTION ===

    @Test
    fun detectsDoubleEncoding() {
        val result = simulator.analyze(
            "https://evil.com/redirect?url=%2523%252F%252Fphishing.com"
        )

        assertTrue(result.warnings.any { it.contains("double", ignoreCase = true) })
    }

    @Test
    fun detectsEncodedPercent() {
        val result = simulator.analyze(
            "https://evil.com/path%252Fto%252Fphish"
        )

        assertTrue(result.warnings.any { it.contains("encoding", ignoreCase = true) })
    }

    // === TRACKER DETECTION ===

    @Test
    fun detectsTrackingService() {
        val result = simulator.analyze("https://track.mailchimp.com/click/abc123")

        assertTrue(result.hasRedirectIndicators)
        assertTrue(result.chain.any { it.type == StaticRedirectPatternAnalyzer.HopType.TRACKER })
    }

    @Test
    fun detectsClickTracker() {
        val result = simulator.analyze("https://click.campaign.email/redirect/abc")

        assertTrue(result.warnings.any { it.contains("track", ignoreCase = true) })
    }

    // === SAFE URL TESTS ===

    @Test
    fun safeUrlHasNoRedirects() {
        val result = simulator.analyze("https://www.google.com/search?q=kotlin")

        assertFalse(result.hasRedirectIndicators)
        assertEquals(1, result.chain.size, "Should only have initial hop")
        assertEquals(0, result.score)
    }

    @Test
    fun normalUrlWithQueryIsNotRedirect() {
        val result = simulator.analyze("https://shop.example.com/product?id=123&color=blue")

        assertFalse(result.hasRedirectIndicators)
    }

    // === CHAIN STRUCTURE TESTS ===

    @Test
    fun chainStartsWithInitial() {
        val result = simulator.analyze("https://bit.ly/abc")

        assertEquals(StaticRedirectPatternAnalyzer.HopType.INITIAL, result.chain.first().type)
    }

    @Test
    fun chainEndsWithUnknown() {
        val result = simulator.analyze("https://bit.ly/abc")

        assertTrue(result.hasRedirectIndicators)
        assertEquals(StaticRedirectPatternAnalyzer.HopType.UNKNOWN, result.chain.last().type)
    }

    @Test
    fun chainHasCorrectOrder() {
        val result = simulator.analyze("https://bit.ly/redirect?url=https://target.com")

        // Should have: INITIAL -> SHORTENER -> EMBEDDED -> UNKNOWN
        assertTrue(result.chain.size >= 3, "Should have multiple hops")
        assertEquals(StaticRedirectPatternAnalyzer.HopType.INITIAL, result.chain.first().type)
    }

    // === SCORE TESTS ===

    @Test
    fun scoreCappedAt40() {
        val result = simulator.analyze(
            "https://bit.ly/track?redirect=%25252Fhttps://evil.com?url=https://target.com"
        )

        assertTrue(result.score <= 40, "Score should be capped at 40")
    }

    @Test
    fun shortenerAddsScore() {
        val result = simulator.analyze("https://bit.ly/abc123")

        assertTrue(result.score >= 15, "Shortener should add at least 15 points")
    }

    @Test
    fun embeddedUrlAddsHighScore() {
        val result = simulator.analyze("https://evil.com/?url=https://payment.com")

        assertTrue(result.score >= 20, "Embedded URL should add 20+ points")
    }

    // === QUICK CHECK TESTS ===

    @Test
    fun quickCheckDetectsShortener() {
        assertTrue(StaticRedirectPatternAnalyzer.quickCheck("https://bit.ly/abc"))
        assertTrue(StaticRedirectPatternAnalyzer.quickCheck("https://t.co/xyz"))
        assertTrue(StaticRedirectPatternAnalyzer.quickCheck("https://goo.gl/123"))
    }

    @Test
    fun quickCheckDetectsRedirectParam() {
        assertTrue(StaticRedirectPatternAnalyzer.quickCheck("https://example.com?redirect=x"))
        assertTrue(StaticRedirectPatternAnalyzer.quickCheck("https://example.com?url=x"))
        assertTrue(StaticRedirectPatternAnalyzer.quickCheck("https://example.com?goto=x"))
    }

    @Test
    fun quickCheckReturnsFalseForSafeUrl() {
        assertFalse(StaticRedirectPatternAnalyzer.quickCheck("https://www.google.com"))
        assertFalse(StaticRedirectPatternAnalyzer.quickCheck("https://example.org/page"))
    }

    // === COMBINED ATTACK PATTERNS ===

    @Test
    fun detectsComplexRedirectChain() {
        val result = simulator.analyze(
            "https://track.email.service/click?redirect=https://bit.ly/3xYz?url=https://phish.tk"
        )

        assertTrue(result.hasRedirectIndicators)
        assertTrue(result.score >= 25, "Complex chain should have high score")
        assertTrue(result.warnings.size >= 2, "Should have multiple warnings")
    }

    @Test
    fun detectsPhishingWithRedirect() {
        val result = simulator.analyze(
            "https://secure-paypa1.tk/verify?redirect_url=https://real-paypal.com"
        )

        assertTrue(result.warnings.any { it.contains("redirect", ignoreCase = true) })
    }
}
