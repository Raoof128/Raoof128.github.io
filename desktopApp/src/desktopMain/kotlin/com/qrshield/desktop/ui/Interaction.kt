package com.qrshield.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Desktop Interaction Helpers for QR-SHIELD
 * 
 * These helpers provide consistent hover, press, and focus states
 * for interactive elements in the desktop application.
 */

// ============================================================================
// State Helpers - for use in composables
// ============================================================================

/**
 * Remembers the hover state from an interaction source.
 * Returns true when the user is hovering over the element.
 */
@Composable
fun rememberHoverState(source: InteractionSource): Boolean {
    val isHovered by source.collectIsHoveredAsState()
    return isHovered
}

/**
 * Remembers the pressed state from an interaction source.
 * Returns true when the user is pressing the element.
 */
@Composable
fun rememberPressedState(source: InteractionSource): Boolean {
    val isPressed by source.collectIsPressedAsState()
    return isPressed
}

/**
 * Returns appropriate background and border colors based on hover/press state.
 * Useful for custom interactive components.
 * 
 * @param source The interaction source to monitor
 * @param defaultBackground Background when idle
 * @param hoverBackground Background when hovered
 * @param pressedBackground Background when pressed
 * @param defaultBorder Border color when idle
 * @param hoverBorder Border color when hovered
 */
@Composable
fun rememberInteractionColors(
    source: InteractionSource,
    defaultBackground: Color,
    hoverBackground: Color,
    pressedBackground: Color = hoverBackground,
    defaultBorder: Color = Color.Transparent,
    hoverBorder: Color = defaultBorder
): Pair<Color, Color> {
    val isHovered by source.collectIsHoveredAsState()
    val isPressed by source.collectIsPressedAsState()
    
    val backgroundColor = when {
        isPressed -> pressedBackground
        isHovered -> hoverBackground
        else -> defaultBackground
    }
    
    val borderColor = when {
        isHovered || isPressed -> hoverBorder
        else -> defaultBorder
    }
    
    return backgroundColor to borderColor
}

// ============================================================================
// Modifier Extensions
// ============================================================================

/**
 * Applies hover indication styling to an element.
 * Use this for interactive elements that should highlight on hover.
 * 
 * @param interactionSource The interaction source (create with remember { MutableInteractionSource() })
 * @param hoverBackground Background color when hovered (recommend: colors.backgroundAlt)
 * @param hoverBorder Border color when hovered (recommend: colors.border)
 * @param radius Corner radius (default: 8.dp - tokens.radius.sm)
 */
@Composable
fun Modifier.hoverHighlight(
    interactionSource: MutableInteractionSource,
    hoverBackground: Color,
    hoverBorder: Color,
    radius: Dp = 8.dp
): Modifier {
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    return this
        .hoverable(interactionSource)
        .clip(RoundedCornerShape(radius))
        .then(
            if (isHovered) {
                Modifier
                    .background(hoverBackground, RoundedCornerShape(radius))
                    .border(1.dp, hoverBorder, RoundedCornerShape(radius))
            } else {
                Modifier
            }
        )
}

// ============================================================================
// Cursor Helpers - Desktop UX
// ============================================================================

/**
 * Applies a hand/pointer cursor to clickable elements.
 * Use this on all clickable non-text elements for proper desktop UX.
 */
fun Modifier.handCursor(): Modifier = this.pointerHoverIcon(PointerIcon.Hand)

/**
 * Applies a text/I-beam cursor for text fields.
 * Use this on text input areas.
 */
fun Modifier.textCursor(): Modifier = this.pointerHoverIcon(PointerIcon.Text)

/**
 * Convenience extension that combines clickable + focusable + hand cursor.
 * Use instead of .clickable { } for consistent desktop behavior.
 * 
 * @param enabled Whether the click is enabled
 * @param onClick The click handler
 */
fun Modifier.clickableWithCursor(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = this
    .clickable(enabled = enabled, onClick = onClick)
    .focusable()
    .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)

// ============================================================================
// Focus Ring Helpers - Accessibility
// ============================================================================

/**
 * Default focus ring color - a visible blue outline for accessibility.
 */
val DefaultFocusRingColor = Color(0xFF3B82F6)  // Blue-500

/**
 * Adds a visible focus ring when the element receives keyboard focus.
 * Essential for accessibility - allows keyboard users to see which element is focused.
 * 
 * @param interactionSource The interaction source to monitor for focus state
 * @param ringColor The color of the focus ring (default: blue)
 * @param ringWidth Width of the focus ring stroke
 * @param cornerRadius Corner radius to match the element's shape
 * @param ringOffset Offset from the element border (creates a gap)
 */
@Composable
fun Modifier.focusRing(
    interactionSource: MutableInteractionSource,
    ringColor: Color = DefaultFocusRingColor,
    ringWidth: Dp = 2.dp,
    cornerRadius: Dp = 8.dp,
    ringOffset: Dp = 2.dp
): Modifier {
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    return this.drawWithContent {
        drawContent()
        if (isFocused) {
            val offsetPx = ringOffset.toPx()
            val widthPx = ringWidth.toPx()
            val radiusPx = cornerRadius.toPx()
            
            drawRoundRect(
                color = ringColor,
                topLeft = Offset(-offsetPx, -offsetPx),
                size = Size(size.width + offsetPx * 2, size.height + offsetPx * 2),
                cornerRadius = CornerRadius(radiusPx + offsetPx),
                style = Stroke(width = widthPx)
            )
        }
    }
}

/**
 * Composable modifier that creates an interaction source and applies focus ring.
 * Use this for elements that need visible keyboard focus indication.
 * 
 * @param ringColor The color of the focus ring
 * @param ringWidth Width of the focus ring stroke
 * @param cornerRadius Corner radius to match the element's shape
 */
fun Modifier.focusableWithRing(
    ringColor: Color = DefaultFocusRingColor,
    ringWidth: Dp = 2.dp,
    cornerRadius: Dp = 8.dp
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this
        .focusable(interactionSource = interactionSource)
        .focusRing(
            interactionSource = interactionSource,
            ringColor = ringColor,
            ringWidth = ringWidth,
            cornerRadius = cornerRadius
        )
}

/**
 * Full interactive modifier combining clickable + hand cursor + focus ring.
 * Use for important interactive elements that need complete accessibility support.
 * 
 * @param enabled Whether the click is enabled
 * @param ringColor The color of the focus ring
 * @param cornerRadius Corner radius to match the element's shape
 * @param onClick The click handler
 */
fun Modifier.fullInteractive(
    enabled: Boolean = true,
    ringColor: Color = DefaultFocusRingColor,
    cornerRadius: Dp = 8.dp,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this
        .clickable(enabled = enabled, interactionSource = interactionSource, indication = null, onClick = onClick)
        .focusable(interactionSource = interactionSource)
        .focusRing(interactionSource = interactionSource, ringColor = ringColor, cornerRadius = cornerRadius)
        .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
}
