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
     * Check if host is an IP address (IPv4 only for performance).
     * 
     * Uses a safe regex pattern that cannot backtrack excessively.
     */
    fun isIpAddress(host: String): Boolean {
        if (host.length > 15) return false // IPv4 max: 255.255.255.255 = 15 chars
        
        val match = IPV4_PATTERN.matchEntire(host) ?: return false
        
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
