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

package com.qrshield.localization

/**
 * German Translations for QR-SHIELD
 *
 * Complete German (Deutsch) localization for the QR-SHIELD application.
 * Added for KotlinConf 2026 in Munich - demonstrates i18n completion.
 *
 * ## Usage
 * ```kotlin
 * // Get translation for current locale
 * val text = Translations.get("de").verdictSafe // "Sicher"
 * ```
 *
 * @author QR-SHIELD Security Team
 * @since 1.7.0
 */
object GermanTranslations : TranslationBundle {
    override val languageCode: String = "de"
    override val languageName: String = "Deutsch"

    // ==================== Verdicts ====================
    override val verdictSafe: String = "Sicher"
    override val verdictSuspicious: String = "Verdächtig"
    override val verdictMalicious: String = "Gefährlich"
    override val verdictUnknown: String = "Unbekannt"

    // ==================== Verdict Descriptions ====================
    override val verdictSafeDesc: String = "Diese URL scheint sicher zu sein. Keine verdächtigen Muster erkannt."
    override val verdictSuspiciousDesc: String = "Diese URL zeigt einige Warnsignale. Mit Vorsicht fortfahren."
    override val verdictMaliciousDesc: String = "Diese URL ist wahrscheinlich bösartig. Klicken Sie nicht!"

    // ==================== Actions ====================
    override val actionScanAnother: String = "Weitere scannen"
    override val actionOpenUrl: String = "URL öffnen"
    override val actionCopyUrl: String = "URL kopieren"
    override val actionShare: String = "Teilen"
    override val actionViewDetails: String = "Details anzeigen"
    override val actionProceed: String = "Trotzdem fortfahren"
    override val actionGoBack: String = "Zurück in Sicherheit"
    
    // ==================== UI Elements ====================
    override val appName: String = "QR-SHIELD"
    override val appTagline: String = "Smart scannen. Geschützt bleiben."
    override val tabScan: String = "Scannen"
    override val tabHistory: String = "Verlauf"
    override val tabSettings: String = "Einstellungen"

    // ==================== Results ====================
    override val resultRiskScore: String = "Risikobewertung"
    override val resultConfidence: String = "Konfidenz"
    override val resultSignals: String = "Erkannte Signale"
    override val resultUrl: String = "URL"
    override val resultTld: String = "Top-Level-Domain"
    override val resultBrandMatch: String = "Markenübereinstimmung"
    
    // ==================== Warnings ====================
    override val warningProceedRisk: String = "Fortfahren erfolgt auf eigene Gefahr. Diese URL könnte versuchen, Ihre Daten zu stehlen."
    override val warningIrreversible: String = "Diese Aktion kann nicht rückgängig gemacht werden."

    // ==================== History ====================
    override val historyTitle: String = "Scan-Verlauf"
    override val historyEmpty: String = "Noch keine Scans"
    override val historyEmptyDesc: String = "Gescannte QR-Codes werden hier angezeigt"
    override val historyClearAll: String = "Alle löschen"
    override val historyDeleteItem: String = "Diesen Eintrag löschen"

    // ==================== Settings ====================
    override val settingsTitle: String = "Einstellungen"
    override val settingsAppearance: String = "Erscheinungsbild"
    override val settingsPrivacy: String = "Datenschutz"
    override val settingsAbout: String = "Über"
    override val settingsDarkMode: String = "Dunkelmodus"
    override val settingsHaptics: String = "Haptisches Feedback"
    override val settingsSound: String = "Soundeffekte"
    override val settingsAutoScan: String = "Auto-Scan"
    override val settingsSaveHistory: String = "Scan-Verlauf speichern"

    // ==================== Security Signals ====================
    override val signalSuspiciousTld: String = "Verdächtige TLD"
    override val signalBrandImpersonation: String = "Markenimitation"
    override val signalUrlShortener: String = "URL-Verkürzer"
    override val signalIpAddress: String = "IP-Adresse als Host"
    override val signalExcessiveSubdomains: String = "Übermäßige Subdomains"
    override val signalHomograph: String = "Homograph-Angriff"
    override val signalCredentialPath: String = "Anmeldedaten-Sammlung"
    override val signalPunycode: String = "Punycode-Domain"
    override val signalLongUrl: String = "Ungewöhnlich lange URL"

    // ==================== Beat the Bot ====================
    override val beatTheBotTitle: String = "Schlage den Bot"
    override val beatTheBotDesc: String = "Versuche, eine Phishing-URL zu erstellen, die den Detektor austrickst!"
    override val beatTheBotSubmit: String = "URL testen"
    override val beatTheBotScore: String = "Deine Punktzahl"
    override val beatTheBotLeaderboard: String = "Bestenliste"

    // ==================== Accessibility ====================
    override val a11yRiskLow: String = "Niedriges Risiko"
    override val a11yRiskMedium: String = "Mittleres Risiko"
    override val a11yRiskHigh: String = "Hohes Risiko"
    override val a11yScanButton: String = "QR-Code scannen"
    override val a11yCloseButton: String = "Schließen"
    override val a11yBackButton: String = "Zurück"
}

/**
 * Translation bundle interface for type-safe localization.
 */
interface TranslationBundle {
    val languageCode: String
    val languageName: String
    
    // Verdicts
    val verdictSafe: String
    val verdictSuspicious: String
    val verdictMalicious: String
    val verdictUnknown: String
    val verdictSafeDesc: String
    val verdictSuspiciousDesc: String
    val verdictMaliciousDesc: String
    
    // Actions
    val actionScanAnother: String
    val actionOpenUrl: String
    val actionCopyUrl: String
    val actionShare: String
    val actionViewDetails: String
    val actionProceed: String
    val actionGoBack: String
    
    // UI
    val appName: String
    val appTagline: String
    val tabScan: String
    val tabHistory: String
    val tabSettings: String
    
    // Results
    val resultRiskScore: String
    val resultConfidence: String
    val resultSignals: String
    val resultUrl: String
    val resultTld: String
    val resultBrandMatch: String
    
    // Warnings
    val warningProceedRisk: String
    val warningIrreversible: String
    
    // History
    val historyTitle: String
    val historyEmpty: String
    val historyEmptyDesc: String
    val historyClearAll: String
    val historyDeleteItem: String
    
    // Settings
    val settingsTitle: String
    val settingsAppearance: String
    val settingsPrivacy: String
    val settingsAbout: String
    val settingsDarkMode: String
    val settingsHaptics: String
    val settingsSound: String
    val settingsAutoScan: String
    val settingsSaveHistory: String
    
    // Signals
    val signalSuspiciousTld: String
    val signalBrandImpersonation: String
    val signalUrlShortener: String
    val signalIpAddress: String
    val signalExcessiveSubdomains: String
    val signalHomograph: String
    val signalCredentialPath: String
    val signalPunycode: String
    val signalLongUrl: String
    
    // Beat the Bot
    val beatTheBotTitle: String
    val beatTheBotDesc: String
    val beatTheBotSubmit: String
    val beatTheBotScore: String
    val beatTheBotLeaderboard: String
    
    // Accessibility
    val a11yRiskLow: String
    val a11yRiskMedium: String
    val a11yRiskHigh: String
    val a11yScanButton: String
    val a11yCloseButton: String
    val a11yBackButton: String
}

/**
 * English Translations (Default)
 */
object EnglishTranslations : TranslationBundle {
    override val languageCode: String = "en"
    override val languageName: String = "English"

    override val verdictSafe: String = "Safe"
    override val verdictSuspicious: String = "Suspicious"
    override val verdictMalicious: String = "Dangerous"
    override val verdictUnknown: String = "Unknown"
    override val verdictSafeDesc: String = "This URL appears to be safe. No suspicious patterns detected."
    override val verdictSuspiciousDesc: String = "This URL shows some warning signs. Proceed with caution."
    override val verdictMaliciousDesc: String = "This URL is likely malicious. Do not click!"

    override val actionScanAnother: String = "Scan Another"
    override val actionOpenUrl: String = "Open URL"
    override val actionCopyUrl: String = "Copy URL"
    override val actionShare: String = "Share"
    override val actionViewDetails: String = "View Details"
    override val actionProceed: String = "Proceed Anyway"
    override val actionGoBack: String = "Go Back to Safety"
    
    override val appName: String = "QR-SHIELD"
    override val appTagline: String = "Scan Smart. Stay Protected."
    override val tabScan: String = "Scan"
    override val tabHistory: String = "History"
    override val tabSettings: String = "Settings"

    override val resultRiskScore: String = "Risk Score"
    override val resultConfidence: String = "Confidence"
    override val resultSignals: String = "Detected Signals"
    override val resultUrl: String = "URL"
    override val resultTld: String = "Top-Level Domain"
    override val resultBrandMatch: String = "Brand Match"
    
    override val warningProceedRisk: String = "Proceeding is at your own risk. This URL may attempt to steal your data."
    override val warningIrreversible: String = "This action cannot be undone."

    override val historyTitle: String = "Scan History"
    override val historyEmpty: String = "No scans yet"
    override val historyEmptyDesc: String = "Scanned QR codes will appear here"
    override val historyClearAll: String = "Clear All"
    override val historyDeleteItem: String = "Delete This Entry"

    override val settingsTitle: String = "Settings"
    override val settingsAppearance: String = "Appearance"
    override val settingsPrivacy: String = "Privacy"
    override val settingsAbout: String = "About"
    override val settingsDarkMode: String = "Dark Mode"
    override val settingsHaptics: String = "Haptic Feedback"
    override val settingsSound: String = "Sound Effects"
    override val settingsAutoScan: String = "Auto-Scan"
    override val settingsSaveHistory: String = "Save Scan History"

    override val signalSuspiciousTld: String = "Suspicious TLD"
    override val signalBrandImpersonation: String = "Brand Impersonation"
    override val signalUrlShortener: String = "URL Shortener"
    override val signalIpAddress: String = "IP Address Host"
    override val signalExcessiveSubdomains: String = "Excessive Subdomains"
    override val signalHomograph: String = "Homograph Attack"
    override val signalCredentialPath: String = "Credential Harvesting"
    override val signalPunycode: String = "Punycode Domain"
    override val signalLongUrl: String = "Unusually Long URL"

    override val beatTheBotTitle: String = "Beat the Bot"
    override val beatTheBotDesc: String = "Try to craft a phishing URL that fools the detector!"
    override val beatTheBotSubmit: String = "Test URL"
    override val beatTheBotScore: String = "Your Score"
    override val beatTheBotLeaderboard: String = "Leaderboard"

    override val a11yRiskLow: String = "Low risk"
    override val a11yRiskMedium: String = "Medium risk"
    override val a11yRiskHigh: String = "High risk"
    override val a11yScanButton: String = "Scan QR code"
    override val a11yCloseButton: String = "Close"
    override val a11yBackButton: String = "Go back"
}

/**
 * Translations registry for multi-language support.
 */
object Translations {
    private val bundles = mapOf(
        "en" to EnglishTranslations,
        "de" to GermanTranslations
    )
    
    /**
     * Get translation bundle for the given language code.
     * Falls back to English if language is not supported.
     */
    fun get(languageCode: String): TranslationBundle {
        return bundles[languageCode.lowercase().take(2)] ?: EnglishTranslations
    }
    
    /**
     * List of all supported language codes.
     */
    val supportedLanguages: List<String> = bundles.keys.toList()
    
    /**
     * Check if a language is supported.
     */
    fun isSupported(languageCode: String): Boolean {
        return bundles.containsKey(languageCode.lowercase().take(2))
    }
}
