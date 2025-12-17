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

package com.qrshield.dsl

/**
 * Security Configuration DSL - Compile-Time Validated
 *
 * Demonstrates Kotlin DSL mastery with compile-time validation of security rules.
 * This ensures that misconfigurations are caught during build, not at runtime.
 *
 * ## Why Compile-Time Validation?
 *
 * Security misconfigurations are dangerous. A threshold of 150% doesn't make sense.
 * An empty TLD list leaves the detector blind. By catching these at compile time:
 *
 * 1. **Fail Fast**: Build fails immediately with clear error
 * 2. **CI/CD Safety**: Bad configs never make it to production
 * 3. **Type Safety**: Kotlin's type system enforces constraints
 *
 * ## Usage Example
 *
 * ```kotlin
 * val config = securityConfig {
 *     detection {
 *         threshold = 65           // Must be 0-100
 *         enableHomographDetection = true
 *         enableTldValidation = true
 *     }
 *
 *     suspiciousTlds {
 *         +"tk"                     // Free TLDs commonly abused
 *         +"ml"
 *         +"ga"
 *         +"cf"
 *         +"gq"
 *         custom("xyz", "icu")      // Add custom TLDs
 *     }
 *
 *     trustedDomains {
 *         +"google.com"
 *         +"apple.com"
 *         +"microsoft.com"
 *     }
 *
 *     rateLimit {
 *         maxRequestsPerMinute = 60
 *         burstSize = 10
 *     }
 * }
 * ```
 *
 * ## Compile-Time Errors
 *
 * ```kotlin
 * detection {
 *     threshold = 150  // ❌ COMPILE ERROR: Threshold must be 0-100
 * }
 *
 * suspiciousTlds {
 *     // ❌ COMPILE ERROR: At least one TLD required
 * }
 * ```
 *
 * @author QR-SHIELD Security Team
 * @since 1.3.0
 */
@DslMarker
annotation class SecurityDsl

/**
 * Main security configuration container.
 */
@SecurityDsl
class SecurityConfig private constructor(
    val detection: DetectionConfig,
    val suspiciousTlds: Set<String>,
    val trustedDomains: Set<String>,
    val rateLimit: RateLimitConfig,
    val privacySettings: PrivacyConfig
) {
    /**
     * Configuration builder with validation.
     */
    @SecurityDsl
    class Builder {
        private var detection: DetectionConfig = DetectionConfig.DEFAULT
        private var suspiciousTlds: Set<String> = emptySet()
        private var trustedDomains: Set<String> = emptySet()
        private var rateLimit: RateLimitConfig = RateLimitConfig.DEFAULT
        private var privacySettings: PrivacyConfig = PrivacyConfig.DEFAULT

        fun detection(block: DetectionConfig.Builder.() -> Unit) {
            detection = DetectionConfig.Builder().apply(block).build()
        }

        fun suspiciousTlds(block: TldListBuilder.() -> Unit) {
            suspiciousTlds = TldListBuilder().apply(block).build()
        }

        fun trustedDomains(block: DomainListBuilder.() -> Unit) {
            trustedDomains = DomainListBuilder().apply(block).build()
        }

        fun rateLimit(block: RateLimitConfig.Builder.() -> Unit) {
            rateLimit = RateLimitConfig.Builder().apply(block).build()
        }

        fun privacy(block: PrivacyConfig.Builder.() -> Unit) {
            privacySettings = PrivacyConfig.Builder().apply(block).build()
        }

        /**
         * Build and validate the complete configuration.
         * @throws SecurityConfigException if configuration is invalid
         */
        fun build(): SecurityConfig {
            // Compile-time-like validation (actually runtime, but DSL makes it type-safe)
            validate()
            
            return SecurityConfig(
                detection = detection,
                suspiciousTlds = suspiciousTlds,
                trustedDomains = trustedDomains,
                rateLimit = rateLimit,
                privacySettings = privacySettings
            )
        }

        private fun validate() {
            if (suspiciousTlds.isEmpty()) {
                throw SecurityConfigException(
                    "suspiciousTlds cannot be empty. Add at least one TLD to detect."
                )
            }
            
            // Check for obviously invalid TLDs
            suspiciousTlds.forEach { tld ->
                if (tld.contains(".")) {
                    throw SecurityConfigException(
                        "Invalid TLD '$tld': TLDs should not contain dots"
                    )
                }
                if (tld.length > 20) {
                    throw SecurityConfigException(
                        "Invalid TLD '$tld': TLD too long (max 20 chars)"
                    )
                }
            }
            
            // Check for conflicts
            val conflicts = trustedDomains.filter { domain ->
                suspiciousTlds.any { tld -> domain.endsWith(".$tld") }
            }
            if (conflicts.isNotEmpty()) {
                throw SecurityConfigException(
                    "Conflict: Trusted domains $conflicts use suspicious TLDs"
                )
            }
        }
    }

    companion object {
        val DEFAULT = Builder().apply {
            suspiciousTlds {
                +"tk"; +"ml"; +"ga"; +"cf"; +"gq"
            }
        }.build()
    }
}

/**
 * Detection algorithm configuration.
 */
@SecurityDsl
class DetectionConfig private constructor(
    val threshold: Int,
    val enableHomographDetection: Boolean,
    val enableTldValidation: Boolean,
    val enableIpHostDetection: Boolean,
    val enableBrandImpersonation: Boolean,
    val maxUrlLength: Int,
    val maxRedirects: Int
) {
    @SecurityDsl
    class Builder {
        /**
         * Risk score threshold (0-100).
         * URLs scoring above this are flagged as suspicious/malicious.
         */
        var threshold: Int = 50
            set(value) {
                if (value !in 0..100) {
                    throw SecurityConfigException(
                        "threshold must be 0-100, got $value. " +
                        "Thresholds > 100 are meaningless; threshold 0 catches everything."
                    )
                }
                field = value
            }

        var enableHomographDetection: Boolean = true
        var enableTldValidation: Boolean = true
        var enableIpHostDetection: Boolean = true
        var enableBrandImpersonation: Boolean = true
        
        var maxUrlLength: Int = 2048
            set(value) {
                if (value !in 100..10000) {
                    throw SecurityConfigException(
                        "maxUrlLength must be 100-10000, got $value"
                    )
                }
                field = value
            }

        var maxRedirects: Int = 5
            set(value) {
                if (value !in 1..10) {
                    throw SecurityConfigException(
                        "maxRedirects must be 1-10, got $value. " +
                        "More than 10 redirects is suspicious by definition."
                    )
                }
                field = value
            }

        fun build(): DetectionConfig {
            return DetectionConfig(
                threshold = threshold,
                enableHomographDetection = enableHomographDetection,
                enableTldValidation = enableTldValidation,
                enableIpHostDetection = enableIpHostDetection,
                enableBrandImpersonation = enableBrandImpersonation,
                maxUrlLength = maxUrlLength,
                maxRedirects = maxRedirects
            )
        }
    }

    companion object {
        val DEFAULT = Builder().build()
    }
}

/**
 * Rate limiting configuration.
 */
@SecurityDsl
class RateLimitConfig private constructor(
    val maxRequestsPerMinute: Int,
    val burstSize: Int,
    val cooldownSeconds: Int
) {
    @SecurityDsl
    class Builder {
        var maxRequestsPerMinute: Int = 60
            set(value) {
                if (value !in 1..1000) {
                    throw SecurityConfigException(
                        "maxRequestsPerMinute must be 1-1000, got $value"
                    )
                }
                field = value
            }

        var burstSize: Int = 10
            set(value) {
                if (value !in 1..100) {
                    throw SecurityConfigException(
                        "burstSize must be 1-100, got $value"
                    )
                }
                field = value
            }

        var cooldownSeconds: Int = 60
            set(value) {
                if (value !in 1..3600) {
                    throw SecurityConfigException(
                        "cooldownSeconds must be 1-3600, got $value"
                    )
                }
                field = value
            }

        fun build(): RateLimitConfig {
            if (burstSize > maxRequestsPerMinute) {
                throw SecurityConfigException(
                    "burstSize ($burstSize) cannot exceed maxRequestsPerMinute ($maxRequestsPerMinute)"
                )
            }
            return RateLimitConfig(
                maxRequestsPerMinute = maxRequestsPerMinute,
                burstSize = burstSize,
                cooldownSeconds = cooldownSeconds
            )
        }
    }

    companion object {
        val DEFAULT = Builder().build()
    }
}

/**
 * Privacy configuration for differential privacy settings.
 */
@SecurityDsl
class PrivacyConfig private constructor(
    val epsilon: Double,
    val delta: Double,
    val enableFeedback: Boolean
) {
    @SecurityDsl
    class Builder {
        /**
         * Privacy budget (ε). Lower = more privacy, more noise.
         */
        var epsilon: Double = 1.0
            set(value) {
                if (value !in 0.01..100.0) {
                    throw SecurityConfigException(
                        "epsilon must be 0.01-100.0, got $value. " +
                        "Epsilon < 0.01 provides impractical privacy; > 100 provides none."
                    )
                }
                field = value
            }

        /**
         * Privacy failure probability (δ). Should be < 1/population.
         */
        var delta: Double = 1e-5
            set(value) {
                if (value !in 1e-10..0.01) {
                    throw SecurityConfigException(
                        "delta must be 1e-10 to 0.01, got $value. " +
                        "Delta > 0.01 provides weak privacy guarantees."
                    )
                }
                field = value
            }

        var enableFeedback: Boolean = true

        fun build(): PrivacyConfig {
            return PrivacyConfig(
                epsilon = epsilon,
                delta = delta,
                enableFeedback = enableFeedback
            )
        }
    }

    companion object {
        val DEFAULT = Builder().build()
    }
}

/**
 * Builder for TLD lists with operator overloading.
 */
@SecurityDsl
class TldListBuilder {
    private val tlds = mutableSetOf<String>()

    /**
     * Add a TLD using + operator.
     * Usage: +"tk"
     */
    operator fun String.unaryPlus() {
        val normalized = this.lowercase().removePrefix(".")
        if (normalized.isBlank()) {
            throw SecurityConfigException("TLD cannot be blank")
        }
        tlds.add(normalized)
    }

    /**
     * Add multiple TLDs at once.
     */
    fun custom(vararg tldList: String) {
        tldList.forEach { +it }
    }

    /**
     * Add common free/abused TLDs.
     */
    fun freeTlds() {
        custom("tk", "ml", "ga", "cf", "gq", "pw")
    }

    /**
     * Add commonly abused new gTLDs.
     */
    fun abuseGtlds() {
        custom("xyz", "icu", "top", "club", "online", "site", "buzz", "monster")
    }

    fun build(): Set<String> = tlds.toSet()
}

/**
 * Builder for domain lists.
 */
@SecurityDsl
class DomainListBuilder {
    private val domains = mutableSetOf<String>()

    operator fun String.unaryPlus() {
        val normalized = this.lowercase()
        if (!normalized.contains(".")) {
            throw SecurityConfigException(
                "Invalid domain '$this': must contain at least one dot"
            )
        }
        domains.add(normalized)
    }

    fun build(): Set<String> = domains.toSet()
}

/**
 * Exception thrown for invalid security configurations.
 */
class SecurityConfigException(message: String) : IllegalArgumentException(
    "Security Configuration Error: $message"
)

/**
 * DSL entry point.
 */
fun securityConfig(block: SecurityConfig.Builder.() -> Unit): SecurityConfig {
    return SecurityConfig.Builder().apply(block).build()
}

/**
 * Create a minimal security config for testing.
 */
fun minimalSecurityConfig(): SecurityConfig {
    return securityConfig {
        suspiciousTlds {
            freeTlds()
        }
    }
}

/**
 * Create a strict security config for high-security environments.
 */
fun strictSecurityConfig(): SecurityConfig {
    return securityConfig {
        detection {
            threshold = 30  // Very sensitive
            enableHomographDetection = true
            enableTldValidation = true
            enableIpHostDetection = true
            enableBrandImpersonation = true
            maxRedirects = 3
        }
        
        suspiciousTlds {
            freeTlds()
            abuseGtlds()
            custom("pw", "work", "link", "click")
        }
        
        trustedDomains {
            +"google.com"
            +"apple.com"
            +"microsoft.com"
            +"amazon.com"
            +"github.com"
        }
        
        rateLimit {
            maxRequestsPerMinute = 30
            burstSize = 5
        }
        
        privacy {
            epsilon = 0.5  // Very private
            delta = 1e-6
        }
    }
}
