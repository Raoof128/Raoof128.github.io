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

/**
 * Configuration for PhishingEngine scoring weights and thresholds.
 *
 * ## Why This Exists (Dependency Injection for Testability)
 *
 * Previously, all weights were hardcoded companion object constants. This made testing
 * difficult because you couldn't inject different configurations for:
 * - **Unit tests**: Want to isolate specific components
 * - **A/B testing**: Compare different weight configurations
 * - **Organization policies**: Let enterprises adjust sensitivity
 * - **Research**: Tune weights for different threat landscapes
 *
 * ## Usage
 *
 * ```kotlin
 * // Default configuration (production)
 * val engine = PhishingEngine()
 *
 * // Custom configuration for testing
 * val testConfig = ScoringConfig(
 *     heuristicWeight = 0.60,
 *     mlWeight = 0.40,
 *     brandWeight = 0.0,  // Disable brand detection for this test
 *     tldWeight = 0.0
 * )
 * val testEngine = PhishingEngine(config = testConfig)
 *
 * // High-sensitivity configuration for enterprise
 * val strictConfig = ScoringConfig(
 *     safeThreshold = 5,       // Lower = more strict
 *     suspiciousThreshold = 30
 * )
 * val strictEngine = PhishingEngine(config = strictConfig)
 * ```
 *
 * ## Thread Safety
 *
 * This is an immutable data class. Once created, it cannot be modified.
 * Multiple engines can safely share the same config instance.
 *
 * @property heuristicWeight Weight for heuristic analysis (0.0-1.0)
 * @property mlWeight Weight for ML model prediction (0.0-1.0)
 * @property brandWeight Weight for brand impersonation detection (0.0-1.0)
 * @property tldWeight Weight for TLD risk scoring (0.0-1.0)
 * @property safeThreshold Score at or below which verdict is SAFE
 * @property suspiciousThreshold Score above which verdict is MALICIOUS (SUSPICIOUS in between)
 * @property baseConfidence Starting confidence value before adjustments
 *
 * @author Mehr Guard Security Team
 * @since 1.7.0
 * @see PhishingEngine
 * @see SecurityConstants
 */
data class ScoringConfig(
    /**
     * Weight for heuristic engine score in combined calculation.
     *
     * Heuristics are the primary detector—rule-based patterns that catch
     * common phishing indicators like:
     * - HTTP instead of HTTPS
     * - IP addresses as hosts
     * - Suspicious TLDs
     * - @ symbol injection
     *
     * Higher weight = more reliance on rule-based detection.
     */
    val heuristicWeight: Double = 0.50,

    /**
     * Weight for ML model prediction in combined calculation.
     *
     * The ML model (ensemble of logistic regression, gradient boosting, and
     * decision rules) provides probabilistic scoring based on URL features.
     *
     * Higher weight = more reliance on learned patterns.
     */
    val mlWeight: Double = 0.20,

    /**
     * Weight for brand detection score in combined calculation.
     *
     * Brand detection catches typosquatting and impersonation:
     * - "paypa1" → "paypal" match
     * - "g00gle" → "google" match
     * - Homograph attacks using Cyrillic/Greek characters
     *
     * Higher weight = more aggressive brand protection.
     */
    val brandWeight: Double = 0.15,

    /**
     * Weight for TLD risk scoring in combined calculation.
     *
     * Some TLDs are statistically more associated with phishing:
     * - .tk, .ml, .ga, .cf: Free TLDs heavily abused
     * - .xyz, .top, .click: Cheap TLDs with high abuse rates
     *
     * Higher weight = more penalty for risky TLDs.
     */
    val tldWeight: Double = 0.15,

    /**
     * Maximum combined score at which verdict is SAFE.
     *
     * URLs scoring at or below this threshold are considered safe.
     * Default is 10, which is intentionally low because the weighted
     * combination of multiple scoring engines produces lower scores
     * for truly safe URLs than single-source scoring.
     *
     * @see SecurityConstants.PHISHING_ENGINE_SAFE_THRESHOLD
     */
    val safeThreshold: Int = SecurityConstants.PHISHING_ENGINE_SAFE_THRESHOLD,

    /**
     * Minimum combined score at which verdict is MALICIOUS.
     *
     * URLs scoring at or above this threshold are flagged as malicious.
     * URLs between [safeThreshold] and [suspiciousThreshold] are SUSPICIOUS.
     *
     * @see SecurityConstants.PHISHING_ENGINE_SUSPICIOUS_THRESHOLD
     */
    val suspiciousThreshold: Int = SecurityConstants.PHISHING_ENGINE_SUSPICIOUS_THRESHOLD,

    /**
     * Base confidence value before agreement/signal adjustments.
     *
     * Confidence increases when:
     * - Heuristics and ML scores agree
     * - Brand impersonation is detected
     * - Multiple heuristic signals fire
     *
     * @see SecurityConstants.BASE_CONFIDENCE
     */
    val baseConfidence: Float = SecurityConstants.BASE_CONFIDENCE,

    /**
     * Maximum URL length to process.
     *
     * URLs longer than this are rejected as potentially malicious
     * (buffer overflow attacks, DoS attempts, or just suspicious).
     *
     * @see SecurityConstants.MAX_URL_LENGTH
     */
    val maxUrlLength: Int = SecurityConstants.MAX_URL_LENGTH
) {

    init {
        // Validate weights sum to approximately 1.0
        val weightSum = heuristicWeight + mlWeight + brandWeight + tldWeight
        require(weightSum in 0.99..1.01) {
            "Weights must sum to 1.0, got $weightSum"
        }

        // Validate weight ranges
        require(heuristicWeight in 0.0..1.0) { "heuristicWeight must be 0.0-1.0" }
        require(mlWeight in 0.0..1.0) { "mlWeight must be 0.0-1.0" }
        require(brandWeight in 0.0..1.0) { "brandWeight must be 0.0-1.0" }
        require(tldWeight in 0.0..1.0) { "tldWeight must be 0.0-1.0" }

        // Validate threshold ordering
        require(safeThreshold < suspiciousThreshold) {
            "safeThreshold ($safeThreshold) must be less than suspiciousThreshold ($suspiciousThreshold)"
        }
        require(safeThreshold >= 0) { "safeThreshold must be non-negative" }
        require(suspiciousThreshold <= 100) { "suspiciousThreshold must be at most 100" }

        // Validate confidence range
        require(baseConfidence in 0f..1f) { "baseConfidence must be 0.0-1.0" }
    }

    companion object {
        /**
         * Default production configuration.
         *
         * Empirically tuned for optimal F1 score on real-world phishing URLs.
         */
        val DEFAULT = ScoringConfig()

        /**
         * High-sensitivity configuration for paranoid mode.
         *
         * Flags more URLs as suspicious, higher false positive rate.
         * Use for high-security environments.
         */
        val HIGH_SENSITIVITY = ScoringConfig(
            safeThreshold = 5,
            suspiciousThreshold = 30
        )

        /**
         * Low-sensitivity configuration for relaxed mode.
         *
         * Flags fewer URLs as suspicious, lower false positive rate but
         * may miss some attacks. Use for general browsing.
         */
        val LOW_SENSITIVITY = ScoringConfig(
            safeThreshold = 20,
            suspiciousThreshold = 70
        )

        /**
         * Configuration optimized for brand protection.
         *
         * Increases weight of brand detection for organizations
         * concerned about impersonation attacks.
         */
        val BRAND_FOCUSED = ScoringConfig(
            heuristicWeight = 0.35,
            mlWeight = 0.15,
            brandWeight = 0.35,
            tldWeight = 0.15
        )

        /**
         * Configuration for ML-first scoring.
         *
         * Relies more on ML model predictions than rules.
         * Good for detecting novel attack patterns.
         */
        val ML_FOCUSED = ScoringConfig(
            heuristicWeight = 0.30,
            mlWeight = 0.40,
            brandWeight = 0.15,
            tldWeight = 0.15
        )
    }
}
