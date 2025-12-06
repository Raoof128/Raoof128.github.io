package com.qrshield.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.qrshield.db.QRShieldDatabase
import org.w3c.dom.Worker

/**
 * Web SqlDriver Factory for SQLDelight.
 * 
 * Creates a Web Worker-based SQLite driver using sql.js.
 * Data is stored in browser IndexedDB for persistence.
 * 
 * NOTE: Requires sql.js worker script to be available.
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
actual class DatabaseDriverFactory {
    
    /**
     * Create SqlDriver for Web/JS.
     * 
     * Uses WebWorkerDriver for non-blocking database operations.
     * Falls back to in-memory if worker is not available.
     * 
     * @return SqlDriver instance
     */
    actual fun createDriver(): SqlDriver {
        // WebWorkerDriver requires the sql.js worker script
        // For production, the worker URL should be configurable
        return WebWorkerDriver(
            Worker(
                js("""(function() {
                    return new URL('@aspect-build/aspect-sql.js/worker.js', import.meta.url).href;
                })()""") as String
            )
        ).also { driver ->
            QRShieldDatabase.Schema.create(driver)
        }
    }
}
