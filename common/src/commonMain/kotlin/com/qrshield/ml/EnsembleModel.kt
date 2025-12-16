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

package com.qrshield.ml

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Ensemble ML Model for QR-SHIELD
 *
 * Advanced ensemble model combining multiple classifiers for robust phishing detection.
 * Demonstrates ML sophistication beyond basic logistic regression.
 *
 * ## Architecture
 *
 * ```
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    ENSEMBLE PREDICTION                          │
 * ├─────────────────────────────────────────────────────────────────┤
 * │                                                                 │
 * │   ┌───────────────┐   ┌───────────────┐   ┌───────────────┐    │
 * │   │   Logistic    │   │   Gradient    │   │   Decision    │    │
 * │   │  Regression   │   │   Boosting    │   │   Stump       │    │
 * │   │   (Linear)    │   │  (Non-linear) │   │  (Rule-based) │    │
 * │   └───────┬───────┘   └───────┬───────┘   └───────┬───────┘    │
 * │           │                   │                   │             │
 * │           v                   v                   v             │
 * │   ┌─────────────────────────────────────────────────────────┐   │
 * │   │              Weighted Average Combiner                  │   │
 * │   │          (Calibrated confidence weighting)              │   │
 * │   └─────────────────────────────────────────────────────────┘   │
 * │                              │                                  │
 * │                              v                                  │
 * │                      Final Prediction                           │
 * │                                                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * ```
 *
 * ## Why Ensemble?
 *
 * 1. **Robustness**: Different models catch different patterns
 * 2. **Reduced Variance**: Averaging reduces individual model errors
 * 3. **Explainability**: Component models provide different perspectives
 * 4. **Graceful Degradation**: If one model fails, others compensate
 *
 * ## Model Components
 *
 * | Model | Strength | Weakness |
 * |-------|----------|----------|
 * | Logistic Regression | Fast, interpretable | Can't capture non-linear patterns |
 * | Gradient Boosting | Captures complex patterns | Slower, risk of overfitting |
 * | Decision Stump | Explicit rules | Limited expressiveness |
 *
 * ## SECURITY NOTES
 * - All inputs are validated and bounded
 * - Thread-safe: all components are immutable
 * - Deterministic: same input produces identical output
 * - No network calls: runs entirely on-device
 *
 * @author QR-SHIELD Security Team
 * @since 1.3.0
 * @see LogisticRegressionModel
 */
class EnsembleModel private constructor(
    private val logisticModel: LogisticRegressionModel,
    private val boostingStumps: List<GradientBoostingStump>,
    private val decisionStumps: List<DecisionStump>,
    private val weights: EnsembleWeights
) {
    /**
     * Ensemble weights for combining predictions.
     */
    data class EnsembleWeights(
        val logistic: Float = 0.40f,
        val boosting: Float = 0.35f,
        val stump: Float = 0.25f
    ) {
        init {
            // Weights must sum to 1.0
            require((logistic + boosting + stump - 1.0f) in -0.01f..0.01f) {
                "Weights must sum to 1.0"
            }
        }
    }

    companion object {
        /** Number of boosting stumps */
        private const val NUM_BOOSTING_STUMPS = 10

        /** Number of decision stumps */
        private const val NUM_DECISION_STUMPS = 5

        /** Neutral prediction for edge cases */
        private const val NEUTRAL_PREDICTION = 0.5f

        /**
         * Create default ensemble model with pre-trained components.
         */
        fun default(): EnsembleModel {
            return EnsembleModel(
                logisticModel = LogisticRegressionModel.default(),
                boostingStumps = createDefaultBoostingStumps(),
                decisionStumps = createDefaultDecisionStumps(),
                weights = EnsembleWeights()
            )
        }

        /**
         * Create boosting stumps for gradient boosting component.
         *
         * Each stump represents a weak learner that focuses on
         * corrections to previous predictions.
         */
        private fun createDefaultBoostingStumps(): List<GradientBoostingStump> = listOf(
            // Stump 1: IP address detection (feature 5)
            GradientBoostingStump(featureIndex = 5, threshold = 0.5f, leftValue = -0.3f, rightValue = 0.4f, weight = 0.15f),
            // Stump 2: @ symbol detection (feature 9)
            GradientBoostingStump(featureIndex = 9, threshold = 0.5f, leftValue = -0.2f, rightValue = 0.5f, weight = 0.12f),
            // Stump 3: Suspicious TLD (feature 14)
            GradientBoostingStump(featureIndex = 14, threshold = 0.5f, leftValue = -0.15f, rightValue = 0.35f, weight = 0.10f),
            // Stump 4: Domain entropy (feature 6)
            GradientBoostingStump(featureIndex = 6, threshold = 0.6f, leftValue = -0.1f, rightValue = 0.25f, weight = 0.10f),
            // Stump 5: Shortener detection (feature 13)
            GradientBoostingStump(featureIndex = 13, threshold = 0.5f, leftValue = -0.1f, rightValue = 0.2f, weight = 0.08f),
            // Stump 6: HTTPS absence (feature 4)
            GradientBoostingStump(featureIndex = 4, threshold = 0.5f, leftValue = 0.2f, rightValue = -0.15f, weight = 0.12f),
            // Stump 7: Subdomain count (feature 3)
            GradientBoostingStump(featureIndex = 3, threshold = 0.4f, leftValue = -0.1f, rightValue = 0.15f, weight = 0.08f),
            // Stump 8: URL length (feature 0)
            GradientBoostingStump(featureIndex = 0, threshold = 0.7f, leftValue = -0.05f, rightValue = 0.1f, weight = 0.08f),
            // Stump 9: Port number (feature 12)
            GradientBoostingStump(featureIndex = 12, threshold = 0.5f, leftValue = -0.1f, rightValue = 0.25f, weight = 0.09f),
            // Stump 10: Path entropy (feature 7)
            GradientBoostingStump(featureIndex = 7, threshold = 0.5f, leftValue = -0.05f, rightValue = 0.1f, weight = 0.08f)
        )

        /**
         * Create decision stumps for explicit rule-based predictions.
         */
        private fun createDefaultDecisionStumps(): List<DecisionStump> = listOf(
            // Critical: IP host + no HTTPS = very risky
            DecisionStump(
                name = "ip_no_https",
                condition = { features -> features[5] > 0.5f && features[4] < 0.5f },
                confidence = 0.85f
            ),
            // Critical: @ symbol = URL spoofing
            DecisionStump(
                name = "at_symbol_injection",
                condition = { features -> features[9] > 0.5f },
                confidence = 0.80f
            ),
            // High: Shortener + suspicious TLD
            DecisionStump(
                name = "shortener_suspicious_tld",
                condition = { features -> features[13] > 0.5f && features[14] > 0.5f },
                confidence = 0.70f
            ),
            // Medium: High entropy + many subdomains
            DecisionStump(
                name = "entropy_subdomains",
                condition = { features -> features[6] > 0.7f && features[3] > 0.5f },
                confidence = 0.60f
            ),
            // Safe: HTTPS + normal structure
            DecisionStump(
                name = "https_safe_structure",
                condition = { features -> 
                    features[4] > 0.5f && features[5] < 0.5f && 
                    features[9] < 0.5f && features[14] < 0.5f 
                },
                confidence = -0.30f  // Negative = reduces risk
            )
        )

        /**
         * Create custom ensemble with provided components.
         */
        fun create(
            logisticModel: LogisticRegressionModel,
            boostingStumps: List<GradientBoostingStump>,
            decisionStumps: List<DecisionStump>,
            weights: EnsembleWeights = EnsembleWeights()
        ): EnsembleModel {
            return EnsembleModel(logisticModel, boostingStumps, decisionStumps, weights)
        }
    }

    /**
     * Predict phishing probability using ensemble of models.
     *
     * @param features Normalized feature vector
     * @return EnsemblePrediction with combined score and component details
     */
    fun predict(features: FloatArray): EnsemblePrediction {
        // Validate input
        if (features.size != LogisticRegressionModel.FEATURE_COUNT) {
            return EnsemblePrediction(
                probability = NEUTRAL_PREDICTION,
                logisticScore = NEUTRAL_PREDICTION,
                boostingScore = 0f,
                stumpScore = 0f,
                confidence = 0f,
                modelAgreement = 0f
            )
        }

        // 1. Logistic regression prediction
        val logisticScore = logisticModel.predict(features)

        // 2. Gradient boosting prediction (sum of weak learners)
        val boostingScore = calculateBoostingScore(features)

        // 3. Decision stump prediction (rule-based adjustments)
        val stumpScore = calculateStumpScore(features)

        // 4. Combine predictions with weighted average
        val combinedLinear = weights.logistic * logisticScore +
            weights.boosting * safeSigmoid(boostingScore) +
            weights.stump * safeSigmoid(stumpScore + 0.5f)  // Shift stump to [0,1] range

        // 5. Calculate model agreement (low variance = high agreement)
        val modelAgreement = calculateAgreement(logisticScore, safeSigmoid(boostingScore), safeSigmoid(stumpScore + 0.5f))

        // 6. Confidence is based on how extreme the prediction is and model agreement
        val confidence = calculateConfidence(combinedLinear, modelAgreement)

        return EnsemblePrediction(
            probability = combinedLinear.coerceIn(0f, 1f),
            logisticScore = logisticScore,
            boostingScore = boostingScore,
            stumpScore = stumpScore,
            confidence = confidence,
            modelAgreement = modelAgreement
        )
    }

    /**
     * Calculate gradient boosting score as sum of weak learners.
     */
    private fun calculateBoostingScore(features: FloatArray): Float {
        return boostingStumps.sumOf { stump ->
            stump.predict(features).toDouble()
        }.toFloat()
    }

    /**
     * Calculate decision stump score based on triggered rules.
     */
    private fun calculateStumpScore(features: FloatArray): Float {
        return decisionStumps.sumOf { stump ->
            if (stump.condition(features)) stump.confidence.toDouble() else 0.0
        }.toFloat()
    }

    /**
     * Calculate model agreement (inverse of variance).
     */
    private fun calculateAgreement(vararg scores: Float): Float {
        if (scores.isEmpty()) return 0f
        val mean = scores.average().toFloat()
        val variance = scores.map { (it - mean) * (it - mean) }.average().toFloat()
        // Convert variance to agreement score (0 = no agreement, 1 = perfect agreement)
        return (1f - min(1f, sqrt(variance) * 3f)).coerceIn(0f, 1f)
    }

    /**
     * Calculate prediction confidence.
     */
    private fun calculateConfidence(prediction: Float, agreement: Float): Float {
        // Confidence is high when prediction is extreme (close to 0 or 1) and models agree
        val extremity = 2f * kotlin.math.abs(prediction - 0.5f)  // 0 at 0.5, 1 at 0 or 1
        return ((extremity * 0.6f) + (agreement * 0.4f)).coerceIn(0f, 1f)
    }

    /**
     * Safe sigmoid to prevent overflow.
     */
    private fun safeSigmoid(x: Float): Float {
        return when {
            x >= 10f -> 0.9999f
            x <= -10f -> 0.0001f
            else -> 1f / (1f + exp(-x))
        }
    }

    /**
     * Ensemble prediction result with component breakdowns.
     */
    data class EnsemblePrediction(
        /** Combined probability [0, 1] */
        val probability: Float,
        /** Logistic regression component score */
        val logisticScore: Float,
        /** Gradient boosting component score (can be negative) */
        val boostingScore: Float,
        /** Decision stump component score */
        val stumpScore: Float,
        /** Prediction confidence [0, 1] */
        val confidence: Float,
        /** Model agreement [0, 1] - how much the components agree */
        val modelAgreement: Float
    ) {
        /** Is this likely phishing? */
        val isPhishing: Boolean get() = probability >= 0.5f

        /** Risk level description */
        val riskLevel: String
            get() = when {
                probability < 0.3f -> "Low"
                probability < 0.7f -> "Medium"
                else -> "High"
            }

        /** Which model contributed most to this prediction? */
        val dominantModel: String
            get() = when {
                kotlin.math.abs(logisticScore - 0.5f) > 0.3f -> "Logistic Regression"
                kotlin.math.abs(boostingScore) > 0.2f -> "Gradient Boosting"
                kotlin.math.abs(stumpScore) > 0.15f -> "Decision Rules"
                else -> "Ensemble Average"
            }
    }

    /**
     * Gradient boosting stump (weak learner).
     *
     * A stump is a single-split decision tree that outputs
     * one value if feature < threshold, another if feature >= threshold.
     */
    data class GradientBoostingStump(
        /** Feature index to split on */
        val featureIndex: Int,
        /** Split threshold */
        val threshold: Float,
        /** Output if feature < threshold */
        val leftValue: Float,
        /** Output if feature >= threshold */
        val rightValue: Float,
        /** Learning rate for this stump */
        val weight: Float
    ) {
        /**
         * Predict contribution from this stump.
         */
        fun predict(features: FloatArray): Float {
            if (featureIndex < 0 || featureIndex >= features.size) return 0f
            val featureValue = features[featureIndex]
            val output = if (featureValue < threshold) leftValue else rightValue
            return output * weight
        }
    }

    /**
     * Decision stump for explicit rule-based predictions.
     */
    data class DecisionStump(
        /** Rule name for explainability */
        val name: String,
        /** Condition function */
        val condition: (FloatArray) -> Boolean,
        /** Confidence adjustment when rule fires (positive = more risky) */
        val confidence: Float
    )
}
