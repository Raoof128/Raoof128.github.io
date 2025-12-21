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
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.android.R
import com.qrshield.android.ui.theme.QRShieldColors
import com.qrshield.model.Verdict
import com.qrshield.ui.HistoryStatistics
import com.qrshield.ui.SharedViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dashboard / Home Screen
 * Matches the HTML "Home / Dashboard" design with:
 * - User avatar and welcome header
 * - Shield Active status chip
 * - Hero headline
 * - Primary action buttons (Scan QR, Import Image)
 * - System Health card with REAL stats from ViewModel
 * - Recent Scans list with REAL history data
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
    val viewModel: SharedViewModel = koinInject()
    val scanHistory by viewModel.scanHistory.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Load statistics
    var stats by remember { mutableStateOf(HistoryStatistics(0, 0, 0, 0, 0.0)) }
    LaunchedEffect(scanHistory) {
        stats = viewModel.getStatistics()
    }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // Top App Bar
        DashboardHeader(
            userName = stringResource(R.string.dashboard_default_user),
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

            // System Health Card with REAL stats
            SystemHealthCard(
                totalScans = stats.totalScans,
                threatsBlocked = stats.maliciousCount + stats.suspiciousCount
            )

            // Recent Scans Section with REAL data
            RecentScansSection(
                scanHistory = scanHistory.take(3),
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
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
            ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
private fun SystemHealthCard(
    totalScans: Int,
    threatsBlocked: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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

            // Stats Grid - REAL DATA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = totalScans.toString(),
                    label = "Scans Today",
                    valueColor = QRShieldColors.Primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = threatsBlocked.toString(),
                    label = "Threats Blocked",
                    valueColor = if (threatsBlocked > 0) QRShieldColors.RiskDanger else MaterialTheme.colorScheme.onBackground
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
    scanHistory: List<com.qrshield.model.ScanHistoryItem>,
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

        // Scan Items - REAL DATA from ViewModel
        if (scanHistory.isEmpty()) {
            // Empty state
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = QRShieldColors.Primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No scans yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Scan a QR code to start building your history",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                scanHistory.forEach { item ->
                    val (icon, iconBgColor, iconColor, statusColor) = when (item.verdict) {
                        Verdict.SAFE -> Quadruple(
                            Icons.Default.VerifiedUser,
                            QRShieldColors.Emerald50,
                            QRShieldColors.Emerald600,
                            QRShieldColors.Emerald600
                        )
                        Verdict.SUSPICIOUS -> Quadruple(
                            Icons.Default.Warning,
                            QRShieldColors.Orange50,
                            QRShieldColors.Orange600,
                            QRShieldColors.Orange600
                        )
                        Verdict.MALICIOUS -> Quadruple(
                            Icons.Default.Dangerous,
                            QRShieldColors.Red50,
                            QRShieldColors.Red600,
                            QRShieldColors.Red600
                        )
                        else -> Quadruple(
                            Icons.AutoMirrored.Filled.Help,
                            QRShieldColors.Gray100,
                            QRShieldColors.Gray600,
                            QRShieldColors.Gray600
                        )
                    }
                    
                    ScanHistoryItem(
                        domain = item.url.substringAfter("://").substringBefore("/").take(40),
                        status = item.verdict.name,
                        statusColor = statusColor,
                        statusBgColor = iconBgColor,
                        statusDetail = "Score: ${item.score}",
                        time = formatTime(item.scannedAt),
                        icon = icon,
                        iconBgColor = iconBgColor,
                        iconColor = iconColor,
                        onClick = { onItemClick(item.id) }
                    )
                }
            }
        }
    }
}

// Helper data class for icon/color assignment
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun formatTime(epochMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - epochMillis
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMillis))
        }
        diff < 172_800_000 -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMillis))
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
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
                icon = Icons.Default.Shield,
                title = "Trust Centre",
                subtitle = "Security settings",
                iconBgColor = QRShieldColors.Blue50,
                iconColor = QRShieldColors.Primary,
                onClick = { onToolClick("trust_centre") }
            )
            
            ToolCard(
                icon = Icons.Default.School,
                title = "Learning",
                subtitle = "Security training",
                iconBgColor = QRShieldColors.Emerald50,
                iconColor = QRShieldColors.Emerald600,
                onClick = { onToolClick("learning_centre") }
            )

            ToolCard(
                icon = Icons.Default.Storage,
                title = "Threat Database",
                subtitle = "Manage signatures",
                iconBgColor = QRShieldColors.Purple50,
                iconColor = QRShieldColors.Purple600,
                onClick = { onToolClick("threat_database") }
            )

            ToolCard(
                icon = Icons.Default.SportsEsports,
                title = "Beat the Bot",
                subtitle = "Test your skills",
                iconBgColor = QRShieldColors.Orange50,
                iconColor = QRShieldColors.Orange600,
                onClick = { onToolClick("beat_the_bot") }
            )

            ToolCard(
                icon = Icons.AutoMirrored.Filled.List,
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
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
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
