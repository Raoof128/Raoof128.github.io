package com.qrshield.android.di

import com.qrshield.core.PhishingEngine
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
 */
val androidModule = module {
    
    // Application scope
    single { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    
    // Core engines
    single { PhishingEngine() }
    
    // Scanner
    single<QrScanner> { QrScannerFactory(androidContext()).create() }
    
    // ViewModel
    factory { SharedViewModel(get(), get()) }
}
