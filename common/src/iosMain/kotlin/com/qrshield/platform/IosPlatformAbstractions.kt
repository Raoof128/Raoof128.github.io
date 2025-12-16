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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.qrshield.platform

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSLog
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UIPasteboard
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

/**
 * iOS implementations of platform abstractions.
 *
 * These implementations use iOS Foundation and UIKit APIs
 * via Kotlin/Native interop.
 *
 * ## KMP Sophistication
 * - Direct iOS API access through Kotlin/Native
 * - UIKit integration for haptics and sharing
 * - Secure random from Security.framework
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */

// ==================== Clipboard ====================

actual object PlatformClipboard {
    actual fun copyToClipboard(text: String): Boolean {
        return try {
            UIPasteboard.generalPasteboard.string = text
            true
        } catch (e: Exception) {
            false
        }
    }

    actual fun getClipboardText(): String? {
        return try {
            UIPasteboard.generalPasteboard.string
        } catch (e: Exception) {
            null
        }
    }

    actual fun hasText(): Boolean {
        return UIPasteboard.generalPasteboard.hasStrings
    }
}

// ==================== Haptic Feedback ====================

actual object PlatformHaptics {
    private val lightGenerator by lazy {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    }
    private val mediumGenerator by lazy {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
    }
    private val heavyGenerator by lazy {
        UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
    }
    private val notificationGenerator by lazy {
        UINotificationFeedbackGenerator()
    }

    actual fun light() {
        lightGenerator.prepare()
        lightGenerator.impactOccurred()
    }

    actual fun medium() {
        mediumGenerator.prepare()
        mediumGenerator.impactOccurred()
    }

    actual fun heavy() {
        heavyGenerator.prepare()
        heavyGenerator.impactOccurred()
    }

    actual fun success() {
        notificationGenerator.prepare()
        notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }

    actual fun warning() {
        notificationGenerator.prepare()
        notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeWarning)
    }

    actual fun error() {
        notificationGenerator.prepare()
        notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeError)
    }
}

// ==================== Logging ====================

actual object PlatformLogger {
    actual fun debug(tag: String, message: String) {
        NSLog("[%@] DEBUG: %@", tag, message)
    }

    actual fun info(tag: String, message: String) {
        NSLog("[%@] INFO: %@", tag, message)
    }

    actual fun warn(tag: String, message: String) {
        NSLog("[%@] WARN: %@", tag, message)
    }

    actual fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("[%@] ERROR: %@ - %@", tag, message, throwable.message ?: "")
        } else {
            NSLog("[%@] ERROR: %@", tag, message)
        }
    }
}

// ==================== Time ====================

actual object PlatformTime {
    private val dateFormatter by lazy {
        NSDateFormatter().apply {
            dateStyle = NSDateFormatterMediumStyle
            timeStyle = NSDateFormatterMediumStyle
        }
    }

    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    actual fun nanoTime(): Long {
        // Use ProcessInfo for monotonic time approximation
        return (NSProcessInfo.processInfo.systemUptime * 1_000_000_000).toLong()
    }

    actual fun formatTimestamp(millis: Long): String {
        // NSDate uses timeIntervalSinceReferenceDate (seconds since Jan 1, 2001)
        // Convert from Unix epoch (1970) to Apple Reference Date (2001)
        val unixEpochToAppleRef = 978307200.0 // seconds between 1970 and 2001
        val secondsSince1970 = millis / 1000.0
        val date = NSDate(timeIntervalSinceReferenceDate = secondsSince1970 - unixEpochToAppleRef)
        return dateFormatter.stringFromDate(date) ?: ""
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

// ==================== Share ====================

actual object PlatformShare {
    actual fun shareText(text: String, title: String): Boolean {
        // Note: Full implementation requires UIActivityViewController from SwiftUI layer
        // This provides clipboard fallback for pure Kotlin contexts
        return PlatformClipboard.copyToClipboard(text)
    }

    actual fun isShareSupported(): Boolean {
        return true // iOS always supports sharing
    }
}

// ==================== Secure Random ====================

actual object PlatformSecureRandom {
    actual fun nextBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pinned.addressOf(0))
        }
        return bytes
    }

    actual fun randomUUID(): String {
        return NSUUID().UUIDString
    }
}

// ==================== URL Opener ====================

actual object PlatformUrlOpener {
    actual fun openUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        return if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
            true
        } else {
            false
        }
    }

    actual fun canOpenUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        return UIApplication.sharedApplication.canOpenURL(nsUrl)
    }
}
