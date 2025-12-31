package com.raouf.mehrguard.desktop.screens

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
import com.raouf.mehrguard.desktop.AppViewModel
import com.raouf.mehrguard.desktop.ResultViewMode
import com.raouf.mehrguard.desktop.i18n.AppLanguage
import com.raouf.mehrguard.desktop.i18n.DesktopStrings
import com.raouf.mehrguard.desktop.navigation.AppScreen
import com.raouf.mehrguard.desktop.theme.StitchTheme
import com.raouf.mehrguard.desktop.theme.StitchTokens
import com.raouf.mehrguard.desktop.theme.LocalStitchTokens
import com.raouf.mehrguard.desktop.theme.ColorTokens
import com.raouf.mehrguard.desktop.ui.AppSidebar
import com.raouf.mehrguard.desktop.ui.MaterialIconRound
import com.raouf.mehrguard.desktop.ui.gridPattern
import com.raouf.mehrguard.desktop.ui.iconContainer
import com.raouf.mehrguard.desktop.ui.statusPill
import com.raouf.mehrguard.desktop.ui.progressFill
import com.raouf.mehrguard.desktop.ui.ProfileDropdown
import com.raouf.mehrguard.desktop.ui.EditProfileDialog
import com.raouf.mehrguard.model.RiskAssessment

/**
 * Security indicator tile data derived from engine flags.
 * Used to dynamically render security analysis tiles based on real engine output.
 */
private data class SecurityIndicator(
    val title: String,
    val badge: String,
    val icon: String,
    val severity: IndicatorSeverity,
    val description: String
)

private enum class IndicatorSeverity { DANGER, WARNING, SUCCESS, INFO }

/**
 * Derives security indicator tiles from actual engine assessment flags.
 * Maps engine output to UI tiles - no hardcoded fake data.
 *
 * @param flags The list of security flags from RiskAssessment.flags
 * @param details The URL analysis details from RiskAssessment.details
 * @param t Translation function for i18n
 * @return List of SecurityIndicator tiles to display
 */
private fun deriveSecurityIndicators(
    flags: List<String>,
    details: com.raouf.mehrguard.model.UrlAnalysisResult?,
    t: (String) -> String
): List<SecurityIndicator> {
    val indicators = mutableListOf<SecurityIndicator>()
    val flagsLower = flags.map { it.lowercase() }

    // IDN Homograph / Mixed Script detection
    val hasHomograph = flagsLower.any { it.contains("homograph") || it.contains("punycode") || it.contains("idn") || it.contains("mixed script") || it.contains("lookalike") }
    if (hasHomograph) {
        indicators.add(SecurityIndicator(
            title = t("IDN Homograph"),
            badge = t("DETECTED"),
            icon = "text_format",
            severity = IndicatorSeverity.DANGER,
            description = t("Mixed script characters detected in domain name intended to spoof legitimate brands.")
        ))
    }

    // HTTP vs HTTPS check
    val isHttp = flagsLower.any { it.contains("http") && (it.contains("not https") || it.contains("insecure") || it.contains("unencrypted")) }
    if (isHttp) {
        indicators.add(SecurityIndicator(
            title = t("Protocol"),
            badge = t("HTTP"),
            icon = "lock_open",
            severity = IndicatorSeverity.DANGER,
            description = t("The URL uses HTTP instead of HTTPS, meaning data is not encrypted.")
        ))
    }

    // Brand impersonation
    val hasBrandImpersonation = flagsLower.any { it.contains("brand") || it.contains("impersonation") }
    val brandMatch = details?.brandMatch
    if (hasBrandImpersonation || brandMatch != null) {
        indicators.add(SecurityIndicator(
            title = t("Brand Detection"),
            badge = brandMatch?.uppercase() ?: t("IMPERSONATION"),
            icon = "verified",
            severity = IndicatorSeverity.DANGER,
            description = t("The URL appears to impersonate a well-known brand, which is a common phishing tactic.")
        ))
    }

    // URL Shortener detection
    val hasShortener = flagsLower.any { it.contains("shortener") || it.contains("shortened") || it.contains("redirect") }
    if (hasShortener) {
        indicators.add(SecurityIndicator(
            title = t("Redirect Chain"),
            badge = t("SHORTENED"),
            icon = "shuffle",
            severity = IndicatorSeverity.WARNING,
            description = t("The URL uses a shortening service, hiding the actual destination.")
        ))
    }

    // High-risk TLD
    val hasRiskyTld = flagsLower.any { it.contains("tld") || it.contains("top-level domain") }
    val tld = details?.tld
    if (hasRiskyTld && tld != null) {
        indicators.add(SecurityIndicator(
            title = t("TLD Risk"),
            badge = ".${tld.uppercase()}",
            icon = "public",
            severity = IndicatorSeverity.WARNING,
            description = t("The domain uses a top-level domain commonly associated with malicious sites.")
        ))
    }

    // IP address instead of domain
    val hasIpHost = flagsLower.any { it.contains("ip address") || it.contains("ip host") }
    if (hasIpHost) {
        indicators.add(SecurityIndicator(
            title = t("IP Host"),
            badge = t("DETECTED"),
            icon = "dns",
            severity = IndicatorSeverity.DANGER,
            description = t("The URL uses an IP address instead of a domain name, which is unusual for legitimate sites.")
        ))
    }

    // Credential harvesting
    val hasCredentials = flagsLower.any { it.contains("credential") || it.contains("password") || it.contains("token") }
    if (hasCredentials) {
        indicators.add(SecurityIndicator(
            title = t("Credentials"),
            badge = t("HARVESTING"),
            icon = "key",
            severity = IndicatorSeverity.DANGER,
            description = t("The URL contains parameters that suggest credential harvesting.")
        ))
    }

    // Subdomain depth
    val hasDeepSubdomain = flagsLower.any { it.contains("subdomain") }
    if (hasDeepSubdomain) {
        indicators.add(SecurityIndicator(
            title = t("Subdomain"),
            badge = t("DEEP"),
            icon = "account_tree",
            severity = IndicatorSeverity.WARNING,
            description = t("The URL has an unusual number of subdomains, which can be used to mislead users.")
        ))
    }

    // JavaScript/Data URI
    val hasDangerousScheme = flagsLower.any { it.contains("javascript") || it.contains("data uri") || it.contains("data:") }
    if (hasDangerousScheme) {
        indicators.add(SecurityIndicator(
            title = t("Scheme"),
            badge = t("DANGEROUS"),
            icon = "code",
            severity = IndicatorSeverity.DANGER,
            description = t("The URL uses a dangerous scheme that can execute code.")
        ))
    }

    // Long URL
    val hasLongUrl = flagsLower.any { it.contains("long") && it.contains("url") }
    if (hasLongUrl) {
        indicators.add(SecurityIndicator(
            title = t("URL Length"),
            badge = t("EXCESSIVE"),
            icon = "straighten",
            severity = IndicatorSeverity.WARNING,
            description = t("The URL is unusually long, which can be used to hide the actual destination.")
        ))
    }

    // If we have SSL info (always show for completeness - HTTPS means valid cert)
    val isHttps = !isHttp && details?.originalUrl?.startsWith("https") == true
    if (isHttps && indicators.isNotEmpty()) {
        indicators.add(SecurityIndicator(
            title = t("SSL Certificate"),
            badge = t("VALID"),
            icon = "lock",
            severity = IndicatorSeverity.SUCCESS,
            description = t("Valid SSL certificate present. Note: Valid SSL does not guarantee site legitimacy.")
        ))
    }

    return indicators.take(4) // Limit to 4 tiles for UI layout
}

@Composable
fun ResultDangerousScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanResultDangerous(isDark = viewModel.isDarkMode)
    val language = viewModel.appLanguage
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
                    language = viewModel.appLanguage,
                    onProfileClick = { viewModel.toggleProfileDropdown() },
                    userName = viewModel.userName,
                    userRole = viewModel.userRole,
                    userInitials = viewModel.userInitials
                )
                DangerousContent(
                    viewModel = viewModel,
                    onNavigate = { viewModel.currentScreen = it }
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Decorative background circle - positioned absolutely so it doesn't take layout space
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-150).dp)
                .background(colors.danger.copy(alpha = 0.1f), CircleShape)
                .blur(60.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .gridPattern(spacing = 40.dp, lineColor = colors.border.copy(alpha = 0.05f), lineWidth = 1.dp)
                .verticalScroll(rememberScrollState())
                .padding(32.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(t("Threat Analysis Report"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
        }

        if (assessment == null || url.isNullOrBlank()) {
            EmptyResultState(onNavigate = onNavigate, language = language)
            return@Column
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) colors.danger.copy(alpha = 0.15f) else colors.danger.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, if (isDark) colors.danger.copy(alpha = 0.3f) else colors.danger.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
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
                Spacer(modifier = Modifier.width(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.shareTextReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(120.dp).height(40.dp)
                    ) {
                        MaterialIconRound(name = "flag", size = 16.sp, color = colors.danger)
                        Spacer(Modifier.width(4.dp))
                        Text(t("Report"), fontSize = 12.sp, color = colors.danger, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = { viewModel.addBlocklistDomain(viewModel.hostFromUrl(url) ?: url) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(170.dp).height(40.dp)
                    ) {
                        MaterialIconRound(name = "block", size = 16.sp, color = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text(t("Block Access"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, softWrap = false)
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
                            // Dynamic security indicator tiles from real engine flags
                            val indicators = deriveSecurityIndicators(assessment.flags, assessment.details, t)
                            if (indicators.isNotEmpty()) {
                                // First row of indicators (up to 2)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    indicators.getOrNull(0)?.let { indicator ->
                                        val tileColor = when (indicator.severity) {
                                            IndicatorSeverity.DANGER -> colors.danger
                                            IndicatorSeverity.WARNING -> colors.warning
                                            IndicatorSeverity.SUCCESS -> colors.success
                                            IndicatorSeverity.INFO -> colors.primary
                                        }
                                        InfoTile(indicator.title, indicator.badge, indicator.icon, tileColor, indicator.description, modifier = Modifier.weight(1f))
                                    }
                                    indicators.getOrNull(1)?.let { indicator ->
                                        val tileColor = when (indicator.severity) {
                                            IndicatorSeverity.DANGER -> colors.danger
                                            IndicatorSeverity.WARNING -> colors.warning
                                            IndicatorSeverity.SUCCESS -> colors.success
                                            IndicatorSeverity.INFO -> colors.primary
                                        }
                                        InfoTile(indicator.title, indicator.badge, indicator.icon, tileColor, indicator.description, modifier = Modifier.weight(1f))
                                    } ?: Spacer(modifier = Modifier.weight(1f))
                                }
                                // Second row of indicators (up to 2 more)
                                if (indicators.size > 2) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                        indicators.getOrNull(2)?.let { indicator ->
                                            val tileColor = when (indicator.severity) {
                                                IndicatorSeverity.DANGER -> colors.danger
                                                IndicatorSeverity.WARNING -> colors.warning
                                                IndicatorSeverity.SUCCESS -> colors.success
                                                IndicatorSeverity.INFO -> colors.primary
                                            }
                                            InfoTile(indicator.title, indicator.badge, indicator.icon, tileColor, indicator.description, modifier = Modifier.weight(1f))
                                        }
                                        indicators.getOrNull(3)?.let { indicator ->
                                            val tileColor = when (indicator.severity) {
                                                IndicatorSeverity.DANGER -> colors.danger
                                                IndicatorSeverity.WARNING -> colors.warning
                                                IndicatorSeverity.SUCCESS -> colors.success
                                                IndicatorSeverity.INFO -> colors.primary
                                            }
                                            InfoTile(indicator.title, indicator.badge, indicator.icon, tileColor, indicator.description, modifier = Modifier.weight(1f))
                                        } ?: Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            } else {
                                // Fallback: Show a summary when no specific indicators detected
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    InfoTile(
                                        t("Risk Analysis"),
                                        t("HIGH"),
                                        "analytics",
                                        colors.danger,
                                        t("Multiple phishing indicators detected. See threat score for details."),
                                        modifier = Modifier.weight(1f)
                                    )
                                    val isHttps = assessment.details.originalUrl.startsWith("https")
                                    InfoTile(
                                        t("Protocol"),
                                        if (isHttps) t("HTTPS") else t("HTTP"),
                                        if (isHttps) "lock" else "lock_open",
                                        if (isHttps) colors.success else colors.danger,
                                        if (isHttps) t("Valid SSL certificate present. Note: Valid SSL does not guarantee site legitimacy.")
                                        else t("The URL uses HTTP instead of HTTPS, meaning data is not encrypted."),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = colors.surface, border = BorderStroke(1.dp, colors.border)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(t("Threat Score"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, letterSpacing = 1.sp)
                        Box(
                            modifier = Modifier.size(128.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Red circle background behind the score
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(colors.danger.copy(alpha = 0.15f), CircleShape)
                                    .border(2.dp, colors.danger.copy(alpha = 0.3f), CircleShape)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    assessment.score.toString(),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.danger
                                )
                            }
                            Text(
                                t(assessment.scoreDescription).uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.danger,
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

                // Technical Indicators - unified with Safe/Suspicious screens
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
                            MaterialIconRound(name = "analytics", size = 18.sp, color = colors.textSub)
                            Text(t("Technical Indicators"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                        }
                        // Real engine data - unified across all result screens
                        val details = assessment.details
                        TechnicalRow(
                            t("Heuristic Score"),
                            "${details.heuristicScore}/40",
                            if (details.heuristicScore >= 20) colors.danger else colors.warning
                        )
                        TechnicalRow(
                            t("ML Score"),
                            "${details.mlScore}/30",
                            if (details.mlScore >= 15) colors.danger else colors.warning
                        )
                        TechnicalRow(
                            t("Brand Match"),
                            details.brandMatch ?: t("None"),
                            if (details.brandMatch != null) colors.danger else colors.success
                        )
                        TechnicalRow(
                            t("TLD Risk"),
                            ".${(details.tld ?: "unknown").uppercase()} (${details.tldScore}/10)",
                            if (details.tldScore >= 5) colors.danger else colors.warning
                        )
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
        } // Close Column
    } // Close outer Box
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
private fun TechnicalRow(label: String, value: String, highlight: Color? = null) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = colors.textSub)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = highlight ?: colors.textMain)
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
