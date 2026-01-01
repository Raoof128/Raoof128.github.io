/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

package com.raouf.mehrguard.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Tests for data model classes.
 */
class ModelsTest {

    // === VERDICT ENUM TESTS ===

    @Test
    fun `verdict has safe value`() {
        assertEquals("SAFE", Verdict.SAFE.name)
    }

    @Test
    fun `verdict has suspicious value`() {
        assertEquals("SUSPICIOUS", Verdict.SUSPICIOUS.name)
    }

    @Test
    fun `verdict has malicious value`() {
        assertEquals("MALICIOUS", Verdict.MALICIOUS.name)
    }

    @Test
    fun `verdict has unknown value`() {
        assertEquals("UNKNOWN", Verdict.UNKNOWN.name)
    }

    @Test
    fun `verdict entries count is 4`() {
        assertEquals(4, Verdict.entries.size)
    }

    // === RISK ASSESSMENT TESTS ===

    @Test
    fun `risk assessment has score description for low`() {
        val assessment = createAssessment(15, Verdict.SAFE)
        assertEquals("Low Risk", assessment.scoreDescription)
    }

    @Test
    fun `risk assessment has score description for medium`() {
        val assessment = createAssessment(50, Verdict.SUSPICIOUS)
        assertEquals("Medium Risk", assessment.scoreDescription)
    }

    @Test
    fun `risk assessment has score description for high`() {
        val assessment = createAssessment(80, Verdict.MALICIOUS)
        assertEquals("High Risk", assessment.scoreDescription)
    }

    @Test
    fun `risk assessment has action recommendation for safe`() {
        val assessment = createAssessment(10, Verdict.SAFE)
        assertTrue(assessment.actionRecommendation.contains("safe", ignoreCase = true))
    }

    @Test
    fun `risk assessment has action recommendation for suspicious`() {
        val assessment = createAssessment(50, Verdict.SUSPICIOUS)
        assertTrue(assessment.actionRecommendation.contains("caution", ignoreCase = true))
    }

    @Test
    fun `risk assessment has action recommendation for malicious`() {
        val assessment = createAssessment(90, Verdict.MALICIOUS)
        assertTrue(assessment.actionRecommendation.contains("not", ignoreCase = true))
    }

    @Test
    fun `risk assessment has action recommendation for unknown`() {
        val assessment = createAssessment(0, Verdict.UNKNOWN)
        assertTrue(assessment.actionRecommendation.contains("verify", ignoreCase = true) ||
                   assessment.actionRecommendation.contains("unable", ignoreCase = true))
    }

    // === URL ANALYSIS RESULT TESTS ===

    @Test
    fun `url analysis result empty factory works`() {
        val empty = UrlAnalysisResult.empty()
        assertEquals("", empty.originalUrl)
        assertEquals(0, empty.heuristicScore)
        assertEquals(0, empty.mlScore)
        assertEquals(0, empty.brandScore)
        assertEquals(0, empty.tldScore)
    }

    @Test
    fun `url analysis result has original url`() {
        val result = UrlAnalysisResult(
            originalUrl = "https://example.com",
            heuristicScore = 10,
            mlScore = 20,
            brandScore = 0,
            tldScore = 0
        )
        assertEquals("https://example.com", result.originalUrl)
    }

    // === SCAN RESULT TESTS ===

    @Test
    fun `scan result success holds content`() {
        val result = ScanResult.Success("https://google.com", ContentType.URL)
        assertEquals("https://google.com", result.content)
        assertEquals(ContentType.URL, result.contentType)
    }

    @Test
    fun `scan result error holds message`() {
        val result = ScanResult.Error("Camera error", ErrorCode.CAMERA_ERROR)
        assertEquals("Camera error", result.message)
        assertEquals(ErrorCode.CAMERA_ERROR, result.code)
    }

    @Test
    fun `scan result no qr found exists`() {
        val result = ScanResult.NoQrFound
        assertNotNull(result)
    }

    // === CONTENT TYPE TESTS ===

    @Test
    fun `content type has url`() {
        assertEquals("URL", ContentType.URL.name)
    }

    @Test
    fun `content type has text`() {
        assertEquals("TEXT", ContentType.TEXT.name)
    }

    @Test
    fun `content type has wifi`() {
        assertEquals("WIFI", ContentType.WIFI.name)
    }

    @Test
    fun `content type has vcard`() {
        assertEquals("VCARD", ContentType.VCARD.name)
    }

    @Test
    fun `content type entries includes all types`() {
        assertTrue(ContentType.entries.size >= 8)
    }

    // === ERROR CODE TESTS ===

    @Test
    fun `error code has camera permission denied`() {
        assertEquals("CAMERA_PERMISSION_DENIED", ErrorCode.CAMERA_PERMISSION_DENIED.name)
    }

    @Test
    fun `error code has camera error`() {
        assertEquals("CAMERA_ERROR", ErrorCode.CAMERA_ERROR.name)
    }

    @Test
    fun `error code entries includes common errors`() {
        assertTrue(ErrorCode.entries.size >= 10)
    }

    // === SCAN HISTORY ITEM TESTS ===

    @Test
    fun `scan history item has all fields`() {
        val item = ScanHistoryItem(
            id = "123",
            url = "https://google.com",
            score = 10,
            verdict = Verdict.SAFE,
            scannedAt = 1234567890L,
            source = ScanSource.CAMERA
        )

        assertEquals("123", item.id)
        assertEquals("https://google.com", item.url)
        assertEquals(10, item.score)
        assertEquals(Verdict.SAFE, item.verdict)
        assertEquals(1234567890L, item.scannedAt)
        assertEquals(ScanSource.CAMERA, item.source)
    }

    // === SCAN SOURCE TESTS ===

    @Test
    fun `scan source has camera`() {
        assertEquals("CAMERA", ScanSource.CAMERA.name)
    }

    @Test
    fun `scan source has gallery`() {
        assertEquals("GALLERY", ScanSource.GALLERY.name)
    }

    @Test
    fun `scan source has clipboard`() {
        assertEquals("CLIPBOARD", ScanSource.CLIPBOARD.name)
    }

    // === HELPER ===

    private fun createAssessment(score: Int, verdict: Verdict): RiskAssessment {
        return RiskAssessment(
            score = score,
            verdict = verdict,
            flags = emptyList(),
            details = UrlAnalysisResult.empty(),
            confidence = 0.9f
        )
    }
}
