/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.desktop.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.theme.DesktopColors
import kotlinx.coroutines.delay

/**
 * Training Screen - "Beat the Bot" phishing simulation game
 * Matches the HTML design with interactive URL classification gameplay.
 */
@Composable
fun TrainingScreen(
    isDarkMode: Boolean = false,
    onThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentRound by remember { mutableStateOf(1) }
    var totalRounds by remember { mutableStateOf(10) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var correctAnswers by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(720) } // 12 minutes in seconds
    
    // Sample training data
    val trainingUrls = remember {
        listOf(
            TrainingUrl(
                url = "https://secure-login.micros0ft-support.com/auth?client_id=19283",
                isPhishing = true,
                context = "Physical Flyer",
                contextDescription = "Found this on a table at Starbeans Coffee. It offered a free coffee coupon if I logged in.",
                flags = listOf(
                    TrainingFlag("Typosquatting Detected", "Domain uses a zero '0' instead of 'o' to impersonate Microsoft.", "⚠️"),
                    TrainingFlag("Suspicious TLD", "Hyphenated structure with 'support' is common in phishing.", "🌐"),
                    TrainingFlag("Social Engineering", "Free coupon creates urgency, bypassing critical thinking.", "🧠")
                )
            ),
            TrainingUrl(
                url = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
                isPhishing = false,
                context = "Work Email",
                contextDescription = "Received from IT department for SSO login verification.",
                flags = listOf(
                    TrainingFlag("Verified Domain", "Microsoft's official authentication domain.", "✓"),
                    TrainingFlag("Valid Certificate", "EV Certificate issued by DigiCert.", "🔒"),
                    TrainingFlag("Known Pattern", "Standard OAuth 2.0 authentication flow.", "✓")
                )
            ),
            TrainingUrl(
                url = "https://app1e-id-verify.com/account/security?ref=8823",
                isPhishing = true,
                context = "SMS Message",
                contextDescription = "Your Apple ID has been locked. Click here to verify.",
                flags = listOf(
                    TrainingFlag("Homograph Attack", "Uses '1' (one) instead of 'l' in 'apple'.", "⚠️"),
                    TrainingFlag("Domain Age < 24h", "Newly registered domain is highly suspicious.", "⏰"),
                    TrainingFlag("Fear Tactic", "Account locked message creates panic.", "🧠")
                )
            ),
            TrainingUrl(
                url = "https://github.com/microsoft/vscode",
                isPhishing = false,
                context = "QR Code on Poster",
                contextDescription = "Developer conference handout for VS Code.",
                flags = listOf(
                    TrainingFlag("Trusted Domain", "GitHub is a verified, well-known platform.", "✓"),
                    TrainingFlag("No Redirects", "Direct link to repository.", "✓"),
                    TrainingFlag("Clear Path", "Repository path matches expected pattern.", "✓")
                )
            ),
            TrainingUrl(
                url = "https://secure-paypa1-update.xyz/verify?id=992831",
                isPhishing = true,
                context = "Email Link",
                contextDescription = "Your PayPal account requires immediate verification.",
                flags = listOf(
                    TrainingFlag("Homograph Attack", "Uses '1' instead of 'l' in 'paypal'.", "⚠️"),
                    TrainingFlag("Suspicious TLD", "'.xyz' is uncommon for financial services.", "🌐"),
                    TrainingFlag("Urgency Keywords", "'Immediate verification' is a red flag.", "🧠")
                )
            )
        )
    }
    
    val currentUrl = trainingUrls.getOrElse(currentRound - 1) { trainingUrls.first() }
    val accuracy = if (currentRound > 1) (correctAnswers * 100) / (currentRound - 1) else 0

    // Timer
    LaunchedEffect(Unit) {
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF2563EB).copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = "MODULE 3",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏱️", fontSize = 14.sp)
                            Text(
                                text = "${timeRemaining / 60}:${(timeRemaining % 60).toString().padStart(2, '0')} remaining",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Beat the Bot",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "Phishing Simulation · Round $currentRound of $totalRounds",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stats Cards
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        value = score.toString(),
                        label = "Score",
                        backgroundColor = MaterialTheme.colorScheme.surface
                    )
                    StatCard(
                        value = streak.toString(),
                        label = "Streak",
                        backgroundColor = Color(0xFFFEF3C7),
                        valueColor = Color(0xFFD97706),
                        icon = "🔥"
                    )
                    StatCard(
                        value = "$accuracy%",
                        label = "Accuracy",
                        valueColor = DesktopColors.VerdictSafe,
                        backgroundColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Progress Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SESSION PROGRESS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(currentRound * 100) / totalRounds}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { currentRound.toFloat() / totalRounds },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF2563EB),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Main Game Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left - Challenge Card
                Surface(
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                    shadowElevation = 4.dp
                ) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👁️", fontSize = 16.sp)
                                Text(
                                    text = "Analyze the QR Code details",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                    )
                                }
                            }
                        }

                        // Content
                        Row(
                            modifier = Modifier.padding(32.dp),
                            horizontalArrangement = Arrangement.spacedBy(32.dp)
                        ) {
                            // QR Code Visual
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // QR pattern placeholder
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("📱", fontSize = 48.sp)
                                        Text(
                                            text = "QR Code",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                TextButton(onClick = { }) {
                                    Text("🔍 Enlarge", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            // URL Details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Decoded Payload
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "DECODED PAYLOAD",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = currentUrl.url,
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Context Source
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "CONTEXT SOURCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF2563EB).copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = when (currentUrl.context) {
                                                        "Physical Flyer" -> "☕"
                                                        "Work Email" -> "📧"
                                                        "SMS Message" -> "📱"
                                                        "QR Code on Poster" -> "🎫"
                                                        else -> "📧"
                                                    },
                                                    fontSize = 18.sp
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = currentUrl.context,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "\"${currentUrl.contextDescription}\"",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Action Buttons
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Phishing Button
                                Button(
                                    onClick = {
                                        val isCorrect = currentUrl.isPhishing
                                        lastAnswerCorrect = isCorrect
                                        if (isCorrect) {
                                            score += 100 + (streak * 10)
                                            streak++
                                            correctAnswers++
                                        } else {
                                            streak = 0
                                        }
                                        showResult = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(70.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DesktopColors.VerdictMalicious.copy(alpha = 0.1f)
                                    ),
                                    border = BorderStroke(2.dp, DesktopColors.VerdictMalicious.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⛔", fontSize = 24.sp)
                                        Column {
                                            Text(
                                                text = "PHISHING",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = DesktopColors.VerdictMalicious
                                            )
                                            Text(
                                                text = "Flag as malicious",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = DesktopColors.VerdictMalicious.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }

                                // Legitimate Button
                                Button(
                                    onClick = {
                                        val isCorrect = !currentUrl.isPhishing
                                        lastAnswerCorrect = isCorrect
                                        if (isCorrect) {
                                            score += 100 + (streak * 10)
                                            streak++
                                            correctAnswers++
                                        } else {
                                            streak = 0
                                        }
                                        showResult = true
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(70.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DesktopColors.VerdictSafe.copy(alpha = 0.1f)
                                    ),
                                    border = BorderStroke(2.dp, DesktopColors.VerdictSafe.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("✓", fontSize = 24.sp)
                                        Column {
                                            Text(
                                                text = "LEGITIMATE",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = DesktopColors.VerdictSafe
                                            )
                                            Text(
                                                text = "Mark as safe",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = DesktopColors.VerdictSafe.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }

                            TextButton(
                                onClick = {
                                    if (currentRound < totalRounds) {
                                        currentRound++
                                        showResult = false
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "Skip this round",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Right - Analysis Report
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (showResult) {
                            if (currentUrl.isPhishing) DesktopColors.VerdictMalicious.copy(alpha = 0.3f)
                            else DesktopColors.VerdictSafe.copy(alpha = 0.3f)
                        } else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    ),
                    shadowElevation = 4.dp
                ) {
                    Column {
                        // Top Border Accent
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(
                                    if (showResult) {
                                        if (currentUrl.isPhishing) DesktopColors.VerdictMalicious
                                        else DesktopColors.VerdictSafe
                                    } else Color(0xFF2563EB)
                                )
                        )

                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Analysis Report",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (showResult) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (currentUrl.isPhishing) 
                                            DesktopColors.VerdictMalicious.copy(alpha = 0.1f)
                                        else DesktopColors.VerdictSafe.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = if (currentUrl.isPhishing) "DETECTED" else "SAFE",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentUrl.isPhishing) DesktopColors.VerdictMalicious
                                                    else DesktopColors.VerdictSafe
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (showResult) {
                                // Show flags
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    currentUrl.flags.forEach { flag ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(flag.icon, fontSize = 18.sp)
                                            Column {
                                                Text(
                                                    text = flag.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = flag.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Result feedback
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (lastAnswerCorrect) DesktopColors.VerdictSafe.copy(alpha = 0.1f)
                                            else DesktopColors.VerdictMalicious.copy(alpha = 0.1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (lastAnswerCorrect) "✓ Correct!" else "✗ Incorrect",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (lastAnswerCorrect) DesktopColors.VerdictSafe
                                                    else DesktopColors.VerdictMalicious
                                        )
                                        if (lastAnswerCorrect) {
                                            Text(
                                                text = "+${100 + ((streak - 1) * 10)} points",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DesktopColors.VerdictSafe
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // AI Confidence
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF6366F1).copy(alpha = 0.05f),
                                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "AI Confidence Score",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6366F1)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = if (currentUrl.isPhishing) "99.8%" else "0.1%",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = if (currentUrl.isPhishing) "Malicious" else "Benign",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (currentUrl.isPhishing) DesktopColors.VerdictMalicious
                                                        else DesktopColors.VerdictSafe
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Next Button
                                Button(
                                    onClick = {
                                        if (currentRound < totalRounds) {
                                            currentRound++
                                            showResult = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (currentRound < totalRounds) "Next Round" else "Finish",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("→")
                                    }
                                }
                            } else {
                                // Instructions before answer
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("🔍", fontSize = 40.sp)
                                    Text(
                                        text = "Analyze the URL",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Review the decoded payload and context, then decide if this is a phishing attempt or legitimate.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
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
private fun StatCard(
    value: String,
    label: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: String? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Box {
            if (icon != null) {
                Text(
                    text = icon,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    fontSize = 10.sp
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .widthIn(min = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class TrainingUrl(
    val url: String,
    val isPhishing: Boolean,
    val context: String,
    val contextDescription: String,
    val flags: List<TrainingFlag>
)

private data class TrainingFlag(
    val title: String,
    val description: String,
    val icon: String
)
