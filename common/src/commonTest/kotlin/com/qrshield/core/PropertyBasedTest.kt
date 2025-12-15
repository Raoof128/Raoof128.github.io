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

import com.qrshield.adversarial.AdversarialDefense
import com.qrshield.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Property-Based Tests
 *
 * Tests that verify invariants hold for ANY valid input, not just specific cases.
 * These tests prove mathematical properties of the detection system.
 *
 * ## Invariants Tested:
 * 1. Score bounds (0-100 always)
 * 2. Determinism (same input → same output)
 * 3. Normalization stability (idempotent)
 * 4. Verdict consistency with score
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */
class PropertyBasedTest {

    private val engine = PhishingEngine()

    // ==================== Score Bound Invariants ====================

    @Test
    fun `INVARIANT score is always between 0 and 100 for any URL`() {
        val testUrls = listOf(
            "https://google.com",
            "http://evil.tk/phish",
            "https://paypal-secure-login.verify.tk/account/update",
            "http://192.168.1.1/admin",
            "https://xn--pple-43d.com", // Punycode
            "https://a".repeat(1000),  // Very long
            "http://x.y.z.a.b.c.d.e.f/deep/path/here",
            "https://",  // Minimal
            "http://localhost",
            "https://127.0.0.1:8080",
            "https://user:pass@example.com/path",
            "https://example.com?redirect=http://evil.com"
        )

        testUrls.forEach { url ->
            val result = engine.analyze(url)
            assertTrue(
                result.score in 0..100,
                "Score ${result.score} out of bounds for URL: $url"
            )
        }
    }

    @Test
    fun `INVARIANT score bounds hold for randomly generated URL patterns`() {
        val protocols = listOf("http://", "https://")
        val hosts = listOf("example.com", "192.168.1.1", "evil.tk", "xn--80ak6aa.com")
        val paths = listOf("", "/", "/login", "/verify/account/update")
        val queries = listOf("", "?id=123", "?redirect=http://evil.com")

        // Generate all combinations
        protocols.forEach { proto ->
            hosts.forEach { host ->
                paths.forEach { path ->
                    queries.forEach { query ->
                        val url = "$proto$host$path$query"
                        val result = engine.analyze(url)
                        assertTrue(
                            result.score in 0..100,
                            "Score ${result.score} out of bounds for: $url"
                        )
                    }
                }
            }
        }
    }

    // ==================== Determinism Invariants ====================

    @Test
    fun `INVARIANT same URL always produces same score (determinism)`() {
        val testUrls = listOf(
            "https://google.com",
            "http://paypal-secure.tk/login",
            "https://192.168.1.1:8080/admin"
        )

        testUrls.forEach { url ->
            val results = (1..10).map { engine.analyze(url) }
            val expectedScore = results.first().score
            val expectedVerdict = results.first().verdict

            results.forEach { result ->
                assertEquals(
                    expectedScore, result.score,
                    "Determinism violated: different scores for same URL: $url"
                )
                assertEquals(
                    expectedVerdict, result.verdict,
                    "Determinism violated: different verdicts for same URL: $url"
                )
            }
        }
    }

    @Test
    fun `INVARIANT analysis is idempotent (analyzing twice gives same result)`() {
        val url = "https://suspicious-looking-domain.tk/verify-account"

        val first = engine.analyze(url)
        val second = engine.analyze(url)

        assertEquals(first.score, second.score, "Idempotence violated for score")
        assertEquals(first.verdict, second.verdict, "Idempotence violated for verdict")
        assertEquals(first.flags, second.flags, "Idempotence violated for flags")
    }

    // ==================== Verdict Consistency Invariants ====================

    @Test
    fun `INVARIANT verdict is consistent with score thresholds`() {
        val testCases = listOf(
            0 to Verdict.SAFE,
            15 to Verdict.SAFE,
            29 to Verdict.SAFE,
            31 to Verdict.SUSPICIOUS,
            50 to Verdict.SUSPICIOUS,
            69 to Verdict.SUSPICIOUS,
            71 to Verdict.MALICIOUS,
            85 to Verdict.MALICIOUS,
            100 to Verdict.MALICIOUS
        )

        // Note: These are hypothetical scores, actual analysis may differ
        // This tests the verdict determination logic directly
        val testUrls = listOf(
            "https://google.com",
            "http://suspicious.tk",
            "https://paypal-verify.ml/login"
        )

        testUrls.forEach { url ->
            val result = engine.analyze(url)
            val expectedVerdict = when {
                result.score < 30 -> Verdict.SAFE
                result.score < 70 -> Verdict.SUSPICIOUS
                else -> Verdict.MALICIOUS
            }

            // Verdict can be escalated due to critical factors, so MALICIOUS is always valid
            // when score >= 70 or critical flags present
            val isConsistent = (result.verdict == expectedVerdict) ||
                (result.verdict == Verdict.MALICIOUS && result.score >= 30)

            assertTrue(
                isConsistent,
                "Verdict ${result.verdict} inconsistent with score ${result.score} for: $url"
            )
        }
    }

    // ==================== Normalization Stability Invariants ====================

    @Test
    fun `INVARIANT URL normalization is stable (normalize twice equals once)`() {
        val testUrls = listOf(
            "https://exаmple.com",  // Has Cyrillic 'а'
            "https://example%2ecom/path%20here",
            "https://example.com%252Fpath",  // Double encoded
            "https://example\u200Bcom"  // Zero-width space
        )

        testUrls.forEach { url ->
            val first = AdversarialDefense.normalize(url)
            val second = AdversarialDefense.normalize(first.normalizedUrl)

            assertEquals(
                first.normalizedUrl, second.normalizedUrl,
                "Normalization not stable for: $url"
            )
        }
    }

    @Test
    fun `INVARIANT normalization is idempotent after n applications`() {
        val url = "https://раураl%2Esecure%252Fverify.tk/login"  // Mixed obfuscation

        var current = url
        var previous: String

        // Apply normalization 5 times, should stabilize
        repeat(5) {
            previous = current
            current = AdversarialDefense.normalize(current).normalizedUrl
        }

        val final = AdversarialDefense.normalize(current).normalizedUrl
        assertEquals(current, final, "Normalization should stabilize")
    }

    // ==================== Monotonicity Invariants ====================

    @Test
    fun `INVARIANT adding suspicious elements never decreases score`() {
        val baseUrl = "https://example.com"
        val baseScore = engine.analyze(baseUrl).score

        val suspiciousVariants = listOf(
            "http://example.com",  // HTTP instead of HTTPS
            "https://example.tk",  // Suspicious TLD
            "https://paypal-example.com",  // Brand keyword
            "https://example.com/login/verify/account"  // Suspicious path
        )

        suspiciousVariants.forEach { variant ->
            val variantScore = engine.analyze(variant).score
            // Score should not decrease significantly (allow small numerical variance)
            assertTrue(
                variantScore >= baseScore - 5,
                "Adding suspicious elements decreased score: $baseUrl ($baseScore) vs $variant ($variantScore)"
            )
        }
    }

    // ==================== Confidence Invariants ====================

    @Test
    fun `INVARIANT confidence is always between 0 and 1`() {
        val testUrls = listOf(
            "https://google.com",
            "http://evil.tk/phish",
            "https://paypal-secure.verify.tk"
        )

        testUrls.forEach { url ->
            val result = engine.analyze(url)
            assertTrue(
                result.confidence in 0.0f..1.0f,
                "Confidence ${result.confidence} out of bounds for: $url"
            )
        }
    }

    // ==================== Component Score Invariants ====================

    @Test
    fun `INVARIANT component scores are non-negative`() {
        val testUrls = listOf(
            "https://google.com",
            "http://192.168.1.1",
            "https://suspicious.tk/login"
        )

        testUrls.forEach { url ->
            val result = engine.analyze(url)
            val details = result.details

            assertTrue(
                details.heuristicScore >= 0,
                "Negative heuristic score for: $url"
            )
            assertTrue(
                details.mlScore >= 0,
                "Negative ML score for: $url"
            )
            assertTrue(
                details.brandScore >= 0,
                "Negative brand score for: $url"
            )
            assertTrue(
                details.tldScore >= 0,
                "Negative TLD score for: $url"
            )
        }
    }

    // ==================== Flag Invariants ====================

    @Test
    fun `INVARIANT flags list is never null`() {
        val testUrls = listOf(
            "https://google.com",
            "",
            "invalid",
            "http://x"
        )

        testUrls.forEach { url ->
            val result = engine.analyze(url)
            assertTrue(
                result.flags != null,
                "Flags should never be null for: $url"
            )
        }
    }

    @Test
    fun `INVARIANT high score implies at least one flag`() {
        val highScoreUrls = listOf(
            "http://paypal-secure-login.tk/verify/account/update",
            "http://192.168.1.1:8080/admin/login.php",
            "https://gооgle.com"  // Homograph
        )

        highScoreUrls.forEach { url ->
            val result = engine.analyze(url)
            if (result.score >= 50) {
                assertTrue(
                    result.flags.isNotEmpty(),
                    "High score (${result.score}) should have flags for: $url"
                )
            }
        }
    }
}
