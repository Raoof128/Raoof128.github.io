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

package com.qrshield.benchmark

import com.qrshield.core.PhishingEngine
import com.qrshield.engine.BrandDetector
import com.qrshield.engine.HeuristicsEngine
import com.qrshield.engine.TldScorer
import com.qrshield.ml.FeatureExtractor
import com.qrshield.ml.LogisticRegressionModel
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Performance benchmarks for QR-SHIELD analysis engines.
 *
 * These tests measure execution time for critical operations:
 * - URL analysis speed (full pipeline)
 * - Heuristics engine throughput
 * - ML inference time
 * - Brand detection throughput
 *
 * ## Running Benchmarks
 * ```bash
 * ./gradlew :common:allTests --tests "*Benchmark*"
 * ```
 *
 * ## Performance Targets
 * - Full URL analysis: < 50ms per URL
 * - Heuristics: < 10ms per URL
 * - ML inference: < 5ms per URL
 * - Brand detection: < 15ms per URL
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class PerformanceBenchmarkTest {

    companion object {
        /** Number of iterations for timing accuracy */
        private const val ITERATIONS = 100

        /** Warmup iterations to JIT-compile code paths */
        private const val WARMUP_ITERATIONS = 10

        /** Performance targets in milliseconds */
        private const val TARGET_FULL_ANALYSIS_MS = 50.0
        private const val TARGET_HEURISTICS_MS = 10.0
        private const val TARGET_ML_INFERENCE_MS = 5.0
        private const val TARGET_BRAND_DETECTION_MS = 15.0

        /** Test URLs covering various scenarios */
        private val TEST_URLS = listOf(
            "https://www.google.com/search?q=kotlin",
            "https://paypa1-secure.tk/login/verify",
            "http://192.168.1.1/admin/console.php",
            "https://login.microsoft.com.secure-verify.ml/auth",
            "https://bit.ly/3xYz123",
            "https://www.amazon.com/dp/B09V3KXJPB",
            "http://xn--pypal-4ve.com/signin",
            "https://applе.com/account",
            "https://secure-bank.suspicious-domain.tk/verify.php?token=abc123&user=test@email.com",
            "https://github.com/Raoof128/qrshield"
        )
    }

    // ==========================================================================
    // FULL URL ANALYSIS BENCHMARK
    // ==========================================================================

    @Test
    fun benchmarkFullUrlAnalysis() {
        val engine = PhishingEngine()

        // Warmup
        repeat(WARMUP_ITERATIONS) {
            TEST_URLS.forEach { url -> engine.analyzeBlocking(url) }
        }

        // Benchmark
        val startTime = currentTimeMillis()
        repeat(ITERATIONS) {
            TEST_URLS.forEach { url -> engine.analyzeBlocking(url) }
        }
        val endTime = currentTimeMillis()

        val totalOperations = ITERATIONS * TEST_URLS.size
        val totalTimeMs = endTime - startTime
        val avgTimePerUrl = totalTimeMs.toDouble() / totalOperations

        printBenchmarkResult(
            name = "Full URL Analysis",
            operations = totalOperations,
            totalTimeMs = totalTimeMs,
            avgTimeMs = avgTimePerUrl,
            targetMs = TARGET_FULL_ANALYSIS_MS
        )

        assertTrue(
            avgTimePerUrl < TARGET_FULL_ANALYSIS_MS,
            "Full analysis took ${avgTimePerUrl}ms, target is ${TARGET_FULL_ANALYSIS_MS}ms"
        )
    }

    // ==========================================================================
    // HEURISTICS ENGINE BENCHMARK
    // ==========================================================================

    @Test
    fun benchmarkHeuristicsEngine() {
        val engine = HeuristicsEngine()

        // Warmup
        repeat(WARMUP_ITERATIONS) {
            TEST_URLS.forEach { url -> engine.analyze(url) }
        }

        // Benchmark
        val startTime = currentTimeMillis()
        repeat(ITERATIONS) {
            TEST_URLS.forEach { url -> engine.analyze(url) }
        }
        val endTime = currentTimeMillis()

        val totalOperations = ITERATIONS * TEST_URLS.size
        val totalTimeMs = endTime - startTime
        val avgTimePerUrl = totalTimeMs.toDouble() / totalOperations

        printBenchmarkResult(
            name = "Heuristics Engine",
            operations = totalOperations,
            totalTimeMs = totalTimeMs,
            avgTimeMs = avgTimePerUrl,
            targetMs = TARGET_HEURISTICS_MS
        )

        assertTrue(
            avgTimePerUrl < TARGET_HEURISTICS_MS,
            "Heuristics took ${avgTimePerUrl}ms, target is ${TARGET_HEURISTICS_MS}ms"
        )
    }

    // ==========================================================================
    // ML INFERENCE BENCHMARK
    // ==========================================================================

    @Test
    fun benchmarkMlInference() {
        val featureExtractor = FeatureExtractor()
        val mlModel = LogisticRegressionModel.default()

        // Pre-extract features
        val featureSets = TEST_URLS.map { featureExtractor.extract(it) }

        // Warmup
        repeat(WARMUP_ITERATIONS) {
            featureSets.forEach { features -> mlModel.predict(features) }
        }

        // Benchmark (inference only, not feature extraction)
        val startTime = currentTimeMillis()
        repeat(ITERATIONS) {
            featureSets.forEach { features -> mlModel.predict(features) }
        }
        val endTime = currentTimeMillis()

        val totalOperations = ITERATIONS * featureSets.size
        val totalTimeMs = endTime - startTime
        val avgTimePerInference = totalTimeMs.toDouble() / totalOperations

        printBenchmarkResult(
            name = "ML Inference",
            operations = totalOperations,
            totalTimeMs = totalTimeMs,
            avgTimeMs = avgTimePerInference,
            targetMs = TARGET_ML_INFERENCE_MS
        )

        assertTrue(
            avgTimePerInference < TARGET_ML_INFERENCE_MS,
            "ML inference took ${avgTimePerInference}ms, target is ${TARGET_ML_INFERENCE_MS}ms"
        )
    }

    // ==========================================================================
    // BRAND DETECTION BENCHMARK
    // ==========================================================================

    @Test
    fun benchmarkBrandDetection() {
        val detector = BrandDetector()

        // Warmup
        repeat(WARMUP_ITERATIONS) {
            TEST_URLS.forEach { url -> detector.detect(url) }
        }

        // Benchmark
        val startTime = currentTimeMillis()
        repeat(ITERATIONS) {
            TEST_URLS.forEach { url -> detector.detect(url) }
        }
        val endTime = currentTimeMillis()

        val totalOperations = ITERATIONS * TEST_URLS.size
        val totalTimeMs = endTime - startTime
        val avgTimePerUrl = totalTimeMs.toDouble() / totalOperations

        printBenchmarkResult(
            name = "Brand Detection",
            operations = totalOperations,
            totalTimeMs = totalTimeMs,
            avgTimeMs = avgTimePerUrl,
            targetMs = TARGET_BRAND_DETECTION_MS
        )

        assertTrue(
            avgTimePerUrl < TARGET_BRAND_DETECTION_MS,
            "Brand detection took ${avgTimePerUrl}ms, target is ${TARGET_BRAND_DETECTION_MS}ms"
        )
    }

    // ==========================================================================
    // TLD SCORING BENCHMARK
    // ==========================================================================

    @Test
    fun benchmarkTldScoring() {
        val scorer = TldScorer()

        // Warmup
        repeat(WARMUP_ITERATIONS) {
            TEST_URLS.forEach { url -> scorer.score(url) }
        }

        // Benchmark
        val startTime = currentTimeMillis()
        repeat(ITERATIONS) {
            TEST_URLS.forEach { url -> scorer.score(url) }
        }
        val endTime = currentTimeMillis()

        val totalOperations = ITERATIONS * TEST_URLS.size
        val totalTimeMs = endTime - startTime
        val avgTimePerUrl = totalTimeMs.toDouble() / totalOperations

        printBenchmarkResult(
            name = "TLD Scoring",
            operations = totalOperations,
            totalTimeMs = totalTimeMs,
            avgTimeMs = avgTimePerUrl,
            targetMs = 5.0
        )

        assertTrue(
            avgTimePerUrl < 5.0,
            "TLD scoring took ${avgTimePerUrl}ms, target is 5.0ms"
        )
    }

    // ==========================================================================
    // THROUGHPUT BENCHMARK
    // ==========================================================================

    @Test
    fun benchmarkAnalysisThroughput() {
        val engine = PhishingEngine()
        val testDurationMs = 5000L // 5 seconds

        // Warmup
        repeat(WARMUP_ITERATIONS) {
            TEST_URLS.forEach { url -> engine.analyzeBlocking(url) }
        }

        // Measure throughput
        var operationCount = 0
        val startTime = currentTimeMillis()
        val endTimeTarget = startTime + testDurationMs

        while (currentTimeMillis() < endTimeTarget) {
            TEST_URLS.forEach { url ->
                engine.analyzeBlocking(url)
                operationCount++
            }
        }

        val actualDurationMs = currentTimeMillis() - startTime
        val throughput = (operationCount.toDouble() / actualDurationMs) * 1000

        println("╔══════════════════════════════════════════════════════════╗")
        println("║              THROUGHPUT BENCHMARK RESULTS                ║")
        println("╠══════════════════════════════════════════════════════════╣")
        println("║ Duration:     ${actualDurationMs}ms                              ")
        println("║ Operations:   $operationCount URLs analyzed                      ")
        println("║ Throughput:   ${formatDouble(throughput, 1)} URLs/second               ")
        println("╚══════════════════════════════════════════════════════════╝")

        // Should be able to analyze at least 100 URLs per second
        assertTrue(
            throughput >= 100,
            "Throughput was ${formatDouble(throughput, 1)} URLs/s, minimum is 100 URLs/s"
        )
    }

    // ==========================================================================
    // HELPERS
    // ==========================================================================

    private fun printBenchmarkResult(
        name: String,
        operations: Int,
        totalTimeMs: Long,
        avgTimeMs: Double,
        targetMs: Double
    ) {
        val passed = avgTimeMs < targetMs
        val status = if (passed) "✅ PASS" else "❌ FAIL"

        println("╔══════════════════════════════════════════════════════════╗")
        println("║ Benchmark: $name")
        println("╠══════════════════════════════════════════════════════════╣")
        println("║ Operations:   $operations")
        println("║ Total Time:   ${totalTimeMs}ms")
        println("║ Avg Time:     ${formatDouble(avgTimeMs, 3)}ms")
        println("║ Target:       ${targetMs}ms")
        println("║ Status:       $status")
        println("╚══════════════════════════════════════════════════════════╝")
    }

    /**
     * Cross-platform time measurement.
     * Uses kotlin.system.measureTimeMillis equivalent.
     */
    private fun currentTimeMillis(): Long {
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }

    /**
     * Cross-platform double formatting.
     * Formats a double to a specified number of decimal places.
     */
    private fun formatDouble(value: Double, decimals: Int): String {
        var factor = 1.0
        repeat(decimals) { factor *= 10 }
        val rounded = kotlin.math.round(value * factor) / factor
        val parts = rounded.toString().split(".")
        return if (parts.size == 1) {
            parts[0] + "." + "0".repeat(decimals)
        } else {
            val decPart = parts[1].take(decimals).padEnd(decimals, '0')
            parts[0] + "." + decPart
        }
    }
}
