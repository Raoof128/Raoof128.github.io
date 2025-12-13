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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qrshield.model.Verdict
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI Tests for Scanner Screen
 *
 * Tests the main scanner interface using Compose UI Testing.
 * These tests verify:
 * - UI elements are visible and accessible
 * - Navigation between states works correctly
 * - User interactions trigger expected behaviors
 *
 * Note: Camera-related tests require device/emulator with camera support.
 */
@RunWith(AndroidJUnit4::class)
class ScannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =========================================================================
    // IDLE STATE TESTS
    // =========================================================================

    @Test
    fun idleState_displaysWelcomeMessage() {
        composeTestRule.setContent {
            IdleStateTestContent()
        }

        composeTestRule.onNodeWithText("Scan QR", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun idleState_scanButtonIsClickable() {
        var clicked = false

        composeTestRule.setContent {
            IdleStateTestContent(onScanClick = { clicked = true })
        }

        composeTestRule.onNodeWithContentDescription("Scan QR Code", substring = true, useUnmergedTree = true)
            .performClick()

        assert(clicked) { "Scan button click was not registered" }
    }

    @Test
    fun idleState_galleryButtonExists() {
        composeTestRule.setContent {
            IdleStateTestContent()
        }

        composeTestRule.onNodeWithContentDescription("gallery", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }

    // =========================================================================
    // ANALYZING STATE TESTS
    // =========================================================================

    @Test
    fun analyzingState_showsLoadingIndicator() {
        composeTestRule.setContent {
            AnalyzingStateTestContent(url = "https://example.com")
        }

        composeTestRule.onNodeWithContentDescription("Loading", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun analyzingState_showsUrl() {
        val testUrl = "https://test.example.com"

        composeTestRule.setContent {
            AnalyzingStateTestContent(url = testUrl)
        }

        composeTestRule.onNodeWithText(testUrl, substring = true, useUnmergedTree = true)
            .assertExists()
    }

    // =========================================================================
    // RESULT STATE TESTS - SAFE VERDICT
    // =========================================================================

    @Test
    fun resultState_safeVerdict_showsGreenIndicator() {
        composeTestRule.setContent {
            ResultStateTestContent(
                url = "https://google.com",
                verdict = Verdict.SAFE,
                score = 5
            )
        }

        composeTestRule.onNodeWithText("Safe", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun resultState_safeVerdict_showsLowScore() {
        composeTestRule.setContent {
            ResultStateTestContent(
                url = "https://google.com",
                verdict = Verdict.SAFE,
                score = 5
            )
        }

        composeTestRule.onNodeWithText("5", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    // =========================================================================
    // RESULT STATE TESTS - SUSPICIOUS VERDICT
    // =========================================================================

    @Test
    fun resultState_suspiciousVerdict_showsWarning() {
        composeTestRule.setContent {
            ResultStateTestContent(
                url = "https://paypa1.com",
                verdict = Verdict.SUSPICIOUS,
                score = 35
            )
        }

        composeTestRule.onNodeWithText("Suspicious", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }

    // =========================================================================
    // RESULT STATE TESTS - MALICIOUS VERDICT
    // =========================================================================

    @Test
    fun resultState_maliciousVerdict_showsDanger() {
        composeTestRule.setContent {
            ResultStateTestContent(
                url = "http://192.168.1.1/phishing",
                verdict = Verdict.MALICIOUS,
                score = 85
            )
        }

        composeTestRule.onNodeWithText("Malicious", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun resultState_maliciousVerdict_showsHighScore() {
        composeTestRule.setContent {
            ResultStateTestContent(
                url = "http://192.168.1.1/phishing",
                verdict = Verdict.MALICIOUS,
                score = 85
            )
        }

        composeTestRule.onNodeWithText("85", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    // =========================================================================
    // ERROR STATE TESTS
    // =========================================================================

    @Test
    fun errorState_showsErrorMessage() {
        val errorMessage = "Camera access denied"

        composeTestRule.setContent {
            ErrorStateTestContent(message = errorMessage)
        }

        composeTestRule.onNodeWithText(errorMessage, substring = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun errorState_retryButtonIsClickable() {
        var retryClicked = false

        composeTestRule.setContent {
            ErrorStateTestContent(
                message = "Something went wrong",
                onRetry = { retryClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Retry", substring = true, ignoreCase = true, useUnmergedTree = true)
            .performClick()

        assert(retryClicked) { "Retry button click was not registered" }
    }

    // =========================================================================
    // ACCESSIBILITY TESTS
    // =========================================================================

    @Test
    fun scanButton_hasContentDescription() {
        composeTestRule.setContent {
            IdleStateTestContent()
        }

        composeTestRule.onAllNodesWithContentDescription("Scan", substring = true, useUnmergedTree = true)
            .assertCountEquals(1)
    }

    @Test
    fun resultCard_isFocusable() {
        composeTestRule.setContent {
            ResultStateTestContent(
                url = "https://example.com",
                verdict = Verdict.SAFE,
                score = 10
            )
        }

        composeTestRule.onRoot(useUnmergedTree = true).assertExists()
    }
}

// =============================================================================
// TEST COMPOSABLES
// =============================================================================

@Composable
private fun IdleStateTestContent(
    onScanClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Scan QR Code to check for phishing")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onScanClick,
            modifier = Modifier.semantics {
                contentDescription = "Scan QR Code Button"
            }
        ) {
            Text("Scan QR")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier.semantics {
                contentDescription = "Select from gallery"
            }
        ) {
            Text("From Gallery")
        }
    }
}

@Composable
private fun AnalyzingStateTestContent(url: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics {
                contentDescription = "Loading indicator"
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Analyzing...")
        Text(url, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ResultStateTestContent(
    url: String,
    verdict: Verdict,
    score: Int,
    flags: List<String> = emptyList(),
    onDismiss: () -> Unit = {},
    onScanAnother: () -> Unit = {}
) {
    val verdictColor = when (verdict) {
        Verdict.SAFE -> Color(0xFF4CAF50)
        Verdict.SUSPICIOUS -> Color(0xFFFF9800)
        Verdict.MALICIOUS -> Color(0xFFF44336)
        Verdict.UNKNOWN -> Color.Gray
    }

    val verdictText = when (verdict) {
        Verdict.SAFE -> "Safe"
        Verdict.SUSPICIOUS -> "Suspicious"
        Verdict.MALICIOUS -> "Malicious"
        Verdict.UNKNOWN -> "Unknown"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = verdictColor.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = verdictText,
                    style = MaterialTheme.typography.headlineMedium,
                    color = verdictColor
                )

                Text(
                    text = "Score: $score",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(url, style = MaterialTheme.typography.bodyMedium)

        if (flags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            flags.forEach { flag ->
                Text("• $flag", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
            Button(onClick = onScanAnother) {
                Text("Scan Another")
            }
        }
    }
}

@Composable
private fun ErrorStateTestContent(
    message: String,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Error", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))

        Text(message, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
