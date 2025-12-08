/*
 * Copyright 2024 QR-SHIELD Contributors
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

package com.qrshield.android.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Animated Scanner Overlay - Android 16 Enhanced
 * 
 * Features:
 * 1. Corner brackets to define scan area
 * 2. Animated laser line sweeping up/down with glow effect
 * 3. Subtle pulsing corner animation
 * 4. Dark mask outside scan area
 * 
 * Design inspired by modern cybersecurity apps with premium feel.
 * 
 * @param modifier Modifier for the overlay
 * @param scanAreaRatio Ratio of screen width for scan area (0.0 - 1.0)
 * @param cornerColor Color for the corner brackets
 * @param laserColor Color for the animated laser line
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    scanAreaRatio: Float = 0.75f,
    cornerColor: Color = Color(0xFF00D68F), // Neon Green
    laserColor: Color = Color(0xFF00D68F)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_animation")
    
    // Animate the laser line moving up and down with smooth easing
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_sweep"
    )
    
    // Subtle pulse for corners
    val cornerPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corner_pulse"
    )
    
    // Glow intensity animation
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_intensity"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val scanAreaSize = width * scanAreaRatio
        val left = (width - scanAreaSize) / 2
        val top = (height - scanAreaSize) / 2
        
        // Corner dimensions
        val cornerLength = 45.dp.toPx()
        val cornerWidth = 4.dp.toPx()
        val cornerRadius = 10.dp.toPx()
        
        val activeColor = cornerColor.copy(alpha = cornerPulse)
        
        // ===================
        // 1. Dim Outside Area (Draw first as background)
        // ===================
        
        val dimColor = Color.Black.copy(alpha = 0.65f)
        
        // Top dim
        drawRect(
            color = dimColor,
            topLeft = Offset(0f, 0f),
            size = Size(width, top)
        )
        
        // Bottom dim
        drawRect(
            color = dimColor,
            topLeft = Offset(0f, top + scanAreaSize),
            size = Size(width, height - top - scanAreaSize)
        )
        
        // Left dim
        drawRect(
            color = dimColor,
            topLeft = Offset(0f, top),
            size = Size(left, scanAreaSize)
        )
        
        // Right dim
        drawRect(
            color = dimColor,
            topLeft = Offset(left + scanAreaSize, top),
            size = Size(width - left - scanAreaSize, scanAreaSize)
        )
        
        // ===================
        // 2. Draw Rounded Corners with Glow Effect
        // ===================
        
        // Draw glow behind corners
        val glowColor = cornerColor.copy(alpha = glowIntensity * 0.3f)
        val glowWidth = cornerWidth * 3
        
        // Top Left Corner Glow
        drawPath(
            path = Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top + cornerRadius)
                quadraticBezierTo(left, top, left + cornerRadius, top)
                lineTo(left + cornerLength, top)
            },
            color = glowColor,
            style = Stroke(width = glowWidth)
        )
        
        // Top Left Corner
        drawPath(
            path = Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top + cornerRadius)
                quadraticBezierTo(left, top, left + cornerRadius, top)
                lineTo(left + cornerLength, top)
            },
            color = activeColor,
            style = Stroke(width = cornerWidth)
        )
        
        // Top Right Corner Glow
        drawPath(
            path = Path().apply {
                moveTo(left + scanAreaSize - cornerLength, top)
                lineTo(left + scanAreaSize - cornerRadius, top)
                quadraticBezierTo(left + scanAreaSize, top, left + scanAreaSize, top + cornerRadius)
                lineTo(left + scanAreaSize, top + cornerLength)
            },
            color = glowColor,
            style = Stroke(width = glowWidth)
        )
        
        // Top Right Corner
        drawPath(
            path = Path().apply {
                moveTo(left + scanAreaSize - cornerLength, top)
                lineTo(left + scanAreaSize - cornerRadius, top)
                quadraticBezierTo(left + scanAreaSize, top, left + scanAreaSize, top + cornerRadius)
                lineTo(left + scanAreaSize, top + cornerLength)
            },
            color = activeColor,
            style = Stroke(width = cornerWidth)
        )
        
        // Bottom Left Corner Glow
        drawPath(
            path = Path().apply {
                moveTo(left, top + scanAreaSize - cornerLength)
                lineTo(left, top + scanAreaSize - cornerRadius)
                quadraticBezierTo(left, top + scanAreaSize, left + cornerRadius, top + scanAreaSize)
                lineTo(left + cornerLength, top + scanAreaSize)
            },
            color = glowColor,
            style = Stroke(width = glowWidth)
        )
        
        // Bottom Left Corner
        drawPath(
            path = Path().apply {
                moveTo(left, top + scanAreaSize - cornerLength)
                lineTo(left, top + scanAreaSize - cornerRadius)
                quadraticBezierTo(left, top + scanAreaSize, left + cornerRadius, top + scanAreaSize)
                lineTo(left + cornerLength, top + scanAreaSize)
            },
            color = activeColor,
            style = Stroke(width = cornerWidth)
        )
        
        // Bottom Right Corner Glow
        drawPath(
            path = Path().apply {
                moveTo(left + scanAreaSize, top + scanAreaSize - cornerLength)
                lineTo(left + scanAreaSize, top + scanAreaSize - cornerRadius)
                quadraticBezierTo(left + scanAreaSize, top + scanAreaSize, left + scanAreaSize - cornerRadius, top + scanAreaSize)
                lineTo(left + scanAreaSize - cornerLength, top + scanAreaSize)
            },
            color = glowColor,
            style = Stroke(width = glowWidth)
        )
        
        // Bottom Right Corner
        drawPath(
            path = Path().apply {
                moveTo(left + scanAreaSize, top + scanAreaSize - cornerLength)
                lineTo(left + scanAreaSize, top + scanAreaSize - cornerRadius)
                quadraticBezierTo(left + scanAreaSize, top + scanAreaSize, left + scanAreaSize - cornerRadius, top + scanAreaSize)
                lineTo(left + scanAreaSize - cornerLength, top + scanAreaSize)
            },
            color = activeColor,
            style = Stroke(width = cornerWidth)
        )
        
        // ===================
        // 3. Draw Animated Laser Line with Enhanced Glow
        // ===================
        
        val padding = cornerWidth * 2
        val laserY = top + padding + ((scanAreaSize - padding * 2) * laserOffset)
        val laserHeight = 3.dp.toPx()
        val gradientHeight = 40.dp.toPx()
        
        // Laser outer glow (wide gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    laserColor.copy(alpha = 0.15f),
                    laserColor.copy(alpha = 0.4f),
                    laserColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                startY = laserY - gradientHeight,
                endY = laserY + gradientHeight
            ),
            topLeft = Offset(left + padding, laserY - gradientHeight),
            size = Size(scanAreaSize - padding * 2, gradientHeight * 2)
        )
        
        // Laser inner glow (narrow gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    laserColor.copy(alpha = 0.6f),
                    laserColor.copy(alpha = 1f),
                    laserColor.copy(alpha = 0.6f),
                    Color.Transparent
                ),
                startY = laserY - gradientHeight / 3,
                endY = laserY + gradientHeight / 3
            ),
            topLeft = Offset(left + padding, laserY - gradientHeight / 3),
            size = Size(scanAreaSize - padding * 2, gradientHeight / 1.5f)
        )
        
        // Laser core line (solid)
        drawRoundRect(
            color = laserColor,
            topLeft = Offset(left + padding, laserY - laserHeight / 2),
            size = Size(scanAreaSize - padding * 2, laserHeight),
            cornerRadius = CornerRadius(laserHeight / 2)
        )
    }
}
