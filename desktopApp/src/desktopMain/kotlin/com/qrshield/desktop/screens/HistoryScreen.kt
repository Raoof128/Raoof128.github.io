/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.desktop.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.theme.DesktopColors
import com.qrshield.model.Verdict

/**
 * History Screen matching the HTML scan history design exactly.
 * Features search, filters, detailed table, and batch actions.
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
    var dateRange by remember { mutableStateOf("All Time") }
    val filterOptions = listOf("All", "Safe", "Suspicious", "Malicious")
    val dateOptions = listOf("All Time", "Today", "This Week", "This Month")

    // Filter scans based on search and filter
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

    // Stats
    val totalCount = scanHistory.size
    val safeCount = scanHistory.count { it.verdict == Verdict.SAFE }
    val suspiciousCount = scanHistory.count { it.verdict == Verdict.SUSPICIOUS }
    val maliciousCount = scanHistory.count { it.verdict == Verdict.MALICIOUS }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Bar
        HistoryHeader(isDarkMode = isDarkMode, onThemeToggle = onThemeToggle)

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Page Title & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Scan History",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Complete log of all QR code and URL analyses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onExport,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📥", fontSize = 14.sp)
                            Text("Export CSV")
                        }
                    }
                    OutlinedButton(
                        onClick = onClearHistory,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🗑️", fontSize = 14.sp)
                            Text("Clear All")
                        }
                    }
                }
            }

            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    value = totalCount.toString(),
                    label = "Total Scans",
                    icon = "📊",
                    iconBgColor = Color(0xFF2563EB).copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = safeCount.toString(),
                    label = "Safe",
                    icon = "✓",
                    iconBgColor = Color(0xFF10B981).copy(alpha = 0.1f),
                    valueColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = suspiciousCount.toString(),
                    label = "Suspicious",
                    icon = "⚠️",
                    iconBgColor = Color(0xFFF59E0B).copy(alpha = 0.1f),
                    valueColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = maliciousCount.toString(),
                    label = "Malicious",
                    icon = "⛔",
                    iconBgColor = Color(0xFFEF4444).copy(alpha = 0.1f),
                    valueColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
            }

            // Search & Filter Bar
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
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search by URL, domain...") },
                        leadingIcon = { Text("🔍", fontSize = 16.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    )

                    // Filter Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        filterOptions.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB).copy(alpha = 0.1f),
                                    selectedLabelColor = Color(0xFF2563EB)
                                )
                            )
                        }
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    )

                    // Date Range Dropdown
                    var dateDropdownExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { dateDropdownExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📅", fontSize = 14.sp)
                                Text(dateRange)
                                Text("▼", fontSize = 10.sp)
                            }
                        }
                        DropdownMenu(
                            expanded = dateDropdownExpanded,
                            onDismissRequest = { dateDropdownExpanded = false }
                        ) {
                            dateOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        dateRange = option
                                        dateDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // History Table
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                shadowElevation = 1.dp
            ) {
                Column {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "STATUS",
                            modifier = Modifier.width(130.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "URL / TARGET",
                            modifier = Modifier.weight(1.5f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "SCORE",
                            modifier = Modifier.width(80.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "FLAGS",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "SCANNED",
                            modifier = Modifier.width(120.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ACTIONS",
                            modifier = Modifier.width(100.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Table Rows
                    if (filteredScans.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("📋", fontSize = 48.sp)
                                Text(
                                    text = "No scan data found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (searchQuery.isNotEmpty() || selectedFilter != "All")
                                        "Try adjusting your filters or search query"
                                    else "Start scanning to build your history",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column {
                            filteredScans.forEach { scan ->
                                HistoryRow(scan = scan, onClick = { onScanClick(scan) })
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            }
                        }
                    }

                    // Pagination Footer
                    if (filteredScans.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Showing ${filteredScans.size} of ${scanHistory.size} entries",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("← Previous", style = MaterialTheme.typography.labelMedium)
                                }
                                Button(
                                    onClick = { },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text("Next →", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
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
                    text = "Scan History",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onThemeToggle) {
                Text(if (isDarkMode) "☀️" else "🌙", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: String,
    iconBgColor: Color,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        Verdict.SAFE -> Color(0xFF10B981)
        Verdict.SUSPICIOUS -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    val verdictLabel = when (scan.verdict) {
        Verdict.SAFE -> "SAFE"
        Verdict.SUSPICIOUS -> "SUSPICIOUS"
        else -> "MALICIOUS"
    }

    val verdictIcon = when (scan.verdict) {
        Verdict.SAFE -> "✓"
        Verdict.SUSPICIOUS -> "⚠"
        else -> "⛔"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Badge
            Surface(
                modifier = Modifier.width(130.dp),
                shape = RoundedCornerShape(6.dp),
                color = verdictColor.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, verdictColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(verdictIcon, fontSize = 12.sp, color = verdictColor)
                    Text(
                        text = verdictLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = verdictColor
                    )
                }
            }

            // URL
            Row(
                modifier = Modifier
                    .weight(1.5f)
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favicon placeholder
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scan.url.removePrefix("https://").removePrefix("http://").take(2).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column {
                    Text(
                        text = scan.url.removePrefix("https://").removePrefix("http://").take(35),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = scan.url.split("/").firstOrNull()?.removePrefix("https://")?.removePrefix("http://") ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Score
            Box(modifier = Modifier.width(80.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (scan.score > 0.7) Color(0xFFEF4444).copy(alpha = 0.1f)
                            else if (scan.score > 0.4) Color(0xFFF59E0B).copy(alpha = 0.1f)
                            else Color(0xFF10B981).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${(scan.score * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (scan.score > 0.7) Color(0xFFEF4444)
                                else if (scan.score > 0.4) Color(0xFFF59E0B)
                                else Color(0xFF10B981)
                    )
                }
            }

            // Flags
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scan.flags.take(2).forEach { flag ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = flag.take(12) + if (flag.length > 12) "…" else "",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
                if (scan.flags.size > 2) {
                    Text(
                        text = "+${scan.flags.size - 2}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Scanned Time
            Text(
                text = formatTimestamp(scan.timestamp),
                modifier = Modifier.width(120.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Actions
            Row(
                modifier = Modifier.width(100.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("👁️", fontSize = 14.sp)
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("📋", fontSize = 14.sp)
                }
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("🗑️", fontSize = 14.sp)
                }
            }
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
        diff < 172800_000 -> "Yesterday"
        else -> "${diff / 86400_000} days ago"
    }
}
