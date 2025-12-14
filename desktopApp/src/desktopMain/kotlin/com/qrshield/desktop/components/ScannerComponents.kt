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

package com.qrshield.desktop.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.theme.DesktopColors
import com.qrshield.model.Verdict

/**
 * Scanner-related UI components for QR-SHIELD Desktop
 *
 * @author QR-SHIELD Team
 * @since 1.1.4
 */

// ============================================
// SCANNER CARD - Premium Glassmorphism Design
// ============================================

/**
 * Main scanner input card with URL field and analyze button.
 * Features glassmorphism, gradient accents, and premium feel.
 */
@Composable
fun EnhancedScannerCard(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = DesktopColors.BrandPrimary.copy(alpha = 0.15f),
                spotColor = DesktopColors.BrandPrimary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    DesktopColors.BrandPrimary.copy(alpha = 0.3f),
                    DesktopColors.BrandAccent.copy(alpha = 0.2f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        // Gradient overlay at top
        Box {
            // Top accent gradient bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                DesktopColors.BrandPrimary,
                                DesktopColors.BrandAccent,
                                DesktopColors.BrandSecondary
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Floating icon with glow
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(8.dp, CircleShape, spotColor = DesktopColors.BrandPrimary.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    DesktopColors.BrandPrimary,
                                    DesktopColors.BrandAccent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔗",
                        fontSize = 22.sp
                    )
                }
                Column {
                    Text(
                        text = "Analyze URL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Paste or type any URL to check for phishing threats",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // URL Input with premium styling
            // URL Input with high visibility
            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                placeholder = {
                    Text(
                        "https://example.com/suspicious-page",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesktopColors.BrandPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    cursorColor = DesktopColors.BrandPrimary
                )
            )

            // Gradient Analyze Button
            Button(
                onClick = onAnalyze,
                enabled = urlInput.isNotBlank() && !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (urlInput.isNotBlank() && !isAnalyzing) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        DesktopColors.BrandPrimary,
                                        DesktopColors.BrandAccent
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        DesktopColors.BrandPrimary.copy(alpha = 0.4f),
                                        DesktopColors.BrandAccent.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAnalyzing) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Text(
                                text = "Analyzing...",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔍",
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Analyze URL",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// QUICK ACTIONS - Premium Design
// ============================================

/**
 * Premium quick action buttons (paste/clear).
 */
@Composable
fun QuickActionsRow(
    onPasteFromClipboard: () -> Unit,
    onClearInput: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Paste Button - Primary action
        Surface(
            onClick = onPasteFromClipboard,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = DesktopColors.BrandPrimary.copy(alpha = 0.1f),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        DesktopColors.BrandPrimary.copy(alpha = 0.4f),
                        DesktopColors.BrandPrimary.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Paste from Clipboard",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DesktopColors.BrandPrimary
                )
            }
        }

        // Clear Button - Secondary action
        Surface(
            onClick = onClearInput,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗑️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Clear Input",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================
// RESULT CARD - Premium Design
// ============================================

/**
 * Premium result card displaying analysis verdict with glassmorphism.
 */
@Composable
fun EnhancedResultCard(result: AnalysisResult, isDarkMode: Boolean) {
    val verdictColor = when (result.verdict) {
        Verdict.SAFE -> DesktopColors.VerdictSafe
        Verdict.SUSPICIOUS -> DesktopColors.VerdictSuspicious
        Verdict.MALICIOUS -> DesktopColors.VerdictMalicious
        Verdict.UNKNOWN -> DesktopColors.VerdictUnknown
    }

    val verdictEmoji = when (result.verdict) {
        Verdict.SAFE -> "✅"
        Verdict.SUSPICIOUS -> "⚠️"
        Verdict.MALICIOUS -> "🚫"
        Verdict.UNKNOWN -> "❓"
    }

    val verdictMessage = when (result.verdict) {
        Verdict.SAFE -> "This URL appears to be safe"
        Verdict.SUSPICIOUS -> "This URL has some suspicious indicators"
        Verdict.MALICIOUS -> "This URL is likely malicious - do not visit!"
        Verdict.UNKNOWN -> "Unable to determine safety level"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            2.dp,
            Brush.linearGradient(
                colors = listOf(
                    verdictColor.copy(alpha = 0.6f),
                    verdictColor.copy(alpha = 0.3f),
                    verdictColor.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        // Top gradient bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            verdictColor,
                            verdictColor.copy(alpha = 0.7f),
                            verdictColor.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Verdict Icon with glow
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                verdictColor.copy(alpha = 0.35f),
                                verdictColor.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                verdictColor,
                                verdictColor.copy(alpha = 0.5f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = verdictEmoji,
                    fontSize = 48.sp
                )
            }

            // Score Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Score Number with gradient
                Text(
                    text = result.score.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = verdictColor
                )

                Text(
                    text = "Risk Score",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Premium Score Bar
                Box(
                    modifier = Modifier
                        .width(240.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(6.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(result.score / 100f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        verdictColor,
                                        verdictColor.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .shadow(4.dp, RoundedCornerShape(6.dp), spotColor = verdictColor)
                    )
                }
            }

            // Verdict Badge - Pill shape
            Surface(
                color = verdictColor,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = result.verdict.name,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 1.sp
                )
            }

            // Verdict Message
            Text(
                text = verdictMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // URL Display - Premium style
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔗", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = result.url,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Confidence Indicator - Enhanced
            EnhancedConfidenceIndicator(
                score = result.score,
                flagCount = result.flags.size
            )

            // Flags with Expandable Details
            if (result.flags.isNotEmpty()) {
                // Gradient Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DesktopColors.BrandPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧠", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "Why This Verdict?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Click signals for details and remediation",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Signal Cards
                    result.flags.forEach { flag ->
                        ExpandableSignalCard(flag = flag)
                    }
                }
            }
        }
    }
}

/**
 * Enhanced Confidence Indicator with better visibility.
 */
@Composable
fun EnhancedConfidenceIndicator(
    score: Int,
    flagCount: Int
) {
    val (dots, level, label) = remember(score, flagCount) {
        var d = 2
        var l = "low"
        var lb = "Low"
        
        if (score >= 80 || score <= 15) {
            d = 5; l = "very-high"; lb = "Very High"
        } else if (score >= 65 || score <= 25) {
            d = 4; l = "high"; lb = "High"
        } else if (score >= 50 || score <= 35) {
            d = 3; l = "medium"; lb = "Medium"
        }
        
        if (flagCount >= 4 && d < 5) {
            d += 1
            if (l == "medium") { l = "high"; lb = "High" }
        }
        
        Triple(d, l, lb)
    }

    val dotColor = when (level) {
        "very-high" -> DesktopColors.VerdictSafe
        "high" -> DesktopColors.VerdictSafe.copy(alpha = 0.85f)
        "medium" -> DesktopColors.VerdictSuspicious
        else -> DesktopColors.VerdictSuspicious.copy(alpha = 0.6f)
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = dotColor.copy(alpha = 0.15f),
        border = BorderStroke(2.dp, dotColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Confidence dots
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < dots) dotColor else dotColor.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            Text(
                text = "$label Confidence",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = dotColor
            )
        }
    }
}

// ============================================
// FEATURES GRID
// ============================================

/**
 * Grid showing detection capabilities.
 */
@Composable
fun EnhancedFeaturesGrid() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Detection Capabilities",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                icon = "🔍",
                title = "Heuristics",
                description = "25+ detection rules",
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                icon = "🤖",
                title = "ML Model",
                description = "AI-powered scoring",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureCard(
                icon = "🏷️",
                title = "Brand Check",
                description = "Typosquat detection",
                modifier = Modifier.weight(1f)
            )
            FeatureCard(
                icon = "🌐",
                title = "TLD Analysis",
                description = "Risk domain scoring",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ============================================
// RECENT SCANS
// ============================================

/**
 * Section showing recent scan history.
 */
@Composable
fun RecentScansSection(
    scans: List<AnalysisResult>,
    onScanClick: (AnalysisResult) -> Unit,
    onClearHistory: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Scans",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            TextButton(
                onClick = onClearHistory,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear History")
            }
        }

        scans.take(5).forEach { scan ->
            RecentScanItem(scan = scan, onClick = { onScanClick(scan) })
        }
    }
}

/**
 * Individual recent scan item.
 */
@Composable
fun RecentScanItem(
    scan: AnalysisResult,
    onClick: () -> Unit
) {
    val verdictColor = when (scan.verdict) {
        Verdict.SAFE -> DesktopColors.VerdictSafe
        Verdict.SUSPICIOUS -> DesktopColors.VerdictSuspicious
        Verdict.MALICIOUS -> DesktopColors.VerdictMalicious
        Verdict.UNKNOWN -> DesktopColors.VerdictUnknown
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Verdict indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(verdictColor)
            )

            // URL
            Text(
                text = scan.url,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Score badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = verdictColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${scan.score}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = verdictColor
                )
            }
        }
    }
}
