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
 * Security Configuration Validator
 *
 * Validates SecurityConfig instances at various stages:
 * - Build time (via DSL property setters)
 * - Runtime (for dynamic configurations)
 * - Integration tests (for CI/CD gates)
 *
 * ## Compiler Plugin Simulation
 *
 * In a full Kotlin Compiler Plugin implementation, these validations
 * would run during compilation. This class simulates that behavior
 * for demonstration purposes while still providing type-safe validation.
 *
 * @author QR-SHIELD Security Team
 * @since 1.3.0
 */
object SecurityConfigValidator {

    /**
     * Validation result sealed class.
     */
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val errors: List<ValidationError>) : ValidationResult()
        
        val isValid: Boolean get() = this is Valid
    }
    
    /**
     * Individual validation error.
     */
    data class ValidationError(
        val field: String,
        val message: String,
        val severity: Severity,
        val suggestion: String? = null
    ) {
        enum class Severity { ERROR, WARNING, INFO }
        
        override fun toString(): String = buildString {
            append("[${severity.name}] $field: $message")
            suggestion?.let { append("\n  ↳ Suggestion: $it") }
        }
    }

    /**
     * Validate a SecurityConfig instance.
     *
     * @param config The configuration to validate
     * @param strict If true, warnings become errors
     * @return ValidationResult with any errors/warnings
     */
    fun validate(config: SecurityConfig, strict: Boolean = false): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Detection validation
        validateDetection(config.detection, errors)
        
        // TLD validation
        validateTlds(config.suspiciousTlds, errors)
        
        // Trusted domains validation
        validateTrustedDomains(config.trustedDomains, config.suspiciousTlds, errors)
        
        // Rate limit validation
        validateRateLimit(config.rateLimit, errors)
        
        // Privacy validation
        validatePrivacy(config.privacySettings, errors)
        
        // In strict mode, promote warnings to errors
        val finalErrors = if (strict) {
            errors.map { error ->
                if (error.severity == ValidationError.Severity.WARNING) {
                    error.copy(severity = ValidationError.Severity.ERROR)
                } else error
            }
        } else errors
        
        val actualErrors = finalErrors.filter { 
            it.severity == ValidationError.Severity.ERROR 
        }
        
        return if (actualErrors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(finalErrors)
        }
    }

    private fun validateDetection(
        detection: DetectionConfig, 
        errors: MutableList<ValidationError>
    ) {
        // Threshold sanity checks
        if (detection.threshold < 20) {
            errors.add(ValidationError(
                field = "detection.threshold",
                message = "Very low threshold (${detection.threshold}) may cause false positives",
                severity = ValidationError.Severity.WARNING,
                suggestion = "Consider threshold >= 30 for balanced detection"
            ))
        }
        
        if (detection.threshold > 80) {
            errors.add(ValidationError(
                field = "detection.threshold",
                message = "High threshold (${detection.threshold}) may miss phishing attempts",
                severity = ValidationError.Severity.WARNING,
                suggestion = "Consider threshold <= 70 for adequate protection"
            ))
        }
        
        // Feature toggles
        if (!detection.enableHomographDetection && !detection.enableBrandImpersonation) {
            errors.add(ValidationError(
                field = "detection.features",
                message = "Both homograph and brand detection disabled",
                severity = ValidationError.Severity.ERROR,
                suggestion = "Enable at least one impersonation detection feature"
            ))
        }
    }

    private fun validateTlds(
        tlds: Set<String>, 
        errors: MutableList<ValidationError>
    ) {
        if (tlds.isEmpty()) {
            errors.add(ValidationError(
                field = "suspiciousTlds",
                message = "No suspicious TLDs configured",
                severity = ValidationError.Severity.ERROR,
                suggestion = "Add at least: tk, ml, ga, cf, gq"
            ))
        }
        
        if (tlds.size > 100) {
            errors.add(ValidationError(
                field = "suspiciousTlds",
                message = "Too many TLDs (${tlds.size}) may slow detection",
                severity = ValidationError.Severity.WARNING,
                suggestion = "Focus on high-risk TLDs only"
            ))
        }
        
        // Check for common safe TLDs that shouldn't be blocked
        val safeTlds = setOf("com", "org", "net", "edu", "gov")
        val blocked = tlds.intersect(safeTlds)
        if (blocked.isNotEmpty()) {
            errors.add(ValidationError(
                field = "suspiciousTlds",
                message = "Blocking common TLDs: $blocked",
                severity = ValidationError.Severity.ERROR,
                suggestion = "Remove ${blocked.joinToString()} from suspicious list"
            ))
        }
    }

    private fun validateTrustedDomains(
        domains: Set<String>,
        suspiciousTlds: Set<String>,
        errors: MutableList<ValidationError>
    ) {
        // Check for domain format
        domains.forEach { domain ->
            if (!domain.contains(".")) {
                errors.add(ValidationError(
                    field = "trustedDomains",
                    message = "Invalid domain format: $domain",
                    severity = ValidationError.Severity.ERROR,
                    suggestion = "Use full domain like 'example.com'"
                ))
            }
        }
        
        // Check for suspicious TLD conflicts
        domains.forEach { domain ->
            val tld = domain.substringAfterLast(".")
            if (tld in suspiciousTlds) {
                errors.add(ValidationError(
                    field = "trustedDomains",
                    message = "Trusted domain '$domain' uses suspicious TLD '.$tld'",
                    severity = ValidationError.Severity.WARNING,
                    suggestion = "Verify this domain is legitimate"
                ))
            }
        }
    }

    private fun validateRateLimit(
        rateLimit: RateLimitConfig, 
        errors: MutableList<ValidationError>
    ) {
        if (rateLimit.burstSize > rateLimit.maxRequestsPerMinute / 2) {
            errors.add(ValidationError(
                field = "rateLimit.burstSize",
                message = "Burst size (${rateLimit.burstSize}) is > 50% of rate limit",
                severity = ValidationError.Severity.WARNING,
                suggestion = "Set burstSize <= ${rateLimit.maxRequestsPerMinute / 2}"
            ))
        }
    }

    private fun validatePrivacy(
        privacy: PrivacyConfig, 
        errors: MutableList<ValidationError>
    ) {
        // Check for weak privacy
        if (privacy.epsilon > 10.0) {
            errors.add(ValidationError(
                field = "privacy.epsilon",
                message = "High epsilon (${privacy.epsilon}) provides weak privacy",
                severity = ValidationError.Severity.WARNING,
                suggestion = "Use epsilon <= 10 for meaningful privacy"
            ))
        }
        
        // Check for impractical privacy
        if (privacy.epsilon < 0.1) {
            errors.add(ValidationError(
                field = "privacy.epsilon",
                message = "Very low epsilon (${privacy.epsilon}) adds excessive noise",
                severity = ValidationError.Severity.INFO,
                suggestion = "Use epsilon >= 0.1 for practical utility"
            ))
        }
    }

    /**
     * Format validation errors as a human-readable string.
     */
    fun formatErrors(result: ValidationResult.Invalid): String = buildString {
        appendLine("Security Configuration Validation Failed")
        appendLine("=" .repeat(50))
        result.errors.forEachIndexed { index, error ->
            appendLine("${index + 1}. $error")
            appendLine()
        }
    }
    
    /**
     * Validate configuration or throw.
     * Use this in tests to fail fast on bad configs.
     */
    fun validateOrThrow(config: SecurityConfig, strict: Boolean = false) {
        when (val result = validate(config, strict)) {
            is ValidationResult.Valid -> { /* OK */ }
            is ValidationResult.Invalid -> {
                throw SecurityConfigException(formatErrors(result))
            }
        }
    }
}
