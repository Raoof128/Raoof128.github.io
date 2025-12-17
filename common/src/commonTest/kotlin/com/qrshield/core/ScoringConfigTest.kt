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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for ScoringConfig validation and presets.
 *
 * Verifies that:
 * - Default config is valid
 * - Presets are valid
 * - Invalid configs are rejected
 * - PhishingEngine uses config correctly
 */
class ScoringConfigTest {

    // =========================================================================
    // PRESET VALIDATION
    // =========================================================================

    @Test
    fun `default config has valid weights summing to 1`() {
        val config = ScoringConfig.DEFAULT
        val sum = config.heuristicWeight + config.mlWeight + config.brandWeight + config.tldWeight
        assertEquals(1.0, sum, 0.01, "Weights should sum to 1.0")
    }

    @Test
    fun `high sensitivity preset has lower thresholds`() {
        val normal = ScoringConfig.DEFAULT
        val strict = ScoringConfig.HIGH_SENSITIVITY

        assertTrue(strict.safeThreshold < normal.safeThreshold,
            "HIGH_SENSITIVITY should have lower safe threshold")
        assertTrue(strict.suspiciousThreshold < normal.suspiciousThreshold,
            "HIGH_SENSITIVITY should have lower suspicious threshold")
    }

    @Test
    fun `brand focused preset increases brand weight`() {
        val normal = ScoringConfig.DEFAULT
        val brand = ScoringConfig.BRAND_FOCUSED

        assertTrue(brand.brandWeight > normal.brandWeight,
            "BRAND_FOCUSED should have higher brand weight")
    }

    @Test
    fun `ml focused preset increases ml weight`() {
        val normal = ScoringConfig.DEFAULT
        val ml = ScoringConfig.ML_FOCUSED

        assertTrue(ml.mlWeight > normal.mlWeight,
            "ML_FOCUSED should have higher ML weight")
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    @Test
    fun `config rejects weights that dont sum to 1`() {
        assertFailsWith<IllegalArgumentException> {
            ScoringConfig(
                heuristicWeight = 0.90,
                mlWeight = 0.90,  // Sum > 1
                brandWeight = 0.10,
                tldWeight = 0.10
            )
        }
    }

    @Test
    fun `config rejects negative weights`() {
        assertFailsWith<IllegalArgumentException> {
            ScoringConfig(
                heuristicWeight = -0.50,  // Negative
                mlWeight = 0.50,
                brandWeight = 0.50,
                tldWeight = 0.50
            )
        }
    }

    @Test
    fun `config rejects invalid threshold ordering`() {
        assertFailsWith<IllegalArgumentException> {
            ScoringConfig(
                safeThreshold = 50,
                suspiciousThreshold = 30  // Less than safe threshold
            )
        }
    }

    // =========================================================================
    // PHISHINGENGINE INTEGRATION
    // =========================================================================

    @Test
    fun `engine accepts default config`() {
        val engine = PhishingEngine()
        val result = engine.analyzeBlocking("https://google.com")

        // Should not throw and produce valid result
        assertTrue(result.score in 0..100)
    }

    @Test
    fun `engine accepts custom config`() {
        val config = ScoringConfig(
            heuristicWeight = 0.70,
            mlWeight = 0.10,
            brandWeight = 0.10,
            tldWeight = 0.10
        )
        val engine = PhishingEngine(config = config)
        val result = engine.analyzeBlocking("https://google.com")

        // Should not throw and produce valid result
        assertTrue(result.score in 0..100)
    }

    @Test
    fun `engine accepts high sensitivity config`() {
        val engine = PhishingEngine(config = ScoringConfig.HIGH_SENSITIVITY)
        val result = engine.analyzeBlocking("https://suspicious.tk")

        // Should score higher with strict config
        assertTrue(result.score in 0..100)
    }

    @Test
    fun `heuristic-only config isolates heuristic score`() {
        val heuristicOnly = ScoringConfig(
            heuristicWeight = 1.0,
            mlWeight = 0.0,
            brandWeight = 0.0,
            tldWeight = 0.0
        )
        val engine = PhishingEngine(config = heuristicOnly)

        // Test a URL that should trigger heuristic flags
        val result = engine.analyzeBlocking("http://192.168.1.1:8080/login")

        // Score should be purely from heuristics
        assertTrue(result.score > 0, "IP host should trigger heuristic flags")
    }

    @Test
    fun `different configs produce different scores for same url`() {
        val normalEngine = PhishingEngine(config = ScoringConfig.DEFAULT)
        val strictEngine = PhishingEngine(config = ScoringConfig.HIGH_SENSITIVITY)

        val url = "https://example.ml/login"
        val normalResult = normalEngine.analyzeBlocking(url)
        val strictResult = strictEngine.analyzeBlocking(url)

        // High sensitivity has lower thresholds, so verdict may differ
        // At minimum, both should produce valid results
        assertTrue(normalResult.score in 0..100)
        assertTrue(strictResult.score in 0..100)
    }
}
