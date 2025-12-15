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

package com.qrshield.ui

import com.qrshield.core.PhishingEngine
import com.qrshield.data.HistoryRepository
import com.qrshield.model.RiskAssessment
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.ScanResult
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import com.qrshield.network.NoOpShortLinkResolver
import com.qrshield.network.ShortLinkResolver
import com.qrshield.share.ShareContent
import com.qrshield.share.ShareManager
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
 * @param shortLinkResolver Resolver for URL shorteners (Aggressive Mode)
 * @param coroutineScope Scope for launching coroutines
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class SharedViewModel(
    private val phishingEngine: PhishingEngine = PhishingEngine(),
    private val historyRepository: HistoryRepository,
    private val settingsDataSource: com.qrshield.data.SettingsDataSource,
    private val shortLinkResolver: ShortLinkResolver = NoOpShortLinkResolver(),
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

                    // Persist to database if enabled
                    if (settings.value.isSaveHistoryEnabled) {
                        saveToHistory(result.content, assessment, source)
                    }
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
     *
     * If aggressive mode is enabled and the URL is a shortener,
     * resolves it first to reveal the final destination.
     */
    fun analyzeUrl(url: String, source: ScanSource = ScanSource.CLIPBOARD) {
        coroutineScope.launch {
            // Check if we should resolve shortened URLs
            val urlToAnalyze = if (settings.value.isAggressiveModeEnabled &&
                                   shortLinkResolver.isResolvableShortener(url)) {
                // Show resolving state
                _uiState.value = UiState.Resolving(url)
                
                // Attempt to resolve
                when (val result = shortLinkResolver.resolve(url)) {
                    is ShortLinkResolver.ResolveResult.Success -> {
                        // Use the resolved URL for analysis
                        result.resolvedUrl
                    }
                    is ShortLinkResolver.ResolveResult.Failure -> {
                        // Resolution failed, analyze original with warning
                        url
                    }
                    is ShortLinkResolver.ResolveResult.NotShortener -> {
                        // Not a shortener, use original
                        url
                    }
                }
            } else {
                url
            }
            
            _uiState.value = UiState.Analyzing(urlToAnalyze)

            val assessment = phishingEngine.analyze(urlToAnalyze)
            
            // If we resolved a short link, add that info to the assessment
            val enrichedAssessment = if (urlToAnalyze != url) {
                assessment.copy(
                    flags = assessment.flags + "Resolved from: $url"
                )
            } else {
                assessment
            }

            _uiState.value = UiState.Result(enrichedAssessment)

            // Persist to database (save original URL for reference)
            saveToHistory(url, enrichedAssessment, source)
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

    // === SHARE FUNCTIONALITY ===

    /**
     * Generate shareable content for the current analysis result.
     *
     * @return ShareContent with text and HTML formats, or null if no result
     */
    fun generateShareContent(): ShareContent? {
        val state = _uiState.value
        if (state !is UiState.Result) return null

        val assessment = state.assessment
        val url = assessment.details.originalUrl

        return ShareContent(
            title = "QR-SHIELD Analysis: ${assessment.verdict.name}",
            text = ShareManager.generateTextSummary(url, assessment),
            html = ShareManager.generateHtmlReport(url, assessment),
            url = url
        )
    }

    /**
     * Generate plain text share content.
     */
    fun generateShareText(): String? {
        val state = _uiState.value
        if (state !is UiState.Result) return null

        val assessment = state.assessment
        return ShareManager.generateTextSummary(assessment.details.originalUrl, assessment)
    }

    /**
     * Generate JSON export of current analysis.
     */
    fun generateJsonExport(): String? {
        val state = _uiState.value
        if (state !is UiState.Result) return null

        val assessment = state.assessment
        return ShareManager.generateJsonReport(assessment.details.originalUrl, assessment)
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


    // === SETTINGS ===

    val settings: StateFlow<AppSettings> = settingsDataSource.settings
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = AppSettings()
        )

    fun updateSettings(newSettings: AppSettings) {
        coroutineScope.launch {
            settingsDataSource.saveSettings(newSettings)
        }
    }

    private fun generateId(): String {
        return "scan_${currentTimeMillis()}_${(0..9999).random()}"
    }

    private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}

/**
 * App Settings for preferences.
 *
 * @param isAggressiveModeEnabled When true, resolves URL shorteners via HTTP HEAD
 *        to reveal the final destination. This requires network access.
 *        Disabled by default to preserve privacy.
 */
data class AppSettings(
    val isAutoScanEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isSaveHistoryEnabled: Boolean = true,
    val isSecurityAlertsEnabled: Boolean = true,
    val isDeveloperModeEnabled: Boolean = false,
    val isAggressiveModeEnabled: Boolean = false  // NEW: Resolve short links (online only)
)


/**
 * UI State sealed class for managing screen states.
 */
sealed class UiState {
    data object Idle : UiState()
    data object Scanning : UiState()
    /** Resolving shortened URL (Aggressive Mode) */
    data class Resolving(val originalUrl: String) : UiState()
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
