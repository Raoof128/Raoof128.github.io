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

package com.raouf.mehrguard.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Android implementations of platform abstractions.
 *
 * These implementations use Android system APIs via Context.
 * Context must be set before use via [AndroidPlatformContext.init].
 *
 * ## KMP Sophistication
 * - Full Android API access
 * - Proper API level checks
 * - Context-aware implementations
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */

/**
 * Android context holder for platform abstractions.
 * Must be initialized in Application.onCreate() or Activity.onCreate().
 */
object AndroidPlatformContext {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun getContext(): Context {
        return appContext ?: throw IllegalStateException(
            "AndroidPlatformContext not initialized. Call init() in Application.onCreate()"
        )
    }
}

// ==================== Clipboard ====================

actual object PlatformClipboard {
    actual fun copyToClipboard(text: String): Boolean {
        return try {
            val context = AndroidPlatformContext.getContext()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("QR-SHIELD", text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            Log.e("PlatformClipboard", "Failed to copy to clipboard", e)
            false
        }
    }

    actual fun getClipboardText(): String? {
        return try {
            val context = AndroidPlatformContext.getContext()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val primaryClip = clipboard.primaryClip
            if (primaryClip != null && primaryClip.itemCount > 0) {
                primaryClip.getItemAt(0).text?.toString()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    actual fun hasText(): Boolean {
        return try {
            val context = AndroidPlatformContext.getContext()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType("text/*") == true
        } catch (e: Exception) {
            false
        }
    }
}

// ==================== Haptic Feedback ====================

actual object PlatformHaptics {
    @Suppress("DEPRECATION")
    private fun getVibrator(): Vibrator {
        val context = AndroidPlatformContext.getContext()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun vibrate(millis: Long) {
        try {
            val vibrator = getVibrator()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(millis)
            }
        } catch (e: Exception) {
            Log.e("PlatformHaptics", "Vibration failed", e)
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            val vibrator = getVibrator()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.e("PlatformHaptics", "Pattern vibration failed", e)
        }
    }

    actual fun light() {
        vibrate(10)
    }

    actual fun medium() {
        vibrate(25)
    }

    actual fun heavy() {
        vibrate(50)
    }

    actual fun success() {
        vibratePattern(longArrayOf(0, 10, 50, 10))
    }

    actual fun warning() {
        vibratePattern(longArrayOf(0, 25, 50, 25, 50, 25))
    }

    actual fun error() {
        vibratePattern(longArrayOf(0, 100, 50, 100))
    }
}

// ==================== Logging ====================

actual object PlatformLogger {
    private const val TAG = "QR-SHIELD"

    actual fun debug(tag: String, message: String) {
        Log.d("$TAG.$tag", message)
    }

    actual fun info(tag: String, message: String) {
        Log.i("$TAG.$tag", message)
    }

    actual fun warn(tag: String, message: String) {
        Log.w("$TAG.$tag", message)
    }

    actual fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e("$TAG.$tag", message, throwable)
        } else {
            Log.e("$TAG.$tag", message)
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
        return try {
            val context = AndroidPlatformContext.getContext()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                if (title.isNotEmpty()) {
                    putExtra(Intent.EXTRA_SUBJECT, title)
                }
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val shareIntent = Intent.createChooser(sendIntent, title.ifEmpty { "Share via" })
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
            true
        } catch (e: Exception) {
            Log.e("PlatformShare", "Share failed", e)
            false
        }
    }

    actual fun isShareSupported(): Boolean {
        return true // Android always supports sharing
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
            val context = AndroidPlatformContext.getContext()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("PlatformUrlOpener", "Failed to open URL", e)
            false
        }
    }

    actual fun canOpenUrl(url: String): Boolean {
        return try {
            val context = AndroidPlatformContext.getContext()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.resolveActivity(context.packageManager) != null
        } catch (e: Exception) {
            false
        }
    }
}
