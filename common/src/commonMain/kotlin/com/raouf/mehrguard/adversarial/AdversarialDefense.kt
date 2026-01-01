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

package com.raouf.mehrguard.adversarial

/**
 * Adversarial Robustness Module
 *
 * Defenses against URL obfuscation tricks used by attackers to bypass detection.
 * These techniques are commonly used in real-world phishing attacks.
 *
 * ## Attack Categories
 *
 * ### 1. Mixed Scripts (Homograph Attacks)
 * - Cyrillic 'а' (U+0430) vs Latin 'a' (U+0061)
 * - Greek 'ο' (U+03BF) vs Latin 'o' (U+006F)
 * - Combining marks to create visual confusion
 *
 * ### 2. Percent-Encoding Abuse
 * - Double/triple URL encoding
 * - Mixed case encoding (%2F vs %2f)
 * - Unnecessary encoding of safe characters
 *
 * ### 3. Nested Redirects
 * - URLs embedded in URL parameters
 * - Multiple levels of redirect chains
 * - Parameter injection attacks
 *
 * ### 4. Unicode Normalization Edge Cases
 * - NFC vs NFD normalization differences
 * - Zero-width characters
 * - RTL override characters
 * - Invisible separators
 *
 * ## Usage
 * ```kotlin
 * val normalizer = UrlNormalizer()
 * val cleaned = normalizer.normalize("https://exа̧mple.com") // Uses Cyrillic 'а'
 * val attacks = normalizer.detectObfuscation(original)
 * ```
 *
 * @author Mehr Guard Security Team
 * @since 1.2.0
 */
object AdversarialDefense {
    
    /**
     * Normalize a URL by removing obfuscation techniques.
     *
     * @param url Raw URL to normalize
     * @return NormalizationResult with cleaned URL and detected attacks
     */
    fun normalize(url: String): NormalizationResult {
        val detectedAttacks = mutableListOf<ObfuscationAttack>()
        var normalized = url
        
        // 1. Remove zero-width characters
        val (noZeroWidth, hasZeroWidth) = removeZeroWidthCharacters(normalized)
        if (hasZeroWidth) {
            detectedAttacks.add(ObfuscationAttack.ZERO_WIDTH_CHARACTERS)
            normalized = noZeroWidth
        }
        
        // 2. Remove RTL override characters
        val (noRtl, hasRtl) = removeRtlOverrides(normalized)
        if (hasRtl) {
            detectedAttacks.add(ObfuscationAttack.RTL_OVERRIDE)
            normalized = noRtl
        }
        
        // 3. Detect and normalize percent encoding
        val (decoded, encodingIssues) = normalizePercentEncoding(normalized)
        detectedAttacks.addAll(encodingIssues)
        normalized = decoded
        
        // 4. Detect homograph/mixed script attacks
        val (asciiNormalized, homographs) = detectHomographs(normalized)
        detectedAttacks.addAll(homographs)
        // Don't replace - just detect (could break legitimate i18n URLs)
        
        // 5. Detect nested URLs in parameters
        val nestedUrls = detectNestedUrls(normalized)
        if (nestedUrls.isNotEmpty()) {
            detectedAttacks.add(ObfuscationAttack.NESTED_REDIRECTS)
        }
        
        // 6. Unicode normalization (NFC)
        val nfcNormalized = unicodeNormalize(normalized)
        if (nfcNormalized != normalized) {
            detectedAttacks.add(ObfuscationAttack.UNICODE_NORMALIZATION)
            normalized = nfcNormalized
        }
        
        // 7. Detect punycode domains
        if (containsPunycode(normalized)) {
            detectedAttacks.add(ObfuscationAttack.PUNYCODE_DOMAIN)
        }
        
        // 8. Detect IP address obfuscation
        val (cleanedIp, ipObfuscation) = detectIpObfuscation(normalized)
        detectedAttacks.addAll(ipObfuscation)
        
        return NormalizationResult(
            originalUrl = url,
            normalizedUrl = normalized,
            detectedAttacks = detectedAttacks,
            nestedUrls = nestedUrls,
            riskScore = calculateObfuscationRisk(detectedAttacks)
        )
    }
    
    /**
     * Remove zero-width Unicode characters.
     * These are invisible and used to defeat pattern matching.
     */
    private fun removeZeroWidthCharacters(url: String): Pair<String, Boolean> {
        val zeroWidthChars = listOf(
            '\u200B', // Zero Width Space
            '\u200C', // Zero Width Non-Joiner
            '\u200D', // Zero Width Joiner
            '\u200E', // Left-to-Right Mark
            '\u200F', // Right-to-Left Mark
            '\uFEFF', // Zero Width No-Break Space (BOM)
            '\u00AD', // Soft Hyphen
            '\u034F', // Combining Grapheme Joiner
            '\u2060', // Word Joiner
            '\u2061', // Function Application
            '\u2062', // Invisible Times
            '\u2063', // Invisible Separator
            '\u2064', // Invisible Plus
            '\u180E', // Mongolian Vowel Separator
        )
        
        val hasAny = zeroWidthChars.any { it in url }
        val cleaned = url.filter { it !in zeroWidthChars }
        
        return cleaned to hasAny
    }
    
    /**
     * Remove RTL override characters.
     * Can be used to visually reverse parts of URLs.
     */
    private fun removeRtlOverrides(url: String): Pair<String, Boolean> {
        val rtlChars = listOf(
            '\u202A', // Left-to-Right Embedding
            '\u202B', // Right-to-Left Embedding
            '\u202C', // Pop Directional Formatting
            '\u202D', // Left-to-Right Override
            '\u202E', // Right-to-Left Override (MOST DANGEROUS)
            '\u2066', // Left-to-Right Isolate
            '\u2067', // Right-to-Left Isolate
            '\u2068', // First Strong Isolate
            '\u2069', // Pop Directional Isolate
        )
        
        val hasAny = rtlChars.any { it in url }
        val cleaned = url.filter { it !in rtlChars }
        
        return cleaned to hasAny
    }
    
    /**
     * Normalize percent encoding and detect abuse.
     */
    private fun normalizePercentEncoding(url: String): Pair<String, List<ObfuscationAttack>> {
        val attacks = mutableListOf<ObfuscationAttack>()
        var decoded = url
        var previousDecoded: String
        var iterations = 0
        
        // Detect double/triple encoding by iterating
        do {
            previousDecoded = decoded
            decoded = decodePercentEncodingOnce(decoded)
            iterations++
            
            if (iterations > 1 && decoded != previousDecoded) {
                attacks.add(ObfuscationAttack.DOUBLE_ENCODING)
            }
        } while (decoded != previousDecoded && iterations < 5)
        
        // Detect unnecessary encoding of safe characters
        val unnecessaryEncodingPattern = Regex("""%(?:2[DEF]|3[0-9]|4[1-9A-F]|5[0-9A]|6[1-9A-F]|7[0-9A])""", RegexOption.IGNORE_CASE)
        if (unnecessaryEncodingPattern.containsMatchIn(url)) {
            // Encoded letters/numbers/safe chars
            attacks.add(ObfuscationAttack.UNNECESSARY_ENCODING)
        }
        
        // Detect mixed case encoding (%2F vs %2f)
        val mixedCasePattern = Regex("""(%[0-9A-Fa-f]{2})""")
        val encodings = mixedCasePattern.findAll(url).map { it.value }.toList()
        if (encodings.any { it.uppercase() != it } && encodings.any { it.lowercase() != it }) {
            attacks.add(ObfuscationAttack.MIXED_CASE_ENCODING)
        }
        
        return decoded to attacks
    }
    
    /**
     * Decode percent encoding once (single pass).
     */
    private fun decodePercentEncodingOnce(input: String): String {
        return input.replace(Regex("%([0-9A-Fa-f]{2})")) { match ->
            val code = match.groupValues[1].toInt(16)
            if (code in 0x20..0x7E) { // Printable ASCII
                code.toChar().toString()
            } else {
                match.value // Keep non-printable encoded
            }
        }
    }
    
    /**
     * Detect homograph attacks using mixed scripts.
     */
    private fun detectHomographs(url: String): Pair<String, List<ObfuscationAttack>> {
        val attacks = mutableListOf<ObfuscationAttack>()
        
        // Common confusable characters (Cyrillic/Greek → Latin)
        // Using Unicode escape sequences for clarity and compatibility
        val confusables = mapOf(
            '\u0430' to 'a', // Cyrillic Small Letter A
            '\u0435' to 'e', // Cyrillic Small Letter Ie
            '\u043E' to 'o', // Cyrillic Small Letter O
            '\u0440' to 'p', // Cyrillic Small Letter Er
            '\u0441' to 'c', // Cyrillic Small Letter Es
            '\u0443' to 'y', // Cyrillic Small Letter U
            '\u0445' to 'x', // Cyrillic Small Letter Ha
            '\u0410' to 'A', // Cyrillic Capital A
            '\u0412' to 'B', // Cyrillic Capital Ve
            '\u0415' to 'E', // Cyrillic Capital Ie
            '\u041A' to 'K', // Cyrillic Capital Ka
            '\u041C' to 'M', // Cyrillic Capital Em
            '\u041D' to 'H', // Cyrillic Capital En
            '\u041E' to 'O', // Cyrillic Capital O
            '\u0420' to 'P', // Cyrillic Capital Er
            '\u0421' to 'C', // Cyrillic Capital Es
            '\u0422' to 'T', // Cyrillic Capital Te
            '\u0425' to 'X', // Cyrillic Capital Ha
            '\u03BF' to 'o', // Greek Small Letter Omicron
            '\u03BD' to 'v', // Greek Small Letter Nu
            '\u0391' to 'A', // Greek Capital Alpha
            '\u0392' to 'B', // Greek Capital Beta
            '\u0395' to 'E', // Greek Capital Epsilon
            '\u0397' to 'H', // Greek Capital Eta
            '\u0399' to 'I', // Greek Capital Iota
            '\u039A' to 'K', // Greek Capital Kappa
            '\u039C' to 'M', // Greek Capital Mu
            '\u039D' to 'N', // Greek Capital Nu
            '\u039F' to 'O', // Greek Capital Omicron
            '\u03A1' to 'P', // Greek Capital Rho
            '\u03A4' to 'T', // Greek Capital Tau
            '\u03A7' to 'X', // Greek Capital Chi
            '\u0131' to 'i', // Latin Small Letter Dotless I
            '\u2113' to 'l', // Script Small L
        )
        
        // Check if URL contains any confusable characters
        val hasConfusables = confusables.keys.any { it in url }
        if (hasConfusables) {
            attacks.add(ObfuscationAttack.MIXED_SCRIPTS)
        }
        
        // Check for combining diacritical marks used to modify characters
        val combiningMarks = url.any { it.code in 0x0300..0x036F }
        if (combiningMarks) {
            attacks.add(ObfuscationAttack.COMBINING_MARKS)
        }
        
        // Normalize to ASCII equivalent
        val normalized = url.map { confusables[it] ?: it }.joinToString("")
        
        return normalized to attacks
    }
    
    /**
     * Detect URLs nested in parameters.
     */
    private fun detectNestedUrls(url: String): List<String> {
        val nestedUrls = mutableListOf<String>()
        
        // Look for URL patterns in query parameters
        val patterns = listOf(
            // Standard redirect parameters
            Regex("""[?&](?:url|redirect|next|return|goto|target|link|dest|destination|continue|callback|redir)=([^&]+)""", RegexOption.IGNORE_CASE),
            // Data URI
            Regex("""data:[^,]+,[^&]+"""),
            // JavaScript URI
            Regex("""javascript:[^&]+""", RegexOption.IGNORE_CASE),
            // Embedded http(s) URLs
            Regex("""=https?%3A%2F%2F[^&]+""", RegexOption.IGNORE_CASE),
            Regex("""=https?://[^&]+""", RegexOption.IGNORE_CASE)
        )
        
        patterns.forEach { pattern ->
            pattern.findAll(url).forEach { match ->
                val nestedUrl = if (match.groupValues.size > 1) {
                    decodePercentEncodingOnce(match.groupValues[1])
                } else {
                    match.value
                }
                nestedUrls.add(nestedUrl)
            }
        }
        
        return nestedUrls
    }
    
    /**
     * Unicode NFC normalization (simplified for multiplatform).
     */
    private fun unicodeNormalize(url: String): String {
        // Remove combining marks after normalization check
        return url.filter { it.code !in 0x0300..0x036F || it.code !in 0xFE20..0xFE2F }
    }
    
    /**
     * Check for punycode domains (xn--).
     */
    private fun containsPunycode(url: String): Boolean {
        return url.lowercase().contains("xn--")
    }
    
    /**
     * Detect IP address obfuscation techniques.
     */
    private fun detectIpObfuscation(url: String): Pair<String, List<ObfuscationAttack>> {
        val attacks = mutableListOf<ObfuscationAttack>()
        
        // Extract host portion
        val hostMatch = Regex("""://([^/]+)""").find(url)
        val host = hostMatch?.groupValues?.get(1) ?: return url to attacks
        
        // 1. Decimal IP (e.g., 3232235777 = 192.168.1.1)
        if (host.all { it.isDigit() } && host.length >= 7) {
            attacks.add(ObfuscationAttack.DECIMAL_IP)
        }
        
        // 2. Octal IP (e.g., 0300.0250.0001.0001)
        if (host.contains(".") && host.split(".").all { it.startsWith("0") && it.all { c -> c.isDigit() } }) {
            attacks.add(ObfuscationAttack.OCTAL_IP)
        }
        
        // 3. Hex IP (e.g., 0xC0.0xA8.0x01.0x01)
        if (host.lowercase().contains("0x")) {
            attacks.add(ObfuscationAttack.HEX_IP)
        }
        
        // 4. Mixed notation
        val mixedPattern = Regex("""(\d{1,3}\.){1,3}0x[0-9a-fA-F]+""")
        if (mixedPattern.containsMatchIn(host)) {
            attacks.add(ObfuscationAttack.MIXED_IP_NOTATION)
        }
        
        return url to attacks
    }
    
    /**
     * Calculate risk score based on detected obfuscation.
     */
    private fun calculateObfuscationRisk(attacks: List<ObfuscationAttack>): Int {
        return attacks.sumOf { it.riskScore }.coerceIn(0, 100)
    }
}

/**
 * Result of URL normalization.
 */
data class NormalizationResult(
    val originalUrl: String,
    val normalizedUrl: String,
    val detectedAttacks: List<ObfuscationAttack>,
    val nestedUrls: List<String>,
    val riskScore: Int
) {
    val hasObfuscation: Boolean
        get() = detectedAttacks.isNotEmpty()
    
    val attackSummary: String
        get() = if (detectedAttacks.isEmpty()) {
            "No obfuscation detected"
        } else {
            detectedAttacks.joinToString(", ") { it.displayName }
        }
}

/**
 * Types of URL obfuscation attacks.
 */
enum class ObfuscationAttack(
    val displayName: String,
    val description: String,
    val riskScore: Int
) {
    // Zero-width and invisible characters
    ZERO_WIDTH_CHARACTERS(
        "Zero-Width Characters",
        "Invisible Unicode characters inserted to bypass pattern matching",
        30
    ),
    RTL_OVERRIDE(
        "RTL Override",
        "Right-to-Left text override to visually reverse URL parts",
        40
    ),
    
    // Percent encoding abuse
    DOUBLE_ENCODING(
        "Double Encoding",
        "URL is encoded multiple times to bypass filters",
        35
    ),
    UNNECESSARY_ENCODING(
        "Unnecessary Encoding",
        "Safe characters unnecessarily percent-encoded",
        15
    ),
    MIXED_CASE_ENCODING(
        "Mixed Case Encoding",
        "Inconsistent case in percent-encoded characters",
        10
    ),
    
    // Homograph/Script attacks
    MIXED_SCRIPTS(
        "Mixed Scripts (Homograph)",
        "URL contains lookalike characters from different scripts (Cyrillic, Greek)",
        45
    ),
    COMBINING_MARKS(
        "Combining Marks",
        "Characters modified with combining diacritical marks",
        25
    ),
    PUNYCODE_DOMAIN(
        "Punycode Domain",
        "Internationalized domain name (potential homograph)",
        20
    ),
    
    // Redirect attacks
    NESTED_REDIRECTS(
        "Nested Redirects",
        "URLs embedded in URL parameters (open redirect risk)",
        30
    ),
    
    // Normalization issues
    UNICODE_NORMALIZATION(
        "Unicode Normalization",
        "URL requires Unicode normalization (NFC/NFD difference)",
        15
    ),
    
    // IP obfuscation
    DECIMAL_IP(
        "Decimal IP Address",
        "IP address as single decimal number",
        25
    ),
    OCTAL_IP(
        "Octal IP Address",
        "IP address with octal notation",
        30
    ),
    HEX_IP(
        "Hexadecimal IP Address",
        "IP address with hex notation",
        30
    ),
    MIXED_IP_NOTATION(
        "Mixed IP Notation",
        "IP address with mixed decimal/octal/hex notation",
        35
    )
}
