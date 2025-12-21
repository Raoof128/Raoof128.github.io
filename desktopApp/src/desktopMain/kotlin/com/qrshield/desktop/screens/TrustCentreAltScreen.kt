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
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
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
            TrustCentreAltSidebar(
                isDark = viewModel.isDarkMode,
                onNavigate = { viewModel.currentScreen = it }
            )
            TrustCentreAltContent(viewModel = viewModel)
        }
    }
}

@Composable
private fun TrustCentreAltSidebar(isDark: Boolean, onNavigate: (AppScreen) -> Unit) {
    val bg = if (isDark) Color(0xFF1E293B) else Color.White
    val border = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textSub = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .width(288.dp)
            .fillMaxHeight()
            .background(bg)
            .border(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                MaterialIconRound(name = "security", size = 20.sp, color = Color.White)
            }
            Column {
                Text("QR-SHIELD", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A))
                Text("Offline Protection", fontSize = 12.sp, color = textSub)
            }
        }
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AltSidebarLink("Dashboard", "dashboard", textSub, onNavigate, AppScreen.Dashboard)
            AltSidebarLink("Scan History", "history", textSub, onNavigate, AppScreen.ScanHistory)
            AltSidebarLink("Threat Database", "storage", textSub, onNavigate, AppScreen.ReportsExport)
            AltSidebarLink("Settings", "settings", textSub, onNavigate, AppScreen.TrustCentreAlt, isActive = true)
        }
        Spacer(modifier = Modifier.weight(1f))
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isDark) Color(0xFF334155) else Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, border)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("v2.4.1", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A))
                    Text("Latest Build", fontSize = 12.sp, color = textSub)
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
            }
        }
    }
}

@Composable
private fun AltSidebarLink(
    label: String,
    icon: String,
    textColor: Color,
    onNavigate: (AppScreen) -> Unit,
    target: AppScreen,
    isActive: Boolean = false
) {
    val bg = if (isActive) Color(0xFFDBEAFE) else Color.Transparent
    val activeText = if (isActive) Color(0xFF2563EB) else textColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onNavigate(target) }
            .focusable()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialIconRound(name = icon, size = 20.sp, color = activeText)
        Text(label, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium, color = activeText)
    }
}

@Composable
private fun TrustCentreAltContent(viewModel: AppViewModel) {
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
                Text("Settings", fontSize = 14.sp, color = textSub)
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 8.dp))
                Text("Onboarding", fontSize = 14.sp, color = textSub)
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(horizontal = 8.dp))
                Text("Offline Privacy", fontSize = 14.sp, color = textMain, fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                        .clickable { viewModel.showInfo("Help is not available yet.") }
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
                        .clickable { viewModel.showInfo("Profile settings are not available yet.") }
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
                    text = "Analysed offline.\nYour data stays on-device.",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = textMain,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "QR-SHIELD processes every scan within a secure, isolated local sandbox. We prioritize explainable security with zero cloud telemetry for image analysis.",
                    fontSize = 16.sp,
                    color = textSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp).widthIn(max = 640.dp)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard(icon = "science", title = "Local Sandbox", body = "Code execution happens in an ephemeral container. Malicious payloads never touch your OS kernel.", modifier = Modifier.weight(1f))
                InfoCard(icon = "cloud_off", title = "No Cloud Logs", body = "We strictly disable outgoing telemetry for scans. Scan results and image hashes remain local.", modifier = Modifier.weight(1f))
                InfoCard(icon = "storage", title = "On-Device DB", body = "The entire threat signature database is downloaded to your device for millisecond lookups.", modifier = Modifier.weight(1f))
            }

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
                            Text("DATA LIFECYCLE VERIFICATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textMain, letterSpacing = 0.8.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("SECURITY AUDIT: PASS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                    Column {
                        DataRow("Raw Image Buffer", "Local Memory (RAM)")
                        DataRow("Decoded URL/Payload", "Isolated Sandbox")
                        DataRow("Threat Verdict", "Local Database")
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
private fun DataRow(label: String, env: String) {
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
        Text("None", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
    }
}
