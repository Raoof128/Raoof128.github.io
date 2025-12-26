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
import com.qrshield.desktop.theme.LocalStitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialIconRound
import com.qrshield.desktop.ui.dottedPattern
import com.qrshield.desktop.ui.statusPill
import com.qrshield.desktop.ui.toggleTrack
import com.qrshield.desktop.ui.handCursor
import com.qrshield.desktop.ui.ProfileDropdown

@Composable
fun TrustCentreAltScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.trustCentreAlt(isDark = viewModel.isDarkMode)
    val language = viewModel.appLanguage
    StitchTheme(tokens = tokens) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tokens.colors.background)
            ) {
                AppSidebar(
                    currentScreen = AppScreen.TrustCentreAlt,
                    onNavigate = { viewModel.currentScreen = it },
                    language = viewModel.appLanguage,
                    onProfileClick = { /* Already on settings */ }
                )
                TrustCentreAltContent(viewModel = viewModel)
            }
            
            // Profile Dropdown Popup (same as DashboardScreen)
            ProfileDropdown(
                isVisible = viewModel.showProfileDropdown,
                onDismiss = { viewModel.dismissProfileDropdown() },
                userName = com.qrshield.desktop.SampleData.userProfile.name,
                userRole = com.qrshield.desktop.SampleData.userProfile.role,
                userInitials = com.qrshield.desktop.SampleData.userProfile.initials,
                historyStats = viewModel.historyStats,
                onViewProfile = { viewModel.currentScreen = AppScreen.TrustCentreAlt },
                onOpenSettings = { viewModel.currentScreen = AppScreen.TrustCentreAlt },
                language = language
            )
        }
    }
}

@Composable
private fun TrustCentreAltContent(viewModel: AppViewModel) {
    val language = viewModel.appLanguage
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val tokens = LocalStitchTokens.current
    val colors = tokens.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(t("Settings"), fontSize = 14.sp, color = colors.textSub)
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = colors.textMuted, modifier = Modifier.padding(horizontal = 8.dp))
                Text(t("Onboarding"), fontSize = 14.sp, color = colors.textSub)
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = colors.textMuted, modifier = Modifier.padding(horizontal = 8.dp))
                Text(t("Offline Privacy"), fontSize = 14.sp, color = colors.textMain, fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.border)
                        .clickable { viewModel.showInfo(t("Keyboard shortcuts: Cmd/Ctrl+V paste, Cmd/Ctrl+1-4 navigate, Escape go back")) }
                        .focusable()
                        .handCursor(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "help_outline", size = 18.sp, color = colors.textSub)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.border)
                        .clickable { viewModel.toggleProfileDropdown() }
                        .focusable()
                        .handCursor(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "person", size = 18.sp, color = colors.textSub)
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
                        .background(colors.surface)
                        .border(1.dp, colors.border),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "security_update_good", size = 32.sp, color = colors.primary)
                }
                Text(
                    text = t("Analysed offline.\nYour data stays on-device."),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = colors.textMain,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = t("QR-SHIELD processes every scan within a secure, isolated local sandbox. We prioritize explainable security with zero cloud telemetry for image analysis."),
                    fontSize = 16.sp,
                    color = colors.textSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp).widthIn(max = 640.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard(icon = "science", title = t("Local Sandbox"), body = t("Code execution happens in an ephemeral container. Malicious payloads never touch an OS kernel."), modifier = Modifier.weight(1f))
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MaterialIconRound(name = "verified", size = 18.sp, color = colors.primary)
                            Text(t("DATA LIFECYCLE VERIFICATION"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMain, letterSpacing = 0.8.sp)
                        }
                        Box(
                            modifier = Modifier
                                .statusPill(colors.success.copy(alpha = 0.1f), colors.success.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(t("SECURITY AUDIT: PASS"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.success)
                        }
                    }
                    Column {
                        DataRow(t("Raw Image Buffer"), t("Local Memory (RAM)"), t("None"))
                        DataRow(t("Decoded URL/Payload"), t("Isolated Sandbox"), t("None"))
                        DataRow(t("Threat Verdict"), t("Local Database"), t("None"))
                    }
                }
            }

            // Security Settings Section (matches Web onboarding.html)
            SecuritySettingsSection(viewModel = viewModel, language = language)
        }
    }
}

@Composable
private fun SecuritySettingsSection(viewModel: AppViewModel, language: AppLanguage) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MaterialIconRound(name = "tune", size = 20.sp, color = colors.primary)
                Text(t("Security Settings"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
            }
            
            // Detection Settings Group
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(t("Detection"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 0.8.sp)
                
                SettingRow(
                    label = t("Auto-Block Threats"),
                    description = t("Automatically block high-risk URLs"),
                    checked = viewModel.autoBlockThreats,
                    onCheckedChange = { viewModel.autoBlockThreats = it }
                )
                SettingRow(
                    label = t("Real-Time Scanning"),
                    description = t("Scan QR codes as they're detected"),
                    checked = viewModel.realTimeScanning,
                    onCheckedChange = { viewModel.realTimeScanning = it }
                )
            }
            
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.3f)))
            
            // Notifications Group
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(t("Notifications"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 0.8.sp)
                
                SettingRow(
                    label = t("Sound Alerts"),
                    description = t("Play sound when threat detected"),
                    checked = viewModel.soundAlerts,
                    onCheckedChange = { viewModel.soundAlerts = it }
                )
                SettingRow(
                    label = t("Threat Alerts"),
                    description = t("Show visual alerts for threats"),
                    checked = viewModel.threatAlerts,
                    onCheckedChange = { viewModel.threatAlerts = it }
                )
            }
            
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.3f)))
            
            // Display Group
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(t("Display"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 0.8.sp)
                
                SettingRow(
                    label = t("Show Confidence Score"),
                    description = t("Display threat probability percentage"),
                    checked = viewModel.showConfidenceScore,
                    onCheckedChange = { viewModel.showConfidenceScore = it }
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
            Text(description, fontSize = 12.sp, color = colors.textSub)
        }
        // Simple toggle switch
        Box(
            modifier = Modifier
                .toggleTrack(checked, colors.primary, colors.border, width = 44.dp)
                .clickable { onCheckedChange(!checked) }
                .focusable()
                .handCursor(),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

@Composable
private fun InfoCard(icon: String, title: String, body: String, modifier: Modifier = Modifier) {
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primary.copy(alpha = 0.15f)), // Adjusted for visibility
                contentAlignment = Alignment.Center
            ) {
                MaterialIconRound(name = icon, size = 24.sp, color = colors.primary)
            }
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
            Text(body, fontSize = 13.sp, color = colors.textSub, lineHeight = 18.sp)
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
    val colors = LocalStitchTokens.current.colors
    val languages = listOf(
        AppLanguage.English,
        AppLanguage.German,
        AppLanguage.Spanish,
        AppLanguage.French,
        AppLanguage.Italian,
        AppLanguage.Portuguese,
        AppLanguage.Russian,
        AppLanguage.ChineseSimplified,
        AppLanguage.Japanese,
        AppLanguage.Korean,
        AppLanguage.Hindi,
        AppLanguage.Arabic,
        AppLanguage.Turkish,
        AppLanguage.Vietnamese,
        AppLanguage.Indonesian,
        AppLanguage.Thai
    )
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(t("Language"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
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
                color = colors.textSub
            )
        }
    }
}

@Composable
private fun LanguageChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalStitchTokens.current.colors
    val background = if (selected) colors.primary.copy(alpha = 0.1f) else Color.Transparent
    val border = if (selected) colors.primary.copy(alpha = 0.3f) else colors.border.copy(alpha = 0.5f)
    val textColor = if (selected) colors.primary else colors.textSub

    Row(
        modifier = Modifier
            .statusPill(background, border)
            .clickable { onClick() }
            .focusable()
            .handCursor()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun DataRow(label: String, env: String, noneLabel: String) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border.copy(alpha = 0.2f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textMain, modifier = Modifier.weight(1f))
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.success))
            Text(env, fontSize = 13.sp, color = colors.textSub)
        }
        Text(noneLabel, fontSize = 12.sp, color = colors.textSub, modifier = Modifier.weight(1f))
    }
}
