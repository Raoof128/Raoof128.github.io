/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.desktop.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.theme.DesktopColors
import com.qrshield.model.Verdict
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Scanner Screen matching the HTML live scan monitor design.
 * Features camera viewport, URL input, real-time analysis, and recent scans.
 */
@Composable
fun ScannerScreen(
    urlInput: String,
    onUrlInputChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onImageSelect: () -> Unit,
    analysisResult: AnalysisResult?,
    isAnalyzing: Boolean,
    recentScans: List<AnalysisResult>,
    onScanClick: (AnalysisResult) -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryBlue = Color(0xFF2563EB)
    var feedStatus by remember { mutableStateOf("IDLE") } // IDLE, SCANNING, PROCESSING
    var frameCount by remember { mutableStateOf(0) }

    // Simulate frame counter
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            frameCount++
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Bar
        ScannerHeader(isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)

        // Main Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Column - Scanner Viewport
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Camera Viewport
                CameraViewport(
                    isScanning = isAnalyzing,
                    feedStatus = feedStatus,
                    frameCount = frameCount,
                    modifier = Modifier.weight(1f)
                )

                // URL Input Panel
                URLInputPanel(
                    urlInput = urlInput,
                    onUrlInputChange = onUrlInputChange,
                    onAnalyze = onAnalyze,
                    onImageSelect = onImageSelect,
                    isAnalyzing = isAnalyzing
                )
            }

            // Right Column - Analysis & Controls
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Live Analysis Panel
                LiveAnalysisPanel(
                    analysisResult = analysisResult,
                    isAnalyzing = isAnalyzing
                )

                // Scanner Controls
                ScannerControlsPanel()

                // Recent Scans
                RecentScansPanel(
                    scans = recentScans.take(5),
                    onScanClick = onScanClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ScannerHeader(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Breadcrumbs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QR-SHIELD",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("/", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Text(
                    text = "Live Scan Monitor",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Right Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live indicator
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pulsing dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                IconButton(onClick = onThemeToggle) {
                    Text(if (isDarkMode) "☀️" else "🌙", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun CameraViewport(
    isScanning: Boolean,
    feedStatus: String,
    frameCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = if (isScanning) BorderStroke(2.dp, Color(0xFF2563EB)) else BorderStroke(1.dp, Color(0xFF334155)),
        shadowElevation = 8.dp
    ) {
        Box {
            // Scan lines effect
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(20) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF334155).copy(alpha = 0.3f))
                    )
                }
            }

            // Scanner Overlay
            Box(
                modifier = Modifier 
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                // QR Frame
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2563EB),
                                    Color(0xFF9333EA)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("📷", fontSize = 64.sp)
                        Text(
                            text = if (isScanning) "Analyzing..." else "Ready to Scan",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Point camera at QR code or enter URL below",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                // Corner brackets
                Box(modifier = Modifier.fillMaxSize()) {
                    // Top-left
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color(0xFF2563EB))
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .background(Color(0xFF2563EB))
                        )
                    }
                    // Top-right
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .align(Alignment.TopEnd)
                                .background(Color(0xFF2563EB))
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .align(Alignment.TopEnd)
                                .background(Color(0xFF2563EB))
                        )
                    }
                    // Bottom-left
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.BottomStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .align(Alignment.BottomStart)
                                .background(Color(0xFF9333EA))
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .align(Alignment.BottomStart)
                                .background(Color(0xFF9333EA))
                        )
                    }
                    // Bottom-right
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color(0xFF9333EA))
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color(0xFF9333EA))
                        )
                    }
                }
            }

            // HUD Overlay - Top Left
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "FEED STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isScanning) Color(0xFF2563EB).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isScanning) Color(0xFF2563EB) else Color(0xFF10B981))
                        )
                        Text(
                            text = if (isScanning) "PROCESSING" else "READY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isScanning) Color(0xFF2563EB) else Color(0xFF10B981)
                        )
                    }
                }
            }

            // HUD Overlay - Top Right (Frame Counter)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "FRAME",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = String.format("%06d", frameCount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // HUD Overlay - Bottom Status Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatusItem(label = "RES", value = "1920x1080")
                    StatusItem(label = "FPS", value = "30")
                    StatusItem(label = "CODEC", value = "H.264")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    StatusItem(label = "LATENCY", value = "<12ms")
                    StatusItem(label = "ENGINE", value = "v2.4.1")
                }
            }
        }
    }
}

@Composable
private fun StatusItem(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun URLInputPanel(
    urlInput: String,
    onUrlInputChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onImageSelect: () -> Unit,
    isAnalyzing: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "MANUAL URL INPUT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Enter URL to analyze...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )

                Button(
                    onClick = onAnalyze,
                    enabled = urlInput.isNotBlank() && !isAnalyzing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔍", fontSize = 16.sp)
                            Text("Analyze", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onImageSelect,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📤", fontSize = 16.sp)
                        Text("Import", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveAnalysisPanel(
    analysisResult: AnalysisResult?,
    isAnalyzing: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            when (analysisResult?.verdict) {
                Verdict.SAFE -> Color(0xFF10B981).copy(alpha = 0.3f)
                Verdict.SUSPICIOUS -> Color(0xFFF59E0B).copy(alpha = 0.3f)
                Verdict.MALICIOUS -> Color(0xFFEF4444).copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            }
        ),
        shadowElevation = 2.dp
    ) {
        Column {
            // Top colored bar
            if (analysisResult != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            when (analysisResult.verdict) {
                                Verdict.SAFE -> Color(0xFF10B981)
                                Verdict.SUSPICIOUS -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
                        )
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF2563EB),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (analysisResult != null) {
                    // Verdict Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (analysisResult.verdict) {
                            Verdict.SAFE -> Color(0xFF10B981).copy(alpha = 0.1f)
                            Verdict.SUSPICIOUS -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                            else -> Color(0xFFEF4444).copy(alpha = 0.1f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (analysisResult.verdict) {
                                    Verdict.SAFE -> "✓"
                                    Verdict.SUSPICIOUS -> "⚠️"
                                    else -> "⛔"
                                },
                                fontSize = 24.sp
                            )
                            Column {
                                Text(
                                    text = when (analysisResult.verdict) {
                                        Verdict.SAFE -> "SAFE"
                                        Verdict.SUSPICIOUS -> "SUSPICIOUS"
                                        else -> "MALICIOUS"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when (analysisResult.verdict) {
                                        Verdict.SAFE -> Color(0xFF10B981)
                                        Verdict.SUSPICIOUS -> Color(0xFFF59E0B)
                                        else -> Color(0xFFEF4444)
                                    }
                                )
                                Text(
                                    text = "Confidence: ${(analysisResult.score * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Flags
                    if (analysisResult.flags.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "DETECTED FLAGS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            analysisResult.flags.take(3).forEach { flag ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("•", color = Color(0xFFF59E0B))
                                    Text(
                                        text = flag,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔍", fontSize = 32.sp)
                            Text(
                                text = "Awaiting Input",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Scan a QR code or enter a URL",
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
private fun ScannerControlsPanel() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Scanner Controls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ControlButton(
                    icon = "📷",
                    label = "Camera",
                    isActive = true,
                    modifier = Modifier.weight(1f)
                )
                ControlButton(
                    icon = "⚡",
                    label = "Flash",
                    isActive = false,
                    modifier = Modifier.weight(1f)
                )
                ControlButton(
                    icon = "🔄",
                    label = "Flip",
                    isActive = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: String,
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Color(0xFF2563EB).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isActive) BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = if (isActive) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentScansPanel(
    scans: List<AnalysisResult>,
    onScanClick: (AnalysisResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Recent Scans",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (scans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent scans",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scans.forEach { scan ->
                        RecentScanItem(scan = scan, onClick = { onScanClick(scan) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentScanItem(
    scan: AnalysisResult,
    onClick: () -> Unit
) {
    val verdictColor = when (scan.verdict) {
        Verdict.SAFE -> Color(0xFF10B981)
        Verdict.SUSPICIOUS -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(verdictColor)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.url.removePrefix("https://").removePrefix("http://").take(25),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (scan.verdict) {
                        Verdict.SAFE -> "Safe"
                        Verdict.SUSPICIOUS -> "Suspicious"
                        else -> "Malicious"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = verdictColor
                )
            }

            Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
