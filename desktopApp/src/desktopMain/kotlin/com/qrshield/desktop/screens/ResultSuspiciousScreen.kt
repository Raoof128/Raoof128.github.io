package com.qrshield.desktop.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.MaterialIcon
import com.qrshield.desktop.ui.gridPattern

@Composable
fun ResultSuspiciousScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.scanResultSuspicious(isDark = viewModel.isDarkMode)
    StitchTheme(tokens = tokens) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.colors.background)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                SuspiciousSidebar(isDark = viewModel.isDarkMode, onNavigate = { viewModel.currentScreen = it })
                SuspiciousContent(
                    isDark = viewModel.isDarkMode,
                    onToggleTheme = { viewModel.toggleTheme() },
                    onNavigate = { viewModel.currentScreen = it }
                )
            }
            ThemeToggleButton(isDark = viewModel.isDarkMode, onToggle = { viewModel.toggleTheme() })
        }
    }
}

@Composable
private fun SuspiciousSidebar(isDark: Boolean, onNavigate: (AppScreen) -> Unit) {
    val bg = if (isDark) Color.Black else Color.White
    val border = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    val textMain = if (isDark) Color(0xFFF9FAFB) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    Column(
        modifier = Modifier
            .width(256.dp)
            .fillMaxHeight()
            .background(bg)
            .border(1.dp, border)
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .border(1.dp, border)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MaterialIcon(name = "security", size = 20.sp, color = if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB))
            Text("QR-SHIELD", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textMain)
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("MAIN MENU", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textMuted, letterSpacing = 1.sp, modifier = Modifier.padding(start = 8.dp))
            NavLink("Dashboard", "dashboard", textMuted, onNavigate, AppScreen.Dashboard)
            NavLink("Scan Monitor", "qr_code_scanner", textMuted, onNavigate, AppScreen.ResultSuspicious, isActive = true)
            NavLink("Scan History", "history", textMuted, onNavigate, AppScreen.ScanHistory)
            NavLink("Safe List", "fact_check", textMuted, onNavigate, AppScreen.TrustCentre)
            Spacer(Modifier.height(16.dp))
            Text("SYSTEM", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textMuted, letterSpacing = 1.sp, modifier = Modifier.padding(start = 8.dp))
            NavLink("Settings", "settings", textMuted, onNavigate, AppScreen.TrustCentreAlt)
            NavLink("Support", "help_outline", textMuted, onNavigate, AppScreen.Training)
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)),
                contentAlignment = Alignment.Center
            ) {
                Text("JS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("John Smith", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textMain)
                Text("Security Analyst", fontSize = 12.sp, color = textMuted)
            }
        }
    }
}

@Composable
private fun NavLink(label: String, icon: String, textMuted: Color, onNavigate: (AppScreen) -> Unit, target: AppScreen, isActive: Boolean = false) {
    val bg = if (isActive) Color(0xFFDBEAFE).copy(alpha = 0.4f) else Color.Transparent
    val textColor = if (isActive) Color(0xFF2563EB) else textMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onNavigate(target) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialIcon(name = icon, size = 18.sp, color = textColor)
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun SuspiciousContent(
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onNavigate: (AppScreen) -> Unit
) {
    val background = if (isDark) Color(0xFF111827) else Color(0xFFF3F4F6)
    val surface = if (isDark) Color(0xFF1F2937) else Color.White
    val border = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    val textMain = if (isDark) Color(0xFFF9FAFB) else Color(0xFF111827)
    val textMuted = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .gridPattern(spacing = 40.dp, lineColor = Color(0xFF374151).copy(alpha = if (isDark) 0.1f else 0.05f), lineWidth = 1.dp)
        )
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = (-40).dp)
                .background(Color(0xFFF59E0B).copy(alpha = 0.05f), CircleShape)
                .blur(100.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(surface.copy(alpha = 0.8f))
                    .border(1.dp, border)
                    .padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Scan Monitor", fontSize = 14.sp, color = textMuted)
                    MaterialIcon(name = "chevron_right", size = 12.sp, color = textMuted)
                    Text("Result #SCAN-8824", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textMain)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFECFDF3).copy(alpha = if (isDark) 0.15f else 1f))
                            .border(1.dp, Color(0xFFD1FAE5), RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PulseDot(Color(0xFF10B981))
                            Text("Engine Active V.2.4", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF10B981))
                        }
                    }
                    Box(modifier = Modifier.size(28.dp)) {
                        MaterialIcon(name = "notifications", size = 18.sp, color = textMuted)
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(2.dp, surface, CircleShape)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = surface,
                    border = BorderStroke(1.dp, border)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xFFF59E0B).copy(alpha = 0.03f))
                        )
                        Row(modifier = Modifier.padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFDE68A)),
                                contentAlignment = Alignment.Center
                            ) {
                                MaterialIcon(name = "warning_amber", size = 28.sp, color = Color(0xFFF59E0B))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Caution Advised: Suspicious Activity Detected", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textMain)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(Color(0xFFFDE68A))
                                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(999.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("SUSPICIOUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), letterSpacing = 1.sp)
                                    }
                                }
                                Text(
                                    "The scanned QR code redirects to a domain with multiple heuristic anomalies. While not on a known blocklist, the structure exhibits patterns common in sophisticated phishing attempts. Proceed with extreme caution.",
                                    fontSize = 13.sp,
                                    color = textMuted,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    ActionButton("content_copy", "Copy URL (Sanitized)", Color.White, Color(0xFFE5E7EB), textMain)
                                    ActionButton("block", "Block Domain", Color(0xFFFEE2E2), Color(0xFFFECACA), Color(0xFFDC2626))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = {},
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                    ) {
                                        Text("Open in Sandbox", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                            color = surface,
                            border = BorderStroke(1.dp, border)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Target Destination", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textMuted, letterSpacing = 1.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("View Mode:", fontSize = 12.sp, color = textMuted)
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isDark) Color(0xFF374151) else Color(0xFFF3F4F6))
                                                .padding(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White)
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text("Visual", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = textMain)
                                            }
                                            Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                                                Text("Raw", fontSize = 12.sp, color = textMuted)
                                            }
                                        }
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0xFF111827) else Color(0xFFF8FAFC))
                                        .border(1.dp, border, RoundedCornerShape(8.dp))
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        MaterialIcon(name = "lock", size = 16.sp, color = Color(0xFF9CA3AF))
                                        Text("https://", fontSize = 13.sp, color = Color(0xFF6B7280))
                                        Text("secure-login", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626), modifier = Modifier.background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp))
                                        Text(".update-account-verification", fontSize = 13.sp, color = textMain)
                                        Text(".xyz", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
                                        Text("/auth/v2?id=8823", fontSize = 13.sp, color = Color(0xFF6B7280))
                                    }
                                }
                                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    IndicatorDot(Color(0xFFF97316), "Redirect Chain: 2 Hops", textMuted)
                                    IndicatorDot(Color(0xFFEF4444), "SSL Issuer: Let's Encrypt (Recent)", textMuted)
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = surface,
                            border = BorderStroke(1.dp, border)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Risk Score", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textMuted, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.Start))
                                RiskGauge(score = 72, color = Color(0xFFF59E0B))
                                Text("Score based on heuristic analysis of domain age, entropy, and keyword matching.", fontSize = 11.sp, color = textMuted, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    AlertCard("spellcheck", "Homograph Attack", "Detected", Color(0xFFDC2626), Color(0xFFFEE2E2), 0.9f, "Confidence", "90%", modifier = Modifier.weight(1f))
                    AlertCard("schedule", "Domain Age", "Suspicious", Color(0xFFF59E0B), Color(0xFFFFFBEB), 0.75f, "Age", "< 2 Days", modifier = Modifier.weight(1f))
                    AlertCard("shuffle", "URL Entropy", "Low Risk", Color(0xFFFBBF24), Color(0xFFF3F4F6), 0.4f, "Score", "4.2", modifier = Modifier.weight(1f))
                    AlertCard("verified_user", "Global Blocklist", "Clean", Color(0xFF10B981), Color(0xFFECFDF3), 0.05f, "Match", "0/52", modifier = Modifier.weight(1f))
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = surface,
                    border = BorderStroke(1.dp, border)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, border)
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Technical Indicators", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textMain)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Tag("DNS Records")
                                Tag("WhoIs")
                                Tag("Heuristics", active = true)
                            }
                        }
                        TableRow("Visual Similarity", "Simulates secure-login.com (Levenshtein Distance: 2)", "warning", Color(0xFFF59E0B))
                        TableRow("Redirect Method", "Javascript-based redirection (obfuscates destination from crawlers)", "error", Color(0xFFEF4444))
                        TableRow("Logo Detection", "Brand logo detected on landing page: Global Bank Ltd", "warning", Color(0xFFF59E0B))
                        TableRow("Scan Latency", "45ms (Local offline analysis)", "check_circle", Color(0xFF10B981))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isDark) Color(0xFF0B0B0B).copy(alpha = 0.2f) else Color(0xFFF8FAFC))
                                .border(1.dp, border)
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Analysis ID: #SCAN-8824-H99", fontSize = 12.sp, color = textMuted)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "View Full JSON Log",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier
                                        .clickable { onNavigate(AppScreen.ReportsExport) }
                                        .focusable()
                                )
                                MaterialIcon(name = "arrow_forward", size = 14.sp, color = Color(0xFFF59E0B))
                            }
                        }
                    }
                }
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
private fun ActionButton(icon: String, label: String, bg: Color, border: Color, text: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
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
private fun RiskGauge(score: Int, color: Color) {
    Box(modifier = Modifier.size(128.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            drawCircle(color = Color(0xFFE5E7EB), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * (score / 100f),
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(score.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Text("High Risk", fontSize = 12.sp, color = color)
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFDE68A).copy(alpha = 0.4f)),
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
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text("Domain uses Cyrillic characters to mimic legitimate Latin letters.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(999.dp))
                        .background(color)
                )
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(footerLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(footerValue, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Tag(label: String, active: Boolean = false) {
    val bg = if (active) Color(0xFFDBEAFE) else Color(0xFFF3F4F6)
    val text = if (active) Color(0xFF2563EB) else Color(0xFF6B7280)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(180.dp))
        Text(detail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        MaterialIcon(name = icon, size = 16.sp, color = color)
    }
}

@Composable
private fun BoxScope.ThemeToggleButton(isDark: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .align(Alignment.BottomEnd)
            .offset(x = (-24).dp, y = (-24).dp)
            .clip(CircleShape)
            .background(if (isDark) Color(0xFF1F2937) else Color.White)
            .border(1.dp, if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB))
            .clickable { onToggle() }
    ) {
        MaterialIcon(
            name = if (isDark) "light_mode" else "dark_mode",
            size = 20.sp,
            color = if (isDark) Color(0xFFFBBF24) else Color(0xFF111827),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
