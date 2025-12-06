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
import com.qrshield.model.Verdict
import com.qrshield.ui.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * QR-SHIELD Desktop Application Entry Point
 */
fun main() = application {
    val windowState = rememberWindowState(size = DpSize(800.dp, 600.dp))
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "🛡️ QR-SHIELD - QRishing Detector",
        state = windowState
    ) {
        QRShieldDesktopApp()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun QRShieldDesktopApp() {
    val coroutineScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    val viewModel = remember { SharedViewModel(PhishingEngine(), coroutineScope) }
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
