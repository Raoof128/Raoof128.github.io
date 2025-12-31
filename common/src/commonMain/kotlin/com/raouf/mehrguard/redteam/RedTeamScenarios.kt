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

package com.raouf.mehrguard.redteam

/**
 * Red Team Developer Mode Scenarios
 *
 * This module provides pre-defined malicious URL scenarios for demonstration
 * purposes. When Developer Mode is enabled (via 7 taps on version text in Settings),
 * these scenarios appear at the top of the Scanner screen, allowing judges and
 * developers to instantly test the detection engine without needing to print QR codes.
 *
 * ## Security Note
 * All URLs in this corpus are either:
 * - Defanged patterns that won't resolve to real domains
 * - Using suspicious TLDs (.tk, .ml, .ga) for obvious demonstration
 *
 * ## Usage
 * ```kotlin
 * val scenario = RedTeamScenarios.SCENARIOS.first()
 * val result = phishingEngine.analyze(scenario.maliciousUrl)
 * ```
 *
 * @see com.raouf.mehrguard.adversarial.AdversarialDefense
 * @see data/red_team_corpus.md
 *
 * @author QR-SHIELD Security Team
 * @since 1.3.0
 */
object RedTeamScenarios {

    /**
     * A red team test scenario with a description and malicious URL.
     *
     * @property id Unique identifier (e.g., "HG-001")
     * @property category Attack category (e.g., "Homograph Attack")
     * @property title Human-readable scenario title
     * @property description Brief explanation of the attack technique
     * @property maliciousUrl The malicious URL to analyze (refanged - real URL format)
     * @property targetBrand The brand being impersonated, if applicable
     * @property expectedScore Expected risk score range (for verification)
     */
    data class Scenario(
        val id: String,
        val category: String,
        val title: String,
        val description: String,
        val maliciousUrl: String,
        val targetBrand: String? = null,
        val expectedScore: IntRange = 50..100
    )

    /**
     * All available red team scenarios for testing.
     * Ordered by attack category for logical grouping in UI.
     */
    val SCENARIOS: List<Scenario> = listOf(
        // =============================================================================
        // HOMOGRAPH ATTACKS (Mixed Scripts)
        // =============================================================================
        Scenario(
            id = "HG-001",
            category = "Homograph Attack",
            title = "Cyrillic 'а' in Apple",
            description = "Uses Cyrillic 'а' (U+0430) instead of Latin 'a' to impersonate Apple",
            maliciousUrl = "https://аpple.com/verify", // Cyrillic 'а'
            targetBrand = "Apple",
            expectedScore = 70..100
        ),
        Scenario(
            id = "HG-002",
            category = "Homograph Attack",
            title = "Cyrillic in PayPal",
            description = "Uses Cyrillic 'р' and 'а' to impersonate PayPal",
            maliciousUrl = "https://раypal.com/login", // Cyrillic 'р' and 'а'
            targetBrand = "PayPal",
            expectedScore = 70..100
        ),
        Scenario(
            id = "HG-003",
            category = "Homograph Attack",
            title = "Cyrillic 'о' in Microsoft",
            description = "Uses Cyrillic 'о' (U+043E) to impersonate Microsoft",
            maliciousUrl = "https://micrоsоft.com/signin", // Cyrillic 'о'
            targetBrand = "Microsoft",
            expectedScore = 70..100
        ),

        // =============================================================================
        // IP OBFUSCATION
        // =============================================================================
        Scenario(
            id = "IP-001",
            category = "IP Obfuscation",
            title = "Decimal IP Address",
            description = "Uses decimal encoding (3232235777) instead of dotted notation",
            maliciousUrl = "http://3232235777/malware",
            targetBrand = null,
            expectedScore = 60..100
        ),
        Scenario(
            id = "IP-002",
            category = "IP Obfuscation",
            title = "Hexadecimal IP Address",
            description = "Uses hex encoding (0xC0A80101) to hide IP address",
            maliciousUrl = "http://0xC0A80101/payload",
            targetBrand = null,
            expectedScore = 60..100
        ),
        Scenario(
            id = "IP-003",
            category = "IP Obfuscation",
            title = "Octal IP Address",
            description = "Uses octal notation (0300.0250.0001.0001) to obfuscate IP",
            maliciousUrl = "http://0300.0250.0001.0001/shell",
            targetBrand = null,
            expectedScore = 50..100
        ),

        // =============================================================================
        // SUSPICIOUS TLD (Known Phishing TLDs)
        // =============================================================================
        Scenario(
            id = "TLD-001",
            category = "Suspicious TLD",
            title = "PayPal on .tk domain",
            description = ".tk is a free TLD commonly abused for phishing",
            maliciousUrl = "https://paypa1-secure.tk/login/verify",
            targetBrand = "PayPal",
            expectedScore = 70..100
        ),
        Scenario(
            id = "TLD-002",
            category = "Suspicious TLD",
            title = "Bank on .ml domain",
            description = ".ml is a free TLD commonly abused for phishing",
            maliciousUrl = "https://bank-secure.ml/verify",
            targetBrand = "Banking",
            expectedScore = 60..100
        ),
        Scenario(
            id = "TLD-003",
            category = "Suspicious TLD",
            title = "Amazon on .ga domain",
            description = ".ga is a free TLD commonly abused for phishing",
            maliciousUrl = "https://amazon-security.ga/giftcard",
            targetBrand = "Amazon",
            expectedScore = 60..100
        ),

        // =============================================================================
        // NESTED REDIRECTS
        // =============================================================================
        Scenario(
            id = "NR-001",
            category = "Nested Redirect",
            title = "URL in Query Parameter",
            description = "Embeds phishing URL in redirect parameter",
            maliciousUrl = "https://legit.com/redirect?url=https://phishing.tk/login",
            targetBrand = null,
            expectedScore = 50..90
        ),
        Scenario(
            id = "NR-002",
            category = "Nested Redirect",
            title = "Encoded Nested URL",
            description = "URL-encoded malicious redirect destination",
            maliciousUrl = "https://legit.com/goto?next=https%3A%2F%2Fmalware.ml%2Fdownload",
            targetBrand = null,
            expectedScore = 50..90
        ),

        // =============================================================================
        // BRAND IMPERSONATION (Typosquatting)
        // =============================================================================
        Scenario(
            id = "BI-001",
            category = "Brand Impersonation",
            title = "PayPal Typosquatting",
            description = "Uses '1' instead of 'l' in paypal (paypa1)",
            maliciousUrl = "https://paypa1.com/signin",
            targetBrand = "PayPal",
            expectedScore = 60..100
        ),
        Scenario(
            id = "BI-002",
            category = "Brand Impersonation",
            title = "Google Typosquatting",
            description = "Uses 'googIe' with capital I instead of 'l'",
            maliciousUrl = "https://googIe.com/account/verify",
            targetBrand = "Google",
            expectedScore = 50..90
        ),
        Scenario(
            id = "BI-003",
            category = "Brand Impersonation",
            title = "Netflix Subdomain Attack",
            description = "Uses netflix as subdomain of malicious domain",
            maliciousUrl = "https://netflix.secure-verify.com/billing",
            targetBrand = "Netflix",
            expectedScore = 50..90
        ),

        // =============================================================================
        // URL SHORTENERS
        // =============================================================================
        Scenario(
            id = "SH-001",
            category = "URL Shortener",
            title = "Bit.ly Shortened URL",
            description = "URL shorteners hide final destination, often used in phishing",
            maliciousUrl = "https://bit.ly/3xYz123",
            targetBrand = null,
            expectedScore = 30..60
        ),
        Scenario(
            id = "SH-002",
            category = "URL Shortener",
            title = "TinyURL Shortened",
            description = "Another common shortener used to hide malicious destinations",
            maliciousUrl = "https://tinyurl.com/y2abc",
            targetBrand = null,
            expectedScore = 30..60
        ),

        // =============================================================================
        // SAFE CONTROL (Baseline for comparison)
        // =============================================================================
        Scenario(
            id = "SAFE-001",
            category = "Safe (Control)",
            title = "Legitimate Google URL",
            description = "Baseline safe URL - should score low",
            maliciousUrl = "https://www.google.com",
            targetBrand = "Google",
            expectedScore = 0..30
        ),
        Scenario(
            id = "SAFE-002",
            category = "Safe (Control)",
            title = "Legitimate GitHub URL",
            description = "Baseline safe URL - should score low",
            maliciousUrl = "https://github.com/Raoof128/QDKMP-KotlinConf-2026-",
            targetBrand = "GitHub",
            expectedScore = 0..30
        )
    )

    /**
     * Get scenarios grouped by category for display in UI.
     */
    fun groupedByCategory(): Map<String, List<Scenario>> {
        return SCENARIOS.groupBy { it.category }
    }

    /**
     * Get a specific scenario by ID.
     */
    fun getById(id: String): Scenario? {
        return SCENARIOS.find { it.id == id }
    }

    /**
     * Get all categories in display order.
     */
    val categories: List<String>
        get() = SCENARIOS.map { it.category }.distinct()
}
