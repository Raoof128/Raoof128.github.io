package com.qrshield.android.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.qrshield.android.ui.components.CameraPreview
import com.qrshield.android.ui.components.ScannerOverlay
import com.qrshield.android.ui.theme.VerdictDanger
import com.qrshield.android.ui.theme.VerdictSafe
import com.qrshield.android.ui.theme.VerdictWarning
import com.qrshield.android.ui.theme.verdictColor
import com.qrshield.model.ScanResult
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import com.qrshield.ui.SharedViewModel
import com.qrshield.ui.UiState
import org.koin.compose.koinInject

/**
 * Main QR-SHIELD Application Composable
 * 
 * Features:
 * - Immersive camera scanner with animated overlay
 * - Real-time QR detection using ML Kit
 * - Cybersecurity-themed result display
 * - Persistent scan history via SQLDelight
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRShieldApp() {
    // Inject ViewModel from Koin (with database wired)
    val viewModel: SharedViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    
    val context = LocalContext.current
    
    // Permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            viewModel.startScanning()
        }
    }
    
    // Handle QR code scanned from camera
    val onQrCodeScanned: (String) -> Unit = remember(viewModel) {
        { content ->
            viewModel.processScanResult(
                ScanResult.Success(content),
                ScanSource.CAMERA
            )
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Idle -> IdleScreen(
                    scanCount = scanHistory.size,
                    onScanClick = {
                        if (hasCameraPermission) {
                            viewModel.startScanning()
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
                
                is UiState.Scanning -> ImmersiveScannerScreen(
                    onQrCodeScanned = onQrCodeScanned,
                    onClose = { viewModel.resetToIdle() }
                )
                
                is UiState.Analyzing -> AnalyzingScreen(url = state.url)
                
                is UiState.Result -> ResultScreen(
                    url = state.assessment.details.originalUrl,
                    score = state.assessment.score,
                    verdict = state.assessment.verdict,
                    flags = state.assessment.flags,
                    onDismiss = { viewModel.resetToIdle() },
                    onScanAnother = { viewModel.startScanning() }
                )
                
                is UiState.Error -> ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.startScanning() }
                )
            }
        }
    }
}

// =============================================================================
// IDLE SCREEN
// =============================================================================

@Composable
private fun IdleScreen(
    scanCount: Int,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Icon
        Text(
            text = "🛡️",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = "QR-SHIELD",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = "Scan QR codes. Detect phishing.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Scan Button
        Button(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Start Scanning",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // Scan History Badge
        if (scanCount > 0) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$scanCount scans in history",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// =============================================================================
// IMMERSIVE SCANNER SCREEN
// =============================================================================

@Composable
private fun ImmersiveScannerScreen(
    onQrCodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Camera Layer (Full Screen)
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onQrCodeScanned = onQrCodeScanned
        )
        
        // 2. Scanner Overlay Layer
        ScannerOverlay(modifier = Modifier.fillMaxSize())
        
        // 3. Close Button (Top Right)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(50)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }
        
        // 4. Hint Text (Bottom Center)
        Text(
            text = "Align QR code within frame",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// =============================================================================
// ANALYZING SCREEN
// =============================================================================

@Composable
private fun AnalyzingScreen(url: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Analyzing URL...",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = url.take(50) + if (url.length > 50) "..." else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================================
// RESULT SCREEN
// =============================================================================

@Composable
private fun ResultScreen(
    url: String,
    score: Int,
    verdict: Verdict,
    flags: List<String>,
    onDismiss: () -> Unit,
    onScanAnother: () -> Unit
) {
    val (color, emoji) = when (verdict) {
        Verdict.SAFE -> VerdictSafe to "✅"
        Verdict.SUSPICIOUS -> VerdictWarning to "⚠️"
        Verdict.MALICIOUS -> VerdictDanger to "🚨"
        Verdict.UNKNOWN -> MaterialTheme.colorScheme.outline to "❓"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Verdict Emoji
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Risk Score
        Text(
            text = "$score",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = "Risk Score",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Verdict Badge
        Surface(
            color = color.copy(alpha = 0.2f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = verdict.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // URL Display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
                maxLines = 3
            )
        }
        
        // Risk Flags
        if (flags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = color.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🚨 Risk Factors (${flags.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    flags.take(5).forEach { flag ->
                        Text(
                            text = "• $flag",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (flags.size > 5) {
                        Text(
                            text = "... and ${flags.size - 5} more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Done")
            }
            
            Button(
                onClick = onScanAnother,
                modifier = Modifier.weight(1f)
            ) {
                Text("Scan Another")
            }
        }
    }
}

// =============================================================================
// ERROR SCREEN
// =============================================================================

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}
