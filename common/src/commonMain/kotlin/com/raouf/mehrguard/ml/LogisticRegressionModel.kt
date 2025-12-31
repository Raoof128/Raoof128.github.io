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

package com.raouf.mehrguard.ml

import kotlin.math.exp

/**
 * Logistic Regression Model for QR-SHIELD
 *
 * Lightweight ML model for phishing URL classification.
 * Runs entirely on-device with no external dependencies.
 *
 * SECURITY NOTES:
 * - All inputs are bounded to prevent numerical overflow
 * - Safe sigmoid prevents exp() overflow
 * - Model is immutable after construction
 * - Thread-safe for concurrent predictions
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class LogisticRegressionModel private constructor(
    private val weights: FloatArray,
    private val bias: Float
) {
    companion object {
        /** Number of features expected by the model */
        const val FEATURE_COUNT = 15

        /** Maximum safe exponent value to prevent overflow */
        private const val MAX_EXP = 88.0f

        /** Minimum prediction value (avoid exact 0 for log-loss) */
        private const val MIN_PREDICTION = 1e-7f

        /** Maximum prediction value (avoid exact 1 for log-loss) */
        private const val MAX_PREDICTION = 1f - 1e-7f

        /** Neutral prediction for invalid inputs */
        private const val NEUTRAL_PREDICTION = 0.5f

        /** Feature clamping range to prevent overflow */
        private const val FEATURE_MIN = -10f
        private const val FEATURE_MAX = 10f

        /**
         * Feature order documentation:
         * 0: urlLength (normalized 0-1)
         * 1: hostLength (normalized 0-1)
         * 2: pathLength (normalized 0-1)
         * 3: subdomainCount (normalized 0-1)
         * 4: hasHttps (0/1)
         * 5: hasIpHost (0/1)
         * 6: domainEntropy (normalized 0-1)
         * 7: pathEntropy (normalized 0-1)
         * 8: queryParamCount (normalized 0-1)
         * 9: hasAtSymbol (0/1)
         * 10: numDots (normalized 0-1)
         * 11: numDashes (normalized 0-1)
         * 12: hasPortNumber (0/1)
         * 13: shortenerDomain (0/1)
         * 14: suspiciousTld (0/1)
         */

        /**
         * Create model with validated weights.
         *
         * @param weights Feature weights array (must have FEATURE_COUNT elements)
         * @param bias Model bias term
         * @return LogisticRegressionModel instance
         * @throws IllegalArgumentException if weights size is incorrect
         */
        fun create(weights: FloatArray, bias: Float): LogisticRegressionModel {
            require(weights.size == FEATURE_COUNT) {
                "Expected $FEATURE_COUNT weights, got ${weights.size}"
            }

            // Validate weights are finite
            require(weights.all { it.isFinite() }) {
                "Weights must be finite numbers"
            }

            require(bias.isFinite()) {
                "Bias must be a finite number"
            }

            // Create defensive copy of weights
            return LogisticRegressionModel(weights.copyOf(), bias)
        }

        /**
         * Default model with pre-trained weights.
         *
         * These weights are calibrated for phishing URL detection.
         * In production, consider loading from an encrypted model file.
         */
        fun default(): LogisticRegressionModel {
            val weights = floatArrayOf(
                WEIGHT_URL_LENGTH,      // Longer URLs slightly risky
                WEIGHT_HOST_LENGTH,     // Host length
                WEIGHT_PATH_LENGTH,     // Path length
                WEIGHT_SUBDOMAIN_COUNT, // More subdomains = risky
                WEIGHT_HAS_HTTPS,       // HTTPS is protective (negative weight)
                WEIGHT_HAS_IP_HOST,     // IP hosts very risky
                WEIGHT_DOMAIN_ENTROPY,  // Random domains risky
                WEIGHT_PATH_ENTROPY,    // Path entropy
                WEIGHT_QUERY_PARAM_COUNT, // Query params
                WEIGHT_HAS_AT_SYMBOL,   // @ in URL is risky
                WEIGHT_NUM_DOTS,        // Dot count
                WEIGHT_NUM_DASHES,      // Dash count
                WEIGHT_HAS_PORT_NUMBER, // Non-standard ports risky
                WEIGHT_SHORTENER_DOMAIN, // URL shorteners
                WEIGHT_SUSPICIOUS_TLD   // Suspicious TLDs
            )

            return LogisticRegressionModel(weights, DEFAULT_BIAS)
        }

        // === DEFAULT MODEL WEIGHTS ===
        // Positive = increases phishing probability
        // Negative = decreases phishing probability

        /** URL length weight - longer URLs slightly risky */
        private const val WEIGHT_URL_LENGTH = 0.25f

        /** Host length weight */
        private const val WEIGHT_HOST_LENGTH = 0.15f

        /** Path length weight */
        private const val WEIGHT_PATH_LENGTH = 0.10f

        /** Subdomain count weight - more subdomains = risky */
        private const val WEIGHT_SUBDOMAIN_COUNT = 0.30f

        /** HTTPS presence weight - protective (negative) */
        private const val WEIGHT_HAS_HTTPS = -0.50f

        /** IP host weight - very risky */
        private const val WEIGHT_HAS_IP_HOST = 0.80f

        /** Domain entropy weight - random domains risky */
        private const val WEIGHT_DOMAIN_ENTROPY = 0.40f

        /** Path entropy weight */
        private const val WEIGHT_PATH_ENTROPY = 0.20f

        /** Query parameter count weight */
        private const val WEIGHT_QUERY_PARAM_COUNT = 0.15f

        /** @ symbol in URL weight - risky injection indicator */
        private const val WEIGHT_HAS_AT_SYMBOL = 0.60f

        /** Dot count weight */
        private const val WEIGHT_NUM_DOTS = 0.10f

        /** Dash count weight */
        private const val WEIGHT_NUM_DASHES = 0.05f

        /** Port number weight - non-standard ports risky */
        private const val WEIGHT_HAS_PORT_NUMBER = 0.45f

        /** URL shortener domain weight */
        private const val WEIGHT_SHORTENER_DOMAIN = 0.35f

        /** Suspicious TLD weight */
        private const val WEIGHT_SUSPICIOUS_TLD = 0.55f

        /** Default bias - slight bias toward "not phishing" */
        private const val DEFAULT_BIAS = -0.30f

        /** Maximum JSON input length for security */
        private const val MAX_JSON_LENGTH = 4096

        /**
         * Load model from JSON string.
         *
         * Expected format:
         * {
         *   "weights": {
         *     "values": [0.25, 0.15, ...],
         *     "bias": -0.30
         *   }
         * }
         *
         * Returns default model if parsing fails.
         * Uses manual parsing for multiplatform compatibility.
         */
        fun fromJson(json: String): LogisticRegressionModel {
            // SECURITY: Validate JSON input length
            if (json.isEmpty() || json.length > MAX_JSON_LENGTH) {
                return default()
            }

            return try {
                parseModelJson(json)
            } catch (e: Exception) {
                // Fail safely to default model
                default()
            }
        }

        /**
         * Parse model JSON manually for multiplatform compatibility.
         *
         * Supports two formats:
         * 1. {"weights": {"values": [...], "bias": N}}
         * 2. {"weights": [...], "bias": N}
         */
        private fun parseModelJson(json: String): LogisticRegressionModel {
            // Extract bias value
            val bias = extractFloat(json, "bias") ?: return default()

            // Extract weights array
            val weightsArray = extractFloatArray(json, "values")
                ?: extractFloatArray(json, "weights")
                ?: return default()

            // Validate weights count
            if (weightsArray.size != FEATURE_COUNT) {
                return default()
            }

            // Validate all values are finite
            if (!weightsArray.all { it.isFinite() } || !bias.isFinite()) {
                return default()
            }

            return create(weightsArray, bias)
        }

        /**
         * Extract a float value from JSON by key.
         */
        private fun extractFloat(json: String, key: String): Float? {
            val pattern = """"$key"\s*:\s*(-?\d+\.?\d*)"""
            val regex = Regex(pattern)
            val match = regex.find(json) ?: return null
            return match.groupValues.getOrNull(1)?.toFloatOrNull()
        }

        /**
         * Extract a float array from JSON by key.
         */
        private fun extractFloatArray(json: String, key: String): FloatArray? {
            // Find the key and the array start
            val keyPattern = """"$key"\s*:\s*\["""
            val keyMatch = Regex(keyPattern).find(json) ?: return null

            val arrayStart = keyMatch.range.last + 1
            val arrayEnd = json.indexOf(']', arrayStart)
            if (arrayEnd < 0) return null

            val arrayContent = json.substring(arrayStart, arrayEnd)

            // Parse comma-separated values
            val values = arrayContent.split(',')
                .mapNotNull { it.trim().toFloatOrNull() }

            return if (values.isNotEmpty()) values.toFloatArray() else null
        }

        /**
         * Load model from the bundled phishing_model_weights.json file.
         *
         * @param jsonContent The JSON content from the file
         * @return LogisticRegressionModel loaded from JSON or default
         */
        fun fromModelFile(jsonContent: String): LogisticRegressionModel {
            return fromJson(jsonContent)
        }
    }

    /**
     * Predict phishing probability.
     *
     * @param features Normalized feature vector (must have FEATURE_COUNT elements)
     * @return Probability [0, 1] where higher values indicate higher phishing risk
     * @throws IllegalArgumentException if feature vector size is incorrect
     */
    fun predict(features: FloatArray): Float {
        // Validate input size
        require(features.size == FEATURE_COUNT) {
            "Feature size mismatch: expected $FEATURE_COUNT, got ${features.size}"
        }

        // Early return for invalid input (fail-safe)
        if (features.any { !it.isFinite() }) return NEUTRAL_PREDICTION

        // Calculate weighted sum using idiomatic Kotlin:
        // zip pairs each weight with feature, then fold accumulates the dot product
        val z = weights
            .zip(features.asIterable())
            .fold(bias) { acc, (weight, feature) ->
                acc + weight * feature.coerceIn(FEATURE_MIN, FEATURE_MAX)
            }

        // Apply safe sigmoid activation
        return safeSigmoid(z)
    }

    /**
     * Batch predict for multiple feature vectors.
     *
     * Uses functional transform for clean batch processing.
     *
     * @param featureVectors List of feature vectors
     * @return List of probabilities in same order
     */
    fun predictBatch(featureVectors: List<FloatArray>): List<Float> =
        featureVectors.map(::predict)

    /**
     * Compute dot product using functional approach.
     *
     * Demonstrates idiomatic Kotlin: operator overloading could be added
     * for a custom Vector class, but FloatArray + zip works cleanly.
     */
    private infix fun FloatArray.dot(other: FloatArray): Float =
        this.zip(other.asIterable()) { a, b -> a * b }.sum()

    /**
     * Predict with confidence threshold.
     *
     * @param features Normalized feature vector
     * @param threshold Classification threshold (default: 0.5)
     * @return Prediction with class, probability, and confidence
     */
    fun predictWithThreshold(
        features: FloatArray,
        threshold: Float = 0.5f
    ): Prediction {
        val probability = predict(features)
        val isPhishing = probability >= threshold

        // Confidence is distance from threshold, scaled to [0, 1]
        val confidence = if (isPhishing) {
            ((probability - threshold) / (1 - threshold)).coerceIn(0f, 1f)
        } else {
            ((threshold - probability) / threshold).coerceIn(0f, 1f)
        }

        return Prediction(
            isPhishing = isPhishing,
            probability = probability,
            confidence = confidence
        )
    }

    /**
     * Safe sigmoid function that prevents overflow.
     *
     * For large positive x: returns ~1
     * For large negative x: returns ~0
     * These limits prevent exp() overflow.
     */
    private fun safeSigmoid(x: Float): Float {
        return when {
            x >= MAX_EXP -> MAX_PREDICTION
            x <= -MAX_EXP -> MIN_PREDICTION
            else -> {
                val expNegX = exp(-x)
                (1.0f / (1.0f + expNegX)).coerceIn(MIN_PREDICTION, MAX_PREDICTION)
            }
        }
    }

    /**
     * Prediction result.
     */
    data class Prediction(
        val isPhishing: Boolean,
        val probability: Float,
        val confidence: Float
    ) {
        /** Risk level string */
        val riskLevel: String
            get() = when {
                probability < 0.3f -> "Low"
                probability < 0.7f -> "Medium"
                else -> "High"
            }
    }
}
