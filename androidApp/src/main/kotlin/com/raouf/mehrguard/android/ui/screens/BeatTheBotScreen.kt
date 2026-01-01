/*
 * Copyright 2025-2026 Mehr Guard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.raouf.mehrguard.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raouf.mehrguard.android.R
import com.raouf.mehrguard.android.ui.theme.MehrGuardColors
import com.raouf.mehrguard.android.ui.theme.MehrGuardShapes
import com.raouf.mehrguard.android.ui.viewmodels.GameResult
import com.raouf.mehrguard.android.ui.viewmodels.GameState
import com.raouf.mehrguard.ui.components.CommonBrainVisualizer
import kotlin.random.Random

/**
 * Beat the Bot Training Screen
 * Interactive training game where users identify phishing vs legitimate URLs
 * Matches the HTML "Training: Beat the Bot" design (game.html/game.css)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeatTheBotScreen(
    onBackClick: () -> Unit = {},
    onEndSession: () -> Unit = {},
    onResetGame: () -> Unit = {},
    onPhishingClick: () -> Unit = {},
    onLegitimateClick: () -> Unit = {},
    onHintDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
    uiState: GameState
) {
    // Extract values
    val currentScore = uiState.score
    val streak = uiState.streak
    val currentRound = uiState.currentRoundIndex + 1
    val totalRounds = uiState.totalRounds
    val currentUrl = uiState.currentUrl?.url ?: stringResource(R.string.beat_the_bot_loading)
    val smsContext = uiState.currentUrl?.context ?: ""
    val smsFrom = uiState.currentUrl?.sender ?: ""
    val lastResult = uiState.lastResult

    // Derived UI state
    val timeRemaining = remember(uiState.timeRemainingSeconds) {
        String.format("%02d:%02d", uiState.timeRemainingSeconds / 60, uiState.timeRemainingSeconds % 60)
    }

    // Simulate Bot Score for "VS Mode" based on current round to match HTML "Live Scoreboard"
    val botScore = remember(currentRound) {
        (currentRound * 95) + Random.nextInt(0, 50)
    }

    val sessionId = remember { "GAME-${(1000..9999).random()}" }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.beat_the_bot_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.beat_the_bot_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Reset Game Button (2025 Android Best Practice: IconButton in TopAppBar)
                    IconButton(
                        onClick = onResetGame,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cd_reset_game),
                            tint = MehrGuardColors.Primary
                        )
                    }
                    // Session ID Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.beat_the_bot_session_fmt, sessionId),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onEndSession) {
                        Text(
                            text = stringResource(R.string.end_session),
                            color = MehrGuardColors.Red500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Live Scoreboard (VS Mode)
            LiveScoreboardCard(
                playerScore = currentScore,
                playerStreak = streak,
                botScore = botScore,
                currentRound = currentRound,
                totalRounds = totalRounds,
                timeRemaining = timeRemaining
            )

            // 2. Challenge Card (Browser Preview)
            BrowserPreviewCard(
                url = currentUrl,
                smsContext = smsContext,
                smsFrom = smsFrom
            )

            // 3. Decision Controls
            GameDecisionButtons(
                onPhishingClick = onPhishingClick,
                onLegitimateClick = onLegitimateClick,
                enabled = lastResult == null,
                lastResult = lastResult
            )

            // 4. Brain Visualizer Section (Always visible, matching iOS)
            BrainVisualizerSection(
                detectedSignals = if (lastResult != null && uiState.currentUrl?.isPhishing == true) {
                    uiState.currentUrl.signals
                } else {
                    emptyList()
                }
            )

            // 5. Analysis / Feedback (Shows after decision)
            AnimatedVisibility(
                visible = lastResult != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                RoundAnalysisCard(
                    result = lastResult,
                    isPhishing = uiState.currentUrl?.isPhishing == true,
                    signals = uiState.currentUrl?.signals ?: emptyList(),
                    onNextRound = onHintDismiss // Reusing dismiss as next round trigger for now
                )
            }
        }
    }
}

/**
 * Matches logic from `game.html` -> `scoreboard-card`
 */
@Composable
private fun LiveScoreboardCard(
    playerScore: Int,
    playerStreak: Int,
    botScore: Int,
    currentRound: Int,
    totalRounds: Int,
    timeRemaining: String
) {
    Card(
        shape = MehrGuardShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: "Live Scoreboard" + VS Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.beat_the_bot_live_scoreboard),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.round_progress_fmt, currentRound, totalRounds),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = MehrGuardColors.Purple500.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MehrGuardColors.Purple500.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = stringResource(R.string.beat_the_bot_vs_mode),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MehrGuardColors.Purple500
                    )
                }
                
                // Timer
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MehrGuardColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = timeRemaining,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Scores
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Player Row
                ScoreBarRow(
                    label = stringResource(R.string.beat_the_bot_you),
                    score = playerScore,
                    color = MehrGuardColors.Primary,
                    secondaryText = stringResource(R.string.beat_the_bot_streak_fmt, playerStreak),
                    progress = (playerScore / 2000f).coerceIn(0f, 1f)
                )

                // Bot Row
                ScoreBarRow(
                    label = stringResource(R.string.beat_the_bot_bot_name),
                    score = botScore,
                    color = MehrGuardColors.Gray500,
                    secondaryText = stringResource(R.string.beat_the_bot_latency_fmt, 2),
                    progress = (botScore / 2000f).coerceIn(0f, 1f),
                    icon = Icons.Default.SmartToy
                )
            }
        }
    }
}

@Composable
private fun ScoreBarRow(
    label: String,
    score: Int,
    color: Color,
    secondaryText: String,
    progress: Float,
    icon: ImageVector? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (icon != null) {
                    Icon(icon, null, modifier = Modifier.size(16.dp), tint = color)
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = stringResource(R.string.beat_the_bot_pts_fmt, score),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        
        Text(
            text = secondaryText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

/**
 * Matches `browser-preview` in `game.html`
 */
@Composable
private fun BrowserPreviewCard(
    url: String,
    smsContext: String,
    smsFrom: String
) {
    Card(
        shape = MehrGuardShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { // Inner container
            // 1. Browser Header (Dots + URL Bar)
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Traffic Lights
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F57)))
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                    Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF28C840)))
                }

                // URL Input Config
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = url,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. Content Preview (SMS + Mock Web)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SMS Bubble
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Sms, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.sms_from_fmt, smsFrom),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = smsContext,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Mock Web Content (Placeholder)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Web, 
                            null, 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            stringResource(R.string.beat_the_bot_preview_hidden),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Matches `decision-buttons` in `game.html`
 */
@Composable
private fun GameDecisionButtons(
    onPhishingClick: () -> Unit,
    onLegitimateClick: () -> Unit,
    enabled: Boolean,
    lastResult: GameResult?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Phishing Button (Danger)
        GameDecisionButton(
            label = stringResource(R.string.phishing),
            subLabel = stringResource(R.string.report_threat),
            icon = Icons.Default.Warning,
            color = MehrGuardColors.Red500,
            onClick = onPhishingClick,
            modifier = Modifier.weight(1f),
            enabled = enabled
        )

        // Legitimate Button (Success)
        GameDecisionButton(
            label = stringResource(R.string.legitimate),
            subLabel = stringResource(R.string.mark_safe),
            icon = Icons.Default.CheckCircle,
            color = MehrGuardColors.Emerald500,
            onClick = onLegitimateClick,
            modifier = Modifier.weight(1f),
            enabled = enabled
        )
    }
}

@Composable
private fun GameDecisionButton(
    label: String,
    subLabel: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    val contentAlpha = if (enabled) 1f else 0.5f
    
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(120.dp),
        shape = MehrGuardShapes.Card,
        color = containerColor,
        border = BorderStroke(
            1.dp, 
            Brush.linearGradient(
                listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.1f))
            )
        ),
        shadowElevation = if (enabled) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color.copy(alpha = contentAlpha), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
            )
        }
    }
}

/**
 * Matches `analysis-card` in `game.html`
 */
@Composable
private fun RoundAnalysisCard(
    result: GameResult?,
    isPhishing: Boolean,
    signals: List<String>,
    onNextRound: () -> Unit
) {
    if (result == null) return

    val isCorrect = result == GameResult.CORRECT
    val badgeColor = if (isCorrect) MehrGuardColors.Emerald500 else MehrGuardColors.Red500
    val badgeIcon = if (isCorrect) Icons.Default.Check else Icons.Default.Close
    val badgeText = if (isCorrect) stringResource(R.string.beat_the_bot_correct) else stringResource(R.string.beat_the_bot_incorrect)
    val badgeDesc = if (isCorrect) stringResource(R.string.beat_the_bot_correct_desc) else if (isPhishing) stringResource(R.string.beat_the_bot_incorrect_phishing) else stringResource(R.string.beat_the_bot_incorrect_legit)

    Card(
        shape = MehrGuardShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Result Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(badgeIcon, null, tint = badgeColor, modifier = Modifier.size(24.dp))
                    }
                }
                Column {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = badgeDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = badgeColor
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Bot Reasoning (Brain Visualizer)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.beat_the_bot_why_flagged, if (isPhishing) stringResource(R.string.beat_the_bot_suspicious) else stringResource(R.string.beat_the_bot_safe)),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onNextRound,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MehrGuardColors.Primary)
            ) {
                Text(stringResource(R.string.beat_the_bot_next_round), fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Brain Visualizer Section - matches iOS brainVisualizerSection
 * Shows AI analysis visualization with detected signals
 */
@Composable
private fun BrainVisualizerSection(
    detectedSignals: List<String>
) {
    Card(
        shape = MehrGuardShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row with title and signal count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.beat_the_bot_ai_analysis),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (detectedSignals.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.beat_the_bot_signals_detected_fmt, detectedSignals.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MehrGuardColors.Red500
                    )
                }
            }
            
            // Brain Visualizer Component
            CommonBrainVisualizer(
                detectedSignals = detectedSignals,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
