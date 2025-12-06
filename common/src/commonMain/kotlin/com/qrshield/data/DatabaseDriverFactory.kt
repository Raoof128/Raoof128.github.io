package com.qrshield.data

import app.cash.sqldelight.db.SqlDriver

/**
 * Expect declaration for platform-specific SqlDriver creation.
 * 
 * Each platform (Android, iOS, Desktop, Web) provides its own
 * actual implementation of this class.
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
