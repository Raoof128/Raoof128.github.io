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

@Composable
fun TrustCentreScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.trustCentre(isDark = viewModel.isDarkMode)
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.colors.background)
                .gridPattern(spacing = 40.dp, lineColor = tokens.colors.border, lineWidth = 1.dp)
        ) {
            AppSidebar(
                currentScreen = AppScreen.TrustCentre,
                onNavigate = { viewModel.currentScreen = it },
                language = viewModel.appLanguage
            )
            TrustCentreContent(
                viewModel = viewModel,
                onNavigate = { viewModel.currentScreen = it }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(t("Trust Centre & Privacy Controls"), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24292F))
            Text(t("Manage offline heuristics, data retention policies, and domain allowlists. All changes apply immediately."), fontSize = 16.sp, color = Color(0xFF57606A), modifier = Modifier.widthIn(max = 640.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFF2EA043).copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                MaterialSymbol(
                    name = "shield_lock",
                    size = 180.sp,
                    color = Color(0xFF2EA043).copy(alpha = 0.1f),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                            Text(t("AIR-GAPPED STATUS: ACTIVE"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2EA043), letterSpacing = 1.2.sp)
                        }
                        Text(t("Strict Offline Guarantee"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24292F))
                        Text(
                            t("QR-SHIELD operates entirely on your local hardware. No image data, scanned URLs, or telemetry are sent to the cloud for analysis."),
                            fontSize = 16.sp,
                            color = Color(0xFF57606A),
                            modifier = Modifier.widthIn(max = 520.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFD0D7DE)),
                        modifier = Modifier
                            .clickable { onNavigate(AppScreen.ReportsExport) }
                            .focusable()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MaterialSymbol(name = "description", size = 16.sp, color = Color(0xFF24292F))
                            Text(t("View Audit Log"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFD0D7DE))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MaterialSymbol(name = "tune", size = 20.sp, color = Color(0xFF135BEC))
                            Text(t("Heuristic Sensitivity"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF135BEC).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFF135BEC).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(modeLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF135BEC))
                        }
                    }
                    Box(modifier = Modifier.height(48.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFFE5E7EB))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF93C5FD), Color(0xFF135BEC))))
                        )
                        Column(
                            modifier = Modifier
                                .offset(x = 0.dp, y = 0.dp)
                                .clickable { viewModel.updateHeuristicSensitivity(HeuristicSensitivity.Low) }
                                .focusable(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (lowSelected) Color(0xFF135BEC) else Color(0xFFE5E7EB))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                            Text(t("Low"), fontSize = 12.sp, color = if (lowSelected) Color(0xFF24292F) else Color(0xFF57606A))
                        }
                        Column(
                            modifier = Modifier
                                .offset(x = 240.dp, y = (-4).dp)
                                .clickable { viewModel.updateHeuristicSensitivity(HeuristicSensitivity.Balanced) }
                                .focusable(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (balancedSelected) Color(0xFF135BEC) else Color(0xFFE5E7EB))
                                    .border(4.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                            Text(
                                t("Balanced"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (balancedSelected) Color(0xFF24292F) else Color(0xFF57606A)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .offset(x = 480.dp, y = 0.dp)
                                .clickable { viewModel.updateHeuristicSensitivity(HeuristicSensitivity.Paranoia) }
                                .focusable(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (paranoiaSelected) Color(0xFF135BEC) else Color(0xFFE5E7EB))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                            Text(t("Paranoia"), fontSize = 12.sp, color = if (paranoiaSelected) Color(0xFF24292F) else Color(0xFF57606A))
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFD0D7DE))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(t("Verdict Logic Explanation"), fontSize = 13.sp, color = Color(0xFF57606A), fontWeight = FontWeight.Medium)
                    Text(
                        t("Balanced Mode uses standard heuristic signatures. It flags known malicious patterns but allows common URL shorteners unless they redirect to suspicious TLDs."),
                        fontSize = 14.sp,
                        color = Color(0xFF24292F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(t("Engine v4.1.2 • Sig DB: 2023-10-27"), fontSize = 11.sp, color = Color(0xFF57606A))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ToggleCard(
                title = t("Strict Offline Mode"),
                subtitle = t("Force disable all network adapters for app"),
                enabled = viewModel.trustCentreToggles.strictOffline,
                activeColor = Color(0xFF2EA043),
                modifier = Modifier.weight(1f),
                onToggle = { viewModel.toggleStrictOffline() }
            )
            ToggleCard(
                title = t("Anonymous Telemetry"),
                subtitle = t("Share threat signatures to improve DB"),
                enabled = viewModel.trustCentreToggles.anonymousTelemetry,
                activeColor = Color(0xFF135BEC),
                modifier = Modifier.weight(1f),
                onToggle = { viewModel.toggleAnonymousTelemetry() }
            )
            ToggleCard(
                title = t("Auto-copy Safe Links"),
                subtitle = t("Copy to clipboard if Verdict is SAFE"),
                enabled = viewModel.trustCentreToggles.autoCopySafe,
                activeColor = Color(0xFF135BEC),
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD0D7DE))
    ) {
        Row(
            modifier = Modifier
                .clickable { onToggle() }
                .focusable()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF57606A))
            }
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (enabled) activeColor else Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(if (enabled) Alignment.CenterEnd else Alignment.CenterStart)
                        .offset(x = if (enabled) (-4).dp else 4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun AllowListCard(viewModel: AppViewModel, modifier: Modifier = Modifier, language: AppLanguage) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val allowlist = viewModel.filteredAllowlist()
    val query = viewModel.allowlistQuery
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD0D7DE))
    ) {
        Column(modifier = Modifier.height(400.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFD0D7DE))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialSymbol(name = "check_circle", size = 20.sp, color = Color(0xFF2EA043))
                    Text(t("Domain Allowlist"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
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
                    MaterialSymbol(name = "add", size = 20.sp, color = Color(0xFF57606A))
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
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFD0D7DE))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { viewModel.updateAllowlistQuery(it) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF24292F)),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MaterialSymbol(name = "search", size = 16.sp, color = Color(0xFF57606A))
                            Box(modifier = Modifier.weight(1f)) {
                                if (query.isBlank()) {
                                    Text(t("Search domains..."), fontSize = 13.sp, color = Color(0xFF94A3B8))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFD0D7DE)),
                contentAlignment = Alignment.Center
            ) {
                if (iconPath != null) {
                    Image(
                        painter = painterResource(iconPath),
                        contentDescription = domain,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        domain.trim().removePrefix("*.").take(1).uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF57606A)
                    )
                }
            }
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
        }
        MaterialSymbol(
            name = "delete",
            size = 18.sp,
            color = Color(0xFFCF222E),
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD0D7DE))
    ) {
        Column(modifier = Modifier.height(400.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFD0D7DE))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaterialSymbol(name = "block", size = 20.sp, color = Color(0xFFCF222E))
                    Text(t("Custom Blocklist"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
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
                    MaterialSymbol(name = "add", size = 20.sp, color = Color(0xFF57606A))
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
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFD0D7DE))
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { viewModel.updateBlocklistQuery(it) },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF24292F)),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MaterialSymbol(name = "search", size = 16.sp, color = Color(0xFF57606A))
                            Box(modifier = Modifier.weight(1f)) {
                                if (query.isBlank()) {
                                    Text(t("Search rules..."), fontSize = 13.sp, color = Color(0xFF94A3B8))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF24292F))
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFD29922).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFFD29922).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD29922))
                }
            }
        }
        MaterialSymbol(
            name = "delete",
            size = 18.sp,
            color = Color(0xFFCF222E),
            modifier = Modifier
                .clickable { onDelete() }
                .focusable()
        )
    }
}

@Composable
private fun EmptyListItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 12.sp, color = Color(0xFF94A3B8))
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
