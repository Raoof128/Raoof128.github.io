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

package com.raouf.mehrguard.core

/**
 * Centralized Error Messages for QR-SHIELD
 *
 * All user-facing and internal error messages in one place for:
 * - Easy internationalization (i18n)
 * - Consistent messaging across platforms
 * - Centralized maintenance
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
object ErrorMessages {

    // =========================================================================
    // SCANNER ERRORS
    // =========================================================================

    object Scanner {
        const val CAMERA_PERMISSION_DENIED = "Camera access is required to scan QR codes. Please enable camera permission in settings."
        const val CAMERA_NOT_AVAILABLE = "Camera is not available on this device."
        const val CAMERA_ERROR = "An error occurred while accessing the camera."
        const val CAMERA_INITIALIZATION_FAILED = "Failed to initialize camera."

        const val NO_QR_FOUND = "No QR code found in the image."
        const val DECODE_ERROR = "Unable to decode QR code."
        const val INVALID_QR_FORMAT = "Invalid QR code format."

        const val IMAGE_TOO_LARGE = "Image size exceeds maximum allowed size."
        const val IMAGE_DECODE_ERROR = "Unable to decode image."
        const val INVALID_IMAGE = "Invalid image format."

        const val CONTENT_TOO_LARGE = "QR code content exceeds maximum allowed length."

        const val ML_KIT_ERROR = "ML Kit barcode scanner error."
        const val VISION_ERROR = "Vision framework error."
        const val LIBRARY_NOT_LOADED = "QR scanning library not loaded."
    }

    // =========================================================================
    // ANALYSIS ERRORS
    // =========================================================================

    object Analysis {
        const val URL_PARSE_FAILED = "Unable to parse URL."
        const val URL_TOO_LONG = "URL exceeds maximum safe length."
        const val URL_EMPTY = "URL cannot be empty."
        const val URL_INVALID = "Invalid URL format."

        const val ANALYSIS_FAILED = "Failed to analyze URL."
        const val ANALYSIS_TIMEOUT = "Analysis timed out."

        const val NETWORK_REQUIRED = "Network connection required for analysis."
    }

    // =========================================================================
    // HISTORY ERRORS
    // =========================================================================

    object History {
        const val SAVE_FAILED = "Failed to save scan to history."
        const val DELETE_FAILED = "Failed to delete scan from history."
        const val CLEAR_FAILED = "Failed to clear history."
        const val LOAD_FAILED = "Failed to load scan history."
        const val NOT_FOUND = "Scan not found in history."

        const val DATABASE_ERROR = "Database error occurred."
        const val DATABASE_MIGRATION_FAILED = "Failed to migrate database."
    }

    // =========================================================================
    // SETTINGS ERRORS
    // =========================================================================

    object Settings {
        const val SAVE_FAILED = "Failed to save settings."
        const val LOAD_FAILED = "Failed to load settings."
        const val INVALID_VALUE = "Invalid setting value."
    }

    // =========================================================================
    // SHARE ERRORS
    // =========================================================================

    object Share {
        const val NO_RESULT = "No analysis result to share."
        const val SHARE_FAILED = "Failed to share result."
        const val COPY_FAILED = "Failed to copy to clipboard."
        const val EXPORT_FAILED = "Failed to export data."
    }

    // =========================================================================
    // NETWORK ERRORS
    // =========================================================================

    object Network {
        const val NO_CONNECTION = "No internet connection."
        const val CONNECTION_TIMEOUT = "Connection timed out."
        const val SERVER_ERROR = "Server error occurred."
        const val UNKNOWN_ERROR = "An unknown network error occurred."
    }

    // =========================================================================
    // PERMISSION ERRORS
    // =========================================================================

    object Permission {
        const val CAMERA_REQUIRED = "Camera permission is required."
        const val STORAGE_REQUIRED = "Storage permission is required."
        const val DENIED_PERMANENTLY = "Permission denied permanently. Please enable in Settings."
    }

    // =========================================================================
    // GENERIC ERRORS
    // =========================================================================

    object Generic {
        const val UNKNOWN_ERROR = "An unknown error occurred."
        const val OPERATION_FAILED = "Operation failed. Please try again."
        const val INVALID_INPUT = "Invalid input provided."
        const val INTERNAL_ERROR = "Internal error occurred."
        const val FEATURE_NOT_AVAILABLE = "This feature is not available."
    }

    // =========================================================================
    // HELPER FUNCTIONS
    // =========================================================================

    /**
     * Get error message for error code.
     */
    fun forCode(code: com.raouf.mehrguard.model.ErrorCode): String {
        return when (code) {
            com.raouf.mehrguard.model.ErrorCode.CAMERA_PERMISSION_DENIED -> Scanner.CAMERA_PERMISSION_DENIED
            com.raouf.mehrguard.model.ErrorCode.CAMERA_NOT_AVAILABLE -> Scanner.CAMERA_NOT_AVAILABLE
            com.raouf.mehrguard.model.ErrorCode.CAMERA_ERROR -> Scanner.CAMERA_ERROR
            com.raouf.mehrguard.model.ErrorCode.IMAGE_DECODE_ERROR -> Scanner.IMAGE_DECODE_ERROR
            com.raouf.mehrguard.model.ErrorCode.IMAGE_TOO_LARGE -> Scanner.IMAGE_TOO_LARGE
            com.raouf.mehrguard.model.ErrorCode.INVALID_IMAGE -> Scanner.INVALID_IMAGE
            com.raouf.mehrguard.model.ErrorCode.INVALID_QR_FORMAT -> Scanner.INVALID_QR_FORMAT
            com.raouf.mehrguard.model.ErrorCode.DECODE_ERROR -> Scanner.DECODE_ERROR
            com.raouf.mehrguard.model.ErrorCode.CONTENT_TOO_LARGE -> Scanner.CONTENT_TOO_LARGE
            com.raouf.mehrguard.model.ErrorCode.ML_KIT_ERROR -> Scanner.ML_KIT_ERROR
            com.raouf.mehrguard.model.ErrorCode.VISION_ERROR -> Scanner.VISION_ERROR
            com.raouf.mehrguard.model.ErrorCode.LIBRARY_NOT_LOADED -> Scanner.LIBRARY_NOT_LOADED
            com.raouf.mehrguard.model.ErrorCode.UNKNOWN_ERROR -> Generic.UNKNOWN_ERROR
        }
    }
}
