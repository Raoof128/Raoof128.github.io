package com.qrshield.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.qrshield.core.PhishingEngine
import com.qrshield.data.DatabaseDriverFactory
import com.qrshield.data.SqlDelightHistoryRepository
import com.qrshield.db.QRShieldDatabase
import com.qrshield.model.Verdict
import com.qrshield.ui.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.util.Properties

/**
 * QR-SHIELD Desktop Application Entry Point
 * 
 * Features:
 * - Window size/position persistence
 * - Cross-platform preferences storage
 * - Persistent scan history via SQLDelight
 */
fun main() = application {
    // Load saved window preferences
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
            // Save window state before exit
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
        QRShieldDesktopApp()
    }
}

/**
 * Window preferences persistence for Desktop.
 */
object WindowPreferences {
    private const val PREFS_FILE = "qrshield_window.properties"
    private const val DEFAULT_WIDTH = 800
    private const val DEFAULT_HEIGHT = 600
    
    data class Prefs(
        val width: Int,
        val height: Int,
        val x: Int,
        val y: Int
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
                    y = props.getProperty("y", "-1").toInt()
                )
            } else {
                Prefs(DEFAULT_WIDTH, DEFAULT_HEIGHT, -1, -1)
            }
        } catch (e: Exception) {
            Prefs(DEFAULT_WIDTH, DEFAULT_HEIGHT, -1, -1)
        }
    }
    
    fun save(width: Int, height: Int, x: Int, y: Int) {
        try {
            val file = getPrefsFile()
            file.parentFile?.mkdirs()
            
            val props = Properties()
            props.setProperty("width", width.toString())
            props.setProperty("height", height.toString())
            props.setProperty("x", x.toString())
            props.setProperty("y", y.toString())
            
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
fun QRShieldDesktopApp() {
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    
    // Initialize database and repository
    val historyRepository = remember {
        val driverFactory = DatabaseDriverFactory()
        val driver = driverFactory.createDriver()
        val database = QRShieldDatabase(driver)
        SqlDelightHistoryRepository(database)
    }
    
    // ViewModel with persistence
    val viewModel = remember { 
        SharedViewModel(
            phishingEngine = PhishingEngine(),
            historyRepository = historyRepository,
            coroutineScope = coroutineScope
        ) 
    }
    
    var urlInput by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
    
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                Text(
                    text = "🛡️ QR-SHIELD",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Kotlin Multiplatform QRishing Detector",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // URL Input
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Enter URL to analyze") },
                    placeholder = { Text("https://example.com") },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    singleLine = true
                )
                
                // Analyze Button
                Button(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            val engine = PhishingEngine()
                            val assessment = engine.analyze(urlInput)
                            analysisResult = AnalysisResult(
                                url = urlInput,
                                score = assessment.score,
                                verdict = assessment.verdict,
                                flags = assessment.flags
                            )
                        }
                    },
                    enabled = urlInput.isNotBlank()
                ) {
                    Text("Analyze URL")
                }
                
                // Results
                analysisResult?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ResultCard(result)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: AnalysisResult) {
    val (color, emoji) = when (result.verdict) {
        Verdict.SAFE -> MaterialTheme.colorScheme.primary to "✅"
        Verdict.SUSPICIOUS -> MaterialTheme.colorScheme.tertiary to "⚠️"
        Verdict.MALICIOUS -> MaterialTheme.colorScheme.error to "❌"
        Verdict.UNKNOWN -> MaterialTheme.colorScheme.outline to "❓"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(0.8f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayMedium
            )
            
            Text(
                text = result.score.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = color
            )
            
            Text(
                text = result.verdict.name,
                style = MaterialTheme.typography.headlineSmall,
                color = color
            )
            
            Text(
                text = result.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (result.flags.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = "Risk Factors:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                result.flags.forEach { flag ->
                    Text(
                        text = "• $flag",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

data class AnalysisResult(
    val url: String,
    val score: Int,
    val verdict: Verdict,
    val flags: List<String>
)
