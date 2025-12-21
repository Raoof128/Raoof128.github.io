/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.theme.DesktopColors

data class GameUrl(val url: String, val isMalicious: Boolean, val explanation: String)

@Composable
fun TrainingScreen(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentRound by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var lastGuessCorrect by remember { mutableStateOf(false) }

    val gameUrls = remember { listOf(
        GameUrl("https://paypa1.com/login", true, "Homograph attack: '1' instead of 'l'"),
        GameUrl("https://google.com/search", false, "Legitimate Google URL"),
        GameUrl("https://arnazon-deals.net/offer", true, "Typosquatting: 'arnazon' mimics 'amazon'"),
        GameUrl("https://github.com/trending", false, "Legitimate GitHub URL"),
        GameUrl("https://secure-bank-login.xyz/auth", true, "Suspicious TLD and excessive hyphens"),
        GameUrl("https://microsoft.com/365", false, "Legitimate Microsoft URL"),
        GameUrl("https://dropbox.com.malware.ru/file", true, "Subdomain spoofing attack"),
        GameUrl("https://stackoverflow.com/questions", false, "Legitimate Stack Overflow URL")
    ).shuffled() }

    val currentUrl = gameUrls.getOrNull(currentRound)

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("🎓 Beat the Bot", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF7C3AED).copy(0.1f)) {
                        Text("Score: $score/${gameUrls.size}", Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onThemeToggle) { Text(if (isDarkMode) "☀️" else "🌙", fontSize = 18.sp) }
                }
            }
        }

        // Game Content
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (currentUrl == null) {
                // Game Complete
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text("🏆", fontSize = 64.sp)
                        Text("Training Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Final Score: $score/${gameUrls.size}", style = MaterialTheme.typography.titleLarge, color = Color(0xFF7C3AED))
                        Text(when { score == gameUrls.size -> "Perfect! You're a phishing detection expert!"
                            score >= gameUrls.size * 0.75 -> "Great job! You can spot most threats."
                            else -> "Keep practicing to improve your skills!" },
                            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { currentRound = 0; score = 0; showResult = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) {
                            Text("Play Again")
                        }
                    }
                }
            } else if (showResult) {
                // Show Result
                Surface(shape = RoundedCornerShape(24.dp), color = if (lastGuessCorrect) DesktopColors.VerdictSafe.copy(0.1f) else DesktopColors.VerdictMalicious.copy(0.1f)) {
                    Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(if (lastGuessCorrect) "✅" else "❌", fontSize = 48.sp)
                        Text(if (lastGuessCorrect) "Correct!" else "Incorrect!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                            color = if (lastGuessCorrect) DesktopColors.VerdictSafe else DesktopColors.VerdictMalicious)
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                            Text(currentUrl.url, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("This URL is ${if (currentUrl.isMalicious) "MALICIOUS" else "SAFE"}", fontWeight = FontWeight.Bold,
                            color = if (currentUrl.isMalicious) DesktopColors.VerdictMalicious else DesktopColors.VerdictSafe)
                        Text(currentUrl.explanation, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Button(onClick = { currentRound++; showResult = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) {
                            Text("Next URL →")
                        }
                    }
                }
            } else {
                // Game Card
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.widthIn(max = 600.dp)) {
                    Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(gameUrls.size) { i ->
                                Box(Modifier.size(10.dp).clip(CircleShape).background(
                                    when { i < currentRound -> Color(0xFF7C3AED); i == currentRound -> Color(0xFF2563EB); else -> MaterialTheme.colorScheme.surfaceVariant }
                                ))
                            }
                        }
                        Text("Round ${currentRound + 1} of ${gameUrls.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Is this URL Safe or Malicious?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.2f))) {
                            Text(currentUrl.url, Modifier.padding(20.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(onClick = { lastGuessCorrect = !currentUrl.isMalicious; if (lastGuessCorrect) score++; showResult = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DesktopColors.VerdictSafe), modifier = Modifier.width(140.dp)) { Text("✓ Safe", fontWeight = FontWeight.Bold) }
                            Button(onClick = { lastGuessCorrect = currentUrl.isMalicious; if (lastGuessCorrect) score++; showResult = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DesktopColors.VerdictMalicious), modifier = Modifier.width(140.dp)) { Text("⚠ Malicious", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}
