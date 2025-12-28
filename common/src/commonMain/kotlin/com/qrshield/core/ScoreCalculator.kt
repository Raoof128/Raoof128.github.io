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

import com.qrshield.engine.BrandDetector
import com.qrshield.engine.HeuristicsEngine
import com.qrshield.engine.TldScorer
import com.qrshield.model.Verdict

/**
 * Score Calculator for PhishingEngine
 *
 * Extracted from PhishingEngine to improve code organization and testability.
 * Handles all score calculation logic including normalization and weighting.
 *
 * ## Responsibilities
 * - Normalize individual engine scores to 0-100 range
 * - Apply configurable weights (heuristic, ML, brand, TLD)
 * - Produce bounded combined score
 *
 * @param config Scoring configuration with weights and thresholds
 * @author QR-SHIELD Security Team
 * @since 1.7.0
 */
class ScoreCalculator(
    private val config: ScoringConfig = ScoringConfig.DEFAULT
) {
    /**
     * Calculate combined risk score from all engines.
     *
     * Uses weighted average with bounds checking.
     *
     * @param heuristicScore Raw score from HeuristicsEngine (0-100)
     * @param mlScore Probability from ML model (0.0-1.0)
     * @param brandScore Score from BrandDetector (0-100)
     * @param tldScore Score from TldScorer (0-100)
     * @return Combined weighted score (0-100)
     */
    fun calculateCombinedScore(
        heuristicScore: Int,
        mlScore: Float,
        brandScore: Int,
        tldScore: Int
    ): Int {
        // Normalize scores to 0-100 range
        val normalizedHeuristic = heuristicScore.coerceIn(0, 100)
        val normalizedMl = (mlScore * 100).toInt().coerceIn(0, 100)
        val normalizedBrand = brandScore.coerceIn(0, 100)
        val normalizedTld = tldScore.coerceIn(0, 100)

        // Weighted combination using injectable config
        val weighted = (
            normalizedHeuristic * config.heuristicWeight +
            normalizedMl * config.mlWeight +
            normalizedBrand * config.brandWeight +
            normalizedTld * config.tldWeight
        )

        return weighted.toInt().coerceIn(0, 100)
    }

    /**
     * Calculate confidence score based on signal agreement.
     *
     * Higher confidence when:
     * - Heuristics and ML agree on risk level
     * - Brand detection has a definitive match
     * - More heuristic signals triggered (more evidence)
     *
     * @param heuristicResult Result from HeuristicsEngine
     * @param mlScore Probability from ML model
     * @param brandResult Result from BrandDetector
     * @return Confidence score (0.3-0.99)
     */
    fun calculateConfidence(
        heuristicResult: HeuristicsEngine.Result,
        mlScore: Float,
        brandResult: BrandDetector.DetectionResult
    ): Float {
        // Start with injectable base confidence
        var confidence = config.baseConfidence

        // Agreement between heuristics and ML increases confidence
        val heuristicNormalized = heuristicResult.score / 100f
        val agreement = 1f - kotlin.math.abs(heuristicNormalized - mlScore)
        confidence += agreement * 0.2f

        // Brand detection adds certainty
        if (brandResult.match != null) {
            confidence += 0.15f
        }

        // More signals = more confidence (capped at 5)
        val signalCount = heuristicResult.flags.size
        confidence += (signalCount.coerceAtMost(5) * 0.02f)

        return confidence.coerceIn(0.3f, 0.99f)
    }
}

/**
 * Verdict Determiner for PhishingEngine
 *
 * Extracted from PhishingEngine to improve code organization and testability.
 * Handles all verdict determination logic including escalation rules.
 *
 * ## Escalation Hierarchy
 * 1. Homograph attack → MALICIOUS (always)
 * 2. Brand impersonation → MALICIOUS or SUSPICIOUS
 * 3. Multiple critical indicators → MALICIOUS
 * 4. @ symbol injection → SUSPICIOUS
 * 5. High-risk TLD → escalates based on score
 * 6. Strong heuristic signal → escalates based on score
 * 7. Standard threshold-based verdict
 *
 * @param config Scoring configuration with thresholds
 * @author QR-SHIELD Security Team
 * @since 1.7.0
 */
class VerdictDeterminer(
    private val config: ScoringConfig = ScoringConfig.DEFAULT
) {
    /**
     * Determine verdict based on VOTING SYSTEM + critical factors.
     *
     * NEW VOTING LOGIC:
     * - Each component (Heuristic, ML, Brand, TLD) casts a vote
     * - Majority vote determines the verdict
     * - 3+ SAFE votes → SAFE
     * - 2+ MALICIOUS votes → MALICIOUS
     * - Otherwise → SUSPICIOUS
     *
     * Critical escalations can override the vote for safety.
     *
     * @param score Combined risk score (0-100) - used for tie-breaking
     * @param heuristicResult Result from HeuristicsEngine
     * @param brandResult Result from BrandDetector
     * @param tldResult Result from TldScorer
     * @param mlScore ML probability score (0.0-1.0)
     * @return Final verdict (SAFE, SUSPICIOUS, MALICIOUS, or UNKNOWN)
     */
    fun determineVerdict(
        score: Int,
        heuristicResult: HeuristicsEngine.Result,
        brandResult: BrandDetector.DetectionResult,
        tldResult: TldScorer.TldResult,
        mlScore: Float = 0.5f
    ): Verdict {
        // Critical escalation: confirmed homograph attack
        if (brandResult.details?.matchType == BrandDetector.MatchType.HOMOGRAPH) {
            return Verdict.MALICIOUS
        }

        // Critical escalation: brand impersonation detected
        // Any brand match should be at least SUSPICIOUS
        if (brandResult.match != null) {
            return if (score > config.suspiciousThreshold || brandResult.score >= 50) {
                Verdict.MALICIOUS
            } else {
                Verdict.SUSPICIOUS
            }
        }

        // Critical escalation: multiple high-severity indicators
        val criticalCount = heuristicResult.details.count { (_, weight) ->
            weight >= 20
        }
        if (criticalCount >= 2 && score > config.safeThreshold) {
            return Verdict.MALICIOUS
        }

        // Escalation: @ symbol injection (common phishing technique)
        if (heuristicResult.flags.any { it.contains("@ symbol", ignoreCase = true) }) {
            return Verdict.SUSPICIOUS
        }

        // Escalation: High Risk TLD
        if (tldResult.isHighRisk) {
            return if (score > config.suspiciousThreshold) Verdict.MALICIOUS else Verdict.SUSPICIOUS
        }

        // Escalation: Strong heuristic signal alone
        if (heuristicResult.score > 60) {
            return if (score > config.suspiciousThreshold) Verdict.MALICIOUS else Verdict.SUSPICIOUS
        }

        // === NEW VOTING SYSTEM ===
        // Each component votes based on its individual score
        val heuristicVote = when {
            heuristicResult.score <= 10 -> Verdict.SAFE
            heuristicResult.score <= 25 -> Verdict.SUSPICIOUS
            else -> Verdict.MALICIOUS
        }
        
        val mlVote = when {
            mlScore <= 0.30f -> Verdict.SAFE  // ML probability < 30%
            mlScore <= 0.60f -> Verdict.SUSPICIOUS  // ML probability 30-60%
            else -> Verdict.MALICIOUS  // ML probability > 60%
        }
        
        val brandVote = when {
            brandResult.score <= 5 -> Verdict.SAFE
            brandResult.score <= 15 -> Verdict.SUSPICIOUS
            else -> Verdict.MALICIOUS
        }
        
        val tldVote = when {
            tldResult.score <= 3 -> Verdict.SAFE
            tldResult.score <= 7 -> Verdict.SUSPICIOUS
            else -> Verdict.MALICIOUS
        }
        
        // Count votes
        val votes = listOf(heuristicVote, mlVote, brandVote, tldVote)
        val safeVotes = votes.count { it == Verdict.SAFE }
        val suspiciousVotes = votes.count { it == Verdict.SUSPICIOUS }
        val maliciousVotes = votes.count { it == Verdict.MALICIOUS }
        
        // Majority vote determines verdict
        return when {
            safeVotes >= 3 -> Verdict.SAFE  // 3 or 4 components say SAFE
            maliciousVotes >= 2 -> Verdict.MALICIOUS  // 2+ components say MALICIOUS
            suspiciousVotes >= 2 -> Verdict.SUSPICIOUS  // 2+ components say SUSPICIOUS
            safeVotes >= 2 -> Verdict.SAFE  // Fallback: 2 SAFE is better than mixed
            else -> Verdict.SUSPICIOUS  // Default to cautious
        }
    }
}
