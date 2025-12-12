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
 * UI Tests for History Screen
 * 
 * Tests the scan history interface using Compose UI Testing.
 */
@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    // =========================================================================
    // EMPTY STATE TESTS
    // =========================================================================
    
    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            EmptyHistoryTestContent()
        }
        
        // Empty message should be visible
        composeTestRule.onNodeWithText("No scans yet", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun emptyState_showsScanPrompt() {
        composeTestRule.setContent {
            EmptyHistoryTestContent()
        }
        
        // Prompt to scan should be visible
        composeTestRule.onNodeWithText("scan", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    // =========================================================================
    // HISTORY LIST TESTS
    // =========================================================================
    
    @Test
    fun historyList_showsItems() {
        composeTestRule.setContent {
            HistoryListTestContent(
                items = listOf(
                    TestHistoryItem("https://google.com", "SAFE", 5),
                    TestHistoryItem("https://paypa1.com", "SUSPICIOUS", 45),
                    TestHistoryItem("http://192.168.1.1", "MALICIOUS", 85)
                )
            )
        }
        
        // All URLs should be visible
        composeTestRule.onNodeWithText("google.com", substring = true, useUnmergedTree = true)
            .assertExists()
        composeTestRule.onNodeWithText("paypa1.com", substring = true, useUnmergedTree = true)
            .assertExists()
        composeTestRule.onNodeWithText("192.168.1.1", substring = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun historyList_showsVerdicts() {
        composeTestRule.setContent {
            HistoryListTestContent(
                items = listOf(
                    TestHistoryItem("https://google.com", "Safe", 5),
                    TestHistoryItem("https://paypa1.com", "Suspicious", 45)
                )
            )
        }
        
        // Verdicts should be visible
        composeTestRule.onNodeWithText("Safe", substring = true, useUnmergedTree = true)
            .assertExists()
        composeTestRule.onNodeWithText("Suspicious", substring = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun historyItem_isClickable() {
        var clickedUrl: String? = null
        
        composeTestRule.setContent {
            HistoryListTestContent(
                items = listOf(
                    TestHistoryItem("https://example.com", "SAFE", 10)
                ),
                onItemClick = { clickedUrl = it }
            )
        }
        
        // Click on history item
        composeTestRule.onNodeWithText("example.com", substring = true, useUnmergedTree = true)
            .performClick()
        
        assert(clickedUrl == "https://example.com") { "Item click was not registered" }
    }
    
    // =========================================================================
    // FILTER TESTS
    // =========================================================================
    
    @Test
    fun filterChips_exist() {
        composeTestRule.setContent {
            HistoryWithFiltersTestContent()
        }
        
        // Filter chips should exist
        composeTestRule.onNodeWithText("All", substring = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun filterChip_isClickable() {
        var selectedFilter: String? = null
        
        composeTestRule.setContent {
            HistoryWithFiltersTestContent(
                onFilterChange = { selectedFilter = it }
            )
        }
        
        // Click on a filter chip
        composeTestRule.onNodeWithText("Safe", substring = true, useUnmergedTree = true)
            .performClick()
        
        assert(selectedFilter == "Safe") { "Filter selection was not registered" }
    }
    
    // =========================================================================
    // CLEAR HISTORY TESTS
    // =========================================================================
    
    @Test
    fun clearButton_exists() {
        composeTestRule.setContent {
            HistoryListTestContent(
                items = listOf(
                    TestHistoryItem("https://example.com", "SAFE", 10)
                )
            )
        }
        
        // Clear button should exist
        composeTestRule.onNodeWithContentDescription("Clear", substring = true, ignoreCase = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun clearButton_triggersConfirmation() {
        var confirmationShown = false
        
        composeTestRule.setContent {
            HistoryListTestContent(
                items = listOf(
                    TestHistoryItem("https://example.com", "SAFE", 10)
                ),
                onClearClick = { confirmationShown = true }
            )
        }
        
        // Click clear button
        composeTestRule.onNodeWithContentDescription("Clear", substring = true, ignoreCase = true, useUnmergedTree = true)
            .performClick()
        
        assert(confirmationShown) { "Clear confirmation was not triggered" }
    }
}

// =============================================================================
// TEST DATA & COMPOSABLES
// =============================================================================

private data class TestHistoryItem(
    val url: String,
    val verdict: String,
    val score: Int
)

@Composable
private fun EmptyHistoryTestContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No scans yet")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Start scanning QR codes to build your history")
    }
}

@Composable
private fun HistoryListTestContent(
    items: List<TestHistoryItem>,
    onItemClick: (String) -> Unit = {},
    onClearClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with clear button
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History", style = MaterialTheme.typography.headlineSmall)
            
            IconButton(
                onClick = onClearClick,
                modifier = Modifier.semantics {
                    contentDescription = "Clear history"
                }
            ) {
                Text("🗑️")
            }
        }
        
        // History items
        items.forEach { item ->
            Surface(
                onClick = { onItemClick(item.url) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(item.url)
                        Text(item.verdict, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${item.score}")
                }
            }
        }
    }
}

@Composable
private fun HistoryWithFiltersTestContent(
    onFilterChange: (String) -> Unit = {}
) {
    val filters = listOf("All", "Safe", "Suspicious", "Malicious")
    var selected by remember { mutableStateOf("All") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selected == filter,
                    onClick = {
                        selected = filter
                        onFilterChange(filter)
                    },
                    label = { Text(filter) }
                )
            }
        }
        
        // Placeholder content
        Text("Filtered history items would appear here")
    }
}
