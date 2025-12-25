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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.android.ui.components.QRShieldToggle
import com.qrshield.android.ui.theme.QRShieldColors
import com.qrshield.android.ui.theme.QRShieldShapes
import androidx.compose.ui.res.stringResource
import com.qrshield.android.BuildConfig
import com.qrshield.android.R

/**
 * Trust Centre Screen
 * Matches the HTML "Trust Centre" design with:
 * - Offline Guarantee hero card
 * - Phishing Sensitivity segmented control
 * - Allowlist/Blocklist cards
 * - Privacy & Access toggles
 * - Footer with version info
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustCentreScreen(
    onBackClick: () -> Unit = {},
    onDoneClick: () -> Unit = {},
    onAllowlistClick: () -> Unit = {},
    onBlocklistClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: com.qrshield.ui.SharedViewModel = org.koin.compose.koinInject()
    val settings by viewModel.settings.collectAsState()
    
    // Inject domain list repository to get real counts
    val domainListRepository: com.qrshield.android.data.DomainListRepository = org.koin.compose.koinInject()
    val allowlistEntries by domainListRepository.allowlist.collectAsState(initial = emptyList())
    val blocklistEntries by domainListRepository.blocklist.collectAsState(initial = emptyList())
    
    // Map settings to local UI state
    val selectedSensitivity = try {
        Sensitivity.valueOf(settings.heuristicSensitivity)
    } catch (e: IllegalArgumentException) {
        Sensitivity.BALANCED
    }
    
    val shareThreatSignatures = settings.isShareThreatSignaturesEnabled
    val biometricUnlock = settings.isBiometricUnlockEnabled
    val autoCopySafeLinks = settings.isAutoCopySafeLinksEnabled

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.trust_center_screen_title),
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
                    TextButton(onClick = onDoneClick) {
                        Text(
                            text = stringResource(R.string.done),
                            color = QRShieldColors.Primary,
                            fontWeight = FontWeight.Bold
                        )
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Hero: Offline Guarantee Card
            OfflineGuaranteeCard()

            // Sensitivity Section
            SensitivitySection(
                selectedSensitivity = selectedSensitivity,
                onSensitivityChange = { newSensitivity -> 
                    viewModel.updateSettings(settings.copy(heuristicSensitivity = newSensitivity.name))
                }
            )

            // Lists Section (Allowlist/Blocklist)
            ListsGrid(
                onAllowlistClick = onAllowlistClick,
                onBlocklistClick = onBlocklistClick,
                allowlistCount = allowlistEntries.size,
                blocklistCount = blocklistEntries.size
            )

            // Privacy Controls
            PrivacyControlsSection(
                shareThreatSignatures = shareThreatSignatures,
                onShareThreatSignaturesChange = { viewModel.updateSettings(settings.copy(isShareThreatSignaturesEnabled = it)) },
                biometricUnlock = biometricUnlock,
                onBiometricUnlockChange = { viewModel.updateSettings(settings.copy(isBiometricUnlockEnabled = it)) },
                autoCopySafeLinks = autoCopySafeLinks,
                onAutoCopySafeLinksChange = { viewModel.updateSettings(settings.copy(isAutoCopySafeLinksEnabled = it)) }
            )

            // Footer
            FooterSection()

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun OfflineGuaranteeCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = QRShieldShapes.Card,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Box {
            // Decorative background blur
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .offset(x = 0.dp, y = (-40).dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(QRShieldColors.Primary.copy(alpha = 0.05f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Shield Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(QRShieldColors.Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShieldMoon,
                        contentDescription = null,
                        tint = QRShieldColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.offline_guarantee_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.offline_guarantee_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = QRShieldColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.air_gapped_active),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = QRShieldColors.Primary
                        )
                    }
                }
            }
        }
    }
}

enum class Sensitivity(val labelRes: Int, val descriptionRes: Int) {
    LOW(
        R.string.sensitivity_low,
        R.string.sensitivity_low_desc
    ),
    BALANCED(
        R.string.sensitivity_balanced,
        R.string.sensitivity_balanced_desc
    ),
    PARANOIA(
        R.string.sensitivity_paranoia,
        R.string.sensitivity_paranoia_desc
    )
}

@Composable
private fun SensitivitySection(
    selectedSensitivity: Sensitivity,
    onSensitivityChange: (Sensitivity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = QRShieldColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.phishing_sensitivity),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.trust_centre_adjust_thresholds),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Custom Segmented Control
        Surface(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Sensitivity.entries.forEach { sensitivity ->
                    val isSelected = sensitivity == selectedSensitivity
                    val textColor = if (isSelected) {
                        when (sensitivity) {
                            Sensitivity.PARANOIA -> QRShieldColors.RiskDanger
                            Sensitivity.BALANCED -> QRShieldColors.Primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent,
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        onClick = { onSensitivityChange(sensitivity) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(sensitivity.labelRes),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = textColor
                            )
                        }
                    }
                }
            }
        }

        // Info Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = QRShieldShapes.Card,
            color = MaterialTheme.colorScheme.surface,
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(selectedSensitivity.labelRes) + " (Recommended)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(selectedSensitivity.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ListsGrid(
    onAllowlistClick: () -> Unit,
    onBlocklistClick: () -> Unit,
    allowlistCount: Int = 0,
    blocklistCount: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ListCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.VerifiedUser,
            iconBgColor = QRShieldColors.Emerald50,
            iconColor = QRShieldColors.Emerald600,
            decorativeIcon = Icons.Default.CheckCircle,
            decorativeColor = QRShieldColors.Emerald500,
            title = stringResource(R.string.allowlist_title),
            subtitle = stringResource(R.string.allowlist_subtitle),
            count = allowlistCount,
            onClick = onAllowlistClick
        )
        
        ListCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.GppBad,
            iconBgColor = QRShieldColors.Red50,
            iconColor = QRShieldColors.Red600,
            decorativeIcon = Icons.Default.Block,
            decorativeColor = QRShieldColors.RiskDanger,
            title = stringResource(R.string.blocklist_title),
            subtitle = stringResource(R.string.blocklist_subtitle),
            count = blocklistCount,
            onClick = onBlocklistClick
        )
    }
}

@Composable
private fun ListCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    decorativeIcon: ImageVector,
    decorativeColor: Color,
    title: String,
    subtitle: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(160.dp),
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
        ),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative icon (large, faded)
            Icon(
                imageVector = decorativeIcon,
                contentDescription = null,
                tint = decorativeColor.copy(alpha = 0.05f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Header with Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Add Button (Visual only for now)
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(28.dp).clickable { /* TODO: Add Item */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.cd_add),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Middle: Count
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Bottom: Title
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.trust_centre_last_added), // Placeholder for parity
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyControlsSection(
    shareThreatSignatures: Boolean,
    onShareThreatSignaturesChange: (Boolean) -> Unit,
    biometricUnlock: Boolean,
    onBiometricUnlockChange: (Boolean) -> Unit,
    autoCopySafeLinks: Boolean,
    onAutoCopySafeLinksChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = QRShieldColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.privacy_access_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Toggle Items
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
            Column {
                // Strict Offline Mode (Simulated using existing boolean or new state)
                PrivacyToggleItem(
                    title = "Strict Offline Mode",
                    subtitle = "Disable all external link previews.",
                    checked = true, // Always true for now as per design
                    onCheckedChange = { }, 
                    showDivider = true
                )
                
                // Anonymous Telemetry (Mapped from Share Threat Signatures)
                PrivacyToggleItem(
                    title = "Anonymous Telemetry",
                    subtitle = "Share detection stats to improve ML.",
                    checked = shareThreatSignatures,
                    onCheckedChange = onShareThreatSignaturesChange,
                    showDivider = true
                )
                
                // Auto-Copy
                PrivacyToggleItem(
                    title = stringResource(R.string.auto_copy_title),
                    subtitle = stringResource(R.string.auto_copy_subtitle),
                    checked = autoCopySafeLinks,
                    onCheckedChange = onAutoCopySafeLinksChange,
                    showDivider = false
                )
            }
        }
    }
}

@Composable
private fun PrivacyToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                     color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            QRShieldToggle(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }

        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .alpha(0.6f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Logo placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.footer_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.footer_edition),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = { }) {
                Text(
                    text = stringResource(R.string.footer_terms),
                    fontSize = 11.sp,
                    color = QRShieldColors.Primary
                )
            }
            TextButton(onClick = { }) {
                Text(
                    text = stringResource(R.string.footer_privacy),
                    fontSize = 11.sp,
                    color = QRShieldColors.Primary
                )
            }
            TextButton(onClick = { }) {
                Text(
                    text = stringResource(R.string.footer_licenses),
                    fontSize = 11.sp,
                    color = QRShieldColors.Primary
                )
            }
        }
    }
}
