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

package com.qrshield.engine

/**
 * Static Redirect Pattern Analyzer for QR-SHIELD
 *
 * ⚠️ **IMPORTANT CLARIFICATION FOR SECURITY EXPERTS:**
 * This class performs **STATIC PATTERN ANALYSIS** on URL strings.
 * It does **NOT** actually follow HTTP redirects (301, 302, etc.).
 *
 * We cannot follow real redirects because:
 * 1. QR-SHIELD is offline-first (no network requests)
 * 2. Server-side redirects (bit.ly → actual-site.com) require network
 * 3. Privacy: we don't want to contact untrusted servers
 *
 * ## What We Actually Do
 *
 * We detect **patterns that INDICATE redirect chains** in the URL string:
 * - URL shortener domains (bit.ly, t.co, etc.)
 * - Embedded URLs in query parameters (?url=https://evil.com)
 * - Common redirect parameter names (redirect=, goto=, next=)
 * - Double URL encoding (potential obfuscation)
 * - Known tracking/redirect services
 *
 * ## What We DON'T Do
 *
 * - Follow actual HTTP 301/302 redirects
 * - Contact any external server
 * - Resolve shortened URLs
 * - Verify final destination
 *
 * ## Attack Pattern
 *
 * Phishers use multiple redirects to:
 * 1. Evade URL blocklists (each hop is different)
 * 2. Track victim engagement
 * 3. Present different content based on location/device
 * 4. Delay detection by rotating destinations
 *
 * ## Detection Approach (Static Analysis)
 *
 * We flag URLs that **appear to use redirect patterns**:
 * - "This URL uses a known shortener" (we can't tell WHERE it redirects)
 * - "This URL has an embedded URL in a parameter"
 * - "This URL has multiple redirect indicators"
 *
 * The warning is: "This URL may redirect - we can't verify the destination offline."
 *
 * @author QR-SHIELD Security Team
 * @since 1.1.0
 */
class RedirectChainSimulator {

    /**
     * Result of redirect chain analysis.
     *
     * @property hasRedirectIndicators True if URL shows redirect chain patterns
     * @property score Risk score contribution (0-40)
     * @property chain Simulated redirect chain for display
     * @property warnings Human-readable warnings about the chain
     */
    data class RedirectAnalysis(
        val hasRedirectIndicators: Boolean,
        val score: Int,
        val chain: List<RedirectHop>,
        val warnings: List<String>
    )

    /**
     * Represents a hop in a simulated redirect chain.
     *
     * @property url The URL at this hop
     * @property type Type of redirect (shortener, tracker, parameter, etc.)
     * @property risk Risk level (LOW, MEDIUM, HIGH)
     */
    data class RedirectHop(
        val url: String,
        val type: HopType,
        val risk: RiskLevel
    )

    enum class HopType {
        INITIAL,        // The starting URL
        SHORTENER,      // URL shortener service
        TRACKER,        // Known tracking/analytics redirect
        EMBEDDED,       // URL embedded in query parameter
        UNKNOWN         // Unknown destination
    }

    enum class RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Analyze a URL for redirect chain indicators.
     *
     * @param url The URL to analyze
     * @return Redirect chain analysis results
     */
    fun analyze(url: String): RedirectAnalysis {
        val warnings = mutableListOf<String>()
        val chain = mutableListOf<RedirectHop>()
        var score = 0

        // Add initial hop
        chain.add(RedirectHop(
            url = url.take(80),
            type = HopType.INITIAL,
            risk = RiskLevel.LOW
        ))

        // Check for URL shorteners
        val shortenerResult = checkShortener(url)
        if (shortenerResult != null) {
            chain.add(shortenerResult)
            warnings.add("⚠️ URL shortener detected - destination hidden")
            score += 15
        }

        // Check for embedded URLs in parameters
        val embeddedUrls = extractEmbeddedUrls(url)
        embeddedUrls.forEach { embeddedUrl ->
            chain.add(RedirectHop(
                url = embeddedUrl.take(80),
                type = HopType.EMBEDDED,
                risk = RiskLevel.HIGH
            ))
            warnings.add("🔴 Embedded URL found: redirects to hidden destination")
            score += 20
        }

        // Check for redirect parameters
        val redirectParams = checkRedirectParams(url)
        if (redirectParams.isNotEmpty()) {
            warnings.add("⚠️ Redirect parameters detected: ${redirectParams.joinToString(", ")}")
            score += 10
        }

        // Check for tracking services
        if (isTracker(url)) {
            chain.add(RedirectHop(
                url = "→ [Tracking Service]",
                type = HopType.TRACKER,
                risk = RiskLevel.MEDIUM
            ))
            warnings.add("📊 Tracking redirect detected - your click may be monitored")
            score += 5
        }

        // Check for double encoding
        if (hasDoubleEncoding(url)) {
            warnings.add("⚠️ Double URL encoding detected - may hide malicious content")
            score += 15
        }

        // Add unknown destination if chain detected
        if (chain.size > 1) {
            chain.add(RedirectHop(
                url = "→ [Unknown Final Destination]",
                type = HopType.UNKNOWN,
                risk = RiskLevel.CRITICAL
            ))
        }

        return RedirectAnalysis(
            hasRedirectIndicators = chain.size > 1,
            score = score.coerceAtMost(40),
            chain = chain,
            warnings = warnings
        )
    }

    /**
     * Check if URL uses a known shortener service.
     */
    private fun checkShortener(url: String): RedirectHop? {
        val shorteners = setOf(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly",
            "is.gd", "buff.ly", "adf.ly", "j.mp", "tr.im",
            "short.link", "cutt.ly", "rb.gy", "shorturl.at",
            "rebrand.ly", "bl.ink", "soo.gd", "s.id", "tiny.cc",
            "clck.ru", "bc.vc", "po.st", "mcaf.ee", "u.to"
        )

        val urlLower = url.lowercase()
        for (shortener in shorteners) {
            if (urlLower.contains(shortener)) {
                return RedirectHop(
                    url = "→ [$shortener]",
                    type = HopType.SHORTENER,
                    risk = RiskLevel.MEDIUM
                )
            }
        }

        return null
    }

    /**
     * Extract embedded URLs from query parameters.
     */
    private fun extractEmbeddedUrls(url: String): List<String> {
        val embedded = mutableListOf<String>()

        // Look for URL-like patterns in the URL
        val urlPattern = Regex("https?://[^&\\s]+")

        // Find URLs that appear after query parameter markers
        val queryStart = url.indexOf('?')
        if (queryStart > 0) {
            val query = url.substring(queryStart + 1)

            // Check for URL-encoded URLs
            val decodedQuery = try {
                urlDecode(query)
            } catch (e: Exception) {
                query
            }

            urlPattern.findAll(decodedQuery).forEach { match ->
                // Avoid matching the original URL
                if (!url.startsWith(match.value)) {
                    embedded.add(match.value)
                }
            }
        }

        return embedded.take(3) // Limit to 3 for display
    }

    /**
     * Check for common redirect parameters.
     */
    private fun checkRedirectParams(url: String): List<String> {
        val redirectParams = listOf(
            "redirect", "redirect_uri", "redirect_url",
            "url", "uri", "link", "goto", "next", "target",
            "continue", "dest", "destination", "return",
            "return_url", "callback", "forward", "to"
        )

        val urlLower = url.lowercase()
        return redirectParams.filter { param ->
            urlLower.contains("$param=") || urlLower.contains("&$param=")
        }
    }

    /**
     * Check if URL uses a known tracking service.
     */
    private fun isTracker(url: String): Boolean {
        val trackers = setOf(
            "click.", "track.", "go.", "redirect.", "r.",
            "analytics.", "pixel.", "beacon.", "metrics.",
            "mailchi.mp", "sendgrid.net", "mailgun.org",
            "constantcontact.com", "aweber.com"
        )

        val urlLower = url.lowercase()
        return trackers.any { tracker -> urlLower.contains(tracker) }
    }

    /**
     * Check for double URL encoding.
     */
    private fun hasDoubleEncoding(url: String): Boolean {
        // Look for patterns like %25XX (encoded %)
        return url.contains("%25") ||
               url.contains("%252F") || // Encoded /
               url.contains("%253A")    // Encoded :
    }

    /**
     * Simple URL decoder for common characters.
     */
    private fun urlDecode(input: String): String {
        return input
            .replace("%3A", ":")
            .replace("%2F", "/")
            .replace("%3F", "?")
            .replace("%3D", "=")
            .replace("%26", "&")
            .replace("%3a", ":")
            .replace("%2f", "/")
            .replace("%3f", "?")
            .replace("%3d", "=")
            .replace("%26", "&")
    }

    companion object {
        /**
         * Quick check if URL likely has redirects.
         *
         * Use this for fast pre-screening before full analysis.
         */
        fun quickCheck(url: String): Boolean {
            val indicators = listOf("redirect", "url=", "goto=", "bit.ly", "t.co", "goo.gl")
            val urlLower = url.lowercase()
            return indicators.any { urlLower.contains(it) }
        }
    }
}
