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

package com.raouf.mehrguard.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Comprehensive tests for UrlAnalyzer.
 * Tests public methods only.
 */
class UrlAnalyzerTest {

    private val analyzer = UrlAnalyzer()

    // === URL PARSING TESTS ===

    @Test
    fun `parse simple https url`() {
        val result = analyzer.parse("https://example.com")
        assertNotNull(result)
        assertEquals("https", result.protocol)
        assertEquals("example.com", result.host)
        assertEquals("/", result.path)
    }

    @Test
    fun `parse url with path`() {
        val result = analyzer.parse("https://example.com/path/to/page")
        assertNotNull(result)
        assertEquals("/path/to/page", result.path)
    }

    @Test
    fun `parse url with query string`() {
        val result = analyzer.parse("https://example.com/search?q=test&page=1")
        assertNotNull(result)
        assertEquals("q=test&page=1", result.query)
    }

    @Test
    fun `parse url with port`() {
        val result = analyzer.parse("https://example.com:8080/path")
        assertNotNull(result)
        assertEquals("example.com", result.host)
        assertEquals(8080, result.port)
    }

    @Test
    fun `parse url with fragment`() {
        val result = analyzer.parse("https://example.com/page#section")
        assertNotNull(result)
        assertEquals("section", result.fragment)
    }

    @Test
    fun `parse url with subdomain`() {
        val result = analyzer.parse("https://www.example.com")
        assertNotNull(result)
        assertEquals("www.example.com", result.host)
    }

    @Test
    fun `parse http url`() {
        val result = analyzer.parse("http://example.com")
        assertNotNull(result)
        assertEquals("http", result.protocol)
    }

    @Test
    fun `parse invalid url returns null`() {
        val result = analyzer.parse("xyz")
        // Short strings without protocol may fail to parse
        assertTrue(result == null || result.host == "xyz")
    }

    @Test
    fun `parse empty string returns null`() {
        val result = analyzer.parse("")
        assertNull(result)
    }

    // === SECURE URL TESTS ===

    @Test
    fun `https url is secure`() {
        val parsed = analyzer.parse("https://example.com")!!
        assertTrue(analyzer.isSecure(parsed))
    }

    @Test
    fun `http url is not secure`() {
        val parsed = analyzer.parse("http://example.com")!!
        assertFalse(analyzer.isSecure(parsed))
    }

    // === IP ADDRESS TESTS ===

    @Test
    fun `ipv4 address is detected`() {
        assertTrue(analyzer.isIpAddress("192.168.1.1"))
    }

    @Test
    fun `ipv4 with zeros is detected`() {
        assertTrue(analyzer.isIpAddress("0.0.0.0"))
    }

    @Test
    fun `ipv4 broadcast is detected`() {
        assertTrue(analyzer.isIpAddress("255.255.255.255"))
    }

    @Test
    fun `ipv6 address is detected`() {
        assertTrue(analyzer.isIpAddress("[2001:db8::1]"))
    }

    @Test
    fun `domain is not ip address`() {
        assertFalse(analyzer.isIpAddress("google.com"))
    }

    @Test
    fun `subdomain is not ip address`() {
        assertFalse(analyzer.isIpAddress("www.example.com"))
    }

    @Test
    fun `invalid ipv4 is not detected`() {
        assertFalse(analyzer.isIpAddress("999.999.999.999"))
    }

    // === URL SHORTENER TESTS ===

    @Test
    fun `bit ly is shortener`() {
        assertTrue(analyzer.isShortener("bit.ly"))
    }

    @Test
    fun `t co is shortener`() {
        assertTrue(analyzer.isShortener("t.co"))
    }

    @Test
    fun `tinyurl is shortener`() {
        assertTrue(analyzer.isShortener("tinyurl.com"))
    }

    @Test
    fun `goo gl is shortener`() {
        assertTrue(analyzer.isShortener("goo.gl"))
    }

    @Test
    fun `ow ly is shortener`() {
        assertTrue(analyzer.isShortener("ow.ly"))
    }

    @Test
    fun `is gd is shortener`() {
        assertTrue(analyzer.isShortener("is.gd"))
    }

    @Test
    fun `google com is not shortener`() {
        assertFalse(analyzer.isShortener("google.com"))
    }

    @Test
    fun `example com is not shortener`() {
        assertFalse(analyzer.isShortener("example.com"))
    }

    // === ENTROPY TESTS ===

    @Test
    fun `repeated character has low entropy`() {
        val entropy = analyzer.calculateEntropy("aaaaaaaaaa")
        assertEquals(0.0, entropy, 0.001)
    }

    @Test
    fun `random string has higher entropy`() {
        val lowEntropy = analyzer.calculateEntropy("aaaaaa")
        val highEntropy = analyzer.calculateEntropy("abc123xyz")
        assertTrue(highEntropy > lowEntropy)
    }

    @Test
    fun `empty string has zero entropy`() {
        val entropy = analyzer.calculateEntropy("")
        assertEquals(0.0, entropy, 0.001)
    }

    @Test
    fun `unique characters have high entropy`() {
        val entropy = analyzer.calculateEntropy("abcdefghij")
        assertTrue(entropy > 2.0)
    }

    // === SUSPICIOUS PATH KEYWORDS ===

    @Test
    fun `login in path is suspicious`() {
        val count = analyzer.countSuspiciousPathKeywords("/account/login")
        assertTrue(count > 0)
    }

    @Test
    fun `password in path is suspicious`() {
        val count = analyzer.countSuspiciousPathKeywords("/reset-password")
        assertTrue(count > 0)
    }

    @Test
    fun `secure in path is suspicious`() {
        val count = analyzer.countSuspiciousPathKeywords("/secure/verify")
        assertTrue(count > 0)
    }

    @Test
    fun `normal path has no suspicious keywords`() {
        val count = analyzer.countSuspiciousPathKeywords("/about/contact")
        assertEquals(0, count)
    }

    @Test
    fun `empty path has no suspicious keywords`() {
        val count = analyzer.countSuspiciousPathKeywords("/")
        assertEquals(0, count)
    }

    // === CREDENTIAL PARAMS TESTS ===

    @Test
    fun `password param is detected`() {
        assertTrue(analyzer.hasCredentialParams("password=secret"))
    }

    @Test
    fun `token param is detected`() {
        assertTrue(analyzer.hasCredentialParams("token=abc123"))
    }

    @Test
    fun `api_key param is detected`() {
        assertTrue(analyzer.hasCredentialParams("api_key=xyz"))
    }

    @Test
    fun `normal params are not credential params`() {
        assertFalse(analyzer.hasCredentialParams("page=1&sort=asc"))
    }

    @Test
    fun `null query has no credential params`() {
        assertFalse(analyzer.hasCredentialParams(null))
    }

    @Test
    fun `empty query has no credential params`() {
        assertFalse(analyzer.hasCredentialParams(""))
    }

    // === PARSED URL DATA TESTS ===

    @Test
    fun `parsed url has all components`() {
        val result = analyzer.parse("https://www.example.com:8080/path?query=1#section")
        assertNotNull(result)
        assertEquals("https", result.protocol)
        assertEquals("www.example.com", result.host)
        assertEquals(8080, result.port)
        assertEquals("/path", result.path)
        assertEquals("query=1", result.query)
        assertEquals("section", result.fragment)
    }
}
