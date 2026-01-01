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

package com.raouf.mehrguard.core

import com.raouf.mehrguard.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

/**
 * Comprehensive tests for RiskScorer.
 */
class RiskScorerTest {

    private val scorer = RiskScorer()

    // === SCORING WEIGHTS TESTS ===

    @Test
    fun `scoring weights must sum to 1`() {
        val weights = RiskScorer.ScoringWeights()
        assertEquals(1.0, weights.heuristic + weights.ml + weights.brand + weights.tld, 0.001)
    }

    @Test
    fun `default weights are correct`() {
        val weights = RiskScorer.ScoringWeights()
        assertEquals(0.40, weights.heuristic, 0.001)
        assertEquals(0.35, weights.ml, 0.001)
        assertEquals(0.15, weights.brand, 0.001)
        assertEquals(0.10, weights.tld, 0.001)
    }

    // === SCORE CALCULATION TESTS ===

    @Test
    fun `all zero scores produces zero final score`() {
        val components = RiskScorer.ScoreComponents(
            heuristicScore = 0,
            mlScore = 0.0f,
            brandScore = 0,
            tldScore = 0
        )
        val result = scorer.calculate(components)
        assertEquals(0, result.score)
        assertEquals(Verdict.SAFE, result.verdict)
    }

    @Test
    fun `maximum scores produces capped 100 final score`() {
        val components = RiskScorer.ScoreComponents(
            heuristicScore = 100,
            mlScore = 1.0f,
            brandScore = 100,
            tldScore = 100
        )
        val result = scorer.calculate(components)
        assertEquals(100, result.score)
        assertEquals(Verdict.MALICIOUS, result.verdict)
    }

    @Test
    fun `moderate scores produces suspicious verdict`() {
        val components = RiskScorer.ScoreComponents(
            heuristicScore = 50,
            mlScore = 0.5f,
            brandScore = 50,
            tldScore = 50
        )
        val result = scorer.calculate(components)
        assertTrue(result.score in 31..70)
        assertEquals(Verdict.SUSPICIOUS, result.verdict)
    }

    @Test
    fun `low scores produces safe verdict`() {
        val components = RiskScorer.ScoreComponents(
            heuristicScore = 5,
            mlScore = 0.05f,
            brandScore = 5,
            tldScore = 5
        )
        val result = scorer.calculate(components)
        assertTrue(result.score <= 15, "Score was ${result.score}")
        assertEquals(Verdict.SAFE, result.verdict)
    }

    @Test
    fun `high scores produces malicious verdict`() {
        val components = RiskScorer.ScoreComponents(
            heuristicScore = 90,
            mlScore = 0.9f,
            brandScore = 90,
            tldScore = 90
        )
        val result = scorer.calculate(components)
        assertTrue(result.score > 70)
        assertEquals(Verdict.MALICIOUS, result.verdict)
    }

    @Test
    fun `breakdown map contains all components`() {
        val components = RiskScorer.ScoreComponents(
            heuristicScore = 50,
            mlScore = 0.5f,
            brandScore = 50,
            tldScore = 50
        )
        val result = scorer.calculate(components)

        assertTrue(result.breakdown.containsKey("heuristic"))
        assertTrue(result.breakdown.containsKey("ml"))
        assertTrue(result.breakdown.containsKey("brand"))
        assertTrue(result.breakdown.containsKey("tld"))
    }

    @Test
    fun `confidence is between 0 and 1`() {
        val components = RiskScorer.ScoreComponents(
            heuristicScore = 50,
            mlScore = 0.5f,
            brandScore = 50,
            tldScore = 50
        )
        val result = scorer.calculate(components)
        assertTrue(result.confidence in 0f..1f)
    }

    @Test
    fun `similar component scores produce high confidence`() {
        val components = RiskScorer.ScoreComponents(
            heuristicScore = 50,
            mlScore = 0.5f,
            brandScore = 50,
            tldScore = 50
        )
        val result = scorer.calculate(components)
        assertTrue(result.confidence > 0.7f, "Uniform scores should have high confidence")
    }

    @Test
    fun `divergent component scores produce lower confidence`() {
        val components = RiskScorer.ScoreComponents(
            heuristicScore = 0,
            mlScore = 1.0f,
            brandScore = 0,
            tldScore = 100
        )
        val result = scorer.calculate(components)
        assertTrue(result.confidence < 0.9f, "Divergent scores should have lower confidence")
    }

    // === VERDICT DETERMINATION TESTS ===

    @Test
    fun `score 0 is SAFE`() {
        assertEquals(Verdict.SAFE, scorer.determineVerdict(0))
    }

    @Test
    fun `score 30 is SAFE`() {
        assertEquals(Verdict.SAFE, scorer.determineVerdict(30))
    }

    @Test
    fun `score 31 is SUSPICIOUS`() {
        assertEquals(Verdict.SUSPICIOUS, scorer.determineVerdict(31))
    }

    @Test
    fun `score 69 is SUSPICIOUS`() {
        assertEquals(Verdict.SUSPICIOUS, scorer.determineVerdict(69))
    }

    @Test
    fun `score 70 is MALICIOUS`() {
        assertEquals(Verdict.MALICIOUS, scorer.determineVerdict(70))
    }

    @Test
    fun `score 100 is MALICIOUS`() {
        assertEquals(Verdict.MALICIOUS, scorer.determineVerdict(100))
    }

    // === VERDICT EXPLANATION TESTS ===

    @Test
    fun `safe verdict explanation is correct`() {
        val explanation = scorer.explainVerdict(Verdict.SAFE)
        assertTrue(explanation.contains("safe", ignoreCase = true))
    }

    @Test
    fun `suspicious verdict explanation is correct`() {
        val explanation = scorer.explainVerdict(Verdict.SUSPICIOUS)
        assertTrue(explanation.contains("suspicious", ignoreCase = true) || explanation.contains("caution", ignoreCase = true))
    }

    @Test
    fun `malicious verdict explanation is correct`() {
        val explanation = scorer.explainVerdict(Verdict.MALICIOUS)
        assertTrue(explanation.contains("phishing", ignoreCase = true) || explanation.contains("not recommended", ignoreCase = true))
    }

    @Test
    fun `unknown verdict explanation is correct`() {
        val explanation = scorer.explainVerdict(Verdict.UNKNOWN)
        assertTrue(explanation.contains("unable", ignoreCase = true) || explanation.contains("verify", ignoreCase = true))
    }

    // === VERDICT COLOR TESTS ===

    @Test
    fun `safe verdict has green color`() {
        val color = scorer.getVerdictColor(Verdict.SAFE)
        assertTrue(color != 0L)
    }

    @Test
    fun `suspicious verdict has amber color`() {
        val color = scorer.getVerdictColor(Verdict.SUSPICIOUS)
        assertTrue(color != 0L)
    }

    @Test
    fun `malicious verdict has red color`() {
        val color = scorer.getVerdictColor(Verdict.MALICIOUS)
        assertTrue(color != 0L)
    }

    @Test
    fun `unknown verdict has gray color`() {
        val color = scorer.getVerdictColor(Verdict.UNKNOWN)
        assertTrue(color != 0L)
    }

    @Test
    fun `each verdict has unique color`() {
        val colors = Verdict.entries.map { scorer.getVerdictColor(it) }.toSet()
        assertEquals(Verdict.entries.size, colors.size, "Each verdict should have unique color")
    }

    // === THRESHOLD CONSTANT TESTS ===

    @Test
    fun `safe threshold matches SecurityConstants`() {
        assertEquals(30, SecurityConstants.SAFE_THRESHOLD)
    }

    @Test
    fun `malicious threshold matches SecurityConstants`() {
        assertEquals(70, SecurityConstants.MALICIOUS_THRESHOLD)
    }
}
