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

package com.qrshield.benchmark

import com.qrshield.core.PhishingEngine
import com.qrshield.model.Verdict
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * False Positive Rate Test Suite
 *
 * Validates that QR-SHIELD does not incorrectly flag legitimate websites.
 * These tests run against major legitimate URLs to ensure low FP rate.
 *
 * ## Why False Positive Rate Matters
 *
 * A security tool that cries wolf on every URL is useless. Users will:
 * 1. Ignore all warnings (alert fatigue)
 * 2. Uninstall the app
 * 3. Tell friends it doesn't work
 *
 * ## Known Limitations
 *
 * Some legitimate URLs may trigger warnings due to:
 * - Unusual TLDs (.io, .app) that are also used in phishing
 * - Subdomains that match brand names (cdn.paypal.com ≠ paypal.com)
 * - Long URLs with encoded parameters
 *
 * We accept SUSPICIOUS as tolerable, but MALICIOUS is always a bug.
 */
class FalsePositiveRateTest {

    private val engine = PhishingEngine()

    /**
     * Core legitimate URLs that should NEVER be flagged as MALICIOUS.
     * 
     * These are globally recognized, legitimate websites.
     */
    private val coreLegitimateUrls = listOf(
        // Search Engines
        "https://google.com",
        "https://www.google.com/search?q=kotlin",
        "https://bing.com",
        "https://duckduckgo.com",

        // Tech Giants
        "https://apple.com",
        "https://microsoft.com",
        "https://amazon.com",

        // Social Media
        "https://facebook.com",
        "https://twitter.com",
        "https://instagram.com",
        "https://linkedin.com",
        "https://youtube.com",
        "https://reddit.com",

        // E-commerce
        "https://ebay.com",
        "https://shopify.com",
        "https://walmart.com",

        // News & Media
        "https://bbc.com",
        "https://cnn.com",
        "https://nytimes.com",

        // Developer Tools
        "https://github.com",
        "https://github.com/JetBrains/kotlin",
        "https://stackoverflow.com",
        "https://gitlab.com",

        // Cloud Services
        "https://aws.amazon.com",
        "https://cloud.google.com",

        // Productivity
        "https://docs.google.com",
        "https://dropbox.com",
        "https://slack.com",

        // Finance (Banks)
        "https://chase.com",
        "https://bankofamerica.com",
        "https://paypal.com",
        "https://stripe.com",

        // Government
        "https://usa.gov",
        "https://gov.uk",

        // Education
        "https://mit.edu",
        "https://stanford.edu",
        "https://coursera.org",

        // Wikipedia
        "https://wikipedia.org",
        "https://en.wikipedia.org/wiki/Kotlin_(programming_language)",

        // Kotlin Specific
        "https://kotlinlang.org",
        "https://kotlinconf.com",
        "https://plugins.jetbrains.com"
    )

    /**
     * Test that core legitimate URLs are NEVER flagged as MALICIOUS.
     * 
     * SUSPICIOUS is tolerable (user can proceed), MALICIOUS is not.
     */
    @Test
    fun core_legitimate_urls_are_never_malicious() {
        var maliciousCount = 0
        val maliciousUrls = mutableListOf<String>()

        for (url in coreLegitimateUrls) {
            val result = engine.analyzeBlocking(url)
            if (result.verdict == Verdict.MALICIOUS) {
                maliciousCount++
                maliciousUrls.add(url)
            }
        }

        // Print results for debugging
        println("=== FALSE POSITIVE TEST RESULTS ===")
        println("Total URLs tested: ${coreLegitimateUrls.size}")
        println("Flagged as MALICIOUS: $maliciousCount")
        
        if (maliciousUrls.isNotEmpty()) {
            println("\nFalse MALICIOUS URLs (BUGS):")
            maliciousUrls.forEach { println("  - $it") }
        }

        // Zero tolerance for MALICIOUS verdicts on legitimate sites
        assertTrue(
            maliciousCount == 0,
            "CRITICAL: $maliciousCount legitimate URLs flagged as MALICIOUS: $maliciousUrls"
        )
    }

    /**
     * Test that major payment providers are NEVER flagged as malicious.
     *
     * These are critical - a false positive on PayPal login would be devastating.
     */
    @Test
    fun payment_providers_are_never_flagged_malicious() {
        val paymentProviders = listOf(
            "https://paypal.com",
            "https://www.paypal.com/signin",
            "https://stripe.com",
            "https://square.com",
            "https://venmo.com",
            "https://wise.com"
        )

        paymentProviders.forEach { url ->
            val result = engine.analyzeBlocking(url)
            assertTrue(
                result.verdict != Verdict.MALICIOUS,
                "CRITICAL: Legitimate payment provider $url was flagged as MALICIOUS. " +
                    "Score: ${result.score}, Flags: ${result.flags}"
            )
        }
    }

    /**
     * Test that JetBrains/Kotlin sites are NEVER flagged as MALICIOUS.
     *
     * SUSPICIOUS is tolerable (some sites trigger domain entropy heuristics).
     * Would be embarrassing to BLOCK Kotlin Foundation sites at KotlinConf!
     */
    @Test
    fun kotlin_ecosystem_sites_are_safe() {
        val kotlinSites = listOf(
            "https://kotlinlang.org",
            "https://kotlinlang.org/docs/home.html",
            "https://kotlinconf.com",
            "https://blog.jetbrains.com/kotlin",
            "https://plugins.jetbrains.com/plugin/6954-kotlin",
            "https://github.com/JetBrains/kotlin"
        )

        kotlinSites.forEach { url ->
            val result = engine.analyzeBlocking(url)
            // Only MALICIOUS is unacceptable - SUSPICIOUS is a warning, not a block
            assertTrue(
                result.verdict != Verdict.MALICIOUS,
                "Kotlin ecosystem site $url was incorrectly flagged as MALICIOUS. " +
                    "Score: ${result.score}, Flags: ${result.flags}"
            )
        }
    }

    /**
     * Test that URLs with long query strings (marketing) don't trigger MALICIOUS.
     */
    @Test
    fun marketing_urls_with_utm_params_are_not_malicious() {
        val marketingUrls = listOf(
            "https://www.google.com/search?q=kotlin+multiplatform&source=hp",
            "https://www.amazon.com/dp/B09V3KXJPB?ref=sr_1_1",
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        )

        marketingUrls.forEach { url ->
            val result = engine.analyzeBlocking(url)
            assertTrue(
                result.verdict != Verdict.MALICIOUS,
                "Marketing URL $url was flagged as MALICIOUS. Score: ${result.score}"
            )
        }
    }

    /**
     * Calculates and reports overall false positive statistics.
     * 
     * This is informational - not a pass/fail test.
     */
    @Test
    fun report_false_positive_statistics() {
        var safeCount = 0
        var suspiciousCount = 0
        var maliciousCount = 0
        var unknownCount = 0

        for (url in coreLegitimateUrls) {
            when (engine.analyzeBlocking(url).verdict) {
                Verdict.SAFE -> safeCount++
                Verdict.SUSPICIOUS -> suspiciousCount++
                Verdict.MALICIOUS -> maliciousCount++
                Verdict.UNKNOWN -> unknownCount++
            }
        }

        val total = coreLegitimateUrls.size
        val safeRate = (safeCount * 100) / total
        val fpRate = ((suspiciousCount + maliciousCount) * 100) / total

        println("\n=== FALSE POSITIVE RATE REPORT ===")
        println("Total: $total")
        println("SAFE: $safeCount ($safeRate%)")
        println("SUSPICIOUS: $suspiciousCount (acceptable FP)")
        println("MALICIOUS: $maliciousCount (bugs!)")
        println("UNKNOWN: $unknownCount")
        println("Overall FP Rate: $fpRate%")
        println("Target: <10% FP rate")

        // This test always passes - it's for reporting only
        assertTrue(true, "Report generated")
    }
}
