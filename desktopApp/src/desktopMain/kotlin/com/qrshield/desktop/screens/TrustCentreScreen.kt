@file:Suppress("DEPRECATION") // painterResource - migration to Compose Resources planned

package com.qrshield.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.HeuristicSensitivity
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.theme.LocalStitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.desktop.ui.gridPattern
import com.qrshield.desktop.ui.iconContainer
import com.qrshield.desktop.ui.progressTrack
import com.qrshield.desktop.ui.progressFill
import com.qrshield.desktop.ui.toggleTrack
import com.qrshield.desktop.ui.handCursor
import com.qrshield.desktop.ui.ProfileDropdown
import com.qrshield.desktop.ui.EditProfileDialog

@Composable
fun TrustCentreScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.trustCentre(isDark = viewModel.isDarkMode)
    val language = viewModel.appLanguage
    StitchTheme(tokens = tokens) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tokens.colors.background)
                    .gridPattern(spacing = 40.dp, lineColor = tokens.colors.border, lineWidth = 1.dp)
            ) {
                AppSidebar(
                    currentScreen = AppScreen.TrustCentre,
                    onNavigate = { viewModel.currentScreen = it },
                    language = viewModel.appLanguage,
                    onProfileClick = { viewModel.toggleProfileDropdown() }
                )
                TrustCentreContent(
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
private fun TrustCentreContent(viewModel: AppViewModel, onNavigate: (AppScreen) -> Unit) {
    val language = viewModel.appLanguage
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    val sensitivity = viewModel.heuristicSensitivity
    val lowSelected = sensitivity == HeuristicSensitivity.Low
    val balancedSelected = sensitivity == HeuristicSensitivity.Balanced
    val paranoiaSelected = sensitivity == HeuristicSensitivity.Paranoia
    val modeLabel = when (sensitivity) {
        HeuristicSensitivity.Low -> t("MODE: LOW")
        HeuristicSensitivity.Balanced -> t("MODE: BALANCED")
        HeuristicSensitivity.Paranoia -> t("MODE: PARANOIA")
    }
    val colors = LocalStitchTokens.current.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(t("Trust Centre & Privacy Controls"), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
            Text(t("Manage offline heuristics, data retention policies, and domain allowlists. All changes apply immediately."), fontSize = 16.sp, color = colors.textSub, modifier = Modifier.widthIn(max = 640.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.success.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                MaterialSymbol(
                    name = "shield_lock",
                    size = 180.sp,
                    color = colors.success.copy(alpha = 0.1f),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header row with button aligned to top-right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.success))
                            Text(t("AIR-GAPPED STATUS: ACTIVE"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.success, letterSpacing = 1.2.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .offset(x = 8.dp, y = (-4).dp)
                                .clickable { onNavigate(AppScreen.ReportsExport) }
                                .focusable()
                                .handCursor()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MaterialSymbol(name = "description", size = 16.sp, color = colors.textMain)
                                Text(t("View Audit Log"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                            }
                        }
                    }
                    // Title and description below
                    Text(t("Strict Offline Guarantee"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                    Text(
                        t("QR-SHIELD operates entirely on your local hardware. No image data, scanned URLs, or telemetry are sent to the cloud for analysis."),
                        fontSize = 16.sp,
                        color = colors.textSub,
                        modifier = Modifier.widthIn(max = 520.dp)
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Header row
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialSymbol(name = "tune", size = 20.sp, color = colors.primary)
                            }
                            Column {
                                Text(t("Heuristic Sensitivity"), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                                Text(t("Adjust detection strictness"), fontSize = 12.sp, color = colors.textSub)
                            }
                        }
                        // Current mode badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    when (sensitivity) {
                                        HeuristicSensitivity.Low -> colors.success.copy(alpha = 0.1f)
                                        HeuristicSensitivity.Balanced -> colors.primary.copy(alpha = 0.1f)
                                        HeuristicSensitivity.Paranoia -> colors.warning.copy(alpha = 0.15f)
                                    }
                                )
                                .border(
                                    1.dp,
                                    when (sensitivity) {
                                        HeuristicSensitivity.Low -> colors.success.copy(alpha = 0.3f)
                                        HeuristicSensitivity.Balanced -> colors.primary.copy(alpha = 0.3f)
                                        HeuristicSensitivity.Paranoia -> colors.warning.copy(alpha = 0.3f)
                                    },
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                modeLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (sensitivity) {
                                    HeuristicSensitivity.Low -> colors.success
                                    HeuristicSensitivity.Balanced -> colors.primary
                                    HeuristicSensitivity.Paranoia -> colors.warning
                                },
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    
                    // Segmented control style selector
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = colors.backgroundAlt,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Low option
                            SensitivityOption(
                                label = t("Low"),
                                description = t("Minimal alerts"),
                                icon = "shield",
                                selected = lowSelected,
                                color = colors.success,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.updateHeuristicSensitivity(HeuristicSensitivity.Low) }
                            )
                            
                            // Balanced option
                            SensitivityOption(
                                label = t("Balanced"),
                                description = t("Recommended"),
                                icon = "verified_user",
                                selected = balancedSelected,
                                color = colors.primary,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.updateHeuristicSensitivity(HeuristicSensitivity.Balanced) }
                            )
                            
                            // Paranoia option
                            SensitivityOption(
                                label = t("Paranoia"),
                                description = t("Maximum security"),
                                icon = "warning",
                                selected = paranoiaSelected,
                                color = colors.warning,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.updateHeuristicSensitivity(HeuristicSensitivity.Paranoia) }
                            )
                        }
                    }
                }
            }
            
            // Verdict Logic Explanation card
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialSymbol(name = "info", size = 16.sp, color = colors.primary)
                        Text(t("Verdict Logic"), fontSize = 13.sp, color = colors.textMain, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        when (sensitivity) {
                            HeuristicSensitivity.Low -> t("Low mode reduces false positives. Only flags confirmed malicious patterns. Best for trusted environments.")
                            HeuristicSensitivity.Balanced -> t("Balanced mode uses standard heuristics. Flags known malicious patterns and suspicious URL shorteners.")
                            HeuristicSensitivity.Paranoia -> t("Paranoia mode enables maximum scrutiny. All edge cases trigger warnings. Best for high-security needs.")
                        },
                        fontSize = 13.sp,
                        color = colors.textSub,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.backgroundAlt)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(t("Engine v4.1.2 • Sig DB: 2023-10-27"), fontSize = 10.sp, color = colors.textMuted)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ToggleCard(
                title = t("Strict Offline Mode"),
                subtitle = t("Force disable all network adapters for app"),
                enabled = viewModel.trustCentreToggles.strictOffline,
                activeColor = colors.primary,
                modifier = Modifier.weight(1f),
                onToggle = { viewModel.toggleStrictOffline() }
            )
            ToggleCard(
                title = t("Anonymous Telemetry"),
                subtitle = t("Share threat signatures to improve DB"),
                enabled = viewModel.trustCentreToggles.anonymousTelemetry,
                activeColor = colors.primary,
                modifier = Modifier.weight(1f),
                onToggle = { viewModel.toggleAnonymousTelemetry() }
            )
            ToggleCard(
                title = t("Auto-copy Safe Links"),
                subtitle = t("Copy to clipboard if Verdict is SAFE"),
                enabled = viewModel.trustCentreToggles.autoCopySafe,
                activeColor = colors.primary,
                modifier = Modifier.weight(1f),
                onToggle = { viewModel.toggleAutoCopySafe() }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
            AllowListCard(viewModel = viewModel, modifier = Modifier.weight(1f), language = language)
            BlockListCard(viewModel = viewModel, modifier = Modifier.weight(1f), language = language)
        }
    }
}

@Composable
private fun ToggleCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier.heightIn(min = 76.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .focusable()
                .handCursor()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(subtitle, fontSize = 12.sp, color = colors.textSub, maxLines = 2)
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Explicit toggle component with fixed sizing
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) activeColor else colors.border.copy(alpha = 0.6f)),
                contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun SensitivityOption(
    label: String,
    description: String,
    icon: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .focusable(),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) color.copy(alpha = 0.1f) else Color.Transparent,
        border = if (selected) BorderStroke(1.5.dp, color.copy(alpha = 0.4f)) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (selected) color.copy(alpha = 0.15f) else colors.border.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(
                    name = icon,
                    size = 18.sp,
                    color = if (selected) color else colors.textMuted
                )
            }
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) color else colors.textSub
            )
            Text(
                description,
                fontSize = 10.sp,
                color = if (selected) color.copy(alpha = 0.8f) else colors.textMuted
            )
        }
    }
}

@Composable
private fun AllowListCard(viewModel: AppViewModel, modifier: Modifier = Modifier, language: AppLanguage) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val allowlist = viewModel.filteredAllowlist()
    val query = viewModel.allowlistQuery
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.height(400.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.backgroundAlt)
                    .border(1.dp, colors.border)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialSymbol(name = "check_circle", size = 20.sp, color = colors.success)
                    Text(t("Domain Allowlist"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .iconContainer(Color.Transparent)
                        .clickable {
                            val trimmed = query.trim()
                            if (trimmed.isBlank()) {
                                viewModel.showInfo(t("Enter a domain to add."))
                            } else {
                                viewModel.addAllowlistDomain(trimmed)
                                viewModel.updateAllowlistQuery("")
                            }
                        }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(name = "add", size = 20.sp, color = colors.textSub)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (allowlist.isEmpty()) {
                    EmptyListItem(t("No allowlisted domains yet."))
                } else {
                    allowlist.forEach { domain ->
                        AllowItem(
                            iconPath = allowlistIconFor(domain),
                            domain = domain,
                            onDelete = { viewModel.removeAllowlistDomain(domain) }
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.backgroundAlt)
                    .border(1.dp, colors.border)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { viewModel.updateAllowlistQuery(it) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = colors.textMain),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MaterialSymbol(name = "search", size = 16.sp, color = colors.textSub)
                            Box(modifier = Modifier.weight(1f)) {
                                if (query.isBlank()) {
                                    Text(t("Search domains..."), fontSize = 13.sp, color = colors.textMuted)
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AllowItem(iconPath: String?, domain: String, onDelete: () -> Unit) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Always show letter avatar (more reliable than external favicons)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    domain.trim().removePrefix("*.").take(1).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
        }
        MaterialSymbol(
            name = "delete",
            size = 18.sp,
            color = colors.danger,
            modifier = Modifier
                .clickable { onDelete() }
                .focusable()
        )
    }
}

@Composable
private fun BlockListCard(viewModel: AppViewModel, modifier: Modifier = Modifier, language: AppLanguage) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val blocklist = viewModel.filteredBlocklist()
    val query = viewModel.blocklistQuery
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.height(400.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.backgroundAlt)
                    .border(1.dp, colors.border)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialSymbol(name = "block", size = 20.sp, color = colors.danger)
                    Text(t("Custom Blocklist"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .iconContainer(Color.Transparent)
                        .clickable {
                            val trimmed = query.trim()
                            if (trimmed.isBlank()) {
                                viewModel.showInfo(t("Enter a domain to block."))
                            } else {
                                viewModel.addBlocklistDomain(trimmed)
                                viewModel.updateBlocklistQuery("")
                            }
                        }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(name = "add", size = 20.sp, color = colors.textSub)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (blocklist.isEmpty()) {
                    EmptyListItem(t("No blocked domains yet."))
                } else {
                    blocklist.forEach { domain ->
                        BlockItem(
                            domain = domain,
                            badge = if (domain.contains("*")) t("WILDCARD") else null,
                            onDelete = { viewModel.removeBlocklistDomain(domain) }
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.backgroundAlt)
                    .border(1.dp, colors.border)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { viewModel.updateBlocklistQuery(it) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = colors.textMain),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MaterialSymbol(name = "search", size = 16.sp, color = colors.textSub)
                            Box(modifier = Modifier.weight(1f)) {
                                if (query.isBlank()) {
                                    Text(t("Search rules..."), fontSize = 13.sp, color = colors.textMuted)
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BlockItem(domain: String, badge: String? = null, onDelete: () -> Unit) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.warning.copy(alpha = 0.1f))
                        .border(1.dp, colors.warning.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.warning)
                }
            }
        }
        MaterialSymbol(
            name = "delete",
            size = 18.sp,
            color = colors.danger,
            modifier = Modifier
                .clickable { onDelete() }
                .focusable()
        )
    }
}

@Composable
private fun EmptyListItem(text: String) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 12.sp, color = colors.textMuted)
    }
}

private fun allowlistIconFor(domain: String): String? {
    val normalized = domain.removePrefix("*.").lowercase()
    return when {
        normalized.contains("google") -> "assets/stitch/favicon-google.png"
        normalized.contains("slack") -> "assets/stitch/favicon-slack.png"
        normalized.contains("company") -> "assets/stitch/favicon-company.png"
        else -> null
    }
}
