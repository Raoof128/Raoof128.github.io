package com.qrshield.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Text that descrambles itself like a movie terminal.
 */
@Composable
fun HackerText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    var displayedText by remember(text) { mutableStateOf("") }
    val characters = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+"
    
    LaunchedEffect(text) {
        val targetLength = text.length
        for (i in 0..targetLength) {
            // Scramble effect
            repeat(3) {
                val partial = text.take(i)
                val randomSuffix = (1..(targetLength - i)).map { characters.random() }.joinToString("")
                displayedText = partial + randomSuffix
                delay(30) // Speed of scrambling
            }
            displayedText = text.take(i) + (1..(targetLength - i)).map { characters.random() }.joinToString("")
        }
        displayedText = text
    }

    Text(
        text = displayedText,
        modifier = modifier,
        color = color,
        style = style.copy(fontFamily = FontFamily.Monospace)
    )
}

/**
 * Particle explosion effect for successful hacks.
 */
@Composable
fun ParticleSystem(
    trigger: Int,
    color: Color = Color.Green
) {
    val particles = remember { List(20) { Particle() } }
    val animatables = remember { particles.map { Animatable(0f) } }
    
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            animatables.forEachIndexed { index, anim ->
                particles[index].reset()
                anim.snapTo(0f)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
                )
            }
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        animatables.forEachIndexed { index, anim ->
            if (anim.value > 0f && anim.value < 1f) {
                val p = particles[index]
                val currentRadius = p.maxRadius * anim.value
                val currentAlpha = 1f - anim.value
                val currentPos = p.initialPos + (p.velocity * (anim.value * 500f))
                
                drawCircle(
                    color = color.copy(alpha = currentAlpha),
                    radius = 5.dp.toPx() * (1f - anim.value),
                    center = currentPos
                )
            }
        }
    }
}

private class Particle {
    var initialPos = Offset(0f, 0f)
    var velocity = Offset(0f, 0f)
    var maxRadius = 0f
    
    fun reset() {
        // Randomize
        val angle = Random.nextFloat() * 2 * kotlin.math.PI
        val speed = Random.nextFloat() * 2 + 1
        velocity = Offset(
            x = (kotlin.math.cos(angle) * speed).toFloat(),
            y = (kotlin.math.sin(angle) * speed).toFloat()
        )
        // Set center (will be updated in draw based on screen size ideally, but we'll assume center logic in usage)
        // For simplicity, we assume drawing happens relative to the container center in usage if properly offset
        initialPos = Offset(Random.nextInt(100, 300).toFloat(), Random.nextInt(100, 300).toFloat()) // Placeholder, better to pass size
    }
}

/**
 * Animated score counter.
 */
@Composable
fun AnimatedScore(
    score: Int,
    label: String,
    color: Color
) {
    val animatedScore = animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1000)
    )
    
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${animatedScore.value}",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
            color = color
        )
    }
}
