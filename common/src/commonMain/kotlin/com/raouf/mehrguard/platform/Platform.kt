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
 * Platform-specific information and capabilities.
 *
 * This expect/actual declaration provides platform-specific details
 * to the shared business logic layer without breaking abstraction.
 *
 * ## Usage
 * ```kotlin
 * val platform = Platform.current()
 * println("Running on ${platform.name} ${platform.version}")
 * if (platform.hasCamera) {
 *     // Enable camera scanning
 * }
 * ```
 *
 * ## expect/actual Pattern
 * - `commonMain`: Declares the expected interface
 * - `androidMain`: Provides Android-specific implementation
 * - `iosMain`: Provides iOS-specific implementation
 * - `desktopMain`: Provides JVM desktop implementation
 * - `jsMain`: Provides browser implementation
 *
 * @author Mehr Guard Security Team
 * @since 1.0.0
 */
expect class Platform() {
    /**
     * Human-readable platform name.
     * Examples: "Android", "iOS", "Desktop", "Web"
     */
    val name: String

    /**
     * Platform version string.
     * Examples: "14", "17.0", "21", "Chrome 120"
     */
    val version: String

    /**
     * Whether this platform has camera access capability.
     */
    val hasCamera: Boolean

    /**
     * Whether this platform supports haptic feedback.
     */
    val hasHaptics: Boolean

    /**
     * Whether this platform supports local file storage.
     */
    val hasLocalStorage: Boolean

    /**
     * Whether this platform is running in debug mode.
     */
    val isDebug: Boolean
}

/**
 * Platform singleton for easy access.
 */
expect fun getPlatform(): Platform
