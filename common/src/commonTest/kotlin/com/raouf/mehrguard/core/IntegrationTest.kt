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

package com.raouf.mehrguard.core

import com.raouf.mehrguard.engine.HeuristicsEngine
import com.raouf.mehrguard.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the full scan-to-result flow.
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class IntegrationTest {

    @Test
    fun testFullPipelineSafeUrl() {
        val engine = PhishingEngine()
        val result = engine.analyzeBlocking("https://google.com")

        assertNotNull(result)
        assertTrue(result.score < 30, "Google.com should be safe, got ${result.score}")
        assertEquals(Verdict.SAFE, result.verdict)
    }

    @Test
    fun testFullPipelineMaliciousUrl() {
        val engine = PhishingEngine()
        val result = engine.analyzeBlocking("https://paypa1-secure.tk/login/verify")

        assertNotNull(result)
        assertTrue(result.score >= 30, "Phishing URL should be flagged, got ${result.score}")
        assertTrue(result.flags.isNotEmpty(), "Should have risk flags")
    }

    @Test
    fun testHeuristicsEngineWorks() {
        val heuristicsEngine = HeuristicsEngine()
        val result = heuristicsEngine.analyze("https://paypal-secure-login.ml/account")
        assertTrue(result.score > 0, "Heuristics should detect issues")
    }

    @Test
    fun testBankingPhishingScenario() {
        val engine = PhishingEngine()

        val phishingUrls = listOf(
            "https://commbank.secure-verify.ml/login",
            "https://westpac-verify.tk/account"
        )

        phishingUrls.forEach { url ->
            val result = engine.analyzeBlocking(url)
            assertTrue(
                result.score > 0,
                "Bank phishing URL '$url' should be flagged (was ${result.score})"
            )
        }
    }

    @Test
    fun testLegitimateUrlsScenario() {
        val engine = PhishingEngine()

        val safeUrls = listOf(
            "https://www.google.com/search?q=kotlin",
            "https://github.com/JetBrains/kotlin",
            "https://kotlinlang.org/docs/home.html"
        )

        safeUrls.forEach { url ->
            val result = engine.analyzeBlocking(url)
            assertTrue(
                result.verdict == Verdict.SAFE,
                "Legitimate URL '$url' should be safe (verdict: ${result.verdict})"
            )
        }
    }

    @Test
    fun testMultipleAnalysis() {
        val engine = PhishingEngine()
        val urls = listOf(
            "https://google.com",
            "https://paypa1.tk/login",
            "https://facebook.com",
            "https://bank-secure.ml/verify"
        )

        val results = urls.map { engine.analyzeBlocking(it) }

        assertEquals(4, results.size, "All analyses should complete")
        results.forEach { result ->
            assertNotNull(result)
            assertTrue(result.score in 0..100)
        }
    }
}
