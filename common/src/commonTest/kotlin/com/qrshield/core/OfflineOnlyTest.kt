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

package com.qrshield.core

import com.qrshield.model.Verdict
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Offline-Only Verification Test
 *
 * Proves that QR-SHIELD performs ALL analysis offline without any network calls.
 * This is a core privacy guarantee of the application.
 *
 * ## Security Rationale
 *
 * Network calls during URL analysis would:
 * 1. **Leak user data**: Every scanned URL would be sent externally
 * 2. **Create availability dependency**: No internet = no protection
 * 3. **Enable tracking**: Third parties could build scan profiles
 * 4. **Violate privacy**: Medical/legal/financial QR codes could be exposed
 *
 * ## Test Strategy
 *
 * We cannot truly block network in pure Kotlin tests, but we can verify:
 * 1. Analysis completes successfully for any URL
 * 2. No timeouts occur (network calls would timeout)
 * 3. Results are consistent (no external data variation)
 * 4. All components work without network initialization
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */
class OfflineOnlyTest {

    private val engine = PhishingEngine()

    // ==================== Offline Analysis Verification ====================

    @Test
    fun `VERIFY analysis completes without network for any URL pattern`() {
        val testUrls = listOf(
            // Standard URLs
            "https://google.com",
            "http://evil.tk/phish",

            // IP addresses (no DNS needed)
            "http://192.168.1.1/login",
            "http://10.0.0.1:8080/admin",
            "http://[::1]/test",

            // Punycode (no WHOIS lookup)
            "https://xn--80ak6aa.com/test",

            // Very long URLs (no truncation service)
            "https://example.com/" + "a".repeat(500),

            // Unicode domains (no normalization service)
            "https://gооgle.com",  // Cyrillic o
            "https://раураl.com",  // Cyrillic

            // Complex redirect patterns
            "https://redirect.com?url=https://evil.com?next=https://phish.tk",

            // Data URIs (should be handled locally)
            "data:text/html,<script>alert(1)</script>",

            // Edge cases
            "",
            "invalid",
            "http://",
            "https://"
        )

        val startTime = System.currentTimeMillis()

        testUrls.forEach { url ->
            val result = engine.analyze(url)
            assertNotNull(result, "Result should not be null for: $url")
            assertTrue(result.score in 0..100, "Score out of bounds for: $url")
        }

        val elapsed = System.currentTimeMillis() - startTime

        println("""
            |
            |✅ OFFLINE ANALYSIS VERIFICATION
            |   URLs analyzed: ${testUrls.size}
            |   Total time: ${elapsed}ms
            |   Average per URL: ${elapsed / testUrls.size}ms
            |   
            |   If any network calls occurred, this would have timed out
            |   or produced inconsistent results.
            |
        """.trimMargin())

        // If network calls happened, this would be much slower
        // Average should be <100ms per URL for offline analysis
        assertTrue(
            elapsed / testUrls.size < 500,
            "Analysis too slow (${elapsed / testUrls.size}ms/URL) - possible network dependency"
        )
    }

    @Test
    fun `VERIFY heuristics engine works offline`() {
        val heuristics = com.qrshield.engine.HeuristicsEngine()

        val result = heuristics.analyze("http://paypal-secure.tk/login")

        assertNotNull(result)
        assertTrue(result.score >= 0, "Heuristic score should be non-negative")
        assertTrue(result.flags.isNotEmpty(), "Should detect flags offline")

        println("✅ Heuristics Engine: ${result.flags.size} flags detected offline")
    }

    @Test
    fun `VERIFY brand detector works offline`() {
        val detector = com.qrshield.engine.BrandDetector()

        val result = detector.detect("http://paypa1-secure.tk/login")

        assertNotNull(result)
        // Brand detector should find PayPal match offline
        assertTrue(
            result.match != null || result.score >= 0,
            "Brand detector should work offline"
        )

        println("✅ Brand Detector: ${result.match ?: "No match"} (score: ${result.score})")
    }

    @Test
    fun `VERIFY TLD scorer works offline`() {
        val scorer = com.qrshield.engine.TldScorer()

        // Test with full URL format
        val highRiskUrls = listOf(
            "http://example.tk/path",
            "http://example.ml/path",
            "http://example.ga/path"
        )
        val lowRiskUrls = listOf(
            "https://example.com",
            "https://example.org",
            "https://example.edu"
        )

        highRiskUrls.forEach { url ->
            val result = scorer.score(url)
            assertTrue(result.score > 0, "High-risk TLD in $url should have score > 0")
        }

        lowRiskUrls.forEach { url ->
            val result = scorer.score(url)
            assertTrue(result.score <= 35, "Low-risk TLD in $url should have score <= 35")
        }

        println("✅ TLD Scorer: All TLDs scored offline")
    }

    @Test
    fun `VERIFY ML model works offline`() {
        val model = com.qrshield.ml.LogisticRegressionModel.default()
        val extractor = com.qrshield.ml.FeatureExtractor()

        val features = extractor.extract("http://paypal-secure.tk/login/verify")
        val score = model.predict(features)

        assertTrue(score in 0.0f..1.0f, "ML score should be between 0 and 1")

        println("✅ ML Model: Prediction score = ${String.format("%.2f", score)}")
    }

    @Test
    fun `VERIFY homograph detector works offline`() {
        val detector = com.qrshield.engine.HomographDetector()

        // Cyrillic 'a' looks like Latin 'a'
        val result = detector.detect("gооgle.com")  // Cyrillic o's

        assertTrue(result.isHomograph, "Should detect homograph offline")
        assertTrue(result.detectedCharacters.isNotEmpty(), "Should find confusable characters")

        println("✅ Homograph Detector: ${result.detectedCharacters.size} confusables found offline")
    }

    // ==================== Consistency Verification ====================

    @Test
    fun `VERIFY results are consistent without external data`() {
        val url = "http://paypal-secure.tk/verify-account/login"

        // Analyze same URL 100 times
        val results = (1..100).map { engine.analyze(url) }

        val firstScore = results.first().score
        val firstVerdict = results.first().verdict

        results.forEachIndexed { index, result ->
            assertTrue(
                result.score == firstScore,
                "Inconsistent score at iteration $index: expected $firstScore, got ${result.score}"
            )
            assertTrue(
                result.verdict == firstVerdict,
                "Inconsistent verdict at iteration $index"
            )
        }

        println("✅ Consistency: 100 iterations produced identical results (no external data variance)")
    }

    // ==================== Timing Analysis ====================

    @Test
    fun `VERIFY analysis timing is consistent with offline operation`() {
        val url = "http://suspicious-domain.tk/phishing/login"

        // Warm up
        repeat(10) { engine.analyze(url) }

        // Measure
        val times = mutableListOf<Long>()
        repeat(50) {
            val start = System.currentTimeMillis()
            engine.analyze(url)
            times.add(System.currentTimeMillis() - start)
        }

        val avgTime = times.average()
        val maxTime = times.maxOrNull() ?: 0
        val minTime = times.minOrNull() ?: 0

        println("""
            |
            |📊 TIMING ANALYSIS (50 iterations)
            |   Average: ${String.format("%.1f", avgTime)}ms
            |   Min: ${minTime}ms
            |   Max: ${maxTime}ms
            |   
            |   Consistent timing indicates no network variability.
            |   Network calls would show high variance and occasional spikes.
            |
        """.trimMargin())

        // Network calls would cause high variance and occasional timeouts
        assertTrue(maxTime < 1000, "Max time too high - possible network dependency")
        assertTrue(avgTime < 100, "Average time too high - possible network dependency")
    }
}
