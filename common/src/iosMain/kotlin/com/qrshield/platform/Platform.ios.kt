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

import platform.UIKit.UIDevice

/**
 * iOS implementation of Platform.
 */
actual class Platform actual constructor() {
    actual val name: String = "iOS"
    actual val version: String = UIDevice.currentDevice.systemVersion
    actual val hasCamera: Boolean = true
    actual val hasHaptics: Boolean = true
    actual val hasLocalStorage: Boolean = true
    actual val isDebug: Boolean = Platform.isDebugBuild
    
    companion object {
        // Debug detection via preprocessor-like check
        val isDebugBuild: Boolean = kotlin.runCatching {
            // In debug builds, assertions are enabled
            var debug = false
            assert(true.also { debug = true })
            debug
        }.getOrDefault(false)
    }
}

actual fun getPlatform(): Platform = Platform()
