package com.qrshield.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrshield.android.data.BeatTheBotGameData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameState(
    val score: Int = 0,
    val streak: Int = 0,
    val totalRounds: Int = 10,
    val currentRoundIndex: Int = 0,
    val timeRemainingSeconds: Int = 30,
    val currentUrl: GameUrl? = null,
    val isGameActive: Boolean = false,
    val lastResult: GameResult? = null,
    val isGameOver: Boolean = false
)

data class GameUrl(
    val url: String,
    val isPhishing: Boolean,
    val context: String,
    val sender: String = "+1 (555) 123-4567"
)

enum class GameResult {
    CORRECT, INCORRECT, TIMEOUT
}

/**
 * ViewModel for the Beat the Bot training game.
 * Manages game state, timer, and scoring.
 * 
 * Fixed issues:
 * - Timer now properly cancelled on round changes and game end
 * - State properly reset between games
 * - Added game over state handling
 */
class BeatTheBotViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val gameData = BeatTheBotGameData.levels
    
    // Track the timer job so we can cancel it
    private var timerJob: Job? = null
    
    // Track URLs already shown to avoid repeats
    private val shownUrls = mutableSetOf<Int>()

    init {
        startNewGame()
    }
    
    /**
     * Start a new game, resetting all state.
     */
    fun startNewGame() {
        // Cancel any existing timer
        timerJob?.cancel()
        timerJob = null
        
        // Reset shown URLs
        shownUrls.clear()
        
        // Get first URL
        val firstUrl = getNextUrl()
        
        _uiState.update {
            GameState(
                isGameActive = true,
                isGameOver = false,
                currentUrl = firstUrl,
                timeRemainingSeconds = 30,
                score = 0,
                streak = 0,
                currentRoundIndex = 0,
                lastResult = null
            )
        }
        startTimer()
    }
    
    /**
     * Get next URL that hasn't been shown yet.
     */
    private fun getNextUrl(): GameUrl {
        // If all URLs have been shown, reset
        if (shownUrls.size >= gameData.size) {
            shownUrls.clear()
        }
        
        var nextUrl: GameUrl
        var index: Int
        do {
            index = gameData.indices.random()
            nextUrl = gameData[index]
        } while (index in shownUrls)
        
        shownUrls.add(index)
        return nextUrl
    }

    /**
     * Start the countdown timer for the current round.
     */
    private fun startTimer() {
        // Cancel any existing timer first
        timerJob?.cancel()
        
        timerJob = viewModelScope.launch {
            while (_uiState.value.isGameActive && _uiState.value.timeRemainingSeconds > 0) {
                delay(1000L)
                // Check again after delay in case game ended
                if (_uiState.value.isGameActive && !_uiState.value.isGameOver) {
                    _uiState.update { it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1) }
                }
            }
            // Only handle timeout if game is still active
            if (_uiState.value.timeRemainingSeconds == 0 && _uiState.value.isGameActive && !_uiState.value.isGameOver) {
                handleTimeout()
            }
        }
    }

    private fun handleTimeout() {
        // Reset streak on timeout
        _uiState.update { it.copy(streak = 0) }
        nextRound(GameResult.TIMEOUT)
    }

    /**
     * Submit user's guess about whether the URL is phishing or legitimate.
     */
    fun submitGuess(isPhishingGuess: Boolean) {
        val current = _uiState.value.currentUrl ?: return
        
        // Ignore if game is not active or already over
        if (!_uiState.value.isGameActive || _uiState.value.isGameOver) return
        
        val isCorrect = current.isPhishing == isPhishingGuess
        val result = if (isCorrect) GameResult.CORRECT else GameResult.INCORRECT
        
        if (isCorrect) {
            _uiState.update { 
                it.copy(
                    score = it.score + (100 * (it.streak + 1)) + (it.timeRemainingSeconds * 10),
                    streak = it.streak + 1
                ) 
            }
        } else {
            _uiState.update { it.copy(streak = 0) }
        }
        
        nextRound(result)
    }

    /**
     * Progress to the next round or end the game.
     */
    private fun nextRound(result: GameResult) {
        // Cancel current timer
        timerJob?.cancel()
        timerJob = null
        
        val nextIndex = _uiState.value.currentRoundIndex + 1
        
        if (nextIndex >= _uiState.value.totalRounds) {
            // Game over
            _uiState.update { 
                it.copy(
                    isGameActive = false, 
                    isGameOver = true,
                    lastResult = result
                ) 
            }
        } else {
            // Get next URL
            val nextUrl = getNextUrl()
            
            _uiState.update { 
                it.copy(
                    currentRoundIndex = nextIndex,
                    currentUrl = nextUrl,
                    timeRemainingSeconds = 30,
                    lastResult = result
                ) 
            }
            
            // Start timer for next round
            startTimer()
        }
    }
    
    /**
     * Clear the last result (e.g., after showing feedback).
     */
    fun clearLastResult() {
        _uiState.update { it.copy(lastResult = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up timer when ViewModel is destroyed
        timerJob?.cancel()
        timerJob = null
    }
}
