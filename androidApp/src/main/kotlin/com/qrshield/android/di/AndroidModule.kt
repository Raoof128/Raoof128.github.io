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

package com.qrshield.android.di

import app.cash.sqldelight.db.SqlDriver
import com.qrshield.core.PhishingEngine
import com.qrshield.data.DatabaseDriverFactory
import com.qrshield.data.HistoryRepository
import com.qrshield.data.SqlDelightHistoryRepository
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
    
    // SQLDriver - used by SqlDelightHistoryRepository
    single<SqlDriver> { 
        get<DatabaseDriverFactory>().createDriver()
    }
    
    // History repository with persistence
    // SqlDelightHistoryRepository takes (driver, scope)
    single<HistoryRepository> { 
        SqlDelightHistoryRepository(
            driver = get(),
            scope = get()
        )
    }
    
    // Scanner (Android-specific using CameraX + ML Kit)
    single<QrScanner> { QrScannerFactory(androidContext()).create() }
    
    // Settings DataSource
    single<com.qrshield.data.SettingsDataSource> { 
        com.qrshield.data.AndroidSettingsDataSource(androidContext())
    }
    
    // ViewModel with injected repository
    factory { 
        SharedViewModel(
            phishingEngine = get(),
            historyRepository = get(),
            settingsDataSource = get(),
            coroutineScope = get()
        ) 
    }
}
