/*
 * Copyright 2024 QR-SHIELD Contributors
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
 * @since 1.1.0
 */

// ============================================
// SCANNER CARD
// ============================================

/**
 * Main scanner input card with URL field and analyze button.
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
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Input Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DesktopColors.BrandPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔗",
                        fontSize = 18.sp
                    )
                }
                Column {
                    Text(
                        text = "URL to Analyze",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Paste or type any URL to check for phishing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // URL Input
            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                placeholder = { 
                    Text(
                        "https://example.com",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesktopColors.BrandPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
            
            // Analyze Button
            Button(
                onClick = onAnalyze,
                enabled = urlInput.isNotBlank() && !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DesktopColors.BrandPrimary,
                    disabledContainerColor = DesktopColors.BrandPrimary.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔍",
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Analyze URL",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// QUICK ACTIONS
// ============================================

/**
 * Row of quick action buttons (paste/clear).
 */
@Composable
fun QuickActionsRow(
    onPasteFromClipboard: () -> Unit,
    onClearInput: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onPasteFromClipboard,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋", fontSize = 14.sp)
                Text("Paste", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
        
        OutlinedButton(
            onClick = onClearInput,
            modifier = Modifier.weight(1f).height(44.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗑️", fontSize = 14.sp)
                Text("Clear", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ============================================
// RESULT CARD
// ============================================

/**
 * Enhanced result card displaying analysis verdict.
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
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) 
                verdictColor.copy(alpha = 0.08f) 
            else 
                verdictColor.copy(alpha = 0.06f)
        ),
        border = BorderStroke(2.dp, verdictColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Verdict Icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                verdictColor.copy(alpha = 0.3f),
                                verdictColor.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(3.dp, verdictColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = verdictEmoji,
                    fontSize = 44.sp
                )
            }
            
            // Score with circular progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = result.score.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = verdictColor
                )
                
                Text(
                    text = "Risk Score",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Score Bar
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(result.score / 100f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(verdictColor, verdictColor.copy(alpha = 0.7f))
                                )
                            )
                    )
                }
            }
            
            // Verdict Badge
            Surface(
                color = verdictColor,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = result.verdict.name,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            // Verdict Message
            Text(
                text = verdictMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // URL
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = result.url,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Flags
            if (result.flags.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚠️ Risk Factors Detected",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    result.flags.forEach { flag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DesktopColors.VerdictSuspicious.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, DesktopColors.VerdictSuspicious.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "•",
                                    fontSize = 16.sp,
                                    color = DesktopColors.VerdictSuspicious
                                )
                                Text(
                                    text = flag,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
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
    onScanClick: (AnalysisResult) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recent Scans",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
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
