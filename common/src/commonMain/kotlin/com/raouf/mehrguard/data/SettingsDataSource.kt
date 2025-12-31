package com.raouf.mehrguard.data

import com.raouf.mehrguard.ui.AppSettings
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
