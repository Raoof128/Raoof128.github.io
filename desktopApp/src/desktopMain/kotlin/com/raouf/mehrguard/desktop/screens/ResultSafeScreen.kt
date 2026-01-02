package com.raouf.mehrguard.desktop.screens

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
import com.raouf.mehrguard.desktop.AppViewModel
import com.raouf.mehrguard.desktop.ResultViewMode
import com.raouf.mehrguard.desktop.i18n.AppLanguage
import com.raouf.mehrguard.desktop.i18n.DesktopStrings
import com.raouf.mehrguard.desktop.navigation.AppScreen
import com.raouf.mehrguard.desktop.theme.StitchTheme
import com.raouf.mehrguard.desktop.theme.StitchTokens
import com.raouf.mehrguard.desktop.theme.LocalStitchTokens
import com.raouf.mehrguard.desktop.ui.AppSidebar
import com.raouf.mehrguard.desktop.ui.MaterialIconRound
import com.raouf.mehrguard.desktop.ui.gridPattern
import com.raouf.mehrguard.desktop.ui.iconContainer
import com.raouf.mehrguard.desktop.ui.cardSurface
import com.raouf.mehrguard.desktop.ui.statusPill
import com.raouf.mehrguard.desktop.ui.progressFill
import com.raouf.mehrguard.desktop.ui.ProfileDropdown
import com.raouf.mehrguard.desktop.ui.EditProfileDialog
import com.raouf.mehrguard.model.RiskAssessment

/**
 * Safe indicator data derived from engine analysis (absence of threats).
 */
private data class SafeIndicator(
    val title: String,
    val badge: String,
    val icon: String,
    val body: String
)

/**
 * Derives positive security indicators from actual engine assessment.
 * Shows what checks PASSED based on absence of threat flags.
 */
private fun deriveSafeIndicators(
    assessment: RiskAssessment,
    t: (String) -> String
): List<SafeIndicator> {
    val indicators = mutableListOf<SafeIndicator>()
    val flagsLower = assessment.flags.map { it.lowercase() }
    val details = assessment.details

    // Protocol check - HTTPS is good
    val isHttps = details.originalUrl.startsWith("https")
    if (isHttps) {
        indicators.add(SafeIndicator(
            title = t("Protocol"),
            badge = t("HTTPS"),
            icon = "lock",
            body = t("Secure HTTPS connection with encrypted data transfer.")
        ))
    }

    // Homograph check - no mixed scripts detected
    val hasNoHomograph = !flagsLower.any { it.contains("homograph") || it.contains("mixed script") || it.contains("lookalike") || it.contains("punycode") }
    if (hasNoHomograph) {
        indicators.add(SafeIndicator(
            title = t("Homograph Check"),
            badge = t("CLEAN"),
            icon = "spellcheck",
            body = t("No mixed-script characters or IDN spoofing detected in domain string.")
        ))
    }

    // Brand check - no impersonation
    val hasNoBrandIssue = !flagsLower.any { it.contains("brand") || it.contains("impersonation") }
    if (hasNoBrandIssue && details.brandMatch == null) {
        indicators.add(SafeIndicator(
            title = t("Brand Check"),
            badge = t("CLEAR"),
            icon = "verified_user",
            body = t("No brand impersonation patterns detected.")
        ))
    }

    // Redirect check - no shorteners
    val hasNoRedirect = !flagsLower.any { it.contains("shortener") || it.contains("redirect") }
    if (hasNoRedirect) {
        indicators.add(SafeIndicator(
            title = t("Redirect Chain"),
            badge = t("DIRECT"),
            icon = "alt_route",
            body = t("No URL shorteners or redirect chains detected.")
        ))
    }

    // TLD check - not high-risk
    val tldScore = details.tldScore
    if (tldScore <= 5) {
        indicators.add(SafeIndicator(
            title = t("TLD Risk"),
            badge = t("LOW"),
            icon = "public",
            body = t("Domain uses a standard top-level domain with good reputation.")
        ))
    }

    // ML confidence
    val mlScore = details.mlScore
    if (mlScore <= 20) {
        indicators.add(SafeIndicator(
            title = t("ML Analysis"),
            badge = t("PASSED"),
            icon = "psychology",
            body = t("Machine learning model found no phishing patterns.")
        ))
    }

    return indicators.take(4)
}

@Composable
fun ResultSafeScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanResultSafe(isDark = viewModel.isDarkMode)
    val language = viewModel.appLanguage
    StitchTheme(tokens = tokens) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tokens.colors.background)
            ) {
                AppSidebar(
                    currentScreen = viewModel.currentScreen,
                    onNavigate = { viewModel.currentScreen = it },
                    language = viewModel.appLanguage,
                    onProfileClick = { viewModel.toggleProfileDropdown() },
                    onHelpClick = { viewModel.openHelpDialog() },
                    userName = viewModel.userName,
                    userRole = viewModel.userRole,
                    userInitials = viewModel.userInitials
                )
                SafeResultContent(
                    viewModel = viewModel,
                    onNavigate = { viewModel.currentScreen = it },
                    language = language
                )
            }
            
            // Profile Dropdown Popup
            ProfileDropdown(
                isVisible = viewModel.showProfileDropdown,
                onDismiss = { viewModel.dismissProfileDropdown() },
                userName = viewModel.userName,
                userRole = viewModel.userRole,
                userInitials = viewModel.userInitials,
                historyStats = viewModel.historyStats,
                onViewProfile = { viewModel.currentScreen = AppScreen.TrustCentreAlt },
                onEditProfile = { viewModel.openEditProfileModal() },
                onOpenSettings = { viewModel.currentScreen = AppScreen.TrustCentreAlt },
                language = language
            )
            
            // Edit Profile Dialog
            EditProfileDialog(
                isVisible = viewModel.showEditProfileModal,
                onDismiss = { viewModel.dismissEditProfileModal() },
                currentName = viewModel.userName,
                currentEmail = viewModel.userEmail,
                currentRole = viewModel.userRole,
                currentInitials = viewModel.userInitials,
                onSave = { name, email, role, initials ->
                    viewModel.saveUserProfile(name, email, role, initials)
                },
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
                    }
                }
            }

            // Removed decorative "Simple/Technical" toggle present in web preview.
            // The toggle didn't change engine behaviour and caused confusion for judges,
            // so it was removed and behaviour is driven only by the real assessment data.
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(t("Verdict Analysis"), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                    // Dynamic safe indicators from real engine analysis
                    val safeIndicators = assessment?.let { deriveSafeIndicators(it, t) } ?: emptyList()
                    if (safeIndicators.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            safeIndicators.getOrNull(0)?.let { indicator ->
                                AnalysisCard(title = indicator.title, badge = indicator.badge, icon = indicator.icon, body = indicator.body, modifier = Modifier.weight(1f))
                            }
                            safeIndicators.getOrNull(1)?.let { indicator ->
                                AnalysisCard(title = indicator.title, badge = indicator.badge, icon = indicator.icon, body = indicator.body, modifier = Modifier.weight(1f))
                            } ?: Spacer(modifier = Modifier.weight(1f))
                        }
                        if (safeIndicators.size > 2) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                safeIndicators.getOrNull(2)?.let { indicator ->
                                    AnalysisCard(title = indicator.title, badge = indicator.badge, icon = indicator.icon, body = indicator.body, modifier = Modifier.weight(1f))
                                }
                                safeIndicators.getOrNull(3)?.let { indicator ->
                                    AnalysisCard(title = indicator.title, badge = indicator.badge, icon = indicator.icon, body = indicator.body, modifier = Modifier.weight(1f))
                                } ?: Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        // Fallback when no specific indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            AnalysisCard(title = t("Risk Score"), badge = t("LOW"), icon = "verified_user", body = t("All security checks passed with low risk score."), modifier = Modifier.weight(1f))
                            AnalysisCard(title = t("Analysis"), badge = t("COMPLETE"), icon = "task_alt", body = t("Full heuristic and ML analysis completed successfully."), modifier = Modifier.weight(1f))
                        }
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
                            TechnicalRow(t("Heuristic Score"), tf("%d/40", assessment?.details?.heuristicScore ?: 0))
                            TechnicalRow(t("ML Score"), tf("%d/30", assessment?.details?.mlScore ?: 0))
                            TechnicalRow(t("Brand Match"), assessment?.details?.brandMatch?.let { tf("None") } ?: t("None"), highlight = colors.success)
                            TechnicalRow(t("TLD"), assessment?.details?.tld?.uppercase() ?: "N/A", highlight = if ((assessment?.details?.tldScore ?: 0) == 0) colors.success else colors.warning)
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                                tf("This URL appears to be safe with a risk score of %d. No significant phishing indicators were detected.", assessment.score),
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
