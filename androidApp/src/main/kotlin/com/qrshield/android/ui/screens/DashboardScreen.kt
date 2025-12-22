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
import androidx.compose.material.icons.automirrored.filled.*
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

import com.qrshield.android.util.DateUtils

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
    val settings by viewModel.settings.collectAsState()
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
        // Top App Bar with dark mode toggle (iOS Parity)
        DashboardHeader(
            userName = stringResource(R.string.dashboard_default_user),
            onSettingsClick = onSettingsClick,
            onDarkModeToggle = {
                // Toggle dark mode matching iOS useDarkMode.toggle()
                viewModel.updateSettings(settings.copy(isDarkModeEnabled = !settings.isDarkModeEnabled))
            },
            isDarkMode = settings.isDarkModeEnabled,
            threatsBlocked = stats.maliciousCount + stats.suspiciousCount,
            onNotificationsClick = onViewAllScans  // Navigate to history/threats
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

            // Feature Cards (Parity with HTML)
            FeatureCardsSection()

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
    onSettingsClick: () -> Unit,
    onDarkModeToggle: () -> Unit = {},
    isDarkMode: Boolean = true,
    threatsBlocked: Int = 0,
    onNotificationsClick: () -> Unit = {}
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
                    text = stringResource(R.string.dashboard_welcome_back),
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

        // Trailing icons - matches iOS DashboardView toolbar
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dark Mode Toggle (iOS Parity)
            IconButton(
                onClick = onDarkModeToggle,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = stringResource(R.string.settings_dark_mode),
                    tint = QRShieldColors.Primary
                )
            }
            
            // Notification Bell (iOS Parity)
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Box {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.cd_notifications),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Badge for threats
                    if (threatsBlocked > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(QRShieldColors.RiskDanger)
                        )
                    }
                }
            }
            
            // Settings Button
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
                    contentDescription = stringResource(R.string.nav_settings),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun HeroSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Enterprise Protection Badge - Matches iOS exactly
        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = QRShieldColors.Primary.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = QRShieldColors.Primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.dashboard_enterprise_protection),
                    color = QRShieldColors.Primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Hero Headline
        Text(
            text = stringResource(R.string.dashboard_headline),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Subtitle
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick URL Input
        var urlInput by remember { mutableStateOf("") }
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { 
                Text(
                    text = stringResource(R.string.dashboard_url_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                Button(
                    onClick = { /* TODO: Implement Analyze */ },
                    modifier = Modifier.padding(end = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QRShieldColors.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.analyze_url),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = QRShieldColors.Primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )
    }
}

@Composable
private fun FeatureCardsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FeatureCard(
            icon = Icons.Default.CloudOff,
            bgIcon = Icons.Default.WifiOff,
            title = "Offline-First Architecture",
            description = "Complete analysis is performed locally. Your camera feed and scanned data never touch an external server.",
            iconColor = QRShieldColors.Blue500,
            iconBgColor = QRShieldColors.Blue50
        )
        
        FeatureCard(
            icon = Icons.AutoMirrored.Filled.ManageSearch,
            bgIcon = Icons.Default.Psychology,
            title = "Explainable Security",
            description = "Don't just get a \"Block\". We provide detailed heuristic breakdowns of URL parameters and redirects.",
            iconColor = QRShieldColors.Purple500,
            iconBgColor = QRShieldColors.Purple50
        )
        
        FeatureCard(
            icon = Icons.Default.Speed,
            bgIcon = Icons.Default.Bolt,
            title = "High-Performance Engine",
            description = "Optimised for mobile environments. Scans are processed in under 5ms using native Kotlin primitives.",
            iconColor = QRShieldColors.Emerald500,
            iconBgColor = QRShieldColors.Emerald50
        )
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    bgIcon: ImageVector,
    title: String,
    description: String,
    iconColor: Color,
    iconBgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Icon
            Icon(
                imageVector = bgIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // Very faint
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(120.dp)
                    .offset(x = 20.dp, y = (-20).dp)
            )
            
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
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
            label = stringResource(R.string.dashboard_scan_qr),
            isPrimary = true,
            onClick = onScanClick
        )

        // Import Image Button
        PrimaryActionButton(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AddPhotoAlternate,
            label = stringResource(R.string.dashboard_import_image),
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
                        text = stringResource(R.string.dashboard_system_health),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.dashboard_db_status_default),
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
                    label = stringResource(R.string.dashboard_total_scans),
                    valueColor = QRShieldColors.Primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = threatsBlocked.toString(),
                    label = stringResource(R.string.dashboard_threats_blocked),
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
                text = stringResource(R.string.dashboard_recent_scans),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = onViewAllClick) {
                Text(
                    text = stringResource(R.string.dashboard_view_all),
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
                        text = stringResource(R.string.dashboard_no_scans),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.dashboard_no_scans_hint),
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
                        statusDetail = stringResource(R.string.dashboard_score_fmt, item.score),
                        time = DateUtils.formatRelativeTime(item.scannedAt),
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
            text = stringResource(R.string.dashboard_tools),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                icon = Icons.Default.Shield,
                title = stringResource(R.string.tool_trust_centre),
                subtitle = stringResource(R.string.tool_trust_centre_subtitle),
                iconBgColor = QRShieldColors.Blue50,
                iconColor = QRShieldColors.Primary,
                onClick = { onToolClick("trust_centre") }
            )
            
            ToolCard(
                icon = Icons.Default.School,
                title = stringResource(R.string.tool_learning),
                subtitle = stringResource(R.string.tool_learning_subtitle),
                iconBgColor = QRShieldColors.Emerald50,
                iconColor = QRShieldColors.Emerald600,
                onClick = { onToolClick("learning_centre") }
            )

            ToolCard(
                icon = Icons.Default.Storage,
                title = stringResource(R.string.tool_threat_database),
                subtitle = stringResource(R.string.tool_threat_database_subtitle),
                iconBgColor = QRShieldColors.Purple50,
                iconColor = QRShieldColors.Purple600,
                onClick = { onToolClick("threat_database") }
            )

            ToolCard(
                icon = Icons.Default.SportsEsports,
                title = stringResource(R.string.tool_beat_the_bot),
                subtitle = stringResource(R.string.tool_beat_the_bot_subtitle),
                iconBgColor = QRShieldColors.Orange50,
                iconColor = QRShieldColors.Orange600,
                onClick = { onToolClick("beat_the_bot") }
            )

            ToolCard(
                icon = Icons.AutoMirrored.Filled.List,
                title = stringResource(R.string.tool_whitelisting),
                subtitle = stringResource(R.string.tool_whitelisting_subtitle),
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
