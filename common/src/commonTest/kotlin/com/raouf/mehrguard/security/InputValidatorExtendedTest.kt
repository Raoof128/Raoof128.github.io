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

package com.raouf.mehrguard.security

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Extended tests for InputValidator.
 */
class InputValidatorExtendedTest {

    // === VALID URL TESTS ===

    @Test
    fun `valid https url passes`() {
        val result = InputValidator.validateUrl("https://google.com")
        assertTrue(result.isValid())
    }

    @Test
    fun `valid http url passes`() {
        val result = InputValidator.validateUrl("http://example.com")
        assertTrue(result.isValid())
    }

    @Test
    fun `url with path passes`() {
        val result = InputValidator.validateUrl("https://example.com/path/to/page")
        assertTrue(result.isValid())
    }

    @Test
    fun `url with query string passes`() {
        val result = InputValidator.validateUrl("https://example.com?foo=bar&baz=qux")
        assertTrue(result.isValid())
    }

    @Test
    fun `url with port passes`() {
        val result = InputValidator.validateUrl("https://example.com:8080")
        assertTrue(result.isValid())
    }

    @Test
    fun `url with fragment passes`() {
        val result = InputValidator.validateUrl("https://example.com#section")
        assertTrue(result.isValid())
    }

    @Test
    fun `url with subdomain passes`() {
        val result = InputValidator.validateUrl("https://www.example.com")
        assertTrue(result.isValid())
    }

    // === INVALID URL TESTS ===

    @Test
    fun `empty url fails`() {
        val result = InputValidator.validateUrl("")
        assertFalse(result.isValid())
    }

    @Test
    fun `null url fails`() {
        val result = InputValidator.validateUrl(null)
        assertFalse(result.isValid())
    }

    @Test
    fun `whitespace only url fails`() {
        val result = InputValidator.validateUrl("   ")
        assertFalse(result.isValid())
    }

    @Test
    fun `very long url fails`() {
        val longUrl = "https://example.com/" + "a".repeat(3000)
        val result = InputValidator.validateUrl(longUrl)
        assertFalse(result.isValid())
    }

    // === DANGEROUS CONTENT TESTS ===

    @Test
    fun `javascript url is rejected`() {
        val result = InputValidator.validateUrl("javascript:alert(1)")
        assertFalse(result.isValid())
    }

    @Test
    fun `data url is rejected`() {
        val result = InputValidator.validateUrl("data:text/html,<script>alert(1)</script>")
        assertFalse(result.isValid())
    }

    @Test
    fun `vbscript url is rejected`() {
        val result = InputValidator.validateUrl("vbscript:msgbox(1)")
        assertFalse(result.isValid())
    }

    @Test
    fun `file url is rejected`() {
        val result = InputValidator.validateUrl("file:///etc/passwd")
        assertFalse(result.isValid())
    }

    // === RESULT METHODS ===

    @Test
    fun `valid result has value`() {
        val result = InputValidator.validateUrl("https://google.com")
        assertNotNull(result.getOrNull())
    }

    @Test
    fun `invalid result returns null`() {
        val result = InputValidator.validateUrl("")
        assertNull(result.getOrNull())
    }

    @Test
    fun `map transforms valid result`() {
        val result = InputValidator.validateUrl("https://google.com")
        val mapped = result.map { it.uppercase() }
        assertTrue(mapped.isValid())
    }

    // === IP ADDRESS TESTS ===

    @Test
    fun `ipv4 url passes validation`() {
        val result = InputValidator.validateUrl("http://192.168.1.1")
        assertTrue(result.isValid())
    }

    // === TEXT INPUT VALIDATION ===

    @Test
    fun `validate text input returns result`() {
        val result = InputValidator.validateTextInput("Hello World")
        assertTrue(result.isValid())
    }

    @Test
    fun `empty text input fails validation`() {
        val result = InputValidator.validateTextInput("")
        // Empty trimmed is allowed
        assertNotNull(result)
    }

    @Test
    fun `null text input fails validation`() {
        val result = InputValidator.validateTextInput(null)
        assertFalse(result.isValid())
    }

    @Test
    fun `very long text input fails validation`() {
        val longContent = "a".repeat(10000)
        val result = InputValidator.validateTextInput(longContent)
        assertFalse(result.isValid())
    }

    @Test
    fun `normal text input passes validation`() {
        val result = InputValidator.validateTextInput("https://google.com")
        assertTrue(result.isValid())
    }

    @Test
    fun `text with null byte fails`() {
        val result = InputValidator.validateTextInput("hello\u0000world")
        assertFalse(result.isValid())
    }

    // === HOSTNAME VALIDATION ===

    @Test
    fun `valid hostname passes`() {
        val result = InputValidator.validateHostname("example.com")
        assertTrue(result.isValid())
    }

    @Test
    fun `empty hostname fails`() {
        val result = InputValidator.validateHostname("")
        assertFalse(result.isValid())
    }

    @Test
    fun `null hostname fails`() {
        val result = InputValidator.validateHostname(null)
        assertFalse(result.isValid())
    }

    @Test
    fun `ip address as hostname passes`() {
        val result = InputValidator.validateHostname("192.168.1.1")
        assertTrue(result.isValid())
    }

    @Test
    fun `punycode hostname passes`() {
        val result = InputValidator.validateHostname("xn--n3h.com")
        assertTrue(result.isValid())
    }

    // === SECURITY PATTERN DETECTION ===

    @Test
    fun `detects sql injection patterns`() {
        assertTrue(InputValidator.containsSqlInjectionPatterns("' OR 1=1 --"))
    }

    @Test
    fun `detects union select injection`() {
        assertTrue(InputValidator.containsSqlInjectionPatterns("UNION SELECT * FROM users"))
    }

    @Test
    fun `safe input has no sql injection`() {
        assertFalse(InputValidator.containsSqlInjectionPatterns("hello world"))
    }

    @Test
    fun `detects xss patterns`() {
        assertTrue(InputValidator.containsXssPatterns("<script>alert(1)</script>"))
    }

    @Test
    fun `detects javascript xss`() {
        assertTrue(InputValidator.containsXssPatterns("javascript:alert(1)"))
    }

    @Test
    fun `detects onerror xss`() {
        assertTrue(InputValidator.containsXssPatterns("<img onerror=alert(1)>"))
    }

    @Test
    fun `safe input has no xss`() {
        assertFalse(InputValidator.containsXssPatterns("hello world"))
    }

    // === BATCH VALIDATION ===

    @Test
    fun `validate multiple urls`() {
        val urls = listOf(
            "https://google.com",
            "https://example.com",
            "http://test.com"
        )

        urls.forEach { url ->
            val result = InputValidator.validateUrl(url)
            assertTrue(result.isValid(), "Failed for $url")
        }
    }

    // === EDGE CASES ===

    @Test
    fun `url with control chars fails`() {
        val result = InputValidator.validateUrl("https://example.com\u0001test")
        assertFalse(result.isValid())
    }

    @Test
    fun `url with null byte fails`() {
        val result = InputValidator.validateUrl("https://example.com\u0000/path")
        assertFalse(result.isValid())
    }
}
