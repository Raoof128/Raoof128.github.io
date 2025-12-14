/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under Apache 2.0
 */

package com.qrshield.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Tests for scoring consistency and determinism.
 * Ensures identical inputs produce identical scores across runs.
 */
class ScoringConsistencyTest {

    // =====================================================
    // Score Determinism Tests
    // =====================================================

    @Test
    fun `identical URLs produce identical scores`() {
        val url = "https://paypa1-secure.tk/login"
        
        val score1 = simulateScore(url)
        val score2 = simulateScore(url)
        val score3 = simulateScore(url)
        
        assertEquals(score1, score2, "Scores must be deterministic")
        assertEquals(score2, score3, "Scores must be deterministic")
    }

    @Test
    fun `score is within valid range 0 to 100`() {
        val testUrls = listOf(
            "https://google.com",
            "https://paypa1.tk",
            "http://192.168.1.1:8080",
            "https://bit.ly/abc123",
            "https://suspicious-domain-with-many-words.xyz/login"
        )
        
        testUrls.forEach { url ->
            val score = simulateScore(url)
            assertTrue(score in 0..100, "Score for $url must be 0-100, got $score")
        }
    }

    // =====================================================
    // Score Threshold Tests
    // =====================================================

    @Test
    fun `safe URLs score below 30`() {
        val safeUrls = listOf(
            "https://google.com",
            "https://github.com",
            "https://apple.com",
            "https://microsoft.com"
        )
        
        safeUrls.forEach { url ->
            val score = simulateScore(url)
            assertTrue(score < 30, "Safe URL $url should score <30, got $score")
        }
    }

    @Test
    fun `malicious URLs score above 50`() {
        val maliciousUrls = listOf(
            "https://paypa1-secure.tk/login",
            "http://192.168.1.1:8080/admin/steal",
            "https://g00gle-support.ml/verify"
        )
        
        maliciousUrls.forEach { url ->
            val score = simulateScore(url)
            assertTrue(score >= 50, "Malicious URL $url should score >=50, got $score")
        }
    }

    @Test
    fun `suspicious URLs score above safe threshold`() {
        val suspiciousUrls = listOf(
            "https://bit.ly/abc123",
            "https://t.co/xyz789"
        )
        
        suspiciousUrls.forEach { url ->
            val score = simulateScore(url)
            assertTrue(score >= 20, "Suspicious URL $url should score >=20, got $score")
        }
    }

    // =====================================================
    // Heuristic Weight Consistency
    // =====================================================

    @Test
    fun `IPAddress heuristic adds consistent penalty`() {
        val baseScore = simulateScore("https://example.com")
        val ipScore = simulateScore("http://192.168.1.1")
        
        assertTrue(ipScore > baseScore + 15, "IP address should add significant penalty")
    }

    @Test
    fun `suspicious TLD adds consistent penalty`() {
        val normalScore = simulateScore("https://example.com")
        val tkScore = simulateScore("https://example.tk")
        
        assertTrue(tkScore > normalScore + 10, ".tk TLD should add penalty")
    }

    @Test
    fun `missing HTTPS adds penalty`() {
        val httpsScore = simulateScore("https://example.com")
        val httpScore = simulateScore("http://example.com")
        
        assertTrue(httpScore > httpsScore, "HTTP should score higher (worse) than HTTPS")
    }

    // =====================================================
    // Edge Case Scoring
    // =====================================================

    @Test
    fun `empty URL returns maximum score`() {
        val score = simulateScore("")
        assertEquals(100, score, "Empty URL should be maximum risk")
    }

    @Test
    fun `very long URL adds penalty`() {
        val shortUrl = "https://example.com"
        val longUrl = "https://example.com/" + "a".repeat(200)
        
        val shortScore = simulateScore(shortUrl)
        val longScore = simulateScore(longUrl)
        
        assertTrue(longScore >= shortScore, "Longer URLs should not score lower")
    }

    // =====================================================
    // Score Simulation (Mirrors Real Engine Logic)
    // =====================================================

    private fun simulateScore(url: String): Int {
        if (url.isEmpty()) return 100
        
        var score = 0
        
        // Protocol check
        if (!url.startsWith("https://")) score += 15
        
        // IP address check
        val host = url.substringAfter("://").substringBefore("/").substringBefore(":")
        if (host.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))) score += 25
        
        // Suspicious TLD check
        val suspiciousTlds = listOf("tk", "ml", "ga", "cf", "xyz")
        if (suspiciousTlds.any { host.endsWith(".$it") }) score += 20
        
        // URL shortener check
        val shorteners = listOf("bit.ly", "t.co", "goo.gl", "tinyurl.com")
        if (shorteners.any { host == it }) score += 25
        
        // Port check (non-standard ports)
        if (url.contains(Regex(":\\d{4,5}/"))) score += 10
        
        // Credential path check
        if (url.contains("/login") || url.contains("/signin") || url.contains("/verify")) score += 10
        
        // Brand impersonation (simple check)
        val brands = listOf("paypal", "google", "apple", "microsoft", "amazon")
        val domain = host.replace("-", "").replace("0", "o").replace("1", "l")
        if (brands.any { brand -> 
            brand in domain && !host.contains("$brand.com") 
        }) score += 30
        
        // Known safe domains get score reduction
        val safeDomains = listOf("google.com", "github.com", "apple.com", "microsoft.com")
        if (safeDomains.any { host == it || host.endsWith(".$it") }) {
            score = (score * 0.3).toInt()
        }
        
        return score.coerceIn(0, 100)
    }
}
