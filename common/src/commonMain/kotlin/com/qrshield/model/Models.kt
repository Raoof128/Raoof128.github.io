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
 * Verdict enum representing the final risk classification
 */
enum class Verdict {
    SAFE,
    SUSPICIOUS,
    MALICIOUS,
    UNKNOWN
}

/**
 * Complete risk assessment result
 */
data class RiskAssessment(
    val score: Int,
    val verdict: Verdict,
    val flags: List<String>,
    val details: UrlAnalysisResult,
    val confidence: Float = 0.8f
) {
    val scoreDescription: String
        get() = when {
            score <= 30 -> "Low Risk"
            score <= 70 -> "Medium Risk"
            else -> "High Risk"
        }
    
    val actionRecommendation: String
        get() = when (verdict) {
            Verdict.SAFE -> "This URL appears safe to visit."
            Verdict.SUSPICIOUS -> "Proceed with caution. Verify the source before clicking."
            Verdict.MALICIOUS -> "Do not visit this URL. It shows strong phishing indicators."
            Verdict.UNKNOWN -> "Unable to fully analyze. Verify manually before visiting."
        }
}

/**
 * Detailed URL analysis breakdown
 */
data class UrlAnalysisResult(
    val originalUrl: String,
    val heuristicScore: Int,
    val mlScore: Int,
    val brandScore: Int,
    val tldScore: Int,
    val brandMatch: String? = null,
    val tld: String? = null
) {
    companion object {
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
 * QR scan result wrapper
 */
sealed class ScanResult {
    data class Success(
        val content: String,
        val contentType: ContentType
    ) : ScanResult()
    
    data class Error(
        val message: String,
        val code: ErrorCode
    ) : ScanResult()
    
    data object NoQrFound : ScanResult()
}

enum class ContentType {
    URL,
    TEXT,
    WIFI,
    VCARD,
    GEO,
    PHONE,
    SMS,
    EMAIL,
    UNKNOWN
}

enum class ErrorCode {
    CAMERA_PERMISSION_DENIED,
    CAMERA_NOT_AVAILABLE,
    CAMERA_ERROR,
    IMAGE_DECODE_ERROR,
    IMAGE_TOO_LARGE,
    INVALID_IMAGE,
    INVALID_QR_FORMAT,
    DECODE_ERROR,
    CONTENT_TOO_LARGE,
    ML_KIT_ERROR,
    VISION_ERROR,
    LIBRARY_NOT_LOADED,
    UNKNOWN_ERROR
}

/**
 * Scan history item for persistence
 */
data class ScanHistoryItem(
    val id: String,
    val url: String,
    val score: Int,
    val verdict: Verdict,
    val scannedAt: Long,
    val source: ScanSource
)

enum class ScanSource {
    CAMERA,
    GALLERY,
    CLIPBOARD
}
