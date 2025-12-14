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

package com.qrshield.desktop

import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.model.Verdict
import java.io.File
import java.util.Properties

/**
 * Manages persistence of scan history.
 * matches Web localStorage and Android Room database behavior.
 */
object HistoryManager {
    private const val HISTORY_FILE = "qrshield_history.properties"
    private const val MAX_HISTORY = 50

    /**
     * Load history from disk.
     */
    fun loadHistory(): List<AnalysisResult> {
        val list = mutableListOf<AnalysisResult>()
        try {
            val file = getHistoryFile()
            if (file.exists()) {
                val props = Properties()
                file.inputStream().use { props.load(it) }

                val size = props.getProperty("size", "0").toIntOrNull() ?: 0
                
                for (i in 0 until size) {
                    val url = props.getProperty("item.$i.url")
                    val score = props.getProperty("item.$i.score")?.toIntOrNull()
                    val verdictStr = props.getProperty("item.$i.verdict")
                    val time = props.getProperty("item.$i.time")?.toLongOrNull()
                    // Reconstruct flags from comma-separated string
                    val flags = props.getProperty("item.$i.flags", "")
                        .split(",")
                        .filter { it.isNotBlank() }

                    if (url != null && score != null && verdictStr != null && time != null) {
                        // Safe enum parsing
                        val verdict = try {
                            Verdict.valueOf(verdictStr) 
                        } catch (e: Exception) {
                            Verdict.UNKNOWN
                        }

                        list.add(
                            AnalysisResult(
                                url = url,
                                score = score,
                                verdict = verdict,
                                flags = flags,
                                timestamp = time
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Return sorted by NEWEST first 
        return list.sortedByDescending { it.timestamp }
    }

    /**
     * Save history to disk.
     */
    fun saveHistory(history: List<AnalysisResult>) {
        try {
            val file = getHistoryFile()
            file.parentFile?.mkdirs()

            val props = Properties()
            // Keep only the latest MAX_HISTORY items
            val toSave = history.take(MAX_HISTORY)
            
            props.setProperty("size", toSave.size.toString())

            toSave.forEachIndexed { index, item ->
                props.setProperty("item.$index.url", item.url)
                props.setProperty("item.$index.score", item.score.toString())
                props.setProperty("item.$index.verdict", item.verdict.name)
                props.setProperty("item.$index.time", item.timestamp.toString())
                props.setProperty("item.$index.flags", item.flags.joinToString(","))
            }

            file.outputStream().use { props.store(it, "QR-SHIELD Analysis History") }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getHistoryFile(): File {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        val appDataDir = when {
            os.contains("windows") -> "${System.getenv("APPDATA")}/QRShield"
            os.contains("mac") -> "$userHome/Library/Application Support/QRShield"
            else -> "$userHome/.config/qrshield"
        }

        return File(appDataDir, HISTORY_FILE)
    }
}
