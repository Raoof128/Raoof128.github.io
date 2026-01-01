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
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

/**
 * QR Payload Analyzer Tests
 *
 * Tests the payload analysis engine for non-URL QR code content.
 *
 * @author Mehr Guard Security Team
 * @since 1.2.0
 */
class QrPayloadAnalyzerTest {
    
    // ==================== Type Detection Tests ====================
    
    @Test
    fun `detect WIFI payload type`() {
        val content = "WIFI:T:WPA;S:MyNetwork;P:password123;;"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.WIFI, type)
    }
    
    @Test
    fun `detect vCard payload type`() {
        val content = "BEGIN:VCARD\nVERSION:3.0\nFN:John Doe\nEND:VCARD"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.VCARD, type)
    }
    
    @Test
    fun `detect SMS payload type`() {
        val content = "sms:+1234567890?body=Hello"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.SMS, type)
    }
    
    @Test
    fun `detect Bitcoin payment type`() {
        val content = "bitcoin:1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2?amount=0.1"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.BITCOIN, type)
    }
    
    @Test
    fun `detect Ethereum payment type`() {
        val content = "ethereum:0x742d35Cc6634C0532925a3b844Bc9e7595f2bD25?amount=1"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.ETHEREUM, type)
    }
    
    @Test
    fun `detect UPI payment type`() {
        val content = "upi://pay?pa=name@upi&pn=Name&am=100"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.UPI, type)
    }
    
    @Test
    fun `detect phone call type`() {
        val content = "tel:+1234567890"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.PHONE, type)
    }
    
    @Test
    fun `detect email type`() {
        val content = "mailto:test@example.com"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.EMAIL, type)
    }
    
    @Test
    fun `detect HTTPS URL type`() {
        val content = "https://example.com/page"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.URL_HTTPS, type)
    }
    
    @Test
    fun `detect HTTP URL type`() {
        val content = "http://example.com/page"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.URL_HTTP, type)
    }
    
    @Test
    fun `detect plain text type`() {
        val content = "Just some plain text"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.TEXT, type)
    }
    
    // ==================== WiFi Analysis Tests ====================
    
    @Test
    fun `analyze secure WPA WiFi network`() {
        val content = "WIFI:T:WPA;S:HomeNetwork;P:SecurePass123;;"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.WIFI, result.payloadType)
        assertTrue(result.riskScore < 50, "Secure network should have lower risk")
        assertEquals("HomeNetwork", result.parsedData["ssid"])
    }
    
    @Test
    fun `flag open WiFi network as high risk`() {
        val content = "WIFI:T:nopass;S:FreeWifi;;"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.riskScore >= 50, "Open network should have high risk")
        assertTrue(result.signals.any { it.name.contains("Open") })
    }
    
    @Test
    fun `flag WEP encryption as weak`() {
        val content = "WIFI:T:WEP;S:OldNetwork;P:12345;;"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("WEP") || it.name.contains("Weak") })
    }
    
    @Test
    fun `flag hidden network`() {
        val content = "WIFI:T:WPA;S:SecretNet;P:pass;H:true;;"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Hidden") })
    }
    
    @Test
    fun `flag suspicious SSID - free wifi`() {
        val content = "WIFI:T:WPA;S:Free Airport Wifi;P:guest;;"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { 
            it.name.lowercase().contains("suspicious") || 
            it.name.lowercase().contains("free")
        })
    }
    
    @Test
    fun `flag brand impersonation in SSID`() {
        val content = "WIFI:T:WPA;S:Starbucks Free Wifi;P:coffee;;"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.description.lowercase().contains("brand") })
    }
    
    // ==================== vCard Analysis Tests ====================
    
    @Test
    fun `analyze clean vCard`() {
        val content = """
            BEGIN:VCARD
            VERSION:3.0
            FN:John Doe
            TEL:+1234567890
            EMAIL:john@example.com
            END:VCARD
        """.trimIndent()
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.VCARD, result.payloadType)
        assertTrue(result.riskScore < 40, "Clean vCard should have lower risk")
    }
    
    @Test
    fun `flag vCard with URL`() {
        val content = """
            BEGIN:VCARD
            FN:John Doe
            URL:https://phishing.tk/verify
            END:VCARD
        """.trimIndent()
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("URL") })
    }
    
    @Test
    fun `flag vCard with executive title`() {
        val content = """
            BEGIN:VCARD
            FN:John Smith
            TITLE:CEO
            ORG:Apple Inc
            END:VCARD
        """.trimIndent()
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Executive") })
    }
    
    @Test
    fun `flag vCard with sensitive organization`() {
        val content = """
            BEGIN:VCARD
            FN:Support Agent
            ORG:PayPal Security Team
            EMAIL:security@paypal.com
            END:VCARD
        """.trimIndent()
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { 
            it.name.contains("Organization") || 
            it.name.contains("Sensitive")
        })
    }
    
    // ==================== SMS Analysis Tests ====================
    
    @Test
    fun `analyze clean SMS`() {
        val content = "sms:+1234567890?body=Hello"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.SMS, result.payloadType)
        assertEquals("+1234567890", result.parsedData["phone"])
    }
    
    @Test
    fun `flag SMS to premium rate number`() {
        val content = "sms:+1900-555-0123?body=Subscribe"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Premium") })
        assertTrue(result.riskScore >= 50)
    }
    
    @Test
    fun `flag smishing URL in SMS body`() {
        val content = "sms:+1234567890?body=Your%20bank%20account%20is%20locked.%20Visit%20https://fake-bank.tk"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("URL") })
        assertTrue(result.riskScore >= 40)
    }
    
    @Test
    fun `flag shortened URL in SMS`() {
        val content = "sms:+1234567890?body=Click%20here:%20https://bit.ly/prize123"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Shortened") })
    }
    
    @Test
    fun `flag urgency language in SMS`() {
        val content = "sms:+1234567890?body=URGENT:%20Verify%20your%20account%20immediately"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Urgency") })
    }
    
    @Test
    fun `flag financial keywords in SMS`() {
        val content = "sms:+1234567890?body=Your%20bank%20payment%20of%20$1000%20has%20been%20processed"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Financial") })
    }
    
    // ==================== Phone Analysis Tests ====================
    
    @Test
    fun `flag premium rate phone number`() {
        val content = "tel:+1-900-555-0123"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.PHONE, result.payloadType)
        assertTrue(result.signals.any { it.name.contains("Premium") })
    }
    
    @Test
    fun `normal phone number has low risk`() {
        val content = "tel:+1-555-123-4567"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.riskScore < 30)
    }
    
    // ==================== Email Analysis Tests ====================
    
    @Test
    fun `analyze normal email`() {
        val content = "mailto:contact@example.com"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.EMAIL, result.payloadType)
        assertTrue(result.riskScore < 30)
    }
    
    @Test
    fun `flag brand impersonation email`() {
        val content = "mailto:security-paypal@gmail.com"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { 
            it.name.contains("Brand") || 
            it.name.contains("Free Email")
        })
    }
    
    @Test
    fun `flag lookalike email domain`() {
        val content = "mailto:support@paypa1.com"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Lookalike") })
    }
    
    // ==================== Cryptocurrency Analysis Tests ====================
    
    @Test
    fun `Bitcoin payment has high base risk`() {
        val content = "bitcoin:1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.BITCOIN, result.payloadType)
        assertTrue(result.riskScore >= 40, "Crypto should have high base risk (score: ${result.riskScore})")
        assertTrue(
            result.recommendation.lowercase().contains("irreversible") || 
            result.recommendation.lowercase().contains("verify"), 
            "Should warn about irreversibility"
        )
    }
    
    @Test
    fun `flag crypto payment with suspicious label`() {
        val content = "bitcoin:1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2?amount=0.5&label=URGENT%20REFUND"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Suspicious") || it.name.contains("Label") })
    }
    
    @Test
    fun `flag large Bitcoin amount`() {
        val content = "bitcoin:1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2?amount=0.5"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Amount") || it.name.contains("Significant") })
    }
    
    // ==================== UPI Payment Tests ====================
    
    @Test
    fun `analyze UPI payment`() {
        val content = "upi://pay?pa=name@upi&pn=Name&am=100"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.UPI, result.payloadType)
        assertTrue(result.signals.any { it.name.contains("Payment") })
    }
    
    // ==================== Geo Location Tests ====================
    
    @Test
    fun `geo location has low risk`() {
        val content = "geo:37.7749,-122.4194"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.GEO, result.payloadType)
        assertEquals(0, result.riskScore)
    }
    
    // ==================== Text Analysis Tests ====================
    
    @Test
    fun `plain text has low risk`() {
        val content = "Hello, this is just plain text."
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.TEXT, result.payloadType)
        assertTrue(result.riskScore < 20)
    }
    
    @Test
    fun `flag URLs hidden in text`() {
        val content = "Visit our website: https://example.com for more info"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("URL") })
    }
    
    @Test
    fun `flag scam keywords in text`() {
        val content = "Congratulations! You are the prize winner of $1,000,000!"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertTrue(result.signals.any { it.name.contains("Scam") || it.name.contains("Keywords") })
    }
    
    // ==================== Verdict Tests ====================
    
    @Test
    fun `high risk score gives DANGEROUS verdict`() {
        val content = "sms:+1900-555-0123?body=URGENT%20https://bit.ly/scam%20bank%20payment"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(PayloadVerdict.DANGEROUS, result.verdict)
    }
    
    @Test
    fun `low risk score gives SAFE verdict`() {
        val content = "geo:37.7749,-122.4194"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(PayloadVerdict.SAFE, result.verdict)
    }
    
    // ==================== Edge Cases ====================
    
    @Test
    fun `empty content has unknown type`() {
        val content = ""
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.UNKNOWN, type)
    }
    
    @Test
    fun `malformed WiFi payload is handled`() {
        val content = "WIFI:garbage"
        val result = QrPayloadAnalyzer.analyze(content)
        
        assertEquals(QrPayloadType.WIFI, result.payloadType)
        // Should not crash
    }
    
    @Test
    fun `case insensitive type detection`() {
        val content = "HTTPS://EXAMPLE.COM/PAGE"
        val type = QrPayloadType.detect(content)
        assertEquals(QrPayloadType.URL_HTTPS, type)
    }
}
