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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.android.ui.theme.QRShieldColors

/**
 * Allowlist Management Screen
 * Matches the HTML "Allowlist Management" design
 */

data class AllowedDomain(
    val id: String,
    val domain: String,
    val addedDate: String,
    val source: AllowlistSource
)

enum class AllowlistSource {
    MANUAL, ENTERPRISE, AUTO_LEARNED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllowlistScreen(
    onBackClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onDeleteItem: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    allowedDomains: List<AllowedDomain> = listOf(
        AllowedDomain("1", "google.com", "Oct 24, 2023", AllowlistSource.AUTO_LEARNED),
        AllowedDomain("2", "microsoft.com", "Oct 20, 2023", AllowlistSource.ENTERPRISE),
        AllowedDomain("3", "github.com", "Sep 12, 2023", AllowlistSource.MANUAL),
        AllowedDomain("4", "slack.com", "Sep 10, 2023", AllowlistSource.ENTERPRISE),
        AllowedDomain("5", "notion.so", "Sep 05, 2023", AllowlistSource.MANUAL)
    ),
    showAddSheet: Boolean = false,
    onDismissSheet: () -> Unit = {},
    onAllowDomain: (String) -> Unit = {}
) {
    var inputUrl by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Allowlist",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(QRShieldColors.Emerald500.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add domain",
                            tint = QRShieldColors.Emerald600
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Info Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = QRShieldColors.Emerald50,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            listOf(
                                QRShieldColors.Emerald100,
                                QRShieldColors.Emerald100
                            )
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = QRShieldColors.Emerald600,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Trusted Domains",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = QRShieldColors.Emerald600
                            )
                            Text(
                                text = "URLs from these domains will always be marked as safe. Use with caution.",
                                style = MaterialTheme.typography.bodySmall,
                                color = QRShieldColors.Emerald600.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Import Button
            item {
                AllowlistImportButton(onClick = onImportClick)
            }

            // List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "TRUSTED DOMAINS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${allowedDomains.size} Entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Domain Items
            items(allowedDomains) { domain ->
                AllowedDomainItem(
                    domain = domain,
                    onDelete = { onDeleteItem(domain.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Bottom Sheet Modal
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismissSheet,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .width(48.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        ) {
            AddAllowlistSheet(
                inputUrl = inputUrl,
                onInputChange = { inputUrl = it },
                onCancel = onDismissSheet,
                onAllow = { onAllowDomain(inputUrl) }
            )
        }
    }
}

@Composable
private fun AllowlistImportButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.colorScheme.outlineVariant
                )
            )
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = null,
                tint = QRShieldColors.Emerald600,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Import Enterprise List",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AllowedDomainItem(
    domain: AllowedDomain,
    onDelete: () -> Unit
) {
    val sourceLabel = when (domain.source) {
        AllowlistSource.MANUAL -> "Manual"
        AllowlistSource.ENTERPRISE -> "Enterprise"
        AllowlistSource.AUTO_LEARNED -> "Auto-learned"
    }

    val sourceBgColor = when (domain.source) {
        AllowlistSource.ENTERPRISE -> QRShieldColors.Blue50
        AllowlistSource.AUTO_LEARNED -> QRShieldColors.Purple50
        AllowlistSource.MANUAL -> MaterialTheme.colorScheme.surfaceVariant
    }

    val sourceTextColor = when (domain.source) {
        AllowlistSource.ENTERPRISE -> QRShieldColors.Blue600
        AllowlistSource.AUTO_LEARNED -> QRShieldColors.Purple600
        AllowlistSource.MANUAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(QRShieldColors.Emerald50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = QRShieldColors.Emerald600,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = domain.domain,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Added ${domain.addedDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = sourceBgColor
                        ) {
                            Text(
                                text = sourceLabel,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                fontSize = 9.sp,
                                color = sourceTextColor
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddAllowlistSheet(
    inputUrl: String,
    onInputChange: (String) -> Unit,
    onCancel: () -> Unit,
    onAllow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Trusted Domain",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Warning Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = QRShieldColors.Orange50,
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(
                    listOf(QRShieldColors.Orange100, QRShieldColors.Orange100)
                )
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = QRShieldColors.Orange600,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Allowlisted domains bypass all security checks. Only add domains you fully trust.",
                    style = MaterialTheme.typography.bodySmall,
                    color = QRShieldColors.Orange600
                )
            }
        }

        // Input Field
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "DOMAIN NAME",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )

            OutlinedTextField(
                value = inputUrl,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("example.com") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = QRShieldColors.Emerald500,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // Allow Button
        Button(
            onClick = onAllow,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(9999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = QRShieldColors.Emerald500
            ),
            enabled = inputUrl.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add to Allowlist",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
