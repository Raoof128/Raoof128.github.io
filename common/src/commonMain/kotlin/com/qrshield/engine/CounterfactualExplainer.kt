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

/**
 * Counterfactual Explainer for QR-SHIELD
 *
 * Provides "what if" explanations that help users understand how
 * changing URL characteristics would affect the risk score.
 *
 * Example:
 * - "If this used HTTPS instead of HTTP, the risk would decrease by 30 points"
 * - "If the domain was 'paypal.com' instead of 'paypa1.tk', this would be SAFE"
 *
 * This improves transparency and user education about phishing indicators.
 *
 * @author QR-SHIELD Security Team
 * @since 1.1.0
 */
class CounterfactualExplainer {

    /**
     * A single counterfactual hint.
     */
    data class Hint(
        val signalType: String,
        val currentValue: String,
        val suggestedChange: String,
        val scoreReduction: Int,
        val explanation: String
    )

    /**
     * Generate counterfactual hints for a given analysis result.
     *
     * @param url The analyzed URL
     * @param triggeredSignals Map of signal name to weight
     * @return List of counterfactual hints ordered by impact
     */
    fun generateHints(url: String, triggeredSignals: Map<String, Int>): List<Hint> {
        val hints = mutableListOf<Hint>()

        triggeredSignals.forEach { (signal, weight) ->
            val hint = generateHintForSignal(url, signal, weight)
            if (hint != null) {
                hints.add(hint)
            }
        }

        // Sort by score reduction (highest impact first)
        return hints.sortedByDescending { it.scoreReduction }
    }

    private fun generateHintForSignal(url: String, signal: String, weight: Int): Hint? {
        return when (signal) {
            "HTTP_NOT_HTTPS" -> Hint(
                signalType = signal,
                currentValue = "http://",
                suggestedChange = "https://",
                scoreReduction = weight,
                explanation = "Using HTTPS would add encryption and reduce suspicion by $weight points"
            )

            "IP_ADDRESS_HOST" -> {
                val ipMatch = Regex("""://(\d+\.\d+\.\d+\.\d+)""").find(url)
                Hint(
                    signalType = signal,
                    currentValue = ipMatch?.groupValues?.get(1) ?: "IP address",
                    suggestedChange = "legitimate-domain.com",
                    scoreReduction = weight,
                    explanation = "A registered domain instead of an IP address would reduce risk by $weight points"
                )
            }

            "URL_SHORTENER" -> Hint(
                signalType = signal,
                currentValue = "Shortened URL",
                suggestedChange = "Full destination URL",
                scoreReduction = weight,
                explanation = "Showing the full destination URL would reduce suspicion by $weight points"
            )

            "EXCESSIVE_SUBDOMAINS" -> {
                val subdomainCount = url.substringAfter("://").substringBefore("/").count { it == '.' }
                Hint(
                    signalType = signal,
                    currentValue = "$subdomainCount subdomain levels",
                    suggestedChange = "1-2 subdomain levels",
                    scoreReduction = weight,
                    explanation = "Fewer subdomains would appear more legitimate, reducing risk by $weight points"
                )
            }

            "NON_STANDARD_PORT" -> {
                val portMatch = Regex(""":(\d+)""").find(url.substringAfter("://"))
                Hint(
                    signalType = signal,
                    currentValue = "Port ${portMatch?.groupValues?.get(1) ?: "custom"}",
                    suggestedChange = "Standard port (80/443)",
                    scoreReduction = weight,
                    explanation = "Using a standard web port would reduce risk by $weight points"
                )
            }

            "SUSPICIOUS_PATH_KEYWORDS" -> Hint(
                signalType = signal,
                currentValue = "Credential keywords in path",
                suggestedChange = "Neutral path",
                scoreReduction = weight,
                explanation = "Removing words like 'login', 'verify', 'password' would reduce risk by $weight points"
            )

            "CREDENTIAL_PARAMS" -> Hint(
                signalType = signal,
                currentValue = "Credential parameters",
                suggestedChange = "No sensitive parameters",
                scoreReduction = weight,
                explanation = "Removing parameters like 'password', 'token', 'auth' would reduce risk by $weight points"
            )

            "AT_SYMBOL_INJECTION" -> Hint(
                signalType = signal,
                currentValue = "@ symbol in URL",
                suggestedChange = "No @ symbol",
                scoreReduction = weight,
                explanation = "Removing the @ symbol would eliminate URL spoofing suspicion, reducing risk by $weight points"
            )

            "PUNYCODE_DOMAIN" -> Hint(
                signalType = signal,
                currentValue = "Internationalized domain (xn--)",
                suggestedChange = "ASCII domain",
                scoreReduction = weight,
                explanation = "Using standard ASCII characters would reduce homograph attack suspicion by $weight points"
            )

            "RISKY_EXTENSION" -> Hint(
                signalType = signal,
                currentValue = "Executable file extension",
                suggestedChange = "Web page (.html) or no extension",
                scoreReduction = weight,
                explanation = "Avoiding executable extensions would reduce malware suspicion by $weight points"
            )

            "DOUBLE_EXTENSION" -> Hint(
                signalType = signal,
                currentValue = "Double file extension",
                suggestedChange = "Single extension",
                scoreReduction = weight,
                explanation = "Single file extension would remove malware obfuscation suspicion, reducing risk by $weight points"
            )

            "HIGH_ENTROPY_HOST" -> Hint(
                signalType = signal,
                currentValue = "Random-looking domain",
                suggestedChange = "Readable domain name",
                scoreReduction = weight,
                explanation = "A human-readable domain would reduce DGA (domain generation algorithm) suspicion by $weight points"
            )

            "ENCODED_PAYLOAD" -> Hint(
                signalType = signal,
                currentValue = "Base64 encoded data",
                suggestedChange = "Plain text parameters",
                scoreReduction = weight,
                explanation = "Removing encoded payloads would reduce data exfiltration suspicion by $weight points"
            )

            "LONG_URL" -> Hint(
                signalType = signal,
                currentValue = "${url.length} characters",
                suggestedChange = "Under 100 characters",
                scoreReduction = weight,
                explanation = "A shorter URL would appear less suspicious, reducing risk by $weight points"
            )

            else -> null // Unknown signal type
        }
    }

    /**
     * Generate a summary of what would make this URL safe.
     */
    fun generateSafetySummary(hints: List<Hint>): String {
        if (hints.isEmpty()) {
            return "This URL has no significant risk factors that can be addressed."
        }

        val totalReduction = hints.sumOf { it.scoreReduction }
        val topHints = hints.take(3)

        val summary = StringBuilder()
        summary.append("To reduce the risk score by up to $totalReduction points:\n")

        topHints.forEachIndexed { index, hint ->
            summary.append("${index + 1}. ${hint.explanation}\n")
        }

        return summary.toString()
    }
}
