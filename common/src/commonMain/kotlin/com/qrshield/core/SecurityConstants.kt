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

/**
 * Security Constants
 *
 * Centralized security-related constants for the detection engine.
 * Replaces magic numbers throughout the codebase for:
 * - Better maintainability
 * - Easier tuning
 * - Clear documentation of security thresholds
 *
 * ## Tuning Rationale
 * Each constant includes documentation of:
 * - What it controls
 * - Why this value was chosen
 * - Impact on detection accuracy
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */
object SecurityConstants {

    // ==================== Score Thresholds ====================

    /**
     * Maximum risk score.
     *
     * All scores are normalized to 0-100 for uniform interpretation.
     */
    const val MAX_SCORE: Int = 100

    /**
     * Minimum risk score.
     */
    const val MIN_SCORE: Int = 0

    /**
     * Threshold for SAFE verdict.
     *
     * URLs scoring BELOW this are considered SAFE.
     * Chosen based on false-positive analysis — 30 minimizes
     * legitimate sites being flagged while catching obvious phishing.
     */
    const val SAFE_THRESHOLD: Int = 30

    /**
     * Threshold for MALICIOUS verdict.
     *
     * URLs scoring AT OR ABOVE this are considered MALICIOUS.
     * Chosen conservatively — only high-confidence phishing crosses this.
     */
    const val MALICIOUS_THRESHOLD: Int = 70

    /**
     * Threshold for high confidence detection.
     *
     * When score exceeds this, we're very confident it's phishing.
     */
    const val HIGH_CONFIDENCE_THRESHOLD: Int = 85

    // ==================== Component Weights ====================

    /**
     * Weight for heuristic score in combined calculation.
     *
     * Heuristics are the primary detector, weighted highest.
     * Based on empirical testing showing 40% weighting optimizes F1 score.
     */
    const val HEURISTIC_WEIGHT: Float = 0.40f

    /**
     * Weight for ML score in combined calculation.
     *
     * ML provides secondary validation, moderate weight.
     */
    const val ML_WEIGHT: Float = 0.30f

    /**
     * Weight for brand detection score.
     *
     * Brand impersonation is a strong signal but not sole indicator.
     */
    const val BRAND_WEIGHT: Float = 0.20f

    /**
     * Weight for TLD risk score.
     *
     * TLD alone is weak signal (many legitimate .tk sites exist).
     */
    const val TLD_WEIGHT: Float = 0.10f

    /**
     * Maximum brand detection score (static + dynamic combined).
     *
     * Caps the contribution from brand detection to prevent
     * over-penalization when multiple brand signals fire.
     */
    const val MAX_BRAND_SCORE: Int = 45

    // ==================== Confidence Calculation ====================

    /**
     * Base confidence when no strong signals detected.
     */
    const val BASE_CONFIDENCE: Float = 0.5f

    /**
     * Maximum confidence achievable.
     */
    const val MAX_CONFIDENCE: Float = 0.99f

    /**
     * Minimum confidence for valid analysis.
     */
    const val MIN_CONFIDENCE: Float = 0.1f

    /**
     * Confidence boost when heuristics and ML agree.
     *
     * When both systems reach same conclusion, confidence increases.
     */
    const val AGREEMENT_BOOST: Float = 0.15f

    /**
     * Confidence boost per additional heuristic signal.
     *
     * More signals = higher confidence in detection.
     */
    const val SIGNAL_BOOST: Float = 0.02f

    // ==================== URL Limits ====================

    /**
     * Maximum URL length to analyze.
     *
     * URLs longer than this are suspicious (often phishing).
     * Legitimate URLs rarely exceed 2048 characters.
     */
    const val MAX_URL_LENGTH: Int = 2048

    /**
     * Maximum hostname length (DNS limit).
     */
    const val MAX_HOSTNAME_LENGTH: Int = 253

    /**
     * Maximum subdomain count before flagging.
     *
     * 4+ subdomains is unusual for legitimate sites.
     */
    const val MAX_SUBDOMAIN_COUNT: Int = 4

    /**
     * Maximum path depth before flagging.
     */
    const val MAX_PATH_DEPTH: Int = 6

    // ==================== Entropy Thresholds ====================

    /**
     * High entropy threshold for domain analysis.
     *
     * Random-looking strings have entropy > 3.5.
     * Legitimate domains typically have lower entropy.
     */
    const val HIGH_ENTROPY_THRESHOLD: Float = 3.5f

    /**
     * Very high entropy threshold.
     *
     * Extremely random strings (likely auto-generated).
     */
    const val VERY_HIGH_ENTROPY_THRESHOLD: Float = 4.0f

    // ==================== Homograph Detection ====================

    /**
     * Cyrillic Unicode range start.
     *
     * Characters in this range look like Latin but are Cyrillic.
     */
    const val CYRILLIC_START: Int = 0x0400

    /**
     * Cyrillic Unicode range end.
     */
    const val CYRILLIC_END: Int = 0x04FF

    /**
     * Greek Unicode range start.
     */
    const val GREEK_START: Int = 0x0370

    /**
     * Greek Unicode range end.
     */
    const val GREEK_END: Int = 0x03FF

    /**
     * Armenian Unicode range start.
     */
    const val ARMENIAN_START: Int = 0x0530

    /**
     * Armenian Unicode range end.
     */
    const val ARMENIAN_END: Int = 0x058F

    /**
     * Georgian Unicode range start.
     */
    const val GEORGIAN_START: Int = 0x10A0

    /**
     * Georgian Unicode range end.
     */
    const val GEORGIAN_END: Int = 0x10FF

    /**
     * Risk score for homograph attack detection.
     */
    const val HOMOGRAPH_RISK_SCORE: Int = 45

    // ==================== IP Address Obfuscation ====================

    /**
     * Maximum valid IP octet value.
     */
    const val MAX_IP_OCTET: Int = 255

    /**
     * Maximum valid port number.
     */
    const val MAX_PORT: Int = 65535

    /**
     * Decimal IP threshold (4 billion = max IPv4).
     */
    const val MAX_DECIMAL_IP: Long = 4_294_967_295L

    // ==================== Levenshtein Distance ====================

    /**
     * Maximum edit distance for typosquat detection.
     *
     * 2-3 edits catches most typosquats without false positives.
     */
    const val MAX_TYPOSQUAT_DISTANCE: Int = 2

    /**
     * Minimum brand length for fuzzy matching.
     *
     * Short brand names have too many false positives.
     */
    const val MIN_BRAND_LENGTH: Int = 4

    // ==================== Rate Limiting ====================

    /**
     * Maximum analysis requests per minute.
     */
    const val MAX_REQUESTS_PER_MINUTE: Int = 100

    /**
     * Debounce time for repeated scans (milliseconds).
     */
    const val SCAN_DEBOUNCE_MS: Long = 2000L

    // ==================== History Limits ====================

    /**
     * Maximum history items to store.
     */
    const val MAX_HISTORY_ITEMS: Int = 1000

    /**
     * Days to retain history.
     */
    const val HISTORY_RETENTION_DAYS: Int = 30
}

/**
 * Feature Extraction Constants
 *
 * Constants for URL feature extraction (ML model input).
 */
object FeatureConstants {

    /**
     * Number of features extracted from URLs.
     */
    const val FEATURE_COUNT: Int = 15

    /**
     * Maximum feature value for normalization.
     */
    const val MAX_FEATURE_VALUE: Float = 1.0f

    /**
     * Feature indices for clarity.
     */
    object Index {
        const val URL_LENGTH: Int = 0
        const val HOST_LENGTH: Int = 1
        const val PATH_LENGTH: Int = 2
        const val QUERY_LENGTH: Int = 3
        const val DOT_COUNT: Int = 4
        const val DIGIT_RATIO: Int = 5
        const val SPECIAL_CHAR_RATIO: Int = 6
        const val ENTROPY: Int = 7
        const val HAS_IP: Int = 8
        const val HAS_AT_SYMBOL: Int = 9
        const val HAS_REDIRECT: Int = 10
        const val SUBDOMAIN_COUNT: Int = 11
        const val PATH_DEPTH: Int = 12
        const val IS_HTTPS: Int = 13
        const val HAS_BRAND_KEYWORD: Int = 14
    }
}

/**
 * TLD Risk Scores
 *
 * Risk scores for top-level domains based on abuse frequency.
 * Data sourced from Spamhaus, SURBL, and phishing databases.
 *
 * ## Scoring Rationale
 * - 0-5: Low risk (established, expensive TLDs)
 * - 6-10: Medium risk (free/new TLDs with some abuse)
 * - 11-20: High risk (frequently abused free TLDs)
 */
object TldRiskScores {

    /** Free TLDs heavily abused for phishing */
    const val HIGH_RISK: Int = 18

    /** New/cheap TLDs with moderate abuse */
    const val MEDIUM_RISK: Int = 10

    /** Established TLDs with low abuse */
    const val LOW_RISK: Int = 3

    /** Premium TLDs (expensive, rarely abused) */
    const val MINIMAL_RISK: Int = 1

    /**
     * High-risk TLDs (frequent phishing abuse).
     */
    val highRiskTlds = setOf(
        "tk", "ml", "ga", "cf", "gq",  // Freenom free TLDs
        "buzz", "xyz", "top", "work",  // Cheap new gTLDs
        "click", "link", "surf"        // Action-oriented
    )

    /**
     * Medium-risk TLDs (occasional abuse).
     */
    val mediumRiskTlds = setOf(
        "info", "biz", "club", "online",
        "site", "website", "space", "store"
    )
}
