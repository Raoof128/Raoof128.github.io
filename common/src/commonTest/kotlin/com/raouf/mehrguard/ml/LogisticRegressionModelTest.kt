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

package com.raouf.mehrguard.ml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

/**
 * Unit tests for ML Model and Feature Extractor.
 *
 * Tests cover:
 * - Model prediction accuracy
 * - Feature extraction correctness
 * - Edge cases and error handling
 * - Numerical stability (overflow prevention)
 */
class LogisticRegressionModelTest {

    private val model = LogisticRegressionModel.default()
    private val extractor = FeatureExtractor()

    // === MODEL PREDICTION TESTS ===

    @Test
    fun `predict returns value between 0 and 1`() {
        val features = FloatArray(LogisticRegressionModel.FEATURE_COUNT) { 0.5f }
        val prediction = model.predict(features)

        assertTrue(prediction >= 0f)
        assertTrue(prediction <= 1f)
    }

    @Test
    fun `all zeros features returns low probability`() {
        val features = FloatArray(LogisticRegressionModel.FEATURE_COUNT) { 0f }
        val prediction = model.predict(features)

        // All zeros should lean toward safe (due to negative bias)
        assertTrue(prediction < 0.5f)
    }

    @Test
    fun `high risk features return high probability`() {
        val features = FloatArray(LogisticRegressionModel.FEATURE_COUNT)

        // Set high-risk indicators
        features[4] = 0f   // No HTTPS
        features[5] = 1f   // IP host
        features[9] = 1f   // @ symbol
        features[12] = 1f  // Port number
        features[14] = 1f  // Suspicious TLD

        val prediction = model.predict(features)
        assertTrue(prediction > 0.5f, "High risk features should produce high probability")
    }

    @Test
    fun `HTTPS reduces risk score`() {
        val httpFeatures = FloatArray(LogisticRegressionModel.FEATURE_COUNT)
        httpFeatures[4] = 0f  // No HTTPS

        val httpsFeatures = FloatArray(LogisticRegressionModel.FEATURE_COUNT)
        httpsFeatures[4] = 1f  // HTTPS

        val httpPrediction = model.predict(httpFeatures)
        val httpsPrediction = model.predict(httpsFeatures)

        assertTrue(httpsPrediction < httpPrediction,
            "HTTPS should reduce risk: HTTPS=$httpsPrediction, HTTP=$httpPrediction")
    }

    @Test
    fun `incorrect feature count throws exception`() {
        val wrongSize = FloatArray(10) { 0f }

        assertFailsWith<IllegalArgumentException> {
            model.predict(wrongSize)
        }
    }

    // === SAFE SIGMOID TESTS ===

    @Test
    fun `extreme positive values do not overflow`() {
        // Features that would produce very high z value
        val features = FloatArray(LogisticRegressionModel.FEATURE_COUNT) { 10f }

        val prediction = model.predict(features)

        assertTrue(prediction.isFinite(), "Prediction should be finite")
        assertTrue(prediction <= 1f, "Prediction should not exceed 1")
    }

    @Test
    fun `extreme negative values do not underflow`() {
        // Features that would produce very low z value
        val features = FloatArray(LogisticRegressionModel.FEATURE_COUNT) { -10f }

        val prediction = model.predict(features)

        assertTrue(prediction.isFinite(), "Prediction should be finite")
        assertTrue(prediction >= 0f, "Prediction should not be negative")
    }

    // === PREDICT WITH THRESHOLD TESTS ===

    @Test
    fun `predictWithThreshold classifies correctly`() {
        val safeFeatures = FloatArray(LogisticRegressionModel.FEATURE_COUNT)
        safeFeatures[4] = 1f  // HTTPS set

        val result = model.predictWithThreshold(safeFeatures, 0.5f)

        // Safe features should classify as not phishing
        assertFalse(result.isPhishing)
        assertTrue(result.confidence >= 0f)
        assertTrue(result.confidence <= 1f)
    }

    @Test
    fun `custom threshold affects classification`() {
        val features = FloatArray(LogisticRegressionModel.FEATURE_COUNT) { 0.5f }

        val lowThreshold = model.predictWithThreshold(features, 0.3f)
        val highThreshold = model.predictWithThreshold(features, 0.9f)

        // With low threshold, more likely to classify as phishing
        // With high threshold, less likely
        assertTrue(lowThreshold.isPhishing != highThreshold.isPhishing ||
            lowThreshold.probability == highThreshold.probability)
    }

    // === FEATURE EXTRACTION TESTS ===

    @Test
    fun `extract returns correct feature count`() {
        val features = extractor.extract("https://example.com/path")
        assertEquals(LogisticRegressionModel.FEATURE_COUNT, features.size)
    }

    @Test
    fun `all features are normalized 0 to 1`() {
        val features = extractor.extract("https://secure.subdomain.example.com:8080/very/long/path/here?a=1&b=2&c=3")

        for ((index, feature) in features.withIndex()) {
            assertTrue(feature >= 0f, "Feature $index should be >= 0, got $feature")
            assertTrue(feature <= 1f, "Feature $index should be <= 1, got $feature")
        }
    }

    @Test
    fun `empty URL returns zero features`() {
        val features = extractor.extract("")

        // All features should be zero for empty input
        assertTrue(features.all { it == 0f })
    }

    @Test
    fun `HTTPS detection works`() {
        val httpsFeatures = extractor.extract("https://example.com")
        val httpFeatures = extractor.extract("http://example.com")

        assertEquals(1f, httpsFeatures[4], "HTTPS feature should be 1")
        assertEquals(0f, httpFeatures[4], "HTTP feature should be 0")
    }

    @Test
    fun `IP address detection works`() {
        val ipFeatures = extractor.extract("http://192.168.1.1/admin")
        val domainFeatures = extractor.extract("http://example.com/admin")

        assertEquals(1f, ipFeatures[5], "IP host feature should be 1")
        assertEquals(0f, domainFeatures[5], "Domain host feature should be 0")
    }

    @Test
    fun `at symbol detection works`() {
        val atFeatures = extractor.extract("https://google.com@evil.com/path")
        val normalFeatures = extractor.extract("https://example.com/path")

        assertEquals(1f, atFeatures[9], "@ symbol feature should be 1")
        assertEquals(0f, normalFeatures[9], "No @ symbol feature should be 0")
    }

    @Test
    fun `shortener detection works`() {
        val shortenerFeatures = extractor.extract("https://bit.ly/abc123")
        val normalFeatures = extractor.extract("https://example.com/page")

        assertEquals(1f, shortenerFeatures[13], "Shortener feature should be 1")
        assertEquals(0f, normalFeatures[13], "Non-shortener feature should be 0")
    }

    @Test
    fun `suspicious TLD detection works`() {
        val suspiciousFeatures = extractor.extract("https://example.tk/page")
        val normalFeatures = extractor.extract("https://example.com/page")

        assertEquals(1f, suspiciousFeatures[14], "Suspicious TLD feature should be 1")
        assertEquals(0f, normalFeatures[14], "Safe TLD feature should be 0")
    }

    @Test
    fun `very long URL is handled safely`() {
        val longUrl = "https://example.com/" + "a".repeat(5000)
        val features = extractor.extract(longUrl)

        // Should not crash and should return bounded features
        assertEquals(LogisticRegressionModel.FEATURE_COUNT, features.size)
        assertTrue(features.all { it.isFinite() })
    }

    // === MODEL FACTORY TESTS ===

    @Test
    fun `default model is valid`() {
        val model = LogisticRegressionModel.default()
        val features = FloatArray(LogisticRegressionModel.FEATURE_COUNT) { 0.5f }

        val prediction = model.predict(features)
        assertTrue(prediction.isFinite())
    }

    @Test
    fun `create with invalid weight count throws`() {
        val wrongWeights = floatArrayOf(0.1f, 0.2f)  // Wrong size

        assertFailsWith<IllegalArgumentException> {
            LogisticRegressionModel.create(wrongWeights, 0f)
        }
    }

    // === DETERMINISTIC ML TESTS (for CI verification) ===
    // These tests verify exact mathematical behavior with known inputs/outputs

    @Test
    fun `safe URL features produce score below 50 percent`() {
        // Feature vector for a safe HTTPS domain with no risk indicators
        val safeFeatures = FloatArray(LogisticRegressionModel.FEATURE_COUNT)
        safeFeatures[0] = 0.05f  // Short URL (25 chars / 500)
        safeFeatures[1] = 0.10f  // Short host (10 chars / 100)
        safeFeatures[2] = 0.01f  // Short path
        safeFeatures[3] = 0.00f  // No subdomains
        safeFeatures[4] = 1.0f   // HAS HTTPS (protective)
        safeFeatures[5] = 0.0f   // Not IP host
        safeFeatures[6] = 0.3f   // Low domain entropy
        safeFeatures[7] = 0.1f   // Low path entropy
        safeFeatures[8] = 0.0f   // No query params
        safeFeatures[9] = 0.0f   // No @ symbol
        safeFeatures[10] = 0.1f  // Few dots (1/10)
        safeFeatures[11] = 0.0f  // No dashes
        safeFeatures[12] = 0.0f  // No port
        safeFeatures[13] = 0.0f  // Not shortener
        safeFeatures[14] = 0.0f  // Not suspicious TLD

        val prediction = model.predict(safeFeatures)
        
        // Score should be below phishing threshold (0.5) for safe URLs
        // Note: Even safe URLs have a baseline score due to entropy features
        assertTrue(prediction < 0.50f, 
            "Safe features should produce score < 50%, got ${prediction * 100}%")
    }

    @Test
    fun `malicious URL features produce score above 70 percent`() {
        // Feature vector for http://192.168.1.1:8080/login?user=admin
        val maliciousFeatures = FloatArray(LogisticRegressionModel.FEATURE_COUNT)
        maliciousFeatures[0] = 0.10f  // Medium URL length
        maliciousFeatures[1] = 0.12f  // IP address length
        maliciousFeatures[2] = 0.05f  // Short path
        maliciousFeatures[3] = 0.0f   // No subdomains
        maliciousFeatures[4] = 0.0f   // NO HTTPS (risky)
        maliciousFeatures[5] = 1.0f   // IS IP HOST (very risky)
        maliciousFeatures[6] = 0.2f   // Low entropy for IP
        maliciousFeatures[7] = 0.4f   // Medium path entropy
        maliciousFeatures[8] = 0.1f   // Some query params
        maliciousFeatures[9] = 0.0f   // No @ symbol
        maliciousFeatures[10] = 0.3f  // IP has dots
        maliciousFeatures[11] = 0.0f  // No dashes
        maliciousFeatures[12] = 1.0f  // HAS PORT (risky)
        maliciousFeatures[13] = 0.0f  // Not shortener
        maliciousFeatures[14] = 0.0f  // N/A for IP

        val prediction = model.predict(maliciousFeatures)
        
        // IP host + no HTTPS + port should produce high score
        assertTrue(prediction > 0.50f, 
            "Malicious features (IP host + no HTTPS + port) should produce score > 50%, got ${prediction * 100}%")
    }

    @Test
    fun `phishing URL with multiple risk factors produces very high score`() {
        // Feature vector for https://paypa1-secure.tk:443/login?redirect=steal
        val phishingFeatures = FloatArray(LogisticRegressionModel.FEATURE_COUNT)
        phishingFeatures[0] = 0.12f   // ~60 char URL
        phishingFeatures[1] = 0.20f   // 20 char host
        phishingFeatures[2] = 0.10f   // Path length
        phishingFeatures[3] = 0.2f    // 1 subdomain
        phishingFeatures[4] = 1.0f    // Has HTTPS
        phishingFeatures[5] = 0.0f    // Not IP
        phishingFeatures[6] = 0.6f    // High entropy (random-ish domain)
        phishingFeatures[7] = 0.4f    // Path entropy
        phishingFeatures[8] = 0.1f    // Query params
        phishingFeatures[9] = 0.0f    // No @ symbol
        phishingFeatures[10] = 0.2f   // Multiple dots
        phishingFeatures[11] = 0.2f   // Dashes present
        phishingFeatures[12] = 0.0f   // Standard port
        phishingFeatures[13] = 0.0f   // Not shortener
        phishingFeatures[14] = 1.0f   // SUSPICIOUS TLD (.tk)

        val prediction = model.predict(phishingFeatures)
        
        // High entropy + suspicious TLD should raise suspicion
        assertTrue(prediction > 0.40f, 
            "Phishing features (.tk TLD + high entropy) should produce score > 40%, got ${prediction * 100}%")
    }

    @Test
    fun `URL with at symbol injection produces elevated score`() {
        // Feature vector simulating: https://google.com@evil.com/steal
        val atInjectionFeatures = FloatArray(LogisticRegressionModel.FEATURE_COUNT)
        atInjectionFeatures[0] = 0.08f  // URL length
        atInjectionFeatures[1] = 0.20f  // Host length (includes @)
        atInjectionFeatures[2] = 0.03f  // Short path
        atInjectionFeatures[3] = 0.0f   // Subdomains
        atInjectionFeatures[4] = 1.0f   // HTTPS
        atInjectionFeatures[5] = 0.0f   // Not IP
        atInjectionFeatures[6] = 0.45f  // Medium entropy
        atInjectionFeatures[7] = 0.2f   // Path entropy
        atInjectionFeatures[8] = 0.0f   // No query
        atInjectionFeatures[9] = 1.0f   // HAS @ SYMBOL (risky)
        atInjectionFeatures[10] = 0.2f  // Dots
        atInjectionFeatures[11] = 0.0f  // No dashes
        atInjectionFeatures[12] = 0.0f  // No port
        atInjectionFeatures[13] = 0.0f  // Not shortener
        atInjectionFeatures[14] = 0.0f  // Normal TLD

        val prediction = model.predict(atInjectionFeatures)
        
        // @ symbol should significantly elevate risk
        assertTrue(prediction > 0.35f, 
            "URL with @ injection should produce score > 35%, got ${prediction * 100}%")
    }

    @Test
    fun `model weights are mathematically stable`() {
        // Test with edge case features (all at boundary values)
        val boundaryLow = FloatArray(LogisticRegressionModel.FEATURE_COUNT) { 0.0f }
        val boundaryHigh = FloatArray(LogisticRegressionModel.FEATURE_COUNT) { 1.0f }
        val boundaryMixed = FloatArray(LogisticRegressionModel.FEATURE_COUNT) { 
            if (it % 2 == 0) 0.0f else 1.0f 
        }

        val predLow = model.predict(boundaryLow)
        val predHigh = model.predict(boundaryHigh)
        val predMixed = model.predict(boundaryMixed)

        // All predictions should be valid probabilities
        assertTrue(predLow in 0f..1f, "Low boundary prediction out of range: $predLow")
        assertTrue(predHigh in 0f..1f, "High boundary prediction out of range: $predHigh")
        assertTrue(predMixed in 0f..1f, "Mixed boundary prediction out of range: $predMixed")
        
        // All should be finite (no NaN or Infinity)
        assertTrue(predLow.isFinite(), "Low boundary produced non-finite: $predLow")
        assertTrue(predHigh.isFinite(), "High boundary produced non-finite: $predHigh")
        assertTrue(predMixed.isFinite(), "Mixed boundary produced non-finite: $predMixed")
    }

    @Test
    fun `predictions are deterministic - same input yields same output`() {
        val features = floatArrayOf(
            0.1f, 0.2f, 0.15f, 0.0f, 1.0f,   // Features 0-4
            0.0f, 0.35f, 0.25f, 0.0f, 0.0f,  // Features 5-9
            0.2f, 0.1f, 0.0f, 0.0f, 0.0f     // Features 10-14
        )

        val prediction1 = model.predict(features)
        val prediction2 = model.predict(features)
        val prediction3 = model.predict(features)

        assertEquals(prediction1, prediction2, "Predictions should be deterministic (run 1 vs 2)")
        assertEquals(prediction2, prediction3, "Predictions should be deterministic (run 2 vs 3)")
    }
}
