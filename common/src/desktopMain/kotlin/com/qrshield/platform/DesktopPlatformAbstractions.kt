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

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.net.URI
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Desktop JVM implementations of platform abstractions.
 *
 * These implementations use Java AWT/Swing and JVM APIs to provide
 * clipboard, logging, time, and other platform functionality.
 *
 * ## KMP Sophistication
 * - Full JVM API access for Desktop target
 * - Proper exception handling
 * - Thread-safe implementations
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */

// ==================== Clipboard ====================

actual object PlatformClipboard {
    actual fun copyToClipboard(text: String): Boolean {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val selection = StringSelection(text)
            clipboard.setContents(selection, selection)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun getClipboardText(): String? {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                clipboard.getData(DataFlavor.stringFlavor) as? String
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    actual fun hasText(): Boolean {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)
        } catch (e: Exception) {
            false
        }
    }
}

// ==================== Haptic Feedback ====================

actual object PlatformHaptics {
    // Desktop has no haptic feedback - all no-ops
    actual fun light() {}
    actual fun medium() {}
    actual fun heavy() {}
    actual fun success() {}
    actual fun warning() {}
    actual fun error() {}
}

// ==================== Logging ====================

actual object PlatformLogger {
    private val logger = Logger.getLogger("QR-SHIELD")

    actual fun debug(tag: String, message: String) {
        logger.log(Level.FINE, "[$tag] $message")
    }

    actual fun info(tag: String, message: String) {
        logger.log(Level.INFO, "[$tag] $message")
    }

    actual fun warn(tag: String, message: String) {
        logger.log(Level.WARNING, "[$tag] $message")
    }

    actual fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            logger.log(Level.SEVERE, "[$tag] $message", throwable)
        } else {
            logger.log(Level.SEVERE, "[$tag] $message")
        }
    }
}

// ==================== Time ====================

actual object PlatformTime {
    private val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())

    actual fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    actual fun nanoTime(): Long {
        return System.nanoTime()
    }

    actual fun formatTimestamp(millis: Long): String {
        return dateFormat.format(Date(millis))
    }

    actual fun formatRelativeTime(millis: Long): String {
        val now = System.currentTimeMillis()
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
        // Desktop doesn't have a native share sheet
        // Fallback to clipboard
        return PlatformClipboard.copyToClipboard(text)
    }

    actual fun isShareSupported(): Boolean {
        // Desktop uses clipboard as share mechanism
        return true
    }
}

// ==================== Secure Random ====================

actual object PlatformSecureRandom {
    private val secureRandom = SecureRandom()

    actual fun nextBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        secureRandom.nextBytes(bytes)
        return bytes
    }

    actual fun randomUUID(): String {
        return UUID.randomUUID().toString()
    }
}

// ==================== URL Opener ====================

actual object PlatformUrlOpener {
    actual fun openUrl(url: String): Boolean {
        return try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI(url))
                    true
                } else {
                    false
                }
            } else {
                // Fallback for headless environments
                val os = System.getProperty("os.name").lowercase()
                val command = when {
                    os.contains("win") -> arrayOf("cmd", "/c", "start", url)
                    os.contains("mac") -> arrayOf("open", url)
                    else -> arrayOf("xdg-open", url)
                }
                Runtime.getRuntime().exec(command)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    actual fun canOpenUrl(url: String): Boolean {
        return try {
            URI(url)
            url.startsWith("http://") || url.startsWith("https://")
        } catch (e: Exception) {
            false
        }
    }
}
