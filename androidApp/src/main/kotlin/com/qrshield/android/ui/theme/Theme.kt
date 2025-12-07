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
// =============================================================================

// Primary Brand Colors - Neon Purple Gradient
val BrandPrimary = Color(0xFF6C5CE7)      // Electric Purple
val BrandSecondary = Color(0xFF00D68F)    // Neon Teal/Cyan
val BrandAccent = Color(0xFFA855F7)       // Light Purple

// Verdict Colors - Security Status
val VerdictSafe = Color(0xFF00D68F)       // Neon Green - SAFE
val VerdictWarning = Color(0xFFF5A623)    // Amber - SUSPICIOUS  
val VerdictDanger = Color(0xFFFF3D71)     // Threat Red - MALICIOUS
val VerdictUnknown = Color(0xFF8B93A1)    // Gray - UNKNOWN

// Background Colors - Deep Dark Hacker Theme
val BackgroundDark = Color(0xFF0D1117)    // Deep Navy (GitHub Dark)
val BackgroundSurface = Color(0xFF161B22) // Elevated Surface
val BackgroundCard = Color(0xFF21262D)    // Card Background
val BackgroundGlass = Color(0x0DFFFFFF)   // Glass Tint (5% white)

// Text Colors
val TextPrimary = Color(0xFFF0F6FC)       // Near White
val TextSecondary = Color(0xFF8B949E)     // Muted Gray
val TextMuted = Color(0xFF6E7681)         // Very Muted

// Accent Colors
val AccentBlue = Color(0xFF58A6FF)        // Link Blue
val AccentPurple = Color(0xFFD2A8FF)      // Highlight Purple

// =============================================================================
// MATERIAL 3 COLOR SCHEMES
// =============================================================================

private val DarkColorScheme = darkColorScheme(
    // Primary Brand
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2D2060),
    onPrimaryContainer = Color(0xFFE8E0FF),
    
    // Secondary (Teal)
    secondary = BrandSecondary,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF004D45),
    onSecondaryContainer = Color(0xFF96F4E5),
    
    // Tertiary (Accent)
    tertiary = BrandAccent,
    onTertiary = Color(0xFF3B0082),
    tertiaryContainer = Color(0xFF4E0594),
    onTertiaryContainer = Color(0xFFF0DBFF),
    
    // Error/Danger
    error = VerdictDanger,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    // Backgrounds - Hacker Dark Theme
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = BackgroundSurface,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundCard,
    onSurfaceVariant = TextSecondary,
    
    // Outlines
    outline = Color(0xFF30363D),
    outlineVariant = Color(0xFF21262D),
    
    // Special
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundDark,
    inversePrimary = Color(0xFF4A3E9E),
    
    // Tonal
    surfaceTint = BrandPrimary,
    scrim = Color(0x80000000)
)

private val LightColorScheme = lightColorScheme(
    // Primary Brand
    primary = Color(0xFF5B4DCF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E0FF),
    onPrimaryContainer = Color(0xFF1B0063),
    
    // Secondary (Teal)
    secondary = Color(0xFF006B62),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF73F8E5),
    onSecondaryContainer = Color(0xFF00201D),
    
    // Tertiary
    tertiary = Color(0xFF7B4397),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF9D8FF),
    onTertiaryContainer = Color(0xFF2F004C),
    
    // Error
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    // Backgrounds
    background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    
    // Outlines
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

// =============================================================================
// TYPOGRAPHY - Cybersecurity / Terminal Style
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
// =============================================================================

@Composable
fun QRShieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled by default for consistent branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Only use dynamic color if explicitly enabled AND available
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
            // Use dark background for status bar
            window.statusBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false // Always dark icons for dark theme
                isAppearanceLightNavigationBars = !darkTheme
            }
            // Edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
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
 */
fun scoreToColor(score: Int): Color = when {
    score <= 30 -> VerdictSafe
    score <= 60 -> VerdictWarning
    else -> VerdictDanger
}
