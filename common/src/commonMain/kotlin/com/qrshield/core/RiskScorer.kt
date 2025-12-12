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

/**
 * Risk Scorer for QR-SHIELD
 * 
 * Combines multiple analysis signals into a final risk score.
 */
class RiskScorer {
    
    data class ScoringWeights(
        val heuristic: Double = 0.40,
        val ml: Double = 0.35,
        val brand: Double = 0.15,
        val tld: Double = 0.10
    ) {
        init {
            require(heuristic + ml + brand + tld == 1.0) {
                "Weights must sum to 1.0"
            }
        }
    }
    
    data class ScoreComponents(
        val heuristicScore: Int,
        val mlScore: Float,
        val brandScore: Int,
        val tldScore: Int
    )
    
    data class FinalScore(
        val score: Int,
        val verdict: Verdict,
        val confidence: Float,
        val breakdown: Map<String, Int>
    )
    
    private val weights = ScoringWeights()
    
    /**
     * Calculate final risk score from component scores
     */
    fun calculate(components: ScoreComponents): FinalScore {
        val weightedHeuristic = (components.heuristicScore * weights.heuristic).toInt()
        val weightedMl = ((components.mlScore * 100) * weights.ml).toInt()
        val weightedBrand = (components.brandScore * weights.brand).toInt()
        val weightedTld = (components.tldScore * weights.tld).toInt()
        
        val totalScore = (weightedHeuristic + weightedMl + weightedBrand + weightedTld)
            .coerceIn(0, 100)
        
        val verdict = determineVerdict(totalScore)
        val confidence = calculateConfidence(components, totalScore)
        
        return FinalScore(
            score = totalScore,
            verdict = verdict,
            confidence = confidence,
            breakdown = mapOf(
                "heuristic" to weightedHeuristic,
                "ml" to weightedMl,
                "brand" to weightedBrand,
                "tld" to weightedTld
            )
        )
    }
    
    /**
     * Determine verdict based on score thresholds
     */
    fun determineVerdict(score: Int): Verdict = when {
        score <= SAFE_THRESHOLD -> Verdict.SAFE
        score <= SUSPICIOUS_THRESHOLD -> Verdict.SUSPICIOUS
        else -> Verdict.MALICIOUS
    }
    
    /**
     * Get human-readable explanation for verdict
     */
    fun explainVerdict(verdict: Verdict): String = when (verdict) {
        Verdict.SAFE -> "This URL appears to be safe. No significant risk indicators detected."
        Verdict.SUSPICIOUS -> "This URL has some suspicious characteristics. Proceed with caution."
        Verdict.MALICIOUS -> "This URL shows strong indicators of phishing. Access is not recommended."
        Verdict.UNKNOWN -> "Unable to analyze this URL. Please verify manually."
    }
    
    /**
     * Get color code for verdict (hex)
     */
    fun getVerdictColor(verdict: Verdict): Long = when (verdict) {
        Verdict.SAFE -> 0xFF00D68F       // Green
        Verdict.SUSPICIOUS -> 0xFFFFAA00 // Amber
        Verdict.MALICIOUS -> 0xFFFF3D71  // Red
        Verdict.UNKNOWN -> 0xFF8B949E    // Gray
    }
    
    private fun calculateConfidence(
        components: ScoreComponents,
        @Suppress("UNUSED_PARAMETER") finalScore: Int
    ): Float {
        // Confidence based on agreement between signals
        val signals = listOf(
            components.heuristicScore,
            (components.mlScore * 100).toInt(),
            components.brandScore,
            components.tldScore
        )
        
        val mean = signals.average()
        val variance = signals.map { (it - mean) * (it - mean) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        
        // Lower variance = higher confidence
        val maxStdDev = 50.0
        val normalizedStdDev = (stdDev / maxStdDev).coerceIn(0.0, 1.0)
        
        return (1.0 - normalizedStdDev * 0.5).toFloat()
    }
    
    companion object {
        const val SAFE_THRESHOLD = 15
        const val SUSPICIOUS_THRESHOLD = 50
    }
}
