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

package com.raouf.mehrguard.engine

/**
 * Brand Impersonation Detector for Mehr Guard
 *
 * Detects when URLs attempt to impersonate well-known brands
 * using typosquatting, homographs, subdomain abuse, and fuzzy matching.
 *
 * SECURITY NOTES:
 * - All inputs are bounded to prevent DoS
 * - Brand database is immutable
 * - Thread-safe for concurrent detection
 * - Includes Australian banking institutions
 *
 * @author Mehr Guard Security Team
 * @since 1.0.0
 */
class BrandDetector {

    companion object {
        // === CONFIGURATION ===

        /** Maximum URL length to process */
        private const val MAX_URL_LENGTH = 2048

        /** Maximum host length to process */
        private const val MAX_HOST_LENGTH = 255

        /** Minimum edit distance threshold for fuzzy matching */
        private const val FUZZY_MATCH_THRESHOLD = 2

        // === SCORING ===

        const val SCORE_EXACT_SUBDOMAIN = 30
        const val SCORE_TYPOSQUAT = 35
        const val SCORE_HOMOGRAPH = 40
        const val SCORE_COMBOSQUAT = 25
        const val SCORE_FUZZY_MATCH = 20
    }


    /**
     * Match type classification.
     */
    enum class MatchType {
        EXACT_IN_SUBDOMAIN,
        TYPOSQUAT,
        HOMOGRAPH,
        COMBO_SQUAT,
        FUZZY_MATCH
    }

    /**
     * Brand match details.
     */
    data class BrandMatch(
        val brand: String,
        val matchType: MatchType,
        val matchedPattern: String,
        val category: BrandDatabase.BrandCategory
    )

    /**
     * Detection result.
     */
    data class DetectionResult(
        val score: Int,
        val match: String?,
        val details: BrandMatch?
    ) {
        val isImpersonation: Boolean get() = match != null

        val severity: String
            get() = when {
                score >= SCORE_HOMOGRAPH -> "CRITICAL"
                score >= SCORE_TYPOSQUAT -> "HIGH"
                score >= SCORE_EXACT_SUBDOMAIN -> "MEDIUM"
                score > 0 -> "LOW"
                else -> "NONE"
            }
    }

    /**
     * Detect brand impersonation in URL.
     *
     * @param url The URL to analyze
     * @return DetectionResult with score and match details
     */
    fun detect(url: String): DetectionResult {
        // SECURITY: Validate input length
        if (url.isBlank() || url.length > MAX_URL_LENGTH) {
            return DetectionResult(score = 0, match = null, details = null)
        }

        val host = extractHost(url)

        // SECURITY: Validate extracted host
        if (host.isBlank() || host.length > MAX_HOST_LENGTH) {
            return DetectionResult(score = 0, match = null, details = null)
        }

        val hostLower = host.lowercase()

        // Check each brand from the database
        for ((brand, config) in BrandDatabase.brands) {
            // 1. EXACT_IN_SUBDOMAIN - brand appears in subdomain
            if (isBrandInSubdomain(hostLower, brand, config.officialDomains)) {
                return DetectionResult(
                    score = SCORE_EXACT_SUBDOMAIN,
                    match = brand,
                    details = BrandMatch(brand, MatchType.EXACT_IN_SUBDOMAIN, brand, config.category)
                )
            }

            // 2. HOMOGRAPH - highest severity, check first
            for (variant in config.homographs) {
                if (hostLower.contains(variant.lowercase())) {
                    return DetectionResult(
                        score = SCORE_HOMOGRAPH,
                        match = brand,
                        details = BrandMatch(brand, MatchType.HOMOGRAPH, variant, config.category)
                    )
                }
            }

            // 3. TYPOSQUAT - character substitution
            for (variant in config.typosquats) {
                if (hostLower.contains(variant.lowercase())) {
                    return DetectionResult(
                        score = SCORE_TYPOSQUAT,
                        match = brand,
                        details = BrandMatch(brand, MatchType.TYPOSQUAT, variant, config.category)
                    )
                }
            }

            // 4. COMBOSQUAT - brand + keyword
            for (combo in config.combosquats) {
                if (hostLower.contains(combo.lowercase())) {
                    return DetectionResult(
                        score = SCORE_COMBOSQUAT,
                        match = brand,
                        details = BrandMatch(brand, MatchType.COMBO_SQUAT, combo, config.category)
                    )
                }
            }

            // 5. FUZZY MATCH - edit distance (expensive, check last)
            if (isFuzzyMatch(hostLower, brand)) {
                return DetectionResult(
                    score = SCORE_FUZZY_MATCH,
                    match = brand,
                    details = BrandMatch(brand, MatchType.FUZZY_MATCH, brand, config.category)
                )
            }
        }

        return DetectionResult(score = 0, match = null, details = null)
    }

    /**
     * Check multiple URLs in batch.
     *
     * @param urls List of URLs to check
     * @return Map of URL to DetectionResult
     */
    fun detectBatch(urls: List<String>): Map<String, DetectionResult> {
        // SECURITY: Limit batch size
        val bounded = urls.take(100)
        return bounded.associateWith { detect(it) }
    }

    /**
     * Check if brand appears in subdomain but not on official domain.
     */
    private fun isBrandInSubdomain(host: String, brand: String, officialDomains: Set<String>): Boolean {
        // If this is an official domain, not impersonation
        if (officialDomains.any { host.endsWith(it) }) {
            return false
        }

        val parts = host.split(".")
        if (parts.size < 3) return false

        // Check subdomains for brand name
        val subdomains = parts.dropLast(2).joinToString(".")
        return subdomains.contains(brand)
    }

    /**
     * Calculate Levenshtein distance for fuzzy matching.
     * Limited to short strings for performance.
     */
    private fun isFuzzyMatch(host: String, brand: String): Boolean {
        // Only check domain part, not full host
        val parts = host.split(".")
        if (parts.size < 2) return false

        val domain = parts[parts.size - 2].lowercase()

        // Skip if lengths are too different
        if (kotlin.math.abs(domain.length - brand.length) > FUZZY_MATCH_THRESHOLD) {
            return false
        }

        // Calculate edit distance
        val distance = levenshteinDistance(domain, brand)
        return distance in 1..FUZZY_MATCH_THRESHOLD
    }

    /**
     * Calculate Levenshtein edit distance.
     *
     * Bounded implementation to prevent DoS on very long strings.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        // SECURITY: Limit input size
        val a = s1.take(50)
        val b = s2.take(50)

        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        // OPTIMIZATION: Always make 'a' the shorter string
        val (shorter, longer) = if (a.length <= b.length) a to b else b to a

        // OPTIMIZATION: Early exit if length difference exceeds threshold
        val lengthDiff = longer.length - shorter.length
        if (lengthDiff > 3) return lengthDiff

        // Single-row DP optimization: O(min(m,n)) space
        var previousRow = IntArray(shorter.length + 1) { it }
        var currentRow = IntArray(shorter.length + 1)

        for (i in 1..longer.length) {
            currentRow[0] = i

            for (j in 1..shorter.length) {
                val cost = if (longer[i - 1] == shorter[j - 1]) 0 else 1
                currentRow[j] = minOf(
                    currentRow[j - 1] + 1,      // insertion
                    previousRow[j] + 1,          // deletion
                    previousRow[j - 1] + cost    // substitution
                )
            }

            // Swap rows
            val temp = previousRow
            previousRow = currentRow
            currentRow = temp
        }

        return previousRow[shorter.length]
    }

    /**
     * Safely extract host from URL.
     * URL-decodes percent-encoded characters to catch obfuscation attacks.
     */
    private fun extractHost(url: String): String {
        val bounded = url.take(MAX_URL_LENGTH)

        val withoutProtocol = bounded
            .removePrefix("https://")
            .removePrefix("http://")

        val endIndex = withoutProtocol.indexOfFirst { it == '/' || it == '?' || it == '#' || it == ':' }

        val rawHost = when {
            endIndex > 0 -> withoutProtocol.substring(0, endIndex)
            else -> withoutProtocol
        }.take(MAX_HOST_LENGTH)
        
        // URL-decode to catch encoded obfuscation like %20 (space), %2E (dot)
        return decodePercentEncoding(rawHost)
    }
    
    /**
     * Decode percent-encoded characters in a string.
     * Handles common URL obfuscation techniques.
     */
    private fun decodePercentEncoding(input: String): String {
        return input.replace(Regex("%([0-9A-Fa-f]{2})")) { match ->
            val code = match.groupValues[1].toInt(16)
            if (code in 0x20..0x7E) { // Printable ASCII
                code.toChar().toString()
            } else {
                match.value // Keep non-printable as-is
            }
        }
    }
}
