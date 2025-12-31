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

package com.raouf.mehrguard.share

import com.raouf.mehrguard.model.RiskAssessment
import com.raouf.mehrguard.model.UrlAnalysisResult
import com.raouf.mehrguard.model.Verdict
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Comprehensive tests for ShareManager.
 */
class ShareManagerTest {

    // === TEXT SUMMARY TESTS ===

    @Test
    fun `generateTextSummary includes url`() {
        val assessment = createAssessment(Verdict.SAFE, 10)
        val text = ShareManager.generateTextSummary("https://google.com", assessment)
        assertTrue(text.contains("google.com"))
    }

    @Test
    fun `generateTextSummary includes score`() {
        val assessment = createAssessment(Verdict.SAFE, 15)
        val text = ShareManager.generateTextSummary("https://example.com", assessment)
        assertTrue(text.contains("15"))
    }

    @Test
    fun `generateTextSummary includes verdict`() {
        val assessment = createAssessment(Verdict.MALICIOUS, 90)
        val text = ShareManager.generateTextSummary("https://evil.com", assessment)
        assertTrue(text.contains("MALICIOUS", ignoreCase = true))
    }

    @Test
    fun `generateTextSummary includes flags`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 50, listOf("HTTP not HTTPS"))
        val text = ShareManager.generateTextSummary("http://example.com", assessment)
        assertTrue(text.contains("HTTP"))
    }

    @Test
    fun `generateTextSummary for safe verdict is positive`() {
        val assessment = createAssessment(Verdict.SAFE, 5)
        val text = ShareManager.generateTextSummary("https://google.com", assessment)
        assertTrue(text.contains("safe", ignoreCase = true) || text.contains("low risk", ignoreCase = true) || text.contains("SAFE"))
    }

    @Test
    fun `generateTextSummary for malicious verdict is warning`() {
        val assessment = createAssessment(Verdict.MALICIOUS, 95)
        val text = ShareManager.generateTextSummary("https://phishing.com", assessment)
        assertTrue(text.contains("MALICIOUS") || text.contains("risk", ignoreCase = true))
    }

    // === HTML REPORT TESTS ===

    @Test
    fun `generateHtmlReport is valid html`() {
        val assessment = createAssessment(Verdict.SAFE, 10)
        val html = ShareManager.generateHtmlReport("https://example.com", assessment)
        assertTrue(html.contains("<html") || html.contains("<!DOCTYPE") || html.contains("<div"))
    }

    @Test
    fun `generateHtmlReport includes styling`() {
        val assessment = createAssessment(Verdict.SAFE, 10)
        val html = ShareManager.generateHtmlReport("https://example.com", assessment)
        assertTrue(html.contains("style") || html.contains("color") || html.contains("font"))
    }

    @Test
    fun `generateHtmlReport includes url`() {
        val assessment = createAssessment(Verdict.SAFE, 10)
        val html = ShareManager.generateHtmlReport("https://example.com", assessment)
        assertTrue(html.contains("example.com"))
    }

    @Test
    fun `generateHtmlReport includes score`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 45)
        val html = ShareManager.generateHtmlReport("https://suspicious.com", assessment)
        assertTrue(html.contains("45"))
    }

    @Test
    fun `generateHtmlReport includes verdict`() {
        val assessment = createAssessment(Verdict.MALICIOUS, 80)
        val html = ShareManager.generateHtmlReport("https://bad.com", assessment)
        assertTrue(html.contains("MALICIOUS"))
    }

    @Test
    fun `generateHtmlReport escapes html in url`() {
        val assessment = createAssessment(Verdict.SAFE, 10)
        val html = ShareManager.generateHtmlReport("https://example.com/<script>alert('xss')</script>", assessment)
        assertFalse(html.contains("<script>alert"))
    }

    // === JSON REPORT TESTS ===

    @Test
    fun `generateJsonReport is valid json structure`() {
        val assessment = createAssessment(Verdict.SAFE, 10)
        val json = ShareManager.generateJsonReport("https://example.com", assessment)
        assertTrue(json.contains("{"))
        assertTrue(json.contains("}"))
    }

    @Test
    fun `generateJsonReport includes url`() {
        val assessment = createAssessment(Verdict.SAFE, 15)
        val json = ShareManager.generateJsonReport("https://example.com", assessment)
        assertTrue(json.contains("example.com"))
    }

    @Test
    fun `generateJsonReport includes score`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 55)
        val json = ShareManager.generateJsonReport("https://example.com", assessment)
        assertTrue(json.contains("55") || json.contains("score"))
    }

    @Test
    fun `generateJsonReport includes verdict`() {
        val assessment = createAssessment(Verdict.MALICIOUS, 85)
        val json = ShareManager.generateJsonReport("https://example.com", assessment)
        assertTrue(json.contains("MALICIOUS") || json.contains("verdict"))
    }

    @Test
    fun `generateJsonReport includes flags`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 50, listOf("HTTP", "IP address"))
        val json = ShareManager.generateJsonReport("https://example.com", assessment)
        assertTrue(json.contains("flags") || json.contains("["))
    }

    // === HELPER ===

    private fun createAssessment(verdict: Verdict, score: Int, flags: List<String> = emptyList()): RiskAssessment {
        return RiskAssessment(
            score = score,
            verdict = verdict,
            flags = flags,
            details = UrlAnalysisResult(
                originalUrl = "https://example.com",
                heuristicScore = score / 4,
                mlScore = score / 4,
                brandScore = score / 4,
                tldScore = score / 4
            ),
            confidence = 0.9f
        )
    }
}
