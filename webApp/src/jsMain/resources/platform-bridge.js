/**
 * QR-SHIELD Platform Bridge
 * 
 * JavaScript implementations for Kotlin/JS and Kotlin/Wasm external declarations.
 * These functions provide platform-specific capabilities via browser APIs.
 *
 * ## Expect/Actual "Escape Hatch" Pattern for KMP Web Targets
 *
 * This file implements the native side of KMP's expect/actual pattern for web:
 * 
 * | Kotlin expect       | JavaScript Implementation          |
 * |---------------------|-------------------------------------|
 * | PlatformTime        | Date.now(), performance.now()       |
 * | PlatformClipboard   | navigator.clipboard.writeText()     |
 * | PlatformSecureRandom| crypto.getRandomValues() (CSPRNG)   |
 * | PlatformUrlOpener   | window.open()                       |
 *
 * @author QR-SHIELD Security Team
 * @since 1.17.26
 * @see common/src/webMain/kotlin/com/qrshield/platform/WebPlatformAbstractions.kt
 */

// ==================== Clipboard API ====================

/**
 * Copy text to clipboard using the modern Clipboard API.
 * Falls back to document.execCommand for older browsers.
 * 
 * @param {string} text - The text to copy
 */
window.copyTextToClipboard = function (text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
        // Modern async clipboard API
        navigator.clipboard.writeText(text).catch(function (err) {
            console.warn('[Platform] Clipboard API failed:', err);
            // Fallback to legacy method
            copyTextLegacy(text);
        });
    } else {
        // Legacy fallback
        copyTextLegacy(text);
    }
};

/**
 * Legacy clipboard fallback using execCommand (deprecated but widely supported).
 * 
 * @param {string} text - The text to copy
 */
function copyTextLegacy(text) {
    var textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.select();
    try {
        document.execCommand('copy');
    } catch (err) {
        console.error('[Platform] Legacy clipboard copy failed:', err);
    }
    document.body.removeChild(textarea);
}

// ==================== Time API ====================

/**
 * Get current Unix timestamp in milliseconds.
 * Uses Date.now() which is the standard method.
 * 
 * @returns {number} Current time in milliseconds since Unix epoch
 */
window.getCurrentTimeMillis = function () {
    return Date.now();
};

/**
 * Get high-resolution monotonic time in milliseconds.
 * Uses performance.now() for accurate timing measurements.
 * 
 * @returns {number} High-resolution time in milliseconds
 */
window.getPerformanceNow = function () {
    return performance.now();
};

/**
 * Format a timestamp for display using locale-aware formatting.
 * 
 * @param {number} millis - Unix timestamp in milliseconds
 * @returns {string} Formatted date string
 */
window.formatDate = function (millis) {
    var date = new Date(millis);
    return date.toLocaleString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
    });
};

// ==================== Secure Random API (Web Crypto) ====================

/**
 * Generate cryptographically secure random bytes using Web Crypto API.
 * Returns comma-separated signed byte values for WASM compatibility.
 * 
 * This uses crypto.getRandomValues() which is:
 * - Cryptographically secure (CSPRNG)
 * - Required by W3C Web Cryptography API spec
 * - Available in all modern browsers (Chrome 11+, Firefox 21+, Safari 5+)
 * - Hardware-backed on most platforms via OS CSPRNG
 * 
 * @param {number} size - Number of bytes to generate (max 65536 per call)
 * @returns {string} Comma-separated signed byte values (e.g., "-128,0,127,42")
 * @throws {Error} If size exceeds 65536 or crypto API unavailable
 */
window.getSecureRandomBytesAsString = function (size) {
    if (size > 65536) {
        throw new Error('getSecureRandomBytesAsString: Maximum size is 65536 bytes per call');
    }

    if (size <= 0) {
        return '';
    }

    // Use Web Crypto API - universally available in modern browsers
    if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
        // Generate random bytes as Uint8Array
        var buffer = new Uint8Array(size);
        crypto.getRandomValues(buffer);

        // Convert to comma-separated signed byte string for WASM compatibility
        var signedBytes = [];
        for (var i = 0; i < size; i++) {
            // Convert unsigned (0-255) to signed (-128 to 127)
            var signedValue = buffer[i] > 127 ? buffer[i] - 256 : buffer[i];
            signedBytes.push(signedValue);
        }
        return signedBytes.join(',');
    } else {
        throw new Error('Web Crypto API not available - secure random generation not possible');
    }
};

/**
 * Legacy function for backwards compatibility with Kotlin/JS.
 * Generates cryptographically secure random bytes.
 * 
 * @param {number} size - Number of bytes to generate (max 65536 per call)
 * @returns {Int8Array} Secure random bytes as signed byte array (Kotlin ByteArray compatible)
 * @throws {Error} If size exceeds 65536 or crypto API unavailable
 */
window.getSecureRandomBytes = function (size) {
    if (size > 65536) {
        throw new Error('getSecureRandomBytes: Maximum size is 65536 bytes per call');
    }

    if (size <= 0) {
        return new Int8Array(0);
    }

    // Use Web Crypto API - universally available in modern browsers
    if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
        // Generate random bytes as Uint8Array
        var buffer = new Uint8Array(size);
        crypto.getRandomValues(buffer);

        // Convert to Int8Array for Kotlin ByteArray compatibility
        // Kotlin ByteArray expects signed bytes (-128 to 127)
        var signedBytes = new Int8Array(size);
        for (var i = 0; i < size; i++) {
            // Convert unsigned (0-255) to signed (-128 to 127)
            signedBytes[i] = buffer[i] > 127 ? buffer[i] - 256 : buffer[i];
        }
        return signedBytes;
    } else {
        throw new Error('Web Crypto API not available - secure random generation not possible');
    }
};

// ==================== URL Opener API ====================

/**
 * Open a URL in a new browser tab/window.
 * Uses window.open with noopener for security.
 * 
 * @param {string} url - The URL to open
 */
window.openUrlExternal = function (url) {
    // Use noopener to prevent the new tab from accessing window.opener (security best practice)
    window.open(url, '_blank', 'noopener,noreferrer');
};

// ==================== Console Bridge (for Wasm) ====================

/**
 * Log message to console (used by Wasm target).
 * The Kotlin/JS target can use console directly, but Wasm needs this bridge.
 * 
 * @param {string} message - The message to log
 */
window.consoleLog = function (message) {
    console.log(message);
};

/**
 * Log error to console (used by Wasm target).
 * 
 * @param {string} message - The error message to log
 */
window.consoleError = function (message) {
    console.error(message);
};

// ==================== Initialization ====================

console.log('[Platform Bridge] QR-SHIELD platform abstractions loaded');
console.log('[Platform Bridge] Web Crypto available:', typeof crypto !== 'undefined' && !!crypto.getRandomValues);
console.log('[Platform Bridge] Clipboard API available:', !!(navigator.clipboard && navigator.clipboard.writeText));
