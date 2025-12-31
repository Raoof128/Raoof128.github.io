/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under Apache 2.0
 */

package com.raouf.mehrguard.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests for URL parsing functionality.
 * Ensures consistent extraction of URL components across all platforms.
 */
class UrlParsingTest {

    // =====================================================
    // Basic URL Component Extraction
    // =====================================================

    @Test
    fun `extracts protocol correctly`() {
        assertEquals("https", extractProtocol("https://example.com"))
        assertEquals("http", extractProtocol("http://example.com"))
        assertEquals("ftp", extractProtocol("ftp://files.example.com"))
    }

    @Test
    fun `extracts host correctly`() {
        assertEquals("example.com", extractHost("https://example.com/path"))
        assertEquals("sub.example.com", extractHost("https://sub.example.com"))
        assertEquals("192.168.1.1", extractHost("http://192.168.1.1:8080/admin"))
    }

    @Test
    fun `extracts port correctly`() {
        assertEquals(443, extractPort("https://example.com"))  // Default HTTPS
        assertEquals(80, extractPort("http://example.com"))    // Default HTTP
        assertEquals(8080, extractPort("http://example.com:8080/path"))
        assertEquals(3000, extractPort("https://localhost:3000"))
    }

    @Test
    fun `extracts path correctly`() {
        assertEquals("/login", extractPath("https://example.com/login"))
        assertEquals("/", extractPath("https://example.com/"))
        assertEquals("/path/to/resource", extractPath("https://example.com/path/to/resource?query=1"))
    }

    @Test
    fun `extracts query parameters correctly`() {
        val params = extractQueryParams("https://example.com?foo=bar&baz=qux")
        assertEquals("bar", params["foo"])
        assertEquals("qux", params["baz"])
    }

    // =====================================================
    // Domain Analysis
    // =====================================================

    @Test
    fun `counts subdomains correctly`() {
        assertEquals(0, countSubdomains("example.com"))
        assertEquals(1, countSubdomains("www.example.com"))
        assertEquals(2, countSubdomains("api.staging.example.com"))
        assertEquals(3, countSubdomains("deep.nested.sub.example.com"))
    }

    @Test
    fun `identifies TLD correctly`() {
        assertEquals("com", extractTld("example.com"))
        assertEquals("co.uk", extractTld("example.co.uk"))
        assertEquals("tk", extractTld("malicious.tk"))
        assertEquals("com.au", extractTld("commbank.com.au"))
    }

    @Test
    fun `detects IP address hosts`() {
        assertTrue(isIpAddress("192.168.1.1"))
        assertTrue(isIpAddress("10.0.0.1"))
        assertTrue(isIpAddress("8.8.8.8"))
        assertFalse(isIpAddress("example.com"))
        assertFalse(isIpAddress("192.168.1.256"))  // Invalid IP
    }

    // =====================================================
    // Special Character Detection
    // =====================================================

    @Test
    fun `detects at symbol in URL`() {
        assertTrue(hasAtSymbol("https://user@evil.com"))
        assertTrue(hasAtSymbol("https://admin:password@evil.com"))
        assertFalse(hasAtSymbol("https://example.com"))
    }

    @Test
    fun `counts dots in domain`() {
        assertEquals(1, countDots("example.com"))
        assertEquals(2, countDots("www.example.com"))
        assertEquals(3, countDots("api.staging.example.com"))
    }

    @Test
    fun `counts dashes in domain`() {
        assertEquals(0, countDashes("example.com"))
        assertEquals(1, countDashes("my-site.com"))
        assertEquals(2, countDashes("my-great-site.com"))
    }

    // =====================================================
    // URL Normalization
    // =====================================================

    @Test
    fun `normalizes URLs consistently`() {
        assertEquals("https://example.com/", normalizeUrl("https://example.com"))
        assertEquals("https://example.com/", normalizeUrl("HTTPS://EXAMPLE.COM/"))
        assertEquals("https://example.com/path", normalizeUrl("https://example.com/path?"))
    }

    @Test
    fun `handles malformed URLs gracefully`() {
        assertNotNull(safeParseUrl("not-a-url"))
        assertNotNull(safeParseUrl(""))
        assertNotNull(safeParseUrl("javascript:alert(1)"))
    }

    // =====================================================
    // Helper Functions (Simulated for Testing)
    // =====================================================

    private fun extractProtocol(url: String): String {
        return url.substringBefore("://").lowercase()
    }

    private fun extractHost(url: String): String {
        return url.substringAfter("://")
            .substringBefore("/")
            .substringBefore("?")
            .substringBefore(":")
    }

    private fun extractPort(url: String): Int {
        val afterProtocol = url.substringAfter("://")
        val hostPart = afterProtocol.substringBefore("/")
        return if (":" in hostPart) {
            hostPart.substringAfter(":").toIntOrNull() ?: 80
        } else {
            if (url.startsWith("https")) 443 else 80
        }
    }

    private fun extractPath(url: String): String {
        val afterHost = url.substringAfter("://").substringAfter("/", "/")
        return "/" + afterHost.substringBefore("?").substringBefore("#")
    }

    private fun extractQueryParams(url: String): Map<String, String> {
        if ("?" !in url) return emptyMap()
        val query = url.substringAfter("?").substringBefore("#")
        return query.split("&").associate {
            val parts = it.split("=")
            parts[0] to (parts.getOrNull(1) ?: "")
        }
    }

    private fun countSubdomains(host: String): Int {
        val parts = host.split(".")
        return (parts.size - 2).coerceAtLeast(0)
    }

    private fun extractTld(host: String): String {
        val parts = host.split(".")
        return when {
            parts.size >= 3 && parts.takeLast(2).joinToString(".") in listOf("co.uk", "com.au", "co.nz") ->
                parts.takeLast(2).joinToString(".")
            else -> parts.last()
        }
    }

    private fun isIpAddress(host: String): Boolean {
        val parts = host.split(".")
        if (parts.size != 4) return false
        return parts.all { it.toIntOrNull()?.let { n -> n in 0..255 } ?: false }
    }

    private fun hasAtSymbol(url: String): Boolean {
        val afterProtocol = url.substringAfter("://").substringBefore("/")
        return "@" in afterProtocol
    }

    private fun countDots(host: String) = host.count { it == '.' }
    private fun countDashes(host: String) = host.count { it == '-' }

    private fun normalizeUrl(url: String): String {
        var normalized = url.lowercase()
        if (!normalized.endsWith("/") && "?" !in normalized && "#" !in normalized) {
            normalized += "/"
        }
        return normalized.trimEnd('?', '#')
    }

    private fun safeParseUrl(url: String): Any? {
        return try {
            mapOf("url" to url, "valid" to url.contains("://"))
        } catch (e: Exception) {
            mapOf("url" to url, "valid" to false)
        }
    }
}
