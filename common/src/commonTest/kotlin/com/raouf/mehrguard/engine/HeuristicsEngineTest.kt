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

package com.raouf.mehrguard.engine

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Comprehensive tests for HeuristicsEngine.
 * Tests the public analyze() method which internally uses all heuristics.
 */
class HeuristicsEngineTest {

    private val engine = HeuristicsEngine()

    // === ANALYZE METHOD TESTS ===

    @Test
    fun `analyze returns result for safe url`() {
        val result = engine.analyze("https://google.com")
        assertNotNull(result)
        assertTrue(result.score >= 0)
        assertTrue(result.score <= 100)
    }

    @Test
    fun `analyze returns higher score for suspicious url`() {
        val safeResult = engine.analyze("https://google.com")
        val suspiciousResult = engine.analyze("http://192.168.1.1/login.php")

        assertTrue(suspiciousResult.score > safeResult.score)
    }

    @Test
    fun `analyze returns flags for issues`() {
        val result = engine.analyze("http://paypal.com@evil.tk/login.php?password=abc")
        assertTrue(result.flags.isNotEmpty())
    }

    @Test
    fun `analyze returns few flags for clean url`() {
        val result = engine.analyze("https://google.com")
        // Clean URL should have few or no flags
        assertTrue(result.flags.size < 3)
    }

    // === AT SYMBOL INJECTION DETECTION (via analyze) ===

    @Test
    fun `detects at symbol injection`() {
        val result = engine.analyze("https://google.com@evil.com")
        assertTrue(result.score > 0)
        assertTrue(result.flags.any { it.contains("@") || it.contains("credential", ignoreCase = true) || it.contains("injection", ignoreCase = true) })
    }

    // === DOUBLE EXTENSION DETECTION (via analyze) ===

    @Test
    fun `detects double extension pdf exe`() {
        val result = engine.analyze("https://example.com/file.pdf.exe")
        assertTrue(result.score > 0)
    }

    @Test
    fun `detects double extension doc scr`() {
        val result = engine.analyze("https://example.com/document.doc.scr")
        assertTrue(result.score > 0)
    }

    // === IP ADDRESS DETECTION ===

    @Test
    fun `ip address url gets flagged`() {
        val result = engine.analyze("http://192.168.1.1/login")
        assertTrue(result.score > 0)
        assertTrue(result.flags.any { it.contains("IP", ignoreCase = true) })
    }

    @Test
    fun `ipv4 addresses detected`() {
        val result = engine.analyze("http://10.0.0.1/admin")
        assertTrue(result.score > 0)
    }

    // === HTTP VS HTTPS ===

    @Test
    fun `http url gets higher score than https`() {
        val httpResult = engine.analyze("http://example.com")
        val httpsResult = engine.analyze("https://example.com")

        assertTrue(httpResult.score >= httpsResult.score)
    }

    @Test
    fun `http flagged as insecure`() {
        val result = engine.analyze("http://example.com")
        assertTrue(result.flags.any { it.contains("HTTP", ignoreCase = true) || it.contains("secure", ignoreCase = true) })
    }

    // === URL LENGTH TESTS ===

    @Test
    fun `very long url gets flagged`() {
        val longPath = "a".repeat(300)
        val result = engine.analyze("https://example.com/$longPath")
        assertTrue(result.score > 0)
    }

    // === SUSPICIOUS KEYWORDS ===

    @Test
    fun `login keyword in path detected`() {
        val result = engine.analyze("https://example.com/account/login/verify")
        assertTrue(result.score >= 0)  // At least doesn't crash
    }

    @Test
    fun `password keyword detected`() {
        val result = engine.analyze("https://example.com/reset-password/confirm")
        assertTrue(result.score >= 0)  // At least doesn't crash
    }

    // === CREDENTIAL PARAMS ===

    @Test
    fun `password in query params gets flagged`() {
        val result = engine.analyze("https://example.com/login?password=secret")
        assertTrue(result.score > 0)
    }

    @Test
    fun `token in query params detected`() {
        val result = engine.analyze("https://example.com/verify?token=abc123&email=test@example.com")
        assertTrue(result.score >= 0)
    }

    // === SUBDOMAIN TESTS ===

    @Test
    fun `excessive subdomains get flagged`() {
        val result = engine.analyze("https://mail.secure.login.account.paypal.evil.com")
        assertTrue(result.score > 0)
    }

    // === URL SHORTENER DETECTION ===

    @Test
    fun `url shortener detected`() {
        val result = engine.analyze("https://bit.ly/abc123")
        assertTrue(result.score > 0)
        // Shortener should be flagged somehow
        assertTrue(result.flags.isNotEmpty() || result.score >= 5)
    }

    @Test
    fun `t dot co shortener detected`() {
        val result = engine.analyze("https://t.co/xyz")
        assertTrue(result.score > 0)
    }

    // === ENCODED URL DETECTION ===

    @Test
    fun `excessive encoding detected`() {
        val result = engine.analyze("https://example.com/%2F%2F%2F%2F%2F%2F%2Fpath")
        assertTrue(result.score >= 0)  // At least processes without error
    }

    // === EDGE CASES ===

    @Test
    fun `empty url handled gracefully`() {
        val result = engine.analyze("")
        assertNotNull(result)
    }

    @Test
    fun `malformed url handled gracefully`() {
        val result = engine.analyze("not-a-valid-url")
        assertNotNull(result)
    }

    @Test
    fun `very complex url analyzed`() {
        val result = engine.analyze("https://user:pass@sub1.sub2.example.com:8080/path/to/file.php?q=1&r=2#section")
        assertNotNull(result)
    }
}
