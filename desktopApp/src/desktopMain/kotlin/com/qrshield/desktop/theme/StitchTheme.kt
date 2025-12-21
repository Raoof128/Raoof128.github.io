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

    fun dashboard(isDark: Boolean): DesignTokens {
        val colors = if (isDark) {
            ColorTokens(
                primary = Color(0xFF2563EB),
                primaryHover = Color(0xFF1D4ED8),
                background = Color(0xFF0F172A),
                backgroundAlt = Color(0xFF1E293B),
                surface = Color(0xFF1E293B),
                surfaceAlt = Color(0xFF111827),
                border = Color(0xFF334155),
                borderStrong = Color(0xFF334155),
                textMain = Color(0xFFF1F5F9),
                textSub = Color(0xFF94A3B8),
                textMuted = Color(0xFF64748B),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFEF4444),
                teal = Color(0xFF10B981),
                amber = Color(0xFFF59E0B),
                red = Color(0xFFEF4444)
            )
        } else {
            ColorTokens(
                primary = Color(0xFF2563EB),
                primaryHover = Color(0xFF1D4ED8),
                background = Color(0xFFF3F4F6),
                backgroundAlt = Color(0xFFF8FAFC),
                surface = Color(0xFFFFFFFF),
                surfaceAlt = Color(0xFFF9FAFB),
                border = Color(0xFFE5E7EB),
                borderStrong = Color(0xFFD1D5DB),
                textMain = Color(0xFF111827),
                textSub = Color(0xFF6B7280),
                textMuted = Color(0xFF94A3B8),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFEF4444),
                teal = Color(0xFF10B981),
                amber = Color(0xFFF59E0B),
                red = Color(0xFFEF4444)
            )
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanMonitor(): DesignTokens {
        val colors = ColorTokens(
            primary = Color(0xFF2563EB),
            primaryHover = Color(0xFF1D4ED8),
            background = Color(0xFFF9FAFB),
            backgroundAlt = Color(0xFFF3F4F6),
            surface = Color(0xFFFFFFFF),
            surfaceAlt = Color(0xFFFFFFFF),
            border = Color(0xFFE5E7EB),
            borderStrong = Color(0xFFD1D5DB),
            textMain = Color(0xFF111827),
            textSub = Color(0xFF6B7280),
            textMuted = Color(0xFF9CA3AF),
            success = Color(0xFF10B981),
            warning = Color(0xFFF59E0B),
            danger = Color(0xFFEF4444),
            teal = Color(0xFF10B981),
            amber = Color(0xFFF59E0B),
            red = Color(0xFFEF4444)
        )
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanResultSafe(): DesignTokens {
        val colors = ColorTokens(
            primary = Color(0xFF10B981),
            primaryHover = Color(0xFF059669),
            background = Color(0xFFF9FAFB),
            backgroundAlt = Color(0xFFF3F4F6),
            surface = Color(0xFFFFFFFF),
            surfaceAlt = Color(0xFFFFFFFF),
            border = Color(0xFFE5E7EB),
            borderStrong = Color(0xFFD1D5DB),
            textMain = Color(0xFF111827),
            textSub = Color(0xFF6B7280),
            textMuted = Color(0xFF9CA3AF),
            success = Color(0xFF10B981),
            warning = Color(0xFFF59E0B),
            danger = Color(0xFFEF4444),
            teal = Color(0xFF10B981),
            amber = Color(0xFFF59E0B),
            red = Color(0xFFEF4444)
        )
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanResultSuspicious(isDark: Boolean): DesignTokens {
        val colors = if (isDark) {
            ColorTokens(
                primary = Color(0xFFF59E0B),
                primaryHover = Color(0xFFD97706),
                background = Color(0xFF111827),
                backgroundAlt = Color(0xFF0F172A),
                surface = Color(0xFF1F2937),
                surfaceAlt = Color(0xFF000000),
                border = Color(0xFF374151),
                borderStrong = Color(0xFF374151),
                textMain = Color(0xFFF9FAFB),
                textSub = Color(0xFF9CA3AF),
                textMuted = Color(0xFF6B7280),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFEF4444),
                teal = Color(0xFF10B981),
                amber = Color(0xFFF59E0B),
                red = Color(0xFFEF4444)
            )
        } else {
            ColorTokens(
                primary = Color(0xFFF59E0B),
                primaryHover = Color(0xFFD97706),
                background = Color(0xFFF3F4F6),
                backgroundAlt = Color(0xFFF9FAFB),
                surface = Color(0xFFFFFFFF),
                surfaceAlt = Color(0xFFFFFFFF),
                border = Color(0xFFE5E7EB),
                borderStrong = Color(0xFFD1D5DB),
                textMain = Color(0xFF111827),
                textSub = Color(0xFF6B7280),
                textMuted = Color(0xFF9CA3AF),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFEF4444),
                teal = Color(0xFF10B981),
                amber = Color(0xFFF59E0B),
                red = Color(0xFFEF4444)
            )
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanResultDangerous(isDark: Boolean): DesignTokens {
        val colors = if (isDark) {
            ColorTokens(
                primary = Color(0xFFDC2626),
                primaryHover = Color(0xFFB91C1C),
                background = Color(0xFF0F1115),
                backgroundAlt = Color(0xFF181B21),
                surface = Color(0xFF181B21),
                surfaceAlt = Color(0xFF1F2937),
                border = Color(0xFF2D3139),
                borderStrong = Color(0xFF374151),
                textMain = Color(0xFFF9FAFB),
                textSub = Color(0xFF9CA3AF),
                textMuted = Color(0xFF6B7280),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFDC2626),
                teal = Color(0xFF10B981),
                amber = Color(0xFFF59E0B),
                red = Color(0xFFDC2626)
            )
        } else {
            ColorTokens(
                primary = Color(0xFFDC2626),
                primaryHover = Color(0xFFB91C1C),
                background = Color(0xFFF3F4F6),
                backgroundAlt = Color(0xFFF9FAFB),
                surface = Color(0xFFFFFFFF),
                surfaceAlt = Color(0xFFFFFFFF),
                border = Color(0xFFE5E7EB),
                borderStrong = Color(0xFFD1D5DB),
                textMain = Color(0xFF111827),
                textSub = Color(0xFF6B7280),
                textMuted = Color(0xFF9CA3AF),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFDC2626),
                teal = Color(0xFF10B981),
                amber = Color(0xFFF59E0B),
                red = Color(0xFFDC2626)
            )
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun trustCentre(): DesignTokens {
        val colors = ColorTokens(
            primary = Color(0xFF135BEC),
            primaryHover = Color(0xFF0F4BC4),
            background = Color(0xFFF6F8FA),
            backgroundAlt = Color(0xFFF3F4F6),
            surface = Color(0xFFFFFFFF),
            surfaceAlt = Color(0xFFFFFFFF),
            border = Color(0xFFD0D7DE),
            borderStrong = Color(0xFFAFB8C1),
            textMain = Color(0xFF24292F),
            textSub = Color(0xFF57606A),
            textMuted = Color(0xFF6B7280),
            success = Color(0xFF2EA043),
            warning = Color(0xFFD29922),
            danger = Color(0xFFCF222E),
            teal = Color(0xFF2EA043),
            amber = Color(0xFFD29922),
            red = Color(0xFFCF222E)
        )
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun trustCentreAlt(isDark: Boolean): DesignTokens {
        val colors = if (isDark) {
            ColorTokens(
                primary = Color(0xFF2563EB),
                primaryHover = Color(0xFF1D4ED8),
                background = Color(0xFF0F172A),
                backgroundAlt = Color(0xFF1E293B),
                surface = Color(0xFF1E293B),
                surfaceAlt = Color(0xFF1E293B),
                border = Color(0xFF334155),
                borderStrong = Color(0xFF475569),
                textMain = Color(0xFFF1F5F9),
                textSub = Color(0xFF94A3B8),
                textMuted = Color(0xFF64748B),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFEF4444),
                teal = Color(0xFF10B981),
                amber = Color(0xFFF59E0B),
                red = Color(0xFFEF4444)
            )
        } else {
            ColorTokens(
                primary = Color(0xFF2563EB),
                primaryHover = Color(0xFF1D4ED8),
                background = Color(0xFFF8FAFC),
                backgroundAlt = Color(0xFFF1F5F9),
                surface = Color(0xFFFFFFFF),
                surfaceAlt = Color(0xFFFFFFFF),
                border = Color(0xFFE2E8F0),
                borderStrong = Color(0xFFCBD5E1),
                textMain = Color(0xFF0F172A),
                textSub = Color(0xFF64748B),
                textMuted = Color(0xFF94A3B8),
                success = Color(0xFF10B981),
                warning = Color(0xFFF59E0B),
                danger = Color(0xFFEF4444),
                teal = Color(0xFF10B981),
                amber = Color(0xFFF59E0B),
                red = Color(0xFFEF4444)
            )
        }
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun scanHistory(): DesignTokens {
        val colors = ColorTokens(
            primary = Color(0xFF135BEC),
            primaryHover = Color(0xFF0F4BC4),
            background = Color(0xFFF8FAFC),
            backgroundAlt = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            surfaceAlt = Color(0xFFFFFFFF),
            border = Color(0xFFE2E8F0),
            borderStrong = Color(0xFFCBD5E1),
            textMain = Color(0xFF0F172A),
            textSub = Color(0xFF64748B),
            textMuted = Color(0xFF94A3B8),
            success = Color(0xFF0D9488),
            warning = Color(0xFFD97706),
            danger = Color(0xFFE11D48),
            teal = Color(0xFF0D9488),
            amber = Color(0xFFD97706),
            red = Color(0xFFE11D48)
        )
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun training(): DesignTokens {
        val colors = ColorTokens(
            primary = Color(0xFF135BEC),
            primaryHover = Color(0xFF0F4BC4),
            background = Color(0xFFF8FAFC),
            backgroundAlt = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            surfaceAlt = Color(0xFFFFFFFF),
            border = Color(0xFFE2E8F0),
            borderStrong = Color(0xFFCBD5E1),
            textMain = Color(0xFF0F172A),
            textSub = Color(0xFF64748B),
            textMuted = Color(0xFF94A3B8),
            success = Color(0xFF10B981),
            warning = Color(0xFFF59E0B),
            danger = Color(0xFFEF4444),
            teal = Color(0xFF10B981),
            amber = Color(0xFFF59E0B),
            red = Color(0xFFEF4444)
        )
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }

    fun reports(): DesignTokens {
        val colors = ColorTokens(
            primary = Color(0xFF135BEC),
            primaryHover = Color(0xFF2563EB),
            background = Color(0xFFF8FAFC),
            backgroundAlt = Color(0xFFF1F5F9),
            surface = Color(0xFFFFFFFF),
            surfaceAlt = Color(0xFFFFFFFF),
            border = Color(0xFFE2E8F0),
            borderStrong = Color(0xFFCBD5E1),
            textMain = Color(0xFF0F172A),
            textSub = Color(0xFF64748B),
            textMuted = Color(0xFF94A3B8),
            success = Color(0xFF10B981),
            warning = Color(0xFFF59E0B),
            danger = Color(0xFFEF4444),
            teal = Color(0xFF10B981),
            amber = Color(0xFFF59E0B),
            red = Color(0xFFEF4444)
        )
        return DesignTokens(colors, typography(), spacing, radius, elevation)
    }
}
