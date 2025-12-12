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

import com.qrshield.model.RiskAssessment
import com.qrshield.model.UrlAnalysisResult
import com.qrshield.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Comprehensive tests for VerdictEngine.
 */
class VerdictEngineTest {

    private val engine = VerdictEngine()

    // === ENRICH TESTS ===

    @Test
    fun `enrich safe assessment produces correct summary`() {
        val assessment = createAssessment(Verdict.SAFE, 15, emptyList())
        val enriched = engine.enrich(assessment)
        
        assertEquals(Verdict.SAFE, enriched.verdict)
        assertTrue(enriched.summary.contains("safe", ignoreCase = true))
        assertTrue(enriched.summary.contains("15"))
    }

    @Test
    fun `enrich suspicious assessment produces correct summary`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 50, listOf("HTTP"))
        val enriched = engine.enrich(assessment)
        
        assertEquals(Verdict.SUSPICIOUS, enriched.verdict)
        assertTrue(enriched.summary.contains("suspicious", ignoreCase = true))
        assertTrue(enriched.summary.contains("50"))
    }

    @Test
    fun `enrich malicious assessment produces correct summary`() {
        val assessment = createAssessment(Verdict.MALICIOUS, 85, listOf("Brand impersonation"))
        val enriched = engine.enrich(assessment)
        
        assertEquals(Verdict.MALICIOUS, enriched.verdict)
        assertTrue(enriched.summary.contains("malicious", ignoreCase = true))
        assertTrue(enriched.summary.contains("85"))
    }

    @Test
    fun `enrich unknown assessment produces correct summary`() {
        val assessment = createAssessment(Verdict.UNKNOWN, 0, emptyList())
        val enriched = engine.enrich(assessment)
        
        assertEquals(Verdict.UNKNOWN, enriched.verdict)
        assertTrue(enriched.summary.contains("unable", ignoreCase = true) || enriched.summary.contains("invalid", ignoreCase = true))
    }

    // === RECOMMENDATION TESTS ===

    @Test
    fun `safe recommendation allows proceeding`() {
        val assessment = createAssessment(Verdict.SAFE, 10, emptyList())
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.recommendation.contains("proceed", ignoreCase = true) || enriched.recommendation.contains("safe", ignoreCase = true))
    }

    @Test
    fun `suspicious recommendation warns user`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 50, listOf("Shortener"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.recommendation.contains("caution", ignoreCase = true) || enriched.recommendation.contains("verify", ignoreCase = true))
    }

    @Test
    fun `malicious recommendation blocks access`() {
        val assessment = createAssessment(Verdict.MALICIOUS, 90, listOf("Brand"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.recommendation.contains("DO NOT", ignoreCase = true) || enriched.recommendation.contains("not recommended", ignoreCase = true))
    }

    // === FLAG EXPLANATION TESTS ===

    @Test
    fun `HTTP flag produces encryption explanation`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 40, listOf("HTTP not HTTPS"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.riskFactorExplanations.any { 
            it.contains("HTTP", ignoreCase = true) || it.contains("encrypt", ignoreCase = true)
        })
    }

    @Test
    fun `IP address flag produces domain explanation`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 50, listOf("IP address host"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.riskFactorExplanations.any { 
            it.contains("IP", ignoreCase = true) 
        })
    }

    @Test
    fun `brand flag produces impersonation explanation`() {
        val assessment = createAssessment(Verdict.MALICIOUS, 80, listOf("Brand impersonation"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.riskFactorExplanations.any { 
            it.contains("Brand", ignoreCase = true) || it.contains("impersonate", ignoreCase = true)
        })
    }

    @Test
    fun `TLD flag produces risk explanation`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 60, listOf("High-risk TLD"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.riskFactorExplanations.any { 
            it.contains("TLD", ignoreCase = true) || it.contains("domain", ignoreCase = true)
        })
    }

    @Test
    fun `shortener flag produces hiding explanation`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 35, listOf("URL shortener"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.riskFactorExplanations.any { 
            it.contains("shortener", ignoreCase = true) || it.contains("hiding", ignoreCase = true)
        })
    }

    @Test
    fun `subdomain flag produces mislead explanation`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 45, listOf("Excessive subdomains"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.riskFactorExplanations.any { 
            it.contains("subdomain", ignoreCase = true)
        })
    }

    @Test
    fun `credential flag produces harvesting explanation`() {
        val assessment = createAssessment(Verdict.MALICIOUS, 75, listOf("Credential parameters"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.riskFactorExplanations.any { 
            it.contains("credential", ignoreCase = true)
        })
    }

    // === SAFETY TIPS TESTS ===

    @Test
    fun `safe verdict has safety tips`() {
        val assessment = createAssessment(Verdict.SAFE, 10, emptyList())
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.safetyTips.isNotEmpty())
        assertTrue(enriched.safetyTips.size >= 2)
    }

    @Test
    fun `suspicious verdict has safety tips`() {
        val assessment = createAssessment(Verdict.SUSPICIOUS, 50, listOf("HTTP"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.safetyTips.isNotEmpty())
        assertTrue(enriched.safetyTips.size >= 3)
    }

    @Test
    fun `malicious verdict has safety tips`() {
        val assessment = createAssessment(Verdict.MALICIOUS, 90, listOf("Brand"))
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.safetyTips.isNotEmpty())
        assertTrue(enriched.safetyTips.any { it.contains("DO NOT", ignoreCase = true) || it.contains("not click", ignoreCase = true) })
    }

    @Test
    fun `unknown verdict has safety tips`() {
        val assessment = createAssessment(Verdict.UNKNOWN, 0, emptyList())
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.safetyTips.isNotEmpty())
    }

    // === MULTIPLE FLAGS TESTS ===

    @Test
    fun `multiple flags all get explanations`() {
        val flags = listOf("HTTP not HTTPS", "IP address", "Brand impersonation", "High-risk TLD")
        val assessment = createAssessment(Verdict.MALICIOUS, 95, flags)
        val enriched = engine.enrich(assessment)
        
        assertEquals(flags.size, enriched.riskFactorExplanations.size)
    }

    @Test
    fun `empty flags produces empty explanations`() {
        val assessment = createAssessment(Verdict.SAFE, 5, emptyList())
        val enriched = engine.enrich(assessment)
        
        assertTrue(enriched.riskFactorExplanations.isEmpty())
    }

    // === HELPER ===

    private fun createAssessment(verdict: Verdict, score: Int, flags: List<String>): RiskAssessment {
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
