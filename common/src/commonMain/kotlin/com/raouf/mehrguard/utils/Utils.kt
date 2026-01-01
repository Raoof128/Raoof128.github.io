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

package com.raouf.mehrguard.utils

/**
 * Constants used throughout Mehr Guard
 */
object Constants {

    // Risk Score Thresholds
    const val SAFE_THRESHOLD = 30
    const val SUSPICIOUS_THRESHOLD = 70

    // Scoring Weights
    const val HEURISTIC_WEIGHT = 0.40
    const val ML_WEIGHT = 0.35
    const val BRAND_WEIGHT = 0.15
    const val TLD_WEIGHT = 0.10

    // URL Length Limits
    const val MAX_URL_LENGTH = 2048
    const val LONG_URL_THRESHOLD = 200

    // Entropy Thresholds
    const val HIGH_ENTROPY_THRESHOLD = 4.0

    // History Limits
    const val MAX_HISTORY_ITEMS = 100

    // Timeouts
    const val SCAN_TIMEOUT_MS = 30_000L
    const val ANALYSIS_TIMEOUT_MS = 5_000L
}

/**
 * URL validation and parsing utilities
 */
object UrlUtils {

    private val URL_REGEX = Regex(
        """^(https?://)?([a-zA-Z0-9]([a-zA-Z0-9\-]*[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}(:\d+)?(/.*)?$"""
    )

    private val IP_REGEX = Regex(
        """^(\d{1,3}\.){3}\d{1,3}$"""
    )

    /**
     * Check if string is a valid URL
     */
    fun isValidUrl(url: String): Boolean {
        if (url.isBlank()) return false
        if (url.length > Constants.MAX_URL_LENGTH) return false

        return url.startsWith("http://") ||
               url.startsWith("https://") ||
               URL_REGEX.matches(url)
    }

    /**
     * Normalize URL for consistent analysis
     */
    fun normalize(url: String): String {
        var normalized = url.trim().lowercase()

        // Add protocol if missing
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://$normalized"
        }

        // Remove trailing slash
        if (normalized.endsWith("/") && normalized.count { it == '/' } > 3) {
            normalized = normalized.dropLast(1)
        }

        return normalized
    }

    /**
     * Extract host from URL
     */
    fun extractHost(url: String): String {
        val withoutProtocol = url
            .removePrefix("https://")
            .removePrefix("http://")

        val endIndex = withoutProtocol.indexOfFirst { it == '/' || it == '?' || it == '#' || it == ':' }
        return if (endIndex > 0) withoutProtocol.substring(0, endIndex) else withoutProtocol
    }

    /**
     * Check if host is an IP address
     */
    fun isIpAddress(host: String): Boolean {
        return IP_REGEX.matches(host)
    }
}

/**
 * Entropy calculator for randomness detection
 */
object EntropyCalculator {

    /**
     * Calculate Shannon entropy of a string
     * Higher entropy = more random/unpredictable
     */
    fun calculate(text: String): Double {
        if (text.isEmpty()) return 0.0

        val frequencies = text.groupingBy { it }.eachCount()
        val length = text.length.toDouble()

        return frequencies.values.sumOf { count ->
            val probability = count / length
            -probability * kotlin.math.log2(probability)
        }
    }

    /**
     * Check if entropy exceeds threshold
     */
    fun isHighEntropy(text: String, threshold: Double = Constants.HIGH_ENTROPY_THRESHOLD): Boolean {
        return calculate(text) > threshold
    }
}
