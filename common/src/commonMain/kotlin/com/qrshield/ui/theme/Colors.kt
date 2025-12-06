package com.qrshield.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * QR-SHIELD Color System
 * 
 * Semantic color palette for consistent theming across platforms.
 */
object QRShieldColors {
    
    // Primary Brand Colors
    val primary = Color(0xFF6C5CE7)           // Deep Purple
    val primaryVariant = Color(0xFF5B4DCF)
    val primaryDark = Color(0xFF4A3FB0)
    val secondary = Color(0xFF00CEC9)         // Teal
    
    // Background Colors - Dark Theme
    val backgroundDark = Color(0xFF0D1117)
    val surfaceDark = Color(0xFF161B22)
    val surfaceVariantDark = Color(0xFF21262D)
    val cardDark = Color(0xFF1C2128)
    
    // Background Colors - Light Theme
    val backgroundLight = Color(0xFFF6F8FA)
    val surfaceLight = Color(0xFFFFFFFF)
    val surfaceVariantLight = Color(0xFFE8ECEF)
    val cardLight = Color(0xFFFFFFFF)
    
    // Semantic Colors - Verdicts
    val safe = Color(0xFF00D68F)               // Emerald Green
    val safeLight = Color(0xFF00F5A0)
    val safeDark = Color(0xFF00B377)
    val safeBackground = Color(0x1A00D68F)
    
    val warning = Color(0xFFFFAA00)            // Amber
    val warningLight = Color(0xFFFFBB33)
    val warningDark = Color(0xFFE69900)
    val warningBackground = Color(0x1AFFAA00)
    
    val danger = Color(0xFFFF3D71)             // Coral Red
    val dangerLight = Color(0xFFFF6B8A)
    val dangerDark = Color(0xFFE6365F)
    val dangerBackground = Color(0x1AFF3D71)
    
    val neutral = Color(0xFF8B949E)            // Gray
    val neutralBackground = Color(0x1A8B949E)
    
    // Text Colors - Dark Theme
    val textPrimaryDark = Color(0xFFF0F6FC)
    val textSecondaryDark = Color(0xFF8B949E)
    val textTertiaryDark = Color(0xFF6E7681)
    
    // Text Colors - Light Theme
    val textPrimaryLight = Color(0xFF24292F)
    val textSecondaryLight = Color(0xFF57606A)
    val textTertiaryLight = Color(0xFF8B949E)
    
    // System Colors
    val divider = Color(0xFF30363D)
    val shimmer = Color(0xFF21262D)
    val overlay = Color(0x80000000)
    
    /**
     * Get verdict color based on score
     */
    fun forScore(score: Int): Color = when {
        score <= 30 -> safe
        score <= 70 -> warning
        else -> danger
    }
    
    /**
     * Get verdict background color based on score
     */
    fun backgroundForScore(score: Int): Color = when {
        score <= 30 -> safeBackground
        score <= 70 -> warningBackground
        else -> dangerBackground
    }
}

/**
 * Color scheme holder for theme
 */
data class QRShieldColorScheme(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val safe: Color,
    val safeBackground: Color,
    val warning: Color,
    val warningBackground: Color,
    val danger: Color,
    val dangerBackground: Color,
    val divider: Color,
    val isDark: Boolean
) {
    companion object {
        val Dark = QRShieldColorScheme(
            primary = QRShieldColors.primary,
            primaryVariant = QRShieldColors.primaryVariant,
            secondary = QRShieldColors.secondary,
            background = QRShieldColors.backgroundDark,
            surface = QRShieldColors.surfaceDark,
            surfaceVariant = QRShieldColors.surfaceVariantDark,
            card = QRShieldColors.cardDark,
            textPrimary = QRShieldColors.textPrimaryDark,
            textSecondary = QRShieldColors.textSecondaryDark,
            textTertiary = QRShieldColors.textTertiaryDark,
            safe = QRShieldColors.safe,
            safeBackground = QRShieldColors.safeBackground,
            warning = QRShieldColors.warning,
            warningBackground = QRShieldColors.warningBackground,
            danger = QRShieldColors.danger,
            dangerBackground = QRShieldColors.dangerBackground,
            divider = QRShieldColors.divider,
            isDark = true
        )
        
        val Light = QRShieldColorScheme(
            primary = QRShieldColors.primary,
            primaryVariant = QRShieldColors.primaryVariant,
            secondary = QRShieldColors.secondary,
            background = QRShieldColors.backgroundLight,
            surface = QRShieldColors.surfaceLight,
            surfaceVariant = QRShieldColors.surfaceVariantLight,
            card = QRShieldColors.cardLight,
            textPrimary = QRShieldColors.textPrimaryLight,
            textSecondary = QRShieldColors.textSecondaryLight,
            textTertiary = QRShieldColors.textTertiaryLight,
            safe = QRShieldColors.safe,
            safeBackground = QRShieldColors.safeBackground,
            warning = QRShieldColors.warning,
            warningBackground = QRShieldColors.warningBackground,
            danger = QRShieldColors.danger,
            dangerBackground = QRShieldColors.dangerBackground,
            divider = QRShieldColors.divider,
            isDark = false
        )
    }
}
