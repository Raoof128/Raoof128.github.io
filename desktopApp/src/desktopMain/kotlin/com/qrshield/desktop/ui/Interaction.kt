package com.qrshield.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
