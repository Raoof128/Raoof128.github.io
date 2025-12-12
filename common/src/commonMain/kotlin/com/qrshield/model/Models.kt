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

package com.qrshield.model

/**
 * Verdict enum representing the final risk classification of a scanned URL.
 * 
 * Used to categorize URLs based on their combined risk scores from heuristics,
 * ML analysis, and brand detection.
 * 
 * @property SAFE URL shows no signs of phishing (score 0-30)
 * @property SUSPICIOUS URL has some risk indicators (score 31-70)
 * @property MALICIOUS URL shows strong phishing indicators (score 71-100)
 * @property UNKNOWN Unable to determine risk level
 * 
 * @see RiskAssessment
 * @since 1.0.0
 */
enum class Verdict {
    /** URL is safe to visit - no phishing indicators detected */
    SAFE,
    /** URL has some risk factors - proceed with caution */
    SUSPICIOUS,
    /** URL is likely a phishing attempt - do not visit */
    MALICIOUS,
    /** Risk level could not be determined */
    UNKNOWN
}

/**
 * Complete risk assessment result from URL analysis.
 * 
 * Contains the combined analysis from all detection engines:
 * - Heuristics Engine (25+ security rules)
 * - ML Scoring Model
 * - Brand Impersonation Detection
 * - TLD Risk Scoring
 * 
 * ## Usage Example
 * ```kotlin
 * val assessment = urlAnalyzer.analyze("https://example.com")
 * when (assessment.verdict) {
 *     Verdict.SAFE -> showSafeResult()
 *     Verdict.SUSPICIOUS -> showWarning()
 *     Verdict.MALICIOUS -> showDanger(assessment.flags)
 * }
 * ```
 * 
 * @property score Risk score from 0 (safe) to 100 (malicious)
 * @property verdict Final classification based on score thresholds
 * @property flags List of triggered security flags (e.g., "SUSPICIOUS_TLD", "BRAND_IMPERSONATION")
 * @property details Detailed breakdown of individual analysis scores
 * @property confidence Analysis confidence level from 0.0 to 1.0
 * 
 * @see UrlAnalysisResult
 * @see Verdict
 * @since 1.0.0
 */
data class RiskAssessment(
    /** Risk score from 0 (safe) to 100 (high risk) */
    val score: Int,
    /** Final verdict classification */
    val verdict: Verdict,
    /** List of triggered security flags */
    val flags: List<String>,
    /** Detailed analysis breakdown */
    val details: UrlAnalysisResult,
    /** Analysis confidence (0.0 to 1.0, default 0.8) */
    val confidence: Float = 0.8f
) {
    /**
     * Human-readable score description based on score ranges.
     * - 0-30: "Low Risk"
     * - 31-70: "Medium Risk"
     * - 71-100: "High Risk"
     */
    val scoreDescription: String
        get() = when {
            score <= 30 -> "Low Risk"
            score <= 70 -> "Medium Risk"
            else -> "High Risk"
        }
    
    /**
     * User-facing action recommendation based on verdict.
     * Provides guidance on whether to proceed with the URL.
     */
    val actionRecommendation: String
        get() = when (verdict) {
            Verdict.SAFE -> "This URL appears safe to visit."
            Verdict.SUSPICIOUS -> "Proceed with caution. Verify the source before clicking."
            Verdict.MALICIOUS -> "Do not visit this URL. It shows strong phishing indicators."
            Verdict.UNKNOWN -> "Unable to fully analyze. Verify manually before visiting."
        }
}

/**
 * Detailed URL analysis breakdown with individual engine scores.
 * 
 * Provides transparency into how the final score was calculated,
 * showing contributions from each detection engine.
 * 
 * @property originalUrl The URL that was analyzed
 * @property heuristicScore Score from 25+ security heuristics (0-40)
 * @property mlScore Score from ML phishing model (0-30)
 * @property brandScore Score from brand impersonation detection (0-20)
 * @property tldScore Score from TLD risk analysis (0-10)
 * @property brandMatch Name of impersonated brand, if detected (e.g., "PayPal")
 * @property tld Extracted top-level domain (e.g., "com", "tk")
 * 
 * @since 1.0.0
 */
data class UrlAnalysisResult(
    /** Original URL that was analyzed */
    val originalUrl: String,
    /** Score from heuristics engine (0-40 points) */
    val heuristicScore: Int,
    /** Score from ML model (0-30 points) */
    val mlScore: Int,
    /** Score from brand detection (0-20 points) */
    val brandScore: Int,
    /** Score from TLD analysis (0-10 points) */
    val tldScore: Int,
    /** Detected brand name if impersonation found */
    val brandMatch: String? = null,
    /** Extracted TLD from the URL */
    val tld: String? = null
) {
    companion object {
        /** Creates an empty result for error cases */
        fun empty() = UrlAnalysisResult(
            originalUrl = "",
            heuristicScore = 0,
            mlScore = 0,
            brandScore = 0,
            tldScore = 0
        )
    }
}

/**
 * Sealed class representing QR code scan results.
 * 
 * Provides type-safe handling of scan outcomes:
 * - [Success] - QR code successfully decoded with content
 * - [Error] - Scan failed with error details
 * - [NoQrFound] - No QR code detected in the image/frame
 * 
 * ## Usage Example
 * ```kotlin
 * when (val result = scanner.scan(image)) {
 *     is ScanResult.Success -> processContent(result.content, result.contentType)
 *     is ScanResult.Error -> showError(result.message)
 *     is ScanResult.NoQrFound -> showNoQrMessage()
 * }
 * ```
 * 
 * @since 1.0.0
 */
sealed class ScanResult {
    /**
     * Successful QR code scan with decoded content.
     * 
     * @property content The decoded QR code content (URL, text, etc.)
     * @property contentType Detected type of content (URL, WiFi, vCard, etc.)
     */
    data class Success(
        val content: String,
        val contentType: ContentType
    ) : ScanResult()
    
    /**
     * Scan failed with an error.
     * 
     * @property message Human-readable error message
     * @property code Specific error code for programmatic handling
     */
    data class Error(
        val message: String,
        val code: ErrorCode
    ) : ScanResult()
    
    /** No QR code was found in the scanned image/frame */
    data object NoQrFound : ScanResult()
}

/**
 * Type of content encoded in a QR code.
 * 
 * Used to determine appropriate handling and UI display.
 * 
 * @since 1.0.0
 */
enum class ContentType {
    /** HTTP/HTTPS URL - subject to phishing analysis */
    URL,
    /** Plain text content */
    TEXT,
    /** WiFi network configuration */
    WIFI,
    /** Contact information (vCard format) */
    VCARD,
    /** Geographic location coordinates */
    GEO,
    /** Phone number (tel:) */
    PHONE,
    /** SMS message (sms:) */
    SMS,
    /** Email address (mailto:) */
    EMAIL,
    /** Unrecognized content type */
    UNKNOWN
}

/**
 * Error codes for scan failures.
 * 
 * Provides programmatic error identification for appropriate
 * error handling and user messaging.
 * 
 * @since 1.0.0
 */
enum class ErrorCode {
    /** Camera permission was denied by user */
    CAMERA_PERMISSION_DENIED,
    /** Device has no camera available */
    CAMERA_NOT_AVAILABLE,
    /** Camera encountered an error during capture */
    CAMERA_ERROR,
    /** Failed to decode image file */
    IMAGE_DECODE_ERROR,
    /** Image exceeds size limits (10MB max) */
    IMAGE_TOO_LARGE,
    /** Invalid or corrupt image data */
    INVALID_IMAGE,
    /** QR code format is malformed */
    INVALID_QR_FORMAT,
    /** QR code decoding failed */
    DECODE_ERROR,
    /** QR code content exceeds length limits */
    CONTENT_TOO_LARGE,
    /** ML Kit error (Android) */
    ML_KIT_ERROR,
    /** Vision framework error (iOS) */
    VISION_ERROR,
    /** QR scanning library not loaded */
    LIBRARY_NOT_LOADED,
    /** Unknown/unexpected error */
    UNKNOWN_ERROR
}

/**
 * Scan history item for persistence and display.
 * 
 * Stored in local database for history tab display
 * and trend analysis.
 * 
 * @property id Unique identifier for the scan
 * @property url The scanned URL
 * @property score Risk score at time of scan
 * @property verdict Verdict at time of scan
 * @property scannedAt Unix timestamp of when scan occurred
 * @property source How the QR code was scanned
 * 
 * @since 1.0.0
 */
data class ScanHistoryItem(
    /** Unique identifier (UUID) */
    val id: String,
    /** Scanned URL */
    val url: String,
    /** Risk score (0-100) */
    val score: Int,
    /** Verdict classification */
    val verdict: Verdict,
    /** Scan timestamp (Unix millis) */
    val scannedAt: Long,
    /** Scan source (camera, gallery, clipboard) */
    val source: ScanSource
)

/**
 * Source of QR code scan input.
 * 
 * @property CAMERA Live camera capture
 * @property GALLERY Image selected from photo gallery
 * @property CLIPBOARD URL pasted from clipboard
 * 
 * @since 1.0.0
 */
enum class ScanSource {
    /** Scanned via live camera */
    CAMERA,
    /** Scanned from gallery image */
    GALLERY,
    /** Pasted from clipboard */
    CLIPBOARD
}

