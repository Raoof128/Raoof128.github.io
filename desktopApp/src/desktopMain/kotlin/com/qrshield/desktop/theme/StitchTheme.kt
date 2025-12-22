package com.qrshield.desktop.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Inter = FontFamily.SansSerif
private val JetBrainsMono = FontFamily.Monospace

/**
 * HTML-Derived Dark Mode Color Palette
 * Extracted from the dark mode HTML/CSS reference files for visual consistency.
 * These values are the single source of truth for dark mode colors.
 */
object DarkModePalette {
    // Background colors (from HTML: background-dark, surface-dark)
    val background = Color(0xFF0F1115)      // Main app background (#0f1115)
    val backgroundAlt = Color(0xFF111621)   // Alternative background (#111621)
    val surface = Color(0xFF161B22)         // Cards, panels (#161b22)
    val surfaceAlt = Color(0xFF1A1F2B)      // Elevated surfaces (#1a1f2b)
    val surfaceCard = Color(0xFF1E2430)     // Card backgrounds (#1e2430)
    val surfaceHover = Color(0xFF21262D)    // Hover states (#21262d)
    
    // Sidebar colors
    val sidebarBg = Color(0xFF111318)       // Sidebar background (#111318)
    val sidebarBorder = Color(0xFF242832)   // Sidebar border (#242832)
    
    // Border colors
    val border = Color(0xFF292E38)          // Standard borders (#292e38)
    val borderStrong = Color(0xFF3C4453)    // Strong/active borders (#3c4453)
    val borderSubtle = Color(0xFF2E3545)    // Subtle borders (#2e3545)
    
    // Text colors
    val textMain = Color(0xFFFFFFFF)        // Primary text (white)
    val textSub = Color(0xFF94A3B8)         // Secondary text (slate-400)
    val textMuted = Color(0xFF64748B)       // Muted text (slate-500)
    val textDim = Color(0xFF6B7280)         // Dimmed text (gray-500)
    
    // Brand/Accent colors
    val primary = Color(0xFF195DE6)         // Brand blue (#195de6)
    val primaryHover = Color(0xFF2563EB)    // Hover state (#2563eb)
    val primaryLight = Color(0xFF3B82F6)    // Light variant (#3b82f6)
    
    // Status colors (consistent across light/dark)
    val success = Color(0xFF10B981)         // Safe/success (emerald-500)
    val warning = Color(0xFFF59E0B)         // Suspicious/warning (amber-500)
    val danger = Color(0xFFEF4444)          // Dangerous/error (red-500)
}

/**
 * Light Mode Color Palette (unchanged from original design)
 */
object LightModePalette {
    val background = Color(0xFFF6F6F8)
    val backgroundAlt = Color(0xFFF3F4F6)
    val surface = Color(0xFFFFFFFF)
    val surfaceAlt = Color(0xFFF9FAFB)
    
    val border = Color(0xFFE5E7EB)
    val borderStrong = Color(0xFFD1D5DB)
    
    val textMain = Color(0xFF111827)
    val textSub = Color(0xFF6B7280)
    val textMuted = Color(0xFF9CA3AF)
    
    val primary = Color(0xFF195DE6)
    val primaryHover = Color(0xFF1550C5)
    
    val success = Color(0xFF10B981)
    val warning = Color(0xFFF59E0B)
    val danger = Color(0xFFEF4444)
}

val LocalStitchTokens = staticCompositionLocalOf { StitchTokens.dashboard(isDark = false) }

@Composable
fun StitchTheme(tokens: DesignTokens, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalStitchTokens provides tokens) {
        MaterialTheme(
            colorScheme = tokens.colors.toMaterialScheme(),
            typography = tokens.typography.toMaterialTypography(),
            content = content
        )
    }
}

data class DesignTokens(
    val colors: ColorTokens,
    val typography: TypographyTokens,
    val spacing: SpacingTokens,
    val radius: RadiusTokens,
    val elevation: ElevationTokens
)

data class ColorTokens(
    val primary: Color,
    val primaryHover: Color,
    val background: Color,
    val backgroundAlt: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val border: Color,
    val borderStrong: Color,
    val textMain: Color,
    val textSub: Color,
    val textMuted: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val teal: Color,
    val amber: Color,
    val red: Color
)

data class TypographyTokens(
    val display: FontFamily,
    val mono: FontFamily,
    val xs: TextStyle,
    val sm: TextStyle,
    val base: TextStyle,
    val lg: TextStyle,
    val xl: TextStyle,
    val xxl: TextStyle,
    val xxxl: TextStyle,
    val giant: TextStyle
)

data class SpacingTokens(
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val xxl: Dp,
    val xxxl: Dp
)

data class RadiusTokens(
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,
    val pill: Dp
)

data class ElevationTokens(
    val soft: Dp,
    val card: Dp,
    val glow: Dp
)

private fun ColorTokens.toMaterialScheme(): ColorScheme {
    val isDark = background.luminance() < 0.5f
    return if (isDark) {
        darkColorScheme(
            primary = primary,
            secondary = teal,
            background = background,
            surface = surface,
            surfaceVariant = backgroundAlt,
            onSurface = textMain,
            onSurfaceVariant = textSub,
            outline = border,
            error = danger
        )
    } else {
        lightColorScheme(
            primary = primary,
            secondary = teal,
            background = background,
            surface = surface,
            surfaceVariant = backgroundAlt,
            onSurface = textMain,
            onSurfaceVariant = textSub,
            outline = border,
            error = danger
        )
    }
}

private fun TypographyTokens.toMaterialTypography(): androidx.compose.material3.Typography {
    return androidx.compose.material3.Typography(
        bodySmall = sm,
        bodyMedium = base,
        bodyLarge = lg,
        titleSmall = sm.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = xl.copy(fontWeight = FontWeight.Bold),
        headlineSmall = xl.copy(fontWeight = FontWeight.Bold),
        headlineMedium = xxl.copy(fontWeight = FontWeight.Bold),
        headlineLarge = xxxl.copy(fontWeight = FontWeight.Bold),
        displayMedium = giant.copy(fontWeight = FontWeight.Black)
    )
}

object StitchTokens {
    private val spacing = SpacingTokens(
        xs = 4.dp,
        sm = 8.dp,
        md = 12.dp,
        lg = 16.dp,
        xl = 24.dp,
        xxl = 32.dp,
        xxxl = 40.dp
    )

    private val radius = RadiusTokens(
        sm = 8.dp,
        md = 12.dp,
        lg = 16.dp,
        xl = 24.dp,
        pill = 999.dp
    )

    private val elevation = ElevationTokens(
        soft = 2.dp,
        card = 4.dp,
        glow = 8.dp
    )

    private fun typography(): TypographyTokens {
        return TypographyTokens(
            display = Inter,
            mono = JetBrainsMono,
            xs = TextStyle(fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.Medium),
            sm = TextStyle(fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Normal),
            base = TextStyle(fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.Normal),
            lg = TextStyle(fontFamily = Inter, fontSize = 16.sp, fontWeight = FontWeight.Medium),
            xl = TextStyle(fontFamily = Inter, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
            xxl = TextStyle(fontFamily = Inter, fontSize = 24.sp, fontWeight = FontWeight.Bold),
            xxxl = TextStyle(fontFamily = Inter, fontSize = 32.sp, fontWeight = FontWeight.Bold),
            giant = TextStyle(fontFamily = Inter, fontSize = 40.sp, fontWeight = FontWeight.Black)
        )
    }

    /**
     * Creates a standard dark mode ColorTokens using the HTML-derived palette.
     * This ensures visual consistency with the dark mode HTML reference.
     */
    private fun darkColorTokens(
        primary: Color = DarkModePalette.primary,
        primaryHover: Color = DarkModePalette.primaryHover
    ) = ColorTokens(
        primary = primary,
        primaryHover = primaryHover,
        background = DarkModePalette.background,
        backgroundAlt = DarkModePalette.backgroundAlt,
        surface = DarkModePalette.surface,
        surfaceAlt = DarkModePalette.surfaceAlt,
        border = DarkModePalette.border,
        borderStrong = DarkModePalette.borderStrong,
        textMain = DarkModePalette.textMain,
        textSub = DarkModePalette.textSub,
        textMuted = DarkModePalette.textMuted,
        success = DarkModePalette.success,
        warning = DarkModePalette.warning,
        danger = DarkModePalette.danger,
        teal = DarkModePalette.success,
        amber = DarkModePalette.warning,
        red = DarkModePalette.danger
    )

    /**
     * Creates a standard light mode ColorTokens using the Light palette.
     */
    private fun lightColorTokens(
        primary: Color = LightModePalette.primary,
        primaryHover: Color = LightModePalette.primaryHover
    ) = ColorTokens(
        primary = primary,
        primaryHover = primaryHover,
        background = LightModePalette.background,
        backgroundAlt = LightModePalette.backgroundAlt,
        surface = LightModePalette.surface,
        surfaceAlt = LightModePalette.surfaceAlt,
        border = LightModePalette.border,
        borderStrong = LightModePalette.borderStrong,
        textMain = LightModePalette.textMain,
        textSub = LightModePalette.textSub,
        textMuted = LightModePalette.textMuted,
        success = LightModePalette.success,
        warning = LightModePalette.warning,
        danger = LightModePalette.danger,
        teal = LightModePalette.success,
        amber = LightModePalette.warning,
        red = LightModePalette.danger
    )

    fun dashboard(isDark: Boolean): DesignTokens {
        val colors = if (isDark) darkColorTokens() else lightColorTokens()
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanMonitor(isDark: Boolean = false): DesignTokens {
        val colors = if (isDark) {
            darkColorTokens(primary = DarkModePalette.primaryLight)
        } else {
            lightColorTokens()
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanResultSafe(isDark: Boolean = false): DesignTokens {
        // Safe result uses success (green) as primary color
        val colors = if (isDark) {
            darkColorTokens(
                primary = DarkModePalette.success,
                primaryHover = Color(0xFF059669)
            )
        } else {
            lightColorTokens(
                primary = LightModePalette.success,
                primaryHover = Color(0xFF059669)
            )
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanResultSuspicious(isDark: Boolean): DesignTokens {
        // Suspicious result uses warning (amber) as primary color
        val colors = if (isDark) {
            darkColorTokens(
                primary = DarkModePalette.warning,
                primaryHover = Color(0xFFD97706)
            )
        } else {
            lightColorTokens(
                primary = LightModePalette.warning,
                primaryHover = Color(0xFFD97706)
            )
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanResultDangerous(isDark: Boolean): DesignTokens {
        // Dangerous result uses danger (red) as primary color
        val colors = if (isDark) {
            darkColorTokens(
                primary = DarkModePalette.danger,
                primaryHover = Color(0xFFB91C1C)
            )
        } else {
            lightColorTokens(
                primary = LightModePalette.danger,
                primaryHover = Color(0xFFB91C1C)
            )
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun trustCentre(isDark: Boolean = false): DesignTokens {
        val colors = if (isDark) {
            darkColorTokens(primary = DarkModePalette.primaryLight)
        } else {
            lightColorTokens()
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun trustCentreAlt(isDark: Boolean): DesignTokens {
        val colors = if (isDark) darkColorTokens() else lightColorTokens()
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanHistory(isDark: Boolean = false): DesignTokens {
        val colors = if (isDark) {
            darkColorTokens(primary = DarkModePalette.primaryLight)
        } else {
            lightColorTokens()
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun training(isDark: Boolean = false): DesignTokens {
        val colors = if (isDark) {
            darkColorTokens(primary = DarkModePalette.primaryLight)
        } else {
            lightColorTokens()
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun reports(isDark: Boolean = false): DesignTokens {
        val colors = if (isDark) {
            darkColorTokens(primary = DarkModePalette.primaryLight)
        } else {
            lightColorTokens()
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }
}
