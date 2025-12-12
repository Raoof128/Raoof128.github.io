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

package com.qrshield.scanner

import com.qrshield.model.ContentType
import com.qrshield.model.ErrorCode
import com.qrshield.model.ScanResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Unit tests for ScanResult model.
 * 
 * Verifies:
 * - Success result construction and properties
 * - Error result construction with error codes
 * - NoQrFound singleton behavior
 * - Pattern matching exhaustiveness
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class ScanResultTest {
    
    // ============================================
    // SUCCESS RESULT TESTS
    // ============================================
    
    @Test
    fun `Success result contains correct content`() {
        val content = "https://example.com"
        val result = ScanResult.Success(content, ContentType.URL)
        
        assertIs<ScanResult.Success>(result)
        assertEquals(content, result.content)
        assertEquals(ContentType.URL, result.contentType)
    }
    
    @Test
    fun `Success result handles empty content`() {
        // Security: Empty content should still be valid (defensive)
        val result = ScanResult.Success("", ContentType.TEXT)
        
        assertIs<ScanResult.Success>(result)
        assertEquals("", result.content)
    }
    
    @Test
    fun `Success result handles special characters`() {
        val content = "https://example.com/path?param=value&other=123#anchor"
        val result = ScanResult.Success(content, ContentType.URL)
        
        assertEquals(content, result.content)
    }
    
    @Test
    fun `Success result handles unicode content`() {
        val content = "https://例え.jp/パス"
        val result = ScanResult.Success(content, ContentType.URL)
        
        assertEquals(content, result.content)
    }
    
    @Test
    fun `Success results with same content are equal`() {
        val result1 = ScanResult.Success("test", ContentType.TEXT)
        val result2 = ScanResult.Success("test", ContentType.TEXT)
        
        assertEquals(result1, result2)
    }
    
    @Test
    fun `Success results with different content are not equal`() {
        val result1 = ScanResult.Success("test1", ContentType.TEXT)
        val result2 = ScanResult.Success("test2", ContentType.TEXT)
        
        assertNotEquals(result1, result2)
    }
    
    @Test
    fun `Success results with different content types are not equal`() {
        val result1 = ScanResult.Success("test", ContentType.TEXT)
        val result2 = ScanResult.Success("test", ContentType.URL)
        
        assertNotEquals(result1, result2)
    }
    
    // ============================================
    // ERROR RESULT TESTS
    // ============================================
    
    @Test
    fun `Error result contains correct message and code`() {
        val message = "Camera permission denied"
        val code = ErrorCode.CAMERA_PERMISSION_DENIED
        val result = ScanResult.Error(message, code)
        
        assertIs<ScanResult.Error>(result)
        assertEquals(message, result.message)
        assertEquals(code, result.code)
    }
    
    @Test
    fun `Error result with empty message is valid`() {
        val result = ScanResult.Error("", ErrorCode.UNKNOWN_ERROR)
        
        assertIs<ScanResult.Error>(result)
        assertEquals("", result.message)
    }
    
    @Test
    fun `All error codes can be used`() {
        ErrorCode.entries.forEach { code ->
            val result = ScanResult.Error("Test error", code)
            assertEquals(code, result.code)
        }
    }
    
    @Test
    fun `Error results with same properties are equal`() {
        val result1 = ScanResult.Error("error", ErrorCode.CAMERA_ERROR)
        val result2 = ScanResult.Error("error", ErrorCode.CAMERA_ERROR)
        
        assertEquals(result1, result2)
    }
    
    @Test
    fun `Error results with different codes are not equal`() {
        val result1 = ScanResult.Error("error", ErrorCode.CAMERA_ERROR)
        val result2 = ScanResult.Error("error", ErrorCode.DECODE_ERROR)
        
        assertNotEquals(result1, result2)
    }
    
    // ============================================
    // NO QR FOUND TESTS
    // ============================================
    
    @Test
    fun `NoQrFound is singleton object`() {
        val result1 = ScanResult.NoQrFound
        val result2 = ScanResult.NoQrFound
        
        // Same reference (object singleton)
        assertTrue(result1 === result2)
    }
    
    @Test
    fun `NoQrFound is a ScanResult`() {
        val result: ScanResult = ScanResult.NoQrFound
        assertIs<ScanResult.NoQrFound>(result)
    }
    
    // ============================================
    // PATTERN MATCHING TESTS
    // ============================================
    
    @Test
    fun `When expression covers all cases`() {
        val results = listOf(
            ScanResult.Success("test", ContentType.TEXT),
            ScanResult.Error("error", ErrorCode.UNKNOWN_ERROR),
            ScanResult.NoQrFound
        )
        
        results.forEach { result ->
            val handled = when (result) {
                is ScanResult.Success -> true
                is ScanResult.Error -> true
                ScanResult.NoQrFound -> true
            }
            assertTrue(handled)
        }
    }
}
