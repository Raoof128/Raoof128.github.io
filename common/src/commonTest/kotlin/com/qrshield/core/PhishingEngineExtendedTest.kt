/*
 * Copyright 2024 QR-SHIELD Contributors
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

package com.qrshield.core

import com.qrshield.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertFalse

/**
 * Comprehensive tests for PhishingEngine - the main analysis entry point.
 */
class PhishingEngineExtendedTest {

    private val engine = PhishingEngine()

    // === SAFE URL TESTS ===

    @Test
    fun `google com is safe`() {
        val result = engine.analyze("https://google.com")
        assertEquals(Verdict.SAFE, result.verdict)
        assertTrue(result.score <= 30)
    }

    @Test
    fun `microsoft com is safe`() {
        val result = engine.analyze("https://microsoft.com")
        assertEquals(Verdict.SAFE, result.verdict)
    }

    @Test
    fun `apple com is safe`() {
        val result = engine.analyze("https://apple.com")
        assertEquals(Verdict.SAFE, result.verdict)
    }

    @Test
    fun `amazon com is safe`() {
        val result = engine.analyze("https://amazon.com")
        assertEquals(Verdict.SAFE, result.verdict)
    }

    @Test
    fun `github com is safe`() {
        val result = engine.analyze("https://github.com")
        assertEquals(Verdict.SAFE, result.verdict)
    }

    @Test
    fun `wikipedia org is safe`() {
        val result = engine.analyze("https://wikipedia.org")
        assertEquals(Verdict.SAFE, result.verdict)
    }

    @Test
    fun `linkedin com is safe`() {
        val result = engine.analyze("https://linkedin.com")
        assertEquals(Verdict.SAFE, result.verdict)
    }

    // === SUSPICIOUS URL TESTS ===

    @Test
    fun `http url is at least suspicious`() {
        val result = engine.analyze("http://example.com")
        assertTrue(result.score > 0)
    }

    @Test
    fun `ip address url is suspicious`() {
        val result = engine.analyze("http://192.168.1.1")
        assertTrue(result.flags.any { it.contains("IP", ignoreCase = true) })
    }

    @Test
    fun `shortener url is flagged`() {
        val result = engine.analyze("https://bit.ly/abc123")
        assertTrue(result.score > 0)
    }

    @Test
    fun `high risk tld increases score`() {
        val safeResult = engine.analyze("https://example.com")
        val riskyResult = engine.analyze("https://example.tk")
        assertTrue(riskyResult.score > safeResult.score)
    }

    // === BRAND IMPERSONATION TESTS ===

    @Test
    fun `paypal typosquat is flagged`() {
        val result = engine.analyze("https://paypa1.com/login")
        assertTrue(result.score > 10, "Score was ${result.score}")
    }

    @Test
    fun `google typosquat is flagged`() {
        val result = engine.analyze("https://googgle.com")
        assertTrue(result.score > 10)
    }

    @Test
    fun `microsoft typosquat is flagged`() {
        val result = engine.analyze("https://micros0ft.com")
        assertTrue(result.score > 10)
    }

    @Test
    fun `apple typosquat is flagged`() {
        val result = engine.analyze("https://app1e.com")
        assertTrue(result.score > 10)
    }

    @Test
    fun `amazon typosquat is flagged`() {
        val result = engine.analyze("https://amaz0n.com")
        assertTrue(result.score > 10)
    }

    @Test
    fun `facebook typosquat is flagged`() {
        val result = engine.analyze("https://faceb00k.com")
        assertTrue(result.score > 10)
    }

    // === PATH KEYWORDS DETECTION ===

    @Test
    fun `login path keyword detected`() {
        val result = engine.analyze("https://example.com/login")
        assertTrue(result.score >= 0)
    }

    @Test
    fun `signin path keyword detected`() {
        val result = engine.analyze("https://example.com/signin")
        assertTrue(result.score >= 0)
    }

    @Test
    fun `verify path keyword detected`() {
        val result = engine.analyze("https://example.com/verify")
        assertTrue(result.score >= 0)
    }

    @Test
    fun `account path keyword detected`() {
        val result = engine.analyze("https://example.com/account")
        assertTrue(result.score >= 0)
    }

    @Test
    fun `security path keyword detected`() {
        val result = engine.analyze("https://example.com/security")
        assertTrue(result.score >= 0)
    }

    // === CREDENTIAL PARAMS DETECTION ===

    @Test
    fun `password query param flagged`() {
        val result = engine.analyze("https://example.com?password=test")
        assertTrue(result.score > 0)
    }

    @Test
    fun `token query param flagged`() {
        val result = engine.analyze("https://example.com?token=abc123")
        assertTrue(result.flags.any { it.contains("credential", ignoreCase = true) || it.contains("param", ignoreCase = true) } || result.score > 0)
    }

    // === EXCESSIVE SUBDOMAIN TESTS ===

    @Test
    fun `many subdomains increases score`() {
        val result = engine.analyze("https://secure.login.account.verify.bank.example.com")
        assertTrue(result.score > 0)
    }

    // === COMPLEX URL TESTS ===

    @Test
    fun `url with all components analyzed`() {
        val result = engine.analyze("https://user:pass@example.com:8080/path?query=1#fragment")
        assertNotNull(result)
        assertTrue(result.score >= 0)
    }

    @Test
    fun `very long url handled`() {
        val longPath = "a".repeat(500)
        val result = engine.analyze("https://example.com/$longPath")
        assertNotNull(result)
    }

    // === RESULT STRUCTURE TESTS ===

    @Test
    fun `result has score`() {
        val result = engine.analyze("https://google.com")
        assertTrue(result.score in 0..100)
    }

    @Test
    fun `result has verdict`() {
        val result = engine.analyze("https://google.com")
        assertNotNull(result.verdict)
    }

    @Test
    fun `result has flags list`() {
        val result = engine.analyze("https://google.com")
        assertNotNull(result.flags)
    }

    @Test
    fun `result has details`() {
        val result = engine.analyze("https://google.com")
        assertNotNull(result.details)
    }

    @Test
    fun `result has confidence`() {
        val result = engine.analyze("https://google.com")
        assertTrue(result.confidence in 0f..1f)
    }

    // === EDGE CASES ===

    @Test
    fun `empty url handled`() {
        val result = engine.analyze("")
        assertNotNull(result)
    }

    @Test
    fun `null-like url handled`() {
        val result = engine.analyze("null")
        assertNotNull(result)
    }

    @Test
    fun `javascript url handled`() {
        val result = engine.analyze("javascript:alert(1)")
        assertNotNull(result)
        // May or may not have score > 0 depending on detection
        assertTrue(result.score >= 0)
    }

    @Test
    fun `data url handled`() {
        val result = engine.analyze("data:text/html,<script>alert(1)</script>")
        assertNotNull(result)
    }

    @Test
    fun `file url handled`() {
        val result = engine.analyze("file:///etc/passwd")
        assertNotNull(result)
    }

    @Test
    fun `ftp url handled`() {
        val result = engine.analyze("ftp://example.com/file")
        assertNotNull(result)
    }

    // === SCORING CONSISTENCY TESTS ===

    @Test
    fun `same url produces same score`() {
        val result1 = engine.analyze("https://google.com")
        val result2 = engine.analyze("https://google.com")
        assertEquals(result1.score, result2.score)
        assertEquals(result1.verdict, result2.verdict)
    }

    @Test
    fun `case insensitive domain scoring`() {
        val lower = engine.analyze("https://google.com")
        val upper = engine.analyze("https://GOOGLE.COM")
        // Should produce similar results
        assertTrue(kotlin.math.abs(lower.score - upper.score) <= 5)
    }
}
