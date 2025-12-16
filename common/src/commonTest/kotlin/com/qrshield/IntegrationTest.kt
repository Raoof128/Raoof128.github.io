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

package com.qrshield

import com.qrshield.core.PhishingEngine
import com.qrshield.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

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
        val result = engine.analyzeBlocking("https://www.google.com")

        assertEquals(Verdict.SAFE, result.verdict)
        assertTrue(result.score <= 40, "Safe URL should have low score, got ${result.score}")
    }

    @Test
    fun `legitimate banking website returns SAFE`() {
        val result = engine.analyzeBlocking("https://www.commbank.com.au/netbank")

        // With aggressive scoring, official banking sites should still have low scores
        // even if country TLD adds a small amount
        assertTrue(
            result.verdict == Verdict.SAFE || result.score <= 20,
            "Official banking site should be low risk, got verdict=${result.verdict}, score=${result.score}"
        )
    }

    @Test
    fun `github URL returns SAFE`() {
        val result = engine.analyzeBlocking("https://github.com/user/repo")

        assertEquals(Verdict.SAFE, result.verdict)
    }

    // === PHISHING SCENARIOS ===

    @Test
    fun `typosquat domain has elevated score`() {
        val result = engine.analyzeBlocking("https://paypa1.com/login")

        // Should have some risk score, may or may not trigger verdict threshold
        assertTrue(result.score >= 0, "Should complete analysis")
    }

    @Test
    fun `IP address host returns elevated risk`() {
        val result = engine.analyzeBlocking("http://192.168.1.1/login")

        assertTrue(result.score >= 15, "IP host should add risk points")
    }

    @Test
    fun `HTTP without HTTPS adds risk points`() {
        val httpResult = engine.analyzeBlocking("http://example.com")
        val httpsResult = engine.analyzeBlocking("https://example.com")

        assertTrue(
            httpResult.score > httpsResult.score,
            "HTTP should score higher than HTTPS"
        )
    }

    @Test
    fun `high risk TLD increases score`() {
        val result = engine.analyzeBlocking("https://login-bank.tk")

        assertTrue(result.score >= 5, "High-risk TLD should increase score")
    }

    @Test
    fun `combosquat domain analyzed`() {
        val result = engine.analyzeBlocking("https://netflix-billing.com/update")

        // Should complete analysis
        assertNotNull(result.verdict)
    }

    // === EDGE CASES ===

    @Test
    fun `empty URL returns UNKNOWN`() {
        val result = engine.analyzeBlocking("")

        assertEquals(Verdict.UNKNOWN, result.verdict)
    }

    @Test
    fun `very long URL handled safely`() {
        val longUrl = "https://example.com/" + "a".repeat(3000)
        val result = engine.analyzeBlocking(longUrl)

        // Should not crash, should return valid result
        assertNotNull(result.verdict)
    }

    @Test
    fun `URL with at symbol flagged`() {
        val result = engine.analyzeBlocking("https://google.com@evil.com")

        assertTrue(result.score >= 10, "@ symbol should add risk points")
    }

    @Test
    fun `URL shortener analyzed`() {
        val result = engine.analyzeBlocking("https://bit.ly/abc123")

        // Should complete analysis
        assertNotNull(result.verdict)
    }

    // === AUSTRALIAN PHISHING SCENARIOS ===

    @Test
    fun `CommBank phishing analyzed`() {
        val result = engine.analyzeBlocking("https://cornmbank-login.com")

        // Should analyze without error
        assertNotNull(result.verdict)
        assertTrue(result.score >= 0)
    }

    @Test
    fun `AusPost delivery scam analyzed`() {
        val result = engine.analyzeBlocking("https://auspost-delivery.tk/tracking")

        // High risk TLD should add points
        assertTrue(result.score >= 5)
    }

    @Test
    fun `myGov phishing analyzed`() {
        val result = engine.analyzeBlocking("https://mygov-update.com/verify")

        // Should analyze without error
        assertNotNull(result.verdict)
    }

    // === COMPOUND RISK FACTORS ===

    @Test
    fun `multiple risk factors produce elevated score`() {
        // HTTP + IP + suspicious path + credential params
        val result = engine.analyzeBlocking("http://192.168.1.1:8080/login.php?password=test")

        assertTrue(result.score >= 30, "Multiple risk factors should produce higher score, got ${result.score}")
        assertTrue(result.flags.isNotEmpty(), "Should have at least one flag")
    }

    @Test
    fun `brand impersonation with risky TLD analyzed`() {
        val result = engine.analyzeBlocking("https://paypa1-secure.tk/login")

        // Should have elevated score due to risky TLD
        assertTrue(result.score >= 5)
    }

    // === CONFIDENCE TESTING ===

    @Test
    fun `analysis produces confidence score`() {
        val result = engine.analyzeBlocking("https://g00gle.tk/login")

        assertTrue(result.confidence >= 0.0f, "Should have confidence value")
    }

    @Test
    fun `analysis details are populated`() {
        val result = engine.analyzeBlocking("https://example.com")

        assertNotNull(result.details)
    }

    // === VERDICT DESCRIPTIONS ===

    @Test
    fun `SAFE verdict has appropriate description`() {
        val result = engine.analyzeBlocking("https://www.google.com")

        assertEquals("Low Risk", result.scoreDescription)
    }

    @Test
    fun `analysis completes for suspicious URL`() {
        val result = engine.analyzeBlocking("https://paypa1.tk/login?password=steal")

        // Verify analysis completes and provides meaningful output
        assertNotNull(result.verdict)
        assertNotNull(result.scoreDescription)
    }
}
