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

package com.qrshield.security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Security-focused unit tests for InputValidator.
 * 
 * Tests cover:
 * - Null byte injection
 * - Control character injection
 * - Protocol validation
 * - XSS patterns
 * - SQL injection patterns
 * - Length limits
 */
class InputValidatorTest {
    
    // === URL VALIDATION TESTS ===
    
    @Test
    fun `valid HTTPS URL passes validation`() {
        val result = InputValidator.validateUrl("https://www.google.com/search?q=test")
        assertTrue(result.isValid())
        assertEquals("https://www.google.com/search?q=test", result.getOrNull())
    }
    
    @Test
    fun `valid HTTP URL passes validation`() {
        val result = InputValidator.validateUrl("http://example.com")
        assertTrue(result.isValid())
    }
    
    @Test
    fun `null URL fails validation`() {
        val result = InputValidator.validateUrl(null)
        assertFalse(result.isValid())
        assertTrue(result is InputValidator.ValidationResult.Invalid)
        assertEquals(InputValidator.ErrorCode.EMPTY_INPUT, 
            (result as InputValidator.ValidationResult.Invalid).code)
    }
    
    @Test
    fun `empty URL fails validation`() {
        val result = InputValidator.validateUrl("")
        assertFalse(result.isValid())
    }
    
    @Test
    fun `whitespace-only URL fails validation`() {
        val result = InputValidator.validateUrl("   ")
        assertFalse(result.isValid())
    }
    
    @Test
    fun `URL exceeding max length fails validation`() {
        val longUrl = "https://example.com/" + "a".repeat(3000)
        val result = InputValidator.validateUrl(longUrl)
        assertFalse(result.isValid())
        assertTrue(result is InputValidator.ValidationResult.Invalid)
        assertEquals(InputValidator.ErrorCode.TOO_LONG,
            (result as InputValidator.ValidationResult.Invalid).code)
    }
    
    // === SECURITY INJECTION TESTS ===
    
    @Test
    fun `URL with null bytes fails validation`() {
        val maliciousUrl = "https://example.com/\u0000malicious"
        val result = InputValidator.validateUrl(maliciousUrl)
        assertFalse(result.isValid())
        assertEquals(InputValidator.ErrorCode.CONTAINS_NULL_BYTES,
            (result as InputValidator.ValidationResult.Invalid).code)
    }
    
    @Test
    fun `URL with control characters fails validation`() {
        val maliciousUrl = "https://example.com/\u0007bell"
        val result = InputValidator.validateUrl(maliciousUrl)
        assertFalse(result.isValid())
    }
    
    @Test
    fun `javascript protocol rejected`() {
        val xssUrl = "javascript:alert('xss')"
        val result = InputValidator.validateUrl(xssUrl)
        assertFalse(result.isValid())
        assertEquals(InputValidator.ErrorCode.INVALID_PROTOCOL,
            (result as InputValidator.ValidationResult.Invalid).code)
    }
    
    @Test
    fun `data protocol rejected`() {
        val xssUrl = "data:text/html,<script>alert('xss')</script>"
        val result = InputValidator.validateUrl(xssUrl)
        assertFalse(result.isValid())
    }
    
    @Test
    fun `vbscript protocol rejected`() {
        val xssUrl = "vbscript:msgbox('xss')"
        val result = InputValidator.validateUrl(xssUrl)
        assertFalse(result.isValid())
    }
    
    @Test
    fun `ftp protocol rejected`() {
        val result = InputValidator.validateUrl("ftp://files.example.com/file.txt")
        assertFalse(result.isValid())
    }
    
    // === XSS PATTERN DETECTION ===
    
    @Test
    fun `detects script tag XSS pattern`() {
        assertTrue(InputValidator.containsXssPatterns("<script>alert(1)</script>"))
    }
    
    @Test
    fun `detects onerror XSS pattern`() {
        assertTrue(InputValidator.containsXssPatterns("<img onerror='alert(1)'>"))
    }
    
    @Test
    fun `detects javascript protocol XSS`() {
        assertTrue(InputValidator.containsXssPatterns("javascript:alert(1)"))
    }
    
    @Test
    fun `clean input has no XSS patterns`() {
        assertFalse(InputValidator.containsXssPatterns("Hello, this is a normal message"))
    }
    
    // === SQL INJECTION PATTERN DETECTION ===
    
    @Test
    fun `detects OR equals SQL injection`() {
        assertTrue(InputValidator.containsSqlInjectionPatterns("' OR 1=1 --"))
    }
    
    @Test
    fun `detects UNION SELECT SQL injection`() {
        assertTrue(InputValidator.containsSqlInjectionPatterns("UNION SELECT * FROM users"))
    }
    
    @Test
    fun `clean input has no SQL injection patterns`() {
        assertFalse(InputValidator.containsSqlInjectionPatterns("normal search query"))
    }
    
    // === HOSTNAME VALIDATION ===
    
    @Test
    fun `valid hostname passes validation`() {
        val result = InputValidator.validateHostname("www.example.com")
        assertTrue(result.isValid())
        assertEquals("www.example.com", result.getOrNull())
    }
    
    @Test
    fun `hostname too long fails validation`() {
        val longHost = "a".repeat(300) + ".com"
        val result = InputValidator.validateHostname(longHost)
        assertFalse(result.isValid())
    }
    
    @Test
    fun `hostname with label too long fails validation`() {
        val longLabel = "a".repeat(70) + ".com"
        val result = InputValidator.validateHostname(longLabel)
        assertFalse(result.isValid())
    }
    
    @Test
    fun `valid IP address passes hostname validation`() {
        val result = InputValidator.validateHostname("192.168.1.1")
        assertTrue(result.isValid())
    }
    
    @Test
    fun testPunycodeHostnamePassesValidation() {
        val result = InputValidator.validateHostname("xn--nxasmq5b.com")
        assertTrue(result.isValid())
    }
    
    // === EDGE CASES ===
    
    @Test
    fun testUrlWithSpacesInPathIsSanitized() {
        val result = InputValidator.validateUrl("https://example.com/path with spaces")
        assertTrue(result.isValid())
        assertTrue(result.getOrNull()?.contains("%20") == true)
    }
    
    @Test
    fun testUrlIsTrimmed() {
        val result = InputValidator.validateUrl("  https://example.com  ")
        assertTrue(result.isValid())
        assertEquals("https://example.com", result.getOrNull())
    }
}
