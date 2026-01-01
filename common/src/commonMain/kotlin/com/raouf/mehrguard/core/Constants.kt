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
 * Centralized Constants for Mehr Guard
 *
 * All magic numbers and configuration values in one place for:
 * - Easy tuning and calibration
 * - Consistent values across components
 * - Clear documentation of each constant's purpose
 *
 * @author Mehr Guard Security Team
 * @since 1.0.0
 */
object Constants {

    // =========================================================================
    // SCORING THRESHOLDS
    // =========================================================================

    object Thresholds {
        /**
         * Score at or below which URL is considered SAFE.
         * Range: 0-100
         */
        const val SAFE_THRESHOLD = 10

        /**
         * Score at or below which URL is considered SUSPICIOUS.
         * Above this is MALICIOUS.
         * Range: 0-100
         */
        const val SUSPICIOUS_THRESHOLD = 50

        /**
         * Minimum score for a URL to be flagged at all.
         */
        const val MINIMUM_FLAG_SCORE = 5

        /**
         * High confidence threshold for ML predictions.
         */
        const val HIGH_CONFIDENCE = 0.8f

        /**
         * Medium confidence threshold for ML predictions.
         */
        const val MEDIUM_CONFIDENCE = 0.6f
    }

    // =========================================================================
    // SCORING WEIGHTS
    // =========================================================================

    object Weights {
        /**
         * Weight for heuristic analysis in combined score.
         * Should sum to 100 with other weights.
         */
        const val HEURISTIC_WEIGHT_PERCENT = 50

        /**
         * Weight for ML prediction in combined score.
         */
        const val ML_WEIGHT_PERCENT = 25

        /**
         * Weight for brand detection in combined score.
         */
        const val BRAND_WEIGHT_PERCENT = 15

        /**
         * Weight for TLD scoring in combined score.
         */
        const val TLD_WEIGHT_PERCENT = 10
    }

    // =========================================================================
    // URL LIMITS
    // =========================================================================

    object Limits {
        /**
         * Maximum URL length to process.
         * Longer URLs are auto-flagged as suspicious.
         */
        const val MAX_URL_LENGTH = 2048

        /**
         * Maximum hostname length.
         */
        const val MAX_HOST_LENGTH = 255

        /**
         * Maximum path length.
         */
        const val MAX_PATH_LENGTH = 1024

        /**
         * Maximum number of subdomains before flagging.
         */
        const val MAX_SUBDOMAINS = 5

        /**
         * Maximum query parameter count before flagging.
         */
        const val MAX_QUERY_PARAMS = 10

        /**
         * Maximum history items to keep.
         */
        const val MAX_HISTORY_ITEMS = 1000

        /**
         * Maximum batch size for bulk operations.
         */
        const val MAX_BATCH_SIZE = 100
    }

    // =========================================================================
    // ENTROPY THRESHOLDS
    // =========================================================================

    object Entropy {
        /**
         * Shannon entropy threshold for suspicious domain names.
         * Higher entropy indicates random/generated strings.
         */
        const val DOMAIN_THRESHOLD = 4.0

        /**
         * Maximum domain entropy (normalized to 0-1).
         */
        const val MAX_ENTROPY = 5.0f

        /**
         * Path entropy threshold for suspicious paths.
         */
        const val PATH_THRESHOLD = 4.5
    }

    // =========================================================================
    // SCORING RANGES
    // =========================================================================

    object Ranges {
        /**
         * Minimum score value.
         */
        const val MIN_SCORE = 0

        /**
         * Maximum score value.
         */
        const val MAX_SCORE = 100

        /**
         * Score range for normalization.
         */
        const val SCORE_RANGE = MAX_SCORE - MIN_SCORE
    }

    // =========================================================================
    // TIMING
    // =========================================================================

    object Timing {
        /**
         * Analysis timeout in milliseconds.
         */
        const val ANALYSIS_TIMEOUT_MS = 10_000L

        /**
         * Camera scan timeout in milliseconds.
         */
        const val SCAN_TIMEOUT_MS = 30_000L

        /**
         * Debounce delay for rapid scans.
         */
        const val SCAN_DEBOUNCE_MS = 500L

        /**
         * Flow subscription timeout in milliseconds.
         */
        const val FLOW_TIMEOUT_MS = 5_000L
    }

    // =========================================================================
    // FEATURE EXTRACTION NORMALIZATION
    // =========================================================================

    object FeatureNormalization {
        /**
         * Maximum URL length for feature normalization.
         */
        const val URL_LENGTH_MAX = 500f

        /**
         * Maximum host length for feature normalization.
         */
        const val HOST_LENGTH_MAX = 100f

        /**
         * Maximum path length for feature normalization.
         */
        const val PATH_LENGTH_MAX = 200f

        /**
         * Maximum subdomain count for feature normalization.
         */
        const val SUBDOMAIN_COUNT_MAX = 5f

        /**
         * Maximum query param count for feature normalization.
         */
        const val QUERY_PARAM_COUNT_MAX = 10f

        /**
         * Maximum dot count for feature normalization.
         */
        const val DOT_COUNT_MAX = 10f

        /**
         * Maximum dash count for feature normalization.
         */
        const val DASH_COUNT_MAX = 10f
    }

    // =========================================================================
    // IMAGE PROCESSING
    // =========================================================================

    object Image {
        /**
         * Maximum image dimension in pixels.
         */
        const val MAX_IMAGE_DIMENSION = 4096

        /**
         * Maximum image file size in bytes (10MB).
         */
        const val MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024L

        /**
         * Default sample size for image decoding.
         */
        const val DEFAULT_SAMPLE_SIZE = 1

        /**
         * JPEG quality for compressed images.
         */
        const val JPEG_QUALITY = 85
    }

    // =========================================================================
    // HELPER FUNCTIONS
    // =========================================================================

    /**
     * Clamp score to valid range.
     */
    fun clampScore(score: Int): Int = score.coerceIn(Ranges.MIN_SCORE, Ranges.MAX_SCORE)

    /**
     * Clamp score to valid range.
     */
    fun clampScore(score: Float): Float = score.coerceIn(Ranges.MIN_SCORE.toFloat(), Ranges.MAX_SCORE.toFloat())

    /**
     * Normalize a value to 0-1 range.
     */
    fun normalize(value: Float, max: Float): Float = (value / max).coerceIn(0f, 1f)
}
