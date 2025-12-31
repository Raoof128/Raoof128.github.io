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

package com.raouf.mehrguard.engine

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Error handling and edge case tests for the detection engine.
 *
 * These tests verify defensive programming practices:
 * - Engine doesn't crash on malformed input
 * - All edge cases are handled gracefully
 * - Results are always returned (never null)
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class ErrorHandlingTest {

    private val engine = HeuristicsEngine()

    // =========================================================================
    // Defensive Programming Tests
    // =========================================================================

    @Test
    fun testEmptyUrlDoesNotCrash() {
        val result = engine.analyze("")
        assertNotNull(result, "Empty URL should return a result")
    }

    @Test
    fun testWhitespaceUrlDoesNotCrash() {
        val result = engine.analyze("   ")
        assertNotNull(result, "Whitespace URL should return a result")
    }

    @Test
    fun testMissingProtocolDoesNotCrash() {
        val result = engine.analyze("example.com")
        assertNotNull(result, "Missing protocol should return a result")
    }

    @Test
    fun testMalformedUrlDoesNotCrash() {
        val result = engine.analyze("not-a-valid-url-at-all")
        assertNotNull(result, "Malformed URL should return a result")
    }

    @Test
    fun testOnlyProtocolDoesNotCrash() {
        val result = engine.analyze("https://")
        assertNotNull(result, "Only protocol should return a result")
    }

    // =========================================================================
    // Long Input Tests
    // =========================================================================

    @Test
    fun testVeryLongUrlFlagged() {
        val longPath = "a".repeat(10000)
        val result = engine.analyze("https://example.com/$longPath")
        assertNotNull(result, "Very long URL should return a result")
        assertTrue(result.score > 0, "Very long URL should be flagged")
    }

    @Test
    fun testManySubdomainsFlagged() {
        val subdomains = (1..20).joinToString(".") { "sub$it" }
        val result = engine.analyze("https://$subdomains.example.com")
        assertNotNull(result, "Many subdomains should return a result")
        assertTrue(result.score > 0, "Many subdomains should be flagged")
    }

    // =========================================================================
    // IP Address Tests
    // =========================================================================

    @Test
    fun testIpAddressFlagged() {
        val result = engine.analyze("https://192.168.1.1/login")
        assertNotNull(result, "IP address should return a result")
        assertTrue(result.score > 0, "IP address should be flagged")
    }

    // =========================================================================
    // Encoding Tests
    // =========================================================================

    @Test
    fun testEncodedUrlHandled() {
        val result = engine.analyze("https://example.com/%2e%2e/etc")
        assertNotNull(result, "Encoded URL should return a result")
    }

    // =========================================================================
    // Special Characters Tests
    // =========================================================================

    @Test
    fun testSpecialCharactersHandled() {
        val result = engine.analyze("https://example.com/path?q=test&foo=bar#fragment")
        assertNotNull(result, "Special characters should be handled")
    }
}
