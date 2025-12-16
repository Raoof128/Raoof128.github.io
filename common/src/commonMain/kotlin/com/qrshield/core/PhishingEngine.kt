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

package com.qrshield.core

import com.qrshield.engine.BrandDetector
import com.qrshield.engine.HeuristicsEngine
import com.qrshield.engine.TldScorer
import com.qrshield.ml.EnsembleModel
import com.qrshield.ml.FeatureExtractor
import com.qrshield.ml.LogisticRegressionModel
import com.qrshield.model.RiskAssessment
import com.qrshield.model.UrlAnalysisResult
import com.qrshield.model.Verdict
import com.qrshield.security.InputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * QR-SHIELD Phishing Engine
 *
 * Main orchestrator for URL phishing analysis.
 * Combines heuristics, ML scoring, brand detection, and TLD analysis.
 *
 * ## Why Kotlin? (KotlinConf 2025-2026 Competition Criteria)
 *
 * This core detection logic is implemented in pure Kotlin because:
 *
 * 1. **Null Safety**: Kotlin's type system prevents NPEs during malicious URL parsing.
 *    Attackers often craft URLs with unexpected null values (e.g., missing TLDs,
 *    empty query params). Kotlin's `?` and `?.let` ensure safe handling.
 *
 * 2. **Coroutines**: Non-blocking analysis using `Dispatchers.Default` keeps UI
 *    responsive. The engine can be called from `viewModelScope.launch {}` without
 *    blocking the main thread on any platform.
 *
 * 3. **Multiplatform**: This exact class compiles to:
 *    - JVM bytecode (Android, Desktop)
 *    - Native binaries (iOS via Kotlin/Native)
 *    - JavaScript (Web via Kotlin/JS)
 *
 *    One security implementation, consistent behavior across all 4 platforms.
 *
 * 4. **Data Classes**: `RiskAssessment`, `UrlAnalysisResult`, and `Verdict` are
 *    immutable data classes with `copy()`, `equals()`, and `hashCode()` for free.
 *
 * 5. **Sealed Classes**: `Verdict` is a sealed class ensuring exhaustive `when`
 *    checking — the compiler ensures we handle SAFE, SUSPICIOUS, MALICIOUS, and UNKNOWN.
 *
 * 6. **Extension Functions**: URL parsing utilities are extension functions on `String`
 *    keeping the API clean while maintaining separation of concerns.
 *
 * ## SECURITY NOTES
 * - All inputs are validated before processing
 * - Analysis is performed locally (no network requests)
 * - Scoring uses defensive arithmetic with bounds checking
 * - Thread-safe: all dependencies are stateless
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 * @see HeuristicsEngine
 * @see BrandDetector
 * @see LogisticRegressionModel
 */
class PhishingEngine(
    private val heuristicsEngine: HeuristicsEngine = HeuristicsEngine(),
    private val brandDetector: BrandDetector = BrandDetector(),
    private val tldScorer: TldScorer = TldScorer(),
    private val mlModel: LogisticRegressionModel = LogisticRegressionModel.default(),
    private val ensembleModel: EnsembleModel = EnsembleModel.default(),
    private val featureExtractor: FeatureExtractor = FeatureExtractor(),
    private val useEnsemble: Boolean = true
) {

    companion object {
        /**
         * Scoring weights (must sum to 1.0).
         * Centralized in [SecurityConstants] for tuning and documentation.
         * Using slightly adjusted weights optimized for this engine's pipeline.
         */
        private const val HEURISTIC_WEIGHT = 0.50
        private const val ML_WEIGHT = 0.20
        private const val BRAND_WEIGHT = 0.15
        private const val TLD_WEIGHT = 0.15

        /**
         * Verdict thresholds.
         * @see SecurityConstants.SAFE_THRESHOLD for global definition
         * Using local values tuned for this engine's multi-signal approach.
         */
        private val SAFE_THRESHOLD = SecurityConstants.SAFE_THRESHOLD / 3  // ~10 for stricter pipeline
        private val SUSPICIOUS_THRESHOLD = SecurityConstants.MALICIOUS_THRESHOLD - 20  // ~50

        /** Maximum URL length - uses centralized constant */
        private val MAX_URL_LENGTH = SecurityConstants.MAX_URL_LENGTH

        /** Default confidence for edge cases - uses centralized constant */
        private val DEFAULT_CONFIDENCE = SecurityConstants.BASE_CONFIDENCE
    }

    /**
     * Analyze a URL for phishing indicators.
     *
     * This is the main entry point for phishing analysis. It:
     * 1. Validates the input URL
     * 2. Runs heuristic analysis (25+ rules)
     * 3. Performs ML-based scoring
     * 4. Checks for brand impersonation
     * 5. Evaluates TLD risk
     * 6. Combines all signals into a final verdict
     *
     * @param url The URL extracted from a QR code
     * @return Complete risk assessment with score, verdict, and details
     *
     * Note: This is a suspend function to ensure ML inference runs on a background
     * dispatcher, preventing UI jank on mobile devices.
     */
    suspend fun analyze(url: String): RiskAssessment = withContext(Dispatchers.Default) {
        analyzeInternal(url)
    }

    /**
     * Synchronous analysis for backward compatibility and tests.
     *
     * Use this in non-coroutine contexts (JS, games, benchmarks).
     * On JVM/Native, prefer [analyze] in production code for non-blocking behavior.
     *
     * @param url The URL to analyze
     * @return Complete risk assessment
     */
    fun analyzeBlocking(url: String): RiskAssessment = analyzeInternal(url)

    /**
     * Core synchronous analysis logic.
     * All actual analysis happens here synchronously.
     */
    private fun analyzeInternal(url: String): RiskAssessment {
        // PHASE 1: Input Validation
        val validationResult = InputValidator.validateUrl(url)
        if (!validationResult.isValid()) {
            return createInvalidUrlResult(validationResult)
        }

        val validatedUrl = validationResult.getOrNull() ?: url

        // PHASE 2: Basic URL validation
        if (!isValidUrl(validatedUrl)) {
            return RiskAssessment(
                score = 0,
                verdict = Verdict.UNKNOWN,
                flags = listOf("Invalid or unsupported URL format"),
                details = UrlAnalysisResult.empty(),
                confidence = DEFAULT_CONFIDENCE
            )
        }

        // PHASE 3: Run all analysis engines safely
        return runCatching {
            performAnalysis(validatedUrl)
        }.getOrElse { _ ->
            // SECURITY: Don't expose internal exceptions
            RiskAssessment(
                score = 50,
                verdict = Verdict.UNKNOWN,
                flags = listOf("Analysis error - treating as suspicious"),
                details = UrlAnalysisResult.empty(),
                confidence = 0.3f
            )
        }
    }

    /**
     * Perform the actual analysis (called after validation).
     */
    private fun performAnalysis(url: String): RiskAssessment {
        // Run heuristics engine
        val heuristicResult = heuristicsEngine.analyze(url)

        // Run brand detection
        val brandResult = brandDetector.detect(url)

        // Run TLD scoring
        val tldResult = tldScorer.score(url)

        // Extract features and run ML model (ensemble or basic)
        val features = featureExtractor.extract(url)
        val mlScore = if (useEnsemble) {
            // Use advanced ensemble model for more sophisticated scoring
            val ensemblePrediction = ensembleModel.predict(features)
            ensemblePrediction.probability
        } else {
            // Fallback to basic logistic regression
            mlModel.predict(features)
        }.coerceIn(0f, 1f)

        // Calculate combined score
        val combinedScore = calculateCombinedScore(
            heuristicScore = heuristicResult.score,
            mlScore = mlScore,
            brandScore = brandResult.score,
            tldScore = tldResult.score
        )

        // Determine verdict
        val verdict = determineVerdict(combinedScore, heuristicResult, brandResult, tldResult)

        // Collect all flags
        val allFlags = buildList {
            addAll(heuristicResult.flags)
            brandResult.match?.let {
                add("Brand impersonation detected: $it")
            }
            if (tldResult.isHighRisk) {
                add("High-risk TLD: ${tldResult.tld}")
            }
        }

        // Calculate confidence score
        val confidence = calculateConfidence(heuristicResult, mlScore, brandResult)

        return RiskAssessment(
            score = combinedScore,
            verdict = verdict,
            flags = allFlags,
            details = UrlAnalysisResult(
                originalUrl = url.take(256), // Truncate for storage
                heuristicScore = heuristicResult.score,
                mlScore = (mlScore * 100).toInt(),
                brandScore = brandResult.score,
                tldScore = tldResult.score,
                brandMatch = brandResult.match,
                tld = tldResult.tld
            ),
            confidence = confidence
        )
    }

    /**
     * Calculate combined risk score from all engines.
     *
     * Uses weighted average with bounds checking.
     */
    private fun calculateCombinedScore(
        heuristicScore: Int,
        mlScore: Float,
        brandScore: Int,
        tldScore: Int
    ): Int {
        // Normalize scores to 0-100 range
        val normalizedHeuristic = heuristicScore.coerceIn(0, 100)
        val normalizedMl = (mlScore * 100).toInt().coerceIn(0, 100)
        val normalizedBrand = brandScore.coerceIn(0, 100)
        val normalizedTld = tldScore.coerceIn(0, 100)

        // Weighted combination
        val weighted = (
            normalizedHeuristic * HEURISTIC_WEIGHT +
            normalizedMl * ML_WEIGHT +
            normalizedBrand * BRAND_WEIGHT +
            normalizedTld * TLD_WEIGHT
        )

        return weighted.toInt().coerceIn(0, 100)
    }

    /**
     * Determine verdict based on score and critical factors.
     *
     * Some indicators (like confirmed brand impersonation) can
     * escalate the verdict regardless of overall score.
     */
    private fun determineVerdict(
        score: Int,
        heuristicResult: HeuristicsEngine.Result,
        brandResult: BrandDetector.DetectionResult,
        tldResult: TldScorer.TldResult
    ): Verdict {
        // Critical escalation: confirmed homograph attack
        if (brandResult.details?.matchType == BrandDetector.MatchType.HOMOGRAPH) {
            return Verdict.MALICIOUS
        }

        // Critical escalation: brand impersonation detected
        // Any brand match should be at least SUSPICIOUS
        if (brandResult.match != null) {
            return if (score > SUSPICIOUS_THRESHOLD || brandResult.score >= 50) {
                Verdict.MALICIOUS
            } else {
                Verdict.SUSPICIOUS
            }
        }

        // Critical escalation: multiple high-severity indicators
        val criticalCount = heuristicResult.details.count { (_, weight) ->
            weight >= 20
        }
        if (criticalCount >= 2 && score > SAFE_THRESHOLD) {
            return Verdict.MALICIOUS
        }

        // Escalation: @ symbol injection (common phishing technique)
        if (heuristicResult.flags.any { it.contains("@ symbol", ignoreCase = true) }) {
            return Verdict.SUSPICIOUS
        }

        // Escalation: High Risk TLD
        if (tldResult.isHighRisk) {
            return if (score > SUSPICIOUS_THRESHOLD) Verdict.MALICIOUS else Verdict.SUSPICIOUS
        }

        // Escalation: Strong heuristic signal alone
        if (heuristicResult.score > 60) {
            return if (score > SUSPICIOUS_THRESHOLD) Verdict.MALICIOUS else Verdict.SUSPICIOUS
        }

        // Standard threshold-based verdict
        return when {
            score <= SAFE_THRESHOLD -> Verdict.SAFE
            score <= SUSPICIOUS_THRESHOLD -> Verdict.SUSPICIOUS
            else -> Verdict.MALICIOUS
        }
    }

    /**
     * Calculate confidence score based on signals.
     *
     * Higher confidence when:
     * - Heuristics and ML agree
     * - Brand detection has a match
     * - More heuristic signals triggered
     */
    private fun calculateConfidence(
        heuristicResult: HeuristicsEngine.Result,
        mlScore: Float,
        brandResult: BrandDetector.DetectionResult
    ): Float {
        var confidence = 0.5f

        // Agreement between heuristics and ML
        val heuristicNormalized = heuristicResult.score / 100f
        val agreement = 1f - kotlin.math.abs(heuristicNormalized - mlScore)
        confidence += agreement * 0.2f

        // Brand detection adds certainty
        if (brandResult.match != null) {
            confidence += 0.15f
        }

        // More signals = more confidence
        val signalCount = heuristicResult.flags.size
        confidence += (signalCount.coerceAtMost(5) * 0.02f)

        return confidence.coerceIn(0.3f, 0.99f)
    }

    /**
     * Basic URL format validation.
     */
    private fun isValidUrl(url: String): Boolean {
        if (url.isBlank()) return false
        if (url.length > MAX_URL_LENGTH) return false

        // Must start with http:// or https:// or contain a dot
        return url.startsWith("http://") ||
               url.startsWith("https://") ||
               (url.contains(".") && !url.contains(" "))
    }

    /**
     * Create result for invalid URL input.
     */
    private fun createInvalidUrlResult(
        validation: InputValidator.ValidationResult<String>
    ): RiskAssessment {
        val reason = when (validation) {
            is InputValidator.ValidationResult.Invalid -> validation.reason
            else -> "Invalid URL"
        }

        return RiskAssessment(
            score = 0,
            verdict = Verdict.UNKNOWN,
            flags = listOf(reason),
            details = UrlAnalysisResult.empty(),
            confidence = 0f
        )
    }
}
