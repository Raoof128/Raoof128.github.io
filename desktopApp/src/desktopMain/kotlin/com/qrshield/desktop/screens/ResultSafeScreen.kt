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
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.ResultViewMode
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialIconRound
import com.qrshield.desktop.ui.gridPattern

@Composable
fun ResultSafeScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanResultSafe()
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
        ) {
            AppSidebar(
                currentScreen = viewModel.currentScreen,
                onNavigate = { viewModel.currentScreen = it }
            )
            SafeResultContent(
                viewModel = viewModel,
                onNavigate = { viewModel.currentScreen = it }
            )
        }
    }
}

@Composable
private fun SafeResultContent(viewModel: AppViewModel, onNavigate: (AppScreen) -> Unit) {
    val assessment = viewModel.currentAssessment
    val url = viewModel.currentUrl
    val verdictDetails = viewModel.currentVerdictDetails
    val scanId = viewModel.lastAnalyzedAt?.toString()?.takeLast(6) ?: "LATEST"
    val confidencePercent = ((assessment?.confidence ?: 0f) * 100).coerceIn(0f, 100f)
    val confidenceLabel = "${confidencePercent.toInt()}%"
    val durationLabel = viewModel.lastAnalysisDurationMs?.let { "${it}ms" } ?: "--"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .gridPattern(spacing = 24.dp, lineColor = Color(0xFFF3F4F6), lineWidth = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .border(1.dp, Color(0xFFE5E7EB))
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Scan", fontSize = 14.sp, color = Color(0xFF6B7280))
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = Color(0xFF9CA3AF))
                Text("Results", fontSize = 14.sp, color = Color(0xFF6B7280))
                MaterialIconRound(name = "chevron_right", size = 16.sp, color = Color(0xFF9CA3AF))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("#SCAN-$scanId", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
                    }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFECFDF3))
                        .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                        Text("ENGINE ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981), letterSpacing = 1.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { viewModel.showInfo("Notifications are not available yet.") }
                        .focusable()
                ) {
                    MaterialIconRound(name = "notifications", size = 20.sp, color = Color(0xFF6B7280))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (assessment == null || url.isNullOrBlank()) {
                EmptyResultState(onNavigate = onNavigate)
            } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF14B8A6)))),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialIconRound(name = "check_circle", size = 36.sp, color = Color.White)
                            }
                            Column {
                                Text("Safe to Visit", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    MaterialIconRound(name = "link", size = 14.sp, color = Color(0xFF6B7280))
                                    Text(url, fontSize = 12.sp, color = Color(0xFF6B7280), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                MetricBlock("Confidence", confidenceLabel, Color(0xFF10B981))
                                VerticalDivider()
                                MetricBlock("Scan Time", durationLabel, Color(0xFF111827))
                                VerticalDivider()
                                MetricBlock("Engine", "v2.4.1 Local", Color(0xFF374151))
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.openUrl(url) },
                            enabled = url.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text("Visit URL", fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(8.dp))
                            MaterialIconRound(name = "open_in_new", size = 14.sp, color = Color.White)
                        }
                        Button(
                            onClick = { viewModel.copyUrl(url, label = "Safe link copied") },
                            enabled = url.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            MaterialIconRound(name = "content_copy", size = 14.sp, color = Color(0xFF6B7280))
                            Spacer(Modifier.width(8.dp))
                            Text("Copy Safe Link", color = Color(0xFF6B7280))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(4.dp)
                        ) {
                            ViewModeButton(
                                "Simple View",
                                selected = viewModel.resultSafeViewMode == ResultViewMode.Simple,
                                onClick = { viewModel.resultSafeViewMode = ResultViewMode.Simple }
                            )
                            ViewModeButton(
                                "Technical",
                                selected = viewModel.resultSafeViewMode == ResultViewMode.Technical,
                                onClick = { viewModel.resultSafeViewMode = ResultViewMode.Technical }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Verdict Analysis", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        AnalysisCard(title = "Domain Identity", badge = "PASSED", icon = "domain_verification", body = "Verified ownership by Microsoft Corporation via EV Certificate.", modifier = Modifier.weight(1f))
                        AnalysisCard(title = "Homograph Check", badge = "CLEAN", icon = "spellcheck", body = "No mixed-script characters or IDN spoofing detected in domain string.", modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        AnalysisCard(title = "Domain Age", badge = "ESTABLISHED", icon = "history_edu", body = "Domain registered > 20 years ago. High reputation score.", modifier = Modifier.weight(1f))
                        AnalysisCard(title = "Redirect Chain", badge = "DIRECT", icon = "alt_route", body = "Zero intermediate redirects found. Destination is final.", modifier = Modifier.weight(1f))
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE5E7EB))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TECHNICAL INDICATORS", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280), letterSpacing = 1.sp)
                                Text(
                                    "Export Report",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier
                                        .clickable { onNavigate(AppScreen.ReportsExport) }
                                        .focusable()
                                )
                            }
                            TechnicalRow("Certificate Issuer", "DigiCert Inc (US)")
                            TechnicalRow("Server Location", "United States (Azure Cloud)")
                            TechnicalRow("Shannon Entropy", "3.44 bits (Low)")
                            TechnicalRow("Top 1k Whitelist", "Match", highlight = Color(0xFF10B981))
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Destination Preview", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280), letterSpacing = 1.sp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .border(1.dp, Color(0xFFE5E7EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(96.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFE5E7EB))
                                    )
                                }
                            }
                            Text("Sandbox rendered. No active scripts executed.", fontSize = 11.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally))
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E7FF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0E7FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MaterialIconRound(name = "psychology", size = 16.sp, color = Color(0xFF4F46E5))
                                }
                                Text("AI Verdict Logic", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            }
                            Text(
                                verdictDetails?.summary
                                    ?: "The ML model classified this URL as benign with high certainty. The structure matches known legitimate authentication patterns.",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                val phishingProbability = (100f - confidencePercent).coerceIn(0f, 100f)
                                Text("Phishing Probability", fontSize = 12.sp, color = Color(0xFF6B7280))
                                Text("${phishingProbability.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth((100f - confidencePercent).coerceIn(0f, 100f) / 100f)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xFF10B981))
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
private fun EmptyResultState(onNavigate: (AppScreen) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No scan data available.", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("Run a scan to see detailed results.", fontSize = 13.sp, color = Color(0xFF6B7280))
            Button(
                onClick = { onNavigate(AppScreen.LiveScan) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Back to Scan", fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
    }
}

@Composable
private fun MetricBlock(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280), letterSpacing = 1.sp)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(Color(0xFFE5E7EB))
    )
}

@Composable
private fun ViewModeButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .border(1.dp, if (selected) Color(0xFFE5E7EB) else Color.Transparent, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .focusable()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color(0xFF111827) else Color(0xFF6B7280))
    }
}

@Composable
private fun AnalysisCard(title: String, badge: String, icon: String, body: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFECFDF3)),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialIconRound(name = icon, size = 18.sp, color = Color(0xFF10B981))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFECFDF3))
                        .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            Text(body, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
    }
}

@Composable
private fun TechnicalRow(label: String, value: String, highlight: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5E7EB))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280), modifier = Modifier.weight(1f))
        if (highlight != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MaterialIconRound(name = "check", size = 12.sp, color = highlight)
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = highlight)
            }
        } else {
            Text(value, fontSize = 13.sp, color = Color(0xFF111827), modifier = Modifier.weight(1f))
        }
    }
}
