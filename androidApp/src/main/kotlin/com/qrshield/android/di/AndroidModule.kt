package com.qrshield.android.di

import com.qrshield.core.PhishingEngine
import com.qrshield.data.DatabaseDriverFactory
import com.qrshield.data.HistoryRepository
import com.qrshield.data.SqlDelightHistoryRepository
import com.qrshield.db.QRShieldDatabase
import com.qrshield.scanner.QrScanner
import com.qrshield.scanner.QrScannerFactory
import com.qrshield.ui.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin dependency injection module for Android.
 * 
 * Provides all dependencies needed for the Android app including:
 * - Database and repository
 * - Scanner implementation
 * - ViewModel with persistence
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
val androidModule = module {
    
    // Application scope for coroutines
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    
    // Core engines
    single { PhishingEngine() }
    
    // Database driver factory (Android-specific)
    single { DatabaseDriverFactory(androidContext()) }
    
    // SQLDelight database
    single { 
        val driver = get<DatabaseDriverFactory>().createDriver()
        QRShieldDatabase(driver)
    }
    
    // History repository with persistence
    single<HistoryRepository> { 
        SqlDelightHistoryRepository(get()) 
    }
    
    // Scanner (Android-specific using CameraX + ML Kit)
    single<QrScanner> { QrScannerFactory(androidContext()).create() }
    
    // ViewModel with injected repository
    factory { 
        SharedViewModel(
            phishingEngine = get(),
            historyRepository = get(),
            coroutineScope = get()
        ) 
    }
}
