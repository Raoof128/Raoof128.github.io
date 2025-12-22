package com.qrshield.web.i18n

import kotlinx.browser.window

enum class WebLanguage(val code: String) {
    English("en"),
    German("de"),
    Spanish("es"),
    French("fr"),
    ChineseSimplified("zh"),
    Japanese("ja"),
    Hindi("hi");

    companion object {
        fun fromCode(code: String): WebLanguage {
            val lang = code.lowercase().substringBefore("-")
            return when (lang) {
                "de" -> German
                "es" -> Spanish
                "fr" -> French
                "zh" -> ChineseSimplified
                "ja" -> Japanese
                "hi" -> Hindi
                else -> English
            }
        }

        fun current(): WebLanguage = fromCode(window.navigator.language)
    }
}

enum class WebStringKey(val defaultText: String) {
    AppName("QR-SHIELD"),
    
    // Navigation
    MenuMain("Main Menu"),
    MenuSecurity("Security"),
    MenuSystem("System"),
    NavDashboard("Dashboard"),
    NavScanMonitor("Scan Monitor"),
    NavScanHistory("Scan History"),
    NavTrustCentre("Trust Centre"),
    NavReports("Reports"),
    NavTraining("Training"),
    NavSettings("Settings"),

    // Dashboard Actions
    StartScan("Start New Scan"),
    ImportImage("Import Image"),
    
    // Dashboard Metrics
    SystemHealth("System Health"),
    ThreatDatabase("Threat Database"),
    Current("Current"),
    Active("Active"),
    Threats("Threats"),
    SafeScans("Safe Scans"),
    
    // Footer / Status
    EngineActive("Engine Active"),
    EnterpriseProtection("Enterprise Protection Active")
}

object WebStrings {
    fun get(key: WebStringKey, language: WebLanguage = WebLanguage.current()): String {
        return when (language) {
            WebLanguage.German -> GermanStrings[key] ?: key.defaultText
            WebLanguage.Spanish -> SpanishStrings[key] ?: key.defaultText
            WebLanguage.French -> FrenchStrings[key] ?: key.defaultText
            WebLanguage.ChineseSimplified -> ChineseStrings[key] ?: key.defaultText
            WebLanguage.Japanese -> JapaneseStrings[key] ?: key.defaultText
            WebLanguage.Hindi -> HindiStrings[key] ?: key.defaultText
            WebLanguage.English -> key.defaultText
        }
    }

    fun translate(text: String, language: WebLanguage = WebLanguage.current()): String {
         return when (language) {
            WebLanguage.German -> GermanCommonStrings[text] ?: text
            WebLanguage.Spanish -> SpanishCommonStrings[text] ?: text
            WebLanguage.French -> FrenchCommonStrings[text] ?: text
            WebLanguage.ChineseSimplified -> ChineseCommonStrings[text] ?: text
            WebLanguage.Japanese -> JapaneseCommonStrings[text] ?: text
            WebLanguage.Hindi -> HindiCommonStrings[text] ?: text
            WebLanguage.English -> text
        }
    }
}
