package com.qrshield.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qrshield.gamification.BeatTheBotViewModel
import com.qrshield.gamification.BeatTheBotViewModel.*
import com.qrshield.gamification.PhishingChallengeDataset
import kotlinx.coroutines.launch

/**
 * Beat the Bot Screen
 *
 * Gamified phishing awareness challenge where users try to craft
 * URLs that evade the detection engine.
 *
 * ## Architecture
 * - State is managed by BeatTheBotViewModel
 * - All animations are state-driven, not timer-based
 * - UI survives configuration changes
 * - No side effects in Composables
 *
 * @param viewModel ViewModel managing game state
 * @param onClose Callback when user exits the game
 */
@Composable
fun BeatTheBotScreen(
    viewModel: BeatTheBotViewModel,
    onClose: (() -> Unit)? = null
) {
    val state by viewModel.state.collectAsState()
    var urlInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Colors
    val darkBg = Color(0xFF0F172A)
    val cyan = Color(0xFF22D3EE)
    val green = Color(0xFF4ADE80)
    val red = Color(0xFFF87171)
    val yellow = Color(0xFFFACC15)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        when (val phase = state.phase) {
            is GamePhase.Idle -> {
                // Welcome screen
                IdleScreen(
                    onStartGame = { viewModel.startGame() },
                    cyan = cyan
                )
            }

            is GamePhase.Playing, is GamePhase.Analyzing, is GamePhase.ShowingResult -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header with difficulty
                    GameHeader(state = state, cyan = cyan)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scoreboard
                    ScoreboardRow(
                        userScore = state.score,
                        botScore = state.botScore,
                        difficulty = state.difficulty,
                        green = green,
                        red = red
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bot reaction indicator (animated)
                    if (phase is GamePhase.Analyzing) {
                        BotAnalyzingIndicator(
                            progress = state.botReactionProgress,
                            cyan = cyan
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Input area (disabled during analysis)
                    InputCard(
                        urlInput = urlInput,
                        onUrlChange = { urlInput = it },
                        onSubmit = {
                            viewModel.submitChallenge(urlInput)
                            urlInput = "" // Clear after submission
                        },
                        isEnabled = phase is GamePhase.Playing,
                        isAnalyzing = phase is GamePhase.Analyzing,
                        cyan = cyan
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Result display with animations
                    Column(modifier = Modifier.weight(1f)) {
                        // Confetti for wins
                        if (state.showConfetti) {
                            ParticleSystem(trigger = state.score, color = green)
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = phase is GamePhase.ShowingResult,
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut()
                        ) {
                            if (phase is GamePhase.ShowingResult) {
                                ResultCard(
                                    result = phase.result,
                                    onNextRound = { viewModel.nextRound() },
                                    green = green,
                                    red = red,
                                    yellow = yellow
                                )
                            }
                        }

                        // Tutorial hint when playing
                        if (phase is GamePhase.Playing && state.roundsPlayed == 0) {
                            TutorialHint(difficulty = state.difficulty)
                        }
                    }

                    // Stats footer
                    GameStatsFooter(state = state)
                }
            }

            is GamePhase.Won -> {
                WinScreen(
                    score = phase.finalScore,
                    achievements = phase.achievements,
                    onPlayAgain = { viewModel.resetGame() },
                    green = green
                )
            }

            is GamePhase.Lost -> {
                LostScreen(
                    reason = phase.reason,
                    score = phase.finalScore,
                    onPlayAgain = { viewModel.resetGame() },
                    red = red
                )
            }
        }
    }
}

@Composable
private fun IdleScreen(
    onStartGame: () -> Unit,
    cyan: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated title
        val infiniteTransition = rememberInfiniteTransition(label = "title")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Text(
            text = "🤖 BEAT THE BOT",
            style = MaterialTheme.typography.headlineLarge,
            color = cyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.scale(scale)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Can you craft a phishing URL that evades our AI detection?",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Submit URLs you think are 'obviously safe' but actually phishing.\nTrick the bot to earn points!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(containerColor = cyan),
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("START CHALLENGE", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GameHeader(state: GameState, cyan: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HackerText(
            text = "PROTOCOL: BEAT_THE_BOT_V2",
            color = cyan,
            style = MaterialTheme.typography.titleMedium
        )

        // Difficulty badge
        DifficultyBadge(difficulty = state.difficulty)
    }
}

@Composable
private fun DifficultyBadge(difficulty: PhishingChallengeDataset.Difficulty) {
    val color = when (difficulty) {
        PhishingChallengeDataset.Difficulty.BEGINNER -> Color(0xFF4ADE80)
        PhishingChallengeDataset.Difficulty.INTERMEDIATE -> Color(0xFF22D3EE)
        PhishingChallengeDataset.Difficulty.ADVANCED -> Color(0xFFFACC15)
        PhishingChallengeDataset.Difficulty.EXPERT -> Color(0xFFF97316)
        PhishingChallengeDataset.Difficulty.NIGHTMARE -> Color(0xFFF87171)
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.border(1.dp, color, RoundedCornerShape(4.dp))
    ) {
        Text(
            text = difficulty.displayName.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScoreboardRow(
    userScore: Int,
    botScore: Int,
    difficulty: PhishingChallengeDataset.Difficulty,
    green: Color,
    red: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ScoreDisplay(score = userScore, label = "YOU", color = green)
        
        Text(
            text = "VS",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.5f)
        )
        
        ScoreDisplay(score = botScore, label = "BOT", color = red)
    }
}

@Composable
private fun ScoreDisplay(score: Int, label: String, color: Color) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(300),
        label = "score"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color.copy(alpha = 0.8f)
        )
        Text(
            text = "$animatedScore",
            style = MaterialTheme.typography.displayMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BotAnalyzingIndicator(progress: Float, cyan: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pulsing bot icon
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Text(
            text = "🤖 ANALYZING...",
            style = MaterialTheme.typography.titleMedium,
            color = cyan.copy(alpha = alpha)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = cyan
        )
    }
}

@Composable
private fun InputCard(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isEnabled: Boolean,
    isAnalyzing: Boolean,
    cyan: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                label = { Text("Enter Phishing URL Payload") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    focusedBorderColor = cyan,
                    unfocusedBorderColor = Color(0xFF475569),
                    disabledBorderColor = Color(0xFF374151)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled && urlInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = cyan)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DECRYPTING...", color = Color.Black)
                } else {
                    Text("INJECT PAYLOAD", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: RoundResult,
    onNextRound: () -> Unit,
    green: Color,
    red: Color,
    yellow: Color
) {
    val (color, title, icon) = when (result.outcome) {
        RoundOutcome.USER_WINS -> Triple(green, "SYSTEM BYPASSED!", "🎉")
        RoundOutcome.BOT_WINS -> Triple(red, "ACCESS DENIED", "🤖")
        RoundOutcome.FALSE_POSITIVE -> Triple(yellow, "FALSE POSITIVE", "⚠️")
        RoundOutcome.INVALID_INPUT -> Triple(Color.Gray, "INVALID INPUT", "❌")
    }

    // Entry animation
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(2.dp, color, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, style = MaterialTheme.typography.displayMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "+${result.pointsEarned} points",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.tip,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            if (result.detectedSignals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Signals Detected:",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelMedium
                )
                result.detectedSignals.take(3).forEach { signal ->
                    Text(
                        text = "• $signal",
                        color = Color(0xFFFECACA),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNextRound,
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("NEXT ROUND", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TutorialHint(difficulty: PhishingChallengeDataset.Difficulty) {
    val challenge = remember { PhishingChallengeDataset.getRandomChallenge(difficulty) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡 TIP: ${challenge.technique.displayName}",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFFACC15)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = challenge.educationalHint,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GameStatsFooter(state: GameState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatItem(label = "Rounds", value = "${state.roundsPlayed}")
        StatItem(label = "Streak", value = "${state.currentStreak}")
        StatItem(label = "Best", value = "${state.bestStreak}")
        StatItem(label = "Rank", value = state.leaderboardRank.take(2))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun WinScreen(
    score: Int,
    achievements: List<com.qrshield.gamification.BeatTheBot.Achievement>,
    onPlayAgain: () -> Unit,
    green: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "VICTORY!",
            style = MaterialTheme.typography.headlineLarge,
            color = green,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Final Score: $score",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onPlayAgain) {
            Text("PLAY AGAIN")
        }
    }
}

@Composable
private fun LostScreen(
    reason: String,
    score: Int,
    onPlayAgain: () -> Unit,
    red: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💀", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "GAME OVER",
            style = MaterialTheme.typography.headlineLarge,
            color = red,
            fontWeight = FontWeight.Bold
        )
        Text(reason, color = Color.White.copy(alpha = 0.7f))
        Text(
            "Final Score: $score",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onPlayAgain) {
            Text("TRY AGAIN")
        }
    }
}

