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

package com.raouf.mehrguard.ui

import com.raouf.mehrguard.model.RiskAssessment
import com.raouf.mehrguard.model.Verdict

/**
 * Shared Text Generator
 *
 * Generates all user-facing text in the shared module, ensuring
 * identical messaging across all platforms (Android, iOS, Desktop, Web).
 *
 * ## KMP Parity Principle
 * - Text generation logic is 100% shared
 * - Platforms only render the generated strings
 * - Localization keys are consistent across all targets
 * - No platform-specific text formatting
 *
 * ## Usage
 * ```kotlin
 * val generator = SharedTextGenerator
 * val title = generator.getVerdictTitle(assessment.verdict)
 * val explanation = generator.getRiskExplanation(assessment)
 * ```
 *
 * @author Mehr Guard Security Team
 * @since 1.2.0
 */
object SharedTextGenerator {

    // ==================== Verdict Text ====================

    /**
     * Get the verdict title for display.
     * Used in result cards across all platforms.
     */
    fun getVerdictTitle(verdict: Verdict): String = when (verdict) {
        Verdict.SAFE -> "Safe"
        Verdict.SUSPICIOUS -> "Suspicious"
        Verdict.MALICIOUS -> "Dangerous"
        Verdict.UNKNOWN -> "Unknown"
    }

    /**
     * Get verdict description for accessibility and tooltips.
     */
    fun getVerdictDescription(verdict: Verdict): String = when (verdict) {
        Verdict.SAFE -> "This URL appears to be safe to visit."
        Verdict.SUSPICIOUS -> "This URL has some suspicious characteristics. Proceed with caution."
        Verdict.MALICIOUS -> "This URL shows strong signs of phishing. Do not visit."
        Verdict.UNKNOWN -> "Unable to determine the risk level of this URL."
    }

    /**
     * Get accessibility label for verdict with score.
     */
    fun getVerdictAccessibilityLabel(verdict: Verdict, score: Int): String =
        "${getVerdictTitle(verdict)}. Risk score: $score out of 100. ${getVerdictDescription(verdict)}"

    // ==================== Score Text ====================

    /**
     * Get human-readable score description.
     */
    fun getScoreDescription(score: Int): String = when {
        score < 20 -> "Very Low Risk"
        score < 40 -> "Low Risk"
        score < 60 -> "Medium Risk"
        score < 80 -> "High Risk"
        else -> "Very High Risk"
    }

    /**
     * Get score range label for UI badges.
     */
    fun getScoreRangeLabel(score: Int): String = when {
        score < 30 -> "Safe (0-29)"
        score < 70 -> "Suspicious (30-69)"
        else -> "Dangerous (70-100)"
    }

    // ==================== Risk Explanation ====================

    /**
     * Generate comprehensive risk explanation for the result card.
     * This is the main text users see explaining why a URL is risky.
     */
    fun getRiskExplanation(assessment: RiskAssessment): String {
        val lines = mutableListOf<String>()

        when (assessment.verdict) {
            Verdict.SAFE -> {
                lines.add("No significant risk indicators detected.")
                lines.add("This URL appears to be legitimate.")
            }
            Verdict.SUSPICIOUS -> {
                lines.add("Some risk indicators were detected.")
                lines.add("Verify this is the official site before entering any information.")
            }
            Verdict.MALICIOUS -> {
                lines.add("Multiple high-risk indicators detected.")
                lines.add("This URL shows strong signs of phishing.")
                lines.add("DO NOT enter any personal information.")
            }
            Verdict.UNKNOWN -> {
                lines.add("Unable to fully analyze this URL.")
                lines.add("Proceed with caution.")
            }
        }

        // Add specific findings
        val details = assessment.details
        if (details.brandMatch != null) {
            lines.add("\n• Brand Impersonation: Mimics '${details.brandMatch}'")
        }
        if (details.tldScore > 10) {
            lines.add("• Suspicious TLD: .${details.tld} is frequently used in phishing")
        }
        if (details.heuristicScore > 30) {
            lines.add("• Multiple Security Flags: ${assessment.flags.take(3).joinToString(", ")}")
        }
        if (details.mlScore > 60) {
            lines.add("• ML Analysis: High probability of phishing content")
        }

        return lines.joinToString("\n")
    }

    /**
     * Generate short risk summary for list views and notifications.
     */
    fun getShortRiskSummary(assessment: RiskAssessment): String {
        val details = assessment.details
        return when {
            details.brandMatch != null -> "Brand impersonation: ${details.brandMatch}"
            details.tldScore > 10 -> "Suspicious TLD: .${details.tld}"
            details.heuristicScore > 30 -> assessment.flags.firstOrNull() ?: "Multiple risk flags"
            assessment.verdict == Verdict.SAFE -> "No risks detected"
            else -> getVerdictTitle(assessment.verdict)
        }
    }

    // ==================== Action Recommendations ====================

    /**
     * Get recommended action based on verdict.
     */
    fun getRecommendedAction(verdict: Verdict): String = when (verdict) {
        Verdict.SAFE -> "Safe to proceed"
        Verdict.SUSPICIOUS -> "Verify before proceeding"
        Verdict.MALICIOUS -> "Do not visit this URL"
        Verdict.UNKNOWN -> "Proceed with caution"
    }

    /**
     * Get detailed action guidance for the result screen.
     */
    fun getActionGuidance(verdict: Verdict): List<String> = when (verdict) {
        Verdict.SAFE -> listOf(
            "You can proceed to visit this URL.",
            "As always, be cautious when entering personal information."
        )
        Verdict.SUSPICIOUS -> listOf(
            "Verify this is the official website by checking the URL carefully.",
            "Look for HTTPS and a valid security certificate.",
            "Consider accessing the site directly instead of through this link.",
            "Be cautious about entering sensitive information."
        )
        Verdict.MALICIOUS -> listOf(
            "DO NOT visit this URL.",
            "DO NOT enter any personal or financial information.",
            "This URL shows strong signs of being a phishing attempt.",
            "Report this QR code to help protect others.",
            "If you already entered information, secure your accounts immediately."
        )
        Verdict.UNKNOWN -> listOf(
            "We could not fully analyze this URL.",
            "Proceed with caution.",
            "Verify the source of this QR code before visiting."
        )
    }

    // ==================== Signal Explanations ====================

    /**
     * Signal ID to human-readable explanation mapping.
     * Used across all platforms for consistent signal descriptions.
     */
    val signalExplanations: Map<String, SignalExplanation> = mapOf(
        "BRAND_IMPERSONATION" to SignalExplanation(
            title = "Brand Impersonation",
            description = "This URL mimics a well-known brand's domain",
            impact = "Attackers create lookalike domains to steal credentials"
        ),
        "SUSPICIOUS_TLD" to SignalExplanation(
            title = "Suspicious TLD",
            description = "The domain uses a high-risk top-level domain",
            impact = "Free or cheap TLDs like .tk, .ml are often used for phishing"
        ),
        "URL_SHORTENER" to SignalExplanation(
            title = "URL Shortener",
            description = "The URL uses a shortening service",
            impact = "Short URLs hide the actual destination"
        ),
        "IP_ADDRESS_HOST" to SignalExplanation(
            title = "IP Address Host",
            description = "The URL uses an IP address instead of a domain",
            impact = "Legitimate sites rarely use raw IP addresses"
        ),
        "HTTP_NO_TLS" to SignalExplanation(
            title = "No Encryption",
            description = "The URL does not use HTTPS",
            impact = "Data sent to this site is not encrypted"
        ),
        "EXCESSIVE_SUBDOMAINS" to SignalExplanation(
            title = "Excessive Subdomains",
            description = "The URL has an unusual number of subdomains",
            impact = "Often used to make malicious URLs look legitimate"
        ),
        "HOMOGRAPH_ATTACK" to SignalExplanation(
            title = "Homograph Attack",
            description = "The URL uses lookalike characters (e.g., Cyrillic)",
            impact = "Characters that look identical but are different Unicode"
        ),
        "CREDENTIAL_PATH" to SignalExplanation(
            title = "Credential Harvesting Path",
            description = "The URL path suggests login/verification content",
            impact = "Common pattern in phishing pages"
        ),
        "PUNYCODE_DOMAIN" to SignalExplanation(
            title = "Punycode Domain",
            description = "The domain is an internationalized domain name (IDN)",
            impact = "Can be used to create lookalike domains"
        ),
        "LONG_URL" to SignalExplanation(
            title = "Unusually Long URL",
            description = "The URL is longer than typical legitimate URLs",
            impact = "May be used to hide malicious components"
        ),
        "HIGH_ENTROPY" to SignalExplanation(
            title = "Random-Looking Components",
            description = "The URL contains random-looking strings",
            impact = "Often indicates automatically generated phishing URLs"
        ),
        "EMBEDDED_REDIRECT" to SignalExplanation(
            title = "Embedded Redirect",
            description = "The URL contains another URL in its parameters",
            impact = "May redirect to a malicious destination"
        )
    )

    /**
     * Get explanation for a specific signal.
     */
    fun getSignalExplanation(signalId: String): SignalExplanation? =
        signalExplanations[signalId]

    // ==================== Share Content ====================

    /**
     * Generate shareable text for a scan result.
     */
    fun generateShareText(url: String, assessment: RiskAssessment): String {
        return buildString {
            appendLine("🛡️ Mehr Guard Scan Result")
            appendLine()
            appendLine("URL: $url")
            appendLine("Verdict: ${getVerdictTitle(assessment.verdict)}")
            appendLine("Risk Score: ${assessment.score}/100")
            appendLine()
            if (assessment.flags.isNotEmpty()) {
                appendLine("Risk Factors:")
                assessment.flags.take(5).forEach { flag ->
                    appendLine("• $flag")
                }
            }
            appendLine()
            appendLine("Scanned with Mehr Guard - raoof128.github.io")
        }
    }

    /**
     * Generate JSON export of scan result.
     * Platform-agnostic serialization (manual to avoid kotlinx.serialization on all platforms).
     */
    fun generateJsonExport(url: String, assessment: RiskAssessment, timestamp: Long): String {
        return buildString {
            appendLine("{")
            appendLine("""  "url": "${escapeJson(url)}",""")
            appendLine("""  "verdict": "${assessment.verdict.name}",""")
            appendLine("""  "score": ${assessment.score},""")
            appendLine("""  "confidence": ${assessment.confidence},""")
            appendLine("""  "timestamp": $timestamp,""")
            appendLine("""  "flags": [${assessment.flags.joinToString { "\"${escapeJson(it)}\"" }}],""")
            appendLine("""  "details": {""")
            appendLine("""    "heuristicScore": ${assessment.details.heuristicScore},""")
            appendLine("""    "mlScore": ${assessment.details.mlScore},""")
            appendLine("""    "brandScore": ${assessment.details.brandScore},""")
            appendLine("""    "tldScore": ${assessment.details.tldScore},""")
            appendLine("""    "brandMatch": ${assessment.details.brandMatch?.let { "\"$it\"" } ?: "null"},""")
            appendLine("""    "tld": "${assessment.details.tld}"""")
            appendLine("  }")
            appendLine("}")
        }
    }

    private fun escapeJson(s: String): String =
        s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
}

/**
 * Data class for signal explanations.
 * Used across all platforms for consistent UI rendering.
 */
data class SignalExplanation(
    val title: String,
    val description: String,
    val impact: String
)
