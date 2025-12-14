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

package com.qrshield.desktop.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.theme.DesktopColors

/**
 * Enhanced UI components for QR-SHIELD Desktop
 * Matching Web App feature-parity
 *
 * @author QR-SHIELD Team
 * @since 1.1.4
 */

// ============================================
// SAMPLE URL SECTION (Try Now for Judges)
// ============================================

data class SampleUrl(
    val url: String,
    val label: String,
    val verdict: String, // SAFE, SUSPICIOUS, MALICIOUS
    val icon: String
)

val sampleUrls = listOf(
    SampleUrl("https://google.com", "google.com", "SAFE", "✅"),
    SampleUrl("https://paypa1-secure.tk/login", "paypa1-secure.tk", "MALICIOUS", "❌"),
    SampleUrl("https://commbank.secure-verify.ml/account", "commbank.ml", "MALICIOUS", "❌"),
    SampleUrl("https://bit.ly/3xYz123", "bit.ly shortener", "SUSPICIOUS", "⚠️")
)

@Composable
fun SampleUrlsSection(
    onUrlSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "🧪", fontSize = 18.sp)
            Text(
                text = "Try These Examples",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sampleUrls.forEach { sample ->
                SampleUrlChip(
                    sample = sample,
                    onClick = { onUrlSelected(sample.url) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SampleUrlChip(
    sample: SampleUrl,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = when (sample.verdict) {
        "SAFE" -> DesktopColors.VerdictSafe
        "SUSPICIOUS" -> DesktopColors.VerdictSuspicious
        "MALICIOUS" -> DesktopColors.VerdictMalicious
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = chipColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, chipColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = sample.icon, fontSize = 14.sp)
            Text(
                text = sample.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = chipColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================
// KEYBOARD SHORTCUTS HINT
// ============================================

@Composable
fun KeyboardShortcutsHint() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⌨️", fontSize = 16.sp)
            Text(
                text = "Shortcuts:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ShortcutBadge("⌘L", "Focus")
            ShortcutBadge("⌘V", "Paste")
            ShortcutBadge("↵", "Analyze")
            ShortcutBadge("⎋", "Clear")
            ShortcutBadge("⌘D", "Theme")
        }
    }
}

@Composable
fun ShortcutBadge(key: String, action: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Text(
                text = key,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = action,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// ============================================
// SIGNAL EXPLANATIONS (Expandable Cards)
// ============================================

data class SignalExplanation(
    val name: String,
    val icon: String,
    val severity: String, // high, medium, low
    val whatItChecks: String,
    val whyItMatters: String,
    val riskImpact: String,
    val counterfactual: String
)

fun getSignalExplanation(flag: String): SignalExplanation {
    val flagLower = flag.lowercase()
    
    return when {
        flagLower.contains("brand") || flagLower.contains("impersonation") -> SignalExplanation(
            name = "Brand Impersonation",
            icon = "🏢",
            severity = "high",
            whatItChecks = "Matches against 500+ known brand names in unexpected positions",
            whyItMatters = "Phishers often use trusted brand names to deceive victims",
            riskImpact = "+25-30 points — Critical indicator",
            counterfactual = "If the URL used the official brand domain (e.g., paypal.com), this signal would not trigger."
        )
        flagLower.contains("tld") || flagLower.contains(".tk") || flagLower.contains(".ml") -> SignalExplanation(
            name = "Suspicious TLD",
            icon = "🌍",
            severity = "high",
            whatItChecks = "Domain uses a TLD associated with abuse (.tk, .ml, .cf, .gq)",
            whyItMatters = "Free/cheap TLDs are disproportionately used for phishing",
            riskImpact = "+20-25 points — Strong indicator",
            counterfactual = "Using a reputable TLD like .com, .org, or .au would remove this flag."
        )
        flagLower.contains("http") && !flagLower.contains("https") -> SignalExplanation(
            name = "No HTTPS",
            icon = "🔓",
            severity = "medium",
            whatItChecks = "URL uses HTTP instead of HTTPS",
            whyItMatters = "Legitimate sites use HTTPS for security; HTTP allows interception",
            riskImpact = "+10-15 points — Moderate indicator",
            counterfactual = "Switching to HTTPS would remove this warning."
        )
        flagLower.contains("login") || flagLower.contains("credential") -> SignalExplanation(
            name = "Credential Harvesting Path",
            icon = "🔐",
            severity = "medium",
            whatItChecks = "URL path contains login, signin, password, or account keywords",
            whyItMatters = "Phishing pages target credential entry points",
            riskImpact = "+15-20 points — Significant indicator",
            counterfactual = "A non-login path would reduce the risk score."
        )
        flagLower.contains("typo") -> SignalExplanation(
            name = "Typosquatting",
            icon = "✏️",
            severity = "high",
            whatItChecks = "Domain uses character substitutions (paypa1 instead of paypal)",
            whyItMatters = "Typosquatting tricks users who mistype URLs",
            riskImpact = "+20-30 points — Strong indicator",
            counterfactual = "Using the correct spelling would remove this detection."
        )
        flagLower.contains("shortener") || flagLower.contains("bit.ly") -> SignalExplanation(
            name = "URL Shortener",
            icon = "🔗",
            severity = "medium",
            whatItChecks = "URL uses a shortening service (bit.ly, tinyurl, etc.)",
            whyItMatters = "Shorteners hide the true destination",
            riskImpact = "+10-15 points — Caution indicator",
            counterfactual = "Using the full destination URL would remove this flag."
        )
        flagLower.contains("@") || flagLower.contains("at symbol") -> SignalExplanation(
            name = "@ Symbol Injection",
            icon = "📧",
            severity = "critical",
            whatItChecks = "URL contains @ symbol to hide true destination",
            whyItMatters = "google.com@evil.com actually goes to evil.com",
            riskImpact = "+30-40 points — Critical attack pattern",
            counterfactual = "Removing the @ symbol would eliminate this severe warning."
        )
        flagLower.contains("ip") || flagLower.contains("numeric") -> SignalExplanation(
            name = "IP Address Host",
            icon = "🔢",
            severity = "high",
            whatItChecks = "URL uses IP address instead of domain name",
            whyItMatters = "Legitimate sites use domains; IPs often indicate phishing",
            riskImpact = "+20-25 points — Strong indicator",
            counterfactual = "Using a proper domain name would remove this flag."
        )
        flagLower.contains("long") || flagLower.contains("length") -> SignalExplanation(
            name = "Excessively Long URL",
            icon = "📏",
            severity = "low",
            whatItChecks = "URL exceeds 100 characters",
            whyItMatters = "Long URLs can hide malicious parameters",
            riskImpact = "+5-10 points — Minor signal",
            counterfactual = "A shorter, cleaner URL would reduce this contribution."
        )
        else -> SignalExplanation(
            name = flag,
            icon = "⚠️",
            severity = "medium",
            whatItChecks = "This URL triggered a security check",
            whyItMatters = "The pattern matches known phishing characteristics",
            riskImpact = "+10-20 points — Contributes to overall risk score",
            counterfactual = "Removing or changing the suspicious pattern would reduce the overall risk score."
        )
    }
}

@Composable
fun ExpandableSignalCard(
    flag: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val signal = remember(flag) { getSignalExplanation(flag) }

    val severityColor = when (signal.severity) {
        "critical" -> DesktopColors.VerdictMalicious
        "high" -> DesktopColors.VerdictMalicious.copy(alpha = 0.8f)
        "medium" -> DesktopColors.VerdictSuspicious
        else -> DesktopColors.VerdictSuspicious.copy(alpha = 0.7f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = severityColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.25f))
    ) {
        Column {
            // Header (clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = signal.icon, fontSize = 18.sp)
                    Column {
                        Text(
                            text = signal.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Severity badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = severityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = signal.severity.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                    }

                    // Expand icon
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SignalDetailRow("🔍 What it checks:", signal.whatItChecks)
                    SignalDetailRow("💡 Why it matters:", signal.whyItMatters)
                    SignalDetailRow("📊 Risk impact:", signal.riskImpact)
                    
                    // Counterfactual (special styling)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "💭", fontSize = 14.sp)
                        Column {
                            Text(
                                text = "What would reduce this?",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = DesktopColors.VerdictSafe
                            )
                            Text(
                                text = signal.counterfactual,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignalDetailRow(label: String, value: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================
// CONFIDENCE INDICATOR
// ============================================

@Composable
fun ConfidenceIndicator(
    score: Int,
    flagCount: Int,
    modifier: Modifier = Modifier
) {
    val (dots, level, label) = remember(score, flagCount) {
        calculateConfidence(score, flagCount)
    }

    val dotColor = when (level) {
        "very-high" -> DesktopColors.VerdictSafe
        "high" -> DesktopColors.VerdictSafe.copy(alpha = 0.8f)
        "medium" -> DesktopColors.VerdictSuspicious
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = dotColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < dots) dotColor else dotColor.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            Text(
                text = "$label Confidence",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = dotColor
            )
        }
    }
}

private fun calculateConfidence(score: Int, signalCount: Int): Triple<Int, String, String> {
    var dots = 2
    var level = "low"
    var label = "Low"

    if (score >= 80 || score <= 15) {
        dots = 5
        level = "very-high"
        label = "Very High"
    } else if (score >= 65 || score <= 25) {
        dots = 4
        level = "high"
        label = "High"
    } else if (score >= 50 || score <= 35) {
        dots = 3
        level = "medium"
        label = "Medium"
    }

    // Boost confidence if multiple signals agree
    if (signalCount >= 4 && dots < 5) {
        dots += 1
        if (level == "medium") {
            level = "high"
            label = "High"
        }
    }

    return Triple(dots, level, label)
}

// ============================================
// HELP CARD (First-time user guidance)
// ============================================

@Composable
fun HelpCard(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DesktopColors.BrandPrimary.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, DesktopColors.BrandPrimary.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "👋", fontSize = 24.sp)
                    Text(
                        text = "Welcome to QR-SHIELD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }

            Text(
                text = "Paste any URL to check for phishing threats. Our AI analyzes 25+ security signals completely offline.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureBullet("🔒", "100% offline")
                FeatureBullet("🤖", "AI-powered")
                FeatureBullet("🎯", "25+ heuristics")
            }
        }
    }
}

@Composable
fun FeatureBullet(icon: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 14.sp)
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
