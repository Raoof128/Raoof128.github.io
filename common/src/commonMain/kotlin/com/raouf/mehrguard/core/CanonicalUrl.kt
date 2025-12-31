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

import com.raouf.mehrguard.engine.PublicSuffixList
import com.raouf.mehrguard.security.UnicodeRiskAnalyzer

/**
 * Canonical URL structure with derived security fields.
 *
 * This is the single source of truth for URL parsing in QR-SHIELD.
 * All URL analysis should go through this structure to ensure consistency.
 *
 * ## Design Principles
 * 1. **Immutable**: All fields are read-only after construction
 * 2. **Derived Fields**: Complex fields are computed once at construction
 * 3. **Deterministic**: Same input always produces same output
 * 4. **Snapshot-testable**: Can be compared in golden tests
 *
 * ## Fields
 * - **Core**: scheme, host, port, path, queryParams, fragment
 * - **Derived**: registrableDomain, subdomainDepth, effectiveTld, etc.
 * - **Security**: safeDisplayHost, isPunycode, isIpAddress, unicodeRisk
 *
 * @author QR-SHIELD Security Team
 * @since 1.19.0
 */
data class CanonicalUrl(
    // === CORE URL COMPONENTS ===

    /** Original URL string (normalized) */
    val original: String,

    /** URL scheme (http, https) - lowercase */
    val scheme: String,

    /** Host/domain name - lowercase */
    val host: String,

    /** Port number if specified, null for default ports */
    val port: Int?,

    /** URL path component (starts with /) */
    val path: String,

    /** Parsed query parameters as key-value map */
    val queryParams: Map<String, String>,

    /** Raw query string (for analysis) */
    val rawQuery: String?,

    /** URL fragment (after #) */
    val fragment: String?,

    // === DOMAIN STRUCTURE (derived) ===

    /** Registrable domain (eTLD+1), e.g., "example.com" from "www.sub.example.com" */
    val registrableDomain: String,

    /** Effective TLD from Public Suffix List, e.g., "co.uk", "com" */
    val effectiveTld: String,

    /** Number of subdomain levels (0 means no subdomains) */
    val subdomainDepth: Int,

    /** List of subdomain parts, e.g., ["www", "sub"] from "www.sub.example.com" */
    val subdomains: List<String>,

    // === SECURITY INDICATORS (derived) ===

    /** Whether the host is an IP address (v4 or v6) */
    val isIpAddress: Boolean,

    /** Whether the host contains punycode (xn--) */
    val isPunycode: Boolean,

    /** Safe display form of the host for UI (ASCII or marked up) */
    val safeDisplayHost: String,

    /** Unicode risk analysis result */
    val unicodeRisk: UnicodeRiskAnalyzer.UnicodeRiskResult
) {
    /**
     * Whether the URL uses HTTPS.
     */
    val isSecure: Boolean
        get() = scheme == "https"

    /**
     * Effective port (accounting for defaults).
     */
    val effectivePort: Int
        get() = port ?: when (scheme) {
            "https" -> 443
            "http" -> 80
            else -> 80
        }

    /**
     * Whether this URL uses a non-standard port.
     */
    val hasNonStandardPort: Boolean
        get() = port != null && port !in setOf(80, 443, 8080, 8443)

    /**
     * Full URL reconstructed from components.
     */
    val reconstructed: String
        get() = buildString {
            append(scheme)
            append("://")
            append(host)
            if (port != null && port != 80 && port != 443) {
                append(":")
                append(port)
            }
            append(path)
            if (!rawQuery.isNullOrEmpty()) {
                append("?")
                append(rawQuery)
            }
            if (!fragment.isNullOrEmpty()) {
                append("#")
                append(fragment)
            }
        }

    /**
     * Whether the host has deep subdomain structure (>3 levels).
     */
    val hasDeepSubdomains: Boolean
        get() = subdomainDepth > 3

    /**
     * Check if a brand name appears only in subdomain (not registrable domain).
     */
    fun isBrandOnlyInSubdomain(brandName: String): Boolean {
        val brandLower = brandName.lowercase()
        val inSubdomain = subdomains.any { it.contains(brandLower) }
        val inRegistrable = registrableDomain.lowercase().contains(brandLower)
        return inSubdomain && !inRegistrable
    }

    companion object {
        /**
         * Parse a URL string into CanonicalUrl.
         *
         * @param url The URL to parse
         * @param psl Public Suffix List for eTLD+1 computation
         * @param unicodeAnalyzer Unicode risk analyzer
         * @return CanonicalUrl or null if parsing fails
         */
        fun parse(
            url: String,
            psl: PublicSuffixList = PublicSuffixList.default,
            unicodeAnalyzer: UnicodeRiskAnalyzer = UnicodeRiskAnalyzer.default
        ): CanonicalUrl? {
            // Validate input
            if (url.isBlank() || url.length > MAX_URL_LENGTH) {
                return null
            }

            // Reject dangerous schemes early
            val urlLower = url.lowercase().trim()
            if (urlLower.startsWith("javascript:") || 
                urlLower.startsWith("vbscript:") ||
                urlLower.startsWith("data:")) {
                return null
            }

            return try {
                parseInternal(url.trim(), psl, unicodeAnalyzer)
            } catch (e: Exception) {
                null
            }
        }

        private fun parseInternal(
            url: String,
            psl: PublicSuffixList,
            unicodeAnalyzer: UnicodeRiskAnalyzer
        ): CanonicalUrl? {
            val normalized = normalizeUrl(url)
            if (normalized.isEmpty()) return null

            // Extract scheme
            val schemeEnd = normalized.indexOf("://")
            val scheme = when {
                schemeEnd > 0 && schemeEnd < 10 -> normalized.substring(0, schemeEnd).lowercase()
                normalized.startsWith("//") -> "https"
                else -> "http"
            }

            // Only allow http/https
            if (scheme !in listOf("http", "https")) {
                return null
            }

            // Extract host portion
            val afterScheme = when {
                schemeEnd > 0 -> normalized.substring(schemeEnd + 3)
                normalized.startsWith("//") -> normalized.substring(2)
                else -> normalized
            }

            if (afterScheme.isEmpty()) return null

            // Find where host ends
            val hostEnd = afterScheme.indexOfFirst { it == '/' || it == '?' || it == '#' }
            val hostWithPort = if (hostEnd > 0) {
                afterScheme.substring(0, hostEnd)
            } else afterScheme

            // Validate host length
            if (hostWithPort.length > MAX_HOST_LENGTH || hostWithPort.isEmpty()) {
                return null
            }

            // Parse host and port
            val (host, port) = parseHostAndPort(hostWithPort)
            if (host.isBlank()) return null

            val hostLower = host.lowercase()

            // Parse path, query, fragment
            val rest = if (hostEnd > 0) afterScheme.substring(hostEnd) else ""
            val (path, rawQuery, fragment) = parsePathQueryFragment(rest)

            // Parse query params
            val queryParams = parseQueryParams(rawQuery)

            // Compute derived fields using PSL
            val pslResult = psl.parse(hostLower)
            val registrableDomain = pslResult.registrableDomain
            val effectiveTld = pslResult.effectiveTld
            val subdomains = pslResult.subdomains
            val subdomainDepth = subdomains.size

            // Security analysis
            val isIpAddress = isIpv4(hostLower) || isIpv6(hostLower)
            val isPunycode = hostLower.contains("xn--")
            val unicodeRisk = unicodeAnalyzer.analyze(hostLower)
            val safeDisplayHost = unicodeAnalyzer.getSafeDisplayHost(hostLower)

            return CanonicalUrl(
                original = url.take(MAX_URL_LENGTH),
                scheme = scheme,
                host = hostLower,
                port = port,
                path = path,
                queryParams = queryParams,
                rawQuery = rawQuery?.take(MAX_QUERY_LENGTH),
                fragment = fragment?.take(MAX_FRAGMENT_LENGTH),
                registrableDomain = registrableDomain,
                effectiveTld = effectiveTld,
                subdomainDepth = subdomainDepth,
                subdomains = subdomains,
                isIpAddress = isIpAddress,
                isPunycode = isPunycode,
                safeDisplayHost = safeDisplayHost,
                unicodeRisk = unicodeRisk
            )
        }

        private fun normalizeUrl(url: String): String {
            return url
                .trim()
                .replace("\t", "")
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "%20")
        }

        private fun parseHostAndPort(hostWithPort: String): Pair<String, Int?> {
            // Handle IPv6 in brackets
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

            // Handle IPv4 or domain with port
            val lastColon = hostWithPort.lastIndexOf(':')
            return if (lastColon > 0 && lastColon < hostWithPort.length - 1) {
                val portStr = hostWithPort.substring(lastColon + 1)
                val port = portStr.toIntOrNull()?.takeIf { it in 1..65535 }
                if (port != null) {
                    hostWithPort.substring(0, lastColon) to port
                } else {
                    hostWithPort to null
                }
            } else {
                hostWithPort to null
            }
        }

        private fun parsePathQueryFragment(rest: String): Triple<String, String?, String?> {
            val bounded = rest.take(MAX_PATH_LENGTH)
            var remaining = bounded

            // Extract fragment first
            val fragmentIndex = remaining.indexOf('#')
            val fragment = if (fragmentIndex >= 0) {
                val f = remaining.substring(fragmentIndex + 1)
                remaining = remaining.substring(0, fragmentIndex)
                f.ifEmpty { null }
            } else null

            // Extract query
            val queryIndex = remaining.indexOf('?')
            val query = if (queryIndex >= 0) {
                val q = remaining.substring(queryIndex + 1)
                remaining = remaining.substring(0, queryIndex)
                q.ifEmpty { null }
            } else null

            val path = remaining.ifEmpty { "/" }
            return Triple(path, query, fragment)
        }

        private fun parseQueryParams(rawQuery: String?): Map<String, String> {
            if (rawQuery.isNullOrEmpty()) return emptyMap()

            return rawQuery
                .split("&")
                .take(MAX_QUERY_PARAMS)
                .mapNotNull { param ->
                    val eqIndex = param.indexOf('=')
                    if (eqIndex > 0) {
                        val key = param.substring(0, eqIndex).take(100)
                        val value = param.substring(eqIndex + 1).take(500)
                        key to value
                    } else null
                }
                .toMap()
        }

        private fun isIpv4(host: String): Boolean {
            val parts = host.split(".")
            if (parts.size != 4) return false
            return parts.all { part ->
                val num = part.toIntOrNull()
                num != null && num in 0..255
            }
        }

        private fun isIpv6(host: String): Boolean {
            // Handle bracketed notation
            val cleaned = host.removePrefix("[").removeSuffix("]")
            if (cleaned.length < 2 || cleaned.length > 45) return false
            
            // Check for :: (zero abbreviation)
            val doubleColonCount = "::".toRegex().findAll(cleaned).count()
            if (doubleColonCount > 1) return false

            // Check each segment
            val segments = if ("::" in cleaned) {
                val parts = cleaned.split("::")
                val left = if (parts[0].isNotEmpty()) parts[0].split(":") else emptyList()
                val right = if (parts.size > 1 && parts[1].isNotEmpty()) parts[1].split(":") else emptyList()
                left + right
            } else {
                cleaned.split(":")
            }

            if (segments.size > 8) return false

            return segments.all { seg ->
                seg.isEmpty() || (seg.length in 1..4 && seg.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' })
            }
        }

        // Limits
        private const val MAX_URL_LENGTH = 2048
        private const val MAX_HOST_LENGTH = 255
        private const val MAX_PATH_LENGTH = 1024
        private const val MAX_QUERY_LENGTH = 1024
        private const val MAX_FRAGMENT_LENGTH = 256
        private const val MAX_QUERY_PARAMS = 50
    }
}
