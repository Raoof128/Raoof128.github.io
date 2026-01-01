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
 * Threat Database Screen
 * Matches the HTML "Threat Database" design with:
 * - Hero status section with shield icon
 * - Stats grid (version, signatures, last sync)
 * - Update methods (online, offline)
 * - Integrity footer with SHA-256 verification
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatDatabaseScreen(
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onCheckNow: () -> Unit = {},
    onImportFile: () -> Unit = {},
    modifier: Modifier = Modifier,
    // Sample data - in real app would come from ViewModel
    isSecure: Boolean = true,
    version: String = "v2.4.19",
    signatureCount: String = "1.2M+",
    lastSync: String = "2h ago"
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.threat_database_title),
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Status Section
            HeroStatusSection(isSecure = isSecure)

            // Stats Grid
            StatsGrid(
                version = version,
                signatureCount = signatureCount,
                lastSync = lastSync
            )

            // Update Methods
            UpdateMethodsSection(
                onCheckNow = onCheckNow,
                onImportFile = onImportFile
            )

            // Spacer to push footer to bottom
            Spacer(modifier = Modifier.weight(1f))

            // Integrity Footer
            IntegrityFooter()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroStatusSection(isSecure: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shield Icon with status badge
        Box(
            modifier = Modifier.size(128.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow effect
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(MehrGuardColors.Primary.copy(alpha = 0.1f))
                    .border(
                        width = 1.dp,
                        color = MehrGuardColors.Primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            )
            
            // Inner icon container
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MehrGuardColors.Primary.copy(alpha = 0.2f))
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = MehrGuardColors.Primary.copy(alpha = 0.15f),
                        spotColor = MehrGuardColors.Primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MehrGuardColors.Primary,
                    modifier = Modifier.size(64.dp)
                )
            }

            // Status badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-8).dp, y = (-8).dp)
                    .clip(CircleShape)
                    .background(MehrGuardColors.RiskSafe)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_verified),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Status text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(if (isSecure) R.string.threat_db_system_secure else R.string.threat_db_update_required),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.threat_database_verified),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsGrid(
    version: String,
    signatureCount: String,
    lastSync: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.threat_db_version),
            value = version,
            valueColor = MaterialTheme.colorScheme.onSurface
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.threat_db_signatures),
            value = signatureCount,
            valueColor = MehrGuardColors.Primary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.threat_db_last_sync),
            value = lastSync,
            valueColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = valueColor
            )
        }
    }
}

@Composable
private fun UpdateMethodsSection(
    onCheckNow: () -> Unit,
    onImportFile: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.threat_database_update_methods),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 4.dp)
        )

        // Online Update Card
        UpdateMethodCard(
            icon = Icons.Default.CloudSync,
            iconBgColor = MehrGuardColors.Blue50,
            iconColor = MehrGuardColors.Primary,
            title = stringResource(R.string.threat_db_online_update),
            description = stringResource(R.string.threat_db_online_update_desc),
            buttonText = stringResource(R.string.threat_db_check_now),
            isPrimary = true,
            onClick = onCheckNow
        )

        // Offline Update Card
        UpdateMethodCard(
            icon = Icons.Default.FolderCopy,
            iconBgColor = MehrGuardColors.Gray100,
            iconColor = MehrGuardColors.Gray600,
            title = stringResource(R.string.threat_db_import_offline),
            description = stringResource(R.string.threat_db_import_offline_desc),
            buttonText = stringResource(R.string.threat_db_import_file),
            isPrimary = false,
            onClick = onImportFile
        )
    }
}

@Composable
private fun UpdateMethodCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    description: String,
    buttonText: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
                }
            }

            // Button
            Button(
                onClick = onClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .then(
                        if (isPrimary) {
                            Modifier.shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(9999.dp),
                                ambientColor = MehrGuardColors.Primary.copy(alpha = 0.3f),
                                spotColor = MehrGuardColors.Primary.copy(alpha = 0.3f)
                            )
                        } else Modifier
                    ),
                shape = RoundedCornerShape(9999.dp),
                colors = if (isPrimary) {
                    ButtonDefaults.buttonColors(
                        containerColor = MehrGuardColors.Primary,
                        contentColor = Color.White
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun IntegrityFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SHA-256 badge
        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = MehrGuardColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.threat_database_sha256_verified),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Description
        Text(
            text = stringResource(R.string.threat_database_integrity_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}
