/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.theme.DesktopColors

@Composable
fun TrustCentreScreen(
    trustedDomains: List<String>,
    onAddDomain: (String) -> Unit,
    onRemoveDomain: (String) -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newDomain by remember { mutableStateOf("") }
    var autoScanEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trust Centre", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onThemeToggle) {
                    Text(if (isDarkMode) "☀️" else "🌙", fontSize = 18.sp)
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Trusted Domains
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Trusted Domains", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                // Add Domain
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = newDomain, onValueChange = { newDomain = it },
                            placeholder = { Text("Add domain (e.g., google.com)") },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent),
                            modifier = Modifier.weight(1f), singleLine = true
                        )
                        Button(onClick = { if (newDomain.isNotBlank()) { onAddDomain(newDomain); newDomain = "" } },
                            colors = ButtonDefaults.buttonColors(containerColor = DesktopColors.VerdictSafe)) { Text("Add") }
                    }
                }

                // Domain List
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                    if (trustedDomains.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🛡️", fontSize = 40.sp)
                                Text("No trusted domains", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            trustedDomains.forEach { domain ->
                                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(Modifier.size(36.dp).clip(CircleShape).background(DesktopColors.VerdictSafe.copy(0.1f)), contentAlignment = Alignment.Center) {
                                            Text("✓", color = DesktopColors.VerdictSafe)
                                        }
                                        Text(domain, fontWeight = FontWeight.Medium)
                                    }
                                    IconButton(onClick = { onRemoveDomain(domain) }) { Text("🗑️") }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.1f))
                            }
                        }
                    }
                }
            }

            // Settings
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Privacy & Security", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                // Privacy Card
                Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF2563EB).copy(0.1f)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🔒", fontSize = 24.sp)
                            Text("Privacy-First Design", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        }
                        Text("All data processed locally. No external servers.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Settings
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Auto-Scan on Paste", fontWeight = FontWeight.Medium); Text("Analyze URLs from clipboard", style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = autoScanEnabled, onCheckedChange = { autoScanEnabled = it })
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("Notifications", fontWeight = FontWeight.Medium); Text("Alert on threats", style = MaterialTheme.typography.bodySmall) }
                            Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                        }
                    }
                }
            }
        }
    }
}
