package com.qrshield.desktop.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

fun Modifier.gridPattern(spacing: Dp, lineColor: Color, lineWidth: Dp): Modifier {
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

fun Modifier.dottedPattern(spacing: Dp, dotColor: Color, dotRadius: Dp): Modifier {
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
