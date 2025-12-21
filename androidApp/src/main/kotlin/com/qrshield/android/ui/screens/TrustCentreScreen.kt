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
    var selectedSensitivity by remember { mutableStateOf(Sensitivity.BALANCED) }
    var shareThreatSignatures by remember { mutableStateOf(false) }
    var biometricUnlock by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trust Centre",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onDoneClick) {
                        Text(
                            text = "Done",
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
                onSensitivityChange = { selectedSensitivity = it }
            )

            // Lists Section (Allowlist/Blocklist)
            ListsGrid(
                onAllowlistClick = onAllowlistClick,
                onBlocklistClick = onBlocklistClick
            )

            // Privacy Controls
            PrivacyControlsSection(
                shareThreatSignatures = shareThreatSignatures,
                onShareThreatSignaturesChange = { shareThreatSignatures = it },
                biometricUnlock = biometricUnlock,
                onBiometricUnlockChange = { biometricUnlock = it }
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
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
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
                        text = "Strict Offline Guarantee",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "No data leaves this device.",
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
                            text = "Air-Gapped Mode Active",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = QRShieldColors.Primary
                        )
                    }
                }
            }
        }
    }
}

enum class Sensitivity(val label: String, val description: String) {
    LOW(
        "Low",
        "Minimal checks. Prioritizes speed and fewer interruptions. Use only for trusted environments."
    ),
    BALANCED(
        "Balanced",
        "Checks for known patterns and homoglyphs without aggressive heuristics. Ideal for daily use."
    ),
    PARANOIA(
        "Paranoia",
        "Aggressive heuristics and strict sandboxing. Expect frequent warnings for unknown domains."
    )
}

@Composable
private fun SensitivitySection(
    selectedSensitivity: Sensitivity,
    onSensitivityChange: (Sensitivity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
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
            Text(
                text = "Phishing Sensitivity",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Segmented Control
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
            Column(modifier = Modifier.padding(8.dp)) {
                // Segmented buttons
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(9999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Sensitivity.entries.forEach { sensitivity ->
                            val isSelected = sensitivity == selectedSensitivity
                            val textColor = when {
                                isSelected && sensitivity == Sensitivity.PARANOIA -> QRShieldColors.RiskDanger
                                isSelected && sensitivity == Sensitivity.BALANCED -> QRShieldColors.Primary
                                isSelected -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(9999.dp))
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(MaterialTheme.colorScheme.surface)
                                        } else {
                                            Modifier.background(Color.Transparent)
                                        }
                                    )
                                    .clickable { onSensitivityChange(sensitivity) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sensitivity.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                // Description
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = selectedSensitivity.label + " Mode",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (selectedSensitivity == Sensitivity.PARANOIA) 
                            QRShieldColors.RiskDanger 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedSensitivity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ListsGrid(
    onAllowlistClick: () -> Unit,
    onBlocklistClick: () -> Unit
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
            title = "Allowlist",
            subtitle = "Safe domains",
            count = 12,
            onClick = onAllowlistClick
        )
        
        ListCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.GppBad,
            iconBgColor = QRShieldColors.Red50,
            iconColor = QRShieldColors.Red600,
            decorativeIcon = Icons.Default.Block,
            decorativeColor = QRShieldColors.RiskDanger,
            title = "Blocklist",
            subtitle = "Blocked rules",
            count = 45,
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
        modifier = modifier.height(128.dp),
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
        ),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative icon (large, faded)
            Icon(
                imageVector = decorativeIcon,
                contentDescription = null,
                tint = decorativeColor.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-4).dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Icon + Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = count.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Bottom: Title + Subtitle
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
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
    onBiometricUnlockChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = QRShieldColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Privacy & Access",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Toggle Items
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
            Column {
                PrivacyToggleItem(
                    title = "Share Threat Signatures",
                    subtitle = "Anonymous heuristics upload",
                    checked = shareThreatSignatures,
                    onCheckedChange = onShareThreatSignaturesChange,
                    showDivider = true
                )
                PrivacyToggleItem(
                    title = "Biometric Unlock",
                    subtitle = "Require FaceID for settings",
                    checked = biometricUnlock,
                    onCheckedChange = onBiometricUnlockChange,
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
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
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                text = "QR-SHIELD v2.4.0",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "ENTERPRISE EDITION",
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
                    text = "Terms",
                    fontSize = 11.sp,
                    color = QRShieldColors.Primary
                )
            }
            TextButton(onClick = { }) {
                Text(
                    text = "Privacy Policy",
                    fontSize = 11.sp,
                    color = QRShieldColors.Primary
                )
            }
            TextButton(onClick = { }) {
                Text(
                    text = "Licenses",
                    fontSize = 11.sp,
                    color = QRShieldColors.Primary
                )
            }
        }
    }
}
