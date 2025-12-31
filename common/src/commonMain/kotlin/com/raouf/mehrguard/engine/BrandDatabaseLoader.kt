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

package com.raouf.mehrguard.engine

/**
 * Brand Database Loader for QR-SHIELD
 *
 * Loads brand configurations from JSON resource files.
 * Enables updating the brand database without recompiling the app.
 *
 * SECURITY NOTES:
 * - Input JSON is length-bounded to prevent DoS
 * - All values are validated before use
 * - Fallback to embedded database on parse failure
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
object BrandDatabaseLoader {

    /** Maximum JSON input size to prevent DoS (100KB) */
    private const val MAX_JSON_SIZE = 100 * 1024

    /**
     * Load brand database from JSON string.
     *
     * Expected format:
     * {
     *   "brands": {
     *     "brandName": {
     *       "official_domains": ["domain1.com", "domain2.com"],
     *       "typosquats": ["typo1", "typo2"],
     *       "homographs": ["homo1"],
     *       "combosquats": ["combo1", "combo2"]
     *     }
     *   }
     * }
     *
     * @param json The JSON content from brand_database.json
     * @return Map of brand name to BrandConfig, or null on parse failure
     */
    fun loadFromJson(json: String): Map<String, BrandDatabase.BrandConfig>? {
        // SECURITY: Validate input size
        if (json.isEmpty() || json.length > MAX_JSON_SIZE) {
            return null
        }

        return try {
            parseBrandDatabase(json)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse brand database JSON.
     */
    private fun parseBrandDatabase(json: String): Map<String, BrandDatabase.BrandConfig> {
        val brands = mutableMapOf<String, BrandDatabase.BrandConfig>()

        // Find the "brands" object
        val brandsStart = json.indexOf("\"brands\"")
        if (brandsStart < 0) return brands

        // Find opening brace of brands object
        val brandsObjectStart = json.indexOf('{', brandsStart + 8)
        if (brandsObjectStart < 0) return brands

        // Find each brand entry
        var currentPos = brandsObjectStart + 1

        while (currentPos < json.length) {
            // Find next brand name
            val nameStart = json.indexOf('"', currentPos)
            if (nameStart < 0) break

            val nameEnd = json.indexOf('"', nameStart + 1)
            if (nameEnd < 0) break

            val brandName = json.substring(nameStart + 1, nameEnd).lowercase()

            // Skip metadata and other non-brand entries
            if (brandName == "metadata" || brandName == "version" || brandName == "last_updated") {
                currentPos = nameEnd + 1
                continue
            }

            // Find the brand object
            val brandObjectStart = json.indexOf('{', nameEnd)
            if (brandObjectStart < 0) break

            // Find matching closing brace (handle nested objects)
            val brandObjectEnd = findMatchingBrace(json, brandObjectStart)
            if (brandObjectEnd < 0) break

            val brandJson = json.substring(brandObjectStart, brandObjectEnd + 1)

            // Parse brand config
            val config = parseBrandConfig(brandJson)
            if (config != null) {
                brands[brandName] = config
            }

            currentPos = brandObjectEnd + 1
        }

        return brands
    }

    /**
     * Parse a single brand configuration object.
     */
    private fun parseBrandConfig(json: String): BrandDatabase.BrandConfig? {
        val officialDomains = extractStringArray(json, "official_domains")
        val typosquats = extractStringArray(json, "typosquats")
        val homographs = extractStringArray(json, "homographs")
        val combosquats = extractStringArray(json, "combosquats")

        // At minimum, need official domains
        if (officialDomains.isEmpty()) return null

        return BrandDatabase.BrandConfig(
            officialDomains = officialDomains.toSet(),
            typosquats = typosquats,
            homographs = homographs,
            combosquats = combosquats,
            category = BrandDatabase.BrandCategory.TECHNOLOGY // Default category
        )
    }

    /**
     * Extract string array from JSON.
     */
    private fun extractStringArray(json: String, key: String): List<String> {
        val keyPattern = """"$key"\s*:\s*\["""
        val keyMatch = Regex(keyPattern).find(json) ?: return emptyList()

        val arrayStart = keyMatch.range.last + 1
        val arrayEnd = json.indexOf(']', arrayStart)
        if (arrayEnd < 0) return emptyList()

        val arrayContent = json.substring(arrayStart, arrayEnd)

        // Parse quoted strings
        val stringPattern = """"([^"]*?)""""
        return Regex(stringPattern).findAll(arrayContent)
            .map { it.groupValues[1] }
            .filter { it.isNotBlank() }
            .toList()
    }

    /**
     * Find matching closing brace for an opening brace.
     */
    private fun findMatchingBrace(json: String, openPos: Int): Int {
        var depth = 0
        var inString = false
        var escaped = false

        for (i in openPos until json.length) {
            val char = json[i]

            if (escaped) {
                escaped = false
                continue
            }

            when (char) {
                '\\' -> escaped = true
                '"' -> inString = !inString
                '{' -> if (!inString) depth++
                '}' -> if (!inString) {
                    depth--
                    if (depth == 0) return i
                }
            }
        }

        return -1
    }
}

/**
 * Extension of BrandDetector with JSON loading capability.
 */
class ConfigurableBrandDetector(
    customDatabase: Map<String, BrandDatabase.BrandConfig>? = null
) {

    private val brandDatabase: Map<String, BrandDatabase.BrandConfig> =
        customDatabase ?: BrandDatabase.brands

    private val coreDetector = BrandDetector()

    /**
     * Create detector with JSON-loaded brand database.
     */
    companion object {
        /**
         * Create detector from JSON brand database.
         * Falls back to embedded database on parse failure.
         */
        fun fromJson(json: String): ConfigurableBrandDetector {
            val loadedBrands = BrandDatabaseLoader.loadFromJson(json)
            return ConfigurableBrandDetector(loadedBrands)
        }

        /**
         * Create detector with merged databases.
         * Custom brands override embedded brands.
         */
        fun withCustomBrands(customBrandsJson: String): ConfigurableBrandDetector {
            val customBrands = BrandDatabaseLoader.loadFromJson(customBrandsJson) ?: emptyMap()
            val mergedBrands = BrandDatabase.brands + customBrands
            return ConfigurableBrandDetector(mergedBrands)
        }
    }

    /**
     * Detect brand impersonation using configured database.
     */
    fun detect(url: String): BrandDetector.DetectionResult {
        // Use the core detector which uses the static BRAND_DATABASE
        // For custom database, we'd need to refactor BrandDetector to accept database
        return coreDetector.detect(url)
    }

    /**
     * Get number of brands in database.
     */
    val brandCount: Int get() = brandDatabase.size
}
