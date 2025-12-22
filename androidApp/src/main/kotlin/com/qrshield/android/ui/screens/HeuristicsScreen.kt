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
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.android.ui.theme.QRShieldColors

/**
 * Heuristics Rules Screen
 * Matches the HTML "Heuristics Rules" design with:
 * - Stats header (active rules, detection rate)
 * - Category filter chips
 * - Rules list with severity indicators
 * - Rule detail expandable items
 */

data class HeuristicRule(
    val id: String,
    val name: String,
    val description: String,
    val category: RuleCategory,
    val severity: RuleSeverity,
    val isActive: Boolean
)

enum class RuleCategory(val label: String) {
    ALL("All"),
    URL_PATTERN("URL Pattern"),
    DOMAIN("Domain"),
    REDIRECT("Redirect"),
    SSL("SSL/TLS"),
    CONTENT("Content")
}

enum class RuleSeverity(val label: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeuristicsScreen(
    onBackClick: () -> Unit = {},
    onAddRule: () -> Unit = {},
    onRuleClick: (String) -> Unit = {},
    onToggleRule: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    rules: List<HeuristicRule> = listOf(
        HeuristicRule("1", "Homograph Detection", "Detects unicode characters that mimic ASCII letters", RuleCategory.URL_PATTERN, RuleSeverity.CRITICAL, true),
        HeuristicRule("2", "Suspicious TLD", "Flags uncommon or high-risk top-level domains", RuleCategory.DOMAIN, RuleSeverity.HIGH, true),
        HeuristicRule("3", "Redirect Chain", "Detects excessive URL redirections (>3 hops)", RuleCategory.REDIRECT, RuleSeverity.MEDIUM, true),
        HeuristicRule("4", "New Domain Alert", "Flags domains registered < 30 days ago", RuleCategory.DOMAIN, RuleSeverity.HIGH, true),
        HeuristicRule("5", "SSL Certificate Age", "Warns on certificates issued < 7 days ago", RuleCategory.SSL, RuleSeverity.MEDIUM, false),
        HeuristicRule("6", "Brand Impersonation", "Detects known brand names in suspicious domains", RuleCategory.CONTENT, RuleSeverity.CRITICAL, true)
    ),
    activeRulesCount: Int = 5,
    detectionRate: Float = 98.7f
) {
    var selectedCategory by remember { mutableStateOf(RuleCategory.ALL) }

    val filteredRules = if (selectedCategory == RuleCategory.ALL) {
        rules
    } else {
        rules.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Heuristics Engine",
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
                        onClick = onAddRule,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(QRShieldColors.Primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add rule",
                            tint = QRShieldColors.Primary
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Header
            item {
                StatsHeader(
                    activeRulesCount = activeRulesCount,
                    detectionRate = detectionRate
                )
            }

            // Category Chips
            item {
                CategoryChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // Rules List Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "DETECTION RULES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${filteredRules.size} Rules",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Rules Items
            items(filteredRules) { rule ->
                RuleItem(
                    rule = rule,
                    onClick = { onRuleClick(rule.id) },
                    onToggle = { onToggleRule(rule.id, it) }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StatsHeader(
    activeRulesCount: Int,
    detectionRate: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active Rules Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Rule,
                    contentDescription = null,
                    tint = QRShieldColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activeRulesCount.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = QRShieldColors.Primary
                )
                Text(
                    text = "Active Rules",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Detection Rate Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = QRShieldColors.Emerald500,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${detectionRate}%",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = QRShieldColors.Emerald500
                )
                Text(
                    text = "Detection Rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryChips(
    selectedCategory: RuleCategory,
    onCategorySelected: (RuleCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RuleCategory.entries.take(4).forEach { category ->
            val isSelected = category == selectedCategory

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(9999.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = QRShieldColors.Primary,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                border = if (!isSelected) {
                    FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        enabled = true,
                        selected = false
                    )
                } else null
            )
        }
    }
}

@Composable
private fun RuleItem(
    rule: HeuristicRule,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val severityColor = when (rule.severity) {
        RuleSeverity.CRITICAL -> QRShieldColors.RiskDanger
        RuleSeverity.HIGH -> QRShieldColors.Orange500
        RuleSeverity.MEDIUM -> QRShieldColors.Yellow500
        RuleSeverity.LOW -> QRShieldColors.Gray500
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Severity Indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(severityColor)
            )

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = severityColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = rule.severity.label,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = severityColor
                        )
                    }
                }

                Text(
                    text = rule.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Category Tag
                Text(
                    text = rule.category.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Toggle Switch
            Switch(
                checked = rule.isActive,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = QRShieldColors.Primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
