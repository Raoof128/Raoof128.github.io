package com.qrshield.desktop.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.ScanMonitorViewMode
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.desktop.ui.gridPattern

@Composable
fun LiveScanScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanMonitor()
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
        ) {
            LiveScanSidebar(onNavigate = { viewModel.currentScreen = it })
            LiveScanContent(viewModel = viewModel)
        }
    }
}

@Composable
private fun LiveScanSidebar(onNavigate: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier
            .width(256.dp)
            .fillMaxHeight()
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE5E7EB))
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MaterialSymbol(name = "security", size = 28.sp, color = Color(0xFF2563EB))
            Text("QR-SHIELD", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text("MAIN MENU", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280), letterSpacing = 1.sp, modifier = Modifier.padding(start = 12.dp, bottom = 12.dp, top = 8.dp))
            SidebarLink(
                label = "Scan Monitor",
                icon = "qr_code_scanner",
                isActive = true,
                onClick = { onNavigate(AppScreen.LiveScan) }
            )
            SidebarLink(label = "Scan History", icon = "history", onClick = { onNavigate(AppScreen.ScanHistory) })
            SidebarLink(label = "Safe List", icon = "verified_user", onClick = { onNavigate(AppScreen.TrustCentre) })

            Text("SYSTEM", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280), letterSpacing = 1.sp, modifier = Modifier.padding(start = 12.dp, top = 28.dp, bottom = 12.dp))
            SidebarLink(label = "Settings", icon = "settings", onClick = { onNavigate(AppScreen.TrustCentreAlt) })
            SidebarLink(label = "Support", icon = "help_outline", onClick = { })
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                .background(Color(0xFFF9FAFB))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("JS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("John Smith", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Enterprise Plan", fontSize = 12.sp, color = Color(0xFF6B7280), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                MaterialSymbol(name = "expand_more", size = 18.sp, color = Color(0xFF9CA3AF))
            }
        }
    }
}

@Composable
private fun SidebarLink(label: String, icon: String, isActive: Boolean = false, onClick: () -> Unit) {
    val background = if (isActive) Color(0xFF2563EB).copy(alpha = 0.05f) else Color.Transparent
    val border = if (isActive) Color(0xFF2563EB).copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isActive) Color(0xFF2563EB) else Color(0xFF6B7280)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialSymbol(name = icon, size = 20.sp, color = textColor)
        Text(label, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun LiveScanContent(viewModel: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
                .border(1.dp, Color(0xFFE5E7EB))
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dashboard", fontSize = 14.sp, color = Color(0xFF6B7280))
                MaterialSymbol(name = "chevron_right", size = 16.sp, color = Color(0xFF9CA3AF), modifier = Modifier.padding(horizontal = 8.dp))
                Text("Scan Monitor", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827), modifier = Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFECFDF3),
                    border = BorderStroke(1.dp, Color(0xFFD1FAE5)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                        }
                        Text("Offline Engine V.2.4 Active", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF047857))
                    }
                }
                Box(modifier = Modifier.size(36.dp)) {
                    MaterialSymbol(name = "notifications", size = 20.sp, color = Color(0xFF6B7280))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Active Scanner", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Text("Real-time QR code analysis and threat detection.", fontSize = 14.sp, color = Color(0xFF6B7280))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Mode:", fontSize = 12.sp, color = Color(0xFF6B7280))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MaterialSymbol(name = "wifi_off", size = 18.sp, color = Color(0xFF10B981))
                                    Text("Offline First", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(480.dp)
                                .gridPattern(spacing = 24.dp, lineColor = Color(0xFF2563EB).copy(alpha = 0.08f), lineWidth = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            ScanFrame(modifier = Modifier.align(Alignment.Center))
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 24.dp)
                                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(999.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                    )
                                    Text("WAITING FOR INPUT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151), letterSpacing = 1.sp)
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp)
                                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF3F4F6))
                                        .border(1.dp, Color(0xFFE5E7EB), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialSymbol(name = "videocam_off", size = 32.sp, color = Color(0xFF9CA3AF))
                                }
                                Text("Camera Access Required", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827), modifier = Modifier.padding(top = 12.dp))
                                Text(
                                    "To scan QR codes directly, please enable camera access on your device or use the manual input options below.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B7280),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                                )
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                                ) {
                                    MaterialSymbol(name = "videocam", size = 18.sp, color = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Enable Camera", fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaterialSymbol(name = "bolt", size = 14.sp, color = Color(0xFF10B981))
                        Spacer(Modifier.width(6.dp))
                        Text("Local analysis engine ready (<5ms latency)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ScanActionButton(icon = "flash_on", label = "Torch", modifier = Modifier.weight(1f))
                            DividerVertical()
                            ScanActionButton(icon = "add_photo_alternate", label = "Upload Image", modifier = Modifier.weight(1f))
                            DividerVertical()
                            ScanActionButton(icon = "link", label = "Paste URL", modifier = Modifier.weight(1f))
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("SYSTEM STATUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 1.sp)
                                MaterialSymbol(name = "refresh", size = 18.sp, color = Color(0xFF9CA3AF))
                            }
                            Spacer(Modifier.height(16.dp))
                            StatusRow(
                                icon = "security",
                                iconColor = Color(0xFF10B981),
                                title = "Detection Engine",
                                value = "Phishing Guard",
                                badgeText = "READY",
                                badgeColor = Color(0xFF10B981)
                            )
                            Spacer(Modifier.height(12.dp))
                            StatusRow(
                                icon = "database",
                                iconColor = Color(0xFF2563EB),
                                title = "Database",
                                value = "Local V.2.4.0",
                                badgeText = "LATEST",
                                badgeColor = Color(0xFF2563EB)
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MaterialSymbol(name = "speed", size = 20.sp, color = Color(0xFF7C3AED))
                                    }
                                    Column {
                                        Text("Latency", fontSize = 12.sp, color = Color(0xFF6B7280))
                                        Text("4ms Avg", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                                    Box(modifier = Modifier.size(width = 4.dp, height = 8.dp).background(Color(0xFF34D399), RoundedCornerShape(999.dp)))
                                    Box(modifier = Modifier.size(width = 4.dp, height = 12.dp).background(Color(0xFF10B981), RoundedCornerShape(999.dp)))
                                    Box(modifier = Modifier.size(width = 4.dp, height = 8.dp).background(Color(0xFF34D399), RoundedCornerShape(999.dp)))
                                    Box(modifier = Modifier.size(width = 4.dp, height = 4.dp).background(Color(0xFFA7F3D0), RoundedCornerShape(999.dp)))
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxHeight(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.fillMaxHeight()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC))
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("RECENT SCANS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 1.sp)
                                Text("View All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2563EB))
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RecentScanItem(
                                    icon = "check_circle",
                                    iconBg = Color(0xFFECFDF3),
                                    iconColor = Color(0xFF10B981),
                                    domain = "wikipedia.org",
                                    time = "2m ago",
                                    badge = "SAFE",
                                    badgeBg = Color(0xFFECFDF3),
                                    badgeColor = Color(0xFF10B981)
                                )
                                RecentScanItem(
                                    icon = "warning",
                                    iconBg = Color.White,
                                    iconColor = Color(0xFFDC2626),
                                    domain = "secure-login-bank.com",
                                    time = "1h ago",
                                    badge = "PHISHING",
                                    badgeBg = Color(0xFFFEE2E2),
                                    badgeColor = Color(0xFFDC2626),
                                    highlight = Color(0xFFFEE2E2)
                                )
                                RecentScanItem(
                                    icon = "check_circle",
                                    iconBg = Color(0xFFECFDF3),
                                    iconColor = Color(0xFF10B981),
                                    domain = "menu-restaurant.com",
                                    time = "3h ago",
                                    badge = "SAFE",
                                    badgeBg = Color(0xFFECFDF3),
                                    badgeColor = Color(0xFF10B981)
                                )
                                RecentScanItem(
                                    icon = "priority_high",
                                    iconBg = Color(0xFFFFFBEB),
                                    iconColor = Color(0xFFF59E0B),
                                    domain = "bit.ly/xs82...",
                                    time = "4h ago",
                                    badge = "SUSPICIOUS",
                                    badgeBg = Color(0xFFFFFBEB),
                                    badgeColor = Color(0xFFD97706),
                                    highlight = Color(0xFFFFFBEB)
                                )
                                RecentScanItem(
                                    icon = "check_circle",
                                    iconBg = Color(0xFFECFDF3),
                                    iconColor = Color(0xFF10B981),
                                    domain = "google.com",
                                    time = "5h ago",
                                    badge = "SAFE",
                                    badgeBg = Color(0xFFECFDF3),
                                    badgeColor = Color(0xFF10B981)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE5E7EB))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MaterialSymbol(name = "download", size = 18.sp, color = Color(0xFF6B7280))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Export Log", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ScanFrame(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()
    val scanOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier.size(256.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color(0xFF2563EB).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
        )
        CornerStroke(Alignment.TopStart)
        CornerStroke(Alignment.TopEnd)
        CornerStroke(Alignment.BottomStart)
        CornerStroke(Alignment.BottomEnd)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .offset(y = (scanOffset * 240f).dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color(0xFF2563EB), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun BoxScope.CornerStroke(alignment: Alignment) {
    val shape = RoundedCornerShape(16.dp)
    val modifier = Modifier
        .size(32.dp)
        .border(3.dp, Color(0xFF2563EB), shape)
        .clip(shape)
    Box(modifier = modifier.align(alignment))
}

@Composable
private fun ScanActionButton(icon: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            MaterialSymbol(name = icon, size = 20.sp, color = Color(0xFF6B7280))
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
    }
}

@Composable
private fun DividerVertical() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(60.dp)
            .background(Color(0xFFF3F4F6))
    )
}

@Composable
private fun StatusRow(
    icon: String,
    iconColor: Color,
    title: String,
    value: String,
    badgeText: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(name = icon, size = 20.sp, color = iconColor)
            }
            Column {
                Text(title, fontSize = 12.sp, color = Color(0xFF6B7280))
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .border(1.dp, badgeColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
        }
    }
}

@Composable
private fun RecentScanItem(
    icon: String,
    iconBg: Color,
    iconColor: Color,
    domain: String,
    time: String,
    badge: String,
    badgeBg: Color,
    badgeColor: Color,
    highlight: Color = Color.Transparent
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(highlight)
            .border(1.dp, if (highlight == Color.Transparent) Color.Transparent else badgeColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg)
                .border(1.dp, iconColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            MaterialSymbol(name = icon, size = 18.sp, color = iconColor)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(domain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (badge == "PHISHING") Color(0xFF7F1D1D) else Color(0xFF111827), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(time, fontSize = 12.sp, color = if (badge == "PHISHING") Color(0xFFDC2626) else Color(0xFF6B7280))
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFD1D5DB)))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .border(1.dp, badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor, letterSpacing = 0.8.sp)
                }
            }
        }
        MaterialSymbol(name = if (badge == "PHISHING") "arrow_forward" else "chevron_right", size = 18.sp, color = badgeColor.copy(alpha = 0.5f))
    }
}
