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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.android.ui.theme.QRShieldColors

/**
 * Offline Privacy Screen
 * Matches the HTML "Offline Privacy" design with:
 * - Hero section explaining offline guarantee
 * - Privacy architecture breakdown
 * - Data flow visualization
 * - Trust badges
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflinePrivacyScreen(
    onBackClick: () -> Unit = {},
    onLearnMoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy & Offline",
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
                    Spacer(modifier = Modifier.width(48.dp))
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

            // Hero Section
            PrivacyHeroSection()

            // Privacy Architecture
            PrivacyArchitectureSection()

            // Data Flow
            DataFlowSection()

            // Trust Badges
            TrustBadgesSection()

            // Learn More Button
            Button(
                onClick = onLearnMoreClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QRShieldColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Read Full Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun PrivacyHeroSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shield Icon
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(QRShieldColors.Primary.copy(alpha = 0.1f))
                .border(1.dp, QRShieldColors.Primary.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockPerson,
                contentDescription = null,
                tint = QRShieldColors.Primary,
                modifier = Modifier.size(48.dp)
            )
        }

        // Title
        Text(
            text = "Your Data Never Leaves",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        // Description
        Text(
            text = "QR-SHIELD operates entirely on your device. No cloud. No servers. No data collection. Complete privacy by design.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Offline Badge
        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = QRShieldColors.Emerald50,
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.linearGradient(
                    listOf(QRShieldColors.Emerald100, QRShieldColors.Emerald100)
                )
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = QRShieldColors.Emerald600,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "100% Offline Operation",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = QRShieldColors.Emerald600
                )
            }
        }
    }
}

@Composable
private fun PrivacyArchitectureSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Privacy Architecture",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PrivacyFeatureCard(
                icon = Icons.Default.Storage,
                iconBgColor = QRShieldColors.Primary.copy(alpha = 0.1f),
                iconColor = QRShieldColors.Primary,
                title = "Local Database",
                description = "Threat signatures stored encrypted on-device. Updated via secure offline imports."
            )

            PrivacyFeatureCard(
                icon = Icons.Default.Memory,
                iconBgColor = QRShieldColors.Purple50,
                iconColor = QRShieldColors.Purple600,
                title = "On-Device AI",
                description = "ML models run locally using optimized inference. Zero cloud dependencies."
            )

            PrivacyFeatureCard(
                icon = Icons.Default.DeleteForever,
                iconBgColor = QRShieldColors.Orange50,
                iconColor = QRShieldColors.Orange600,
                title = "No Data Retention",
                description = "Scan history can be wiped instantly. We collect zero telemetry or analytics."
            )

            PrivacyFeatureCard(
                icon = Icons.Default.VpnLock,
                iconBgColor = QRShieldColors.Emerald50,
                iconColor = QRShieldColors.Emerald600,
                title = "Air-Gap Compatible",
                description = "Works perfectly in isolated networks. Designed for enterprise security."
            )
        }
    }
}

@Composable
private fun PrivacyFeatureCard(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
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

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DataFlowSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Data Flow",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

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
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DataFlowStep(
                    step = 1,
                    title = "QR Code Scanned",
                    description = "Camera captures and decodes QR locally",
                    isCompleted = true
                )
                DataFlowConnector()
                DataFlowStep(
                    step = 2,
                    title = "URL Extracted",
                    description = "Link parsed without external lookups",
                    isCompleted = true
                )
                DataFlowConnector()
                DataFlowStep(
                    step = 3,
                    title = "AI Analysis",
                    description = "On-device ML evaluates threat level",
                    isCompleted = true
                )
                DataFlowConnector()
                DataFlowStep(
                    step = 4,
                    title = "Result Displayed",
                    description = "Verdict shown, data stays on device",
                    isCompleted = true
                )
            }
        }
    }
}

@Composable
private fun DataFlowStep(
    step: Int,
    title: String,
    description: String,
    isCompleted: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) QRShieldColors.Primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DataFlowConnector() {
    Box(
        modifier = Modifier
            .padding(start = 15.dp)
            .width(2.dp)
            .height(24.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun TrustBadgesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Compliance & Certifications",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrustBadge(
                modifier = Modifier.weight(1f),
                title = "GDPR",
                subtitle = "Compliant"
            )
            TrustBadge(
                modifier = Modifier.weight(1f),
                title = "SOC 2",
                subtitle = "Type II"
            )
            TrustBadge(
                modifier = Modifier.weight(1f),
                title = "ISO",
                subtitle = "27001"
            )
        }
    }
}

@Composable
private fun TrustBadge(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = QRShieldColors.Primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
