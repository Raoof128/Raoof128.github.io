/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.ml

import kotlin.math.ln
import kotlin.math.sqrt

/**
 * URL Feature Extractor for ML scoring.
 *
 * Extracts numerical features from URLs for the ML model.
 * All features are normalized to [0, 1] range.
 *
 * ## Features (24 total)
 * - Length features: url, host, path, query
 * - Count features: dots, hyphens, digits, special chars
 * - Ratio features: digit ratio, letter ratio, special ratio
 * - Structure features: subdomain depth, port presence, scheme
 * - Entropy features: host entropy, path entropy
 * - Pattern features: IP detection, punycode, suspicious keywords
 *
 * @author QR-SHIELD Security Team
 * @since 1.19.0
 */
class UrlFeatureExtractor {

    /**
     * Extracted features for a URL.
     */
    data class UrlFeatures(
        val vector: FloatArray,
        val featureNames: List<String>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as UrlFeatures
            return vector.contentEquals(other.vector)
        }

        override fun hashCode(): Int = vector.contentHashCode()
    }

    /**
     * Extract features from a URL.
     */
    fun extract(url: String): UrlFeatures {
        val normalized = url.lowercase().take(2048)
        
        // Parse URL components
        val hasScheme = normalized.contains("://")
        val schemeEnd = if (hasScheme) normalized.indexOf("://") + 3 else 0
        val afterScheme = normalized.substring(schemeEnd)
        
        val pathStart = afterScheme.indexOf("/").let { if (it < 0) afterScheme.length else it }
        val hostPart = afterScheme.substring(0, pathStart)
        val pathPart = if (pathStart < afterScheme.length) afterScheme.substring(pathStart) else ""
        
        val queryStart = pathPart.indexOf("?")
        val path = if (queryStart >= 0) pathPart.substring(0, queryStart) else pathPart
        val query = if (queryStart >= 0) pathPart.substring(queryStart + 1) else ""

        // Extract host and port
        val portIdx = hostPart.lastIndexOf(":")
        val host = if (portIdx > 0 && hostPart.substring(portIdx + 1).all { it.isDigit() }) {
            hostPart.substring(0, portIdx)
        } else {
            hostPart
        }
        val hasPort = portIdx > 0 && hostPart.substring(portIdx + 1).all { it.isDigit() }

        // Build feature vector
        val features = mutableListOf<Float>()

        // 1-4: Length features (normalized)
        features.add(normalize(normalized.length.toFloat(), 0f, 500f))
        features.add(normalize(host.length.toFloat(), 0f, 100f))
        features.add(normalize(path.length.toFloat(), 0f, 200f))
        features.add(normalize(query.length.toFloat(), 0f, 300f))

        // 5-8: Count features
        features.add(normalize(host.count { it == '.' }.toFloat(), 0f, 10f))
        features.add(normalize(normalized.count { it == '-' }.toFloat(), 0f, 20f))
        features.add(normalize(normalized.count { it.isDigit() }.toFloat(), 0f, 50f))
        features.add(normalize(normalized.count { it in SPECIAL_CHARS }.toFloat(), 0f, 30f))

        // 9-11: Ratio features
        val totalChars = normalized.length.coerceAtLeast(1)
        features.add(normalized.count { it.isDigit() }.toFloat() / totalChars)
        features.add(normalized.count { it.isLetter() }.toFloat() / totalChars)
        features.add(normalized.count { it in SPECIAL_CHARS }.toFloat() / totalChars)

        // 12-14: Structure features
        features.add(normalize(host.count { it == '.' }.toFloat(), 0f, 5f))  // Subdomain depth
        features.add(if (hasPort) 1.0f else 0.0f)
        features.add(if (normalized.startsWith("https")) 0.0f else 1.0f)  // HTTP = risky

        // 15-16: Entropy features
        features.add(normalize(calculateEntropy(host), 0f, 5f))
        features.add(normalize(calculateEntropy(path), 0f, 5f))

        // 17-20: Pattern features
        features.add(if (isIpAddress(host)) 1.0f else 0.0f)
        features.add(if (host.contains("xn--")) 1.0f else 0.0f)  // Punycode
        features.add(if (normalized.contains("@")) 1.0f else 0.0f)  // @ injection
        features.add(countSuspiciousKeywords(normalized).toFloat().coerceAtMost(5f) / 5f)

        // 21-24: Additional patterns
        features.add(if (hasRiskyTld(host)) 1.0f else 0.0f)
        features.add(if (hasDoubleExtension(path)) 1.0f else 0.0f)
        features.add(countEncodedChars(normalized).toFloat().coerceAtMost(10f) / 10f)
        features.add(if (normalized.startsWith("javascript:") || normalized.startsWith("data:")) 1.0f else 0.0f)

        return UrlFeatures(
            vector = features.toFloatArray(),
            featureNames = FEATURE_NAMES
        )
    }

    // === Private helpers ===

    private fun normalize(value: Float, min: Float, max: Float): Float {
        return ((value - min) / (max - min)).coerceIn(0f, 1f)
    }

    private fun calculateEntropy(s: String): Float {
        if (s.isEmpty()) return 0f
        val freq = s.groupingBy { it }.eachCount()
        val len = s.length.toFloat()
        return -freq.values.map { count ->
            val p = count / len
            p * ln(p)
        }.sum().toFloat()
    }

    private fun isIpAddress(host: String): Boolean {
        val ipv4 = Regex("""^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$""")
        val ipv6 = host.startsWith("[") && host.endsWith("]")
        return ipv4.matches(host) || ipv6
    }

    private fun countSuspiciousKeywords(url: String): Int {
        return SUSPICIOUS_KEYWORDS.count { url.contains(it) }
    }

    private fun hasRiskyTld(host: String): Boolean {
        return RISKY_TLDS.any { host.endsWith(".$it") }
    }

    private fun hasDoubleExtension(path: String): Boolean {
        val extensions = listOf(".exe", ".scr", ".bat", ".cmd", ".ps1", ".js", ".vbs")
        val lowerPath = path.lowercase()
        return extensions.any { ext ->
            val idx = lowerPath.indexOf(ext)
            idx > 0 && lowerPath.substring(0, idx).contains(".")
        }
    }

    private fun countEncodedChars(url: String): Int {
        return Regex("""%[0-9A-Fa-f]{2}""").findAll(url).count()
    }

    companion object {
        val default = UrlFeatureExtractor()

        private val SPECIAL_CHARS = setOf('@', '#', '$', '%', '^', '&', '*', '!', '~', '`', '|')

        private val SUSPICIOUS_KEYWORDS = listOf(
            "login", "verify", "update", "secure", "account",
            "confirm", "password", "billing", "signin", "bank"
        )

        private val RISKY_TLDS = listOf(
            "tk", "ml", "ga", "cf", "gq", "xyz", "top", "work", "click", "loan"
        )

        val FEATURE_NAMES = listOf(
            "url_length", "host_length", "path_length", "query_length",
            "dot_count", "hyphen_count", "digit_count", "special_count",
            "digit_ratio", "letter_ratio", "special_ratio",
            "subdomain_depth", "has_port", "is_http",
            "host_entropy", "path_entropy",
            "is_ip", "is_punycode", "has_at_symbol", "suspicious_keywords",
            "risky_tld", "double_extension", "encoded_chars", "dangerous_scheme"
        )
    }
}
