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
import com.qrshield.core.UrlAnalyzer
import com.qrshield.engine.BrandDetector
import com.qrshield.engine.HeuristicsEngine
import com.qrshield.engine.TldScorer
import com.qrshield.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertNull

/**
 * Unit tests for PhishingEngine and related components.
 * Tests cover the core phishing detection logic.
 */
class PhishingEngineTest {

    private val engine = PhishingEngine()

    @Test
    fun `safe URL returns low risk score`() {
        val result = engine.analyzeBlocking("https://www.google.com")

        assertTrue(result.score <= 30, "Expected safe score <= 30, got ${result.score}")
        assertEquals(Verdict.SAFE, result.verdict)
    }

    @Test
    fun `HTTP URL adds risk points`() {
        val httpResult = engine.analyzeBlocking("http://example.com")
        val httpsResult = engine.analyzeBlocking("https://example.com")

        assertTrue(
            httpResult.score > httpsResult.score,
            "HTTP (${httpResult.score}) should score higher than HTTPS (${httpsResult.score})"
        )
    }

    @Test
    fun `IP address host flagged as suspicious`() {
        val result = engine.analyzeBlocking("http://192.168.1.1/login")

        assertTrue(result.score >= 20, "IP host should increase risk score significantly")
    }

    @Test
    fun `combined score calculated correctly`() {
        val result = engine.analyzeBlocking("http://192.168.1.1:8080/login.php?password=test")

        // Should have multiple risk factors
        assertTrue(result.score >= 40, "Multiple risk factors should produce high score")
        assertTrue(result.flags.size >= 2, "Expected multiple flags, got ${result.flags.size}")
    }

    @Test
    fun `verdict thresholds are correct`() {
        // These are synthetic URLs to test threshold boundaries
        val safeResult = engine.analyzeBlocking("https://www.github.com")
        assertEquals(Verdict.SAFE, safeResult.verdict, "Score ${safeResult.score} should be SAFE")

        val maliciousResult = engine.analyzeBlocking("http://paypa1-secure.tk/login?password=steal")
        assertTrue(
            maliciousResult.verdict == Verdict.MALICIOUS || maliciousResult.verdict == Verdict.SUSPICIOUS,
            "Obvious phishing should be flagged as risky"
        )
    }
}

class UrlAnalyzerTest {

    private val analyzer = UrlAnalyzer()

    @Test
    fun `parse extracts all URL components`() {
        val result = analyzer.parse("https://sub.example.com:8080/path?query=1#fragment")

        assertNotNull(result)
        assertEquals("https", result.protocol)
        assertEquals("sub.example.com", result.host)
        assertEquals(8080, result.port)
        assertEquals("/path", result.path)
        assertEquals("query=1", result.query)
        assertEquals("fragment", result.fragment)
    }

    @Test
    fun `parse handles simple URLs`() {
        val result = analyzer.parse("https://example.com")

        assertNotNull(result)
        assertEquals("https", result.protocol)
        assertEquals("example.com", result.host)
        assertNull(result.port)
    }

    @Test
    fun `isIpAddress detects IPv4`() {
        assertTrue(analyzer.isIpAddress("192.168.1.1"))
        assertTrue(analyzer.isIpAddress("10.0.0.1"))
        assertTrue(analyzer.isIpAddress("255.255.255.255"))
        assertTrue(!analyzer.isIpAddress("example.com"))
        assertTrue(!analyzer.isIpAddress("not-an-ip"))
    }

    @Test
    fun `isShortener detects known services`() {
        assertTrue(analyzer.isShortener("bit.ly"))
        assertTrue(analyzer.isShortener("tinyurl.com"))
        assertTrue(analyzer.isShortener("t.co"))
        assertTrue(!analyzer.isShortener("google.com"))
        assertTrue(!analyzer.isShortener("example.org"))
    }

    @Test
    fun `entropy calculation works correctly`() {
        val lowEntropy = analyzer.calculateEntropy("aaaaaa")
        val highEntropy = analyzer.calculateEntropy("a1b2c3d4e5")

        assertTrue(highEntropy > lowEntropy, "Random string should have higher entropy")
        assertEquals(0.0, analyzer.calculateEntropy(""), "Empty string should have 0 entropy")
    }

    @Test
    fun `suspicious keywords detected in path`() {
        assertEquals(0, analyzer.countSuspiciousPathKeywords("/"))
        assertTrue(analyzer.countSuspiciousPathKeywords("/login") > 0)
        assertTrue(analyzer.countSuspiciousPathKeywords("/secure/verify/account") > 0)
    }

    @Test
    fun `credential params detected`() {
        assertTrue(analyzer.hasCredentialParams("password=secret"))
        assertTrue(analyzer.hasCredentialParams("token=abc123"))
        assertTrue(!analyzer.hasCredentialParams("page=1"))
        assertTrue(!analyzer.hasCredentialParams(null))
    }
}

class BrandDetectorTest {

    private val detector = BrandDetector()

    @Test
    fun `detects exact brand in subdomain`() {
        val result = detector.detect("https://paypal.evil-site.com")

        assertTrue(result.score > 0, "Brand in subdomain should be detected")
        assertEquals("paypal", result.match)
    }

    @Test
    fun `detects typosquat variant`() {
        val result = detector.detect("https://paypa1.com")

        assertTrue(result.score > 0, "Typosquat should be detected")
        assertEquals("paypal", result.match)
        assertNotNull(result.details)
        assertEquals(BrandDetector.MatchType.TYPOSQUAT, result.details?.matchType)
    }

    @Test
    fun `official domain not flagged`() {
        val result = detector.detect("https://www.paypal.com")

        assertEquals(0, result.score, "Official domain should not be flagged")
        assertNull(result.match)
    }

    @Test
    fun `multiple brands detected correctly`() {
        val googleResult = detector.detect("https://g00gle.com")
        assertEquals("google", googleResult.match)

        val amazonResult = detector.detect("https://amaz0n-deals.com")
        assertEquals("amazon", amazonResult.match)
    }
}

class TldScorerTest {

    private val scorer = TldScorer()

    @Test
    fun `safe TLD gets low score`() {
        val result = scorer.score("https://example.com")

        assertEquals(TldScorer.SCORE_SAFE, result.score)
        assertEquals(TldScorer.RiskCategory.SAFE, result.riskCategory)
        assertTrue(!result.isHighRisk)
    }

    @Test
    fun `high risk TLD flagged`() {
        val tkResult = scorer.score("https://example.tk")
        assertTrue(tkResult.isHighRisk)
        assertEquals(TldScorer.RiskCategory.FREE_TIER, tkResult.riskCategory)

        val mlResult = scorer.score("https://example.ml")
        assertTrue(mlResult.isHighRisk)
    }

    @Test
    fun `moderate risk TLD scored appropriately`() {
        val result = scorer.score("https://example.xyz")

        assertTrue(result.score > TldScorer.SCORE_SAFE)
        assertTrue(result.isHighRisk)
    }

    @Test
    fun `country code TLD handled`() {
        val result = scorer.score("https://example.uk")

        assertEquals(TldScorer.RiskCategory.COUNTRY_CODE, result.riskCategory)
        assertTrue(!result.isHighRisk)
    }
}

class HeuristicsEngineTest {

    private val engine = HeuristicsEngine()

    @Test
    fun `HTTP not HTTPS flagged`() {
        val result = engine.analyze("http://example.com")

        assertTrue(result.score > 0)
        assertTrue(result.flags.any { it.contains("HTTP", ignoreCase = true) || it.contains("insecure", ignoreCase = true) })
    }

    @Test
    fun `excessive subdomains flagged`() {
        val result = engine.analyze("https://a.b.c.d.example.com")

        assertTrue(result.flags.any { it.contains("subdomain", ignoreCase = true) })
    }

    @Test
    fun `long URL flagged`() {
        val longPath = "a".repeat(250)
        val result = engine.analyze("https://example.com/$longPath")

        assertTrue(result.flags.any { it.contains("long", ignoreCase = true) })
    }

    @Test
    fun `credential params detected`() {
        val result = engine.analyze("https://example.com/login?password=test")

        assertTrue(result.flags.any {
            it.contains("credential", ignoreCase = true) ||
            it.contains("password", ignoreCase = true)
        })
    }

    @Test
    fun `IP address host detected`() {
        val result = engine.analyze("http://192.168.1.1/admin")

        assertTrue(result.flags.any { it.contains("IP", ignoreCase = true) })
    }

    @Test
    fun `URL shortener detected`() {
        val result = engine.analyze("https://bit.ly/abc123")

        assertTrue(result.flags.any { it.contains("shortener", ignoreCase = true) || it.contains("shorten", ignoreCase = true) })
    }

    @Test
    fun `safe URL has low score`() {
        val result = engine.analyze("https://www.github.com/user/repo")

        assertTrue(result.score < 30, "Safe URL should have low heuristic score")
    }
}
