package com.qrshield.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.qrshield.db.QRShieldDatabase

/**
 * Android SqlDriver Factory for SQLDelight.
 * 
 * Creates an Android-specific SQLite driver using AndroidSqliteDriver.
 * The database file is stored in the app's private data directory.
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
actual class DatabaseDriverFactory(private val context: Context) {
    
    companion object {
        private const val DATABASE_NAME = "qrshield.db"
    }
    
    /**
     * Create SqlDriver for Android.
     * 
     * @return SqlDriver instance
     */
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = QRShieldDatabase.Schema,
            context = context,
            name = DATABASE_NAME
        )
    }
}
