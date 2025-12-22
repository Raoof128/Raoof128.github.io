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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
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
import com.qrshield.android.ui.components.CircularProgressIndicatorWithPercentage
import com.qrshield.android.ui.theme.QRShieldColors
import androidx.compose.ui.res.stringResource
import com.qrshield.android.R

/**
 * Learning Centre Screen
 * Matches the HTML "Learning Centre" design with:
 * - Progress section with circular progress
 * - Daily tip card
 * - Modules list (In Progress, Completed, New)
 * - Report Threat mini module
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningCentreScreen(
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onViewCertificate: () -> Unit = {},
    onReadTip: () -> Unit = {},
    onModuleClick: (String) -> Unit = {},
    onReportThreatClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.learning_centre_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
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
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Section
            ProgressSection(
                progress = 0.6f,
                modulesCompleted = 3,
                totalModules = 5,
                onViewCertificate = onViewCertificate
            )

            // Daily Tip Card
            DailyTipCard(
                title = "URL Shorteners",
                description = "Learn how attackers hide malicious links.",
                onReadTip = onReadTip
            )

            // Modules Section
            ModulesSection(onModuleClick = onModuleClick)

            // Report Threat Mini Module
            ReportThreatCard(onClick = onReportThreatClick)

            // Bottom padding for navigation bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ProgressSection(
    progress: Float,
    modulesCompleted: Int,
    totalModules: Int,
    onViewCertificate: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.your_progress),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onViewCertificate) {
                Text(
                    text = stringResource(R.string.view_certificate),
                    color = QRShieldColors.Primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress
                CircularProgressIndicatorWithPercentage(
                    progress = progress,
                    size = 80.dp,
                    strokeWidth = 7.dp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.qr_shield_certified),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.modules_completed_fmt, modulesCompleted, totalModules),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // On Track Badge
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = QRShieldColors.Emerald50
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = QRShieldColors.Emerald600,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.on_track),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = QRShieldColors.Emerald600
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyTipCard(
    title: String,
    description: String,
    onReadTip: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = QRShieldColors.Primary.copy(alpha = 0.05f),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                listOf(
                    QRShieldColors.Primary.copy(alpha = 0.1f),
                    QRShieldColors.Primary.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Label
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = QRShieldColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.daily_tip_label),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = QRShieldColors.Primary
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Read Tip Button
                TextButton(
                    onClick = onReadTip,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.read_tip),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = QRShieldColors.Primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = QRShieldColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Decorative Image Placeholder
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                QRShieldColors.Primary.copy(alpha = 0.2f),
                                QRShieldColors.Primary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = QRShieldColors.Primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
private fun ModulesSection(onModuleClick: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.modules),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Module 1: In Progress
        ModuleCard(
            title = stringResource(R.string.module_spot_the_phish),
            description = stringResource(R.string.module_spot_the_phish_desc),
            status = ModuleStatus.IN_PROGRESS,
            progress = 0.4f,
            hasOfflinePin = true,
            onClick = { onModuleClick("spot_the_phish") }
        )

        // Module 2: Completed
        ModuleCard(
            title = stringResource(R.string.module_qr_basics),
            description = stringResource(R.string.module_qr_basics_desc),
            status = ModuleStatus.COMPLETED,
            hasOfflinePin = true,
            onClick = { onModuleClick("qr_basics") }
        )

        // Module 3: New
        ModuleCard(
            title = stringResource(R.string.module_link_hygiene),
            description = stringResource(R.string.module_link_hygiene_desc),
            status = ModuleStatus.NEW,
            onClick = { onModuleClick("link_hygiene") }
        )
    }
}

enum class ModuleStatus {
    IN_PROGRESS, COMPLETED, NEW
}

@Composable
private fun ModuleCard(
    title: String,
    description: String,
    status: ModuleStatus,
    progress: Float = 0f,
    hasOfflinePin: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Module Image/Icon Placeholder
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (status == ModuleStatus.COMPLETED) {
                                QRShieldColors.Primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (status == ModuleStatus.COMPLETED) {
                        // Checkmark overlay for completed
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(QRShieldColors.Primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = QRShieldColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // Offline pin for non-completed
                        if (hasOfflinePin) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OfflinePin,
                                    contentDescription = stringResource(R.string.available_offline),
                                    tint = QRShieldColors.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Status Badge
                    StatusBadge(status = status, hasOfflinePin = hasOfflinePin && status == ModuleStatus.COMPLETED)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Progress bar for In Progress modules
            if (status == ModuleStatus.IN_PROGRESS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(QRShieldColors.Primary)
                        )
                    }
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action Button
            when (status) {
                ModuleStatus.IN_PROGRESS -> {
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = QRShieldColors.Primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_continue), fontWeight = FontWeight.SemiBold)
                    }
                }
                ModuleStatus.COMPLETED -> {
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(9999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = QRShieldColors.Primary.copy(alpha = 0.1f),
                            contentColor = QRShieldColors.Primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.action_review), fontWeight = FontWeight.SemiBold)
                    }
                }
                ModuleStatus.NEW -> {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(9999.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = Brush.linearGradient(
                                listOf(QRShieldColors.Primary, QRShieldColors.Primary)
                            ),
                            width = 2.dp
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = QRShieldColors.Primary
                        )
                    ) {
                        Text(stringResource(R.string.action_start_module), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ModuleStatus, hasOfflinePin: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (bgColor, textColor, text) = when (status) {
            ModuleStatus.IN_PROGRESS -> Triple(
                QRShieldColors.Orange50,
                QRShieldColors.Orange600,
                stringResource(R.string.status_in_progress)
            )
            ModuleStatus.COMPLETED -> Triple(
                QRShieldColors.Emerald50,
                QRShieldColors.Emerald600,
                stringResource(R.string.status_completed)
            )
            ModuleStatus.NEW -> Triple(
                QRShieldColors.Gray100,
                QRShieldColors.Gray600,
                stringResource(R.string.status_new_module)
            )
        }

        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = bgColor
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }

        if (hasOfflinePin) {
            Icon(
                imageVector = Icons.Default.OfflinePin,
                contentDescription = "Available offline",
                tint = QRShieldColors.Gray400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ReportThreatCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = QRShieldColors.Slate900,
        shadowElevation = 4.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Label
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = QRShieldColors.Yellow400,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.security_tool),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = QRShieldColors.Yellow400
                    )
                }

                Text(
                    text = stringResource(R.string.report_a_threat),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = stringResource(R.string.report_a_threat_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = QRShieldColors.Slate300
                )
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.report_a_threat),
                    tint = Color.White
                )
            }
        }
    }
}
