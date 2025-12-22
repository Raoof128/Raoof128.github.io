package com.qrshield.android.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qrshield.android.data.BeatTheBotGameData
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
    val lastResult: GameResult? = null
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

class BeatTheBotViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val gameData = BeatTheBotGameData.levels

    init {
        startNewGame()
    }

    fun startNewGame() {
        _uiState.update {
            GameState(
                isGameActive = true,
                currentUrl = gameData.random(),
                timeRemainingSeconds = 30
            )
        }
        startTimer()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_uiState.value.isGameActive && _uiState.value.timeRemainingSeconds > 0) {
                delay(1000L)
                _uiState.update { it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1) }
            }
            if (_uiState.value.timeRemainingSeconds == 0 && _uiState.value.isGameActive) {
                handleTimeout()
            }
        }
    }

    private fun handleTimeout() {
         // Logic for timeout
         nextRound(GameResult.TIMEOUT)
    }

    fun submitGuess(isPhishingGuess: Boolean) {
        val current = _uiState.value.currentUrl ?: return
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

    private fun nextRound(result: GameResult) {
        val nextIndex = _uiState.value.currentRoundIndex + 1
        if (nextIndex >= _uiState.value.totalRounds) {
            _uiState.update { it.copy(isGameActive = false, lastResult = result) }
        } else {
            _uiState.update { 
                it.copy(
                    currentRoundIndex = nextIndex,
                    currentUrl = gameData.random(),
                    timeRemainingSeconds = 30,
                    lastResult = result
                ) 
            }
        }
    }
}
