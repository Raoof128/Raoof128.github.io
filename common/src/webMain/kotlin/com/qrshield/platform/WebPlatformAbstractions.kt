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

import kotlin.random.Random

/**
 * Web (JS + Wasm) implementations of platform abstractions.
 *
 * Shared implementation for both Kotlin/JS and Kotlin/Wasm targets
 * using the webMain source set introduced in Kotlin 2.2.20.
 *
 * Note: Uses pure Kotlin implementations to avoid js() calls that
 * are incompatible with Wasm. Browser APIs are accessed via external
 * declarations when needed.
 *
 * @author QR-SHIELD Security Team
 * @since 1.17.25
 */

// ==================== Clipboard ====================

actual object PlatformClipboard {
    actual fun copyToClipboard(text: String): Boolean {
        return try {
            copyTextToClipboard(text)
            true
        } catch (e: Throwable) {
            false
        }
    }

    actual fun getClipboardText(): String? = null

    actual fun hasText(): Boolean = false
}

// External JS function declarations
private external fun copyTextToClipboard(text: String)

// ==================== Haptic Feedback ====================

actual object PlatformHaptics {
    actual fun light() { /* No-op in web */ }
    actual fun medium() { /* No-op in web */ }
    actual fun heavy() { /* No-op in web */ }
    actual fun success() { /* No-op in web */ }
    actual fun warning() { /* No-op in web */ }
    actual fun error() { /* No-op in web */ }
}

// ==================== Logging ====================

actual object PlatformLogger {
    actual fun debug(tag: String, message: String) {
        println("[$tag] DEBUG: $message")
    }

    actual fun info(tag: String, message: String) {
        println("[$tag] INFO: $message")
    }

    actual fun warn(tag: String, message: String) {
        println("[$tag] WARN: $message")
    }

    actual fun error(tag: String, message: String, throwable: Throwable?) {
        println("[$tag] ERROR: $message")
        throwable?.let { println(it.stackTraceToString()) }
    }
}

// ==================== Time ====================

actual object PlatformTime {
    actual fun currentTimeMillis(): Long {
        return getCurrentTimeMillis()
    }

    actual fun nanoTime(): Long {
        return (getPerformanceNow() * 1_000_000).toLong()
    }

    actual fun formatTimestamp(millis: Long): String {
        return formatDate(millis)
    }

    actual fun formatRelativeTime(millis: Long): String {
        val now = currentTimeMillis()
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

// External JS function declarations for time
private external fun getCurrentTimeMillis(): Long
private external fun getPerformanceNow(): Double
private external fun formatDate(millis: Long): String

// ==================== Share ====================

actual object PlatformShare {
    actual fun shareText(text: String, title: String): Boolean {
        return PlatformClipboard.copyToClipboard(text)
    }

    actual fun isShareSupported(): Boolean = false
}

// ==================== Secure Random ====================

actual object PlatformSecureRandom {
    actual fun nextBytes(size: Int): ByteArray {
        val array = ByteArray(size)
        for (i in 0 until size) {
            array[i] = (Random.nextInt(256) - 128).toByte()
        }
        return array
    }

    actual fun randomUUID(): String {
        val bytes = nextBytes(16)
        return buildString {
            bytes.forEachIndexed { index, byte ->
                append(byte.toUByte().toString(16).padStart(2, '0'))
                if (index in listOf(3, 5, 7, 9)) append("-")
            }
        }
    }
}

// ==================== URL Opener ====================

actual object PlatformUrlOpener {
    actual fun openUrl(url: String): Boolean {
        return try {
            openUrlExternal(url)
            true
        } catch (e: Throwable) {
            false
        }
    }

    actual fun canOpenUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
}

// External JS function declarations
private external fun openUrlExternal(url: String)
