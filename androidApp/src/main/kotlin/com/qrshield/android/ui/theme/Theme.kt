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

package com.qrshield.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// =============================================================================
// QR-SHIELD CYBERSECURITY COLOR PALETTE
// Updated to match HTML TailwindCSS dark mode patterns exactly
// =============================================================================

// Primary Brand Colors - Matching iOS #2563EB (brandPrimary)
val BrandPrimary = Color(0xFF2563EB)       // Primary Blue (iOS brandPrimary)
val BrandPrimaryDark = Color(0xFF1D4ED8)   // Darker variant
val BrandSecondary = Color(0xFF10B981)     // Emerald Green (iOS brandSecondary)
val BrandAccent = Color(0xFF8B5CF6)        // Violet (iOS brandAccent)

// Verdict Colors - Matching iOS system colors exactly
val VerdictSafe = Color(0xFF34C759)        // iOS Green - SAFE
val VerdictWarning = Color(0xFFFF9500)     // iOS Orange - SUSPICIOUS
val VerdictDanger = Color(0xFFFF3B30)      // iOS Red - MALICIOUS
val VerdictUnknown = Color(0xFF8E8E93)     // iOS Gray - UNKNOWN

// Background Colors - From HTML TailwindCSS config
val BackgroundLight = Color(0xFFF6F6F8)    // background-light: #f6f6f8
val BackgroundDark = Color(0xFF101622)     // background-dark: #101622

// Surface Colors - From HTML
val SurfaceLight = Color(0xFFFFFFFF)       // surface-light: white
val SurfaceDark = Color(0xFF1A2230)        // surface-dark: #1a2230 (also #1e293b in some pages)
val SurfaceDarkAlt = Color(0xFF1E293B)     // Alternative dark surface (slate-800)

// Text Colors - From HTML
val TextPrimaryLight = Color(0xFF0F172A)   // slate-900
val TextPrimaryDark = Color(0xFFFFFFFF)    // white
val TextSecondaryLight = Color(0xFF64748B) // slate-500
val TextSecondaryDark = Color(0xFF94A3B8)  // slate-400

// Border Colors - From HTML
val BorderLight = Color(0xFFE2E8F0)        // slate-200 / gray-200
val BorderDark = Color(0xFF334155)         // slate-700

// =============================================================================
// BACKWARDS COMPATIBILITY ALIASES
// These maintain compatibility with existing screen code
// =============================================================================

// Legacy color aliases (for backwards compatibility with existing screens)
val TextPrimary = Color(0xFFF0F6FC)         // Near White (dark mode text)
val TextSecondary = Color(0xFF8B949E)       // Muted Gray
val TextMuted = Color(0xFF848D97)           // Slightly Lighter Muted
val BackgroundSurface = Color(0xFF161B22)   // Elevated Surface
val BackgroundCard = Color(0xFF21262D)      // Card Background
val BackgroundGlass = Color(0x1AFFFFFF)     // Glass Tint (10% white)
val AccentBlue = Color(0xFF58A6FF)          // Link Blue
val AccentPurple = Color(0xFFD2A8FF)        // Highlight Purple

// =============================================================================
// MATERIAL 3 COLOR SCHEMES - Matching HTML dark mode
// =============================================================================

private val DarkColorScheme = darkColorScheme(
    // Primary Brand - #215eed
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A3D80),
    onPrimaryContainer = Color(0xFFDBEAFE),

    // Secondary (Emerald/Teal)
    secondary = BrandSecondary,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF004D45),
    onSecondaryContainer = Color(0xFFD1FAE5),

    // Tertiary (Purple)
    tertiary = BrandAccent,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFF3E8FF),

    // Error/Danger (risk-high: #ef4444)
    error = VerdictDanger,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),

    // Backgrounds - HTML dark mode: bg-background-dark (#101622)
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    
    // Surfaces - HTML dark mode: bg-surface-dark / bg-slate-800
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDarkAlt,
    onSurfaceVariant = TextSecondaryDark,

    // Container colors for cards
    surfaceContainer = Color(0xFF1A2230),
    surfaceContainerHigh = Color(0xFF1E293B),
    surfaceContainerHighest = Color(0xFF21262D),
    surfaceContainerLow = Color(0xFF151B26),
    surfaceContainerLowest = Color(0xFF101622),

    // Outlines - HTML border-gray-700/800
    outline = Color(0xFF475569),          // slate-600
    outlineVariant = Color(0xFF334155),   // slate-700

    // Inverse
    inverseSurface = TextPrimaryDark,
    inverseOnSurface = BackgroundDark,
    inversePrimary = Color(0xFF4A7EEF),

    // Tonal
    surfaceTint = BrandPrimary,
    scrim = Color(0xCC000000)
)

private val LightColorScheme = lightColorScheme(
    // Primary Brand - #215eed
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A5F),

    // Secondary (Emerald)
    secondary = Color(0xFF059669),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF064E3B),

    // Tertiary (Purple)
    tertiary = Color(0xFF9333EA),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E8FF),
    onTertiaryContainer = Color(0xFF4C1D95),

    // Error
    error = VerdictDanger,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),

    // Backgrounds - HTML light mode: bg-background-light (#f6f6f8)
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    
    // Surfaces - HTML light mode: bg-white
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),   // slate-100
    onSurfaceVariant = TextSecondaryLight,

    // Container colors
    surfaceContainer = Color(0xFFF8FAFC),
    surfaceContainerHigh = Color(0xFFF1F5F9),
    surfaceContainerHighest = Color(0xFFE2E8F0),
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainerLowest = Color.White,

    // Outlines - HTML border-gray-100/200
    outline = Color(0xFF94A3B8),          // slate-400
    outlineVariant = Color(0xFFE2E8F0)    // slate-200
)

// =============================================================================
// TYPOGRAPHY - Cybersecurity / Terminal Style
// Android 16: Optimized for variable fonts and accessibility
// =============================================================================

val Typography = Typography(
    // Display Large - Hero Headlines
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),

    // Display Medium - Section Headers
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),

    // Headline Large - Screen Titles
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // Headline Medium - Card Titles
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    // Title Large - Component Headers
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // Title Medium - List Item Headers
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    // Body Large - Main Content
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    // Body Medium - Secondary Content
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),

    // Body Small - Captions
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label Large - Buttons
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Label Medium - Tags
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    // Label Small - Chips
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// =============================================================================
// MONOSPACE TYPOGRAPHY (For URLs, Code)
// =============================================================================

val MonospaceTypography = Typography.copy(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )
)

// =============================================================================
// THEME COMPOSABLE
// Android 16: Enhanced with improved dynamic colors and edge-to-edge
// =============================================================================

@Composable
fun QRShieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ (default enabled on Android 16)
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Use dynamic color on Android 12+ for system theme consistency
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Android 16: Enhanced edge-to-edge with transparent status bar
            // Note: statusBarColor and navigationBarColor are deprecated in API 35+
            // but still needed for backward compatibility on older versions
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                // Android 16+ uses transparent system bars
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
            } else {
                // Fallback for older versions
                window.statusBarColor = BackgroundDark.toArgb()
                window.navigationBarColor = BackgroundDark.toArgb()
            }

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }

            // Ensure edge-to-edge is enabled
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = QRShieldTypography.typography,
        content = content
    )
}

// =============================================================================
// UTILITY EXTENSIONS
// =============================================================================

/**
 * Get color for a specific verdict.
 */
fun verdictColor(verdict: String): Color = when (verdict.uppercase()) {
    "SAFE" -> VerdictSafe
    "SUSPICIOUS" -> VerdictWarning
    "MALICIOUS" -> VerdictDanger
    else -> VerdictUnknown
}

/**
 * Convert score (0-100) to a gradient color.
 * Uses smooth interpolation for better visual feedback.
 */
fun scoreToColor(score: Int): Color = when {
    score <= 30 -> VerdictSafe
    score <= 60 -> VerdictWarning
    else -> VerdictDanger
}

/**
 * Android 16: Check if device supports predictive back animations.
 */
fun isPredictiveBackSupported(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}
