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
 * URL Parser for QR-SHIELD
 *
 * Low-level URL parsing utilities extracted from UrlAnalyzer.
 * Handles protocol extraction, host parsing, and component splitting.
 *
 * SECURITY NOTES:
 * - All inputs are length-bounded
 * - Safe patterns prevent ReDoS
 * - Null-safe handling throughout
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
object UrlParser {

    /** Maximum allowed URL length */
    const val MAX_URL_LENGTH = 2048

    /** Maximum host length */
    const val MAX_HOST_LENGTH = 255

    /** Maximum subdomain depth to analyze */
    const val MAX_SUBDOMAIN_DEPTH = 10

    /**
     * Parsed URL components.
     */
    data class UrlComponents(
        val protocol: String,
        val host: String,
        val port: Int?,
        val path: String,
        val query: String?,
        val fragment: String?
    )

    /**
     * Parse URL into basic components.
     *
     * @param url Raw URL string
     * @return UrlComponents or null if parsing fails
     */
    fun parseComponents(url: String): UrlComponents? {
        if (url.length > MAX_URL_LENGTH) return null

        val normalized = normalizeUrl(url)
        if (normalized.isEmpty()) return null

        // Extract protocol
        val protocolEnd = normalized.indexOf("://")
        val protocol = when {
            protocolEnd in 1..9 -> normalized.substring(0, protocolEnd).lowercase()
            normalized.startsWith("//") -> "https"
            else -> "http"
        }

        // Only allow http/https
        if (protocol !in listOf("http", "https")) return null

        // Get content after protocol
        val afterProtocol = when {
            protocolEnd > 0 -> normalized.substring(protocolEnd + 3)
            normalized.startsWith("//") -> normalized.substring(2)
            else -> normalized
        }

        if (afterProtocol.isEmpty()) return null

        // Find host end
        val hostEnd = afterProtocol.indexOfFirst { it == '/' || it == '?' || it == '#' }
        val hostWithPort = if (hostEnd > 0) {
            afterProtocol.substring(0, hostEnd)
        } else afterProtocol

        if (hostWithPort.length > MAX_HOST_LENGTH) return null

        // Parse host and port
        val (host, port) = parseHostAndPort(hostWithPort)
        if (host.isBlank()) return null

        // Parse path, query, fragment
        val rest = if (hostEnd > 0) afterProtocol.substring(hostEnd) else ""
        val (path, query, fragment) = parsePathQueryFragment(rest)

        return UrlComponents(
            protocol = protocol,
            host = host.lowercase(),
            port = port,
            path = path,
            query = query,
            fragment = fragment
        )
    }

    /**
     * Extract domain parts from host.
     *
     * @return Triple of (subdomains, domain, tld)
     */
    fun extractDomainParts(host: String): Triple<List<String>, String, String> {
        val parts = host.split(".").take(MAX_SUBDOMAIN_DEPTH)
        val tld = parts.lastOrNull()?.lowercase() ?: ""

        val domain = if (parts.size >= 2) {
            "${parts[parts.size - 2]}.$tld"
        } else {
            host.lowercase()
        }

        val subdomains = if (parts.size > 2) {
            parts.dropLast(2)
        } else {
            emptyList()
        }

        return Triple(subdomains, domain, tld)
    }

    /**
     * Normalize URL by cleaning whitespace and control characters.
     */
    fun normalizeUrl(url: String): String {
        return url
            .trim()
            .replace(" ", "%20")
            .replace("\t", "")
            .replace("\n", "")
            .replace("\r", "")
    }

    /**
     * Parse host:port string.
     */
    fun parseHostAndPort(hostWithPort: String): Pair<String, Int?> {
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
    fun parsePathQueryFragment(rest: String): Triple<String, String?, String?> {
        val bounded = rest.take(1024)
        var remaining = bounded

        // Extract fragment
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

    /**
     * Check if string is a valid IPv4 address.
     */
    fun isIpv4(host: String): Boolean {
        val clean = host.substringBefore(':')
        if (clean.length > 15) return false

        val parts = clean.split('.')
        if (parts.size != 4) return false

        return parts.all { part ->
            part.length in 1..3 &&
            part.all { it.isDigit() } &&
            (part.toIntOrNull() ?: -1) in 0..255
        }
    }

    /**
     * Check if string is a valid IPv6 address.
     */
    fun isIpv6(host: String): Boolean {
        if (host.length > 45) return false

        val ipv6 = when {
            host.startsWith("[") && host.contains("]") ->
                host.substringAfter("[").substringBefore("]")
            else -> host
        }

        val clean = ipv6.substringBefore('%')
        if (clean.length < 2) return false

        val doubleColonCount = "::".toRegex().findAll(clean).count()
        if (doubleColonCount > 1) return false

        val segments = if ("::" in clean) {
            val parts = clean.split("::")
            val left = if (parts[0].isNotEmpty()) parts[0].split(":") else emptyList()
            val right = if (parts.size > 1 && parts[1].isNotEmpty()) parts[1].split(":") else emptyList()
            left + right
        } else {
            clean.split(":")
        }

        if (segments.size > 8) return false

        return segments.all { seg ->
            seg.isEmpty() || (seg.length in 1..4 && seg.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' })
        }
    }
}
