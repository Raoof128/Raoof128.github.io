package com.qrshield.desktop.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.DesktopScanState
import com.qrshield.desktop.MessageKind
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.LocalStitchTokens
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.desktop.ui.gridPattern
import com.qrshield.desktop.ui.cardSurface
import com.qrshield.desktop.ui.panelSurface
import com.qrshield.desktop.ui.iconContainer
import com.qrshield.desktop.ui.statusPill
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.Verdict
import com.qrshield.desktop.ui.handCursor
import com.qrshield.desktop.ui.ProfileDropdown
import com.qrshield.desktop.ui.EditProfileDialog
import com.qrshield.redteam.RedTeamScenarios
import androidx.compose.foundation.horizontalScroll
import java.io.File

@Composable
fun LiveScanScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanMonitor(isDark = viewModel.isDarkMode)
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
                language = viewModel.appLanguage,
                onProfileClick = { viewModel.toggleProfileDropdown() },
                onHelpClick = { viewModel.openHelpDialog() },
                userName = viewModel.userName,
                userRole = viewModel.userRole,
                userInitials = viewModel.userInitials
            )
            LiveScanContent(
                viewModel = viewModel,
                onNavigate = { viewModel.currentScreen = it },
                language = language
            )
            
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
private fun LiveScanContent(
    viewModel: AppViewModel,
    onNavigate: (AppScreen) -> Unit,
    language: AppLanguage
) {
    val tokens = LocalStitchTokens.current
    val colors = tokens.colors
    val scanState = viewModel.scanState
    val statusMessage = viewModel.statusMessage
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    
    // Drag and Drop state (parity with Web app scanner.js)
    var isDragging by remember { mutableStateOf(false) }
    
    val stateLabel = when (scanState) {
        DesktopScanState.Idle -> if (isDragging) t("DROP IMAGE HERE") else t("READY TO SCAN")
        DesktopScanState.Scanning -> t("SCANNING")
        is DesktopScanState.Analyzing -> t("ANALYZING")
        is DesktopScanState.Error -> t("ERROR")
        is DesktopScanState.Result -> t("SCAN COMPLETE")
    }
    val stateBody = when (scanState) {
        DesktopScanState.Idle -> if (isDragging) 
            t("Release to scan the dropped image for QR codes.")
        else 
            t("Drop an image here, upload a QR code, or paste a URL to analyze.")
        DesktopScanState.Scanning -> t("Processing your QR code image...")
        is DesktopScanState.Analyzing -> tf("Analyzing %s for threats.", scanState.url)
        is DesktopScanState.Error -> scanState.message
        is DesktopScanState.Result -> t("Scan complete. Review the result screen for details.")
    }
    val stateTitle = when (scanState) {
        DesktopScanState.Scanning -> t("Scanning in Progress")
        is DesktopScanState.Analyzing -> t("Analyzing URL")
        is DesktopScanState.Error -> t("Scan Error")
        is DesktopScanState.Result -> t("Scan Complete")
        DesktopScanState.Idle -> if (isDragging) t("Drop Image to Scan") else t("Upload QR Code")
    }
    val recentScans = viewModel.scanHistory.sortedByDescending { it.scannedAt }.take(5)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(2f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(t("Active Scanner"), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                            Text(t("Real-time QR code analysis and threat detection."), fontSize = 14.sp, color = colors.textSub)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(t("Mode:"), fontSize = 12.sp, color = colors.textSub)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.surface,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    MaterialSymbol(name = "wifi_off", size = 18.sp, color = colors.success)
                                    Text(t("Offline First"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textMain)
                                }
                            }
                            // Judge Demo Mode Toggle
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF2D1F1F),
                                border = BorderStroke(1.dp, Color(0xFF6B3A3A)),
                                modifier = Modifier.clickable { viewModel.toggleJudgeDemoMode() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🕵️", fontSize = 14.sp)
                                    Text(
                                        t("Judge Mode"),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (viewModel.isJudgeDemoModeEnabled) Color(0xFFFF6B6B) else Color(0xFFAB7878)
                                    )
                                    Text(
                                        if (viewModel.isJudgeDemoModeEnabled) "ON" else "OFF",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (viewModel.isJudgeDemoModeEnabled) Color(0xFFFF6B6B) else Color(0xFFAB7878)
                                    )
                                }
                            }
                        }
                    }
                    
                    // === RED TEAM SCENARIOS PANEL (Only visible when Judge Demo Mode is enabled) ===
                    if (viewModel.isJudgeDemoModeEnabled) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2D1F1F),
                            border = BorderStroke(1.dp, Color(0xFF6B3A3A))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("🕵️", fontSize = 16.sp)
                                        Text(
                                            t("Red Team Test Scenarios"),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFF6B6B)
                                        )
                                    }
                                    Text(
                                        "${RedTeamScenarios.SCENARIOS.size} attacks",
                                        fontSize = 12.sp,
                                        color = Color(0xFFAB7878)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RedTeamScenarios.SCENARIOS.forEach { scenario ->
                                        RedTeamChip(
                                            scenario = scenario,
                                            onClick = { viewModel.analyzeUrlDirectly(scenario.maliciousUrl) }
                                        )
                                    }
                                }
                                Text(
                                    t("Tap any scenario to bypass camera and test detection engine directly"),
                                    fontSize = 11.sp,
                                    color = Color(0xFFAB7878)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(480.dp)
                                .gridPattern(spacing = 24.dp, lineColor = colors.primary.copy(alpha = 0.08f), lineWidth = 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(Color.Transparent, colors.surface.copy(alpha = 0.8f))
                                        )
                                    )
                            )
                            // Status pill at top
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 24.dp)
                                    .statusPill(colors.surface.copy(alpha = 0.9f), colors.border)
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (scanState) {
                                                    is DesktopScanState.Error -> colors.danger
                                                    is DesktopScanState.Result -> colors.success
                                                    DesktopScanState.Scanning, is DesktopScanState.Analyzing -> colors.warning
                                                    DesktopScanState.Idle -> colors.primary
                                                }
                                            )
                                    )
                                    Text(stateLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMain, letterSpacing = 1.sp)
                                }
                            }
                            // Centered content - without the scanner frame
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.1f))
                                        .border(2.dp, colors.primary.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialSymbol(name = "upload_file", size = 40.sp, color = colors.primary)
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    stateTitle,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textMain,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    stateBody,
                                    fontSize = 14.sp,
                                    color = colors.textSub,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.widthIn(max = 340.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { viewModel.pickImageAndScan() },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    MaterialSymbol(name = "add_photo_alternate", size = 18.sp, color = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text(t("Upload Image"), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaterialSymbol(name = "bolt", size = 14.sp, color = colors.success)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            statusMessage?.text ?: t("Local analysis engine ready (<5ms latency)"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = when (statusMessage?.kind) {
                                MessageKind.Error -> colors.danger
                                MessageKind.Success -> colors.success
                                else -> colors.textSub
                            }
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ScanActionButton(
                                icon = "flash_on",
                                label = t("Torch"),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.showInfo(t("Torch not available on desktop")) },
                                colors = colors
                            )
                            DividerVertical(colors = colors)
                            ScanActionButton(
                                icon = "add_photo_alternate",
                                label = t("Upload Image"),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.pickImageAndScan() },
                                colors = colors
                            )
                            DividerVertical(colors = colors)
                            ScanActionButton(
                                icon = "link",
                                label = t("Paste URL"),
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.analyzeClipboardUrl() },
                                colors = colors
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(t("SYSTEM STATUS"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSub, letterSpacing = 1.sp)
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable { viewModel.showInfo(t("System status is always up-to-date")) }
                                        .focusable()
                                        .handCursor(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialSymbol(name = "refresh", size = 18.sp, color = colors.textMuted)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            StatusRow(
                                icon = "security",
                                iconColor = colors.success,
                                title = t("Detection Engine"),
                                value = t("Phishing Guard"),
                                badgeText = t("READY"),
                                badgeColor = colors.success,
                                colors = colors
                            )
                            Spacer(Modifier.height(12.dp))
                            StatusRow(
                                icon = "database",
                                iconColor = colors.primary,
                                title = t("Database"),
                                value = t("Local V.2.4.0"),
                                badgeText = t("LATEST"),
                                badgeColor = colors.primary,
                                colors = colors
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.backgroundAlt, RoundedCornerShape(8.dp))
                                    .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .cardSurface(colors.surface, colors.border, radius = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MaterialSymbol(name = "speed", size = 20.sp, color = colors.primary)
                                    }
                                    Column {
                                        Text(t("Latency"), fontSize = 12.sp, color = colors.textSub)
                                        Text(t("4ms Avg"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
                                    Box(modifier = Modifier.size(width = 4.dp, height = 8.dp).background(colors.success.copy(alpha = 0.6f), RoundedCornerShape(999.dp)))
                                    Box(modifier = Modifier.size(width = 4.dp, height = 12.dp).background(colors.success, RoundedCornerShape(999.dp)))
                                    Box(modifier = Modifier.size(width = 4.dp, height = 8.dp).background(colors.success.copy(alpha = 0.6f), RoundedCornerShape(999.dp)))
                                    Box(modifier = Modifier.size(width = 4.dp, height = 4.dp).background(colors.success.copy(alpha = 0.3f), RoundedCornerShape(999.dp)))
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxHeight(),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.fillMaxHeight()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.backgroundAlt)
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(t("RECENT SCANS"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textSub, letterSpacing = 1.sp)
                                Text(
                                    t("View All"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primary,
                                    modifier = Modifier
                                        .clickable { onNavigate(AppScreen.ScanHistory) }
                                        .focusable()
                                        .handCursor()
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (recentScans.isEmpty()) {
                                    EmptyRecentScanItem(
                                        title = t("No scans yet"),
                                        body = t("Run a scan to populate history."),
                                        colors = colors
                                    )
                                } else {
                                    recentScans.forEach { item ->
                                        RecentScanItem(
                                            item = item,
                                            timeLabel = viewModel.formatRelativeTime(item.scannedAt),
                                            onClick = { viewModel.selectHistoryItem(it) },
                                            language = language,
                                            colors = colors
                                        )
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colors.border)
                                    .clickable {
                                        viewModel.exportHistoryCsv()
                                        onNavigate(AppScreen.ReportsExport)
                                    }
                                    .focusable()
                                    .handCursor()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MaterialSymbol(name = "download", size = 18.sp, color = colors.textSub)
                                    Spacer(Modifier.width(8.dp))
                                    Text(t("Export Log"), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textSub)
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
private fun BoxScope.ScanFrame(modifier: Modifier = Modifier, colors: com.qrshield.desktop.theme.ColorTokens) {
    val transition = rememberInfiniteTransition()
    val scanOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier.size(256.dp)
    ) {
        // Main frame border (subtle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        )
        
        // Corner brackets using Canvas for clean L-shaped corners
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val strokeWidth = 3.dp.toPx()
            val cornerLength = 40.dp.toPx()
            val cornerRadius = 8.dp.toPx()
            val color = colors.primary
            
            // Top-left corner
            drawLine(
                color = color,
                start = Offset(cornerRadius, 0f),
                end = Offset(cornerLength, 0f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(0f, cornerRadius),
                end = Offset(0f, cornerLength),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(cornerRadius * 2, cornerRadius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            
            // Top-right corner
            drawLine(
                color = color,
                start = Offset(size.width - cornerLength, 0f),
                end = Offset(size.width - cornerRadius, 0f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(size.width, cornerRadius),
                end = Offset(size.width, cornerLength),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawArc(
                color = color,
                startAngle = 270f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width - cornerRadius * 2, 0f),
                size = androidx.compose.ui.geometry.Size(cornerRadius * 2, cornerRadius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            
            // Bottom-left corner
            drawLine(
                color = color,
                start = Offset(cornerRadius, size.height),
                end = Offset(cornerLength, size.height),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(0f, size.height - cornerLength),
                end = Offset(0f, size.height - cornerRadius),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawArc(
                color = color,
                startAngle = 90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(0f, size.height - cornerRadius * 2),
                size = androidx.compose.ui.geometry.Size(cornerRadius * 2, cornerRadius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            
            // Bottom-right corner
            drawLine(
                color = color,
                start = Offset(size.width - cornerLength, size.height),
                end = Offset(size.width - cornerRadius, size.height),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(size.width, size.height - cornerLength),
                end = Offset(size.width, size.height - cornerRadius),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(size.width - cornerRadius * 2, size.height - cornerRadius * 2),
                size = androidx.compose.ui.geometry.Size(cornerRadius * 2, cornerRadius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        
        // Scanning line animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .offset(y = (scanOffset * 240f).dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, colors.primary, Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun ScanActionButton(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: com.qrshield.desktop.theme.ColorTokens
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .focusable()
            .handCursor()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.backgroundAlt),
            contentAlignment = Alignment.Center
        ) {
            MaterialSymbol(name = icon, size = 20.sp, color = colors.textSub)
        }
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSub)
    }
}

@Composable
private fun DividerVertical(colors: com.qrshield.desktop.theme.ColorTokens) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(60.dp)
            .background(colors.backgroundAlt)
    )
}

@Composable
private fun StatusRow(
    icon: String,
    iconColor: Color,
    title: String,
    value: String,
    badgeText: String,
    badgeColor: Color,
    colors: com.qrshield.desktop.theme.ColorTokens
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundAlt, RoundedCornerShape(8.dp))
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .cardSurface(colors.surface, colors.border, radius = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                MaterialSymbol(name = icon, size = 20.sp, color = iconColor)
            }
            Column {
                Text(title, fontSize = 12.sp, color = colors.textSub)
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .border(1.dp, badgeColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
        }
    }
}

@Composable
private fun EmptyRecentScanItem(title: String, body: String, colors: com.qrshield.desktop.theme.ColorTokens) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .panelSurface(colors.backgroundAlt, colors.border)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.border),
            contentAlignment = Alignment.Center
        ) {
            MaterialSymbol(name = "history", size = 18.sp, color = colors.textMuted)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
            Text(body, fontSize = 12.sp, color = colors.textSub)
        }
    }
}

@Composable
private fun RecentScanItem(
    item: ScanHistoryItem,
    timeLabel: String,
    onClick: (ScanHistoryItem) -> Unit,
    language: AppLanguage,
    colors: com.qrshield.desktop.theme.ColorTokens
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val badge = when (item.verdict) {
        Verdict.SAFE -> t("SAFE")
        Verdict.SUSPICIOUS -> t("SUSPICIOUS")
        Verdict.MALICIOUS -> t("PHISHING")
        Verdict.UNKNOWN -> t("UNKNOWN")
    }
    val style = when (item.verdict) {
        Verdict.SAFE -> RecentScanStyle(
            icon = "check_circle",
            iconBg = colors.success.copy(alpha = 0.1f),
            iconColor = colors.success,
            badgeBg = colors.success.copy(alpha = 0.1f),
            badgeColor = colors.success,
            highlight = Color.Transparent
        )
        Verdict.SUSPICIOUS -> RecentScanStyle(
            icon = "priority_high",
            iconBg = colors.warning.copy(alpha = 0.1f),
            iconColor = colors.warning,
            badgeBg = colors.warning.copy(alpha = 0.1f),
            badgeColor = colors.warning.copy(alpha = 0.8f),
            highlight = colors.warning.copy(alpha = 0.05f)
        )
        Verdict.MALICIOUS -> RecentScanStyle(
            icon = "warning",
            iconBg = colors.surface,
            iconColor = colors.danger,
            badgeBg = colors.danger.copy(alpha = 0.1f),
            badgeColor = colors.danger,
            highlight = colors.danger.copy(alpha = 0.05f)
        )
        Verdict.UNKNOWN -> RecentScanStyle(
            icon = "help_outline",
            iconBg = colors.backgroundAlt,
            iconColor = colors.textMuted,
            badgeBg = colors.backgroundAlt,
            badgeColor = colors.textMuted,
            highlight = Color.Transparent
        )
    }
    val displayDomain = item.url.removePrefix("https://").removePrefix("http://").substringBefore("/")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(style.highlight)
            .border(1.dp, if (style.highlight == Color.Transparent) Color.Transparent else style.badgeColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable { onClick(item) }
            .focusable()
            .handCursor()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(style.iconBg)
                .border(1.dp, style.iconColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            MaterialSymbol(name = style.icon, size = 18.sp, color = style.iconColor)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayDomain,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (item.verdict == Verdict.MALICIOUS) colors.danger else colors.textMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    timeLabel,
                    fontSize = 12.sp,
                    color = if (item.verdict == Verdict.MALICIOUS) colors.danger else colors.textSub
                )
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(colors.border))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(style.badgeBg)
                        .border(1.dp, style.badgeColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = style.badgeColor, letterSpacing = 0.8.sp)
                }
            }
        }
        MaterialSymbol(
            name = if (item.verdict == Verdict.MALICIOUS) "arrow_forward" else "chevron_right",
            size = 18.sp,
            color = style.badgeColor.copy(alpha = 0.5f)
        )
    }
}

private data class RecentScanStyle(
    val icon: String,
    val iconBg: Color,
    val iconColor: Color,
    val badgeBg: Color,
    val badgeColor: Color,
    val highlight: Color
)


// === RED TEAM CHIP COMPOSABLE ===

@Composable
private fun RedTeamChip(
    scenario: RedTeamScenarios.Scenario,
    onClick: () -> Unit
) {
    val categoryColor = when {
        scenario.category.contains("Homograph", ignoreCase = true) -> Color(0xFFFF6B6B)
        scenario.category.contains("IP", ignoreCase = true) -> Color(0xFFFFAA00)
        scenario.category.contains("TLD", ignoreCase = true) -> Color(0xFFFF69B4)
        scenario.category.contains("Redirect", ignoreCase = true) -> Color(0xFFAA66FF)
        scenario.category.contains("Brand", ignoreCase = true) -> Color(0xFF6B66FF)
        scenario.category.contains("Shortener", ignoreCase = true) -> Color(0xFF00D4FF)
        scenario.category.contains("Safe", ignoreCase = true) -> Color(0xFF00D68F)
        else -> Color(0xFFFF5722)
    }
    val categoryIcon = when {
        scenario.category.contains("Homograph", ignoreCase = true) -> "🔤"
        scenario.category.contains("IP", ignoreCase = true) -> "🔢"
        scenario.category.contains("TLD", ignoreCase = true) -> "🌐"
        scenario.category.contains("Redirect", ignoreCase = true) -> "↪️"
        scenario.category.contains("Brand", ignoreCase = true) -> "🏷️"
        scenario.category.contains("Shortener", ignoreCase = true) -> "🔗"
        scenario.category.contains("Safe", ignoreCase = true) -> "✅"
        else -> "⚠️"
    }
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = categoryColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(categoryIcon, fontSize = 12.sp)
            Text(
                scenario.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = categoryColor,
                maxLines = 1
            )
        }
    }
}

