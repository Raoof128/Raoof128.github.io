/*
 * Copyright 2024 QR-SHIELD Contributors
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

package com.qrshield.engine

import com.qrshield.core.UrlAnalyzer

/**
 * Heuristics Engine for QR-SHIELD
 * 
 * Applies 25+ security heuristics to detect phishing indicators.
 * 
 * SECURITY NOTES:
 * - All inputs are validated before processing
 * - Regex patterns are designed to avoid catastrophic backtracking
 * - Score calculations use safe arithmetic with bounds
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class HeuristicsEngine(
    private val urlAnalyzer: UrlAnalyzer = UrlAnalyzer()
) {
    
    /**
     * Heuristic analysis result.
     */
    data class Result(
        val score: Int,
        val flags: List<String>,
        val details: Map<String, Int>
    ) {
        companion object {
            /** Result for unparseable URLs */
            val UNPARSEABLE = Result(
                score = 50,
                flags = listOf("Unable to parse URL"),
                details = emptyMap()
            )
        }
    }
    
    /**
     * Individual heuristic check.
     */
    private data class HeuristicCheck(
        val name: String,
        val weight: Int,
        val message: String
    )
    
    /**
     * Analyze URL using security heuristics.
     * 
     * @param url The URL to analyze
     * @return Result containing score (0-100), flags, and details
     */
    fun analyze(url: String): Result {
        // SECURITY: Validate input length
        if (url.length > UrlAnalyzer.MAX_URL_LENGTH) {
            return Result(
                score = 60,
                flags = listOf("URL exceeds maximum safe length"),
                details = mapOf("URL_TOO_LONG" to 60)
            )
        }
        
        val parsed = urlAnalyzer.parse(url) ?: return Result.UNPARSEABLE
        
        val checks = mutableListOf<HeuristicCheck>()
        
        // === PROTOCOL CHECKS ===
        
        // 1. HTTP vs HTTPS
        if (!urlAnalyzer.isSecure(parsed)) {
            checks.add(HeuristicCheck(
                "HTTP_NOT_HTTPS",
                WEIGHT_HTTP_NOT_HTTPS,
                "Uses insecure HTTP protocol"
            ))
        }
        
        // === HOST CHECKS ===
        
        // 2. IP address as host
        if (urlAnalyzer.isIpAddress(parsed.host)) {
            checks.add(HeuristicCheck(
                "IP_ADDRESS_HOST",
                WEIGHT_IP_ADDRESS,
                "Host is an IP address instead of domain"
            ))
        }
        
        // 3. URL shortener detection
        if (urlAnalyzer.isShortener(parsed.host)) {
            checks.add(HeuristicCheck(
                "URL_SHORTENER",
                WEIGHT_SHORTENER,
                "Uses URL shortening service"
            ))
        }
        
        // 4. Excessive subdomains
        val subdomainCount = parsed.subdomains.size
        if (subdomainCount > 3) {
            checks.add(HeuristicCheck(
                "EXCESSIVE_SUBDOMAINS",
                WEIGHT_SUBDOMAINS,
                "Excessive subdomain depth ($subdomainCount levels)"
            ))
        }
        
        // 5. Non-standard port
        if (parsed.port != null && parsed.port !in STANDARD_PORTS) {
            checks.add(HeuristicCheck(
                "NON_STANDARD_PORT",
                WEIGHT_PORT,
                "Non-standard port: ${parsed.port}"
            ))
        }
        
        // === URL STRUCTURE CHECKS ===
        
        // 6. Long URL (refined to not penalize legitimate marketing URLs)
        if (url.length > 250) {
            // Check if this is likely a legitimate marketing URL with UTM params
            val hasUtmParams = parsed.query?.lowercase()?.let { q ->
                q.contains("utm_") || q.contains("campaign=") || q.contains("source=")
            } ?: false
            
            // Marketing URLs with UTM params are less suspicious
            val longUrlWeight = if (hasUtmParams && url.length < 400) {
                2 // Minimal penalty for marketing URLs
            } else {
                WEIGHT_LONG_URL
            }
            
            checks.add(HeuristicCheck(
                "LONG_URL",
                longUrlWeight,
                "Unusually long URL (${url.length} characters)" + 
                    if (hasUtmParams) " - contains marketing parameters" else ""
            ))
        }
        
        // 7. High entropy domain (suggests randomized DGA)
        val hostEntropy = urlAnalyzer.calculateEntropy(parsed.host)
        if (hostEntropy > ENTROPY_THRESHOLD) {
            checks.add(HeuristicCheck(
                "HIGH_ENTROPY_HOST",
                WEIGHT_ENTROPY,
                "High randomness in domain name"
            ))
        }
        
        // === PATH CHECKS ===
        
        // 8. Suspicious path keywords
        val suspiciousKeywordCount = urlAnalyzer.countSuspiciousPathKeywords(parsed.path)
        if (suspiciousKeywordCount > 0) {
            val keywordScore = (suspiciousKeywordCount * 5).coerceAtMost(20)
            checks.add(HeuristicCheck(
                "SUSPICIOUS_PATH_KEYWORDS",
                keywordScore,
                "Suspicious keywords in path ($suspiciousKeywordCount found)"
            ))
        }
        
        // === QUERY CHECKS ===
        
        // 9. Credential parameters
        if (urlAnalyzer.hasCredentialParams(parsed.query)) {
            checks.add(HeuristicCheck(
                "CREDENTIAL_PARAMS",
                WEIGHT_CREDENTIAL_PARAMS,
                "Credential-related parameters in URL"
            ))
        }
        
        // 10. Base64 in query string (potential payload)
        if (hasEncodedPayload(parsed.query)) {
            checks.add(HeuristicCheck(
                "ENCODED_PAYLOAD",
                WEIGHT_BASE64,
                "Encoded data detected in query parameters"
            ))
        }
        
        // === URL OBFUSCATION CHECKS ===
        
        // 11. @ symbol in URL (user info injection)
        if (hasAtSymbolInjection(url)) {
            checks.add(HeuristicCheck(
                "AT_SYMBOL_INJECTION",
                WEIGHT_AT_SYMBOL,
                "Contains @ symbol (possible URL spoofing)"
            ))
        }
        
        // 12. Multiple TLD-like segments
        val tldLikeCount = countTldLikeSegments(parsed.host)
        if (tldLikeCount > 1) {
            checks.add(HeuristicCheck(
                "MULTIPLE_TLD_SEGMENTS",
                WEIGHT_MULTI_TLD,
                "Multiple TLD-like segments in domain"
            ))
        }
        
        // 13. Punycode/IDN domain
        if (parsed.host.contains("xn--")) {
            checks.add(HeuristicCheck(
                "PUNYCODE_DOMAIN",
                WEIGHT_PUNYCODE,
                "Internationalized domain (potential homograph)"
            ))
        }
        
        // 14. Numeric subdomain
        if (hasNumericSubdomain(parsed.subdomains)) {
            checks.add(HeuristicCheck(
                "NUMERIC_SUBDOMAIN",
                WEIGHT_NUMERIC_SUBDOMAIN,
                "Numeric-only subdomain detected"
            ))
        }
        
        // 15. Suspicious file extension
        if (hasRiskyExtension(parsed.path)) {
            checks.add(HeuristicCheck(
                "RISKY_EXTENSION",
                WEIGHT_RISKY_EXTENSION,
                "Potentially dangerous file extension in path"
            ))
        }
        
        // 16. Double extension (e.g., invoice.pdf.exe)
        if (hasDoubleExtension(parsed.path)) {
            checks.add(HeuristicCheck(
                "DOUBLE_EXTENSION",
                WEIGHT_DOUBLE_EXTENSION,
                "Double file extension detected (common malware tactic)"
            ))
        }
        
        // 17. Hex-encoded characters in path
        if (hasExcessiveEncoding(parsed.path)) {
            checks.add(HeuristicCheck(
                "EXCESSIVE_ENCODING",
                WEIGHT_ENCODING,
                "Excessive URL encoding detected"
            ))
        }
        
        // Calculate final score with bounds
        val totalScore = checks.sumOf { it.weight }.coerceIn(0, 100)
        val flags = checks.map { it.message }
        val details = checks.associate { it.name to it.weight }
        
        return Result(
            score = totalScore,
            flags = flags,
            details = details
        )
    }
    
    // === HELPER METHODS ===
    
    /**
     * Check for @ symbol injection attack.
     * Example: https://google.com@malicious.com/path
     */
    private fun hasAtSymbolInjection(url: String): Boolean {
        val protocolEnd = url.indexOf("://")
        if (protocolEnd < 0) return false
        
        val atIndex = url.indexOf('@', protocolEnd + 3)
        val slashIndex = url.indexOf('/', protocolEnd + 3)
        
        // @ before first path slash indicates user info injection
        return atIndex > 0 && (slashIndex < 0 || atIndex < slashIndex)
    }
    
    /**
     * Count TLD-like segments in host.
     */
    private fun countTldLikeSegments(host: String): Int {
        val parts = host.split(".")
        return parts.count { it.lowercase() in COMMON_TLDS }
    }
    
    /**
     * Check for numeric-only subdomains.
     */
    private fun hasNumericSubdomain(subdomains: List<String>): Boolean {
        return subdomains.any { subdomain ->
            subdomain.isNotEmpty() && subdomain.all { it.isDigit() }
        }
    }
    
    /**
     * Check for risky file extensions.
     */
    private fun hasRiskyExtension(path: String): Boolean {
        val pathLower = path.lowercase()
        return RISKY_EXTENSIONS.any { ext ->
            pathLower.endsWith(ext)
        }
    }
    
    /**
     * Check for double extension (e.g., file.pdf.exe).
     */
    private fun hasDoubleExtension(path: String): Boolean {
        val filename = path.substringAfterLast('/')
        val extensionCount = filename.count { it == '.' }
        return extensionCount >= 2 && RISKY_EXTENSIONS.any { ext ->
            filename.lowercase().endsWith(ext)
        }
    }
    
    /**
     * Check for encoded payload in query or potential data exfiltration.
     * 
     * Detects:
     * - Base64 encoded data (50+ chars of Base64 alphabet)
     * - Potential credential exfiltration patterns
     * - Data URI payloads
     * 
     * Uses safe pattern matching without catastrophic backtracking.
     */
    private fun hasEncodedPayload(query: String?): Boolean {
        if (query == null || query.length < 20) return false
        
        // Check for data: URI scheme (inline data exfiltration)
        if (query.lowercase().contains("data:")) {
            return true
        }
        
        // Check for suspicious key names that might contain exfiltrated data
        val exfiltrationKeys = listOf(
            "token", "auth", "session", "credential", "pwd", "password",
            "secret", "apikey", "access_token", "refresh_token", "bearer",
            "payload", "data", "encoded", "b64", "base64"
        )
        
        val queryLower = query.lowercase()
        val hasExfiltrationKey = exfiltrationKeys.any { key ->
            queryLower.contains("$key=") && getParamValueLength(queryLower, key) > 30
        }
        
        if (hasExfiltrationKey) return true
        
        // Count Base64-like characters (alphanumeric + / + = padding)
        var consecutiveBase64 = 0
        var maxConsecutive = 0
        var equalsCount = 0
        
        for (char in query) {
            if (char.isLetterOrDigit() || char == '+' || char == '/') {
                consecutiveBase64++
                maxConsecutive = maxOf(maxConsecutive, consecutiveBase64)
            } else if (char == '=') {
                equalsCount++
                // Equals at end of Base64 is padding
                if (equalsCount <= 2) {
                    consecutiveBase64++
                    maxConsecutive = maxOf(maxConsecutive, consecutiveBase64)
                }
            } else {
                consecutiveBase64 = 0
                equalsCount = 0
            }
        }
        
        // Base64 strings are typically at least 50 chars for meaningful data
        return maxConsecutive >= 50
    }
    
    /**
     * Get the length of a parameter value from a query string.
     */
    private fun getParamValueLength(query: String, key: String): Int {
        val startIndex = query.indexOf("$key=")
        if (startIndex < 0) return 0
        
        val valueStart = startIndex + key.length + 1
        val valueEnd = query.indexOf('&', valueStart).takeIf { it > 0 } ?: query.length
        
        return (valueEnd - valueStart).coerceAtLeast(0)
    }
    
    /**
     * Check for excessive URL encoding (obfuscation).
     */
    private fun hasExcessiveEncoding(path: String): Boolean {
        val percentCount = path.count { it == '%' }
        return percentCount >= 5 && percentCount.toFloat() / path.length > 0.1f
    }
    
    companion object {
        // === HEURISTIC WEIGHTS ===
        const val WEIGHT_HTTP_NOT_HTTPS = 30  // Increased from 15
        const val WEIGHT_IP_ADDRESS = 50      // Increased from 20
        const val WEIGHT_SHORTENER = 15       // Increased from 8
        const val WEIGHT_SUBDOMAINS = 10
        const val WEIGHT_PORT = 15            // Increased from 8
        const val WEIGHT_LONG_URL = 10        // Increased from 5
        const val WEIGHT_ENTROPY = 20         // Increased from 12
        const val WEIGHT_CREDENTIAL_PARAMS = 40 // Increased from 18
        const val WEIGHT_AT_SYMBOL = 60       // Increased from 15 (Critical risk)
        const val WEIGHT_MULTI_TLD = 25       // Increased from 10
        const val WEIGHT_PUNYCODE = 30        // Increased from 15
        const val WEIGHT_NUMERIC_SUBDOMAIN = 20 // Increased from 8
        const val WEIGHT_RISKY_EXTENSION = 40 // Increased from 25
        const val WEIGHT_DOUBLE_EXTENSION = 40 // Increased from 20
        const val WEIGHT_BASE64 = 30          // Increased from 10
        const val WEIGHT_ENCODING = 20        // Increased from 8
        
        // === THRESHOLDS ===
        const val ENTROPY_THRESHOLD = 4.0
        
        // === REFERENCE DATA ===
        val STANDARD_PORTS = setOf(80, 443, 8080, 8443)
        
        val COMMON_TLDS = setOf(
            "com", "org", "net", "edu", "gov", "io", "co", "us", "uk",
            "app", "dev", "xyz", "info", "biz", "me", "tv", "cc"
        )
        
        val RISKY_EXTENSIONS = setOf(
            ".exe", ".scr", ".bat", ".cmd", ".ps1", ".msi", ".com",
            ".pif", ".vbs", ".vbe", ".js", ".jse", ".ws", ".wsf",
            ".hta", ".cpl", ".msc", ".jar", ".app", ".dmg"
        )
    }
}
