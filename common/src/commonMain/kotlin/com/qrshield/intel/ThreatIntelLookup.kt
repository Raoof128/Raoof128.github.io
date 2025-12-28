/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.intel

/**
 * Two-stage offline threat intelligence lookup.
 *
 * Stage 1: Bloom filter says "maybe bad" (fast, space-efficient)
 * Stage 2: Exact set confirms (eliminates false positives)
 *
 * ## Design
 * - Bloom filter: ~1% false positive rate, O(k) lookup
 * - Exact set: HashSet for confirmed malicious domains
 * - Combined: Zero false positives, minimal memory
 *
 * @author QR-SHIELD Security Team
 * @since 1.19.0
 */
class ThreatIntelLookup private constructor(
    private val bloomFilter: BloomFilter,
    private val exactSet: Set<String>,
    private val metadata: LookupMetadata
) {
    /**
     * Lookup result with confidence level.
     */
    data class LookupResult(
        val isKnownBad: Boolean,
        val confidence: Confidence,
        val category: ThreatCategory?
    ) {
        enum class Confidence {
            /** Confirmed in exact set */
            CONFIRMED,
            /** Bloom filter positive, not in exact set (false positive) */
            PROBABLE_FALSE_POSITIVE,
            /** Not in bloom filter (definitely clean) */
            CLEAN
        }
    }

    /**
     * Threat categories for known bad entries.
     */
    enum class ThreatCategory {
        PHISHING,
        MALWARE,
        SCAM,
        SPAM,
        CRYPTOJACKING,
        UNKNOWN
    }

    /**
     * Metadata about the intel bundle.
     */
    data class LookupMetadata(
        val version: String,
        val buildTimestamp: Long,
        val entryCount: Int,
        val source: String
    )

    /**
     * Two-stage lookup for a domain/URL.
     */
    fun lookup(input: String): LookupResult {
        val normalized = normalize(input)

        // Stage 1: Bloom filter (fast negative)
        if (!bloomFilter.mightContain(normalized)) {
            return LookupResult(
                isKnownBad = false,
                confidence = LookupResult.Confidence.CLEAN,
                category = null
            )
        }

        // Stage 2: Exact set (eliminates false positives)
        if (normalized in exactSet) {
            return LookupResult(
                isKnownBad = true,
                confidence = LookupResult.Confidence.CONFIRMED,
                category = ThreatCategory.PHISHING // Default category
            )
        }

        // Bloom filter false positive
        return LookupResult(
            isKnownBad = false,
            confidence = LookupResult.Confidence.PROBABLE_FALSE_POSITIVE,
            category = null
        )
    }

    /**
     * Check if a domain is in the deny list.
     */
    fun isDenied(domain: String): Boolean {
        return lookup(domain).isKnownBad
    }

    /**
     * Get metadata about the loaded bundle.
     */
    fun getMetadata(): LookupMetadata = metadata

    /**
     * Get statistics about the filter.
     */
    fun getStats(): Stats = Stats(
        bloomFilterSize = bloomFilter.count(),
        exactSetSize = exactSet.size,
        estimatedFPR = bloomFilter.estimatedFalsePositiveRate()
    )

    data class Stats(
        val bloomFilterSize: Int,
        val exactSetSize: Int,
        val estimatedFPR: Double
    )

    /**
     * Normalize input for consistent lookup.
     */
    private fun normalize(input: String): String {
        return input
            .lowercase()
            .trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("www.")
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore(":")
    }

    companion object {
        /**
         * Create from a list of known bad domains.
         */
        fun create(
            domains: List<String>,
            version: String = "1.0.0",
            source: String = "bundled"
        ): ThreatIntelLookup {
            val normalized = domains.map { it.lowercase().trim() }.distinct()
            
            val bloomFilter = BloomFilter.fromItems(normalized, falsePositiveRate = 0.01)
            val exactSet = normalized.toSet()
            
            val metadata = LookupMetadata(
                version = version,
                buildTimestamp = currentTimeMillis(),
                entryCount = normalized.size,
                source = source
            )

            return ThreatIntelLookup(bloomFilter, exactSet, metadata)
        }

        /**
         * Create with the bundled default deny list.
         */
        fun createDefault(): ThreatIntelLookup {
            return create(
                domains = BUNDLED_DENYLIST,
                version = "2025.12.29",
                source = "qrshield-bundled"
            )
        }

        // Portable time function
        private fun currentTimeMillis(): Long {
            return kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
        }

        /**
         * Bundled deny list of known phishing/malicious domains.
         *
         * Sources: PhishTank, OpenPhish, URLhaus (anonymized patterns)
         * This is a sample - production would have thousands of entries.
         */
        private val BUNDLED_DENYLIST = listOf(
            // Known phishing TLDs (entire TLD is high risk)
            "example-phish.tk",
            "login-verify.ml",
            "secure-update.ga",
            "account-confirm.cf",
            
            // Brand impersonation patterns
            "paypa1-secure.com",
            "amaz0n-verify.com",
            "app1e-id.com",
            "micr0soft-login.com",
            "g00gle-verify.com",
            "faceb00k-security.com",
            "netfl1x-update.com",
            "twitt3r-verify.com",
            
            // Crypto scam patterns
            "binance-airdrop.xyz",
            "coinbase-claim.site",
            "metamask-verify.io",
            "trust-wallet-sync.com",
            
            // Government impersonation
            "irs-refund-claim.com",
            "tax-return-gov.com",
            "social-security-update.com",
            
            // Banking impersonation
            "chase-verify-account.com",
            "wellsfargo-security.net",
            "bankofamerica-alert.com",
            
            // Delivery scam patterns
            "usps-package-delivery.com",
            "fedex-tracking-update.net",
            "ups-delivery-notice.com",
            "dhl-package-claim.com",
            
            // Tech support scam patterns
            "windows-support-center.com",
            "apple-support-help.com",
            "microsoft-alert-center.com",
            
            // Known malware distribution
            "free-download-crack.ru",
            "keygen-generator.top",
            "free-antivirus-scan.com"
        )
    }
}
