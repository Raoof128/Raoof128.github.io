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

package com.raouf.mehrguard.gamification

import com.raouf.mehrguard.core.PhishingEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for BeatTheBotViewModel.
 *
 * Tests game state management, difficulty scaling, and achievement system.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BeatTheBotViewModelTest {

    private fun createViewModel(scope: TestScope): BeatTheBotViewModel {
        return BeatTheBotViewModel(
            engine = PhishingEngine(),
            scope = scope
        )
    }

    // =========================================================================
    // GAME STATE TESTS
    // =========================================================================

    @Test
    fun `initial state is Idle`() = runTest {
        val viewModel = createViewModel(this)

        assertEquals(
            BeatTheBotViewModel.GamePhase.Idle,
            viewModel.state.value.phase
        )
    }

    @Test
    fun `startGame transitions to Playing phase`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.startGame()

        assertTrue(viewModel.state.value.phase is BeatTheBotViewModel.GamePhase.Playing)
        assertEquals(0, viewModel.state.value.score)
        assertEquals(0, viewModel.state.value.roundsPlayed)
    }

    @Test
    fun `resetGame returns to initial state`() = runTest {
        val viewModel = createViewModel(this)

        viewModel.startGame()
        viewModel.submitChallenge("https://test.com")
        advanceUntilIdle()

        viewModel.resetGame()

        assertEquals(BeatTheBotViewModel.GamePhase.Idle, viewModel.state.value.phase)
        assertEquals(0, viewModel.state.value.score)
        assertEquals(0, viewModel.state.value.roundsPlayed)
    }

    // =========================================================================
    // INPUT VALIDATION TESTS
    // =========================================================================

    @Test
    fun `empty URL is rejected`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.startGame()

        viewModel.submitChallenge("")
        advanceUntilIdle()

        val phase = viewModel.state.value.phase
        assertTrue(phase is BeatTheBotViewModel.GamePhase.ShowingResult)
        assertEquals(
            BeatTheBotViewModel.RoundOutcome.INVALID_INPUT,
            (phase as BeatTheBotViewModel.GamePhase.ShowingResult).result.outcome
        )
    }

    @Test
    fun `URL without protocol is rejected`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.startGame()

        viewModel.submitChallenge("example.com")
        advanceUntilIdle()

        val phase = viewModel.state.value.phase
        assertTrue(phase is BeatTheBotViewModel.GamePhase.ShowingResult)
        assertEquals(
            BeatTheBotViewModel.RoundOutcome.INVALID_INPUT,
            (phase as BeatTheBotViewModel.GamePhase.ShowingResult).result.outcome
        )
    }

    @Test
    fun `valid URL transitions to Analyzing then ShowingResult`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.startGame()

        viewModel.submitChallenge("https://example.com")

        // Should transition to Analyzing
        val analyzingPhase = viewModel.state.value.phase
        assertTrue(
            analyzingPhase is BeatTheBotViewModel.GamePhase.Analyzing,
            "Expected Analyzing phase, got $analyzingPhase"
        )

        advanceUntilIdle()

        // Should transition to ShowingResult
        val resultPhase = viewModel.state.value.phase
        assertTrue(
            resultPhase is BeatTheBotViewModel.GamePhase.ShowingResult,
            "Expected ShowingResult phase, got $resultPhase"
        )
    }

    // =========================================================================
    // SCORING TESTS
    // =========================================================================

    @Test
    fun `bot wins increments bot score`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.startGame()

        // Submit an obvious phishing URL that should be detected
        viewModel.submitChallenge("https://paypa1-secure.tk/login")
        advanceUntilIdle()

        // Bot should have won
        assertTrue(viewModel.state.value.botScore > 0 || viewModel.state.value.botWins > 0)
    }

    @Test
    fun `rounds played increments after each challenge`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.startGame()
        assertEquals(0, viewModel.state.value.roundsPlayed)

        viewModel.submitChallenge("https://example.com")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.roundsPlayed)
    }

    // =========================================================================
    // DIFFICULTY SCALING TESTS
    // =========================================================================

    @Test
    fun `difficulty starts at BEGINNER`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.startGame()

        assertEquals(
            PhishingChallengeDataset.Difficulty.BEGINNER,
            viewModel.state.value.difficulty
        )
    }

    @Test
    fun `difficulty calculation is deterministic`() {
        // Test with known score values
        assertEquals(
            PhishingChallengeDataset.Difficulty.BEGINNER,
            PhishingChallengeDataset.calculateDifficulty(0, 0)
        )
        assertEquals(
            PhishingChallengeDataset.Difficulty.INTERMEDIATE,
            PhishingChallengeDataset.calculateDifficulty(50, 0)
        )
        assertEquals(
            PhishingChallengeDataset.Difficulty.ADVANCED,
            PhishingChallengeDataset.calculateDifficulty(100, 0)
        )
        assertEquals(
            PhishingChallengeDataset.Difficulty.EXPERT,
            PhishingChallengeDataset.calculateDifficulty(200, 0)
        )
        assertEquals(
            PhishingChallengeDataset.Difficulty.NIGHTMARE,
            PhishingChallengeDataset.calculateDifficulty(300, 0)
        )
    }

    @Test
    fun `streak affects difficulty`() {
        // Streak should add to effective score
        assertEquals(
            PhishingChallengeDataset.Difficulty.INTERMEDIATE,
            PhishingChallengeDataset.calculateDifficulty(40, 1) // 40 + 10 = 50
        )
        assertEquals(
            PhishingChallengeDataset.Difficulty.ADVANCED,
            PhishingChallengeDataset.calculateDifficulty(90, 1) // 90 + 10 = 100
        )
    }

    // =========================================================================
    // HISTORY TESTS
    // =========================================================================

    @Test
    fun `history is updated after challenge`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.startGame()
        assertTrue(viewModel.state.value.history.isEmpty())

        viewModel.submitChallenge("https://example.com")
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.history.size)
    }

    @Test
    fun `history contains correct result data`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.startGame()

        val testUrl = "https://test-example.com"
        viewModel.submitChallenge(testUrl)
        advanceUntilIdle()

        val historyEntry = viewModel.state.value.history.firstOrNull()
        assertNotNull(historyEntry)
        assertEquals(testUrl, historyEntry.url)
    }

    // =========================================================================
    // NEXT ROUND TESTS
    // =========================================================================

    @Test
    fun `nextRound transitions to Playing`() = runTest {
        val viewModel = createViewModel(this)
        viewModel.startGame()
        viewModel.submitChallenge("https://example.com")
        advanceUntilIdle()

        viewModel.nextRound()

        assertTrue(viewModel.state.value.phase is BeatTheBotViewModel.GamePhase.Playing)
    }
}

/**
 * Tests for PhishingChallengeDataset.
 */
class PhishingChallengeDatasetTest {

    @Test
    fun `dataset has challenges for all difficulties`() {
        for (difficulty in PhishingChallengeDataset.Difficulty.entries) {
            val challenges = PhishingChallengeDataset.getChallengesForDifficulty(difficulty)
            assertTrue(
                challenges.isNotEmpty(),
                "Expected challenges for $difficulty"
            )
        }
    }

    @Test
    fun `challenges have valid URLs`() {
        for (challenge in PhishingChallengeDataset.challenges) {
            assertTrue(
                challenge.url.startsWith("http://") || challenge.url.startsWith("https://"),
                "Challenge ${challenge.id} has invalid URL: ${challenge.url}"
            )
        }
    }

    @Test
    fun `challenges have educational hints`() {
        for (challenge in PhishingChallengeDataset.challenges) {
            assertTrue(
                challenge.educationalHint.isNotBlank(),
                "Challenge ${challenge.id} missing educational hint"
            )
        }
    }

    @Test
    fun `getRandomChallenge returns challenge of correct difficulty`() {
        for (difficulty in PhishingChallengeDataset.Difficulty.entries) {
            val challenge = PhishingChallengeDataset.getRandomChallenge(difficulty)
            assertEquals(difficulty, challenge.difficulty)
        }
    }

    @Test
    fun `difficulty has correct scoring multiplier order`() {
        val difficulties = PhishingChallengeDataset.Difficulty.entries
        for (i in 0 until difficulties.size - 1) {
            assertTrue(
                difficulties[i].scoreMultiplier <= difficulties[i + 1].scoreMultiplier,
                "Score multiplier should increase with difficulty"
            )
        }
    }

    @Test
    fun `difficulty has decreasing bot reaction delay`() {
        val difficulties = PhishingChallengeDataset.Difficulty.entries
        for (i in 0 until difficulties.size - 1) {
            assertTrue(
                difficulties[i].botReactionDelayMs >= difficulties[i + 1].botReactionDelayMs,
                "Bot reaction delay should decrease with difficulty"
            )
        }
    }
}
