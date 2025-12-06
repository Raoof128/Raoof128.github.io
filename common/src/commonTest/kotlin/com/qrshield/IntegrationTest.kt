package com.qrshield

import com.qrshield.core.PhishingEngine
import com.qrshield.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * End-to-end integration tests for QR-SHIELD.
 * 
 * Tests the complete analysis pipeline from URL input to verdict.
 */
class IntegrationTest {
    
    private val engine = PhishingEngine()
    
    // === SAFE URL SCENARIOS ===
    
    @Test
    fun `safe HTTPS website returns SAFE verdict`() {
        val result = engine.analyze("https://www.google.com")
        
        assertEquals(Verdict.SAFE, result.verdict)
        assertTrue(result.score <= 30, "Safe URL should have score <= 30, got ${result.score}")
        assertTrue(result.confidence > 0.3f)
    }
    
    @Test
    fun `legitimate banking website returns SAFE`() {
        val result = engine.analyze("https://www.commbank.com.au/netbank")
        
        assertEquals(Verdict.SAFE, result.verdict)
    }
    
    @Test
    fun `github URL returns SAFE`() {
        val result = engine.analyze("https://github.com/user/repo")
        
        assertEquals(Verdict.SAFE, result.verdict)
    }
    
    // === OBVIOUS PHISHING SCENARIOS ===
    
    @Test
    fun `typosquat domain returns MALICIOUS or SUSPICIOUS`() {
        val result = engine.analyze("https://paypa1.com/login")
        
        assertTrue(
            result.verdict in listOf(Verdict.MALICIOUS, Verdict.SUSPICIOUS),
            "Typosquat should be flagged as risky"
        )
        assertTrue(result.score >= 30)
    }
    
    @Test
    fun `IP address host returns elevated risk`() {
        val result = engine.analyze("http://192.168.1.1/login")
        
        assertTrue(result.score >= 20)
        assertTrue(result.flags.any { it.contains("IP", ignoreCase = true) })
    }
    
    @Test
    fun `HTTP without HTTPS adds risk points`() {
        val httpResult = engine.analyze("http://example.com")
        val httpsResult = engine.analyze("https://example.com")
        
        assertTrue(
            httpResult.score > httpsResult.score,
            "HTTP should score higher than HTTPS"
        )
    }
    
    @Test
    fun `high risk TLD increases score`() {
        val result = engine.analyze("https://login-bank.tk")
        
        assertTrue(result.score >= 10, "High-risk TLD should increase score")
    }
    
    @Test
    fun `combosquat domain flagged`() {
        val result = engine.analyze("https://netflix-billing.com/update")
        
        assertTrue(result.isImpersonation || result.score >= 25)
    }
    
    // === EDGE CASES ===
    
    @Test
    fun `empty URL returns UNKNOWN`() {
        val result = engine.analyze("")
        
        assertEquals(Verdict.UNKNOWN, result.verdict)
    }
    
    @Test
    fun `very long URL handled safely`() {
        val longUrl = "https://example.com/" + "a".repeat(3000)
        val result = engine.analyze(longUrl)
        
        // Should not crash, should return valid result
        assertTrue(result.verdict != null)
    }
    
    @Test
    fun `URL with at symbol flagged`() {
        val result = engine.analyze("https://google.com@evil.com")
        
        assertTrue(result.score >= 15, "@ symbol should add risk points")
    }
    
    @Test
    fun `URL shortener adds risk points`() {
        val result = engine.analyze("https://bit.ly/abc123")
        
        assertTrue(result.flags.any { 
            it.contains("shortener", ignoreCase = true) || 
            it.contains("redirect", ignoreCase = true) 
        })
    }
    
    // === AUSTRALIAN PHISHING SCENARIOS ===
    
    @Test
    fun `CommBank phishing detected`() {
        val result = engine.analyze("https://cornmbank-login.com")
        
        assertTrue(
            result.verdict in listOf(Verdict.MALICIOUS, Verdict.SUSPICIOUS),
            "CommBank phishing should be flagged"
        )
    }
    
    @Test
    fun `AusPost delivery scam detected`() {
        val result = engine.analyze("https://auspost-delivery.tk/tracking")
        
        assertTrue(result.score >= 30)
    }
    
    @Test
    fun `myGov phishing detected`() {
        val result = engine.analyze("https://mygov-update.com/verify")
        
        assertTrue(
            result.verdict in listOf(Verdict.MALICIOUS, Verdict.SUSPICIOUS),
            "myGov phishing should be flagged"
        )
    }
    
    // === COMPOUND RISK FACTORS ===
    
    @Test
    fun `multiple risk factors produce high score`() {
        // HTTP + IP + suspicious path + credential params
        val result = engine.analyze("http://192.168.1.1:8080/login.php?password=test")
        
        assertTrue(result.score >= 50, "Multiple risk factors should produce high score")
        assertTrue(result.flags.size >= 2, "Should have multiple flags")
    }
    
    @Test
    fun `brand impersonation + risky TLD produces MALICIOUS`() {
        val result = engine.analyze("https://paypa1-secure.tk/login")
        
        assertEquals(Verdict.MALICIOUS, result.verdict)
    }
    
    // === CONFIDENCE TESTING ===
    
    @Test
    fun `high confidence for clear phishing`() {
        val result = engine.analyze("https://g00gle.tk/login")
        
        assertTrue(result.confidence >= 0.5f, "Clear phishing should have high confidence")
    }
    
    @Test
    fun `analysis details are populated`() {
        val result = engine.analyze("https://example.com")
        
        assertTrue(result.details.heuristicScore >= 0)
        assertTrue(result.details.mlScore >= 0)
    }
    
    // === VERDICT DESCRIPTIONS ===
    
    @Test
    fun `SAFE verdict has appropriate description`() {
        val result = engine.analyze("https://www.google.com")
        
        assertEquals("Low Risk", result.scoreDescription)
        assertTrue(result.actionRecommendation.contains("safe", ignoreCase = true))
    }
    
    @Test
    fun `MALICIOUS verdict has appropriate description`() {
        val result = engine.analyze("https://paypa1.tk/login?password=steal")
        
        if (result.verdict == Verdict.MALICIOUS) {
            assertEquals("High Risk", result.scoreDescription)
            assertTrue(result.actionRecommendation.contains("not", ignoreCase = true))
        }
    }
    
    // === RiskAssessment properties ===
    
    private val com.qrshield.model.RiskAssessment.isImpersonation: Boolean
        get() = this.details.brandMatch != null
}
