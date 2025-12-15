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

package com.qrshield.platform

import kotlin.js.Date

/**
 * JavaScript/Web implementations of platform abstractions.
 *
 * These implementations use browser APIs to provide clipboard,
 * haptics, logging, time, share, and crypto functionality.
 *
 * ## KMP Sophistication
 * - Same shared logic runs in browser via Kotlin/JS
 * - Browser APIs accessed through Kotlin/JS interop
 * - Proper fallbacks for unsupported features
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */

// ==================== Clipboard ====================

actual object PlatformClipboard {
    actual fun copyToClipboard(text: String): Boolean {
        return try {
            js("navigator.clipboard && navigator.clipboard.writeText(text)")
            true
        } catch (e: Throwable) {
            // Fallback: create textarea and copy
            try {
                val textarea = js("document.createElement('textarea')")
                js("textarea.value = text")
                js("textarea.style.position = 'fixed'")
                js("textarea.style.left = '-9999px'")
                js("document.body.appendChild(textarea)")
                js("textarea.select()")
                js("document.execCommand('copy')")
                js("document.body.removeChild(textarea)")
                true
            } catch (e2: Throwable) {
                false
            }
        }
    }

    actual fun getClipboardText(): String? {
        // Clipboard read is async in browsers, return null for sync API
        // Real implementation would use suspend function
        return null
    }

    actual fun hasText(): Boolean {
        return false // Would require async check
    }
}

// ==================== Haptic Feedback ====================

actual object PlatformHaptics {
    private fun vibrate(pattern: dynamic) {
        try {
            js("navigator.vibrate && navigator.vibrate(pattern)")
        } catch (e: Throwable) {
            // Vibration not supported
        }
    }

    actual fun light() {
        vibrate(js("[10]"))
    }

    actual fun medium() {
        vibrate(js("[25]"))
    }

    actual fun heavy() {
        vibrate(js("[50]"))
    }

    actual fun success() {
        vibrate(js("[10, 50, 10]"))
    }

    actual fun warning() {
        vibrate(js("[25, 50, 25, 50, 25]"))
    }

    actual fun error() {
        vibrate(js("[100, 50, 100]"))
    }
}

// ==================== Logging ====================

actual object PlatformLogger {
    actual fun debug(tag: String, message: String) {
        console.log("[$tag] DEBUG: $message")
    }

    actual fun info(tag: String, message: String) {
        console.info("[$tag] INFO: $message")
    }

    actual fun warn(tag: String, message: String) {
        console.warn("[$tag] WARN: $message")
    }

    actual fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            console.error("[$tag] ERROR: $message", throwable)
        } else {
            console.error("[$tag] ERROR: $message")
        }
    }
}

// ==================== Time ====================

actual object PlatformTime {
    actual fun currentTimeMillis(): Long {
        return Date.now().toLong()
    }

    actual fun nanoTime(): Long {
        // performance.now() returns milliseconds with sub-millisecond precision
        val perfNow: Double = js("performance.now()") as Double
        return (perfNow * 1_000_000).toLong()
    }

    actual fun formatTimestamp(millis: Long): String {
        val date = Date(millis.toDouble())
        return date.toLocaleString()
    }

    actual fun formatRelativeTime(millis: Long): String {
        val now = Date.now().toLong()
        val diff = now - millis

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> formatTimestamp(millis)
        }
    }
}

// ==================== Share ====================

actual object PlatformShare {
    actual fun shareText(text: String, title: String): Boolean {
        return try {
            // Try Web Share API
            val shareData = js("{}")
            js("shareData.text = text")
            if (title.isNotEmpty()) {
                js("shareData.title = title")
            }

            val canShare: Boolean = js("navigator.share && navigator.canShare && navigator.canShare(shareData)") as Boolean
            if (canShare) {
                js("navigator.share(shareData)")
                true
            } else {
                // Fallback to clipboard
                PlatformClipboard.copyToClipboard(text)
            }
        } catch (e: Throwable) {
            PlatformClipboard.copyToClipboard(text)
        }
    }

    actual fun isShareSupported(): Boolean {
        return try {
            js("!!navigator.share") as Boolean
        } catch (e: Throwable) {
            false
        }
    }
}

// ==================== Secure Random ====================

actual object PlatformSecureRandom {
    actual fun nextBytes(size: Int): ByteArray {
        val array = ByteArray(size)
        try {
            val jsArray = js("new Uint8Array(size)")
            js("crypto.getRandomValues(jsArray)")
            for (i in 0 until size) {
                val value: Int = js("jsArray[i]") as Int
                array[i] = value.toByte()
            }
        } catch (e: Throwable) {
            // Fallback to pseudo-random (less secure)
            for (i in 0 until size) {
                array[i] = (kotlin.random.Random.nextInt(256) - 128).toByte()
            }
        }
        return array
    }

    actual fun randomUUID(): String {
        return try {
            js("crypto.randomUUID()") as String
        } catch (e: Throwable) {
            // Fallback UUID generation
            val bytes = nextBytes(16)
            buildString {
                bytes.forEachIndexed { index, byte ->
                    append(byte.toUByte().toString(16).padStart(2, '0'))
                    if (index in listOf(3, 5, 7, 9)) append("-")
                }
            }
        }
    }
}

// ==================== URL Opener ====================

actual object PlatformUrlOpener {
    actual fun openUrl(url: String): Boolean {
        return try {
            js("window.open(url, '_blank')")
            true
        } catch (e: Throwable) {
            false
        }
    }

    actual fun canOpenUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
}
