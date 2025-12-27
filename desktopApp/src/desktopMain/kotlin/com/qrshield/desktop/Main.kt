@file:Suppress("DEPRECATION") // painterResource - migration to Compose Resources planned

package com.qrshield.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.screens.*
import com.qrshield.model.ScanSource
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(1280.dp, 850.dp),
        position = WindowPosition(Alignment.Center)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "QR-SHIELD Desktop",
        state = windowState,
        resizable = true,
        icon = painterResource("assets/app-icon.png")
    ) {
        DisposableEffect(window) {
            window.minimumSize = Dimension(1200, 800)
            onDispose { }
        }
        val viewModel = remember { AppViewModel() }
        DisposableEffect(Unit) {
            onDispose { viewModel.dispose() }
        }
        QRShieldApp(viewModel = viewModel)
    }
}

@Composable
@Preview
fun QRShieldApp(viewModel: AppViewModel) {
    val focusRequester = remember { FocusRequester() }
    
    // Request focus for global keyboard handling
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    handleGlobalKeyEvent(event, viewModel)
                } else false
            }
    ) {
        when (viewModel.currentScreen) {
            AppScreen.Dashboard -> DashboardScreen(viewModel = viewModel)
            AppScreen.LiveScan -> LiveScanScreen(viewModel = viewModel)
            AppScreen.ScanHistory -> ScanHistoryScreen(viewModel = viewModel)
            AppScreen.TrustCentre -> TrustCentreScreen(viewModel = viewModel)
            AppScreen.TrustCentreAlt -> TrustCentreAltScreen(viewModel = viewModel)
            AppScreen.Training -> TrainingScreen(viewModel = viewModel)
            AppScreen.ReportsExport -> ReportsExportScreen(viewModel = viewModel)
            AppScreen.ResultSafe -> ResultSafeScreen(viewModel = viewModel)
            AppScreen.ResultSuspicious -> ResultSuspiciousScreen(viewModel = viewModel)
            AppScreen.ResultDangerous -> ResultDangerousScreen(viewModel = viewModel)
            AppScreen.ResultDangerousAlt -> ResultDangerousAltScreen(viewModel = viewModel)
        }
    }
}

/**
 * Handles global keyboard shortcuts for desktop UX.
 * 
 * Shortcuts:
 * - Cmd/Ctrl+V: Paste URL from clipboard and analyze
 * - Cmd/Ctrl+,: Open Settings (TrustCentreAlt)
 * - Cmd/Ctrl+1: Go to Dashboard
 * - Cmd/Ctrl+2: Go to Live Scan
 * - Cmd/Ctrl+3: Go to Scan History
 * - Cmd/Ctrl+4: Go to Training
 * - Cmd/Ctrl+F: Find/Search (go to Scan History)
 * - I: Import image (parity with Web app scanner.js)
 * - Escape: Go back from result screens
 */
private fun handleGlobalKeyEvent(event: KeyEvent, viewModel: AppViewModel): Boolean {
    val isCtrlOrCmd = event.isCtrlPressed || event.isMetaPressed
    
    return when {
        // Cmd/Ctrl+V: Paste from clipboard and analyze
        isCtrlOrCmd && event.key == Key.V -> {
            pasteAndAnalyze(viewModel)
            true
        }
        
        // Cmd/Ctrl+, (Comma): Open Settings
        isCtrlOrCmd && event.key == Key.Comma -> {
            viewModel.currentScreen = AppScreen.TrustCentreAlt
            true
        }
        
        // Cmd/Ctrl+1: Dashboard
        isCtrlOrCmd && event.key == Key.One -> {
            viewModel.currentScreen = AppScreen.Dashboard
            true
        }
        
        // Cmd/Ctrl+2: Live Scan
        isCtrlOrCmd && event.key == Key.Two -> {
            viewModel.currentScreen = AppScreen.LiveScan
            true
        }
        
        // Cmd/Ctrl+3: Scan History
        isCtrlOrCmd && event.key == Key.Three -> {
            viewModel.currentScreen = AppScreen.ScanHistory
            true
        }
        
        // Cmd/Ctrl+4: Training
        isCtrlOrCmd && event.key == Key.Four -> {
            viewModel.currentScreen = AppScreen.Training
            true
        }
        
        // Cmd/Ctrl+F: Find/Search - go to Scan History (which has search)
        isCtrlOrCmd && event.key == Key.F -> {
            viewModel.currentScreen = AppScreen.ScanHistory
            true
        }
        
        // I: Import image (parity with Web app scanner.js)
        !isCtrlOrCmd && event.key == Key.I -> {
            handleImportShortcut(viewModel)
            true
        }
        
        // Note: G key removed to avoid interference with typing URLs containing 'G'
        
        // Escape: Go back from result/secondary screens
        event.key == Key.Escape -> {
            handleEscapeKey(viewModel)
        }
        
        else -> false
    }
}

/**
 * Handle Import/Gallery keyboard shortcut.
 * Opens file picker for image import with QR code.
 */
private fun handleImportShortcut(viewModel: AppViewModel) {
    viewModel.currentScreen = AppScreen.LiveScan
    viewModel.pickImageAndScan()
}

/**
 * Paste URL from system clipboard and analyze it.
 */
private fun pasteAndAnalyze(viewModel: AppViewModel) {
    try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            val text = clipboard.getData(DataFlavor.stringFlavor) as? String
            if (!text.isNullOrBlank()) {
                viewModel.analyzeUrl(text.trim(), ScanSource.CLIPBOARD)
            }
        }
    } catch (e: Exception) {
        // Clipboard access can fail; silently ignore
    }
}

/**
 * Handle Escape key - go back from result/secondary screens.
 */
private fun handleEscapeKey(viewModel: AppViewModel): Boolean {
    return when (viewModel.currentScreen) {
        // Result screens -> go back to LiveScan
        AppScreen.ResultSafe,
        AppScreen.ResultSuspicious,
        AppScreen.ResultDangerous,
        AppScreen.ResultDangerousAlt -> {
            viewModel.currentScreen = AppScreen.LiveScan
            true
        }
        
        // Secondary screens -> go back to parent
        AppScreen.ReportsExport -> {
            viewModel.currentScreen = AppScreen.TrustCentre
            true
        }
        AppScreen.TrustCentreAlt -> {
            viewModel.currentScreen = AppScreen.TrustCentre
            true
        }
        
        // Training has its own escape handling, don't interfere
        AppScreen.Training -> false
        
        // Main screens - no escape action
        else -> false
    }
}
