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

package com.qrshield.android.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Sound Manager for QR-SHIELD scan feedback.
 * 
 * Uses ToneGenerator for efficient, lightweight beep sounds.
 * This avoids the need for raw audio files while providing
 * clear auditory feedback.
 */
object SoundManager {
    
    private const val TAG = "SoundManager"
    private const val TONE_DURATION_MS = 150
    
    private var toneGenerator: ToneGenerator? = null
    
    /**
     * Sound types for different scan events.
     */
    enum class SoundType {
        SCAN,       // QR code detected
        SUCCESS,    // Safe result
        WARNING,    // Suspicious result
        ERROR       // Malicious result or error
    }
    
    /**
     * Initialize the ToneGenerator lazily.
     */
    private fun getToneGenerator(): ToneGenerator? {
        if (toneGenerator == null) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to create ToneGenerator: ${e.message}")
            }
        }
        return toneGenerator
    }
    
    /**
     * Play a sound for the given type.
     * 
     * @param type The type of sound to play
     * @param enabled Whether sound is enabled in settings
     */
    fun playSound(type: SoundType, enabled: Boolean) {
        if (!enabled) return
        
        val generator = getToneGenerator() ?: return
        
        try {
            val toneType = when (type) {
                SoundType.SCAN -> ToneGenerator.TONE_PROP_BEEP
                SoundType.SUCCESS -> ToneGenerator.TONE_PROP_ACK
                SoundType.WARNING -> ToneGenerator.TONE_PROP_PROMPT
                SoundType.ERROR -> ToneGenerator.TONE_PROP_NACK
            }
            
            generator.startTone(toneType, TONE_DURATION_MS)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play tone: ${e.message}")
        }
    }
    
    /**
     * Release resources when no longer needed.
     */
    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release ToneGenerator: ${e.message}")
        }
    }
}
