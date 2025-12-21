package com.qrshield.desktop

interface DesktopSettingsStore {
    fun load(): SettingsManager.Settings
    fun save(settings: SettingsManager.Settings)
}

class FileDesktopSettingsStore : DesktopSettingsStore {
    override fun load(): SettingsManager.Settings = SettingsManager.loadSettings()

    override fun save(settings: SettingsManager.Settings) {
        SettingsManager.saveSettings(settings)
    }
}
