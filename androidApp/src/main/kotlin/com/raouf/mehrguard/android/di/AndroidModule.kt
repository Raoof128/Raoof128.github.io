/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

package com.raouf.mehrguard.android.di

import app.cash.sqldelight.db.SqlDriver
import com.raouf.mehrguard.core.PhishingEngine
import com.raouf.mehrguard.data.DatabaseDriverFactory
import com.raouf.mehrguard.data.HistoryRepository
import com.raouf.mehrguard.data.SqlDelightHistoryRepository
import com.raouf.mehrguard.scanner.QrScanner
import com.raouf.mehrguard.scanner.QrScannerFactory
import com.raouf.mehrguard.ui.SharedViewModel
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
 * @author Mehr Guard Security Team
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
    single<com.raouf.mehrguard.data.SettingsDataSource> {
        com.raouf.mehrguard.data.AndroidSettingsDataSource(androidContext())
    }

    // Domain List Repository for allowlist/blocklist persistence
    single { com.raouf.mehrguard.android.data.DomainListRepository(androidContext()) }

    // ViewModel as singleton to share state across all screens
    single {
        SharedViewModel(
            phishingEngine = get(),
            historyRepository = get(),
            settingsDataSource = get(),
            coroutineScope = get()
        )
    }

    // Beat the Bot Game ViewModel
    factory { com.raouf.mehrguard.android.ui.viewmodels.BeatTheBotViewModel() }
}
