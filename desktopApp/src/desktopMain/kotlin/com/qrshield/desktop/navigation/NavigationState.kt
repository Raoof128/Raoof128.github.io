package com.qrshield.desktop.navigation

sealed class AppScreen {
    data object Dashboard : AppScreen()
    data object LiveScan : AppScreen()
    data object ScanHistory : AppScreen()
    data object TrustCentre : AppScreen()
    data object TrustCentreAlt : AppScreen()
    data object Training : AppScreen()
    data object ReportsExport : AppScreen()
    data object ResultSafe : AppScreen()
    data object ResultSuspicious : AppScreen()
    data object ResultDangerous : AppScreen()
    data object ResultDangerousAlt : AppScreen()
}
