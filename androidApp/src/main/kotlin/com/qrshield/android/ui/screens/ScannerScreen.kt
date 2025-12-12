/*
 * Copyright 2025-2026 QR-SHIELD Contributors
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

package com.qrshield.android.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.qrshield.android.R
import com.qrshield.android.ui.components.CameraPreview
import com.qrshield.android.ui.components.ResultCard
import com.qrshield.android.ui.components.ScannerOverlay
import com.qrshield.android.ui.theme.*
import com.qrshield.model.ContentType
import com.qrshield.model.ScanResult
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import com.qrshield.scanner.AndroidQrScanner
import com.qrshield.ui.SharedViewModel
import com.qrshield.ui.UiState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.qrshield.android.util.SoundManager

/**
 * Main Scanner Screen with camera preview and QR code detection.
 * 
 * Features:
 * - Full camera preview with ML Kit scanning
 * - Photo picker for gallery scanning with QR decode
 * - Haptic feedback on scan/result
 * - Accessibility support with TalkBack
 * - Error handling with user-facing messages
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen() {
    val viewModel: SharedViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // QR Scanner for gallery images
    val qrScanner = remember { AndroidQrScanner(context) }
    
    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Camera error state
    var cameraError by remember { mutableStateOf<String?>(null) }
    
    // Flash state
    var isFlashOn by remember { mutableStateOf(false) }
    
    // Gallery scanning state
    var isProcessingGalleryImage by remember { mutableStateOf(false) }
    
    // Vibrator for haptic feedback
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
    }
    
    // Photo picker launcher with QR decode
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            // Scan QR code from selected image
            isProcessingGalleryImage = true
            triggerHapticFeedback(vibrator, HapticType.SCAN, settings.isHapticEnabled)
            SoundManager.playSound(SoundManager.SoundType.SCAN, settings.isSoundEnabled)
            
            scope.launch {
                val result = qrScanner.scanFromUri(uri)
                isProcessingGalleryImage = false
                
                when (result) {
                    is ScanResult.Success -> {
                        triggerHapticFeedback(vibrator, HapticType.SUCCESS, settings.isHapticEnabled)
                        SoundManager.playSound(SoundManager.SoundType.SUCCESS, settings.isSoundEnabled)
                        viewModel.processScanResult(result, ScanSource.GALLERY)
                    }
                    is ScanResult.NoQrFound -> {
                        triggerHapticFeedback(vibrator, HapticType.ERROR, settings.isHapticEnabled)
                        SoundManager.playSound(SoundManager.SoundType.ERROR, settings.isSoundEnabled)
                        // Show error message - no QR found
                        cameraError = "No QR code found in the selected image"
                    }
                    is ScanResult.Error -> {
                        triggerHapticFeedback(vibrator, HapticType.ERROR, settings.isHapticEnabled)
                        SoundManager.playSound(SoundManager.SoundType.ERROR, settings.isSoundEnabled)
                        cameraError = result.message
                    }
                }
            }
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            viewModel.startScanning()
            triggerHapticFeedback(vibrator, HapticType.SUCCESS, settings.isHapticEnabled)
        } else {
            triggerHapticFeedback(vibrator, HapticType.ERROR, settings.isHapticEnabled)
        }
    }
    
    // QR code scanned callback with haptic
    val onQrCodeScanned: (String) -> Unit = remember(viewModel, settings) {
        { content ->
            triggerHapticFeedback(vibrator, HapticType.SCAN, settings.isHapticEnabled)
            SoundManager.playSound(SoundManager.SoundType.SCAN, settings.isSoundEnabled)
            viewModel.processScanResult(
                ScanResult.Success(content, ContentType.URL),
                ScanSource.CAMERA
            )
        }
    }
    
    // Handle state changes with haptic feedback
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Result -> {
                val verdict = (uiState as UiState.Result).assessment.verdict
                when (verdict) {
                    Verdict.SAFE -> {
                        triggerHapticFeedback(vibrator, HapticType.SUCCESS, settings.isHapticEnabled)
                        SoundManager.playSound(SoundManager.SoundType.SUCCESS, settings.isSoundEnabled)
                    }
                    Verdict.SUSPICIOUS -> {
                        triggerHapticFeedback(vibrator, HapticType.WARNING, settings.isHapticEnabled)
                        SoundManager.playSound(SoundManager.SoundType.WARNING, settings.isSoundEnabled)
                    }
                    Verdict.MALICIOUS -> {
                        triggerHapticFeedback(vibrator, HapticType.ERROR, settings.isHapticEnabled)
                        SoundManager.playSound(SoundManager.SoundType.ERROR, settings.isSoundEnabled)
                    }
                    else -> triggerHapticFeedback(vibrator, HapticType.LIGHT, settings.isHapticEnabled)
                }
            }
            is UiState.Error -> {
                triggerHapticFeedback(vibrator, HapticType.ERROR, settings.isHapticEnabled)
                SoundManager.playSound(SoundManager.SoundType.ERROR, settings.isSoundEnabled)
            }
            else -> {}
        }
    }
    
    // Clear camera errors when state changes
    LaunchedEffect(uiState) {
        if (uiState !is UiState.Scanning) {
            cameraError = null
        }
    }
    
    // Auto-scan: Start scanning automatically when enabled and have permission
    LaunchedEffect(settings.isAutoScanEnabled, hasCameraPermission) {
        if (settings.isAutoScanEnabled && hasCameraPermission && uiState is UiState.Idle) {
            viewModel.startScanning()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.3f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        // Show loading overlay when processing gallery image
        if (isProcessingGalleryImage) {
            ProcessingOverlay()
        }
        
        when (val state = uiState) {
            is UiState.Idle -> {
                IdleContent(
                    scanCount = scanHistory.size,
                    onScanClick = {
                        if (hasCameraPermission) {
                            viewModel.startScanning()
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onGalleryClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
            
            is UiState.Scanning -> {
                ScanningContent(
                    onQrCodeScanned = onQrCodeScanned,
                    onClose = { viewModel.resetToIdle() },
                    onGalleryClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    isFlashOn = isFlashOn,
                    onFlashToggle = { isFlashOn = !isFlashOn },
                    cameraError = cameraError,
                    onCameraError = { error -> cameraError = error }
                )
            }
            
            is UiState.Analyzing -> {
                AnalyzingContent(url = state.url)
            }
            
            is UiState.Result -> {
                ResultContent(
                    url = state.assessment.details.originalUrl,
                    score = state.assessment.score,
                    verdict = state.assessment.verdict,
                    flags = state.assessment.flags,
                    onDismiss = { viewModel.resetToIdle() },
                    onScanAnother = { viewModel.startScanning() }
                )
            }
            
            is UiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.startScanning() }
                )
            }
        }
    }
}

// =============================================================================
// PROCESSING OVERLAY (for gallery scanning)
// =============================================================================

@Composable
private fun ProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .semantics { 
                contentDescription = "Processing image, please wait"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = BrandPrimary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Scanning image for QR codes...",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

// =============================================================================
// IDLE STATE
// =============================================================================

@Composable
private fun IdleContent(
    scanCount: Int,
    onScanClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp)
            .semantics { 
                contentDescription = "QR Shield home screen. Tap Start Scanning to begin, or choose from gallery."
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated shield icon
        val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        Text(
            text = "🛡️",
            fontSize = (80 * scale).sp,
            modifier = Modifier.semantics { 
                contentDescription = "QR Shield logo"
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = BrandPrimary,
            modifier = Modifier.semantics { 
                contentDescription = "QR Shield, Phishing Detection App"
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.tagline),
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Main scan button
        Button(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics { 
                    contentDescription = "Start scanning for QR codes using camera"
                },
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.start_scanning),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gallery button
        OutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics { 
                    contentDescription = "Choose QR code image from photo gallery"
                },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandSecondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.scan_from_gallery),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        if (scanCount > 0) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.scans_in_history, scanCount),
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.semantics { 
                    contentDescription = "$scanCount scans saved in history"
                }
            )
        }
    }
}

// =============================================================================
// SCANNING STATE
// =============================================================================

@Composable
private fun ScanningContent(
    onQrCodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    onGalleryClick: () -> Unit,
    isFlashOn: Boolean,
    onFlashToggle: () -> Unit,
    cameraError: String?,
    onCameraError: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { 
                contentDescription = "Camera scanning mode. Point camera at a QR code."
            }
    ) {
        // Camera preview
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onQrCodeScanned = onQrCodeScanned,
            onCameraError = onCameraError
        )
        
        // Scanner overlay with animated corners
        ScannerOverlay(modifier = Modifier.fillMaxSize())
        
        // Top bar with controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .semantics { 
                        contentDescription = "Close scanner and return to home"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
            
            // Flash toggle
            IconButton(
                onClick = onFlashToggle,
                modifier = Modifier
                    .background(
                        color = if (isFlashOn) BrandPrimary.copy(alpha = 0.8f) 
                               else Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .semantics { 
                        contentDescription = if (isFlashOn) "Turn off flashlight" else "Turn on flashlight"
                    }
            ) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (isFlashOn) "Flash On" else "Flash Off",
                    tint = Color.White
                )
            }
        }
        
        // Bottom controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gallery button
            FilledTonalButton(
                onClick = onGalleryClick,
                modifier = Modifier.semantics { 
                    contentDescription = "Scan QR code from photo gallery"
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.scan_from_gallery))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Instruction text
            Text(
                text = stringResource(R.string.scan_instruction),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Error message if any
            AnimatedVisibility(visible = cameraError != null) {
                Text(
                    text = cameraError ?: "",
                    color = VerdictDanger,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(
                            color = VerdictDanger.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .semantics { 
                            contentDescription = "Error: $cameraError"
                        }
                )
            }
        }
    }
}

// =============================================================================
// ANALYZING STATE
// =============================================================================

@Composable
private fun AnalyzingContent(url: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp)
            .semantics { 
                contentDescription = "Analyzing URL for security threats. Please wait."
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = BrandPrimary,
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.analyzing_url),
            fontSize = 18.sp,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = url.take(50) + if (url.length > 50) "..." else "",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================================
// RESULT STATE
// =============================================================================

@Composable
private fun ResultContent(
    url: String,
    score: Int,
    verdict: Verdict,
    flags: List<String>,
    onDismiss: () -> Unit,
    onScanAnother: () -> Unit
) {
    ResultCard(
        url = url,
        score = score,
        verdict = verdict,
        flags = flags,
        onDismiss = onDismiss,
        onScanAnother = onScanAnother
    )
}

// =============================================================================
// ERROR STATE
// =============================================================================

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp)
            .semantics { 
                contentDescription = "Error occurred: $message. Tap Try Again to retry."
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "❌",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.error_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = VerdictDanger
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            modifier = Modifier.semantics { 
                contentDescription = "Try again"
            }
        ) {
            Text(stringResource(R.string.try_again))
        }
    }
}

// =============================================================================
// HAPTIC FEEDBACK
// =============================================================================

private enum class HapticType {
    LIGHT, SCAN, SUCCESS, WARNING, ERROR
}

private fun triggerHapticFeedback(vibrator: Vibrator?, type: HapticType, enabled: Boolean = true) {
    if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val effect = when (type) {
            HapticType.LIGHT -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            HapticType.SCAN -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            HapticType.SUCCESS -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            HapticType.WARNING -> VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1)
            HapticType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1)
        }
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(50)
    }
}
