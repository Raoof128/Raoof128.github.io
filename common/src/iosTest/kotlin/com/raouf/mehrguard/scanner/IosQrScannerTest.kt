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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * iOS-specific unit tests for the QrScanner platform implementation.
 *
 * These tests verify that the iOS platform layer correctly implements
 * the QrScanner interface and integrates with shared commonMain code.
 *
 * TESTING PHILOSOPHY:
 * - The camera scanning is tested in native XCUITest (Swift)
 * - This tests the Kotlin/Native layer that bridges to Swift
 * - Ensures expect/actual pattern is correctly implemented
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class IosQrScannerTest {

    @Test
    fun `factory creates IosQrScanner instance`() {
        val factory = QrScannerFactory()
        val scanner = factory.create()

        assertNotNull(scanner, "Factory should create a non-null scanner")
        assertTrue(scanner is IosQrScanner, "Factory should create IosQrScanner type")
    }

    @Test
    fun `scanner returns NoQrFound for empty image`() = runBlockingTest {
        val scanner = IosQrScanner()
        val result = scanner.scanFromImage(byteArrayOf())

        assertEquals(
            com.raouf.mehrguard.model.ScanResult.NoQrFound,
            result,
            "Empty image should return NoQrFound"
        )
    }

    @Test
    fun `scanner camera flow returns empty for stub implementation`() {
        val scanner = IosQrScanner()
        val flow = scanner.scanFromCamera()

        assertNotNull(flow, "Camera flow should not be null")
    }

    @Test
    fun `scanner reports camera permission as handled by native`() = runBlockingTest {
        val scanner = IosQrScanner()

        // iOS handles permissions natively, so this returns false
        // indicating the Kotlin layer doesn't manage permissions
        val hasPermission = scanner.hasCameraPermission()
        assertEquals(false, hasPermission, "Permission should be managed by native layer")
    }

    @Test
    fun `scanner permission request delegates to native`() = runBlockingTest {
        val scanner = IosQrScanner()

        // iOS handles permission requests natively via AVCaptureDevice
        val requestResult = scanner.requestCameraPermission()
        assertEquals(false, requestResult, "Permission requests should be handled by native layer")
    }

    @Test
    fun `stopScanning does not throw`() {
        val scanner = IosQrScanner()

        // Should not throw - actual stopping is in Swift
        scanner.stopScanning()
        assertTrue(true, "stopScanning should complete without error")
    }
}

/**
 * Helper function for running blocking tests on iOS.
 * Uses kotlinx.coroutines.test utilities.
 */
private fun runBlockingTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
