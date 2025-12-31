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

package com.raouf.mehrguard.network

import com.raouf.mehrguard.core.UrlAnalyzer

/**
 * Short Link Resolver for "Aggressive Mode"
 *
 * Resolves URL shorteners (bit.ly, t.co, etc.) by performing HTTP HEAD requests
 * to extract the final destination URL without downloading the body.
 *
 * ## Privacy Considerations
 * - This is an OPT-IN feature that requires network access
 * - Only performs HTTP HEAD (no body download)
 * - Only resolves KNOWN shortener domains
 * - User must explicitly enable in settings
 *
 * ## Security Considerations
 * - Max 5 redirects to prevent infinite loops
 * - 5-second timeout per request
 * - Only HTTPS final destinations are trusted
 * - Validates resolved URL before analysis
 *
 * @author QR-SHIELD Security Team
 * @since 1.3.0
 */
interface ShortLinkResolver {

    /**
     * Result of short link resolution.
     */
    sealed class ResolveResult {
        /**
         * Successfully resolved to final URL.
         * @param originalUrl The short URL that was resolved
         * @param resolvedUrl The final destination URL
         * @param redirectCount Number of redirects followed
         */
        data class Success(
            val originalUrl: String,
            val resolvedUrl: String,
            val redirectCount: Int
        ) : ResolveResult()

        /**
         * Resolution failed.
         * @param originalUrl The URL that failed to resolve
         * @param reason Human-readable failure reason
         */
        data class Failure(
            val originalUrl: String,
            val reason: String
        ) : ResolveResult()

        /**
         * URL is not a shortener, no resolution needed.
         */
        data class NotShortener(val url: String) : ResolveResult()
    }

    /**
     * Resolve a short URL to its final destination.
     *
     * Performs HTTP HEAD request following up to MAX_REDIRECTS.
     * Only extracts Location header, never downloads body.
     *
     * @param url The URL to resolve
     * @return ResolveResult indicating success, failure, or not a shortener
     */
    suspend fun resolve(url: String): ResolveResult

    /**
     * Check if a URL is a known shortener that can be resolved.
     */
    fun isResolvableShortener(url: String): Boolean

    companion object {
        /** Maximum number of redirects to follow */
        const val MAX_REDIRECTS = 5

        /** Timeout per request in milliseconds */
        const val TIMEOUT_MS = 5000L

        /** Known URL shortener domains */
        val SHORTENER_DOMAINS = setOf(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly",
            "is.gd", "buff.ly", "adf.ly", "j.mp", "tr.im",
            "short.link", "cutt.ly", "rb.gy", "shorturl.at",
            "tiny.cc", "shorte.st", "v.gd", "clicky.me",
            "rebrand.ly", "bl.ink", "soo.gd", "s.id",
            "clck.ru", "bc.vc", "po.st", "mcaf.ee", "u.to"
        )

        /**
         * Check if host is a known shortener.
         */
        fun isShortenerHost(host: String): Boolean {
            val hostLower = host.lowercase()
            return SHORTENER_DOMAINS.any { shortener ->
                hostLower == shortener || hostLower.endsWith(".$shortener")
            }
        }

        /**
         * Extract host from URL for shortener checking.
         */
        fun extractHost(url: String): String? {
            val urlAnalyzer = UrlAnalyzer()
            return urlAnalyzer.parse(url)?.host
        }
    }
}

/**
 * No-op resolver for when aggressive mode is disabled.
 *
 * This is the default implementation that preserves privacy.
 */
class NoOpShortLinkResolver : ShortLinkResolver {
    override suspend fun resolve(url: String): ShortLinkResolver.ResolveResult {
        return ShortLinkResolver.ResolveResult.NotShortener(url)
    }

    override fun isResolvableShortener(url: String): Boolean = false
}
