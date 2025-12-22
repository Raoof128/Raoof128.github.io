package com.qrshield.data

import android.content.Context
import android.content.SharedPreferences
import com.qrshield.ui.AppSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Android implementation of SettingsDataSource using SharedPreferences.
 */
class AndroidSettingsDataSource(context: Context) : SettingsDataSource {

    private val prefs: SharedPreferences = context.getSharedPreferences("qr_shield_settings", Context.MODE_PRIVATE)

    override val settings: Flow<AppSettings> = callbackFlow {
        // Initial value
        trySend(readSettings())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readSettings())
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    override suspend fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putBoolean("auto_scan", settings.isAutoScanEnabled)
            putBoolean("haptic", settings.isHapticEnabled)
            putBoolean("sound", settings.isSoundEnabled)
            putBoolean("save_history", settings.isSaveHistoryEnabled)
            putBoolean("security_alerts", settings.isSecurityAlertsEnabled)
            putBoolean("developer_mode", settings.isDeveloperModeEnabled)
            putBoolean("aggressive_mode", settings.isAggressiveModeEnabled)
            
            // Trust Centre
            putString("heuristic_sensitivity", settings.heuristicSensitivity)
            putBoolean("share_threats", settings.isShareThreatSignaturesEnabled)
            putBoolean("biometric_unlock", settings.isBiometricUnlockEnabled)
            putBoolean("auto_copy", settings.isAutoCopySafeLinksEnabled)
            apply()
        }
    }

    private fun readSettings(): AppSettings {
        return AppSettings(
            isAutoScanEnabled = prefs.getBoolean("auto_scan", true),
            isHapticEnabled = prefs.getBoolean("haptic", true),
            isSoundEnabled = prefs.getBoolean("sound", true),
            isSaveHistoryEnabled = prefs.getBoolean("save_history", true),
            isSecurityAlertsEnabled = prefs.getBoolean("security_alerts", true),
            isDeveloperModeEnabled = prefs.getBoolean("developer_mode", false),
            isAggressiveModeEnabled = prefs.getBoolean("aggressive_mode", false),
            
            heuristicSensitivity = prefs.getString("heuristic_sensitivity", "BALANCED") ?: "BALANCED",
            isShareThreatSignaturesEnabled = prefs.getBoolean("share_threats", false),
            isBiometricUnlockEnabled = prefs.getBoolean("biometric_unlock", true),
            isAutoCopySafeLinksEnabled = prefs.getBoolean("auto_copy", false)
        )
    }
}
