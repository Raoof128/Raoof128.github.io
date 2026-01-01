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

package com.raouf.mehrguard.ml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

/**
 * Comprehensive tests for FeatureExtractor.
 *
 * Tests all 15 features extracted from URLs for ML inference.
 */
class FeatureExtractorTest {

    private val extractor = FeatureExtractor()

    // =========================================================================
    // FEATURE COUNT VALIDATION
    // =========================================================================

    @Test
    fun `feature count matches model expectation`() {
        val features = extractor.extract("https://example.com")
        assertEquals(LogisticRegressionModel.FEATURE_COUNT, features.size)
    }

    @Test
    fun `all features are in valid range 0 to 1`() {
        val testUrls = listOf(
            "https://google.com",
            "http://192.168.1.1/login.php?password=test",
            "https://bit.ly/abc123",
            "https://paypal.com.login-secure.tk/verify?token=xyz"
        )

        for (url in testUrls) {
            val features = extractor.extract(url)
            features.forEachIndexed { index, value ->
                assertTrue(
                    value in 0f..1f,
                    "Feature $index for $url is $value, expected 0-1"
                )
            }
        }
    }

    // =========================================================================
    // FEATURE 0: URL LENGTH
    // =========================================================================

    @Test
    fun `feature 0 - short url has low length`() {
        val features = extractor.extract("https://a.com")
        assertTrue(features[0] < 0.1f, "Short URL should have low length feature")
    }

    @Test
    fun `feature 0 - long url has high length`() {
        val longPath = "a".repeat(400)
        val features = extractor.extract("https://example.com/$longPath")
        assertTrue(features[0] > 0.8f, "Long URL should have high length feature")
    }

    @Test
    fun `feature 0 - max url length is capped at 1`() {
        val veryLongPath = "a".repeat(1000)
        val features = extractor.extract("https://example.com/$veryLongPath")
        assertEquals(1f, features[0], 0.01f)
    }

    // =========================================================================
    // FEATURE 1: HOST LENGTH
    // =========================================================================

    @Test
    fun `feature 1 - short host has low value`() {
        val features = extractor.extract("https://a.io")
        assertTrue(features[1] < 0.1f)
    }

    @Test
    fun `feature 1 - long host has higher value`() {
        val longHost = "subdomain." + "a".repeat(50) + ".example.com"
        val features = extractor.extract("https://$longHost/path")
        assertTrue(features[1] > 0.5f)
    }

    // =========================================================================
    // FEATURE 2: PATH LENGTH
    // =========================================================================

    @Test
    fun `feature 2 - no path has zero value`() {
        val features = extractor.extract("https://example.com")
        assertEquals(0f, features[2], 0.01f)
    }

    @Test
    fun `feature 2 - long path has high value`() {
        val longPath = "/" + "path/".repeat(30)
        val features = extractor.extract("https://example.com$longPath")
        assertTrue(features[2] > 0.5f)
    }

    // =========================================================================
    // FEATURE 3: SUBDOMAIN COUNT
    // =========================================================================

    @Test
    fun `feature 3 - no subdomain returns zero`() {
        val features = extractor.extract("https://example.com")
        assertEquals(0f, features[3], 0.01f)
    }

    @Test
    fun `feature 3 - one subdomain returns low value`() {
        val features = extractor.extract("https://www.example.com")
        assertTrue(features[3] > 0f)
        assertTrue(features[3] < 0.5f)
    }

    @Test
    fun `feature 3 - many subdomains returns high value`() {
        val features = extractor.extract("https://a.b.c.d.e.example.com")
        assertTrue(features[3] > 0.5f)
    }

    // =========================================================================
    // FEATURE 4: HTTPS
    // =========================================================================

    @Test
    fun `feature 4 - https returns 1`() {
        val features = extractor.extract("https://example.com")
        assertEquals(1f, features[4], 0.01f)
    }

    @Test
    fun `feature 4 - http returns 0`() {
        val features = extractor.extract("http://example.com")
        assertEquals(0f, features[4], 0.01f)
    }

    @Test
    fun `feature 4 - https is case insensitive`() {
        val features = extractor.extract("HTTPS://example.com")
        assertEquals(1f, features[4], 0.01f)
    }

    // =========================================================================
    // FEATURE 5: IP ADDRESS HOST
    // =========================================================================

    @Test
    fun `feature 5 - domain returns 0`() {
        val features = extractor.extract("https://example.com")
        assertEquals(0f, features[5], 0.01f)
    }

    @Test
    fun `feature 5 - ipv4 returns 1`() {
        val features = extractor.extract("https://192.168.1.1")
        assertEquals(1f, features[5], 0.01f)
    }

    @Test
    fun `feature 5 - ipv4 with port returns 1`() {
        val features = extractor.extract("https://192.168.1.1:8080")
        assertEquals(1f, features[5], 0.01f)
    }

    @Test
    fun `feature 5 - invalid ip returns 0`() {
        val features = extractor.extract("https://999.999.999.999")
        assertEquals(0f, features[5], 0.01f)
    }

    // =========================================================================
    // FEATURE 6: DOMAIN ENTROPY
    // =========================================================================

    @Test
    fun `feature 6 - simple domain has moderate entropy`() {
        val features = extractor.extract("https://google.com")
        assertTrue(features[6] > 0.2f, "Should have some entropy")
        assertTrue(features[6] < 0.8f, "Should not be too high")
    }

    @Test
    fun `feature 6 - random domain has high entropy`() {
        val features = extractor.extract("https://x7k9m2p4q1w8e5r3.com")
        assertTrue(features[6] > 0.5f, "Random string should have high entropy")
    }

    @Test
    fun `feature 6 - repetitive domain has low entropy`() {
        val features = extractor.extract("https://aaaaaaa.com")
        assertTrue(features[6] < 0.5f, "Repetitive string should have lower entropy")
    }

    // =========================================================================
    // FEATURE 7: PATH ENTROPY
    // =========================================================================

    @Test
    fun `feature 7 - empty path has zero entropy`() {
        val features = extractor.extract("https://example.com")
        assertEquals(0f, features[7], 0.01f)
    }

    @Test
    fun `feature 7 - random path has high entropy`() {
        val features = extractor.extract("https://example.com/x7k9m2p4q1w8e5r3")
        assertTrue(features[7] > 0.4f)
    }

    // =========================================================================
    // FEATURE 8: QUERY PARAM COUNT
    // =========================================================================

    @Test
    fun `feature 8 - no query params returns zero`() {
        val features = extractor.extract("https://example.com/path")
        assertEquals(0f, features[8], 0.01f)
    }

    @Test
    fun `feature 8 - one param returns normalized value`() {
        val features = extractor.extract("https://example.com?foo=bar")
        assertTrue(features[8] > 0f)
        assertTrue(features[8] <= 0.2f)
    }

    @Test
    fun `feature 8 - many params returns higher value`() {
        val features = extractor.extract("https://example.com?a=1&b=2&c=3&d=4&e=5")
        assertTrue(features[8] >= 0.5f)
    }

    // =========================================================================
    // FEATURE 9: @ SYMBOL
    // =========================================================================

    @Test
    fun `feature 9 - no at symbol returns 0`() {
        val features = extractor.extract("https://example.com")
        assertEquals(0f, features[9], 0.01f)
    }

    @Test
    fun `feature 9 - at symbol in url returns 1`() {
        val features = extractor.extract("https://google.com@evil.com")
        assertEquals(1f, features[9], 0.01f)
    }

    @Test
    fun `feature 9 - at symbol in query returns 1`() {
        val features = extractor.extract("https://example.com?email=test@mail.com")
        assertEquals(1f, features[9], 0.01f)
    }

    // =========================================================================
    // FEATURE 10: DOT COUNT
    // =========================================================================

    @Test
    fun `feature 10 - simple domain has few dots`() {
        val features = extractor.extract("https://example.com")
        assertTrue(features[10] < 0.3f)
    }

    @Test
    fun `feature 10 - many subdomains have more dots`() {
        val features = extractor.extract("https://a.b.c.d.e.example.com")
        assertTrue(features[10] > 0.5f)
    }

    // =========================================================================
    // FEATURE 11: DASH COUNT
    // =========================================================================

    @Test
    fun `feature 11 - no dashes returns zero`() {
        val features = extractor.extract("https://example.com")
        assertEquals(0f, features[11], 0.01f)
    }

    @Test
    fun `feature 11 - dashes in domain count`() {
        val features = extractor.extract("https://my-secure-login-page.com")
        assertTrue(features[11] > 0.2f)
    }

    // =========================================================================
    // FEATURE 12: PORT NUMBER
    // =========================================================================

    @Test
    fun `feature 12 - no port returns 0`() {
        val features = extractor.extract("https://example.com")
        assertEquals(0f, features[12], 0.01f)
    }

    @Test
    fun `feature 12 - port returns 1`() {
        val features = extractor.extract("https://example.com:8080")
        assertEquals(1f, features[12], 0.01f)
    }

    @Test
    fun `feature 12 - standard ports count`() {
        val features = extractor.extract("https://example.com:443")
        assertEquals(1f, features[12], 0.01f)
    }

    // =========================================================================
    // FEATURE 13: SHORTENER DOMAIN
    // =========================================================================

    @Test
    fun `feature 13 - regular domain returns 0`() {
        val features = extractor.extract("https://example.com")
        assertEquals(0f, features[13], 0.01f)
    }

    @Test
    fun `feature 13 - bit ly returns 1`() {
        val features = extractor.extract("https://bit.ly/abc123")
        assertEquals(1f, features[13], 0.01f)
    }

    @Test
    fun `feature 13 - tinyurl returns 1`() {
        val features = extractor.extract("https://tinyurl.com/xyz")
        assertEquals(1f, features[13], 0.01f)
    }

    @Test
    fun `feature 13 - subdomain of shortener returns 1`() {
        val features = extractor.extract("https://custom.bit.ly/abc")
        assertEquals(1f, features[13], 0.01f)
    }

    // =========================================================================
    // FEATURE 14: SUSPICIOUS TLD
    // =========================================================================

    @Test
    fun `feature 14 - com tld returns 0`() {
        val features = extractor.extract("https://example.com")
        assertEquals(0f, features[14], 0.01f)
    }

    @Test
    fun `feature 14 - tk tld returns 1`() {
        val features = extractor.extract("https://example.tk")
        assertEquals(1f, features[14], 0.01f)
    }

    @Test
    fun `feature 14 - xyz tld returns 1`() {
        val features = extractor.extract("https://example.xyz")
        assertEquals(1f, features[14], 0.01f)
    }

    @Test
    fun `feature 14 - ml tld returns 1`() {
        val features = extractor.extract("https://login.example.ml")
        assertEquals(1f, features[14], 0.01f)
    }

    // =========================================================================
    // EDGE CASES
    // =========================================================================

    @Test
    fun `empty url returns all zeros`() {
        val features = extractor.extract("")
        assertTrue(features.all { it == 0f })
    }

    @Test
    fun `very long url is handled safely`() {
        val longUrl = "https://example.com/" + "a".repeat(5000)
        val features = extractor.extract(longUrl)

        // Should not crash and return valid features
        assertEquals(LogisticRegressionModel.FEATURE_COUNT, features.size)
        features.forEach { assertTrue(it in 0f..1f) }
    }

    @Test
    fun `malformed url is handled safely`() {
        val features = extractor.extract("not-a-url")

        // Should not crash
        assertEquals(LogisticRegressionModel.FEATURE_COUNT, features.size)
    }

    @Test
    fun `url with special characters is handled`() {
        val features = extractor.extract("https://example.com/path?q=hello%20world&x=<script>")

        // Should not crash
        assertEquals(LogisticRegressionModel.FEATURE_COUNT, features.size)
    }

    // =========================================================================
    // CONSISTENCY TESTS
    // =========================================================================

    @Test
    fun `same url produces same features`() {
        val url = "https://google.com/search?q=test"

        val features1 = extractor.extract(url)
        val features2 = extractor.extract(url)

        features1.forEachIndexed { index, value ->
            assertEquals(value, features2[index], 0.0001f)
        }
    }

    @Test
    fun `different urls produce different features`() {
        val features1 = extractor.extract("https://google.com")
        val features2 = extractor.extract("http://192.168.1.1:8080/login?password=test")

        // At least some features should differ
        val differences = features1.zip(features2.toList()).count { (a, b) -> a != b }
        assertTrue(differences > 5, "Different URLs should have different features")
    }

    // =========================================================================
    // PHISHING PATTERN DETECTION
    // =========================================================================

    @Test
    fun `phishing url has distinguishing features`() {
        val safeFeatures = extractor.extract("https://google.com")
        val phishingFeatures = extractor.extract("http://192.168.1.1/login.php?password=test")

        // Phishing URL should have:
        // - No HTTPS (feature 4 = 0)
        assertEquals(0f, phishingFeatures[4], 0.01f)
        // - IP address (feature 5 = 1)
        assertEquals(1f, phishingFeatures[5], 0.01f)
        // - @ symbol or credential params
        assertTrue(phishingFeatures[8] > 0f || phishingFeatures[9] > 0f)

        // Safe URL should have HTTPS
        assertEquals(1f, safeFeatures[4], 0.01f)
        // And no IP address
        assertEquals(0f, safeFeatures[5], 0.01f)
    }
}
