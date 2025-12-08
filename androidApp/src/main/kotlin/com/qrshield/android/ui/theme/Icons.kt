/*
 * Copyright 2024 QR-SHIELD Contributors
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

package com.qrshield.android.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.qrshield.android.R
import com.qrshield.model.Verdict

/**
 * QR-SHIELD Icon Resources
 * 
 * Provides access to all app icons as Compose Painters.
 * Mirrors the iOS SF Symbols for cross-platform consistency.
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
object QRShieldIcons {
    
    // =====================================
    // VERDICT ICONS
    // =====================================
    
    /** Shield with checkmark - SAFE verdict */
    val Safe: Painter
        @Composable get() = painterResource(R.drawable.ic_shield_safe)
    
    /** Shield with exclamation - SUSPICIOUS verdict */
    val Warning: Painter
        @Composable get() = painterResource(R.drawable.ic_shield_warning)
    
    /** Shield with X - MALICIOUS verdict */
    val Danger: Painter
        @Composable get() = painterResource(R.drawable.ic_shield_danger)
    
    // =====================================
    // NAVIGATION ICONS
    // =====================================
    
    /** History/clock icon */
    val History: Painter
        @Composable get() = painterResource(R.drawable.ic_history)
    
    /** Settings gear icon */
    val Settings: Painter
        @Composable get() = painterResource(R.drawable.ic_settings)
    
    /** Photo gallery icon */
    val Gallery: Painter
        @Composable get() = painterResource(R.drawable.ic_gallery)
    
    // =====================================
    // SCANNER ICONS
    // =====================================
    
    /** QR code pattern */
    val QrCode: Painter
        @Composable get() = painterResource(R.drawable.ic_qr_code)
    
    /** Scanner viewfinder */
    val Scan: Painter
        @Composable get() = painterResource(R.drawable.ic_scan)
    
    // =====================================
    // HELPER FUNCTIONS
    // =====================================
    
    /**
     * Get the appropriate icon for a verdict.
     */
    @Composable
    fun forVerdict(verdict: Verdict): Painter = when (verdict) {
        Verdict.SAFE -> Safe
        Verdict.SUSPICIOUS -> Warning
        Verdict.MALICIOUS -> Danger
        Verdict.UNKNOWN -> Warning
    }
}
