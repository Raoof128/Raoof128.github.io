package com.qrshield.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.focusable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialIconRound
import com.qrshield.desktop.ui.dottedPattern
import com.qrshield.desktop.ui.iconContainer
import com.qrshield.desktop.ui.panelSurface
import com.qrshield.desktop.ui.progressTrack
import com.qrshield.desktop.ui.progressFill
import com.qrshield.desktop.ui.statusPill
import com.qrshield.data.ScanHistoryManager
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.Verdict
import com.qrshield.desktop.theme.LocalStitchTokens

@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.dashboard(isDark = viewModel.isDarkMode)
    val recentScans = viewModel.scanHistory.sortedByDescending { it.scannedAt }.take(2)
    val language = viewModel.appLanguage
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.colors.background)
        ) {
            AppSidebar(
                currentScreen = viewModel.currentScreen,
                onNavigate = { viewModel.currentScreen = it },
                language = viewModel.appLanguage,
                onProfileClick = { viewModel.currentScreen = AppScreen.TrustCentreAlt }
            )
            DashboardContent(
                onStartScan = {
                    viewModel.currentScreen = AppScreen.LiveScan
                    viewModel.startCameraScan()
                },
                onImportImage = {
                    viewModel.currentScreen = AppScreen.LiveScan
                    viewModel.pickImageAndScan()
                },
                onViewHistory = { viewModel.currentScreen = AppScreen.ScanHistory },
                onShowNotifications = { viewModel.showInfo(DesktopStrings.translate("Notifications are not available yet.", language)) },
                onOpenSettings = { viewModel.currentScreen = AppScreen.TrustCentreAlt },
                onCheckUpdates = { viewModel.showInfo(DesktopStrings.translate("Update checks are not available in offline mode.", language)) },
                onAnalyzeUrl = { url -> viewModel.analyzeUrlDirectly(url) },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onOpenTraining = { viewModel.currentScreen = AppScreen.Training },
                isDarkMode = viewModel.isDarkMode,
                stats = viewModel.historyStats,
                recentScans = recentScans,
                onSelectScan = { viewModel.selectHistoryItem(it) },
                formatTimestamp = { viewModel.formatTimestamp(it) },
                language = language
            )
        }
    }
}


@Composable
private fun DashboardContent(
    onStartScan: () -> Unit,
    onImportImage: () -> Unit,
    onViewHistory: () -> Unit,
    onShowNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onCheckUpdates: () -> Unit,
    onAnalyzeUrl: (String) -> Unit,
    onToggleDarkMode: () -> Unit,
    onOpenTraining: () -> Unit,
    isDarkMode: Boolean,
    stats: ScanHistoryManager.HistoryStatistics,
    recentScans: List<ScanHistoryItem>,
    onSelectScan: (ScanHistoryItem) -> Unit,
    formatTimestamp: (Long) -> String,
    language: AppLanguage
) {
    val tokens = LocalStitchTokens.current
    val colors = tokens.colors
    val t = { text: String -> DesktopStrings.translate(text, language) }
    var urlInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // App Bar / Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(colors.surface)
                .border(1.dp, colors.border)
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t("QR-SHIELD"), fontSize = 14.sp, color = colors.textSub)
                Text("/", fontSize = 14.sp, color = colors.textMuted)
                Text(t("Dashboard"), fontSize = 14.sp, color = colors.textMain, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .statusPill(colors.success.copy(alpha = 0.1f), colors.success.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.success)
                        )
                        Text(t("Engine Active"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.success)
                    }
                }
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(colors.border)
                )
                // Dark Mode Toggle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleDarkMode() }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(
                        name = if (isDarkMode) "light_mode" else "dark_mode",
                        size = 20.sp,
                        color = colors.textMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onShowNotifications() }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "notifications", size = 20.sp, color = colors.textMuted)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(colors.danger)
                            .border(2.dp, colors.surface, CircleShape)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onOpenSettings() }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "settings", size = 20.sp, color = colors.textMuted)
                }
            }
        }

        // Content Scrollable Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Hero / Welcome Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dottedPattern(spacing = 24.dp, dotColor = colors.textMuted.copy(alpha = 0.1f), dotRadius = 1.dp)
                        .padding(32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(40.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .statusPill(colors.primary.copy(alpha = 0.1f), colors.primary.copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MaterialIconRound(name = "verified", size = 14.sp, color = colors.primary)
                                    Text(
                                        text = t("Enterprise Protection Active"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp,
                                        color = colors.primary
                                    )
                                }
                            }
                            Text(
                                text = t("Secure. Offline.\nExplainable Defence."),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 46.sp,
                                color = colors.textMain
                            )
                            Text(
                                text = t("QR-SHIELD analyses potential threats directly on your hardware. Experience zero-latency phishing detection without compromising data privacy."),
                                fontSize = 18.sp,
                                color = colors.textSub,
                                lineHeight = 26.sp,
                                modifier = Modifier.widthIn(max = 520.dp)
                            )
                            // URL Input Bar
                            Surface(
                                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = colors.backgroundAlt,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .height(48.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MaterialIconRound(name = "search", size = 20.sp, color = colors.textMuted)
                                    OutlinedTextField(
                                        value = urlInput,
                                        onValueChange = { urlInput = it },
                                        placeholder = {
                                            Text(
                                                t("Paste URL to analyze (e.g., https://example.com)"),
                                                color = colors.textMuted,
                                                fontSize = 14.sp
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedTextColor = colors.textMain,
                                            unfocusedTextColor = colors.textMain,
                                            cursorColor = colors.primary
                                        )
                                    )
                                    Button(
                                        onClick = {
                                            if (urlInput.isNotBlank()) {
                                                onAnalyzeUrl(urlInput)
                                                urlInput = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            MaterialIconRound(name = "security", size = 16.sp, color = Color.White)
                                            Text(t("Analyze"), fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = onStartScan,
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    MaterialIconRound(name = "qr_code_scanner", size = 18.sp, color = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text(t("Start New Scan"), fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = onImportImage,
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, colors.borderStrong),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    MaterialIconRound(name = "upload_file", size = 18.sp, color = colors.textSub)
                                    Spacer(Modifier.width(8.dp))
                                    Text(t("Import Image"), color = colors.textSub, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        
                        // System Health Card
                        Surface(
                            modifier = Modifier.width(360.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surfaceAlt,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(t("System Health"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSub, letterSpacing = 1.sp)
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(colors.success)
                                    )
                                }
                                HealthBar(label = t("Threat Database"), valueLabel = t("Current"), color = colors.success, progress = 0.98f, colors = colors)
                                HealthBar(label = t("Heuristic Engine"), valueLabel = t("Active"), color = colors.primary, progress = 1f, colors = colors)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    HealthMetric(label = t("Threats"), value = stats.maliciousCount.toString(), modifier = Modifier.weight(1f), colors = colors)
                                    HealthMetric(label = t("Safe Scans"), value = stats.safeCount.toString(), modifier = Modifier.weight(1f), colors = colors)
                                }
                            }
                        }
                    }
                }
            }

            // Feature Cards
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                FeatureCard(
                    icon = "wifi_off",
                    title = t("Offline-First Architecture"),
                    body = t("Complete analysis is performed locally. Your camera feed and scanned data never touch an external server, ensuring absolute privacy."),
                    iconBg = colors.primary.copy(alpha = 0.1f),
                    iconColor = colors.primary,
                    ghostIcon = "cloud_off",
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
                FeatureCard(
                    icon = "manage_search",
                    title = t("Explainable Security"),
                    body = t("Don't just get a \"Block\". We provide detailed heuristic breakdowns of URL parameters, redirects, and javascript payloads."),
                    iconBg = colors.warning.copy(alpha = 0.1f),
                    iconColor = colors.warning,
                    ghostIcon = "psychology",
                    modifier = Modifier.weight(1f),
                    colors = colors
                )

                FeatureCard(
                    icon = "speed",
                    title = t("High-Performance Engine"),
                    body = t("Optimised for desktop environments. Scans are processed in under 5ms using native Kotlin Multiplatform binaries."),
                    iconBg = colors.success.copy(alpha = 0.1f),
                    iconColor = colors.success,
                    ghostIcon = "bolt",
                    modifier = Modifier.weight(1f),
                    colors = colors
                )
            }

            // Recent Scans Table
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t("Recent Scans"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.textMain)
                            Text(
                                t("View Full History"),
                                fontSize = 14.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable { onViewHistory() }
                                    .focusable()
                            )
                        }
                        Column {
                            TableHeader(
                                statusLabel = t("Status"),
                                sourceLabel = t("Source"),
                                detailsLabel = t("Details"),
                                timeLabel = t("Time"),
                                colors = colors
                            )
                            if (recentScans.isEmpty()) {
                                EmptyRecentRow(text = t("No recent scans yet."), colors = colors)
                            } else {
                                recentScans.forEach { item ->
                                    RecentScanRow(
                                        item = item,
                                        timeLabel = formatTimestamp(item.scannedAt),
                                        onClick = onSelectScan,
                                        language = language,
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                }

                // Database Status Card (Small)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .iconContainer(colors.backgroundAlt),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialIconRound(name = "storage", size = 18.sp, color = colors.textSub)
                            }
                            Text(t("Threat Database"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                        }
                        KeyValueRow(label = t("Version"), value = "v2.4.1-stable", colors = colors)
                        KeyValueRow(label = t("Last Update"), value = t("Today, 04:00 AM"), colors = colors)
                        KeyValueRow(label = t("Signatures"), value = "4,281,092", colors = colors)
                        Button(
                            onClick = onCheckUpdates,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                            border = BorderStroke(1.dp, colors.borderStrong),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            MaterialIconRound(name = "refresh", size = 16.sp, color = colors.textSub)
                            Spacer(Modifier.width(6.dp))
                            Text(t("Check for Updates"), fontSize = 14.sp, color = colors.textSub)
                        }
                    }
                }
                
                // Training Centre Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.primary.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            MaterialIconRound(name = "school", size = 24.sp, color = colors.primary)
                        }
                        Text(
                            t("Training Centre"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain
                        )
                        Text(
                            t("Learn how to identify advanced QR homograph attacks."),
                            fontSize = 14.sp,
                            color = colors.textSub,
                            lineHeight = 20.sp
                        )
                        Text(
                            t("Beat the Bot") + " →",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary,
                            modifier = Modifier
                                .clickable { onOpenTraining() }
                                .focusable()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthBar(label: String, valueLabel: String, color: Color, progress: Float, colors: com.qrshield.desktop.theme.ColorTokens) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontSize = 14.sp, color = colors.textSub)
            Text(valueLabel, fontSize = 14.sp, color = color, fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .progressFill(colors.border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .progressFill(color)
            )
        }
    }
}

@Composable
private fun HealthMetric(label: String, value: String, modifier: Modifier = Modifier, colors: com.qrshield.desktop.theme.ColorTokens) {
    Column(
        modifier = modifier
            .panelSurface(colors.backgroundAlt, colors.border)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
        Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.textSub, letterSpacing = 0.8.sp)
    }
}

@Composable
private fun FeatureCard(
    icon: String,
    title: String,
    body: String,
    iconBg: Color,
    iconColor: Color,
    ghostIcon: String,
    modifier: Modifier = Modifier,
    colors: com.qrshield.desktop.theme.ColorTokens
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .iconContainer(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = icon, size = 18.sp, color = iconColor)
                }
                MaterialIconRound(name = ghostIcon, size = 36.sp, color = colors.border)
            }
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
            Text(body, fontSize = 13.sp, color = colors.textSub, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun TableHeader(
    statusLabel: String,
    sourceLabel: String,
    detailsLabel: String,
    timeLabel: String,
    colors: com.qrshield.desktop.theme.ColorTokens
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundAlt)
            .padding(vertical = 12.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.width(120.dp))
        Text(sourceLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.width(200.dp))
        Text(detailsLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.width(160.dp))
        Text(timeLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
private fun RecentScanRow(
    item: ScanHistoryItem,
    timeLabel: String,
    onClick: (ScanHistoryItem) -> Unit,
    language: AppLanguage,
    colors: com.qrshield.desktop.theme.ColorTokens
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val statusLabel = when (item.verdict) {
        Verdict.SAFE -> t("SAFE")
        Verdict.SUSPICIOUS -> t("SUSPICIOUS")
        Verdict.MALICIOUS -> t("PHISHING")
        Verdict.UNKNOWN -> t("UNKNOWN")
    }
    val statusColor = when (item.verdict) {
        Verdict.SAFE -> colors.success
        Verdict.SUSPICIOUS -> colors.warning
        Verdict.MALICIOUS -> colors.danger
        Verdict.UNKNOWN -> colors.textMuted
    }
    val statusBg = statusColor.copy(alpha = 0.1f)

    val details = when (item.verdict) {
        Verdict.SAFE -> t("Trusted Domain")
        Verdict.SUSPICIOUS -> t("Heuristic Anomaly")
        Verdict.MALICIOUS -> t("Phishing Indicators")
        Verdict.UNKNOWN -> t("Unclassified")
    }
    val domain = item.url.removePrefix("https://").removePrefix("http://").substringBefore("/")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .focusable()
            .padding(vertical = 12.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusBg,
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
            modifier = Modifier.width(120.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MaterialIconRound(name = if (statusLabel == "SAFE") "check_circle" else "warning", size = 14.sp, color = statusColor)
                Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }
        }
        Row(modifier = Modifier.width(200.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.border),
                contentAlignment = Alignment.Center
            ) {
                Text(domain.take(2).uppercase(), fontSize = 8.sp, color = colors.textMuted)
            }
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(details, fontSize = 13.sp, color = colors.textSub, modifier = Modifier.width(160.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(timeLabel, fontSize = 12.sp, color = colors.textMuted, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
private fun EmptyRecentRow(text: String, colors: com.qrshield.desktop.theme.ColorTokens) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 13.sp, color = colors.textMuted)
    }
}

@Composable
private fun KeyValueRow(label: String, value: String, colors: com.qrshield.desktop.theme.ColorTokens) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = colors.textSub)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
    }
}
