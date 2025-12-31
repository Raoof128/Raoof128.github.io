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

package com.raouf.mehrguard.android.ota

import android.content.Context
import android.util.Log
import com.raouf.mehrguard.ota.OtaHttpClient
import com.raouf.mehrguard.ota.OtaStorage
import com.raouf.mehrguard.ota.OtaUpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Android implementation of OTA storage using filesDir.
 *
 * Files are stored in the app's internal storage directory,
 * which is private and survives app updates.
 */
class AndroidOtaStorage(private val context: Context) : OtaStorage {

    private val TAG = "AndroidOtaStorage"
    private val OTA_DIR = "ota_cache"

    private fun getOtaDir(): File {
        val dir = File(context.filesDir, OTA_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    override fun read(filename: String): String? {
        return try {
            val file = File(getOtaDir(), filename)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read $filename: ${e.message}")
            null
        }
    }

    override fun write(filename: String, content: String) {
        try {
            val file = File(getOtaDir(), filename)
            file.writeText(content)
            Log.d(TAG, "Wrote ${content.length} bytes to $filename")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write $filename: ${e.message}")
        }
    }

    override fun exists(filename: String): Boolean {
        return File(getOtaDir(), filename).exists()
    }

    override fun delete(filename: String) {
        try {
            File(getOtaDir(), filename).delete()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete $filename: ${e.message}")
        }
    }
}

/**
 * Android implementation of OTA HTTP client using HttpURLConnection.
 *
 * Uses HttpURLConnection for compatibility without adding dependencies.
 * Could be replaced with OkHttp for more features.
 */
class AndroidOtaHttpClient : OtaHttpClient {

    companion object {
        private const val TAG = "AndroidOtaHttpClient"
        private const val CONNECT_TIMEOUT = 10_000 // 10 seconds
        private const val READ_TIMEOUT = 30_000 // 30 seconds
        private const val MAX_RESPONSE_SIZE = 500 * 1024 // 500KB
    }

    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Fetching: $url")

        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "MehrGuard-Android/1.3.0")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw RuntimeException("HTTP $responseCode")
            }

            // Check content length
            val contentLength = connection.contentLength
            if (contentLength > MAX_RESPONSE_SIZE) {
                throw RuntimeException("Response too large: $contentLength bytes")
            }

            val response = connection.inputStream.bufferedReader().readText()

            // Validate size after reading (in case Content-Length was missing)
            if (response.length > MAX_RESPONSE_SIZE) {
                throw RuntimeException("Response too large: ${response.length} chars")
            }

            Log.d(TAG, "Received ${response.length} chars from $url")
            response
        } finally {
            connection.disconnect()
        }
    }
}

/**
 * Factory for creating Android-specific OTA components.
 */
object AndroidOtaFactory {

    /**
     * Create an OTAUpdateManager configured for Android.
     */
    fun createUpdateManager(context: Context): OtaUpdateManager {
        return OtaUpdateManager(
            storage = AndroidOtaStorage(context.applicationContext),
            httpClient = AndroidOtaHttpClient()
        )
    }
}
