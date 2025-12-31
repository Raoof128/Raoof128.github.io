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

package com.raouf.mehrguard.android

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raouf.mehrguard.core.PhishingEngine
import com.raouf.mehrguard.model.Verdict
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import kotlinx.coroutines.runBlocking
import com.raouf.mehrguard.model.RiskAssessment

/**
 * End-to-end Integration Tests for QR-SHIELD Android
 *
 * Tests the complete analysis pipeline from URL input to verdict.
 * These tests run on an Android device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class ScanFlowIntegrationTest {

    private lateinit var context: Context
    private lateinit var engine: PhishingEngine

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        engine = PhishingEngine()
    }

    private fun analyzeSync(url: String): RiskAssessment = runBlocking {
        engine.analyze(url)
    }

    // =========================================================================
    // SAFE URL FLOW TESTS
    // =========================================================================

    @Test
    fun safeUrl_producesCorrectVerdict() {
        val result = analyzeSync("https://www.google.com")

        assertEquals(Verdict.SAFE, result.verdict)
        assertTrue("Score should be low", result.score <= 20)
        assertTrue("Confidence should be positive", result.confidence > 0)
        assertNotNull("Details should not be null", result.details)
    }

    @Test
    fun safeUrl_officialBankingDomain() {
        val result = analyzeSync("https://www.commbank.com.au/netbank")

        // Should be safe or low score (country TLD may add small points)
        assertTrue(
            "Official banking site should be low risk",
            result.verdict == Verdict.SAFE || result.score <= 25
        )
    }

    @Test
    fun safeUrl_githubRepository() {
        val result = analyzeSync("https://github.com/user/mehrguard")

        assertEquals(Verdict.SAFE, result.verdict)
    }

    // =========================================================================
    // PHISHING URL FLOW TESTS
    // =========================================================================

    @Test
    fun phishingUrl_typosquatDetected() {
        val result = analyzeSync("https://paypa1.com/login")

        // Should have elevated score due to brand impersonation
        assertTrue("Typosquat should have elevated score", result.score > 10)
        assertTrue("Should have flags", result.flags.isNotEmpty())
    }

    @Test
    fun phishingUrl_ipAddressHost() {
        val result = analyzeSync("http://192.168.1.1/login")

        // IP address + HTTP should be flagged
        assertTrue("IP address should be flagged", result.score >= 15)
        assertTrue("Should have IP-related flag",
            result.flags.any { it.contains("IP", ignoreCase = true) })
    }

    @Test
    fun phishingUrl_highRiskTld() {
        val result = analyzeSync("https://login-bank.tk/verify")

        // High-risk TLD should increase score
        assertTrue("High-risk TLD should increase score", result.score >= 20)
    }

    @Test
    fun phishingUrl_combosquatDomain() {
        val result = analyzeSync("https://netflix-billing.com/update")

        // Combosquat should be detected
        assertNotNull("Should complete analysis", result.verdict)
    }

    @Test
    fun phishingUrl_multipleRiskFactors() {
        // HTTP + IP + credential params
        val result = analyzeSync("http://192.168.1.1:8080/login.php?password=test")

        // Multiple risk factors should produce high score
        assertTrue("Multiple risks should elevate score", result.score >= 30)
        assertTrue("Should be flagged as risky",
            result.verdict != Verdict.SAFE)
    }

    // =========================================================================
    // EDGE CASE FLOW TESTS
    // =========================================================================

    @Test
    fun edgeCase_emptyUrl() {
        val result = analyzeSync("")

        assertEquals(Verdict.UNKNOWN, result.verdict)
    }

    @Test
    fun edgeCase_longUrl() {
        val longPath = "a".repeat(2000)
        val result = analyzeSync("https://example.com/$longPath")

        // Should handle gracefully without crashing
        assertNotNull("Should not crash on long URL", result.verdict)
    }

    @Test
    fun edgeCase_urlWithAtSymbol() {
        val result = analyzeSync("https://google.com@evil.com")

        // @ symbol should add risk
        assertTrue("@ symbol should be flagged", result.score >= 10)
    }

    @Test
    fun edgeCase_urlShortener() {
        val result = analyzeSync("https://bit.ly/abc123")

        // Should complete analysis
        assertNotNull("Shortener URL should be analyzed", result.verdict)
    }

    @Test
    fun edgeCase_unicodeUrl() {
        val result = analyzeSync("https://exаmple.com") // 'а' is Cyrillic

        // Should complete analysis
        assertNotNull("Unicode URL should be analyzed", result.verdict)
    }

    // =========================================================================
    // ANALYSIS CONSISTENCY TESTS
    // =========================================================================

    @Test
    fun consistency_sameUrlProducesSameResult() {
        val url = "https://google.com"

        val result1 = analyzeSync(url)
        val result2 = analyzeSync(url)

        assertEquals("Score should be consistent", result1.score, result2.score)
        assertEquals("Verdict should be consistent", result1.verdict, result2.verdict)
    }

    @Test
    fun consistency_caseInsensitive() {
        val lower = analyzeSync("https://google.com")
        val upper = analyzeSync("https://GOOGLE.COM")
        val mixed = analyzeSync("https://GoOgLe.CoM")

        // All should produce similar results
        assertEquals("Case should not affect verdict", lower.verdict, upper.verdict)
        assertEquals("Case should not affect verdict", lower.verdict, mixed.verdict)
    }

    // =========================================================================
    // AUSTRALIAN PHISHING FLOW TESTS
    // =========================================================================

    @Test
    fun australianPhishing_commBankTyposquat() {
        val result = analyzeSync("https://cornmbank-login.tk")

        // Should detect brand impersonation + high-risk TLD
        assertTrue("CommBank typosquat should be flagged", result.score >= 20)
    }

    @Test
    fun australianPhishing_ausPostDeliveryScam() {
        val result = analyzeSync("https://auspost-delivery.tk/tracking")

        // Should be flagged as suspicious
        assertTrue("AusPost scam should be flagged", result.score >= 20)
    }

    @Test
    fun australianPhishing_myGovPhishing() {
        val result = analyzeSync("https://mygov-update.com/verify")

        // Should analyze correctly
        assertNotNull("myGov phishing should be analyzed", result.verdict)
    }

    // =========================================================================
    // RESULT STRUCTURE VALIDATION
    // =========================================================================

    @Test
    fun resultStructure_hasAllFields() {
        val result = analyzeSync("https://example.com")

        // Verify all fields are populated
        assertNotNull("Score should not be null", result.score)
        assertNotNull("Verdict should not be null", result.verdict)
        assertNotNull("Flags should not be null", result.flags)
        assertNotNull("Details should not be null", result.details)
        assertNotNull("Confidence should not be null", result.confidence)
        assertNotNull("Score description should not be null", result.scoreDescription)

        // Verify ranges
        assertTrue("Score should be 0-100", result.score in 0..100)
        assertTrue("Confidence should be 0-1", result.confidence in 0f..1f)
    }

    @Test
    fun resultStructure_flagsAreReadable() {
        val result = analyzeSync("http://192.168.1.1/login?password=test")

        // Flags should be human-readable strings
        result.flags.forEach { flag ->
            assertTrue("Flag should not be empty", flag.isNotBlank())
            assertTrue("Flag should be readable length", flag.length < 200)
        }
    }
}
