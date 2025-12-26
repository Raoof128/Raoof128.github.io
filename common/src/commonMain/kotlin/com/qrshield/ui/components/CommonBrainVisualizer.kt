package com.qrshield.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

/**
 * Premium AI Shield Visualizer
 * Features:
 * - Smooth organic node drift (non-chaotic)
 * - Neon glowing aesthetics with gradients
 * - Hex-grid background
 * - Shield scanner effect
 * - Robust rendering (no physics glitches)
 */
@Composable
fun CommonBrainVisualizer(
    detectedSignals: List<String>,
    modifier: Modifier = Modifier
) {
    val description = remember(detectedSignals) {
        if (detectedSignals.isEmpty()) {
            "AI Shield: System integrity 100%. Scanning for threats."
        } else {
            "AI Shield: Alert! ${detectedSignals.size} threats detected in scan."
        }
    }

    Column(
        modifier = modifier.semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PremiumShieldCanvas(
            hasThreats = detectedSignals.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        
        if (detectedSignals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            DetectedSignalsBadges(detectedSignals)
        }
    }
}

// Premium Color Palette
private val NeonCyan = Color(0xFF00F0FF)
private val NeonBlue = Color(0xFF00A3FF)
private val DeepPurple = Color(0xFF7000FF)
private val DangerRed = Color(0xFFFF2A2A)
private val DangerOrange = Color(0xFFFF7A00)
private val GridColor = Color(0xFFFFFFFF).copy(alpha = 0.05f)

@Composable
private fun PremiumShieldCanvas(
    hasThreats: Boolean,
    modifier: Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shield_main")
    
    // Smooth pulsing global phase
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    
    // Scanner sweep
    val scannerProgress by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart // One way sweep looks more like a scanner
        ),
        label = "scanner"
    )

    // Generate drift paths for nodes once
    val nodes = remember { generateSmoothNodes(65) }
    
    // Dynamic Colors
    val primaryColor = if (hasThreats) DangerRed else NeonCyan
    val secondaryColor = if (hasThreats) DangerOrange else NeonBlue
    val glowBrush = remember(hasThreats) {
        Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.6f),
                primaryColor.copy(alpha = 0.0f)
            )
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        // Shift up more to center in card
        val centerY = height * 0.35f
        // Shield scale
        val scale = minOf(width, height) * 0.42f 

        // 1. Create Shield Geometry
        val shieldPath = createShieldPath(centerX, centerY, scale)
        val shieldBounds = Rect(centerX - scale, centerY - scale, centerX + scale, centerY + scale)

        // 2. Draw Background (Hex Grid inside Shield)
        // Use clipPath to perfectly contain the grid
        clipPath(shieldPath) {
            // Fill dark glass background
            drawRect(
                color = Color(0xFF020617).copy(alpha = 0.6f), 
                topLeft = Offset(0f, 0f), 
                size = size
            )
            
            // Draw Hex Grid
            drawHexPattern(width, height, GridColor)
            
            // 3. Draw Nodes & Connections
            // We calculate node positions based on time (pulsePhase) for smooth drift
            // instead of unstable physics integration
            
            val currentNodes = nodes.map { node ->
                // Orbit/Drift logic:
                // Base position + circular motion offset + breathing scale
                val t = pulsePhase + node.phaseOffset
                // Reduced drift range and speed influence
                val driftX = sin(t) * node.driftRadius * scale * 0.05f
                val driftY = cos(t * 0.7f) * node.driftRadius * scale * 0.05f
                
                val lx = centerX + (node.baseX * scale * 0.85f) + driftX
                val ly = centerY + (node.baseY * scale * 0.85f * 0.6f) + driftY // Flatten Y for perspective
                
                Offset(lx, ly) to node
            }
            
            // Draw Connections (Distance based)
            val connectionDistance = scale * 0.3f
            val connectionThresholdSq = connectionDistance * connectionDistance
            
            currentNodes.forEachIndexed { i, (pos1, node1) ->
                // Check neighbors
                for (j in (i + 1) until currentNodes.size) {
                    val (pos2, _) = currentNodes[j]
                    
                    val dx = pos1.x - pos2.x
                    val dy = pos1.y - pos2.y
                    val distSq = dx*dx + dy*dy
                    
                    if (distSq < connectionThresholdSq) {
                        val dist = sqrt(distSq)
                        val alpha = (1f - dist / connectionDistance) * 0.4f
                        
                        drawLine(
                            color = secondaryColor.copy(alpha = alpha),
                            start = pos1,
                            end = pos2,
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                
                // Draw Node - much smaller, cleaner look
                val nodeSize = (1.5.dp.toPx() + node1.size * 2.dp.toPx())
                
                // Subtle glow halo
                drawCircle(
                    color = primaryColor.copy(alpha = 0.3f),
                    radius = nodeSize * 1.8f,
                    center = pos1
                )
                
                // Main node body
                drawCircle(
                    color = primaryColor.copy(alpha = 0.8f),
                    radius = nodeSize,
                    center = pos1
                )
                
                // Bright core
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = nodeSize * 0.4f,
                    center = pos1
                )
            }
            
            // 4. Scanner Beam (Clipped inside shield)
            val scanX = centerX - scale * 1.5f + (scale * 3f) * scannerProgress
            val beamWidth = 60.dp.toPx()
            
            // Gradient Beam
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.0f),
                        primaryColor.copy(alpha = 0.1f),
                        primaryColor.copy(alpha = 0.3f), // Leading edge
                        Color.White.copy(alpha = 0.6f),  // Bright line
                        primaryColor.copy(alpha = 0.0f)
                    ),
                    startX = scanX - beamWidth,
                    endX = scanX + 2.dp.toPx()
                ),
                topLeft = Offset(scanX - beamWidth, 0f),
                size = androidx.compose.ui.geometry.Size(beamWidth, height)
            )
        }

        // 5. Draw Shield Border (Crisp on top)
        drawPath(
            path = shieldPath,
            color = primaryColor.copy(alpha = 0.9f),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
        
        // Outer Border Glow
        drawPath(
            path = shieldPath,
            color = primaryColor.copy(alpha = 0.4f),
            style = Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

// --- Smooth drifting helpers ---

private data class SmoothNode(
    val baseX: Float,       // -1 to 1
    val baseY: Float,       // -1 to 1
    val driftRadius: Float, // 0 to 1
    val phaseOffset: Float, // 0 to 2PI
    val size: Float         // 0 to 1
)

private fun generateSmoothNodes(count: Int): List<SmoothNode> {
    val random = Random(1337)
    val nodes = mutableListOf<SmoothNode>()
    repeat(count) {
        // Distribute in a cylinder/shield shape volume
        val angle = random.nextFloat() * 2 * PI.toFloat()
        val r = sqrt(random.nextFloat()) // Bias towards outside for volume, or uniform?
        val y = (random.nextFloat() * 2 - 1) * 0.9f
        
        // Keep inside rough shield bounds (Top wide, bottom narrow)
        val maxWidthAtY = if (y < 0.2f) 1f else (1.1f - y)
        val x = (random.nextFloat() * 2 - 1) * maxWidthAtY * 0.9f
        
        nodes.add(SmoothNode(
            baseX = x,
            baseY = y,
            driftRadius = 0.5f + random.nextFloat(),
            phaseOffset = random.nextFloat() * 2 * PI.toFloat(),
            size = random.nextFloat()
        ))
    }
    return nodes
}

private fun createShieldPath(cx: Float, cy: Float, s: Float): Path {
    return Path().apply {
        // Precise Shield Geometry
        moveTo(cx - s, cy - s * 0.5f) // Top Left
        lineTo(cx + s, cy - s * 0.5f) // Top Right
        
        // Side curves to bottom tip
        cubicTo(
            cx + s, cy + s * 0.2f,        
            cx + s * 0.7f, cy + s * 0.9f, 
            cx, cy + s * 1.25f            // Bottom Tip
        )
        cubicTo(
            cx - s * 0.7f, cy + s * 0.9f, 
            cx - s, cy + s * 0.2f,        
            cx - s, cy - s * 0.5f         // Back to Top Left
        )
        close()
    }
}

private fun DrawScope.drawHexPattern(width: Float, height: Float, color: Color) {
    val hexSize = 25.dp.toPx()
    val dx = hexSize * 1.5f
    val dy = hexSize * sqrt(3f)
    
    // Draw simple grid points or lines for hex effect
    // Full hex drawing is expensive, we'll use a staggered dot/cross pattern which hints at hex
    val cols = (width / dx).toInt() + 2
    val rows = (height / dy).toInt() + 2
    
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            val xOffset = if (row % 2 == 1) dx * 0.5f else 0f
            val x = col * dx + xOffset - dx
            val y = row * dy * 0.5f - dy // Compress Y for density
            
            drawCircle(
                color = color,
                radius = 1.dp.toPx(),
                center = Offset(x, y)
            )
            
            // Draw faint lines between neighbors to form "web"
            if (col < cols - 1) {
                 drawLine(
                    color = color.copy(alpha = 0.3f),
                    start = Offset(x, y),
                    end = Offset(x + dx, y),
                    strokeWidth = 0.5f
                 )
            }
        }
    }
}

@Composable
private fun DetectedSignalsBadges(signals: List<String>) {
    val errorColor = MaterialTheme.colorScheme.error
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        signals.take(4).forEach { signal ->
            Surface(
                color = errorColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, errorColor.copy(alpha = 0.3f)),
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = signal.replace("_", " "),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = errorColor
                )
            }
        }
    }
}
