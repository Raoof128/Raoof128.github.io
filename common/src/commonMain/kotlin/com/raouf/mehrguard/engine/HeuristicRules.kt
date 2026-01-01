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

package com.raouf.mehrguard.engine

/**
 * Heuristic Rule Weights and Configuration
 *
 * Centralized configuration for all security heuristic weights.
 * This allows easy tuning of the phishing detection sensitivity.
 *
 * Weight scale:
 * - 0-10: Low impact indicators
 * - 11-25: Medium impact indicators
 * - 26-50: High impact indicators
 * - 51+: Critical indicators
 *
 * @author Mehr Guard Security Team
 * @since 1.0.0
 */
object HeuristicWeights {

    // === PROTOCOL WEIGHTS ===

    /** HTTP instead of HTTPS - moderate risk */
    const val HTTP_NOT_HTTPS = 30

    // === HOST WEIGHTS ===

    /** IP address as hostname - high risk */
    const val IP_ADDRESS = 50

    /** URL shortener domain - moderate risk */
    const val SHORTENER = 15

    /** Excessive subdomains (>3) - low risk */
    const val EXCESSIVE_SUBDOMAINS = 10

    /** Non-standard port - moderate risk */
    const val NON_STANDARD_PORT = 15

    /** Punycode/IDN domain - high risk */
    const val PUNYCODE = 30

    /** Numeric-only subdomain - moderate risk */
    const val NUMERIC_SUBDOMAIN = 20

    /** Multiple TLD-like segments - high risk */
    const val MULTI_TLD = 25

    // === URL STRUCTURE WEIGHTS ===

    /** Excessively long URL (>100 chars) - low risk */
    const val LONG_URL = 10

    /** High entropy (random-looking) domain - moderate risk */
    const val HIGH_ENTROPY = 20

    /** @ symbol in URL (deceptive) - critical risk */
    const val AT_SYMBOL = 60

    // === QUERY PARAMETER WEIGHTS ===

    /** Credential-related parameters - high risk */
    const val CREDENTIAL_PARAMS = 40

    /** Base64 encoded data in URL - high risk */
    const val BASE64_PAYLOAD = 30

    /** Excessive URL encoding - moderate risk */
    const val EXCESSIVE_ENCODING = 20

    // === FILE EXTENSION WEIGHTS ===

    /** Risky file extension (.exe, .scr, etc.) - high risk */
    const val RISKY_EXTENSION = 40

    /** Double extension attack (file.pdf.exe) - high risk */
    const val DOUBLE_EXTENSION = 40
}

/**
 * Reference Data for Heuristic Analysis
 *
 * Contains curated lists of patterns used for threat detection.
 */
object HeuristicData {

    /** Standard web ports that are not suspicious */
    val STANDARD_PORTS = setOf(80, 443, 8080, 8443)

    /** Common legitimate TLDs */
    val COMMON_TLDS = setOf(
        "com", "org", "net", "edu", "gov", "io", "co", "us", "uk",
        "app", "dev", "xyz", "info", "biz", "me", "tv", "cc"
    )

    /** Risky file extensions that may indicate malware */
    val RISKY_EXTENSIONS = setOf(
        ".exe", ".scr", ".bat", ".cmd", ".ps1", ".msi", ".com",
        ".pif", ".vbs", ".vbe", ".js", ".jse", ".ws", ".wsf",
        ".hta", ".cpl", ".msc", ".jar", ".app", ".dmg"
    )

    /** Safe file extensions */
    val SAFE_EXTENSIONS = setOf(
        ".html", ".htm", ".css", ".json", ".xml", ".txt",
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
        ".png", ".jpg", ".jpeg", ".gif", ".svg", ".webp",
        ".mp3", ".mp4", ".wav", ".webm", ".ogg"
    )

    /** Known URL shortener domains */
    val SHORTENER_DOMAINS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly",
        "is.gd", "buff.ly", "adf.ly", "j.mp", "tr.im",
        "short.link", "cutt.ly", "rb.gy", "shorturl.at",
        "rebrand.ly", "bl.ink", "soo.gd", "s.id"
    )

    /** Credential-related parameter names */
    val CREDENTIAL_PARAMS = setOf(
        "password", "passwd", "pwd", "pass",
        "token", "auth", "access_token", "refresh_token",
        "secret", "key", "api_key", "apikey",
        "session", "sessionid", "session_id",
        "ssn", "credit_card", "cc", "cvv",
        "pin", "otp", "2fa", "mfa"
    )

    /** High entropy threshold for domain randomness detection */
    const val ENTROPY_THRESHOLD = 4.0

    /** Maximum safe URL length */
    const val MAX_SAFE_URL_LENGTH = 2000

    /** Maximum subdomains before flagging */
    const val MAX_SAFE_SUBDOMAINS = 3
}
