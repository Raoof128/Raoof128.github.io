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
    Vietnamese("vi"),
    Hebrew("he"),
    Persian("fa");

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
                "he", "iw" -> Hebrew
                "fa", "per" -> Persian
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
    AppName("Mehr Guard"),
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
    HeroDescription("Mehr Guard analyses potential threats directly on your hardware. Experience zero-latency protection."),
    
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
    OfflineGuaranteeDesc("Mehr Guard analysis runs entirely on your device's Neural Engine. No URL data ever leaves your phone."),
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
    VerdictSuspicious("SUSPICIOUS ACTIVITY"),
    VerdictWarning("Warning"),
    VerdictSuspiciousDesc("The scanned QR code shows suspicious patterns that may indicate potential security risks. Proceed with caution."),
    VerdictMinorConcerns("MINOR CONCERNS"),
    VerdictCaution("Caution"),
    VerdictMinorConcernsDesc("Some minor security concerns were identified. The link appears mostly safe but review recommended."),
    VerdictVerifiedSafe("VERIFIED SAFE"),
    VerdictSafeLabel("Safe"),
    VerdictVerifiedSafeDesc("No security threats were detected. The QR code leads to a verified safe destination."),
    VerdictNoActivity("No Activity"),
    VerdictAwaitingScan("Awaiting Scan"),
    VerdictNoActivityDesc("No scan data available. Please scan a QR code or enter a URL to analyze."),
    ScanQrToSeeActivity("Scan a QR code to see your activity here"),
    
    // Data Lifecycle Verification Table
    DataPoint("Data Point"),
    ProcessingEnv("Processing Env"),
    ExternalTransmission("External Transmission"),
    RawImageBuffer("Raw Image Buffer"),
    LocalMemoryRam("Local Memory (RAM)"),
    TransmissionNone("None"),
    DecodedUrlPayload("Decoded URL/Payload"),
    LocalAnalysis("Local Analysis"),
    ThreatVerdictLabel("Threat Verdict"),
    LocalDatabase("Local Database"),
    
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
    MehrGuardBot("Mehr Guard Bot"),
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
    LowSensitivityMode("Low Sensitivity Mode"),
    LowSensitivityModeDesc("Only blocks known malicious URLs from the threat database. Minimal false positives, but may miss novel attacks."),
    BalancedModeRecommended("Balanced Mode (Recommended)"),
    BalancedModeDesc("Scans for known malicious patterns and heuristic mismatches. Blocks homoglyph attacks and redirect chains. Low false positive rate expected."),
    ParanoiaMode("Paranoia Mode"),
    ParanoiaModeDesc("Maximum protection. Blocks any suspicious patterns including newly registered domains, uncommon TLDs, and any URL with potential encoding tricks."),
    SecureEnvironment("Secure Environment"),
    PrivacyGuaranteeDesc("Mehr Guard uses local heuristic analysis engines. No image data, URL strings, or metadata leaves this device during scanning. Your privacy is mathematically guaranteed."),
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
    AboutMehrGuard("About Mehr Guard"),
    OpenSourceLicenses("Open Source Licenses"),
    PrivacyPolicy("Privacy Policy"),
    Acknowledgements("Acknowledgements"),
    ResetAllSettings("Reset all settings to default"),
    AddToAllowlist("Add to Allowlist"),
    AddToBlocklist("Add to Blocklist"),
    
    // Onboarding
    AnalysedOfflineTitle("Analysed offline."),
    YourDataStaysOnDevice("Your data stays on-device."),
    OnboardingHeroDesc("Mehr Guard processes every scan using secure on-device analysis. We prioritize explainable security with zero cloud telemetry for image analysis."),
    OnDeviceAnalysis("On-Device Analysis"),
    OnDeviceAnalysisDesc("All threat detection runs locally on your device. No data ever leaves your system."),
    NoCloudLogs("No Cloud Logs"),
    NoCloudLogsDesc("We strictly disable outgoing telemetry for scans. Scan results and image hashes remain local."),
    OnDeviceDB("On-Device DB"),
    OnDeviceDBDesc("The entire threat signature database is downloaded to your device for millisecond lookups."),
    DataLifecycleVerification("Data Lifecycle Verification"),
    SecurityAuditPass("Security Audit: PASS"),
    
    // Footer
    Copyright("© 2025-2026 Mehr Guard Security Inc. All rights reserved."),
    Support("Support"),
    Terms("Terms"),
    SystemsOperational("Systems Operational"),
    SystemSecure("System Secure"),
    
    // Version / Build Info
    VersionStatus("v2.4.1 • System Secure"),
    CoreVersion("Core v2.4.1 • Build 2025.12.29"),
    VerifiedBy("Verified by Mehr Guard Enterprise"),
    
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
    Hebrew("Hebrew"),
    Persian("Persian"),
    
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
    ScanURLToSeeResults("Scan a URL to see detailed results."),
    
    // Help Modal & Keyboard Shortcuts
    HelpKeyboardShortcuts("Help & Keyboard Shortcuts"),
    KeyboardShortcuts("Keyboard Shortcuts"),
    KeyboardShortcutsDesc("Press these keys when not typing in an input field. Works on Windows, Mac and Linux."),
    StartScanner("Start Scanner"),
    CloseMenuModal("Close Menu / Modal"),
    NavigateToDashboard("Navigate to Dashboard"),
    NavigateToHistory("Scan History"),
    NavigateToTrust("Trust Centre / Allow List"),
    NavigateToGame("Beat the Bot Game"),
    ShowHelp("Show Help"),
    AboutDescription("Enterprise-grade QR code security with 100% offline analysis. Your data never leaves your device. All threat detection is performed locally using our advanced phishing detection engine."),
    VersionOfflineReady("Version 2.4.1 • Offline Ready"),
    GotIt("Got it"),
    
    // Security Settings Page
    SecuritySettings("Security Settings"),
    SettingsDetection("Detection"),
    SensitivityLevel("Sensitivity Level"),
    SensitivityLevelDesc("How aggressive should threat detection be?"),
    SensitivityPermissive("Permissive"),
    SensitivityStrict("Strict"),
    AutoBlockThreats("Auto-Block Threats"),
    AutoBlockThreatsDesc("Automatically block high-risk URLs"),
    RealTimeScanning("Real-Time Scanning"),
    RealTimeScanningDesc("Scan QR codes as they're detected"),
    SettingsNotifications("Notifications"),
    SoundAlerts("Sound Alerts"),
    SoundAlertsDesc("Play sound when threat detected"),
    ThreatAlerts("Threat Alerts"),
    ThreatAlertsDesc("Show visual alerts for threats"),
    SettingsDisplay("Display"),
    ShowConfidenceScore("Show Confidence Score"),
    ShowConfidenceScoreDesc("Display threat probability percentage"),
    CompactView("Compact View"),
    CompactViewDesc("Use condensed layout for results"),
    SettingsLanguage("Language"),
    SettingsLanguageDesc("Choose your preferred language"),
    SettingsSavedAutomatically("Settings are saved automatically and sync across sessions."),
    ResetDefaults("Reset Defaults"),
    SaveAndContinue("Save & Continue"),
    
    // Security Signals & Heuristics (for dynamic attack cards)
    SecuritySignal("Security Signal"),
    HighRisk("High Risk"),
    LowRisk("Low Risk"),
    Unknown("Unknown"),
    UnknownURL("Unknown URL"),
    VisualAppearance("Visual Appearance"),
    ActualDomain("Actual Domain"),
    
    // Result Page Verdict Labels
    ScanCompleteLabel("Scan Complete"),
    SafeToVisit("SAFE TO VISIT"),
    SafeToVisitDesc("Verified by local heuristics v2.4. No phishing patterns, obfuscated scripts, or blacklist matches found."),
    SafetyScore("Safety Score"),
    CautionAdvised("Caution Advised"),
    ProceedWithCaution("PROCEED WITH CAUTION"),
    ProceedWithCautionDesc("Some suspicious indicators detected. Verify the source before entering sensitive information."),
    ThreatDetected("Threat Detected"),
    DoNotVisit("DO NOT VISIT"),
    DoNotVisitDesc("High-confidence phishing detected. This URL exhibits multiple malicious indicators."),
    AnalysisComplete("Analysis Complete"),
    UnableToDetermine("Unable to determine verdict. Please try again."),
    LowRiskLabel("LOW RISK"),
    MediumRiskLabel("MEDIUM RISK"),
    HighRiskLabel("HIGH RISK"),
    NoData("NO DATA"),
    AwaitingScanLabel("Awaiting Scan"),
    NoScanDataResults("No scan data available. Please scan a URL to see analysis results."),
    WaitingForAnalysisLabel("Waiting for analysis..."),
    NoUrlScanned("No URL scanned"),
    ScanToAnalyze("Scan a QR code or enter a URL to analyze."),
    GoToScanner("Go to Scanner"),
    
    // Attack Type Labels
    HomographIdnAttack("Homograph / IDN Attack"),
    InternationalizedDomainDetected("Internationalized domain name detected."),
    DomainUsesInternationalChars("This domain uses international characters that may mimic legitimate domains."),
    RedirectShortenerDetected("Redirect/Shortener Detected"),
    UrlShortenerDetected("URL Shortener Detected"),
    UrlShortenerDesc("URL uses a shortening service that hides the final destination."),
    ScannedURLLabel("Scanned URL"),
    HiddenDestination("Hidden Destination"),
    SuspiciousTld("Suspicious TLD"),
    BrandImpersonation("Brand Impersonation"),
    SuspiciousEncoding("Suspicious Encoding"),
    PhishingIndicators("Phishing Indicators"),
    SuspiciousKeywords("Suspicious Keywords"),
    SuspiciousUrlParams("Suspicious URL Parameters"),
    SuspiciousUrlParamsDesc("URL contains complex or potentially obfuscated parameters."),
    SuspiciousDomain("Suspicious Domain"),
    SuspiciousTldDesc("Domain uses {tld} TLD commonly associated with malicious sites."),
    HeuristicAnalysis("Heuristic Analysis"),
    HeuristicAnalysisDesc("URL flagged based on multiple risk factors detected by the analysis engine."),
    
    // Heuristic Signal Descriptions
    SignalHttpNotHttps("Uses insecure HTTP protocol"),
    SignalIpAddressHost("Host is an IP address instead of domain"),
    SignalUrlShortener("Uses URL shortening service"),
    SignalExcessiveSubdomains("Excessive subdomain depth ({count} levels)"),
    SignalNonStandardPort("Non-standard port: {port}"),
    SignalLongUrl("Unusually long URL ({length} characters)"),
    SignalLongUrlMarketing("Unusually long URL ({length} characters) - contains marketing parameters"),
    SignalHighEntropyHost("High randomness in domain name"),
    SignalSuspiciousPathKeywords("Suspicious keywords in path ({count} found)"),
    SignalCredentialParams("Credential-related parameters in URL"),
    SignalEncodedPayload("Encoded data detected in query parameters"),
    SignalNumericSubdomain("Numeric-only subdomain detected"),
    SignalSuspiciousTld("Suspicious TLD: {tld}"),
    SignalNewlyRegisteredDomain("Newly registered domain detected"),
    SignalHomographAttack("Domain contains homograph/confusable characters"),
    SignalBrandImpersonation("Potential brand impersonation detected"),
    
    // Scanner Page Labels
    Malicious("Malicious"),
    LiveFeedActive("LIVE FEED ACTIVE"),
    JustNow("Just now"),
    
    // Reason Code Titles and Descriptions (for i18n of attack analysis cards)
    // CRITICAL severity
    ReasonJavascriptUrlTitle("JavaScript URL"),
    ReasonJavascriptUrlDesc("JavaScript URL scheme detected - executes code when clicked"),
    ReasonDataUriTitle("Data URI"),
    ReasonDataUriDesc("Data URI scheme detected - may contain embedded malicious code"),
    ReasonAtSymbolInjectionTitle("At Symbol Injection"),
    ReasonAtSymbolInjectionDesc("URL contains @ symbol indicating possible credential theft attempt"),
    // HIGH severity
    ReasonHomographTitle("Homograph Attack"),
    ReasonHomographDesc("Internationalized domain name (IDN) may impersonate another domain"),
    ReasonMixedScriptTitle("Mixed Script"),
    ReasonMixedScriptDesc("Hostname contains characters from multiple scripts (possible spoofing)"),
    ReasonLookalikeCharsTitle("Lookalike Characters"),
    ReasonLookalikeCharsDesc("Domain contains Unicode characters that look like ASCII letters"),
    ReasonZeroWidthCharsTitle("Zero-Width Characters"),
    ReasonZeroWidthCharsDesc("Hidden zero-width Unicode characters detected (obfuscation attempt)"),
    ReasonIpHostTitle("IP Host"),
    ReasonIpHostDesc("URL uses IP address instead of domain name"),
    ReasonCredentialParamTitle("Credential Parameter"),
    ReasonCredentialParamDesc("URL contains credential-related parameters (password, token, etc.)"),
    ReasonBrandImpersonationTitle("Brand Impersonation"),
    ReasonBrandImpersonationDesc("URL appears to impersonate a known brand"),
    ReasonBrandInSubdomainTitle("Brand In Subdomain"),
    ReasonBrandInSubdomainDesc("Brand name appears in subdomain but not the main domain"),
    ReasonRiskyExtensionTitle("Risky Extension"),
    ReasonRiskyExtensionDesc("Path contains potentially dangerous file extension"),
    ReasonDoubleExtensionTitle("Double Extension"),
    ReasonDoubleExtensionDesc("Double file extension detected (common malware tactic)"),
    ReasonEncodedPayloadTitle("Encoded Payload"),
    ReasonEncodedPayloadDesc("Large encoded data detected in URL parameters"),
    // MEDIUM severity
    ReasonHttpNotHttpsTitle("HTTP Not HTTPS"),
    ReasonHttpNotHttpsDesc("URL uses insecure HTTP protocol instead of HTTPS"),
    ReasonSuspiciousTldTitle("Suspicious TLD"),
    ReasonSuspiciousTldDesc("Top-level domain is frequently used for phishing"),
    ReasonHighEntropyHostTitle("High Entropy Host"),
    ReasonHighEntropyHostDesc("Domain name appears randomly generated"),
    ReasonRedirectParamTitle("Redirect Parameter"),
    ReasonRedirectParamDesc("URL contains redirect parameter that may lead to another site"),
    ReasonDeepSubdomainTitle("Deep Subdomain"),
    ReasonDeepSubdomainDesc("Excessive subdomain depth (may hide real domain)"),
    ReasonMultiTldTitle("Multi TLD"),
    ReasonMultiTldDesc("Multiple TLD-like segments in domain name"),
    ReasonNumericSubdomainTitle("Numeric Subdomain"),
    ReasonNumericSubdomainDesc("Numeric-only subdomain detected"),
    ReasonNonStandardPortTitle("Non-Standard Port"),
    ReasonNonStandardPortDesc("URL uses non-standard port number"),
    ReasonSuspiciousPortTitle("Suspicious Port"),
    ReasonSuspiciousPortDesc("URL uses port commonly associated with attacks"),
    ReasonFragmentHidingTitle("Fragment Hiding"),
    ReasonFragmentHidingDesc("URL fragment appears to hide additional content"),
    ReasonExcessiveEncodingTitle("Excessive Encoding"),
    ReasonExcessiveEncodingDesc("Excessive URL encoding detected (obfuscation attempt)"),
    ReasonDomainAgePatternTitle("Domain Age Pattern"),
    ReasonDomainAgePatternDesc("Domain name matches patterns of auto-generated domains"),
    // LOW severity
    ReasonUrlShortenerTitle("URL Shortener"),
    ReasonUrlShortenerDesc("URL uses shortening service (hides real destination)"),
    ReasonSuspiciousPathTitle("Suspicious Path"),
    ReasonSuspiciousPathDesc("Path contains suspicious keywords (login, verify, etc.)"),
    ReasonLongUrlTitle("Long URL"),
    ReasonLongUrlDesc("Unusually long URL"),
    ReasonCredentialKeywordsTitle("Credential Keywords"),
    ReasonCredentialKeywordsDesc("URL contains credential harvesting keywords"),
    // INFO
    ReasonUnparseableTitle("Unparseable URL"),
    ReasonUnparseableDesc("URL could not be fully parsed"),
    ReasonAnalysisCompleteTitle("Analysis Complete"),
    ReasonAnalysisCompleteDesc("Analysis completed successfully"),
    
    // ML Score Labels
    MlPhishingScore("ML Phishing Score"),
    MlCharacterAnalysis("Character analysis"),
    MlFeatureAnalysis("Feature analysis"),
    
    // Unicode Attack Breakdowns (Red Team scenarios)
    IdnPunycodeDomainTitle("IDN / Punycode Domain"),
    IdnPunycodeDomainDesc("This domain uses internationalized characters. Safe display:"),
    MixedScriptAttackTitle("Mixed Script Attack"),
    MixedScriptAttackDesc("Domain contains characters from multiple scripts (e.g., Cyrillic + Latin). Common in homograph attacks."),
    ConfusableCharsTitle("Confusable Characters"),
    ConfusableCharsDesc("Domain contains characters that look similar to common letters (e.g., 'а' vs 'a').")
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
            WebLanguage.Hebrew -> HebrewStrings[key] ?: key.defaultText
            WebLanguage.Persian -> PersianStrings[key] ?: key.defaultText
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
            WebLanguage.Hebrew -> HebrewCommonStrings[normalized] ?: HebrewCommonStrings[text] ?: text
            WebLanguage.Persian -> PersianCommonStrings[normalized] ?: PersianCommonStrings[text] ?: text
            WebLanguage.English -> text
        }
    }
}
