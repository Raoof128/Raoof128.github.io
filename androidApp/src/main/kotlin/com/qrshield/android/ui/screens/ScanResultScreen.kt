/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.qrshield.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.android.ui.theme.QRShieldColors

/**
 * Scan Result Summary Screen
 * Matches the HTML "Scan Result Summary" design with:
 * - Verdict header with risk icon
 * - Risk score card with progress bar
 * - Tag chips (scrollable)
 * - Target URL display
 * - Explainable security list (AI Analysis)
 * - Footer with scan metadata
 * - Bottom action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    // Navigation params
    url: String = "https://example.com",
    verdict: String = "UNKNOWN",
    score: Int = 0,
    // Callbacks
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onBlockClick: () -> Unit = {},
    onIgnoreClick: () -> Unit = {},
    onCopyUrl: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Derive display values from navigation params
    val displayVerdict = when (verdict.uppercase()) {
        "MALICIOUS" -> "High Risk Detected"
        "SUSPICIOUS" -> "Suspicious Activity"
        "SAFE" -> "Safe to Open"
        else -> "Unknown Risk"
    }
    val threatType = when (verdict.uppercase()) {
        "MALICIOUS" -> "Phishing"
        "SUSPICIOUS" -> "Suspicious"
        "SAFE" -> "Verified"
        else -> "Unknown"
    }
    val confidence = when (verdict.uppercase()) {
        "MALICIOUS" -> 98
        "SUSPICIOUS" -> 75
        "SAFE" -> 99
        else -> 50
    }
    val severityScore = score / 10f
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scan Result",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(bottom = 180.dp) // Space for bottom actions
            ) {
                // Verdict Header
                VerdictHeader(
                    verdict = displayVerdict,
                    threatType = threatType,
                    confidence = confidence
                )

                // Risk Score Card
                RiskScoreCard(
                    score = severityScore,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tags/Chips
                TagsRow()

                Spacer(modifier = Modifier.height(24.dp))

                // Target URL
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TARGET URL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    UrlDisplayCard(url = url, onCopyClick = onCopyUrl)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Analysis Breakdown
                AnalysisBreakdownSection(
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Footer Meta
                Text(
                    text = "Scanned: Oct 24, 14:32 • Engine v4.2.0 • Offline",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Bottom Action Buttons
            BottomActionBar(
                onBlockClick = onBlockClick,
                onIgnoreClick = onIgnoreClick,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun VerdictHeader(
    verdict: String,
    threatType: String,
    confidence: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pulsing Icon Container
        Box(contentAlignment = Alignment.Center) {
            // Glow effect
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                QRShieldColors.RiskDanger.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
            // Icon container
            Surface(
                modifier = Modifier.padding(4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(QRShieldColors.RiskDanger, QRShieldColors.Red600)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GppBad,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Verdict text
        Text(
            text = verdict,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Threat type badge and confidence
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = QRShieldColors.RiskDanger.copy(alpha = 0.1f),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        listOf(
                            QRShieldColors.RiskDanger.copy(alpha = 0.2f),
                            QRShieldColors.RiskDanger.copy(alpha = 0.2f)
                        )
                    )
                )
            ) {
                Text(
                    text = threatType.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = QRShieldColors.RiskDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "$confidence% Confidence",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RiskScoreCard(
    score: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "SEVERITY SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "/10",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ThermostatAuto,
                    contentDescription = null,
                    tint = QRShieldColors.RiskDanger,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(score / 10f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(QRShieldColors.RiskDanger, QRShieldColors.Red600)
                            )
                        )
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(6.dp),
                            ambientColor = QRShieldColors.RiskDanger.copy(alpha = 0.5f),
                            spotColor = QRShieldColors.RiskDanger.copy(alpha = 0.5f)
                        )
                )
            }

            Text(
                text = "This URL exhibits patterns consistent with known malicious campaigns targeting enterprise credentials.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TagsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TagChip(
            icon = Icons.Default.VpnKey,
            text = "Credential Harvesting",
            isPrimary = true
        )
        TagChip(
            icon = Icons.Default.Abc,
            text = "Homograph Attack",
            isPrimary = false
        )
        TagChip(
            icon = Icons.Default.Public,
            text = "New Domain",
            isPrimary = false
        )
    }
}

@Composable
private fun TagChip(
    icon: ImageVector,
    text: String,
    isPrimary: Boolean
) {
    val bgColor = if (isPrimary) QRShieldColors.Red50 else MaterialTheme.colorScheme.surface
    val borderColor = if (isPrimary) QRShieldColors.Red100 else MaterialTheme.colorScheme.outlineVariant
    val iconColor = if (isPrimary) QRShieldColors.RiskDanger else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = if (isPrimary) QRShieldColors.Red600 else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(9999.dp),
        color = bgColor,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(listOf(borderColor, borderColor))
        ),
        shadowElevation = if (!isPrimary) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}

@Composable
private fun UrlDisplayCard(
    url: String,
    onCopyClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.colorScheme.outlineVariant
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCopyClick) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy URL",
                    tint = QRShieldColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalysisBreakdownSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analysis Breakdown",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = QRShieldColors.Primary.copy(alpha = 0.1f),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        listOf(
                            QRShieldColors.Primary.copy(alpha = 0.2f),
                            QRShieldColors.Primary.copy(alpha = 0.2f)
                        )
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = QRShieldColors.Primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AI Explained",
                        color = QRShieldColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Analysis Items
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column {
                AnalysisItem(
                    icon = Icons.Default.History,
                    iconBgColor = QRShieldColors.Red50,
                    iconColor = QRShieldColors.RiskDanger,
                    title = "Domain Age Alert",
                    description = "Domain created less than 24 hours ago. High likelihood of disposable phishing infrastructure.",
                    highlightText = "24 hours ago",
                    showDivider = true
                )
                AnalysisItem(
                    icon = Icons.Default.AltRoute,
                    iconBgColor = QRShieldColors.Orange50,
                    iconColor = QRShieldColors.Orange600,
                    title = "Suspicious Redirection",
                    description = "URL contains 3 levels of redirection designed to bypass email filters.",
                    highlightText = "3 levels",
                    showDivider = true
                )
                AnalysisItem(
                    icon = Icons.Default.Storage,
                    iconBgColor = QRShieldColors.Gray100,
                    iconColor = QRShieldColors.Gray600,
                    title = "Database Match",
                    description = "Pattern matches known threat signature #4421 from global threat intelligence.",
                    highlightText = "#4421",
                    showDivider = false
                )
            }
        }
    }
}

@Composable
private fun AnalysisItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    description: String,
    highlightText: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun BottomActionBar(
    onBlockClick: () -> Unit,
    onIgnoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Block Button
            Button(
                onClick = onBlockClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(9999.dp),
                        ambientColor = QRShieldColors.RiskDanger.copy(alpha = 0.3f),
                        spotColor = QRShieldColors.RiskDanger.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QRShieldColors.RiskDanger,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Block & Report to IT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Ignore Button
            OutlinedButton(
                onClick = onIgnoreClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.outlineVariant,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                )
            ) {
                Text(
                    text = "Ignore Warning (Admin)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
