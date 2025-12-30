package com.qrshield.desktop.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.qrshield.desktop.ui.MaterialIcon
import com.qrshield.desktop.ui.gridPattern
import com.qrshield.desktop.ui.iconContainer
import com.qrshield.desktop.ui.panelSurface
import com.qrshield.desktop.ui.statusPill
import com.qrshield.desktop.ui.progressFill
import com.qrshield.desktop.ui.ProfileDropdown
import com.qrshield.desktop.ui.EditProfileDialog
import com.qrshield.model.RiskAssessment

/**
 * Suspicious indicator data derived from engine flags.
 */
private data class SuspiciousIndicatorDot(
    val color: Color,
    val label: String
)

/**
 * Alert card data derived from engine assessment.
 */
private data class SuspiciousAlert(
    val icon: String,
    val title: String,
    val status: String,
    val statusColor: Color,
    val badgeBg: Color,
    val progress: Float,
    val footerLabel: String,
    val footerValue: String,
    val body: String
)

/**
 * Derives indicator dots from actual engine flags for the suspicious result screen.
 */
private fun deriveSuspiciousIndicatorDots(
    flags: List<String>,
    details: com.qrshield.model.UrlAnalysisResult?,
    t: (String) -> String,
    warningColor: Color,
    dangerColor: Color,
    successColor: Color
): List<SuspiciousIndicatorDot> {
    val dots = mutableListOf<SuspiciousIndicatorDot>()
    val flagsLower = flags.map { it.lowercase() }

    // Protocol indicator
    val isHttps = details?.originalUrl?.startsWith("https") == true
    dots.add(SuspiciousIndicatorDot(
        if (isHttps) successColor else dangerColor,
        if (isHttps) t("Protocol: HTTPS") else t("Protocol: HTTP (No Encryption)")
    ))

    // Shortener/redirect
    if (flagsLower.any { it.contains("shortener") || it.contains("redirect") }) {
        dots.add(SuspiciousIndicatorDot(warningColor, t("URL Shortener Detected")))
    }

    // TLD indicator
    if (flagsLower.any { it.contains("tld") }) {
        val tld = details?.tld ?: "unknown"
        dots.add(SuspiciousIndicatorDot(warningColor, t("TLD: .$tld (High Risk)")))
    }

    // Subdomain depth
    if (flagsLower.any { it.contains("subdomain") }) {
        dots.add(SuspiciousIndicatorDot(warningColor, t("Deep Subdomain Structure")))
    }

    return dots.take(3)
}

/**
 * Derives alert cards from actual engine assessment for suspicious results.
 */
private fun deriveSuspiciousAlerts(
    assessment: RiskAssessment,
    t: (String) -> String,
    warningColor: Color,
    dangerColor: Color,
    successColor: Color,
    backgroundAlt: Color
): List<SuspiciousAlert> {
    val alerts = mutableListOf<SuspiciousAlert>()
    val flagsLower = assessment.flags.map { it.lowercase() }
    val details = assessment.details

    // Homograph/IDN check
    val hasHomograph = flagsLower.any { it.contains("homograph") || it.contains("mixed script") || it.contains("lookalike") || it.contains("punycode") }
    alerts.add(SuspiciousAlert(
        icon = "spellcheck",
        title = t("Homograph Check"),
        status = if (hasHomograph) t("Detected") else t("Clean"),
        statusColor = if (hasHomograph) dangerColor else successColor,
        badgeBg = if (hasHomograph) dangerColor.copy(alpha = 0.1f) else successColor.copy(alpha = 0.1f),
        progress = if (hasHomograph) 0.9f else 0.1f,
        footerLabel = t("Confidence"),
        footerValue = if (hasHomograph) "90%" else "10%",
        body = if (hasHomograph) t("Domain uses lookalike characters to mimic legitimate sites.")
               else t("No mixed-script or lookalike characters detected.")
    ))

    // Heuristic score-based alert
    val heuristicScore = details.heuristicScore
    alerts.add(SuspiciousAlert(
        icon = "analytics",
        title = t("Heuristic Analysis"),
        status = when {
            heuristicScore >= 30 -> t("High Risk")
            heuristicScore >= 15 -> t("Medium Risk")
            else -> t("Low Risk")
        },
        statusColor = when {
            heuristicScore >= 30 -> dangerColor
            heuristicScore >= 15 -> warningColor
            else -> successColor
        },
        badgeBg = when {
            heuristicScore >= 30 -> dangerColor.copy(alpha = 0.1f)
            heuristicScore >= 15 -> warningColor.copy(alpha = 0.1f)
            else -> backgroundAlt
        },
        progress = (heuristicScore / 40f).coerceIn(0f, 1f),
        footerLabel = t("Score"),
        footerValue = "$heuristicScore/40",
        body = when {
            heuristicScore >= 30 -> t("Multiple security heuristics triggered.")
            heuristicScore >= 15 -> t("Some security patterns detected.")
            else -> t("Few security patterns detected.")
        }
    ))

    // ML score alert
    val mlScore = details.mlScore
    alerts.add(SuspiciousAlert(
        icon = "psychology",
        title = t("ML Analysis"),
        status = when {
            mlScore >= 20 -> t("Suspicious")
            mlScore >= 10 -> t("Cautious")
            else -> t("Low Risk")
        },
        statusColor = when {
            mlScore >= 20 -> dangerColor
            mlScore >= 10 -> warningColor
            else -> successColor
        },
        badgeBg = when {
            mlScore >= 20 -> dangerColor.copy(alpha = 0.1f)
            mlScore >= 10 -> warningColor.copy(alpha = 0.1f)
            else -> backgroundAlt
        },
        progress = (mlScore / 30f).coerceIn(0f, 1f),
        footerLabel = t("Score"),
        footerValue = "$mlScore/30",
        body = when {
            mlScore >= 20 -> t("Machine learning model detected phishing patterns.")
            mlScore >= 10 -> t("Some phishing-like patterns detected.")
            else -> t("Low phishing probability from ML analysis.")
        }
    ))

    // Brand detection
    val brandMatch = details.brandMatch
    alerts.add(SuspiciousAlert(
        icon = "verified_user",
        title = t("Brand Detection"),
        status = if (brandMatch != null) t("Match: $brandMatch") else t("Clean"),
        statusColor = if (brandMatch != null) dangerColor else successColor,
        badgeBg = if (brandMatch != null) dangerColor.copy(alpha = 0.1f) else successColor.copy(alpha = 0.1f),
        progress = if (brandMatch != null) 0.85f else 0.05f,
        footerLabel = t("Brand"),
        footerValue = brandMatch ?: t("None"),
        body = if (brandMatch != null) t("URL appears to impersonate $brandMatch.")
               else t("No brand impersonation patterns detected.")
    ))

    return alerts.take(4)
}

@Composable
fun ResultSuspiciousScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanResultSuspicious(isDark = viewModel.isDarkMode)
    val language = viewModel.appLanguage
    StitchTheme(tokens = tokens) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.colors.background)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                AppSidebar(
                    currentScreen = AppScreen.ResultSuspicious,
                    onNavigate = { viewModel.currentScreen = it },
                    language = viewModel.appLanguage,
                    onProfileClick = { viewModel.toggleProfileDropdown() },
                    onHelpClick = { viewModel.openHelpDialog() },
                    userName = viewModel.userName,
                    userRole = viewModel.userRole,
                    userInitials = viewModel.userInitials
                )
                SuspiciousContent(
                    viewModel = viewModel,
                    isDark = viewModel.isDarkMode,
                    onNavigate = { viewModel.currentScreen = it },
                    language = language
                )
            }
            ThemeToggleButton(isDark = viewModel.isDarkMode, onToggle = { viewModel.toggleTheme() })
            
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
private fun SuspiciousContent(
    viewModel: AppViewModel,
    isDark: Boolean,
    onNavigate: (AppScreen) -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    val assessment = viewModel.currentAssessment
    val url = viewModel.currentUrl
    val verdictDetails = viewModel.currentVerdictDetails
    val scanId = viewModel.lastAnalyzedAt?.toString()?.takeLast(6) ?: t("LATEST")
    val analysisId = assessment?.let { tf("SCAN-%s-H%s", scanId, it.score) } ?: tf("SCAN-%s", scanId)
    val colors = LocalStitchTokens.current.colors
    val resolvedUrl = url.orEmpty()
    val uri = runCatching { java.net.URI(resolvedUrl) }.getOrNull()
    val scheme = uri?.scheme ?: "https"
    val host = uri?.host ?: resolvedUrl.substringAfter("://").substringBefore("/")
    val path = uri?.rawPath.orEmpty()
    val query = uri?.rawQuery?.let { "?$it" }.orEmpty()
    val tld = host.substringAfterLast('.', "")
    val hostBase = if (tld.isNotBlank()) host.removeSuffix(".$tld") else host
    val visualSelected = viewModel.scanMonitorViewMode == com.qrshield.desktop.ScanMonitorViewMode.Visual
    val rawSelected = viewModel.scanMonitorViewMode == com.qrshield.desktop.ScanMonitorViewMode.Raw

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .gridPattern(spacing = 40.dp, lineColor = colors.border.copy(alpha = if (isDark) 0.1f else 0.05f), lineWidth = 1.dp)
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = (-40).dp)
                .background(colors.warning.copy(alpha = 0.05f), CircleShape)
                .blur(100.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (assessment == null || url.isNullOrBlank()) {
                    EmptyResultState(onNavigate = onNavigate, language = language)
                } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(colors.warning.copy(alpha = 0.03f))
                        )
                        Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colors.warning.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialIcon(name = "warning_amber", size = 28.sp, color = colors.warning)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(t("Caution Advised: Suspicious Activity Detected"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                    Box(
                                        modifier = Modifier
                                            .statusPill(colors.warning.copy(alpha = 0.2f), colors.warning.copy(alpha = 0.3f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(t("SUSPICIOUS"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.warning, letterSpacing = 1.sp)
                                    }
                                }
                                Text(
                                    verdictDetails?.summary?.let { t(it) }
                                        ?: t(assessment.actionRecommendation),
                                    fontSize = 13.sp,
                                    color = colors.textSub,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ActionButton(
                                        "content_copy",
                                        t("Copy URL (Sanitized)"),
                                        colors.surface,
                                        colors.border,
                                        colors.textMain,
                                        onClick = { viewModel.copyUrl(viewModel.sanitizedUrl(url), label = t("Sanitized URL copied")) }
                                    )
                                    ActionButton(
                                        "block",
                                        t("Block Domain"),
                                        colors.danger.copy(alpha = 0.1f),
                                        colors.danger.copy(alpha = 0.2f),
                                        colors.danger,
                                        onClick = { viewModel.addBlocklistDomain(viewModel.hostFromUrl(url) ?: url) }
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = {
                                            viewModel.showInfo(t("Opening in browser sandbox..."))
                                            viewModel.openUrl(url)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.warning),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Text(t("Open in Sandbox"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(Modifier.width(6.dp))
                                        MaterialIcon(name = "open_in_new", size = 14.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(t("Target Destination"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, letterSpacing = 1.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(t("View Mode:"), fontSize = 12.sp, color = colors.textSub)
                                        Row(
                                            modifier = Modifier
                                                .iconContainer(if (isDark) colors.backgroundAlt else colors.backgroundAlt)
                                                .padding(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (visualSelected) colors.surface else Color.Transparent)
                                                    .clickable { viewModel.scanMonitorViewMode = com.qrshield.desktop.ScanMonitorViewMode.Visual }
                                                    .focusable()
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    t("Visual"),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (visualSelected) colors.textMain else colors.textSub
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (rawSelected) colors.surface else Color.Transparent)
                                                    .clickable { viewModel.scanMonitorViewMode = com.qrshield.desktop.ScanMonitorViewMode.Raw }
                                                    .focusable()
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    t("Raw"),
                                                    fontSize = 12.sp,
                                                    fontWeight = if (rawSelected) FontWeight.Medium else FontWeight.Normal,
                                                    color = if (rawSelected) colors.textMain else colors.textSub
                                                )
                                            }
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) colors.background else colors.backgroundAlt)
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .padding(16.dp)
                                ) {
                                    if (rawSelected) {
                                        Text(
                                            resolvedUrl.ifBlank { t("No URL captured.") },
                                            fontSize = 13.sp,
                                            color = colors.textMain
                                        )
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            val domainColor = if (assessment.verdict == com.qrshield.model.Verdict.SUSPICIOUS) colors.danger else colors.textMain
                                            val domainBg = if (assessment.verdict == com.qrshield.model.Verdict.SUSPICIOUS) colors.danger.copy(alpha = 0.1f) else Color.Transparent
                                            MaterialIcon(name = "lock", size = 16.sp, color = colors.textMuted)
                                            Text("$scheme://", fontSize = 13.sp, color = colors.textSub)
                                            Text(
                                                hostBase.ifBlank { host },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = domainColor,
                                                modifier = if (domainBg == Color.Transparent) Modifier else Modifier.background(domainBg, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)
                                            )
                                            if (tld.isNotBlank()) {
                                                Text(".${tld}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.warning)
                                            }
                                            Text(path + query, fontSize = 13.sp, color = colors.textSub)
                                        }
                                    }
                                }
                                // Dynamic indicator dots from real engine flags
                                val indicatorDots = deriveSuspiciousIndicatorDots(
                                    assessment.flags,
                                    assessment.details,
                                    t,
                                    colors.warning,
                                    colors.danger,
                                    colors.success
                                )
                                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    indicatorDots.forEach { dot ->
                                        IndicatorDot(dot.color, dot.label, colors.textSub)
                                    }
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(t("Risk Score"), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.textMuted, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.Start))
                                RiskGauge(score = assessment.score, color = colors.warning, label = t(assessment.scoreDescription))
                                Text(t("Score based on heuristic analysis of domain age, entropy, and keyword matching."), fontSize = 11.sp, color = colors.textSub, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                // Dynamic alert cards from real engine assessment
                val suspiciousAlerts = deriveSuspiciousAlerts(
                    assessment,
                    t,
                    colors.warning,
                    colors.danger,
                    colors.success,
                    colors.backgroundAlt
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    suspiciousAlerts.forEachIndexed { index, alert ->
                        AlertCard(
                            icon = alert.icon,
                            title = alert.title,
                            status = alert.status,
                            color = alert.statusColor,
                            badgeBg = alert.badgeBg,
                            progress = alert.progress,
                            footerLabel = alert.footerLabel,
                            footerValue = alert.footerValue,
                            body = alert.body,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if less than 4 alerts
                    repeat(4 - suspiciousAlerts.size) {
                        Spacer(modifier = Modifier.weight(1f))
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
                                .border(1.dp, colors.border)
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(t("Technical Indicators"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Tag(t("DNS Records"))
                                Tag(t("WhoIs"))
                                Tag(t("Heuristics"), active = true)
                            }
                        }
                        // Dynamic technical indicators from real engine analysis
                        val details = assessment.details
                        val heuristicScore = details.heuristicScore
                        val mlScore = details.mlScore
                        val brandMatch = details.brandMatch
                        val tld = details.tld
                        val tldScore = details.tldScore

                        TableRow(
                            t("Heuristic Score"),
                            "$heuristicScore/40",
                            when {
                                heuristicScore >= 30 -> "error"
                                heuristicScore >= 15 -> "warning"
                                else -> "check_circle"
                            },
                            when {
                                heuristicScore >= 30 -> colors.danger
                                heuristicScore >= 15 -> colors.warning
                                else -> colors.success
                            }
                        )
                        TableRow(
                            t("ML Score"),
                            "$mlScore/30",
                            when {
                                mlScore >= 20 -> "error"
                                mlScore >= 10 -> "warning"
                                else -> "check_circle"
                            },
                            when {
                                mlScore >= 20 -> colors.danger
                                mlScore >= 10 -> colors.warning
                                else -> colors.success
                            }
                        )
                        TableRow(
                            t("Brand Match"),
                            brandMatch ?: t("None"),
                            if (brandMatch != null) "warning" else "check_circle",
                            if (brandMatch != null) colors.warning else colors.success
                        )
                        TableRow(
                            t("TLD Risk"),
                            ".${(tld ?: "unknown").uppercase()} (${tldScore}/10)",
                            when {
                                tldScore >= 7 -> "error"
                                tldScore >= 3 -> "warning"
                                else -> "check_circle"
                            },
                            when {
                                tldScore >= 7 -> colors.danger
                                tldScore >= 3 -> colors.warning
                                else -> colors.success
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) colors.background.copy(alpha = 0.2f) else colors.backgroundAlt)
                                .border(1.dp, colors.border)
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tf("Analysis ID: #%s", analysisId), fontSize = 12.sp, color = colors.textSub)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    t("View Full JSON Log"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.warning,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.copyJsonReport()
                                            onNavigate(AppScreen.ReportsExport)
                                        }
                                        .focusable()
                                )
                                MaterialIcon(name = "arrow_forward", size = 14.sp, color = colors.warning)
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
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(t("No scan data available."), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
            Text(t("Run a scan to view suspicious results."), fontSize = 13.sp, color = colors.textSub)
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
private fun PulseDot(color: Color) {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse)
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun ActionButton(icon: String, label: String, bg: Color, border: Color, text: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .focusable()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MaterialIcon(name = icon, size = 14.sp, color = text)
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = text)
    }
}

@Composable
private fun IndicatorDot(color: Color, label: String, textMuted: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 12.sp, color = textMuted)
    }
}

@Composable
private fun RiskGauge(score: Int, color: Color, label: String) {
    val colors = LocalStitchTokens.current.colors
    val outline = colors.border.copy(alpha = 0.3f)
    Box(modifier = Modifier.size(128.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            drawCircle(color = outline, style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (score / 100f),
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(score.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
            Text(label, fontSize = 12.sp, color = color)
        }
    }
}

@Composable
private fun AlertCard(
    icon: String,
    title: String,
    status: String,
    color: Color,
    badgeBg: Color,
    progress: Float,
    footerLabel: String,
    footerValue: String,
    body: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .iconContainer(colors.warning.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIcon(name = icon, size = 18.sp, color = color)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(status, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = color)
                }
            }
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
            Text(body, fontSize = 12.sp, color = colors.textSub, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .progressFill(colors.border.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .progressFill(color)
                )
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(footerLabel, fontSize = 11.sp, color = colors.textSub)
                Text(footerValue, fontSize = 11.sp, color = colors.textSub)
            }
        }
    }
}

@Composable
private fun Tag(label: String, active: Boolean = false) {
    val colors = LocalStitchTokens.current.colors
    val bg = if (active) colors.primary.copy(alpha = 0.1f) else colors.backgroundAlt
    val text = if (active) colors.primary else colors.textSub
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, text.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = text)
    }
}

@Composable
private fun TableRow(label: String, detail: String, icon: String, color: Color) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border.copy(alpha = 0.2f))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textSub, modifier = Modifier.width(180.dp))
        Text(detail, fontSize = 13.sp, color = colors.textMain, modifier = Modifier.weight(1f))
        MaterialIcon(name = icon, size = 16.sp, color = color)
    }
}

@Composable
private fun BoxScope.ThemeToggleButton(isDark: Boolean, onToggle: () -> Unit) {
    val colors = LocalStitchTokens.current.colors
    Box(
        modifier = Modifier
            .size(48.dp)
            .align(Alignment.BottomEnd)
            .offset(x = (-24).dp, y = (-24).dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(1.dp, colors.border)
            .clickable { onToggle() }
    ) {
        MaterialIcon(
            name = if (isDark) "light_mode" else "dark_mode",
            size = 20.sp,
            color = if (isDark) colors.warning else colors.textMain,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
