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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.SampleData
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialIconRound

@Composable
fun ResultDangerousAltScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanResultDangerous(isDark = viewModel.isDarkMode)
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(if (viewModel.isDarkMode) Color(0xFF111827) else Color(0xFFF3F4F6))
        ) {
            AppSidebar(currentScreen = AppScreen.ResultDangerousAlt, onNavigate = { viewModel.currentScreen = it })
            DangerousAltContent(
                viewModel = viewModel,
                isDark = viewModel.isDarkMode,
                onNavigate = { viewModel.currentScreen = it }
            )
        }
    }
}

@Composable
private fun DangerousAltContent(viewModel: AppViewModel, isDark: Boolean, onNavigate: (AppScreen) -> Unit) {
    val assessment = viewModel.currentAssessment
    val url = viewModel.currentUrl
    val confidencePercent = ((assessment?.confidence ?: 0f) * 100).coerceIn(0f, 100f)
    val scanId = viewModel.lastAnalyzedAt?.toString()?.takeLast(6) ?: "LATEST"
    val bg = if (isDark) Color(0xFF111827) else Color(0xFFF3F4F6)
    val surface = if (isDark) Color(0xFF1F2937) else Color.White
    val border = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    val textMain = if (isDark) Color(0xFFF9FAFB) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val scanTimeLabel = viewModel.lastAnalyzedAt?.let { viewModel.formatTimestamp(it) } ?: "Unknown"
    val sourceLabel = url?.substringAfter("://")?.substringBefore("/")?.ifBlank { "Unknown" } ?: "Unknown"
    val userProfile = SampleData.userProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Home", fontSize = 12.sp, color = textMuted)
                Text("/", fontSize = 12.sp, color = textMuted)
                Text("Scans", fontSize = 12.sp, color = textMuted)
                Text("/", fontSize = 12.sp, color = textMuted)
                Text("Scan #SCAN-$scanId", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textMain)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(surface)
                        .border(1.dp, border, RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Text("Engine Active (Offline)", fontSize = 11.sp, color = textMuted)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { viewModel.showInfo("Notifications are not available yet.") }
                        .focusable(),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = "notifications", size = 18.sp, color = textMuted)
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(userProfile.initials, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }

        if (assessment == null || url.isNullOrBlank()) {
            EmptyAltResultState(onNavigate = onNavigate)
            return@Column
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = surface,
            border = BorderStroke(1.dp, border)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            MaterialIconRound(name = "gpp_bad", size = 28.sp, color = Color(0xFFEF4444))
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("HIGH RISK DETECTED", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textMain)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFEF4444))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("DANGEROUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Text(assessment.actionRecommendation, fontSize = 12.sp, color = textMuted)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Threat Confidence", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textMuted, letterSpacing = 1.sp)
                        Text("${confidencePercent.toInt()}%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge("Phishing Attempt", Color(0xFFFEE2E2), Color(0xFFEF4444), "bug_report")
                    Badge("Obfuscated Script", Color(0xFFFFEDD5), Color(0xFFF97316), "code_off")
                    Badge("Homograph Attack", Color(0xFFDBEAFE), Color(0xFF2563EB), "link")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Attack Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textMain)
                Surface(shape = RoundedCornerShape(12.dp), color = surface, border = BorderStroke(1.dp, Color(0xFFBFDBFE))) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDBEAFE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialIconRound(name = "abc", size = 18.sp, color = Color(0xFF2563EB))
                                }
                                Column {
                                    Text("Homograph / IDN Attack", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textMain)
                                    Text("Cyrillic characters mimicking Latin alphabet detected.", fontSize = 12.sp, color = textMuted)
                                }
                            }
                            MaterialIconRound(name = "expand_less", size = 18.sp, color = Color(0xFF2563EB))
                        }
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Visual Appearance", fontSize = 10.sp, color = textMuted)
                                    Text("secure-banking.com", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textMain)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Actual Punycode", fontSize = 10.sp, color = textMuted)
                                    Text("xn--secure-bankng-87b.com", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFEF4444))
                                }
                            }
                            Text("The domain uses the Cyrillic 'a' (U+0430) instead of Latin 'a' (U+0061). This technique is commonly used to trick users into believing they are visiting a legitimate service.", fontSize = 12.sp, color = textMuted, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
                ExpandableRow("Suspicious Redirect Chain", "3 hops detected involving known URL shorteners.", "call_split")
                ExpandableRow("Obfuscated JavaScript", "High entropy string detected in URL parameters.", "javascript")
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = RoundedCornerShape(12.dp), color = surface, border = BorderStroke(1.dp, border)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBEAFE)),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialIconRound(name = "thumb_up", size = 12.sp, color = Color(0xFF2563EB))
                            }
                            Text("Recommended Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textMain)
                        }
                        Button(
                            onClick = {
                                viewModel.addBlocklistDomain(viewModel.hostFromUrl(url) ?: url)
                                viewModel.shareTextReport()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            MaterialIconRound(name = "block", size = 16.sp, color = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text("Block & Report", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { viewModel.showInfo("Sandbox quarantine is not available on desktop yet.") },
                            colors = ButtonDefaults.buttonColors(containerColor = surface),
                            border = BorderStroke(1.dp, border),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            MaterialIconRound(name = "science", size = 16.sp, color = textMuted)
                            Spacer(Modifier.width(6.dp))
                            Text("Quarantine in Sandbox", color = textMain)
                        }
                        Text("Explainable Security", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textMuted, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
                        Bullet("Domain age is less than 24 hours.")
                        Bullet("Matched 3 signatures in local phishing DB.")
                        Bullet("Target IP is located in a high-risk ASN.")
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = surface, border = BorderStroke(1.dp, border)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Scan Meta", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textMuted, letterSpacing = 1.sp)
                        MetaRow("Scan Time", scanTimeLabel)
                        MetaRow("Source", sourceLabel)
                        MetaRow("Engine", "Offline Core v2.4")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyAltResultState(onNavigate: (AppScreen) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No scan data available.", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Run a scan to view dangerous results.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = { onNavigate(AppScreen.LiveScan) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Back to Scan", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun Badge(text: String, bg: Color, color: Color, icon: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, bg.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MaterialIconRound(name = icon, size = 14.sp, color = color)
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun ExpandableRow(title: String, subtitle: String, icon: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEDD5)),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = icon, size = 18.sp, color = Color(0xFFF97316))
                }
                Column {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            MaterialIconRound(name = "expand_more", size = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MaterialIconRound(name = "check_circle", size = 16.sp, color = Color(0xFFEF4444))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
