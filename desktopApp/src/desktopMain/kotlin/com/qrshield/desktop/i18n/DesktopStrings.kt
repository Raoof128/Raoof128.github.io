package com.qrshield.desktop.i18n

import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String) {
    English("en", "English"),
    German("de", "Deutsch");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return when (code.lowercase()) {
                "de", "de-de", "de_at", "de-ch" -> German
                else -> English
            }
        }

        fun systemDefault(): AppLanguage {
            val language = Locale.getDefault().language
            return fromCode(language)
        }
    }
}

enum class DesktopStringKey(val defaultText: String) {
    AppName("QR-SHIELD"),
    MenuMain("Main Menu"),
    MenuSecurity("Security"),
    MenuSystem("System"),
    NavDashboard("Dashboard"),
    NavScanMonitor("Scan Monitor"),
    NavScanHistory("Scan History"),
    NavTrustCentre("Trust Centre"),
    NavReports("Reports"),
    NavTraining("Training"),
    NavSettings("Settings")
}

object DesktopStrings {
    private val german = mapOf(
        DesktopStringKey.AppName to "QR-SHIELD",
        DesktopStringKey.MenuMain to "Hauptmenue",
        DesktopStringKey.MenuSecurity to "Sicherheit",
        DesktopStringKey.MenuSystem to "System",
        DesktopStringKey.NavDashboard to "Dashboard",
        DesktopStringKey.NavScanMonitor to "Scanmonitor",
        DesktopStringKey.NavScanHistory to "Scanverlauf",
        DesktopStringKey.NavTrustCentre to "Vertrauenszentrum",
        DesktopStringKey.NavReports to "Berichte",
        DesktopStringKey.NavTraining to "Training",
        DesktopStringKey.NavSettings to "Einstellungen"
    )

    fun text(key: DesktopStringKey, language: AppLanguage): String {
        return when (language) {
            AppLanguage.German -> german[key] ?: key.defaultText
            AppLanguage.English -> key.defaultText
        }
    }
}
