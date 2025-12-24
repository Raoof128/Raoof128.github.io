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
    val correct: Int,
    val attempts: Int,
    val remainingSeconds: Int
) {
    val accuracy: Float
        get() = if (attempts == 0) 0f else correct.toFloat() / attempts.toFloat()
    val progress: Float
        get() = if (totalRounds == 0) 0f else round.toFloat() / totalRounds.toFloat()
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
    var exportFilename by mutableStateOf("scan_report_20231024_8821X")
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
    var notifications by mutableStateOf(sampleNotifications())

    var trainingState by mutableStateOf(
        TrainingState(
            module = 3,
            round = 3,
            totalRounds = 10,
            score = 1250,
            streak = 5,
            correct = 23,
            attempts = 25,
            remainingSeconds = 12 * 60 + 5
        )
    )
    private var trainingScenarioIndex by mutableStateOf(0)
    val currentTrainingScenario: TrainingScenario
        get() = trainingScenarios[trainingScenarioIndex]

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

    fun updateHistoryFilter(filter: HistoryFilter) {
        historyFilter = filter
    }

    fun updateHistorySearch(query: String) {
        historySearchQuery = query
    }

    fun selectHistoryItem(item: ScanHistoryItem) {
        analyzeUrl(item.url, item.source, recordHistory = false)
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

    fun addNotification(title: String, message: String, type: NotificationType) {
        val now = PlatformTime.currentTimeMillis()
        val newNotification = AppNotification(
            id = "notif_$now",
            title = title,
            message = message,
            type = type,
            timestamp = now,
            isRead = false
        )
        notifications = listOf(newNotification) + notifications
    }

    fun submitTrainingVerdict(isPhishing: Boolean) {
        val expectedPhishing = isPhishingVerdict(currentTrainingScenario.expectedVerdict)
        val nextAttempts = trainingState.attempts + 1
        val isCorrect = expectedPhishing == isPhishing
        val nextScore = if (isCorrect) trainingState.score + 50 else (trainingState.score - 25).coerceAtLeast(0)
        val nextStreak = if (isCorrect) trainingState.streak + 1 else 0
        val nextCorrect = if (isCorrect) trainingState.correct + 1 else trainingState.correct

        trainingState = trainingState.copy(
            score = nextScore,
            streak = nextStreak,
            correct = nextCorrect,
            attempts = nextAttempts
        )
        setMessage(if (isCorrect) "Correct answer" else "Incorrect answer", if (isCorrect) MessageKind.Success else MessageKind.Error)
        advanceTrainingRound()
    }

    fun skipTrainingRound() {
        setMessage("Round skipped", MessageKind.Info)
        advanceTrainingRound()
    }

    fun nextTrainingRound() {
        advanceTrainingRound()
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
            languageCode = appLanguage.code
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
        val nextRound = if (trainingState.round >= trainingState.totalRounds) 1 else trainingState.round + 1
        trainingScenarioIndex = (trainingScenarioIndex + 1) % trainingScenarios.size
        trainingState = trainingState.copy(round = nextRound)
    }

    private val trainingScenarios: List<TrainingScenario>
        get() = listOf(
        TrainingScenario(
            payload = "https://secure-login.micros0ft-support.com/auth?client_id=19283",
            contextTitle = t("Physical Flyer"),
            contextBody = t("Found this on a table at Starbeans Coffee. It offered a free coffee coupon if I logged in."),
            expectedVerdict = Verdict.MALICIOUS,
            aiConfidence = 0.998f,
            insights = listOf(
                TrainingInsight(
                    icon = "warning",
                    title = t("Typosquatting Detected"),
                    body = t("The domain micros0ft-support.com uses a zero '0' instead of the letter 'o'."),
                    kind = TrainingInsightKind.Warning
                ),
                TrainingInsight(
                    icon = "public_off",
                    title = t("Suspicious TLD"),
                    body = t("While .com is standard, the hyphenated structure with \"support\" is common in phishing."),
                    kind = TrainingInsightKind.Suspicious
                ),
                TrainingInsight(
                    icon = "psychology",
                    title = t("Social Engineering"),
                    body = t("The \"free coupon\" promise creates urgency and incentive."),
                    kind = TrainingInsightKind.Psychology
                )
            )
        ),
        TrainingScenario(
            payload = "https://accounts.google.com/o/oauth2/v2/auth",
            contextTitle = t("Support Email"),
            contextBody = t("Corporate IT sent a reset notice using the official Google auth domain."),
            expectedVerdict = Verdict.SAFE,
            aiConfidence = 0.95f,
            insights = listOf(
                TrainingInsight(
                    icon = "verified_user",
                    title = t("Verified Domain"),
                    body = t("OAuth endpoint uses a trusted, well-known domain."),
                    kind = TrainingInsightKind.Psychology
                ),
                TrainingInsight(
                    icon = "lock",
                    title = t("TLS Enabled"),
                    body = t("HTTPS with valid certificate detected."),
                    kind = TrainingInsightKind.Suspicious
                )
            )
        ),
        TrainingScenario(
            payload = "https://bit.ly/3x89s",
            contextTitle = t("Chat Message"),
            contextBody = t("A shortened link shared in a group chat without context."),
            expectedVerdict = Verdict.SUSPICIOUS,
            aiConfidence = 0.83f,
            insights = listOf(
                TrainingInsight(
                    icon = "link",
                    title = t("Shortened URL"),
                    body = t("URL shorteners hide the true destination."),
                    kind = TrainingInsightKind.Warning
                ),
                TrainingInsight(
                    icon = "visibility_off",
                    title = t("Low Context"),
                    body = t("No source attribution or description provided."),
                    kind = TrainingInsightKind.Suspicious
                )
            )
        )
    )
}

/**
 * Provides sample notifications for demonstration purposes
 */
private fun sampleNotifications(): List<AppNotification> {
    val now = PlatformTime.currentTimeMillis()
    val minute = 60_000L
    val hour = 60 * minute
    return listOf(
        AppNotification(
            id = "notif_1",
            title = "Threat Blocked",
            message = "Malicious URL detected and blocked automatically.",
            type = NotificationType.ERROR,
            timestamp = now - 5 * minute,
            isRead = false
        ),
        AppNotification(
            id = "notif_2",
            title = "Scan Complete",
            message = "URL analysis finished. Result: Safe.",
            type = NotificationType.SUCCESS,
            timestamp = now - 30 * minute,
            isRead = false
        ),
        AppNotification(
            id = "notif_3",
            title = "Database Updated",
            message = "Threat signatures updated to latest version.",
            type = NotificationType.INFO,
            timestamp = now - 2 * hour,
            isRead = true
        ),
        AppNotification(
            id = "notif_4",
            title = "Suspicious Activity",
            message = "A domain on your blocklist was accessed.",
            type = NotificationType.WARNING,
            timestamp = now - 4 * hour,
            isRead = true
        )
    )
}
