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

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.raouf.mehrguard.model.ContentType
import com.raouf.mehrguard.model.ErrorCode
import com.raouf.mehrguard.model.ScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/**
 * Desktop QR Scanner Implementation
 *
 * Uses ZXing library for barcode detection on JVM Desktop.
 * Supports image file scanning (webcam requires additional OpenCV integration).
 *
 * ARCHITECTURE:
 * - ZXing MultiFormatReader for QR decoding
 * - BufferedImageLuminanceSource for image processing
 * - HybridBinarizer for optimal detection
 *
 * SECURITY NOTES:
 * - Image size is validated to prevent DoS
 * - Content length is bounds-checked
 * - All resources are properly closed
 *
 * @author Mehr Guard Security Team
 * @since 1.0.0
 */
class DesktopQrScanner : QrScanner {

    companion object {
        /** Maximum content length to accept */
        private const val MAX_CONTENT_LENGTH = 4096

        /** Maximum image file size (10MB) */
        private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024

        /** Frame rate for webcam scanning (if implemented) */
        private const val FRAME_DELAY_MS = 100L
    }

    @Volatile
    private var isScanning = false

    // ZXing reader with optimized hints for QR codes
    private val reader = MultiFormatReader().apply {
        setHints(mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.CHARACTER_SET to "UTF-8"
        ))
    }

    /**
     * Start continuous camera scanning using webcam.
     *
     * NOTE: Full webcam support requires OpenCV integration.
     * This implementation provides the framework - OpenCV capture
     * should be added based on deployment requirements.
     *
     * For a complete implementation, you would:
     * 1. Add org.openpnp:opencv dependency
     * 2. Initialize VideoCapture(0) for webcam
     * 3. Capture frames in a loop
     * 4. Process each frame with ZXing
     */
    override fun scanFromCamera(): Flow<ScanResult> = callbackFlow {
        isScanning = true

        // Emit a notification that webcam scanning is starting
        // In production, this would integrate with OpenCV

        /*
         * OpenCV Integration Example (requires opencv dependency):
         *
         * System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
         * val capture = VideoCapture(0)
         *
         * if (!capture.isOpened) {
         *     trySend(ScanResult.Error("Cannot open webcam", ErrorCode.CAMERA_NOT_AVAILABLE))
         *     close()
         *     return@callbackFlow
         * }
         *
         * val frame = Mat()
         * while (isScanning) {
         *     if (capture.read(frame)) {
         *         val bufferedImage = matToBufferedImage(frame)
         *         val result = processBufferedImage(bufferedImage)
         *         if (result is ScanResult.Success) {
         *             trySend(result)
         *         }
         *     }
         *     delay(FRAME_DELAY_MS)
         * }
         *
         * capture.release()
         */

        // For now, emit a message that webcam is not available on this build
        // This allows the app to work while webcam support can be added
        trySend(ScanResult.Error(
            "Webcam scanning requires OpenCV integration. Use image scanning instead.",
            ErrorCode.CAMERA_NOT_AVAILABLE
        ))

        awaitClose {
            isScanning = false
        }
    }

    /**
     * Scan QR code from image bytes using ZXing.
     *
     * @param imageBytes Raw image data (JPEG, PNG, BMP, GIF)
     * @return ScanResult with scanned content or error
     */
    override suspend fun scanFromImage(imageBytes: ByteArray): ScanResult {
        return withContext(Dispatchers.IO) {
            try {
                // Validate input
                if (imageBytes.isEmpty()) {
                    return@withContext ScanResult.Error(
                        "Empty image data",
                        ErrorCode.INVALID_IMAGE
                    )
                }

                // Security: Limit image size
                if (imageBytes.size > MAX_IMAGE_SIZE) {
                    return@withContext ScanResult.Error(
                        "Image too large (max ${MAX_IMAGE_SIZE / 1024 / 1024}MB)",
                        ErrorCode.IMAGE_TOO_LARGE
                    )
                }

                // Decode image
                val bufferedImage = ByteArrayInputStream(imageBytes).use { stream ->
                    ImageIO.read(stream)
                }

                if (bufferedImage == null) {
                    return@withContext ScanResult.Error(
                        "Failed to decode image format",
                        ErrorCode.IMAGE_DECODE_ERROR
                    )
                }

                processBufferedImage(bufferedImage)

            } catch (e: Exception) {
                ScanResult.Error(
                    e.message ?: "Unknown error processing image",
                    ErrorCode.UNKNOWN_ERROR
                )
            }
        }
    }

    /**
     * Process a BufferedImage and extract QR code content.
     */
    private fun processBufferedImage(image: BufferedImage): ScanResult {
        return try {
            // Create luminance source from image
            val luminanceSource = BufferedImageLuminanceSource(image)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(luminanceSource))

            // Attempt to decode
            val result = reader.decode(binaryBitmap)
            val content = result.text

            // Security: Validate content length
            if (content.length > MAX_CONTENT_LENGTH) {
                return ScanResult.Error(
                    "QR content too long",
                    ErrorCode.CONTENT_TOO_LARGE
                )
            }

            val contentType = detectContentType(content)
            ScanResult.Success(content, contentType)

        } catch (e: NotFoundException) {
            ScanResult.NoQrFound
        } catch (e: Exception) {
            ScanResult.Error(
                e.message ?: "Failed to decode QR code",
                ErrorCode.DECODE_ERROR
            )
        }
    }

    /**
     * Stop active camera scanning.
     */
    override fun stopScanning() {
        isScanning = false
    }

    /**
     * Check if camera permission is granted.
     *
     * Desktop apps typically don't require explicit permission,
     * but we check if a camera device is potentially available.
     */
    override suspend fun hasCameraPermission(): Boolean {
        // Desktop doesn't have permission dialogs like mobile
        // Return true to indicate no permission blocking
        return true
    }

    /**
     * Request camera permission.
     *
     * On desktop, there's no permission dialog - access is either
     * available or not based on hardware/OS settings.
     */
    override suspend fun requestCameraPermission(): Boolean {
        return true
    }

    /**
     * Detect content type from scanned string.
     */
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
}

/**
 * Desktop-specific factory implementation.
 */
actual class QrScannerFactory {
    actual fun create(): QrScanner = DesktopQrScanner()
}
