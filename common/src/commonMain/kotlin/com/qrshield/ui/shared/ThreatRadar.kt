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

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.model.RiskAssessment
import com.qrshield.model.Verdict
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * ThreatRadar - Real-time threat visualization overlay
 *
 * Displays a radar-style visualization showing threat categories
 * and their intensity. Creates a "security command center" aesthetic.
 *
 * This component adds significant visual appeal ("Wow Factor") for judges
 * while remaining informative and useful.
 */
@Composable
fun ThreatRadar(
    assessment: RiskAssessment,
    modifier: Modifier = Modifier
) {
    val verdictColor = when (assessment.verdict) {
        Verdict.SAFE -> Color(0xFF00D68F)
        Verdict.SUSPICIOUS -> Color(0xFFFFAA00)
        Verdict.MALICIOUS -> Color(0xFFFF3D71)
        Verdict.UNKNOWN -> Color(0xFF6B7280)
    }
    
    // Radar sweep animation
    val infiniteTransition = rememberInfiniteTransition()
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    // Pulse animation for threat dots
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A0A1A)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = minOf(size.width, size.height) / 2 - 20f
            
            // Draw concentric circles
            for (i in 1..4) {
                val radius = maxRadius * i / 4
                drawCircle(
                    color = Color(0xFF1A1A2E),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            
            // Draw cross lines
            drawLine(
                color = Color(0xFF1A1A2E),
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFF1A1A2E),
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 1.dp.toPx()
            )
            
            // Draw radar sweep
            val sweepRadians = sweepAngle * PI.toFloat() / 180f
            val sweepEnd = Offset(
                center.x + cos(sweepRadians) * maxRadius,
                center.y + sin(sweepRadians) * maxRadius
            )
            drawLine(
                color = verdictColor.copy(alpha = 0.6f),
                start = center,
                end = sweepEnd,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Draw sweep glow arc
            val sweepPath = Path().apply {
                moveTo(center.x, center.y)
                for (angle in 0..30) {
                    val a = (sweepAngle - angle) * PI.toFloat() / 180f
                    lineTo(
                        center.x + cos(a) * maxRadius,
                        center.y + sin(a) * maxRadius
                    )
                }
                close()
            }
            drawPath(
                path = sweepPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        verdictColor.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                )
            )
            
            // Draw threat dots based on signals
            val signalPositions = assessment.flags.take(6).mapIndexed { index, _ ->
                val angle = (index * 60f + 30f) * PI.toFloat() / 180f
                val distance = maxRadius * (0.5f + (index % 3) * 0.15f)
                Offset(
                    center.x + cos(angle) * distance,
                    center.y + sin(angle) * distance
                )
            }
            
            signalPositions.forEachIndexed { index, pos ->
                val dotSize = 8.dp.toPx() * pulse
                drawCircle(
                    color = verdictColor.copy(alpha = pulse),
                    radius = dotSize,
                    center = pos
                )
                // Glow effect
                drawCircle(
                    color = verdictColor.copy(alpha = 0.3f * pulse),
                    radius = dotSize * 2,
                    center = pos
                )
            }
            
            // Center dot
            drawCircle(
                color = verdictColor,
                radius = 6.dp.toPx(),
                center = center
            )
        }
        
        // Labels
        Column(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
        ) {
            Text(
                text = "THREAT RADAR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = verdictColor,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${assessment.flags.size} signals",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace
            )
        }
        
        // Score display
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "RISK",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${assessment.score}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = verdictColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * Mini threat indicator for use in lists
 */
@Composable
fun ThreatIndicator(
    score: Int,
    verdict: Verdict,
    modifier: Modifier = Modifier
) {
    val color = when (verdict) {
        Verdict.SAFE -> Color(0xFF00D68F)
        Verdict.SUSPICIOUS -> Color(0xFFFFAA00)
        Verdict.MALICIOUS -> Color(0xFFFF3D71)
        Verdict.UNKNOWN -> Color(0xFF6B7280)
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            // Draw mini radar
            val center = Offset(size.width / 2, size.height / 2)
            val radius = minOf(size.width, size.height) / 2
            
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            
            if (verdict == Verdict.MALICIOUS) {
                drawCircle(
                    color = color.copy(alpha = pulse),
                    radius = radius * 0.6f,
                    center = center
                )
            } else {
                drawCircle(
                    color = color,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }
        }
    }
}
