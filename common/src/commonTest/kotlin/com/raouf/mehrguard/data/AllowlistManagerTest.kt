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

package com.raouf.mehrguard.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for AllowlistManager.
 *
 * Tests state management, persistence synchronization, and toggle operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AllowlistManagerTest {

    private fun createManager(scope: TestScope): AllowlistManager {
        return AllowlistManager(
            repository = InMemoryAllowlistRepository(),
            scope = scope
        )
    }

    // =========================================================================
    // INITIAL STATE TESTS
    // =========================================================================

    @Test
    fun `initial state is loading then empty`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        assertFalse(manager.state.value.isLoading)
        assertTrue(manager.state.value.allowlist.isEmpty())
        assertTrue(manager.state.value.blocklist.isEmpty())
    }

    // =========================================================================
    // ALLOWLIST OPERATIONS
    // =========================================================================

    @Test
    fun `addToAllowlist adds domain and updates state`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()

        assertEquals(1, manager.state.value.allowlist.size)
        assertEquals("example.com", manager.state.value.allowlist[0].domain)
    }

    @Test
    fun `addToAllowlist normalizes domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("https://WWW.EXAMPLE.COM/path")
        advanceUntilIdle()

        assertEquals("example.com/path", manager.state.value.allowlist[0].domain)
    }

    @Test
    fun `addToAllowlist rejects empty domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        val result = manager.addToAllowlist("")
        advanceUntilIdle()

        assertFalse(result)
        assertTrue(manager.state.value.allowlist.isEmpty())
    }

    @Test
    fun `addToAllowlist rejects duplicate domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()
        manager.addToAllowlist("example.com")
        advanceUntilIdle()

        assertEquals(1, manager.state.value.allowlist.size)
        assertTrue(manager.state.value.lastOperation is AllowlistManager.Operation.Error)
    }

    @Test
    fun `removeFromAllowlist removes domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()
        manager.removeFromAllowlist("example.com")
        advanceUntilIdle()

        assertTrue(manager.state.value.allowlist.isEmpty())
    }

    @Test
    fun `isAllowlisted returns true for allowlisted domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()

        assertTrue(manager.isAllowlisted("example.com"))
        assertFalse(manager.isAllowlisted("other.com"))
    }

    @Test
    fun `isAllowlisted supports wildcard domains`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("*.example.com")
        advanceUntilIdle()

        assertTrue(manager.isAllowlisted("sub.example.com"))
        assertTrue(manager.isAllowlisted("deep.sub.example.com"))
        assertFalse(manager.isAllowlisted("other.com"))
    }

    @Test
    fun `clearAllowlist removes all entries`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example1.com")
        manager.addToAllowlist("example2.com")
        advanceUntilIdle()

        manager.clearAllowlist()
        advanceUntilIdle()

        assertTrue(manager.state.value.allowlist.isEmpty())
    }

    // =========================================================================
    // BLOCKLIST OPERATIONS
    // =========================================================================

    @Test
    fun `addToBlocklist adds domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToBlocklist("malicious.com")
        advanceUntilIdle()

        assertEquals(1, manager.state.value.blocklist.size)
        assertEquals("malicious.com", manager.state.value.blocklist[0].domain)
    }

    @Test
    fun `removeFromBlocklist removes domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToBlocklist("malicious.com")
        advanceUntilIdle()
        manager.removeFromBlocklist("malicious.com")
        advanceUntilIdle()

        assertTrue(manager.state.value.blocklist.isEmpty())
    }

    @Test
    fun `isBlocklisted returns true for blocked domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToBlocklist("malicious.com")
        advanceUntilIdle()

        assertTrue(manager.isBlocklisted("malicious.com"))
        assertFalse(manager.isBlocklisted("safe.com"))
    }

    // =========================================================================
    // TOGGLE OPERATIONS
    // =========================================================================

    @Test
    fun `setAllowlistEnabled true adds domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.setAllowlistEnabled("example.com", enabled = true)
        advanceUntilIdle()

        assertTrue(manager.isAllowlisted("example.com"))
    }

    @Test
    fun `setAllowlistEnabled false removes domain`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()
        manager.setAllowlistEnabled("example.com", enabled = false)
        advanceUntilIdle()

        assertFalse(manager.isAllowlisted("example.com"))
    }

    @Test
    fun `setBlocklistEnabled toggles correctly`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.setBlocklistEnabled("malicious.com", enabled = true)
        advanceUntilIdle()
        assertTrue(manager.isBlocklisted("malicious.com"))

        manager.setBlocklistEnabled("malicious.com", enabled = false)
        advanceUntilIdle()
        assertFalse(manager.isBlocklisted("malicious.com"))
    }

    // =========================================================================
    // STATE SYNCHRONIZATION TESTS
    // =========================================================================

    @Test
    fun `state updates after successful operation`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()

        assertNotNull(manager.state.value.lastOperation)
        assertTrue(manager.state.value.lastOperation is AllowlistManager.Operation.Added)
    }

    @Test
    fun `clearLastOperation removes operation from state`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()
        assertNotNull(manager.state.value.lastOperation)

        manager.clearLastOperation()
        advanceUntilIdle()

        assertEquals(null, manager.state.value.lastOperation)
    }

    @Test
    fun `isLoading is false after operation completes`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()

        assertFalse(manager.state.value.isLoading)
    }

    // =========================================================================
    // DOMAIN ENTRY TESTS
    // =========================================================================

    @Test
    fun `domain entry has timestamp`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()

        val entry = manager.state.value.allowlist[0]
        assertTrue(entry.addedAt > 0)
    }

    @Test
    fun `domain entry has addedBy field`() = runTest {
        val manager = createManager(this)
        advanceUntilIdle()

        manager.addToAllowlist("example.com")
        advanceUntilIdle()

        val entry = manager.state.value.allowlist[0]
        assertEquals("user", entry.addedBy)
    }
}
