/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

package com.raouf.mehrguard.ota

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * OTA (Over-the-Air) Update Manager for the Living Engine
 *
 * Manages remote updates for the detection engine's databases:
 * - Brand database (brand_db_v2.json)
 * - Heuristics weights (heuristics_v2.json)
 *
 * ## Why This Matters
 * The "Static Database" criticism is that security apps become stale without updates.
 * This Living Engine fetches fresh detection rules from GitHub Pages, allowing the
 * app to stay current even between app store releases.
 *
 * ## Architecture
 * - **Versioning**: Server hosts `version.json` with current version numbers
 * - **Offline-First**: App works with bundled data if network unavailable
 * - **Cache Layer**: Downloaded updates saved to local storage
 * - **Priority Loading**: Cached > Bundled resources
 *
 * ## Security Considerations
 * - HTTPS-only connections (TLS ensures transport integrity)
 * - Version comparison prevents downgrade attacks
 * - Max file size limits to prevent DoS
 *
 * **Design Decision (Checksum Verification):**
 * SHA-256 verification was evaluated but deemed unnecessary for competition:
 * 1. HTTPS already provides integrity via TLS certificate chain
 * 2. GitHub Pages is a trusted origin with built-in CDN integrity
 * 3. Adding crypto dependencies would bloat the multiplatform bundle
 * Future production deployments may add Ed25519 signatures for supply chain security.
 *
 * @see OtaStorage for platform-specific storage implementation
 *
 * @author Mehr Guard Security Team
 * @since 1.3.0
 */
class OtaUpdateManager(
    private val storage: OtaStorage,
    private val httpClient: OtaHttpClient
) {

    companion object {
        /** Base URL for OTA updates (GitHub Pages) */
        const val UPDATE_BASE_URL = "https://raoof128.github.io/QDKMP-KotlinConf-2026-/data/updates"

        /** Version manifest filename */
        const val VERSION_MANIFEST = "version.json"

        /** Maximum file size for downloads (500KB) */
        const val MAX_FILE_SIZE = 500 * 1024

        /** Current bundled version */
        const val BUNDLED_VERSION = 1

        /** Cache file names */
        const val CACHED_BRAND_DB = "brand_db_cached.json"
        const val CACHED_HEURISTICS = "heuristics_cached.json"
        const val CACHED_VERSION = "version_cached.json"
    }

    /**
     * Update state for tracking progress
     */
    sealed class UpdateState {
        data object Idle : UpdateState()
        data object Checking : UpdateState()
        data object Downloading : UpdateState()
        data class Success(val version: Int) : UpdateState()
        data class Error(val message: String) : UpdateState()
        data object NoUpdateNeeded : UpdateState()
    }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _currentVersion = MutableStateFlow(BUNDLED_VERSION)
    val currentVersion: StateFlow<Int> = _currentVersion.asStateFlow()

    /**
     * Check for and apply updates.
     *
     * Call this from Application.onCreate() in a background coroutine.
     */
    suspend fun checkAndUpdate() {
        _updateState.value = UpdateState.Checking

        try {
            // 1. Load cached version if available
            loadCachedVersion()

            // 2. Fetch remote version manifest
            val manifest = fetchVersionManifest()
            if (manifest == null) {
                _updateState.value = UpdateState.NoUpdateNeeded
                return
            }

            // 3. Compare versions
            val remoteVersion = parseVersion(manifest)
            if (remoteVersion <= _currentVersion.value) {
                _updateState.value = UpdateState.NoUpdateNeeded
                return
            }

            // 4. Download updates
            _updateState.value = UpdateState.Downloading

            val brandDbSuccess = downloadAndCache(
                filename = parseBrandDbFilename(manifest),
                cacheFile = CACHED_BRAND_DB
            )

            val heuristicsSuccess = downloadAndCache(
                filename = parseHeuristicsFilename(manifest),
                cacheFile = CACHED_HEURISTICS
            )

            // 5. Save version if any update succeeded
            if (brandDbSuccess || heuristicsSuccess) {
                storage.write(CACHED_VERSION, manifest)
                _currentVersion.value = remoteVersion
                _updateState.value = UpdateState.Success(remoteVersion)
            } else {
                _updateState.value = UpdateState.Error("Download failed")
            }

        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Get cached brand database JSON, or null if not available.
     */
    fun getCachedBrandDb(): String? {
        return storage.read(CACHED_BRAND_DB)
    }

    /**
     * Get cached heuristics JSON, or null if not available.
     */
    fun getCachedHeuristics(): String? {
        return storage.read(CACHED_HEURISTICS)
    }

    /**
     * Check if cached data is available.
     */
    fun hasCachedData(): Boolean {
        return storage.exists(CACHED_BRAND_DB) || storage.exists(CACHED_HEURISTICS)
    }

    /**
     * Clear all cached data (for testing/reset).
     */
    fun clearCache() {
        storage.delete(CACHED_BRAND_DB)
        storage.delete(CACHED_HEURISTICS)
        storage.delete(CACHED_VERSION)
        _currentVersion.value = BUNDLED_VERSION
    }

    // === PRIVATE METHODS ===

    private fun loadCachedVersion() {
        val cached = storage.read(CACHED_VERSION)
        if (cached != null) {
            _currentVersion.value = parseVersion(cached)
        }
    }

    private suspend fun fetchVersionManifest(): String? {
        return try {
            val url = "$UPDATE_BASE_URL/$VERSION_MANIFEST"
            val response = httpClient.get(url)

            if (response.length > MAX_FILE_SIZE) {
                null // File too large, reject
            } else {
                response
            }
        } catch (e: Exception) {
            null // Network error, use cached/bundled
        }
    }

    private suspend fun downloadAndCache(filename: String, cacheFile: String): Boolean {
        return try {
            val url = "$UPDATE_BASE_URL/$filename"
            val content = httpClient.get(url)

            if (content.length > MAX_FILE_SIZE) {
                return false // File too large
            }

            storage.write(cacheFile, content)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Parse version number from manifest JSON.
     * Simple extraction without full JSON parsing.
     */
    private fun parseVersion(json: String): Int {
        // Simple string-based extraction for multiplatform compatibility
        val versionPattern = """"version"\s*:\s*(\d+)"""
        val regex = Regex(versionPattern)
        val matchResult = regex.find(json) ?: return BUNDLED_VERSION
        val groups = matchResult.groups
        val versionGroup = groups[1] ?: return BUNDLED_VERSION
        return versionGroup.value.toIntOrNull() ?: BUNDLED_VERSION
    }

    private fun parseBrandDbFilename(manifest: String): String {
        // Look for filename in brand_db section
        val pattern = """"brand_db"[^}]*"filename"\s*:\s*"([^"]+)""""
        val regex = Regex(pattern)
        val matchResult = regex.find(manifest) ?: return "brand_db_v2.json"
        val groups = matchResult.groups
        val filenameGroup = groups[1] ?: return "brand_db_v2.json"
        return filenameGroup.value
    }

    private fun parseHeuristicsFilename(manifest: String): String {
        // Look for filename in heuristics section
        val pattern = """"heuristics"[^}]*"filename"\s*:\s*"([^"]+)""""
        val regex = Regex(pattern)
        val matchResult = regex.find(manifest) ?: return "heuristics_v2.json"
        val groups = matchResult.groups
        val filenameGroup = groups[1] ?: return "heuristics_v2.json"
        return filenameGroup.value
    }
}

/**
 * Platform-specific storage interface for OTA files.
 *
 * Each platform implements this to store/retrieve cached update files:
 * - Android: Context.filesDir
 * - iOS: NSDocumentDirectory  
 * - Desktop: User home directory
 * - Web: localStorage
 */
interface OtaStorage {
    /**
     * Read file contents from cache.
     * @return File contents or null if not found
     */
    fun read(filename: String): String?

    /**
     * Write content to cache file.
     */
    fun write(filename: String, content: String)

    /**
     * Check if cached file exists.
     */
    fun exists(filename: String): Boolean

    /**
     * Delete cached file.
     */
    fun delete(filename: String)
}

/**
 * HTTP client interface for OTA downloads.
 *
 * Each platform provides its own HTTP implementation:
 * - Android: OkHttp or HttpURLConnection
 * - iOS: NSURLSession
 * - Desktop: java.net.HttpURLConnection
 * - Web: fetch() API
 */
interface OtaHttpClient {
    /**
     * Perform HTTP GET request.
     * @param url The URL to fetch
     * @return Response body as string
     * @throws Exception on network error
     */
    suspend fun get(url: String): String
}
