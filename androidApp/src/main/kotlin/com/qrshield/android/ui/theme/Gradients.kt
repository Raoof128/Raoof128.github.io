package com.qrshield.android.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.qrshield.model.Verdict

/**
 * QR-SHIELD Gradient Definitions
 * 
 * Provides pre-defined gradients for backgrounds and effects.
 * Matches iOS Liquid Glass aesthetic.
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
object QRShieldGradients {
    
    // =====================================
    // BACKGROUND GRADIENTS
    // =====================================
    
    /** Main app background gradient */
    val Background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D1117),
            Color(0xFF161B22),
            Color(0xFF0D1117)
        )
    )
    
    /** Card background with subtle glow */
    val CardBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF21262D),
            Color(0xFF161B22)
        )
    )
    
    // =====================================
    // VERDICT GRADIENTS
    // =====================================
    
    /** Safe verdict gradient */
    val Safe = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF00D68F).copy(alpha = 0.2f),
            Color(0xFF00D68F).copy(alpha = 0.05f)
        )
    )
    
    /** Warning verdict gradient */
    val Warning = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF5A623).copy(alpha = 0.2f),
            Color(0xFFF5A623).copy(alpha = 0.05f)
        )
    )
    
    /** Danger verdict gradient */
    val Danger = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF3D71).copy(alpha = 0.2f),
            Color(0xFFFF3D71).copy(alpha = 0.05f)
        )
    )
    
    /** Unknown verdict gradient */
    val Unknown = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF8B93A1).copy(alpha = 0.2f),
            Color(0xFF8B93A1).copy(alpha = 0.05f)
        )
    )
    
    // =====================================
    // BRAND GRADIENTS
    // =====================================
    
    /** Primary brand gradient */
    val Brand = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF6C5CE7),
            Color(0xFFA855F7)
        )
    )
    
    /** Scanner laser gradient */
    val ScannerLaser = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color(0xFF00D68F).copy(alpha = 0.3f),
            Color(0xFF00D68F).copy(alpha = 0.8f),
            Color(0xFF00D68F).copy(alpha = 0.3f),
            Color.Transparent
        )
    )
    
    // =====================================
    // HELPER FUNCTIONS
    // =====================================
    
    /**
     * Get the appropriate gradient for a verdict.
     */
    fun forVerdict(verdict: Verdict): Brush = when (verdict) {
        Verdict.SAFE -> Safe
        Verdict.SUSPICIOUS -> Warning
        Verdict.MALICIOUS -> Danger
        Verdict.UNKNOWN -> Unknown
    }
    
    /**
     * Get the appropriate gradient for a risk score.
     */
    fun forScore(score: Int): Brush = when {
        score <= 30 -> Safe
        score <= 60 -> Warning
        else -> Danger
    }
}
