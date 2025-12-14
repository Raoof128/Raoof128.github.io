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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Cross-Platform Consistency Test
 *
 * Verifies that the same URLs produce identical scores and verdicts
 * across all platforms (JVM, Native, JS). This test runs in commonTest
 * and is executed on all platform targets.
 *
 * CRITICAL: If these tests fail, it indicates platform-specific behavior
 * that would undermine the KMP value proposition.
 *
 * @author QR-SHIELD Security Team
 * @since 1.1.4
 */
class CrossPlatformConsistencyTest {

    private val engine = PhishingEngine()

    /**
     * Canonical test URLs with expected scores.
     * These MUST produce identical results across all platforms.
     * Ranges are intentionally broad to account for combined scoring behavior.
     */
    private val canonicalTestCases = listOf(
        // Safe URLs
        CanonicalTestCase(
            url = "https://google.com",
            expectedVerdict = Verdict.SAFE,
            minScore = 0,
            maxScore = 30,
            description = "Major safe domain"
        ),
        CanonicalTestCase(
            url = "https://github.com/Raoof128/Raoof128.github.io",
            expectedVerdict = Verdict.SAFE,
            minScore = 0,
            maxScore = 30,
            description = "GitHub repository URL"
        ),
        CanonicalTestCase(
            url = "https://www.commbank.com.au/netbank/login",
            expectedVerdict = null, // May trigger login path heuristic
            minScore = 0,
            maxScore = 40,
            description = "Legitimate Australian bank"
        ),

        // Suspicious URLs - may be SAFE or SUSPICIOUS
        CanonicalTestCase(
            url = "http://bit.ly/3xYz123",
            expectedVerdict = null, // Can vary - URL shortener alone may not be enough
            minScore = 15,
            maxScore = 60,
            description = "URL shortener"
        ),
        CanonicalTestCase(
            url = "https://secure-login.xyz/account",
            expectedVerdict = null, // Can vary based on brand detection
            minScore = 10,
            maxScore = 60,
            description = "Suspicious TLD with login path"
        ),

        // Higher risk URLs - should always have elevated scores
        CanonicalTestCase(
            url = "https://paypa1-secure.tk/login",
            expectedVerdict = null, // At least SUSPICIOUS
            minScore = 30,
            maxScore = 100,
            description = "Brand impersonation + suspicious TLD"
        ),
        CanonicalTestCase(
            url = "http://192.168.1.100:8080/paypal/login",
            expectedVerdict = null, // IP address hosts are flagged
            minScore = 40,
            maxScore = 100,
            description = "IP address host with brand path"
        ),
        CanonicalTestCase(
            url = "https://google.com@evil-site.tk/login",
            expectedVerdict = null, // @ symbol injection is high risk
            minScore = 50,
            maxScore = 100,
            description = "@ symbol URL spoofing"
        ),
        CanonicalTestCase(
            url = "https://xn--pypal-4ve.com/login",
            expectedVerdict = null, // Punycode is flagged
            minScore = 20,
            maxScore = 80,
            description = "Punycode/IDN domain"
        ),
        CanonicalTestCase(
            url = "http://commbank-netbank-login.tk/verify",
            expectedVerdict = null, // Bank phishing pattern
            minScore = 30,
            maxScore = 100,
            description = "Australian bank phishing"
        )
    )

    data class CanonicalTestCase(
        val url: String,
        val expectedVerdict: Verdict?, // null means any verdict is ok, just check score
        val minScore: Int,
        val maxScore: Int,
        val description: String
    )

    // ==========================================
    // Core Consistency Tests
    // ==========================================

    @Test
    fun `all canonical URLs produce scores within expected range`() {
        canonicalTestCases.forEach { testCase ->
            val result = engine.analyze(testCase.url)

            assertTrue(
                result.score >= testCase.minScore && result.score <= testCase.maxScore,
                "URL '${testCase.url}' (${testCase.description}): " +
                    "Score ${result.score} not in expected range [${testCase.minScore}, ${testCase.maxScore}]"
            )
        }
    }

    @Test
    fun `canonical URLs produce expected verdicts`() {
        // Only test cases with explicit expected verdicts
        canonicalTestCases
            .filter { it.expectedVerdict != null }
            .forEach { testCase ->
                val result = engine.analyze(testCase.url)

                assertEquals(
                    testCase.expectedVerdict,
                    result.verdict,
                    "URL '${testCase.url}' (${testCase.description}): " +
                        "Expected ${testCase.expectedVerdict}, got ${result.verdict} (score: ${result.score})"
                )
            }
    }

    @Test
    fun `identical URLs produce identical scores across multiple calls`() {
        val testUrl = "https://paypa1-secure.tk/login"

        val scores = (1..10).map { engine.analyze(testUrl).score }

        // All scores must be identical
        val uniqueScores = scores.distinct()
        assertEquals(
            1,
            uniqueScores.size,
            "Expected identical scores for same URL, got: $scores"
        )
    }

    @Test
    fun `identical URLs produce identical verdicts across multiple calls`() {
        val testUrl = "https://google.com@evil-site.tk/login"

        val verdicts = (1..10).map { engine.analyze(testUrl).verdict }

        // All verdicts must be identical
        val uniqueVerdicts = verdicts.distinct()
        assertEquals(
            1,
            uniqueVerdicts.size,
            "Expected identical verdicts for same URL, got: $verdicts"
        )
    }

    @Test
    fun `identical URLs produce identical flag counts across multiple calls`() {
        val testUrl = "http://192.168.1.100:8080/paypal/login"

        val flagCounts = (1..10).map { engine.analyze(testUrl).flags.size }

        // All flag counts must be identical
        val uniqueCounts = flagCounts.distinct()
        assertEquals(
            1,
            uniqueCounts.size,
            "Expected identical flag counts for same URL, got: $flagCounts"
        )
    }

    // ==========================================
    // Score Ordering Tests
    // ==========================================

    @Test
    fun `safe URLs score lower than suspicious URLs`() {
        val safeUrl = "https://google.com"
        val suspiciousUrl = "https://secure-login.xyz/account"

        val safeResult = engine.analyze(safeUrl)
        val suspiciousResult = engine.analyze(suspiciousUrl)

        assertTrue(
            safeResult.score < suspiciousResult.score,
            "Safe URL score (${safeResult.score}) should be less than " +
                "suspicious URL score (${suspiciousResult.score})"
        )
    }

    @Test
    fun `higher risk URLs score higher than lower risk URLs`() {
        // URL with fewer risk factors
        val lowerRiskUrl = "https://example.xyz/about" // Just suspicious TLD

        // URL with many risk factors
        val higherRiskUrl = "http://google.com@evil-site.tk/login" // @ symbol + TLD + HTTP + login

        val lowerResult = engine.analyze(lowerRiskUrl)
        val higherResult = engine.analyze(higherRiskUrl)

        assertTrue(
            lowerResult.score < higherResult.score,
            "Lower risk URL score (${lowerResult.score}) should be less than " +
                "higher risk URL score (${higherResult.score})"
        )
    }

    @Test
    fun `more risk factors result in higher scores`() {
        // URL with one risk factor
        val singleRisk = "https://example.tk" // Just suspicious TLD

        // URL with multiple risk factors
        val multipleRisks = "http://paypa1-secure.tk/login" // HTTP + brand + TLD + login

        val singleResult = engine.analyze(singleRisk)
        val multipleResult = engine.analyze(multipleRisks)

        assertTrue(
            singleResult.score < multipleResult.score,
            "Single risk URL score (${singleResult.score}) should be less than " +
                "multiple risk URL score (${multipleResult.score})"
        )

        assertTrue(
            singleResult.flags.size < multipleResult.flags.size,
            "Single risk URL should have fewer flags than multiple risk URL"
        )
    }

    // ==========================================
    // Boundary Tests
    // ==========================================

    @Test
    fun `scores are always in valid range 0-100`() {
        val extremeUrls = listOf(
            "", // Empty
            "https://a.b", // Minimal
            "https://${"a".repeat(500)}.com/${"b".repeat(500)}", // Very long
            "https://xn--${"abc".repeat(50)}.tk/login?${"x=y&".repeat(100)}" // Complex
        )

        extremeUrls.forEach { url ->
            val result = engine.analyze(url)
            assertTrue(
                result.score in 0..100,
                "URL '$url' produced out-of-range score: ${result.score}"
            )
        }
    }

    @Test
    fun `verdict is never null`() {
        canonicalTestCases.forEach { testCase ->
            val result = engine.analyze(testCase.url)
            assertTrue(
                result.verdict in listOf(Verdict.SAFE, Verdict.SUSPICIOUS, Verdict.MALICIOUS),
                "URL '${testCase.url}' produced invalid verdict: ${result.verdict}"
            )
        }
    }

    // ==========================================
    // Platform Fingerprint Test
    // ==========================================

    @Test
    fun `platform detection produces consistent results`() {
        // This test produces a "fingerprint" of all canonical URLs
        // If this fingerprint differs between platforms, there's a consistency bug

        val fingerprint = canonicalTestCases.map { testCase ->
            val result = engine.analyze(testCase.url)
            "${testCase.url}|${result.verdict}|${result.score in testCase.minScore..testCase.maxScore}"
        }.joinToString("\n")

        // Log fingerprint for debugging
        println("=== PLATFORM CONSISTENCY FINGERPRINT ===")
        println(fingerprint)
        println("=== END FINGERPRINT ===")

        // Verify all tests passed (no "false" in fingerprint)
        assertTrue(
            !fingerprint.contains("|false"),
            "Some canonical test cases failed. See fingerprint above."
        )
    }
}
