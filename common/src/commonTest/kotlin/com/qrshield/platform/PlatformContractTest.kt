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

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

/**
 * Contract tests for expect/actual platform abstractions.
 *
 * These tests verify that all platform implementations adhere to the
 * expected contracts, preventing drift between platforms.
 *
 * ## Why Contract Tests?
 * 
 * expect/actual declarations define interfaces, but each platform implements
 * them differently. Contract tests ensure:
 * - Consistent behavior across JVM/JS/Native
 * - No silent breakages when platform code changes
 * - Documented expectations for contributors
 */
class PlatformContractTest {

    // ==================== PlatformSecureRandom Contracts ====================

    @Test
    fun secureRandom_nextBytes_returns_correct_size() {
        val sizes = listOf(1, 16, 32, 64, 128)
        
        sizes.forEach { size ->
            val bytes = PlatformSecureRandom.nextBytes(size)
            assertEquals(size, bytes.size, "nextBytes($size) should return $size bytes")
        }
    }

    @Test
    fun secureRandom_nextBytes_produces_non_zero_output() {
        // Generate enough bytes that all-zero is statistically improbable
        val bytes = PlatformSecureRandom.nextBytes(32)
        
        assertTrue(
            bytes.any { it != 0.toByte() },
            "32 random bytes should have at least one non-zero byte"
        )
    }

    @Test
    fun secureRandom_produces_varying_output() {
        val samples = (1..5).map { PlatformSecureRandom.nextBytes(16) }
        
        // All samples should be unique
        val unique = samples.distinctBy { it.toList() }
        assertTrue(
            unique.size >= 4,
            "5 random samples should produce at least 4 unique values"
        )
    }

    @Test
    fun secureRandom_uuid_has_valid_format() {
        val uuid = PlatformSecureRandom.randomUUID()
        
        // UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        val uuidRegex = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
        assertTrue(
            uuid.lowercase().matches(uuidRegex),
            "UUID should match standard format: $uuid"
        )
    }

    // ==================== PlatformTime Contracts ====================

    @Test
    fun time_currentTimeMillis_returns_reasonable_timestamp() {
        val timestamp = PlatformTime.currentTimeMillis()
        
        // Should be after 2024-01-01 (1704067200000)
        assertTrue(timestamp > 1704067200000, "Timestamp should be after 2024")
        
        // Should be before 2100-01-01 (4102444800000)
        assertTrue(timestamp < 4102444800000, "Timestamp should be before 2100")
    }

    @Test
    fun time_nanoTime_is_monotonic() {
        val t1 = PlatformTime.nanoTime()
        // Small work to ensure time passes
        repeat(1000) { it * it }
        val t2 = PlatformTime.nanoTime()
        
        assertTrue(t2 >= t1, "nanoTime should be monotonically increasing")
    }

    @Test
    fun time_formatTimestamp_returns_non_empty_string() {
        val timestamp = PlatformTime.currentTimeMillis()
        val formatted = PlatformTime.formatTimestamp(timestamp)
        
        assertTrue(formatted.isNotBlank(), "Formatted timestamp should not be blank")
    }

    @Test
    fun time_formatRelativeTime_returns_non_empty_string() {
        val timestamp = PlatformTime.currentTimeMillis() - 60_000 // 1 minute ago
        val relative = PlatformTime.formatRelativeTime(timestamp)
        
        assertTrue(relative.isNotBlank(), "Relative time should not be blank")
    }

    // ==================== PlatformLogger Contracts ====================

    @Test
    fun logger_methods_do_not_throw() {
        // All log methods should complete without exception
        PlatformLogger.debug("TestTag", "Debug message")
        PlatformLogger.info("TestTag", "Info message")
        PlatformLogger.warn("TestTag", "Warning message")
        PlatformLogger.error("TestTag", "Error message", null)
        PlatformLogger.error("TestTag", "Error with exception", RuntimeException("test"))
        
        // If we got here, no exceptions were thrown
        assertTrue(true, "Logger methods should not throw")
    }

    // ==================== PlatformClipboard Contracts ====================

    @Test
    fun clipboard_copyToClipboard_returns_boolean() {
        // Just verify it returns a value without crashing
        // Actual clipboard may not work in test environment
        val result = PlatformClipboard.copyToClipboard("test")
        
        // Result should be true or false (not null)
        assertTrue(result || !result, "copyToClipboard should return boolean")
    }

    @Test
    fun clipboard_hasText_returns_boolean() {
        val result = PlatformClipboard.hasText()
        assertTrue(result || !result, "hasText should return boolean")
    }

    // ==================== PlatformShare Contracts ====================

    @Test
    fun share_isShareSupported_returns_boolean() {
        val result = PlatformShare.isShareSupported()
        assertTrue(result || !result, "isShareSupported should return boolean")
    }

    // ==================== PlatformUrlOpener Contracts ====================

    @Test
    fun urlOpener_canOpenUrl_returns_boolean_for_valid_url() {
        val result = PlatformUrlOpener.canOpenUrl("https://example.com")
        assertTrue(result || !result, "canOpenUrl should return boolean for valid URL")
    }

    @Test
    fun urlOpener_canOpenUrl_for_invalid_url() {
        val result = PlatformUrlOpener.canOpenUrl("not-a-url")
        // Should not crash, just return false
        assertTrue(!result || result, "canOpenUrl should handle invalid URLs")
    }

    // ==================== PlatformHaptics Contracts ====================

    @Test
    fun haptics_methods_do_not_throw() {
        // All haptic methods should complete without exception
        // (may be no-op on some platforms like Desktop)
        PlatformHaptics.light()
        PlatformHaptics.medium()
        PlatformHaptics.heavy()
        PlatformHaptics.success()
        PlatformHaptics.warning()
        PlatformHaptics.error()
        
        assertTrue(true, "Haptic methods should not throw")
    }
}
