/*
 * Copyright 2024 QR-SHIELD Contributors
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
                0.25f,   // urlLength - longer URLs slightly risky
                0.15f,   // hostLength
                0.10f,   // pathLength
                0.30f,   // subdomainCount - more subdomains = risky
                -0.50f,  // hasHttps - HTTPS is protective (negative weight)
                0.80f,   // hasIpHost - IP hosts very risky
                0.40f,   // domainEntropy - random domains risky
                0.20f,   // pathEntropy
                0.15f,   // queryParamCount
                0.60f,   // hasAtSymbol - @ in URL is risky
                0.10f,   // numDots
                0.05f,   // numDashes
                0.45f,   // hasPortNumber - non-standard ports risky
                0.35f,   // shortenerDomain
                0.55f    // suspiciousTld
            )
            
            val bias = -0.30f  // Slight bias toward "not phishing"
            
            return LogisticRegressionModel(weights, bias)
        }
        
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
            if (json.isEmpty() || json.length > 4096) {
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
        
        // Validate all features are finite and normalized
        for (i in features.indices) {
            val f = features[i]
            if (!f.isFinite()) {
                // Replace invalid values with 0 (fail safe)
                return 0.5f  // Return neutral prediction for invalid input
            }
        }
        
        // Calculate weighted sum
        var z = bias
        for (i in features.indices) {
            // Clamp features to reasonable range to prevent overflow
            val clampedFeature = features[i].coerceIn(-10f, 10f)
            z += weights[i] * clampedFeature
        }
        
        // Apply safe sigmoid
        return safeSigmoid(z)
    }
    
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

/**
 * Feature Extractor for ML Model
 * 
 * Extracts normalized features from URLs for ML inference.
 * All features are normalized to [0, 1] range.
 * 
 * SECURITY NOTES:
 * - Input length is bounded to prevent DoS
 * - All calculations use defensive bounds checking
 * - No exceptions thrown for malformed input
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class FeatureExtractor {
    
    companion object {
        /** Maximum URL length to process */
        private const val MAX_URL_LENGTH = 2048
        
        /** Maximum host length to process */
        private const val MAX_HOST_LENGTH = 255
        
        /** Known URL shortener domains */
        private val SHORTENERS = setOf(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly",
            "is.gd", "buff.ly", "adf.ly", "j.mp", "tr.im",
            "short.link", "cutt.ly", "rb.gy", "shorturl.at"
        )
        
        /** Suspicious TLDs commonly used in phishing */
        private val SUSPICIOUS_TLDS = setOf(
            "tk", "ml", "ga", "cf", "gq",  // Free domains
            "xyz", "icu", "top", "club", "online", "site",  // Abused TLDs
            "buzz", "surf", "monster", "quest", "sbs"
        )
    }
    
    /**
     * Extract features from URL string.
     * 
     * @param url The URL to analyze
     * @return FloatArray of normalized features
     */
    fun extract(url: String): FloatArray {
        val features = FloatArray(LogisticRegressionModel.FEATURE_COUNT)
        
        // SECURITY: Bound input length
        if (url.isEmpty() || url.length > MAX_URL_LENGTH) {
            return features  // Return zeros for invalid input
        }
        
        // Safe URL parsing
        val (host, path, pathAndQuery) = parseUrl(url)
        
        // Feature 0: URL length (normalized to 0-1, max 500 chars)
        features[0] = (url.length / 500f).coerceIn(0f, 1f)
        
        // Feature 1: Host length (normalized, max 100 chars)
        features[1] = (host.length / 100f).coerceIn(0f, 1f)
        
        // Feature 2: Path length (normalized, max 200 chars)
        features[2] = (path.length / 200f).coerceIn(0f, 1f)
        
        // Feature 3: Subdomain count (normalized, max 5)
        val subdomainCount = countSubdomains(host)
        features[3] = (subdomainCount / 5f).coerceIn(0f, 1f)
        
        // Feature 4: Has HTTPS (binary)
        features[4] = if (url.startsWith("https://", ignoreCase = true)) 1f else 0f
        
        // Feature 5: Has IP host (binary)
        features[5] = if (isIpAddress(host)) 1f else 0f
        
        // Feature 6: Domain entropy (normalized, max 5.0)
        features[6] = (calculateEntropy(host) / 5.0f).coerceIn(0f, 1f)
        
        // Feature 7: Path entropy (normalized, max 5.0)
        features[7] = (calculateEntropy(path) / 5.0f).coerceIn(0f, 1f)
        
        // Feature 8: Query param count (normalized, max 10)
        val queryParamCount = countQueryParams(pathAndQuery)
        features[8] = (queryParamCount / 10f).coerceIn(0f, 1f)
        
        // Feature 9: Has @ symbol (binary)
        features[9] = if ('@' in url) 1f else 0f
        
        // Feature 10: Number of dots (normalized, max 10)
        features[10] = (url.count { it == '.' } / 10f).coerceIn(0f, 1f)
        
        // Feature 11: Number of dashes (normalized, max 10)
        features[11] = (url.count { it == '-' } / 10f).coerceIn(0f, 1f)
        
        // Feature 12: Has port number (binary)
        features[12] = if (hasPortNumber(host)) 1f else 0f
        
        // Feature 13: Shortener domain (binary)
        features[13] = if (isShortenerDomain(host)) 1f else 0f
        
        // Feature 14: Suspicious TLD (binary)
        features[14] = if (hasSuspiciousTld(host)) 1f else 0f
        
        return features
    }
    
    /**
     * Safely parse URL into components.
     */
    private fun parseUrl(url: String): Triple<String, String, String> {
        val normalized = url
            .removePrefix("https://")
            .removePrefix("http://")
            .take(MAX_URL_LENGTH)
        
        val hostEnd = normalized.indexOfFirst { it == '/' || it == '?' || it == '#' }
        val host = if (hostEnd > 0) {
            normalized.substring(0, hostEnd.coerceAtMost(MAX_HOST_LENGTH))
        } else {
            normalized.take(MAX_HOST_LENGTH)
        }
        
        val pathAndQuery = if (hostEnd > 0) {
            normalized.substring(hostEnd).take(1024)
        } else ""
        
        val path = pathAndQuery.substringBefore("?").substringBefore("#")
        
        return Triple(host.lowercase(), path, pathAndQuery)
    }
    
    /**
     * Count subdomains in host.
     */
    private fun countSubdomains(host: String): Int {
        val dotCount = host.count { it == '.' }
        return (dotCount - 1).coerceAtLeast(0)
    }
    
    /**
     * Check if host is an IP address.
     */
    private fun isIpAddress(host: String): Boolean {
        val clean = host.substringBefore(":")
        if (clean.length > 15) return false  // IPv4 max: 255.255.255.255
        
        val parts = clean.split(".")
        if (parts.size != 4) return false
        
        return parts.all { part ->
            part.length in 1..3 && part.all { it.isDigit() } && 
            part.toIntOrNull()?.let { it in 0..255 } == true
        }
    }
    
    /**
     * Calculate Shannon entropy of string.
     */
    private fun calculateEntropy(text: String): Float {
        if (text.isEmpty()) return 0f
        
        val bounded = text.take(256)
        val frequencies = bounded.groupingBy { it }.eachCount()
        val length = bounded.length.toFloat()
        
        var entropy = 0f
        for (count in frequencies.values) {
            val p = count / length
            if (p > 0) {
                entropy -= p * kotlin.math.log2(p)
            }
        }
        
        return entropy
    }
    
    /**
     * Count query parameters.
     */
    private fun countQueryParams(pathAndQuery: String): Int {
        if ('?' !in pathAndQuery) return 0
        return pathAndQuery.count { it == '&' } + 1
    }
    
    /**
     * Check if host has a port number.
     */
    private fun hasPortNumber(host: String): Boolean {
        val colonIndex = host.lastIndexOf(':')
        if (colonIndex < 0) return false
        
        val portPart = host.substring(colonIndex + 1)
        return portPart.isNotEmpty() && portPart.all { it.isDigit() }
    }
    
    /**
     * Check if host is a known shortener.
     */
    private fun isShortenerDomain(host: String): Boolean {
        val hostLower = host.lowercase()
        return SHORTENERS.any { shortener ->
            hostLower == shortener || hostLower.endsWith(".$shortener")
        }
    }
    
    /**
     * Check if host has suspicious TLD.
     */
    private fun hasSuspiciousTld(host: String): Boolean {
        val tld = host.substringAfterLast(".")
        return tld.lowercase() in SUSPICIOUS_TLDS
    }
}
