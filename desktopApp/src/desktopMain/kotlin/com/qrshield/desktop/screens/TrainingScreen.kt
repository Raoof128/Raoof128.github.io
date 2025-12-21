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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.desktop.ui.gridPattern

@Composable
fun TrainingScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.training()
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            TrainingSidebar(onNavigate = { viewModel.currentScreen = it })
            TrainingContent()
        }
    }
}

@Composable
private fun TrainingSidebar(onNavigate: (AppScreen) -> Unit) {
    Column(
        modifier = Modifier
            .width(288.dp)
            .fillMaxHeight()
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MaterialSymbol(name = "qr_code_scanner", size = 28.sp, color = Color(0xFF135BEC))
                Text("QR-SHIELD", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
            Text("Offline-First Detection", fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(start = 28.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TrainingNavLink("Dashboard", "dashboard", onNavigate, AppScreen.Dashboard)
            TrainingNavLink("Scan", "center_focus_weak", onNavigate, AppScreen.LiveScan)
            TrainingNavLink("History", "history", onNavigate, AppScreen.ScanHistory)
            TrainingNavLink("Training", "school", onNavigate, AppScreen.Training, isActive = true)
            TrainingNavLink("Settings", "settings", onNavigate, AppScreen.TrustCentreAlt)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF1F5F9))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF135BEC), Color(0xFF9333EA)))),
                contentAlignment = Alignment.Center
            ) {
                Text("JD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column {
                Text("John Doe", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Text("Security Analyst L2", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun TrainingNavLink(label: String, icon: String, onNavigate: (AppScreen) -> Unit, target: AppScreen, isActive: Boolean = false) {
    val bg = if (isActive) Color(0xFFDBEAFE) else Color.Transparent
    val textColor = if (isActive) Color(0xFF135BEC) else Color(0xFF64748B)
    val border = if (isActive) Color(0xFFBFDBFE) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable { onNavigate(target) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialSymbol(name = icon, size = 20.sp, color = textColor)
        Text(label, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun TrainingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .gridPattern(spacing = 40.dp, lineColor = Color(0xFFE2E8F0), lineWidth = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFDBEAFE))
                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("MODULE 3", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF135BEC), letterSpacing = 1.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            MaterialSymbol(name = "timer", size = 14.sp, color = Color(0xFF64748B))
                            Text("12:05 remaining", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                        }
                    }
                    Text("Beat the Bot", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    Text("Phishing Simulation · Round 3 of 10", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(value = "1,250", label = "Score")
                    StatCard(value = "5", label = "Streak", highlight = Color(0xFFF59E0B))
                    StatCard(value = "92%", label = "Accuracy", color = Color(0xFF10B981))
                }
            }

            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text("Session Progress", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.3f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFF135BEC))
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0))
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MaterialSymbol(name = "visibility", size = 18.sp, color = Color(0xFF94A3B8))
                                    Text("Analyze the QR Code details", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DotIndicator()
                                    DotIndicator()
                                    DotIndicator()
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(192.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFE2E8F0))
                                            .padding(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource("assets/stitch/qr-example.png"),
                                            contentDescription = "QR Code",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Text("Enlarge", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF135BEC), modifier = Modifier.padding(top = 8.dp))
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Decoded Payload", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFF1F5F9))
                                                .border(1.dp, Color(0xFFE2E8F0))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                "https://secure-login.micros0ft-support.com/auth?client_id=19283",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF0F172A)
                                            )
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Context Source", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0xFFE2E8F0))
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFDBEAFE))
                                                    .border(1.dp, Color(0xFFBFDBFE), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                MaterialSymbol(name = "coffee", size = 20.sp, color = Color(0xFF135BEC))
                                            }
                                            Column {
                                                Text("Physical Flyer", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                                Text("Found this on a table at Starbeans Coffee. It offered a free coffee coupon if I logged in.", fontSize = 12.sp, color = Color(0xFF64748B))
                                            }
                                        }
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8FAFC))
                                    .border(1.dp, Color(0xFFE2E8F0))
                                    .padding(24.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    TrainingActionButton(
                                        label = "Phishing",
                                        subtitle = "Flag as malicious",
                                        icon = "gpp_bad",
                                        bg = Color(0xFFFEE2E2),
                                        textColor = Color(0xFFDC2626),
                                        modifier = Modifier.weight(1f)
                                    )
                                    TrainingActionButton(
                                        label = "Legitimate",
                                        subtitle = "Mark as safe",
                                        icon = "verified_user",
                                        bg = Color(0xFFD1FAE5),
                                        textColor = Color(0xFF059669),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Text("Skip this round", fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp))
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Analysis Report", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("DETECTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                }
                            }
                            ReportItem(icon = "warning", color = Color(0xFFDC2626), title = "Typosquatting Detected", body = "The domain micros0ft-support.com uses a zero '0' instead of the letter 'o'. This is a common tactic to impersonate legitimate brands.")
                            ReportItem(icon = "public_off", color = Color(0xFFF59E0B), title = "Suspicious TLD", body = "While .com is standard, the hyphenated structure combined with \"support\" is often used in phishing campaigns.")
                            ReportItem(icon = "psychology", color = Color(0xFF3B82F6), title = "Social Engineering", body = "The context of a \"free coupon\" creates urgency and incentive, bypassing critical thinking.")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFDBEAFE))
                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("AI Confidence Score", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF135BEC))
                                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("99.8%", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                        Text("Malicious", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF135BEC))
                                    }
                                }
                            }
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF135BEC)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text("Next Round", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                MaterialSymbol(name = "arrow_forward", size = 18.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, highlight: Color? = null, color: Color = Color(0xFF0F172A)) {
    val bg = highlight?.copy(alpha = 0.1f) ?: Color.White
    val border = highlight?.copy(alpha = 0.2f) ?: Color(0xFFE2E8F0)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(min = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = highlight ?: color)
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = highlight?.copy(alpha = 0.7f) ?: Color(0xFF64748B), letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun DotIndicator() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(Color(0xFFE2E8F0))
            .border(1.dp, Color(0xFFCBD5E1), CircleShape)
    )
}

@Composable
private fun TrainingActionButton(label: String, subtitle: String, icon: String, bg: Color, textColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MaterialSymbol(name = icon, size = 28.sp, color = textColor)
            Column {
                Text(label.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor, letterSpacing = 1.sp)
                Text(subtitle, fontSize = 12.sp, color = textColor.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun ReportItem(icon: String, color: Color, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MaterialSymbol(name = icon, size = 20.sp, color = color)
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Text(body, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}
