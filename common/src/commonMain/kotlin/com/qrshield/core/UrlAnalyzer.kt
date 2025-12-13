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
 * URL Analyzer for QR-SHIELD
 *
 * Parses and extracts features from URLs for phishing analysis.
 *
 * SECURITY NOTES:
 * - All inputs are length-bounded to prevent DoS
 * - Regex patterns are designed to avoid ReDoS
 * - All string operations use safe methods
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class UrlAnalyzer {

    companion object {
        /** Maximum allowed URL length to prevent DoS attacks */
        const val MAX_URL_LENGTH = 2048

        /** Maximum host length */
        const val MAX_HOST_LENGTH = 255

        /** Maximum number of subdomains to analyze */
        const val MAX_SUBDOMAIN_DEPTH = 10

        /** Safe IPv4 pattern (non-backtracking) */
        private val IPV4_PATTERN = Regex("""^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""")

        /** Known URL shortener services */
        private val SHORTENER_DOMAINS = setOf(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly",
            "is.gd", "buff.ly", "adf.ly", "j.mp", "tr.im",
            "short.link", "cutt.ly", "rb.gy", "shorturl.at",
            "tiny.cc", "shorte.st", "v.gd", "clicky.me"
        )

        /** Suspicious path keywords indicating phishing */
        private val SUSPICIOUS_KEYWORDS = setOf(
            "login", "signin", "sign-in", "verify", "secure", "account",
            "update", "confirm", "banking", "password", "credential",
            "authenticate", "validate", "recover", "reset", "unlock",
            "suspend", "limited", "unusual", "activity", "verify-identity"
        )

        /** Credential-related query parameters */
        private val CREDENTIAL_PARAMS = setOf(
            "password", "pwd", "pass", "token", "session", "sessionid",
            "auth", "key", "secret", "credential", "api_key", "apikey",
            "access_token", "bearer", "jwt", "oauth", "authorization"
        )
    }

    /**
     * Parsed URL data structure
     */
    data class ParsedUrl(
        val original: String,
        val protocol: String,
        val host: String,
        val port: Int?,
        val path: String,
        val query: String?,
        val fragment: String?,
        val subdomains: List<String>,
        val domain: String,
        val tld: String
    )

    /**
     * Parse a URL into its components with security validation.
     *
     * @param url The raw URL string to parse
     * @return ParsedUrl if valid, null if parsing fails or URL is malformed
     * @throws IllegalArgumentException if URL exceeds maximum length
     */
    fun parse(url: String): ParsedUrl? {
        // SECURITY: Validate input length to prevent DoS
        if (url.length > MAX_URL_LENGTH) {
            return null
        }

        // SECURITY: Reject null bytes and control characters
        if (url.any { it.code < 32 && it != '\t' }) {
            return null
        }

        return try {
            val normalized = normalizeUrl(url)
            if (normalized.isEmpty()) return null

            // Extract protocol safely
            val protocolEnd = normalized.indexOf("://")
            val protocol = when {
                protocolEnd > 0 && protocolEnd < 10 -> normalized.substring(0, protocolEnd)
                normalized.startsWith("//") -> "https"
                else -> "http"
            }.lowercase()

            // SECURITY: Only allow http/https protocols
            if (protocol !in listOf("http", "https")) {
                return null
            }

            // Extract host and rest
            val afterProtocol = when {
                protocolEnd > 0 -> normalized.substring(protocolEnd + 3)
                normalized.startsWith("//") -> normalized.substring(2)
                else -> normalized
            }

            // SECURITY: Validate remaining content exists
            if (afterProtocol.isEmpty()) return null

            val hostEnd = afterProtocol.indexOfFirst { it == '/' || it == '?' || it == '#' }
            val hostWithPort = if (hostEnd > 0) {
                afterProtocol.substring(0, hostEnd)
            } else afterProtocol

            // SECURITY: Validate host length
            if (hostWithPort.length > MAX_HOST_LENGTH) return null

            // Parse host and port with validation
            val (host, port) = parseHostAndPort(hostWithPort)

            // SECURITY: Validate host is not empty
            if (host.isBlank()) return null

            // Parse path, query, fragment
            val rest = if (hostEnd > 0) afterProtocol.substring(hostEnd) else ""
            val (path, query, fragment) = parsePathQueryFragment(rest)

            // Parse domain structure with depth limit
            val hostParts = host.split(".").take(MAX_SUBDOMAIN_DEPTH)
            val tld = hostParts.lastOrNull()?.lowercase() ?: ""
            val domain = if (hostParts.size >= 2) {
                "${hostParts[hostParts.size - 2]}.$tld"
            } else host.lowercase()

            val subdomains = if (hostParts.size > 2) {
                hostParts.dropLast(2)
            } else emptyList()

            ParsedUrl(
                original = url.take(MAX_URL_LENGTH), // Ensure bounded
                protocol = protocol,
                host = host.lowercase(),
                port = port,
                path = path,
                query = query?.take(1024), // Limit query length
                fragment = fragment?.take(256), // Limit fragment length
                subdomains = subdomains,
                domain = domain,
                tld = tld
            )
        } catch (e: Exception) {
            // SECURITY: Fail safely on any parsing error
            null
        }
    }

    /**
     * Check if URL uses HTTPS protocol.
     */
    fun isSecure(parsedUrl: ParsedUrl): Boolean {
        return parsedUrl.protocol == "https"
    }

    /**
     * Check if host is an IP address (IPv4 or IPv6).
     *
     * Detects:
     * - IPv4: 192.168.1.1
     * - IPv6 literal: [2001:db8::1]
     * - IPv6 without brackets for direct checking
     *
     * Uses safe patterns that cannot backtrack excessively.
     */
    fun isIpAddress(host: String): Boolean {
        return isIpv4Address(host) || isIpv6Address(host)
    }

    /**
     * Check if host is an IPv4 address.
     */
    fun isIpv4Address(host: String): Boolean {
        // Remove port if present
        val hostWithoutPort = host.substringBefore(':')

        if (hostWithoutPort.length > 15) return false // IPv4 max: 255.255.255.255 = 15 chars

        val match = IPV4_PATTERN.matchEntire(hostWithoutPort) ?: return false

        // SECURITY: Validate each octet is 0-255
        return try {
            match.groupValues.drop(1).all { octet ->
                val value = octet.toInt()
                value in 0..255
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if host is an IPv6 address.
     *
     * Handles:
     * - Bracketed notation: [2001:db8::1]
     * - Full format: 2001:0db8:0000:0000:0000:0000:0000:0001
     * - Abbreviated: 2001:db8::1
     * - With zone ID: fe80::1%eth0
     */
    fun isIpv6Address(host: String): Boolean {
        // Security: Length check
        if (host.length > 45) return false // Max IPv6 with brackets: 45 chars

        // Handle bracketed notation
        val ipv6 = when {
            host.startsWith("[") && host.contains("]") -> {
                host.substringAfter("[").substringBefore("]")
            }
            else -> host
        }

        // Remove zone ID if present
        val cleanIpv6 = ipv6.substringBefore('%')

        // Empty or too short to be valid IPv6
        if (cleanIpv6.length < 2) return false

        // Check for :: (zero abbreviation)
        val doubleColonCount = "::".toRegex().findAll(cleanIpv6).count()
        if (doubleColonCount > 1) return false

        // Split by : and validate each segment
        val segments = if ("::" in cleanIpv6) {
            // Handle abbreviated format
            val parts = cleanIpv6.split("::")
            val leftParts = if (parts[0].isNotEmpty()) parts[0].split(":") else emptyList()
            val rightParts = if (parts.size > 1 && parts[1].isNotEmpty()) parts[1].split(":") else emptyList()
            leftParts + rightParts
        } else {
            cleanIpv6.split(":")
        }

        // IPv6 has max 8 segments
        if (segments.size > 8) return false

        // Each segment must be valid hex (1-4 chars)
        return segments.all { segment ->
            segment.isEmpty() || (
                segment.length in 1..4 &&
                segment.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
            )
        }
    }

    /**
     * Check if URL uses a known shortener service.
     */
    fun isShortener(host: String): Boolean {
        val hostLower = host.lowercase()
        return SHORTENER_DOMAINS.any { shortener ->
            hostLower == shortener || hostLower.endsWith(".$shortener")
        }
    }

    /**
     * Calculate Shannon entropy of a string.
     *
     * Higher entropy suggests randomized/generated strings.
     * Formula: H(X) = -Σ p(x) * log2(p(x))
     *
     * @param text Input string to analyze
     * @return Entropy value (0.0 to ~4.7 for ASCII)
     */
    fun calculateEntropy(text: String): Double {
        // SECURITY: Limit input to prevent CPU exhaustion
        val bounded = text.take(256)
        if (bounded.isEmpty()) return 0.0

        val frequencies = bounded.groupingBy { it }.eachCount()
        val length = bounded.length.toDouble()

        return frequencies.values.sumOf { count ->
            val probability = count / length
            if (probability > 0) {
                -probability * kotlin.math.log2(probability)
            } else 0.0
        }
    }

    /**
     * Count suspicious keywords in path.
     *
     * @param path URL path component
     * @return Number of suspicious keywords found
     */
    fun countSuspiciousPathKeywords(path: String): Int {
        // SECURITY: Limit path length to analyze
        val pathLower = path.take(512).lowercase()
        return SUSPICIOUS_KEYWORDS.count { keyword ->
            pathLower.contains(keyword)
        }
    }

    /**
     * Check for credential-related query parameters.
     *
     * @param query URL query string
     * @return true if credential parameters detected
     */
    fun hasCredentialParams(query: String?): Boolean {
        if (query.isNullOrBlank()) return false

        // SECURITY: Limit query length to analyze
        val queryLower = query.take(1024).lowercase()
        return CREDENTIAL_PARAMS.any { param ->
            // Check for param= or &param= patterns
            queryLower.contains("$param=") || queryLower.contains("&$param=")
        }
    }

    /**
     * Normalize URL by removing whitespace and dangerous characters.
     */
    private fun normalizeUrl(url: String): String {
        return url
            .trim()
            .replace(" ", "%20")
            .replace("\t", "")
            .replace("\n", "")
            .replace("\r", "")
    }

    /**
     * Parse host and port from host:port string.
     *
     * @return Pair of (host, port) where port may be null
     */
    private fun parseHostAndPort(hostWithPort: String): Pair<String, Int?> {
        // SECURITY: Handle IPv6 addresses in brackets
        if (hostWithPort.startsWith("[")) {
            val closeBracket = hostWithPort.indexOf(']')
            if (closeBracket > 0) {
                val host = hostWithPort.substring(1, closeBracket)
                val afterBracket = hostWithPort.substring(closeBracket + 1)
                val port = if (afterBracket.startsWith(":")) {
                    afterBracket.substring(1).toIntOrNull()?.takeIf { it in 1..65535 }
                } else null
                return host to port
            }
        }

        val colonIndex = hostWithPort.lastIndexOf(':')
        return if (colonIndex > 0 && colonIndex < hostWithPort.length - 1) {
            val portStr = hostWithPort.substring(colonIndex + 1)
            val port = portStr.toIntOrNull()?.takeIf { it in 1..65535 }
            if (port != null) {
                hostWithPort.substring(0, colonIndex) to port
            } else {
                hostWithPort to null
            }
        } else {
            hostWithPort to null
        }
    }

    /**
     * Parse path, query, and fragment from URL remainder.
     */
    private fun parsePathQueryFragment(rest: String): Triple<String, String?, String?> {
        // SECURITY: Limit total length
        val bounded = rest.take(1024)
        var remaining = bounded

        // Extract fragment first (after #)
        val fragmentIndex = remaining.indexOf('#')
        val fragment = if (fragmentIndex >= 0) {
            val f = remaining.substring(fragmentIndex + 1)
            remaining = remaining.substring(0, fragmentIndex)
            f.ifEmpty { null }
        } else null

        // Extract query (after ?)
        val queryIndex = remaining.indexOf('?')
        val query = if (queryIndex >= 0) {
            val q = remaining.substring(queryIndex + 1)
            remaining = remaining.substring(0, queryIndex)
            q.ifEmpty { null }
        } else null

        val path = remaining.ifEmpty { "/" }

        return Triple(path, query, fragment)
    }
}
