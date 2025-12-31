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

package com.raouf.mehrguard.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.raouf.mehrguard.db.MehrGuardDatabase
import java.io.File

/**
 * Desktop SqlDriver Factory for SQLDelight.
 *
 * Creates a JVM SQLite driver using JdbcSqliteDriver.
 * The database file is stored in the user's app data directory.
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
actual class DatabaseDriverFactory {

    companion object {
        private const val DATABASE_NAME = "qrshield.db"
    }

    /**
     * Create SqlDriver for Desktop JVM.
     *
     * @return SqlDriver instance
     */
    actual fun createDriver(): SqlDriver {
        val path = getDefaultDatabasePath()

        // Ensure parent directory exists
        val file = File(path)
        file.parentFile?.mkdirs()

        val driver = JdbcSqliteDriver("jdbc:sqlite:$path")

        // Create schema if database is new
        if (!file.exists() || file.length() == 0L) {
            MehrGuardDatabase.Schema.create(driver)
        }

        return driver
    }

    /**
     * Get default database path for the platform.
     */
    private fun getDefaultDatabasePath(): String {
        val os = System.getProperty("os.name").lowercase()
        val userHome = System.getProperty("user.home")

        val appDataDir = when {
            os.contains("windows") -> "${System.getenv("APPDATA")}/MehrGuard"
            os.contains("mac") -> "$userHome/Library/Application Support/MehrGuard"
            else -> "$userHome/.config/qrshield"
        }

        return "$appDataDir/$DATABASE_NAME"
    }
}
