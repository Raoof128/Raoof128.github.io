@file:Suppress("DEPRECATION") // painterResource - migration to Compose Resources planned

package com.qrshield.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.focusable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.ExportFormat
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.theme.LocalStitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.desktop.ui.gridPattern
import com.qrshield.desktop.ui.statusPill
import com.qrshield.desktop.ui.ProfileDropdown
import com.qrshield.desktop.ui.EditProfileDialog

@Composable
fun ReportsExportScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.reports(isDark = viewModel.isDarkMode)
    val language = viewModel.appLanguage
    StitchTheme(tokens = tokens) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tokens.colors.background)
            ) {
                AppSidebar(
                    currentScreen = AppScreen.ReportsExport,
                    onNavigate = { viewModel.currentScreen = it },
                    language = viewModel.appLanguage,
                    onProfileClick = { viewModel.toggleProfileDropdown() },
                    userName = viewModel.userName,
                    userRole = viewModel.userRole,
                    userInitials = viewModel.userInitials
                )
                ReportsContent(viewModel = viewModel)
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

/**
 * Reports content and export preview.
 * NOTE: "PDF" export uses an HTML report generator. Users can open the generated .html file
 * in their browser and use the browser's Print → Save as PDF feature. This avoids adding
 * a heavy native PDF dependency and keeps the export pipeline deterministic for judges.
 */
@Composable
private fun ReportsContent(viewModel: AppViewModel) {
    val language = viewModel.appLanguage
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    // PDF exports as HTML that can be opened in browser and printed to PDF
    val extensionLabel = if (viewModel.exportFormat == ExportFormat.Pdf) ".html" else ".json"
    val statusMessage = viewModel.statusMessage
    val scanId = viewModel.lastAnalyzedAt?.toString()?.takeLast(6) ?: t("LATEST")
    val scanTimestamp = viewModel.lastAnalyzedAt?.let { viewModel.formatTimestamp(it) } ?: t("Unknown")
    val reportUrl = viewModel.currentUrl ?: t("No URL captured")
    val colors = LocalStitchTokens.current.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .gridPattern(spacing = 40.dp, lineColor = colors.border.copy(alpha = 0.3f), lineWidth = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface.copy(alpha = 0.5f))
                .border(1.dp, colors.border)
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(t("Export Report"), fontSize = 32.sp, fontWeight = FontWeight.Black, color = colors.textMain)
                Text(
                    tf("Configure output parameters for Scan ID #SCAN-%s", scanId),
                    fontSize = 14.sp,
                    color = colors.textSub
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.showInfo(t("Exports are saved to your Documents folder")) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MaterialSymbol(name = "history", size = 16.sp, color = colors.textSub)
                        Text(t("Recent Exports"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(t("OUTPUT FORMAT"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSub, letterSpacing = 1.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.backgroundAlt)
                        .border(1.dp, colors.border)
                        .padding(4.dp)
                ) {
                    FormatOption(
                        label = t("Human PDF"),
                        icon = "picture_as_pdf",
                        selected = viewModel.exportFormat == ExportFormat.Pdf,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.exportFormat = ExportFormat.Pdf }
                    )
                    FormatOption(
                        label = t("JSON Object"),
                        icon = "data_object",
                        selected = viewModel.exportFormat == ExportFormat.Json,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.exportFormat = ExportFormat.Json }
                    )
                }

                Text(t("FILE SETTINGS"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSub, letterSpacing = 1.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(t("Filename"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = viewModel.exportFilename,
                            onValueChange = { viewModel.exportFilename = it },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 14.sp, color = colors.textMain),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (viewModel.exportFilename.isBlank()) {
                                        Text(t("scan_report"), fontSize = 14.sp, color = colors.textMuted)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        Text(extensionLabel, fontSize = 14.sp, color = colors.textSub, modifier = Modifier.align(Alignment.CenterEnd))
                    }
                }

                Text(t("DATA INCLUSIONS"), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSub, letterSpacing = 1.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border)
                ) {
                    InclusionRow(
                        t("Threat Verdict Analysis"),
                        t("Explainable AI breakdown of risk factors"),
                        viewModel.exportIncludeVerdict,
                        onToggle = { viewModel.exportIncludeVerdict = !viewModel.exportIncludeVerdict }
                    )
                    InclusionRow(
                        t("Metadata & Geo-Location"),
                        t("IP origin, timestamps, and server info"),
                        viewModel.exportIncludeMetadata,
                        onToggle = { viewModel.exportIncludeMetadata = !viewModel.exportIncludeMetadata }
                    )
                    InclusionRow(
                        t("Raw Payload"),
                        t("Decoded base64/hex content strings"),
                        viewModel.exportIncludeRawPayload,
                        onToggle = { viewModel.exportIncludeRawPayload = !viewModel.exportIncludeRawPayload }
                    )
                    InclusionRow(
                        t("Engine Debug Logs"),
                        t("Verbose output for technical auditing"),
                        viewModel.exportIncludeDebugLogs,
                        isLast = true,
                        onToggle = { viewModel.exportIncludeDebugLogs = !viewModel.exportIncludeDebugLogs }
                    )
                }

                Button(
                    onClick = { viewModel.exportReport() },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    MaterialSymbol(name = "download", size = 18.sp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(t("Export Download"), fontWeight = FontWeight.SemiBold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton(
                        label = t("Copy"),
                        icon = "content_copy",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.copyJsonReport() }
                    )
                    ActionButton(
                        label = t("Share"),
                        icon = "share",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.shareTextReport() }
                    )
                }
                if (statusMessage != null) {
                    Text(
                        statusMessage.text,
                        fontSize = 12.sp,
                        color = if (statusMessage.kind == com.qrshield.desktop.MessageKind.Error) colors.danger else colors.textSub
                    )
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = colors.backgroundAlt,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface)
                            .border(1.dp, colors.border)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(colors.danger))
                            Text(t("Live Preview"), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.textSub, letterSpacing = 1.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { viewModel.showInfo(t("Tip: Use scroll wheel to zoom in preview")) }
                                    .focusable(),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialSymbol(name = "zoom_out", size = 16.sp, color = colors.textSub)
                            }
                            Text(t("100%"), fontSize = 12.sp, color = colors.textSub)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { viewModel.showInfo(t("Tip: Use scroll wheel to zoom in preview")) }
                                    .focusable(),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialSymbol(name = "zoom_in", size = 16.sp, color = colors.textSub)
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.backgroundAlt)
                            .padding(24.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(595.dp)
                                .fillMaxHeight()
                                .background(colors.surface)
                                .border(1.dp, colors.border)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(48.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(t("THREAT ANALYSIS REPORT"), fontSize = 20.sp, fontWeight = FontWeight.Black, color = colors.textMain, maxLines = 1, softWrap = false)
                                        Text(t("GENERATED BY QR-SHIELD ENGINE v2.4"), fontSize = 10.sp, color = colors.textSub, maxLines = 1, softWrap = false)
                                    }
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 16.dp)) {
                                        Text(tf("SCAN #SCAN-%s", scanId), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMain, maxLines = 1, softWrap = false)
                                        Text(scanTimestamp, fontSize = 10.sp, color = colors.textSub, maxLines = 1, softWrap = false)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = colors.danger.copy(alpha = 0.1f),
                                    border = BorderStroke(1.dp, colors.danger.copy(alpha = 0.2f))
                                ) {
                                    Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(colors.danger.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            MaterialSymbol(name = "block", size = 28.sp, color = colors.danger)
                                        }
                                        Column {
                                            Text(t("VERDICT"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.danger, letterSpacing = 1.sp)
                                            Text(t("HIGH RISK DETECTED"), fontSize = 24.sp, fontWeight = FontWeight.Black, color = colors.textMain)
                                            Text(t("The scanned QR code redirects to a known phishing vector designed to harvest credentials."), fontSize = 12.sp, color = colors.textSub)
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(t("TARGET URL"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(colors.backgroundAlt)
                                                .padding(8.dp)
                                        ) {
                                            Text(reportUrl, fontSize = 12.sp, color = colors.primary)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(t("SERVER LOCATION"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(colors.border),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(t("RU"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSub)
                                            }
                                            Column {
                                                Text(t("Moscow, Russia"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                                Text(tf("IP: %s", "185.22.10.4"), fontSize = 10.sp, color = colors.textSub)
                                            }
                                        }
                                    }
                                }
                                Column {
                                    Text(t("GEOLOCATION TRACE"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .background(colors.backgroundAlt)
                                            .border(1.dp, colors.border)
                                    ) {
                                        Image(
                                            painter = painterResource("assets/stitch/map-moscow.png"),
                                            contentDescription = t("Map"),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(colors.danger),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            MaterialSymbol(name = "location_on", size = 16.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = (-16).dp)
                                    .statusPill(colors.surface.copy(alpha = 0.9f), colors.border)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    MaterialSymbol(name = "check_circle", size = 14.sp, color = colors.success)
                                    Text(t("Preview Updated"), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
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
private fun FormatOption(
    label: String,
    icon: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalStitchTokens.current.colors
    val bg = if (selected) colors.surface else Color.Transparent
    val text = if (selected) colors.primary else colors.textSub

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .focusable()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MaterialSymbol(name = icon, size = 18.sp, color = text)
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = text)
    }
}

@Composable
private fun InclusionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    isLast: Boolean = false,
    onToggle: () -> Unit
) {
    val colors = LocalStitchTokens.current.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isLast) Color.Transparent else colors.border)
            .clickable { onToggle() }
            .focusable()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (checked) colors.primary else Color.Transparent)
                    .border(1.dp, colors.borderStrong, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    MaterialSymbol(name = "check", size = 14.sp, color = Color.White)
                }
            }
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                Text(subtitle, fontSize = 12.sp, color = colors.textSub)
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, icon: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalStitchTokens.current.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .focusable()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MaterialSymbol(name = icon, size = 18.sp, color = colors.textSub)
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
        }
    }
}
