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
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.theme.StitchTheme
import com.qrshield.desktop.theme.StitchTokens
import com.qrshield.desktop.theme.LocalStitchTokens
import com.qrshield.desktop.ui.AppSidebar
import com.qrshield.desktop.ui.MaterialSymbol
import com.qrshield.desktop.ui.gridPattern
import com.qrshield.desktop.ui.panelSurface
import com.qrshield.desktop.ui.cardSurface

@Composable
fun TrainingScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.training(isDark = viewModel.isDarkMode)
    StitchTheme(tokens = tokens) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(tokens.colors.background)
        ) {
            AppSidebar(
                currentScreen = AppScreen.Training,
                onNavigate = { viewModel.currentScreen = it },
                language = viewModel.appLanguage
            )
            TrainingContent(viewModel = viewModel)
        }
    }
}

@Composable
private fun TrainingContent(viewModel: AppViewModel) {
    val language = viewModel.appLanguage
    val t = { text: String -> DesktopStrings.translate(text, language) }
    fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, language, *args)
    val training = viewModel.trainingState
    val scenario = viewModel.currentTrainingScenario
    val minutes = training.remainingSeconds / 60
    val seconds = training.remainingSeconds % 60
    val timeLabel = tf("%d:%02d remaining", minutes, seconds)
    val accuracyLabel = "${(training.accuracy * 100).toInt()}%"
    val colors = LocalStitchTokens.current.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .gridPattern(spacing = 40.dp, lineColor = colors.border, lineWidth = 1.dp)
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
                                .background(colors.primary.copy(alpha = 0.1f))
                                .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(tf("MODULE %d", training.module), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primary, letterSpacing = 1.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            MaterialSymbol(name = "timer", size = 14.sp, color = colors.textSub)
                            Text(timeLabel, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.textSub)
                        }
                    }
                    Text(t("Beat the Bot"), fontSize = 40.sp, fontWeight = FontWeight.Black, color = colors.textMain)
                    Text(tf("Phishing Simulation · Round %d of %d", training.round, training.totalRounds), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colors.textSub)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(value = training.score.toString(), label = t("Score"))
                    StatCard(value = training.streak.toString(), label = t("Streak"), highlight = colors.warning)
                    StatCard(value = accuracyLabel, label = t("Accuracy"), color = colors.success)
                }
            }

            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(t("Session Progress"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 1.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.backgroundAlt)
                        .border(1.dp, colors.border, RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(training.progress.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.primary)
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
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.backgroundAlt)
                                    .border(1.dp, colors.border)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    MaterialSymbol(name = "visibility", size = 18.sp, color = colors.textMuted)
                                    Text(t("Analyze the QR Code details"), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textSub)
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
                                            .cardSurface(colors.surface, colors.border, radius = 8.dp)
                                            .padding(8.dp)
                                    ) {
                                        Image(
                                            painter = painterResource("assets/stitch/qr-example.png"),
                                            contentDescription = t("QR Code"),
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Text(
                                        t("Enlarge"),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary,
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .clickable { viewModel.showInfo(t("Zoom is not available yet.")) }
                                            .focusable()
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(t("Browser Preview"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 1.sp)
                                        // Browser chrome wrapper
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = colors.backgroundAlt,
                                            border = BorderStroke(1.dp, colors.border)
                                        ) {
                                            Column {
                                                // Browser title bar with window dots
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(colors.surface)
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    // Traffic light dots
                                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(t("Browser Preview"), fontSize = 11.sp, color = colors.textMuted)
                                                }
                                                // URL address bar
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(colors.surface)
                                                        .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    MaterialSymbol(
                                                        name = if (scenario.payload.startsWith("https")) "lock" else "lock_open",
                                                        size = 14.sp,
                                                        color = if (scenario.payload.startsWith("https")) colors.success else colors.warning
                                                    )
                                                    Text(
                                                        scenario.payload,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = colors.textMain
                                                    )
                                                }
                                                // Message context (simulating page content)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(colors.surface)
                                                        .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                                        .padding(12.dp)
                                                ) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                        MaterialSymbol(name = "sms", size = 20.sp, color = colors.textMuted)
                                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text(t("Incoming Message"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 0.5.sp)
                                                            Text(t(scenario.contextBody), fontSize = 12.sp, color = colors.textSub)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(t("Context Source"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, letterSpacing = 1.sp)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .cardSurface(colors.surface, colors.border, radius = 8.dp)
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.primary.copy(alpha = 0.1f))
                                                    .border(1.dp, colors.primary.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                MaterialSymbol(name = "coffee", size = 20.sp, color = colors.primary)
                                            }
                                            Column {
                                                Text(t(scenario.contextTitle), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
                                                Text(t(scenario.contextBody), fontSize = 12.sp, color = colors.textSub)
                                            }
                                        }
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.backgroundAlt)
                                    .border(1.dp, colors.border)
                                    .padding(24.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    TrainingActionButton(
                                        label = t("Phishing"),
                                        subtitle = t("Flag as malicious"),
                                        icon = "gpp_bad",
                                        bg = colors.danger.copy(alpha = 0.1f),
                                        textColor = colors.danger,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.submitTrainingVerdict(isPhishing = true) }
                                    )
                                    TrainingActionButton(
                                        label = t("Legitimate"),
                                        subtitle = t("Mark as safe"),
                                        icon = "verified_user",
                                        bg = colors.success.copy(alpha = 0.1f),
                                        textColor = colors.success,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.submitTrainingVerdict(isPhishing = false) }
                                    )
                                }
                                Text(
                                    t("Skip this round"),
                                    fontSize = 12.sp,
                                    color = colors.textMuted,
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
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            val isSafeScenario = scenario.expectedVerdict == com.qrshield.model.Verdict.SAFE
                            val badgeBg = if (isSafeScenario) colors.success.copy(alpha = 0.1f) else colors.danger.copy(alpha = 0.1f)
                            val badgeBorder = if (isSafeScenario) colors.success.copy(alpha = 0.2f) else colors.danger.copy(alpha = 0.2f)
                            val badgeTextColor = if (isSafeScenario) colors.success else colors.danger
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(t("Analysis Report"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textMain)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeBg)
                                        .border(1.dp, badgeBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(if (isSafeScenario) t("CLEAN") else t("DETECTED"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeTextColor)
                                }
                            }
                            scenario.insights.forEach { insight ->
                                val color = when (insight.kind) {
                                    TrainingInsightKind.Warning -> colors.danger
                                    TrainingInsightKind.Suspicious -> colors.warning
                                    TrainingInsightKind.Psychology -> colors.primary
                                }
                                ReportItem(icon = insight.icon, color = color, title = t(insight.title), body = t(insight.body))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colors.primary.copy(alpha = 0.1f))
                                    .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(t("AI Confidence Score"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("${(scenario.aiConfidence * 100).toInt()}%", fontSize = 28.sp, fontWeight = FontWeight.Black, color = colors.textMain)
                                        Text(if (isSafeScenario) t("Benign") else t("Malicious"), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = colors.primary)
                                    }
                                }
                            }
                            Button(
                                onClick = { viewModel.nextTrainingRound() },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text(t("Next Round"), fontWeight = FontWeight.Bold)
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
private fun StatCard(value: String, label: String, highlight: Color? = null, color: Color = LocalStitchTokens.current.colors.textMain) {
    val colors = LocalStitchTokens.current.colors
    val bg = highlight?.copy(alpha = 0.1f) ?: colors.surface
    val border = highlight?.copy(alpha = 0.2f) ?: colors.border
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
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = highlight?.copy(alpha = 0.7f) ?: colors.textSub, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun DotIndicator() {
    val colors = LocalStitchTokens.current.colors
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(colors.border)
            .border(1.dp, colors.border.copy(alpha = 0.5f), CircleShape)
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
    val colors = LocalStitchTokens.current.colors
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MaterialSymbol(name = icon, size = 20.sp, color = color)
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.textMain)
            Text(body, fontSize = 12.sp, color = colors.textSub)
        }
    }
}
