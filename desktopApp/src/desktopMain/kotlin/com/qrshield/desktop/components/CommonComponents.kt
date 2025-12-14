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

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.desktop.theme.DesktopColors

/**
 * Reusable UI components for QR-SHIELD Desktop
 *
 * @author QR-SHIELD Team
 * @since 1.1.0
 */

// ============================================
// TOP APP BAR
// ============================================

/**
 * Enhanced top app bar with gradient background and theme toggle.
 */
@Composable
fun EnhancedTopAppBar(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        DesktopColors.BrandGradientStart,
                        DesktopColors.BrandGradientEnd
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource("assets/app-icon.png"),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Column {
                    Text(
                        text = "QR-SHIELD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "QRishing Detector",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // About Button
                Surface(
                    onClick = onAboutClick,
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "ℹ️", fontSize = 16.sp)
                    }
                }

                // Settings Button
                Surface(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "⚙️", fontSize = 16.sp)
                    }
                }

                // Theme Toggle Button
                Surface(
                    onClick = onThemeToggle,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isDarkMode) "☀️" else "🌙",
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isDarkMode) "Light" else "Dark",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// HERO SECTION
// ============================================

/**
 * Animated hero section matching Web app design.
 */
@Composable
fun AnimatedHeroSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Animated Logo with glow effect
        Box(
            modifier = Modifier
                .size((96 * scale).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DesktopColors.BrandPrimary.copy(alpha = 0.4f),
                            DesktopColors.BrandAccent.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Load PNG logo from resources
            Image(
                painter = painterResource("assets/app-icon.png"),
                contentDescription = "QR-SHIELD Logo",
                modifier = Modifier
                    .size((64 * scale).dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        // Gradient Title (matching Web "Scan URLs Safely")
        Text(
            text = "Scan URLs Safely",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = DesktopColors.BrandPrimary
        )

        // Subtitle
        Text(
            text = "AI-powered phishing detection running 100% offline",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Trust badges row (matching Web)
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustBadge("🔒", "Zero data collection")
            TrustBadge("🚀", "Same engine as Mobile")
            TrustBadge("🧠", "25+ AI heuristics")
        }
    }
}

@Composable
private fun TrustBadge(icon: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 12.sp)
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// ============================================
// STATS COMPONENTS
// ============================================

/**
 * Metrics Grid matching Web app design.
 */
@Composable
fun MetricsGrid() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            value = "25+",
            label = "Security Heuristics",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            value = "500+",
            label = "Brands Detected",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            value = "<50ms",
            label = "Analysis Time",
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            value = "100%",
            label = "Local Privacy",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = DesktopColors.BrandPrimary.copy(alpha = 0.08f),
        border = BorderStroke(3.dp, DesktopColors.BrandPrimary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = DesktopColors.BrandPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Row of stats cards showing scan metrics (legacy).
 */
@Composable
fun StatsRow(scanCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = "25+",
            label = "Heuristics",
            icon = "🔍",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "$scanCount",
            label = "Scans",
            icon = "📊",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "100%",
            label = "Offline",
            icon = "🔒",
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual stat card component.
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 20.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DesktopColors.BrandPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================
// FEATURE CARD
// ============================================

/**
 * Feature showcase card component.
 */
@Composable
fun FeatureCard(
    icon: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, fontSize = 20.sp)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================
// FOOTER
// ============================================

/**
 * Application footer with premium design.
 */
@Composable
fun EnhancedFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gradient divider
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DesktopColors.BrandPrimary.copy(alpha = 0.3f),
                            DesktopColors.BrandAccent.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        // KMP Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DesktopColors.BrandPrimary.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, DesktopColors.BrandPrimary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💜", fontSize = 14.sp)
                Text(
                    text = "Built with Kotlin Multiplatform",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = DesktopColors.BrandPrimary
                )
            }
        }

        // Version & Edition
        Text(
            text = "🛡️ QR-SHIELD v1.1.4 • Desktop Edition • KotlinConf 2026",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        // Links Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FooterLink("🔗 GitHub", "https://github.com/Raoof128/QDKMP-KotlinConf-2026-")
            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            FooterLink("🐛 Report Issue", "https://github.com/Raoof128/QDKMP-KotlinConf-2026-/issues")
            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            FooterLink("📜 License", "https://github.com/Raoof128/QDKMP-KotlinConf-2026-/blob/main/LICENSE")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun FooterLink(text: String, url: String) {
    Surface(
        onClick = {
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().browse(java.net.URI(url))
                }
            } catch (e: Exception) {
                // Ignore
            }
        },
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
    }
}
