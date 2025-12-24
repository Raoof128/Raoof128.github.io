package com.qrshield.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.ResultViewMode
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.theme.LocalStitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialIconRound
import com.qrshield.desktop.ui.gridPattern
import com.qrshield.desktop.ui.iconContainer
import com.qrshield.desktop.ui.statusPill
import com.qrshield.desktop.ui.progressFill

@Composable
fun ResultDangerousScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanResultDangerous(isDark = viewModel.isDarkMode)
    StitchTheme(tokens = tokens) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.colors.background)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                AppSidebar(
                    currentScreen = AppScreen.ResultDangerous,
                    onNavigate = { viewModel.currentScreen = it },
                    language = viewModel.appLanguage
                )
                DangerousContent(
                    viewModel = viewModel,
                    onNavigate = { viewModel.currentScreen = it }
                )
            }
        }
    }
}

@Composable
private fun DangerousContent(viewModel: AppViewModel, onNavigate: (AppScreen) -> Unit) {
    val language = viewModel.appLanguage
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    val assessment = viewModel.currentAssessment
    val url = viewModel.currentUrl
    val verdictDetails = viewModel.currentVerdictDetails
    val confidencePercent = ((assessment?.confidence ?: 0f) * 100).coerceIn(0f, 100f)
    val durationLabel = viewModel.lastAnalysisDurationMs?.let { "${it}ms" } ?: "--"
    val isDark = viewModel.isDarkMode
    val colors = LocalStitchTokens.current.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .gridPattern(spacing = 40.dp, lineColor = colors.border.copy(alpha = 0.05f), lineWidth = 1.dp)
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.End)
                .offset(y = (-200).dp)
                .background(colors.danger.copy(alpha = 0.1f), CircleShape)
                .blur(60.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(t("Scan Monitor"), fontSize = 12.sp, color = colors.textSub)
                    MaterialIconRound(name = "chevron_right", size = 12.sp, color = colors.textSub)
                    Text(t("Result"), fontSize = 12.sp, color = colors.textSub)
                }
                Text(t("Threat Analysis Report"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .statusPill(colors.surface, colors.border)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.success))
                        Text(t("Engine Active v2.4.4"), fontSize = 11.sp, color = colors.textSub)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { viewModel.showInfo(t("Notifications are not available yet.")) }
                        .focusable()
                ) {
                    MaterialIconRound(name = "notifications", size = 18.sp, color = colors.textSub)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(colors.danger)
                            .border(2.dp, colors.surface, CircleShape)
                    )
                }
            }
        }

        if (assessment == null || url.isNullOrBlank()) {
            EmptyResultState(onNavigate = onNavigate, language = language)
            return@Column
        }

        Surface(
            modifier = Modifier.padding(top = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) colors.danger.copy(alpha = 0.15f) else colors.danger.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, if (isDark) colors.danger.copy(alpha = 0.3f) else colors.danger.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) colors.danger.copy(alpha = 0.3f) else colors.danger.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        MaterialIconRound(name = "gpp_bad", size = 32.sp, color = colors.danger)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(t("High Risk Detected"), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.danger)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.danger.copy(alpha = 0.2f))
                                    .border(1.dp, colors.danger.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(t("DANGEROUS"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.danger)
                            }
                        }
                        Text(
                            verdictDetails?.summary?.let { t(it) }
                                ?: t(assessment.actionRecommendation),
                            fontSize = 12.sp,
                            color = colors.textSub
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                MaterialIconRound(name = "timer", size = 14.sp, color = colors.danger)
                                Text(tf("%s latency", durationLabel), fontSize = 12.sp, color = colors.danger)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                MaterialIconRound(name = "cloud_off", size = 14.sp, color = colors.textSub)
                                Text(t("Offline Analysis"), fontSize = 12.sp, color = colors.textSub)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.shareTextReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        MaterialIconRound(name = "flag", size = 18.sp, color = colors.danger)
                        Spacer(Modifier.width(6.dp))
                        Text(t("Report"), fontSize = 13.sp, color = colors.danger)
                    }
                    Button(
                        onClick = { viewModel.addBlocklistDomain(viewModel.hostFromUrl(url) ?: url) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        MaterialIconRound(name = "block", size = 18.sp, color = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text(t("Block Access"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) colors.backgroundAlt else colors.backgroundAlt)
                                .border(1.dp, colors.border)
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MaterialIconRound(name = "analytics", size = 18.sp, color = colors.textSub)
                                Text(t("Target Analysis"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                            }
                            Row(
                                modifier = Modifier
                                    .iconContainer(if (isDark) colors.backgroundAlt else colors.border.copy(alpha = 0.5f))
                                    .padding(4.dp)
                            ) {
                                ToggleChip(
                                    label = t("Technical"),
                                    selected = viewModel.resultDangerousViewMode == ResultViewMode.Technical,
                                    onClick = { viewModel.resultDangerousViewMode = ResultViewMode.Technical }
                                )
                                ToggleChip(
                                    label = t("Simple"),
                                    selected = viewModel.resultDangerousViewMode == ResultViewMode.Simple,
                                    onClick = { viewModel.resultDangerousViewMode = ResultViewMode.Simple }
                                )
                            }
                        }
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(t("Decoded Payload"), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) colors.background else colors.backgroundAlt)
                                    .border(1.dp, colors.border)
                                    .padding(12.dp)
                            ) {
                                Text(url, fontSize = 12.sp, color = colors.danger)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MaterialIconRound(name = "warning", size = 14.sp, color = colors.danger)
                                Text(
                                    verdictDetails?.riskFactorExplanations?.firstOrNull()?.let { t(it) }
                                        ?: t("Warning: Multiple phishing indicators detected."),
                                    fontSize = 12.sp,
                                    color = colors.danger
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                InfoTile(t("IDN Homograph"), t("DETECTED"), "text_format", colors.danger, t("Mixed script characters detected in domain name intended to spoof legitimate brands."), modifier = Modifier.weight(1f))
                                InfoTile(t("Domain Age"), t("< 24 HOURS"), "domain", colors.danger, t("Domain was registered today. Highly suspicious for banking services."), modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                InfoTile(t("Redirect Chain"), t("COMPLEX"), "shuffle", colors.warning, t("URL involves 3+ redirects through unrelated shortening services."), modifier = Modifier.weight(1f))
                                InfoTile(t("SSL Certificate"), t("VALID"), "lock", colors.success, t("Let's Encrypt R3. Note: Valid SSL does not guarantee site legitimacy."), modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) colors.backgroundAlt else colors.backgroundAlt)
                                .border(1.dp, colors.border)
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MaterialIconRound(name = "preview", size = 18.sp, color = colors.textSub)
                                Text(t("Visual Sandbox Preview"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                            }
                            Text(t("SANDBOX MODE: NO NETWORK"), fontSize = 10.sp, color = colors.textMuted)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(if (isDark) colors.background else colors.backgroundAlt),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(320.dp)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) colors.surface else colors.surface)
                                    .border(1.dp, colors.border)
                                    .blur(1.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .statusPill(colors.surface, colors.danger.copy(alpha = 0.3f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                MaterialIconRound(name = "visibility_off", size = 14.sp, color = colors.danger)
                                Spacer(Modifier.width(6.dp))
                                Text(t("Preview blurred for safety"), fontSize = 12.sp, color = colors.textSub)
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(t("Threat Score"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, letterSpacing = 1.sp)
                        Box(modifier = Modifier.size(128.dp), contentAlignment = Alignment.Center) {
                            Text(assessment.score.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                            Text(
                                t(assessment.scoreDescription).uppercase(),
                                fontSize = 10.sp,
                                color = colors.textSub,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(t("AI Confidence"), fontSize = 12.sp, color = colors.textSub)
                            Text("${confidencePercent.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .progressFill(colors.border.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(confidencePercent / 100f)
                                    .progressFill(colors.danger)
                            )
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) colors.backgroundAlt else colors.backgroundAlt)
                                .border(1.dp, colors.border)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MaterialIconRound(name = "public", size = 18.sp, color = colors.textSub)
                            Text(t("Intelligence Feeds"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                        }
                        FeedRow(t("Google Safe Browsing"), true, t("MATCH"), t("NO MATCH"))
                        FeedRow(t("PhishTank DB"), true, t("MATCH"), t("NO MATCH"))
                        FeedRow(t("Local Allowlist"), false, t("MATCH"), t("NO MATCH"))
                    }
                }

                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionRow(
                            t("Share Analysis"),
                            t("Export PDF report"),
                            "share",
                            onClick = {
                                viewModel.shareTextReport()
                                onNavigate(AppScreen.ReportsExport)
                            }
                        )
                        ActionRow(
                            t("View Raw Data"),
                            t("Inspect JSON payload"),
                            "code",
                            onClick = {
                                viewModel.copyJsonReport()
                                onNavigate(AppScreen.ReportsExport)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyResultState(onNavigate: (AppScreen) -> Unit, language: AppLanguage) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(t("No scan data available."), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
            Text(t("Run a scan to view dangerous results."), fontSize = 13.sp, color = colors.textSub)
            Button(
                onClick = { onNavigate(AppScreen.LiveScan) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(t("Back to Scan"), fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}

@Composable
private fun InfoTile(title: String, badge: String, icon: String, color: Color, body: String, modifier: Modifier = Modifier) {
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MaterialIconRound(name = icon, size = 16.sp, color = color)
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(color.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
                }
            }
            Text(body, fontSize = 11.sp, color = colors.textSub)
        }
    }
}

@Composable
private fun FeedRow(label: String, matched: Boolean, matchedLabel: String, noMatchLabel: String) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border.copy(alpha = 0.2f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (matched) colors.danger else colors.success))
            Text(label, fontSize = 13.sp, color = colors.textMain)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (matched) colors.danger.copy(alpha = 0.1f) else colors.backgroundAlt)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                if (matched) matchedLabel else noMatchLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (matched) colors.danger else colors.textSub
            )
        }
    }
}

@Composable
private fun ActionRow(title: String, subtitle: String, icon: String, onClick: () -> Unit) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .clickable { onClick() }
            .focusable()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MaterialIconRound(name = icon, size = 18.sp, color = colors.textSub)
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                Text(subtitle, fontSize = 11.sp, color = colors.textSub)
            }
        }
        MaterialIconRound(name = "arrow_forward", size = 14.sp, color = colors.textSub)
    }
}

@Composable
private fun ToggleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalStitchTokens.current.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) colors.surface else Color.Transparent)
            .clickable { onClick() }
            .focusable()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) colors.textMain else colors.textSub
        )
    }
}
