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

/**
 * Tests for CounterfactualExplainer.
 *
 * Verifies that the explainer generates meaningful "what if" hints
 * for each type of risk signal.
 */
class CounterfactualExplainerTest {

    private val explainer = CounterfactualExplainer()

    @Test
    fun `generates hint for HTTP not HTTPS`() {
        val signals = mapOf("HTTP_NOT_HTTPS" to 30)
        val hints = explainer.generateHints("http://example.com/login", signals)

        assertEquals(1, hints.size)
        assertEquals("HTTP_NOT_HTTPS", hints[0].signalType)
        assertEquals(30, hints[0].scoreReduction)
        assertTrue(hints[0].explanation.contains("HTTPS"))
    }

    @Test
    fun `generates hint for IP address host`() {
        val signals = mapOf("IP_ADDRESS_HOST" to 50)
        val hints = explainer.generateHints("http://192.168.1.1/admin", signals)

        assertEquals(1, hints.size)
        assertEquals("IP_ADDRESS_HOST", hints[0].signalType)
        assertTrue(hints[0].explanation.contains("domain"))
    }

    @Test
    fun `generates hint for URL shortener`() {
        val signals = mapOf("URL_SHORTENER" to 15)
        val hints = explainer.generateHints("https://bit.ly/abc123", signals)

        assertEquals(1, hints.size)
        assertEquals("URL_SHORTENER", hints[0].signalType)
        assertTrue(hints[0].explanation.contains("destination"))
    }

    @Test
    fun `generates hint for at symbol injection`() {
        val signals = mapOf("AT_SYMBOL_INJECTION" to 60)
        val hints = explainer.generateHints("https://google.com@evil.tk/login", signals)

        assertEquals(1, hints.size)
        assertEquals("AT_SYMBOL_INJECTION", hints[0].signalType)
        assertEquals(60, hints[0].scoreReduction)
    }

    @Test
    fun `generates multiple hints sorted by impact`() {
        val signals = mapOf(
            "HTTP_NOT_HTTPS" to 30,
            "IP_ADDRESS_HOST" to 50,
            "CREDENTIAL_PARAMS" to 40
        )
        val hints = explainer.generateHints("http://192.168.1.1/login?password=test", signals)

        assertEquals(3, hints.size)
        // Should be sorted by score reduction (highest first)
        assertEquals(50, hints[0].scoreReduction)
        assertEquals(40, hints[1].scoreReduction)
        assertEquals(30, hints[2].scoreReduction)
    }

    @Test
    fun `generates safety summary`() {
        val signals = mapOf(
            "HTTP_NOT_HTTPS" to 30,
            "IP_ADDRESS_HOST" to 50
        )
        val hints = explainer.generateHints("http://192.168.1.1/login", signals)
        val summary = explainer.generateSafetySummary(hints)

        assertTrue(summary.contains("80"))
        assertTrue(summary.contains("1."))
        assertTrue(summary.contains("2."))
    }

    @Test
    fun `returns empty hints for unknown signals`() {
        val signals = mapOf("UNKNOWN_SIGNAL" to 10)
        val hints = explainer.generateHints("https://example.com", signals)

        assertEquals(0, hints.size)
    }

    @Test
    fun `generates hint for punycode domain`() {
        val signals = mapOf("PUNYCODE_DOMAIN" to 30)
        val hints = explainer.generateHints("https://xn--pypal-4ve.com/login", signals)

        assertEquals(1, hints.size)
        assertTrue(hints[0].explanation.contains("homograph"))
    }

    @Test
    fun `generates hint for risky extension`() {
        val signals = mapOf("RISKY_EXTENSION" to 40)
        val hints = explainer.generateHints("https://example.com/file.exe", signals)

        assertEquals(1, hints.size)
        assertTrue(hints[0].explanation.contains("executable"))
    }

    @Test
    fun `generates hint for long URL`() {
        val longUrl = "https://example.com/" + "a".repeat(300)
        val signals = mapOf("LONG_URL" to 10)
        val hints = explainer.generateHints(longUrl, signals)

        assertEquals(1, hints.size)
        assertTrue(hints[0].currentValue.contains(longUrl.length.toString()))
    }
}
