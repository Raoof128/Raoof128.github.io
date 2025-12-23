/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.qrshield.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.android.ui.theme.QRShieldColors
import com.qrshield.android.ui.theme.QRShieldShapes

/**
 * Export Report Screen
 * Matches the HTML "Export Report" design with:
 * - Report preview card
 * - Format selection (PDF, CSV, JSON)
 * - Content customization toggles
 * - Export button with offline note
 */

enum class ExportFormat(val label: String, val icon: ImageVector, val extension: String) {
    PDF("PDF", Icons.Default.PictureAsPdf, ".pdf"),
    CSV("CSV", Icons.Default.TableChart, ".csv"),
    JSON("JSON", Icons.Default.Code, ".json")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportScreen(
    onBackClick: () -> Unit = {},
    onExport: (ExportFormat) -> Unit = {},
    modifier: Modifier = Modifier,
    // Sample data
    scanUrl: String = "login-microsoft-update.com.xyz",
    verdict: String = "Malicious",
    scanDate: String = "24 October 2024",
    scanTime: String = "14:32 UTC"
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }
    var includeScreenshot by remember { mutableStateOf(true) }
    var includeHeuristicsLog by remember { mutableStateOf(false) }
    var includeRawHeaders by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Export Report",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Report Preview Card
                ReportPreviewCard(
                    scanUrl = scanUrl,
                    verdict = verdict,
                    scanDate = scanDate,
                    scanTime = scanTime
                )

                // Format Selection
                FormatSelectionSection(
                    selectedFormat = selectedFormat,
                    onFormatSelected = { selectedFormat = it }
                )

                // Content Options
                ContentOptionsSection(
                    includeScreenshot = includeScreenshot,
                    onIncludeScreenshotChange = { includeScreenshot = it },
                    includeHeuristicsLog = includeHeuristicsLog,
                    onIncludeHeuristicsLogChange = { includeHeuristicsLog = it },
                    includeRawHeaders = includeRawHeaders,
                    onIncludeRawHeadersChange = { includeRawHeaders = it }
                )
            }

            // Bottom Export Button
            ExportBottomBar(
                selectedFormat = selectedFormat,
                onExport = { onExport(selectedFormat) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ReportPreviewCard(
    scanUrl: String,
    verdict: String,
    scanDate: String,
    scanTime: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Rounded-2xl
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp, // slightly stronger shadow
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Column {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFEF4444), Color(0xFFDC2626)) // red-500 to red-600
                        )
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Text(
                            text = "QR-SHIELD",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Text(
                        text = "v2.4",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Body
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Dark mode color adjustments
                val isDark = isSystemInDarkTheme()
                val riskBg = if (isDark) QRShieldColors.Red900.copy(alpha = 0.2f) else QRShieldColors.Red50
                val riskBorder = if (isDark) QRShieldColors.Red900.copy(alpha = 0.3f) else QRShieldColors.Red100
                val riskTitleColor = if (isDark) QRShieldColors.Red100 else QRShieldColors.Red900
                val riskVerdictColor = if (isDark) QRShieldColors.Red400 else QRShieldColors.Red600

                // Risk Assessment Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = riskBg,
                    border = BorderStroke(1.dp, riskBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Risk Assessment",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = riskTitleColor
                            )
                            Text(
                                text = verdict,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = riskVerdictColor
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = if (isDark) QRShieldColors.Red900 else Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = "HIGH RISK",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) QRShieldColors.Red100 else QRShieldColors.Red600
                            )
                        }
                    }
                }

                // Vector Analysis Chart
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Vector Analysis",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    VectorBar("Heuristic Match", 0.85f, QRShieldColors.Red500)
                    VectorBar("ML Confidence", 0.92f, QRShieldColors.Red500)
                    VectorBar("Database Hit", 1.0f, QRShieldColors.Red500)
                }
            }
        }
    }
}

@Composable
private fun VectorBar(label: String, progress: Float, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(9999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(9999.dp))
                    .background(color)
            )
        }
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PreviewRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
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
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = valueColor
        )
    }
}

@Composable
private fun FormatSelectionSection(
    selectedFormat: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Export Format",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        // Segmented Control
        Surface(
            modifier = Modifier.fillMaxWidth().height(56.dp), // h-14 equivalent
            shape = RoundedCornerShape(9999.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExportFormat.entries.forEach { format ->
                    val isSelected = format == selectedFormat
                    
                    // Selected: bg-white shadow-sm text-primary
                    // Unselected: text-gray-500
                    
                    Surface(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = RoundedCornerShape(9999.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        onClick = { onFormatSelected(format) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                           Icon(
                               imageVector = format.icon,
                               contentDescription = null,
                               tint = if (isSelected) QRShieldColors.Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                               modifier = Modifier.size(18.dp)
                           )
                           Spacer(modifier = Modifier.width(8.dp))
                           Text(
                               text = format.label,
                               style = MaterialTheme.typography.bodyMedium.copy(
                                   fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                               ),
                               color = if (isSelected) QRShieldColors.Primary else MaterialTheme.colorScheme.onSurfaceVariant
                           )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentOptionsSection(
    includeScreenshot: Boolean,
    onIncludeScreenshotChange: (Boolean) -> Unit,
    includeHeuristicsLog: Boolean,
    onIncludeHeuristicsLogChange: (Boolean) -> Unit,
    includeRawHeaders: Boolean,
    onIncludeRawHeadersChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Include in Report",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = QRShieldShapes.Card,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column {
                ContentToggleRow(
                    icon = Icons.Default.Fullscreen,
                    title = "Page Screenshot",
                    subtitle = "Visual capture of landing page",
                    checked = includeScreenshot,
                    onCheckedChange = onIncludeScreenshotChange,
                    showDivider = true
                )
                ContentToggleRow(
                    icon = Icons.Default.BugReport,
                    title = "Heuristics Log",
                    subtitle = "Detailed rule matching output",
                    checked = includeHeuristicsLog,
                    onCheckedChange = onIncludeHeuristicsLogChange,
                    showDivider = true
                )
                ContentToggleRow(
                    icon = Icons.Default.Code,
                    title = "Raw HTTP Headers",
                    subtitle = "Server response metadata",
                    checked = includeRawHeaders,
                    onCheckedChange = onIncludeRawHeadersChange,
                    showDivider = false
                )
            }
        }
    }
}

@Composable
private fun ContentToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = QRShieldColors.Primary
                )
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ExportBottomBar(
    selectedFormat: ExportFormat,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Offline Note
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Generated locally • No data leaves your device",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Export Button
            Button(
                onClick = onExport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(9999.dp),
                        ambientColor = QRShieldColors.Primary.copy(alpha = 0.3f),
                        spotColor = QRShieldColors.Primary.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QRShieldColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export as ${selectedFormat.label}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
