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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.AppViewModel
import com.qrshield.desktop.TrainingInsightKind
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.ui.AppSidebar
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
            AppSidebar(currentScreen = AppScreen.Training, onNavigate = { viewModel.currentScreen = it })
            TrainingContent(viewModel = viewModel)
        }
    }
}

@Composable
private fun TrainingContent(viewModel: AppViewModel) {
    val training = viewModel.trainingState
    val scenario = viewModel.currentTrainingScenario
    val minutes = training.remainingSeconds / 60
    val seconds = training.remainingSeconds % 60
    val timeLabel = "%d:%02d remaining".format(minutes, seconds)
    val accuracyLabel = "${(training.accuracy * 100).toInt()}%"
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
                            Text("MODULE ${training.module}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF135BEC), letterSpacing = 1.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            MaterialSymbol(name = "timer", size = 14.sp, color = Color(0xFF64748B))
                            Text(timeLabel, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                        }
                    }
                    Text("Beat the Bot", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                    Text("Phishing Simulation · Round ${training.round} of ${training.totalRounds}", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(value = training.score.toString(), label = "Score")
                    StatCard(value = training.streak.toString(), label = "Streak", highlight = Color(0xFFF59E0B))
                    StatCard(value = accuracyLabel, label = "Accuracy", color = Color(0xFF10B981))
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
                            .fillMaxWidth(training.progress.coerceIn(0f, 1f))
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
                                    Text(
                                        "Enlarge",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF135BEC),
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .clickable { viewModel.showInfo("Zoom is not available yet.") }
                                            .focusable()
                                    )
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
                                                scenario.payload,
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
                                                Text(scenario.contextTitle, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                                Text(scenario.contextBody, fontSize = 12.sp, color = Color(0xFF64748B))
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
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.submitTrainingVerdict(isPhishing = true) }
                                    )
                                    TrainingActionButton(
                                        label = "Legitimate",
                                        subtitle = "Mark as safe",
                                        icon = "verified_user",
                                        bg = Color(0xFFD1FAE5),
                                        textColor = Color(0xFF059669),
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.submitTrainingVerdict(isPhishing = false) }
                                    )
                                }
                                Text(
                                    "Skip this round",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(top = 12.dp)
                                        .clickable { viewModel.skipTrainingRound() }
                                        .focusable()
                                )
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
                            val isSafeScenario = scenario.expectedVerdict == com.qrshield.model.Verdict.SAFE
                            val badgeBg = if (isSafeScenario) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                            val badgeBorder = if (isSafeScenario) Color(0xFFA7F3D0) else Color(0xFFFECACA)
                            val badgeTextColor = if (isSafeScenario) Color(0xFF059669) else Color(0xFFDC2626)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Analysis Report", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeBg)
                                        .border(1.dp, badgeBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isSafeScenario) "CLEAN" else "DETECTED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeTextColor)
                                }
                            }
                            scenario.insights.forEach { insight ->
                                val color = when (insight.kind) {
                                    TrainingInsightKind.Warning -> Color(0xFFDC2626)
                                    TrainingInsightKind.Suspicious -> Color(0xFFF59E0B)
                                    TrainingInsightKind.Psychology -> Color(0xFF3B82F6)
                                }
                                ReportItem(icon = insight.icon, color = color, title = insight.title, body = insight.body)
                            }
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
                                        Text("${(scenario.aiConfidence * 100).toInt()}%", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                        Text(if (isSafeScenario) "Benign" else "Malicious", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF135BEC))
                                    }
                                }
                            }
                            Button(
                                onClick = { viewModel.nextTrainingRound() },
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
private fun TrainingActionButton(
    label: String,
    subtitle: String,
    icon: String,
    bg: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .focusable()
                .padding(16.dp),
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
