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
 * Web (JS + Wasm) implementation of Platform.
 * 
 * Shared implementation for both Kotlin/JS and Kotlin/Wasm targets
 * using the webMain source set introduced in Kotlin 2.2.20.
 *
 * @author QR-SHIELD Security Team
 * @since 1.17.25
 */
actual class Platform actual constructor() {
    actual val name: String = "Web"
    actual val version: String = "Kotlin/Web"
    actual val hasCamera: Boolean = true
    actual val hasHaptics: Boolean = false
    actual val hasLocalStorage: Boolean = true
    actual val isDebug: Boolean = false
}

actual fun getPlatform(): Platform = Platform()
