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
 * Centralized color definitions and theme configuration.
 * 
 * @author QR-SHIELD Team
 * @since 1.1.0
 */
object DesktopColors {
    
    // ========================================
    // BRAND COLORS
    // ========================================
    
    /** Kotlin Purple - Primary brand color */
    val BrandPrimary = Color(0xFF7F52FF)
    
    /** Secondary brand color - lighter purple */
    val BrandSecondary = Color(0xFFA78BFA)
    
    /** Accent color - cyan highlight */
    val BrandAccent = Color(0xFF00D9FF)
    
    /** Gradient start color */
    val BrandGradientStart = Color(0xFF7F52FF)
    
    /** Gradient end color */
    val BrandGradientEnd = Color(0xFFE879F9)
    
    // ========================================
    // VERDICT COLORS
    // ========================================
    
    /** Safe verdict - green */
    val VerdictSafe = Color(0xFF22C55E)
    
    /** Suspicious verdict - amber */
    val VerdictSuspicious = Color(0xFFF59E0B)
    
    /** Malicious verdict - red */
    val VerdictMalicious = Color(0xFFEF4444)
    
    /** Unknown verdict - gray */
    val VerdictUnknown = Color(0xFF9CA3AF)
    
    // ========================================
    // DARK THEME COLORS
    // ========================================
    
    val DarkBackground = Color(0xFF0A0A0B)
    val DarkSurface = Color(0xFF141416)
    val DarkSurfaceElevated = Color(0xFF1C1C1F)
    val DarkSurfaceVariant = Color(0xFF262629)
    val DarkTextPrimary = Color(0xFFFAFAFA)
    val DarkTextSecondary = Color(0xFFA1A1AA)
    val DarkBorder = Color(0xFF27272A)
    
    // ========================================
    // LIGHT THEME COLORS
    // ========================================
    
    val LightBackground = Color(0xFFFAFAFB)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceElevated = Color(0xFFF4F4F5)
    val LightSurfaceVariant = Color(0xFFE4E4E7)
    val LightTextPrimary = Color(0xFF18181B)
    val LightTextSecondary = Color(0xFF52525B)
    val LightBorder = Color(0xFFE4E4E7)
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
