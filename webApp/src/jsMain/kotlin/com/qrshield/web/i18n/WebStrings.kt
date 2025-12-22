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

        fun current(): WebLanguage {
            val stored = try {
                window.localStorage.getItem("qrshield_language")
            } catch (_: Throwable) {
                null
            }
            return if (!stored.isNullOrBlank()) {
                fromCode(stored)
            } else {
                fromCode(window.navigator.language)
            }
        }
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
    EnterpriseProtection("Enterprise Protection Active"),

    // Dashboard Sections
    SectionOverview("Overview"),
    SectionSecurity("Security"),
    SectionTraining("Training"),
    SectionReports("Reports"),
    OfflineReady("Offline Ready"),
    OfflineDescription("Local database v2.4.1 active. No data leaves this device."),

    // Dashboard Content
    Analyze("Analyze"),
    HeuristicEngine("Heuristic Engine"),
    HighPerformanceEngine("High-Performance Engine"),
    RecentScans("Recent Scans"),
    ViewFullHistory("View Full History"),
    Status("Status"),
    Source("Source"),
    Details("Details"),
    Time("Time"),
    Version("Version"),
    LastUpdate("Last Update"),
    Signatures("Signatures"),
    CheckForUpdates("Check for Updates"),
    FeatureOfflineTitle("Offline-First Architecture"),
    FeatureOfflineDesc("Complete analysis is performed locally. Your camera feed and scanned data never touch an external server, ensuring absolute privacy."),
    FeatureExplainableTitle("Explainable Security"),
    FeatureExplainableDesc("Don't just get a \"Block\". We provide detailed heuristic breakdowns of URL parameters, redirects, and javascript payloads."),
    FeaturePerformanceDesc("Optimised for desktop environments. Scans are processed in under 5ms using native Kotlin Multiplatform binaries."),
    TrainingCentre("Training Centre"),
    LearnHomograph("Learn how to identify advanced QR homograph attacks."),
    BeatTheBotAction("Beat the Bot →"),
    NoScansYet("No scans yet. Start a new scan to see results here."),
    VerdictSafe("SAFE"),
    VerdictPhish("PHISH"),
    VerdictWarn("WARN"),
    ScoreLabel("Score:"),

    // Scanner
    ActiveScanner("Active Scanner"),
    OfflineMode("Offline Mode"),
    LiveFeedDisconnected("LIVE FEED DISCONNECTED"),
    CameraAccessRequired("Camera Access Required"),
    CameraAccessDescription("Drag & drop a QR code image here or enable camera access to start scanning."),
    EnableCamera("Enable Camera"),
    Scanning("Scanning..."),
    ScanHint("Hold steady. Analysed offline in <5ms."),
    Torch("Torch"),
    Gallery("Gallery"),
    PasteUrl("Paste URL"),
    PasteUrlTitle("Paste URL to Analyze"),
    AnalyzeUrl("Analyze URL"),
    SystemStatus("System Status"),
    PhishingEngine("Phishing Engine"),
    Ready("READY"),
    LocalDb("Local DB"),
    Latency("Latency"),
    ViewAll("View All"),

    // Threat / Results
    BreadcrumbHome("Home"),
    BreadcrumbScans("Scans"),
    OfflineProtection("Offline Protection"),
    VerdictHighRisk("HIGH RISK DETECTED"),
    VerdictDangerous("Dangerous"),
    VerdictDangerousDesc("The scanned QR code contains malicious indicators associated with phishing and credential harvesting. Do not proceed to the target URL."),
    ThreatConfidence("Threat Confidence"),
    FlagPhishing("Phishing Attempt"),
    FlagObfuscated("Obfuscated Script"),
    FlagHomograph("Homograph Attack"),
    ClearHistory("Clear All"),
    SectionAttackBreakdown("Attack Breakdown"),
    AttackHomographTitle("Homograph / IDN Attack"),
    AttackHomographDesc("Cyrillic characters mimicking Latin alphabet detected."),
    AttackVisualLabel("Visual Appearance"),
    AttackPunycodeLabel("Actual Punycode"),
    AttackHomographExplain("The domain uses the Cyrillic 'а' (U+0430) instead of Latin 'a' (U+0061). This technique is commonly used to trick users into believing they are visiting a legitimate service."),
    AttackRedirectTitle("Suspicious Redirect Chain"),
    AttackRedirectDesc("3 hops detected involving known URL shorteners."),
    RedirectStart("QR Code Scan"),
    RedirectHop("Intermediate Hop"),
    RedirectFinal("Final Destination"),
    AttackJsTitle("Obfuscated JavaScript"),
    AttackJsDesc("High entropy string detected in URL parameters."),
    SectionActions("Recommended Actions"),
    ActionBlock("Block & Report"),
    ActionQuarantine("Quarantine in Sandbox"),
    ReasonDomainAge("Domain age is less than 24 hours."),
    ReasonSignatures("Matched 3 signatures in local phishing DB."),
    ReasonAsn("Target IP is located in a high-risk ASN."),
    SectionMeta("Scan Meta"),
    MetaTime("Scan Time"),
    MetaSource("Source"),
    MetaEngine("Engine"),
    MetaOfflineGuaranteeLabel("Offline Guarantee:"),
    MetaOfflineGuaranteeDesc("This image was analysed locally on your device. No data was sent to the cloud for this verdict.")
}

object WebStrings {
    private fun normalizeKey(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }

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
        val normalized = normalizeKey(text)
         return when (language) {
            WebLanguage.German -> GermanCommonStrings[normalized] ?: GermanCommonStrings[text] ?: text
            WebLanguage.Spanish -> SpanishCommonStrings[normalized] ?: SpanishCommonStrings[text] ?: text
            WebLanguage.French -> FrenchCommonStrings[normalized] ?: FrenchCommonStrings[text] ?: text
            WebLanguage.ChineseSimplified -> ChineseCommonStrings[normalized] ?: ChineseCommonStrings[text] ?: text
            WebLanguage.Japanese -> JapaneseCommonStrings[normalized] ?: JapaneseCommonStrings[text] ?: text
            WebLanguage.Hindi -> HindiCommonStrings[normalized] ?: HindiCommonStrings[text] ?: text
            WebLanguage.English -> text
        }
    }
}
