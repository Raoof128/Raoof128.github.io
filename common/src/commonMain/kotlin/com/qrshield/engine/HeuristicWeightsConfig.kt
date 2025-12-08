/*
 * Copyright 2024 QR-SHIELD Contributors
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
 * Configurable heuristic weights for the detection engine.
 * 
 * This configuration allows the detection engine to be tuned
 * without recompiling the application, enabling:
 * - A/B testing of different weight configurations
 * - Region-specific tuning (e.g., higher brand weights in AU)
 * - Dynamic updates via remote config
 * - Easy experimentation during development
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
data class HeuristicWeightsConfig(
    // Protocol checks
    val httpNotHttps: Int = 15,
    
    // Host checks
    val ipAddress: Int = 20,
    val urlShortener: Int = 8,
    val excessiveSubdomains: Int = 10,
    val nonStandardPort: Int = 8,
    
    // URL structure checks
    val longUrl: Int = 5,
    val highEntropy: Int = 12,
    
    // Path/Query checks
    val credentialParams: Int = 18,
    val encodedPayload: Int = 10,
    
    // Obfuscation checks
    val atSymbol: Int = 15,
    val multipleTlds: Int = 10,
    val punycode: Int = 15,
    val numericSubdomain: Int = 8,
    
    // File extension checks
    val riskyExtension: Int = 25,
    val doubleExtension: Int = 20,
    
    // Encoding checks
    val excessiveEncoding: Int = 8
) {
    companion object {
        /**
         * Default configuration optimized for general phishing detection.
         */
        val DEFAULT = HeuristicWeightsConfig()
        
        /**
         * Aggressive configuration with higher weights.
         * Use for high-security environments.
         */
        val AGGRESSIVE = HeuristicWeightsConfig(
            httpNotHttps = 20,
            ipAddress = 30,
            credentialParams = 25,
            punycode = 25,
            riskyExtension = 35,
            doubleExtension = 30
        )
        
        /**
         * Lenient configuration with lower weights.
         * Use to reduce false positives in trusted environments.
         */
        val LENIENT = HeuristicWeightsConfig(
            httpNotHttps = 10,
            ipAddress = 15,
            urlShortener = 5,
            excessiveSubdomains = 5,
            longUrl = 3
        )
        
        /**
         * Australian-tuned configuration.
         * Higher sensitivity to AU-specific threats.
         */
        val AUSTRALIA = HeuristicWeightsConfig(
            // Standard weights
            httpNotHttps = 15,
            ipAddress = 20,
            // AU banks are heavily targeted
            credentialParams = 22,
            // Delivery scams are very common in AU
            urlShortener = 12
        )
        
        /**
         * Parse configuration from JSON string.
         * Returns DEFAULT if parsing fails.
         */
        fun fromJson(json: String): HeuristicWeightsConfig {
            // Simple manual parsing for multiplatform compatibility
            return try {
                val httpNotHttps = extractInt(json, "httpNotHttps") ?: DEFAULT.httpNotHttps
                val ipAddress = extractInt(json, "ipAddress") ?: DEFAULT.ipAddress
                val urlShortener = extractInt(json, "urlShortener") ?: DEFAULT.urlShortener
                val excessiveSubdomains = extractInt(json, "excessiveSubdomains") ?: DEFAULT.excessiveSubdomains
                val nonStandardPort = extractInt(json, "nonStandardPort") ?: DEFAULT.nonStandardPort
                val credentialParams = extractInt(json, "credentialParams") ?: DEFAULT.credentialParams
                val punycode = extractInt(json, "punycode") ?: DEFAULT.punycode
                
                HeuristicWeightsConfig(
                    httpNotHttps = httpNotHttps,
                    ipAddress = ipAddress,
                    urlShortener = urlShortener,
                    excessiveSubdomains = excessiveSubdomains,
                    nonStandardPort = nonStandardPort,
                    credentialParams = credentialParams,
                    punycode = punycode
                )
            } catch (e: Exception) {
                DEFAULT
            }
        }
        
        private fun extractInt(json: String, key: String): Int? {
            val pattern = """"$key"\s*:\s*(\d+)"""
            val regex = Regex(pattern)
            val match = regex.find(json)
            return match?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }
    
    /**
     * Validate that all weights are within reasonable bounds.
     */
    fun validate(): Boolean {
        val allWeights = listOf(
            httpNotHttps, ipAddress, urlShortener, excessiveSubdomains,
            nonStandardPort, longUrl, highEntropy, credentialParams,
            encodedPayload, atSymbol, multipleTlds, punycode,
            numericSubdomain, riskyExtension, doubleExtension, excessiveEncoding
        )
        
        return allWeights.all { it in 0..50 }
    }
    
    /**
     * Export configuration to JSON string.
     */
    fun toJson(): String {
        return """
            {
                "httpNotHttps": $httpNotHttps,
                "ipAddress": $ipAddress,
                "urlShortener": $urlShortener,
                "excessiveSubdomains": $excessiveSubdomains,
                "nonStandardPort": $nonStandardPort,
                "longUrl": $longUrl,
                "highEntropy": $highEntropy,
                "credentialParams": $credentialParams,
                "encodedPayload": $encodedPayload,
                "atSymbol": $atSymbol,
                "multipleTlds": $multipleTlds,
                "punycode": $punycode,
                "numericSubdomain": $numericSubdomain,
                "riskyExtension": $riskyExtension,
                "doubleExtension": $doubleExtension,
                "excessiveEncoding": $excessiveEncoding
            }
        """.trimIndent()
    }
}
