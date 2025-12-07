package com.qrshield.android

import android.app.Application
import com.qrshield.android.di.androidModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * QR-SHIELD Android Application
 * 
 * Initializes Koin dependency injection with all required modules.
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class QRShieldApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin for dependency injection
        startKoin {
            // Log Koin events (use Level.ERROR in production)
            androidLogger(Level.DEBUG)
            
            // Provide Android context
            androidContext(this@QRShieldApplication)
            
            // Load all modules
            modules(androidModule)
        }
    }
}
