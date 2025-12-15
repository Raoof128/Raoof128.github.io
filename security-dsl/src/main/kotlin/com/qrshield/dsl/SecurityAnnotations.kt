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
 * Security Validation Annotations
 *
 * These annotations provide compile-time hints and runtime validation.
 * A Kotlin Compiler Plugin (KCP) could process these to emit compile errors.
 *
 * ## Compiler Plugin Integration
 *
 * With a full KCP implementation, these annotations would trigger:
 * - Compile-time value range checking
 * - Static analysis for security patterns
 * - Build failure on constraint violations
 *
 * @author QR-SHIELD Security Team
 * @since 1.3.0
 */

/**
 * Marks a parameter as requiring a specific value range.
 * 
 * @property min Minimum allowed value (inclusive)
 * @property max Maximum allowed value (inclusive)
 * @property message Error message on violation
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ValidRange(
    val min: Int,
    val max: Int,
    val message: String = ""
)

/**
 * Marks a string as requiring valid TLD format.
 * A compiler plugin would verify:
 * - No dots in TLD
 * - Length <= 20 characters
 * - Only alphanumeric + hyphen
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class ValidTld

/**
 * Marks a string as requiring valid domain format.
 * A compiler plugin would verify:
 * - Contains at least one dot
 * - Valid characters only
 * - Reasonable length
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class ValidDomain

/**
 * Marks a double as requiring a valid privacy epsilon.
 * Must be in range [0.01, 100.0].
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class PrivacyEpsilon

/**
 * Marks a double as requiring a valid privacy delta.
 * Must be in range [1e-10, 0.01].
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class PrivacyDelta

/**
 * Marks a security-critical function.
 * Compiler plugin could enforce:
 * - Exception handling requirements
 * - Logging requirements
 * - Input sanitization
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SecurityCritical(
    val reason: String = "",
    val requiresAudit: Boolean = true
)

/**
 * Marks a parameter as untrusted user input.
 * Compiler plugin would verify sanitization before use.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Untrusted

/**
 * Marks a value as sanitized and safe to use.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.LOCAL_VARIABLE)
@Retention(AnnotationRetention.SOURCE)
annotation class Sanitized

/**
 * Indicates a constant should never exceed the given value.
 * Used for security limits like MAX_REDIRECTS.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class SecurityLimit(
    val max: Int,
    val reason: String
)
