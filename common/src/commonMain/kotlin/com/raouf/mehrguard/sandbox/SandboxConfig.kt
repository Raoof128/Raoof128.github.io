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

package com.raouf.mehrguard.sandbox

/**
 * Sandbox Configuration for Safe URL Preview
 *
 * Defines security settings for the isolated sandbox environment
 * used to safely preview suspicious URLs without risk.
 *
 * ## Security Guarantees
 * - No JavaScript execution
 * - No cookies or session data
 * - No local storage access
 * - No autofill or form data
 * - No external intents or deep links
 * - No redirect escape to system browser
 * - Persistent safety overlay
 *
 * @author QR-SHIELD Security Team
 * @since 1.4.0
 */
data class SandboxConfig(
    /**
     * Whether JavaScript is enabled.
     * Must always be false for security.
     */
    val javaScriptEnabled: Boolean = false,
    
    /**
     * Whether cookies are accepted.
     * Must always be false for isolation.
     */
    val cookiesEnabled: Boolean = false,
    
    /**
     * Whether local storage (DOM storage) is enabled.
     * Must always be false for privacy.
     */
    val localStorageEnabled: Boolean = false,
    
    /**
     * Whether autofill is enabled.
     * Must always be false to prevent credential theft.
     */
    val autofillEnabled: Boolean = false,
    
    /**
     * Whether form data is saved.
     * Must always be false for privacy.
     */
    val saveFormDataEnabled: Boolean = false,
    
    /**
     * Whether external links should open in sandbox.
     * If false, external links are blocked entirely.
     */
    val allowExternalLinks: Boolean = false,
    
    /**
     * Whether redirects are followed.
     * Should be true to show final destination, but capped.
     */
    val followRedirects: Boolean = true,
    
    /**
     * Maximum redirects to follow before blocking.
     */
    val maxRedirects: Int = 3,
    
    /**
     * Connection timeout in milliseconds.
     */
    val timeoutMs: Long = 10_000,
    
    /**
     * Whether to show safety overlay.
     * Must always be true for user awareness.
     */
    val showSafetyOverlay: Boolean = true,
    
    /**
     * Whether zoom controls are enabled.
     */
    val zoomEnabled: Boolean = true,
    
    /**
     * User agent string for sandbox.
     * Uses a generic agent to avoid fingerprinting.
     */
    val userAgent: String = "QR-SHIELD Sandbox/1.0 (Security Preview Mode)"
) {
    companion object {
        /**
         * Maximum security sandbox configuration.
         * All dangerous features disabled.
         */
        val MAXIMUM_SECURITY = SandboxConfig(
            javaScriptEnabled = false,
            cookiesEnabled = false,
            localStorageEnabled = false,
            autofillEnabled = false,
            saveFormDataEnabled = false,
            allowExternalLinks = false,
            followRedirects = true,
            maxRedirects = 3,
            showSafetyOverlay = true
        )
        
        /**
         * Validate URL before opening in sandbox.
         * Returns null if valid, error message if invalid.
         */
        fun validateUrl(url: String): String? {
            return when {
                url.isBlank() -> "URL cannot be empty"
                !url.startsWith("http://") && !url.startsWith("https://") -> 
                    "Only HTTP and HTTPS URLs are supported"
                url.length > 2048 -> "URL is too long (max 2048 characters)"
                url.contains("javascript:", ignoreCase = true) -> 
                    "JavaScript URLs are not allowed"
                url.contains("data:", ignoreCase = true) -> 
                    "Data URLs are not allowed"
                url.contains("file:", ignoreCase = true) -> 
                    "File URLs are not allowed"
                else -> null // Valid
            }
        }
        
        /**
         * Sanitize URL for sandbox loading.
         * Removes potentially dangerous components.
         */
        fun sanitizeUrl(url: String): String {
            return url
                .trim()
                .take(2048)
                .replace("javascript:", "blocked:", ignoreCase = true)
                .replace("data:", "blocked:", ignoreCase = true)
        }
    }
}

/**
 * Sandbox state for UI representation.
 */
sealed class SandboxState {
    data object Idle : SandboxState()
    data class Loading(val url: String, val progress: Int) : SandboxState()
    data class Loaded(val url: String, val finalUrl: String, val redirectCount: Int) : SandboxState()
    data class Error(val message: String, val url: String) : SandboxState()
    data class Blocked(val reason: String, val url: String) : SandboxState()
}

/**
 * Events emitted by the sandbox.
 */
sealed class SandboxEvent {
    data class PageStarted(val url: String) : SandboxEvent()
    data class PageFinished(val url: String) : SandboxEvent()
    data class ProgressChanged(val progress: Int) : SandboxEvent()
    data class RedirectDetected(val fromUrl: String, val toUrl: String, val count: Int) : SandboxEvent()
    data class ExternalLinkBlocked(val url: String) : SandboxEvent()
    data class Error(val message: String, val errorCode: Int) : SandboxEvent()
}

/**
 * Security overlay message for sandbox.
 */
object SandboxOverlay {
    const val TITLE = "Isolated Sandbox Mode"
    const val SUBTITLE = "Links Are Non-Clickable"
    const val WARNING = "⚠️ You are viewing this page in a secure sandbox. JavaScript, cookies, and navigation are disabled."
    
    val securityFeatures = listOf(
        "✓ JavaScript Disabled",
        "✓ Cookies Blocked",
        "✓ Storage Isolated",
        "✓ Links Disabled",
        "✓ No Tracking"
    )
}
