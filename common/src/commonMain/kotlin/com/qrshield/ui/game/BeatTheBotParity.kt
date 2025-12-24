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

package com.qrshield.ui.game

/**
 * Cross-Platform Parity Constants for Beat The Bot
 * 
 * These values MUST be identical across all 5 platforms:
 * - Android (KMP Compose)
 * - iOS (SwiftUI)
 * - Desktop (KMP Compose)
 * - Web (JavaScript)
 * 
 * Any change here requires corresponding updates to:
 * - iosApp/QRShield/UI/Components/BrainVisualizer.swift
 * - webApp/src/jsMain/resources/visualizer.js
 * - webApp/src/jsMain/resources/game.js
 * 
 * @author QR-SHIELD Cross-Platform Team
 * @since 1.17.30
 */
object BeatTheBotParity {
    
    // =========================================================================
    // BRAIN VISUALIZER
    // =========================================================================
    
    /** Number of nodes in the brain visualization */
    const val NODE_COUNT = 80
    
    /** Fixed seed for reproducible node positions across all platforms */
    const val NODE_SEED = 12345
    
    /** Animation cycle duration in milliseconds */
    const val PULSE_DURATION_MS = 2000
    
    /** Base node radius in dp/pt/px */
    const val BASE_NODE_RADIUS = 3f
    
    /** Active node radius when threat detected */
    const val ACTIVE_NODE_RADIUS = 6f
    
    /** Ripple radius multiplier */
    const val RIPPLE_MULTIPLIER = 2.5f
    
    /** Pulse scale amplitude (1 ± this value) */
    const val PULSE_AMPLITUDE = 0.3f
    
    /** Canvas height in dp */
    const val CANVAS_HEIGHT_DP = 200
    
    /** Nodes per signal cluster (center + neighbors) */
    const val CLUSTER_NEIGHBOR_COUNT = 8
    
    // =========================================================================
    // COLORS
    // =========================================================================
    
    /** Safe/idle brain color (hex) */
    const val COLOR_SAFE_HEX = "#3b82f6"
    
    /** Danger/alert brain color (hex) */
    const val COLOR_DANGER_HEX = "#ef4444"
    
    /** Inactive node color (hex with opacity) */
    const val COLOR_INACTIVE_HEX = "#6b7280"
    const val OPACITY_INACTIVE = 0.3f
    
    /** Connection line color opacity */
    const val OPACITY_CONNECTION = 0.1f
    const val OPACITY_ACTIVE_CONNECTION = 0.3f
    
    /** Ripple opacity */
    const val OPACITY_RIPPLE = 0.2f
    
    // =========================================================================
    // GAME CONFIG
    // =========================================================================
    
    /** Total rounds per game */
    const val TOTAL_ROUNDS = 10
    
    /** Points for correct answer */
    const val POINTS_CORRECT = 100
    
    /** Points deducted for wrong answer */
    const val POINTS_WRONG = -25
    
    /** Bonus points per streak count */
    const val STREAK_BONUS = 25
    
    /** Bot base points per round */
    const val BOT_BASE_POINTS = 100
    
    // =========================================================================
    // KEYBOARD SHORTCUTS (Desktop/Web only)
    // =========================================================================
    
    /** Key for Phishing decision */
    val KEYS_PHISHING = listOf('P', 'p', '1')
    
    /** Key for Legitimate decision */
    val KEYS_LEGITIMATE = listOf('L', 'l', '2')
    
    /** Key for Next Round / Continue */
    val KEYS_CONTINUE = listOf('\n') // Enter key
    
    /** Key for Escape / Return to Dashboard */
    val KEYS_ESCAPE = listOf('\u001B') // Escape key
    
    // =========================================================================
    // ACCESSIBILITY
    // =========================================================================
    
    /** Idle state accessibility description template */
    const val A11Y_IDLE = "AI Neural Net: No threats detected. Brain pattern is calm and blue."
    
    /** Active state accessibility description template */
    const val A11Y_ACTIVE_TEMPLATE = "AI Neural Net: Active alert. Detected signals: {signals}. Brain pattern is pulsing red."
    
    /** Generate accessibility description */
    fun getAccessibilityDescription(signals: List<String>): String {
        return if (signals.isEmpty()) {
            A11Y_IDLE
        } else {
            A11Y_ACTIVE_TEMPLATE.replace("{signals}", signals.joinToString(", "))
        }
    }
    
    // =========================================================================
    // SIGNAL TYPES
    // =========================================================================
    
    /** Standard signal types across all platforms */
    val STANDARD_SIGNALS = listOf(
        "SUSPICIOUS_DOMAIN",
        "TYPOSQUATTING", 
        "HOMOGRAPH_ATTACK",
        "RISKY_TLD",
        "INSECURE_PROTOCOL",
        "SUBDOMAIN_ABUSE",
        "URL_SHORTENER",
        "URGENCY_TACTIC",
        "BRAND_IMPERSONATION",
        "NEW_DOMAIN",
        "CERTIFICATE_ISSUE"
    )
    
    /** Format signal for display (replace underscores with spaces) */
    fun formatSignalForDisplay(signal: String): String {
        return signal.replace("_", " ")
    }
}
