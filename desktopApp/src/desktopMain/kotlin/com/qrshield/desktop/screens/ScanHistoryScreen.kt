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
import com.qrshield.desktop.SampleData
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.theme.LocalStitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import com.qrshield.desktop.ui.iconContainer
import com.qrshield.desktop.ui.panelSurface
import com.qrshield.desktop.ui.surfaceBorder
import com.qrshield.desktop.ui.statusPill
import com.qrshield.desktop.ui.pillShape

@Composable
fun ScanHistoryScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanHistory(isDark = viewModel.isDarkMode)
    val language = viewModel.appLanguage
    StitchTheme(tokens = tokens) {
        val colors = LocalStitchTokens.current.colors
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            AppSidebar(
                currentScreen = viewModel.currentScreen,
                onNavigate = { viewModel.currentScreen = it },
                language = viewModel.appLanguage,
                onProfileClick = { viewModel.currentScreen = AppScreen.TrustCentreAlt }
            )
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                ScanHistoryHeader(
                    onNavigate = { viewModel.currentScreen = it },
                    onShowNotifications = { viewModel.showInfo(DesktopStrings.translate("Notifications are not available yet.", language)) },
                    onOpenSettings = { viewModel.currentScreen = AppScreen.TrustCentreAlt },
                    language = language
                )
                ScanHistoryContent(
                    viewModel = viewModel,
                    onNavigate = { viewModel.currentScreen = it },
                    language = language
                )
            }
        }
    }
}

@Composable
private fun ScanHistoryHeader(
    onNavigate: (AppScreen) -> Unit,
    onShowNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface.copy(alpha = 0.9f))
            .border(1.dp, colors.border)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(t("QR-SHIELD"), fontSize = 14.sp, color = colors.textSub)
            Text("/", fontSize = 14.sp, color = colors.textMuted)
            Text(t("Scan History"), fontSize = 14.sp, color = colors.textMain, fontWeight = FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.backgroundAlt)
                    .border(1.dp, colors.border)
                    .clickable { onShowNotifications() }
                    .focusable(),
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(name = "notifications", size = 18.sp, color = colors.textMuted)
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.backgroundAlt)
                    .border(1.dp, colors.border)
                    .clickable { onOpenSettings() }
                    .focusable(),
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(name = "settings", size = 18.sp, color = colors.textMuted)
            }
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(colors.border)
            )
            Row(
                modifier = Modifier
                    .pillShape(Color.Transparent)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val userProfile = SampleData.userProfile
                ImageAvatar(language)
                Text(t(userProfile.name), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textSub)
                MaterialSymbol(name = "expand_more", size = 16.sp, color = colors.textMuted)
            }
        }
    }
}

@Composable
private fun ImageAvatar(language: AppLanguage) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    val userProfile = SampleData.userProfile
    val colors = LocalStitchTokens.current.colors
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(2.dp, colors.border)
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource("assets/stitch/avatar-admin.png"),
            contentDescription = tf("%s avatar", t(userProfile.name)),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ScanHistoryContent(
    viewModel: AppViewModel,
    onNavigate: (AppScreen) -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    val colors = LocalStitchTokens.current.colors
    val stats = viewModel.historyStats
    val history = viewModel.filteredHistory()
    val searchQuery = viewModel.historySearchQuery
    val visibleCount = history.size
    val countLabel = if (visibleCount == 0) {
        tf("Showing %d of %d", 0, stats.totalScans)
    } else {
        tf("Showing %d-%d of %d", 1, visibleCount, stats.totalScans)
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
                    Text(t("Scan History"), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                    Box(
                        modifier = Modifier
                            .statusPill(colors.primary.copy(alpha = 0.15f), colors.primary.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text(t("LIVE"), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.primary, letterSpacing = 1.sp)
                    }
                }
                Text(
                    t("Real-time audit logs of all QR code captures, including verdicts from the local heuristic engine."),
                    fontSize = 16.sp,
                    color = colors.textSub,
                    modifier = Modifier.widthIn(max = 640.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialSymbol(name = "calendar_today", size = 16.sp, color = colors.textMuted)
                        Text(t("Oct 24 - Oct 25"), fontSize = 14.sp, color = colors.textSub)
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
                    color = colors.primary
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialSymbol(name = "download", size = 18.sp, color = Color.White)
                        Text(t("Export CSV"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                title = t("Total Scans (24h)"),
                value = stats.totalScans.toString(),
                delta = "0%",
                deltaIcon = "trending_up",
                deltaColor = colors.success,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = t("Threats Blocked"),
                value = stats.maliciousCount.toString(),
                delta = "0%",
                deltaIcon = "trending_down",
                deltaColor = colors.danger,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = t("Suspicious Flags"),
                value = stats.suspiciousCount.toString(),
                delta = "0",
                deltaIcon = "trending_up",
                deltaColor = colors.warning,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = t("Safe Scans"),
                value = stats.safeCount.toString(),
                delta = "0%",
                deltaIcon = "",
                deltaColor = colors.success,
                modifier = Modifier.weight(1f)
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.backgroundAlt)
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
                                .background(colors.surface)
                                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp)
                        ) {
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateHistorySearch(it) },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp, color = colors.textMain),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        MaterialSymbol(name = "search", size = 18.sp, color = colors.textMuted)
                                        Spacer(Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (searchQuery.isBlank()) {
                                                Text(t("Search domains, sources, or hashes..."), fontSize = 13.sp, color = colors.textMuted)
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
                            label = t("All Scans"),
                            active = viewModel.historyFilter == HistoryFilter.All,
                            onClick = { viewModel.updateHistoryFilter(HistoryFilter.All) },
                            color = colors.textMain,
                            background = colors.surface
                        )
                        FilterChip(
                            label = t("Safe"),
                            active = viewModel.historyFilter == HistoryFilter.Safe,
                            color = colors.success,
                            background = colors.success.copy(alpha = 0.1f),
                            onClick = { viewModel.updateHistoryFilter(HistoryFilter.Safe) },
                            showDot = true
                        )
                        FilterChip(
                            label = t("Suspicious"),
                            active = viewModel.historyFilter == HistoryFilter.Suspicious,
                            color = colors.warning,
                            background = colors.warning.copy(alpha = 0.1f),
                            onClick = { viewModel.updateHistoryFilter(HistoryFilter.Suspicious) },
                            showDot = true
                        )
                        FilterChip(
                            label = t("Dangerous"),
                            active = viewModel.historyFilter == HistoryFilter.Dangerous,
                            color = colors.danger,
                            background = colors.danger.copy(alpha = 0.1f),
                            onClick = { viewModel.updateHistoryFilter(HistoryFilter.Dangerous) },
                            showDot = true
                        )
                        Box(modifier = Modifier.size(24.dp).background(colors.border).width(1.dp))
                        FilterChip(
                            label = t("Advanced"),
                            icon = "filter_list",
                            background = colors.surface,
                            border = colors.border,
                            color = colors.textSub,
                            onClick = { viewModel.showInfo(t("Advanced filters are not available yet.")) }
                        )
                    }
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    TableHeaderRow(
                        riskLabel = t("Risk"),
                        domainLabel = t("Domain / Payload"),
                        sourceLabel = t("Source"),
                        timeLabel = t("Time"),
                        verdictLabel = t("Verdict"),
                        actionsLabel = t("Actions")
                    )
                    if (history.isEmpty()) {
                        EmptyHistoryRow(text = t("No scan history yet. Run a scan to populate results."))
                    } else {
                        history.forEach { item ->
                            HistoryRow(
                                item = item,
                                timeLabel = viewModel.formatRelativeTime(item.scannedAt),
                                onClick = { viewModel.selectHistoryItem(it) },
                                language = language
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.backgroundAlt)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(t("Show rows:"), fontSize = 12.sp, color = colors.textMuted)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.borderStrong, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("10", fontSize = 12.sp, color = colors.textSub)
                        }
                        Text(countLabel, fontSize = 12.sp, color = colors.textMuted)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        PaginationButton("chevron_left", disabled = true)
                        PaginationNumber("1", active = true)
                        PaginationNumber("2")
                        PaginationNumber("3")
                        Text("...", fontSize = 12.sp, color = colors.textMuted)
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
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontSize = 14.sp, color = colors.textSub, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
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
    color: Color? = null,
    background: Color? = null,
    border: Color = Color.Transparent,
    icon: String? = null,
    showDot: Boolean = false,
    onClick: () -> Unit
) {
    val tokens = LocalStitchTokens.current
    val finalColor = color ?: if (active) Color.White else tokens.colors.textMain
    val finalBackground = background ?: if (active) tokens.colors.primary else tokens.colors.backgroundAlt

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(finalBackground)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .focusable()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            MaterialSymbol(name = icon, size = 14.sp, color = finalColor)
        }
        if (!active && showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(finalColor)
            )
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = finalColor)
    }
}

@Composable
private fun TableHeaderRow(
    riskLabel: String,
    domainLabel: String,
    sourceLabel: String,
    timeLabel: String,
    verdictLabel: String,
    actionsLabel: String
) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundAlt)
            .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(riskLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.width(60.dp))
        Text(domainLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.weight(1f))
        Text(sourceLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.width(140.dp))
        Text(timeLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.width(120.dp))
        Text(verdictLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.width(120.dp))
        Text(actionsLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun HistoryRow(
    item: ScanHistoryItem,
    timeLabel: String,
    onClick: (ScanHistoryItem) -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    val riskIcon = when (item.verdict) {
        Verdict.SAFE -> "shield"
        Verdict.SUSPICIOUS -> "warning"
        Verdict.MALICIOUS -> "gpp_bad"
        Verdict.UNKNOWN -> "help"
    }
    val riskColor = when (item.verdict) {
        Verdict.SAFE -> colors.success
        Verdict.SUSPICIOUS -> colors.warning
        Verdict.MALICIOUS -> colors.danger
        Verdict.UNKNOWN -> colors.textMuted
    }
    val riskBg = when (item.verdict) {
        Verdict.SAFE -> colors.success.copy(alpha = 0.1f)
        Verdict.SUSPICIOUS -> colors.warning.copy(alpha = 0.1f)
        Verdict.MALICIOUS -> colors.danger.copy(alpha = 0.1f)
        Verdict.UNKNOWN -> colors.backgroundAlt
    }
    val detail = when (item.verdict) {
        Verdict.SAFE -> t("Known Domain")
        Verdict.SUSPICIOUS -> t("Heuristic Anomaly")
        Verdict.MALICIOUS -> t("Phishing Heuristic Match")
        Verdict.UNKNOWN -> t("Unclassified")
    }
    val sourceIcon = when (item.source) {
        ScanSource.CAMERA -> "videocam"
        ScanSource.GALLERY -> "upload_file"
        ScanSource.CLIPBOARD -> "content_paste"
        ScanSource.MANUAL -> "link"
    }
    val sourceLabel = when (item.source) {
        ScanSource.CAMERA -> t("Webcam")
        ScanSource.GALLERY -> t("File Upload")
        ScanSource.CLIPBOARD -> t("Clipboard")
        ScanSource.MANUAL -> t("Manual")
    }
    val verdictLabel = when (item.verdict) {
        Verdict.SAFE -> t("ALLOWED")
        Verdict.SUSPICIOUS -> t("FLAGGED")
        Verdict.MALICIOUS -> t("BLOCKED")
        Verdict.UNKNOWN -> t("UNKNOWN")
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
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, fontSize = 12.sp, color = colors.textMuted)
        }
        Row(modifier = Modifier.width(140.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MaterialSymbol(name = sourceIcon, size = 14.sp, color = colors.textMuted)
            Text(sourceLabel, fontSize = 12.sp, color = colors.textSub)
        }
        Text(timeLabel, fontSize = 12.sp, color = colors.textMuted, modifier = Modifier.width(120.dp))
        Row(
            modifier = Modifier
                .width(120.dp)
                .statusPill(verdictBg, verdictColor.copy(alpha = 0.2f))
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
            MaterialSymbol(name = "more_horiz", size = 18.sp, color = colors.textMuted)
        }
    }
}

@Composable
private fun EmptyHistoryRow(text: String) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 12.sp, color = colors.textMuted)
    }
}

@Composable
private fun PaginationButton(icon: String, disabled: Boolean = false) {
    val colors = LocalStitchTokens.current.colors
    Box(
        modifier = Modifier
            .size(32.dp)
            .surfaceBorder(colors.border, radius = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        MaterialSymbol(name = icon, size = 16.sp, color = if (disabled) colors.textMuted.copy(alpha = 0.5f) else colors.textMuted)
    }
}

@Composable
private fun PaginationNumber(number: String, active: Boolean = false) {
    val colors = LocalStitchTokens.current.colors
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) colors.primary else Color.Transparent)
            .border(1.dp, if (active) Color.Transparent else colors.border, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(number, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else colors.textSub)
    }
}
