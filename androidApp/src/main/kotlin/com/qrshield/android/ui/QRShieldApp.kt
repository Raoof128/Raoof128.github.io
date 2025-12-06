package com.qrshield.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qrshield.model.Verdict
import com.qrshield.ui.SharedViewModel
import com.qrshield.ui.UiState
import org.koin.compose.koinInject

/**
 * Main QR-SHIELD application composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRShieldApp(
    viewModel: SharedViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🛡️ QR-SHIELD") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is UiState.Idle -> IdleScreen(onScanClick = { viewModel.startScanning() })
                is UiState.Scanning -> ScanningScreen()
                is UiState.Analyzing -> AnalyzingScreen(url = state.url)
                is UiState.Result -> ResultScreen(
                    score = state.assessment.score,
                    verdict = state.assessment.verdict,
                    flags = state.assessment.flags,
                    onDismiss = { viewModel.resetToIdle() }
                )
                is UiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.resetToIdle() }
                )
            }
        }
    }
}

@Composable
private fun IdleScreen(onScanClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "🔍",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = "Scan a QR code to check for phishing",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onScanClick) {
            Text("Start Scanning")
        }
    }
}

@Composable
private fun ScanningScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()
        Text("Point camera at QR code...")
    }
}

@Composable
private fun AnalyzingScreen(url: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator()
        Text("Analyzing URL...")
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultScreen(
    score: Int,
    verdict: Verdict,
    flags: List<String>,
    onDismiss: () -> Unit
) {
    val (color, emoji) = when (verdict) {
        Verdict.SAFE -> MaterialTheme.colorScheme.primary to "✅"
        Verdict.SUSPICIOUS -> MaterialTheme.colorScheme.tertiary to "⚠️"
        Verdict.MALICIOUS -> MaterialTheme.colorScheme.error to "❌"
        Verdict.UNKNOWN -> MaterialTheme.colorScheme.outline to "❓"
    }
    
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displayLarge,
            color = color
        )
        Text(
            text = verdict.name,
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        
        if (flags.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Risk Factors",
                        style = MaterialTheme.typography.titleMedium
                    )
                    flags.forEach { flag ->
                        Text(
                            text = "• $flag",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onDismiss) {
            Text("Scan Another")
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "❌",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}
