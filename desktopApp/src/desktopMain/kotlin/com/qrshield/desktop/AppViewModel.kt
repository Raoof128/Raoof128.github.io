package com.qrshield.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qrshield.core.PhishingEngine
import com.qrshield.core.ScoringConfig
import com.qrshield.core.VerdictEngine
import com.qrshield.data.DatabaseDriverFactory
import com.qrshield.data.HistoryRepository
import com.qrshield.data.HistoryRepositoryFactory
import com.qrshield.data.ScanHistoryManager
import com.qrshield.model.ContentType
import com.qrshield.model.RiskAssessment
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.platform.PlatformClipboard
import com.qrshield.platform.PlatformTime
import com.qrshield.platform.PlatformUrlOpener
import com.qrshield.scanner.DesktopQrScanner
import com.qrshield.security.InputValidator
import com.qrshield.share.ShareManager
import com.qrshield.desktop.ui.AppNotification
import com.qrshield.desktop.ui.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.IOException

enum class ScanMonitorViewMode { Visual, Raw }
enum class ResultViewMode { Simple, Technical }
enum class ExportFormat { Pdf, Json }
enum class HistoryFilter { All, Safe, Suspicious, Dangerous }
enum class HeuristicSensitivity { Low, Balanced, Paranoia }

data class TrustCentreToggles(
    val strictOffline: Boolean,
    val anonymousTelemetry: Boolean,
    val autoCopySafe: Boolean
)

data class StatusMessage(val text: String, val kind: MessageKind)

enum class MessageKind { Info, Success, Error }

data class TrainingInsight(
    val icon: String,
    val title: String,
    val body: String,
    val kind: TrainingInsightKind
)

enum class TrainingInsightKind { Warning, Suspicious, Psychology }

data class TrainingScenario(
    val payload: String,
    val contextTitle: String,
    val contextBody: String,
    val expectedVerdict: Verdict,
    val aiConfidence: Float,
    val insights: List<TrainingInsight>
)

data class TrainingState(
    val module: Int,
    val round: Int,
    val totalRounds: Int,
    val score: Int,
    val streak: Int,
    val bestStreak: Int,
    val correct: Int,
    val attempts: Int,
    val remainingSeconds: Int,
    val botScore: Int,
    val sessionId: String,
    val roundStartTimeMs: Long,
    val isGameOver: Boolean,
    val showResultModal: Boolean,
    val lastRoundCorrect: Boolean?,
    val lastRoundPoints: Int,
    val lastResponseTimeMs: Long
) {
    val accuracy: Float
        get() = if (attempts == 0) 0f else correct.toFloat() / attempts.toFloat()
    val progress: Float
        get() = if (totalRounds == 0) 0f else round.toFloat() / totalRounds.toFloat()
    val playerWon: Boolean
        get() = score > botScore
}

sealed class DesktopScanState {
    data object Idle : DesktopScanState()
    data object Scanning : DesktopScanState()
    data class Analyzing(val url: String) : DesktopScanState()
    data class Error(val message: String) : DesktopScanState()
    data class Result(val url: String, val assessment: RiskAssessment) : DesktopScanState()
}

class AppViewModel(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val settingsStore: DesktopSettingsStore = FileDesktopSettingsStore(),
    private val historyRepository: HistoryRepository = HistoryRepositoryFactory.createPersistent(
        driver = DatabaseDriverFactory().createDriver(),
        scope = scope
    ),
    private val qrScanner: DesktopQrScanner = DesktopQrScanner()
) {
    private val historyManager = ScanHistoryManager(historyRepository)
    private val verdictEngine = VerdictEngine()
    private var phishingEngine = buildPhishingEngine(HeuristicSensitivity.Balanced)

    var currentScreen by mutableStateOf<AppScreen>(AppScreen.Dashboard)
    var isDarkMode by mutableStateOf(false)
    var appLanguage by mutableStateOf(AppLanguage.systemDefault())

    var scanMonitorViewMode by mutableStateOf(ScanMonitorViewMode.Visual)
    var resultSafeViewMode by mutableStateOf(ResultViewMode.Simple)
    var resultDangerousViewMode by mutableStateOf(ResultViewMode.Technical)

    var scanState by mutableStateOf<DesktopScanState>(DesktopScanState.Idle)
    var statusMessage by mutableStateOf<StatusMessage?>(null)
    var lastAnalysisDurationMs by mutableStateOf<Long?>(null)

    var currentAssessment by mutableStateOf<RiskAssessment?>(null)
    var currentUrl by mutableStateOf<String?>(null)
    var currentVerdict by mutableStateOf<Verdict?>(null)
    var currentVerdictDetails by mutableStateOf<VerdictEngine.EnrichedVerdict?>(null)
    var lastAnalyzedAt by mutableStateOf<Long?>(null)

    var scanHistory by mutableStateOf<List<ScanHistoryItem>>(emptyList())
    var historyStats by mutableStateOf(emptyStats())
    var historyFilter by mutableStateOf(HistoryFilter.All)
    var historySearchQuery by mutableStateOf("")

    var allowlist by mutableStateOf<List<String>>(emptyList())
    var blocklist by mutableStateOf<List<String>>(emptyList())
    var allowlistQuery by mutableStateOf("")
    var blocklistQuery by mutableStateOf("")

    var trustCentreToggles by mutableStateOf(
        TrustCentreToggles(
            strictOffline = true,
            anonymousTelemetry = false,
            autoCopySafe = false
        )
    )
    var heuristicSensitivity by mutableStateOf(HeuristicSensitivity.Balanced)

    var exportFormat by mutableStateOf(ExportFormat.Pdf)
    var exportFilename by mutableStateOf("scan_report_20251228_8821X")
    var exportIncludeVerdict by mutableStateOf(true)
    var exportIncludeMetadata by mutableStateOf(true)
    var exportIncludeRawPayload by mutableStateOf(false)
    var exportIncludeDebugLogs by mutableStateOf(false)

    // Security Settings (for parity with Web onboarding.html)
    var autoBlockThreats by mutableStateOf(true)
    var realTimeScanning by mutableStateOf(true)
    var soundAlerts by mutableStateOf(true)
    var threatAlerts by mutableStateOf(true)
    var showConfidenceScore by mutableStateOf(true)

    // Notification System
    var showNotificationPanel by mutableStateOf(false)
    var notifications by mutableStateOf(emptyList<AppNotification>())
    
    // Profile Dropdown
    var showProfileDropdown by mutableStateOf(false)
    
    // User Profile (parity with Web app shared-ui.js)
    var userName by mutableStateOf("QR-SHIELD User")
    var userEmail by mutableStateOf("user@example.com")
    var userInitials by mutableStateOf("QU")
    var userRole by mutableStateOf("Security Analyst")
    var userPlan by mutableStateOf("Enterprise Plan")
    var showEditProfileModal by mutableStateOf(false)

    // Game Statistics (parity with Web app training.js)
    // NOTE: Must be declared BEFORE init block since applySettings() accesses these
    var gameHighScore by mutableStateOf(0)
    var gameBestStreak by mutableStateOf(0)
    var gameTotalGamesPlayed by mutableStateOf(0)
    var gameTotalCorrect by mutableStateOf(0)
    var gameTotalAttempts by mutableStateOf(0)

    var trainingState by mutableStateOf(createInitialTrainingState())
    private var trainingScenarioIndex by mutableStateOf(0)
    private var shuffledChallengeIndices by mutableStateOf(trainingScenarios.indices.shuffled())
    val currentTrainingScenario: TrainingScenario
        get() = trainingScenarios[shuffledChallengeIndices.getOrElse(trainingScenarioIndex) { 0 }]

    init {
        applySettings(settingsStore.load())

        scope.launch {
            historyRepository.observe().collectLatest { history ->
                scanHistory = history
                historyStats = computeStats(history)
            }
        }
    }

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    fun toggleDarkMode() {
        toggleTheme()
    }

    fun analyzeUrlDirectly(url: String) {
        analyzeUrl(url, ScanSource.CLIPBOARD)
    }

    fun setLanguage(language: AppLanguage) {
        if (appLanguage != language) {
            appLanguage = language
            persistSettings()
        }
    }

    fun analyzeClipboardUrl() {
        val clipboard = PlatformClipboard.getClipboardText()?.trim().orEmpty()
        if (clipboard.isBlank()) {
            setError("Clipboard is empty or unavailable.")
            return
        }
        analyzeUrl(clipboard, ScanSource.CLIPBOARD)
    }

    fun analyzeUrl(url: String, source: ScanSource, recordHistory: Boolean = true) {
        val cleanedUrl = url.trim()
        val validation = InputValidator.validateUrl(cleanedUrl)
        val sanitizedUrl = validation.getOrNull()
        if (sanitizedUrl == null) {
            setError("Invalid URL format")
            return
        }
        scanState = DesktopScanState.Analyzing(sanitizedUrl)
        val startedAt = PlatformTime.currentTimeMillis()

        scope.launch {
            try {
                val assessment = phishingEngine.analyze(sanitizedUrl)
                val duration = PlatformTime.currentTimeMillis() - startedAt
                updateResult(sanitizedUrl, assessment, source, recordHistory, duration)
            } catch (e: Exception) {
                setError(e.message ?: "Analysis failed")
            }
        }
    }

    fun startCameraScan() {
        scanState = DesktopScanState.Scanning
        scope.launch {
            qrScanner.scanFromCamera().collectLatest { result ->
                handleScanResult(result, ScanSource.CAMERA)
                qrScanner.stopScanning()
                cancel("Scan completed")
            }
        }
    }

    fun scanImageFile(file: File) {
        scanState = DesktopScanState.Scanning
        scope.launch {
            try {
                val bytes = file.readBytes()
                val result = qrScanner.scanFromImage(bytes)
                handleScanResult(result, ScanSource.GALLERY)
            } catch (e: IOException) {
                setError("Failed to read image file")
            }
        }
    }

    fun pickImageAndScan() {
        // "Select QR Image" is already wrapped in t() in original code? No, let's check.
        // Line 247: val dialog = FileDialog(null as Frame?, t("Select QR Image"), FileDialog.LOAD) -> It was t("Select QR Image") already!
        // But let's verify others.
        val dialog = FileDialog(null as Frame?, t("Select QR Image"), FileDialog.LOAD)
        dialog.isVisible = true
        val selected = dialog.file
        val directory = dialog.directory
        if (selected.isNullOrBlank() || directory.isNullOrBlank()) {
            setMessage("No file selected", MessageKind.Info)
            return
        }
        scanImageFile(File(directory, selected))
    }

    // Drag and Drop support (parity with Web app scanner.js handleDrop)
    fun scanDroppedImageFile(file: File) {
        val supportedExtensions = listOf("png", "jpg", "jpeg", "gif", "bmp", "webp")
        val extension = file.extension.lowercase()
        if (extension !in supportedExtensions) {
            setError("Unsupported file type: .$extension. Please drop an image file.")
            return
        }
        if (!file.exists()) {
            setError("File not found: ${file.name}")
            return
        }
        scanImageFile(file)
    }

    private fun handleScanResult(result: com.qrshield.model.ScanResult, source: ScanSource) {
        when (result) {
            is com.qrshield.model.ScanResult.Success -> {
                if (result.contentType == ContentType.URL) {
                    analyzeUrl(result.content, source)
                } else {
                    setError(tf("Unsupported QR payload: %s", result.contentType))
                }
            }
            is com.qrshield.model.ScanResult.Error -> setError(result.message) // message from result might need translation if it's fixed? assuming it's dynamic or english.
            is com.qrshield.model.ScanResult.NoQrFound -> setError("No QR code found in image")
        }
    }

    fun openUrl(url: String) {
        if (trustCentreToggles.strictOffline) {
            setError("Offline mode enabled. Disable Strict Offline Mode to open URLs.", updateScanState = false)
            return
        }
        if (!PlatformUrlOpener.canOpenUrl(url)) {
            setError("Invalid URL format", updateScanState = false)
            return
        }
        if (!PlatformUrlOpener.openUrl(url)) {
            setError("Unable to open URL", updateScanState = false)
            return
        }
        setMessage("Opened in browser", MessageKind.Success)
    }

    fun copyUrl(url: String, label: String = "Copied") {
        if (PlatformClipboard.copyToClipboard(url)) {
            setMessage(label, MessageKind.Success) // label is usually passed in already localized or needs t()? 
            // label is passed from UI usually? Or internally?
            // "Safe link copied" is passed internally. "Copied" is default.
            // If label is passed, it might be already translated or not. 
            // setMessage calls t(message). So if we pass "Copied", t("Copied") will run.
        } else {
            setError("Clipboard unavailable", updateScanState = false)
        }
    }

    fun copyJsonReport() {
        val assessment = currentAssessment
        val url = currentUrl
        if (assessment == null || url.isNullOrBlank()) {
            setError("No report available", updateScanState = false)
            return
        }
        val json = ShareManager.generateJsonReport(url, assessment)
        copyUrl(json, label = "JSON report copied")
    }

    fun shareTextReport() {
        val assessment = currentAssessment
        val url = currentUrl
        if (assessment == null || url.isNullOrBlank()) {
            setError("No report available", updateScanState = false)
            return
        }
        val report = ShareManager.generateTextSummary(url, assessment)
        copyUrl(report, label = "Report copied")
    }

    fun exportReport() {
        val assessment = currentAssessment
        val url = currentUrl
        if (assessment == null || url.isNullOrBlank()) {
            setError("No report available", updateScanState = false)
            return
        }
        val content = when (exportFormat) {
            ExportFormat.Pdf -> ShareManager.generateHtmlReport(url, assessment)
            ExportFormat.Json -> ShareManager.generateJsonReport(url, assessment)
        }
        val extension = if (exportFormat == ExportFormat.Pdf) "pdf" else "json"
        val file = defaultExportFile(exportFilename, extension)
        try {
            file.writeText(content)
            setMessage(tf("Saved report to %s", file.name), MessageKind.Success)
        } catch (e: IOException) {
            setError("Failed to save report", updateScanState = false)
        }
    }

    fun exportHistoryCsv() {
        val history = filteredHistory()
        if (history.isEmpty()) {
            setError("No history to export", updateScanState = false)
            return
        }
        val csv = buildString {
            appendLine("id,url,score,verdict,scannedAt,source")
            history.forEach { item ->
                appendLine("${item.id},${item.url},${item.score},${item.verdict},${item.scannedAt},${item.source}")
            }
        }
        val file = defaultExportFile("scan_history", "csv")
        try {
            file.writeText(csv)
            setMessage(tf("Saved CSV to %s", file.name), MessageKind.Success)
        } catch (e: IOException) {
            setError("Failed to save CSV", updateScanState = false)
        }
    }

    // Security Audit Export (parity with Web app export.js generateSecurityAudit)
    fun exportSecurityAudit() {
        val history = scanHistory
        val stats = historyStats
        val timestamp = PlatformTime.currentTimeMillis()
        
        val auditReport = buildString {
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine("                    QR-SHIELD SECURITY AUDIT REPORT")
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine()
            appendLine("Generated: ${formatTimestamp(timestamp)}")
            appendLine("Engine Version: v2.4.0")
            appendLine("Sensitivity: ${heuristicSensitivity.name}")
            appendLine()
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine("                         EXECUTIVE SUMMARY")
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine()
            appendLine("Total Scans Performed: ${stats.totalScans}")
            appendLine("  ✓ Safe:       ${stats.safeCount} (${if (stats.totalScans > 0) (stats.safeCount * 100 / stats.totalScans) else 0}%)")
            appendLine("  ⚠ Suspicious: ${stats.suspiciousCount} (${if (stats.totalScans > 0) (stats.suspiciousCount * 100 / stats.totalScans) else 0}%)")
            appendLine("  ✗ Malicious:  ${stats.maliciousCount} (${if (stats.totalScans > 0) (stats.maliciousCount * 100 / stats.totalScans) else 0}%)")
            appendLine()
            appendLine("Average Risk Score: ${String.format("%.1f", stats.averageScore)}")
            appendLine()
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine("                        THREAT INTELLIGENCE")
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine()
            appendLine("Allowlist Entries: ${allowlist.size}")
            allowlist.take(10).forEach { appendLine("  + $it") }
            if (allowlist.size > 10) appendLine("  ... and ${allowlist.size - 10} more")
            appendLine()
            appendLine("Blocklist Entries: ${blocklist.size}")
            blocklist.take(10).forEach { appendLine("  - $it") }
            if (blocklist.size > 10) appendLine("  ... and ${blocklist.size - 10} more")
            appendLine()
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine("                         RECENT THREATS")
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine()
            val threats = history.filter { it.verdict != Verdict.SAFE }.take(10)
            if (threats.isEmpty()) {
                appendLine("No threats detected in recent scans.")
            } else {
                threats.forEach { item ->
                    appendLine("[${item.verdict}] ${item.url}")
                    appendLine("    Score: ${item.score} | ${formatTimestamp(item.scannedAt)}")
                }
            }
            appendLine()
            appendLine("═══════════════════════════════════════════════════════════════")
            appendLine("                    END OF SECURITY AUDIT REPORT")
            appendLine("═══════════════════════════════════════════════════════════════")
        }
        
        val file = defaultExportFile("security_audit", "txt")
        try {
            file.writeText(auditReport)
            setMessage(tf("Security audit saved to %s", file.name), MessageKind.Success)
        } catch (e: IOException) {
            setError("Failed to save audit report", updateScanState = false)
        }
    }

    // Full User Data Export (parity with Web app export.js exportFullUserData)
    fun exportFullUserData() {
        val timestamp = PlatformTime.currentTimeMillis()
        
        val userData = buildString {
            appendLine("{")
            appendLine("  \"exportedAt\": $timestamp,")
            appendLine("  \"version\": \"1.17.65\",")
            appendLine("  \"profile\": {")
            appendLine("    \"name\": \"$userName\",")
            appendLine("    \"email\": \"$userEmail\",")
            appendLine("    \"initials\": \"$userInitials\",")
            appendLine("    \"role\": \"$userRole\",")
            appendLine("    \"plan\": \"$userPlan\"")
            appendLine("  },")
            appendLine("  \"settings\": {")
            appendLine("    \"heuristicSensitivity\": \"${heuristicSensitivity.name}\",")
            appendLine("    \"strictOffline\": ${trustCentreToggles.strictOffline},")
            appendLine("    \"anonymousTelemetry\": ${trustCentreToggles.anonymousTelemetry},")
            appendLine("    \"autoCopySafe\": ${trustCentreToggles.autoCopySafe},")
            appendLine("    \"language\": \"${appLanguage.code}\"")
            appendLine("  },")
            appendLine("  \"allowlist\": [${allowlist.joinToString(",") { "\"$it\"" }}],")
            appendLine("  \"blocklist\": [${blocklist.joinToString(",") { "\"$it\"" }}],")
            appendLine("  \"gameStats\": {")
            appendLine("    \"highScore\": $gameHighScore,")
            appendLine("    \"bestStreak\": $gameBestStreak,")
            appendLine("    \"totalGamesPlayed\": $gameTotalGamesPlayed,")
            appendLine("    \"totalCorrect\": $gameTotalCorrect,")
            appendLine("    \"totalAttempts\": $gameTotalAttempts")
            appendLine("  },")
            appendLine("  \"scanHistory\": [")
            scanHistory.forEachIndexed { index, item ->
                val comma = if (index < scanHistory.size - 1) "," else ""
                appendLine("    {\"id\": \"${item.id}\", \"url\": \"${item.url}\", \"score\": ${item.score}, \"verdict\": \"${item.verdict}\", \"scannedAt\": ${item.scannedAt}, \"source\": \"${item.source}\"}$comma")
            }
            appendLine("  ]")
            appendLine("}")
        }
        
        val file = defaultExportFile("qrshield_full_export", "json")
        try {
            file.writeText(userData)
            setMessage(tf("Full data export saved to %s", file.name), MessageKind.Success)
        } catch (e: IOException) {
            setError("Failed to export user data", updateScanState = false)
        }
    }

    fun updateHistoryFilter(filter: HistoryFilter) {
        historyFilter = filter
    }

    fun updateHistorySearch(query: String) {
        historySearchQuery = query
    }

    fun selectHistoryItem(item: ScanHistoryItem) {
        analyzeUrl(item.url, item.source, recordHistory = false)
    }

    // Clear Scan History (parity with Web app shared-ui.js clearScanHistory)
    var showClearHistoryConfirmation by mutableStateOf(false)

    fun showClearHistoryDialog() {
        showClearHistoryConfirmation = true
    }

    fun dismissClearHistoryDialog() {
        showClearHistoryConfirmation = false
    }

    fun clearScanHistory() {
        scope.launch {
            val count = historyRepository.clearAll()
            showClearHistoryConfirmation = false
            setMessage("Cleared $count scan entries", MessageKind.Success)
        }
    }

    fun getScanById(id: String): ScanHistoryItem? {
        return scanHistory.find { it.id == id }
    }

    fun addAllowlistDomain(domain: String) {
        val normalized = normalizeDomain(domain)
        if (normalized.isBlank()) {
            setError("Domain cannot be empty", updateScanState = false)
            return
        }
        val validation = InputValidator.validateHostname(normalized)
        if (!validation.isValid()) {
            setError("Invalid domain", updateScanState = false)
            return
        }
        if (allowlist.any { it.equals(domain, ignoreCase = true) }) {
            setError("Domain already allowlisted", updateScanState = false) // Note: "allowlisted" vs "whitelist" consistency
            return
        }
        allowlist = allowlist + domain
        persistSettings()
        setMessage("Added to allowlist", MessageKind.Success)
    }

    fun removeAllowlistDomain(domain: String) {
        allowlist = allowlist.filterNot { it.equals(domain, ignoreCase = true) }
        persistSettings()
        setMessage("Removed from allowlist", MessageKind.Info)
    }

    fun addBlocklistDomain(domain: String) {
        val normalized = domain.trim()
        if (normalized.isBlank()) {
            setError("Domain cannot be empty", updateScanState = false)
            return
        }
        if (blocklist.any { it.equals(domain, ignoreCase = true) }) {
            setError("Domain already blocked", updateScanState = false)
            return
        }
        blocklist = blocklist + normalized
        persistSettings()
        setMessage("Added to blocklist", MessageKind.Success)
    }

    fun removeBlocklistDomain(domain: String) {
        blocklist = blocklist.filterNot { it.equals(domain, ignoreCase = true) }
        persistSettings()
        setMessage("Removed from blocklist", MessageKind.Info)
    }

    fun updateAllowlistQuery(query: String) {
        allowlistQuery = query
    }

    fun updateBlocklistQuery(query: String) {
        blocklistQuery = query
    }

    fun toggleStrictOffline() {
        trustCentreToggles = trustCentreToggles.copy(strictOffline = !trustCentreToggles.strictOffline)
        persistSettings()
    }

    fun toggleAnonymousTelemetry() {
        trustCentreToggles = trustCentreToggles.copy(anonymousTelemetry = !trustCentreToggles.anonymousTelemetry)
        persistSettings()
    }

    fun toggleAutoCopySafe() {
        trustCentreToggles = trustCentreToggles.copy(autoCopySafe = !trustCentreToggles.autoCopySafe)
        persistSettings()
    }

    fun updateHeuristicSensitivity(value: HeuristicSensitivity) {
        heuristicSensitivity = value
        phishingEngine = buildPhishingEngine(value)
        persistSettings()
    }

    fun clearStatusMessage() {
        statusMessage = null
    }

    fun showInfo(message: String) {
        setMessage(message, MessageKind.Info)
    }

    // Notification Panel Functions
    fun toggleNotificationPanel() {
        showNotificationPanel = !showNotificationPanel
    }

    fun dismissNotificationPanel() {
        showNotificationPanel = false
    }

    fun markAllNotificationsRead() {
        notifications = notifications.map { it.copy(isRead = true) }
    }

    fun markNotificationRead(notification: AppNotification) {
        notifications = notifications.map { 
            if (it.id == notification.id) it.copy(isRead = true) else it 
        }
    }

    fun clearAllNotifications() {
        notifications = emptyList()
        showNotificationPanel = false
    }

    fun addNotification(title: String, message: String, type: NotificationType, scanUrl: String? = null) {
        val now = PlatformTime.currentTimeMillis()
        
        // Prevent duplicate notifications for the same URL within 5 seconds
        if (scanUrl != null) {
            val recentDuplicate = notifications.any { 
                it.scanUrl == scanUrl && (now - it.timestamp) < 5000 
            }
            if (recentDuplicate) return
        }
        
        val newNotification = AppNotification(
            id = "notif_$now",
            title = title,
            message = message,
            type = type,
            timestamp = now,
            isRead = false,
            scanUrl = scanUrl
        )
        // Add new notification and keep only last 20
        notifications = (listOf(newNotification) + notifications).take(20)
    }

    /**
     * Handle notification click - navigates to the appropriate result screen
     * Parity with Web app shared-ui.js handleNotificationClick
     */
    fun handleNotificationClick(notification: AppNotification) {
        markNotificationRead(notification)
        dismissNotificationPanel()
        
        // Navigate directly based on notification type (no re-analysis to avoid duplicates)
        currentScreen = when (notification.type) {
            NotificationType.SUCCESS -> AppScreen.ResultSafe
            NotificationType.WARNING -> AppScreen.ResultSuspicious
            NotificationType.ERROR -> AppScreen.ResultDangerous
            NotificationType.INFO -> AppScreen.Dashboard
        }
    }

    // Profile Dropdown Functions
    fun toggleProfileDropdown() {
        showProfileDropdown = !showProfileDropdown
        // Close notification panel if opening profile
        if (showProfileDropdown) {
            showNotificationPanel = false
        }
    }

    fun dismissProfileDropdown() {
        showProfileDropdown = false
    }

    // Profile Edit Functions (parity with Web app shared-ui.js showEditProfileModal)
    fun openEditProfileModal() {
        showProfileDropdown = false
        showEditProfileModal = true
    }

    fun dismissEditProfileModal() {
        showEditProfileModal = false
    }

    fun saveUserProfile(name: String, email: String, role: String, initials: String? = null) {
        userName = name.trim().ifBlank { "QR-SHIELD User" }
        userEmail = email.trim().ifBlank { "user@example.com" }
        userRole = role.trim().ifBlank { "Security Analyst" }
        // Auto-generate initials from name if not provided
        userInitials = initials?.trim()?.takeIf { it.isNotBlank() } ?: generateInitials(userName)
        showEditProfileModal = false
        persistSettings()
        setMessage("Profile updated", MessageKind.Success)
    }

    private fun generateInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex())
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}".uppercase()
            parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
            parts.isNotEmpty() -> parts[0].take(1).uppercase() + "U"
            else -> "QU"
        }
    }

    fun submitTrainingVerdict(isPhishing: Boolean) {
        if (trainingState.isGameOver) return
        
        val expectedPhishing = isPhishingVerdict(currentTrainingScenario.expectedVerdict)
        val isCorrect = expectedPhishing == isPhishing
        val responseTimeMs = PlatformTime.currentTimeMillis() - trainingState.roundStartTimeMs
        
        // Calculate points
        var points = 0
        if (isCorrect) {
            points = 100 // Base points
            // Streak bonus
            if (trainingState.streak >= 2) {
                points += 25 * (trainingState.streak - 1)
            }
        } else {
            points = -25
        }
        
        val nextScore = (trainingState.score + points).coerceAtLeast(0)
        val nextStreak = if (isCorrect) trainingState.streak + 1 else 0
        val nextBestStreak = maxOf(trainingState.bestStreak, nextStreak)
        val nextCorrect = if (isCorrect) trainingState.correct + 1 else trainingState.correct
        val nextAttempts = trainingState.attempts + 1
        
        // Bot always gets 100 points per round
        val nextBotScore = trainingState.botScore + 100
        
        trainingState = trainingState.copy(
            score = nextScore,
            streak = nextStreak,
            bestStreak = nextBestStreak,
            correct = nextCorrect,
            attempts = nextAttempts,
            botScore = nextBotScore,
            showResultModal = true,
            lastRoundCorrect = isCorrect,
            lastRoundPoints = points,
            lastResponseTimeMs = responseTimeMs
        )
    }

    fun dismissTrainingResultModal() {
        trainingState = trainingState.copy(showResultModal = false)
        advanceTrainingRound()
    }

    fun skipTrainingRound() {
        // Bot gets points, player skips
        trainingState = trainingState.copy(
            botScore = trainingState.botScore + 100,
            streak = 0
        )
        advanceTrainingRound()
    }

    fun resetTrainingGame() {
        shuffledChallengeIndices = trainingScenarios.indices.shuffled()
        trainingScenarioIndex = 0
        trainingState = createInitialTrainingState()
    }


    fun endTrainingSession() {
        trainingState = trainingState.copy(isGameOver = true)
        
        // Update persistent game statistics (parity with Web app training.js saveGameStats)
        gameTotalGamesPlayed++
        gameTotalCorrect += trainingState.correct
        gameTotalAttempts += trainingState.attempts
        
        // Check for new high score
        if (trainingState.score > gameHighScore) {
            gameHighScore = trainingState.score
        }
        
        // Check for new best streak
        if (trainingState.bestStreak > gameBestStreak) {
            gameBestStreak = trainingState.bestStreak
        }
        
        // Persist game stats
        persistSettings()
    }

    fun dispose() {
        scope.cancel("AppViewModel disposed")
    }

    fun filteredHistory(): List<ScanHistoryItem> {
        val query = historySearchQuery.trim().lowercase()
        return scanHistory.filter { item ->
            val matchesFilter = when (historyFilter) {
                HistoryFilter.All -> true
                HistoryFilter.Safe -> item.verdict == Verdict.SAFE
                HistoryFilter.Suspicious -> item.verdict == Verdict.SUSPICIOUS
                HistoryFilter.Dangerous -> item.verdict == Verdict.MALICIOUS
            }
            val matchesQuery = query.isBlank() || item.url.lowercase().contains(query)
            matchesFilter && matchesQuery
        }
    }

    fun filteredAllowlist(): List<String> {
        val query = allowlistQuery.trim().lowercase()
        return allowlist.filter { query.isBlank() || it.lowercase().contains(query) }
    }

    fun filteredBlocklist(): List<String> {
        val query = blocklistQuery.trim().lowercase()
        return blocklist.filter { query.isBlank() || it.lowercase().contains(query) }
    }

    fun formatRelativeTime(millis: Long): String = PlatformTime.formatRelativeTime(millis)

    fun formatTimestamp(millis: Long): String = PlatformTime.formatTimestamp(millis)

    fun sanitizedUrl(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val sanitized = java.net.URI(uri.scheme, uri.authority, uri.path, null, null)
            sanitized.toString().ifBlank { url }
        } catch (e: Exception) {
            url
        }
    }

    fun hostFromUrl(url: String): String? {
        return try {
            val uri = java.net.URI(url)
            uri.host ?: url.substringAfter("://").substringBefore("/")
        } catch (e: Exception) {
            null
        }
    }

    private fun updateResult(
        url: String,
        assessment: RiskAssessment,
        source: ScanSource,
        recordHistory: Boolean,
        durationMs: Long
    ) {
        currentUrl = url
        currentAssessment = assessment
        currentVerdict = assessment.verdict
        currentVerdictDetails = verdictEngine.enrich(assessment)
        lastAnalyzedAt = PlatformTime.currentTimeMillis()
        lastAnalysisDurationMs = durationMs
        scanState = DesktopScanState.Result(url, assessment)

        if (recordHistory) {
            scope.launch {
                historyManager.recordScan(url, assessment.score, assessment.verdict, source)
            }
        }

        if (assessment.verdict == Verdict.SAFE && trustCentreToggles.autoCopySafe) {
            copyUrl(url, label = "Safe link copied")
        }

        // Trigger notifications based on verdict
        val urlPreview = url.take(40) + if (url.length > 40) "…" else ""
        when (assessment.verdict) {
            Verdict.SAFE -> addNotification(
                title = t("Scan Complete"),
                message = tf("URL analysis finished: Safe. %s", urlPreview),
                type = NotificationType.SUCCESS,
                scanUrl = url
            )
            Verdict.SUSPICIOUS -> addNotification(
                title = t("Suspicious Activity"),
                message = tf("Potentially risky URL detected. %s", urlPreview),
                type = NotificationType.WARNING,
                scanUrl = url
            )
            Verdict.MALICIOUS -> addNotification(
                title = t("Threat Blocked"),
                message = tf("Malicious URL detected! %s", urlPreview),
                type = NotificationType.ERROR,
                scanUrl = url
            )
            Verdict.UNKNOWN -> addNotification(
                title = t("Analysis Incomplete"),
                message = tf("Could not fully analyze URL. %s", urlPreview),
                type = NotificationType.INFO,
                scanUrl = url
            )
        }

        currentScreen = when (assessment.verdict) {
            Verdict.SAFE -> AppScreen.ResultSafe
            Verdict.SUSPICIOUS -> AppScreen.ResultSuspicious
            Verdict.MALICIOUS -> AppScreen.ResultDangerous
            Verdict.UNKNOWN -> AppScreen.ResultSuspicious
        }
    }

    private fun t(text: String): String = DesktopStrings.translate(text, appLanguage)

    private fun tf(text: String, vararg args: Any): String = DesktopStrings.format(text, appLanguage, *args)

    private fun setError(message: String, updateScanState: Boolean = true) {
        val translated = t(message)
        if (updateScanState) {
            scanState = DesktopScanState.Error(translated)
        }
        statusMessage = StatusMessage(translated, MessageKind.Error)
    }

    private fun setMessage(message: String, kind: MessageKind) {
        statusMessage = StatusMessage(t(message), kind)
    }

    private fun applySettings(settings: SettingsManager.Settings) {
        allowlist = settings.trustedDomains
        blocklist = settings.blockedDomains
        trustCentreToggles = TrustCentreToggles(
            strictOffline = settings.offlineOnlyEnabled,
            anonymousTelemetry = settings.telemetryEnabled,
            autoCopySafe = settings.autoCopySafeLinksEnabled
        )
        heuristicSensitivity = when (settings.heuristicSensitivity.lowercase()) {
            "low" -> HeuristicSensitivity.Low
            "paranoia" -> HeuristicSensitivity.Paranoia
            else -> HeuristicSensitivity.Balanced
        }
        appLanguage = AppLanguage.fromCode(settings.languageCode)
        phishingEngine = buildPhishingEngine(heuristicSensitivity)
        
        // User Profile fields
        userName = settings.userName
        userEmail = settings.userEmail
        userInitials = settings.userInitials
        userRole = settings.userRole
        userPlan = settings.userPlan
        
        // Game Statistics fields (parity with Web app training.js)
        gameHighScore = settings.gameHighScore
        gameBestStreak = settings.gameBestStreak
        gameTotalGamesPlayed = settings.gameTotalGamesPlayed
        gameTotalCorrect = settings.gameTotalCorrect
        gameTotalAttempts = settings.gameTotalAttempts
    }

    // Reset Settings to Default (parity with Web app trust.js resetSettings)
    var showResetSettingsConfirmation by mutableStateOf(false)

    fun showResetSettingsDialog() {
        showResetSettingsConfirmation = true
    }

    fun dismissResetSettingsDialog() {
        showResetSettingsConfirmation = false
    }

    fun resetSettingsToDefaults() {
        // Reset to default settings
        val defaults = SettingsManager.Settings()
        applySettings(defaults)
        settingsStore.save(defaults)
        
        // Reset security settings (not in Settings data class)
        autoBlockThreats = true
        realTimeScanning = true
        soundAlerts = true
        threatAlerts = true
        showConfidenceScore = true
        
        showResetSettingsConfirmation = false
        setMessage("Settings reset to defaults", MessageKind.Success)
    }

    private fun persistSettings() {
        val settings = SettingsManager.Settings(
            trustedDomains = allowlist,
            blockedDomains = blocklist,
            offlineOnlyEnabled = trustCentreToggles.strictOffline,
            blockUnknownEnabled = false,
            autoScanHistoryEnabled = true,
            autoCopySafeLinksEnabled = trustCentreToggles.autoCopySafe,
            heuristicSensitivity = when (heuristicSensitivity) {
                HeuristicSensitivity.Low -> "Low"
                HeuristicSensitivity.Balanced -> "Balanced"
                HeuristicSensitivity.Paranoia -> "Paranoia"
            },
            telemetryEnabled = trustCentreToggles.anonymousTelemetry,
            biometricLockEnabled = false,
            languageCode = appLanguage.code,
            // User Profile fields
            userName = userName,
            userEmail = userEmail,
            userInitials = userInitials,
            userRole = userRole,
            userPlan = userPlan,
            // Game Statistics fields (parity with Web app training.js)
            gameHighScore = gameHighScore,
            gameBestStreak = gameBestStreak,
            gameTotalGamesPlayed = gameTotalGamesPlayed,
            gameTotalCorrect = gameTotalCorrect,
            gameTotalAttempts = gameTotalAttempts
        )
        settingsStore.save(settings)
    }

    private fun buildPhishingEngine(sensitivity: HeuristicSensitivity): PhishingEngine {
        val config = when (sensitivity) {
            HeuristicSensitivity.Low -> ScoringConfig.LOW_SENSITIVITY
            HeuristicSensitivity.Balanced -> ScoringConfig.DEFAULT
            HeuristicSensitivity.Paranoia -> ScoringConfig.HIGH_SENSITIVITY
        }
        return PhishingEngine(config = config)
    }

    private fun computeStats(history: List<ScanHistoryItem>): ScanHistoryManager.HistoryStatistics {
        val total = history.size
        val safe = history.count { it.verdict == Verdict.SAFE }
        val suspicious = history.count { it.verdict == Verdict.SUSPICIOUS }
        val malicious = history.count { it.verdict == Verdict.MALICIOUS }
        val average = if (total == 0) 0.0 else history.map { it.score }.average()
        return ScanHistoryManager.HistoryStatistics(
            totalScans = total,
            safeCount = safe,
            suspiciousCount = suspicious,
            maliciousCount = malicious,
            averageScore = average
        )
    }

    private fun emptyStats(): ScanHistoryManager.HistoryStatistics {
        return ScanHistoryManager.HistoryStatistics(
            totalScans = 0,
            safeCount = 0,
            suspiciousCount = 0,
            maliciousCount = 0,
            averageScore = 0.0
        )
    }

    private fun normalizeDomain(value: String): String {
        val trimmed = value.trim()
        return if (trimmed.startsWith("*.")) trimmed.removePrefix("*.") else trimmed
    }

    private fun defaultExportFile(baseName: String, extension: String): File {
        val dir = defaultExportDirectory()
        val safeName = if (baseName.isBlank()) "qrshield_export" else baseName
        return File(dir, "$safeName.$extension")
    }

    private fun defaultExportDirectory(): File {
        val home = System.getProperty("user.home") ?: "."
        val downloads = File(home, "Downloads")
        return if (downloads.exists()) downloads else File(home)
    }

    private fun isPhishingVerdict(verdict: Verdict): Boolean {
        return verdict == Verdict.MALICIOUS || verdict == Verdict.SUSPICIOUS
    }

    private fun advanceTrainingRound() {
        if (trainingState.round >= trainingState.totalRounds) {
            // Game over
            trainingState = trainingState.copy(isGameOver = true)
            return
        }
        
        val nextRound = trainingState.round + 1
        trainingScenarioIndex = (trainingScenarioIndex + 1) % shuffledChallengeIndices.size
        trainingState = trainingState.copy(
            round = nextRound,
            roundStartTimeMs = PlatformTime.currentTimeMillis()
        )
    }

    private val trainingScenarios: List<TrainingScenario>
        get() = listOf(
        // Challenge 1: AusPost Phishing
        TrainingScenario(
            payload = "https://au-post-tracking.verify-deliveries.net/login",
            contextTitle = t("SMS Message"),
            contextBody = t("\"AusPost: Your parcel failed delivery due to incorrect address details. Please update immediately via the link above to avoid return.\""),
            expectedVerdict = Verdict.MALICIOUS,
            aiConfidence = 0.97f,
            insights = listOf(
                TrainingInsight(
                    icon = "warning",
                    title = t("Suspicious Domain"),
                    body = t("Domain uses hyphenated structure (verify-deliveries.net) - common in phishing."),
                    kind = TrainingInsightKind.Warning
                ),
                TrainingInsight(
                    icon = "sms",
                    title = t("Unknown Sender"),
                    body = t("SMS from unknown number impersonating AusPost."),
                    kind = TrainingInsightKind.Suspicious
                ),
                TrainingInsight(
                    icon = "schedule",
                    title = t("Recently Registered"),
                    body = t("Domain age < 30 days (newly registered)."),
                    kind = TrainingInsightKind.Warning
                )
            )
        ),
        // Challenge 2: GitHub (Safe)
        TrainingScenario(
            payload = "https://www.github.com/login",
            contextTitle = t("Email Notification"),
            contextBody = t("Sign in to your GitHub account to access your repositories."),
            expectedVerdict = Verdict.SAFE,
            aiConfidence = 0.99f,
            insights = listOf(
                TrainingInsight(
                    icon = "verified_user",
                    title = t("Verified Domain"),
                    body = t("Certificate Issuer matches domain owner (DigiCert Inc)."),
                    kind = TrainingInsightKind.Psychology
                ),
                TrainingInsight(
                    icon = "history",
                    title = t("Established Domain"),
                    body = t("Domain age > 10 years (High trust)."),
                    kind = TrainingInsightKind.Psychology
                )
            )
        ),
        // Challenge 3: Commonwealth Bank Homograph
        TrainingScenario(
            payload = "https://secure-banking.c0mmonwealth.net/verify",
            contextTitle = t("Urgent SMS"),
            contextBody = t("\"Commonwealth Bank: Your account has been locked due to suspicious activity. Verify your identity immediately.\""),
            expectedVerdict = Verdict.MALICIOUS,
            aiConfidence = 0.998f,
            insights = listOf(
                TrainingInsight(
                    icon = "warning",
                    title = t("Homograph Attack"),
                    body = t("\"c0mmonwealth\" uses zero instead of letter O - deceptive spelling."),
                    kind = TrainingInsightKind.Warning
                ),
                TrainingInsight(
                    icon = "psychology",
                    title = t("Urgency Tactics"),
                    body = t("Message creates fear with \"locked\" and \"immediately\" - classic phishing."),
                    kind = TrainingInsightKind.Psychology
                )
            )
        ),
        // Challenge 4: Atlassian (Safe)
        TrainingScenario(
            payload = "https://www.atlassian.com/software/jira",
            contextTitle = t("Work Email"),
            contextBody = t("Welcome to Jira - project tracking for agile teams."),
            expectedVerdict = Verdict.SAFE,
            aiConfidence = 0.98f,
            insights = listOf(
                TrainingInsight(
                    icon = "verified",
                    title = t("Extended Validation"),
                    body = t("Valid EV certificate from trusted authority."),
                    kind = TrainingInsightKind.Psychology
                ),
                TrainingInsight(
                    icon = "history_edu",
                    title = t("Established Company"),
                    body = t("Domain age > 15 years - Alexa Top 1000 site."),
                    kind = TrainingInsightKind.Psychology
                )
            )
        ),
        // Challenge 5: Apple Phishing
        TrainingScenario(
            payload = "http://signin.apple-id-verify.com/account",
            contextTitle = t("Email Alert"),
            contextBody = t("\"Your Apple ID was used to sign in on a new device. If this was not you, verify immediately.\""),
            expectedVerdict = Verdict.MALICIOUS,
            aiConfidence = 0.96f,
            insights = listOf(
                TrainingInsight(
                    icon = "warning",
                    title = t("Fake Domain"),
                    body = t("Domain \"apple-id-verify.com\" is NOT owned by Apple."),
                    kind = TrainingInsightKind.Warning
                ),
                TrainingInsight(
                    icon = "lock_open",
                    title = t("No HTTPS"),
                    body = t("HTTP instead of HTTPS - no encryption."),
                    kind = TrainingInsightKind.Warning
                )
            )
        ),
        // Challenge 6: Gmail (Safe)
        TrainingScenario(
            payload = "https://mail.google.com/mail/u/0/",
            contextTitle = t("Browser Bookmark"),
            contextBody = t("You have 3 unread emails in your inbox."),
            expectedVerdict = Verdict.SAFE,
            aiConfidence = 0.99f,
            insights = listOf(
                TrainingInsight(
                    icon = "verified_user",
                    title = t("Google Subdomain"),
                    body = t("Subdomain of google.com - trusted infrastructure."),
                    kind = TrainingInsightKind.Psychology
                ),
                TrainingInsight(
                    icon = "lock",
                    title = t("Valid Certificate"),
                    body = t("Standard Gmail URL structure with HTTPS."),
                    kind = TrainingInsightKind.Psychology
                )
            )
        ),
        // Challenge 7: PayPal Homograph
        TrainingScenario(
            payload = "https://www.paypa1-secure.com/login",
            contextTitle = t("Email Warning"),
            contextBody = t("\"Your PayPal account has been limited. Please update your information.\""),
            expectedVerdict = Verdict.MALICIOUS,
            aiConfidence = 0.995f,
            insights = listOf(
                TrainingInsight(
                    icon = "warning",
                    title = t("Homograph Attack"),
                    body = t("\"paypa1\" uses number 1 instead of letter L."),
                    kind = TrainingInsightKind.Warning
                ),
                TrainingInsight(
                    icon = "new_releases",
                    title = t("Newly Registered"),
                    body = t("Domain registered in the last 24 hours."),
                    kind = TrainingInsightKind.Suspicious
                )
            )
        ),
        // Challenge 8: LinkedIn (Safe)
        TrainingScenario(
            payload = "https://linkedin.com/in/john-smith",
            contextTitle = t("Connection Request"),
            contextBody = t("View John Smith's professional profile on LinkedIn."),
            expectedVerdict = Verdict.SAFE,
            aiConfidence = 0.98f,
            insights = listOf(
                TrainingInsight(
                    icon = "verified",
                    title = t("Official Domain"),
                    body = t("Official LinkedIn domain with standard profile URL structure."),
                    kind = TrainingInsightKind.Psychology
                ),
                TrainingInsight(
                    icon = "business",
                    title = t("Microsoft Platform"),
                    body = t("LinkedIn is a Microsoft-owned platform."),
                    kind = TrainingInsightKind.Psychology
                )
            )
        ),
        // Challenge 9: Bit.ly Scam
        TrainingScenario(
            payload = "https://bit.ly/3x8K9mZ",
            contextTitle = t("Prize SMS"),
            contextBody = t("\"You won a \$500 gift card! Click to claim your prize.\""),
            expectedVerdict = Verdict.MALICIOUS,
            aiConfidence = 0.92f,
            insights = listOf(
                TrainingInsight(
                    icon = "link_off",
                    title = t("Shortened URL"),
                    body = t("URL shorteners hide the true destination."),
                    kind = TrainingInsightKind.Warning
                ),
                TrainingInsight(
                    icon = "money_off",
                    title = t("Prize Scam"),
                    body = t("Unsolicited prize notifications are almost always scams."),
                    kind = TrainingInsightKind.Psychology
                )
            )
        ),
        // Challenge 10: Google Docs (Safe)
        TrainingScenario(
            payload = "https://docs.google.com/document/d/1abc123",
            contextTitle = t("Share Notification"),
            contextBody = t("Sarah shared a document with you: Q4 Report.docx"),
            expectedVerdict = Verdict.SAFE,
            aiConfidence = 0.97f,
            insights = listOf(
                TrainingInsight(
                    icon = "verified_user",
                    title = t("Google Subdomain"),
                    body = t("docs.google.com is the legitimate Google Docs address."),
                    kind = TrainingInsightKind.Psychology
                ),
                TrainingInsight(
                    icon = "description",
                    title = t("Standard Format"),
                    body = t("Standard Google Docs URL format with valid certificate."),
                    kind = TrainingInsightKind.Psychology
                )
            )
        )
    )
}

/**
 * Creates the initial training game state
 */
private fun createInitialTrainingState(): TrainingState {
    return TrainingState(
        module = 1,
        round = 1,
        totalRounds = 10,
        score = 0,
        streak = 0,
        bestStreak = 0,
        correct = 0,
        attempts = 0,
        remainingSeconds = 15 * 60, // 15 minutes
        botScore = 0,
        sessionId = generateSessionId(),
        roundStartTimeMs = PlatformTime.currentTimeMillis(),
        isGameOver = false,
        showResultModal = false,
        lastRoundCorrect = null,
        lastRoundPoints = 0,
        lastResponseTimeMs = 0
    )
}

/**
 * Generates a random session ID for the game
 */
private fun generateSessionId(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..4).map { chars.random() }.joinToString("")
}
