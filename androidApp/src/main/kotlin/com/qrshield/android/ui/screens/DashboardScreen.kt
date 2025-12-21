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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.android.ui.theme.QRShieldColors

/**
 * Dashboard / Home Screen
 * Matches the HTML "Home / Dashboard" design with:
 * - User avatar and welcome header
 * - Shield Active status chip
 * - Hero headline
 * - Primary action buttons (Scan QR, Import Image)
 * - System Health card with stats
 * - Recent Scans list
 * - Tools carousel
 */
@Composable
fun DashboardScreen(
    onScanClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onViewAllScans: () -> Unit = {},
    onScanItemClick: (String) -> Unit = {},
    onToolClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Top App Bar
        DashboardHeader(
            userName = "Admin User",
            onSettingsClick = onSettingsClick
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Section
            HeroSection()

            // Primary Actions
            PrimaryActionsRow(
                onScanClick = onScanClick,
                onImportClick = onImportClick
            )

            // System Health Card
            SystemHealthCard()

            // Recent Scans Section
            RecentScansSection(
                onViewAllClick = onViewAllScans,
                onItemClick = onScanItemClick
            )

            // Tools Carousel
            ToolsCarousel(onToolClick = onToolClick)

            // Bottom padding for nav bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar with online indicator
            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(QRShieldColors.Primary.copy(alpha = 0.2f))
                        .border(2.dp, QRShieldColors.Primary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = QRShieldColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Online indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(QRShieldColors.RiskSafe)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                )
            }

            Column {
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun HeroSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Shield Active Chip
        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = QRShieldColors.RiskSafe.copy(alpha = 0.1f),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(
                        QRShieldColors.RiskSafe.copy(alpha = 0.2f),
                        QRShieldColors.RiskSafe.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = QRShieldColors.Emerald600,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "SHIELD ACTIVE",
                    color = QRShieldColors.Emerald600,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Hero Headline
        Text(
            text = "Secure. Offline.\nExplainable Defence.",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Subtitle
        Text(
            text = "Your enterprise database is up to date and monitoring locally.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrimaryActionsRow(
    onScanClick: () -> Unit,
    onImportClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scan QR Button
        PrimaryActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.QrCodeScanner,
            label = "Scan QR",
            isPrimary = true,
            onClick = onScanClick
        )

        // Import Image Button
        PrimaryActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AddPhotoAlternate,
            label = "Import Image",
            isPrimary = false,
            onClick = onImportClick
        )
    }
}

@Composable
private fun PrimaryActionButton(
    icon: ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isPrimary) QRShieldColors.Primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isPrimary) Color.White else QRShieldColors.Primary

    Surface(
        modifier = modifier
            .height(112.dp)
            .then(
                if (isPrimary) {
                    Modifier.shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = QRShieldColors.Primary.copy(alpha = 0.3f),
                        spotColor = QRShieldColors.Primary.copy(alpha = 0.3f)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (!isPrimary) {
            ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                )
            )
        } else null,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPrimary) Color.White.copy(alpha = 0.2f)
                        else QRShieldColors.Primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SystemHealthCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
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
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "System Health",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "DB v2.4.1 • Updated 2h ago",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(QRShieldColors.RiskSafe.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = QRShieldColors.RiskSafe,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "12",
                    label = "Scans Today",
                    valueColor = QRShieldColors.Primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "0",
                    label = "Threats Blocked",
                    valueColor = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentScansSection(
    onViewAllClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Scans",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = onViewAllClick) {
                Text(
                    text = "View All",
                    color = QRShieldColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }

        // Scan Items
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ScanHistoryItem(
                domain = "login-microsoft-secure.com",
                status = "Suspicious",
                statusColor = QRShieldColors.Orange600,
                statusBgColor = QRShieldColors.Orange50,
                statusDetail = "Phishing Heuristic",
                time = "10:42 AM",
                icon = Icons.Default.Warning,
                iconBgColor = QRShieldColors.Orange50,
                iconColor = QRShieldColors.Orange600,
                onClick = { onItemClick("1") }
            )

            ScanHistoryItem(
                domain = "google.com",
                status = "Safe",
                statusColor = QRShieldColors.Emerald600,
                statusBgColor = QRShieldColors.Emerald50,
                statusDetail = "Whitelisted",
                time = "09:15 AM",
                icon = Icons.Default.VerifiedUser,
                iconBgColor = QRShieldColors.Emerald50,
                iconColor = QRShieldColors.Emerald600,
                onClick = { onItemClick("2") }
            )

            ScanHistoryItem(
                domain = "bit.ly/free-prize-99",
                status = "Malicious",
                statusColor = QRShieldColors.Red600,
                statusBgColor = QRShieldColors.Red50,
                statusDetail = "Known Threat",
                time = "Yesterday",
                icon = Icons.Default.Dangerous,
                iconBgColor = QRShieldColors.Red50,
                iconColor = QRShieldColors.Red600,
                onClick = { onItemClick("3") }
            )
        }
    }
}

@Composable
private fun ScanHistoryItem(
    domain: String,
    status: String,
    statusColor: Color,
    statusBgColor: Color,
    statusDetail: String,
    time: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$status • $statusDetail",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor
                    )
                }
            }

            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToolsCarousel(onToolClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Tools",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                icon = Icons.Default.Science,
                title = "Deep Analysis",
                subtitle = "Manual inspection",
                iconBgColor = QRShieldColors.Blue50,
                iconColor = QRShieldColors.Primary,
                onClick = { onToolClick("deep_analysis") }
            )

            ToolCard(
                icon = Icons.Default.Report,
                title = "Report Incident",
                subtitle = "Flag a URL",
                iconBgColor = QRShieldColors.Purple50,
                iconColor = QRShieldColors.Purple600,
                onClick = { onToolClick("report") }
            )

            ToolCard(
                icon = Icons.Default.List,
                title = "Whitelisting",
                subtitle = "Manage exceptions",
                iconBgColor = QRShieldColors.Gray100,
                iconColor = QRShieldColors.Gray600,
                onClick = { onToolClick("whitelist") }
            )
        }
    }
}

@Composable
private fun ToolCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
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

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}
