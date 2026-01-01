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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
 * Attack Breakdown Screen
 * Matches the HTML "Attack Breakdown" design with:
 * - Threat summary header
 * - Attack chain visualization
 * - Indicators of Compromise (IOCs)
 * - Remediation steps
 */

data class AttackPhase(
    val id: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val status: PhaseStatus
)

enum class PhaseStatus {
    COMPLETED, BLOCKED, PREVENTED
}

data class IndicatorOfCompromise(
    val type: String,
    val value: String,
    val severity: IOCSeverity
)

enum class IOCSeverity {
    HIGH, MEDIUM, LOW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttackBreakdownScreen(
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onBlockDomain: () -> Unit = {},
    onReportToIT: () -> Unit = {},
    modifier: Modifier = Modifier,
    // Sample data
    threatType: String = "Credential Phishing",
    attackerGoal: String = "Harvest Microsoft 365 login credentials",
    confidenceScore: Int = 94,
    attackPhases: List<AttackPhase> = listOf(
        AttackPhase(1, "Initial Access", "QR code scanned from physical document", Icons.Default.QrCodeScanner, PhaseStatus.COMPLETED),
        AttackPhase(2, "Redirection", "3-hop URL redirect chain detected", Icons.AutoMirrored.Filled.AltRoute, PhaseStatus.COMPLETED),
        AttackPhase(3, "Spoofed Login", "Fake Microsoft login page rendered", Icons.Default.Web, PhaseStatus.BLOCKED),
        AttackPhase(4, "Data Exfiltration", "Credentials would be sent to attacker server", Icons.Default.CloudUpload, PhaseStatus.PREVENTED)
    ),
    iocs: List<IndicatorOfCompromise> = listOf(
        IndicatorOfCompromise("Domain", "login-microsoft-update.xyz", IOCSeverity.HIGH),
        IndicatorOfCompromise("IP Address", "185.234.72.91", IOCSeverity.HIGH),
        IndicatorOfCompromise("SSL Cert", "Let's Encrypt (< 24h old)", IOCSeverity.MEDIUM),
        IndicatorOfCompromise("Registrar", "NameCheap via privacy proxy", IOCSeverity.LOW)
    )
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.attack_analysis),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.cd_share))
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Threat Summary Header
                ThreatSummaryCard(
                    threatType = threatType,
                    attackerGoal = attackerGoal,
                    confidenceScore = confidenceScore
                )

                // Attack Chain Section
                AttackChainSection(attackPhases = attackPhases)

                // IOCs Section
                IOCsSection(iocs = iocs)

                // Remediation Section
                RemediationSection()
            }

            // Bottom Actions
            ActionBottomBar(
                onBlockDomain = onBlockDomain,
                onReportToIT = onReportToIT,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ThreatSummaryCard(
    threatType: String,
    attackerGoal: String,
    confidenceScore: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MehrGuardShapes.Card,
        color = MehrGuardColors.Red50,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                listOf(MehrGuardColors.Red100, MehrGuardColors.Red100)
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MehrGuardColors.RiskDanger.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MehrGuardColors.RiskDanger,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = threatType,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MehrGuardColors.Red600
                        )
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = MehrGuardColors.RiskDanger
                        ) {
                            Text(
                                text = "$confidenceScore%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = attackerGoal,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MehrGuardColors.Red600.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AttackChainSection(attackPhases: List<AttackPhase>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.attack_chain),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
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
            Column(modifier = Modifier.padding(16.dp)) {
                attackPhases.forEachIndexed { index, phase ->
                    AttackPhaseItem(
                        phase = phase,
                        isLast = index == attackPhases.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun AttackPhaseItem(
    phase: AttackPhase,
    isLast: Boolean
) {
    val (statusColor, statusIcon, statusLabel) = when (phase.status) {
        PhaseStatus.COMPLETED -> Triple(
            MehrGuardColors.Orange500,
            Icons.Default.CheckCircle,
            "Executed"
        )
        PhaseStatus.BLOCKED -> Triple(
            MehrGuardColors.RiskDanger,
            Icons.Default.Block,
            "Blocked"
        )
        PhaseStatus.PREVENTED -> Triple(
            MehrGuardColors.Emerald500,
            Icons.Default.Shield,
            "Prevented"
        )
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = phase.id.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) 16.dp else 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = phase.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = phase.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = phase.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IOCsSection(iocs: List<IndicatorOfCompromise>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.iocs_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
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
            )
        ) {
            Column {
                iocs.forEachIndexed { index, ioc ->
                    IOCItem(
                        ioc = ioc,
                        showDivider = index < iocs.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun IOCItem(
    ioc: IndicatorOfCompromise,
    showDivider: Boolean
) {
    val severityColor = when (ioc.severity) {
        IOCSeverity.HIGH -> MehrGuardColors.RiskDanger
        IOCSeverity.MEDIUM -> MehrGuardColors.Orange500
        IOCSeverity.LOW -> MehrGuardColors.Gray500
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ioc.type,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ioc.value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(severityColor)
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
private fun RemediationSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.recommended_actions),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MehrGuardShapes.Card,
            color = MehrGuardColors.Emerald50,
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = Brush.linearGradient(
                    listOf(MehrGuardColors.Emerald100, MehrGuardColors.Emerald100)
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RemediationItem(
                    number = 1,
                    text = stringResource(R.string.remediation_1)
                )
                RemediationItem(
                    number = 2,
                    text = stringResource(R.string.remediation_2)
                )
                RemediationItem(
                    number = 3,
                    text = stringResource(R.string.remediation_3)
                )
            }
        }
    }
}

@Composable
private fun RemediationItem(number: Int, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MehrGuardColors.Emerald500),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MehrGuardColors.Emerald600,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionBottomBar(
    onBlockDomain: () -> Unit,
    onReportToIT: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBlockDomain,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = Brush.linearGradient(
                        listOf(MehrGuardColors.RiskDanger, MehrGuardColors.RiskDanger)
                    )
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MehrGuardColors.RiskDanger
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.action_block),
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onReportToIT,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(9999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MehrGuardColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.action_report_it),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
