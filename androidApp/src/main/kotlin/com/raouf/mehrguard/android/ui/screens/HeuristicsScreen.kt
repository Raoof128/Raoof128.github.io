/*
 * Copyright 2025-2026 Mehr Guard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.raouf.mehrguard.android.ui.screens

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
import com.raouf.mehrguard.android.ui.theme.MehrGuardColors
import com.raouf.mehrguard.android.ui.theme.MehrGuardShapes
import androidx.compose.ui.res.stringResource
import com.raouf.mehrguard.android.R

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

enum class RuleCategory(val labelResId: Int) {
    ALL(R.string.category_all),
    URL_PATTERN(R.string.category_url_pattern),
    DOMAIN(R.string.category_domain),
    REDIRECT(R.string.category_redirect),
    SSL(R.string.category_ssl),
    CONTENT(R.string.category_content)
}

enum class RuleSeverity(val labelResId: Int) {
    CRITICAL(R.string.severity_critical),
    HIGH(R.string.severity_high),
    MEDIUM(R.string.severity_medium),
    LOW(R.string.severity_low)
}

/**
 * Returns localized default heuristic rules
 */
@Composable
private fun getDefaultRules(): List<HeuristicRule> {
    return listOf(
        HeuristicRule("1", stringResource(R.string.heuristic_homograph_title), stringResource(R.string.heuristic_homograph_desc), RuleCategory.URL_PATTERN, RuleSeverity.CRITICAL, true),
        HeuristicRule("2", stringResource(R.string.heuristic_suspicious_tld_title), stringResource(R.string.heuristic_suspicious_tld_desc), RuleCategory.DOMAIN, RuleSeverity.HIGH, true),
        HeuristicRule("3", stringResource(R.string.heuristic_redirect_chain_title), stringResource(R.string.heuristic_redirect_chain_desc), RuleCategory.REDIRECT, RuleSeverity.MEDIUM, true),
        HeuristicRule("4", stringResource(R.string.heuristic_new_domain_title), stringResource(R.string.heuristic_new_domain_desc), RuleCategory.DOMAIN, RuleSeverity.HIGH, true),
        HeuristicRule("5", stringResource(R.string.heuristic_ssl_cert_title), stringResource(R.string.heuristic_ssl_cert_desc), RuleCategory.SSL, RuleSeverity.MEDIUM, false),
        HeuristicRule("6", stringResource(R.string.heuristic_brand_title), stringResource(R.string.heuristic_brand_desc), RuleCategory.CONTENT, RuleSeverity.CRITICAL, true)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeuristicsScreen(
    onBackClick: () -> Unit = {},
    onAddRule: () -> Unit = {},
    onRuleClick: (String) -> Unit = {},
    onToggleRule: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    rules: List<HeuristicRule>? = null,
    activeRulesCount: Int = 5,
    detectionRate: Float = 98.7f
) {
    val effectiveRules = rules ?: getDefaultRules()
    var selectedCategory by remember { mutableStateOf(RuleCategory.ALL) }

    val filteredRules = if (selectedCategory == RuleCategory.ALL) {
        effectiveRules
    } else {
        effectiveRules.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.heuristics_engine_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = onAddRule,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MehrGuardColors.Primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_rule),
                            tint = MehrGuardColors.Primary
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
                        text = stringResource(R.string.detection_rules),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.rules_count_fmt, filteredRules.size),
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
            shape = MehrGuardShapes.Card,
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
                    tint = MehrGuardColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activeRulesCount.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MehrGuardColors.Primary
                )
                Text(
                    text = stringResource(R.string.active_rules),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Detection Rate Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = MehrGuardShapes.Card,
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
                    tint = MehrGuardColors.Emerald500,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${detectionRate}%",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MehrGuardColors.Emerald500
                )
                Text(
                    text = stringResource(R.string.detection_rate),
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
                        text = stringResource(category.labelResId),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(9999.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MehrGuardColors.Primary,
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
        RuleSeverity.CRITICAL -> MehrGuardColors.RiskDanger
        RuleSeverity.HIGH -> MehrGuardColors.Orange500
        RuleSeverity.MEDIUM -> MehrGuardColors.Yellow500
        RuleSeverity.LOW -> MehrGuardColors.Gray500
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MehrGuardShapes.Card,
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
                            text = stringResource(rule.severity.labelResId),
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
                    text = stringResource(rule.category.labelResId),
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
                    checkedTrackColor = MehrGuardColors.Primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}
