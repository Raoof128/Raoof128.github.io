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

package com.qrshield.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.qrshield.core.PhishingEngine
import com.qrshield.model.Verdict
import java.io.File
import java.util.Properties

// ============================================
// QR-SHIELD DESIGN SYSTEM
// ============================================

// Brand Colors
private val BrandPrimary = Color(0xFF7F52FF)  // Kotlin Purple
private val BrandSecondary = Color(0xFFA78BFA)
private val BrandAccent = Color(0xFF00D9FF)

// Verdict Colors
private val VerdictSafe = Color(0xFF22C55E)      // Green
private val VerdictSuspicious = Color(0xFFF59E0B) // Amber
private val VerdictMalicious = Color(0xFFEF4444)  // Red

// Dark Theme Colors
private val DarkBackground = Color(0xFF0F0F0F)
private val DarkSurface = Color(0xFF1A1A1A)
private val DarkSurfaceVariant = Color(0xFF262626)
private val DarkTextPrimary = Color(0xFFFFFFFF)
private val DarkTextSecondary = Color(0xFFB3B3B3)

// Light Theme Colors  
private val LightBackground = Color(0xFFF8F9FA)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF0F0F0)
private val LightTextPrimary = Color(0xFF1A1A1A)
private val LightTextSecondary = Color(0xFF4A4A4A)

// Custom Color Schemes
private val QRShieldDarkColors = darkColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandAccent,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    error = VerdictMalicious
)

private val QRShieldLightColors = lightColorScheme(
    primary = BrandPrimary,
    secondary = BrandSecondary,
    tertiary = BrandAccent,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    error = VerdictMalicious
)

// ============================================
// MAIN APPLICATION
// ============================================

/**
 * QR-SHIELD Desktop Application Entry Point
 * 
 * Features:
 * - Dark/Light theme toggle with persistence
 * - Window size/position persistence
 * - Cross-platform preferences storage
 * - Modern Material 3 design
 */
fun main() = application {
    val prefs = WindowPreferences.load()
    
    val windowState = rememberWindowState(
        size = DpSize(prefs.width.dp, prefs.height.dp),
        position = if (prefs.x >= 0 && prefs.y >= 0) {
            androidx.compose.ui.window.WindowPosition(prefs.x.dp, prefs.y.dp)
        } else {
            androidx.compose.ui.window.WindowPosition.PlatformDefault
        }
    )
    
    Window(
        onCloseRequest = {
            WindowPreferences.save(
                width = windowState.size.width.value.toInt(),
                height = windowState.size.height.value.toInt(),
                x = windowState.position.x.value.toInt(),
                y = windowState.position.y.value.toInt()
            )
            exitApplication()
        },
        title = "🛡️ QR-SHIELD - QRishing Detector",
        state = windowState
    ) {
        QRShieldDesktopApp(initialDarkMode = prefs.darkMode)
    }
}

/**
 * Window preferences persistence for Desktop.
 */
object WindowPreferences {
    private const val PREFS_FILE = "qrshield_window.properties"
    private const val DEFAULT_WIDTH = 460
    private const val DEFAULT_HEIGHT = 820
    
    data class Prefs(
        val width: Int,
        val height: Int,
        val x: Int,
        val y: Int,
        val darkMode: Boolean
    )
    
    fun load(): Prefs {
        return try {
            val file = getPrefsFile()
            if (file.exists()) {
                val props = Properties()
                file.inputStream().use { props.load(it) }
                Prefs(
                    width = props.getProperty("width", DEFAULT_WIDTH.toString()).toInt(),
                    height = props.getProperty("height", DEFAULT_HEIGHT.toString()).toInt(),
                    x = props.getProperty("x", "-1").toInt(),
                    y = props.getProperty("y", "-1").toInt(),
                    darkMode = props.getProperty("darkMode", "true").toBoolean()
                )
            } else {
                Prefs(DEFAULT_WIDTH, DEFAULT_HEIGHT, -1, -1, true)
            }
        } catch (e: Exception) {
            Prefs(DEFAULT_WIDTH, DEFAULT_HEIGHT, -1, -1, true)
        }
    }
    
    fun save(width: Int, height: Int, x: Int, y: Int, darkMode: Boolean = true) {
        try {
            val file = getPrefsFile()
            file.parentFile?.mkdirs()
            
            val props = Properties()
            props.setProperty("width", width.toString())
            props.setProperty("height", height.toString())
            props.setProperty("x", x.toString())
            props.setProperty("y", y.toString())
            props.setProperty("darkMode", darkMode.toString())
            
            file.outputStream().use { props.store(it, "QR-SHIELD Window Preferences") }
        } catch (e: Exception) {
            // Silently fail on save error
        }
    }
    
    private fun getPrefsFile(): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        
        val appDataDir = when {
            os.contains("windows") -> "${System.getenv("APPDATA")}/QRShield"
            os.contains("mac") -> "$userHome/Library/Application Support/QRShield"
            else -> "$userHome/.config/qrshield"
        }
        
        return File(appDataDir, PREFS_FILE)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun QRShieldDesktopApp(initialDarkMode: Boolean = true) {
    var isDarkMode by remember { mutableStateOf(initialDarkMode) }
    
    // Simple phishing engine for desktop - direct usage
    val phishingEngine = remember { PhishingEngine() }
    
    var urlInput by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
    
    MaterialTheme(
        colorScheme = if (isDarkMode) QRShieldDarkColors else QRShieldLightColors
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top App Bar with gradient
                TopAppBar(
                    isDarkMode = isDarkMode,
                    onThemeToggle = { isDarkMode = !isDarkMode }
                )
                
                // Main Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Hero Section
                    HeroSection()
                    
                    // Scanner Card
                    ScannerCard(
                        urlInput = urlInput,
                        onUrlChange = { urlInput = it },
                        onAnalyze = {
                            if (urlInput.isNotBlank()) {
                                val assessment = phishingEngine.analyze(urlInput)
                                analysisResult = AnalysisResult(
                                    url = urlInput,
                                    score = assessment.score,
                                    verdict = assessment.verdict,
                                    flags = assessment.flags
                                )
                            }
                        }
                    )
                    
                    // Results
                    analysisResult?.let { result ->
                        ResultCard(result = result)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Features Grid
                    FeaturesGrid()
                    
                    // Footer
                    Footer()
                }
            }
        }
    }
}

@Composable
private fun TopAppBar(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(BrandPrimary, BrandSecondary)
                )
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🛡️",
                    fontSize = 24.sp
                )
                Text(
                    text = "QR-SHIELD",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            // Theme Toggle Button
            Button(
                onClick = onThemeToggle,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = if (isDarkMode) "☀️ Light" else "🌙 Dark",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun HeroSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "QRishing Detector",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Scan URLs safely with AI-powered phishing detection",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ScannerCard(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    onAnalyze: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🔗",
                    fontSize = 20.sp
                )
                Text(
                    text = "Enter URL to analyze",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // URL Input
            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                placeholder = { 
                    Text(
                        "https://example.com",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )
            
            // Analyze Button
            Button(
                onClick = onAnalyze,
                enabled = urlInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    disabledContainerColor = BrandPrimary.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔍",
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Analyze URL",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: AnalysisResult) {
    val verdictColor = when (result.verdict) {
        Verdict.SAFE -> VerdictSafe
        Verdict.SUSPICIOUS -> VerdictSuspicious
        Verdict.MALICIOUS -> VerdictMalicious
        Verdict.UNKNOWN -> MaterialTheme.colorScheme.outline
    }
    
    val verdictEmoji = when (result.verdict) {
        Verdict.SAFE -> "✅"
        Verdict.SUSPICIOUS -> "⚠️"
        Verdict.MALICIOUS -> "❌"
        Verdict.UNKNOWN -> "❓"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = verdictColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = verdictColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Verdict Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(verdictColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = verdictEmoji,
                    fontSize = 40.sp
                )
            }
            
            // Score
            Text(
                text = result.score.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = verdictColor
            )
            
            // Risk Score Label
            Text(
                text = "Risk Score",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Verdict Badge
            Surface(
                color = verdictColor,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = result.verdict.name,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            // URL
            Text(
                text = result.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Flags
            if (result.flags.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Risk Factors Detected:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    result.flags.forEach { flag ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 14.sp
                            )
                            Text(
                                text = flag,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturesGrid() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Powered by",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureChip(
                icon = "🔍",
                title = "25+ Heuristics",
                modifier = Modifier.weight(1f)
            )
            FeatureChip(
                icon = "🤖",
                title = "ML Scoring",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureChip(
                icon = "🏷️",
                title = "Brand Detection",
                modifier = Modifier.weight(1f)
            )
            FeatureChip(
                icon = "🔒",
                title = "100% Offline",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FeatureChip(
    icon: String,
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Footer() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Built with Kotlin Multiplatform",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = "🛡️ QR-SHIELD v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = BrandPrimary
        )
    }
}

data class AnalysisResult(
    val url: String,
    val score: Int,
    val verdict: Verdict,
    val flags: List<String>
)
