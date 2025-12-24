/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.qrshield.benchmark

/**
 * Multiplatform-compatible string formatting utilities.
 * 
 * Kotlin/JS doesn't support String.format(), so we provide
 * a simple implementation that works across all targets.
 */
object FormatUtils {
    
    /**
     * Format a double with the specified number of decimal places.
     */
    fun formatDouble(value: Double, decimals: Int): String {
        if (decimals <= 0) return value.toLong().toString()
        
        var factor = 1.0
        repeat(decimals) { factor *= 10 }
        val rounded = kotlin.math.round(value * factor) / factor
        
        val str = rounded.toString()
        val parts = str.split(".")
        
        return if (parts.size == 1) {
            // No decimal point - add zeros
            str + "." + "0".repeat(decimals)
        } else {
            // Pad or trim decimals
            val wholePart = parts[0]
            val decimalPart = parts[1].take(decimals).padEnd(decimals, '0')
            "$wholePart.$decimalPart"
        }
    }
    
    /**
     * Extension function for formatting like "%.2f".format(value)
     */
    fun String.formatValue(value: Double): String {
        // Parse format string like "%.1f" or "%.2f"
        val match = Regex("""^%\.(\d+)f$""").find(this)
        val decimals = match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 2
        return formatDouble(value, decimals)
    }
}
