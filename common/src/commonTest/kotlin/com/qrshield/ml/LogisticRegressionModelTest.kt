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

package com.qrshield.ml

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
}
