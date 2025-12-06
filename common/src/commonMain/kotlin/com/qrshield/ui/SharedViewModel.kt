package com.qrshield.ui

import com.qrshield.core.PhishingEngine
import com.qrshield.data.HistoryRepository
import com.qrshield.model.RiskAssessment
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.ScanResult
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Shared ViewModel for QR-SHIELD UI
 * 
 * Manages UI state across all platforms using Kotlin Coroutines Flow.
 * Now with persistent history storage via HistoryRepository.
 * 
 * @param phishingEngine Engine for analyzing URLs for phishing threats
 * @param historyRepository Repository for persisting scan history
 * @param coroutineScope Scope for launching coroutines
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class SharedViewModel(
    private val phishingEngine: PhishingEngine = PhishingEngine(),
    private val historyRepository: HistoryRepository,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    /**
     * Scan history as an observable Flow from the database.
     * Automatically updates when new scans are added.
     */
    val scanHistory: StateFlow<List<ScanHistoryItem>> = historyRepository
        .observe()
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    /**
     * Process a scan result from camera or gallery.
     * Analyzes the content and persists to database.
     */
    fun processScanResult(result: ScanResult, source: ScanSource) {
        coroutineScope.launch {
            when (result) {
                is ScanResult.Success -> {
                    _uiState.value = UiState.Analyzing(result.content)
                    
                    val assessment = phishingEngine.analyze(result.content)
                    
                    _uiState.value = UiState.Result(assessment)
                    
                    // Persist to database
                    saveToHistory(result.content, assessment, source)
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
     * Analyze a URL directly (e.g., from clipboard).
     * Analyzes and persists to database.
     */
    fun analyzeUrl(url: String, source: ScanSource = ScanSource.CLIPBOARD) {
        coroutineScope.launch {
            _uiState.value = UiState.Analyzing(url)
            
            val assessment = phishingEngine.analyze(url)
            
            _uiState.value = UiState.Result(assessment)
            
            // Persist to database
            saveToHistory(url, assessment, source)
        }
    }
    
    /**
     * Start scanning mode.
     */
    fun startScanning() {
        _uiState.value = UiState.Scanning
    }
    
    /**
     * Return to idle state.
     */
    fun resetToIdle() {
        _uiState.value = UiState.Idle
    }
    
    /**
     * Clear scan history from database.
     */
    fun clearHistory() {
        coroutineScope.launch {
            historyRepository.clearAll()
        }
    }
    
    /**
     * Delete a specific scan from history.
     */
    fun deleteScan(id: String) {
        coroutineScope.launch {
            historyRepository.delete(id)
        }
    }
    
    /**
     * Get scan by ID from history.
     */
    suspend fun getScanById(id: String): ScanHistoryItem? {
        return historyRepository.getById(id)
    }
    
    /**
     * Get history statistics (for dashboard display).
     */
    suspend fun getStatistics(): HistoryStatistics {
        val all = historyRepository.getAll()
        
        return HistoryStatistics(
            totalScans = all.size,
            safeCount = all.count { it.verdict == Verdict.SAFE },
            suspiciousCount = all.count { it.verdict == Verdict.SUSPICIOUS },
            maliciousCount = all.count { it.verdict == Verdict.MALICIOUS },
            averageScore = if (all.isEmpty()) 0.0 else all.map { it.score }.average()
        )
    }
    
    /**
     * Save scan result to persistent database.
     */
    private suspend fun saveToHistory(
        url: String,
        assessment: RiskAssessment,
        source: ScanSource
    ) {
        val item = ScanHistoryItem(
            id = generateId(),
            url = url.take(2048), // Bound URL length for security
            score = assessment.score.coerceIn(0, 100),
            verdict = assessment.verdict,
            scannedAt = currentTimeMillis(),
            source = source
        )
        
        historyRepository.insert(item)
    }
    
    private fun generateId(): String {
        return "scan_${currentTimeMillis()}_${(0..9999).random()}"
    }
    
    private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}

/**
 * UI State sealed class for managing screen states.
 */
sealed class UiState {
    data object Idle : UiState()
    data object Scanning : UiState()
    data class Analyzing(val url: String) : UiState()
    data class Result(val assessment: RiskAssessment) : UiState()
    data class Error(val message: String) : UiState()
}

/**
 * History statistics for dashboard display.
 */
data class HistoryStatistics(
    val totalScans: Int,
    val safeCount: Int,
    val suspiciousCount: Int,
    val maliciousCount: Int,
    val averageScore: Double
) {
    val safePercentage: Double
        get() = if (totalScans > 0) safeCount.toDouble() / totalScans * 100 else 0.0
    
    val threatPercentage: Double
        get() = if (totalScans > 0) (suspiciousCount + maliciousCount).toDouble() / totalScans * 100 else 0.0
}
