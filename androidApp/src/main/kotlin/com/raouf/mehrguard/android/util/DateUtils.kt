package com.raouf.mehrguard.android.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for date and time formatting.
 */
object DateUtils {

    /**
     * Formats a timestamp (millis) into a relative time string (e.g., "Just now", "5m ago", "Yesterday")
     * or a standard date format.
     */
    fun formatRelativeTime(epochMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - epochMillis
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMillis))
            }
            diff < 172_800_000 -> "Yesterday"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMillis))
        }
    }
}
