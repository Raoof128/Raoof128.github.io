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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.ExportFormat
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.desktop.ui.gridPattern

@Composable
fun ReportsExportScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.reports()
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            ReportsSidebar(onNavigate = { viewModel.currentScreen = it })
            ReportsContent(viewModel = viewModel)
        }
    }
}

@Composable
private fun ReportsSidebar(onNavigate: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier
            .width(256.dp)
            .fillMaxHeight()
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF135BEC).copy(alpha = 0.1f))
                        .border(1.dp, Color(0xFF135BEC).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(name = "shield_lock", size = 20.sp, color = Color(0xFF135BEC))
                }
                Column {
                    Text("QR-SHIELD", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("Offline-First Detection", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
            SidebarLink("Dashboard", "dashboard", onNavigate, AppScreen.Dashboard)
            SidebarLink("Scans", "qr_code_scanner", onNavigate, AppScreen.LiveScan)
            SidebarLink("Reports", "description", onNavigate, AppScreen.ReportsExport, isActive = true)
            SidebarLink("Settings", "settings", onNavigate, AppScreen.TrustCentreAlt)
            SidebarLink("Profile", "account_circle", onNavigate, AppScreen.TrustCentreAlt)
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
            )
            Column {
                Text("Engine Online", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Text("v2.4.1 (Stable)", fontSize = 10.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun SidebarLink(label: String, icon: String, onNavigate: (AppScreen) -> Unit, target: AppScreen, isActive: Boolean = false) {
    val bg = if (isActive) Color(0xFF135BEC).copy(alpha = 0.05f) else Color.Transparent
    val border = if (isActive) Color(0xFF135BEC).copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isActive) Color(0xFF135BEC) else Color(0xFF64748B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onNavigate(target) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialSymbol(name = icon, size = 18.sp, color = textColor)
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun ReportsContent(viewModel: AppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .gridPattern(spacing = 40.dp, lineColor = Color(0xFFCBD5E1).copy(alpha = 0.3f), lineWidth = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.5f))
                .border(1.dp, Color(0xFFE2E8F0))
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Reports", fontSize = 14.sp, color = Color(0xFF64748B))
                    MaterialSymbol(name = "chevron_right", size = 12.sp, color = Color(0xFF94A3B8))
                    Text("Export", fontSize = 14.sp, color = Color(0xFF0F172A))
                }
                Text("Export Report", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Text(
                    "Configure output parameters for Scan ID #8821-X",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MaterialSymbol(name = "history", size = 16.sp, color = Color(0xFF64748B))
                        Text("Recent Exports", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
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
                Text("OUTPUT FORMAT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFE2E8F0))
                        .padding(4.dp)
                ) {
                    FormatOption(label = "Human PDF", icon = "picture_as_pdf", selected = viewModel.exportFormat == ExportFormat.Pdf, modifier = Modifier.weight(1f))
                    FormatOption(label = "JSON Object", icon = "data_object", selected = viewModel.exportFormat == ExportFormat.Json, modifier = Modifier.weight(1f))
                }

                Text("FILE SETTINGS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Filename", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(viewModel.exportFilename, fontSize = 14.sp, color = Color(0xFF0F172A))
                        Text(".pdf", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.align(Alignment.CenterEnd))
                    }
                }

                Text("DATA INCLUSIONS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0))
                ) {
                    InclusionRow("Threat Verdict Analysis", "Explainable AI breakdown of risk factors", viewModel.exportIncludeVerdict)
                    InclusionRow("Metadata & Geo-Location", "IP origin, timestamps, and server info", viewModel.exportIncludeMetadata)
                    InclusionRow("Raw Payload", "Decoded base64/hex content strings", viewModel.exportIncludeRawPayload)
                    InclusionRow("Engine Debug Logs", "Verbose output for technical auditing", viewModel.exportIncludeDebugLogs, isLast = true)
                }

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF135BEC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    MaterialSymbol(name = "download", size = 18.sp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Export Download", fontWeight = FontWeight.SemiBold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionButton(label = "Copy", icon = "content_copy", modifier = Modifier.weight(1f))
                    ActionButton(label = "Share", icon = "share", modifier = Modifier.weight(1f))
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                            Text("Live Preview", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MaterialSymbol(name = "zoom_out", size = 16.sp, color = Color(0xFF64748B))
                            Text("100%", fontSize = 12.sp, color = Color(0xFF64748B))
                            MaterialSymbol(name = "zoom_in", size = 16.sp, color = Color(0xFF64748B))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF1F5F9))
                            .padding(24.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(595.dp)
                                .fillMaxHeight()
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(48.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("THREAT ANALYSIS REPORT", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                        Text("GENERATED BY QR-SHIELD ENGINE v2.4", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("SCAN #8821-X", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text("OCT 24, 2023 • 14:32 UTC", fontSize = 10.sp, color = Color(0xFF64748B))
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFEE2E2),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                                ) {
                                    Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFECACA)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            MaterialSymbol(name = "block", size = 28.sp, color = Color(0xFFDC2626))
                                        }
                                        Column {
                                            Text("VERDICT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), letterSpacing = 1.sp)
                                            Text("HIGH RISK DETECTED", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                            Text("The scanned QR code redirects to a known phishing vector designed to harvest credentials.", fontSize = 12.sp, color = Color(0xFF374151))
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("TARGET URL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF1F5F9))
                                                .padding(8.dp)
                                        ) {
                                            Text("http://login-micros0ft.secure-auth.com/verify?id=992", fontSize = 12.sp, color = Color(0xFF2563EB))
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("SERVER LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(Color(0xFFE2E8F0)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("RU", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                            }
                                            Column {
                                                Text("Moscow, Russia", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                                Text("IP: 185.22.10.4", fontSize = 10.sp, color = Color(0xFF64748B))
                                            }
                                        }
                                    }
                                }
                                Column {
                                    Text("GEOLOCATION TRACE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .background(Color(0xFFF1F5F9))
                                            .border(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Image(
                                            painter = painterResource("assets/stitch/map-moscow.png"),
                                            contentDescription = "Map",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFDC2626)),
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
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color.White.copy(alpha = 0.9f))
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    MaterialSymbol(name = "check_circle", size = 14.sp, color = Color(0xFF10B981))
                                    Text("Preview Updated", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
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
private fun FormatOption(label: String, icon: String, selected: Boolean, modifier: Modifier = Modifier) {
    val bg = if (selected) Color.White else Color.Transparent
    val text = if (selected) Color(0xFF135BEC) else Color(0xFF64748B)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MaterialSymbol(name = icon, size = 18.sp, color = text)
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = text)
    }
}

@Composable
private fun InclusionRow(title: String, subtitle: String, checked: Boolean, isLast: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isLast) Color.Transparent else Color(0xFFE2E8F0))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (checked) Color(0xFF135BEC) else Color.Transparent)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    MaterialSymbol(name = "check", size = 14.sp, color = Color.White)
                }
            }
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, icon: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MaterialSymbol(name = icon, size = 18.sp, color = Color(0xFF64748B))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
        }
    }
}
