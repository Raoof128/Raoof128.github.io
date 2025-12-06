package com.qrshield.android

import android.app.Application
import com.qrshield.android.di.androidModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * QR-SHIELD Application class.
 * Initializes Koin dependency injection.
 */
class QRShieldApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@QRShieldApp)
            modules(androidModule)
        }
    }
}
