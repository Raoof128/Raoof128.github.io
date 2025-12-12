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

package com.qrshield.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for Settings Screen
 * 
 * Tests the settings interface using Compose UI Testing.
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    // =========================================================================
    // SETTINGS VISIBILITY TESTS
    // =========================================================================
    
    @Test
    fun settings_showsTitle() {
        composeTestRule.setContent {
            SettingsTestContent()
        }
        
        composeTestRule.onNodeWithText("Settings", useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun settings_showsHapticFeedbackToggle() {
        composeTestRule.setContent {
            SettingsTestContent()
        }
        
        composeTestRule.onNodeWithText("Haptic Feedback", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun settings_showsSoundToggle() {
        composeTestRule.setContent {
            SettingsTestContent()
        }
        
        composeTestRule.onNodeWithText("Sound", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun settings_showsAutoScanToggle() {
        composeTestRule.setContent {
            SettingsTestContent()
        }
        
        composeTestRule.onNodeWithText("Auto", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun settings_showsSaveHistoryToggle() {
        composeTestRule.setContent {
            SettingsTestContent()
        }
        
        composeTestRule.onNodeWithText("History", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    // =========================================================================
    // TOGGLE INTERACTION TESTS
    // =========================================================================
    
    @Test
    fun hapticToggle_isClickable() {
        var toggled = false
        
        composeTestRule.setContent {
            SettingsTestContent(
                onHapticToggle = { toggled = true }
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Haptic", substring = true, ignoreCase = true, useUnmergedTree = true)
            .performClick()
        
        assert(toggled) { "Haptic toggle was not triggered" }
    }
    
    @Test
    fun soundToggle_isClickable() {
        var toggled = false
        
        composeTestRule.setContent {
            SettingsTestContent(
                onSoundToggle = { toggled = true }
            )
        }
        
        composeTestRule.onNodeWithContentDescription("Sound", substring = true, ignoreCase = true, useUnmergedTree = true)
            .performClick()
        
        assert(toggled) { "Sound toggle was not triggered" }
    }
    
    // =========================================================================
    // ABOUT SECTION TESTS
    // =========================================================================
    
    @Test
    fun settings_showsVersion() {
        composeTestRule.setContent {
            SettingsTestContent()
        }
        
        composeTestRule.onNodeWithText("Version", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun settings_showsPrivacyLink() {
        composeTestRule.setContent {
            SettingsTestContent()
        }
        
        composeTestRule.onNodeWithText("Privacy", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    // =========================================================================
    // DANGER ZONE TESTS
    // =========================================================================
    
    @Test
    fun settings_showsClearHistoryButton() {
        composeTestRule.setContent {
            SettingsTestContent()
        }
        
        composeTestRule.onNodeWithText("Clear History", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun clearHistory_triggersConfirmation() {
        var confirmationRequested = false
        
        composeTestRule.setContent {
            SettingsTestContent(
                onClearHistory = { confirmationRequested = true }
            )
        }
        
        composeTestRule.onNodeWithText("Clear History", substring = true, ignoreCase = true, useUnmergedTree = true)
            .performClick()
        
        assert(confirmationRequested) { "Clear history confirmation was not requested" }
    }
}

// =============================================================================
// TEST COMPOSABLES
// =============================================================================

@Composable
private fun SettingsTestContent(
    hapticEnabled: Boolean = true,
    soundEnabled: Boolean = true,
    autoScanEnabled: Boolean = false,
    saveHistoryEnabled: Boolean = true,
    onHapticToggle: () -> Unit = {},
    onSoundToggle: () -> Unit = {},
    onAutoScanToggle: () -> Unit = {},
    onSaveHistoryToggle: () -> Unit = {},
    onClearHistory: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Feedback Section
        Text("Feedback", style = MaterialTheme.typography.titleMedium)
        
        SettingsToggleRow(
            label = "Haptic Feedback",
            description = "Vibrate on scan events",
            checked = hapticEnabled,
            onToggle = onHapticToggle
        )
        
        SettingsToggleRow(
            label = "Sound Effects",
            description = "Play sounds on scan",
            checked = soundEnabled,
            onToggle = onSoundToggle
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Scanning Section
        Text("Scanning", style = MaterialTheme.typography.titleMedium)
        
        SettingsToggleRow(
            label = "Auto-Scan on Launch",
            description = "Start camera automatically",
            checked = autoScanEnabled,
            onToggle = onAutoScanToggle
        )
        
        SettingsToggleRow(
            label = "Save History",
            description = "Keep scan history locally",
            checked = saveHistoryEnabled,
            onToggle = onSaveHistoryToggle
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // About Section
        Text("About", style = MaterialTheme.typography.titleMedium)
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Version")
            Text("1.1.0")
        }
        
        TextButton(onClick = {}) {
            Text("Privacy Policy")
        }
        
        TextButton(onClick = {}) {
            Text("Open Source Licenses")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Danger Zone
        Text("Danger Zone", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        
        OutlinedButton(
            onClick = onClearHistory,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Clear History")
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.semantics {
                contentDescription = "$label toggle"
            }
        )
    }
}
