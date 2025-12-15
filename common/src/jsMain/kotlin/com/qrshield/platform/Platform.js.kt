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

import kotlinx.browser.window

/**
 * Web (JavaScript) implementation of Platform.
 */
actual class Platform actual constructor() {
    actual val name: String = "Web"
    actual val version: String = detectBrowserVersion()
    actual val hasCamera: Boolean = js("navigator.mediaDevices !== undefined") as Boolean
    actual val hasHaptics: Boolean = js("navigator.vibrate !== undefined") as Boolean
    actual val hasLocalStorage: Boolean = js("window.localStorage !== undefined") as Boolean
    actual val isDebug: Boolean = window.location.hostname == "localhost"
    
    private fun detectBrowserVersion(): String {
        val userAgent = window.navigator.userAgent
        return when {
            userAgent.contains("Chrome") -> "Chrome"
            userAgent.contains("Firefox") -> "Firefox"
            userAgent.contains("Safari") -> "Safari"
            userAgent.contains("Edge") -> "Edge"
            else -> "Browser"
        }
    }
}

actual fun getPlatform(): Platform = Platform()
