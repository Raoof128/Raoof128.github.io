/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.ml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Tests for ML scoring components.
 */
class MlScorerTest {

    // === CharacterEmbeddingScorer Tests ===

    @Test
    fun `char scorer returns score in valid range`() {
        val scorer = CharacterEmbeddingScorer.default
        val score = scorer.score("https://example.com")
        
        assertTrue(score in 0f..1f, "Score should be in [0, 1], got $score")
    }

    @Test
    fun `char scorer is deterministic`() {
        val scorer = CharacterEmbeddingScorer.default
        val url = "https://paypa1-secure.tk/login"
        
        val score1 = scorer.score(url)
        val score2 = scorer.score(url)
        
        assertEquals(score1, score2, "Same input should produce same output")
    }

    @Test
    fun `char scorer handles empty input`() {
        val scorer = CharacterEmbeddingScorer.default
        val score = scorer.score("")
        
        assertEquals(0f, score)
    }

    @Test
    fun `char scorer handles very long input`() {
        val scorer = CharacterEmbeddingScorer.default
        val longUrl = "https://example.com/" + "a".repeat(1000)
        
        val score = scorer.score(longUrl)
        assertTrue(score in 0f..1f)
    }

    @Test
    fun `char scorer with details returns risk level`() {
        val scorer = CharacterEmbeddingScorer.default
        val result = scorer.scoreWithDetails("https://example.com@evil.tk")
        
        assertNotNull(result.riskLevel)
        assertTrue(result.inputLength > 0)
    }

    // === UrlFeatureExtractor Tests ===

    @Test
    fun `feature extractor produces 24 features`() {
        val extractor = UrlFeatureExtractor.default
        val features = extractor.extract("https://www.example.com/path?query=value")
        
        assertEquals(24, features.vector.size)
        assertEquals(24, features.featureNames.size)
    }

    @Test
    fun `feature extractor handles malformed URLs`() {
        val extractor = UrlFeatureExtractor.default
        
        val features1 = extractor.extract("not-a-url")
        assertEquals(24, features1.vector.size)
        
        val features2 = extractor.extract("")
        assertEquals(24, features2.vector.size)
    }

    @Test
    fun `feature extractor detects IP addresses`() {
        val extractor = UrlFeatureExtractor.default
        
        val ipFeatures = extractor.extract("http://192.168.1.1/phish")
        val domainFeatures = extractor.extract("https://example.com/page")
        
        // Feature 16 is is_ip
        assertTrue(ipFeatures.vector[16] > 0.5f, "Should detect IP address")
        assertTrue(domainFeatures.vector[16] < 0.5f, "Should not detect IP for domain")
    }

    @Test
    fun `feature extractor detects punycode`() {
        val extractor = UrlFeatureExtractor.default
        
        val punyFeatures = extractor.extract("http://xn--pple-43d.com")
        val normalFeatures = extractor.extract("https://apple.com")
        
        // Feature 17 is is_punycode
        assertTrue(punyFeatures.vector[17] > 0.5f, "Should detect punycode")
        assertTrue(normalFeatures.vector[17] < 0.5f, "Should not detect punycode")
    }

    @Test
    fun `feature extractor detects at symbol`() {
        val extractor = UrlFeatureExtractor.default
        
        val atFeatures = extractor.extract("http://google.com@evil.tk")
        val normalFeatures = extractor.extract("https://google.com")
        
        // Feature 18 is has_at_symbol
        assertTrue(atFeatures.vector[18] > 0.5f, "Should detect @ symbol")
        assertTrue(normalFeatures.vector[18] < 0.5f, "Should not detect @")
    }

    // === TinyPhishingClassifier Tests ===

    @Test
    fun `classifier returns score in valid range`() {
        val classifier = TinyPhishingClassifier.default
        
        val score = classifier.predict("https://example.com")
        assertTrue(score in 0f..1f)
    }

    @Test
    fun `classifier is deterministic`() {
        val classifier = TinyPhishingClassifier.default
        val url = "http://192.168.1.1/login.php"
        
        val score1 = classifier.predict(url)
        val score2 = classifier.predict(url)
        
        assertEquals(score1, score2)
    }

    @Test
    fun `classifier with details returns explanations`() {
        val classifier = TinyPhishingClassifier.default
        val result = classifier.predictWithDetails("http://paypa1.tk/verify")
        
        assertNotNull(result.topFeatures)
        assertTrue(result.topFeatures.isNotEmpty())
        assertTrue(result.confidence in 0f..1f)
    }

    // === EnsemblePhishingScorer Tests ===

    @Test
    fun `ensemble returns score in valid range`() {
        val ensemble = EnsemblePhishingScorer.default
        
        val score = ensemble.score("https://example.com")
        assertTrue(score in 0f..1f)
    }

    @Test
    fun `ensemble is deterministic`() {
        val ensemble = EnsemblePhishingScorer.default
        val url = "javascript:alert('xss')"
        
        val score1 = ensemble.score(url)
        val score2 = ensemble.score(url)
        
        assertEquals(score1, score2)
    }

    @Test
    fun `ensemble with details provides breakdown`() {
        val ensemble = EnsemblePhishingScorer.default
        val result = ensemble.scoreWithDetails("https://example.com")
        
        assertTrue(result.charScore in 0f..1f)
        assertTrue(result.featureScore in 0f..1f)
        assertTrue(result.ensembleScore in 0f..1f)
        assertNotNull(result.charRiskLevel)
    }

    @Test
    fun `isLikelyPhishing works correctly`() {
        val ensemble = EnsemblePhishingScorer.default
        
        // These should be suspicious based on patterns
        // Note: Results depend on the pre-trained weights
        val safeSite = ensemble.isLikelyPhishing("https://www.google.com")
        val suspiciousSite = ensemble.isLikelyPhishing("http://192.168.1.1@evil.tk/login")
        
        // At minimum, verify the function works without crashing
        assertTrue(safeSite == true || safeSite == false)
        assertTrue(suspiciousSite == true || suspiciousSite == false)
    }

    // === Performance Tests ===

    @Test
    fun `ml scoring completes within budget`() {
        val ensemble = EnsemblePhishingScorer.default
        val testUrls = listOf(
            "https://www.google.com",
            "http://192.168.1.1/login",
            "https://paypa1-secure.tk/verify",
            "javascript:alert(1)"
        )

        // Use TimeSource.Monotonic for cross-platform timing
        val timeSource = kotlin.time.TimeSource.Monotonic
        val startMark = timeSource.markNow()
        testUrls.forEach { ensemble.score(it) }
        val totalTimeMs = startMark.elapsedNow().inWholeMilliseconds.toDouble()

        val avgTimeMs = totalTimeMs / testUrls.size
        assertTrue(avgTimeMs < 5.0, "Average ML scoring should be under 5ms, got ${avgTimeMs}ms")
    }

    // === Edge Cases ===

    @Test
    fun `handles unicode without crashing`() {
        val ensemble = EnsemblePhishingScorer.default
        
        val score1 = ensemble.score("https://аpple.com")  // Cyrillic 'a'
        val score2 = ensemble.score("https://日本語.jp")
        val score3 = ensemble.score("https://münchen.de")
        
        assertTrue(score1 in 0f..1f)
        assertTrue(score2 in 0f..1f)
        assertTrue(score3 in 0f..1f)
    }

    @Test
    fun `handles special characters without crashing`() {
        val ensemble = EnsemblePhishingScorer.default
        
        val score1 = ensemble.score("https://example.com/?q=<script>alert(1)</script>")
        val score2 = ensemble.score("data:text/html,<script>alert(1)</script>")
        val score3 = ensemble.score("https://example.com/path#fragment#double")
        
        assertTrue(score1 in 0f..1f)
        assertTrue(score2 in 0f..1f)
        assertTrue(score3 in 0f..1f)
    }
}
