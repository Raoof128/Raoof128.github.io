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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun TrustCentreAltScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.trustCentreAlt(isDark = viewModel.isDarkMode)
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.colors.background)
        ) {
            AppSidebar(
                currentScreen = AppScreen.TrustCentreAlt,
                onNavigate = { viewModel.currentScreen = it },
                language = viewModel.appLanguage
            )
            TrustCentreAltContent(viewModel = viewModel)
        }
    }
}

@Composable
private fun TrustCentreAltContent(viewModel: AppViewModel) {
    val language = viewModel.appLanguage
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val isDark = viewModel.isDarkMode
    val background = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val textMain = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textSub = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f))
                .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(t("Settings"), fontSize = 14.sp, color = textSub)
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 8.dp))
                Text(t("Onboarding"), fontSize = 14.sp, color = textSub)
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 8.dp))
                Text(t("Offline Privacy"), fontSize = 14.sp, color = textMain, fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                        .clickable { viewModel.showInfo(t("Help is not available yet.")) }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "help_outline", size = 18.sp, color = textSub)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                        .clickable { viewModel.showInfo(t("Profile settings are not available yet.")) }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "person", size = 18.sp, color = textSub)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 48.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF1E293B) else Color.White)
                        .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "security_update_good", size = 32.sp, color = Color(0xFF2563EB))
                }
                Text(
                    text = t("Analysed offline.\nYour data stays on-device."),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = textMain,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = t("QR-SHIELD processes every scan within a secure, isolated local sandbox. We prioritize explainable security with zero cloud telemetry for image analysis."),
                    fontSize = 16.sp,
                    color = textSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp).widthIn(max = 640.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard(icon = "science", title = t("Local Sandbox"), body = t("Code execution happens in an ephemeral container. Malicious payloads never touch your OS kernel."), modifier = Modifier.weight(1f))
                InfoCard(icon = "cloud_off", title = t("No Cloud Logs"), body = t("We strictly disable outgoing telemetry for scans. Scan results and image hashes remain local."), modifier = Modifier.weight(1f))
                InfoCard(icon = "storage", title = t("On-Device DB"), body = t("The entire threat signature database is downloaded to your device for millisecond lookups."), modifier = Modifier.weight(1f))
            }

            LanguageSection(
                currentLanguage = viewModel.appLanguage,
                onSelectLanguage = { viewModel.setLanguage(it) },
                language = language
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF1E293B) else Color.White,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MaterialIconRound(name = "verified", size = 18.sp, color = Color(0xFF2563EB))
                            Text(t("DATA LIFECYCLE VERIFICATION"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textMain, letterSpacing = 0.8.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(t("SECURITY AUDIT: PASS"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                    Column {
                        DataRow(t("Raw Image Buffer"), t("Local Memory (RAM)"), t("None"))
                        DataRow(t("Decoded URL/Payload"), t("Isolated Sandbox"), t("None"))
                        DataRow(t("Threat Verdict"), t("Local Database"), t("None"))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(icon: String, title: String, body: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
            ) {
                MaterialIconRound(name = icon, size = 24.sp, color = Color(0xFF2563EB))
            }
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun LanguageSection(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val languages = listOf(
        AppLanguage.English,
        AppLanguage.German,
        AppLanguage.Spanish,
        AppLanguage.French,
        AppLanguage.ChineseSimplified,
        AppLanguage.Japanese,
        AppLanguage.Hindi
    )
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(t("Language"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                languages.chunked(4).forEach { rowLanguages ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowLanguages.forEach { language ->
                            LanguageChip(
                                label = language.displayName,
                                selected = language == currentLanguage,
                                onClick = { onSelectLanguage(language) }
                            )
                        }
                    }
                }
            }
            Text(
                t("Changes apply immediately to navigation labels."),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LanguageChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
    val border = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .clickable { onClick() }
            .focusable()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun DataRow(label: String, env: String, noneLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
            Text(env, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(noneLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
    }
}
