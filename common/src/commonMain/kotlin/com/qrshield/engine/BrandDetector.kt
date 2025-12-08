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

/**
 * Brand Impersonation Detector for QR-SHIELD
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
 * @author QR-SHIELD Security Team
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
        
        // === BRAND DATABASE ===
        
        /**
         * Comprehensive brand database including:
         * - Global tech companies
         * - Financial institutions
         * - Australian banks (for AU compliance)
         * - Social media platforms
         * - E-commerce platforms
         */
        val BRAND_DATABASE: Map<String, BrandConfig> = buildBrandDatabase()
        
        private fun buildBrandDatabase(): Map<String, BrandConfig> = mapOf(
            
            // === FINANCIAL - GLOBAL ===
            
            "paypal" to BrandConfig(
                officialDomains = setOf("paypal.com", "paypal.me"),
                typosquats = listOf("paypa1", "paypai", "paypol", "paypaI", "paypall", "pypai"),
                homographs = listOf("pаypal", "раypal"), // Cyrillic 'а'
                combosquats = listOf("paypal-secure", "paypal-login", "paypal-verify", "paypal-update", "paypal-support"),
                category = BrandCategory.FINANCIAL
            ),
            
            "stripe" to BrandConfig(
                officialDomains = setOf("stripe.com"),
                typosquats = listOf("str1pe", "striipe", "strpe"),
                homographs = listOf("strіpe"), // Cyrillic 'і'
                combosquats = listOf("stripe-payment", "stripe-login", "stripe-verify"),
                category = BrandCategory.FINANCIAL
            ),
            
            // === FINANCIAL - AUSTRALIAN BANKS ===
            
            "commbank" to BrandConfig(
                officialDomains = setOf("commbank.com.au", "commonwealth.bank"),
                typosquats = listOf("cornmbank", "commbenk", "comnbank", "c0mmbank", "combank"),
                homographs = listOf("соmmbank"), // Cyrillic 'о'
                combosquats = listOf("commbank-login", "commbank-secure", "commbank-netbank", "commbank-verify"),
                category = BrandCategory.FINANCIAL
            ),
            
            "nab" to BrandConfig(
                officialDomains = setOf("nab.com.au"),
                typosquats = listOf("n4b", "naab", "nnab"),
                homographs = listOf("nаb"), // Cyrillic 'а'
                combosquats = listOf("nab-login", "nab-secure", "nab-internet-banking"),
                category = BrandCategory.FINANCIAL
            ),
            
            "westpac" to BrandConfig(
                officialDomains = setOf("westpac.com.au"),
                typosquats = listOf("westpec", "westpacc", "w3stpac", "wetspac"),
                homographs = listOf("wеstpac"), // Cyrillic 'е'
                combosquats = listOf("westpac-login", "westpac-secure", "westpac-online"),
                category = BrandCategory.FINANCIAL
            ),
            
            "anz" to BrandConfig(
                officialDomains = setOf("anz.com.au", "anz.com"),
                typosquats = listOf("4nz", "annz", "anzz"),
                homographs = listOf("аnz"), // Cyrillic 'а'
                combosquats = listOf("anz-login", "anz-secure", "anz-internet-banking"),
                category = BrandCategory.FINANCIAL
            ),
            
            "bendigo" to BrandConfig(
                officialDomains = setOf("bendigobank.com.au"),
                typosquats = listOf("bendlgo", "bendig0", "bendiqo"),
                homographs = listOf("bеndigo"), // Cyrillic 'е'
                combosquats = listOf("bendigo-login", "bendigo-bank-login"),
                category = BrandCategory.FINANCIAL
            ),
            
            // === TECH GIANTS ===
            
            "google" to BrandConfig(
                officialDomains = setOf("google.com", "google.co.uk", "google.com.au", "gmail.com", "youtube.com"),
                typosquats = listOf("g00gle", "googie", "goog1e", "gooogle", "goggle", "gogle"),
                homographs = listOf("gооgle", "googlе"), // Cyrillic 'о', 'е'
                combosquats = listOf("google-login", "google-verify", "google-account", "google-security", "google-alert"),
                category = BrandCategory.TECHNOLOGY
            ),
            
            "microsoft" to BrandConfig(
                officialDomains = setOf("microsoft.com", "live.com", "outlook.com", "office.com", "azure.com"),
                typosquats = listOf("micr0soft", "rnicrosoft", "mircosoft", "microsft", "microsofl"),
                homographs = listOf("mісrosoft", "miсrosoft"), // Cyrillic 'і', 'с'
                combosquats = listOf("microsoft-login", "microsoft-account", "office-login", "microsoft-security", "office365-login"),
                category = BrandCategory.TECHNOLOGY
            ),
            
            "apple" to BrandConfig(
                officialDomains = setOf("apple.com", "icloud.com", "apple.co"),
                typosquats = listOf("app1e", "appie", "aple", "applle", "applе"),
                homographs = listOf("аpple", "apрle"), // Cyrillic 'а', 'р'
                combosquats = listOf("apple-id", "icloud-login", "apple-verify", "apple-support", "appleid-login"),
                category = BrandCategory.TECHNOLOGY
            ),
            
            "amazon" to BrandConfig(
                officialDomains = setOf("amazon.com", "amazon.co.uk", "amazon.com.au", "aws.amazon.com", "amzn.com"),
                typosquats = listOf("amaz0n", "arnazon", "amazom", "amazonn", "amazn"),
                homographs = listOf("аmazon", "amаzon"), // Cyrillic 'а'
                combosquats = listOf("amazon-prime", "amazon-order", "amazon-delivery", "amazon-login", "amazon-security"),
                category = BrandCategory.ECOMMERCE
            ),
            
            // === SOCIAL MEDIA ===
            
            "facebook" to BrandConfig(
                officialDomains = setOf("facebook.com", "fb.com", "meta.com", "messenger.com"),
                typosquats = listOf("faceb00k", "facebok", "faceboook", "facebk", "facbook"),
                homographs = listOf("fасebook", "facebооk"), // Cyrillic 'а', 'о'
                combosquats = listOf("facebook-login", "facebook-verify", "fb-security", "facebook-support"),
                category = BrandCategory.SOCIAL
            ),
            
            "instagram" to BrandConfig(
                officialDomains = setOf("instagram.com"),
                typosquats = listOf("1nstagram", "instagran", "lnstagram", "instaqram", "instagrom"),
                homographs = listOf("іnstagram", "instаgram"), // Cyrillic 'і', 'а'
                combosquats = listOf("instagram-verify", "instagram-login", "instagram-support"),
                category = BrandCategory.SOCIAL
            ),
            
            "twitter" to BrandConfig(
                officialDomains = setOf("twitter.com", "x.com"),
                typosquats = listOf("twltter", "tw1tter", "twiiter", "tvvitter", "twiter"),
                homographs = listOf("twіtter", "tωitter"), // Cyrillic 'і', Greek 'ω'
                combosquats = listOf("twitter-verify", "twitter-login", "twitter-support"),
                category = BrandCategory.SOCIAL
            ),
            
            "linkedin" to BrandConfig(
                officialDomains = setOf("linkedin.com"),
                typosquats = listOf("1inkedin", "linkedln", "linkdin", "linkedn", "llnkedin"),
                homographs = listOf("lіnkedin", "linkеdin"), // Cyrillic 'і', 'е'
                combosquats = listOf("linkedin-login", "linkedin-verify", "linkedin-job"),
                category = BrandCategory.SOCIAL
            ),
            
            "tiktok" to BrandConfig(
                officialDomains = setOf("tiktok.com"),
                typosquats = listOf("tikt0k", "tikttok", "tiktop", "tlktok"),
                homographs = listOf("tіktok"), // Cyrillic 'і'
                combosquats = listOf("tiktok-verify", "tiktok-login"),
                category = BrandCategory.SOCIAL
            ),
            
            // === STREAMING ===
            
            "netflix" to BrandConfig(
                officialDomains = setOf("netflix.com"),
                typosquats = listOf("netf1ix", "netfiix", "nettflix", "netfllx", "netlfix"),
                homographs = listOf("nеtflix", "netflіx"), // Cyrillic 'е', 'і'
                combosquats = listOf("netflix-billing", "netflix-update", "netflix-account", "netflix-payment"),
                category = BrandCategory.ENTERTAINMENT
            ),
            
            "spotify" to BrandConfig(
                officialDomains = setOf("spotify.com"),
                typosquats = listOf("spot1fy", "spotlfy", "spoitfy", "spotfy"),
                homographs = listOf("spоtify"), // Cyrillic 'о'
                combosquats = listOf("spotify-login", "spotify-premium", "spotify-verify"),
                category = BrandCategory.ENTERTAINMENT
            ),
            
            // === DELIVERY/LOGISTICS ===
            
            "auspost" to BrandConfig(
                officialDomains = setOf("auspost.com.au"),
                typosquats = listOf("ausp0st", "auspostt", "aspost", "austpost"),
                homographs = listOf("аuspost"), // Cyrillic 'а'
                combosquats = listOf("auspost-delivery", "auspost-tracking", "auspost-parcel"),
                category = BrandCategory.LOGISTICS
            ),
            
            "dhl" to BrandConfig(
                officialDomains = setOf("dhl.com", "dhl.com.au"),
                typosquats = listOf("dh1", "dhll", "d-hl"),
                homographs = listOf("dһl"), // Cyrillic 'һ'
                combosquats = listOf("dhl-tracking", "dhl-delivery", "dhl-parcel"),
                category = BrandCategory.LOGISTICS
            ),
            
            "fedex" to BrandConfig(
                officialDomains = setOf("fedex.com"),
                typosquats = listOf("fed3x", "fedx", "feddex"),
                homographs = listOf("fеdex"), // Cyrillic 'е'
                combosquats = listOf("fedex-tracking", "fedex-delivery"),
                category = BrandCategory.LOGISTICS
            ),
            
            // === GOVERNMENT (AU) ===
            
            "mygovau" to BrandConfig(
                officialDomains = setOf("my.gov.au", "mygov.gov.au"),
                typosquats = listOf("myg0v", "mygove", "myygov"),
                homographs = listOf("mуgov"), // Cyrillic 'у'
                combosquats = listOf("mygov-login", "mygov-verify", "mygov-au"),
                category = BrandCategory.GOVERNMENT
            ),
            
            "ato" to BrandConfig(
                officialDomains = setOf("ato.gov.au"),
                typosquats = listOf("at0", "attoo", "atoo"),
                homographs = listOf("аto"), // Cyrillic 'а'
                combosquats = listOf("ato-refund", "ato-login", "ato-tax"),
                category = BrandCategory.GOVERNMENT
            )
        )
    }
    
    /**
     * Brand category for classification.
     */
    enum class BrandCategory {
        FINANCIAL,
        TECHNOLOGY,
        SOCIAL,
        ECOMMERCE,
        ENTERTAINMENT,
        LOGISTICS,
        GOVERNMENT
    }
    
    /**
     * Brand configuration with patterns.
     */
    data class BrandConfig(
        val officialDomains: Set<String>,
        val typosquats: List<String>,
        val homographs: List<String>,
        val combosquats: List<String>,
        val category: BrandCategory = BrandCategory.TECHNOLOGY
    )
    
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
        val category: BrandCategory
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
        
        // Check each brand
        for ((brand, config) in BRAND_DATABASE) {
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
        // This reduces space complexity from O(m*n) to O(min(m,n))
        val (shorter, longer) = if (a.length <= b.length) a to b else b to a
        
        // OPTIMIZATION: Early exit if length difference exceeds threshold (3)
        val lengthDiff = longer.length - shorter.length
        if (lengthDiff > 3) return lengthDiff  // Can't be close match
        
        // Single-row DP optimization: O(min(m,n)) space instead of O(m*n)
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
            
            // Swap rows (reuse arrays to avoid allocation)
            val temp = previousRow
            previousRow = currentRow
            currentRow = temp
        }
        
        return previousRow[shorter.length]
    }
    
    /**
     * Safely extract host from URL.
     */
    private fun extractHost(url: String): String {
        val bounded = url.take(MAX_URL_LENGTH)
        
        val withoutProtocol = bounded
            .removePrefix("https://")
            .removePrefix("http://")
        
        val endIndex = withoutProtocol.indexOfFirst { it == '/' || it == '?' || it == '#' || it == ':' }
        
        return when {
            endIndex > 0 -> withoutProtocol.substring(0, endIndex)
            else -> withoutProtocol
        }.take(MAX_HOST_LENGTH)
    }
}
