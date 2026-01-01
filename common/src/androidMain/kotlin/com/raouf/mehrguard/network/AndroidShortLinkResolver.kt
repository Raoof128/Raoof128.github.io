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

package com.raouf.mehrguard.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Android implementation of ShortLinkResolver.
 *
 * Uses HttpURLConnection to perform HEAD requests and follow redirects.
 * Only extracts Location header, never downloads body content.
 *
 * @author Mehr Guard Security Team
 * @since 1.3.0
 */
class AndroidShortLinkResolver : ShortLinkResolver {

    override suspend fun resolve(url: String): ShortLinkResolver.ResolveResult {
        // First check if it's actually a shortener
        if (!isResolvableShortener(url)) {
            return ShortLinkResolver.ResolveResult.NotShortener(url)
        }

        return withContext(Dispatchers.IO) {
            try {
                resolveWithRedirects(url)
            } catch (e: Exception) {
                ShortLinkResolver.ResolveResult.Failure(
                    originalUrl = url,
                    reason = e.message ?: "Unknown error"
                )
            }
        }
    }

    override fun isResolvableShortener(url: String): Boolean {
        val host = ShortLinkResolver.extractHost(url) ?: return false
        return ShortLinkResolver.isShortenerHost(host)
    }

    /**
     * Follow redirects using HEAD requests.
     */
    private fun resolveWithRedirects(startUrl: String): ShortLinkResolver.ResolveResult {
        var currentUrl = startUrl
        var redirectCount = 0

        while (redirectCount < ShortLinkResolver.MAX_REDIRECTS) {
            val connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                instanceFollowRedirects = false // We'll follow manually
                connectTimeout = ShortLinkResolver.TIMEOUT_MS.toInt()
                readTimeout = ShortLinkResolver.TIMEOUT_MS.toInt()
                setRequestProperty("User-Agent", "Mehr Guard/1.3.0 (URL Safety Check)")
            }

            try {
                connection.connect()
                val responseCode = connection.responseCode

                when {
                    // Success - no more redirects
                    responseCode in 200..299 -> {
                        return if (redirectCount > 0) {
                            ShortLinkResolver.ResolveResult.Success(
                                originalUrl = startUrl,
                                resolvedUrl = currentUrl,
                                redirectCount = redirectCount
                            )
                        } else {
                            // No redirect happened, return as-is
                            ShortLinkResolver.ResolveResult.NotShortener(startUrl)
                        }
                    }

                    // Redirect - follow it
                    responseCode in 300..399 -> {
                        val location = connection.getHeaderField("Location")
                        if (location.isNullOrBlank()) {
                            return ShortLinkResolver.ResolveResult.Failure(
                                originalUrl = startUrl,
                                reason = "Redirect without Location header"
                            )
                        }

                        // Handle relative URLs
                        currentUrl = if (location.startsWith("http")) {
                            location
                        } else {
                            URL(URL(currentUrl), location).toString()
                        }
                        redirectCount++
                    }

                    // Error
                    else -> {
                        return ShortLinkResolver.ResolveResult.Failure(
                            originalUrl = startUrl,
                            reason = "HTTP $responseCode"
                        )
                    }
                }
            } finally {
                connection.disconnect()
            }
        }

        // Max redirects exceeded
        return ShortLinkResolver.ResolveResult.Success(
            originalUrl = startUrl,
            resolvedUrl = currentUrl,
            redirectCount = redirectCount
        )
    }
}
