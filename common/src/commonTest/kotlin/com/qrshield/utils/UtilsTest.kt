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

package com.qrshield.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Tests for utility classes in Utils.kt.
 */
class UtilsTest {

    // === CONSTANTS TESTS ===

    @Test
    fun `safe threshold is 30`() {
        assertEquals(30, Constants.SAFE_THRESHOLD)
    }

    @Test
    fun `suspicious threshold is 70`() {
        assertEquals(70, Constants.SUSPICIOUS_THRESHOLD)
    }

    @Test
    fun `max url length is 2048`() {
        assertEquals(2048, Constants.MAX_URL_LENGTH)
    }

    @Test
    fun `weights sum to 1`() {
        val sum = Constants.HEURISTIC_WEIGHT + Constants.ML_WEIGHT + Constants.BRAND_WEIGHT + Constants.TLD_WEIGHT
        assertEquals(1.0, sum, 0.001)
    }

    // === URL UTILS TESTS ===

    @Test
    fun `valid https url passes`() {
        assertTrue(UrlUtils.isValidUrl("https://google.com"))
    }

    @Test
    fun `valid http url passes`() {
        assertTrue(UrlUtils.isValidUrl("http://example.com"))
    }

    @Test
    fun `empty url fails`() {
        assertFalse(UrlUtils.isValidUrl(""))
    }

    @Test
    fun `blank url fails`() {
        assertFalse(UrlUtils.isValidUrl("   "))
    }

    @Test
    fun `very long url fails`() {
        val longUrl = "https://example.com/" + "a".repeat(3000)
        assertFalse(UrlUtils.isValidUrl(longUrl))
    }

    @Test
    fun `normalize adds https if missing`() {
        val normalized = UrlUtils.normalize("example.com")
        assertTrue(normalized.startsWith("https://"))
    }

    @Test
    fun `normalize keeps existing https`() {
        val normalized = UrlUtils.normalize("https://example.com")
        assertTrue(normalized.startsWith("https://"))
    }

    @Test
    fun `normalize keeps existing http`() {
        val normalized = UrlUtils.normalize("http://example.com")
        assertTrue(normalized.startsWith("http://"))
    }

    @Test
    fun `normalize converts to lowercase`() {
        val normalized = UrlUtils.normalize("https://EXAMPLE.COM")
        assertEquals("https://example.com", normalized)
    }

    @Test
    fun `normalize trims whitespace`() {
        val normalized = UrlUtils.normalize("  https://example.com  ")
        assertEquals("https://example.com", normalized)
    }

    @Test
    fun `extract host from simple url`() {
        val host = UrlUtils.extractHost("https://example.com/path")
        assertEquals("example.com", host)
    }

    @Test
    fun `extract host from url with port`() {
        val host = UrlUtils.extractHost("https://example.com:8080/path")
        assertEquals("example.com", host)
    }

    @Test
    fun `extract host from url with query`() {
        val host = UrlUtils.extractHost("https://example.com?query=value")
        assertEquals("example.com", host)
    }

    @Test
    fun `isIpAddress detects ipv4`() {
        assertTrue(UrlUtils.isIpAddress("192.168.1.1"))
    }

    @Test
    fun `isIpAddress rejects domain`() {
        assertFalse(UrlUtils.isIpAddress("example.com"))
    }

    // === ENTROPY CALCULATOR TESTS ===

    @Test
    fun `entropy of repeated character is zero`() {
        val entropy = EntropyCalculator.calculate("aaaaaaaaaa")
        assertEquals(0.0, entropy, 0.001)
    }

    @Test
    fun `entropy of empty string is zero`() {
        val entropy = EntropyCalculator.calculate("")
        assertEquals(0.0, entropy, 0.001)
    }

    @Test
    fun `entropy of unique characters is high`() {
        val entropy = EntropyCalculator.calculate("abcdefghij")
        assertTrue(entropy > 2.0)
    }

    @Test
    fun `entropy of binary string`() {
        val entropy = EntropyCalculator.calculate("0101010101")
        assertTrue(entropy > 0)
    }

    @Test
    fun `entropy of mixed case alphabet`() {
        val entropy = EntropyCalculator.calculate("AaBbCcDdEe")
        assertTrue(entropy > 3.0)
    }

    @Test
    fun `entropy of random string is high`() {
        val entropy = EntropyCalculator.calculate("x7kP9mQ2wL")
        assertTrue(entropy > 2.5)
    }

    @Test
    fun `longer repeated string still has low entropy`() {
        val entropy = EntropyCalculator.calculate("a".repeat(100))
        assertEquals(0.0, entropy, 0.001)
    }

    @Test
    fun `isHighEntropy returns true for random strings`() {
        // Use a longer string with more character variety
        assertTrue(EntropyCalculator.isHighEntropy("x7kP9mQ2wL!@#\$%^&*()abcdefghij"))
    }

    @Test
    fun `isHighEntropy returns false for repeated strings`() {
        assertFalse(EntropyCalculator.isHighEntropy("aaaaaaaaaa"))
    }

    @Test
    fun `isHighEntropy with custom threshold`() {
        val result = EntropyCalculator.isHighEntropy("abc", 1.0)
        assertTrue(result)
    }
}
