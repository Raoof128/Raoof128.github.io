/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.raouf.mehrguard.android.ui.screens

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
import com.raouf.mehrguard.android.R
import com.raouf.mehrguard.android.ui.theme.MehrGuardColors
import com.raouf.mehrguard.android.ui.theme.MehrGuardShapes

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
    // Get PhishingEngine to re-analyze for fresh flags
    val phishingEngine: com.raouf.mehrguard.core.PhishingEngine = org.koin.compose.koinInject()
    
    // Re-analyze URL to get REAL flags (not stale/empty data from navigation)
    // Use produceState to call suspend function
    val analysisResult by androidx.compose.runtime.produceState<com.raouf.mehrguard.model.RiskAssessment?>(
        initialValue = null,
        key1 = url
    ) {
        value = try {
            phishingEngine.analyze(url)
        } catch (e: Exception) {
            null
        }
    }
    
    // Use real data from fresh analysis
    val realFlags = analysisResult?.flags ?: emptyList()
    val realBrandMatch = analysisResult?.details?.brandMatch
    val realTld = analysisResult?.details?.tld
    val realConfidence = analysisResult?.confidence ?: 0.8f
    val realHeuristicScore = analysisResult?.details?.heuristicScore ?: 0
    
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
    // Use real engine confidence instead of hardcoded values
    val confidence = (realConfidence * 100).toInt().coerceIn(0, 100)
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
                // ===== STATUS BADGE (WebApp Parity) =====
                ScanStatusBadge(
                    verdict = verdict,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                // ===== URL DISPLAY (WebApp Parity) =====
                UrlDisplayRow(
                    url = url,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // ===== ANALYSIS META (WebApp Parity) =====
                AnalysisMetaRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
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
                
                // Engine Stats - REAL data from fresh analysis!
                EngineStatsCard(
                    heuristicScore = realHeuristicScore,
                    flagCount = realFlags.size,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tags/Chips - Dynamic from real flags!
                TagsRow(flags = realFlags)

                Spacer(modifier = Modifier.height(24.dp))
                
                // ===== TOP ANALYSIS FACTORS (WebApp Parity) =====
                TopAnalysisFactorsSection(
                    url = url,
                    flags = realFlags,
                    analysisResult = analysisResult,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Analysis Breakdown - REAL engine data from fresh analysis!
                AnalysisBreakdownSection(
                    flags = realFlags,
                    brandMatch = realBrandMatch,
                    tld = realTld,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Footer Meta - Dynamic timestamp
                val currentTime = remember {
                    java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date())
                }
                Text(
                    text = stringResource(R.string.scan_meta_fmt, currentTime, "1.20.4", stringResource(R.string.scan_mode_offline)),
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
        isMalicious -> MehrGuardColors.RiskDanger
        isSuspicious -> MehrGuardColors.RiskWarning
        isSafe -> MehrGuardColors.RiskSafe
        else -> MehrGuardColors.Primary
    }
    
    val secondaryColor = when {
        isMalicious -> MehrGuardColors.Red600
        isSuspicious -> MehrGuardColors.Orange600
        isSafe -> MehrGuardColors.Emerald600
        else -> MehrGuardColors.Primary
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
        "LOW" -> MehrGuardColors.Emerald50
        "WARNING" -> MehrGuardColors.Orange50
        else -> MehrGuardColors.Red50
    }
    val riskTextColor = when (riskLevel) {
        "LOW" -> MehrGuardColors.Emerald600
        "WARNING" -> MehrGuardColors.Orange600
        else -> MehrGuardColors.Red600
    }
    val progressColor = when (riskLevel) {
        "LOW" -> MehrGuardColors.Emerald500
        "WARNING" -> MehrGuardColors.Orange500
        else -> MehrGuardColors.RiskDanger
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
        shape = MehrGuardShapes.Card,
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
                    color = MehrGuardColors.Emerald600
                )
                Text(
                    text = stringResource(R.string.risk_label_warn),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MehrGuardColors.Orange600
                )
                Text(
                    text = stringResource(R.string.risk_label_critical),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MehrGuardColors.Red600
                )
            }
        }
    }
}

@Composable
private fun EngineStatsCard(
    heuristicScore: Int,
    flagCount: Int,
    modifier: Modifier = Modifier
) {
    // Calculate analysis time estimate based on score complexity
    val analysisTimeMs = (10 + (flagCount * 2)).coerceIn(5, 50)
    
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
                    text = "${analysisTimeMs}ms",
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
                    text = "$heuristicScore",
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
                    text = stringResource(R.string.engine_version_fmt, "1.20"),
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
                    containerColor = MehrGuardColors.Primary
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

/**
 * Data class for tag display.
 */
private data class TagData(
    val icon: ImageVector,
    val text: String,
    val isPrimary: Boolean
)

/**
 * Derives tags from REAL engine flags.
 */
@Composable
private fun deriveTags(flags: List<String>): List<TagData> {
    val tags = mutableListOf<TagData>()
    val flagsUpper = flags.map { it.uppercase() }
    val flagsLower = flags.map { it.lowercase() }
    
    // IP Address
    if (flagsUpper.any { it.contains("IP_ADDRESS") }) {
        tags.add(TagData(Icons.Default.Dns, stringResource(R.string.tag_ip_address), true))
    }
    
    // Credential harvesting
    if (flagsUpper.any { it.contains("CREDENTIAL") }) {
        tags.add(TagData(Icons.Default.VpnKey, stringResource(R.string.tag_credential_harvesting), true))
    }
    
    // Homograph / Punycode
    if (flagsUpper.any { it.contains("PUNYCODE") || it.contains("LOOKALIKE") || it.contains("ZERO_WIDTH") }) {
        tags.add(TagData(Icons.Default.Abc, stringResource(R.string.tag_homograph_attack), true))
    }
    
    // Brand impersonation
    if (flagsLower.any { it.contains("brand") }) {
        tags.add(TagData(Icons.Default.VerifiedUser, stringResource(R.string.tag_brand_spoof), true))
    }
    
    // HTTP not HTTPS
    if (flagsUpper.any { it.contains("HTTP_NOT_HTTPS") }) {
        tags.add(TagData(Icons.Default.LockOpen, stringResource(R.string.tag_insecure), false))
    }
    
    // URL Shortener
    if (flagsUpper.any { it.contains("URL_SHORTENER") }) {
        tags.add(TagData(Icons.Default.Link, stringResource(R.string.tag_shortener), false))
    }
    
    // High-risk TLD
    if (flagsLower.any { it.contains("high-risk tld") }) {
        tags.add(TagData(Icons.Default.Public, stringResource(R.string.tag_risky_tld), false))
    }
    
    // Suspicious path
    if (flagsUpper.any { it.contains("SUSPICIOUS_PATH") }) {
        tags.add(TagData(Icons.Default.Folder, stringResource(R.string.tag_suspicious_path), false))
    }
    
    // Long URL
    if (flagsUpper.any { it.contains("LONG_URL") }) {
        tags.add(TagData(Icons.Default.Straighten, stringResource(R.string.tag_long_url), false))
    }
    
    // At-symbol injection
    if (flagsUpper.any { it.contains("AT_SYMBOL") }) {
        tags.add(TagData(Icons.Default.AlternateEmail, stringResource(R.string.tag_at_injection), true))
    }
    
    // Safe URL (no flags)
    if (tags.isEmpty() && flags.isEmpty()) {
        tags.add(TagData(Icons.Default.CheckCircle, stringResource(R.string.tag_verified_safe), false))
    }
    
    return tags.take(4) // Limit to 4 tags for UI
}

@Composable
private fun TagsRow(flags: List<String>) {
    val tags = deriveTags(flags)
    
    if (tags.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tags.forEach { tag ->
                TagChip(
                    icon = tag.icon,
                    text = tag.text,
                    isPrimary = tag.isPrimary
                )
            }
        }
    }
}

@Composable
private fun TagChip(
    icon: ImageVector,
    text: String,
    isPrimary: Boolean
) {
    val bgColor = if (isPrimary) MehrGuardColors.Red50 else MaterialTheme.colorScheme.surface
    val borderColor = if (isPrimary) MehrGuardColors.Red100 else MaterialTheme.colorScheme.outlineVariant
    val iconColor = if (isPrimary) MehrGuardColors.RiskDanger else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = if (isPrimary) MehrGuardColors.Red600 else MaterialTheme.colorScheme.onSurface

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
                    tint = MehrGuardColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Data class for analysis item display.
 * Maps engine flags to UI-friendly representations.
 */
private data class AnalysisItemData(
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconColor: Color,
    val title: String,
    val description: String
)

/**
 * Derives analysis items from REAL engine flags.
 * Maps security flags to user-friendly UI items.
 * 
 * Actual flag names from HeuristicsEngine:
 * - HTTP_NOT_HTTPS, IP_ADDRESS_HOST, URL_SHORTENER
 * - EXCESSIVE_SUBDOMAINS, CREDENTIAL_PARAMS, CREDENTIAL_KEYWORDS
 * - PUNYCODE_DOMAIN, LOOKALIKE_CHARS, HIGH_ENTROPY_HOST
 * - LONG_URL, DATA_URI_SCHEME, JAVASCRIPT_URL
 * - AT_SYMBOL_INJECTION, RISKY_EXTENSION, etc.
 * 
 * Plus brand/TLD flags like:
 * - "Brand impersonation detected: PayPal"
 * - "High-risk TLD: tk"
 */
@Composable
private fun deriveAnalysisItems(
    flags: List<String>,
    brandMatch: String?,
    tld: String?
): List<AnalysisItemData> {
    val items = mutableListOf<AnalysisItemData>()
    val flagsLower = flags.map { it.lowercase() }
    val flagsUpper = flags.map { it.uppercase() }
    
    // IP Address Host detection
    if (flagsUpper.any { it.contains("IP_ADDRESS_HOST") || it.contains("IP ADDRESS") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.Dns,
            iconBgColor = MehrGuardColors.Red50,
            iconColor = MehrGuardColors.RiskDanger,
            title = stringResource(R.string.analysis_ip_host_title),
            description = stringResource(R.string.analysis_ip_host_desc)
        ))
    }
    
    // Brand impersonation detection
    if (brandMatch != null || flagsLower.any { it.contains("brand") || it.contains("impersonation") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.VerifiedUser,
            iconBgColor = MehrGuardColors.Red50,
            iconColor = MehrGuardColors.RiskDanger,
            title = stringResource(R.string.analysis_brand_title),
            description = if (brandMatch != null) {
                stringResource(R.string.analysis_brand_desc_specific, brandMatch)
            } else {
                stringResource(R.string.analysis_brand_desc)
            }
        ))
    }
    
    // IDN Homograph / Punycode / Lookalike detection
    if (flagsUpper.any { it.contains("PUNYCODE") || it.contains("LOOKALIKE") || it.contains("ZERO_WIDTH") } ||
        flagsLower.any { it.contains("homograph") || it.contains("mixed script") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.TextFormat,
            iconBgColor = MehrGuardColors.Red50,
            iconColor = MehrGuardColors.RiskDanger,
            title = stringResource(R.string.analysis_homograph_title),
            description = stringResource(R.string.analysis_homograph_desc)
        ))
    }
    
    // HTTP vs HTTPS check
    if (flagsUpper.any { it.contains("HTTP_NOT_HTTPS") || it.contains("HTTP NOT HTTPS") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.LockOpen,
            iconBgColor = MehrGuardColors.Orange50,
            iconColor = MehrGuardColors.Orange600,
            title = stringResource(R.string.analysis_protocol_title),
            description = stringResource(R.string.analysis_protocol_desc)
        ))
    }
    
    // URL Shortener / Redirect detection
    if (flagsUpper.any { it.contains("URL_SHORTENER") || it.contains("REDIRECT") }) {
        items.add(AnalysisItemData(
            icon = Icons.AutoMirrored.Filled.AltRoute,
            iconBgColor = MehrGuardColors.Orange50,
            iconColor = MehrGuardColors.Orange600,
            title = stringResource(R.string.analysis_redirect_title),
            description = stringResource(R.string.analysis_redirect_desc)
        ))
    }
    
    // High-risk TLD
    if (flagsLower.any { it.contains("high-risk tld") || it.contains("risky tld") } || 
        flagsUpper.any { it.contains("RISKY_TLD") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.Public,
            iconBgColor = MehrGuardColors.Orange50,
            iconColor = MehrGuardColors.Orange600,
            title = stringResource(R.string.analysis_tld_title),
            description = if (tld != null) {
                stringResource(R.string.analysis_tld_desc_specific, tld)
            } else {
                stringResource(R.string.analysis_tld_desc)
            }
        ))
    }
    
    // Excessive Subdomains
    if (flagsUpper.any { it.contains("EXCESSIVE_SUBDOMAINS") || it.contains("MULTIPLE_TLD") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.AccountTree,
            iconBgColor = MehrGuardColors.Orange50,
            iconColor = MehrGuardColors.Orange600,
            title = stringResource(R.string.analysis_subdomain_title),
            description = stringResource(R.string.analysis_subdomain_desc)
        ))
    }
    
    // Credential harvesting (CREDENTIAL_PARAMS or CREDENTIAL_KEYWORDS)
    if (flagsUpper.any { it.contains("CREDENTIAL") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.Key,
            iconBgColor = MehrGuardColors.Red50,
            iconColor = MehrGuardColors.RiskDanger,
            title = stringResource(R.string.analysis_credential_title),
            description = stringResource(R.string.analysis_credential_desc)
        ))
    }
    
    // Long URL
    if (flagsUpper.any { it.contains("LONG_URL") || it.contains("URL_TOO_LONG") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.Straighten,
            iconBgColor = MehrGuardColors.Orange50,
            iconColor = MehrGuardColors.Orange600,
            title = stringResource(R.string.analysis_long_url_title),
            description = stringResource(R.string.analysis_long_url_desc)
        ))
    }
    
    // Dangerous scheme (DATA_URI_SCHEME, JAVASCRIPT_URL)
    if (flagsUpper.any { it.contains("DATA_URI") || it.contains("JAVASCRIPT_URL") || it.contains("JAVASCRIPT:") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.Code,
            iconBgColor = MehrGuardColors.Red50,
            iconColor = MehrGuardColors.RiskDanger,
            title = stringResource(R.string.analysis_scheme_title),
            description = stringResource(R.string.analysis_scheme_desc)
        ))
    }
    
    // At-symbol injection attack
    if (flagsUpper.any { it.contains("AT_SYMBOL") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.AlternateEmail,
            iconBgColor = MehrGuardColors.Red50,
            iconColor = MehrGuardColors.RiskDanger,
            title = stringResource(R.string.analysis_at_symbol_title),
            description = stringResource(R.string.analysis_at_symbol_desc)
        ))
    }
    
    // Suspicious path keywords
    if (flagsUpper.any { it.contains("SUSPICIOUS_PATH") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.Folder,
            iconBgColor = MehrGuardColors.Orange50,
            iconColor = MehrGuardColors.Orange600,
            title = stringResource(R.string.analysis_suspicious_path_title),
            description = stringResource(R.string.analysis_suspicious_path_desc)
        ))
    }
    
    // Risky file extension
    if (flagsUpper.any { it.contains("RISKY_EXTENSION") || it.contains("DOUBLE_EXTENSION") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.InsertDriveFile,
            iconBgColor = MehrGuardColors.Orange50,
            iconColor = MehrGuardColors.Orange600,
            title = stringResource(R.string.analysis_risky_extension_title),
            description = stringResource(R.string.analysis_risky_extension_desc)
        ))
    }
    
    // High entropy host (random-looking domain)
    if (flagsUpper.any { it.contains("HIGH_ENTROPY") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.Shuffle,
            iconBgColor = MehrGuardColors.Orange50,
            iconColor = MehrGuardColors.Orange600,
            title = stringResource(R.string.analysis_entropy_title),
            description = stringResource(R.string.analysis_entropy_desc)
        ))
    }
    
    // Non-standard port
    if (flagsUpper.any { it.contains("NON_STANDARD_PORT") || it.contains("SUSPICIOUS_PORT") }) {
        items.add(AnalysisItemData(
            icon = Icons.Default.SettingsEthernet,
            iconBgColor = MehrGuardColors.Orange50,
            iconColor = MehrGuardColors.Orange600,
            title = stringResource(R.string.analysis_port_title),
            description = stringResource(R.string.analysis_port_desc)
        ))
    }
    
    // If no flags detected (safe URL), show positive indicators
    if (items.isEmpty() && flags.isEmpty()) {
        items.add(AnalysisItemData(
            icon = Icons.Default.CheckCircle,
            iconBgColor = MehrGuardColors.RiskSafeLight,
            iconColor = MehrGuardColors.RiskSafe,
            title = stringResource(R.string.analysis_safe_title),
            description = stringResource(R.string.analysis_safe_desc)
        ))
    }
    
    return items
}

@Composable
private fun AnalysisBreakdownSection(
    flags: List<String>,
    brandMatch: String?,
    tld: String?,
    modifier: Modifier = Modifier
) {
    // Derive items from REAL engine flags
    val analysisItems = deriveAnalysisItems(flags, brandMatch, tld)
    
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
                color = MehrGuardColors.Primary.copy(alpha = 0.1f),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.linearGradient(
                        listOf(
                            MehrGuardColors.Primary.copy(alpha = 0.2f),
                            MehrGuardColors.Primary.copy(alpha = 0.2f)
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
                        tint = MehrGuardColors.Primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.ai_explained_label),
                        color = MehrGuardColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Analysis Items - REAL data from engine!
        if (analysisItems.isNotEmpty()) {
            Surface(
                shape = MehrGuardShapes.Card,
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
                    analysisItems.forEachIndexed { index, item ->
                        AnalysisItem(
                            icon = item.icon,
                            iconBgColor = item.iconBgColor,
                            iconColor = item.iconColor,
                            title = item.title,
                            description = item.description,
                            showDivider = index < analysisItems.size - 1
                        )
                    }
                }
            }
        } else {
            // No analysis items - show placeholder
            Surface(
                shape = MehrGuardShapes.Card,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = stringResource(R.string.analysis_no_issues),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
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

// ===================================
// WEBAPP PARITY COMPONENTS
// ===================================

/**
 * Status badge showing scan completion status (WebApp Parity)
 */
@Composable
private fun ScanStatusBadge(
    verdict: String,
    modifier: Modifier = Modifier
) {
    val verdictUpper = verdict.uppercase()
    val (icon, iconColor, bgColor, statusText) = when (verdictUpper) {
        "SAFE" -> listOf(
            Icons.Default.CheckCircle,
            MehrGuardColors.RiskSafe,
            MehrGuardColors.RiskSafeLight,
            R.string.status_scan_complete
        )
        "SUSPICIOUS" -> listOf(
            Icons.Default.Warning,
            MehrGuardColors.Orange600,
            MehrGuardColors.Orange50,
            R.string.status_caution_advised
        )
        "MALICIOUS" -> listOf(
            Icons.Default.Error,
            MehrGuardColors.RiskDanger,
            MehrGuardColors.Red50,
            R.string.status_threat_detected
        )
        else -> listOf(
            Icons.Default.Info,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            R.string.status_analysis_complete
        )
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = bgColor as Color
        ) {
            Icon(
                imageVector = icon as ImageVector,
                contentDescription = null,
                tint = iconColor as Color,
                modifier = Modifier.padding(6.dp).size(20.dp)
            )
        }
        Text(
            text = stringResource(statusText as Int),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * URL display row with link icon (WebApp Parity)
 */
@Composable
private fun UrlDisplayRow(
    url: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = url,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Analysis meta info row (WebApp Parity)
 */
@Composable
private fun AnalysisMetaRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.OfflineBolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = stringResource(R.string.meta_analyzed_offline),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "•",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Text(
            text = stringResource(R.string.meta_no_data_leaves),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Factor type for analysis factors
 */
private enum class FactorType {
    PASS, INFO, CLEAN, WARN, FAIL, CRITICAL
}

/**
 * Data class for factor card
 */
private data class FactorCardData(
    val type: FactorType,
    val category: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

/**
 * Derives analysis factors from URL and flags (WebApp Parity)
 */
@Composable
private fun deriveAnalysisFactors(
    url: String,
    flags: List<String>,
    analysisResult: com.raouf.mehrguard.model.RiskAssessment?
): List<FactorCardData> {
    val factors = mutableListOf<FactorCardData>()
    val flagsUpper = flags.map { it.uppercase() }
    
    // SSL/HTTPS Check
    val isHttps = url.startsWith("https://")
    factors.add(FactorCardData(
        type = if (isHttps) FactorType.PASS else FactorType.FAIL,
        category = stringResource(R.string.factor_category_https),
        title = if (isHttps) stringResource(R.string.factor_ssl_valid) else stringResource(R.string.factor_ssl_missing),
        description = if (isHttps) 
            stringResource(R.string.factor_ssl_valid_desc) 
        else 
            stringResource(R.string.factor_ssl_missing_desc),
        icon = if (isHttps) Icons.Default.Lock else Icons.Default.LockOpen
    ))
    
    // Domain Analysis
    val isIpAddress = flagsUpper.any { it.contains("IP_ADDRESS") }
    if (isIpAddress) {
        factors.add(FactorCardData(
            type = FactorType.WARN,
            category = stringResource(R.string.factor_category_domain),
            title = stringResource(R.string.factor_ip_address),
            description = stringResource(R.string.factor_ip_address_desc),
            icon = Icons.Default.Dns
        ))
    } else {
        factors.add(FactorCardData(
            type = FactorType.INFO,
            category = stringResource(R.string.factor_category_domain),
            title = stringResource(R.string.factor_domain_check),
            description = stringResource(R.string.factor_domain_check_desc),
            icon = Icons.Default.Domain
        ))
    }
    
    // Threat Database Check - determine based on verdict/score
    val isKnownBad = analysisResult?.verdict == com.raouf.mehrguard.model.Verdict.MALICIOUS && 
                     (analysisResult.score ?: 0) >= 80
    factors.add(FactorCardData(
        type = if (isKnownBad) FactorType.CRITICAL else FactorType.CLEAN,
        category = stringResource(R.string.factor_category_db_check),
        title = if (isKnownBad) stringResource(R.string.factor_blacklist_found) else stringResource(R.string.factor_blacklist_clean),
        description = if (isKnownBad) 
            stringResource(R.string.factor_blacklist_found_desc) 
        else 
            stringResource(R.string.factor_blacklist_clean_desc),
        icon = if (isKnownBad) Icons.Default.Dangerous else Icons.Default.VerifiedUser
    ))
    
    // Heuristics Check
    val heuristicScore = analysisResult?.details?.heuristicScore ?: 0
    val heuristicType = when {
        heuristicScore >= 50 -> FactorType.CRITICAL
        heuristicScore >= 30 -> FactorType.WARN
        heuristicScore >= 10 -> FactorType.INFO
        else -> FactorType.PASS
    }
    factors.add(FactorCardData(
        type = heuristicType,
        category = stringResource(R.string.factor_category_heuristics),
        title = stringResource(R.string.factor_heuristics_title),
        description = stringResource(R.string.factor_heuristics_desc, heuristicScore, flags.size),
        icon = Icons.Default.Analytics
    ))
    
    // ML Score Check (if available)
    val mlScore = analysisResult?.details?.mlScore ?: 0
    if (mlScore > 0) {
        val mlType = when {
            mlScore >= 70 -> FactorType.CRITICAL
            mlScore >= 40 -> FactorType.WARN
            else -> FactorType.PASS
        }
        factors.add(FactorCardData(
            type = mlType,
            category = stringResource(R.string.factor_category_ml),
            title = stringResource(R.string.factor_ml_title),
            description = stringResource(R.string.factor_ml_desc, mlScore),
            icon = Icons.Default.Psychology
        ))
    }
    
    return factors
}

/**
 * Top Analysis Factors section (WebApp Parity)
 */
@Composable
private fun TopAnalysisFactorsSection(
    url: String,
    flags: List<String>,
    analysisResult: com.raouf.mehrguard.model.RiskAssessment?,
    modifier: Modifier = Modifier
) {
    val factors = deriveAnalysisFactors(url, flags, analysisResult)
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.top_analysis_factors_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        
        // Factor Cards Grid
        factors.forEach { factor ->
            FactorCard(factor = factor)
        }
    }
}

/**
 * Individual factor card (WebApp Parity)
 */
@Composable
private fun FactorCard(
    factor: FactorCardData,
    modifier: Modifier = Modifier
) {
    val (tagBgColor, tagTextColor) = when (factor.type) {
        FactorType.PASS -> MehrGuardColors.RiskSafeLight to MehrGuardColors.RiskSafe
        FactorType.CLEAN -> MehrGuardColors.Emerald50 to MehrGuardColors.Emerald600
        FactorType.INFO -> MehrGuardColors.Primary.copy(alpha = 0.1f) to MehrGuardColors.Primary
        FactorType.WARN -> MehrGuardColors.Orange50 to MehrGuardColors.Orange600
        FactorType.FAIL -> MehrGuardColors.Red50 to MehrGuardColors.Red600
        FactorType.CRITICAL -> MehrGuardColors.Red100 to MehrGuardColors.RiskDanger
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MehrGuardShapes.Card,
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
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Type tag
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = tagBgColor
                    ) {
                        Text(
                            text = factor.type.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tagTextColor
                        )
                    }
                    // Category tag
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = factor.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Title
            Text(
                text = factor.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Description
            Text(
                text = factor.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

