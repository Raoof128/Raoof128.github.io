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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.qrshield.data.ScanHistoryManager
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.Verdict

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
                language = viewModel.appLanguage
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
                onShowNotifications = { viewModel.showInfo("Notifications are not available yet.") },
                onOpenSettings = { viewModel.currentScreen = AppScreen.TrustCentreAlt },
                onCheckUpdates = { viewModel.showInfo("Update checks are not available in offline mode.") },
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
    stats: ScanHistoryManager.HistoryStatistics,
    recentScans: List<ScanHistoryItem>,
    onSelectScan: (ScanHistoryItem) -> Unit,
    formatTimestamp: (Long) -> String,
    language: AppLanguage
) {
    val colors = MaterialTheme.colorScheme
    val t = { text: String -> DesktopStrings.translate(text, language) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(colors.surface)
                .border(1.dp, colors.outline.copy(alpha = 0.4f))
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t("QR-SHIELD"), fontSize = 14.sp, color = Color(0xFF6B7280))
                Text("/", fontSize = 14.sp, color = Color(0xFF94A3B8))
                Text(t("Dashboard"), fontSize = 14.sp, color = colors.onSurface, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                        )
                        Text(t("Engine Active"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onShowNotifications() }
                        .focusable()
                ) {
                    MaterialIconRound(name = "notifications", size = 20.sp, color = Color(0xFF94A3B8))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
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
                    MaterialIconRound(name = "settings", size = 20.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .dottedPattern(spacing = 24.dp, dotColor = Color(0xFF64748B).copy(alpha = 0.03f), dotRadius = 1.dp)
                        .padding(32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(40.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color(0xFFDBEAFE),
                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MaterialIconRound(name = "verified", size = 14.sp, color = Color(0xFF2563EB))
                                    Text(
                                        text = t("Enterprise Protection Active"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                            }
                            Text(
                                text = t("Secure. Offline.\nExplainable Defence."),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 46.sp,
                                color = colors.onSurface
                            )
                            Text(
                                text = t("QR-SHIELD analyses potential threats directly on your hardware. Experience zero-latency phishing detection without compromising data privacy."),
                                fontSize = 18.sp,
                                color = Color(0xFF6B7280),
                                lineHeight = 26.sp,
                                modifier = Modifier.widthIn(max = 520.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = onStartScan,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    MaterialIconRound(name = "qr_code_scanner", size = 18.sp, color = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text(t("Start New Scan"), fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = onImportImage,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFFFF)),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    MaterialIconRound(name = "upload_file", size = 18.sp, color = Color(0xFF64748B))
                                    Spacer(Modifier.width(8.dp))
                                    Text(t("Import Image"), color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Surface(
                            modifier = Modifier.width(360.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFFFFF),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(t("System Health"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 1.sp)
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                }
                                HealthBar(label = t("Threat Database"), valueLabel = t("Current"), color = Color(0xFF10B981), progress = 0.98f)
                                HealthBar(label = t("Heuristic Engine"), valueLabel = t("Active"), color = Color(0xFF2563EB), progress = 1f)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    HealthMetric(label = t("Threats"), value = stats.maliciousCount.toString(), modifier = Modifier.weight(1f))
                                    HealthMetric(label = t("Safe Scans"), value = stats.safeCount.toString(), modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                FeatureCard(
                    icon = "wifi_off",
                    title = t("Offline-First Architecture"),
                    body = t("Complete analysis is performed locally. Your camera feed and scanned data never touch an external server, ensuring absolute privacy."),
                    iconBg = Color(0xFFDBEAFE),
                    iconColor = Color(0xFF2563EB),
                    ghostIcon = "cloud_off",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    icon = "manage_search",
                    title = t("Explainable Security"),
                    body = t("Don't just get a \"Block\". We provide detailed heuristic breakdowns of URL parameters, redirects, and javascript payloads."),
                    iconBg = Color(0xFFEDE9FE),
                    iconColor = Color(0xFF8B5CF6),
                    ghostIcon = "psychology",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    icon = "speed",
                    title = t("High-Performance Engine"),
                    body = t("Optimised for desktop environments. Scans are processed in under 5ms using native Kotlin Multiplatform binaries."),
                    iconBg = Color(0xFFD1FAE5),
                    iconColor = Color(0xFF10B981),
                    ghostIcon = "bolt",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t("Recent Scans"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
                            Text(
                                t("View Full History"),
                                fontSize = 14.sp,
                                color = Color(0xFF2563EB),
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
                                timeLabel = t("Time")
                            )
                            if (recentScans.isEmpty()) {
                                EmptyRecentRow(text = t("No recent scans yet."))
                            } else {
                                recentScans.forEach { item ->
                                    RecentScanRow(
                                        item = item,
                                        timeLabel = formatTimestamp(item.scannedAt),
                                        onClick = onSelectScan,
                                        language = language
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialIconRound(name = "storage", size = 18.sp, color = Color(0xFF64748B))
                            }
                            Text(t("Threat Database"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                        KeyValueRow(label = t("Version"), value = "v2.4.1-stable")
                        KeyValueRow(label = t("Last Update"), value = t("Today, 04:00 AM"))
                        KeyValueRow(label = t("Signatures"), value = "4,281,092")
                        Button(
                            onClick = onCheckUpdates,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFFFF)),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            MaterialIconRound(name = "refresh", size = 16.sp, color = Color(0xFF64748B))
                            Spacer(Modifier.width(6.dp))
                            Text(t("Check for Updates"), fontSize = 14.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthBar(label: String, valueLabel: String, color: Color, progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontSize = 14.sp, color = Color(0xFF64748B))
            Text(valueLabel, fontSize = 14.sp, color = color, fontWeight = FontWeight.Medium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFFE5E7EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun HealthMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), letterSpacing = 0.8.sp)
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = icon, size = 18.sp, color = iconColor)
                }
                MaterialIconRound(name = ghostIcon, size = 36.sp, color = Color(0xFFE2E8F0))
            }
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text(body, fontSize = 13.sp, color = Color(0xFF6B7280), lineHeight = 18.sp)
        }
    }
}

@Composable
private fun TableHeader(
    statusLabel: String,
    sourceLabel: String,
    detailsLabel: String,
    timeLabel: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .padding(vertical = 12.dp, horizontal = 24.dp)
    ) {
        Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(120.dp))
        Text(sourceLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(200.dp))
        Text(detailsLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.weight(1f))
        Text(timeLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun RecentScanRow(
    item: ScanHistoryItem,
    timeLabel: String,
    onClick: (ScanHistoryItem) -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val statusLabel = when (item.verdict) {
        Verdict.SAFE -> t("SAFE")
        Verdict.SUSPICIOUS -> t("SUSPICIOUS")
        Verdict.MALICIOUS -> t("PHISHING")
        Verdict.UNKNOWN -> t("UNKNOWN")
    }
    val statusColor = when (item.verdict) {
        Verdict.SAFE -> Color(0xFF10B981)
        Verdict.SUSPICIOUS -> Color(0xFFF59E0B)
        Verdict.MALICIOUS -> Color(0xFFEF4444)
        Verdict.UNKNOWN -> Color(0xFF94A3B8)
    }
    val statusBg = when (item.verdict) {
        Verdict.SAFE -> Color(0xFFD1FAE5)
        Verdict.SUSPICIOUS -> Color(0xFFFFFBEB)
        Verdict.MALICIOUS -> Color(0xFFFEE2E2)
        Verdict.UNKNOWN -> Color(0xFFF1F5F9)
    }
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
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Text(domain.take(2).uppercase(), fontSize = 8.sp, color = Color(0xFF94A3B8))
            }
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(details, fontSize = 13.sp, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
        Text(timeLabel, fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun EmptyRecentRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 13.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = Color(0xFF6B7280))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    }
}
