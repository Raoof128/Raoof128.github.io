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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.MaterialIconRound
import com.qrshield.desktop.ui.dottedPattern

@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.dashboard(isDark = viewModel.isDarkMode)
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.colors.background)
        ) {
            DashboardSidebar(
                isDark = viewModel.isDarkMode,
                onNavigate = { viewModel.currentScreen = it }
            )
            DashboardContent(
                onStartScan = { viewModel.currentScreen = AppScreen.LiveScan },
                onImportImage = { viewModel.currentScreen = AppScreen.LiveScan },
                onViewHistory = { viewModel.currentScreen = AppScreen.ScanHistory }
            )
        }
    }
}

@Composable
private fun DashboardSidebar(
    isDark: Boolean,
    onNavigate: (AppScreen) -> Unit
) {
    val tokens = MaterialTheme.colorScheme
    val bg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val border = if (isDark) Color(0xFF334155) else Color(0xFFE5E7EB)

    Column(
        modifier = Modifier
            .width(256.dp)
            .fillMaxHeight()
            .background(bg)
            .border(1.dp, border)
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .border(1.dp, border)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MaterialIconRound(
                name = "security",
                size = 20.sp,
                color = Color(0xFF2563EB)
            )
            Text(
                text = "QR-SHIELD",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "OVERVIEW",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SidebarItem(
                label = "Dashboard",
                icon = "dashboard",
                isActive = true,
                onClick = { onNavigate(AppScreen.Dashboard) }
            )
            SidebarItem(
                label = "Live Scanner",
                icon = "qr_code_scanner",
                onClick = { onNavigate(AppScreen.LiveScan) }
            )
            SidebarItem(
                label = "Scan History",
                icon = "history",
                onClick = { onNavigate(AppScreen.ScanHistory) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SECURITY",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SidebarItem(label = "Allow List", icon = "verified_user", onClick = { onNavigate(AppScreen.TrustCentre) })
            SidebarItem(label = "Heuristics Rules", icon = "policy", onClick = { onNavigate(AppScreen.TrustCentre) })
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialIconRound(name = "wifi_off", size = 14.sp, color = Color(0xFF10B981))
                        Text(
                            text = "OFFLINE READY",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                    Text(
                        text = "Local database v2.4.1 active. No data leaves this device.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Text("JS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("John Smith", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color(0xFF0F172A))
                Text("Security Analyst", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
            MaterialIconRound(name = "expand_more", size = 20.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun SidebarItem(
    label: String,
    icon: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    val activeBackground = Color(0xFFDBEAFE)
    val activeText = Color(0xFF2563EB)
    val inactiveText = Color(0xFF64748B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) activeBackground else Color.Transparent)
            .clickable { onClick() }
            .border(
                width = if (isActive) 0.dp else 0.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialIconRound(name = icon, size = 18.sp, color = if (isActive) activeText else inactiveText)
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
            color = if (isActive) activeText else inactiveText
        )
    }
}

@Composable
private fun DashboardContent(
    onStartScan: () -> Unit,
    onImportImage: () -> Unit,
    onViewHistory: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

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
                Text("QR-SHIELD", fontSize = 14.sp, color = Color(0xFF6B7280))
                Text("/", fontSize = 14.sp, color = Color(0xFF94A3B8))
                Text("Dashboard", fontSize = 14.sp, color = colors.onSurface, fontWeight = FontWeight.SemiBold)
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
                        Text("Engine Active", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                    }
                }
                Box(modifier = Modifier.size(32.dp)) {
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
                MaterialIconRound(name = "settings", size = 20.sp, color = Color(0xFF94A3B8))
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
                                        text = "Enterprise Protection Active",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                            }
                            Text(
                                text = "Secure. Offline.\nExplainable Defence.",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 46.sp,
                                color = colors.onSurface
                            )
                            Text(
                                text = "QR-SHIELD analyses potential threats directly on your hardware. Experience zero-latency phishing detection without compromising data privacy.",
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
                                    Text("Start New Scan", fontWeight = FontWeight.SemiBold)
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
                                    Text("Import Image", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
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
                                    Text("System Health", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 1.sp)
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                }
                                HealthBar(label = "Threat Database", valueLabel = "Current", color = Color(0xFF10B981), progress = 0.98f)
                                HealthBar(label = "Heuristic Engine", valueLabel = "Active", color = Color(0xFF2563EB), progress = 1f)
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    HealthMetric(label = "Threats", value = "0", modifier = Modifier.weight(1f))
                                    HealthMetric(label = "Safe Scans", value = "124", modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                FeatureCard(
                    icon = "wifi_off",
                    title = "Offline-First Architecture",
                    body = "Complete analysis is performed locally. Your camera feed and scanned data never touch an external server, ensuring absolute privacy.",
                    iconBg = Color(0xFFDBEAFE),
                    iconColor = Color(0xFF2563EB),
                    ghostIcon = "cloud_off",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    icon = "manage_search",
                    title = "Explainable Security",
                    body = "Don't just get a \"Block\". We provide detailed heuristic breakdowns of URL parameters, redirects, and javascript payloads.",
                    iconBg = Color(0xFFEDE9FE),
                    iconColor = Color(0xFF8B5CF6),
                    ghostIcon = "psychology",
                    modifier = Modifier.weight(1f)
                )
                FeatureCard(
                    icon = "speed",
                    title = "High-Performance Engine",
                    body = "Optimised for desktop environments. Scans are processed in under 5ms using native Kotlin Multiplatform binaries.",
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
                            Text("Recent Scans", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
                            Text(
                                "View Full History",
                                fontSize = 14.sp,
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .clickable { onViewHistory() }
                                    .focusable()
                            )
                        }
                        Column {
                            TableHeader()
                            TableRowItem(
                                status = "SAFE",
                                statusColor = Color(0xFF10B981),
                                statusBg = Color(0xFFD1FAE5),
                                source = "github.com/login",
                                details = "Valid TLS cert, Trusted Domain",
                                time = "10:42 AM"
                            )
                            TableRowItem(
                                status = "PHISHING",
                                statusColor = Color(0xFFEF4444),
                                statusBg = Color(0xFFFEE2E2),
                                source = "secure-login-bank.com",
                                details = "Homoglyph detected, Blocked",
                                time = "09:15 AM"
                            )
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
                            Text("Threat Database", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                        KeyValueRow(label = "Version", value = "v2.4.1-stable")
                        KeyValueRow(label = "Last Update", value = "Today, 04:00 AM")
                        KeyValueRow(label = "Signatures", value = "4,281,092")
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFFFF)),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            MaterialIconRound(name = "refresh", size = 16.sp, color = Color(0xFF64748B))
                            Spacer(Modifier.width(6.dp))
                            Text("Check for Updates", fontSize = 14.sp, color = Color(0xFF64748B))
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
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .padding(vertical = 12.dp, horizontal = 24.dp)
    ) {
        Text("Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(120.dp))
        Text("Source", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(200.dp))
        Text("Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.weight(1f))
        Text("Time", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun TableRowItem(
    status: String,
    statusColor: Color,
    statusBg: Color,
    source: String,
    details: String,
    time: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                MaterialIconRound(name = if (status == "SAFE") "check_circle" else "warning", size = 14.sp, color = statusColor)
                Text(status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor)
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
                Text(source.take(2).uppercase(), fontSize = 8.sp, color = Color(0xFF94A3B8))
            }
            Text(source, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(details, fontSize = 13.sp, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
        Text(time, fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = Color(0xFF6B7280))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    }
}
