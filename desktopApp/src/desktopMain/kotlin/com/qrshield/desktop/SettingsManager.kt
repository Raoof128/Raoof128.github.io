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
 */
object SettingsManager {
    private const val SETTINGS_FILE = "qrshield_settings.properties"
    
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
        val languageCode: String = defaultLanguageCode()
    )

    fun loadSettings(): Settings {
        try {
            val file = getSettingsFile()
            if (file.exists()) {
                val props = Properties()
                file.inputStream().use { props.load(it) }

                val trustedDomainsStr = props.getProperty("trustedDomains", "google.com,github.com,microsoft.com")
                val trustedDomains = trustedDomainsStr.split(",").filter { it.isNotBlank() }
                val blockedDomainsStr = props.getProperty("blockedDomains", "bit.ly/*,suspicious-domain.xyz,free-crypto-giveaway.net")
                val blockedDomains = blockedDomainsStr.split(",").filter { it.isNotBlank() }

                return Settings(
                    trustedDomains = trustedDomains,
                    blockedDomains = blockedDomains,
                    offlineOnlyEnabled = props.getProperty("offlineOnlyEnabled", "true").toBoolean(),
                    blockUnknownEnabled = props.getProperty("blockUnknownEnabled", "false").toBoolean(),
                    autoScanHistoryEnabled = props.getProperty("autoScanHistoryEnabled", "true").toBoolean(),
                    autoCopySafeLinksEnabled = props.getProperty("autoCopySafeLinksEnabled", "false").toBoolean(),
                    heuristicSensitivity = props.getProperty("heuristicSensitivity", "Balanced"),
                    telemetryEnabled = props.getProperty("telemetryEnabled", "false").toBoolean(),
                    biometricLockEnabled = props.getProperty("biometricLockEnabled", "false").toBoolean(),
                    languageCode = props.getProperty("languageCode", defaultLanguageCode())
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Settings()
    }

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

            file.outputStream().use { props.store(it, "QR-SHIELD Application Settings") }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun defaultLanguageCode(): String {
        val language = Locale.getDefault().language.lowercase()
        return if (language.startsWith("de")) "de" else "en"
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
