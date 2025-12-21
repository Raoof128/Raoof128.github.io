package com.qrshield.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qrshield.desktop.navigation.AppScreen

enum class ScanMonitorViewMode { Visual, Raw }
enum class ResultViewMode { Simple, Technical }
enum class ExportFormat { Pdf, Json }

data class TrustCentreToggles(
    val strictOffline: Boolean,
    val anonymousTelemetry: Boolean,
    val autoCopySafe: Boolean
)

class AppViewModel {
    var currentScreen by mutableStateOf<AppScreen>(AppScreen.Dashboard)
    var isDarkMode by mutableStateOf(false)

    var scanMonitorViewMode by mutableStateOf(ScanMonitorViewMode.Visual)
    var resultSafeViewMode by mutableStateOf(ResultViewMode.Simple)
    var resultDangerousViewMode by mutableStateOf(ResultViewMode.Technical)

    var trustCentreToggles by mutableStateOf(
        TrustCentreToggles(
            strictOffline = true,
            anonymousTelemetry = false,
            autoCopySafe = true
        )
    )

    var exportFormat by mutableStateOf(ExportFormat.Pdf)
    var exportFilename by mutableStateOf("scan_report_20231024_8821X")
    var exportIncludeVerdict by mutableStateOf(true)
    var exportIncludeMetadata by mutableStateOf(true)
    var exportIncludeRawPayload by mutableStateOf(false)
    var exportIncludeDebugLogs by mutableStateOf(false)

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }
}
