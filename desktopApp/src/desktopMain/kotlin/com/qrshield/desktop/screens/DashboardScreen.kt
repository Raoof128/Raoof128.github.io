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
 * Dashboard Screen matching the HTML dashboard design.
 * Features:
 * - Hero section with tagline
 * - URL input for quick analysis
 * - System health card
 * - Feature cards grid
 * - Recent scans table
 * - Threat database card
 */
@Composable
fun DashboardScreen(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit,
    onScanQR: () -> Unit,
    onImportImage: () -> Unit,
    scanHistory: List<AnalysisResult>,
    onScanClick: (AnalysisResult) -> Unit,
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
        DashboardHeader(
            isDarkMode = isDarkMode,
            onThemeToggle = onThemeToggle
        )

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Section
            HeroSection(
                urlInput = urlInput,
                onUrlChange = onUrlChange,
                isAnalyzing = isAnalyzing,
                onAnalyze = onAnalyze,
                onScanQR = onScanQR,
                onImportImage = onImportImage
            )

            // Features Grid
            FeaturesGrid()

            // Main Grid: Recent Scans + Sidebar Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Recent Scans Table
                RecentScansCard(
                    scans = scanHistory.take(5),
                    onViewAll = { /* Navigate to history */ },
                    onScanClick = onScanClick,
                    modifier = Modifier.weight(2f)
                )

                // Side Cards Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ThreatDatabaseCard()
                    TrainingCentreCard()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardHeader(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
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
                    text = "QR-SHIELD",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "/",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Right side controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engine Status
                EngineStatusBadge()

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
                    // Red dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .clip(CircleShape)
                            .background(DesktopColors.VerdictMalicious)
                    )
                }

                // Settings
                IconButton(onClick = { }) {
                    Text(text = "⚙️", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun EngineStatusBadge() {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DesktopColors.VerdictSafe.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, DesktopColors.VerdictSafe.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated pulse dot
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(DesktopColors.VerdictSafe.copy(alpha = alpha))
            )
            Text(
                text = "Engine Active",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = DesktopColors.VerdictSafe
            )
        }
    }
}

@Composable
private fun HeroSection(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit,
    onScanQR: () -> Unit,
    onImportImage: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Text + Actions
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Enterprise Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF2563EB).copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✓", color = Color(0xFF2563EB))
                        Text(
                            text = "Enterprise Protection Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }

                // Hero Title
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Secure. Offline.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Explainable Defence.",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }

                // Description
                Text(
                    text = "QR-SHIELD analyses potential threats directly on your hardware. " +
                            "Experience zero-latency phishing detection without compromising data privacy.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 26.sp
                )

                // URL Input
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔍",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 18.sp
                        )
                        TextField(
                            value = urlInput,
                            onValueChange = onUrlChange,
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text("Paste URL to analyze (e.g., https://example.com)")
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                        Button(
                            onClick = onAnalyze,
                            enabled = !isAnalyzing && urlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🛡️", fontSize = 14.sp)
                                Text(
                                    text = if (isAnalyzing) "Analyzing..." else "Analyze",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onScanQR,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "📷", fontSize = 16.sp)
                            Text("Scan QR Code", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedButton(
                        onClick = onImportImage,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "📁", fontSize = 16.sp)
                            Text("Import Image")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(40.dp))

            // Right: System Health Card
            SystemHealthCard()
        }
    }
}

@Composable
private fun SystemHealthCard() {
    Surface(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System Health",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(DesktopColors.VerdictSafe)
                )
            }

            // Threat Database
            HealthItem(
                label = "Threat Database",
                status = "Current",
                statusColor = DesktopColors.VerdictSafe,
                progress = 1f
            )

            // Heuristic Engine
            HealthItem(
                label = "Heuristic Engine",
                status = "Active",
                statusColor = Color(0xFF2563EB),
                progress = 0.92f
            )

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBox(
                    value = "0",
                    label = "Threats",
                    modifier = Modifier.weight(1f)
                )
                StatBox(
                    value = "124",
                    label = "Safe Scans",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HealthItem(
    label: String,
    status: String,
    statusColor: Color,
    progress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = statusColor,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeaturesGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FeatureCard(
            icon = "☁️",
            title = "Offline-First Architecture",
            description = "Complete analysis is performed locally. Your camera feed and scanned data never touch an external server.",
            accentColor = Color(0xFF2563EB),
            modifier = Modifier.weight(1f)
        )
        FeatureCard(
            icon = "🔍",
            title = "Explainable Security",
            description = "Don't just get a \"Block\". We provide detailed heuristic breakdowns of URL parameters and redirects.",
            accentColor = Color(0xFF7C3AED),
            modifier = Modifier.weight(1f)
        )
        FeatureCard(
            icon = "⚡",
            title = "High-Performance Engine",
            description = "Optimised for desktop environments. Scans are processed in under 5ms using native Kotlin Multiplatform.",
            accentColor = DesktopColors.VerdictSafe,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FeatureCard(
    icon: String,
    title: String,
    description: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 22.sp)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun RecentScansCard(
    scans: List<AnalysisResult>,
    onViewAll: () -> Unit,
    onScanClick: (AnalysisResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Scans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onViewAll) {
                    Text(
                        text = "View Full History",
                        color = Color(0xFF2563EB)
                    )
                }
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Status",
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Source",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Details",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Scan Rows
            if (scans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No scans yet. Analyze a URL to get started!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                scans.forEach { scan ->
                    ScanRow(scan = scan, onClick = { onScanClick(scan) })
                }
            }
        }
    }
}

@Composable
private fun ScanRow(
    scan: AnalysisResult,
    onClick: () -> Unit
) {
    val verdictColor = when (scan.verdict) {
        Verdict.SAFE -> DesktopColors.VerdictSafe
        Verdict.SUSPICIOUS -> DesktopColors.VerdictSuspicious
        else -> DesktopColors.VerdictMalicious
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Badge
            Surface(
                modifier = Modifier.width(100.dp),
                shape = RoundedCornerShape(6.dp),
                color = verdictColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, verdictColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (scan.verdict) {
                            Verdict.SAFE -> "✓"
                            Verdict.SUSPICIOUS -> "⚠"
                            else -> "✕"
                        },
                        fontSize = 12.sp,
                        color = verdictColor
                    )
                    Text(
                        text = scan.verdict.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = verdictColor
                    )
                }
            }

            // Source
            Text(
                text = scan.url.removePrefix("https://").removePrefix("http://").take(30),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Details
            Text(
                text = scan.flags.firstOrNull() ?: "URL analyzed",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Time
            Text(
                text = formatTimestamp(scan.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun ThreatDatabaseCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💾", fontSize = 20.sp)
                Text(
                    text = "Threat Database",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DbStatRow("Version", "v2.4.1-stable")
                DbStatRow("Last Update", "Today, 04:00 AM")
                DbStatRow("Signatures", "4,281,092")
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔄", fontSize = 14.sp)
                    Text("Check for Updates")
                }
            }
        }
    }
}

@Composable
private fun DbStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TrainingCentreCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF7C3AED).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🎓", fontSize = 32.sp)
            Text(
                text = "Training Centre",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Learn how to identify advanced QR homograph attacks.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            TextButton(onClick = { }) {
                Text(
                    text = "Beat the Bot →",
                    color = Color(0xFF7C3AED),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
