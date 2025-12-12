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

package com.qrshield.android

import android.app.Application
import android.os.Build
import com.qrshield.android.di.androidModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * QR-SHIELD Android Application
 * 
 * Entry point for the Android application.
 * Initializes Koin dependency injection with all required modules.
 * 
 * Features:
 * - Koin DI initialization
 * - Production-ready logging configuration
 * - Android 16 compatibility
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class QRShieldApplication : Application() {
    
    companion object {
        /** Check if running on Android 16+ */
        val isAndroid16OrHigher: Boolean
            get() = Build.VERSION.SDK_INT >= 35
        
        /** Check if running on Android 12+ (dynamic colors) */
        val supportsDynamicColors: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
    
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
            androidContext(this@QRShieldApplication)
            
            // Load all modules
            modules(androidModule)
        }
    }
}
