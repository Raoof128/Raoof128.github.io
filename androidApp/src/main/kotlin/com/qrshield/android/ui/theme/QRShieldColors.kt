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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * QR-SHIELD Design System Colors
 * Based on HTML TailwindCSS dark mode patterns:
 * - Primary: #215eed
 * - Background Light: #f6f6f8
 * - Background Dark: #101622
 * - Surface Dark: #1a2230
 */
object QRShieldColors {
    // Primary Brand - #2563EB (matching iOS brandPrimary exactly)
    val Primary = Color(0xFF2563EB)
    val PrimaryDark = Color(0xFF1D4ED8)
    val PrimaryLight = Color(0xFF60A5FA)
    
    // Background Colors (from HTML TailwindCSS)
    val BackgroundLight = Color(0xFFF6F6F8)   // background-light
    val BackgroundDark = Color(0xFF101622)    // background-dark
    
    // Surface Colors (from HTML)
    val SurfaceLight = Color(0xFFFFFFFF)      // white
    val SurfaceDark = Color(0xFF1A2230)       // surface-dark
    val SurfaceDarkAlt = Color(0xFF1E293B)    // slate-800 (some pages use this)
    
    // Text Colors (from HTML)
    val TextPrimaryLight = Color(0xFF0F172A)  // slate-900
    val TextPrimaryDark = Color(0xFFFFFFFF)   // white
    val TextSecondaryLight = Color(0xFF64748B) // slate-500
    val TextSecondaryDark = Color(0xFF94A3B8)  // slate-400
    
    // Slate Scale (matching TailwindCSS)
    val Slate50 = Color(0xFFF8FAFC)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate300 = Color(0xFFCBD5E1)
    val Slate400 = Color(0xFF94A3B8)
    val Slate500 = Color(0xFF64748B)
    val Slate600 = Color(0xFF475569)
    val Slate700 = Color(0xFF334155)
    val Slate800 = Color(0xFF1E293B)
    val Slate900 = Color(0xFF0F172A)
    
    // Risk/Verdict Colors - Matching iOS system colors exactly
    val RiskSafe = Color(0xFF34C759)       // iOS Green - Safe
    val RiskSafeLight = Color(0xFFDCFCE7)  // green-100
    val RiskSafeDark = Color(0xFF14532D)   // green-900
    
    val RiskWarning = Color(0xFFFF9500)    // iOS Orange - Suspicious
    val RiskWarningLight = Color(0xFFFEF3C7) // amber-100
    val RiskWarningDark = Color(0xFF92400E)  // amber-900
    
    val RiskDanger = Color(0xFFFF3B30)     // iOS Red - Malicious
    val RiskDangerLight = Color(0xFFFEE2E2) // red-100
    val RiskDangerDark = Color(0xFF7F1D1D)  // red-900
    
    // Emerald (Success) - from HTML
    val Emerald50 = Color(0xFFECFDF5)
    val Emerald100 = Color(0xFFD1FAE5)
    val Emerald400 = Color(0xFF34D399)
    val Emerald500 = Color(0xFF10B981)
    val Emerald600 = Color(0xFF059669)
    val Emerald900 = Color(0xFF064E3B)
    
    // Orange (Warning) - from HTML
    val Orange50 = Color(0xFFFFF7ED)
    val Orange100 = Color(0xFFFFEDD5)
    val Orange400 = Color(0xFFFB923C)
    val Orange500 = Color(0xFFF97316)
    val Orange600 = Color(0xFFEA580C)
    
    // Red (Error/Danger) - from HTML
    val Red50 = Color(0xFFFEF2F2)
    val Red100 = Color(0xFFFEE2E2)
    val Red400 = Color(0xFFF87171)
    val Red500 = Color(0xFFEF4444)
    val Red600 = Color(0xFFDC2626)
    val Red900 = Color(0xFF7F1D1D)
    
    // Blue (Info/Primary) - from HTML
    val Blue50 = Color(0xFFEFF6FF)
    val Blue100 = Color(0xFFDBEAFE)
    val Blue400 = Color(0xFF60A5FA)
    val Blue500 = Color(0xFF3B82F6)
    val Blue600 = Color(0xFF2563EB)
    
    // Purple - from HTML
    val Purple50 = Color(0xFFFAF5FF)
    val Purple100 = Color(0xFFF3E8FF)
    val Purple400 = Color(0xFFC084FC)
    val Purple500 = Color(0xFFA855F7)
    val Purple600 = Color(0xFF9333EA)
    
    // Yellow - from HTML
    val Yellow400 = Color(0xFFFACC15)
    val Yellow500 = Color(0xFFEAB308)
    
    // Gray Scale (matching TailwindCSS)
    val Gray50 = Color(0xFFF9FAFB)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray200 = Color(0xFFE5E7EB)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    val Gray600 = Color(0xFF4B5563)
    val Gray700 = Color(0xFF374151)
    val Gray800 = Color(0xFF1F2937)
    val Gray900 = Color(0xFF111827)
}

/**
 * Theme-aware color accessors for easy dark mode support.
 * Usage: QRShieldThemeColors.background (auto-selects based on theme)
 */
object QRShieldThemeColors {
    val background: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) QRShieldColors.BackgroundDark else QRShieldColors.BackgroundLight
    
    val surface: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) QRShieldColors.SurfaceDark else QRShieldColors.SurfaceLight
    
    val surfaceVariant: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) QRShieldColors.SurfaceDarkAlt else QRShieldColors.Slate100
    
    val textPrimary: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) QRShieldColors.TextPrimaryDark else QRShieldColors.TextPrimaryLight
    
    val textSecondary: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) QRShieldColors.TextSecondaryDark else QRShieldColors.TextSecondaryLight
    
    val border: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) QRShieldColors.Slate700 else QRShieldColors.Slate200
    
    val borderVariant: Color
        @Composable @ReadOnlyComposable
        get() = if (isSystemInDarkTheme()) QRShieldColors.Slate800 else QRShieldColors.Slate100
}

/**
 * Spacing values matching Tailwind's spacing scale
 */
object QRShieldSpacing {
    // Base unit: 4dp (1 = 4dp in Tailwind)
    const val UNIT = 4
    
    val dp0 = 0
    val dp1 = 4     // p-1
    val dp2 = 8     // p-2
    val dp3 = 12    // p-3
    val dp4 = 16    // p-4
    val dp5 = 20    // p-5
    val dp6 = 24    // p-6
    val dp8 = 32    // p-8
    val dp10 = 40   // p-10
    val dp12 = 48   // p-12
    val dp16 = 64   // p-16
    val dp20 = 80   // p-20
    val dp24 = 96   // p-24
}

/**
 * Border radius values matching Tailwind's rounded scale
 */
object QRShieldRadius {
    val None = 0f
    val Sm = 2f         // rounded-sm
    val Default = 4f    // rounded
    val Md = 6f         // rounded-md
    val Lg = 8f         // rounded-lg
    val Xl = 12f        // rounded-xl
    val Xxl = 16f       // rounded-2xl (DEFAULT in our HTML)
    val Xxxl = 24f      // rounded-3xl
    val Full = 9999f    // rounded-full
}

