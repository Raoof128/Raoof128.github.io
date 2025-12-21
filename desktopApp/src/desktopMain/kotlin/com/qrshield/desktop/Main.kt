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

package com.qrshield.desktop

import androidx.compose.animation.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.res.painterResource
import com.qrshield.core.PhishingEngine
import com.qrshield.desktop.components.*
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.navigation.Screen
import com.qrshield.desktop.navigation.Sidebar
import com.qrshield.desktop.screens.*
import com.qrshield.desktop.theme.DesktopColors
import com.qrshield.desktop.theme.QRShieldDarkColors
import com.qrshield.desktop.theme.QRShieldLightColors
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import com.qrshield.core.ScoringConfig
import com.qrshield.model.Verdict

/**
 * QR-SHIELD Desktop Application Entry Point
 *
 * A cross-platform desktop application for detecting phishing URLs.
 *
 * Features:
 * - Dark/Light theme toggle with persistence
 * - Window size/position persistence
 * - Cross-platform preferences storage
 * - Modern Material 3 design with animations
 * - Glassmorphism effects
 * - Keyboard shortcuts (Cmd/Ctrl+V paste, Enter analyze, Esc clear)
 *
 * Keyboard Shortcuts:
 * - Cmd/Ctrl+L: Focus URL input
 * - Cmd/Ctrl+V: Paste from clipboard
 * - Enter: Analyze URL
 * - Esc: Clear input and results
 * - Cmd/Ctrl+D: Toggle dark mode
 *
 * @author QR-SHIELD Team
 * @since 1.0.0
 */
fun main() = application {
    val prefs = WindowPreferences.load()

    val windowState = rememberWindowState(
        size = DpSize(prefs.width.dp, prefs.height.dp),
        position = if (prefs.x >= 0 && prefs.y >= 0) {
            WindowPosition(prefs.x.dp, prefs.y.dp)
        } else {
            WindowPosition.PlatformDefault
        }
    )

    Window(
        onCloseRequest = {
            WindowPreferences.save(
                width = windowState.size.width.value.toInt(),
                height = windowState.size.height.value.toInt(),
                x = windowState.position.x.value.toInt(),
                y = windowState.position.y.value.toInt()
            )
            exitApplication()
        },
        title = "🛡️ QR-SHIELD - QRishing Detector",
        state = windowState,
        icon = painterResource("assets/app-icon.png")
    ) {
        QRShieldDesktopApp(initialDarkMode = prefs.darkMode)
    }
}

/**
 * Main application composable.
 *
 * Orchestrates the phishing detection UI and manages application state.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun QRShieldDesktopApp(initialDarkMode: Boolean = true) {
    var isDarkMode by remember { mutableStateOf(initialDarkMode) }

    // Navigation state
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Application Settings
    var settings by remember { mutableStateOf(SettingsManager.loadSettings()) }

    // Phishing engine with dynamic config
    val phishingEngine = remember(settings.heuristicSensitivity) {
        val config = when (settings.heuristicSensitivity) {
            "Low" -> ScoringConfig.LOW_SENSITIVITY
            "Paranoia" -> ScoringConfig.HIGH_SENSITIVITY
            else -> ScoringConfig.DEFAULT
        }
        PhishingEngine(config = config)
    }

    var urlInput by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var scanHistory by remember { mutableStateOf(HistoryManager.loadHistory()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showHelpCard by remember { mutableStateOf(true) }
    
    // Dialog states
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Focus requester for keyboard navigation
    val inputFocusRequester = remember { FocusRequester() }

    // URL Validation helper
    fun isValidUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://") ||
            url.contains(".") // Allow domain-only entries
    }
    
    // Helper to update settings
    fun updateSettings(newSettings: SettingsManager.Settings) {
        settings = newSettings
        SettingsManager.saveSettings(newSettings)
    }

    // Auto-fix URL by adding https:// if missing
    fun normalizeUrl(url: String): String {
        return if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
    }

    // Analysis function extracted for reuse
    val performAnalysis: () -> Unit = performAnalysis@{
        if (urlInput.isNotBlank() && !isAnalyzing) {
            val normalizedUrl = normalizeUrl(urlInput.trim())

            // Basic validation
            if (!isValidUrl(normalizedUrl)) {
                errorMessage = "Invalid URL format. Please enter a valid URL."
                return@performAnalysis
            }

            // Check if blocked by settings
            if (settings.offlineOnlyEnabled) {
                // Engine is local-only by default, ensuring offline compliance
            }

            errorMessage = null
            isAnalyzing = true

            try {
                val assessment = phishingEngine.analyzeBlocking(normalizedUrl)
                val result = AnalysisResult(
                    url = normalizedUrl,
                    score = assessment.score,
                    verdict = assessment.verdict,
                    flags = assessment.flags,
                    timestamp = System.currentTimeMillis()
                )
                analysisResult = result
                
                // Only save history if enabled
                if (settings.autoScanHistoryEnabled) {
                    val newHistory = listOf(result) + scanHistory.take(49)
                    scanHistory = newHistory
                    HistoryManager.saveHistory(newHistory)
                }
                
                // Auto-copy Safe Links logic
                if (settings.autoCopySafeLinksEnabled && result.verdict == Verdict.SAFE) {
                     try {
                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        clipboard.setContents(java.awt.datatransfer.StringSelection(result.url), null)
                        // toastMessage = "Safe URL copied to clipboard" 
                     } catch (e: Exception) {}
                }
                
                // Update input with normalized URL
                urlInput = normalizedUrl
            } catch (e: Exception) {
                errorMessage = "Analysis failed: ${e.message}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    // Clipboard paste function
    val pasteFromClipboard: () -> Unit = {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val data = clipboard.getData(DataFlavor.stringFlavor) as? String
            if (!data.isNullOrBlank()) {
                urlInput = data.trim()
            }
        } catch (e: Exception) {
            // Clipboard access failed, ignore
        }
    }

    MaterialTheme(
        colorScheme = if (isDarkMode) QRShieldDarkColors else QRShieldLightColors
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        val isCtrlOrCmd = keyEvent.isCtrlPressed || keyEvent.isMetaPressed
                        when {
                            // Cmd/Ctrl+L: Focus input
                            isCtrlOrCmd && keyEvent.key == Key.L -> {
                                inputFocusRequester.requestFocus()
                                true
                            }
                            // Cmd/Ctrl+V: Paste
                            isCtrlOrCmd && keyEvent.key == Key.V -> {
                                pasteFromClipboard()
                                true
                            }
                            // Cmd/Ctrl+D: Toggle dark mode
                            isCtrlOrCmd && keyEvent.key == Key.D -> {
                                isDarkMode = !isDarkMode
                                true
                            }
                            // Enter: Analyze
                            keyEvent.key == Key.Enter -> {
                                performAnalysis()
                                true
                            }
                            // Escape: Clear
                            keyEvent.key == Key.Escape -> {
                                urlInput = ""
                                analysisResult = null
                                true
                            }
                            else -> false
                        }
                    } else false
                },
            color = MaterialTheme.colorScheme.background
        ) {
            // Main Layout: Sidebar + Content
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar Navigation
                Sidebar(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> currentScreen = screen }
                )

                // Main Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    DesktopColors.BrandPrimary.copy(alpha = 0.02f),
                                    Color.Transparent,
                                    DesktopColors.BrandSecondary.copy(alpha = 0.01f)
                                )
                            )
                        )
                ) {
                    // Screen Content based on navigation
                    when (val screen = currentScreen) {
                        is Screen.Dashboard -> DashboardScreen(
                            onStartScan = { currentScreen = Screen.Scanner },
                            onImportImage = { /* TODO: implement image import */ },
                            scanHistory = scanHistory,
                            onScanClick = { result ->
                                currentScreen = Screen.Results(result)
                            },
                            onViewHistory = { currentScreen = Screen.History },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                        is Screen.Scanner -> ScannerScreen(
                            urlInput = urlInput,
                            onUrlInputChange = { urlInput = it },
                            onAnalyze = performAnalysis,
                            onImageSelect = { /* TODO: image selection */ },
                            analysisResult = analysisResult,
                            isAnalyzing = isAnalyzing,
                            recentScans = scanHistory.take(5),
                            onScanClick = { result ->
                                currentScreen = Screen.Results(result)
                            },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                        is Screen.History -> HistoryScreen(
                            scanHistory = scanHistory,
                            onScanClick = { result ->
                                currentScreen = Screen.Results(result)
                            },
                            onClearHistory = {
                                scanHistory = emptyList()
                                HistoryManager.saveHistory(emptyList())
                            },
                            onExport = { /* TODO: export history */ },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        ) 
                        is Screen.TrustCentre -> TrustCentreScreen(
                            settings = settings,
                            onUpdateSettings = { updateSettings(it) },
                            onAddDomain = { domain ->
                                updateSettings(settings.copy(trustedDomains = settings.trustedDomains + domain))
                            },
                            onRemoveDomain = { domain ->
                                updateSettings(settings.copy(trustedDomains = settings.trustedDomains - domain))
                            },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                        is Screen.Training -> TrainingScreen(
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                        is Screen.Results -> ResultsScreen(
                            result = screen.result,
                            onBack = { currentScreen = Screen.Dashboard },
                            onCopyUrl = {
                                try {
                                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                    clipboard.setContents(java.awt.datatransfer.StringSelection(screen.result.url), null)
                                } catch (_: Exception) {}
                            },
                            onAddToTrusted = {
                                val domain = screen.result.url
                                    .removePrefix("https://")
                                    .removePrefix("http://")
                                    .split("/").firstOrNull() ?: ""
                                if (domain.isNotEmpty()) {
                                    updateSettings(settings.copy(trustedDomains = settings.trustedDomains + domain))
                                }
                            },
                            onScanAgain = { currentScreen = Screen.Scanner },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                        is Screen.Settings -> TrustCentreScreen(
                            settings = settings,
                            onUpdateSettings = { updateSettings(it) },
                            onAddDomain = { domain ->
                                updateSettings(settings.copy(trustedDomains = settings.trustedDomains + domain))
                            },
                            onRemoveDomain = { domain ->
                                updateSettings(settings.copy(trustedDomains = settings.trustedDomains - domain))
                            },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                        is Screen.Export -> HistoryScreen(
                            scanHistory = scanHistory,
                            onScanClick = { result -> currentScreen = Screen.Results(result) },
                            onClearHistory = { scanHistory = emptyList() },
                            onExport = { /* Export logic */ },
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = !isDarkMode }
                        )
                    }
                }
            }

            // Dialogs
            AboutDialog(
                isVisible = showAboutDialog,
                onDismiss = { showAboutDialog = false }
            )

            SettingsDialog(
                isVisible = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                isDarkMode = isDarkMode,
                onDarkModeChange = { isDarkMode = it },
                onClearHistory = {
                    scanHistory = emptyList()
                    HistoryManager.saveHistory(emptyList())
                    analysisResult = null
                    urlInput = ""
                }
            )
        }
    }
}
