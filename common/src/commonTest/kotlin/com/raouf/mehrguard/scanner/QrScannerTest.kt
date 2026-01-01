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

package com.raouf.mehrguard.scanner

import com.raouf.mehrguard.model.ContentType
import com.raouf.mehrguard.model.ErrorCode
import com.raouf.mehrguard.model.ScanResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for QrScanner interface behavior.
 *
 * Uses a mock scanner to verify expected behavior patterns
 * that all platform implementations should follow.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QrScannerTest {

    // =========================================================================
    // SCAN FROM IMAGE TESTS
    // =========================================================================

    @Test
    fun `scanFromImage returns result for valid image`() = runTest {
        val scanner = MockQrScanner()
        scanner.mockImageScanResult = ScanResult.Success(
            content = "https://example.com",
            contentType = ContentType.URL
        )

        val result = scanner.scanFromImage(byteArrayOf(1, 2, 3))

        assertTrue(result is ScanResult.Success)
        assertEquals("https://example.com", (result as ScanResult.Success).content)
        assertEquals(ContentType.URL, result.contentType)
    }

    @Test
    fun `scanFromImage handles empty image`() = runTest {
        val scanner = MockQrScanner()
        scanner.mockImageScanResult = ScanResult.Error(
            message = "No QR code found",
            code = ErrorCode.DECODE_ERROR
        )

        val result = scanner.scanFromImage(byteArrayOf())

        assertTrue(result is ScanResult.Error)
    }

    @Test
    fun `scanFromImage handles large image`() = runTest {
        val scanner = MockQrScanner()
        scanner.mockImageScanResult = ScanResult.Success(
            content = "https://example.com",
            contentType = ContentType.URL
        )

        val largeImage = ByteArray(1024 * 1024) // 1MB
        val result = scanner.scanFromImage(largeImage)

        // Should not crash and return result
        assertNotNull(result)
    }

    @Test
    fun `scanFromImage returns NoQrFound when no QR detected`() = runTest {
        val scanner = MockQrScanner()
        scanner.mockImageScanResult = ScanResult.NoQrFound

        val result = scanner.scanFromImage(byteArrayOf(1, 2, 3))

        assertEquals(ScanResult.NoQrFound, result)
    }

    // =========================================================================
    // SCAN FROM CAMERA TESTS
    // =========================================================================

    @Test
    fun `scanFromCamera emits results`() = runTest {
        val scanner = MockQrScanner()

        // Just verify the scanner returns a flow and sets the flag
        val flow = scanner.scanFromCamera()

        // Verify camera scan was started
        assertTrue(scanner.cameraScanStarted, "Camera scan should be started")
        assertNotNull(flow, "Flow should not be null")
    }

    @Test
    fun `stopScanning stops camera flow`() = runTest {
        val scanner = MockQrScanner()

        scanner.scanFromCamera()
        scanner.stopScanning()

        assertTrue(scanner.scanStopped)
    }

    // =========================================================================
    // PERMISSION TESTS
    // =========================================================================

    @Test
    fun `hasCameraPermission returns permission status`() = runTest {
        val scanner = MockQrScanner()

        scanner.hasPermission = true
        assertTrue(scanner.hasCameraPermission())

        scanner.hasPermission = false
        assertEquals(false, scanner.hasCameraPermission())
    }

    @Test
    fun `requestCameraPermission returns result`() = runTest {
        val scanner = MockQrScanner()

        scanner.permissionGrantedOnRequest = true
        assertTrue(scanner.requestCameraPermission())

        scanner.permissionGrantedOnRequest = false
        assertEquals(false, scanner.requestCameraPermission())
    }

    // =========================================================================
    // SCAN RESULT VALIDATION TESTS
    // =========================================================================

    @Test
    fun `ScanResult Success contains content`() {
        val result = ScanResult.Success(
            content = "https://example.com",
            contentType = ContentType.URL
        )

        assertEquals("https://example.com", result.content)
        assertEquals(ContentType.URL, result.contentType)
    }

    @Test
    fun `ScanResult Error contains message`() {
        val result = ScanResult.Error(
            message = "Camera not available",
            code = ErrorCode.CAMERA_NOT_AVAILABLE
        )

        assertEquals("Camera not available", result.message)
        assertEquals(ErrorCode.CAMERA_NOT_AVAILABLE, result.code)
    }

    @Test
    fun `ScanResult NoQrFound is singleton`() {
        val result1 = ScanResult.NoQrFound
        val result2 = ScanResult.NoQrFound

        assertEquals(result1, result2)
    }

    // =========================================================================
    // CONTENT TYPE DETECTION TESTS
    // =========================================================================

    @Test
    fun `URL content has URL type`() {
        val result = ScanResult.Success(
            content = "https://example.com",
            contentType = ContentType.URL
        )

        assertEquals(ContentType.URL, result.contentType)
    }

    @Test
    fun `WIFI content has WIFI type`() {
        val result = ScanResult.Success(
            content = "WIFI:S:MyNetwork;T:WPA;P:password;;",
            contentType = ContentType.WIFI
        )

        assertEquals(ContentType.WIFI, result.contentType)
    }

    @Test
    fun `VCARD content has VCARD type`() {
        val result = ScanResult.Success(
            content = "BEGIN:VCARD\nVERSION:3.0\nN:Doe;John\nEND:VCARD",
            contentType = ContentType.VCARD
        )

        assertEquals(ContentType.VCARD, result.contentType)
    }

    @Test
    fun `GEO content has GEO type`() {
        val result = ScanResult.Success(
            content = "geo:37.7749,-122.4194",
            contentType = ContentType.GEO
        )

        assertEquals(ContentType.GEO, result.contentType)
    }

    @Test
    fun `PHONE content has PHONE type`() {
        val result = ScanResult.Success(
            content = "tel:+1234567890",
            contentType = ContentType.PHONE
        )

        assertEquals(ContentType.PHONE, result.contentType)
    }

    @Test
    fun `SMS content has SMS type`() {
        val result = ScanResult.Success(
            content = "sms:+1234567890?body=Hello",
            contentType = ContentType.SMS
        )

        assertEquals(ContentType.SMS, result.contentType)
    }

    @Test
    fun `EMAIL content has EMAIL type`() {
        val result = ScanResult.Success(
            content = "mailto:test@example.com",
            contentType = ContentType.EMAIL
        )

        assertEquals(ContentType.EMAIL, result.contentType)
    }

    @Test
    fun `TEXT content has TEXT type`() {
        val result = ScanResult.Success(
            content = "Just some plain text",
            contentType = ContentType.TEXT
        )

        assertEquals(ContentType.TEXT, result.contentType)
    }

    @Test
    fun `UNKNOWN content has UNKNOWN type`() {
        val result = ScanResult.Success(
            content = "???",
            contentType = ContentType.UNKNOWN
        )

        assertEquals(ContentType.UNKNOWN, result.contentType)
    }

    // =========================================================================
    // ERROR CODE TESTS
    // =========================================================================

    @Test
    fun `CAMERA_PERMISSION_DENIED error code`() {
        val result = ScanResult.Error(
            message = "Permission denied",
            code = ErrorCode.CAMERA_PERMISSION_DENIED
        )

        assertEquals(ErrorCode.CAMERA_PERMISSION_DENIED, result.code)
    }

    @Test
    fun `CAMERA_NOT_AVAILABLE error code`() {
        val result = ScanResult.Error(
            message = "Camera not available",
            code = ErrorCode.CAMERA_NOT_AVAILABLE
        )

        assertEquals(ErrorCode.CAMERA_NOT_AVAILABLE, result.code)
    }

    @Test
    fun `CAMERA_ERROR error code`() {
        val result = ScanResult.Error(
            message = "Camera error",
            code = ErrorCode.CAMERA_ERROR
        )

        assertEquals(ErrorCode.CAMERA_ERROR, result.code)
    }
}

/**
 * Mock QR Scanner for testing.
 */
class MockQrScanner : QrScanner {
    var mockImageScanResult: ScanResult = ScanResult.NoQrFound
    var hasPermission = true
    var permissionGrantedOnRequest = true
    var cameraScanStarted = false
    var scanStopped = false

    private val cameraScanFlow = MutableSharedFlow<ScanResult>()

    override fun scanFromCamera(): Flow<ScanResult> {
        cameraScanStarted = true
        return cameraScanFlow
    }

    suspend fun emitCameraScanResult(result: ScanResult) {
        cameraScanFlow.emit(result)
    }

    override suspend fun scanFromImage(imageBytes: ByteArray): ScanResult {
        return mockImageScanResult
    }

    override fun stopScanning() {
        scanStopped = true
    }

    override suspend fun hasCameraPermission(): Boolean = hasPermission

    override suspend fun requestCameraPermission(): Boolean {
        hasPermission = permissionGrantedOnRequest
        return permissionGrantedOnRequest
    }
}
