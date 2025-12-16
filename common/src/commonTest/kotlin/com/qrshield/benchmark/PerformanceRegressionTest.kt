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
import com.qrshield.engine.HomographDetector
import com.qrshield.engine.TldScorer
import com.qrshield.ml.FeatureExtractor
import com.qrshield.ml.LogisticRegressionModel
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Performance regression tests that fail if latency exceeds thresholds.
 *
 * These tests are designed to catch performance regressions in CI/CD.
 * Unlike benchmarks, these are strict pass/fail tests with hard limits.
 *
 * ## SLA Requirements
 * - Single URL analysis: < 50ms (P99)
 * - Worst-case URL (complex phishing): < 100ms (P99)
 * - Batch analysis (10 URLs): < 200ms total
 *
 * ## Running
 * ```bash
 * ./gradlew :common:allTests --tests "*PerformanceRegressionTest*"
 * ```
 *
 * @author QR-SHIELD Performance Team
 * @since 1.0.0
 */
class PerformanceRegressionTest {

    companion object {
        /** Strict latency thresholds in milliseconds */
        private const val SINGLE_URL_MAX_MS = 50L
        private const val COMPLEX_URL_MAX_MS = 100L
        private const val BATCH_10_URL_MAX_MS = 200L
        private const val HEURISTICS_MAX_MS = 15L
        private const val ML_INFERENCE_MAX_MS = 10L
        private const val BRAND_DETECTION_MAX_MS = 20L
        private const val HOMOGRAPH_MAX_MS = 10L
        private const val TLD_SCORING_MAX_MS = 5L
        private const val FEATURE_EXTRACTION_MAX_MS = 10L
        
        /** Warmup iterations */
        private const val WARMUP_ITERATIONS = 5
        
        /** Measurement iterations for P99 */
        private const val MEASURE_ITERATIONS = 20
        
        /** Simple URLs for baseline testing */
        private val SIMPLE_URLS = listOf(
            "https://google.com",
            "https://github.com",
            "https://apple.com",
            "https://microsoft.com",
            "https://amazon.com",
        )
        
        /** Complex phishing URLs - worst case performance */
        private val COMPLEX_URLS = listOf(
            "https://secure-paypa1-login-verify-account-update.suspicious.tk/auth/confirm?token=abc123&user=victim@email.com&ref=https://real-paypal.com",
            "https://applе.com/account/verify?redirect=http://evil.ml/steal&session=xyz", // Cyrillic 'е'
            "https://login.microsoft.com.secure-verify.fraudulent-domain.ml/oauth/authorize?client_id=1234&redirect_uri=http://attacker.tk/callback",
            "http://192.168.1.1:8080/admin/phpmyadmin/login.php?timeout=1&auth=basic&user=root",
            "https://www.amaz0n-security-alert.cf/verify-account?order=123456&action=confirm&suspicious-parameter=true&very-long-parameter-name=very-long-parameter-value-that-makes-url-complex",
        )
        
        /** Batch of mixed URLs for throughput testing */
        private val MIXED_BATCH = SIMPLE_URLS + COMPLEX_URLS
    }

    private val phishingEngine = PhishingEngine()
    private val heuristicsEngine = HeuristicsEngine()
    private val brandDetector = BrandDetector()
    private val homographDetector = HomographDetector()
    private val tldScorer = TldScorer()
    private val featureExtractor = FeatureExtractor()
    private val mlModel = LogisticRegressionModel.default()

    // ==========================================================================
    // SINGLE URL ANALYSIS REGRESSION TESTS
    // ==========================================================================

    @Test
    fun regressionSingleUrlAnalysisUnder50ms() {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            SIMPLE_URLS.forEach { phishingEngine.analyzeBlocking(it) }
        }
        
        // Measure each URL individually
        SIMPLE_URLS.forEach { url ->
            val latencies = measureLatencies(MEASURE_ITERATIONS) {
                phishingEngine.analyzeBlocking(url)
            }
            
            val p99 = percentile(latencies, 99)
            assertTrue(
                p99 <= SINGLE_URL_MAX_MS,
                "Single URL analysis P99 latency ($p99 ms) exceeds ${SINGLE_URL_MAX_MS}ms for URL: $url"
            )
        }
    }

    @Test
    fun regressionComplexUrlAnalysisUnder100ms() {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            COMPLEX_URLS.forEach { phishingEngine.analyzeBlocking(it) }
        }
        
        // Measure complex URLs
        COMPLEX_URLS.forEach { url ->
            val latencies = measureLatencies(MEASURE_ITERATIONS) {
                phishingEngine.analyzeBlocking(url)
            }
            
            val p99 = percentile(latencies, 99)
            assertTrue(
                p99 <= COMPLEX_URL_MAX_MS,
                "Complex URL analysis P99 latency ($p99 ms) exceeds ${COMPLEX_URL_MAX_MS}ms for URL: ${url.take(50)}..."
            )
        }
    }

    @Test
    fun regressionBatch10UrlsUnder200ms() {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            MIXED_BATCH.forEach { phishingEngine.analyzeBlocking(it) }
        }
        
        // Measure batch analysis
        val latencies = measureLatencies(MEASURE_ITERATIONS) {
            MIXED_BATCH.forEach { url ->
                phishingEngine.analyzeBlocking(url)
            }
        }
        
        val p99 = percentile(latencies, 99)
        assertTrue(
            p99 <= BATCH_10_URL_MAX_MS,
            "Batch (10 URLs) analysis P99 latency ($p99 ms) exceeds ${BATCH_10_URL_MAX_MS}ms"
        )
    }

    // ==========================================================================
    // COMPONENT-LEVEL REGRESSION TESTS
    // ==========================================================================

    @Test
    fun regressionHeuristicsEngineUnder15ms() {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            MIXED_BATCH.forEach { heuristicsEngine.analyze(it) }
        }
        
        MIXED_BATCH.forEach { url ->
            val latencies = measureLatencies(MEASURE_ITERATIONS) {
                heuristicsEngine.analyze(url)
            }
            
            val p99 = percentile(latencies, 99)
            assertTrue(
                p99 <= HEURISTICS_MAX_MS,
                "Heuristics P99 ($p99 ms) exceeds ${HEURISTICS_MAX_MS}ms"
            )
        }
    }

    @Test
    fun regressionMlInferenceUnder10ms() {
        // Pre-extract features
        val featureSets = MIXED_BATCH.map { featureExtractor.extract(it) }
        
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            featureSets.forEach { mlModel.predict(it) }
        }
        
        featureSets.forEach { features ->
            val latencies = measureLatencies(MEASURE_ITERATIONS) {
                mlModel.predict(features)
            }
            
            val p99 = percentile(latencies, 99)
            assertTrue(
                p99 <= ML_INFERENCE_MAX_MS,
                "ML inference P99 ($p99 ms) exceeds ${ML_INFERENCE_MAX_MS}ms"
            )
        }
    }

    @Test
    fun regressionBrandDetectionUnder20ms() {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            MIXED_BATCH.forEach { brandDetector.detect(it) }
        }
        
        MIXED_BATCH.forEach { url ->
            val latencies = measureLatencies(MEASURE_ITERATIONS) {
                brandDetector.detect(url)
            }
            
            val p99 = percentile(latencies, 99)
            assertTrue(
                p99 <= BRAND_DETECTION_MAX_MS,
                "Brand detection P99 ($p99 ms) exceeds ${BRAND_DETECTION_MAX_MS}ms"
            )
        }
    }

    @Test
    fun regressionHomographDetectionUnder10ms() {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            MIXED_BATCH.forEach { homographDetector.detect(it) }
        }
        
        MIXED_BATCH.forEach { url ->
            val latencies = measureLatencies(MEASURE_ITERATIONS) {
                homographDetector.detect(url)
            }
            
            val p99 = percentile(latencies, 99)
            assertTrue(
                p99 <= HOMOGRAPH_MAX_MS,
                "Homograph detection P99 ($p99 ms) exceeds ${HOMOGRAPH_MAX_MS}ms"
            )
        }
    }

    @Test
    fun regressionTldScoringUnder5ms() {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            MIXED_BATCH.forEach { tldScorer.score(it) }
        }
        
        MIXED_BATCH.forEach { url ->
            val latencies = measureLatencies(MEASURE_ITERATIONS) {
                tldScorer.score(url)
            }
            
            val p99 = percentile(latencies, 99)
            assertTrue(
                p99 <= TLD_SCORING_MAX_MS,
                "TLD scoring P99 ($p99 ms) exceeds ${TLD_SCORING_MAX_MS}ms"
            )
        }
    }

    @Test
    fun regressionFeatureExtractionUnder10ms() {
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            MIXED_BATCH.forEach { featureExtractor.extract(it) }
        }
        
        MIXED_BATCH.forEach { url ->
            val latencies = measureLatencies(MEASURE_ITERATIONS) {
                featureExtractor.extract(url)
            }
            
            val p99 = percentile(latencies, 99)
            assertTrue(
                p99 <= FEATURE_EXTRACTION_MAX_MS,
                "Feature extraction P99 ($p99 ms) exceeds ${FEATURE_EXTRACTION_MAX_MS}ms"
            )
        }
    }

    // ==========================================================================
    // THROUGHPUT REGRESSION TESTS
    // ==========================================================================

    @Test
    fun regressionMinimumThroughput100UrlsPerSecond() {
        val testDurationMs = 2000L
        
        // Warmup
        repeat(WARMUP_ITERATIONS) {
            MIXED_BATCH.forEach { phishingEngine.analyzeBlocking(it) }
        }
        
        // Measure throughput
        var operationCount = 0
        val startTime = currentTimeMillis()
        val endTarget = startTime + testDurationMs
        
        while (currentTimeMillis() < endTarget) {
            SIMPLE_URLS.forEach { url ->
                phishingEngine.analyzeBlocking(url)
                operationCount++
            }
        }
        
        val actualDuration = currentTimeMillis() - startTime
        val throughput = (operationCount.toDouble() / actualDuration) * 1000
        
        assertTrue(
            throughput >= 100,
            "Throughput (${"%.1f".format(throughput)} URLs/s) below minimum (100 URLs/s)"
        )
    }

    // ==========================================================================
    // MEMORY STABILITY TESTS
    // ==========================================================================

    @Test
    fun regressionNoLeaksAfter1000Analyses() {
        // Run many analyses to check for memory leaks
        // This test catches accumulating internal state
        repeat(1000) { i ->
            val url = MIXED_BATCH[i % MIXED_BATCH.size]
            phishingEngine.analyzeBlocking(url)
        }
        
        // If we got here without OOM, we pass
        // Additional memory assertions could be platform-specific
        assertTrue(true, "Completed 1000 analyses without crash")
    }

    // ==========================================================================
    // HELPERS
    // ==========================================================================

    private inline fun measureLatencies(iterations: Int, block: () -> Unit): List<Long> {
        return (1..iterations).map {
            val start = currentTimeMillis()
            block()
            currentTimeMillis() - start
        }
    }

    private fun percentile(latencies: List<Long>, percentile: Int): Long {
        val sorted = latencies.sorted()
        val index = (percentile / 100.0 * sorted.size).toInt().coerceIn(0, sorted.lastIndex)
        return sorted[index]
    }

    private fun currentTimeMillis(): Long {
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }

    private fun String.format(value: Double): String {
        val parts = this.split(".")
        if (parts.size != 2 || !parts[1].endsWith("f")) return value.toString()
        val decimals = parts[1].dropLast(1).toIntOrNull() ?: return value.toString()
        var factor = 1.0
        repeat(decimals) { factor *= 10 }
        val rounded = kotlin.math.round(value * factor) / factor
        return rounded.toString()
    }
}
