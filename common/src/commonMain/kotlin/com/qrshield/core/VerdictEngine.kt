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

package com.qrshield.core

import com.qrshield.model.RiskAssessment
import com.qrshield.model.Verdict

/**
 * Verdict Engine for QR-SHIELD
 *
 * Generates human-readable explanations and recommendations
 * based on risk assessment results.
 */
class VerdictEngine {

    data class EnrichedVerdict(
        val verdict: Verdict,
        val summary: String,
        val recommendation: String,
        val riskFactorExplanations: List<String>,
        val safetyTips: List<String>
    )

    /**
     * Generate enriched verdict with explanations
     */
    fun enrich(assessment: RiskAssessment): EnrichedVerdict {
        val verdict = assessment.verdict
        val summary = generateSummary(verdict, assessment.score)
        val recommendation = generateRecommendation(verdict)
        val explanations = generateExplanations(assessment.flags)
        val tips = generateSafetyTips(verdict)

        return EnrichedVerdict(
            verdict = verdict,
            summary = summary,
            recommendation = recommendation,
            riskFactorExplanations = explanations,
            safetyTips = tips
        )
    }

    private fun generateSummary(verdict: Verdict, score: Int): String {
        return when (verdict) {
            Verdict.SAFE ->
                "This URL appears to be safe with a risk score of $score. " +
                "No significant phishing indicators were detected."

            Verdict.SUSPICIOUS ->
                "This URL has some suspicious characteristics with a risk score of $score. " +
                "Several potential phishing indicators were found."

            Verdict.MALICIOUS ->
                "This URL is likely malicious with a high risk score of $score. " +
                "Multiple strong phishing indicators were detected."

            Verdict.UNKNOWN ->
                "Unable to fully analyze this URL. " +
                "The format may be unsupported or invalid."
        }
    }

    private fun generateRecommendation(verdict: Verdict): String {
        return when (verdict) {
            Verdict.SAFE ->
                "You can proceed to this URL. However, always verify that you're " +
                "on the official website before entering any personal information."

            Verdict.SUSPICIOUS ->
                "Proceed with caution. Verify the URL carefully, check for typos, " +
                "and confirm the source before clicking. When in doubt, navigate " +
                "directly to the official website instead."

            Verdict.MALICIOUS ->
                "DO NOT visit this URL. It has strong indicators of being a " +
                "phishing attempt. If you received this in a message, report it " +
                "as spam or phishing."

            Verdict.UNKNOWN ->
                "Could not analyze this URL. If you must visit it, use caution " +
                "and verify through official channels first."
        }
    }

    private fun generateExplanations(flags: List<String>): List<String> {
        return flags.map { flag ->
            when {
                flag.contains("HTTP", ignoreCase = true) ->
                    "The URL uses HTTP instead of HTTPS, meaning data is not encrypted."
                flag.contains("IP", ignoreCase = true) ->
                    "The URL uses an IP address instead of a domain name, which is unusual for legitimate sites."
                flag.contains("Brand", ignoreCase = true) ->
                    "The URL appears to impersonate a well-known brand, which is a common phishing tactic."
                flag.contains("TLD", ignoreCase = true) ->
                    "The domain uses a top-level domain commonly associated with malicious sites."
                flag.contains("shortener", ignoreCase = true) ->
                    "The URL uses a shortening service, hiding the actual destination."
                flag.contains("subdomain", ignoreCase = true) ->
                    "The URL has an unusual number of subdomains, which can be used to mislead users."
                flag.contains("credential", ignoreCase = true) ->
                    "The URL contains parameters that suggest credential harvesting."
                flag.contains("long", ignoreCase = true) ->
                    "The URL is unusually long, which can be used to hide the actual destination."
                else -> flag
            }
        }
    }

    private fun generateSafetyTips(verdict: Verdict): List<String> {
        return when (verdict) {
            Verdict.SAFE -> listOf(
                "Even for safe URLs, never enter passwords on unfamiliar sites",
                "Look for the padlock icon in your browser's address bar",
                "Bookmark important sites to avoid typos"
            )
            Verdict.SUSPICIOUS -> listOf(
                "Hover over links to preview the actual URL before clicking",
                "Check for subtle misspellings in the domain name",
                "When in doubt, navigate directly to the official website",
                "Enable two-factor authentication on important accounts"
            )
            Verdict.MALICIOUS -> listOf(
                "Do not click this link under any circumstances",
                "Report this URL to your IT department or security team",
                "If you've already visited similar URLs, change your passwords",
                "Consider reporting to relevant authorities (e.g., Scamwatch in Australia)"
            )
            Verdict.UNKNOWN -> listOf(
                "Verify the URL through official channels",
                "Contact the sender through a known, trusted method",
                "Use a different device or network if verification is needed"
            )
        }
    }
}
