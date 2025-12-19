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

package com.qrshield.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Allowlist/Blocklist Manager
 *
 * Manages trusted and blocked domains with proper state synchronization.
 * Uses MutableStateFlow for reactive UI updates and ensures persistence
 * is always in sync with UI state.
 *
 * ## State Management
 * - All changes go through a single source of truth (StateFlow)
 * - UI state always reflects persisted state (no optimistic updates)
 * - Changes are persisted before state is updated
 * - Configuration changes are survived via StateFlow
 *
 * ## Thread Safety
 * - All mutations are synchronized via CoroutineScope
 * - State is immutable and replaced atomically
 *
 * @author QR-SHIELD Security Team
 * @since 1.4.0
 */
class AllowlistManager(
    private val repository: AllowlistRepository,
    private val scope: CoroutineScope
) {
    // =========================================================================
    // STATE
    // =========================================================================
    
    /**
     * Represents a domain entry in the list.
     */
    data class DomainEntry(
        val domain: String,
        val addedAt: Long = Clock.System.now().toEpochMilliseconds(),
        val addedBy: String = "user",
        val notes: String? = null
    )

    /**
     * UI State for the allowlist/blocklist manager.
     */
    data class AllowlistState(
        val allowlist: List<DomainEntry> = emptyList(),
        val blocklist: List<DomainEntry> = emptyList(),
        val isLoading: Boolean = false,
        val lastError: String? = null,
        val lastOperation: Operation? = null
    )

    /**
     * Operations that can trigger feedback.
     */
    sealed class Operation {
        data class Added(val domain: String, val toAllowlist: Boolean) : Operation()
        data class Removed(val domain: String, val fromAllowlist: Boolean) : Operation()
        data class Cleared(val allowlist: Boolean) : Operation()
        data class Error(val message: String) : Operation()
    }

    // Private mutable state
    private val _state = MutableStateFlow(AllowlistState(isLoading = true))
    
    /**
     * Observable state for UI.
     * Updates automatically when allowlist/blocklist changes.
     */
    val state: StateFlow<AllowlistState> = _state.asStateFlow()

    init {
        // Load initial state from persistence
        scope.launch {
            loadFromPersistence()
        }
    }

    // =========================================================================
    // ALLOWLIST OPERATIONS
    // =========================================================================

    /**
     * Add a domain to the allowlist.
     * Persists first, then updates state.
     *
     * @param domain Domain to add (e.g., "example.com")
     * @return true if added successfully
     */
    fun addToAllowlist(domain: String): Boolean {
        val normalizedDomain = normalizeDomain(domain)
        if (normalizedDomain.isBlank()) {
            _state.update { it.copy(lastOperation = Operation.Error("Invalid domain")) }
            return false
        }

        scope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Check if already exists
                if (_state.value.allowlist.any { it.domain == normalizedDomain }) {
                    _state.update { it.copy(
                        isLoading = false,
                        lastOperation = Operation.Error("Domain already in allowlist")
                    )}
                    return@launch
                }

                val entry = DomainEntry(domain = normalizedDomain)

                // Persist first
                repository.addToAllowlist(entry)

                // Then update state
                _state.update { current ->
                    current.copy(
                        allowlist = current.allowlist + entry,
                        isLoading = false,
                        lastError = null,
                        lastOperation = Operation.Added(normalizedDomain, toAllowlist = true)
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    lastError = e.message,
                    lastOperation = Operation.Error(e.message ?: "Failed to add to allowlist")
                )}
            }
        }
        return true
    }

    /**
     * Remove a domain from the allowlist.
     *
     * @param domain Domain to remove
     */
    fun removeFromAllowlist(domain: String) {
        val normalizedDomain = normalizeDomain(domain)

        scope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Persist first
                repository.removeFromAllowlist(normalizedDomain)

                // Then update state
                _state.update { current ->
                    current.copy(
                        allowlist = current.allowlist.filter { it.domain != normalizedDomain },
                        isLoading = false,
                        lastError = null,
                        lastOperation = Operation.Removed(normalizedDomain, fromAllowlist = true)
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    lastError = e.message,
                    lastOperation = Operation.Error(e.message ?: "Failed to remove from allowlist")
                )}
            }
        }
    }

    /**
     * Check if a domain is in the allowlist.
     */
    fun isAllowlisted(domain: String): Boolean {
        val normalizedDomain = normalizeDomain(domain)
        return _state.value.allowlist.any { 
            it.domain == normalizedDomain || 
            (it.domain.startsWith("*.") && normalizedDomain.endsWith(it.domain.substring(1)))
        }
    }

    /**
     * Clear the entire allowlist.
     */
    fun clearAllowlist() {
        scope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                repository.clearAllowlist()

                _state.update { current ->
                    current.copy(
                        allowlist = emptyList(),
                        isLoading = false,
                        lastError = null,
                        lastOperation = Operation.Cleared(allowlist = true)
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    lastError = e.message,
                    lastOperation = Operation.Error(e.message ?: "Failed to clear allowlist")
                )}
            }
        }
    }

    // =========================================================================
    // BLOCKLIST OPERATIONS
    // =========================================================================

    /**
     * Add a domain to the blocklist.
     * Persists first, then updates state.
     *
     * @param domain Domain to block
     * @return true if added successfully
     */
    fun addToBlocklist(domain: String): Boolean {
        val normalizedDomain = normalizeDomain(domain)
        if (normalizedDomain.isBlank()) {
            _state.update { it.copy(lastOperation = Operation.Error("Invalid domain")) }
            return false
        }

        scope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Check if already exists
                if (_state.value.blocklist.any { it.domain == normalizedDomain }) {
                    _state.update { it.copy(
                        isLoading = false,
                        lastOperation = Operation.Error("Domain already in blocklist")
                    )}
                    return@launch
                }

                val entry = DomainEntry(domain = normalizedDomain)

                // Persist first
                repository.addToBlocklist(entry)

                // Then update state
                _state.update { current ->
                    current.copy(
                        blocklist = current.blocklist + entry,
                        isLoading = false,
                        lastError = null,
                        lastOperation = Operation.Added(normalizedDomain, toAllowlist = false)
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    lastError = e.message,
                    lastOperation = Operation.Error(e.message ?: "Failed to add to blocklist")
                )}
            }
        }
        return true
    }

    /**
     * Remove a domain from the blocklist.
     */
    fun removeFromBlocklist(domain: String) {
        val normalizedDomain = normalizeDomain(domain)

        scope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                repository.removeFromBlocklist(normalizedDomain)

                _state.update { current ->
                    current.copy(
                        blocklist = current.blocklist.filter { it.domain != normalizedDomain },
                        isLoading = false,
                        lastError = null,
                        lastOperation = Operation.Removed(normalizedDomain, fromAllowlist = false)
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    lastError = e.message,
                    lastOperation = Operation.Error(e.message ?: "Failed to remove from blocklist")
                )}
            }
        }
    }

    /**
     * Check if a domain is in the blocklist.
     */
    fun isBlocklisted(domain: String): Boolean {
        val normalizedDomain = normalizeDomain(domain)
        return _state.value.blocklist.any { 
            it.domain == normalizedDomain || 
            (it.domain.startsWith("*.") && normalizedDomain.endsWith(it.domain.substring(1)))
        }
    }

    /**
     * Clear the entire blocklist.
     */
    fun clearBlocklist() {
        scope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                repository.clearBlocklist()

                _state.update { current ->
                    current.copy(
                        blocklist = emptyList(),
                        isLoading = false,
                        lastError = null,
                        lastOperation = Operation.Cleared(allowlist = false)
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    lastError = e.message,
                    lastOperation = Operation.Error(e.message ?: "Failed to clear blocklist")
                )}
            }
        }
    }

    // =========================================================================
    // TOGGLE OPERATIONS (UI convenience methods)
    // =========================================================================

    /**
     * Toggle a domain's allowlist status.
     * If enabled, adds to allowlist. If disabled, removes.
     *
     * @param domain Domain to toggle
     * @param enabled New enabled state
     */
    fun setAllowlistEnabled(domain: String, enabled: Boolean) {
        if (enabled) {
            addToAllowlist(domain)
        } else {
            removeFromAllowlist(domain)
        }
    }

    /**
     * Toggle a domain's blocklist status.
     *
     * @param domain Domain to toggle
     * @param enabled New enabled state
     */
    fun setBlocklistEnabled(domain: String, enabled: Boolean) {
        if (enabled) {
            addToBlocklist(domain)
        } else {
            removeFromBlocklist(domain)
        }
    }

    /**
     * Clear last operation (for dismissing snackbars).
     */
    fun clearLastOperation() {
        _state.update { it.copy(lastOperation = null) }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private suspend fun loadFromPersistence() {
        try {
            val allowlist = repository.getAllowlist()
            val blocklist = repository.getBlocklist()

            _state.update { current ->
                current.copy(
                    allowlist = allowlist,
                    blocklist = blocklist,
                    isLoading = false,
                    lastError = null
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(
                isLoading = false,
                lastError = e.message
            )}
        }
    }

    private fun normalizeDomain(domain: String): String {
        return domain
            .trim()
            .lowercase()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("www.")
            .trimEnd('/')
    }
}

/**
 * Repository interface for allowlist/blocklist persistence.
 */
interface AllowlistRepository {
    suspend fun getAllowlist(): List<AllowlistManager.DomainEntry>
    suspend fun getBlocklist(): List<AllowlistManager.DomainEntry>
    suspend fun addToAllowlist(entry: AllowlistManager.DomainEntry)
    suspend fun removeFromAllowlist(domain: String)
    suspend fun clearAllowlist()
    suspend fun addToBlocklist(entry: AllowlistManager.DomainEntry)
    suspend fun removeFromBlocklist(domain: String)
    suspend fun clearBlocklist()
}

/**
 * In-memory implementation for testing.
 */
class InMemoryAllowlistRepository : AllowlistRepository {
    private val allowlist = mutableListOf<AllowlistManager.DomainEntry>()
    private val blocklist = mutableListOf<AllowlistManager.DomainEntry>()

    override suspend fun getAllowlist() = allowlist.toList()
    override suspend fun getBlocklist() = blocklist.toList()
    override suspend fun addToAllowlist(entry: AllowlistManager.DomainEntry) { allowlist.add(entry) }
    override suspend fun removeFromAllowlist(domain: String) { allowlist.removeAll { it.domain == domain } }
    override suspend fun clearAllowlist() { allowlist.clear() }
    override suspend fun addToBlocklist(entry: AllowlistManager.DomainEntry) { blocklist.add(entry) }
    override suspend fun removeFromBlocklist(domain: String) { blocklist.removeAll { it.domain == domain } }
    override suspend fun clearBlocklist() { blocklist.clear() }
}
