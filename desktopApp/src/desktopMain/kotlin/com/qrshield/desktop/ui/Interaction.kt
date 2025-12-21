package com.qrshield.desktop.ui

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun rememberHoverState(source: InteractionSource): Boolean {
    val isHovered by source.collectIsHoveredAsState()
    return isHovered
}

@Composable
fun rememberPressedState(source: InteractionSource): Boolean {
    val isPressed by source.collectIsPressedAsState()
    return isPressed
}
