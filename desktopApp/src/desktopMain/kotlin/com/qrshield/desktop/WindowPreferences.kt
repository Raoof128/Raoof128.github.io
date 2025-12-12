/*
 * Copyright 2024 QR-SHIELD Contributors
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

package com.qrshield.desktop

import java.io.File
import java.util.Properties

/**
 * Window preferences persistence for Desktop.
 * 
 * Handles saving and loading window state including:
 * - Window size (width/height)
 * - Window position (x/y)
 * - Theme preference (dark/light)
 * 
 * Preferences are stored in a platform-appropriate location:
 * - Windows: %APPDATA%/QRShield
 * - macOS: ~/Library/Application Support/QRShield
 * - Linux: ~/.config/qrshield
 * 
 * @author QR-SHIELD Team
 * @since 1.0.0
 */
object WindowPreferences {
    private const val PREFS_FILE = "qrshield_window.properties"
    private const val DEFAULT_WIDTH = 480
    private const val DEFAULT_HEIGHT = 860
    
    /**
     * Window preferences data class.
     */
    data class Prefs(
        val width: Int,
        val height: Int,
        val x: Int,
        val y: Int,
        val darkMode: Boolean
    )
    
    /**
     * Load saved preferences or return defaults.
     */
    fun load(): Prefs {
        return try {
            val file = getPrefsFile()
            if (file.exists()) {
                val props = Properties()
                file.inputStream().use { props.load(it) }
                Prefs(
                    width = props.getProperty("width", DEFAULT_WIDTH.toString()).toInt(),
                    height = props.getProperty("height", DEFAULT_HEIGHT.toString()).toInt(),
                    x = props.getProperty("x", "-1").toInt(),
                    y = props.getProperty("y", "-1").toInt(),
                    darkMode = props.getProperty("darkMode", "true").toBoolean()
                )
            } else {
                Prefs(DEFAULT_WIDTH, DEFAULT_HEIGHT, -1, -1, true)
            }
        } catch (e: Exception) {
            Prefs(DEFAULT_WIDTH, DEFAULT_HEIGHT, -1, -1, true)
        }
    }
    
    /**
     * Save current window preferences.
     */
    fun save(width: Int, height: Int, x: Int, y: Int, darkMode: Boolean = true) {
        try {
            val file = getPrefsFile()
            file.parentFile?.mkdirs()
            
            val props = Properties()
            props.setProperty("width", width.toString())
            props.setProperty("height", height.toString())
            props.setProperty("x", x.toString())
            props.setProperty("y", y.toString())
            props.setProperty("darkMode", darkMode.toString())
            
            file.outputStream().use { props.store(it, "QR-SHIELD Window Preferences") }
        } catch (e: Exception) {
            // Silently fail on save error
        }
    }
    
    /**
     * Get the preferences file path based on the operating system.
     */
    private fun getPrefsFile(): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")
        
        val appDataDir = when {
            os.contains("windows") -> "${System.getenv("APPDATA")}/QRShield"
            os.contains("mac") -> "$userHome/Library/Application Support/QRShield"
            else -> "$userHome/.config/qrshield"
        }
        
        return File(appDataDir, PREFS_FILE)
    }
}
