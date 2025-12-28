/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.evaluation

import com.qrshield.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Tests for EvaluationHarness.
 */
class EvaluationHarnessTest {

    @Test
    fun `load corpora populates test cases`() {
        val harness = EvaluationHarness()
        harness.loadCorpora()
        
        val allCases = harness.getAllTestCases()
        assertTrue(allCases.isNotEmpty())
        assertTrue(allCases.size >= 50, "Should have at least 50 test cases")
    }

    @Test
    fun `corpora has balanced classes`() {
        val harness = EvaluationHarness()
        harness.loadCorpora()
        
        val allCases = harness.getAllTestCases()
        val safeCases = allCases.count { it.expectedVerdict == Verdict.SAFE }
        val maliciousCases = allCases.count { it.expectedVerdict == Verdict.MALICIOUS }
        
        assertTrue(safeCases > 10, "Should have benign cases")
        assertTrue(maliciousCases > 10, "Should have phishing cases")
    }

    @Test
    fun `evaluate with mock engine`() {
        val harness = EvaluationHarness()
        harness.loadCorpora()
        
        // Mock engine that always returns SAFE  
        val alwaysSafeEngine = EvaluationHarness.EngineEvaluator { _ ->
            Verdict.SAFE to 0
        }
        
        val metrics = harness.evaluate(alwaysSafeEngine)
        
        assertNotNull(metrics)
        assertTrue(metrics.totalTests > 0)
        // All safe = low precision on malicious, high on benign
        assertTrue(metrics.accuracy >= 0.0 && metrics.accuracy <= 1.0)
    }

    @Test
    fun `evaluate with perfect engine`() {
        val harness = EvaluationHarness()
        harness.loadCorpora()
        
        // Perfect engine that returns exactly what's expected
        val perfectEngine = EvaluationHarness.EngineEvaluator { url ->
            val testCase = harness.getAllTestCases().find { it.url == url }
            val verdict = testCase?.expectedVerdict ?: Verdict.UNKNOWN
            val score = when (verdict) {
                Verdict.MALICIOUS -> 80
                Verdict.SUSPICIOUS -> 50
                Verdict.SAFE -> 10
                Verdict.UNKNOWN -> 30
            }
            verdict to score
        }
        
        val metrics = harness.evaluate(perfectEngine)
        
        // Perfect engine should have ~100% accuracy
        assertEquals(1.0, metrics.accuracy, 0.01)
        assertEquals(1.0, metrics.precision, 0.01)
        assertEquals(1.0, metrics.recall, 0.01)
        assertEquals(1.0, metrics.f1Score, 0.01)
    }

    @Test
    fun `confusion matrix is correct`() {
        val harness = EvaluationHarness()
        harness.loadCorpora()
        
        // Engine that detects half correctly
        var counter = 0
        val halfCorrectEngine = EvaluationHarness.EngineEvaluator { url ->
            counter++
            if (counter % 2 == 0) {
                val testCase = harness.getAllTestCases().find { it.url == url }
                (testCase?.expectedVerdict ?: Verdict.UNKNOWN) to 50
            } else {
                Verdict.SAFE to 0
            }
        }
        
        val metrics = harness.evaluate(halfCorrectEngine)
        
        val cm = metrics.confusionMatrix
        assertEquals(metrics.totalTests, cm.total)
        assertTrue(cm.truePositive >= 0)
        assertTrue(cm.falsePositive >= 0)
        assertTrue(cm.trueNegative >= 0)
        assertTrue(cm.falseNegative >= 0)
    }

    @Test
    fun `runtime is measured`() {
        val harness = EvaluationHarness()
        harness.loadCorpora()
        
        val engine = EvaluationHarness.EngineEvaluator { _ ->
            Verdict.SAFE to 0
        }
        
        val metrics = harness.evaluate(engine)
        
        assertTrue(metrics.averageRuntimeMs >= 0.0)
        assertTrue(metrics.minRuntimeMs >= 0)
        assertTrue(metrics.maxRuntimeMs >= metrics.minRuntimeMs)
    }

    @Test
    fun `summary produces readable output`() {
        val harness = EvaluationHarness()
        harness.loadCorpora()
        
        val engine = EvaluationHarness.EngineEvaluator { _ ->
            Verdict.SAFE to 0
        }
        
        val metrics = harness.evaluate(engine)
        val summary = metrics.summary()
        
        assertTrue(summary.contains("Evaluation Results"))
        assertTrue(summary.contains("Precision"))
        assertTrue(summary.contains("Recall"))
        assertTrue(summary.contains("F1 Score"))
        assertTrue(summary.contains("Confusion Matrix"))
    }

    @Test
    fun `edge cases include unicode and punycode`() {
        val allCases = EvaluationHarness.EDGE_CASES
        
        val idnCases = allCases.filter { it.category == "idn" || it.category == "idn-legit" }
        val mixedScriptCases = allCases.filter { it.category == "mixed-script" }
        val zeroWidthCases = allCases.filter { it.category == "zero-width" }
        
        assertTrue(idnCases.isNotEmpty(), "Should have IDN test cases")
        assertTrue(mixedScriptCases.isNotEmpty(), "Should have mixed-script test cases")
        assertTrue(zeroWidthCases.isNotEmpty(), "Should have zero-width test cases")
    }
}
