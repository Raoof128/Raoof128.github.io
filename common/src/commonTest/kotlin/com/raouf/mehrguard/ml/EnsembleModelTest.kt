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

/**
 * Tests for EnsembleModel - Advanced ML architecture.
 */
class EnsembleModelTest {

    private val model = EnsembleModel.default()

    // ========================================
    // Basic Prediction Tests
    // ========================================

    @Test
    fun testDefaultModelCreation() {
        val prediction = model.predict(createNeutralFeatures())
        assertTrue(prediction.probability in 0f..1f, "Probability should be in [0, 1]")
    }

    @Test
    fun testSafeUrlPrediction() {
        // Features representing a safe URL (HTTPS, no suspicious indicators)
        val safeFeatures = floatArrayOf(
            0.3f,  // urlLength - normal
            0.2f,  // hostLength - normal
            0.3f,  // pathLength - normal
            0.1f,  // subdomainCount - low
            1.0f,  // hasHttps - YES
            0.0f,  // hasIpHost - no
            0.3f,  // domainEntropy - low
            0.3f,  // pathEntropy - low
            0.2f,  // queryParamCount - normal
            0.0f,  // hasAtSymbol - no
            0.2f,  // numDots - normal
            0.1f,  // numDashes - normal
            0.0f,  // hasPortNumber - no
            0.0f,  // shortenerDomain - no
            0.0f   // suspiciousTld - no
        )

        val prediction = model.predict(safeFeatures)
        // Ensemble may produce slightly higher scores but should still be below phishing threshold
        assertTrue(prediction.probability < 0.55f, "Safe URL should have low-medium probability: ${prediction.probability}")
        assertTrue(prediction.riskLevel in listOf("Low", "Medium"), "Safe URL should have Low or Medium risk")
    }

    @Test
    fun testPhishingUrlPrediction() {
        // Features representing a phishing URL (IP host, @ symbol, suspicious TLD)
        val phishingFeatures = floatArrayOf(
            0.8f,  // urlLength - long
            0.4f,  // hostLength - medium
            0.5f,  // pathLength - medium
            0.6f,  // subdomainCount - high
            0.0f,  // hasHttps - NO
            1.0f,  // hasIpHost - YES (critical)
            0.8f,  // domainEntropy - high
            0.6f,  // pathEntropy - medium
            0.4f,  // queryParamCount - high
            1.0f,  // hasAtSymbol - YES (critical)
            0.6f,  // numDots - high
            0.3f,  // numDashes - medium
            1.0f,  // hasPortNumber - yes
            0.0f,  // shortenerDomain - no
            1.0f   // suspiciousTld - YES
        )

        val prediction = model.predict(phishingFeatures)
        assertTrue(prediction.probability > 0.5f, "Phishing URL should have high probability: ${prediction.probability}")
        assertTrue(prediction.isPhishing, "Phishing URL should be marked as phishing")
    }

    @Test
    fun testIpHostNoHttpsRule() {
        // Specifically trigger the IP + no HTTPS decision stump
        val features = createNeutralFeatures().also {
            it[4] = 0.0f  // No HTTPS
            it[5] = 1.0f  // IP host
        }

        val prediction = model.predict(features)
        assertTrue(prediction.probability > 0.5f, "IP + no HTTPS should trigger high risk")
    }

    @Test
    fun testAtSymbolInjectionRule() {
        // Trigger @ symbol decision stump
        val features = createNeutralFeatures().also {
            it[9] = 1.0f  // @ symbol present
        }

        val prediction = model.predict(features)
        assertTrue(prediction.probability > 0.4f, "@ symbol should increase risk")
    }

    // ========================================
    // Ensemble Component Tests
    // ========================================

    @Test
    fun testModelAgreementWithClearPhishing() {
        val extremePhishing = floatArrayOf(
            0.9f, 0.5f, 0.6f, 0.7f, 0.0f, 1.0f, 0.9f, 0.7f,
            0.5f, 1.0f, 0.7f, 0.4f, 1.0f, 1.0f, 1.0f
        )

        val prediction = model.predict(extremePhishing)
        // With such clear signals, models should mostly agree
        assertTrue(prediction.modelAgreement > 0.3f, "Clear phishing should have moderate agreement")
    }

    @Test
    fun testDominantModelIdentification() {
        val prediction = model.predict(createNeutralFeatures())
        assertTrue(
            prediction.dominantModel in listOf(
                "Logistic Regression",
                "Gradient Boosting",
                "Decision Rules",
                "Ensemble Average"
            ),
            "Dominant model should be one of the known types"
        )
    }

    @Test
    fun testConfidenceCalculation() {
        // Extreme prediction should have high confidence
        val extremePhishing = floatArrayOf(
            0.9f, 0.5f, 0.6f, 0.7f, 0.0f, 1.0f, 0.9f, 0.7f,
            0.5f, 1.0f, 0.7f, 0.4f, 1.0f, 1.0f, 1.0f
        )
        val phishingPrediction = model.predict(extremePhishing)

        // Neutral features should have lower confidence
        val neutralPrediction = model.predict(createNeutralFeatures())

        assertTrue(
            phishingPrediction.confidence >= neutralPrediction.confidence,
            "Extreme prediction should have higher or equal confidence"
        )
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    fun testInvalidFeatureSize() {
        val wrongSize = floatArrayOf(0.5f, 0.5f, 0.5f)  // Only 3 features
        val prediction = model.predict(wrongSize)
        assertEquals(0.5f, prediction.probability, "Invalid input should return neutral prediction")
        assertEquals(0f, prediction.confidence, "Invalid input should have zero confidence")
    }

    @Test
    fun testAllZeroFeatures() {
        val allZero = FloatArray(15) { 0f }
        val prediction = model.predict(allZero)
        assertTrue(prediction.probability in 0f..1f, "All zero features should produce valid prediction")
    }

    @Test
    fun testAllOneFeatures() {
        val allOne = FloatArray(15) { 1f }
        val prediction = model.predict(allOne)
        assertTrue(prediction.probability in 0f..1f, "All one features should produce valid prediction")
        assertTrue(prediction.probability > 0.5f, "All high-risk features should indicate phishing")
    }

    @Test
    fun testDeterminism() {
        val features = createNeutralFeatures()
        val predictions = (1..10).map { model.predict(features) }
        
        // All predictions should be identical
        predictions.forEach {
            assertEquals(predictions[0].probability, it.probability, "Predictions should be deterministic")
        }
    }

    // ========================================
    // Component Score Tests
    // ========================================

    @Test
    fun testLogisticScoreReturned() {
        val prediction = model.predict(createNeutralFeatures())
        assertTrue(prediction.logisticScore in 0f..1f, "Logistic score should be in [0, 1]")
    }

    @Test
    fun testBoostingScoreReturned() {
        val prediction = model.predict(createNeutralFeatures())
        // Boosting score can be negative or positive (it's a sum of adjustments)
        assertTrue(prediction.boostingScore in -5f..5f, "Boosting score should be reasonable")
    }

    @Test
    fun testStumpScoreReturned() {
        val prediction = model.predict(createNeutralFeatures())
        // Stump score is sum of triggered rules
        assertTrue(prediction.stumpScore in -2f..2f, "Stump score should be reasonable")
    }

    // ========================================
    // Helper Methods
    // ========================================

    private fun createNeutralFeatures(): FloatArray = floatArrayOf(
        0.5f,  // urlLength
        0.5f,  // hostLength
        0.5f,  // pathLength
        0.3f,  // subdomainCount
        1.0f,  // hasHttps (safe default)
        0.0f,  // hasIpHost
        0.5f,  // domainEntropy
        0.5f,  // pathEntropy
        0.3f,  // queryParamCount
        0.0f,  // hasAtSymbol
        0.3f,  // numDots
        0.2f,  // numDashes
        0.0f,  // hasPortNumber
        0.0f,  // shortenerDomain
        0.0f   // suspiciousTld
    )
}
