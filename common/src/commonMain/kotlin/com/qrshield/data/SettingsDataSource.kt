package com.qrshield.data

import com.qrshield.ui.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Interface for persisting app settings.
 */
interface SettingsDataSource {
    /**
     * Observe settings changes.
     */
    val settings: Flow<AppSettings>

    /**
     * Update settings.
     */
    suspend fun saveSettings(settings: AppSettings)
}
