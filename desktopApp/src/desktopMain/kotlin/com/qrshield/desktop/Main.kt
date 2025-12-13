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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.qrshield.core.PhishingEngine
import com.qrshield.desktop.components.*
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.theme.QRShieldDarkColors
import com.qrshield.desktop.theme.QRShieldLightColors
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

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
        state = windowState
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

    // Phishing engine
    val phishingEngine = remember { PhishingEngine() }

    var urlInput by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var scanHistory by remember { mutableStateOf<List<AnalysisResult>>(emptyList()) }

    // Focus requester for keyboard navigation
    val inputFocusRequester = remember { FocusRequester() }

    // Analysis function extracted for reuse
    val performAnalysis: () -> Unit = {
        if (urlInput.isNotBlank() && !isAnalyzing) {
            isAnalyzing = true
            val assessment = phishingEngine.analyze(urlInput)
            val result = AnalysisResult(
                url = urlInput,
                score = assessment.score,
                verdict = assessment.verdict,
                flags = assessment.flags,
                timestamp = System.currentTimeMillis()
            )
            analysisResult = result
            scanHistory = listOf(result) + scanHistory.take(9)
            isAnalyzing = false
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top App Bar with gradient
                EnhancedTopAppBar(
                    isDarkMode = isDarkMode,
                    onThemeToggle = { isDarkMode = !isDarkMode }
                )

                // Main Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Hero Section with animated shield
                    AnimatedHeroSection()

                    // Stats Row
                    StatsRow(scanCount = scanHistory.size)

                    // Scanner Card
                    EnhancedScannerCard(
                        urlInput = urlInput,
                        onUrlChange = { urlInput = it },
                        isAnalyzing = isAnalyzing,
                        onAnalyze = performAnalysis
                    )

                    // Quick Actions
                    QuickActionsRow(
                        onPasteFromClipboard = pasteFromClipboard,
                        onClearInput = {
                            urlInput = ""
                            analysisResult = null
                        }
                    )

                    // Results
                    AnimatedVisibility(
                        visible = analysisResult != null,
                        enter = fadeIn() + slideInVertically { -40 },
                        exit = fadeOut() + slideOutVertically { -40 }
                    ) {
                        analysisResult?.let { result ->
                            EnhancedResultCard(result = result, isDarkMode = isDarkMode)
                        }
                    }

                    // Features Grid
                    EnhancedFeaturesGrid()

                    // Recent Scans
                    if (scanHistory.isNotEmpty()) {
                        RecentScansSection(
                            scans = scanHistory,
                            onScanClick = { result ->
                                urlInput = result.url
                                analysisResult = result
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Footer
                    EnhancedFooter()
                }
            }
        }
    }
}
