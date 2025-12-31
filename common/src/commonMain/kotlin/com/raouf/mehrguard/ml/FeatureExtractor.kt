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
