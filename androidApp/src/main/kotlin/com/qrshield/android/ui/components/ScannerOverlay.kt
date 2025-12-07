package com.qrshield.android.ui.components

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Animated Scanner Overlay
 * 
 * Draws:
 * 1. Corner brackets to define scan area
 * 2. Animated laser line sweeping up/down
 * 3. Subtle glow effects for premium feel
 * 
 * Design inspired by iOS Liquid Glass aesthetic.
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
    scanAreaRatio: Float = 0.7f,
    cornerColor: Color = Color(0xFF00D68F), // Neon Green
    laserColor: Color = Color(0xFF00D68F)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_animation")
    
    // Animate the laser line moving up and down
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_sweep"
    )
    
    // Subtle pulse for corners
    val cornerPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corner_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val scanAreaSize = width * scanAreaRatio
        val left = (width - scanAreaSize) / 2
        val top = (height - scanAreaSize) / 2
        
        // Corner dimensions
        val cornerLength = 50.dp.toPx()
        val cornerWidth = 5.dp.toPx()
        val cornerRadius = 8.dp.toPx()
        
        val activeColor = cornerColor.copy(alpha = cornerPulse)
        
        // ===================
        // 1. Draw Corners
        // ===================
        
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
        // 2. Draw Laser Line
        // ===================
        
        val laserY = top + (scanAreaSize * laserOffset)
        val laserHeight = 4.dp.toPx()
        val gradientHeight = 30.dp.toPx()
        
        // Laser glow (gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    laserColor.copy(alpha = 0.3f),
                    laserColor.copy(alpha = 0.8f),
                    laserColor.copy(alpha = 0.3f),
                    Color.Transparent
                ),
                startY = laserY - gradientHeight / 2,
                endY = laserY + gradientHeight / 2
            ),
            topLeft = Offset(left + cornerWidth, laserY - gradientHeight / 2),
            size = Size(scanAreaSize - cornerWidth * 2, gradientHeight)
        )
        
        // Laser core (solid line)
        drawRect(
            color = laserColor.copy(alpha = 0.9f),
            topLeft = Offset(left + cornerWidth, laserY - laserHeight / 2),
            size = Size(scanAreaSize - cornerWidth * 2, laserHeight)
        )
        
        // ===================
        // 3. Dim Outside Area
        // ===================
        
        val dimColor = Color.Black.copy(alpha = 0.5f)
        
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
    }
}
