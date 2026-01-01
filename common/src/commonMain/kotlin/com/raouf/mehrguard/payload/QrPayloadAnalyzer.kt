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

package com.raouf.mehrguard.payload

import com.raouf.mehrguard.policy.QrPayloadType
import com.raouf.mehrguard.policy.RiskLevel

/**
 * QR Payload Analyzer
 *
 * Analyzes non-URL QR code payloads for security risks.
 * Covers WiFi configurations, vCards, SMS, payment URIs, and more.
 *
 * ## Payload-Specific Attack Patterns
 *
 * ### WiFi (WIFI:)
 * - Open network without password (captive portal attacks)
 * - Fake corporate WiFi names (credential harvesting)
 * - Hidden network joining (privacy risk)
 * - Password exfiltration via fake hotspot name
 *
 * ### vCard (BEGIN:VCARD)
 * - Embedded malicious URLs
 * - Fake contact impersonation (CEO fraud prep)
 * - Excessive data collection (phone, email, address)
 *
 * ### SMS (sms:)
 * - Premium rate number scams
 * - Smishing URLs in message body
 * - Fake bank/service messages
 *
 * ### Payment URIs (bitcoin:, ethereum:, upi:)
 * - Address replacement attacks
 * - Dust attacks (small amounts to track)
 * - Smart contract interaction URLs
 *
 * @author Mehr Guard Security Team
 * @since 1.2.0
 */
object QrPayloadAnalyzer {
    
    /**
     * Analyze any QR payload for security risks.
     *
     * @param content Raw QR code content
     * @return PayloadAnalysisResult with type, score, and signals
     */
    fun analyze(content: String): PayloadAnalysisResult {
        val payloadType = QrPayloadType.detect(content)
        
        return when (payloadType) {
            QrPayloadType.WIFI -> analyzeWifi(content)
            QrPayloadType.VCARD, QrPayloadType.MECARD -> analyzeContact(content, payloadType)
            QrPayloadType.SMS -> analyzeSms(content)
            QrPayloadType.PHONE -> analyzePhone(content)
            QrPayloadType.EMAIL -> analyzeEmail(content)
            QrPayloadType.BITCOIN, QrPayloadType.ETHEREUM, QrPayloadType.CRYPTO_OTHER -> 
                analyzeCrypto(content, payloadType)
            QrPayloadType.UPI, QrPayloadType.PAYPAL, QrPayloadType.WECHAT_PAY, QrPayloadType.ALIPAY ->
                analyzePayment(content, payloadType)
            QrPayloadType.GEO -> analyzeGeo(content)
            QrPayloadType.VEVENT -> analyzeCalendar(content)
            QrPayloadType.URL, QrPayloadType.URL_HTTP, QrPayloadType.URL_HTTPS -> 
                PayloadAnalysisResult(
                    payloadType = payloadType,
                    riskScore = if (payloadType == QrPayloadType.URL_HTTP) 20 else 0,
                    signals = if (payloadType == QrPayloadType.URL_HTTP) {
                        listOf(PayloadSignal("HTTP (Insecure)", "URL uses unencrypted HTTP", 20))
                    } else emptyList(),
                    recommendation = "Analyze with URL phishing engine",
                    parsedData = mapOf("url" to content)
                )
            QrPayloadType.TEXT -> analyzeText(content)
            QrPayloadType.UNKNOWN -> PayloadAnalysisResult(
                payloadType = payloadType,
                riskScore = 10,
                signals = listOf(PayloadSignal("Unknown Format", "Unrecognized QR payload format", 10)),
                recommendation = "Exercise caution with unknown content"
            )
        }
    }
    
    /**
     * Analyze WiFi configuration payload.
     *
     * Format: WIFI:T:WPA;S:NetworkName;P:Password;H:true;;
     * - T = Authentication type (WPA, WEP, nopass)
     * - S = SSID (network name)
     * - P = Password
     * - H = Hidden network (true/false)
     */
    fun analyzeWifi(content: String): PayloadAnalysisResult {
        val signals = mutableListOf<PayloadSignal>()
        var riskScore = QrPayloadType.WIFI.riskLevel.weight
        
        // Parse WiFi components
        val authType = extractWifiField(content, "T")
        val ssid = extractWifiField(content, "S")
        val password = extractWifiField(content, "P")
        val hidden = extractWifiField(content, "H")
        
        // 1. Open network (no password)
        if (authType.isNullOrEmpty() || authType.uppercase() == "NOPASS" || password.isNullOrEmpty()) {
            signals.add(PayloadSignal(
                name = "Open Network",
                description = "No password required - vulnerable to man-in-the-middle attacks",
                riskPoints = 35
            ))
            riskScore += 35
        }
        
        // 2. Weak encryption (WEP)
        if (authType?.uppercase() == "WEP") {
            signals.add(PayloadSignal(
                name = "Weak Encryption (WEP)",
                description = "WEP is deprecated and easily cracked",
                riskPoints = 25
            ))
            riskScore += 25
        }
        
        // 3. Hidden network
        if (hidden?.lowercase() == "true") {
            signals.add(PayloadSignal(
                name = "Hidden Network",
                description = "Hidden SSID can indicate rogue access point",
                riskPoints = 10
            ))
            riskScore += 10
        }
        
        // 4. Suspicious SSID patterns
        if (ssid != null) {
            val suspiciousSsidPatterns = listOf(
                "free" to "Free WiFi often used for credential harvesting",
                "guest" to "Guest networks may have monitoring",
                "airport" to "Public location impersonation",
                "hotel" to "Hotel WiFi impersonation",
                "starbucks" to "Brand impersonation",
                "mcdonalds" to "Brand impersonation",
                "bank" to "Financial institution impersonation",
                "corporate" to "Corporate network impersonation"
            )
            
            val ssidLower = ssid.lowercase()
            suspiciousSsidPatterns.forEach { (pattern, description) ->
                if (ssidLower.contains(pattern)) {
                    signals.add(PayloadSignal(
                        name = "Suspicious SSID: $pattern",
                        description = description,
                        riskPoints = 15
                    ))
                    riskScore += 15
                }
            }
            
            // Check for SSID that looks like password exfiltration
            if (ssid.length > 30 && ssid.matches(Regex(".*[A-Za-z0-9]{20,}.*"))) {
                signals.add(PayloadSignal(
                    name = "Potential Data Exfiltration",
                    description = "SSID contains suspicious encoded data",
                    riskPoints = 30
                ))
                riskScore += 30
            }
        }
        
        return PayloadAnalysisResult(
            payloadType = QrPayloadType.WIFI,
            riskScore = riskScore.coerceIn(0, 100),
            signals = signals,
            parsedData = mapOf(
                "authType" to (authType ?: "unknown"),
                "ssid" to (ssid ?: ""),
                "hasPassword" to (password?.isNotEmpty() == true).toString(),
                "hidden" to (hidden ?: "false")
            ),
            recommendation = when {
                riskScore >= 50 -> "DO NOT CONNECT - High risk network configuration"
                riskScore >= 25 -> "Exercise caution - verify network authenticity"
                else -> "Network configuration appears normal"
            }
        )
    }
    
    /**
     * Analyze vCard/MeCard contact payload.
     */
    fun analyzeContact(content: String, type: QrPayloadType): PayloadAnalysisResult {
        val signals = mutableListOf<PayloadSignal>()
        var riskScore = type.riskLevel.weight
        
        // Extract contact fields
        val name = extractVcardField(content, "FN") ?: extractVcardField(content, "N")
        val email = extractVcardField(content, "EMAIL")
        val phone = extractVcardField(content, "TEL")
        val url = extractVcardField(content, "URL")
        val org = extractVcardField(content, "ORG")
        val title = extractVcardField(content, "TITLE")
        
        // 1. Check for embedded URLs
        if (url != null) {
            signals.add(PayloadSignal(
                name = "Contains URL",
                description = "Contact card contains a URL - verify before visiting",
                riskPoints = 15
            ))
            riskScore += 15
            
            // Check if URL looks suspicious
            if (url.contains("bit.ly") || url.contains("tinyurl") || url.count { it == '.' } > 4) {
                signals.add(PayloadSignal(
                    name = "Suspicious URL",
                    description = "URL appears shortened or has excessive subdomains",
                    riskPoints = 20
                ))
                riskScore += 20
            }
        }
        
        // 2. Check for executive impersonation patterns
        val executiveTitles = listOf("ceo", "cfo", "cto", "president", "director", "executive")
        if (title != null && executiveTitles.any { title.lowercase().contains(it) }) {
            signals.add(PayloadSignal(
                name = "Executive Title",
                description = "Contact claims executive position - verify authenticity",
                riskPoints = 15
            ))
            riskScore += 15
        }
        
        // 3. Check for known brand/company impersonation
        val sensitiveOrgs = listOf(
            "bank", "paypal", "google", "microsoft", "apple", "amazon",
            "facebook", "meta", "irs", "tax", "government"
        )
        if (org != null && sensitiveOrgs.any { org.lowercase().contains(it) }) {
            signals.add(PayloadSignal(
                name = "Sensitive Organization",
                description = "Claims affiliation with $org - verify before trusting",
                riskPoints = 20
            ))
            riskScore += 20
        }
        
        // 4. Check for premium rate phone numbers
        if (phone != null && isPremiumRateNumber(phone)) {
            signals.add(PayloadSignal(
                name = "Premium Rate Number",
                description = "Phone number may incur charges",
                riskPoints = 25
            ))
            riskScore += 25
        }
        
        return PayloadAnalysisResult(
            payloadType = type,
            riskScore = riskScore.coerceIn(0, 100),
            signals = signals,
            parsedData = mapOf(
                "name" to (name ?: ""),
                "email" to (email ?: ""),
                "phone" to (phone ?: ""),
                "url" to (url ?: ""),
                "org" to (org ?: "")
            ),
            recommendation = when {
                riskScore >= 50 -> "Verify this contact through official channels before saving"
                riskScore >= 25 -> "Review contact details carefully before saving"
                else -> "Contact appears normal - always verify before sharing sensitive info"
            }
        )
    }
    
    /**
     * Analyze SMS payload.
     *
     * Format: sms:+1234567890?body=Message%20text
     * or: smsto:+1234567890:Message text
     */
    fun analyzeSms(content: String): PayloadAnalysisResult {
        val signals = mutableListOf<PayloadSignal>()
        var riskScore = QrPayloadType.SMS.riskLevel.weight
        
        // Extract phone number and body
        val phoneMatch = Regex("""(?:sms(?:to)?:)([^?:]+)""", RegexOption.IGNORE_CASE).find(content)
        val phone = phoneMatch?.groupValues?.get(1)?.trim()
        
        val bodyMatch = Regex("""(?:body=|:)(.+)$""", RegexOption.IGNORE_CASE).find(content)
        val body = bodyMatch?.groupValues?.get(1)?.let { decodeUrlComponent(it) }
        
        // 1. Check for premium rate numbers
        if (phone != null && isPremiumRateNumber(phone)) {
            signals.add(PayloadSignal(
                name = "Premium Rate Number",
                description = "SMS may incur significant charges",
                riskPoints = 35
            ))
            riskScore += 35
        }
        
        // 2. Check for URLs in message body (smishing)
        if (body != null) {
            val urlPattern = Regex("""https?://[^\s]+""", RegexOption.IGNORE_CASE)
            val urls = urlPattern.findAll(body).toList()
            
            if (urls.isNotEmpty()) {
                signals.add(PayloadSignal(
                    name = "URL in SMS Body",
                    description = "Message contains ${urls.size} URL(s) - common smishing pattern",
                    riskPoints = 30
                ))
                riskScore += 30
                
                // Check for shortened URLs
                val shorteners = listOf("bit.ly", "t.co", "tinyurl", "goo.gl", "ow.ly")
                if (urls.any { url -> shorteners.any { url.value.contains(it) } }) {
                    signals.add(PayloadSignal(
                        name = "Shortened URL in SMS",
                        description = "Shortened URLs hide the true destination",
                        riskPoints = 20
                    ))
                    riskScore += 20
                }
            }
            
            // 3. Check for urgency keywords (social engineering)
            val urgencyKeywords = listOf(
                "urgent", "immediately", "verify", "suspended", "locked",
                "confirm", "expires", "limited time", "act now", "click"
            )
            if (urgencyKeywords.any { body.lowercase().contains(it) }) {
                signals.add(PayloadSignal(
                    name = "Urgency Language",
                    description = "Message uses social engineering tactics",
                    riskPoints = 15
                ))
                riskScore += 15
            }
            
            // 4. Check for financial keywords
            val financialKeywords = listOf(
                "bank", "account", "payment", "credit", "debit", 
                "transfer", "refund", "prize", "winner", "lottery"
            )
            if (financialKeywords.any { body.lowercase().contains(it) }) {
                signals.add(PayloadSignal(
                    name = "Financial Keywords",
                    description = "Message mentions financial topics - verify sender",
                    riskPoints = 20
                ))
                riskScore += 20
            }
        }
        
        return PayloadAnalysisResult(
            payloadType = QrPayloadType.SMS,
            riskScore = riskScore.coerceIn(0, 100),
            signals = signals,
            parsedData = mapOf(
                "phone" to (phone ?: ""),
                "body" to (body ?: ""),
                "bodyLength" to (body?.length ?: 0).toString()
            ),
            recommendation = when {
                riskScore >= 60 -> "DO NOT SEND - High probability of smishing/scam"
                riskScore >= 35 -> "Verify recipient before sending this message"
                else -> "SMS appears normal - verify phone number before sending"
            }
        )
    }
    
    /**
     * Analyze phone number payload.
     */
    fun analyzePhone(content: String): PayloadAnalysisResult {
        val phone = content.removePrefix("tel:").trim()
        val signals = mutableListOf<PayloadSignal>()
        var riskScore = QrPayloadType.PHONE.riskLevel.weight
        
        if (isPremiumRateNumber(phone)) {
            signals.add(PayloadSignal(
                name = "Premium Rate Number",
                description = "This number may incur significant per-minute charges",
                riskPoints = 40
            ))
            riskScore += 40
        }
        
        // Check for international premium numbers
        val premiumPrefixes = listOf("+44900", "+441011", "+1900", "+1976", "+44909")
        if (premiumPrefixes.any { phone.startsWith(it) }) {
            signals.add(PayloadSignal(
                name = "International Premium Number",
                description = "Known premium rate international prefix",
                riskPoints = 35
            ))
            riskScore += 35
        }
        
        return PayloadAnalysisResult(
            payloadType = QrPayloadType.PHONE,
            riskScore = riskScore.coerceIn(0, 100),
            signals = signals,
            parsedData = mapOf("phone" to phone),
            recommendation = when {
                riskScore >= 40 -> "WARNING: Premium rate number - may incur significant charges"
                else -> "Verify the phone number belongs to who you expect"
            }
        )
    }
    
    /**
     * Analyze email payload.
     */
    fun analyzeEmail(content: String): PayloadAnalysisResult {
        val signals = mutableListOf<PayloadSignal>()
        var riskScore = QrPayloadType.EMAIL.riskLevel.weight
        
        // Parse mailto: format
        val cleaned = content.removePrefix("mailto:").removePrefix("MATMSG:")
        val emailMatch = Regex("""([^?;]+)""").find(cleaned)
        val email = emailMatch?.groupValues?.get(1)?.trim()
        
        // Check for suspicious email patterns
        if (email != null) {
            // Free email services impersonating brands
            val freeProviders = listOf("gmail.com", "yahoo.com", "outlook.com", "hotmail.com")
            val brands = listOf("paypal", "amazon", "bank", "support", "admin", "security")
            
            if (freeProviders.any { email.contains(it) } && brands.any { email.lowercase().contains(it) }) {
                signals.add(PayloadSignal(
                    name = "Brand + Free Email",
                    description = "Suspicious: Brand name with free email provider",
                    riskPoints = 30
                ))
                riskScore += 30
            }
            
            // Lookalike domain check
            if (email.contains("paypa1") || email.contains("amaz0n") || email.contains("g00gle")) {
                signals.add(PayloadSignal(
                    name = "Lookalike Domain",
                    description = "Email uses character substitution (l→1, o→0)",
                    riskPoints = 40
                ))
                riskScore += 40
            }
        }
        
        return PayloadAnalysisResult(
            payloadType = QrPayloadType.EMAIL,
            riskScore = riskScore.coerceIn(0, 100),
            signals = signals,
            parsedData = mapOf("email" to (email ?: "")),
            recommendation = when {
                riskScore >= 40 -> "Suspicious email address - verify before contacting"
                else -> "Verify the email recipient before sending sensitive information"
            }
        )
    }
    
    /**
     * Analyze cryptocurrency payment payload.
     *
     * Format: bitcoin:1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2?amount=0.1
     */
    fun analyzeCrypto(content: String, type: QrPayloadType): PayloadAnalysisResult {
        val signals = mutableListOf<PayloadSignal>()
        var riskScore = type.riskLevel.weight // Starts at CRITICAL (50)
        
        // Extract address and amount
        val parts = content.split(":")
        val addressPart = if (parts.size > 1) parts[1].split("?").firstOrNull() else null
        val amountMatch = Regex("""amount=([0-9.]+)""").find(content)
        val amount = amountMatch?.groupValues?.get(1)
        
        // Always flag crypto payments as high risk
        signals.add(PayloadSignal(
            name = "Cryptocurrency Payment",
            description = "Crypto transactions are irreversible - verify address carefully",
            riskPoints = 0 // Already in base score
        ))
        
        // Check for labels that could be social engineering
        val labelMatch = Regex("""label=([^&]+)""").find(content)
        val label = labelMatch?.groupValues?.get(1)?.let { decodeUrlComponent(it) }
        
        if (label != null) {
            signals.add(PayloadSignal(
                name = "Custom Label",
                description = "Payment labeled as: $label",
                riskPoints = 5
            ))
            riskScore += 5
            
            // Check for impersonation
            val sensitiveLabels = listOf("support", "refund", "prize", "winner", "giveaway", "donation")
            if (sensitiveLabels.any { label.lowercase().contains(it) }) {
                signals.add(PayloadSignal(
                    name = "Suspicious Label",
                    description = "Label suggests potential scam ($label)",
                    riskPoints = 25
                ))
                riskScore += 25
            }
        }
        
        // Large amount warning
        if (amount != null) {
            val amountVal = amount.toDoubleOrNull() ?: 0.0
            if (amountVal > 0.1 && type == QrPayloadType.BITCOIN) {
                signals.add(PayloadSignal(
                    name = "Significant Amount",
                    description = "Payment request for $amount BTC",
                    riskPoints = 15
                ))
                riskScore += 15
            }
        }
        
        return PayloadAnalysisResult(
            payloadType = type,
            riskScore = riskScore.coerceIn(0, 100),
            signals = signals,
            parsedData = mapOf(
                "address" to (addressPart ?: ""),
                "amount" to (amount ?: ""),
                "label" to (label ?: "")
            ),
            recommendation = "CRITICAL: Verify the wallet address through multiple channels before sending. Crypto transactions cannot be reversed!"
        )
    }
    
    /**
     * Analyze traditional payment platform payload.
     */
    fun analyzePayment(content: String, type: QrPayloadType): PayloadAnalysisResult {
        val signals = mutableListOf<PayloadSignal>()
        var riskScore = type.riskLevel.weight
        
        signals.add(PayloadSignal(
            name = "Payment Request",
            description = "QR code requests ${type.displayName} payment",
            riskPoints = 10
        ))
        riskScore += 10
        
        // UPI-specific checks
        if (type == QrPayloadType.UPI) {
            val payeeMatch = Regex("""pa=([^&]+)""").find(content)
            val payee = payeeMatch?.groupValues?.get(1)
            val amountMatch = Regex("""am=([^&]+)""").find(content)
            val amount = amountMatch?.groupValues?.get(1)
            
            if (amount != null) {
                val amountVal = amount.toDoubleOrNull() ?: 0.0
                if (amountVal > 10000) {
                    signals.add(PayloadSignal(
                        name = "Large Payment Amount",
                        description = "Payment request for ₹$amount",
                        riskPoints = 25
                    ))
                    riskScore += 25
                }
            }
            
            return PayloadAnalysisResult(
                payloadType = type,
                riskScore = riskScore.coerceIn(0, 100),
                signals = signals,
                parsedData = mapOf(
                    "payee" to (payee ?: ""),
                    "amount" to (amount ?: "")
                ),
                recommendation = "Verify the payee name and amount before completing payment"
            )
        }
        
        return PayloadAnalysisResult(
            payloadType = type,
            riskScore = riskScore.coerceIn(0, 100),
            signals = signals,
            parsedData = emptyMap(),
            recommendation = "Verify payment details before proceeding"
        )
    }
    
    /**
     * Analyze geographic location payload.
     */
    fun analyzeGeo(content: String): PayloadAnalysisResult {
        val coords = content.removePrefix("geo:")
        return PayloadAnalysisResult(
            payloadType = QrPayloadType.GEO,
            riskScore = 0,
            signals = emptyList(),
            parsedData = mapOf("coordinates" to coords),
            recommendation = "Geographic location will open in maps app"
        )
    }
    
    /**
     * Analyze calendar event payload.
     */
    fun analyzeCalendar(content: String): PayloadAnalysisResult {
        val signals = mutableListOf<PayloadSignal>()
        var riskScore = QrPayloadType.VEVENT.riskLevel.weight
        
        // Check for URLs in event
        val urlMatch = Regex("""URL[;:](.+)""", RegexOption.IGNORE_CASE).find(content)
        if (urlMatch != null) {
            signals.add(PayloadSignal(
                name = "Event Contains URL",
                description = "Calendar event includes a link",
                riskPoints = 10
            ))
            riskScore += 10
        }
        
        return PayloadAnalysisResult(
            payloadType = QrPayloadType.VEVENT,
            riskScore = riskScore,
            signals = signals,
            parsedData = emptyMap(),
            recommendation = "Review event details before adding to calendar"
        )
    }
    
    /**
     * Analyze plain text payload.
     */
    fun analyzeText(content: String): PayloadAnalysisResult {
        val signals = mutableListOf<PayloadSignal>()
        var riskScore = 0
        
        // Check for hidden URLs
        val urlPattern = Regex("""https?://[^\s]+""", RegexOption.IGNORE_CASE)
        val urls = urlPattern.findAll(content).toList()
        if (urls.isNotEmpty()) {
            signals.add(PayloadSignal(
                name = "Contains URLs",
                description = "Text contains ${urls.size} URL(s)",
                riskPoints = 15
            ))
            riskScore += 15
        }
        
        // Check for suspicious keywords
        val scamKeywords = listOf("prize", "winner", "lottery", "inherit", "million", "urgent")
        if (scamKeywords.any { content.lowercase().contains(it) }) {
            signals.add(PayloadSignal(
                name = "Scam Keywords",
                description = "Text contains suspicious keywords",
                riskPoints = 20
            ))
            riskScore += 20
        }
        
        return PayloadAnalysisResult(
            payloadType = QrPayloadType.TEXT,
            riskScore = riskScore,
            signals = signals,
            parsedData = mapOf(
                "length" to content.length.toString(),
                "urlCount" to urls.size.toString()
            ),
            recommendation = if (riskScore > 0) "Review text content carefully" else "Plain text content"
        )
    }
    
    // ================ Helper Functions ================
    
    /**
     * Extract field from WiFi QR code.
     */
    private fun extractWifiField(content: String, field: String): String? {
        val pattern = Regex("""$field:([^;]+)""", RegexOption.IGNORE_CASE)
        return pattern.find(content)?.groupValues?.get(1)?.trim()
    }
    
    /**
     * Extract field from vCard.
     */
    private fun extractVcardField(content: String, field: String): String? {
        val pattern = Regex("""$field[;:]([^\r\n]+)""", RegexOption.IGNORE_CASE)
        return pattern.find(content)?.groupValues?.get(1)?.trim()
    }
    
    /**
     * Check if phone number is premium rate.
     */
    private fun isPremiumRateNumber(phone: String): Boolean {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        
        // US premium (900, 976)
        if (cleanPhone.startsWith("+1900") || cleanPhone.startsWith("+1976")) return true
        if (cleanPhone.startsWith("1900") || cleanPhone.startsWith("1976")) return true
        
        // UK premium (09, 0871, 0872)
        if (cleanPhone.startsWith("+4490") || cleanPhone.startsWith("+44871")) return true
        if (cleanPhone.startsWith("090") || cleanPhone.startsWith("0871")) return true
        
        // Australian premium (190)
        if (cleanPhone.startsWith("+61190") || cleanPhone.startsWith("190")) return true
        
        // Short codes (usually 5-6 digits)
        if (cleanPhone.length in 5..6 && cleanPhone.all { it.isDigit() }) return true
        
        return false
    }
    
    /**
     * URL decode a component.
     */
    private fun decodeUrlComponent(encoded: String): String {
        return try {
            encoded.replace("+", " ")
                .replace(Regex("%([0-9A-Fa-f]{2})")) { match ->
                    match.groupValues[1].toInt(16).toChar().toString()
                }
        } catch (_: Exception) {
            encoded
        }
    }
}

/**
 * Result of payload analysis.
 */
data class PayloadAnalysisResult(
    /**
     * Detected payload type.
     */
    val payloadType: QrPayloadType,
    
    /**
     * Overall risk score (0-100).
     */
    val riskScore: Int,
    
    /**
     * List of detected risk signals.
     */
    val signals: List<PayloadSignal>,
    
    /**
     * Parsed data from payload.
     */
    val parsedData: Map<String, String> = emptyMap(),
    
    /**
     * Recommendation for user.
     */
    val recommendation: String = ""
) {
    /**
     * Get verdict based on score.
     */
    val verdict: PayloadVerdict
        get() = when {
            riskScore >= 60 -> PayloadVerdict.DANGEROUS
            riskScore >= 30 -> PayloadVerdict.SUSPICIOUS
            riskScore >= 10 -> PayloadVerdict.CAUTION
            else -> PayloadVerdict.SAFE
        }
}

/**
 * Individual risk signal.
 */
data class PayloadSignal(
    val name: String,
    val description: String,
    val riskPoints: Int
)

/**
 * Verdict for payload analysis.
 */
enum class PayloadVerdict {
    SAFE,
    CAUTION,
    SUSPICIOUS,
    DANGEROUS
}
