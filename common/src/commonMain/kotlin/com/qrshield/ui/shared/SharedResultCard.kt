/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qrshield.ui.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import com.qrshield.model.RiskAssessment
import com.qrshield.model.Verdict

/**
 * Shared Result Card - Compose Multiplatform UI Component
 *
 * This component is designed to be shared across ALL platforms including iOS.
 * On iOS, it can be embedded via UIViewControllerRepresentable using ComposeUIViewController.
 *
 * ## KMP UI Sharing Strategy
 *
 * This demonstrates the "Hybrid" approach:
 * - Complex UI components (like this ResultCard) are built once in Compose
 * - Platform-specific screens (Scanner) remain native for optimal UX
 * - Best of both worlds: code sharing + native feel
 *
 * @param assessment The risk assessment to display
 * @param onDismiss Callback when user dismisses the result
 * @param onShare Callback to share the result
 * @param modifier Optional modifier
 */
@Composable
fun SharedResultCard(
    assessment: RiskAssessment,
    onDismiss: () -> Unit = {},
    onShare: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val verdictColor = when (assessment.verdict) {
        Verdict.SAFE -> Color(0xFF00D68F)       // Emerald Green
        Verdict.SUSPICIOUS -> Color(0xFFFFAA00) // Amber
        Verdict.MALICIOUS -> Color(0xFFFF3D71)  // Coral Red
        Verdict.UNKNOWN -> Color(0xFF6B7280)    // Gray
    }
    
    val animatedColor by animateColorAsState(
        targetValue = verdictColor,
        animationSpec = tween(500)
    )
    
    // Pulsing animation for high-risk results
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (assessment.verdict == Verdict.MALICIOUS) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                animatedColor.copy(alpha = 0.3f),
                                animatedColor.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(4.dp, animatedColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${assessment.score}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedColor
                    )
                    Text(
                        text = "/ 100",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Verdict Label
            Surface(
                shape = RoundedCornerShape(50),
                color = animatedColor.copy(alpha = 0.2f),
                modifier = Modifier.border(1.dp, animatedColor, RoundedCornerShape(50))
            ) {
                Text(
                    text = getVerdictLabel(assessment.verdict),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = animatedColor
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Verdict Description
            Text(
                text = getVerdictDescription(assessment.verdict),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            // Risk Signals
            if (assessment.flags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "DETECTED SIGNALS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    assessment.flags.take(5).forEach { flag ->
                        SignalChip(signal = flag, color = animatedColor)
                    }
                    if (assessment.flags.size > 5) {
                        Text(
                            text = "+${assessment.flags.size - 5} more",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Dismiss")
                }
                
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = animatedColor
                    )
                ) {
                    Text("Share", color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun SignalChip(signal: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = signal,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

private fun getVerdictLabel(verdict: Verdict): String = when (verdict) {
    Verdict.SAFE -> "✓ SAFE"
    Verdict.SUSPICIOUS -> "⚠ SUSPICIOUS"
    Verdict.MALICIOUS -> "✕ MALICIOUS"
    Verdict.UNKNOWN -> "? UNKNOWN"
}

private fun getVerdictDescription(verdict: Verdict): String = when (verdict) {
    Verdict.SAFE -> "No significant threats detected. This URL appears to be legitimate."
    Verdict.SUSPICIOUS -> "Some risk indicators found. Proceed with caution and verify the source."
    Verdict.MALICIOUS -> "High-risk URL detected! This appears to be a phishing attempt. Do NOT proceed."
    Verdict.UNKNOWN -> "Unable to fully analyze this URL. Exercise caution."
}
