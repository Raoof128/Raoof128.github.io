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

package com.raouf.mehrguard.policy

/**
 * QR Code Payload Types
 *
 * Represents all standard and common QR code payload formats.
 * Used for payload classification and type-specific risk analysis.
 *
 * ## Standard Formats
 * - URL (http/https)
 * - WiFi configuration (WIFI:)
 * - vCard (BEGIN:VCARD)
 * - Calendar event (BEGIN:VEVENT)
 * - SMS (smsto:/sms:)
 * - Phone (tel:)
 * - Email (mailto:)
 * - Geo location (geo:)
 *
 * ## Payment Formats
 * - Bitcoin (bitcoin:)
 * - Ethereum (ethereum:)
 * - PayPal (paypal.me)
 * - UPI (upi://)
 * - WeChat Pay
 * - Alipay
 *
 * @author QR-SHIELD Security Team
 * @since 1.2.0
 */
enum class QrPayloadType(
    val displayName: String,
    val riskLevel: RiskLevel,
    val description: String
) {
    // URLs
    URL(
        displayName = "Web URL",
        riskLevel = RiskLevel.MEDIUM,
        description = "Standard web URL (analyzed for phishing)"
    ),
    URL_HTTP(
        displayName = "HTTP URL (Insecure)",
        riskLevel = RiskLevel.HIGH,
        description = "Insecure HTTP URL - data not encrypted"
    ),
    URL_HTTPS(
        displayName = "HTTPS URL (Secure)",
        riskLevel = RiskLevel.LOW,
        description = "Secure HTTPS URL with encryption"
    ),
    
    // Communication
    SMS(
        displayName = "SMS Message",
        riskLevel = RiskLevel.HIGH,
        description = "SMS message with phone number - check for smishing"
    ),
    PHONE(
        displayName = "Phone Call",
        riskLevel = RiskLevel.MEDIUM,
        description = "Phone number for calling"
    ),
    EMAIL(
        displayName = "Email",
        riskLevel = RiskLevel.MEDIUM,
        description = "Email address or mailto: link"
    ),
    
    // Contact/Calendar
    VCARD(
        displayName = "Contact Card",
        riskLevel = RiskLevel.MEDIUM,
        description = "vCard contact information"
    ),
    MECARD(
        displayName = "MeCard Contact",
        riskLevel = RiskLevel.MEDIUM,
        description = "Japanese MeCard contact format"
    ),
    VEVENT(
        displayName = "Calendar Event",
        riskLevel = RiskLevel.LOW,
        description = "iCalendar event"
    ),
    
    // Network
    WIFI(
        displayName = "WiFi Network",
        riskLevel = RiskLevel.HIGH,
        description = "WiFi configuration - may expose or change network settings"
    ),
    
    // Location
    GEO(
        displayName = "Geographic Location",
        riskLevel = RiskLevel.LOW,
        description = "GPS coordinates"
    ),
    
    // Payment - Cryptocurrency
    BITCOIN(
        displayName = "Bitcoin Payment",
        riskLevel = RiskLevel.CRITICAL,
        description = "Bitcoin payment request - verify address carefully!"
    ),
    ETHEREUM(
        displayName = "Ethereum Payment",
        riskLevel = RiskLevel.CRITICAL,
        description = "Ethereum payment request - verify address carefully!"
    ),
    CRYPTO_OTHER(
        displayName = "Cryptocurrency",
        riskLevel = RiskLevel.CRITICAL,
        description = "Other cryptocurrency payment"
    ),
    
    // Payment - Traditional
    UPI(
        displayName = "UPI Payment (India)",
        riskLevel = RiskLevel.HIGH,
        description = "Unified Payments Interface payment"
    ),
    PAYPAL(
        displayName = "PayPal",
        riskLevel = RiskLevel.HIGH,
        description = "PayPal payment request"
    ),
    WECHAT_PAY(
        displayName = "WeChat Pay",
        riskLevel = RiskLevel.HIGH,
        description = "WeChat Pay payment"
    ),
    ALIPAY(
        displayName = "Alipay",
        riskLevel = RiskLevel.HIGH,
        description = "Alipay payment"
    ),
    
    // Plain text
    TEXT(
        displayName = "Plain Text",
        riskLevel = RiskLevel.LOW,
        description = "Plain text content"
    ),
    
    // Unknown/Other
    UNKNOWN(
        displayName = "Unknown Format",
        riskLevel = RiskLevel.MEDIUM,
        description = "Unrecognized QR code format"
    );
    
    companion object {
        /**
         * Detect payload type from QR code content.
         *
         * @param content Raw QR code content
         * @return Detected QrPayloadType
         */
        fun detect(content: String): QrPayloadType {
            val trimmed = content.trim()
            val lower = trimmed.lowercase()
            
            return when {
                // WiFi Configuration
                lower.startsWith("wifi:") -> WIFI
                
                // vCard/MeCard
                lower.startsWith("begin:vcard") -> VCARD
                lower.startsWith("mecard:") -> MECARD
                
                // Calendar
                lower.startsWith("begin:vevent") -> VEVENT
                
                // SMS/Phone
                lower.startsWith("smsto:") || lower.startsWith("sms:") -> SMS
                lower.startsWith("tel:") -> PHONE
                
                // Email
                lower.startsWith("mailto:") -> EMAIL
                lower.startsWith("matmsg:") -> EMAIL
                
                // Geo
                lower.startsWith("geo:") -> GEO
                
                // Cryptocurrency
                lower.startsWith("bitcoin:") -> BITCOIN
                lower.startsWith("ethereum:") -> ETHEREUM
                lower.startsWith("litecoin:") || 
                lower.startsWith("dogecoin:") ||
                lower.startsWith("monero:") ||
                lower.startsWith("ripple:") -> CRYPTO_OTHER
                
                // Payment platforms
                lower.startsWith("upi://") -> UPI
                lower.contains("paypal.me") || lower.startsWith("paypal://") -> PAYPAL
                lower.startsWith("weixin://") || lower.contains("wx.tenpay") -> WECHAT_PAY
                lower.startsWith("alipay://") || lower.contains("alipay.com") -> ALIPAY
                
                // URLs
                lower.startsWith("https://") -> URL_HTTPS
                lower.startsWith("http://") -> URL_HTTP
                lower.startsWith("//") -> URL
                
                // Check for URL-like content without protocol
                looksLikeUrl(lower) -> URL
                
                // Plain text
                trimmed.isNotEmpty() -> TEXT
                
                else -> UNKNOWN
            }
        }
        
        /**
         * Check if content looks like a URL without protocol.
         */
        private fun looksLikeUrl(content: String): Boolean {
            // Common TLDs check
            val commonTlds = listOf(
                ".com", ".org", ".net", ".edu", ".gov", ".io", ".co", 
                ".app", ".dev", ".me", ".info", ".biz", ".xyz"
            )
            return commonTlds.any { content.contains(it) } &&
                   content.contains(".") &&
                   !content.contains(" ")
        }
        
        /**
         * Get all payment-related types.
         */
        val paymentTypes: Set<QrPayloadType> = setOf(
            BITCOIN, ETHEREUM, CRYPTO_OTHER, UPI, PAYPAL, WECHAT_PAY, ALIPAY
        )
        
        /**
         * Get all contact-related types.
         */
        val contactTypes: Set<QrPayloadType> = setOf(
            VCARD, MECARD, PHONE, EMAIL
        )
        
        /**
         * Get all URL-related types.
         */
        val urlTypes: Set<QrPayloadType> = setOf(URL, URL_HTTP, URL_HTTPS)
    }
}

/**
 * Risk level for payload types.
 */
enum class RiskLevel(val weight: Int) {
    LOW(0),
    MEDIUM(10),
    HIGH(25),
    CRITICAL(50)
}
