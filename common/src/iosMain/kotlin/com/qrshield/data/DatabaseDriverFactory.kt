package com.qrshield.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.qrshield.db.QRShieldDatabase

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
            schema = QRShieldDatabase.Schema,
            name = DATABASE_NAME
        )
    }
}
