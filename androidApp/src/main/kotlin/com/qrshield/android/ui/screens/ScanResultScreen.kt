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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale

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
import androidx.compose.ui.res.stringResource
import com.qrshield.android.R
import com.qrshield.android.ui.theme.QRShieldColors
import com.qrshield.android.ui.theme.QRShieldShapes

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
        "MALICIOUS" -> stringResource(R.string.verdict_malicious)
        "SUSPICIOUS" -> stringResource(R.string.verdict_suspicious)
        "SAFE" -> stringResource(R.string.verdict_safe)
        else -> stringResource(R.string.verdict_unknown)
    }
    val threatType = when (verdict.uppercase()) {
        "MALICIOUS" -> stringResource(R.string.threat_type_phishing)
        "SUSPICIOUS" -> stringResource(R.string.threat_type_suspicious)
        "SAFE" -> stringResource(R.string.threat_type_verified)
        else -> stringResource(R.string.threat_type_unknown)
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
                        text = stringResource(R.string.scan_result_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.cd_share))
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
                    rawVerdict = verdict,
                    displayVerdict = displayVerdict,
                    threatType = threatType,
                    confidence = confidence
                )

                // Risk Score Card
                RiskScoreCard(
                    score = severityScore,
                    verdict = verdict,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                // Engine Stats
                EngineStatsCard(
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
                        text = stringResource(R.string.target_url_label),
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
                    text = stringResource(R.string.scan_meta_fmt, "Oct 24, 14:32", "4.2.0", stringResource(R.string.scan_mode_offline)),
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
                onShareClick = onShareClick,
                onOpenClick = { /* TODO: Implement Sandbox */ },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun VerdictHeader(
    rawVerdict: String,
    displayVerdict: String,
    threatType: String,
    confidence: Int
) {
    // Determine colors and icon based on RAW verdict (MALICIOUS, SUSPICIOUS, SAFE, UNKNOWN)
    val verdictUpper = rawVerdict.uppercase()
    val isSafe = verdictUpper == "SAFE"
    val isSuspicious = verdictUpper == "SUSPICIOUS"
    val isMalicious = verdictUpper == "MALICIOUS"
    
    val primaryColor = when {
        isMalicious -> QRShieldColors.RiskDanger
        isSuspicious -> QRShieldColors.RiskWarning
        isSafe -> QRShieldColors.RiskSafe
        else -> QRShieldColors.Primary
    }
    
    val secondaryColor = when {
        isMalicious -> QRShieldColors.Red600
        isSuspicious -> QRShieldColors.Orange600
        isSafe -> QRShieldColors.Emerald600
        else -> QRShieldColors.Primary
    }
    
    val verdictIcon = when {
        isMalicious -> Icons.Default.GppBad
        isSuspicious -> Icons.Default.GppMaybe
        isSafe -> Icons.Default.GppGood
        else -> Icons.Default.Shield
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val pingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingScale"
    )
    
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pingAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pulsing Icon Container
        Box(contentAlignment = Alignment.Center) {
            // Ping effect (outer ring)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pingScale)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = pingAlpha))
            )
            
            // Pulse effect (inner ring)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.2f))
            )
            
            // Icon container
            Surface(
                modifier = Modifier.padding(4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.background)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(primaryColor, secondaryColor)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = verdictIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Verdict text
        Text(
            text = displayVerdict,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), // Larger text
            color = MaterialTheme.colorScheme.onBackground
        )

        // Threat type badge and confidence
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = primaryColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
            ) {
                Text(
                    text = threatType.uppercase(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = stringResource(R.string.confidence_fmt, confidence),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RiskScoreCard(
    score: Float,
    verdict: String = "UNKNOWN",
    modifier: Modifier = Modifier
) {
    // Use verdict to determine risk level, not just score
    val verdictUpper = verdict.uppercase()
    val isSafe = verdictUpper == "SAFE"
    val isSuspicious = verdictUpper == "SUSPICIOUS"
    val isMalicious = verdictUpper == "MALICIOUS"
    
    // Determine risk level based on verdict first, score as fallback
    val riskLevel = when {
        isMalicious -> "CRITICAL"
        isSuspicious -> "WARNING"
        isSafe -> "LOW"
        score < 3 -> "LOW"
        score < 7 -> "WARNING"
        else -> "CRITICAL"
    }
    
    // Determine colors based on risk level
    val riskBgColor = when (riskLevel) {
        "LOW" -> QRShieldColors.Emerald50
        "WARNING" -> QRShieldColors.Orange50
        else -> QRShieldColors.Red50
    }
    val riskTextColor = when (riskLevel) {
        "LOW" -> QRShieldColors.Emerald600
        "WARNING" -> QRShieldColors.Orange600
        else -> QRShieldColors.Red600
    }
    val progressColor = when (riskLevel) {
        "LOW" -> QRShieldColors.Emerald500
        "WARNING" -> QRShieldColors.Orange500
        else -> QRShieldColors.RiskDanger
    }
    
    // Calculate active segments based on verdict and score
    val activeSegments = when {
        isMalicious -> 5  // Full bar for malicious
        isSuspicious -> 3 // Medium bar for suspicious
        isSafe -> 1       // Low bar for safe
        else -> (score / 2).toInt().coerceIn(1, 5)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = QRShieldShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.risk_assessment_title),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = riskBgColor
                ) {
                    Text(
                        text = when (riskLevel) {
                            "LOW" -> stringResource(R.string.risk_level_low)
                            "WARNING" -> stringResource(R.string.risk_level_warning)
                            else -> stringResource(R.string.risk_level_critical)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = riskTextColor
                    )
                }
            }

            // Segmented Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth().height(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 5 Segments - use pre-calculated activeSegments based on verdict
                for (i in 1..5) {
                    val isSegmentActive = i <= activeSegments
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isSegmentActive) progressColor
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.risk_label_safe),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = QRShieldColors.Emerald600
                )
                Text(
                    text = stringResource(R.string.risk_label_warn),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = QRShieldColors.Orange600
                )
                Text(
                    text = stringResource(R.string.risk_label_critical),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = QRShieldColors.Red600
                )
            }
        }
    }
}

@Composable
private fun EngineStatsCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.analysis_time_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "4ms",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.heuristics_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "142",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Box(
                modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.engine_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.engine_version_fmt, "2.4"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    onShareClick: () -> Unit,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Share (Secondary)
            OutlinedButton(
                onClick = onShareClick,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_share),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Sandbox (Primary)
            Button(
                onClick = onOpenClick,
                modifier = Modifier.weight(1.5f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QRShieldColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_open_safely),
                    fontWeight = FontWeight.Bold
                )
            }
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
            text = stringResource(R.string.tag_credential_harvesting),
            isPrimary = true
        )
        TagChip(
            icon = Icons.Default.Abc,
            text = stringResource(R.string.tag_homograph_attack),
            isPrimary = false
        )
        TagChip(
            icon = Icons.Default.Public,
            text = stringResource(R.string.tag_new_domain),
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
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
                    contentDescription = stringResource(R.string.cd_copy_url),
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
                text = stringResource(R.string.analysis_breakdown_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Surface(
                shape = RoundedCornerShape(9999.dp),
                color = QRShieldColors.Primary.copy(alpha = 0.1f),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
                        text = stringResource(R.string.ai_explained_label),
                        color = QRShieldColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Analysis Items
        Surface(
            shape = QRShieldShapes.Card,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
                    title = stringResource(R.string.analysis_domain_age_title),
                    description = stringResource(R.string.analysis_domain_age_desc),
                    highlightText = "24 hours ago",
                    showDivider = true
                )
                AnalysisItem(
                    icon = Icons.AutoMirrored.Filled.AltRoute,
                    iconBgColor = QRShieldColors.Orange50,
                    iconColor = QRShieldColors.Orange600,
                    title = stringResource(R.string.analysis_redirect_title),
                    description = stringResource(R.string.analysis_redirect_desc),
                    highlightText = "3 levels",
                    showDivider = true
                )
                AnalysisItem(
                    icon = Icons.Default.Storage,
                    iconBgColor = QRShieldColors.Gray100,
                    iconColor = QRShieldColors.Gray600,
                    title = stringResource(R.string.analysis_db_match_title),
                    description = stringResource(R.string.analysis_db_match_desc),
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


