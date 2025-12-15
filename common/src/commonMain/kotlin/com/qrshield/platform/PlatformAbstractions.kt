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

/**
 * Platform Abstractions - Strategic expect/actual Declarations
 *
 * This file defines all platform-specific capabilities that require
 * native implementations. Each expect declaration documents WHY
 * it must be platform-specific.
 *
 * ## KMP Architecture Principle
 * - Business logic is 100% shared (PhishingEngine, HeuristicsEngine, etc.)
 * - Only platform-specific capabilities use expect/actual
 * - Each expect declaration includes justification
 *
 * ## expect/actual Boundaries
 *
 * | Capability       | Why Native Required                                           |
 * |------------------|--------------------------------------------------------------|
 * | QR Scanning      | Camera APIs are platform-specific (CameraX, AVFoundation)   |
 * | Database Driver  | SQLite drivers differ per platform (SQLDelight requirement) |
 * | Clipboard        | System clipboard APIs are platform-specific                  |
 * | Haptics          | Tactile feedback APIs differ (Android Vibrator, iOS haptics)|
 * | Time             | High-resolution time APIs vary by platform                   |
 * | Logging          | Logging backends differ (Logcat, OSLog, console)            |
 * | Share            | System share sheets are platform-specific                    |
 * | Crypto           | Secure random generation varies by platform                  |
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */

// ==================== Clipboard ====================

/**
 * Platform-specific clipboard operations.
 *
 * ## Why Native Required
 * - Android: `ClipboardManager` system service
 * - iOS: `UIPasteboard.general`
 * - Desktop: `java.awt.Toolkit.getSystemClipboard()`
 * - Web: `navigator.clipboard` API
 *
 * Each platform has different security models and async requirements.
 */
expect object PlatformClipboard {
    /**
     * Copy text to the system clipboard.
     * @param text The text to copy
     * @return true if successful
     */
    fun copyToClipboard(text: String): Boolean

    /**
     * Read text from system clipboard.
     * @return Clipboard content or null if empty/inaccessible
     */
    fun getClipboardText(): String?

    /**
     * Check if clipboard contains text.
     */
    fun hasText(): Boolean
}

// ==================== Haptic Feedback ====================

/**
 * Platform-specific haptic feedback.
 *
 * ## Why Native Required
 * - Android: `HapticFeedbackConstants`, `Vibrator` service
 * - iOS: `UIImpactFeedbackGenerator`, `UINotificationFeedbackGenerator`
 * - Desktop: No haptics (no-op implementation)
 * - Web: `navigator.vibrate()` (limited support)
 *
 * Each platform has different haptic types and APIs.
 */
expect object PlatformHaptics {
    /**
     * Trigger light haptic feedback.
     * Used for: button taps, selections
     */
    fun light()

    /**
     * Trigger medium haptic feedback.
     * Used for: confirmations, toggles
     */
    fun medium()

    /**
     * Trigger heavy haptic feedback.
     * Used for: important actions, warnings
     */
    fun heavy()

    /**
     * Trigger success haptic pattern.
     * Used for: scan complete (safe verdict)
     */
    fun success()

    /**
     * Trigger warning haptic pattern.
     * Used for: suspicious verdict
     */
    fun warning()

    /**
     * Trigger error haptic pattern.
     * Used for: malicious verdict, errors
     */
    fun error()
}

// ==================== Logging ====================

/**
 * Platform-specific logging.
 *
 * ## Why Native Required
 * - Android: `android.util.Log` (Logcat)
 * - iOS: `os.Logger` (OSLog, Console.app)
 * - Desktop: `java.util.logging` or SLF4J
 * - Web: `console.log/warn/error`
 *
 * Each platform has different logging levels, destinations, and formats.
 */
expect object PlatformLogger {
    /**
     * Log debug message.
     */
    fun debug(tag: String, message: String)

    /**
     * Log info message.
     */
    fun info(tag: String, message: String)

    /**
     * Log warning message.
     */
    fun warn(tag: String, message: String)

    /**
     * Log error message.
     */
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

// ==================== Time ====================

/**
 * Platform-specific time operations.
 *
 * ## Why Native Required
 * - Android: `System.currentTimeMillis()`, `System.nanoTime()`
 * - iOS: `CFAbsoluteTimeGetCurrent()`, `ProcessInfo.processInfo.systemUptime`
 * - Desktop: `System.currentTimeMillis()`, `System.nanoTime()`
 * - Web: `Date.now()`, `performance.now()`
 *
 * High-resolution timing and monotonic clocks vary by platform.
 * Note: kotlinx.datetime is preferred for general use; this is for performance timing.
 */
expect object PlatformTime {
    /**
     * Current Unix timestamp in milliseconds.
     */
    fun currentTimeMillis(): Long

    /**
     * High-resolution monotonic time in nanoseconds.
     * Used for performance measurements.
     */
    fun nanoTime(): Long

    /**
     * Format timestamp for display (locale-aware).
     */
    fun formatTimestamp(millis: Long): String

    /**
     * Format relative time (e.g., "5 minutes ago").
     */
    fun formatRelativeTime(millis: Long): String
}

// ==================== Share ====================

/**
 * Platform-specific share functionality.
 *
 * ## Why Native Required
 * - Android: `Intent.ACTION_SEND` with chooser
 * - iOS: `UIActivityViewController`
 * - Desktop: System-specific or clipboard fallback
 * - Web: `navigator.share()` or clipboard fallback
 *
 * Each platform has different share sheet implementations and capabilities.
 */
expect object PlatformShare {
    /**
     * Share text content.
     * @param text The text to share
     * @param title Optional title for share sheet
     * @return true if share was initiated
     */
    fun shareText(text: String, title: String = ""): Boolean

    /**
     * Check if native sharing is supported.
     */
    fun isShareSupported(): Boolean
}

// ==================== Secure Random ====================

/**
 * Platform-specific cryptographically secure random generation.
 *
 * ## Why Native Required
 * - Android: `java.security.SecureRandom`
 * - iOS: `Security.framework` / `SecRandomCopyBytes`
 * - Desktop: `java.security.SecureRandom`
 * - Web: `crypto.getRandomValues()`
 *
 * Each platform has different secure random sources.
 */
expect object PlatformSecureRandom {
    /**
     * Generate secure random bytes.
     * @param size Number of bytes to generate
     * @return Cryptographically secure random bytes
     */
    fun nextBytes(size: Int): ByteArray

    /**
     * Generate a secure random UUID string.
     */
    fun randomUUID(): String
}

// ==================== URL Opener ====================

/**
 * Platform-specific URL opening.
 *
 * ## Why Native Required
 * - Android: `Intent.ACTION_VIEW` with URL
 * - iOS: `UIApplication.shared.open(url)`
 * - Desktop: `Desktop.getDesktop().browse(uri)`
 * - Web: `window.open(url)` or location change
 *
 * Each platform handles external URLs differently.
 */
expect object PlatformUrlOpener {
    /**
     * Open URL in default browser.
     * @param url The URL to open
     * @return true if URL was opened
     */
    fun openUrl(url: String): Boolean

    /**
     * Check if URL can be opened.
     */
    fun canOpenUrl(url: String): Boolean
}
