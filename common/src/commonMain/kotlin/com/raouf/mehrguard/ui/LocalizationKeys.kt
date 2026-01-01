/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

package com.raouf.mehrguard.ui

/**
 * Shared Localization Keys
 *
 * Centralized constants for all translatable UI strings.
 * These keys are used across all platforms to ensure:
 * - Consistent string IDs for localization
 * - Platform parity in text references
 * - Single source of truth for translatable content
 *
 * ## KMP Parity Principle
 * - All platforms use the same key constants
 * - Platform-specific resources (strings.xml, Localizable.strings) use these keys
 * - Default English text is provided as fallback
 *
 * ## Usage
 * ```kotlin
 * // In Android Compose
 * Text(stringResource(LocalizationKeys.VERDICT_SAFE.resourceId))
 *
 * // In iOS SwiftUI
 * Text(NSLocalizedString(LocalizationKeys.VERDICT_SAFE.key, comment: ""))
 *
 * // In shared code (fallback)
 * Text(LocalizationKeys.VERDICT_SAFE.defaultText)
 * ```
 *
 * @author Mehr Guard Security Team
 * @since 1.2.0
 */
object LocalizationKeys {

    // ==================== App ====================
    val APP_NAME = LocalizedKey("app_name", "Mehr Guard")
    val APP_TAGLINE = LocalizedKey("app_tagline", "Scan Smart. Stay Protected.")

    // ==================== Tabs ====================
    val TAB_SCAN = LocalizedKey("tab_scan", "Scan")
    val TAB_HISTORY = LocalizedKey("tab_history", "History")
    val TAB_SETTINGS = LocalizedKey("tab_settings", "Settings")

    // ==================== Scanner ====================
    val SCANNER_TITLE = LocalizedKey("scanner_title", "Scan QR Code")
    val SCANNER_INSTRUCTION = LocalizedKey("scanner_instruction", "Point camera at a QR code")
    val SCANNER_SCANNING = LocalizedKey("scanner_scanning", "Scanning...")
    val SCANNER_ANALYZING = LocalizedKey("scanner_analyzing", "Analyzing URL...")
    val SCANNER_NO_CAMERA = LocalizedKey("scanner_no_camera", "Camera not available")
    val SCANNER_PERMISSION_NEEDED = LocalizedKey("scanner_permission_needed", "Camera permission required")
    val SCANNER_GRANT_PERMISSION = LocalizedKey("scanner_grant_permission", "Grant Permission")
    val SCANNER_OPEN_SETTINGS = LocalizedKey("scanner_open_settings", "Open Settings")

    // ==================== Verdicts ====================
    val VERDICT_SAFE = LocalizedKey("verdict_safe", "Safe")
    val VERDICT_SUSPICIOUS = LocalizedKey("verdict_suspicious", "Suspicious")
    val VERDICT_MALICIOUS = LocalizedKey("verdict_malicious", "Dangerous")
    val VERDICT_UNKNOWN = LocalizedKey("verdict_unknown", "Unknown")

    val VERDICT_SAFE_DESC = LocalizedKey(
        "verdict_safe_desc",
        "This URL appears to be safe to visit."
    )
    val VERDICT_SUSPICIOUS_DESC = LocalizedKey(
        "verdict_suspicious_desc",
        "This URL has some suspicious characteristics. Proceed with caution."
    )
    val VERDICT_MALICIOUS_DESC = LocalizedKey(
        "verdict_malicious_desc",
        "This URL shows strong signs of phishing. Do not visit."
    )
    val VERDICT_UNKNOWN_DESC = LocalizedKey(
        "verdict_unknown_desc",
        "Unable to determine the risk level of this URL."
    )

    // ==================== Result Screen ====================
    val RESULT_TITLE = LocalizedKey("result_title", "Analysis Complete")
    val RESULT_RISK_SCORE = LocalizedKey("result_risk_score", "Risk Score")
    val RESULT_RISK_FACTORS = LocalizedKey("result_risk_factors", "Risk Factors")
    val RESULT_DETAILS = LocalizedKey("result_details", "Details")
    val RESULT_WHAT_TO_DO = LocalizedKey("result_what_to_do", "What To Do")
    val RESULT_URL = LocalizedKey("result_url", "URL")
    val RESULT_TLD = LocalizedKey("result_tld", "Top-Level Domain")
    val RESULT_BRAND_MATCH = LocalizedKey("result_brand_match", "Brand Match")

    // ==================== Actions ====================
    val ACTION_SCAN_ANOTHER = LocalizedKey("action_scan_another", "Scan Another")
    val ACTION_OPEN_URL = LocalizedKey("action_open_url", "Open URL")
    val ACTION_COPY_URL = LocalizedKey("action_copy_url", "Copy URL")
    val ACTION_SHARE = LocalizedKey("action_share", "Share")
    val ACTION_REPORT = LocalizedKey("action_report", "Report")
    val ACTION_DETAILS = LocalizedKey("action_details", "View Details")
    val ACTION_DISMISS = LocalizedKey("action_dismiss", "Dismiss")
    val ACTION_CANCEL = LocalizedKey("action_cancel", "Cancel")
    val ACTION_CONFIRM = LocalizedKey("action_confirm", "Confirm")
    val ACTION_DELETE = LocalizedKey("action_delete", "Delete")
    val ACTION_CLEAR_ALL = LocalizedKey("action_clear_all", "Clear All")

    // ==================== Warnings ====================
    val WARNING_OPEN_MALICIOUS = LocalizedKey(
        "warning_open_malicious",
        "This URL has been flagged as dangerous. Are you sure you want to open it?"
    )
    val WARNING_OPEN_SUSPICIOUS = LocalizedKey(
        "warning_open_suspicious",
        "This URL has some suspicious characteristics. Proceed with caution."
    )
    val WARNING_IRREVERSIBLE = LocalizedKey(
        "warning_irreversible",
        "This action cannot be undone."
    )

    // ==================== History ====================
    val HISTORY_TITLE = LocalizedKey("history_title", "Scan History")
    val HISTORY_EMPTY = LocalizedKey("history_empty", "No scans yet")
    val HISTORY_EMPTY_DESC = LocalizedKey(
        "history_empty_desc",
        "Scanned QR codes will appear here"
    )
    val HISTORY_CLEAR = LocalizedKey("history_clear", "Clear History")
    val HISTORY_CLEAR_CONFIRM = LocalizedKey(
        "history_clear_confirm",
        "Are you sure you want to clear all scan history?"
    )
    val HISTORY_FILTER_ALL = LocalizedKey("history_filter_all", "All")
    val HISTORY_FILTER_SAFE = LocalizedKey("history_filter_safe", "Safe")
    val HISTORY_FILTER_SUSPICIOUS = LocalizedKey("history_filter_suspicious", "Suspicious")
    val HISTORY_FILTER_MALICIOUS = LocalizedKey("history_filter_malicious", "Dangerous")
    val HISTORY_SCANNED_AT = LocalizedKey("history_scanned_at", "Scanned")

    // ==================== Settings ====================
    val SETTINGS_TITLE = LocalizedKey("settings_title", "Settings")
    val SETTINGS_GENERAL = LocalizedKey("settings_general", "General")
    val SETTINGS_PRIVACY = LocalizedKey("settings_privacy", "Privacy")
    val SETTINGS_ABOUT = LocalizedKey("settings_about", "About")

    val SETTINGS_DARK_MODE = LocalizedKey("settings_dark_mode", "Dark Mode")
    val SETTINGS_HAPTICS = LocalizedKey("settings_haptics", "Haptic Feedback")
    val SETTINGS_SOUND = LocalizedKey("settings_sound", "Sound Effects")
    val SETTINGS_AUTO_SCAN = LocalizedKey("settings_auto_scan", "Auto-Scan")
    val SETTINGS_SAVE_HISTORY = LocalizedKey("settings_save_history", "Save Scan History")
    val SETTINGS_ALERTS = LocalizedKey("settings_alerts", "Security Alerts")

    val SETTINGS_VERSION = LocalizedKey("settings_version", "Version")
    val SETTINGS_PRIVACY_POLICY = LocalizedKey("settings_privacy_policy", "Privacy Policy")
    val SETTINGS_TERMS = LocalizedKey("settings_terms", "Terms of Service")
    val SETTINGS_LICENSES = LocalizedKey("settings_licenses", "Open Source Licenses")
    val SETTINGS_GITHUB = LocalizedKey("settings_github", "View on GitHub")

    // ==================== Risk Signal Names ====================
    val SIGNAL_BRAND_IMPERSONATION = LocalizedKey("signal_brand_impersonation", "Brand Impersonation")
    val SIGNAL_SUSPICIOUS_TLD = LocalizedKey("signal_suspicious_tld", "Suspicious TLD")
    val SIGNAL_URL_SHORTENER = LocalizedKey("signal_url_shortener", "URL Shortener")
    val SIGNAL_IP_ADDRESS = LocalizedKey("signal_ip_address", "IP Address Host")
    val SIGNAL_NO_HTTPS = LocalizedKey("signal_no_https", "No Encryption")
    val SIGNAL_EXCESSIVE_SUBDOMAINS = LocalizedKey("signal_excessive_subdomains", "Excessive Subdomains")
    val SIGNAL_HOMOGRAPH = LocalizedKey("signal_homograph", "Homograph Attack")
    val SIGNAL_CREDENTIAL_PATH = LocalizedKey("signal_credential_path", "Credential Harvesting")
    val SIGNAL_PUNYCODE = LocalizedKey("signal_punycode", "Punycode Domain")
    val SIGNAL_LONG_URL = LocalizedKey("signal_long_url", "Unusually Long URL")
    val SIGNAL_HIGH_ENTROPY = LocalizedKey("signal_high_entropy", "Random Components")
    val SIGNAL_EMBEDDED_REDIRECT = LocalizedKey("signal_embedded_redirect", "Embedded Redirect")

    // ==================== Errors ====================
    val ERROR_GENERIC = LocalizedKey("error_generic", "An error occurred")
    val ERROR_NETWORK = LocalizedKey("error_network", "Network error")
    val ERROR_INVALID_URL = LocalizedKey("error_invalid_url", "Invalid URL")
    val ERROR_NO_QR_FOUND = LocalizedKey("error_no_qr_found", "No QR code found")
    val ERROR_CAMERA_UNAVAILABLE = LocalizedKey("error_camera_unavailable", "Camera unavailable")
    val ERROR_PERMISSION_DENIED = LocalizedKey("error_permission_denied", "Permission denied")

    // ==================== Accessibility ====================
    val A11Y_RISK_SCORE_LABEL = LocalizedKey("a11y_risk_score_label", "Risk score: %d out of 100")
    val A11Y_VERDICT_LABEL = LocalizedKey("a11y_verdict_label", "Verdict: %s")
    val A11Y_SCAN_BUTTON = LocalizedKey("a11y_scan_button", "Scan QR code")
    val A11Y_HISTORY_ITEM = LocalizedKey("a11y_history_item", "Scan history item")
    val A11Y_CLOSE_BUTTON = LocalizedKey("a11y_close_button", "Close")
    val A11Y_BACK_BUTTON = LocalizedKey("a11y_back_button", "Go back")

    /**
     * Get all keys as a list for validation.
     */
    fun allKeys(): List<LocalizedKey> = listOf(
        APP_NAME, APP_TAGLINE,
        TAB_SCAN, TAB_HISTORY, TAB_SETTINGS,
        SCANNER_TITLE, SCANNER_INSTRUCTION, SCANNER_SCANNING, SCANNER_ANALYZING,
        SCANNER_NO_CAMERA, SCANNER_PERMISSION_NEEDED, SCANNER_GRANT_PERMISSION, SCANNER_OPEN_SETTINGS,
        VERDICT_SAFE, VERDICT_SUSPICIOUS, VERDICT_MALICIOUS, VERDICT_UNKNOWN,
        VERDICT_SAFE_DESC, VERDICT_SUSPICIOUS_DESC, VERDICT_MALICIOUS_DESC, VERDICT_UNKNOWN_DESC,
        RESULT_TITLE, RESULT_RISK_SCORE, RESULT_RISK_FACTORS, RESULT_DETAILS,
        RESULT_WHAT_TO_DO, RESULT_URL, RESULT_TLD, RESULT_BRAND_MATCH,
        ACTION_SCAN_ANOTHER, ACTION_OPEN_URL, ACTION_COPY_URL, ACTION_SHARE,
        ACTION_REPORT, ACTION_DETAILS, ACTION_DISMISS, ACTION_CANCEL, ACTION_CONFIRM, ACTION_DELETE, ACTION_CLEAR_ALL,
        HISTORY_TITLE, HISTORY_EMPTY, HISTORY_EMPTY_DESC, HISTORY_CLEAR, HISTORY_CLEAR_CONFIRM,
        SETTINGS_TITLE, SETTINGS_GENERAL, SETTINGS_PRIVACY, SETTINGS_ABOUT,
        ERROR_GENERIC, ERROR_NETWORK, ERROR_INVALID_URL, ERROR_NO_QR_FOUND
    )
}

/**
 * Data class representing a localized string key.
 *
 * @property key The string resource key (used in platform resources)
 * @property defaultText The default English text (fallback)
 */
data class LocalizedKey(
    val key: String,
    val defaultText: String
)
