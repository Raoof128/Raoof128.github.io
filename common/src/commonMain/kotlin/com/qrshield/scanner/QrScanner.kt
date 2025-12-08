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

import com.qrshield.model.ScanResult
import kotlinx.coroutines.flow.Flow

/**
 * QR Scanner Interface
 * 
 * Platform-agnostic interface for QR code scanning.
 * Each platform provides its own implementation using
 * expect/actual declarations.
 */
interface QrScanner {
    
    /**
     * Start continuous camera scanning.
     * Emits scan results as QR codes are detected.
     */
    fun scanFromCamera(): Flow<ScanResult>
    
    /**
     * Scan a QR code from an image.
     * @param imageBytes Raw image data
     * @return Scan result
     */
    suspend fun scanFromImage(imageBytes: ByteArray): ScanResult
    
    /**
     * Stop active camera scanning
     */
    fun stopScanning()
    
    /**
     * Check if camera permission is granted
     */
    suspend fun hasCameraPermission(): Boolean
    
    /**
     * Request camera permission
     */
    suspend fun requestCameraPermission(): Boolean
}

/**
 * Expect declaration for platform-specific QR scanner factory
 */
expect class QrScannerFactory {
    fun create(): QrScanner
}
