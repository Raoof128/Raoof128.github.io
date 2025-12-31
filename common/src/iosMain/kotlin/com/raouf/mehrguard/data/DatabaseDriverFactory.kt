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
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.raouf.mehrguard.db.MehrGuardDatabase

/**
 * iOS SqlDriver Factory for SQLDelight.
 *
 * Creates a Native SQLite driver using NativeSqliteDriver.
 * The database file is stored in the app's Documents directory.
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
actual class DatabaseDriverFactory {

    companion object {
        private const val DATABASE_NAME = "qrshield.db"
    }

    /**
     * Create SqlDriver for iOS.
     *
     * @return SqlDriver instance
     */
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = MehrGuardDatabase.Schema,
            name = DATABASE_NAME
        )
    }
}
