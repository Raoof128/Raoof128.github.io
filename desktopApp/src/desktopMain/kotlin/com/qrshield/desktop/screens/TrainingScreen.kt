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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.qrshield.desktop.ui.progressTrack
import com.qrshield.desktop.ui.progressFill
import com.qrshield.desktop.ui.statusPill
import com.qrshield.ui.components.CommonBrainVisualizer
import com.qrshield.desktop.ui.handCursor

@Composable
fun TrainingScreen(viewModel: AppViewModel) {
    val tokens = StitchTokens.training(isDark = viewModel.isDarkMode)
    val training = viewModel.trainingState
    val focusRequester = remember { FocusRequester() }
    var showKeyboardHelp by remember { mutableStateOf(false) }
    
    // Request focus when screen loads for keyboard handling
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    StitchTheme(tokens = tokens) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when {
                            // P key = Mark as Phishing
                            (event.key == Key.P) && !training.showResultModal && !training.isGameOver -> {
                                viewModel.submitTrainingVerdict(isPhishing = true)
                                true
                            }
                            // L key = Mark as Legitimate
                            (event.key == Key.L) && !training.showResultModal && !training.isGameOver -> {
                                viewModel.submitTrainingVerdict(isPhishing = false)
                                true
                            }
                            // Enter key = Next Round / Play Again / Close Modal
                            event.key == Key.Enter -> {
                                when {
                                    training.showResultModal -> {
                                        viewModel.dismissTrainingResultModal()
                                        true
                                    }
                                    training.isGameOver -> {
                                        viewModel.resetTrainingGame()
                                        true
                                    }
                                    else -> false
                                }
                            }
                            // Escape = Return to Dashboard (only when modal is open)
                            event.key == Key.Escape && training.isGameOver -> {
                                viewModel.currentScreen = AppScreen.Dashboard
                                true
                            }
                            // H or ? key = Toggle keyboard shortcuts help
                            (event.key == Key.H || event.key == Key.Slash) && event.isShiftPressed -> {
                                showKeyboardHelp = !showKeyboardHelp
                                true
                            }
                            event.key == Key.H && !training.showResultModal && !training.isGameOver -> {
                                showKeyboardHelp = !showKeyboardHelp
                                true
                            }
                            // Escape also closes keyboard help
                            event.key == Key.Escape && showKeyboardHelp -> {
                                showKeyboardHelp = false
                                true
                            }
                            else -> false
                        }
                    } else false
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(tokens.colors.background)
            ) {
                AppSidebar(
                    currentScreen = AppScreen.Training,
                    onNavigate = { viewModel.currentScreen = it },
                    language = viewModel.appLanguage,
                    onProfileClick = { viewModel.currentScreen = AppScreen.TrustCentreAlt }
                )
                TrainingContent(viewModel = viewModel)
            }
            
            // Result Modal (shown after each round decision)
            if (training.showResultModal) {
                TrainingResultModal(
                    isCorrect = training.lastRoundCorrect ?: false,
                    points = training.lastRoundPoints,
                    responseTimeMs = training.lastResponseTimeMs,
                    isLastRound = training.round >= training.totalRounds,
                    onNextRound = { viewModel.dismissTrainingResultModal() },
                    language = viewModel.appLanguage
                )
            }
            
            // Game Over Modal
            if (training.isGameOver) {
                TrainingGameOverModal(
                    playerScore = training.score,
                    botScore = training.botScore,
                    accuracy = training.accuracy,
                    bestStreak = training.bestStreak,
                    playerWon = training.playerWon,
                    onPlayAgain = { viewModel.resetTrainingGame() },
                    onReturnToDashboard = { viewModel.currentScreen = AppScreen.Dashboard },
                    language = viewModel.appLanguage
                )
            }
            
            // Keyboard Shortcuts Help Overlay
            if (showKeyboardHelp) {
                KeyboardShortcutsOverlay(
                    onDismiss = { showKeyboardHelp = false },
                    language = viewModel.appLanguage
                )
            }
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
                        .progressTrack(colors.backgroundAlt)
                        .border(1.dp, colors.border, RoundedCornerShape(999.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(training.progress.coerceIn(0f, 1f))
                            .progressFill(colors.primary)
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
                                            .clickable { viewModel.showInfo(t("Press H to show keyboard shortcuts")) }
                                            .focusable()
                                            .handCursor()
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
                                        subtitle = t("Flag as malicious (P)"),
                                        icon = "gpp_bad",
                                        bg = colors.danger.copy(alpha = 0.1f),
                                        textColor = colors.danger,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.submitTrainingVerdict(isPhishing = true) }
                                    )
                                    TrainingActionButton(
                                        label = t("Legitimate"),
                                        subtitle = t("Mark as safe (L)"),
                                        icon = "verified_user",
                                        bg = colors.success.copy(alpha = 0.1f),
                                        textColor = colors.success,
                                        modifier = Modifier.weight(1f),
                                        onClick = { viewModel.submitTrainingVerdict(isPhishing = false) }
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        t("Skip this round"),
                                        fontSize = 12.sp,
                                        color = colors.textMuted,
                                        modifier = Modifier
                                            .clickable { viewModel.skipTrainingRound() }
                                            .focusable()
                                            .handCursor()
                                    )
                                    Text(
                                        t("⌨ P/L = Phishing/Legitimate · H = Help"),
                                        fontSize = 10.sp,
                                        color = colors.textMuted.copy(alpha = 0.6f)
                                    )
                                }
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
                            
                            val isRoundDone = training.showResultModal || training.isGameOver
                            val visualSignals = if (isRoundDone && !isSafeScenario) {
                                scenario.insights.map { t(it.title) }
                            } else {
                                emptyList()
                            }
                            CommonBrainVisualizer(
                                detectedSignals = visualSignals,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            
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
                                onClick = { viewModel.dismissTrainingResultModal() },
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
                .handCursor()
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

/**
 * Result Modal shown after each round decision
 */
@Composable
private fun TrainingResultModal(
    isCorrect: Boolean,
    points: Int,
    responseTimeMs: Long,
    isLastRound: Boolean,
    onNextRound: () -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.width(400.dp),
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isCorrect) colors.success.copy(alpha = 0.1f) else colors.danger.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(
                        name = if (isCorrect) "check_circle" else "cancel",
                        size = 48.sp,
                        color = if (isCorrect) colors.success else colors.danger
                    )
                }
                
                // Title
                Text(
                    text = if (isCorrect) t("Correct!") else t("Wrong!"),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain
                )
                
                // Description
                Text(
                    text = if (isCorrect) t("You spotted it correctly!") else t("Better luck next time!"),
                    fontSize = 14.sp,
                    color = colors.textSub
                )
                
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (points >= 0) "+$points" else "$points",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (points >= 0) colors.success else colors.danger
                        )
                        Text(t("Points"), fontSize = 12.sp, color = colors.textMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${responseTimeMs / 1000.0}s",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain
                        )
                        Text(t("Response"), fontSize = 12.sp, color = colors.textMuted)
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Next Round Button
                Button(
                    onClick = onNextRound,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = if (isLastRound) t("See Results") else t("Next Round"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Game Over Modal shown when all rounds are complete
 */
@Composable
private fun TrainingGameOverModal(
    playerScore: Int,
    botScore: Int,
    accuracy: Float,
    bestStreak: Int,
    playerWon: Boolean,
    onPlayAgain: () -> Unit,
    onReturnToDashboard: () -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    val tied = playerScore == botScore
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.width(480.dp),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Trophy Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            if (playerWon) colors.warning.copy(alpha = 0.2f)
                            else colors.primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    MaterialSymbol(
                        name = if (playerWon) "emoji_events" else "smart_toy",
                        size = 56.sp,
                        color = if (playerWon) colors.warning else colors.primary
                    )
                }
                
                // Title
                Text(
                    text = when {
                        playerWon -> t("🎉 You Win!")
                        tied -> t("It's a Tie!")
                        else -> t("Game Over!")
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textMain
                )
                
                // Subtitle
                Text(
                    text = if (playerWon) t("Congratulations! You beat the bot!")
                           else t("The bot was faster this time. Try again!"),
                    fontSize = 14.sp,
                    color = colors.textSub,
                    textAlign = TextAlign.Center
                )
                
                // Score Comparison
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.backgroundAlt)
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(t("Your Score"), fontSize = 12.sp, color = colors.textMuted)
                        Text(
                            text = playerScore.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = if (playerWon) colors.success else colors.textMain
                        )
                    }
                    
                    Text(
                        text = "VS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMuted
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            MaterialSymbol(name = "smart_toy", size = 14.sp, color = colors.textMuted)
                            Text(t("Bot Score"), fontSize = 12.sp, color = colors.textMuted)
                        }
                        Text(
                            text = botScore.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textMain
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onReturnToDashboard,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surface, contentColor = colors.textMain),
                        border = BorderStroke(1.dp, colors.border),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(t("Dashboard"), fontWeight = FontWeight.SemiBold)
                    }
                    
                    Button(
                        onClick = onPlayAgain,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(t("Play Again"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Keyboard Shortcuts Help Overlay
 * Shows all available keyboard shortcuts for the training game.
 */
@Composable
private fun KeyboardShortcutsOverlay(
    onDismiss: () -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .clickable(enabled = false) { }, // Prevent click-through
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MaterialSymbol(name = "keyboard", size = 24.sp, color = colors.primary)
                        Text(
                            t("Keyboard Shortcuts"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.backgroundAlt)
                            .clickable { onDismiss() }
                            .handCursor(),
                        contentAlignment = Alignment.Center
                    ) {
                        MaterialSymbol(name = "close", size = 16.sp, color = colors.textMuted)
                    }
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.border)
                )
                
                // Shortcuts List
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShortcutRow(key = "P", description = t("Mark as Phishing"), color = colors.danger)
                    ShortcutRow(key = "L", description = t("Mark as Legitimate"), color = colors.success)
                    ShortcutRow(key = "Enter", description = t("Next Round / Confirm"))
                    ShortcutRow(key = "Esc", description = t("Return to Dashboard"))
                    ShortcutRow(key = "H", description = t("Toggle this help panel"))
                }
                
                // Footer hint
                Text(
                    t("Press any key to close"),
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ShortcutRow(
    key: String,
    description: String,
    color: Color = LocalStitchTokens.current.colors.textMain
) {
    val colors = LocalStitchTokens.current.colors
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.backgroundAlt)
                    .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    key,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            Text(
                description,
                fontSize = 14.sp,
                color = colors.textSub
            )
        }
    }
}
