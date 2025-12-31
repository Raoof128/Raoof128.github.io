package com.raouf.mehrguard.web.i18n

import kotlinx.browser.window

enum class WebLanguage(val code: String) {
    English("en"),
    German("de"),
    Spanish("es"),
    French("fr"),
    ChineseSimplified("zh"),
    Japanese("ja"),
    Hindi("hi"),
    Arabic("ar"),
    Indonesian("id"),
    Italian("it"),
    Korean("ko"),
    Portuguese("pt"),
    Russian("ru"),
    Thai("th"),
    Turkish("tr"),
    Vietnamese("vi");

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
                "ar" -> Arabic
                "id", "in" -> Indonesian
                "it" -> Italian
                "ko" -> Korean
                "pt" -> Portuguese
                "ru" -> Russian
                "th" -> Thai
                "tr" -> Turkish
                "vi" -> Vietnamese
                else -> English
            }
        }

        fun current(): WebLanguage {
            val stored = try {
                window.localStorage.getItem("mehrguard_language")
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
    AppTagline("Kotlin Multiplatform QRishing Detector"),
    
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
    QuickActions("Quick Actions"),

    // Hero Section
    HeroTagline("Secure. Offline."),
    HeroTagline2("Explainable Defence."),
    HeroDescription("QR-SHIELD analyses potential threats directly on your hardware. Experience zero-latency protection."),
    
    // Dashboard Actions
    StartScan("Start New Scan"),
    ImportImage("Import Image"),
    ScanQrCode("Scan QR Code"),
    
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
    SystemOptimal("System Optimal"),
    EngineStatus("Engine v2.4 • Updated 2h ago"),
    ThreatsBlocked("Threats Blocked"),
    AllSystemsOperational("All systems operational"),

    // Dashboard Sections
    SectionOverview("Overview"),
    SectionSecurity("Security"),
    SectionTraining("Training"),
    SectionReports("Reports"),
    OfflineReady("Offline Ready"),
    OfflineDescription("Local database v2.4.1 active. No data leaves this device."),
    
    // Trust Centre
    TrustCentreTitle("Trust Centre"),
    OfflineGuarantee("Strict Offline Guarantee"),
    OfflineGuaranteeDesc("QR-SHIELD analysis runs entirely on your device's Neural Engine. No URL data ever leaves your phone."),
    ThreatSensitivity("Threat Sensitivity"),
    ResetConfirm("This will reset all Trust Centre settings to their defaults."),
    
    // Settings Quick Actions
    ThreatMonitor("Threat Monitor"),
    ThreatMonitorDesc("View live threats and run security audit"),
    TrustCentreDesc("Privacy settings and threat sensitivity"),
    ExportReport("Export Report"),
    ExportReportDesc("Generate PDF or JSON security report"),

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
    ActionBlockDesc("Prevent access and notify admin"),
    ActionQuarantine("Quarantine in Sandbox"),
    ActionQuarantineDesc("Open safely for analysis"),
    Expected("Expected: "),
    Detected("Detected: "),
    ExplainableSecurity("Explainable Security"),
    UrlBreakdown("URL BREAKDOWN"),
    FullUrl("FULL URL"),
    OpenInBrowser("Open in Browser (Risky)"),
    OpenWarning("Opening this URL in your browser may expose you to security risks."),
    RestrictedMode("Restricted Mode"),
    RestrictedDesc("This URL has been flagged as potentially dangerous. Review the analysis below before proceeding."),
    DangerousWarning("This URL has been flagged as potentially dangerous. Opening it may expose you to phishing, malware, or other security threats. Are you sure you want to proceed?"),
    CopyUrl("Copy URL"),
    Share("Share"),
    Dismiss("Dismiss"),
    ReasonDomainAge("Domain age is less than 24 hours."),
    ReasonSignatures("Matched 3 signatures in local phishing DB."),
    ReasonAsn("Target IP is located in a high-risk ASN."),
    SectionMeta("Scan Meta"),
    MetaTime("Scan Time"),
    MetaSource("Source"),
    MetaEngine("Engine"),
    MetaOfflineGuaranteeLabel("Offline Guarantee:"),
    MetaOfflineGuaranteeDesc("This image was analysed locally on your device. No data was sent to the cloud for this verdict."),
    
    // Results Page
    ScanComplete("Scan Complete"),
    Loading("Loading..."),
    AnalyzedOffline("Analyzed offline in <5ms"),
    NoDataLeaves("No data leaves device"),
    ActiveProtection("Active Protection"),
    ConfidenceScore("Confidence Score"),
    Analyzing("ANALYZING..."),
    ProcessingResults("Processing scan results..."),
    RiskAssessment("Risk Assessment"),
    AnalysisTime("Analysis Time"),
    Heuristics("Heuristics"),
    TopAnalysisFactors("Top Analysis Factors"),
    Pass("PASS"),
    Info("INFO"),
    Clean("CLEAN"),
    ValidSslCertificate("Valid SSL Certificate"),
    SslCertificateDesc("Certificate issued by DigiCert Inc. No anomalies in chain of trust."),
    EstablishedDomain("Established Domain"),
    EstablishedDomainDesc("Domain registered > 5 years ago. Low probability of churn-and-burn."),
    BlacklistStatus("Blacklist Status"),
    BlacklistStatusDesc("Not found in 52 local offline threat databases."),
    BackToDashboard("Back to Dashboard"),
    ShareReport("Share Report"),
    CopyLink("Copy Link"),
    LinkCopied("Link copied to clipboard!"),
    
    // Game Page
    BeatTheBot("Beat the Bot"),
    BeatTheBotDesc("Challenge our offline AI engine. Can you spot the phish faster?"),
    SessionId("Session ID"),
    EndSession("End Session"),
    Round("Round"),
    BrowserPreview("Browser Preview"),
    MakeDecision("Make your decision"),
    Phishing("Phishing"),
    Legitimate("Legitimate"),
    LiveHint("Live Hint"),
    LiveScoreboard("Live Scoreboard"),
    VsMode("VS MODE"),
    You("You"),
    Points("pts"),
    Streak("Streak"),
    Accuracy("Accuracy"),
    QrShieldBot("QR-Shield Bot"),
    Confidence("Confidence"),
    RoundAnalysis("Round Analysis"),
    Analysis("Analysis"),
    CorrectDecision("Correct Decision!"),
    IdentifiedSafe("You identified the safe URL correctly."),
    AiNeuralAnalysis("AI Neural Analysis"),
    WhyBotFlagged("Why the bot flagged it"),
    EducationalNote("Educational Note"),
    Correct("Correct!"),
    SpottedPhishing("You spotted the phishing URL correctly."),
    Response("Response"),
    NextRound("Next Round"),
    GameOver("Game Over!"),
    FinalResults("Final Results"),
    YourScore("Your Score"),
    BotScore("Bot Score"),
    BestStreak("Best Streak"),
    PlayAgain("Play Again"),
    ReturnToDashboard("Return to Dashboard"),
    
    // Export Page  
    ExportSecurityReport("Export Security Report"),
    GenerateReport("Generate comprehensive security reports from your scan history."),
    ReportFormat("Report Format"),
    HumanReadable("Human-Readable Report"),
    HumanReadableDesc("PDF with visual breakdown and executive summary"),
    MachineReadable("Machine-Readable Data"),
    MachineReadableDesc("JSON format for integration with SIEM tools"),
    DateRange("Date Range"),
    Last7Days("Last 7 Days"),
    Last30Days("Last 30 Days"),
    Last90Days("Last 90 Days"),
    AllTime("All Time"),
    CustomRange("Custom Range"),
    ReportSections("Report Sections"),
    ExecutiveSummary("Executive Summary"),
    ThreatBreakdown("Threat Breakdown"),
    ScanTimeline("Scan Timeline"),
    SafeUrlList("Safe URL List"),
    GenerateExport("Export Report"),
    
    // Toast / Notifications
    Success("Success!"),
    Error("Error"),
    Warning("Warning"),
    CopiedToClipboard("Copied to clipboard"),
    
    // General Actions
    Cancel("Cancel"),
    Confirm("Confirm"),
    Save("Save"),
    Delete("Delete"),
    Edit("Edit"),
    Close("Close"),
    Back("Back"),
    Next("Next"),
    Previous("Previous"),
    Submit("Submit"),
    Reset("Reset"),
    Clear("Clear"),
    Search("Search"),
    Filter("Filter"),
    Sort("Sort"),
    
    // Accessibility
    ToggleMenu("Toggle menu"),
    ToggleTheme("Toggle theme"),
    Notifications("Notifications"),
    Help("Help"),
    Profile("Profile"),
    ToggleLightDarkMode("Toggle light/dark mode"),
    
    // Placeholders
    UrlInputPlaceholder("Paste URL to analyze (e.g., https://example.com)"),
    UrlExamplePlaceholder("https://example.com"),
    
    // Export Page Additional
    Scan("Scan"),
    Result("Result"),
    ReportGeneration("Report Generation"),
    ReportGenerationDesc("Configure formats and download your threat analysis."),
    OfflineSecurity("Offline Security"),
    OfflineSecurityDesc("Your data never leaves this device. All processing is local. No tracking, no uploads."),
    ChooseFormat("Choose Format"),
    PdfFormatDesc("PDF format. Best for sharing with stakeholders."),
    JsonFormatDesc("JSON format. Best for SIEM integration."),
    Copy("Copy"),
    LivePreview("Live Preview"),
    ReadOnly("Read Only"),
    ThreatAnalysisReport("Threat Analysis Report"),
    Verdict("Verdict"),
    RiskScore("Risk Score"),
    TargetUrl("Target URL"),
    AnalysisSummaryLabel("Analysis Summary"),
    TechnicalIndicators("Technical Indicators"),
    ExportInfoNote("Exports include all identified threat vectors, headers, and the raw payload analysis."),
    
    // Trust Centre Page
    AllowList("Allow List"),
    BlockList("Block List"),
    AddDomain("Add Domain"),
    RemoveDomain("Remove Domain"),  
    TrustedDomains("Trusted Domains"),
    BlockedDomains("Blocked Domains"),
    DomainPlaceholder("Enter domain (e.g., example.com)"),
    
    // Game Page Additional
    Safe("Safe"),
    TimeAgo("ago"),
    MinutesAgo("mins ago"),
    HoursAgo("hrs ago"),
    
    // Misc
    Critical("CRITICAL"),
    Warn("WARN"),
    
    // Trust Centre - Sensitivity
    PhishingDetectionSensitivity("Phishing Detection Sensitivity"),
    SensitivityDescription("Adjust heuristic thresholds. Higher sensitivity may increase false positives."),
    SensitivityLow("Low"),
    SensitivityBalanced("Balanced"),
    SensitivityParanoia("Paranoia"),
    BalancedModeRecommended("Balanced Mode (Recommended)"),
    BalancedModeDesc("Scans for known malicious patterns and heuristic mismatches. Blocks homoglyph attacks and redirect chains. Low false positive rate expected."),
    SecureEnvironment("Secure Environment"),
    LearnLocalArchitecture("Learn about our local architecture"),
    NoCustomDomainsBlocked("No custom domains blocked."),
    AddManually("Add manually"),
    AddedDaysAgo("Added 2 days ago"),
    AddedWeekAgo("Added 1 week ago"),
    
    // Trust Centre - Privacy Controls
    PrivacyControls("Privacy Controls"),
    StrictOfflineMode("Strict Offline Mode"),
    DisableExternalPreviews("Disable all external link previews."),
    AnonymousTelemetry("Anonymous Telemetry"),
    ShareDetectionStats("Share detection stats to improve ML."),
    AutoCopySafeLinks("Auto-Copy Safe Links"),
    CopyTrustedUrls("Copy trusted URLs to clipboard."),
    
    // Trust Centre - About
    AboutQrShield("About QR-SHIELD"),
    OpenSourceLicenses("Open Source Licenses"),
    PrivacyPolicy("Privacy Policy"),
    Acknowledgements("Acknowledgements"),
    ResetAllSettings("Reset all settings to default"),
    AddToAllowlist("Add to Allowlist"),
    AddToBlocklist("Add to Blocklist"),
    
    // Onboarding
    AnalysedOfflineTitle("Analysed offline."),
    YourDataStaysOnDevice("Your data stays on-device."),
    OnboardingHeroDesc("QR-SHIELD processes every scan using secure on-device analysis. We prioritize explainable security with zero cloud telemetry for image analysis."),
    OnDeviceAnalysis("On-Device Analysis"),
    OnDeviceAnalysisDesc("All threat detection runs locally on your device. No data ever leaves your system."),
    NoCloudLogs("No Cloud Logs"),
    NoCloudLogsDesc("We strictly disable outgoing telemetry for scans. Scan results and image hashes remain local."),
    OnDeviceDB("On-Device DB"),
    OnDeviceDBDesc("The entire threat signature database is downloaded to your device for millisecond lookups."),
    DataLifecycleVerification("Data Lifecycle Verification"),
    SecurityAuditPass("Security Audit: PASS"),
    
    // Footer
    Copyright("© 2025-2026 QR-SHIELD Security Inc. All rights reserved."),
    Support("Support"),
    Terms("Terms"),
    SystemsOperational("Systems Operational"),
    SystemSecure("System Secure"),
    
    // Version / Build Info
    VersionStatus("v2.4.1 • System Secure"),
    CoreVersion("Core v2.4.1 • Build 2025.12.29"),
    VerifiedBy("Verified by QR-SHIELD Enterprise"),
    
    // User Profile (Sample)
    SampleUserName("John Smith"),
    SampleUserRole("Security Analyst"),
    
    // Export Preview
    DetailLabel("Detail Label"),
    DetailValue("Detail Value"),
    Suspicious("Suspicious"),
    
    // Game Modal Strings
    IncorrectDecision("Incorrect Decision"),
    WrongAnswer("Oops! That wasn't correct."),
    ResponseTime("Response Time"),
    IncomingMessage("Incoming Message"),
    CertificateIssuer("Certificate Issuer matches domain owner"),
    DomainAgeTrust("Domain age > 5 years (High trust)"),
    WhitelistedRank("Top 1k Alexa Rank whitelisted offline"),
    

    // Game Tips/Hints
    GameEducationalNote("Educational Note"),
    GameHintTitle("Game Hint"),
    
    // Trust Modal
    AddToAllowlistTitle("Add to Allowlist"),
    AddToBlocklistTitle("Add to Blocklist"),
    EnterDomainPrompt("Enter a domain to add to your list"),
    
    // Additional Game Strings
    LatencyMs("Latency"),
    StreakFire("Streak"),
    AccuracyPercent("Accuracy"),
    ConfidencePercent("Confidence"),
    
    // Language Names
    English("English"),
    German("German"),
    Spanish("Spanish"),
    French("French"),
    ChineseSimplified("Chinese (Simplified)"),
    Japanese("Japanese"),
    Hindi("Hindi"),
    Arabic("Arabic"),
    Indonesian("Indonesian"),
    Italian("Italian"),
    Korean("Korean"),
    Portuguese("Portuguese"),
    Russian("Russian"),
    Thai("Thai"),
    Turkish("Turkish"),
    Vietnamese("Vietnamese"),
    
    // Component Voting System
    ComponentVoting("Component Voting"),
    Heuristic("Heuristic"),
    MLModel("ML Model"),
    Brand("Brand"),
    TLD("TLD"),
    VotingExplanation("Majority vote determines verdict: 3+ SAFE = green, 2+ MAL = red"),
    
    // Results Page Placeholders
    WaitingForScan("Waiting for scan..."),
    WaitingForAnalysis("Waiting for analysis..."),
    ScanURLToSeeResults("Scan a URL to see detailed results.")
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
            WebLanguage.Arabic -> ArabicStrings[key] ?: key.defaultText
            WebLanguage.Indonesian -> IndonesianStrings[key] ?: key.defaultText
            WebLanguage.Italian -> ItalianStrings[key] ?: key.defaultText
            WebLanguage.Korean -> KoreanStrings[key] ?: key.defaultText
            WebLanguage.Portuguese -> PortugueseStrings[key] ?: key.defaultText
            WebLanguage.Russian -> RussianStrings[key] ?: key.defaultText
            WebLanguage.Thai -> ThaiStrings[key] ?: key.defaultText
            WebLanguage.Turkish -> TurkishStrings[key] ?: key.defaultText
            WebLanguage.Vietnamese -> VietnameseStrings[key] ?: key.defaultText
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
            WebLanguage.Arabic -> ArabicCommonStrings[normalized] ?: ArabicCommonStrings[text] ?: text
            WebLanguage.Indonesian -> IndonesianCommonStrings[normalized] ?: IndonesianCommonStrings[text] ?: text
            WebLanguage.Italian -> ItalianCommonStrings[normalized] ?: ItalianCommonStrings[text] ?: text
            WebLanguage.Korean -> KoreanCommonStrings[normalized] ?: KoreanCommonStrings[text] ?: text
            WebLanguage.Portuguese -> PortugueseCommonStrings[normalized] ?: PortugueseCommonStrings[text] ?: text
            WebLanguage.Russian -> RussianCommonStrings[normalized] ?: RussianCommonStrings[text] ?: text
            WebLanguage.Thai -> ThaiCommonStrings[normalized] ?: ThaiCommonStrings[text] ?: text
            WebLanguage.Turkish -> TurkishCommonStrings[normalized] ?: TurkishCommonStrings[text] ?: text
            WebLanguage.Vietnamese -> VietnameseCommonStrings[normalized] ?: VietnameseCommonStrings[text] ?: text
            WebLanguage.English -> text
        }
    }
}
