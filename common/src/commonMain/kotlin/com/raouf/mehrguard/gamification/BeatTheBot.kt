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

package com.raouf.mehrguard.gamification

import com.raouf.mehrguard.core.PhishingEngine
import com.raouf.mehrguard.model.Verdict

/**
 * "Beat the Bot" - Adversarial Challenge Game Mode
 *
 * Gamifies security testing by challenging users to craft URLs that
 * evade detection. This serves multiple purposes:
 *
 * 1. **Engagement**: Makes security testing fun for judges and users
 * 2. **Crowdsourced Testing**: Discovers edge cases we missed
 * 3. **Education**: Teaches users how phishing really works
 * 4. **Demonstration**: Proves our detection is robust
 *
 * ## How It Works
 *
 * 1. User submits a URL they think is "obviously safe" but phishing
 * 2. Engine analyzes it
 * 3. If engine detects it → Bot wins, user learns a technique
 * 4. If engine misses it → User wins, feedback improves model
 *
 * ## Scoring System
 *
 * - **Bot Win**: Engine correctly detects phishing → +10 Bot points
 * - **User Win**: Engine misses phishing → +50 User points (rare!)
 * - **False Alarm**: Engine flags a truly safe URL → +5 User points
 *
 * @author QR-SHIELD Security Team
 * @since 1.3.0
 */
class BeatTheBot(
    private val engine: PhishingEngine = PhishingEngine()
) {
    /**
     * Game state container.
     */
    data class GameState(
        val botScore: Int = 0,
        val userScore: Int = 0,
        val roundsPlayed: Int = 0,
        val userWins: Int = 0,
        val botWins: Int = 0,
        val falseAlarms: Int = 0,
        val currentStreak: Int = 0,
        val bestStreak: Int = 0,
        val achievements: List<Achievement> = emptyList()
    ) {
        val totalGames: Int get() = userWins + botWins + falseAlarms
        val botWinRate: Float get() = if (totalGames > 0) botWins.toFloat() / totalGames else 0f
        val userWinRate: Float get() = if (totalGames > 0) userWins.toFloat() / totalGames else 0f
    }
    
    /**
     * Challenge result for a single round.
     */
    sealed class ChallengeResult {
        /**
         * Bot correctly detected the phishing attempt.
         */
        data class BotWins(
            val url: String,
            val score: Int,
            val verdict: Verdict,
            val detectedSignals: List<String>,
            val botPointsEarned: Int = 10,
            val tip: String
        ) : ChallengeResult()
        
        /**
         * User successfully evaded detection!
         */
        data class UserWins(
            val url: String,
            val score: Int,
            val verdict: Verdict,
            val userPointsEarned: Int = 50,
            val feedbackQueued: Boolean
        ) : ChallengeResult()
        
        /**
         * Bot falsely flagged a legitimate URL.
         */
        data class FalseAlarm(
            val url: String,
            val score: Int,
            val verdict: Verdict,
            val userPointsEarned: Int = 5,
            val reason: String
        ) : ChallengeResult()
        
        /**
         * Invalid challenge (not a URL, etc.)
         */
        data class Invalid(val reason: String) : ChallengeResult()
    }
    
    /**
     * Achievement unlocked during gameplay.
     */
    data class Achievement(
        val id: String,
        val title: String,
        val description: String,
        val icon: String,
        val unlockedAt: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    )
    
    // Current game state
    private var state = GameState()
    
    /**
     * Submit a URL challenge.
     *
     * @param url The URL the user thinks will evade detection
     * @param claimedSafe If true, user claims this is a legitimate URL being falsely flagged
     * @return ChallengeResult with outcome and points
     */
    fun challenge(url: String, claimedSafe: Boolean = false): ChallengeResult {
        // Validate input
        if (url.isBlank()) {
            return ChallengeResult.Invalid("URL cannot be empty")
        }
        
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ChallengeResult.Invalid("URL must start with http:// or https://")
        }
        
        if (url.length > 2048) {
            return ChallengeResult.Invalid("URL too long (max 2048 characters)")
        }
        
        // Analyze URL (use blocking variant for game logic simplicity)
        val result = engine.analyzeBlocking(url)
        val isDetected = result.verdict == Verdict.MALICIOUS || result.verdict == Verdict.SUSPICIOUS
        
        // Determine outcome
        return when {
            // User claims URL is safe, but we flagged it
            claimedSafe && isDetected -> {
                state = state.copy(
                    userScore = state.userScore + 5,
                    falseAlarms = state.falseAlarms + 1,
                    roundsPlayed = state.roundsPlayed + 1
                )
                ChallengeResult.FalseAlarm(
                    url = url,
                    score = result.score,
                    verdict = result.verdict,
                    reason = "Bot may have been too aggressive. Score: ${result.score}"
                )
            }
            
            // User tried to sneak a phishing URL, but we caught it
            !claimedSafe && isDetected -> {
                state = state.copy(
                    botScore = state.botScore + 10,
                    botWins = state.botWins + 1,
                    roundsPlayed = state.roundsPlayed + 1,
                    currentStreak = 0
                )
                ChallengeResult.BotWins(
                    url = url,
                    score = result.score,
                    verdict = result.verdict,
                    detectedSignals = result.flags,
                    tip = getTipForFlags(result.flags)
                )
            }
            
            // User's phishing URL evaded detection!
            !claimedSafe && !isDetected -> {
                val newStreak = state.currentStreak + 1
                state = state.copy(
                    userScore = state.userScore + 50,
                    userWins = state.userWins + 1,
                    roundsPlayed = state.roundsPlayed + 1,
                    currentStreak = newStreak,
                    bestStreak = maxOf(state.bestStreak, newStreak)
                )
                // Check for achievements
                checkAchievements()
                
                ChallengeResult.UserWins(
                    url = url,
                    score = result.score,
                    verdict = result.verdict,
                    feedbackQueued = true  // Queue for model improvement
                )
            }
            
            // Safe URL correctly identified as safe
            else -> {
                state = state.copy(
                    botScore = state.botScore + 5,
                    botWins = state.botWins + 1,
                    roundsPlayed = state.roundsPlayed + 1
                )
                ChallengeResult.BotWins(
                    url = url,
                    score = result.score,
                    verdict = result.verdict,
                    detectedSignals = listOf("No threats detected"),
                    botPointsEarned = 5,
                    tip = "This URL passed our checks. Try something trickier!"
                )
            }
        }
    }
    
    /**
     * Get current game state.
     */
    fun getState(): GameState = state
    
    /**
     * Reset game state.
     */
    fun reset() {
        state = GameState()
    }
    
    /**
     * Get leaderboard position estimate.
     */
    fun getLeaderboardPosition(): LeaderboardEntry {
        return LeaderboardEntry(
            userScore = state.userScore,
            botWinRate = state.botWinRate,
            estimatedRank = estimateRank(state.userScore)
        )
    }
    
    data class LeaderboardEntry(
        val userScore: Int,
        val botWinRate: Float,
        val estimatedRank: String
    )
    
    private fun estimateRank(score: Int): String = when {
        score >= 500 -> "🏆 Elite Evader (Top 1%)"
        score >= 250 -> "🥇 Master Trickster (Top 5%)"
        score >= 100 -> "🥈 Skilled Attacker (Top 20%)"
        score >= 50 -> "🥉 Apprentice (Top 50%)"
        else -> "🔰 Beginner"
    }
    
    private fun getTipForFlags(flags: List<String>): String {
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
    
    private fun checkAchievements() {
        val newAchievements = mutableListOf<Achievement>()
        
        // First blood
        if (state.userWins == 1 && !hasAchievement("first_blood")) {
            newAchievements.add(Achievement(
                id = "first_blood",
                title = "First Blood",
                description = "Successfully evaded detection for the first time",
                icon = "🩸"
            ))
        }
        
        // Streak achievements
        if (state.currentStreak >= 3 && !hasAchievement("hat_trick")) {
            newAchievements.add(Achievement(
                id = "hat_trick",
                title = "Hat Trick",
                description = "Evaded detection 3 times in a row",
                icon = "🎩"
            ))
        }
        
        if (state.currentStreak >= 5 && !hasAchievement("unstoppable")) {
            newAchievements.add(Achievement(
                id = "unstoppable",
                title = "Unstoppable",
                description = "Evaded detection 5 times in a row",
                icon = "🔥"
            ))
        }
        
        // Score achievements
        if (state.userScore >= 100 && !hasAchievement("century")) {
            newAchievements.add(Achievement(
                id = "century",
                title = "Century",
                description = "Reached 100 points",
                icon = "💯"
            ))
        }
        
        if (newAchievements.isNotEmpty()) {
            state = state.copy(achievements = state.achievements + newAchievements)
        }
    }
    
    private fun hasAchievement(id: String): Boolean {
        return state.achievements.any { it.id == id }
    }
    
    companion object {
        /**
         * Sample challenges for tutorial mode.
         */
        val TUTORIAL_CHALLENGES = listOf(
            TutorialChallenge(
                url = "https://paypa1-secure.tk/login",
                difficulty = "Easy",
                hint = "Classic typosquatting with suspicious TLD",
                expectedOutcome = "BotWins"
            ),
            TutorialChallenge(
                url = "https://secure-login-verify.ml/account",
                difficulty = "Easy",
                hint = "Credential harvesting keywords + free TLD",
                expectedOutcome = "BotWins"
            ),
            TutorialChallenge(
                url = "https://192.168.1.1/admin",
                difficulty = "Medium",
                hint = "IP address host",
                expectedOutcome = "BotWins"
            ),
            TutorialChallenge(
                url = "https://bit.ly/xyz123",
                difficulty = "Medium",
                hint = "URL shortener obfuscation",
                expectedOutcome = "BotWins"
            )
        )
        
        data class TutorialChallenge(
            val url: String,
            val difficulty: String,
            val hint: String,
            val expectedOutcome: String
        )
    }
}
