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

package com.raouf.mehrguard.ui

import com.raouf.mehrguard.model.RiskAssessment
import com.raouf.mehrguard.model.UrlAnalysisResult
import com.raouf.mehrguard.model.Verdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for UI State classes and data structures.
 *
 * These tests validate the UI state management without requiring
 * full ViewModel instantiation (which requires platform-specific deps).
 */
class UiStateTest {

    // =========================================================================
    // UI STATE TESTS
    // =========================================================================

    @Test
    fun `UiState Idle is singleton`() {
        val state1 = UiState.Idle
        val state2 = UiState.Idle

        assertEquals(state1, state2)
    }

    @Test
    fun `UiState Scanning is singleton`() {
        val state1 = UiState.Scanning
        val state2 = UiState.Scanning

        assertEquals(state1, state2)
    }

    @Test
    fun `UiState Analyzing contains url`() {
        val state = UiState.Analyzing("https://example.com")

        assertEquals("https://example.com", state.url)
    }

    @Test
    fun `UiState Result contains assessment`() {
        val assessment = createTestAssessment(
            score = 10,
            verdict = Verdict.SAFE
        )

        val state = UiState.Result(assessment)

        assertEquals(assessment, state.assessment)
        assertEquals(10, state.assessment.score)
        assertEquals(Verdict.SAFE, state.assessment.verdict)
    }

    @Test
    fun `UiState Error contains message`() {
        val state = UiState.Error("Something went wrong")

        assertEquals("Something went wrong", state.message)
    }

    @Test
    fun `UiState different types are not equal`() {
        val idle: UiState = UiState.Idle
        val scanning: UiState = UiState.Scanning
        val analyzing: UiState = UiState.Analyzing("https://test.com")
        val result: UiState = UiState.Result(createTestAssessment(10, Verdict.SAFE))
        val error: UiState = UiState.Error("Error")

        // All states should be different via when exhaustiveness
        assertFalse(idle is UiState.Scanning)
        assertFalse(scanning is UiState.Analyzing)
        assertFalse(analyzing is UiState.Result)
        assertFalse(result is UiState.Error)
    }

    @Test
    fun `Two Analyzing with different urls are not equal`() {
        val state1 = UiState.Analyzing("https://example1.com")
        val state2 = UiState.Analyzing("https://example2.com")

        assertNotEquals(state1, state2)
    }

    @Test
    fun `Two Analyzing with same url are equal`() {
        val state1 = UiState.Analyzing("https://example.com")
        val state2 = UiState.Analyzing("https://example.com")

        assertEquals(state1, state2)
    }

    @Test
    fun `Two Error with different messages are not equal`() {
        val state1 = UiState.Error("Error 1")
        val state2 = UiState.Error("Error 2")

        assertNotEquals(state1, state2)
    }

    // =========================================================================
    // APP SETTINGS TESTS
    // =========================================================================

    @Test
    fun `AppSettings has default values`() {
        val settings = AppSettings()

        assertTrue(settings.isAutoScanEnabled)
        assertTrue(settings.isHapticEnabled)
        assertTrue(settings.isSoundEnabled)
        assertTrue(settings.isSaveHistoryEnabled)
        assertTrue(settings.isSecurityAlertsEnabled)
    }

    @Test
    fun `AppSettings can be customized`() {
        val settings = AppSettings(
            isAutoScanEnabled = false,
            isHapticEnabled = false,
            isSoundEnabled = true,
            isSaveHistoryEnabled = false,
            isSecurityAlertsEnabled = true
        )

        assertEquals(false, settings.isAutoScanEnabled)
        assertEquals(false, settings.isHapticEnabled)
        assertEquals(true, settings.isSoundEnabled)
        assertEquals(false, settings.isSaveHistoryEnabled)
        assertEquals(true, settings.isSecurityAlertsEnabled)
    }

    @Test
    fun `AppSettings equality works`() {
        val settings1 = AppSettings(isAutoScanEnabled = true)
        val settings2 = AppSettings(isAutoScanEnabled = true)
        val settings3 = AppSettings(isAutoScanEnabled = false)

        assertEquals(settings1, settings2)
        assertNotEquals(settings1, settings3)
    }

    @Test
    fun `AppSettings copy works`() {
        val original = AppSettings()
        val modified = original.copy(isHapticEnabled = false)

        assertTrue(original.isHapticEnabled)
        assertFalse(modified.isHapticEnabled)
    }

    // =========================================================================
    // HISTORY STATISTICS TESTS
    // =========================================================================

    @Test
    fun `HistoryStatistics contains all counts`() {
        val stats = HistoryStatistics(
            totalScans = 100,
            safeCount = 70,
            suspiciousCount = 20,
            maliciousCount = 10,
            averageScore = 25.5
        )

        assertEquals(100, stats.totalScans)
        assertEquals(70, stats.safeCount)
        assertEquals(20, stats.suspiciousCount)
        assertEquals(10, stats.maliciousCount)
        assertEquals(25.5, stats.averageScore, 0.01)
    }

    @Test
    fun `HistoryStatistics calculates safe percentage`() {
        val stats = HistoryStatistics(
            totalScans = 100,
            safeCount = 70,
            suspiciousCount = 20,
            maliciousCount = 10,
            averageScore = 25.0
        )

        assertEquals(70.0, stats.safePercentage, 0.01)
    }

    @Test
    fun `HistoryStatistics calculates threat percentage`() {
        val stats = HistoryStatistics(
            totalScans = 100,
            safeCount = 70,
            suspiciousCount = 20,
            maliciousCount = 10,
            averageScore = 25.0
        )

        assertEquals(30.0, stats.threatPercentage, 0.01)
    }

    @Test
    fun `HistoryStatistics handles empty data`() {
        val stats = HistoryStatistics(
            totalScans = 0,
            safeCount = 0,
            suspiciousCount = 0,
            maliciousCount = 0,
            averageScore = 0.0
        )

        assertEquals(0.0, stats.safePercentage, 0.01)
        assertEquals(0.0, stats.threatPercentage, 0.01)
    }

    @Test
    fun `HistoryStatistics handles single scan`() {
        val stats = HistoryStatistics(
            totalScans = 1,
            safeCount = 1,
            suspiciousCount = 0,
            maliciousCount = 0,
            averageScore = 5.0
        )

        assertEquals(100.0, stats.safePercentage, 0.01)
        assertEquals(0.0, stats.threatPercentage, 0.01)
    }

    // =========================================================================
    // RESULT STATE VERDICT TESTS
    // =========================================================================

    @Test
    fun `Result state with SAFE verdict`() {
        val state = UiState.Result(createTestAssessment(5, Verdict.SAFE))

        assertEquals(Verdict.SAFE, state.assessment.verdict)
        assertTrue(state.assessment.score <= 15)
    }

    @Test
    fun `Result state with SUSPICIOUS verdict`() {
        val state = UiState.Result(createTestAssessment(35, Verdict.SUSPICIOUS))

        assertEquals(Verdict.SUSPICIOUS, state.assessment.verdict)
        assertTrue(state.assessment.score > 15)
        assertTrue(state.assessment.score <= 50)
    }

    @Test
    fun `Result state with MALICIOUS verdict`() {
        val state = UiState.Result(createTestAssessment(75, Verdict.MALICIOUS))

        assertEquals(Verdict.MALICIOUS, state.assessment.verdict)
        assertTrue(state.assessment.score > 50)
    }

    @Test
    fun `Result state with UNKNOWN verdict`() {
        val state = UiState.Result(createTestAssessment(0, Verdict.UNKNOWN))

        assertEquals(Verdict.UNKNOWN, state.assessment.verdict)
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private fun createTestAssessment(score: Int, verdict: Verdict): RiskAssessment {
        return RiskAssessment(
            score = score,
            verdict = verdict,
            flags = emptyList(),
            details = UrlAnalysisResult(
                originalUrl = "https://example.com",
                heuristicScore = 0,
                mlScore = 0,
                brandScore = 0,
                tldScore = 0
            ),
            confidence = 0.8f
        )
    }
}
