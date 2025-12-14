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

package com.qrshield.desktop.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * QR-SHIELD Desktop Design System
 *
 * Centralized color definitions matching Android/iOS/Web exactly.
 * Based on the shared brand identity for cross-platform consistency.
 *
 * @author QR-SHIELD Team
 * @since 1.1.4
 */
object DesktopColors {

    // ========================================
    // BRAND COLORS (Matching Web/Android/iOS)
    // ========================================

    /** Electric Purple - Primary brand color (from Web: #6C5CE7) */
    val BrandPrimary = Color(0xFF6C5CE7)

    /** Neon Teal/Cyan - Secondary brand color (from Web: #00CEC9) */
    val BrandSecondary = Color(0xFF00D68F)

    /** Light Purple Accent (from Web: #A78BFA) */
    val BrandAccent = Color(0xFFA855F7)

    /** Gradient start color - Primary Purple */
    val BrandGradientStart = Color(0xFF6C5CE7)

    /** Gradient end color - Light Purple */
    val BrandGradientEnd = Color(0xFFA855F7)

    // ========================================
    // VERDICT COLORS (WCAG AA Compliant)
    // ========================================

    /** Safe verdict - Neon Green (#00D68F) */
    val VerdictSafe = Color(0xFF00D68F)

    /** Suspicious verdict - Amber (#F5A623) */
    val VerdictSuspicious = Color(0xFFF5A623)

    /** Malicious verdict - Threat Red (#FF3D71) */
    val VerdictMalicious = Color(0xFFFF3D71)

    /** Unknown verdict - Gray (#8B93A1) */
    val VerdictUnknown = Color(0xFF8B93A1)

    // ========================================
    // DARK THEME COLORS (GitHub Dark Style)
    // ========================================

    val DarkBackground = Color(0xFF0D1117)        // Deep Navy (GitHub Dark)
    val DarkSurface = Color(0xFF161B22)           // Elevated Surface
    val DarkSurfaceElevated = Color(0xFF21262D)   // Card Background
    val DarkSurfaceVariant = Color(0xFF1C2128)    // Surface Variant
    val DarkTextPrimary = Color(0xFFF0F6FC)       // Near White
    val DarkTextSecondary = Color(0xFF8B949E)     // Muted Gray
    val DarkBorder = Color(0xFF30363D)            // Border color

    // ========================================
    // LIGHT THEME COLORS
    // ========================================

    val LightBackground = Color(0xFFF6F8FA)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceElevated = Color(0xFFF0F2F5)
    val LightSurfaceVariant = Color(0xFFE7E0EC)
    val LightTextPrimary = Color(0xFF18181B)
    val LightTextSecondary = Color(0xFF3F3F46)
    val LightBorder = Color(0xFFE4E4E7)

    // ========================================
    // ACCENT COLORS
    // ========================================

    val AccentBlue = Color(0xFF58A6FF)            // Link Blue
    val AccentPurple = Color(0xFFD2A8FF)          // Highlight Purple
}

/**
 * Dark color scheme for QR-SHIELD Desktop
 */
val QRShieldDarkColors = darkColorScheme(
    primary = DesktopColors.BrandPrimary,
    secondary = DesktopColors.BrandSecondary,
    tertiary = DesktopColors.BrandAccent,
    background = DesktopColors.DarkBackground,
    surface = DesktopColors.DarkSurface,
    surfaceVariant = DesktopColors.DarkSurfaceVariant,
    onBackground = DesktopColors.DarkTextPrimary,
    onSurface = DesktopColors.DarkTextPrimary,
    onSurfaceVariant = DesktopColors.DarkTextSecondary,
    outline = DesktopColors.DarkBorder,
    error = DesktopColors.VerdictMalicious
)

/**
 * Light color scheme for QR-SHIELD Desktop
 */
val QRShieldLightColors = lightColorScheme(
    primary = DesktopColors.BrandPrimary,
    secondary = DesktopColors.BrandSecondary,
    tertiary = DesktopColors.BrandAccent,
    background = DesktopColors.LightBackground,
    surface = DesktopColors.LightSurface,
    surfaceVariant = DesktopColors.LightSurfaceVariant,
    onBackground = DesktopColors.LightTextPrimary,
    onSurface = DesktopColors.LightTextPrimary,
    onSurfaceVariant = DesktopColors.LightTextSecondary,
    outline = DesktopColors.LightBorder,
    error = DesktopColors.VerdictMalicious
)
