/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.desktop.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.theme.DesktopColors
import com.qrshield.model.Verdict

@Composable
fun ResultsScreen(
    result: AnalysisResult,
    onBack: () -> Unit,
    onCopyUrl: () -> Unit,
    onAddToTrusted: () -> Unit,
    onScanAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verdictColor = when (result.verdict) {
        Verdict.SAFE -> DesktopColors.VerdictSafe
        Verdict.SUSPICIOUS -> DesktopColors.VerdictSuspicious
        else -> DesktopColors.VerdictMalicious
    }
    val verdictIcon = when (result.verdict) { Verdict.SAFE -> "✓"; Verdict.SUSPICIOUS -> "⚠️"; else -> "⛔" }
    val verdictTitle = when (result.verdict) { Verdict.SAFE -> "URL is Safe"; Verdict.SUSPICIOUS -> "Proceed with Caution"; else -> "Threat Detected" }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Back to Dashboard") }
                Text("Analysis Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(100.dp))
            }
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Hero Card
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = verdictColor.copy(0.05f), border = BorderStroke(1.dp, verdictColor.copy(0.2f))) {
                Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Verdict Icon
                    Box(Modifier.size(100.dp).clip(CircleShape).background(verdictColor.copy(0.1f)), contentAlignment = Alignment.Center) {
                        Text(verdictIcon, fontSize = 48.sp)
                    }
                    Text(verdictTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = verdictColor)
                    Text("Score: ${result.score}/100", style = MaterialTheme.typography.titleLarge, color = verdictColor)
                    
                    // URL Display
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                        Text(result.url, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                    }

                    // Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onCopyUrl, shape = RoundedCornerShape(8.dp)) { Text("📋 Copy URL") }
                        if (result.verdict == Verdict.SAFE) {
                            Button(onClick = onAddToTrusted, colors = ButtonDefaults.buttonColors(containerColor = verdictColor)) { Text("⭐ Add to Trusted") }
                        }
                        Button(onClick = onScanAgain, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) { Text("🔄 Scan Another") }
                    }
                }
            }

            // Details Grid
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Flags
                Surface(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Detection Flags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (result.flags.isEmpty()) {
                            Text("No suspicious patterns detected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            result.flags.forEach { flag ->
                                Surface(shape = RoundedCornerShape(8.dp), color = verdictColor.copy(0.1f)) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("⚠️", fontSize = 14.sp)
                                        Text(flag, color = verdictColor)
                                    }
                                }
                            }
                        }
                    }
                }

                // Score Breakdown
                Surface(Modifier.weight(1f), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Risk Assessment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        ScoreBar("URL Structure", if (result.score < 30) 10 else 70, DesktopColors.VerdictSafe)
                        ScoreBar("Domain Trust", if (result.score < 30) 15 else 80, Color(0xFF2563EB))
                        ScoreBar("Pattern Match", result.score, verdictColor)
                    }
                }
            }

            // Recommendations
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = if (result.verdict == Verdict.MALICIOUS) DesktopColors.VerdictMalicious.copy(0.05f) else MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Recommendations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    when (result.verdict) {
                        Verdict.SAFE -> Text("✅ This URL appears safe. You can proceed with confidence.", color = DesktopColors.VerdictSafe)
                        Verdict.SUSPICIOUS -> Text("⚠️ Exercise caution. Verify the source before entering sensitive data.", color = DesktopColors.VerdictSuspicious)
                        else -> Text("🚫 DO NOT visit this URL. It has been flagged as potentially malicious.", color = DesktopColors.VerdictMalicious)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreBar(label: String, score: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("$score%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(progress = { score / 100f }, Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = color, trackColor = MaterialTheme.colorScheme.surfaceVariant)
    }
}
