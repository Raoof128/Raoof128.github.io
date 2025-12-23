package com.qrshield.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Decorative Modifier Extensions for QR-SHIELD Desktop UI
 * 
 * These helpers provide consistent visual polish across all screens.
 * Use theme tokens from LocalStitchTokens.current for colors when calling.
 */

/**
 * Draws a background grid pattern.
 * 
 * @param spacing Distance between grid lines (default: 40.dp - standard for backgrounds)
 * @param lineColor Color of grid lines (recommend: colors.border or colors.border.copy(alpha = 0.3f))
 * @param lineWidth Width of grid lines (default: 1.dp)
 */
fun Modifier.gridPattern(
    spacing: Dp = 40.dp,
    lineColor: Color,
    lineWidth: Dp = 1.dp
): Modifier {
    return drawBehind {
        val spacingPx = spacing.toPx()
        val strokePx = lineWidth.toPx()
        val width = size.width
        val height = size.height
        var x = 0f
        while (x <= width) {
            drawLine(lineColor, Offset(x, 0f), Offset(x, height), strokeWidth = strokePx)
            x += spacingPx
        }
        var y = 0f
        while (y <= height) {
            drawLine(lineColor, Offset(0f, y), Offset(width, y), strokeWidth = strokePx)
            y += spacingPx
        }
    }
}

/**
 * Draws a dotted background pattern.
 * 
 * @param spacing Distance between dots (default: 24.dp - standard for content areas)
 * @param dotColor Color of dots (recommend: colors.textMuted.copy(alpha = 0.1f))
 * @param dotRadius Radius of dots (default: 1.dp)
 */
fun Modifier.dottedPattern(
    spacing: Dp = 24.dp,
    dotColor: Color,
    dotRadius: Dp = 1.dp
): Modifier {
    return drawBehind {
        val spacingPx = spacing.toPx()
        val radiusPx = dotRadius.toPx()
        val width = size.width
        val height = size.height
        var x = 0f
        while (x <= width) {
            var y = 0f
            while (y <= height) {
                drawCircle(dotColor, radiusPx, Offset(x, y))
                y += spacingPx
            }
            x += spacingPx
        }
    }
}

/**
 * Applies a standard surface border consistent with the design system.
 * 
 * @param color Border color (recommend: colors.border)
 * @param width Border width (default: 1.dp)
 * @param radius Corner radius (default: 12.dp - standard card radius)
 */
fun Modifier.surfaceBorder(
    color: Color,
    width: Dp = 1.dp,
    radius: Dp = 12.dp
): Modifier = this.border(width, color, RoundedCornerShape(radius))

// ============================================================================
// Card & Surface Composable Helpers
// ============================================================================

/**
 * Standard card surface styling: clip + background + border.
 * Use this for main content cards throughout the app.
 * 
 * @param backgroundColor Card background (recommend: colors.surface)
 * @param borderColor Border color (recommend: colors.border)
 * @param radius Corner radius (default: 12.dp - tokens.radius.md)
 * @param borderWidth Border thickness (default: 1.dp)
 */
fun Modifier.cardSurface(
    backgroundColor: Color,
    borderColor: Color,
    radius: Dp = 12.dp,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(RoundedCornerShape(radius))
    .background(backgroundColor, RoundedCornerShape(radius))
    .border(borderWidth, borderColor, RoundedCornerShape(radius))

/**
 * Panel/section surface styling with subtle background.
 * Use for nested sections within cards.
 * 
 * @param backgroundColor Panel background (recommend: colors.backgroundAlt)
 * @param borderColor Border color (recommend: colors.border)
 * @param radius Corner radius (default: 8.dp - tokens.radius.sm)
 */
fun Modifier.panelSurface(
    backgroundColor: Color,
    borderColor: Color,
    radius: Dp = 8.dp
): Modifier = this
    .clip(RoundedCornerShape(radius))
    .background(backgroundColor, RoundedCornerShape(radius))
    .border(1.dp, borderColor, RoundedCornerShape(radius))

/**
 * Status pill/badge styling.
 * Use for status indicators, tags, and small labels.
 * 
 * @param backgroundColor Pill background (recommend: colors.success.copy(alpha = 0.1f))
 * @param borderColor Pill border (recommend: colors.success.copy(alpha = 0.3f))
 */
fun Modifier.statusPill(
    backgroundColor: Color,
    borderColor: Color
): Modifier = this
    .clip(RoundedCornerShape(999.dp))
    .background(backgroundColor, RoundedCornerShape(999.dp))
    .border(1.dp, borderColor, RoundedCornerShape(999.dp))

/**
 * Icon container styling - rounded square for icon backgrounds.
 * Use for feature icons, action icons in cards.
 * 
 * @param backgroundColor Container background (recommend: colors.primary.copy(alpha = 0.1f))
 * @param radius Corner radius (default: 8.dp - tokens.radius.sm)
 */
fun Modifier.iconContainer(
    backgroundColor: Color,
    radius: Dp = 8.dp
): Modifier = this
    .clip(RoundedCornerShape(radius))
    .background(backgroundColor, RoundedCornerShape(radius))

/**
 * Button surface styling.
 * Use for primary/secondary action buttons.
 * 
 * @param backgroundColor Button background
 * @param radius Corner radius (default: 8.dp - tokens.radius.sm)
 */
fun Modifier.buttonSurface(
    backgroundColor: Color,
    radius: Dp = 8.dp
): Modifier = this
    .clip(RoundedCornerShape(radius))
    .background(backgroundColor, RoundedCornerShape(radius))
