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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.theme.DesktopColors
import com.qrshield.model.Verdict

/**
 * History Screen (Scan History) matching the HTML design.
 * Features:
 * - Full scan history table with filtering
 * - Search functionality
 * - Export options
 * - Detailed scan information
 */
@Composable
fun HistoryScreen(
    scanHistory: List<AnalysisResult>,
    onScanClick: (AnalysisResult) -> Unit,
    onClearHistory: () -> Unit,
    onExport: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredScans = scanHistory.filter { scan ->
        val matchesSearch = searchQuery.isEmpty() || 
            scan.url.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Safe" -> scan.verdict == Verdict.SAFE
            "Suspicious" -> scan.verdict == Verdict.SUSPICIOUS
            "Malicious" -> scan.verdict == Verdict.MALICIOUS
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        HistoryHeader(isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Page Title & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scan History",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Review and manage your scan records",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stats Summary
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBadge(
                        count = scanHistory.count { it.verdict == Verdict.SAFE },
                        label = "Safe",
                        color = DesktopColors.VerdictSafe
                    )
                    StatBadge(
                        count = scanHistory.count { it.verdict == Verdict.SUSPICIOUS },
                        label = "Suspicious",
                        color = DesktopColors.VerdictSuspicious
                    )
                    StatBadge(
                        count = scanHistory.count { it.verdict == Verdict.MALICIOUS },
                        label = "Blocked",
                        color = DesktopColors.VerdictMalicious
                    )
                }
            }

            // Controls Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search
                    Surface(
                        modifier = Modifier.width(300.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔍", fontSize = 14.sp)
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search scans...") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Filters
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("All", "Safe", "Suspicious", "Malicious").forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB).copy(alpha = 0.1f),
                                    selectedLabelColor = Color(0xFF2563EB)
                                )
                            )
                        }
                    }

                    // Actions
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onExport,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📥", fontSize = 14.sp)
                                Text("Export")
                            }
                        }
                        OutlinedButton(
                            onClick = onClearHistory,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DesktopColors.VerdictMalicious
                            ),
                            border = BorderStroke(1.dp, DesktopColors.VerdictMalicious.copy(alpha = 0.5f))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🗑️", fontSize = 14.sp)
                                Text("Clear")
                            }
                        }
                    }
                }
            }

            // History Table
            HistoryTable(
                scans = filteredScans,
                onScanClick = onScanClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HistoryHeader(
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
                        text = "Scan History",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Theme Toggle
            IconButton(onClick = onThemeToggle) {
                Text(
                    text = if (isDarkMode) "☀️" else "🌙",
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    count: Int,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun HistoryTable(
    scans: List<AnalysisResult>,
    onScanClick: (AnalysisResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Status",
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "URL",
                    modifier = Modifier.weight(2f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Risk Score",
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Flags",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Time",
                    modifier = Modifier.width(120.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Table Body
            if (scans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "📋", fontSize = 40.sp)
                        Text(
                            text = "No scans found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Scanned URLs will appear here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    scans.forEach { scan ->
                        HistoryRow(
                            scan = scan,
                            onClick = { onScanClick(scan) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    scan: AnalysisResult,
    onClick: () -> Unit
) {
    val verdictColor = when (scan.verdict) {
        Verdict.SAFE -> DesktopColors.VerdictSafe
        Verdict.SUSPICIOUS -> DesktopColors.VerdictSuspicious
        else -> DesktopColors.VerdictMalicious
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(verdictColor)
                    )
                    Text(
                        text = scan.verdict.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = verdictColor
                    )
                }
            }

            // URL
            Text(
                text = scan.url,
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Risk Score
            Text(
                text = "${scan.score}%",
                modifier = Modifier.width(100.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = verdictColor
            )

            // Flags
            Text(
                text = scan.flags.firstOrNull() ?: "-",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Time
            Text(
                text = formatTimestamp(scan.timestamp),
                modifier = Modifier.width(120.dp),
                style = MaterialTheme.typography.labelMedium,
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
        diff < 3600_000 -> "${diff / 60_000} min ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        else -> "${diff / 86400_000} days ago"
    }
}
