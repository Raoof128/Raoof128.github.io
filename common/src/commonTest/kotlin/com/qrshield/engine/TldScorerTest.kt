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

package com.qrshield.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Comprehensive tests for TldScorer.
 * Tests the public score() method.
 */
class TldScorerTest {

    private val scorer = TldScorer()

    // === HIGH RISK TLD TESTS ===

    @Test
    fun `tk tld is high risk`() {
        val result = scorer.score("example.tk")
        assertTrue(result.score >= 15, "TK score was ${result.score}")
        assertTrue(result.isHighRisk)
    }

    @Test
    fun `ml tld is high risk`() {
        val result = scorer.score("example.ml")
        assertTrue(result.score >= 15, "ML score was ${result.score}")
        assertTrue(result.isHighRisk)
    }

    @Test
    fun `ga tld is high risk`() {
        val result = scorer.score("example.ga")
        assertTrue(result.score >= 15, "GA score was ${result.score}")
    }

    @Test
    fun `cf tld is high risk`() {
        val result = scorer.score("example.cf")
        assertTrue(result.isHighRisk)
    }

    @Test
    fun `gq tld is high risk`() {
        val result = scorer.score("example.gq")
        assertTrue(result.isHighRisk)
    }

    @Test
    fun `xyz tld is high risk`() {
        val result = scorer.score("example.xyz")
        assertTrue(result.score >= 10)
    }

    @Test
    fun `top tld is high risk`() {
        val result = scorer.score("example.top")
        assertTrue(result.score >= 10)
    }

    @Test
    fun `site tld is risky`() {
        val result = scorer.score("example.site")
        assertTrue(result.score >= 5)
    }

    @Test
    fun `online tld is risky`() {
        val result = scorer.score("example.online")
        assertTrue(result.score >= 5)
    }

    @Test
    fun `club tld is risky`() {
        val result = scorer.score("example.club")
        assertTrue(result.score >= 5)
    }

    @Test
    fun `bid tld is high risk`() {
        val result = scorer.score("example.bid")
        assertTrue(result.score >= 10)
    }

    @Test
    fun `loan tld is high risk`() {
        val result = scorer.score("example.loan")
        assertTrue(result.score >= 10)
    }

    @Test
    fun `win tld is high risk`() {
        val result = scorer.score("example.win")
        assertTrue(result.score >= 10)
    }

    @Test
    fun `stream tld is high risk`() {
        val result = scorer.score("example.stream")
        assertTrue(result.score >= 10)
    }

    @Test
    fun `download tld is high risk`() {
        val result = scorer.score("example.download")
        assertTrue(result.score >= 10)
    }

    // === SAFE TLD TESTS ===

    @Test
    fun `com tld is safe`() {
        val result = scorer.score("example.com")
        assertEquals(0, result.score)
        assertFalse(result.isHighRisk)
    }

    @Test
    fun `org tld is safe`() {
        val result = scorer.score("example.org")
        assertEquals(0, result.score)
    }

    @Test
    fun `net tld is safe`() {
        val result = scorer.score("example.net")
        assertEquals(0, result.score)
    }

    @Test
    fun `gov tld is safe`() {
        val result = scorer.score("example.gov")
        assertEquals(0, result.score)
    }

    @Test
    fun `edu tld is safe`() {
        val result = scorer.score("example.edu")
        assertEquals(0, result.score)
    }

    @Test
    fun `io tld is low risk`() {
        val result = scorer.score("example.io")
        assertTrue(result.score <= 35, "io score was ${result.score}")  // Moderate risk category
    }

    @Test
    fun `dev tld is safe`() {
        val result = scorer.score("example.dev")
        assertTrue(result.score <= 5)
    }

    @Test
    fun `app tld is safe`() {
        val result = scorer.score("example.app")
        assertTrue(result.score <= 5)
    }

    // === COUNTRY CODE TLD TESTS ===

    @Test
    fun `au tld has country score`() {
        val result = scorer.score("example.com.au")
        assertTrue(result.score <= 15, "au score was ${result.score}")  // Country TLDs score up to 15
    }

    @Test
    fun `uk tld has country score`() {
        val result = scorer.score("example.co.uk")
        assertTrue(result.score <= 15, "uk score was ${result.score}")
    }

    @Test
    fun `de tld has country score`() {
        val result = scorer.score("example.de")
        assertTrue(result.score <= 15, "de score was ${result.score}")
    }

    @Test
    fun `jp tld has country score`() {
        val result = scorer.score("example.jp")
        assertTrue(result.score <= 15, "jp score was ${result.score}")
    }

    // === SCORE RANGE TESTS ===

    @Test
    fun `score is between 0 and 100`() {
        val highRiskTLDs = listOf("tk", "ml", "ga", "xyz", "top", "site")
        for (tld in highRiskTLDs) {
            val result = scorer.score("example.$tld")
            assertTrue(result.score in 0..100, "$tld score ${result.score} out of range")
        }
    }

    @Test
    fun `safe tld has zero score`() {
        val safeTLDs = listOf("com", "org", "net", "gov", "edu")
        for (tld in safeTLDs) {
            val result = scorer.score("example.$tld")
            assertEquals(0, result.score)
        }
    }

    // === IS HIGH RISK METHOD TESTS ===

    @Test
    fun `isHighRiskTld returns true for free tlds`() {
        assertTrue(scorer.isHighRiskTld("tk"))
        assertTrue(scorer.isHighRiskTld("ml"))
        assertTrue(scorer.isHighRiskTld("ga"))
        assertTrue(scorer.isHighRiskTld("cf"))
        assertTrue(scorer.isHighRiskTld("gq"))
    }

    @Test
    fun `isHighRiskTld returns false for established tlds`() {
        assertFalse(scorer.isHighRiskTld("com"))
        assertFalse(scorer.isHighRiskTld("org"))
        assertFalse(scorer.isHighRiskTld("net"))
    }

    @Test
    fun `isHighRiskTld is case insensitive`() {
        assertTrue(scorer.isHighRiskTld("TK"))
        assertTrue(scorer.isHighRiskTld("Tk"))
        assertTrue(scorer.isHighRiskTld("XYZ"))
    }

    // === FULL URL TESTS ===

    @Test
    fun `scores url with https protocol`() {
        val result = scorer.score("https://example.tk")
        assertTrue(result.score >= 15, "Score was ${result.score}")
    }

    @Test
    fun `scores url with path`() {
        val result = scorer.score("https://example.ml/path/to/page")
        assertTrue(result.score >= 15, "Score was ${result.score}")
    }

    @Test
    fun `scores url with query string`() {
        val result = scorer.score("https://example.ga?foo=bar")
        assertTrue(result.score >= 15, "Score was ${result.score}")
    }
}
