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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.MaterialSymbol

@Composable
fun ScanHistoryScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanHistory()
    StitchTheme(tokens = tokens) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            ScanHistoryHeader(onNavigate = { viewModel.currentScreen = it })
            ScanHistoryContent(onNavigate = { viewModel.currentScreen = it })
        }
    }
}

@Composable
private fun ScanHistoryHeader(onNavigate: (AppScreen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.9f))
            .border(1.dp, Color(0xFFE2E8F0))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(name = "qr_code_scanner", size = 20.sp, color = Color(0xFF135BEC))
            }
            Text("QR-SHIELD", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF1F5F9))
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HeaderNavItem(label = "Dashboard", onClick = { onNavigate(AppScreen.Dashboard) })
            HeaderNavItem(label = "Scan History", isActive = true, onClick = { onNavigate(AppScreen.ScanHistory) })
            HeaderNavItem(label = "Engine Config", onClick = { onNavigate(AppScreen.TrustCentreAlt) })
            HeaderNavItem(label = "Logs", onClick = { onNavigate(AppScreen.ReportsExport) })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(name = "notifications", size = 18.sp, color = Color(0xFF94A3B8))
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(name = "settings", size = 18.sp, color = Color(0xFF94A3B8))
            }
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color(0xFFE2E8F0))
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImageAvatar()
                Text("Admin", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                MaterialSymbol(name = "expand_more", size = 16.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun HeaderNavItem(label: String, isActive: Boolean = false, onClick: () -> Unit) {
    val bg = if (isActive) Color.White else Color.Transparent
    val text = if (isActive) Color(0xFF135BEC) else Color(0xFF64748B)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(if (isActive) 1.dp else 0.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium, color = text)
    }
}

@Composable
private fun ImageAvatar() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(2.dp, Color(0xFFE2E8F0))
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource("assets/stitch/avatar-admin.png"),
            contentDescription = "Admin Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ScanHistoryContent(onNavigate: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Scan History", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFDBEAFE))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text("LIVE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF135BEC), letterSpacing = 1.sp)
                    }
                }
                Text(
                    "Real-time audit logs of all QR code captures, including verdicts from the local heuristic engine.",
                    fontSize = 16.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.widthIn(max = 640.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialSymbol(name = "calendar_today", size = 16.sp, color = Color(0xFF94A3B8))
                        Text("Oct 24 - Oct 25", fontSize = 14.sp, color = Color(0xFF64748B))
                    }
                }
                Surface(
                    modifier = Modifier
                        .clickable { onNavigate(AppScreen.ReportsExport) }
                        .focusable(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF135BEC)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialSymbol(name = "download", size = 18.sp, color = Color.White)
                        Text("Export CSV", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(title = "Total Scans (24h)", value = "1,204", delta = "5%", deltaIcon = "trending_up", deltaColor = Color(0xFF10B981), modifier = Modifier.weight(1f))
            MetricCard(title = "Threats Blocked", value = "12", delta = "2%", deltaIcon = "trending_down", deltaColor = Color(0xFFE11D48), modifier = Modifier.weight(1f))
            MetricCard(title = "Suspicious Flags", value = "45", delta = "8", deltaIcon = "trending_up", deltaColor = Color(0xFFD97706), modifier = Modifier.weight(1f))
            MetricCard(title = "Safe Scans", value = "1,147", delta = "+8%", deltaIcon = "", deltaColor = Color(0xFF10B981), modifier = Modifier.weight(1f))
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .width(320.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            MaterialSymbol(name = "search", size = 18.sp, color = Color(0xFF94A3B8))
                            Spacer(Modifier.width(8.dp))
                            Text("Search domains, sources, or hashes...", fontSize = 13.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(start = 24.dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(label = "All Scans", active = true)
                        FilterChip(label = "Safe", color = Color(0xFF0D9488), background = Color(0xFFF0FDFA))
                        FilterChip(label = "Suspicious", color = Color(0xFFD97706), background = Color(0xFFFFFBEB))
                        FilterChip(label = "Dangerous", color = Color(0xFFE11D48), background = Color(0xFFFFF1F2))
                        Box(modifier = Modifier.size(24.dp).background(Color(0xFFCBD5E1)).width(1.dp))
                        FilterChip(label = "Advanced", icon = "filter_list", background = Color.White, border = Color(0xFFE2E8F0), color = Color(0xFF64748B))
                    }
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    TableHeaderRow()
                    HistoryRow(
                        riskIcon = "gpp_bad",
                        riskColor = Color(0xFFE11D48),
                        riskBg = Color(0xFFFFF1F2),
                        domain = "login.apple-id-verify.com",
                        detail = "Phishing Heuristic Match",
                        sourceIcon = "videocam",
                        source = "Webcam",
                        time = "2 mins ago",
                        verdict = "BLOCKED",
                        verdictColor = Color(0xFFE11D48),
                        verdictBg = Color(0xFFFFF1F2),
                        animateDot = true
                    )
                    HistoryRow(
                        riskIcon = "warning",
                        riskColor = Color(0xFFD97706),
                        riskBg = Color(0xFFFFFBEB),
                        domain = "bit.ly/3x89s",
                        detail = "Shortened URL / Obfuscated",
                        sourceIcon = "upload_file",
                        source = "File Upload",
                        time = "15 mins ago",
                        verdict = "FLAGGED",
                        verdictColor = Color(0xFFD97706),
                        verdictBg = Color(0xFFFFFBEB)
                    )
                    HistoryRow(
                        riskIcon = "shield",
                        riskColor = Color(0xFF0D9488),
                        riskBg = Color(0xFFF0FDFA),
                        domain = "menu.restaurant.com/t/883",
                        detail = "Known Domain",
                        sourceIcon = "videocam",
                        source = "Webcam",
                        time = "1 hour ago",
                        verdict = "ALLOWED",
                        verdictColor = Color(0xFF0D9488),
                        verdictBg = Color(0xFFF0FDFA)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Show rows:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("10", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        Text("Showing 1-7 of 1,204", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        PaginationButton("chevron_left", disabled = true)
                        PaginationNumber("1", active = true)
                        PaginationNumber("2")
                        PaginationNumber("3")
                        Text("...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        PaginationNumber("12")
                        PaginationButton("chevron_right")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, delta: String, deltaIcon: String, deltaColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontSize = 14.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                if (deltaIcon.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(deltaColor.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaterialSymbol(name = deltaIcon, size = 14.sp, color = deltaColor)
                        Text(delta, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = deltaColor)
                    }
                } else {
                    Text(delta, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = deltaColor)
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    active: Boolean = false,
    color: Color = Color(0xFF0F172A),
    background: Color = Color(0xFFF1F5F9),
    border: Color = Color.Transparent,
    icon: String? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0xFF0F172A) else background)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            MaterialSymbol(name = icon, size = 14.sp, color = color)
        }
        if (!active && label in listOf("Safe", "Suspicious", "Dangerous")) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (active) Color.White else color)
    }
}

@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC))
            .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Risk", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(60.dp))
        Text("Domain / Payload", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.weight(1f))
        Text("Source", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(140.dp))
        Text("Time", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(120.dp))
        Text("Verdict", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(120.dp))
        Text("Actions", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun HistoryRow(
    riskIcon: String,
    riskColor: Color,
    riskBg: Color,
    domain: String,
    detail: String,
    sourceIcon: String,
    source: String,
    time: String,
    verdict: String,
    verdictColor: Color,
    verdictBg: Color,
    animateDot: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(riskBg)
                .border(1.dp, riskColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            MaterialSymbol(name = riskIcon, size = 16.sp, color = riskColor)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, fontSize = 12.sp, color = Color(0xFF94A3B8))
        }
        Row(modifier = Modifier.width(140.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MaterialSymbol(name = sourceIcon, size = 14.sp, color = Color(0xFF94A3B8))
            Text(source, fontSize = 12.sp, color = Color(0xFF475569))
        }
        Text(time, fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.width(120.dp))
        Row(
            modifier = Modifier
                .width(120.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(verdictBg)
                .border(1.dp, verdictColor.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (animateDot) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(verdictColor))
            }
            Text(verdict, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = verdictColor)
        }
        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.CenterEnd) {
            MaterialSymbol(name = "more_horiz", size = 18.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun PaginationButton(icon: String, disabled: Boolean = false) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        MaterialSymbol(name = icon, size = 16.sp, color = if (disabled) Color(0xFFCBD5E1) else Color(0xFF94A3B8))
    }
}

@Composable
private fun PaginationNumber(number: String, active: Boolean = false) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0xFF135BEC) else Color.Transparent)
            .border(1.dp, if (active) Color.Transparent else Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(number, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else Color(0xFF64748B))
    }
}
