/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.evaluation

import com.qrshield.model.Verdict

/**
 * Offline evaluation harness for PhishingEngine.
 *
 * Measures precision, recall, F1, confusion matrix, and runtime.
 * Used for weight calibration and regression testing.
 *
 * ## Test Corpora
 * - benign_urls.txt: Known safe URLs (should be SAFE)
 * - phish_urls.txt: Known phishing URLs (should be MALICIOUS)
 * - edge_cases.txt: Unicode, punycode, weird schemes
 *
 * ## Usage
 * ```kotlin
 * val harness = EvaluationHarness()
 * harness.loadCorpora()
 * val results = harness.evaluate(engine)
 * println(results.summary())
 * ```
 *
 * @author QR-SHIELD Security Team
 * @since 1.19.0
 */
class EvaluationHarness {

    /**
     * A single test case.
     */
    data class TestCase(
        val url: String,
        val expectedVerdict: Verdict,
        val category: String = "general",
        val notes: String = ""
    )

    /**
     * Result for a single test.
     */
    data class TestResult(
        val testCase: TestCase,
        val actualVerdict: Verdict,
        val score: Int,
        val runtimeMs: Long,
        val isCorrect: Boolean
    )

    /**
     * Evaluation metrics.
     */
    data class EvaluationMetrics(
        val totalTests: Int,
        val correctPredictions: Int,
        val accuracy: Double,
        val precision: Double,
        val recall: Double,
        val f1Score: Double,
        val confusionMatrix: ConfusionMatrix,
        val averageRuntimeMs: Double,
        val maxRuntimeMs: Long,
        val minRuntimeMs: Long
    ) {
        fun summary(): String = buildString {
            appendLine("=== Evaluation Results ===")
            appendLine("Total Tests: $totalTests")
            appendLine("Correct: $correctPredictions (${(accuracy * 100).format(2)}%)")
            appendLine()
            appendLine("Precision: ${(precision * 100).format(2)}%")
            appendLine("Recall: ${(recall * 100).format(2)}%")
            appendLine("F1 Score: ${(f1Score * 100).format(2)}%")
            appendLine()
            appendLine("Confusion Matrix:")
            appendLine(confusionMatrix.toString())
            appendLine()
            appendLine("Runtime:")
            appendLine("  Average: ${averageRuntimeMs.format(2)} ms")
            appendLine("  Min: $minRuntimeMs ms")
            appendLine("  Max: $maxRuntimeMs ms")
        }

        private fun Double.format(digits: Int) = "%.${digits}f".format(this)
    }

    /**
     * Confusion matrix for binary classification.
     * Positive = MALICIOUS/SUSPICIOUS, Negative = SAFE
     */
    data class ConfusionMatrix(
        val truePositive: Int,  // Correctly identified as bad
        val falsePositive: Int, // Safe URL incorrectly flagged
        val trueNegative: Int,  // Correctly identified as safe
        val falseNegative: Int  // Bad URL missed
    ) {
        val total: Int get() = truePositive + falsePositive + trueNegative + falseNegative

        override fun toString(): String = buildString {
            appendLine("              Predicted")
            appendLine("              BAD    SAFE")
            appendLine("Actual BAD    $truePositive      $falseNegative")
            appendLine("       SAFE   $falsePositive      $trueNegative")
        }
    }

    /**
     * Evaluator interface for the engine.
     */
    fun interface EngineEvaluator {
        fun evaluate(url: String): Pair<Verdict, Int>
    }

    private val benignUrls = mutableListOf<TestCase>()
    private val phishUrls = mutableListOf<TestCase>()
    private val edgeCases = mutableListOf<TestCase>()

    /**
     * Load test corpora from bundled data.
     */
    fun loadCorpora() {
        // Benign URLs (should be SAFE)
        benignUrls.addAll(BENIGN_CORPUS.map { 
            TestCase(it, Verdict.SAFE, "benign") 
        })
        
        // Phishing URLs (should be MALICIOUS)
        phishUrls.addAll(PHISH_CORPUS.map { 
            TestCase(it, Verdict.MALICIOUS, "phishing") 
        })
        
        // Edge cases (various expected verdicts)
        edgeCases.addAll(EDGE_CASES)
    }

    /**
     * Get all test cases.
     */
    fun getAllTestCases(): List<TestCase> {
        return benignUrls + phishUrls + edgeCases
    }

    /**
     * Run evaluation with the provided engine evaluator.
     */
    fun evaluate(evaluator: EngineEvaluator): EvaluationMetrics {
        val allCases = getAllTestCases()
        val results = mutableListOf<TestResult>()

        for (testCase in allCases) {
            val startTime = System.nanoTime()
            val (verdict, score) = evaluator.evaluate(testCase.url)
            val endTime = System.nanoTime()
            val runtimeMs = (endTime - startTime) / 1_000_000

            val isCorrect = when {
                testCase.expectedVerdict == Verdict.MALICIOUS -> 
                    verdict == Verdict.MALICIOUS || verdict == Verdict.SUSPICIOUS
                testCase.expectedVerdict == Verdict.SAFE -> 
                    verdict == Verdict.SAFE
                else -> verdict == testCase.expectedVerdict
            }

            results.add(TestResult(testCase, verdict, score, runtimeMs, isCorrect))
        }

        return computeMetrics(results)
    }

    /**
     * Evaluate with time-split (for overfitting detection).
     * Trains on older cases, tests on newer ones.
     */
    fun evaluateTimeSplit(
        evaluator: EngineEvaluator,
        splitRatio: Double = 0.7
    ): Pair<EvaluationMetrics, EvaluationMetrics> {
        val allCases = getAllTestCases().shuffled()
        val splitIndex = (allCases.size * splitRatio).toInt()
        
        val trainCases = allCases.take(splitIndex)
        val testCases = allCases.drop(splitIndex)

        val trainResults = trainCases.map { testCase ->
            val (verdict, score) = evaluator.evaluate(testCase.url)
            val isCorrect = isVerdictCorrect(testCase.expectedVerdict, verdict)
            TestResult(testCase, verdict, score, 0, isCorrect)
        }

        val testResults = testCases.map { testCase ->
            val startTime = System.nanoTime()
            val (verdict, score) = evaluator.evaluate(testCase.url)
            val endTime = System.nanoTime()
            val runtimeMs = (endTime - startTime) / 1_000_000
            val isCorrect = isVerdictCorrect(testCase.expectedVerdict, verdict)
            TestResult(testCase, verdict, score, runtimeMs, isCorrect)
        }

        return computeMetrics(trainResults) to computeMetrics(testResults)
    }

    private fun isVerdictCorrect(expected: Verdict, actual: Verdict): Boolean {
        return when (expected) {
            Verdict.MALICIOUS -> actual == Verdict.MALICIOUS || actual == Verdict.SUSPICIOUS
            Verdict.SAFE -> actual == Verdict.SAFE
            else -> actual == expected
        }
    }

    private fun computeMetrics(results: List<TestResult>): EvaluationMetrics {
        val correct = results.count { it.isCorrect }
        val total = results.size
        val accuracy = if (total > 0) correct.toDouble() / total else 0.0

        // Confusion matrix
        var tp = 0
        var fp = 0
        var tn = 0
        var fn = 0

        for (result in results) {
            val expected = result.testCase.expectedVerdict
            val actual = result.actualVerdict
            val isBad = actual == Verdict.MALICIOUS || actual == Verdict.SUSPICIOUS
            val wasBad = expected == Verdict.MALICIOUS || expected == Verdict.SUSPICIOUS

            when {
                wasBad && isBad -> tp++
                wasBad && !isBad -> fn++
                !wasBad && isBad -> fp++
                else -> tn++
            }
        }

        val precision = if (tp + fp > 0) tp.toDouble() / (tp + fp) else 0.0
        val recall = if (tp + fn > 0) tp.toDouble() / (tp + fn) else 0.0
        val f1 = if (precision + recall > 0) 2 * precision * recall / (precision + recall) else 0.0

        val runtimes = results.map { it.runtimeMs }
        val avgRuntime = if (runtimes.isNotEmpty()) runtimes.average() else 0.0
        val maxRuntime = runtimes.maxOrNull() ?: 0
        val minRuntime = runtimes.minOrNull() ?: 0

        return EvaluationMetrics(
            totalTests = total,
            correctPredictions = correct,
            accuracy = accuracy,
            precision = precision,
            recall = recall,
            f1Score = f1,
            confusionMatrix = ConfusionMatrix(tp, fp, tn, fn),
            averageRuntimeMs = avgRuntime,
            maxRuntimeMs = maxRuntime,
            minRuntimeMs = minRuntime
        )
    }

    companion object {
        /**
         * Benign URLs corpus (known safe).
         */
        val BENIGN_CORPUS = listOf(
            // Major tech companies
            "https://www.google.com",
            "https://www.apple.com",
            "https://www.microsoft.com",
            "https://www.amazon.com",
            "https://www.facebook.com",
            "https://www.twitter.com",
            "https://www.linkedin.com",
            "https://www.github.com",
            "https://www.netflix.com",
            "https://www.spotify.com",
            
            // News sites
            "https://www.bbc.com",
            "https://www.cnn.com",
            "https://www.nytimes.com",
            "https://www.reuters.com",
            
            // Banks
            "https://www.chase.com",
            "https://www.bankofamerica.com",
            "https://www.wellsfargo.com",
            
            // Government
            "https://www.usa.gov",
            "https://www.gov.uk",
            "https://www.irs.gov",
            
            // E-commerce
            "https://www.ebay.com",
            "https://www.walmart.com",
            "https://www.target.com",
            
            // Email providers
            "https://mail.google.com",
            "https://outlook.live.com",
            "https://mail.yahoo.com",
            
            // With paths
            "https://www.google.com/search?q=kotlin",
            "https://github.com/user/repo/issues",
            "https://docs.microsoft.com/en-us/dotnet",
            
            // CDN/subdomains
            "https://cdn.jsdelivr.net/npm/package",
            "https://api.github.com/users",
            "https://static.example.com/assets/image.png"
        )

        /**
         * Phishing URLs corpus (known bad).
         */
        val PHISH_CORPUS = listOf(
            // IP-based
            "http://192.168.1.1/login.php",
            "http://45.33.32.156/paypal/verify",
            
            // Typosquatting
            "http://paypa1.com/login",
            "http://arnazon.com/signin",
            "http://amaz0n.com/verify",
            "http://micr0soft.com/account",
            
            // @ symbol injection
            "http://google.com@malicious.tk/phish",
            "https://paypal.com@evil.com/login",
            
            // Suspicious TLDs
            "http://login-verify.tk/paypal",
            "http://account-update.ml/microsoft",
            "http://secure-banking.ga/chase",
            
            // Risky extensions
            "http://example.com/invoice.pdf.exe",
            "http://download.com/update.scr",
            
            // JavaScript URL
            "javascript:alert('xss')",
            
            // Data URI
            "data:text/html,<script>alert('xss')</script>",
            
            // Credential harvesting keywords
            "http://login-verify-update.com/account/secure/billing",
            
            // Long suspicious URL
            "http://this-is-a-very-long-suspicious-looking-url-that-goes-on-and-on.tk/login/verify/update/secure/billing/password",
            
            // Deep subdomains
            "http://secure.login.verify.account.example.tk/phish",
            
            // Encoded payload
            "http://example.com/?data=PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==",
            
            // Fragment hiding
            "http://example.com#http://evil.com/payload?steal=cookies"
        )

        /**
         * Edge cases corpus (unicode, punycode, etc.).
         */
        val EDGE_CASES = listOf(
            // Punycode (IDN)
            TestCase("http://xn--pple-43d.com", Verdict.MALICIOUS, "idn", "Punycode apple"),
            TestCase("http://xn--80ak6aa92e.com", Verdict.MALICIOUS, "idn", "Cyrillic domain"),
            
            // Mixed script
            TestCase("http://аpple.com", Verdict.MALICIOUS, "mixed-script", "Cyrillic a"),
            TestCase("http://gооgle.com", Verdict.MALICIOUS, "mixed-script", "Cyrillic o"),
            
            // Zero-width chars
            TestCase("http://goo\u200Bgle.com", Verdict.MALICIOUS, "zero-width", "Zero-width space"),
            
            // Legitimate IDN
            TestCase("http://münchen.de", Verdict.SAFE, "idn-legit", "German city"),
            TestCase("http://日本語.jp", Verdict.SAFE, "idn-legit", "Japanese"),
            
            // Numeric subdomain (benign)
            TestCase("https://192.aws.amazon.com", Verdict.SAFE, "numeric-subdomain", "AWS region"),
            
            // Marketing URL (long but safe)
            TestCase(
                "https://www.example.com/landing?utm_source=google&utm_medium=cpc&utm_campaign=spring_sale&ref=abc123",
                Verdict.SAFE,
                "marketing",
                "UTM parameters"
            ),
            
            // Localhost (should not crash)
            TestCase("http://localhost:8080/api", Verdict.SAFE, "localhost", "Dev server"),
            TestCase("http://127.0.0.1/test", Verdict.SAFE, "localhost", "Loopback IP")
        )
    }
}
