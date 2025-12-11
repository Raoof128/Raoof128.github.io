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
 * TLD Risk Scorer for QR-SHIELD
 * 
 * Scores top-level domains based on historical abuse data from
 * threat intelligence feeds (Spamhaus, SURBL, etc.).
 * 
 * SECURITY NOTES:
 * - Input is validated and normalized before processing
 * - TLD lists are immutable and cannot be modified at runtime
 * - Thread-safe for concurrent access
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class TldScorer {
    
    /**
     * TLD scoring result.
     */
    data class TldResult(
        val tld: String,
        val score: Int,
        val isHighRisk: Boolean,
        val riskCategory: RiskCategory
    ) {
        /** Human-readable risk description */
        val riskDescription: String
            get() = when (riskCategory) {
                RiskCategory.SAFE -> "Well-established, low abuse rates"
                RiskCategory.MODERATE -> "Some historical abuse"
                RiskCategory.HIGH_RISK -> "Known for high abuse rates"
                RiskCategory.FREE_TIER -> "Free registration, high abuse"
                RiskCategory.COUNTRY_CODE -> "Country-specific domain"
            }
    }
    
    /**
     * TLD risk categories.
     */
    enum class RiskCategory {
        SAFE,           // Well-established, low abuse (.com, .org, .edu)
        MODERATE,       // Some abuse history (.io, .co, .me)
        HIGH_RISK,      // Known for high abuse rates (.xyz, .icu, .club)
        FREE_TIER,      // Free registration, very high abuse (.tk, .ml, .ga)
        COUNTRY_CODE    // Country-specific, context-dependent (.uk, .de, .au)
    }
    
    /**
     * Score TLD risk level.
     * 
     * @param url The URL to extract and score TLD from
     * @return TldResult with score and risk category
     */
    fun score(url: String): TldResult {
        // SECURITY: Validate input
        if (url.isBlank() || url.length > MAX_URL_LENGTH) {
            return TldResult(
                tld = "",
                score = SCORE_UNKNOWN,
                isHighRisk = false,
                riskCategory = RiskCategory.MODERATE
            )
        }
        
        val tld = extractTld(url)
        
        // Empty TLD - invalid URL
        if (tld.isEmpty()) {
            return TldResult(
                tld = "",
                score = SCORE_UNKNOWN,
                isHighRisk = false,
                riskCategory = RiskCategory.MODERATE
            )
        }
        
        // Check each risk category in order of severity
        return when {
            tld in FREE_HIGH_RISK_TLDS -> TldResult(
                tld = tld,
                score = SCORE_FREE_HIGH_RISK,
                isHighRisk = true,
                riskCategory = RiskCategory.FREE_TIER
            )
            
            tld in ABUSED_TLDS -> TldResult(
                tld = tld,
                score = SCORE_ABUSED,
                isHighRisk = true,
                riskCategory = RiskCategory.HIGH_RISK
            )
            
            tld in MODERATE_RISK_TLDS -> TldResult(
                tld = tld,
                score = SCORE_MODERATE,
                isHighRisk = false,
                riskCategory = RiskCategory.MODERATE
            )
            
            tld in SAFE_TLDS -> TldResult(
                tld = tld,
                score = SCORE_SAFE,
                isHighRisk = false,
                riskCategory = RiskCategory.SAFE
            )
            
            // 2-letter TLDs are likely country codes
            tld.length == 2 -> TldResult(
                tld = tld,
                score = SCORE_COUNTRY,
                isHighRisk = false,
                riskCategory = RiskCategory.COUNTRY_CODE
            )
            
            // Unknown TLD - moderate suspicion
            else -> TldResult(
                tld = tld,
                score = SCORE_UNKNOWN,
                isHighRisk = false,
                riskCategory = RiskCategory.MODERATE
            )
        }
    }
    
    /**
     * Batch score multiple URLs.
     * 
     * @param urls List of URLs to score
     * @return Map of URL to TldResult
     */
    fun scoreBatch(urls: List<String>): Map<String, TldResult> {
        // SECURITY: Limit batch size
        val bounded = urls.take(MAX_BATCH_SIZE)
        return bounded.associateWith { score(it) }
    }
    
    /**
     * Check if a TLD is in the high-risk category.
     * 
     * @param tld The TLD to check (without leading dot)
     * @return true if high risk
     */
    fun isHighRiskTld(tld: String): Boolean {
        val normalized = tld.lowercase().removePrefix(".")
        return normalized in FREE_HIGH_RISK_TLDS || normalized in ABUSED_TLDS
    }
    
    /**
     * Safely extract TLD from URL.
     */
    private fun extractTld(url: String): String {
        // SECURITY: Bound input processing
        val bounded = url.take(MAX_URL_LENGTH)
        
        val host = bounded
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore("#")
            .substringBefore(":")
            .lowercase()
            .take(MAX_HOST_LENGTH)
        
        // Validate host is not empty and contains a dot
        if (host.isEmpty() || '.' !in host) {
            return ""
        }
        
        val parts = host.split(".")
        val tld = parts.lastOrNull()?.trim() ?: ""
        
        // SECURITY: Validate TLD format (letters only, reasonable length)
        if (tld.isEmpty() || tld.length > 20 || !tld.all { it.isLetter() }) {
            return ""
        }
        
        return tld
    }
    
    companion object {
        // === RISK SCORES ===
        
        /** Score for safe, well-established TLDs */
        const val SCORE_SAFE = 0
        
        /** Score for TLDs with moderate abuse history */
        const val SCORE_MODERATE = 35
        
        /** Score for country-code TLDs */
        const val SCORE_COUNTRY = 15
        
        /** Score for historically abused TLDs */
        const val SCORE_ABUSED = 75
        
        /** Score for free/high-risk TLDs */
        const val SCORE_FREE_HIGH_RISK = 90
        
        /** Score for unknown TLDs */
        const val SCORE_UNKNOWN = 30
        
        // === LIMITS ===
        
        private const val MAX_URL_LENGTH = 2048
        private const val MAX_HOST_LENGTH = 255
        private const val MAX_BATCH_SIZE = 100
        
        // === TLD DATABASES ===
        // Sources: Spamhaus, SURBL, APWG, various threat intelligence feeds
        
        /**
         * Free TLDs with very high abuse rates.
         * These domains are often used because they're free to register.
         */
        val FREE_HIGH_RISK_TLDS = setOf(
            // Freenom free domains (suspended but still in use)
            "tk", "ml", "ga", "cf", "gq",
            // Other cheap/free with high abuse
            "buzz", "top", "work", "surf", "monster",
            "ooo", "rest", "bar"
        )
        
        /**
         * TLDs with historically high abuse rates.
         * Not necessarily free, but commonly abused.
         */
        val ABUSED_TLDS = setOf(
            "xyz", "icu", "club", "online", "site", "vip",
            "live", "click", "link", "space", "fun", "host",
            "website", "store", "cam", "quest", "sbs", "beauty",
            "hair", "skin", "makeup", "loan", "loans", "bid",
            "stream", "download", "racing", "win", "review",
            "party", "science", "trade", "date", "faith"
        )
        
        /**
         * TLDs with moderate risk.
         * Legitimate use but also some abuse history.
         */
        val MODERATE_RISK_TLDS = setOf(
            "io", "co", "me", "biz", "info", "cc", "tv",
            "ws", "mobi", "pro", "name", "asia", "in",
            "tech", "cloud", "digital", "media", "studio"
        )
        
        /**
         * Safe, well-established TLDs.
         * Low abuse rates, often have registration requirements.
         */
        val SAFE_TLDS = setOf(
            "com", "org", "net", "edu", "gov", "mil",
            "app", "dev", "page", "new", "google", "amazon",
            "apple", "microsoft", "int", "coop", "museum",
            "aero", "jobs", "travel"
        )
    }
}
