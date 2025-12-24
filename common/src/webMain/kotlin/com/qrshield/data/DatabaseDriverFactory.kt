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

package com.qrshield.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.qrshield.db.QRShieldDatabase
import org.w3c.dom.Worker

/**
 * Web SqlDriver Factory for SQLDelight.
 *
 * Shared implementation for both Kotlin/JS and Kotlin/Wasm targets
 * using the webMain source set introduced in Kotlin 2.2.20.
 *
 * Creates a Web Worker-based SQLite driver using sql.js.
 * Data is stored in browser IndexedDB for persistence.
 *
 * @author QR-SHIELD Security Team
 * @since 1.17.25
 */
actual class DatabaseDriverFactory {

    /**
     * Create SqlDriver for Web (JS + Wasm).
     *
     * Uses WebWorkerDriver for non-blocking database operations.
     *
     * @return SqlDriver instance
     */
    actual fun createDriver(): SqlDriver {
        return WebWorkerDriver(
            Worker(getSqlWorkerUrl())
        ).also { driver ->
            QRShieldDatabase.Schema.create(driver)
        }
    }
}

/**
 * External declaration for worker URL.
 * Implementation provided in JavaScript/Wasm interop layer.
 */
private external fun getSqlWorkerUrl(): String
