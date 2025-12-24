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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.SampleData
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.theme.LocalStitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialIconRound
import com.qrshield.desktop.ui.statusPill

@Composable
fun ResultDangerousAltScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanResultDangerous(isDark = viewModel.isDarkMode)
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.colors.background)
        ) {
            AppSidebar(
                currentScreen = AppScreen.ResultDangerousAlt,
                onNavigate = { viewModel.currentScreen = it },
                language = viewModel.appLanguage,
                onProfileClick = { viewModel.currentScreen = AppScreen.TrustCentreAlt }
            )
            DangerousAltContent(
                viewModel = viewModel,
                isDark = viewModel.isDarkMode,
                onNavigate = { viewModel.currentScreen = it }
            )
        }
    }
}

@Composable
private fun DangerousAltContent(viewModel: AppViewModel, isDark: Boolean, onNavigate: (AppScreen) -> Unit) {
    val language = viewModel.appLanguage
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    val assessment = viewModel.currentAssessment
    val url = viewModel.currentUrl
    val confidencePercent = ((assessment?.confidence ?: 0f) * 100).coerceIn(0f, 100f)
    val scanId = viewModel.lastAnalyzedAt?.toString()?.takeLast(6) ?: t("LATEST")
    val colors = LocalStitchTokens.current.colors
    val scanTimeLabel = viewModel.lastAnalyzedAt?.let { viewModel.formatTimestamp(it) } ?: t("Unknown")
    val sourceLabel = url?.substringAfter("://")?.substringBefore("/")?.ifBlank { t("Unknown") } ?: t("Unknown")
    val userProfile = SampleData.userProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t("Home"), fontSize = 12.sp, color = colors.textSub)
                Text("/", fontSize = 12.sp, color = colors.textSub)
                Text(t("Scans"), fontSize = 12.sp, color = colors.textSub)
                Text("/", fontSize = 12.sp, color = colors.textSub)
                Text(tf("Scan #SCAN-%s", scanId), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .statusPill(colors.surface, colors.border)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.success))
                        Text(t("Engine Active (Offline)"), fontSize = 11.sp, color = colors.textSub)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.toggleNotificationPanel() }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "notifications", size = 18.sp, color = colors.textSub)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(userProfile.initials, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }

        if (assessment == null || url.isNullOrBlank()) {
            EmptyAltResultState(onNavigate = onNavigate, language = language)
            return@Column
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.danger.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            MaterialIconRound(name = "gpp_bad", size = 28.sp, color = colors.danger)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(t("HIGH RISK DETECTED"), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(colors.danger)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(t("DANGEROUS"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Text(t(assessment.actionRecommendation), fontSize = 12.sp, color = colors.textSub)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(t("Threat Confidence"), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, letterSpacing = 1.sp)
                        Text("${confidencePercent.toInt()}%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.danger)
                    }
                }
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(t("Phishing Attempt"), colors.danger.copy(alpha = 0.1f), colors.danger, "bug_report")
                    Badge(t("Obfuscated Script"), colors.warning.copy(alpha = 0.1f), colors.warning, "code_off")
                    Badge(t("Homograph Attack"), colors.primary.copy(alpha = 0.1f), colors.primary, "link")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(t("Attack Breakdown"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.2f))) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialIconRound(name = "abc", size = 18.sp, color = colors.primary)
                                }
                                Column {
                                    Text(t("Homograph / IDN Attack"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                    Text(t("Cyrillic characters mimicking Latin alphabet detected."), fontSize = 12.sp, color = colors.textSub)
                                }
                            }
                            MaterialIconRound(name = "expand_less", size = 18.sp, color = colors.primary)
                        }
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(t("Visual Appearance"), fontSize = 10.sp, color = colors.textSub)
                                    Text("secure-banking.com", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(t("Actual Punycode"), fontSize = 10.sp, color = colors.textSub)
                                    Text("xn--secure-bankng-87b.com", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.danger)
                                }
                            }
                            Text(t("The domain uses the Cyrillic 'a' (U+0430) instead of Latin 'a' (U+0061). This technique is commonly used to trick users into believing they are visiting a legitimate service."), fontSize = 12.sp, color = colors.textSub, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
                ExpandableRow(t("Suspicious Redirect Chain"), t("3 hops detected involving known URL shorteners."), "call_split")
                ExpandableRow(t("Obfuscated JavaScript"), t("High entropy string detected in URL parameters."), "javascript")
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialIconRound(name = "thumb_up", size = 12.sp, color = colors.primary)
                            }
                            Text(t("Recommended Actions"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                        }
                        Button(
                            onClick = {
                                viewModel.addBlocklistDomain(viewModel.hostFromUrl(url) ?: url)
                                viewModel.shareTextReport()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            MaterialIconRound(name = "block", size = 16.sp, color = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text(t("Block & Report"), fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { viewModel.showInfo(t("Sandbox quarantine is not available on desktop yet.")) },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                            border = BorderStroke(1.dp, colors.border),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            MaterialIconRound(name = "science", size = 16.sp, color = colors.textSub)
                            Spacer(Modifier.width(6.dp))
                            Text(t("Quarantine in Sandbox"), color = colors.textMain)
                        }
                        Text(t("Explainable Security"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
                        Bullet(t("Domain age is less than 24 hours."))
                        Bullet(t("Matched 3 signatures in local phishing DB."))
                        Bullet(t("Target IP is located in a high-risk ASN."))
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(t("Scan Meta"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 1.sp)
                        MetaRow(t("Scan Time"), scanTimeLabel)
                        MetaRow(t("Source"), sourceLabel)
                        MetaRow(t("Engine"), t("Offline Core v2.4"))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAltResultState(onNavigate: (AppScreen) -> Unit, language: AppLanguage) {
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
private fun Badge(text: String, bg: Color, color: Color, icon: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, bg.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MaterialIconRound(name = icon, size = 14.sp, color = color)
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun ExpandableRow(title: String, subtitle: String, icon: String) {
    val colors = LocalStitchTokens.current.colors
    Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border.copy(alpha = 0.4f))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.warning.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = icon, size = 18.sp, color = colors.warning)
                }
                Column {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                    Text(subtitle, fontSize = 12.sp, color = colors.textSub)
                }
            }
            MaterialIconRound(name = "expand_more", size = 18.sp, color = colors.textSub)
        }
    }
}

@Composable
private fun Bullet(text: String) {
    val colors = LocalStitchTokens.current.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MaterialIconRound(name = "check_circle", size = 16.sp, color = colors.danger)
        Text(text, fontSize = 12.sp, color = colors.textSub)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    val colors = LocalStitchTokens.current.colors
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = colors.textSub)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
    }
}
