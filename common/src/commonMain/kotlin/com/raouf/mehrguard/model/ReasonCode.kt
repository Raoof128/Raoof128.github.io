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

package com.raouf.mehrguard.model

/**
 * Stable reason codes for explainable security analysis.
 *
 * Each reason code represents a specific security indicator that contributed
 * to the risk score. These codes are:
 * - **Stable**: Code strings never change (safe for persistence/logging)
 * - **Enumerable**: Can iterate all possible reasons
 * - **Typed**: Compiler-enforced exhaustive handling
 *
 * ## Usage
 * ```kotlin
 * val result = engine.analyze(url)
 * result.reasons.forEach { reason ->
 *     when (reason) {
 *         ReasonCode.REASON_HOMOGRAPH -> showHomographWarning()
 *         ReasonCode.REASON_BRAND_IMPERSONATION -> showBrandWarning()
 *         // ... compiler ensures all cases handled
 *     }
 * }
 * ```
 *
 * ## Severity Levels
 * - **CRITICAL**: Immediate threat, likely malicious (score impact: 60-100)
 * - **HIGH**: Strong phishing indicator (score impact: 30-59)
 * - **MEDIUM**: Moderate risk factor (score impact: 15-29)
 * - **LOW**: Minor risk indicator (score impact: 1-14)
 * - **INFO**: Informational, no score impact
 *
 * @author QR-SHIELD Security Team
 * @since 1.19.0
 */
enum class ReasonCode(
    /** Stable code string for persistence/logging */
    val code: String,
    /** Severity level for UI display and prioritization */
    val severity: Severity,
    /** Human-readable description (for debugging, not UI) */
    val description: String
) {
    // === CRITICAL SEVERITY (60-100 score impact) ===

    /** JavaScript URL scheme detected - executes code */
    REASON_JAVASCRIPT_URL(
        "JAVASCRIPT_URL",
        Severity.CRITICAL,
        "JavaScript URL scheme detected - executes code when clicked"
    ),

    /** Data URI scheme with embedded code */
    REASON_DATA_URI(
        "DATA_URI",
        Severity.CRITICAL,
        "Data URI scheme detected - may contain embedded malicious code"
    ),

    /** @ symbol injection for URL spoofing */
    REASON_AT_SYMBOL_INJECTION(
        "AT_SYMBOL_INJECTION",
        Severity.CRITICAL,
        "URL contains @ symbol indicating possible credential theft attempt"
    ),

    // === HIGH SEVERITY (30-59 score impact) ===

    /** Homograph/punycode attack detected */
    REASON_HOMOGRAPH(
        "HOMOGRAPH",
        Severity.HIGH,
        "Internationalized domain name (IDN) may impersonate another domain"
    ),

    /** Mixed scripts in hostname (Latin + Cyrillic) */
    REASON_MIXED_SCRIPT(
        "MIXED_SCRIPT",
        Severity.HIGH,
        "Hostname contains characters from multiple scripts (possible spoofing)"
    ),

    /** Lookalike Unicode characters detected */
    REASON_LOOKALIKE_CHARS(
        "LOOKALIKE_CHARS",
        Severity.HIGH,
        "Domain contains Unicode characters that look like ASCII letters"
    ),

    /** Zero-width characters for obfuscation */
    REASON_ZERO_WIDTH_CHARS(
        "ZERO_WIDTH_CHARS",
        Severity.HIGH,
        "Hidden zero-width Unicode characters detected (obfuscation attempt)"
    ),

    /** IP address instead of domain name */
    REASON_IP_HOST(
        "IP_HOST",
        Severity.HIGH,
        "URL uses IP address instead of domain name"
    ),

    /** Credential-related parameters in URL */
    REASON_CREDENTIAL_PARAM(
        "CREDENTIAL_PARAM",
        Severity.HIGH,
        "URL contains credential-related parameters (password, token, etc.)"
    ),

    /** Brand impersonation detected */
    REASON_BRAND_IMPERSONATION(
        "BRAND_IMPERSONATION",
        Severity.HIGH,
        "URL appears to impersonate a known brand"
    ),

    /** Brand appears only in subdomain (not registrable domain) */
    REASON_BRAND_IN_SUBDOMAIN(
        "BRAND_IN_SUBDOMAIN",
        Severity.HIGH,
        "Brand name appears in subdomain but not the main domain"
    ),

    /** Dangerous file extension in path */
    REASON_RISKY_EXTENSION(
        "RISKY_EXTENSION",
        Severity.HIGH,
        "Path contains potentially dangerous file extension"
    ),

    /** Double file extension (e.g., invoice.pdf.exe) */
    REASON_DOUBLE_EXTENSION(
        "DOUBLE_EXTENSION",
        Severity.HIGH,
        "Double file extension detected (common malware tactic)"
    ),

    /** Encoded payload in query string */
    REASON_ENCODED_PAYLOAD(
        "ENCODED_PAYLOAD",
        Severity.HIGH,
        "Large encoded data detected in URL parameters"
    ),

    // === MEDIUM SEVERITY (15-29 score impact) ===

    /** HTTP instead of HTTPS */
    REASON_HTTP_NOT_HTTPS(
        "HTTP_NOT_HTTPS",
        Severity.MEDIUM,
        "URL uses insecure HTTP protocol instead of HTTPS"
    ),

    /** High-risk or free TLD */
    REASON_SUSPICIOUS_TLD(
        "SUSPICIOUS_TLD",
        Severity.MEDIUM,
        "Top-level domain is frequently used for phishing"
    ),

    /** High entropy hostname (DGA-like) */
    REASON_HIGH_ENTROPY_HOST(
        "HIGH_ENTROPY_HOST",
        Severity.MEDIUM,
        "Domain name appears randomly generated"
    ),

    /** Redirect parameter detected */
    REASON_REDIRECT_PARAM(
        "REDIRECT_PARAM",
        Severity.MEDIUM,
        "URL contains redirect parameter that may lead to another site"
    ),

    /** Deep subdomain structure (>3 levels) */
    REASON_DEEP_SUBDOMAIN(
        "DEEP_SUBDOMAIN",
        Severity.MEDIUM,
        "Excessive subdomain depth (may hide real domain)"
    ),

    /** Multiple TLD-like segments */
    REASON_MULTI_TLD(
        "MULTI_TLD",
        Severity.MEDIUM,
        "Multiple TLD-like segments in domain name"
    ),

    /** Numeric-only subdomain */
    REASON_NUMERIC_SUBDOMAIN(
        "NUMERIC_SUBDOMAIN",
        Severity.MEDIUM,
        "Numeric-only subdomain detected"
    ),

    /** Non-standard port */
    REASON_NON_STANDARD_PORT(
        "NON_STANDARD_PORT",
        Severity.MEDIUM,
        "URL uses non-standard port number"
    ),

    /** Suspicious port number (Metasploit, backdoor, etc.) */
    REASON_SUSPICIOUS_PORT(
        "SUSPICIOUS_PORT",
        Severity.MEDIUM,
        "URL uses port commonly associated with attacks"
    ),

    /** Fragment used to hide content */
    REASON_FRAGMENT_HIDING(
        "FRAGMENT_HIDING",
        Severity.MEDIUM,
        "URL fragment appears to hide additional content"
    ),

    /** Excessive URL encoding */
    REASON_EXCESSIVE_ENCODING(
        "EXCESSIVE_ENCODING",
        Severity.MEDIUM,
        "Excessive URL encoding detected (obfuscation attempt)"
    ),

    /** Domain appears newly generated */
    REASON_DOMAIN_AGE_PATTERN(
        "DOMAIN_AGE_PATTERN",
        Severity.MEDIUM,
        "Domain name matches patterns of auto-generated domains"
    ),

    // === LOW SEVERITY (1-14 score impact) ===

    /** URL shortener service */
    REASON_URL_SHORTENER(
        "URL_SHORTENER",
        Severity.LOW,
        "URL uses shortening service (hides real destination)"
    ),

    /** Suspicious path keywords */
    REASON_SUSPICIOUS_PATH(
        "SUSPICIOUS_PATH",
        Severity.LOW,
        "Path contains suspicious keywords (login, verify, etc.)"
    ),

    /** Long URL (>250 chars) */
    REASON_LONG_URL(
        "LONG_URL",
        Severity.LOW,
        "Unusually long URL"
    ),

    /** Credential harvesting keywords */
    REASON_CREDENTIAL_KEYWORDS(
        "CREDENTIAL_KEYWORDS",
        Severity.LOW,
        "URL contains credential harvesting keywords"
    ),

    // === INFO (no score impact) ===

    /** URL could not be parsed */
    REASON_UNPARSEABLE(
        "UNPARSEABLE",
        Severity.INFO,
        "URL could not be fully parsed"
    ),

    /** Analysis completed normally */
    REASON_ANALYSIS_COMPLETE(
        "ANALYSIS_COMPLETE",
        Severity.INFO,
        "Analysis completed successfully"
    );

    companion object {
        /**
         * Get ReasonCode by its stable code string.
         * @param code The stable code string (e.g., "HOMOGRAPH")
         * @return ReasonCode or null if not found
         */
        fun fromCode(code: String): ReasonCode? {
            return entries.find { it.code == code }
        }

        /**
         * Get all reason codes of a specific severity.
         */
        fun bySeverity(severity: Severity): List<ReasonCode> {
            return entries.filter { it.severity == severity }
        }

        /**
         * Get all critical and high severity codes (for urgent warnings).
         */
        fun urgent(): List<ReasonCode> {
            return entries.filter { 
                it.severity == Severity.CRITICAL || it.severity == Severity.HIGH 
            }
        }
    }
}

/**
 * Severity levels for reason codes.
 *
 * Determines UI treatment and score impact.
 */
enum class Severity {
    /** Immediate threat - block access recommended */
    CRITICAL,
    /** Strong indicator - show prominent warning */
    HIGH,
    /** Moderate risk - show caution notice */
    MEDIUM,
    /** Minor indicator - informational warning */
    LOW,
    /** Informational only - no score impact */
    INFO;

    /** Whether this severity should trigger a warning */
    val isWarning: Boolean
        get() = this != INFO

    /** Whether this severity indicates urgent action needed */
    val isUrgent: Boolean
        get() = this == CRITICAL || this == HIGH
}
