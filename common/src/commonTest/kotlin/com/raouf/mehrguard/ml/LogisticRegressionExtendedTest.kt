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
import kotlin.test.assertNotNull

/**
 * Extended tests for LogisticRegressionModel and FeatureExtractor.
 */
class LogisticRegressionExtendedTest {

    private val model = LogisticRegressionModel.default()
    private val extractor = FeatureExtractor()

    // === FEATURE EXTRACTION TESTS ===

    @Test
    fun `extract features from safe url`() {
        val features = extractor.extract("https://google.com")
        assertNotNull(features)
        assertTrue(features.isNotEmpty())
    }

    @Test
    fun `extract features from suspicious url`() {
        val features = extractor.extract("http://192.168.1.1/login.php?password=abc")
        assertNotNull(features)
        assertTrue(features.isNotEmpty())
    }

    @Test
    fun `extract features from empty url`() {
        val features = extractor.extract("")
        assertNotNull(features)
    }

    @Test
    fun `extract features from http url`() {
        val features = extractor.extract("http://example.com")
        assertNotNull(features)
    }

    @Test
    fun `extract features from https url`() {
        val features = extractor.extract("https://example.com")
        assertNotNull(features)
    }

    @Test
    fun `extract features from url with path`() {
        val features = extractor.extract("https://example.com/path/to/page")
        assertNotNull(features)
    }

    @Test
    fun `extract features from url with query`() {
        val features = extractor.extract("https://example.com?foo=bar&baz=qux")
        assertNotNull(features)
    }

    @Test
    fun `extract features from ip address url`() {
        val features = extractor.extract("http://192.168.1.1")
        assertNotNull(features)
    }

    @Test
    fun `extract features from url with subdomains`() {
        val features = extractor.extract("https://www.mail.secure.example.com")
        assertNotNull(features)
    }

    // === MODEL PREDICTION TESTS ===

    @Test
    fun `predict on safe url`() {
        val features = extractor.extract("https://google.com")
        val prediction = model.predict(features)
        assertTrue(prediction in 0f..1f)
    }

    @Test
    fun `predict on suspicious url`() {
        val features = extractor.extract("http://paypa1.evil.tk/login")
        val prediction = model.predict(features)
        assertTrue(prediction in 0f..1f)
    }

    @Test
    fun `prediction is bounded between 0 and 1`() {
        val urls = listOf(
            "https://google.com",
            "http://evil.tk",
            "https://paypal-secure.evil.com",
            "http://192.168.1.1/admin"
        )

        urls.forEach { url ->
            val features = extractor.extract(url)
            val prediction = model.predict(features)
            assertTrue(prediction >= 0f && prediction <= 1f, "Prediction $prediction out of bounds for $url")
        }
    }

    // === FEATURE COUNT TESTS ===

    @Test
    fun `features have consistent count`() {
        val features1 = extractor.extract("https://google.com")
        val features2 = extractor.extract("https://example.com")

        // Feature vectors should have same dimension
        assertEquals(features1.size, features2.size)
    }

    // === URL PARSING TESTS ===

    @Test
    fun `parse url extracts components`() {
        val features = extractor.extract("https://example.com")
        assertNotNull(features)
    }

    @Test
    fun `parse url handles port`() {
        val features = extractor.extract("https://example.com:8080")
        assertNotNull(features)
    }

    @Test
    fun `parse url handles fragment`() {
        val features = extractor.extract("https://example.com#section")
        assertNotNull(features)
    }

    // === EDGE CASES ===

    @Test
    fun `handle very long url`() {
        val longPath = "a".repeat(500)
        val features = extractor.extract("https://example.com/$longPath")
        assertNotNull(features)
    }

    @Test
    fun `handle url with special characters`() {
        val features = extractor.extract("https://example.com/path?q=hello%20world&foo=bar#section")
        assertNotNull(features)
    }

    @Test
    fun `handle malformed url`() {
        val features = extractor.extract("not-a-valid-url")
        assertNotNull(features)
    }

    // === CONSISTENCY TESTS ===

    @Test
    fun `same url produces same features`() {
        val features1 = extractor.extract("https://google.com")
        val features2 = extractor.extract("https://google.com")

        assertEquals(features1.size, features2.size)
        for (i in features1.indices) {
            assertEquals(features1[i], features2[i], 0.001f)
        }
    }

    @Test
    fun `same features produce same prediction`() {
        val features = extractor.extract("https://google.com")
        val prediction1 = model.predict(features)
        val prediction2 = model.predict(features)

        assertEquals(prediction1, prediction2, 0.001f)
    }

    // === PREDICT WITH THRESHOLD ===

    @Test
    fun `predict with threshold returns prediction object`() {
        val features = extractor.extract("https://google.com")
        val prediction = model.predictWithThreshold(features)

        assertNotNull(prediction)
        assertTrue(prediction.probability in 0f..1f)
    }

    @Test
    fun `predict with custom threshold`() {
        val features = extractor.extract("https://google.com")
        val prediction = model.predictWithThreshold(features, 0.7f)

        assertNotNull(prediction)
    }
}
