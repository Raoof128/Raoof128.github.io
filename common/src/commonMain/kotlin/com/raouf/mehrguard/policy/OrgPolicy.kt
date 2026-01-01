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

package com.raouf.mehrguard.policy

/**
 * Organization Policy Engine
 *
 * Allows enterprises and organizations to define custom security policies
 * that are enforced across all platforms (Android, iOS, Desktop, Web).
 *
 * ## Key Features
 * - Allowlisted domains that bypass scanning
 * - Blocklisted domains that always fail
 * - TLD restrictions (e.g., block all .tk, .ml domains)
 * - Brand allowlist/blocklist
 * - Custom risk thresholds per organization
 * - Category-based blocking (crypto, gambling, adult, etc.)
 *
 * ## Usage
 * ```kotlin
 * // Load from JSON
 * val policy = OrgPolicy.fromJson(jsonString)
 *
 * // Check if URL passes policy
 * val result = policy.evaluate("https://example.com")
 * when (result) {
 *     is PolicyResult.Allowed -> // proceed normally
 *     is PolicyResult.Blocked -> // show blocked message
 *     is PolicyResult.RequiresReview -> // flag for manual review
 * }
 * ```
 *
 * ## JSON Policy Format
 * ```json
 * {
 *   "version": "1.0",
 *   "orgId": "company-xyz",
 *   "strictMode": true,
 *   "allowedDomains": ["company.com", "*.company.com"],
 *   "blockedDomains": ["malware-site.com"],
 *   "blockedTlds": ["tk", "ml", "ga", "cf", "gq"],
 *   "blockedCategories": ["crypto", "gambling"],
 *   "allowedBrands": ["company", "partner"],
 *   "customThresholds": {
 *     "safeThreshold": 5,
 *     "suspiciousThreshold": 30
 *   }
 * }
 * ```
 *
 * @author Mehr Guard Security Team
 * @since 1.2.0
 */
data class OrgPolicy(
    /**
     * Policy version for compatibility checking.
     */
    val version: String = "1.0",
    
    /**
     * Organization identifier for audit logging.
     */
    val orgId: String = "default",
    
    /**
     * Organization display name.
     */
    val orgName: String = "Default Organization",
    
    /**
     * Enable strict mode - more aggressive blocking.
     */
    val strictMode: Boolean = false,
    
    /**
     * Domains that bypass all scanning (e.g., internal sites).
     * Supports wildcard patterns: "*.company.com"
     */
    val allowedDomains: Set<String> = emptySet(),
    
    /**
     * Domains that are always blocked regardless of scan results.
     * Supports wildcard patterns.
     */
    val blockedDomains: Set<String> = emptySet(),
    
    /**
     * TLDs that are always blocked (e.g., high-risk TLDs).
     */
    val blockedTlds: Set<String> = emptySet(),
    
    /**
     * Categories of sites to block.
     * Options: "crypto", "gambling", "adult", "social", "shopping", "streaming"
     */
    val blockedCategories: Set<String> = emptySet(),
    
    /**
     * Brand names that are allowed (organization's own brands).
     * URLs impersonating these are auto-blocked unless from allowed domains.
     */
    val allowedBrands: Set<String> = emptySet(),
    
    /**
     * Brands to always flag regardless of domain.
     */
    val sensitivebrands: Set<String> = emptySet(),
    
    /**
     * Custom safe threshold (URLs at or below this are SAFE).
     * Overrides global setting.
     */
    val safeThreshold: Int? = null,
    
    /**
     * Custom suspicious threshold (URLs at or above this are MALICIOUS).
     * Overrides global setting.
     */
    val suspiciousThreshold: Int? = null,
    
    /**
     * Enable QR payload type restrictions.
     */
    val allowedPayloadTypes: Set<QrPayloadType> = QrPayloadType.entries.toSet(),
    
    /**
     * Maximum URL length allowed (0 = no limit).
     */
    val maxUrlLength: Int = 0,
    
    /**
     * Require HTTPS for all URLs.
     */
    val requireHttps: Boolean = false,
    
    /**
     * Block URLs with IP addresses instead of domains.
     */
    val blockIpAddresses: Boolean = false,
    
    /**
     * Block shortened URLs (bit.ly, t.co, etc.).
     */
    val blockShorteners: Boolean = false,
    
    /**
     * Custom blocked URL patterns (regex).
     */
    val blockedPatterns: List<String> = emptyList(),
    
    /**
     * Custom allowed URL patterns (regex) - bypass normal checks.
     */
    val allowedPatterns: List<String> = emptyList(),
    
    /**
     * Policy expiration timestamp (ISO 8601).
     * Expired policies should trigger re-fetch.
     */
    val expiresAt: String? = null,
    
    /**
     * Contact email for policy issues.
     */
    val contactEmail: String? = null
) {
    
    /**
     * Evaluate a URL against this policy.
     *
     * @param url Full URL to evaluate
     * @return PolicyResult indicating allow/block/review status
     */
    fun evaluate(url: String): PolicyResult {
        val normalizedUrl = url.lowercase().trim()
        
        // Extract domain and TLD
        val domain = extractDomain(normalizedUrl)
        val tld = domain?.substringAfterLast('.', "")
        
        // 1. Check allowed patterns first (highest priority)
        if (matchesAnyPattern(normalizedUrl, allowedPatterns)) {
            return PolicyResult.Allowed("Matches allowed pattern")
        }
        
        // 2. Check allowed domains (bypass all checks)
        if (domain != null && matchesAllowedDomain(domain)) {
            return PolicyResult.Allowed("Domain in allowlist: $domain")
        }
        
        // 3. Check blocked patterns
        if (matchesAnyPattern(normalizedUrl, blockedPatterns)) {
            return PolicyResult.Blocked("Matches blocked pattern", BlockReason.PATTERN_MATCH)
        }
        
        // 4. Check blocked domains
        if (domain != null && matchesBlockedDomain(domain)) {
            return PolicyResult.Blocked("Domain in blocklist: $domain", BlockReason.DOMAIN_BLOCKED)
        }
        
        // 5. Check blocked TLDs
        if (tld != null && tld in blockedTlds) {
            return PolicyResult.Blocked("TLD blocked by policy: .$tld", BlockReason.TLD_BLOCKED)
        }
        
        // 6. Check HTTPS requirement
        if (requireHttps && !normalizedUrl.startsWith("https://")) {
            return PolicyResult.Blocked("HTTPS required by policy", BlockReason.HTTPS_REQUIRED)
        }
        
        // 7. Check IP address blocking
        if (blockIpAddresses && domain != null && isIpAddress(domain)) {
            return PolicyResult.Blocked("IP addresses blocked by policy", BlockReason.IP_ADDRESS)
        }
        
        // 8. Check URL shorteners
        if (blockShorteners && domain != null && isUrlShortener(domain)) {
            return PolicyResult.Blocked("URL shorteners blocked by policy", BlockReason.SHORTENER)
        }
        
        // 9. Check URL length
        if (maxUrlLength > 0 && normalizedUrl.length > maxUrlLength) {
            return PolicyResult.Blocked(
                "URL exceeds max length: ${normalizedUrl.length} > $maxUrlLength",
                BlockReason.LENGTH_EXCEEDED
            )
        }
        
        // 10. Strict mode catches
        if (strictMode) {
            if (domain != null && hasExcessiveSubdomains(domain)) {
                return PolicyResult.RequiresReview("Excessive subdomains in strict mode")
            }
        }
        
        // URL passed all policy checks
        return PolicyResult.PassedPolicy
    }
    
    /**
     * Evaluate a QR payload against policy.
     *
     * @param payload Raw QR code content
     * @param payloadType Detected payload type
     * @return PolicyResult
     */
    fun evaluatePayload(payload: String, payloadType: QrPayloadType): PolicyResult {
        // Check if payload type is allowed
        if (payloadType !in allowedPayloadTypes) {
            return PolicyResult.Blocked(
                "Payload type not allowed: $payloadType",
                BlockReason.PAYLOAD_TYPE_BLOCKED
            )
        }
        
        // If it's a URL type, also evaluate the URL
        return when (payloadType) {
            QrPayloadType.URL, QrPayloadType.URL_HTTP, QrPayloadType.URL_HTTPS -> evaluate(payload)
            QrPayloadType.SMS -> evaluateSmsPayload(payload)
            QrPayloadType.WIFI -> evaluateWifiPayload(payload)
            QrPayloadType.VCARD -> evaluateVcardPayload(payload)
            else -> PolicyResult.PassedPolicy
        }
    }
    
    /**
     * Check if domain matches any allowed domain (with wildcard support).
     */
    private fun matchesAllowedDomain(domain: String): Boolean {
        return allowedDomains.any { pattern ->
            matchesDomainPattern(domain, pattern)
        }
    }
    
    /**
     * Check if domain matches any blocked domain (with wildcard support).
     */
    private fun matchesBlockedDomain(domain: String): Boolean {
        return blockedDomains.any { pattern ->
            matchesDomainPattern(domain, pattern)
        }
    }
    
    /**
     * Match domain against pattern with wildcard support.
     * Patterns like "*.company.com" match "sub.company.com"
     */
    private fun matchesDomainPattern(domain: String, pattern: String): Boolean {
        if (pattern.startsWith("*.")) {
            val suffix = pattern.substring(1) // ".company.com"
            return domain.endsWith(suffix) || domain == pattern.substring(2)
        }
        return domain == pattern
    }
    
    /**
     * Check if string matches any regex pattern.
     */
    private fun matchesAnyPattern(input: String, patterns: List<String>): Boolean {
        return patterns.any { pattern ->
            try {
                Regex(pattern).containsMatchIn(input)
            } catch (_: Exception) {
                false
            }
        }
    }
    
    /**
     * Extract domain from URL.
     */
    private fun extractDomain(url: String): String? {
        val withoutProtocol = url
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("//")
        
        val hostEnd = withoutProtocol.indexOfFirst { it == '/' || it == '?' || it == '#' || it == ':' }
        return if (hostEnd > 0) {
            withoutProtocol.substring(0, hostEnd)
        } else {
            withoutProtocol.takeIf { it.isNotEmpty() }
        }
    }
    
    /**
     * Check if string is an IP address.
     */
    private fun isIpAddress(host: String): Boolean {
        // IPv4 pattern
        val ipv4Pattern = Regex("""^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$""")
        // IPv6 simplified check
        val isIpv6 = host.startsWith("[") || host.count { it == ':' } >= 2
        return ipv4Pattern.matches(host) || isIpv6
    }
    
    /**
     * Check if domain is a known URL shortener.
     */
    private fun isUrlShortener(domain: String): Boolean {
        val shorteners = setOf(
            "bit.ly", "t.co", "tinyurl.com", "goo.gl", "ow.ly",
            "is.gd", "buff.ly", "rb.gy", "cutt.ly", "bl.ink",
            "short.io", "t.ly", "v.gd", "x.co", "lnkd.in",
            "1url.com", "tiny.cc", "shorte.st", "adf.ly"
        )
        return domain in shorteners || shorteners.any { domain.endsWith(".$it") }
    }
    
    /**
     * Check for excessive subdomains (phishing indicator).
     */
    private fun hasExcessiveSubdomains(domain: String): Boolean {
        return domain.count { it == '.' } > 4
    }
    
    /**
     * Evaluate SMS payload for smishing patterns.
     */
    private fun evaluateSmsPayload(payload: String): PolicyResult {
        // Extract phone number and body
        val phoneMatch = Regex("""sms:([^?]+)""").find(payload)
        val bodyMatch = Regex("""body=(.+)""").find(payload)
        
        val body = bodyMatch?.groupValues?.get(1)?.let { 
            try { 
                decodeUrlComponent(it)
            } catch (_: Exception) { 
                it 
            }
        }
        
        // Check for URLs in SMS body (smishing pattern)
        if (body != null) {
            val urlPattern = Regex("""https?://[^\s]+""")
            urlPattern.findAll(body).forEach { match ->
                val embeddedUrl = match.value
                val urlResult = evaluate(embeddedUrl)
                if (urlResult is PolicyResult.Blocked) {
                    return PolicyResult.Blocked(
                        "SMS contains blocked URL: $embeddedUrl",
                        BlockReason.SMISHING_DETECTED
                    )
                }
            }
        }
        
        return PolicyResult.PassedPolicy
    }
    
    /**
     * Simple URL decode without Java dependencies (multiplatform compatible).
     */
    private fun decodeUrlComponent(encoded: String): String {
        val result = StringBuilder()
        var i = 0
        while (i < encoded.length) {
            when {
                encoded[i] == '%' && i + 2 < encoded.length -> {
                    val hex = encoded.substring(i + 1, i + 3)
                    try {
                        result.append(hex.toInt(16).toChar())
                        i += 3
                    } catch (_: Exception) {
                        result.append(encoded[i])
                        i++
                    }
                }
                encoded[i] == '+' -> {
                    result.append(' ')
                    i++
                }
                else -> {
                    result.append(encoded[i])
                    i++
                }
            }
        }
        return result.toString()
    }
    
    /**
     * Evaluate WiFi payload for security issues.
     */
    private fun evaluateWifiPayload(payload: String): PolicyResult {
        // Check for password exfiltration patterns
        // WiFi format: WIFI:T:WPA;S:NetworkName;P:Password;;
        val typeMatch = Regex("""T:([^;]+)""").find(payload)
        val ssidMatch = Regex("""S:([^;]+)""").find(payload)
        
        val authType = typeMatch?.groupValues?.get(1) ?: ""
        val ssid = ssidMatch?.groupValues?.get(1) ?: ""
        
        // Flag open networks
        if (authType.uppercase() == "NOPASS" || authType.isEmpty()) {
            return PolicyResult.RequiresReview("Open WiFi network detected: $ssid")
        }
        
        // Check for suspicious SSIDs
        val suspiciousSsids = listOf(
            "free", "wifi", "public", "guest", "open", "hotspot"
        )
        if (suspiciousSsids.any { ssid.lowercase().contains(it) }) {
            return PolicyResult.RequiresReview("Potentially malicious WiFi SSID: $ssid")
        }
        
        return PolicyResult.PassedPolicy
    }
    
    /**
     * Evaluate vCard payload for data exfiltration.
     */
    private fun evaluateVcardPayload(payload: String): PolicyResult {
        // Check for URLs in vCard
        val urlPattern = Regex("""URL[;:](.+)""", RegexOption.IGNORE_CASE)
        urlPattern.findAll(payload).forEach { match ->
            val embeddedUrl = match.groupValues[1].trim()
            val urlResult = evaluate(embeddedUrl)
            if (urlResult is PolicyResult.Blocked) {
                return PolicyResult.Blocked(
                    "vCard contains blocked URL",
                    BlockReason.PATTERN_MATCH
                )
            }
        }
        
        return PolicyResult.PassedPolicy
    }
    
    companion object {
        /**
         * Default permissive policy.
         */
        val DEFAULT = OrgPolicy()
        
        /**
         * Enterprise strict policy template.
         */
        val ENTERPRISE_STRICT = OrgPolicy(
            orgId = "enterprise",
            orgName = "Enterprise Policy",
            strictMode = true,
            blockedTlds = setOf("tk", "ml", "ga", "cf", "gq", "top", "xyz", "club", "work", "click"),
            requireHttps = true,
            blockIpAddresses = true,
            blockShorteners = true,
            safeThreshold = 5,
            suspiciousThreshold = 30
        )
        
        /**
         * Financial institution policy template.
         */
        val FINANCIAL = OrgPolicy(
            orgId = "financial",
            orgName = "Financial Services Policy",
            strictMode = true,
            blockedTlds = setOf("tk", "ml", "ga", "cf", "gq", "top", "xyz", "club", "work", "click", "loan", "win"),
            blockedCategories = setOf("crypto", "gambling"),
            requireHttps = true,
            blockIpAddresses = true,
            blockShorteners = true,
            safeThreshold = 3,
            suspiciousThreshold = 25,
            allowedPayloadTypes = setOf(QrPayloadType.URL_HTTPS, QrPayloadType.TEXT, QrPayloadType.PHONE)
        )
        
        /**
         * Parse policy from JSON string.
         */
        fun fromJson(json: String): OrgPolicy {
            // Simple JSON parsing without external dependencies
            val values = parseJsonToMap(json)
            
            return OrgPolicy(
                version = values["version"] as? String ?: DEFAULT.version,
                orgId = values["orgId"] as? String ?: DEFAULT.orgId,
                orgName = values["orgName"] as? String ?: DEFAULT.orgName,
                strictMode = values["strictMode"] as? Boolean ?: DEFAULT.strictMode,
                allowedDomains = parseStringSet(values["allowedDomains"]),
                blockedDomains = parseStringSet(values["blockedDomains"]),
                blockedTlds = parseStringSet(values["blockedTlds"]),
                blockedCategories = parseStringSet(values["blockedCategories"]),
                allowedBrands = parseStringSet(values["allowedBrands"]),
                safeThreshold = (values["safeThreshold"] as? Number)?.toInt(),
                suspiciousThreshold = (values["suspiciousThreshold"] as? Number)?.toInt(),
                requireHttps = values["requireHttps"] as? Boolean ?: DEFAULT.requireHttps,
                blockIpAddresses = values["blockIpAddresses"] as? Boolean ?: DEFAULT.blockIpAddresses,
                blockShorteners = values["blockShorteners"] as? Boolean ?: DEFAULT.blockShorteners,
                maxUrlLength = (values["maxUrlLength"] as? Number)?.toInt() ?: DEFAULT.maxUrlLength,
                contactEmail = values["contactEmail"] as? String
            )
        }
        
        /**
         * Parse JSON string to map (simple implementation).
         */
        private fun parseJsonToMap(json: String): Map<String, Any> {
            val result = mutableMapOf<String, Any>()
            
            // Simple regex-based parsing
            val stringPattern = Regex(""""(\w+)":\s*"([^"]+)"""")
            val boolPattern = Regex(""""(\w+)":\s*(true|false)""")
            val numberPattern = Regex(""""(\w+)":\s*(\d+)""")
            val arrayPattern = Regex(""""(\w+)":\s*\[([^\]]*)]""")
            
            stringPattern.findAll(json).forEach { match ->
                result[match.groupValues[1]] = match.groupValues[2]
            }
            
            boolPattern.findAll(json).forEach { match ->
                result[match.groupValues[1]] = match.groupValues[2] == "true"
            }
            
            numberPattern.findAll(json).forEach { match ->
                result[match.groupValues[1]] = match.groupValues[2].toIntOrNull() ?: 0
            }
            
            arrayPattern.findAll(json).forEach { match ->
                val items = match.groupValues[2]
                    .split(",")
                    .map { it.trim().trim('"') }
                    .filter { it.isNotEmpty() }
                result[match.groupValues[1]] = items
            }
            
            return result
        }
        
        @Suppress("UNCHECKED_CAST")
        private fun parseStringSet(value: Any?): Set<String> {
            return when (value) {
                is List<*> -> (value as List<String>).toSet()
                is Set<*> -> value as Set<String>
                is String -> setOf(value)
                else -> emptySet()
            }
        }
    }
    
    /**
     * Export policy to JSON string.
     */
    fun toJson(): String = buildString {
        appendLine("{")
        appendLine("""  "version": "$version",""")
        appendLine("""  "orgId": "$orgId",""")
        appendLine("""  "orgName": "$orgName",""")
        appendLine("""  "strictMode": $strictMode,""")
        appendLine("""  "allowedDomains": ${allowedDomains.toJsonArray()},""")
        appendLine("""  "blockedDomains": ${blockedDomains.toJsonArray()},""")
        appendLine("""  "blockedTlds": ${blockedTlds.toJsonArray()},""")
        appendLine("""  "blockedCategories": ${blockedCategories.toJsonArray()},""")
        appendLine("""  "requireHttps": $requireHttps,""")
        appendLine("""  "blockIpAddresses": $blockIpAddresses,""")
        appendLine("""  "blockShorteners": $blockShorteners,""")
        appendLine("""  "maxUrlLength": $maxUrlLength""")
        safeThreshold?.let { appendLine(""",  "safeThreshold": $it""") }
        suspiciousThreshold?.let { appendLine(""",  "suspiciousThreshold": $it""") }
        contactEmail?.let { appendLine(""",  "contactEmail": "$it"""") }
        appendLine("}")
    }
    
    private fun Set<String>.toJsonArray(): String {
        return "[${joinToString(", ") { "\"$it\"" }}]"
    }
}

/**
 * Result of policy evaluation.
 */
sealed class PolicyResult {
    /**
     * URL/payload is explicitly allowed by policy.
     */
    data class Allowed(val reason: String) : PolicyResult()
    
    /**
     * URL/payload is blocked by policy.
     */
    data class Blocked(
        val reason: String,
        val blockReason: BlockReason
    ) : PolicyResult()
    
    /**
     * URL/payload requires manual review.
     */
    data class RequiresReview(val reason: String) : PolicyResult()
    
    /**
     * URL/payload passed policy checks, proceed with normal analysis.
     */
    data object PassedPolicy : PolicyResult()
}

/**
 * Reasons why a URL/payload was blocked.
 */
enum class BlockReason {
    DOMAIN_BLOCKED,
    TLD_BLOCKED,
    PATTERN_MATCH,
    HTTPS_REQUIRED,
    IP_ADDRESS,
    SHORTENER,
    LENGTH_EXCEEDED,
    PAYLOAD_TYPE_BLOCKED,
    SMISHING_DETECTED,
    CATEGORY_BLOCKED
}
