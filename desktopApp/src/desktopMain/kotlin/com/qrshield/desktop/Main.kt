package com.qrshield.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.res.painterResource
import com.qrshield.desktop.navigation.AppScreen
import com.qrshield.desktop.screens.*
import java.awt.Dimension

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
