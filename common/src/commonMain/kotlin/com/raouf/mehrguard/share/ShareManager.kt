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

package com.raouf.mehrguard.share

import com.raouf.mehrguard.model.RiskAssessment
import com.raouf.mehrguard.model.Verdict

/**
 * Share Manager for Mehr Guard
 *
 * Generates shareable content from scan analysis results.
 * Platform-specific implementations handle actual sharing.
 *
 * @author Mehr Guard Security Team
 * @since 1.0.0
 */
object ShareManager {

    /**
     * Generate plain text summary for sharing.
     */
    fun generateTextSummary(url: String, assessment: RiskAssessment): String {
        val emoji = when (assessment.verdict) {
            Verdict.SAFE -> "✅"
            Verdict.SUSPICIOUS -> "⚠️"
            Verdict.MALICIOUS -> "🚨"
            Verdict.UNKNOWN -> "❓"
        }

        return buildString {
            appendLine("$emoji Mehr Guard Analysis Report")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("🔗 URL: ${url.take(100)}${if (url.length > 100) "..." else ""}")
            appendLine()
            appendLine("📊 Risk Score: ${assessment.score}/100")
            appendLine("🏷️ Verdict: ${assessment.verdict.name}")
            appendLine("📈 Confidence: ${(assessment.confidence * 100).toInt()}%")

            if (assessment.flags.isNotEmpty()) {
                appendLine()
                appendLine("⚠️ Risk Factors:")
                assessment.flags.take(5).forEach { flag ->
                    appendLine("  • $flag")
                }
                if (assessment.flags.size > 5) {
                    appendLine("  • ...and ${assessment.flags.size - 5} more")
                }
            }

            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Analyzed with Mehr Guard 🛡️")
            appendLine("Kotlin Multiplatform QRishing Detector")
        }
    }

    /**
     * Generate HTML report for sharing/email.
     */
    fun generateHtmlReport(url: String, assessment: RiskAssessment): String {
        val color = when (assessment.verdict) {
            Verdict.SAFE -> "#00D68F"
            Verdict.SUSPICIOUS -> "#FFAA00"
            Verdict.MALICIOUS -> "#FF3D71"
            Verdict.UNKNOWN -> "#8B949E"
        }

        val emoji = when (assessment.verdict) {
            Verdict.SAFE -> "✅"
            Verdict.SUSPICIOUS -> "⚠️"
            Verdict.MALICIOUS -> "🚨"
            Verdict.UNKNOWN -> "❓"
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Mehr Guard Analysis Report</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: #1a1a2e;
                        color: #edf2f4;
                        padding: 20px;
                        max-width: 600px;
                        margin: 0 auto;
                    }
                    .card {
                        background: #16213e;
                        border-radius: 16px;
                        padding: 24px;
                        box-shadow: 0 4px 20px rgba(0,0,0,0.3);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 20px;
                    }
                    .score {
                        font-size: 64px;
                        font-weight: bold;
                        color: $color;
                        text-align: center;
                    }
                    .verdict {
                        font-size: 24px;
                        color: $color;
                        text-align: center;
                        margin-bottom: 20px;
                    }
                    .url {
                        background: rgba(255,255,255,0.1);
                        padding: 12px;
                        border-radius: 8px;
                        word-break: break-all;
                        font-family: monospace;
                        font-size: 12px;
                    }
                    .flags {
                        margin-top: 20px;
                    }
                    .flag {
                        background: rgba(255,255,255,0.05);
                        padding: 8px 12px;
                        border-radius: 6px;
                        margin: 4px 0;
                        font-size: 14px;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 20px;
                        opacity: 0.7;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <h1>🛡️ Mehr Guard</h1>
                        <p>Analysis Report</p>
                    </div>

                    <div class="score">${emoji} ${assessment.score}</div>
                    <div class="verdict">${assessment.verdict.name}</div>

                    <div class="url">${escapeHtml(url.take(200))}</div>

                    ${if (assessment.flags.isNotEmpty()) """
                    <div class="flags">
                        <strong>Risk Factors:</strong>
                        ${assessment.flags.take(10).joinToString("") { flag ->
                            "<div class=\"flag\">• ${escapeHtml(flag)}</div>"
                        }}
                    </div>
                    """ else ""}

                    <div class="footer">
                        <p>Confidence: ${(assessment.confidence * 100).toInt()}%</p>
                        <p>Analyzed with Mehr Guard - Kotlin Multiplatform QRishing Detector</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Generate JSON export for programmatic use.
     */
    fun generateJsonReport(url: String, assessment: RiskAssessment): String {
        return buildString {
            appendLine("{")
            appendLine("  \"url\": \"${escapeJsonString(url)}\",")
            appendLine("  \"score\": ${assessment.score},")
            appendLine("  \"verdict\": \"${assessment.verdict.name}\",")
            appendLine("  \"confidence\": ${assessment.confidence},")
            appendLine("  \"flags\": [")
            assessment.flags.forEachIndexed { index, flag ->
                val comma = if (index < assessment.flags.size - 1) "," else ""
                appendLine("    \"${escapeJsonString(flag)}\"$comma")
            }
            appendLine("  ],")
            appendLine("  \"details\": {")
            appendLine("    \"heuristicScore\": ${assessment.details.heuristicScore},")
            appendLine("    \"mlScore\": ${assessment.details.mlScore},")
            appendLine("    \"brandScore\": ${assessment.details.brandScore},")
            appendLine("    \"tldScore\": ${assessment.details.tldScore},")
            assessment.details.brandMatch?.let {
                appendLine("    \"brandMatch\": \"${escapeJsonString(it)}\",")
            }
            appendLine("    \"tld\": \"${escapeJsonString(assessment.details.tld ?: "")}\"")
            appendLine("  },")
            appendLine("  \"analyzedBy\": \"Mehr Guard v1.0.0\",")
            appendLine("  \"platform\": \"Kotlin Multiplatform\"")
            appendLine("}")
        }
    }

    /**
     * Escape HTML special characters.
     */
    private fun escapeHtml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }

    /**
     * Escape JSON special characters.
     */
    private fun escapeJsonString(input: String): String {
        return buildString {
            for (char in input) {
                when (char) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (char.code < 32) {
                            append("\\u${char.code.toString(16).padStart(4, '0')}")
                        } else {
                            append(char)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class for share content.
 */
data class ShareContent(
    val title: String,
    val text: String,
    val html: String? = null,
    val url: String? = null
)
