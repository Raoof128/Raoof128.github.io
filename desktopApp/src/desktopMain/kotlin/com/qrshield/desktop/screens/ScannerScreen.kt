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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.theme.DesktopColors
import com.qrshield.model.Verdict

/**
 * Scanner Screen (Live Scan Monitor) matching the HTML design.
 * Features:
 * - Active scanner viewport with grid pattern
 * - Corner markers and scan line animation
 * - Camera enable prompt
 * - Action bar (Torch, Upload, Paste URL)
 * - System status panel
 * - Recent scans list
 */
@Composable
fun ScannerScreen(
    onEnableCamera: () -> Unit,
    onUploadImage: () -> Unit,
    onPasteUrl: () -> Unit,
    onTorchToggle: () -> Unit,
    scanHistory: List<AnalysisResult>,
    onScanClick: (AnalysisResult) -> Unit,
    onViewAll: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        ScannerHeader(isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)

        // Scrollable Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Column: Scanner Viewport
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Page Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Scanner",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Real-time QR code analysis and threat detection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Offline Mode Badge
                    OfflineModeBadge()
                }

                // Scanner Viewport Card
                ScannerViewport(
                    onEnableCamera = onEnableCamera
                )

                // Latency hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Local analysis engine ready (<5ms latency)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Bar
                ActionBar(
                    onTorchToggle = onTorchToggle,
                    onUploadImage = onUploadImage,
                    onPasteUrl = onPasteUrl
                )
            }

            // Right Column: Status & Recent Scans
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // System Status Card
                SystemStatusCard()

                // Recent Scans Card
                RecentScansCard(
                    scans = scanHistory.take(5),
                    onScanClick = onScanClick,
                    onViewAll = onViewAll,
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
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Breadcrumbs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = "›", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Scan Monitor",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Right side
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engine Status with pulse
                EngineStatusBadgeGreen()

                // Theme Toggle
                IconButton(onClick = onThemeToggle) {
                    Text(
                        text = if (isDarkMode) "☀️" else "🌙",
                        fontSize = 18.sp
                    )
                }

                // Notifications
                Box {
                    IconButton(onClick = { }) {
                        Text(text = "🔔", fontSize = 18.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .clip(CircleShape)
                            .background(DesktopColors.VerdictMalicious)
                    )
                }
            }
        }
    }
}

@Composable
private fun EngineStatusBadgeGreen() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DesktopColors.VerdictSafe.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, DesktopColors.VerdictSafe.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated pulse
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(DesktopColors.VerdictSafe)
            )

            Text(
                text = "Offline Engine V.2.4 Active",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DesktopColors.VerdictSafe
            )
        }
    }
}

@Composable
private fun OfflineModeBadge() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📶", fontSize = 14.sp)
            Text(
                text = "Offline First",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ScannerViewport(
    onEnableCamera: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Grid Pattern Background
            GridPatternBackground()

            // Corner Markers
            CornerMarkers()

            // Scan Line Animation
            ScanLineAnimation()

            // Status Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF59E0B)) // Amber
                    )
                    Text(
                        text = "WAITING FOR INPUT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Center Content - Camera Prompt
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon Container with glassmorphism
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "📷", fontSize = 32.sp)
                    }
                }

                Text(
                    text = "Camera Access Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "To scan QR codes directly, please enable camera access\non your device or use the manual input options below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    modifier = Modifier.widthIn(max = 400.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onEnableCamera,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📷", fontSize = 16.sp)
                        Text(
                            text = "Enable Camera",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridPatternBackground() {
    // Simple grid representation using boxes
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ),
                    radius = 800f
                )
            )
    ) {
        // Grid lines would be drawn here - using gradient for performance
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2563EB).copy(alpha = 0.02f),
                            Color.Transparent,
                            Color(0xFF2563EB).copy(alpha = 0.02f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun CornerMarkers() {
    val cornerColor = Color(0xFF2563EB)
    val cornerSize = 50.dp
    val strokeWidth = 4.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // Center scanning area
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.Center)
        ) {
            // Top-left corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.TopStart)
                    .border(
                        width = strokeWidth,
                        color = cornerColor,
                        shape = RoundedCornerShape(topStart = 16.dp)
                    )
            )

            // Top-right corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.TopEnd)
                    .border(
                        width = strokeWidth,
                        color = cornerColor,
                        shape = RoundedCornerShape(topEnd = 16.dp)
                    )
            )

            // Bottom-left corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomStart)
                    .border(
                        width = strokeWidth,
                        color = cornerColor,
                        shape = RoundedCornerShape(bottomStart = 16.dp)
                    )
            )

            // Bottom-right corner
            Box(
                modifier = Modifier
                    .size(cornerSize)
                    .align(Alignment.BottomEnd)
                    .border(
                        width = strokeWidth,
                        color = cornerColor,
                        shape = RoundedCornerShape(bottomEnd = 16.dp)
                    )
            )
        }
    }
}

@Composable
private fun ScanLineAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(2.dp)
                .align(Alignment.Center)
                .offset(y = (offset * 200 - 100).dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF2563EB).copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun ActionBar(
    onTorchToggle: () -> Unit,
    onUploadImage: () -> Unit,
    onPasteUrl: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                icon = "🔦",
                label = "Torch",
                onClick = onTorchToggle,
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(
                modifier = Modifier
                    .height(48.dp)
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            ActionButton(
                icon = "🖼️",
                label = "Upload Image",
                onClick = onUploadImage,
                modifier = Modifier.weight(1f)
            )

            VerticalDivider(
                modifier = Modifier
                    .height(48.dp)
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            ActionButton(
                icon = "🔗",
                label = "Paste URL",
                onClick = onPasteUrl,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SystemStatusCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYSTEM STATUS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                    Text(text = "🔄", fontSize = 14.sp)
                }
            }

            StatusItem(
                icon = "🛡️",
                label = "Detection Engine",
                sublabel = "Phishing Guard",
                badgeText = "READY",
                badgeColor = DesktopColors.VerdictSafe
            )

            StatusItem(
                icon = "💾",
                label = "Database",
                sublabel = "Local V.2.4.0",
                badgeText = "LATEST",
                badgeColor = Color(0xFF2563EB)
            )

            StatusItem(
                icon = "⚡",
                label = "Latency",
                sublabel = "4ms Avg",
                badgeColor = DesktopColors.VerdictSafe,
                showLatencyBars = true
            )
        }
    }
}

@Composable
private fun StatusItem(
    icon: String,
    label: String,
    sublabel: String,
    badgeText: String? = null,
    badgeColor: Color,
    showLatencyBars: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = sublabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            if (showLatencyBars) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(((index + 1) * 4).dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(badgeColor.copy(alpha = if (index < 3) 1f else 0.5f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentScansCard(
    scans: List<AnalysisResult>,
    onScanClick: (AnalysisResult) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shadowElevation = 1.dp
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT SCANS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onViewAll) {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2563EB)
                    )
                }
            }

            // Scan List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (scans.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent scans",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    scans.forEach { scan ->
                        RecentScanItem(
                            scan = scan,
                            onClick = { onScanClick(scan) }
                        )
                    }
                }
            }

            // Footer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                TextButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📥", fontSize = 14.sp)
                        Text(
                            text = "Export Log",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        Verdict.SAFE -> DesktopColors.VerdictSafe
        Verdict.SUSPICIOUS -> DesktopColors.VerdictSuspicious
        else -> DesktopColors.VerdictMalicious
    }

    val verdictIcon = when (scan.verdict) {
        Verdict.SAFE -> "✓"
        Verdict.SUSPICIOUS -> "⚠️"
        else -> "⚠️"
    }

    val bgColor = if (scan.verdict == Verdict.MALICIOUS || scan.verdict == Verdict.SUSPICIOUS) {
        verdictColor.copy(alpha = 0.05f)
    } else {
        Color.Transparent
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = bgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(verdictColor.copy(alpha = 0.15f))
                    .border(1.dp, verdictColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = verdictIcon, fontSize = 14.sp)
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.url.removePrefix("https://").removePrefix("http://").take(25),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (scan.verdict == Verdict.MALICIOUS) {
                        DesktopColors.VerdictMalicious
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(scan.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = verdictColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, verdictColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = scan.verdict.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = verdictColor,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Chevron
            Text(
                text = "›",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> "${diff / 86400_000}d ago"
    }
}
