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

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.qrshield.android.ui.QRShieldApp
import com.qrshield.android.ui.theme.QRShieldTheme

/**
 * Main entry point for QR-SHIELD Android application.
 *
 * Handles deep links and widget actions:
 * - ACTION=SCAN: Opens directly to scanner with camera active
 * - Default: Opens to home screen
 */
class MainActivity : ComponentActivity() {

    companion object {
        /** Intent extra key for widget/shortcut actions */
        const val EXTRA_ACTION = "ACTION"
        
        /** Action to start scanning immediately */
        const val ACTION_SCAN = "SCAN"
        
        /** Observable state for quick scan from widget */
        val shouldStartScan = mutableStateOf(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE calling super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Handle widget/shortcut action
        handleIntent(intent)

        setContent {
            QRShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D1117)
                ) {
                    QRShieldApp()
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    /**
     * Handle intents from widgets, shortcuts, and deep links.
     */
    private fun handleIntent(intent: Intent?) {
        val action = intent?.getStringExtra(EXTRA_ACTION)
        when (action) {
            ACTION_SCAN -> {
                // Signal to start scanning immediately
                shouldStartScan.value = true
            }
        }
    }
}

