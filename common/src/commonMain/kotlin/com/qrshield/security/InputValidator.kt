package com.qrshield.security

/**
 * Input Validation and Sanitization Layer for QR-SHIELD
 * 
 * Provides comprehensive input validation for all user inputs
 * to prevent injection attacks, DoS, and other security issues.
 * 
 * SECURITY NOTES:
 * - All public methods validate inputs before processing
 * - Returns sealed class results for explicit error handling
 * - No exceptions thrown for invalid inputs
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
object InputValidator {
    
    // === CONSTANTS ===
    
    /** Maximum URL length */
    const val MAX_URL_LENGTH = 2048
    
    /** Maximum input length for general text */
    const val MAX_INPUT_LENGTH = 4096
    
    /** Allowed URL protocols */
    private val ALLOWED_PROTOCOLS = setOf("http", "https")
    
    /** Control characters that should be rejected */
    private val CONTROL_CHARS = (0..31).filter { it != 9 && it != 10 && it != 13 }
    
    // === RESULT TYPES ===
    
    /**
     * Validation result sealed class.
     */
    sealed class ValidationResult<out T> {
        data class Valid<T>(val value: T) : ValidationResult<T>()
        data class Invalid(val reason: String, val code: ErrorCode) : ValidationResult<Nothing>()
        
        fun isValid(): Boolean = this is Valid
        
        fun getOrNull(): T? = when (this) {
            is Valid -> value
            is Invalid -> null
        }
        
        fun <R> map(transform: (T) -> R): ValidationResult<R> = when (this) {
            is Valid -> Valid(transform(value))
            is Invalid -> this
        }
    }
    
    /**
     * Error codes for validation failures.
     */
    enum class ErrorCode {
        EMPTY_INPUT,
        TOO_LONG,
        CONTAINS_NULL_BYTES,
        CONTAINS_CONTROL_CHARS,
        INVALID_PROTOCOL,
        MALFORMED_URL,
        INVALID_HOST,
        SUSPICIOUS_ENCODING,
        REJECTED_PATTERN
    }
    
    // === VALIDATION METHODS ===
    
    /**
     * Validate a URL string for QR code content.
     * 
     * @param url Raw URL string from QR code
     * @return ValidationResult with sanitized URL or error
     */
    fun validateUrl(url: String?): ValidationResult<String> {
        // Null check
        if (url == null) {
            return ValidationResult.Invalid("URL cannot be null", ErrorCode.EMPTY_INPUT)
        }
        
        // Empty check
        val trimmed = url.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Invalid("URL cannot be empty", ErrorCode.EMPTY_INPUT)
        }
        
        // Length check
        if (trimmed.length > MAX_URL_LENGTH) {
            return ValidationResult.Invalid(
                "URL exceeds maximum length of $MAX_URL_LENGTH",
                ErrorCode.TOO_LONG
            )
        }
        
        // Null byte check (common injection vector)
        if (trimmed.contains('\u0000')) {
            return ValidationResult.Invalid(
                "URL contains null bytes",
                ErrorCode.CONTAINS_NULL_BYTES
            )
        }
        
        // Control character check
        if (trimmed.any { it.code in CONTROL_CHARS }) {
            return ValidationResult.Invalid(
                "URL contains invalid control characters",
                ErrorCode.CONTAINS_CONTROL_CHARS
            )
        }
        
        // Protocol validation
        val protocolEnd = trimmed.indexOf("://")
        if (protocolEnd > 0) {
            val protocol = trimmed.substring(0, protocolEnd).lowercase()
            if (protocol !in ALLOWED_PROTOCOLS) {
                return ValidationResult.Invalid(
                    "Protocol '$protocol' is not allowed",
                    ErrorCode.INVALID_PROTOCOL
                )
            }
        }
        
        // Check for javascript: or data: schemes (XSS vectors)
        val lowerUrl = trimmed.lowercase()
        if (lowerUrl.startsWith("javascript:") || 
            lowerUrl.startsWith("data:") ||
            lowerUrl.startsWith("vbscript:")) {
            return ValidationResult.Invalid(
                "Dangerous URL scheme detected",
                ErrorCode.INVALID_PROTOCOL
            )
        }
        
        // Sanitize and return
        val sanitized = sanitizeUrl(trimmed)
        return ValidationResult.Valid(sanitized)
    }
    
    /**
     * Validate general text input.
     * 
     * @param input Raw input string
     * @param maxLength Maximum allowed length (default: MAX_INPUT_LENGTH)
     * @return ValidationResult with sanitized input or error
     */
    fun validateTextInput(
        input: String?,
        maxLength: Int = MAX_INPUT_LENGTH
    ): ValidationResult<String> {
        if (input == null) {
            return ValidationResult.Invalid("Input cannot be null", ErrorCode.EMPTY_INPUT)
        }
        
        val trimmed = input.trim()
        if (trimmed.length > maxLength) {
            return ValidationResult.Invalid(
                "Input exceeds maximum length of $maxLength",
                ErrorCode.TOO_LONG
            )
        }
        
        if (trimmed.contains('\u0000')) {
            return ValidationResult.Invalid(
                "Input contains null bytes",
                ErrorCode.CONTAINS_NULL_BYTES
            )
        }
        
        return ValidationResult.Valid(trimmed)
    }
    
    /**
     * Validate and sanitize a hostname.
     * 
     * @param host Raw hostname string
     * @return ValidationResult with sanitized hostname or error
     */
    fun validateHostname(host: String?): ValidationResult<String> {
        if (host.isNullOrBlank()) {
            return ValidationResult.Invalid("Hostname cannot be empty", ErrorCode.EMPTY_INPUT)
        }
        
        val trimmed = host.trim().lowercase()
        
        // Length check (DNS limit)
        if (trimmed.length > 253) {
            return ValidationResult.Invalid(
                "Hostname exceeds maximum length",
                ErrorCode.TOO_LONG
            )
        }
        
        // Label length check (each segment max 63 chars)
        val labels = trimmed.split(".")
        if (labels.any { it.length > 63 }) {
            return ValidationResult.Invalid(
                "Hostname label exceeds 63 characters",
                ErrorCode.INVALID_HOST
            )
        }
        
        // Character validation
        val validHostPattern = Regex("""^[a-z0-9]([a-z0-9\-]*[a-z0-9])?(\.[a-z0-9]([a-z0-9\-]*[a-z0-9])?)*$""")
        if (!validHostPattern.matches(trimmed) && !isValidIpAddress(trimmed)) {
            // Allow punycode domains
            if (!trimmed.contains("xn--")) {
                return ValidationResult.Invalid(
                    "Invalid hostname format",
                    ErrorCode.INVALID_HOST
                )
            }
        }
        
        return ValidationResult.Valid(trimmed)
    }
    
    // === SANITIZATION METHODS ===
    
    /**
     * Sanitize a URL by removing dangerous patterns.
     */
    private fun sanitizeUrl(url: String): String {
        return url
            .replace("\t", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace(" ", "%20")
            .trim()
    }
    
    /**
     * Check if string is a valid IPv4 address.
     */
    private fun isValidIpAddress(host: String): Boolean {
        val ipv4Pattern = Regex("""^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$""")
        val match = ipv4Pattern.matchEntire(host) ?: return false
        
        return try {
            match.groupValues.drop(1).all { octet ->
                octet.toInt() in 0..255
            }
        } catch (e: Exception) {
            false
        }
    }
    
    // === SECURITY PATTERN DETECTION ===
    
    /**
     * Check for common SQL injection patterns.
     * Note: This is for logging/alerting, not as a security boundary.
     */
    fun containsSqlInjectionPatterns(input: String): Boolean {
        val patterns = listOf(
            "' OR ",
            "\" OR ",
            "'; --",
            "\"; --",
            "1=1",
            "UNION SELECT",
            "DROP TABLE",
            "DELETE FROM"
        )
        val inputUpper = input.uppercase()
        return patterns.any { inputUpper.contains(it.uppercase()) }
    }
    
    /**
     * Check for XSS patterns.
     */
    fun containsXssPatterns(input: String): Boolean {
        val lowerInput = input.lowercase()
        val patterns = listOf(
            "<script",
            "javascript:",
            "onerror=",
            "onload=",
            "onclick=",
            "onmouseover=",
            "<iframe",
            "<img src=",
            "data:text/html"
        )
        return patterns.any { lowerInput.contains(it) }
    }
}
