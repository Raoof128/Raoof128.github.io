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

package com.raouf.mehrguard.android

import android.app.Application
import android.os.Build
import android.util.Log
import com.raouf.mehrguard.android.di.androidModule
import com.raouf.mehrguard.android.ota.AndroidOtaFactory
import com.raouf.mehrguard.ota.OtaUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * QR-SHIELD Android Application
 *
 * Entry point for the Android application.
 * Initializes Koin dependency injection and OTA update manager.
 *
 * Features:
 * - Koin DI initialization
 * - Production-ready logging configuration
 * - Android 16 compatibility
 * - **Living Engine OTA Updates** (NEW in v1.3.0)
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class MehrGuardApplication : Application() {

    companion object {
        private const val TAG = "MehrGuardApplication"

        /** Check if running on Android 16+ */
        val isAndroid16OrHigher: Boolean
            get() = Build.VERSION.SDK_INT >= 35

        /** Check if running on Android 12+ (dynamic colors) */
        val supportsDynamicColors: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        /** OTA Update Manager instance (accessible app-wide) */
        lateinit var otaUpdateManager: OtaUpdateManager
            private set
    }

    // Application-scoped coroutine scope for background tasks
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin for dependency injection
        startKoin {
            // Use ERROR level logging in production for performance
            // Use DEBUG only during development
            androidLogger(
                if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR
            )

            // Provide Android context
            androidContext(this@MehrGuardApplication)

            // Load all modules
            modules(androidModule)
        }

        // Initialize and launch OTA updates (Living Engine)
        initializeOtaUpdates()
    }

    /**
     * Initialize the Living Engine OTA update system.
     *
     * Launches a background coroutine to:
     * 1. Check version.json on GitHub Pages
     * 2. Download newer engine data if available
     * 3. Cache to local storage for offline use
     *
     * This ensures the detection engine stays fresh
     * even between app store releases.
     */
    private fun initializeOtaUpdates() {
        Log.d(TAG, "Initializing OTA Update Manager...")

        otaUpdateManager = AndroidOtaFactory.createUpdateManager(this)

        // Launch update check in background
        applicationScope.launch {
            try {
                Log.d(TAG, "Starting OTA update check...")
                otaUpdateManager.checkAndUpdate()

                when (val state = otaUpdateManager.updateState.value) {
                    is OtaUpdateManager.UpdateState.Success -> {
                        Log.i(TAG, "✅ OTA Update successful! Version: ${state.version}")
                    }
                    is OtaUpdateManager.UpdateState.NoUpdateNeeded -> {
                        Log.d(TAG, "📦 Using current engine version: ${otaUpdateManager.currentVersion.value}")
                    }
                    is OtaUpdateManager.UpdateState.Error -> {
                        Log.w(TAG, "⚠️ OTA Update failed: ${state.message}")
                    }
                    else -> {
                        Log.d(TAG, "OTA State: $state")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "OTA Update error: ${e.message}", e)
            }
        }
    }
}
