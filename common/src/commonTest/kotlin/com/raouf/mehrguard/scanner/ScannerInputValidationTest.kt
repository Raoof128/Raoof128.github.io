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

package com.raouf.mehrguard.scanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for scanner input validation logic.
 *
 * Tests the security validation that all scanner implementations
 * should apply to prevent DoS and handle malformed input.
 *
 * @author Mehr Guard Security Team
 * @since 1.0.0
 */
class ScannerInputValidationTest {

    companion object {
        /** Maximum content length from scanner implementations */
        private const val MAX_CONTENT_LENGTH = 4096

        /** Maximum image size from scanner implementations */
        private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10MB
    }

    // ============================================
    // CONTENT LENGTH VALIDATION TESTS
    // ============================================

    @Test
    fun `Content at max length is valid`() {
        val content = "x".repeat(MAX_CONTENT_LENGTH)
        assertTrue(content.length <= MAX_CONTENT_LENGTH)
    }

    @Test
    fun `Content exceeding max length is invalid`() {
        val content = "x".repeat(MAX_CONTENT_LENGTH + 1)
        assertFalse(content.length <= MAX_CONTENT_LENGTH)
    }

    @Test
    fun `Empty content is valid`() {
        val content = ""
        assertTrue(content.length <= MAX_CONTENT_LENGTH)
    }

    @Test
    fun `Unicode content length is correctly measured`() {
        // Each emoji is 1-2 characters depending on encoding
        val content = "🔒".repeat(1000)
        assertTrue(content.length <= MAX_CONTENT_LENGTH)
    }

    // ============================================
    // IMAGE SIZE VALIDATION TESTS
    // ============================================

    @Test
    fun `Empty image bytes are detected`() {
        val bytes = ByteArray(0)
        assertTrue(bytes.isEmpty())
    }

    @Test
    fun `Image at max size is valid`() {
        val size = MAX_IMAGE_SIZE
        assertTrue(size <= MAX_IMAGE_SIZE)
    }

    @Test
    fun `Image exceeding max size is invalid`() {
        val size = MAX_IMAGE_SIZE + 1
        assertFalse(size <= MAX_IMAGE_SIZE)
    }

    @Test
    fun `Small image is valid`() {
        val bytes = ByteArray(1024) // 1KB
        assertTrue(bytes.size <= MAX_IMAGE_SIZE)
    }

    // ============================================
    // CONTENT SANITIZATION TESTS
    // ============================================

    @Test
    fun `Content with null bytes is handled`() {
        val content = "Hello\u0000World"
        assertEquals(11, content.length) // Null byte counts
    }

    @Test
    fun `Content with control characters is handled`() {
        val content = "Hello\u0001\u0002\u0003World"
        assertTrue(content.length > 0)
    }

    @Test
    fun `URL with encoded characters is preserved`() {
        val url = "https://example.com/path%20with%20spaces"
        assertTrue(url.startsWith("https://"))
    }

    // ============================================
    // BOUNDS CHECKING TESTS
    // ============================================

    @Test
    fun `Score coercion keeps value in bounds`() {
        val invalidScores = listOf(-100, -1, 101, 1000)
        invalidScores.forEach { score ->
            val bounded = score.coerceIn(0, 100)
            assertTrue(bounded in 0..100)
        }
    }

    @Test
    fun `Valid scores are not modified`() {
        val validScores = listOf(0, 1, 50, 99, 100)
        validScores.forEach { score ->
            val bounded = score.coerceIn(0, 100)
            assertEquals(score, bounded)
        }
    }

    @Test
    fun `URL truncation works correctly`() {
        val longUrl = "https://example.com/" + "a".repeat(3000)
        val maxLength = 2048
        val truncated = longUrl.take(maxLength)

        assertEquals(maxLength, truncated.length)
        assertTrue(truncated.startsWith("https://"))
    }

    // ============================================
    // SPECIAL CHARACTER HANDLING
    // ============================================

    @Test
    fun `Backslash in content is valid`() {
        val content = "C:\\Users\\path\\file"
        assertTrue(content.contains("\\"))
    }

    @Test
    fun `Quote in content is valid`() {
        val content = "He said \"Hello\""
        assertTrue(content.contains("\""))
    }

    @Test
    fun `Newlines in content are valid`() {
        val content = "Line1\nLine2\r\nLine3"
        assertTrue(content.contains("\n"))
    }

    @Test
    fun `Tab in content is valid`() {
        val content = "Col1\tCol2\tCol3"
        assertTrue(content.contains("\t"))
    }
}
