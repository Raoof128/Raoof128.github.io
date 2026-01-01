/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

package com.raouf.mehrguard.platform

/**
 * Web (JS + Wasm) implementations of platform abstractions.
 *
 * Shared implementation for both Kotlin/JS and Kotlin/Wasm targets
 * using the webMain source set introduced in Kotlin 2.2.20.
 *
 * ## Expect/Actual "Escape Hatch" Pattern (KMP Best Practice)
 *
 * This file implements the platform-specific capabilities that require native APIs:
 *
 * | Abstraction          | Web Implementation                  |
 * |----------------------|-------------------------------------|
 * | PlatformTime         | Date.now(), performance.now()       |
 * | PlatformClipboard    | navigator.clipboard API             |
 * | PlatformSecureRandom | crypto.getRandomValues() (Web Crypto)|
 * | PlatformLogger       | console.log/warn/error              |
 * | PlatformUrlOpener    | window.open()                       |
 *
 * Note: Uses external declarations for JS interop to ensure compatibility
 * with both Kotlin/JS and Kotlin/Wasm targets.
 *
 * @author Mehr Guard Security Team
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

/**
 * Web Crypto API-backed secure random implementation.
 *
 * Uses crypto.getRandomValues() which is cryptographically secure
 * and available in all modern browsers and Web Workers.
 *
 * ## Security Note
 * This implementation uses the Web Crypto API which is:
 * - Cryptographically secure (CSPRNG)
 * - Available in all modern browsers
 * - Hardware-backed where available (via OS APIs)
 *
 * @see https://developer.mozilla.org/en-US/docs/Web/API/Crypto/getRandomValues
 */
actual object PlatformSecureRandom {
    /**
     * Generate cryptographically secure random bytes using Web Crypto API.
     *
     * @param size Number of bytes to generate (max 65536 per call per spec)
     * @return Secure random bytes
     */
    actual fun nextBytes(size: Int): ByteArray {
        require(size >= 0) { "Size must be non-negative" }
        if (size == 0) return ByteArray(0)
        
        // Web Crypto API has a max of 65536 bytes per call
        val result = ByteArray(size)
        var offset = 0
        
        while (offset < size) {
            val chunkSize = minOf(65536, size - offset)
            // Get random bytes via JS interop
            val chunk = getSecureRandomBytesImpl(chunkSize)
            chunk.copyInto(result, offset)
            offset += chunkSize
        }
        
        return result
    }

    /**
     * Generate a cryptographically secure UUID v4.
     */
    actual fun randomUUID(): String {
        val bytes = nextBytes(16)
        
        // Set version to 4 (random UUID)
        bytes[6] = ((bytes[6].toInt() and 0x0F) or 0x40).toByte()
        // Set variant to RFC 4122
        bytes[8] = ((bytes[8].toInt() and 0x3F) or 0x80).toByte()
        
        return buildString {
            bytes.forEachIndexed { index, byte ->
                append(byte.toUByte().toString(16).padStart(2, '0'))
                if (index in listOf(3, 5, 7, 9)) append("-")
            }
        }
    }
    
    /**
     * Internal implementation that gets bytes from JS and converts to ByteArray.
     * Uses string-based interop which is WASM-compatible.
     */
    private fun getSecureRandomBytesImpl(size: Int): ByteArray {
        // Get comma-separated signed byte values as string from JS
        val bytesString = getSecureRandomBytesAsString(size)
        if (bytesString.isEmpty()) return ByteArray(0)
        
        // Parse comma-separated signed byte values
        return bytesString.split(",").map { it.toInt().toByte() }.toByteArray()
    }
}

// External declaration for Web Crypto API - returns string for WASM compatibility
// Returns comma-separated signed byte values (e.g., "-128,0,127,42")
private external fun getSecureRandomBytesAsString(size: Int): String

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
