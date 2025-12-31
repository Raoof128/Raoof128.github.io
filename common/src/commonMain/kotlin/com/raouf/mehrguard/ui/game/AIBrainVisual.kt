package com.raouf.mehrguard.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * AI Brain Visualization
 *
 * Visually represents the AI's "neural activity" by mapping detected signals
 * to clusters of nodes that pulse and activate.
 *
 * @param detectedSignals List of detection flags (e.g., "TLD", "Brand Spoof")
 * @param modifier Modifier for layout
 */
@Composable
fun AIBrainVisual(
    detectedSignals: List<String>,
    modifier: Modifier = Modifier
) {
    // Neural grid configuration
    val nodes = remember { generateBrainNodes(50) }
    val connections = remember { generateConnections(nodes) }
    
    // Identify active clusters based on signals
    val activeClusters = remember(detectedSignals) {
        val clusters = mutableSetOf<BrainCluster>()
        val upperSignals = detectedSignals.map { it.uppercase() }
        
        if (upperSignals.any { "TLD" in it || "IP" in it || "SHORTENER" in it }) {
            clusters.add(BrainCluster.TLD)
        }
        if (upperSignals.any { "BRAND" in it || "IMPERSONATION" in it || "HOMOGRAPH" in it }) {
            clusters.add(BrainCluster.BRAND)
        }
        if (upperSignals.any { "ML" in it || "MODEL" in it }) {
            clusters.add(BrainCluster.ML)
        }
        // If unknown signals are present, activate simple heuristic cluster
        if (clusters.isEmpty() && detectedSignals.isNotEmpty()) {
            clusters.add(BrainCluster.HEURISTIC)
        }
        clusters
    }

    // Animation for pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "brain_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Rotation for "alive" feel
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The Brain Visual
        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer { rotationZ = rotation } // Slow background rotation
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 * 0.8f

                // Draw connections first (background)
                connections.forEach { (start, end) ->
                    val startNode = nodes[start]
                    val endNode = nodes[end]
                    
                    val isActive = startNode.cluster in activeClusters || endNode.cluster in activeClusters
                    val lineColor = if (isActive) Color(0xFFF87171) else Color(0xFF334155) // Red or Slate
                    val lineAlpha = if (isActive) pulse * 0.6f else 0.2f
                    
                    drawLine(
                        color = lineColor,
                        start = center + Offset(
                            startNode.x * radius,
                            startNode.y * radius
                        ),
                        end = center + Offset(
                            endNode.x * radius,
                            endNode.y * radius
                        ),
                        strokeWidth = 1f,
                        alpha = lineAlpha
                    )
                }

                // Draw nodes
                nodes.forEach { node ->
                    val isActive = node.cluster in activeClusters
                    
                    val nodeColor = when {
                        isActive -> Color(0xFFEF4444) // Red-500
                        else -> Color(0xFF94A3B8)     // Slate-400
                    }
                    
                    val nodeRadius = if (isActive) 4.dp.toPx() * pulse else 2.dp.toPx()
                    val position = center + Offset(node.x * radius, node.y * radius)
                    
                    drawCircle(
                        color = nodeColor,
                        radius = nodeRadius,
                        center = position,
                        alpha = if (isActive) 1f else 0.4f
                    )
                    
                    // Ripple effect for active nodes
                    if (isActive) {
                        drawCircle(
                            color = nodeColor,
                            radius = nodeRadius * (1f + (1f - pulse) * 2f), // Ripple out
                            center = position,
                            style = Stroke(width = 1f),
                            alpha = (pulse - 0.5f) * 2f // Fade out
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compact Explanations
        if (activeClusters.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (BrainCluster.TLD in activeClusters) BrainBadge("Suspicious TLD", Color(0xFFF87171))
                if (BrainCluster.BRAND in activeClusters) BrainBadge("Brand Spoof", Color(0xFFF87171))
                if (BrainCluster.ML in activeClusters) BrainBadge("AI Logic", Color(0xFFF87171))
                if (BrainCluster.HEURISTIC in activeClusters) BrainBadge("Heuristics", Color(0xFFF87171))
            }
        } else if (detectedSignals.isEmpty()) {
            Text(
                "No Threat Signals Detected",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Green.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun BrainBadge(text: String, color: Color) {
    androidx.compose.material3.Surface(
        color = color.copy(alpha = 0.1f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// Data structures & Generators

private enum class BrainCluster {
    TLD, BRAND, ML, HEURISTIC
}

private data class Node(val id: Int, val x: Float, val y: Float, val cluster: BrainCluster)

private fun generateBrainNodes(count: Int): List<Node> {
    val random = Random(12345) // Fixed seed for consistent "brain" shape
    return List(count) { id ->
        // Generate points in a circle but clustered
        // Cluster distribution:
        // TLD: Top Right (Quadrant 1)
        // BRAND: Top Left (Quadrant 2)
        // ML: Bottom (Quadrants 3 & 4)
        // HEURISTIC: Center
        
        val cluster = when(id % 4) {
            0 -> BrainCluster.TLD
            1 -> BrainCluster.BRAND
            2 -> BrainCluster.ML
            else -> BrainCluster.HEURISTIC
        }
        
        var angle = 0f
        var dist = 0f
        
        when (cluster) {
            BrainCluster.TLD -> {
                angle = random.nextFloat() * 1.57f // 0 to 90 deg (Top Right)
                dist = 0.5f + random.nextFloat() * 0.5f // Outer rim
            }
            BrainCluster.BRAND -> {
                angle = 1.57f + random.nextFloat() * 1.57f // 90 to 180 deg (Bottom Right in Compose coords?) No, standard trig.
                // Let's simpler mapping: x, y ranges
                // Actually simple angle ranges:
                // 0..PI/2 = Top Right (ish)
            }
            BrainCluster.ML -> {
                angle = 3.14f + random.nextFloat() * 3.14f // Bottom half
                dist = 0.4f + random.nextFloat() * 0.6f
            }
            BrainCluster.HEURISTIC -> {
                angle = random.nextFloat() * 6.28f
                dist = random.nextFloat() * 0.5f // Inner circle
            }
        }
        
        // Override simplistic angle logic for visual separation
        val finalAngle = when(cluster) {
            BrainCluster.TLD -> (0.0..1.5).random(random) // Top Right
            BrainCluster.BRAND -> (4.7..6.2).random(random) // Top Left (approx in standard coords, let's test)
            BrainCluster.ML -> (2.0..4.2).random(random) // Bottom
            BrainCluster.HEURISTIC -> (0.0..6.28).random(random) // Any
        }
        
        val finalDist = if (cluster == BrainCluster.HEURISTIC) random.nextFloat() * 0.4f else 0.5f + random.nextFloat() * 0.5f
        
        val x = cos(finalAngle).toFloat() * finalDist
        val y = sin(finalAngle).toFloat() * finalDist
        
        Node(id, x, y, cluster)
    }
}

private fun generateConnections(nodes: List<Node>): List<Pair<Int, Int>> {
    val connections = mutableListOf<Pair<Int, Int>>()
    // Connect reliable nodes to their nearest neighbors to form a mesh
    // Simple heuristic: distance check
    nodes.forEachIndexed { i, nodeA ->
        nodes.forEachIndexed { j, nodeB ->
            if (i < j) {
                val dx = nodeA.x - nodeB.x
                val dy = nodeA.y - nodeB.y
                val distSq = dx * dx + dy * dy
                if (distSq < 0.15f) { // Threshold for connection
                    connections.add(i to j)
                }
            }
        }
    }
    return connections
}

private fun ClosedFloatingPointRange<Double>.random(random: Random): Double {
    return start + random.nextDouble() * (endInclusive - start)
}
