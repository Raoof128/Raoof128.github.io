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

import com.raouf.mehrguard.model.ContentType
import com.raouf.mehrguard.model.ErrorCode
import com.raouf.mehrguard.model.ScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Web QR Scanner Implementation
 *
 * Shared implementation for both Kotlin/JS and Kotlin/Wasm targets
 * using the webMain source set introduced in Kotlin 2.2.20.
 *
 * Note: Camera scanning is handled by JavaScript/HTML5 in the web app.
 * This implementation provides the Kotlin interface.
 *
 * @author QR-SHIELD Security Team
 * @since 1.17.25
 */
class WebQrScanner : QrScanner {

    private var isScanning = false

    /**
     * Start continuous camera scanning.
     * 
     * In the web environment, camera scanning is handled by the 
     * JavaScript layer using HTML5 MediaDevices API.
     */
    override fun scanFromCamera(): Flow<ScanResult> {
        return flowOf(
            ScanResult.Error(
                "Camera scanning is handled by JavaScript. Use the web app's scanner page.",
                ErrorCode.CAMERA_NOT_AVAILABLE
            )
        )
    }

    /**
     * Scan QR code from image bytes.
     */
    override suspend fun scanFromImage(imageBytes: ByteArray): ScanResult {
        return if (imageBytes.isEmpty()) {
            ScanResult.Error("Empty image data", ErrorCode.INVALID_IMAGE)
        } else {
            // In web, image scanning is delegated to the JavaScript layer
            ScanResult.Error(
                "Image scanning is handled by JavaScript. Use the web app's import feature.",
                ErrorCode.CAMERA_NOT_AVAILABLE
            )
        }
    }

    override fun stopScanning() {
        isScanning = false
    }

    override suspend fun hasCameraPermission(): Boolean = true

    override suspend fun requestCameraPermission(): Boolean = true
}

/**
 * Web-specific factory implementation.
 */
actual class QrScannerFactory {
    actual fun create(): QrScanner = WebQrScanner()
}
