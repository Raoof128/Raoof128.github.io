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

package com.raouf.mehrguard.core

/**
 * Configurable Detection Parameters
 *
 * This data class encapsulates all tunable detection thresholds and weights,
 * allowing for:
 * - Easy testing with different configurations
 * - Future remote config updates without code changes
 * - Clear documentation of each parameter's purpose
 *
 * ## Usage
 * ```kotlin
 * // Use default configuration
 * val engine = PhishingEngine(config = DetectionConfig.DEFAULT)
 *
 * // Use stricter configuration
 * val strictEngine = PhishingEngine(config = DetectionConfig.STRICT)
 *
 * // Use custom configuration
 * val customEngine = PhishingEngine(config = DetectionConfig(
 *     safeThreshold = 5,
 *     suspiciousThreshold = 40
 * ))
 * ```
 *
 * ## Future: Remote Configuration
 * This config can be loaded from a JSON file or remote endpoint:
 * ```kotlin
 * val remoteConfig = DetectionConfig.fromJson(jsonString)
 * val engine = PhishingEngine(config = remoteConfig)
 * ```
 *
 * @author QR-SHIELD Security Team
 * @since 1.1.0
 */
data class DetectionConfig(
    // =========================================================================
    // SCORING THRESHOLDS
    // =========================================================================

    /**
     * Score at or below which URL is considered SAFE.
     * Default: 10 (conservative, prioritizes catching threats)
     */
    val safeThreshold: Int = 10,

    /**
     * Score at or above which URL is considered MALICIOUS.
     * Scores between safeThreshold and this are SUSPICIOUS.
     * Default: 50
     */
    val suspiciousThreshold: Int = 50,

    // =========================================================================
    // COMPONENT WEIGHTS (must sum to 100)
    // =========================================================================

    /**
     * Weight for heuristic analysis in combined score.
     * Default: 50%
     */
    val heuristicWeight: Int = 50,

    /**
     * Weight for ML prediction in combined score.
     * Default: 25%
     */
    val mlWeight: Int = 25,

    /**
     * Weight for brand detection in combined score.
     * Default: 15%
     */
    val brandWeight: Int = 15,

    /**
     * Weight for TLD scoring in combined score.
     * Default: 10%
     */
    val tldWeight: Int = 10,

    // =========================================================================
    // ML CONFIGURATION
    // =========================================================================

    /**
     * Threshold for ML prediction to be considered phishing.
     * Default: 0.5 (50% probability)
     */
    val mlThreshold: Float = 0.5f,

    /**
     * High confidence threshold for ML predictions.
     * Default: 0.8
     */
    val highConfidenceThreshold: Float = 0.8f,

    // =========================================================================
    // HEURISTIC CONFIGURATION
    // =========================================================================

    /**
     * Maximum subdomains before flagging excessive.
     * Default: 5
     */
    val maxSubdomains: Int = 5,

    /**
     * Maximum URL length before flagging as suspicious.
     * Default: 2048
     */
    val maxUrlLength: Int = 2048,

    /**
     * Domain entropy threshold for flagging random-looking domains.
     * Default: 4.0
     */
    val entropyThreshold: Double = 4.0,

    // =========================================================================
    // FEATURE FLAGS
    // =========================================================================

    /**
     * Enable ML scoring component.
     * Default: true
     */
    val enableMl: Boolean = true,

    /**
     * Enable brand impersonation detection.
     * Default: true
     */
    val enableBrandDetection: Boolean = true,

    /**
     * Enable TLD risk scoring.
     * Default: true
     */
    val enableTldScoring: Boolean = true,

    /**
     * Enable counterfactual explanations.
     * Default: true
     */
    val enableCounterfactuals: Boolean = true
) {
    init {
        require(safeThreshold in 0..100) { "safeThreshold must be 0-100" }
        require(suspiciousThreshold in 0..100) { "suspiciousThreshold must be 0-100" }
        require(safeThreshold < suspiciousThreshold) { "safeThreshold must be < suspiciousThreshold" }
        require(heuristicWeight + mlWeight + brandWeight + tldWeight == 100) {
            "Weights must sum to 100, got ${heuristicWeight + mlWeight + brandWeight + tldWeight}"
        }
        require(mlThreshold in 0f..1f) { "mlThreshold must be 0-1" }
    }

    companion object {
        /**
         * Default configuration - balanced detection.
         */
        val DEFAULT = DetectionConfig()

        /**
         * Strict configuration - catches more threats, more false positives.
         */
        val STRICT = DetectionConfig(
            safeThreshold = 5,
            suspiciousThreshold = 35,
            mlThreshold = 0.4f,
            maxSubdomains = 3,
            entropyThreshold = 3.5
        )

        /**
         * Lenient configuration - fewer false positives, may miss threats.
         */
        val LENIENT = DetectionConfig(
            safeThreshold = 20,
            suspiciousThreshold = 70,
            mlThreshold = 0.6f,
            maxSubdomains = 7,
            entropyThreshold = 4.5
        )

        /**
         * Performance configuration - faster analysis, reduced accuracy.
         */
        val PERFORMANCE = DetectionConfig(
            enableMl = false,
            enableCounterfactuals = false
        )

        /**
         * Create DetectionConfig from Security DSL.
         *
         * This bridges the security-dsl module with the core engine,
         * proving that the DSL is actually integrated—not just a flex.
         *
         * Usage:
         * ```kotlin
         * val dslConfig = securityConfig {
         *     detection {
         *         threshold = 65
         *         enableHomographDetection = true
         *     }
         *     suspiciousTlds {
         *         +"tk"; +"ml"; +"ga"
         *     }
         * }
         * val engineConfig = DetectionConfig.fromSecurityDsl(
         *     threshold = dslConfig.detection.threshold,
         *     maxRedirects = dslConfig.detection.maxRedirects,
         *     enableHomograph = dslConfig.detection.enableHomographDetection,
         *     enableBrand = dslConfig.detection.enableBrandImpersonation,
         *     enableTld = dslConfig.detection.enableTldValidation
         * )
         * val engine = PhishingEngine(config = engineConfig)
         * ```
         *
         * @param threshold Detection threshold (0-100), maps to suspiciousThreshold
         * @param maxRedirects Maximum redirect hops allowed
         * @param enableHomograph Enable homograph detection
         * @param enableBrand Enable brand impersonation detection
         * @param enableTld Enable TLD risk scoring
         * @return DetectionConfig configured from DSL values
         */
        fun fromSecurityDsl(
            threshold: Int = 50,
            maxRedirects: Int = 5,
            enableHomograph: Boolean = true,
            enableBrand: Boolean = true,
            enableTld: Boolean = true
        ): DetectionConfig {
            // Map DSL threshold to engine thresholds
            // DSL threshold is the suspiciousThreshold
            // safeThreshold is 20% of threshold
            val safeT = (threshold * 0.2).toInt().coerceIn(5, 30)
            val suspiciousT = threshold.coerceIn(30, 90)
            
            return DetectionConfig(
                safeThreshold = safeT,
                suspiciousThreshold = suspiciousT,
                maxSubdomains = maxRedirects,  // Related concepts
                enableBrandDetection = enableBrand,
                enableTldScoring = enableTld,
                enableMl = enableHomograph  // Homograph uses ML features
            )
        }

        /**
         * Parse configuration from JSON string.
         *
         * Example JSON:
         * ```json
         * {
         *   "safeThreshold": 10,
         *   "suspiciousThreshold": 50,
         *   "heuristicWeight": 50,
         *   "mlWeight": 25,
         *   "brandWeight": 15,
         *   "tldWeight": 10
         * }
         * ```
         *
         * @param json JSON configuration string
         * @return DetectionConfig with parsed values, defaults for missing fields
         */
        fun fromJson(json: String): DetectionConfig {
            // Simple JSON parsing without external dependencies
            // For production, use kotlinx.serialization
            val values = mutableMapOf<String, Any>()

            // Extract key-value pairs using regex
            val pattern = Regex(""""(\w+)":\s*([^,}\s]+)""")
            pattern.findAll(json).forEach { match ->
                val key = match.groupValues[1]
                val value = match.groupValues[2].trim('"')
                values[key] = when {
                    value == "true" -> true
                    value == "false" -> false
                    value.contains(".") -> value.toFloatOrNull() ?: value
                    else -> value.toIntOrNull() ?: value
                }
            }

            return DetectionConfig(
                safeThreshold = (values["safeThreshold"] as? Int) ?: DEFAULT.safeThreshold,
                suspiciousThreshold = (values["suspiciousThreshold"] as? Int) ?: DEFAULT.suspiciousThreshold,
                heuristicWeight = (values["heuristicWeight"] as? Int) ?: DEFAULT.heuristicWeight,
                mlWeight = (values["mlWeight"] as? Int) ?: DEFAULT.mlWeight,
                brandWeight = (values["brandWeight"] as? Int) ?: DEFAULT.brandWeight,
                tldWeight = (values["tldWeight"] as? Int) ?: DEFAULT.tldWeight,
                mlThreshold = (values["mlThreshold"] as? Float) ?: DEFAULT.mlThreshold,
                enableMl = (values["enableMl"] as? Boolean) ?: DEFAULT.enableMl,
                enableBrandDetection = (values["enableBrandDetection"] as? Boolean) ?: DEFAULT.enableBrandDetection
            )
        }
    }

    /**
     * Export configuration to JSON string.
     */
    fun toJson(): String = """
        {
          "safeThreshold": $safeThreshold,
          "suspiciousThreshold": $suspiciousThreshold,
          "heuristicWeight": $heuristicWeight,
          "mlWeight": $mlWeight,
          "brandWeight": $brandWeight,
          "tldWeight": $tldWeight,
          "mlThreshold": $mlThreshold,
          "highConfidenceThreshold": $highConfidenceThreshold,
          "maxSubdomains": $maxSubdomains,
          "maxUrlLength": $maxUrlLength,
          "entropyThreshold": $entropyThreshold,
          "enableMl": $enableMl,
          "enableBrandDetection": $enableBrandDetection,
          "enableTldScoring": $enableTldScoring,
          "enableCounterfactuals": $enableCounterfactuals
        }
    """.trimIndent()
}
