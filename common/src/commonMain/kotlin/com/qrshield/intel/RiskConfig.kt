/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 * Licensed under the Apache License, Version 2.0
 */

package com.qrshield.intel

/**
 * Risk configuration for weight calibration.
 *
 * All heuristic weights are externalized here for:
 * - Easy tuning based on evaluation harness results
 * - A/B testing different weight sets
 * - Version-locked configurations
 *
 * ## Weight Tuning Process
 * 1. Run evaluation harness with current weights
 * 2. Analyze precision/recall/F1
 * 3. Adjust weights (increase weight = more false positives, better recall)
 * 4. Re-run harness, lock config version
 * 5. Add regression gate in CI
 *
 * @author QR-SHIELD Security Team
 * @since 1.19.0
 */
data class RiskConfig(
    val version: String,
    val weights: WeightConfig,
    val thresholds: ThresholdConfig,
    val features: FeatureConfig
) {
    /**
     * Heuristic weights for scoring.
     */
    data class WeightConfig(
        // Protocol weights
        val httpNotHttps: Int = 30,
        
        // Host weights
        val ipAddressHost: Int = 50,
        val urlShortener: Int = 15,
        val excessiveSubdomains: Int = 10,
        val nonStandardPort: Int = 15,
        val suspiciousPort: Int = 25,
        
        // URL structure weights
        val longUrl: Int = 10,
        val highEntropyHost: Int = 20,
        
        // Path weights
        val suspiciousPathKeywords: Int = 5, // per keyword, max 20
        val riskyExtension: Int = 40,
        val doubleExtension: Int = 40,
        
        // Query weights
        val credentialParams: Int = 40,
        val encodedPayload: Int = 30,
        val excessiveEncoding: Int = 20,
        
        // Obfuscation weights
        val atSymbolInjection: Int = 60,
        val multipleTldSegments: Int = 25,
        val punycodeDomain: Int = 30,
        val numericSubdomain: Int = 20,
        
        // Unicode weights
        val zeroWidthChars: Int = 50,
        val mixedScript: Int = 45,
        val lookalikeChars: Int = 35,
        
        // Scheme weights
        val dataUri: Int = 60,
        val javascriptUrl: Int = 70,
        val fragmentHiding: Int = 25,
        
        // Intel weights
        val knownBadDomain: Int = 90,
        val domainAgePattern: Int = 20,
        val credentialKeywords: Int = 10 // per keyword, max 40
    )

    /**
     * Verdict thresholds.
     */
    data class ThresholdConfig(
        val safeMax: Int = 30,
        val suspiciousMax: Int = 70,
        // Above suspiciousMax = MALICIOUS
        
        val entropyThreshold: Double = 4.0,
        val maxUrlLength: Int = 2048,
        val maxSubdomainDepth: Int = 10
    )

    /**
     * Feature flags for enabling/disabling checks.
     */
    data class FeatureConfig(
        val enableBloomFilter: Boolean = true,
        val enableUnicodeAnalysis: Boolean = true,
        val enablePslLookup: Boolean = true,
        val enableBrandDetection: Boolean = true,
        val enableTldScoring: Boolean = true,
        val enableMlScoring: Boolean = true
    )

    /**
     * Get weight for a specific heuristic by name.
     */
    fun getWeight(name: String): Int = when (name) {
        "HTTP_NOT_HTTPS" -> weights.httpNotHttps
        "IP_ADDRESS_HOST" -> weights.ipAddressHost
        "URL_SHORTENER" -> weights.urlShortener
        "EXCESSIVE_SUBDOMAINS" -> weights.excessiveSubdomains
        "NON_STANDARD_PORT" -> weights.nonStandardPort
        "SUSPICIOUS_PORT" -> weights.suspiciousPort
        "LONG_URL" -> weights.longUrl
        "HIGH_ENTROPY_HOST" -> weights.highEntropyHost
        "SUSPICIOUS_PATH_KEYWORDS" -> weights.suspiciousPathKeywords
        "RISKY_EXTENSION" -> weights.riskyExtension
        "DOUBLE_EXTENSION" -> weights.doubleExtension
        "CREDENTIAL_PARAMS" -> weights.credentialParams
        "ENCODED_PAYLOAD" -> weights.encodedPayload
        "EXCESSIVE_ENCODING" -> weights.excessiveEncoding
        "AT_SYMBOL_INJECTION" -> weights.atSymbolInjection
        "MULTIPLE_TLD_SEGMENTS" -> weights.multipleTldSegments
        "PUNYCODE_DOMAIN" -> weights.punycodeDomain
        "NUMERIC_SUBDOMAIN" -> weights.numericSubdomain
        "ZERO_WIDTH_CHARS" -> weights.zeroWidthChars
        "MIXED_SCRIPT" -> weights.mixedScript
        "LOOKALIKE_CHARS" -> weights.lookalikeChars
        "DATA_URI_SCHEME" -> weights.dataUri
        "JAVASCRIPT_URL" -> weights.javascriptUrl
        "FRAGMENT_HIDING" -> weights.fragmentHiding
        "KNOWN_BAD_DOMAIN" -> weights.knownBadDomain
        "DOMAIN_AGE_SIMULATION" -> weights.domainAgePattern
        "CREDENTIAL_KEYWORDS" -> weights.credentialKeywords
        else -> 0
    }

    /**
     * Serialize to JSON string.
     */
    fun toJson(): String = buildString {
        appendLine("{")
        appendLine("  \"version\": \"$version\",")
        appendLine("  \"weights\": {")
        appendLine("    \"httpNotHttps\": ${weights.httpNotHttps},")
        appendLine("    \"ipAddressHost\": ${weights.ipAddressHost},")
        appendLine("    \"urlShortener\": ${weights.urlShortener},")
        appendLine("    \"atSymbolInjection\": ${weights.atSymbolInjection},")
        appendLine("    \"javascriptUrl\": ${weights.javascriptUrl},")
        appendLine("    \"dataUri\": ${weights.dataUri},")
        appendLine("    \"knownBadDomain\": ${weights.knownBadDomain}")
        appendLine("  },")
        appendLine("  \"thresholds\": {")
        appendLine("    \"safeMax\": ${thresholds.safeMax},")
        appendLine("    \"suspiciousMax\": ${thresholds.suspiciousMax}")
        appendLine("  }")
        appendLine("}")
    }

    companion object {
        /** Current default configuration */
        fun default(): RiskConfig = RiskConfig(
            version = "1.19.0",
            weights = WeightConfig(),
            thresholds = ThresholdConfig(),
            features = FeatureConfig()
        )

        /**
         * Known config versions for regression testing.
         */
        val KNOWN_VERSIONS = mapOf(
            "1.18.11" to RiskConfig(
                version = "1.18.11",
                weights = WeightConfig(),
                thresholds = ThresholdConfig(),
                features = FeatureConfig()
            ),
            "1.19.0" to default()
        )

        /**
         * Get config by version.
         */
        fun forVersion(version: String): RiskConfig? = KNOWN_VERSIONS[version]
    }
}
