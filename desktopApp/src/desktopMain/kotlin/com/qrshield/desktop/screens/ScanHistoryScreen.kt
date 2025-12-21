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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.HistoryFilter
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict

@Composable
fun ScanHistoryScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanHistory()
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            ScanHistorySidebar(onNavigate = { viewModel.currentScreen = it })
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                ScanHistoryHeader(
                    onNavigate = { viewModel.currentScreen = it },
                    onShowNotifications = { viewModel.showInfo("Notifications are not available yet.") },
                    onOpenSettings = { viewModel.currentScreen = AppScreen.TrustCentreAlt }
                )
                ScanHistoryContent(
                    viewModel = viewModel,
                    onNavigate = { viewModel.currentScreen = it }
                )
            }
        }
    }
}

@Composable
private fun ScanHistorySidebar(onNavigate: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier
            .width(256.dp)
            .fillMaxHeight()
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MaterialSymbol(name = "qr_code_scanner", size = 22.sp, color = Color(0xFF135BEC))
            Text("QR-SHIELD", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("MAIN MENU", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), letterSpacing = 1.sp, modifier = Modifier.padding(start = 12.dp, top = 8.dp))
            SidebarNavItem("Dashboard", "dashboard", onNavigate, AppScreen.Dashboard)
            SidebarNavItem("Scan Monitor", "qr_code_scanner", onNavigate, AppScreen.LiveScan)
            SidebarNavItem("Scan History", "history", onNavigate, AppScreen.ScanHistory, isActive = true)
            SidebarNavItem("Trust Centre", "verified_user", onNavigate, AppScreen.TrustCentre)
            Text("SYSTEM", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8), letterSpacing = 1.sp, modifier = Modifier.padding(start = 12.dp, top = 16.dp))
            SidebarNavItem("Reports", "description", onNavigate, AppScreen.ReportsExport)
            SidebarNavItem("Settings", "settings", onNavigate, AppScreen.TrustCentreAlt)
            SidebarNavItem("Training", "school", onNavigate, AppScreen.Training)
        }
    }
}

@Composable
private fun SidebarNavItem(
    label: String,
    icon: String,
    onNavigate: (AppScreen) -> Unit,
    target: AppScreen,
    isActive: Boolean = false
) {
    val background = if (isActive) Color(0xFF135BEC).copy(alpha = 0.08f) else Color.Transparent
    val textColor = if (isActive) Color(0xFF135BEC) else Color(0xFF64748B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable { onNavigate(target) }
            .focusable()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialSymbol(name = icon, size = 18.sp, color = textColor)
        Text(label, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun ScanHistoryHeader(
    onNavigate: (AppScreen) -> Unit,
    onShowNotifications: () -> Unit,
    onOpenSettings: () -> Unit
) {
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
                    .border(1.dp, Color(0xFFE2E8F0))
                    .clickable { onShowNotifications() }
                    .focusable(),
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(name = "notifications", size = 18.sp, color = Color(0xFF94A3B8))
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0))
                    .clickable { onOpenSettings() }
                    .focusable(),
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
            .focusable()
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
private fun ScanHistoryContent(viewModel: AppViewModel, onNavigate: (AppScreen) -> Unit) {
    val stats = viewModel.historyStats
    val history = viewModel.filteredHistory()
    val searchQuery = viewModel.historySearchQuery
    val visibleCount = history.size
    val countLabel = if (visibleCount == 0) {
        "Showing 0 of ${stats.totalScans}"
    } else {
        "Showing 1-$visibleCount of ${stats.totalScans}"
    }
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
                        .clickable {
                            viewModel.exportHistoryCsv()
                            onNavigate(AppScreen.ReportsExport)
                        }
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                title = "Total Scans (24h)",
                value = stats.totalScans.toString(),
                delta = "0%",
                deltaIcon = "trending_up",
                deltaColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Threats Blocked",
                value = stats.maliciousCount.toString(),
                delta = "0%",
                deltaIcon = "trending_down",
                deltaColor = Color(0xFFE11D48),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Suspicious Flags",
                value = stats.suspiciousCount.toString(),
                delta = "0",
                deltaIcon = "trending_up",
                deltaColor = Color(0xFFD97706),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Safe Scans",
                value = stats.safeCount.toString(),
                delta = "0%",
                deltaIcon = "",
                deltaColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
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
                                .padding(horizontal = 12.dp)
                        ) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateHistorySearch(it) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF0F172A)),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        MaterialSymbol(name = "search", size = 18.sp, color = Color(0xFF94A3B8))
                                        Spacer(Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (searchQuery.isBlank()) {
                                                Text("Search domains, sources, or hashes...", fontSize = 13.sp, color = Color(0xFF94A3B8))
                                            }
                                            innerTextField()
                                        }
                                    }
                                }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            label = "All Scans",
                            active = viewModel.historyFilter == HistoryFilter.All,
                            onClick = { viewModel.updateHistoryFilter(HistoryFilter.All) }
                        )
                        FilterChip(
                            label = "Safe",
                            active = viewModel.historyFilter == HistoryFilter.Safe,
                            color = Color(0xFF0D9488),
                            background = Color(0xFFF0FDFA),
                            onClick = { viewModel.updateHistoryFilter(HistoryFilter.Safe) }
                        )
                        FilterChip(
                            label = "Suspicious",
                            active = viewModel.historyFilter == HistoryFilter.Suspicious,
                            color = Color(0xFFD97706),
                            background = Color(0xFFFFFBEB),
                            onClick = { viewModel.updateHistoryFilter(HistoryFilter.Suspicious) }
                        )
                        FilterChip(
                            label = "Dangerous",
                            active = viewModel.historyFilter == HistoryFilter.Dangerous,
                            color = Color(0xFFE11D48),
                            background = Color(0xFFFFF1F2),
                            onClick = { viewModel.updateHistoryFilter(HistoryFilter.Dangerous) }
                        )
                        Box(modifier = Modifier.size(24.dp).background(Color(0xFFCBD5E1)).width(1.dp))
                        FilterChip(
                            label = "Advanced",
                            icon = "filter_list",
                            background = Color.White,
                            border = Color(0xFFE2E8F0),
                            color = Color(0xFF64748B),
                            onClick = { viewModel.showInfo("Advanced filters are not available yet.") }
                        )
                    }
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    TableHeaderRow()
                    if (history.isEmpty()) {
                        EmptyHistoryRow()
                    } else {
                        history.forEach { item ->
                            HistoryRow(
                                item = item,
                                timeLabel = viewModel.formatRelativeTime(item.scannedAt),
                                onClick = { viewModel.selectHistoryItem(it) }
                            )
                        }
                    }
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
                        Text(countLabel, fontSize = 12.sp, color = Color(0xFF94A3B8))
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
    icon: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0xFF0F172A) else background)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .focusable()
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
    item: ScanHistoryItem,
    timeLabel: String,
    onClick: (ScanHistoryItem) -> Unit
) {
    val riskIcon = when (item.verdict) {
        Verdict.SAFE -> "shield"
        Verdict.SUSPICIOUS -> "warning"
        Verdict.MALICIOUS -> "gpp_bad"
        Verdict.UNKNOWN -> "help"
    }
    val riskColor = when (item.verdict) {
        Verdict.SAFE -> Color(0xFF0D9488)
        Verdict.SUSPICIOUS -> Color(0xFFD97706)
        Verdict.MALICIOUS -> Color(0xFFE11D48)
        Verdict.UNKNOWN -> Color(0xFF94A3B8)
    }
    val riskBg = when (item.verdict) {
        Verdict.SAFE -> Color(0xFFF0FDFA)
        Verdict.SUSPICIOUS -> Color(0xFFFFFBEB)
        Verdict.MALICIOUS -> Color(0xFFFFF1F2)
        Verdict.UNKNOWN -> Color(0xFFF1F5F9)
    }
    val detail = when (item.verdict) {
        Verdict.SAFE -> "Known Domain"
        Verdict.SUSPICIOUS -> "Heuristic Anomaly"
        Verdict.MALICIOUS -> "Phishing Heuristic Match"
        Verdict.UNKNOWN -> "Unclassified"
    }
    val sourceIcon = when (item.source) {
        ScanSource.CAMERA -> "videocam"
        ScanSource.GALLERY -> "upload_file"
        ScanSource.CLIPBOARD -> "content_paste"
    }
    val sourceLabel = when (item.source) {
        ScanSource.CAMERA -> "Webcam"
        ScanSource.GALLERY -> "File Upload"
        ScanSource.CLIPBOARD -> "Clipboard"
    }
    val verdictLabel = when (item.verdict) {
        Verdict.SAFE -> "ALLOWED"
        Verdict.SUSPICIOUS -> "FLAGGED"
        Verdict.MALICIOUS -> "BLOCKED"
        Verdict.UNKNOWN -> "UNKNOWN"
    }
    val verdictBg = riskBg
    val verdictColor = riskColor
    val animateDot = item.verdict == Verdict.MALICIOUS
    val domain = item.url.removePrefix("https://").removePrefix("http://").substringBefore("/")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .focusable()
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
            Text(sourceLabel, fontSize = 12.sp, color = Color(0xFF475569))
        }
        Text(timeLabel, fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.width(120.dp))
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
            Text(verdictLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = verdictColor)
        }
        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.CenterEnd) {
            MaterialSymbol(name = "more_horiz", size = 18.sp, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
private fun EmptyHistoryRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("No scan history yet. Run a scan to populate results.", fontSize = 12.sp, color = Color(0xFF94A3B8))
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
