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

package com.qrshield.gamification

import com.qrshield.core.PhishingEngine
import com.qrshield.model.Verdict
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Beat the Bot ViewModel
 *
 * Manages game state for the adversarial phishing challenge game.
 * Implements proper state management that survives configuration changes
 * and follows MVVM architecture patterns.
 *
 * ## Game Phases
 * - **Idle**: Game not started, showing intro
 * - **Playing**: Active challenge in progress
 * - **Analyzing**: Bot is analyzing the submitted URL
 * - **ShowingResult**: Displaying round outcome
 * - **GameOver**: Game ended (optional, for timed modes)
 *
 * ## Difficulty Scaling
 * Difficulty increases deterministically based on:
 * - Current score
 * - Win streak
 * - Rounds played
 *
 * @param engine PhishingEngine for URL analysis
 * @param scope CoroutineScope for async operations
 * @author QR-SHIELD Security Team
 * @since 1.4.0
 */
class BeatTheBotViewModel(
    private val engine: PhishingEngine = PhishingEngine(),
    private val scope: CoroutineScope
) {
    // =========================================================================
    // STATE
    // =========================================================================

    /**
     * Sealed class representing all possible game phases.
     */
    sealed class GamePhase {
        data object Idle : GamePhase()
        data class Playing(val challenge: PhishingChallengeDataset.PhishingChallenge? = null) : GamePhase()
        data class Analyzing(val url: String, val startTimeMs: Long) : GamePhase()
        data class ShowingResult(val result: RoundResult) : GamePhase()
        data class Won(val finalScore: Int, val achievements: List<BeatTheBot.Achievement>) : GamePhase()
        data class Lost(val reason: String, val finalScore: Int) : GamePhase()
    }

    /**
     * Result of a single round.
     */
    data class RoundResult(
        val outcome: RoundOutcome,
        val url: String,
        val userTimeMs: Long,
        val botTimeMs: Long,
        val score: Int,
        val verdict: Verdict,
        val detectedSignals: List<String>,
        val pointsEarned: Int,
        val tip: String
    )

    /**
     * Possible round outcomes.
     */
    enum class RoundOutcome {
        USER_WINS,      // User submitted phishing URL that evaded detection
        BOT_WINS,       // Bot detected the phishing attempt
        FALSE_POSITIVE, // Bot flagged a legitimate URL
        INVALID_INPUT   // User input was not a valid URL
    }

    /**
     * Full game state container.
     */
    data class GameState(
        val phase: GamePhase = GamePhase.Idle,
        val score: Int = 0,
        val botScore: Int = 0,
        val roundsPlayed: Int = 0,
        val userWins: Int = 0,
        val botWins: Int = 0,
        val falseAlarms: Int = 0,
        val currentStreak: Int = 0,
        val bestStreak: Int = 0,
        val difficulty: PhishingChallengeDataset.Difficulty = PhishingChallengeDataset.Difficulty.BEGINNER,
        val currentChallenge: PhishingChallengeDataset.PhishingChallenge? = null,
        val history: List<RoundResult> = emptyList(),
        val achievements: List<BeatTheBot.Achievement> = emptyList(),
        val lastInputUrl: String = "",
        val botReactionProgress: Float = 0f, // 0.0 to 1.0 for animation
        val showConfetti: Boolean = false
    ) {
        val totalGames: Int get() = userWins + botWins + falseAlarms
        val userWinRate: Float get() = if (totalGames > 0) userWins.toFloat() / totalGames else 0f
        val botWinRate: Float get() = if (totalGames > 0) botWins.toFloat() / totalGames else 0f
        
        val leaderboardRank: String get() = when {
            score >= 500 -> "🏆 Elite Evader (Top 1%)"
            score >= 250 -> "🥇 Master Trickster (Top 5%)"
            score >= 100 -> "🥈 Skilled Attacker (Top 20%)"
            score >= 50 -> "🥉 Apprentice (Top 50%)"
            else -> "🔰 Beginner"
        }
    }

    // Private mutable state
    private val _state = MutableStateFlow(GameState())
    
    /**
     * Observable game state for UI.
     */
    val state: StateFlow<GameState> = _state.asStateFlow()

    // =========================================================================
    // GAME ACTIONS
    // =========================================================================

    /**
     * Start a new game session.
     */
    fun startGame() {
        _state.update { 
            GameState(
                phase = GamePhase.Playing(),
                difficulty = PhishingChallengeDataset.Difficulty.BEGINNER
            )
        }
    }

    /**
     * Submit a URL challenge from the user.
     *
     * @param url The URL the user thinks will evade detection
     * @param claimedSafe If true, user claims this is a legitimate URL being falsely flagged
     */
    fun submitChallenge(url: String, claimedSafe: Boolean = false) {
        val trimmedUrl = url.trim()
        
        // Validate input
        if (trimmedUrl.isBlank()) {
            _state.update { it.copy(
                phase = GamePhase.ShowingResult(
                    RoundResult(
                        outcome = RoundOutcome.INVALID_INPUT,
                        url = url,
                        userTimeMs = 0,
                        botTimeMs = 0,
                        score = 0,
                        verdict = Verdict.SAFE,
                        detectedSignals = emptyList(),
                        pointsEarned = 0,
                        tip = "URL cannot be empty"
                    )
                )
            )}
            return
        }
        
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            _state.update { it.copy(
                phase = GamePhase.ShowingResult(
                    RoundResult(
                        outcome = RoundOutcome.INVALID_INPUT,
                        url = url,
                        userTimeMs = 0,
                        botTimeMs = 0,
                        score = 0,
                        verdict = Verdict.SAFE,
                        detectedSignals = emptyList(),
                        pointsEarned = 0,
                        tip = "URL must start with http:// or https://"
                    )
                )
            )}
            return
        }

        val startTime = Clock.System.now().toEpochMilliseconds()
        
        // Transition to analyzing state
        _state.update { it.copy(
            phase = GamePhase.Analyzing(trimmedUrl, startTime),
            lastInputUrl = trimmedUrl,
            botReactionProgress = 0f
        )}

        scope.launch {
            // Simulate bot reaction time based on difficulty
            val currentDifficulty = _state.value.difficulty
            val botReactionTime = currentDifficulty.botReactionDelayMs
            
            // Animate bot reaction progress
            val animationSteps = 10
            val stepDelay = botReactionTime / animationSteps
            for (i in 1..animationSteps) {
                delay(stepDelay)
                _state.update { it.copy(botReactionProgress = i.toFloat() / animationSteps) }
            }

            // Analyze URL
            val result = engine.analyzeBlocking(trimmedUrl)
            val analysisEndTime = Clock.System.now().toEpochMilliseconds()
            val botTimeMs = analysisEndTime - startTime
            
            val isDetected = result.verdict == Verdict.MALICIOUS || result.verdict == Verdict.SUSPICIOUS
            
            // Determine outcome
            val (outcome, pointsEarned, tip) = when {
                claimedSafe && isDetected -> {
                    Triple(
                        RoundOutcome.FALSE_POSITIVE,
                        5,
                        "Bot may have been too aggressive. Score: ${result.score}"
                    )
                }
                !claimedSafe && isDetected -> {
                    Triple(
                        RoundOutcome.BOT_WINS,
                        0,
                        getTipForSignals(result.flags)
                    )
                }
                !claimedSafe && !isDetected -> {
                    Triple(
                        RoundOutcome.USER_WINS,
                        calculatePoints(currentDifficulty),
                        "You successfully evaded detection!"
                    )
                }
                else -> {
                    Triple(
                        RoundOutcome.BOT_WINS,
                        0,
                        "This URL passed our checks. Try something trickier!"
                    )
                }
            }

            val roundResult = RoundResult(
                outcome = outcome,
                url = trimmedUrl,
                userTimeMs = 0, // User time not tracked in this mode
                botTimeMs = botTimeMs,
                score = result.score,
                verdict = result.verdict,
                detectedSignals = result.flags,
                pointsEarned = pointsEarned,
                tip = tip
            )
            
            // Update state based on outcome
            updateStateForResult(roundResult)
        }
    }

    /**
     * Continue to next round after seeing result.
     */
    fun nextRound() {
        val currentState = _state.value
        val newDifficulty = PhishingChallengeDataset.calculateDifficulty(
            currentState.score,
            currentState.currentStreak
        )
        
        _state.update { it.copy(
            phase = GamePhase.Playing(
                PhishingChallengeDataset.getRandomChallenge(newDifficulty)
            ),
            difficulty = newDifficulty,
            showConfetti = false
        )}
    }

    /**
     * Reset game to initial state.
     */
    fun resetGame() {
        _state.value = GameState()
    }

    /**
     * Use a challenge from the dataset (tutorial mode).
     */
    fun useChallenge(challenge: PhishingChallengeDataset.PhishingChallenge) {
        submitChallenge(challenge.url, claimedSafe = false)
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private fun updateStateForResult(result: RoundResult) {
        _state.update { current ->
            val newAchievements = mutableListOf<BeatTheBot.Achievement>()
            
            when (result.outcome) {
                RoundOutcome.USER_WINS -> {
                    val newStreak = current.currentStreak + 1
                    val newScore = current.score + result.pointsEarned
                    
                    // Check for achievements
                    if (current.userWins == 0) {
                        newAchievements.add(BeatTheBot.Achievement(
                            id = "first_blood",
                            title = "First Blood",
                            description = "Successfully evaded detection for the first time",
                            icon = "🩸"
                        ))
                    }
                    if (newStreak >= 3 && !hasAchievement(current, "hat_trick")) {
                        newAchievements.add(BeatTheBot.Achievement(
                            id = "hat_trick",
                            title = "Hat Trick",
                            description = "Evaded detection 3 times in a row",
                            icon = "🎩"
                        ))
                    }
                    if (newStreak >= 5 && !hasAchievement(current, "unstoppable")) {
                        newAchievements.add(BeatTheBot.Achievement(
                            id = "unstoppable",
                            title = "Unstoppable",
                            description = "Evaded detection 5 times in a row",
                            icon = "🔥"
                        ))
                    }
                    if (newScore >= 100 && !hasAchievement(current, "century")) {
                        newAchievements.add(BeatTheBot.Achievement(
                            id = "century",
                            title = "Century",
                            description = "Reached 100 points",
                            icon = "💯"
                        ))
                    }
                    
                    current.copy(
                        phase = GamePhase.ShowingResult(result),
                        score = newScore,
                        userWins = current.userWins + 1,
                        roundsPlayed = current.roundsPlayed + 1,
                        currentStreak = newStreak,
                        bestStreak = maxOf(current.bestStreak, newStreak),
                        history = current.history + result,
                        achievements = current.achievements + newAchievements,
                        showConfetti = true
                    )
                }
                RoundOutcome.BOT_WINS -> {
                    current.copy(
                        phase = GamePhase.ShowingResult(result),
                        botScore = current.botScore + 10,
                        botWins = current.botWins + 1,
                        roundsPlayed = current.roundsPlayed + 1,
                        currentStreak = 0, // Reset streak
                        history = current.history + result,
                        showConfetti = false
                    )
                }
                RoundOutcome.FALSE_POSITIVE -> {
                    current.copy(
                        phase = GamePhase.ShowingResult(result),
                        score = current.score + result.pointsEarned,
                        falseAlarms = current.falseAlarms + 1,
                        roundsPlayed = current.roundsPlayed + 1,
                        history = current.history + result,
                        showConfetti = false
                    )
                }
                RoundOutcome.INVALID_INPUT -> {
                    current.copy(
                        phase = GamePhase.ShowingResult(result)
                    )
                }
            }
        }
    }

    private fun hasAchievement(state: GameState, id: String): Boolean =
        state.achievements.any { it.id == id }

    private fun calculatePoints(difficulty: PhishingChallengeDataset.Difficulty): Int =
        (50 * difficulty.scoreMultiplier).toInt()

    private fun getTipForSignals(flags: List<String>): String {
        val flagsUpper = flags.joinToString(" ").uppercase()
        return when {
            "TLD" in flagsUpper -> 
                "TLDs like .tk, .ml are always flagged. Try registered domains."
            "BRAND" in flagsUpper || "IMPERSONATION" in flagsUpper -> 
                "Typosquatting 'paypa1' for 'paypal' is detected. Get creative!"
            "HOMOGRAPH" in flagsUpper -> 
                "Unicode tricks are detected. We check for lookalike characters."
            "IP" in flagsUpper -> 
                "IP addresses are suspicious. Use domain names."
            "SHORTENER" in flagsUpper -> 
                "Shorteners are flagged. Use full URLs."
            else -> 
                "Multiple signals contributed. Try a more subtle approach."
        }
    }
}
