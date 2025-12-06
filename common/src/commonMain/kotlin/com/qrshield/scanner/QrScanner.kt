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
