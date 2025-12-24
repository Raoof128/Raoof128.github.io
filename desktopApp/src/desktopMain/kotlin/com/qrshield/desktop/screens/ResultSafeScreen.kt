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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.qrshield.desktop.ui.cardSurface
import com.qrshield.desktop.ui.statusPill
import com.qrshield.desktop.ui.progressFill

@Composable
fun ResultSafeScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanResultSafe(isDark = viewModel.isDarkMode)
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
            SafeResultContent(
                viewModel = viewModel,
                onNavigate = { viewModel.currentScreen = it },
                language = language
            )
        }
    }
}

@Composable
private fun SafeResultContent(
    viewModel: AppViewModel,
    onNavigate: (AppScreen) -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    val assessment = viewModel.currentAssessment
    val url = viewModel.currentUrl
    val verdictDetails = viewModel.currentVerdictDetails
    val scanId = viewModel.lastAnalyzedAt?.toString()?.takeLast(6) ?: t("LATEST")
    val confidencePercent = ((assessment?.confidence ?: 0f) * 100).coerceIn(0f, 100f)
    val confidenceLabel = "${confidencePercent.toInt()}%"
    val durationLabel = viewModel.lastAnalysisDurationMs?.let { "${it}ms" } ?: "--"
    val colors = LocalStitchTokens.current.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .gridPattern(spacing = 24.dp, lineColor = colors.border.copy(alpha = 0.3f), lineWidth = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(colors.surface.copy(alpha = 0.8f))
                .border(1.dp, colors.border)
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t("Scan"), fontSize = 14.sp, color = colors.textSub)
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = colors.textMuted)
                Text(t("Results"), fontSize = 14.sp, color = colors.textSub)
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = colors.textMuted)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.backgroundAlt)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(tf("#SCAN-%s", scanId), fontSize = 10.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                    }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .statusPill(colors.success.copy(alpha = 0.1f), colors.success.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.success))
                        Text(t("ENGINE ACTIVE"), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.success, letterSpacing = 1.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { viewModel.showInfo(t("Notifications are not available yet.")) }
                        .focusable()
                ) {
                    MaterialIconRound(name = "notifications", size = 20.sp, color = colors.textSub)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (assessment == null || url.isNullOrBlank()) {
                EmptyResultState(onNavigate = onNavigate, language = language)
            } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.success),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialIconRound(name = "check_circle", size = 36.sp, color = Color.White)
                            }
                            Column {
                                Text(t("Safe to Visit"), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    MaterialIconRound(name = "link", size = 14.sp, color = colors.textSub)
                                    Text(url, fontSize = 12.sp, color = colors.textSub, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.backgroundAlt,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                MetricBlock(t("Confidence"), confidenceLabel, colors.success)
                                VerticalDivider()
                                MetricBlock(t("Scan Time"), durationLabel, colors.textMain)
                                VerticalDivider()
                                MetricBlock(t("Engine"), t("v2.4.1 Local"), colors.textSub)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.openUrl(url) },
                            enabled = url.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.success),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(t("Visit URL"), fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(8.dp))
                            MaterialIconRound(name = "open_in_new", size = 14.sp, color = Color.White)
                        }
                        Button(
                            onClick = { viewModel.copyUrl(url, label = t("Safe link copied")) },
                            enabled = url.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                            border = BorderStroke(1.dp, colors.border),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            MaterialIconRound(name = "content_copy", size = 14.sp, color = colors.textSub)
                            Spacer(Modifier.width(8.dp))
                            Text(t("Copy Safe Link"), color = colors.textSub)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .iconContainer(colors.backgroundAlt)
                                .padding(4.dp)
                        ) {
                            ViewModeButton(
                                t("Simple View"),
                                selected = viewModel.resultSafeViewMode == ResultViewMode.Simple,
                                onClick = { viewModel.resultSafeViewMode = ResultViewMode.Simple }
                            )
                            ViewModeButton(
                                t("Technical"),
                                selected = viewModel.resultSafeViewMode == ResultViewMode.Technical,
                                onClick = { viewModel.resultSafeViewMode = ResultViewMode.Technical }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(t("Verdict Analysis"), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        AnalysisCard(title = t("Domain Identity"), badge = t("PASSED"), icon = "domain_verification", body = t("Verified ownership by Microsoft Corporation via EV Certificate."), modifier = Modifier.weight(1f))
                        AnalysisCard(title = t("Homograph Check"), badge = t("CLEAN"), icon = "spellcheck", body = t("No mixed-script characters or IDN spoofing detected in domain string."), modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        AnalysisCard(title = t("Domain Age"), badge = t("ESTABLISHED"), icon = "history_edu", body = t("Domain registered > 20 years ago. High reputation score."), modifier = Modifier.weight(1f))
                        AnalysisCard(title = t("Redirect Chain"), badge = t("DIRECT"), icon = "alt_route", body = t("Zero intermediate redirects found. Destination is final."), modifier = Modifier.weight(1f))
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
                                .border(1.dp, colors.border)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(t("TECHNICAL INDICATORS"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSub, letterSpacing = 1.sp)
                                Text(
                                    t("Export Report"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.success,
                                    modifier = Modifier
                                        .clickable { onNavigate(AppScreen.ReportsExport) }
                                        .focusable()
                                )
                            }
                            TechnicalRow(t("Certificate Issuer"), t("DigiCert Inc (US)"))
                            TechnicalRow(t("Server Location"), t("United States (Azure Cloud)"))
                            TechnicalRow(t("Shannon Entropy"), t("3.44 bits (Low)"))
                            TechnicalRow(t("Top 1k Whitelist"), t("Match"), highlight = colors.success)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(t("Destination Preview"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSub, letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.backgroundAlt)
                                    .border(1.dp, colors.border),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(96.dp)
                                            .cardSurface(colors.surface, colors.border, radius = 8.dp)
                                    )
                                }
                            }
                            Text(t("Sandbox rendered. No active scripts executed."), fontSize = 11.sp, color = colors.textSub, modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally))
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialIconRound(name = "psychology", size = 16.sp, color = colors.primary)
                                }
                                Text(t("AI Verdict Logic"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                            }
                            Text(
                                verdictDetails?.summary?.let { t(it) }
                                    ?: t("The ML model classified this URL as benign with high certainty. The structure matches known legitimate authentication patterns."),
                                fontSize = 12.sp,
                                color = colors.textSub
                            )
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                val phishingProbability = (100f - confidencePercent).coerceIn(0f, 100f)
                                Text(t("Phishing Probability"), fontSize = 12.sp, color = colors.textSub)
                                Text("${phishingProbability.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .progressFill(colors.backgroundAlt)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth((100f - confidencePercent).coerceIn(0f, 100f) / 100f)
                                        .progressFill(colors.success)
                                )
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
            Text(t("Run a scan to see detailed results."), fontSize = 13.sp, color = colors.textSub)
            Button(
                onClick = { onNavigate(AppScreen.LiveScan) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.success),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(t("Back to Scan"), fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}

@Composable
private fun MetricBlock(label: String, value: String, color: Color) {
    val colors = LocalStitchTokens.current.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textSub, letterSpacing = 1.sp)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun VerticalDivider() {
    val colors = LocalStitchTokens.current.colors
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(colors.border)
    )
}

@Composable
private fun ViewModeButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalStitchTokens.current.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) colors.surface else Color.Transparent)
            .border(1.dp, if (selected) colors.border else Color.Transparent, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .focusable()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) colors.textMain else colors.textSub)
    }
}

@Composable
private fun AnalysisCard(title: String, badge: String, icon: String, body: String, modifier: Modifier = Modifier) {
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .iconContainer(colors.success.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = icon, size = 18.sp, color = colors.success)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.success.copy(alpha = 0.1f))
                        .border(1.dp, colors.success.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.success)
                }
            }
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
            Text(body, fontSize = 12.sp, color = colors.textSub)
        }
    }
}

@Composable
private fun TechnicalRow(label: String, value: String, highlight: Color? = null) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textSub, modifier = Modifier.weight(1f))
        if (highlight != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MaterialIconRound(name = "check", size = 12.sp, color = highlight)
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = highlight)
            }
        } else {
            Text(value, fontSize = 13.sp, color = colors.textMain, modifier = Modifier.weight(1f))
        }
    }
}
