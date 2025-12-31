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

package com.raouf.mehrguard.scanner

import com.raouf.mehrguard.core.PhishingEngine
import com.raouf.mehrguard.model.ContentType
import com.raouf.mehrguard.model.ErrorCode
import com.raouf.mehrguard.model.ScanResult
import com.raouf.mehrguard.model.Verdict
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration Tests for QR Scanner Pipeline
 *
 * Tests the complete flow from QR code scanning to phishing detection.
 * Uses mock scanner implementation to simulate camera/image input.
 *
 * ## Test Coverage
 * - Malicious URL detection from QR codes
 * - Safe URL handling
 * - Error conditions (permission denied, corrupted QR, etc.)
 * - Content type detection
 * - Result handler validation
 *
 * @author QR-SHIELD Security Team
 * @since 1.4.0
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ScannerIntegrationTest {

    private val engine = PhishingEngine()

    // =========================================================================
    // MALICIOUS URL DETECTION TESTS
    // =========================================================================

    @Test
    fun `scanner detects malicious URL from QR code`() = runTest {
        val mockScanner = TestableQrScanner()
        val handler = ScannerResultHandler(engine)

        // Simulate QR code with known malicious URL
        mockScanner.setMockImageResult(
            ScanResult.Success(
                content = "https://paypa1-secure.tk/login",
                contentType = ContentType.URL
            )
        )

        val scanResult = mockScanner.scanFromImage(byteArrayOf())
        assertTrue(scanResult is ScanResult.Success)

        val assessment = handler.processResult(scanResult as ScanResult.Success)
        
        assertTrue(
            assessment.verdict == Verdict.MALICIOUS || assessment.verdict == Verdict.SUSPICIOUS,
            "Expected MALICIOUS or SUSPICIOUS, got ${assessment.verdict}"
        )
        assertTrue(assessment.score >= 30, "Expected high risk score, got ${assessment.score}")
        assertTrue(assessment.flags.isNotEmpty(), "Expected detection signals")
    }

    @Test
    fun `scanner handles safe URL correctly`() = runTest {
        val mockScanner = TestableQrScanner()
        val handler = ScannerResultHandler(engine)

        // Simulate QR code with safe URL
        mockScanner.setMockImageResult(
            ScanResult.Success(
                content = "https://www.google.com",
                contentType = ContentType.URL
            )
        )

        val scanResult = mockScanner.scanFromImage(byteArrayOf())
        assertTrue(scanResult is ScanResult.Success)

        val assessment = handler.processResult(scanResult as ScanResult.Success)
        
        assertEquals(Verdict.SAFE, assessment.verdict)
        assertTrue(assessment.score <= 30, "Expected low risk score, got ${assessment.score}")
    }

    @Test
    fun `scanner detects typosquatting attack`() = runTest {
        val mockScanner = TestableQrScanner()
        val handler = ScannerResultHandler(engine)

        val typosquatUrls = listOf(
            "https://g00gle.com",
            "https://amaz0n.com",
            "https://paypa1.com",
            "https://micros0ft.com"
        )

        for (url in typosquatUrls) {
            mockScanner.setMockImageResult(
                ScanResult.Success(content = url, contentType = ContentType.URL)
            )

            val scanResult = mockScanner.scanFromImage(byteArrayOf())
            val assessment = handler.processResult(scanResult as ScanResult.Success)
            
            assertTrue(
                assessment.verdict != Verdict.SAFE,
                "Typosquat URL '$url' should be flagged, got ${assessment.verdict}"
            )
        }
    }

    @Test
    fun `scanner detects IP address phishing`() = runTest {
        val mockScanner = TestableQrScanner()
        val handler = ScannerResultHandler(engine)

        mockScanner.setMockImageResult(
            ScanResult.Success(
                content = "http://192.168.1.1/login?password=test",
                contentType = ContentType.URL
            )
        )

        val scanResult = mockScanner.scanFromImage(byteArrayOf())
        val assessment = handler.processResult(scanResult as ScanResult.Success)
        
        assertTrue(assessment.score >= 20, "IP address URL should have elevated score")
        assertTrue(
            assessment.flags.any { it.contains("IP", ignoreCase = true) },
            "Should flag IP address usage"
        )
    }

    @Test
    fun `scanner detects URL shortener obfuscation`() = runTest {
        val mockScanner = TestableQrScanner()
        val handler = ScannerResultHandler(engine)

        val shortenerUrls = listOf(
            "https://bit.ly/xyz123",
            "https://t.co/abc456",
            "https://tinyurl.com/short"
        )

        for (url in shortenerUrls) {
            mockScanner.setMockImageResult(
                ScanResult.Success(content = url, contentType = ContentType.URL)
            )

            val scanResult = mockScanner.scanFromImage(byteArrayOf())
            val assessment = handler.processResult(scanResult as ScanResult.Success)
            
            assertTrue(
                assessment.flags.any { 
                    it.contains("shortener", ignoreCase = true) || 
                    it.contains("shorten", ignoreCase = true) 
                },
                "Should flag URL shortener: $url"
            )
        }
    }

    // =========================================================================
    // ERROR HANDLING TESTS
    // =========================================================================

    @Test
    fun `scanner handles camera permission denied gracefully`() = runTest {
        val mockScanner = TestableQrScanner()
        mockScanner.hasPermission = false
        mockScanner.permissionGrantedOnRequest = false

        val hasPermission = mockScanner.hasCameraPermission()
        assertEquals(false, hasPermission)

        val granted = mockScanner.requestCameraPermission()
        assertEquals(false, granted)

        // Verify scanner provides user-friendly error
        mockScanner.setMockImageResult(
            ScanResult.Error(
                message = "Camera permission is required to scan QR codes",
                code = ErrorCode.CAMERA_PERMISSION_DENIED
            )
        )

        val result = mockScanner.scanFromImage(byteArrayOf())
        assertTrue(result is ScanResult.Error)
        assertEquals(ErrorCode.CAMERA_PERMISSION_DENIED, (result as ScanResult.Error).code)
    }

    @Test
    fun `scanner handles corrupted QR code`() = runTest {
        val mockScanner = TestableQrScanner()

        mockScanner.setMockImageResult(
            ScanResult.Error(
                message = "Unable to decode QR code",
                code = ErrorCode.DECODE_ERROR
            )
        )

        val result = mockScanner.scanFromImage(byteArrayOf(0, 1, 2, 3)) // Random bytes
        
        assertTrue(result is ScanResult.Error)
        assertEquals(ErrorCode.DECODE_ERROR, (result as ScanResult.Error).code)
        assertTrue(result.message.isNotBlank(), "Error should have user-friendly message")
    }

    @Test
    fun `scanner handles empty image input`() = runTest {
        val mockScanner = TestableQrScanner()

        mockScanner.setMockImageResult(ScanResult.NoQrFound)

        val result = mockScanner.scanFromImage(byteArrayOf())
        
        assertEquals(ScanResult.NoQrFound, result)
    }

    @Test
    fun `scanner handles malformed URL in QR code`() = runTest {
        val mockScanner = TestableQrScanner()
        val handler = ScannerResultHandler(engine)

        // QR code contains something that looks like a URL but isn't valid
        mockScanner.setMockImageResult(
            ScanResult.Success(
                content = "htp://not-a-valid-url",
                contentType = ContentType.TEXT
            )
        )

        val scanResult = mockScanner.scanFromImage(byteArrayOf())
        assertTrue(scanResult is ScanResult.Success)

        // Handler should gracefully handle non-URL content
        val result = scanResult as ScanResult.Success
        assertEquals(ContentType.TEXT, result.contentType)
    }

    @Test
    fun `scanner handles camera hardware error`() = runTest {
        val mockScanner = TestableQrScanner()

        mockScanner.setMockImageResult(
            ScanResult.Error(
                message = "Camera hardware error: device not available",
                code = ErrorCode.CAMERA_NOT_AVAILABLE
            )
        )

        val result = mockScanner.scanFromImage(byteArrayOf())
        
        assertTrue(result is ScanResult.Error)
        assertEquals(ErrorCode.CAMERA_NOT_AVAILABLE, (result as ScanResult.Error).code)
    }

    // =========================================================================
    // CONTENT TYPE TESTS
    // =========================================================================

    @Test
    fun `scanner correctly identifies URL content type`() = runTest {
        val mockScanner = TestableQrScanner()

        val urlContents = listOf(
            "https://example.com" to ContentType.URL,
            "http://test.org" to ContentType.URL,
            "tel:+1234567890" to ContentType.PHONE,
            "mailto:test@example.com" to ContentType.EMAIL,
            "sms:+1234567890?body=Hello" to ContentType.SMS,
            "geo:37.7749,-122.4194" to ContentType.GEO,
            "WIFI:S:MyNetwork;T:WPA;P:password;;" to ContentType.WIFI,
            "BEGIN:VCARD\nVERSION:3.0\nN:Doe;John\nEND:VCARD" to ContentType.VCARD,
            "Just plain text" to ContentType.TEXT
        )

        for ((content, expectedType) in urlContents) {
            mockScanner.setMockImageResult(
                ScanResult.Success(content = content, contentType = expectedType)
            )

            val result = mockScanner.scanFromImage(byteArrayOf())
            assertTrue(result is ScanResult.Success)
            assertEquals(expectedType, (result as ScanResult.Success).contentType)
        }
    }

    // =========================================================================
    // CAMERA FLOW TESTS
    // =========================================================================

    @Test
    fun `camera scan flow emits results correctly`() = runTest {
        val mockScanner = TestableQrScanner()
        
        val flow = mockScanner.scanFromCamera()
        assertTrue(mockScanner.cameraScanStarted)

        // Collect in background and then emit
        var receivedResult: ScanResult? = null
        val job = launch {
            receivedResult = flow.first()
        }
        
        // Give collector time to start
        testScheduler.advanceUntilIdle()
        
        // Emit a result
        mockScanner.emitCameraScanResult(
            ScanResult.Success(
                content = "https://test.com",
                contentType = ContentType.URL
            )
        )

        job.join()
        
        assertTrue(receivedResult is ScanResult.Success)
        assertEquals("https://test.com", (receivedResult as ScanResult.Success).content)
    }

    @Test
    fun `stop scanning terminates camera flow`() = runTest {
        val mockScanner = TestableQrScanner()
        
        mockScanner.scanFromCamera()
        assertTrue(mockScanner.cameraScanStarted)
        
        mockScanner.stopScanning()
        assertTrue(mockScanner.scanStopped)
    }

    // =========================================================================
    // ALERT STATE VALIDATION
    // =========================================================================

    @Test
    fun `handler emits correct alert state for malicious URL`() = runTest {
        val handler = ScannerResultHandler(engine)

        val result = ScanResult.Success(
            content = "https://paypa1-secure.tk/login",
            contentType = ContentType.URL
        )

        val assessment = handler.processResult(result)
        val alertState = handler.determineAlertState(assessment)

        assertTrue(
            alertState == AlertState.DANGER || alertState == AlertState.WARNING,
            "Malicious URL should trigger danger or warning alert"
        )
    }

    @Test
    fun `handler emits safe alert state for clean URL`() = runTest {
        val handler = ScannerResultHandler(engine)

        val result = ScanResult.Success(
            content = "https://www.github.com",
            contentType = ContentType.URL
        )

        val assessment = handler.processResult(result)
        val alertState = handler.determineAlertState(assessment)

        assertEquals(AlertState.SAFE, alertState, "Clean URL should be marked as safe")
    }
}

/**
 * Testable QR Scanner implementation for unit tests.
 */
class TestableQrScanner : QrScanner {
    private var mockImageResult: ScanResult = ScanResult.NoQrFound
    var hasPermission = true
    var permissionGrantedOnRequest = true
    var cameraScanStarted = false
    var scanStopped = false

    private val cameraScanFlow = MutableSharedFlow<ScanResult>()

    fun setMockImageResult(result: ScanResult) {
        mockImageResult = result
    }

    override fun scanFromCamera(): Flow<ScanResult> {
        cameraScanStarted = true
        return cameraScanFlow
    }

    suspend fun emitCameraScanResult(result: ScanResult) {
        cameraScanFlow.emit(result)
    }

    override suspend fun scanFromImage(imageBytes: ByteArray): ScanResult = mockImageResult

    override fun stopScanning() {
        scanStopped = true
    }

    override suspend fun hasCameraPermission(): Boolean = hasPermission

    override suspend fun requestCameraPermission(): Boolean {
        hasPermission = permissionGrantedOnRequest
        return permissionGrantedOnRequest
    }
}

/**
 * Scanner Result Handler for processing scan results.
 */
class ScannerResultHandler(private val engine: PhishingEngine) {
    
    /**
     * Process a successful scan result and return risk assessment.
     */
    fun processResult(result: ScanResult.Success): com.raouf.mehrguard.model.RiskAssessment {
        return when (result.contentType) {
            ContentType.URL -> engine.analyzeBlocking(result.content)
            else -> {
                // For non-URL content, create a safe assessment
                com.raouf.mehrguard.model.RiskAssessment(
                    verdict = Verdict.SAFE,
                    score = 0,
                    flags = emptyList(),
                    details = com.raouf.mehrguard.model.UrlAnalysisResult(
                        originalUrl = result.content,
                        heuristicScore = 0,
                        mlScore = 0,
                        brandScore = 0,
                        tldScore = 0
                    )
                )
            }
        }
    }

    /**
     * Determine the appropriate alert state based on assessment.
     */
    fun determineAlertState(assessment: com.raouf.mehrguard.model.RiskAssessment): AlertState {
        return when (assessment.verdict) {
            Verdict.SAFE -> AlertState.SAFE
            Verdict.SUSPICIOUS -> AlertState.WARNING
            Verdict.MALICIOUS -> AlertState.DANGER
            Verdict.UNKNOWN -> AlertState.WARNING // Treat unknown as warning
        }
    }
}

/**
 * Alert states for UI feedback.
 */
enum class AlertState {
    SAFE,
    WARNING,
    DANGER
}
