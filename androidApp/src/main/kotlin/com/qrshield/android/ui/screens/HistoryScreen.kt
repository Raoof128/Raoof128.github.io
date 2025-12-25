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

package com.qrshield.android.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qrshield.android.R
import com.qrshield.android.ui.theme.*
import com.qrshield.model.Verdict
import com.qrshield.ui.SharedViewModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

/**
 * History Screen showing all previous scan results.
 *
 * Features:
 * - Filterable list by verdict type
 * - Copy URL to clipboard
 * - Delete individual items
 * - Full accessibility support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onItemClick: (url: String, verdict: String, score: Int) -> Unit = { _, _, _ -> }
) {
    val viewModel: SharedViewModel = koinInject()
    val scanHistory by viewModel.scanHistory.collectAsState()

    var selectedFilter by remember { mutableStateOf(VerdictFilter.ALL) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current

    // Filter history based on selection
    val filteredHistory = remember(scanHistory, selectedFilter, searchQuery) {
        scanHistory.filter { item ->
            val matchesFilter = when (selectedFilter) {
                VerdictFilter.ALL -> true
                VerdictFilter.SAFE -> item.verdict == Verdict.SAFE
                VerdictFilter.SUSPICIOUS -> item.verdict == Verdict.SUSPICIOUS
                VerdictFilter.MALICIOUS -> item.verdict == Verdict.MALICIOUS
            }
            val matchesSearch = searchQuery.isBlank() ||
                item.url.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    val historyScreenDesc = stringResource(R.string.cd_history_screen_items, filteredHistory.size)
    val clearAllDesc = stringResource(R.string.cd_clear_history)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics {
                contentDescription = historyScreenDesc
            }
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.nav_history),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            actions = {
                if (scanHistory.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirmation = true },
                        modifier = Modifier.semantics {
                            contentDescription = clearAllDesc
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = stringResource(R.string.cd_clear_all),
                            tint = VerdictDanger
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Search Bar
        val searchDesc = stringResource(R.string.cd_search_history_url)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .semantics {
                    contentDescription = searchDesc
                },
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.cd_clear_search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VerdictFilter.entries) { filter ->
                val filterDesc = stringResource(R.string.cd_filter_by, filter.displayName)
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = filter.color.copy(alpha = 0.2f),
                        selectedLabelColor = filter.color,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = filter.icon,
                            contentDescription = null,
                            tint = if (selectedFilter == filter) filter.color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = filterDesc
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History List or Empty State
        if (filteredHistory.isEmpty()) {
            EmptyHistoryState(
                hasFilters = selectedFilter != VerdictFilter.ALL || searchQuery.isNotBlank()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = filteredHistory,
                    key = { it.id }
                ) { historyItem ->
                    HistoryItemCard(
                        url = historyItem.url,
                        score = historyItem.score,
                        verdict = historyItem.verdict,
                        scannedAt = historyItem.scannedAt,
                        onClick = {
                            onItemClick(historyItem.url, historyItem.verdict.name, historyItem.score)
                        },
                        onCopyUrl = {
                            clipboardManager.setText(AnnotatedString(historyItem.url))
                        },
                        onDelete = {
                            viewModel.deleteScan(historyItem.id)
                        }
                    )
                }

                // Bottom padding for navigation bar
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Clear All Confirmation Dialog
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = {
                Text(
                    text = stringResource(R.string.clear_history_title),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.clear_history_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirmation = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.clear_all),
                        color = VerdictDanger
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = BrandPrimary
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = QRShieldShapes.Card
        )
    }
}

// =============================================================================
// HISTORY ITEM CARD
// =============================================================================

@Composable
private fun HistoryItemCard(
    url: String,
    score: Int,
    verdict: Verdict,
    scannedAt: Long,
    onClick: () -> Unit = {},
    onCopyUrl: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verdictColor = verdictColor(verdict.name)
    val verdictEmoji = when (verdict) {
        Verdict.SAFE -> "✅"
        Verdict.SUSPICIOUS -> "⚠️"
        Verdict.MALICIOUS -> "🚨"
        Verdict.UNKNOWN -> "❓"
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    val scanResultDesc = stringResource(R.string.cd_scan_result_details, url, verdict.name, score)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = scanResultDesc
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = QRShieldShapes.Card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Verdict Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(verdictColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = verdictEmoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // URL and Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = url,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Verdict badge
                    Text(
                        text = verdict.name,
                        color = verdictColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = " • ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    // Timestamp
                    Text(
                        text = formatTimestamp(scannedAt),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Score Badge
            Surface(
                color = verdictColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$score",
                    color = verdictColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Actions Menu
            Box {
                var expanded by remember { mutableStateOf(false) }
                val moreOptionsDesc = stringResource(R.string.cd_more_options_scan)

                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.semantics {
                        contentDescription = moreOptionsDesc
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.cd_more),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.copy_url), color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            onCopyUrl()
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = BrandPrimary
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = VerdictDanger) },
                        onClick = {
                            expanded = false
                            showDeleteConfirm = true
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = VerdictDanger
                            )
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_item_title), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(stringResource(R.string.delete_item_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.delete), color = VerdictDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = BrandPrimary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = QRShieldShapes.Card
        )
    }
}

// =============================================================================
// EMPTY STATE
// =============================================================================

@Composable
private fun EmptyHistoryState(hasFilters: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .semantics {
                contentDescription = if (hasFilters)
                    "No results match your filters"
                else
                    "No scan history yet. Start scanning to see results here."
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Nice shield graphic for empty state
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(BrandPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasFilters) "🔍" else "🛡️",
                fontSize = 56.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (hasFilters)
                stringResource(R.string.no_results)
            else
                stringResource(R.string.no_history),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasFilters)
                stringResource(R.string.try_different_filter)
            else
                stringResource(R.string.scan_to_see_history),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp
        )

        if (!hasFilters) {
            Spacer(modifier = Modifier.height(24.dp))

            // Subtle hint
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.history_go_to_scanner),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// =============================================================================
// HELPERS
// =============================================================================

private enum class VerdictFilter(
    val displayName: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ALL("All", BrandPrimary, Icons.Default.FilterList),
    SAFE("Safe", VerdictSafe, Icons.Default.CheckCircle),
    SUSPICIOUS("Suspicious", VerdictWarning, Icons.Default.Warning),
    MALICIOUS("Malicious", VerdictDanger, Icons.Default.Dangerous)
}

private fun formatTimestamp(epochMillis: Long): String {
    val dateFormat = SimpleDateFormat("M/d HH:mm", Locale.getDefault())
    return dateFormat.format(Date(epochMillis))
}
