/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.desktop

import java.io.File
import java.util.Locale
import java.util.Properties

/**
 * Manages persistence of application settings and preferences.
 *
 * **Security Design:**
 * - Settings are stored in platform-specific application data directories
 *   (macOS: ~/Library/Application Support/QRShield, Windows: %APPDATA%/QRShield, Linux: ~/.config/qrshield)
 * - No sensitive data (passwords, tokens) is stored in settings
 * - File I/O errors are silently handled with fallback to defaults (non-critical persistence)
 * - Telemetry is disabled by default for privacy
 * - Offline mode is enabled by default for security
 */
object SettingsManager {
    private const val SETTINGS_FILE = "qrshield_settings.properties"
    
    /**
     * Application settings with secure defaults.
     *
     * @property trustedDomains User-defined domains to skip heuristic checks (default: major trusted sites)
     * @property blockedDomains User-defined domains to always flag as dangerous
     * @property offlineOnlyEnabled When true, prevents opening URLs (default: true for security)
     * @property telemetryEnabled Anonymous usage telemetry (default: false for privacy)
     */
    data class Settings(
        val trustedDomains: List<String> = listOf("google.com", "github.com", "microsoft.com"),
        val blockedDomains: List<String> = listOf("bit.ly/*", "suspicious-domain.xyz", "free-crypto-giveaway.net"),
        val offlineOnlyEnabled: Boolean = true,
        val blockUnknownEnabled: Boolean = false,
        val autoScanHistoryEnabled: Boolean = true,
        val autoCopySafeLinksEnabled: Boolean = false,
        val heuristicSensitivity: String = "Balanced",
        val telemetryEnabled: Boolean = false,
        val biometricLockEnabled: Boolean = false,
        val languageCode: String = defaultLanguageCode(),
        // User Profile fields (parity with Web app)
        val userName: String = "QR-SHIELD User",
        val userEmail: String = "user@example.com",
        val userInitials: String = "QU",
        val userRole: String = "Security Analyst",
        val userPlan: String = "Enterprise Plan",
        // Game Statistics fields (parity with Web app training.js)
        val gameHighScore: Int = 0,
        val gameBestStreak: Int = 0,
        val gameTotalGamesPlayed: Int = 0,
        val gameTotalCorrect: Int = 0,
        val gameTotalAttempts: Int = 0
    )

    /**
     * Loads settings from disk.
     *
     * @return Persisted settings if available, otherwise secure defaults.
     *         Errors are silently handled - settings persistence is non-critical.
     */
    fun loadSettings(): Settings {
        return try {
            val file = getSettingsFile()
            if (file.exists()) {
                val props = Properties()
                file.inputStream().use { props.load(it) }

                val trustedDomainsStr = props.getProperty("trustedDomains", "google.com,github.com,microsoft.com")
                val trustedDomains = trustedDomainsStr.split(",").filter { it.isNotBlank() }
                val blockedDomainsStr = props.getProperty("blockedDomains", "bit.ly/*,suspicious-domain.xyz,free-crypto-giveaway.net")
                val blockedDomains = blockedDomainsStr.split(",").filter { it.isNotBlank() }

                Settings(
                    trustedDomains = trustedDomains,
                    blockedDomains = blockedDomains,
                    offlineOnlyEnabled = props.getProperty("offlineOnlyEnabled", "true").toBoolean(),
                    blockUnknownEnabled = props.getProperty("blockUnknownEnabled", "false").toBoolean(),
                    autoScanHistoryEnabled = props.getProperty("autoScanHistoryEnabled", "true").toBoolean(),
                    autoCopySafeLinksEnabled = props.getProperty("autoCopySafeLinksEnabled", "false").toBoolean(),
                    heuristicSensitivity = props.getProperty("heuristicSensitivity", "Balanced"),
                    telemetryEnabled = props.getProperty("telemetryEnabled", "false").toBoolean(),
                    biometricLockEnabled = props.getProperty("biometricLockEnabled", "false").toBoolean(),
                    languageCode = props.getProperty("languageCode", defaultLanguageCode()),
                    userName = props.getProperty("userName", "QR-SHIELD User"),
                    userEmail = props.getProperty("userEmail", "user@example.com"),
                    userInitials = props.getProperty("userInitials", "QU"),
                    userRole = props.getProperty("userRole", "Security Analyst"),
                    userPlan = props.getProperty("userPlan", "Enterprise Plan"),
                    // Game Statistics (parity with Web app training.js)
                    gameHighScore = props.getProperty("gameHighScore", "0").toIntOrNull() ?: 0,
                    gameBestStreak = props.getProperty("gameBestStreak", "0").toIntOrNull() ?: 0,
                    gameTotalGamesPlayed = props.getProperty("gameTotalGamesPlayed", "0").toIntOrNull() ?: 0,
                    gameTotalCorrect = props.getProperty("gameTotalCorrect", "0").toIntOrNull() ?: 0,
                    gameTotalAttempts = props.getProperty("gameTotalAttempts", "0").toIntOrNull() ?: 0
                )
            } else {
                Settings()
            }
        } catch (_: Exception) {
            // Settings persistence is non-critical; return defaults on any error
            Settings()
        }
    }

    /**
     * Persists settings to disk.
     *
     * Errors are silently handled - the application continues normally even if
     * settings cannot be saved. Users will lose customizations on restart.
     */
    fun saveSettings(settings: Settings) {
        try {
            val file = getSettingsFile()
            file.parentFile?.mkdirs()

            val props = Properties()
            props.setProperty("trustedDomains", settings.trustedDomains.joinToString(","))
            props.setProperty("blockedDomains", settings.blockedDomains.joinToString(","))
            props.setProperty("offlineOnlyEnabled", settings.offlineOnlyEnabled.toString())
            props.setProperty("blockUnknownEnabled", settings.blockUnknownEnabled.toString())
            props.setProperty("autoScanHistoryEnabled", settings.autoScanHistoryEnabled.toString())
            props.setProperty("autoCopySafeLinksEnabled", settings.autoCopySafeLinksEnabled.toString())
            props.setProperty("heuristicSensitivity", settings.heuristicSensitivity)
            props.setProperty("telemetryEnabled", settings.telemetryEnabled.toString())
            props.setProperty("biometricLockEnabled", settings.biometricLockEnabled.toString())
            props.setProperty("languageCode", settings.languageCode)
            props.setProperty("userName", settings.userName)
            props.setProperty("userEmail", settings.userEmail)
            props.setProperty("userInitials", settings.userInitials)
            props.setProperty("userRole", settings.userRole)
            props.setProperty("userPlan", settings.userPlan)
            // Game Statistics (parity with Web app training.js)
            props.setProperty("gameHighScore", settings.gameHighScore.toString())
            props.setProperty("gameBestStreak", settings.gameBestStreak.toString())
            props.setProperty("gameTotalGamesPlayed", settings.gameTotalGamesPlayed.toString())
            props.setProperty("gameTotalCorrect", settings.gameTotalCorrect.toString())
            props.setProperty("gameTotalAttempts", settings.gameTotalAttempts.toString())

            file.outputStream().use { props.store(it, "QR-SHIELD Application Settings") }
        } catch (_: Exception) {
            // Settings persistence is non-critical; app continues normally
        }
    }

    private fun defaultLanguageCode(): String {
        return when (Locale.getDefault().language.lowercase()) {
            "de" -> "de"
            "es" -> "es"
            "fr" -> "fr"
            "zh" -> "zh"
            "ja" -> "ja"
            "hi" -> "hi"
            else -> "en"
        }
    }

    private fun getSettingsFile(): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        val appDataDir = when {
            os.contains("windows") -> "${System.getenv("APPDATA")}/QRShield"
            os.contains("mac") -> "$userHome/Library/Application Support/QRShield"
            else -> "$userHome/.config/qrshield"
        }

        return File(appDataDir, SETTINGS_FILE)
    }
}
