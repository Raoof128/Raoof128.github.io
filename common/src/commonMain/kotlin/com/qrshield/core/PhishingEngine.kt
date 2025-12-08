/*
 * Copyright 2024 QR-SHIELD Contributors
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

/*
 * Copyright 2024 QR-SHIELD Contributors
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
import com.qrshield.ml.FeatureExtractor
import com.qrshield.ml.LogisticRegressionModel
import com.qrshield.model.RiskAssessment
import com.qrshield.model.UrlAnalysisResult
import com.qrshield.model.Verdict
import com.qrshield.security.InputValidator

/**
 * QR-SHIELD Phishing Engine
 * 
 * Main orchestrator for URL phishing analysis.
 * Combines heuristics, ML scoring, brand detection, and TLD analysis.
 * 
 * SECURITY NOTES:
 * - All inputs are validated before processing
 * - Analysis is performed locally (no network requests)
 * - Scoring uses defensive arithmetic with bounds checking
 * - Thread-safe: all dependencies are stateless
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class PhishingEngine(
    private val heuristicsEngine: HeuristicsEngine = HeuristicsEngine(),
    private val brandDetector: BrandDetector = BrandDetector(),
    private val tldScorer: TldScorer = TldScorer(),
    private val mlModel: LogisticRegressionModel = LogisticRegressionModel.default(),
    private val featureExtractor: FeatureExtractor = FeatureExtractor()
) {
    
    companion object {
        // Scoring weights (must sum to 1.0)
        private const val HEURISTIC_WEIGHT = 0.40
        private const val ML_WEIGHT = 0.35
        private const val BRAND_WEIGHT = 0.15
        private const val TLD_WEIGHT = 0.10
        
        // Verdict thresholds
        private const val SAFE_THRESHOLD = 30
        private const val SUSPICIOUS_THRESHOLD = 70
        
        // Validation
        private const val MAX_URL_LENGTH = 2048
        
        // Default confidence for edge cases
        private const val DEFAULT_CONFIDENCE = 0.5f
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
     */
    fun analyze(url: String): RiskAssessment {
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
        val analysisResult = runCatching {
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
        
        return analysisResult
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
        
        // Extract features and run ML model
        val features = featureExtractor.extract(url)
        val mlScore = mlModel.predict(features).coerceIn(0f, 1f)
        
        // Calculate combined score
        val combinedScore = calculateCombinedScore(
            heuristicScore = heuristicResult.score,
            mlScore = mlScore,
            brandScore = brandResult.score,
            tldScore = tldResult.score
        )
        
        // Determine verdict
        val verdict = determineVerdict(combinedScore, heuristicResult, brandResult)
        
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
        brandResult: BrandDetector.DetectionResult
    ): Verdict {
        // Critical escalation: confirmed homograph attack
        if (brandResult.details?.matchType == BrandDetector.MatchType.HOMOGRAPH) {
            return Verdict.MALICIOUS
        }
        
        // Critical escalation: multiple high-severity indicators
        val criticalCount = heuristicResult.details.count { (_, weight) ->
            weight >= 20
        }
        if (criticalCount >= 2 && score > SAFE_THRESHOLD) {
            return Verdict.MALICIOUS
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
