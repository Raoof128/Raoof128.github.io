package com.qrshield.ui

import com.qrshield.core.PhishingEngine
import com.qrshield.model.RiskAssessment
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.ScanResult
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Shared ViewModel for QR-SHIELD UI
 * 
 * Manages UI state across all platforms using Kotlin Coroutines Flow.
 */
class SharedViewModel(
    private val phishingEngine: PhishingEngine = PhishingEngine(),
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _scanHistory = MutableStateFlow<List<ScanHistoryItem>>(emptyList())
    val scanHistory: StateFlow<List<ScanHistoryItem>> = _scanHistory.asStateFlow()
    
    /**
     * Process a scan result from camera or gallery
     */
    fun processScanResult(result: ScanResult, source: ScanSource) {
        coroutineScope.launch {
            when (result) {
                is ScanResult.Success -> {
                    _uiState.value = UiState.Analyzing(result.content)
                    
                    val assessment = phishingEngine.analyze(result.content)
                    
                    _uiState.value = UiState.Result(assessment)
                    
                    // Add to history
                    addToHistory(result.content, assessment, source)
                }
                is ScanResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                is ScanResult.NoQrFound -> {
                    _uiState.value = UiState.Error("No QR code found in image")
                }
            }
        }
    }
    
    /**
     * Analyze a URL directly (e.g., from clipboard)
     */
    fun analyzeUrl(url: String, source: ScanSource = ScanSource.CLIPBOARD) {
        coroutineScope.launch {
            _uiState.value = UiState.Analyzing(url)
            
            val assessment = phishingEngine.analyze(url)
            
            _uiState.value = UiState.Result(assessment)
            
            addToHistory(url, assessment, source)
        }
    }
    
    /**
     * Start scanning mode
     */
    fun startScanning() {
        _uiState.value = UiState.Scanning
    }
    
    /**
     * Return to idle state
     */
    fun resetToIdle() {
        _uiState.value = UiState.Idle
    }
    
    /**
     * Clear scan history
     */
    fun clearHistory() {
        _scanHistory.value = emptyList()
    }
    
    /**
     * Get scan by ID from history
     */
    fun getScanById(id: String): ScanHistoryItem? {
        return _scanHistory.value.find { it.id == id }
    }
    
    private fun addToHistory(
        url: String,
        assessment: RiskAssessment,
        source: ScanSource
    ) {
        val item = ScanHistoryItem(
            id = generateId(),
            url = url,
            score = assessment.score,
            verdict = assessment.verdict,
            scannedAt = currentTimeMillis(),
            source = source
        )
        _scanHistory.value = listOf(item) + _scanHistory.value.take(99)
    }
    
    private fun generateId(): String {
        return "scan_${currentTimeMillis()}_${(0..9999).random()}"
    }
    
    // Expect function to be implemented per platform
    private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}

/**
 * UI State sealed class
 */
sealed class UiState {
    data object Idle : UiState()
    data object Scanning : UiState()
    data class Analyzing(val url: String) : UiState()
    data class Result(val assessment: RiskAssessment) : UiState()
    data class Error(val message: String) : UiState()
}
