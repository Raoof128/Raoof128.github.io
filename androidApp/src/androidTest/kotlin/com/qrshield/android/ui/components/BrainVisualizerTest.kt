package com.qrshield.android.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrainVisualizerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun brainVisualizer_showsIdleState_whenSignalsEmpty() {
        composeTestRule.setContent {
            BrainVisualizer(detectedSignals = emptyList())
        }

        // Check content description for idle state (Blue brain)
        composeTestRule.onNodeWithContentDescription("AI Neural Net: No threats detected. Brain pattern is calm and blue.", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun brainVisualizer_showsBadges_whenSignalsProvided() {
        val signals = listOf("TLD_ABUSE", "BRAND_IMPERSONATION")
        composeTestRule.setContent {
            BrainVisualizer(detectedSignals = signals)
        }

        // Check content description for active state (Red pulsing brain)
        composeTestRule.onNodeWithContentDescription("AI Neural Net: Active alert. Detected signals: TLD_ABUSE, BRAND_IMPERSONATION. Brain pattern is pulsing red.", useUnmergedTree = true)
            .assertExists()

        // Check that badges are rendered with cleaned up text (underscore replaced by space)
        composeTestRule.onNodeWithText("TLD ABUSE").assertExists()
        composeTestRule.onNodeWithText("BRAND IMPERSONATION").assertExists()
    }
}
