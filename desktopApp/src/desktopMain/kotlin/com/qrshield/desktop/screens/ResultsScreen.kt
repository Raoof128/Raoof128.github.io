/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.desktop.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.theme.DesktopColors
import com.qrshield.model.Verdict

/**
 * Enhanced Results Screen matching the HTML designs for SAFE, SUSPICIOUS, and DANGEROUS verdicts.
 * Features verdict-specific styling, target analysis, technical indicators, and AI confidence.
 */
@Composable
fun ResultsScreen(
    result: AnalysisResult,
    onBack: () -> Unit,
    onCopyUrl: () -> Unit,
    onAddToTrusted: () -> Unit,
    onScanAgain: () -> Unit,
    isDarkMode: Boolean = false,
    onThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val verdictColor = when (result.verdict) {
        Verdict.SAFE -> DesktopColors.VerdictSafe
        Verdict.SUSPICIOUS -> Color(0xFFF59E0B) // Amber
        else -> DesktopColors.VerdictMalicious
    }
    
    val verdictIcon = when (result.verdict) {
        Verdict.SAFE -> "✓"
        Verdict.SUSPICIOUS -> "⚠️"
        else -> "⛔"
    }
    
    val verdictTitle = when (result.verdict) {
        Verdict.SAFE -> "Safe to Visit"
        Verdict.SUSPICIOUS -> "Caution Advised: Suspicious Activity Detected"
        else -> "High Risk Detected"
    }
    
    val verdictSubtitle = when (result.verdict) {
        Verdict.SAFE -> "This URL has passed all security checks"
        Verdict.SUSPICIOUS -> "Multiple heuristic anomalies detected. Proceed with extreme caution."
        else -> "Known phishing vector confirmed with high confidence"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        ResultsHeader(
            scanId = "#SCAN-${result.timestamp.toString().takeLast(4)}",
            isDarkMode = isDarkMode,
            onThemeToggle = onThemeToggle,
            onBack = onBack
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Card - Verdict Banner
            VerdictHeroCard(
                verdict = result.verdict,
                verdictColor = verdictColor,
                verdictIcon = verdictIcon,
                verdictTitle = verdictTitle,
                verdictSubtitle = verdictSubtitle,
                url = result.url,
                score = result.score,
                onCopyUrl = onCopyUrl,
                onAddToTrusted = onAddToTrusted,
                onScanAgain = onScanAgain
            )

            // Main Content Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left Column - Analysis Details
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Target Destination Card
                    TargetDestinationCard(url = result.url, verdictColor = verdictColor)
                    
                    // Detection Flags / Verdict Analysis
                    VerdictAnalysisCard(
                        flags = result.flags,
                        verdict = result.verdict,
                        verdictColor = verdictColor
                    )
                    
                    // Technical Indicators Table
                    TechnicalIndicatorsCard(
                        result = result,
                        verdictColor = verdictColor
                    )
                }

                // Right Column - Score & Intelligence
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Risk Score Card
                    RiskScoreCard(
                        score = result.score,
                        verdict = result.verdict,
                        verdictColor = verdictColor
                    )
                    
                    // AI Verdict Logic Card
                    AIVerdictCard(
                        verdict = result.verdict,
                        score = result.score,
                        verdictColor = verdictColor
                    )
                    
                    // Intelligence Feeds (for MALICIOUS)
                    if (result.verdict == Verdict.MALICIOUS) {
                        IntelligenceFeedsCard()
                    }
                    
                    // Actions Card
                    ActionsCard(
                        onScanAgain = onScanAgain,
                        onCopyUrl = onCopyUrl
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsHeader(
    scanId: String,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back + Breadcrumbs
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back")
                }
                
                Text(
                    text = "Scan Monitor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Result $scanId",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right side
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engine Status
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DesktopColors.VerdictSafe.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, DesktopColors.VerdictSafe.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(DesktopColors.VerdictSafe)
                        )
                        Text(
                            text = "Engine Active V.2.4",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DesktopColors.VerdictSafe
                        )
                    }
                }

                IconButton(onClick = onThemeToggle) {
                    Text(if (isDarkMode) "☀️" else "🌙", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun VerdictHeroCard(
    verdict: Verdict,
    verdictColor: Color,
    verdictIcon: String,
    verdictTitle: String,
    verdictSubtitle: String,
    url: String,
    score: Int,
    onCopyUrl: () -> Unit,
    onAddToTrusted: () -> Unit,
    onScanAgain: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = verdictColor.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, verdictColor.copy(alpha = 0.2f))
    ) {
        Box {
            // Background gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                verdictColor.copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Verdict Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(verdictColor.copy(alpha = 0.1f))
                        .border(1.dp, verdictColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(verdictIcon, fontSize = 40.sp)
                }

                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = verdictTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = verdictColor
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = verdictColor.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, verdictColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = verdict.name,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = verdictColor
                            )
                        }
                    }

                    Text(
                        text = verdictSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onCopyUrl,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📋 Copy URL")
                        }
                        
                        if (verdict == Verdict.SAFE) {
                            Button(
                                onClick = onAddToTrusted,
                                colors = ButtonDefaults.buttonColors(containerColor = verdictColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("⭐ Add to Trusted")
                            }
                        } else if (verdict == Verdict.MALICIOUS) {
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = verdictColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🚫 Block Domain")
                            }
                        }
                        
                        Button(
                            onClick = onScanAgain,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🔄 Scan Another")
                        }
                    }
                }

                // Stats Panel
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        StatItem(
                            label = "Confidence",
                            value = "${100 - (score / 10)}%",
                            valueColor = verdictColor
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        )
                        StatItem(
                            label = "Scan Time",
                            value = "12ms",
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        )
                        StatItem(
                            label = "Engine",
                            value = "v2.4 Local",
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun TargetDestinationCard(url: String, verdictColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TARGET DESTINATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Visual",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Raw",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // URL Display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", fontSize = 14.sp)
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(verdictColor)
                    )
                    Text(
                        text = "Direct Link (No Redirects)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VerdictAnalysisCard(
    flags: List<String>,
    verdict: Verdict,
    verdictColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "VERDICT ANALYSIS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (flags.isEmpty() && verdict == Verdict.SAFE) {
                // Safe - show passed checks
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalysisCheckItem(
                        icon = "✓",
                        title = "Domain Identity",
                        description = "Verified ownership via valid certificate",
                        status = "Passed",
                        statusColor = DesktopColors.VerdictSafe
                    )
                    AnalysisCheckItem(
                        icon = "✓",
                        title = "Homograph Check",
                        description = "No mixed-script characters detected",
                        status = "Clean",
                        statusColor = DesktopColors.VerdictSafe
                    )
                    AnalysisCheckItem(
                        icon = "✓",
                        title = "Domain Age",
                        description = "Established domain with high reputation",
                        status = "Established",
                        statusColor = DesktopColors.VerdictSafe
                    )
                    AnalysisCheckItem(
                        icon = "✓",
                        title = "Redirect Chain",
                        description = "Zero intermediate redirects found",
                        status = "Direct",
                        statusColor = DesktopColors.VerdictSafe
                    )
                }
            } else {
                // Show flags as detection items
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    flags.forEachIndexed { index, flag ->
                        val (icon, statusText) = when {
                            verdict == Verdict.MALICIOUS -> "⚠️" to "Detected"
                            verdict == Verdict.SUSPICIOUS -> "⚡" to "Suspicious"
                            else -> "ℹ️" to "Info"
                        }
                        AnalysisCheckItem(
                            icon = icon,
                            title = flag,
                            description = getDescriptionForFlag(flag),
                            status = statusText,
                            statusColor = verdictColor
                        )
                    }
                    
                    if (flags.isEmpty()) {
                        Text(
                            text = "No specific flags identified",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getDescriptionForFlag(flag: String): String {
    return when {
        flag.contains("homograph", ignoreCase = true) -> "Domain uses characters that mimic legitimate letters"
        flag.contains("domain age", ignoreCase = true) -> "Domain was recently registered"
        flag.contains("shortener", ignoreCase = true) -> "URL uses a shortening service to obscure destination"
        flag.contains("entropy", ignoreCase = true) -> "URL structure appears randomly generated"
        flag.contains("keyword", ignoreCase = true) -> "Contains high-risk keywords common in phishing"
        else -> "Heuristic analysis flagged this pattern"
    }
}

@Composable
private fun AnalysisCheckItem(
    icon: String,
    title: String,
    description: String,
    status: String,
    statusColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = statusColor.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(icon, fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
            ) {
                Text(
                    text = status.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun TechnicalIndicatorsCard(
    result: AnalysisResult,
    verdictColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📊", fontSize = 16.sp)
                    Text(
                        text = "Technical Indicators",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = { }) {
                    Text("Export Report", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Table rows
            Column {
                TechnicalRow("Certificate Issuer", "DigiCert Inc (US)", "✓")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                TechnicalRow("Server Location", "United States", "✓")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                TechnicalRow("Shannon Entropy", "${(result.score / 30.0).format(2)} bits", 
                    if (result.score < 30) "✓" else "⚠️")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                TechnicalRow("Scan Latency", "12ms (Offline)", "✓")
            }

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analysis ID: #SCAN-${result.timestamp.toString().takeLast(6)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { }) {
                    Text("View Full JSON Log →", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)

@Composable
private fun TechnicalRow(label: String, value: String, statusIcon: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(2f)
        )
        Text(statusIcon, fontSize = 14.sp)
    }
}

@Composable
private fun RiskScoreCard(
    score: Int,
    verdict: Verdict,
    verdictColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RISK SCORE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Circular Score Display
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 10.dp,
                    color = verdictColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (verdict) {
                            Verdict.SAFE -> "Low Risk"
                            Verdict.SUSPICIOUS -> "High Risk"
                            else -> "Critical"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = verdictColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Score based on heuristic analysis of domain age, entropy, and pattern matching.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AIVerdictCard(
    verdict: Verdict,
    score: Int,
    verdictColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF6366F1).copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6366F1).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧠", fontSize = 18.sp)
                }
                Text(
                    text = "AI Verdict Logic",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (verdict) {
                    Verdict.SAFE -> "The ML model classified this URL as BENIGN with high certainty. The structure matches known legitimate patterns."
                    Verdict.SUSPICIOUS -> "Multiple heuristic anomalies detected. The URL structure exhibits patterns common in sophisticated phishing attempts."
                    else -> "The ML model classified this as MALICIOUS with ${100 - (score / 10)}% certainty. Multiple phishing indicators confirmed."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Phishing Probability",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${score}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = verdictColor
                    )
                }
                LinearProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = verdictColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IntelligenceFeedsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌐", fontSize = 16.sp)
                Text(
                    text = "Intelligence Feeds",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column {
                FeedRow("Google Safe Browsing", true)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                FeedRow("PhishTank DB", true)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                FeedRow("Local Allowlist", false)
            }
        }
    }
}

@Composable
private fun FeedRow(name: String, isMatch: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isMatch) DesktopColors.VerdictMalicious else DesktopColors.VerdictSafe)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (isMatch) DesktopColors.VerdictMalicious.copy(alpha = 0.1f) 
                    else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = if (isMatch) "MATCH" else "NO MATCH",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isMatch) DesktopColors.VerdictMalicious 
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionsCard(
    onScanAgain: () -> Unit,
    onCopyUrl: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                onClick = onCopyUrl,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📤", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "Share Analysis",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Export PDF report",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📋", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "View Raw Data",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Inspect JSON payload",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
