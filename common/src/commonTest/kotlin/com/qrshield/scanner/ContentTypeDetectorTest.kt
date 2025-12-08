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

package com.qrshield.scanner

import com.qrshield.model.ContentType
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for ContentType detection logic.
 * 
 * Tests the content type detection from QR code payload strings.
 * This logic is shared across all platform scanner implementations.
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class ContentTypeDetectorTest {
    
    // Helper function that mirrors platform scanner detection logic
    private fun detectContentType(content: String): ContentType {
        return when {
            content.startsWith("http://", ignoreCase = true) || 
            content.startsWith("https://", ignoreCase = true) -> ContentType.URL
            content.startsWith("WIFI:", ignoreCase = true) -> ContentType.WIFI
            content.startsWith("BEGIN:VCARD", ignoreCase = true) -> ContentType.VCARD
            content.startsWith("geo:", ignoreCase = true) -> ContentType.GEO
            content.startsWith("tel:", ignoreCase = true) -> ContentType.PHONE
            content.startsWith("sms:", ignoreCase = true) || 
            content.startsWith("smsto:", ignoreCase = true) -> ContentType.SMS
            content.startsWith("mailto:", ignoreCase = true) -> ContentType.EMAIL
            else -> ContentType.TEXT
        }
    }
    
    // ============================================
    // URL DETECTION TESTS
    // ============================================
    
    @Test
    fun `HTTP URL is detected as URL type`() {
        assertEquals(ContentType.URL, detectContentType("http://example.com"))
    }
    
    @Test
    fun `HTTPS URL is detected as URL type`() {
        assertEquals(ContentType.URL, detectContentType("https://example.com"))
    }
    
    @Test
    fun `URL with path is detected correctly`() {
        assertEquals(ContentType.URL, detectContentType("https://example.com/path/to/resource"))
    }
    
    @Test
    fun `URL with query params is detected correctly`() {
        assertEquals(ContentType.URL, detectContentType("https://example.com?param=value"))
    }
    
    @Test
    fun `URL with fragment is detected correctly`() {
        assertEquals(ContentType.URL, detectContentType("https://example.com#section"))
    }
    
    @Test
    fun `URL detection is case insensitive`() {
        assertEquals(ContentType.URL, detectContentType("HTTP://EXAMPLE.COM"))
        assertEquals(ContentType.URL, detectContentType("HTTPS://EXAMPLE.COM"))
        assertEquals(ContentType.URL, detectContentType("HtTpS://Example.com"))
    }
    
    // ============================================
    // WIFI DETECTION TESTS
    // ============================================
    
    @Test
    fun `WiFi QR code is detected as WIFI type`() {
        assertEquals(ContentType.WIFI, detectContentType("WIFI:T:WPA;S:MyNetwork;P:password123;;"))
    }
    
    @Test
    fun `WiFi detection is case insensitive`() {
        assertEquals(ContentType.WIFI, detectContentType("wifi:T:WPA;S:Network;P:pass;;"))
        assertEquals(ContentType.WIFI, detectContentType("Wifi:T:WPA;S:Network;P:pass;;"))
    }
    
    @Test
    fun `WEP WiFi is detected correctly`() {
        assertEquals(ContentType.WIFI, detectContentType("WIFI:T:WEP;S:OldNetwork;P:wepkey;;"))
    }
    
    @Test
    fun `Open WiFi is detected correctly`() {
        assertEquals(ContentType.WIFI, detectContentType("WIFI:T:nopass;S:OpenNetwork;;"))
    }
    
    // ============================================
    // VCARD DETECTION TESTS
    // ============================================
    
    @Test
    fun `VCard is detected as VCARD type`() {
        val vcard = """
            BEGIN:VCARD
            VERSION:3.0
            N:Doe;John
            FN:John Doe
            END:VCARD
        """.trimIndent()
        assertEquals(ContentType.VCARD, detectContentType(vcard))
    }
    
    @Test
    fun `VCard detection is case insensitive`() {
        assertEquals(ContentType.VCARD, detectContentType("begin:vcard\nVERSION:3.0"))
        assertEquals(ContentType.VCARD, detectContentType("BEGIN:vCard\nVERSION:3.0"))
    }
    
    // ============================================
    // GEO DETECTION TESTS
    // ============================================
    
    @Test
    fun `Geo URI is detected as GEO type`() {
        assertEquals(ContentType.GEO, detectContentType("geo:40.7128,-74.0060"))
    }
    
    @Test
    fun `Geo URI with altitude is detected correctly`() {
        assertEquals(ContentType.GEO, detectContentType("geo:40.7128,-74.0060,100"))
    }
    
    @Test
    fun `Geo detection is case insensitive`() {
        assertEquals(ContentType.GEO, detectContentType("GEO:40.7128,-74.0060"))
    }
    
    // ============================================
    // PHONE DETECTION TESTS
    // ============================================
    
    @Test
    fun `Tel URI is detected as PHONE type`() {
        assertEquals(ContentType.PHONE, detectContentType("tel:+1234567890"))
    }
    
    @Test
    fun `Tel detection is case insensitive`() {
        assertEquals(ContentType.PHONE, detectContentType("TEL:+1234567890"))
    }
    
    @Test
    fun `Tel with formatting is detected correctly`() {
        assertEquals(ContentType.PHONE, detectContentType("tel:+1-234-567-890"))
    }
    
    // ============================================
    // SMS DETECTION TESTS
    // ============================================
    
    @Test
    fun `SMS URI is detected as SMS type`() {
        assertEquals(ContentType.SMS, detectContentType("sms:+1234567890"))
    }
    
    @Test
    fun `SMSTO URI is detected as SMS type`() {
        assertEquals(ContentType.SMS, detectContentType("smsto:+1234567890"))
    }
    
    @Test
    fun `SMS with body is detected correctly`() {
        assertEquals(ContentType.SMS, detectContentType("sms:+1234567890?body=Hello"))
    }
    
    @Test
    fun `SMS detection is case insensitive`() {
        assertEquals(ContentType.SMS, detectContentType("SMS:+1234567890"))
        assertEquals(ContentType.SMS, detectContentType("SMSTO:+1234567890"))
    }
    
    // ============================================
    // EMAIL DETECTION TESTS
    // ============================================
    
    @Test
    fun `Mailto URI is detected as EMAIL type`() {
        assertEquals(ContentType.EMAIL, detectContentType("mailto:user@example.com"))
    }
    
    @Test
    fun `Email with subject is detected correctly`() {
        assertEquals(ContentType.EMAIL, detectContentType("mailto:user@example.com?subject=Hello"))
    }
    
    @Test
    fun `Email detection is case insensitive`() {
        assertEquals(ContentType.EMAIL, detectContentType("MAILTO:user@example.com"))
    }
    
    // ============================================
    // TEXT (DEFAULT) DETECTION TESTS
    // ============================================
    
    @Test
    fun `Plain text is detected as TEXT type`() {
        assertEquals(ContentType.TEXT, detectContentType("Hello, World!"))
    }
    
    @Test
    fun `Empty string is detected as TEXT type`() {
        assertEquals(ContentType.TEXT, detectContentType(""))
    }
    
    @Test
    fun `Unknown protocol is detected as TEXT type`() {
        assertEquals(ContentType.TEXT, detectContentType("ftp://example.com"))
    }
    
    @Test
    fun `Data URI is detected as TEXT type`() {
        assertEquals(ContentType.TEXT, detectContentType("data:text/plain;base64,SGVsbG8="))
    }
    
    @Test
    fun `Partial URL without protocol is TEXT`() {
        assertEquals(ContentType.TEXT, detectContentType("example.com"))
        assertEquals(ContentType.TEXT, detectContentType("www.example.com"))
    }
    
    // ============================================
    // EDGE CASES
    // ============================================
    
    @Test
    fun `Whitespace before protocol still TEXT`() {
        // Whitespace prefix means not a valid URI
        assertEquals(ContentType.TEXT, detectContentType(" https://example.com"))
    }
    
    @Test
    fun `Newline in content still detects protocol`() {
        // First line determines type
        assertEquals(ContentType.URL, detectContentType("https://example.com\nSome text"))
    }
    
    @Test
    fun `Very long URL is still detected`() {
        val longUrl = "https://example.com/" + "a".repeat(1000)
        assertEquals(ContentType.URL, detectContentType(longUrl))
    }
}
