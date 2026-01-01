/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

package com.raouf.mehrguard.android.ui.screens

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
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Link
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
import com.raouf.mehrguard.android.R
import com.raouf.mehrguard.android.ui.components.CameraPreview
import com.raouf.mehrguard.android.ui.components.ResultCard
import com.raouf.mehrguard.android.ui.components.ScannerOverlay
import com.raouf.mehrguard.android.ui.theme.*
import com.raouf.mehrguard.model.ContentType
import com.raouf.mehrguard.model.ScanResult
import com.raouf.mehrguard.model.ScanSource
import com.raouf.mehrguard.model.Verdict
import com.raouf.mehrguard.scanner.AndroidQrScanner
import com.raouf.mehrguard.ui.SharedViewModel
import com.raouf.mehrguard.ui.UiState
import com.raouf.mehrguard.redteam.RedTeamScenarios
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.raouf.mehrguard.android.util.SoundManager

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
                        cameraError = context.getString(R.string.error_no_qr_found)
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Show loading overlay when processing gallery image
        if (isProcessingGalleryImage) {
            ProcessingOverlay()
        }

        when (val state = uiState) {
            is UiState.Idle -> {
                IdleContent(
                    scanCount = scanHistory.size,
                    isDeveloperModeEnabled = settings.isDeveloperModeEnabled,
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
                    },
                    onUrlAnalyze = { url ->
                        triggerHapticFeedback(vibrator, HapticType.SCAN, settings.isHapticEnabled)
                        SoundManager.playSound(SoundManager.SoundType.SCAN, settings.isSoundEnabled)
                        viewModel.analyzeUrl(url, ScanSource.MANUAL)
                    },
                    onRedTeamScenarioClick = { scenario ->
                        // Bypass camera - feed malicious URL directly to analysis engine
                        triggerHapticFeedback(vibrator, HapticType.WARNING, settings.isHapticEnabled)
                        SoundManager.playSound(SoundManager.SoundType.SCAN, settings.isSoundEnabled)
                        viewModel.analyzeUrl(scenario.maliciousUrl, ScanSource.CAMERA)
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

            is UiState.Resolving -> {
                ResolvingContent(originalUrl = state.originalUrl)
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
    val processingDesc = stringResource(R.string.cd_processing_image)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .semantics {
                contentDescription = processingDesc
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
                text = stringResource(R.string.scanning_image_progress),
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
    isDeveloperModeEnabled: Boolean = false,
    onScanClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onUrlAnalyze: (String) -> Unit = {},
    onRedTeamScenarioClick: (RedTeamScenarios.Scenario) -> Unit = {}
) {
    val homeScreenDesc = stringResource(R.string.cd_home_screen)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics {
                contentDescription = homeScreenDesc
            }
    ) {
        // === RED TEAM SCENARIOS PANEL (Developer Mode Only) ===
        AnimatedVisibility(
            visible = isDeveloperModeEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            RedTeamScenariosPanel(
                scenarios = RedTeamScenarios.SCENARIOS,
                onScenarioClick = onRedTeamScenarioClick
            )
        }

        // === MAIN CONTENT ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
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

            val logoDesc = stringResource(R.string.cd_app_logo_full)
            Text(
                text = "🛡️",
                fontSize = (80 * scale).sp,
                modifier = Modifier.semantics {
                    contentDescription = logoDesc
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            val taglineDesc = stringResource(R.string.cd_app_tagline)
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPrimary,
                modifier = Modifier.semantics {
                    contentDescription = taglineDesc
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
            val startScanningDesc = stringResource(R.string.cd_start_scanning)
            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = startScanningDesc
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
            val galleryDesc = stringResource(R.string.cd_choose_from_gallery)
            OutlinedButton(
                onClick = onGalleryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics {
                        contentDescription = galleryDesc
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

            // === URL Manual Input Section ===
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.enter_url_manually),
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            var urlInput by remember { mutableStateOf("") }
            
            val enterUrlDesc = stringResource(R.string.cd_enter_url)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = enterUrlDesc },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.url_placeholder),
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = BrandPrimary
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = TextMuted.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = BrandPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Button(
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            val url = if (urlInput.startsWith("http://") || urlInput.startsWith("https://")) {
                                urlInput
                            } else {
                                "https://$urlInput"
                            }
                            onUrlAnalyze(url)
                            urlInput = ""
                        }
                    },
                    enabled = urlInput.isNotBlank(),
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.analyze_url),
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
}

// =============================================================================
// RED TEAM SCENARIOS PANEL
// =============================================================================

/**
 * Red Team Scenarios Panel - Shows attack scenarios for testing.
 * This panel appears at the top of the scanner screen when Developer Mode is enabled.
 * Clicking a scenario bypasses the camera and feeds the malicious URL directly to PhishingEngine.
 */
@Composable
private fun RedTeamScenariosPanel(
    scenarios: List<RedTeamScenarios.Scenario>,
    onScenarioClick: (RedTeamScenarios.Scenario) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3D1515), // Dark red
                        Color(0xFF2A1010)  // Darker red
                    )
                )
            )
            .padding(vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🕵️",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.red_team_scenarios_title),
                color = Color(0xFFFF6B6B),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.red_team_attacks_fmt, scenarios.size),
                color = Color(0xFFFF9999),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontally scrolling scenario chips
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(scenarios.size) { index ->
                val scenario = scenarios[index]
                RedTeamScenarioChip(
                    scenario = scenario,
                    onClick = { onScenarioClick(scenario) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hint text
        Text(
            text = stringResource(R.string.red_team_hint),
            color = Color(0xFFAA7777),
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * A single Red Team scenario chip.
 */
@Composable
private fun RedTeamScenarioChip(
    scenario: RedTeamScenarios.Scenario,
    onClick: () -> Unit
) {
    val categoryColor = when {
        scenario.category.contains("Homograph", ignoreCase = true) -> Color(0xFFE53935) // Red
        scenario.category.contains("IP", ignoreCase = true) -> Color(0xFFFF9800) // Orange
        scenario.category.contains("TLD", ignoreCase = true) -> Color(0xFFE91E63) // Pink
        scenario.category.contains("Redirect", ignoreCase = true) -> Color(0xFF9C27B0) // Purple
        scenario.category.contains("Brand", ignoreCase = true) -> Color(0xFF3F51B5) // Indigo
        scenario.category.contains("Shortener", ignoreCase = true) -> Color(0xFF00BCD4) // Cyan
        scenario.category.contains("Safe", ignoreCase = true) -> Color(0xFF4CAF50) // Green
        else -> Color(0xFFFF5722) // Deep Orange
    }

    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = "${scenario.title}. ${scenario.description}. Tap to test."
            },
        color = categoryColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Text(
                text = when {
                    scenario.category.contains("Homograph") -> "🔤"
                    scenario.category.contains("IP") -> "🔢"
                    scenario.category.contains("TLD") -> "🌐"
                    scenario.category.contains("Redirect") -> "↪️"
                    scenario.category.contains("Brand") -> "🏷️"
                    scenario.category.contains("Shortener") -> "🔗"
                    scenario.category.contains("Safe") -> "✅"
                    else -> "⚠️"
                },
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = scenario.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                scenario.targetBrand?.let { brand ->
                    Text(
                        text = brand,
                        color = categoryColor,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
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
    val cameraScanningDesc = stringResource(R.string.cd_camera_scanning)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = cameraScanningDesc
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
            val closeScannerDesc = stringResource(R.string.cd_close_scanner)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .semantics {
                        contentDescription = closeScannerDesc
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.cd_close),
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
        val scanFromGalleryDesc = stringResource(R.string.cd_scan_from_gallery)
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
                    contentDescription = scanFromGalleryDesc
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
                val errorDesc = stringResource(R.string.cd_camera_error, cameraError ?: "")
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
                            contentDescription = errorDesc
                        }
                )
            }
        }
    }
}

// =============================================================================
// RESOLVING STATE (Aggressive Mode - URL Unshortener)
// =============================================================================

@Composable
private fun ResolvingContent(originalUrl: String) {
    val resolvingDesc = stringResource(R.string.cd_resolving_url)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
            .semantics {
                contentDescription = resolvingDesc
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Link icon with animation
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = BrandSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = BrandSecondary,
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.resolving_short_link_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.resolving_short_link_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show original short URL
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            color = BackgroundCard.copy(alpha = 0.5f)
        ) {
            Text(
                text = originalUrl.take(60) + if (originalUrl.length > 60) "..." else "",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

// =============================================================================
// ANALYZING STATE
// =============================================================================

@Composable
private fun AnalyzingContent(url: String) {
    val analyzingDesc = stringResource(R.string.cd_analyzing_url)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
            .semantics {
                contentDescription = analyzingDesc
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
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = url.take(50) + if (url.length > 50) "..." else "",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val errorDesc = stringResource(R.string.cd_error_occurred, message)
    val tryAgainDesc = stringResource(R.string.cd_try_again)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
            .semantics {
                contentDescription = errorDesc
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
            color = MaterialTheme.colorScheme.onBackground
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
                contentDescription = tryAgainDesc
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
