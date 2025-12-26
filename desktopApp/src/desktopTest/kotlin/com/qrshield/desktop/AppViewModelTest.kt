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

package com.qrshield.desktop

import com.qrshield.data.HistoryRepositoryFactory
import com.qrshield.model.ScanHistoryItem
import com.qrshield.model.ScanSource
import com.qrshield.model.Verdict
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppViewModelTest {

    private class InMemorySettingsStore : DesktopSettingsStore {
        private var settings = SettingsManager.Settings()

        override fun load(): SettingsManager.Settings = settings

        override fun save(settings: SettingsManager.Settings) {
            this.settings = settings
        }
    }

    private fun createViewModel(): AppViewModel {
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        return AppViewModel(
            scope = testScope,
            settingsStore = InMemorySettingsStore(),
            historyRepository = HistoryRepositoryFactory.create()
        )
    }

    @Test
    fun filteredHistory_respectsFilterAndQuery() {
        val viewModel = createViewModel()
        try {
            viewModel.scanHistory = listOf(
                ScanHistoryItem(
                    id = "1",
                    url = "https://example.com/login",
                    score = 10,
                    verdict = Verdict.SAFE,
                    scannedAt = 1000L,
                    source = ScanSource.CAMERA
                ),
                ScanHistoryItem(
                    id = "2",
                    url = "https://phish.test/offer",
                    score = 90,
                    verdict = Verdict.MALICIOUS,
                    scannedAt = 2000L,
                    source = ScanSource.GALLERY
                ),
                ScanHistoryItem(
                    id = "3",
                    url = "https://example.com/reset",
                    score = 40,
                    verdict = Verdict.SUSPICIOUS,
                    scannedAt = 3000L,
                    source = ScanSource.CLIPBOARD
                )
            )

            viewModel.updateHistoryFilter(HistoryFilter.Safe)
            viewModel.updateHistorySearch("example")

            val filtered = viewModel.filteredHistory()

            assertEquals(1, filtered.size)
            assertEquals("1", filtered.first().id)
        } finally {
            viewModel.dispose()
        }
    }

    @Test
    fun sanitizedUrl_stripsQueryAndFragment() {
        val viewModel = createViewModel()
        try {
            val sanitized = viewModel.sanitizedUrl("https://example.com/path?token=123#section")

            assertEquals("https://example.com/path", sanitized)
        } finally {
            viewModel.dispose()
        }
    }

    @Test
    fun hostFromUrl_extractsHost() {
        val viewModel = createViewModel()
        try {
            val host = viewModel.hostFromUrl("https://sub.example.com/path")

            assertEquals("sub.example.com", host)
        } finally {
            viewModel.dispose()
        }
    }

    @Test
    fun submitTrainingVerdict_updatesTrainingState() {
        val viewModel = createViewModel()
        try {
            val initial = viewModel.trainingState

            // Submit verdict - this shows the result modal but doesn't advance round yet
            // Note: Scenarios are shuffled, so we don't know if isPhishing=true is correct
            viewModel.submitTrainingVerdict(isPhishing = true)

            var updated = viewModel.trainingState
            // Score should be updated (either +100 if correct or -25 if incorrect)
            // Since scenarios are shuffled, we can't assume the answer is correct
            // Just verify that score changed and attempts incremented
            assertTrue(updated.score != initial.score || updated.attempts > initial.attempts, 
                "State should update after verdict")
            assertEquals(initial.attempts + 1, updated.attempts, "Attempts should increment")
            assertTrue(updated.showResultModal, "Result modal should be shown")
            // Round doesn't advance until modal is dismissed
            assertEquals(initial.round, updated.round, "Round should not advance until modal dismissed")

            // Dismiss modal to advance round
            viewModel.dismissTrainingResultModal()
            updated = viewModel.trainingState
            assertEquals(initial.round + 1, updated.round, "Round should advance after modal dismissed")
        } finally {
            viewModel.dispose()
        }
    }

    @Test
    fun statusMessage_isSetOnError() {
        val viewModel = createViewModel()
        try {
            viewModel.analyzeUrl("javascript:alert(1)", ScanSource.CLIPBOARD)

            assertNotNull(viewModel.statusMessage)
            assertEquals(MessageKind.Error, viewModel.statusMessage?.kind)
        } finally {
            viewModel.dispose()
        }
    }
}
