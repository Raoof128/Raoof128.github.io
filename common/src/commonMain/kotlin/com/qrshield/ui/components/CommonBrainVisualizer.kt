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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

/**
 * A "Brain" visualization that lights up specific neural clusters based on detected signals.
 * Designed for immediate visual feedback in the "Beat The Bot" game.
 *
 * This component is shared between Android and Desktop (KMP).
 *
 * @param detectedSignals List of signal strings (e.g. "TLD_ABUSE", "BRAND_IMPERSONATION")
 * @param modifier Modifier for layout
 */
@Composable
fun CommonBrainVisualizer(
    detectedSignals: List<String>,
    modifier: Modifier = Modifier
) {
    // Accessibility description
    val description = remember(detectedSignals) {
        if (detectedSignals.isEmpty()) {
            "AI Neural Net: No threats detected. Brain pattern is calm and blue."
        } else {
            "AI Neural Net: Active alert. Detected signals: ${detectedSignals.joinToString(", ")}. Brain pattern is pulsing red."
        }
    }

    Column(
        modifier = modifier
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The Brain Canvas
        BrainCanvas(
            signals = detectedSignals,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .graphicsLayer {
                    // Use hardware layer for better animation performance
                    clip = true
                }
        )
        
        // Text explanation
        if (detectedSignals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            DetectedSignalsBadges(detectedSignals)
        }
    }
}

@Composable
private fun BrainCanvas(
    signals: List<String>,
    modifier: Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "brain_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Memoize node structure so we don't recreate it on every frame
    val nodes = remember { generateBrainNodes(count = 80) }
    
    // Map signals to active node indices
    val activeIndices = remember(signals) {
        mapSignalsToNodes(signals, nodes.size)
    }

    // Color definitions from Material Theme for KMP compatibility
    val safeColor = MaterialTheme.colorScheme.primary
    val dangerColor = MaterialTheme.colorScheme.error
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2

        // Draw connections first (background)
        nodes.forEachIndexed { index, node ->
            // Only draw connections for a subset to avoid clutter
            if (index % 3 == 0) {
                // Connect to a few nearby nodes (simulated by index proximity for performance)
                val neighborIndex = (index + 3) % nodes.size
                val neighbor = nodes[neighborIndex]
                
                val isActive = index in activeIndices || neighborIndex in activeIndices
                val lineColor = if (isActive && signals.isNotEmpty()) dangerColor.copy(alpha = 0.3f) 
                                else inactiveColor.copy(alpha = 0.1f)
                
                drawLine(
                    color = lineColor,
                    start = Offset(
                        centerX + node.x * width * 0.4f,
                        centerY + node.y * height * 0.4f
                    ),
                    end = Offset(
                        centerX + neighbor.x * width * 0.4f,
                        centerY + neighbor.y * height * 0.4f
                    ),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Draw nodes
        nodes.forEachIndexed { index, node ->
            val isActive = index in activeIndices
            
            // Calculate dynamic position (subtle floating)
            val floatOffset = sin(pulsePhase + node.phaseOffset) * 5f
            
            val x = centerX + node.x * width * 0.4f
            val y = centerY + node.y * height * 0.4f + floatOffset

            // Determine color and size
            val color = when {
                signals.isEmpty() -> safeColor.copy(alpha = 0.6f) // Idle state
                isActive -> dangerColor // Active signal state
                else -> inactiveColor // Inactive background nodes
            }
            
            val baseRadius = if (isActive) 6.dp.toPx() else 3.dp.toPx()
            
            // Pulse effect for active nodes
            val pulseScale = if (isActive) {
                1f + 0.3f * sin(pulsePhase * 2 + node.phaseOffset)
            } else {
                1f
            }

            drawCircle(
                color = color,
                radius = baseRadius * pulseScale,
                center = Offset(x, y)
            )
            
            // Draw ripple for active nodes
            if (isActive) {
                val rippleRadius = baseRadius * pulseScale * 2.5f
                drawCircle(
                    color = color.copy(alpha = 0.2f),
                    radius = rippleRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun DetectedSignalsBadges(signals: List<String>) {
    val errorColor = MaterialTheme.colorScheme.error
    
    // Use FlowRow if available in newer Compose, but Row is safer for older versions
    // For now, we'll wrap in a Row with horizontal scrolling if needed, or simple wrapping isn't easy without FlowRow
    // Let's use a simple Row for MVP centered
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        signals.forEach { signal ->
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

// Data Classes & Helpers

private data class BrainNode(
    val x: Float, // Normalized -1 to 1
    val y: Float, // Normalized -1 to 1
    val phaseOffset: Float
)

private fun generateBrainNodes(count: Int): List<BrainNode> {
    val random = Random(12345) // Fixed seed for stable visual
    return List(count) {
        // Generate points in a somewhat circular/brain-like cluster
        // Using rejection sampling for a nicer distribution
        var x: Float
        var y: Float
        do {
            x = (random.nextFloat() * 2) - 1
            y = (random.nextFloat() * 2) - 1
        } while (x * x + y * y > 1f) // Keep inside unit circle
        
        BrainNode(
            x = x,
            y = y,
            phaseOffset = random.nextFloat() * 2 * PI.toFloat()
        )
    }
}

private fun mapSignalsToNodes(signals: List<String>, nodeCount: Int): Set<Int> {
    if (signals.isEmpty()) return emptySet()
    
    val activeNodes = mutableSetOf<Int>()
    val random = Random(signals.hashCode()) // Deterministic based on signals
    
    // For each signal, light up a cluster of nodes
    signals.forEach { signal ->
        // Hash signal to a "center" node index
        val centerIndex = kotlin.math.abs(signal.hashCode()) % nodeCount
        
        // Add the center and neighbors
        activeNodes.add(centerIndex)
        // Add 5-10 nearby nodes
        repeat(8) {
            val neighbor = (centerIndex + random.nextInt(-10, 10)).coerceIn(0, nodeCount - 1)
            activeNodes.add(neighbor)
        }
    }
    return activeNodes
}
