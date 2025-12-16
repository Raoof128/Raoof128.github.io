package com.qrshield.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qrshield.gamification.BeatTheBot
import kotlinx.coroutines.launch

@Composable
fun BeatTheBotScreen(
    beatTheBot: BeatTheBot = remember { BeatTheBot() }
) {
    var urlInput by remember { mutableStateOf("") }
    var lastResult by remember { mutableStateOf<BeatTheBot.ChallengeResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var particleTrigger by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    val state = beatTheBot.getState()

    LaunchedEffect(lastResult) {
        if (lastResult is BeatTheBot.ChallengeResult.UserWins) {
            particleTrigger++ // Explode confetti
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Dark cyberpunk blue
            .padding(16.dp)
    ) {
        // Header
        HackerText(
            text = "PROTOCOL: BEAT_THE_BOT_V1",
            color = Color(0xFF22D3EE), // Cyan
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Scoreboard
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AnimatedScore(state.userScore, "USER: HACKER", Color(0xFF4ADE80)) // Green
            AnimatedScore(state.botScore, "SYSTEM: BOT", Color(0xFFF87171))   // Red
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Input Area
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Enter Phishing URL Payload") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF22D3EE),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        isAnalyzing = true
                        scope.launch {
                            // Simulate processing delay for effect
                            kotlinx.coroutines.delay(800)
                            lastResult = beatTheBot.challenge(urlInput)
                            isAnalyzing = false
                            if (lastResult is BeatTheBot.ChallengeResult.BotWins) {
                                urlInput = "" // Clear on fail
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isAnalyzing && urlInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE))
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DECRYPTING...", color = Color.Black)
                    } else {
                        Text("INJECT PAYLOAD", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Result Display
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            // Particle layer
            ParticleSystem(trigger = particleTrigger, color = Color(0xFF4ADE80))
            
            // Content
            lastResult?.let { result ->
                ResultCard(result)
            }
        }
    }
}

@Composable
fun ResultCard(result: BeatTheBot.ChallengeResult) {
    val (color, title, message) = when (result) {
        is BeatTheBot.ChallengeResult.UserWins -> 
            Triple(Color(0xFF4ADE80), "SYSTEM BYPASSED!", "You successfully evaded detection! (+${result.userPointsEarned} pts)")
        is BeatTheBot.ChallengeResult.BotWins -> 
            Triple(Color(0xFFF87171), "ACCESS DENIED", "Bot detected the threat. ${result.tip}")
        is BeatTheBot.ChallengeResult.FalseAlarm -> 
            Triple(Color(0xFFFACC15), "FALSE POSITIVE", "Bot was too aggressive. (+${result.userPointsEarned} pts)")
        is BeatTheBot.ChallengeResult.Invalid ->
            Triple(Color.Gray, "INVALID INPUT", result.reason)
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().border(2.dp, color, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            
            if (result is BeatTheBot.ChallengeResult.BotWins) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Signals Detected:", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                result.detectedSignals.forEach { signal ->
                    Text("• $signal", color = Color(0xFFFECACA))
                }
            }
        }
    }
}
