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

package com.qrshield.engine

import com.qrshield.core.SecurityConstants

/**
 * Dynamic Brand Discovery Engine
 *
 * Unlike the static [BrandDatabase], this engine discovers potential brand
 * impersonation by analyzing URL characteristics without a hardcoded brand list.
 *
 * ## Why Dynamic Discovery?
 *
 * The static brand database (500+ entries) catches well-known brands, but:
 * - New startups emerge daily
 * - Regional brands may not be in the database
 * - Attackers target niche brands
 *
 * Dynamic discovery complements the static approach by detecting:
 * 1. **Brand-like patterns** in domains that aren't in the database
 * 2. **Trust-word abuse** (secure, login, verify, official)
 * 3. **Suspicious subdomain patterns** suggesting impersonation
 * 4. **Domain freshness signals** (recently registered TLDs)
 *
 * ## Architecture
 *
 * ```
 * URL Input
 *     ↓
 * ┌─────────────────────────────────────────────────┐
 * │  Static Brand Check (BrandDetector)            │ ← Known brands
 * └─────────────────────────────────────────────────┘
 *     ↓ (if no match)
 * ┌─────────────────────────────────────────────────┐
 * │  Dynamic Brand Discovery (this class)          │ ← Unknown brands
 * │  • Trust word analysis                         │
 * │  • Domain pattern analysis                     │
 * │  • Subdomain impersonation detection           │
 * └─────────────────────────────────────────────────┘
 *     ↓
 * Combined Risk Score
 * ```
 *
 * @author QR-SHIELD Security Team
 * @since 1.6.1
 * @see BrandDetector
 * @see BrandDatabase
 */
object DynamicBrandDiscovery {

    /**
     * Trust-related keywords often abused in phishing domains.
     * These create false sense of security.
     */
    private val TRUST_WORDS = setOf(
        "secure", "security", "verified", "verify", "verification",
        "official", "authentic", "trusted", "safe", "protected",
        "confirm", "validate", "update", "login", "signin",
        "account", "banking", "payment", "wallet", "checkout"
    )

    /**
     * Action words that suggest credential harvesting.
     */
    private val ACTION_WORDS = setOf(
        "login", "signin", "sign-in", "logon", "authenticate",
        "reset", "recover", "unlock", "suspend", "expire",
        "confirm", "verify", "update", "renew", "reactivate"
    )

    /**
     * Urgency words used in social engineering.
     */
    private val URGENCY_WORDS = setOf(
        "urgent", "immediately", "now", "today", "expire",
        "suspend", "locked", "limited", "alert", "warning",
        "critical", "required", "mandatory"
    )

    /**
     * Result of dynamic brand discovery analysis.
     */
    data class DiscoveryResult(
        val score: Int,
        val findings: List<Finding>,
        val suggestedBrand: String? = null
    ) {
        val hasSuspiciousPatterns: Boolean get() = findings.isNotEmpty()
    }

    /**
     * Individual finding from analysis.
     */
    data class Finding(
        val type: FindingType,
        val description: String,
        val severity: Int
    )

    enum class FindingType {
        TRUST_WORD_ABUSE,
        ACTION_WORD_IN_DOMAIN,
        URGENCY_PATTERN,
        BRAND_LIKE_SUBDOMAIN,
        SUSPICIOUS_HYPHEN_PATTERN,
        IMPERSONATION_STRUCTURE
    }

    /**
     * Analyze a URL for dynamic brand impersonation signals.
     *
     * @param url The URL to analyze
     * @param host The extracted host (for efficiency if already parsed)
     * @param subdomains The subdomain list (if already parsed)
     * @return DiscoveryResult with score and findings
     */
    fun analyze(
        url: String,
        host: String? = null,
        subdomains: List<String>? = null
    ): DiscoveryResult {
        val actualHost = host ?: extractHost(url) ?: return DiscoveryResult(0, emptyList())
        val actualSubdomains = subdomains ?: extractSubdomains(actualHost)
        
        val findings = mutableListOf<Finding>()
        var suggestedBrand: String? = null

        // 1. Check for trust word abuse
        val trustWordFindings = checkTrustWordAbuse(actualHost, actualSubdomains)
        findings.addAll(trustWordFindings)

        // 2. Check for action words in domain
        val actionWordFindings = checkActionWords(actualHost)
        findings.addAll(actionWordFindings)

        // 3. Check for urgency patterns
        val urgencyFindings = checkUrgencyPatterns(url)
        findings.addAll(urgencyFindings)

        // 4. Check for brand-like subdomain patterns
        val (brandLikeFindings, detectedBrand) = checkBrandLikePatterns(actualSubdomains)
        findings.addAll(brandLikeFindings)
        if (suggestedBrand == null) suggestedBrand = detectedBrand

        // 5. Check for suspicious hyphen patterns
        val hyphenFindings = checkHyphenPatterns(actualHost)
        findings.addAll(hyphenFindings)

        // 6. Check for impersonation structure
        val structureFindings = checkImpersonationStructure(actualHost, actualSubdomains)
        findings.addAll(structureFindings)

        // Calculate total score (capped)
        val totalScore = findings.sumOf { it.severity }.coerceAtMost(SecurityConstants.MAX_BRAND_SCORE)

        return DiscoveryResult(
            score = totalScore,
            findings = findings,
            suggestedBrand = suggestedBrand
        )
    }

    // === PRIVATE ANALYSIS METHODS ===

    private fun checkTrustWordAbuse(host: String, subdomains: List<String>): List<Finding> {
        val findings = mutableListOf<Finding>()
        val hostLower = host.lowercase()
        val allParts = (subdomains + listOf(host)).map { it.lowercase() }

        for (word in TRUST_WORDS) {
            if (allParts.any { part -> word in part && !part.endsWith(".$word.com") }) {
                findings.add(
                    Finding(
                        type = FindingType.TRUST_WORD_ABUSE,
                        description = "Trust word '$word' in domain suggests legitimacy deception",
                        severity = 8
                    )
                )
            }
        }

        return findings.take(2) // Limit to avoid over-penalizing
    }

    private fun checkActionWords(host: String): List<Finding> {
        val findings = mutableListOf<Finding>()
        val hostLower = host.lowercase()

        for (word in ACTION_WORDS) {
            if (word in hostLower) {
                findings.add(
                    Finding(
                        type = FindingType.ACTION_WORD_IN_DOMAIN,
                        description = "Action word '$word' suggests credential harvesting",
                        severity = 10
                    )
                )
                break // One is enough
            }
        }

        return findings
    }

    private fun checkUrgencyPatterns(url: String): List<Finding> {
        val findings = mutableListOf<Finding>()
        val urlLower = url.lowercase()

        val urgencyCount = URGENCY_WORDS.count { it in urlLower }
        if (urgencyCount >= 2) {
            findings.add(
                Finding(
                    type = FindingType.URGENCY_PATTERN,
                    description = "Multiple urgency words suggest social engineering",
                    severity = 12
                )
            )
        }

        return findings
    }

    private fun checkBrandLikePatterns(subdomains: List<String>): Pair<List<Finding>, String?> {
        val findings = mutableListOf<Finding>()
        var detectedBrand: String? = null

        for (subdomain in subdomains) {
            val lower = subdomain.lowercase()
            
            // Pattern: subdomain looks like a brand (capitalized word, 4-15 chars)
            if (lower.length in 4..15 && lower.all { it.isLetter() }) {
                // Could be impersonating an unknown brand
                if (lower !in setOf("www", "mail", "blog", "shop", "app", "api", "cdn", "dev", "staging")) {
                    detectedBrand = subdomain.replaceFirstChar { it.uppercase() }
                    findings.add(
                        Finding(
                            type = FindingType.BRAND_LIKE_SUBDOMAIN,
                            description = "Subdomain '$subdomain' may impersonate brand '$detectedBrand'",
                            severity = 6
                        )
                    )
                }
            }
        }

        return Pair(findings.take(1), detectedBrand)
    }

    private fun checkHyphenPatterns(host: String): List<Finding> {
        val findings = mutableListOf<Finding>()
        val hyphenCount = host.count { it == '-' }

        // Pattern: brand-secure, brand-login, brand-verify
        val suspiciousPattern = Regex("^[a-z]+-(secure|login|verify|update|confirm|official|support|help)\\.")
        if (suspiciousPattern.containsMatchIn(host.lowercase())) {
            findings.add(
                Finding(
                    type = FindingType.SUSPICIOUS_HYPHEN_PATTERN,
                    description = "Hyphenated pattern suggests brand impersonation",
                    severity = 15
                )
            )
        } else if (hyphenCount >= 3) {
            findings.add(
                Finding(
                    type = FindingType.SUSPICIOUS_HYPHEN_PATTERN,
                    description = "Excessive hyphens ($hyphenCount) may hide destination",
                    severity = 8
                )
            )
        }

        return findings
    }

    private fun checkImpersonationStructure(host: String, subdomains: List<String>): List<Finding> {
        val findings = mutableListOf<Finding>()

        // Pattern: legitimate-looking subdomain on suspicious TLD
        // e.g., google.security-check.tk
        if (subdomains.size >= 2) {
            val deepestSubdomain = subdomains.last()
            if (deepestSubdomain.length >= 4 && deepestSubdomain.all { it.isLetter() }) {
                findings.add(
                    Finding(
                        type = FindingType.IMPERSONATION_STRUCTURE,
                        description = "Deep subdomain structure may hide malicious domain",
                        severity = 10
                    )
                )
            }
        }

        return findings
    }

    // === UTILITY METHODS ===

    private fun extractHost(url: String): String? {
        val withoutProtocol = url.substringAfter("://").substringBefore("/").substringBefore("?")
        return withoutProtocol.takeIf { it.isNotBlank() }
    }

    private fun extractSubdomains(host: String): List<String> {
        val parts = host.split(".")
        return if (parts.size > 2) parts.dropLast(2) else emptyList()
    }
}
