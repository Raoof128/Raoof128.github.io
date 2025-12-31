/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.raouf.mehrguard.ml

/**
 * Ensemble ML Scorer combining character-level and feature-based models.
 *
 * ## Ensemble Strategy
 * - Character Embedding: Captures lexical patterns (typosquatting, etc.)
 * - Feature-based NN: Captures structural patterns (IP, TLD, entropy)
 * - Final score: Weighted average with anomaly boost
 *
 * ## Integration
 * This score is a "bonus signal" that augments heuristics.
 * It should NOT replace the rule-based detection.
 *
 * ```kotlin
 * val mlScore = EnsemblePhishingScorer.default.score(url)
 * val finalScore = heuristicScore * 0.7 + mlScore * 0.3
 * ```
 *
 * @author QR-SHIELD Security Team
 * @since 1.19.0
 */
class EnsemblePhishingScorer private constructor(
    private val charScorer: CharacterEmbeddingScorer,
    private val featureClassifier: TinyPhishingClassifier
) {
    /**
     * Score a URL using ensemble of models.
     *
     * @param url The URL to score
     * @return Score in [0.0, 1.0] where 1.0 = phishing
     */
    fun score(url: String): Float {
        val charScore = charScorer.score(url)
        val featureScore = featureClassifier.predict(url)

        // Weighted ensemble with agreement boost
        val baseScore = charScore * CHAR_WEIGHT + featureScore * FEATURE_WEIGHT
        
        // If both models agree it's bad, boost the score
        val agreement = if (charScore > 0.5f && featureScore > 0.5f) {
            AGREEMENT_BOOST
        } else if (charScore < 0.3f && featureScore < 0.3f) {
            -AGREEMENT_BOOST  // Both say safe
        } else {
            0f
        }

        return (baseScore + agreement).coerceIn(0f, 1f)
    }

    /**
     * Score with detailed breakdown from both models.
     */
    fun scoreWithDetails(url: String): EnsembleResult {
        val charResult = charScorer.scoreWithDetails(url)
        val featureResult = featureClassifier.predictWithDetails(url)
        val ensembleScore = score(url)

        return EnsembleResult(
            ensembleScore = ensembleScore,
            charScore = charResult.score,
            featureScore = featureResult.score,
            isPhishing = ensembleScore >= PHISHING_THRESHOLD,
            confidence = calculateConfidence(charResult.score, featureResult.score),
            charRiskLevel = charResult.riskLevel,
            topFeatures = featureResult.topFeatures,
            riskCharacters = charResult.riskCharacters,
            modelVersion = MODEL_VERSION
        )
    }

    /**
     * Quick check if URL is likely phishing.
     */
    fun isLikelyPhishing(url: String): Boolean {
        return score(url) >= PHISHING_THRESHOLD
    }

    private fun calculateConfidence(charScore: Float, featureScore: Float): Float {
        // Higher confidence when models agree
        val agreement = 1f - kotlin.math.abs(charScore - featureScore)
        val extremity = kotlin.math.abs((charScore + featureScore) / 2f - 0.5f) * 2f
        return (agreement * 0.4f + extremity * 0.6f).coerceIn(0f, 1f)
    }

    data class EnsembleResult(
        val ensembleScore: Float,
        val charScore: Float,
        val featureScore: Float,
        val isPhishing: Boolean,
        val confidence: Float,
        val charRiskLevel: CharacterEmbeddingScorer.RiskLevel,
        val topFeatures: List<TinyPhishingClassifier.FeatureContribution>,
        val riskCharacters: Set<Char>,
        val modelVersion: String
    )

    companion object {
        const val MODEL_VERSION = "1.0.0"
        const val PHISHING_THRESHOLD = 0.5f

        // Ensemble weights
        const val CHAR_WEIGHT = 0.4f
        const val FEATURE_WEIGHT = 0.6f
        const val AGREEMENT_BOOST = 0.1f

        /**
         * Create ensemble scorer.
         */
        fun create(): EnsemblePhishingScorer {
            return EnsemblePhishingScorer(
                charScorer = CharacterEmbeddingScorer.create(),
                featureClassifier = TinyPhishingClassifier.create()
            )
        }

        /**
         * Singleton instance.
         */
        val default: EnsemblePhishingScorer by lazy { create() }
    }
}
