package com.qrshield.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.qrshield.android.ui.QRShieldApp
import com.qrshield.android.ui.theme.QRShieldTheme

/**
 * Main entry point for QR-SHIELD Android application.
 * 
 * Features:
 * - Android 12+ SplashScreen API
 * - Edge-to-edge display
 * - Material 3 theming
 * - Koin dependency injection (via QRShieldApplication)
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Configure splash screen behavior
        splashScreen.setKeepOnScreenCondition { false }
        
        // Enable edge-to-edge for immersive experience
        enableEdgeToEdge()
        
        setContent {
            QRShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QRShieldApp()
                }
            }
        }
    }
}
