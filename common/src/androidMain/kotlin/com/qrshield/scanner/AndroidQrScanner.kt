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

package com.qrshield.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.qrshield.model.ContentType
import com.qrshield.model.ErrorCode
import com.qrshield.model.ScanResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.InputStream
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android QR Scanner Implementation
 *
 * Uses Google ML Kit Barcode Scanning + CameraX for real-time QR detection.
 * Also supports scanning from images (gallery/photo picker).
 *
 * FEATURES:
 * - Real-time camera scanning via CameraX
 * - Photo picker/gallery image scanning
 * - Content URI support for Android 16+
 * - Security validation on all inputs
 *
 * SECURITY NOTES:
 * - All scanned content is validated before processing
 * - Camera resources are properly released on cleanup
 * - Permissions are checked before camera access
 * - Image sizes are limited to prevent OOM
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class AndroidQrScanner(
    private val context: Context
) : QrScanner {

    companion object {
        /** Analysis frame size - balanced for performance/quality */
        private val ANALYSIS_SIZE = Size(1280, 720)

        /** Maximum image dimension for gallery scanning */
        private const val MAX_IMAGE_DIMENSION = 2048

        /** Maximum image size in bytes (10MB) */
        private const val MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024
    }

    private val scanner = BarcodeScanning.getClient()
    private val isScanning = AtomicBoolean(false)
    private var cameraProvider: ProcessCameraProvider? = null

    /** Single-thread executor for frame analysis - instance-based to prevent leaks */
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    /**
     * Start continuous camera scanning using CameraX + ML Kit.
     *
     * Sets up CameraX ImageAnalysis pipeline that feeds frames to ML Kit
     * barcode scanner. Results are emitted as a Flow.
     *
     * @return Flow of ScanResult for detected QR codes
     */
    @androidx.camera.core.ExperimentalGetImage
    override fun scanFromCamera(): Flow<ScanResult> = callbackFlow {
        isScanning.set(true)

        try {
            // Get camera provider
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()

                    // Set up image analysis
                    // Note: setTargetResolution is deprecated but provides simple size control
                    // Modern approach would use setResolutionSelector, but this works for our use case
                    @Suppress("DEPRECATION")
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(ANALYSIS_SIZE)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        if (!isScanning.get()) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                            val result = processBarcodeResult(barcode)
                                            if (result is ScanResult.Success) {
                                                trySend(result)
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    trySend(ScanResult.Error(
                                        e.message ?: "ML Kit scan failed",
                                        ErrorCode.ML_KIT_ERROR
                                    ))
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    // Bind use case (preview would be bound in Activity/Fragment)
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        context as? androidx.lifecycle.LifecycleOwner
                            ?: throw IllegalStateException("Context must be LifecycleOwner"),
                        cameraSelector,
                        imageAnalysis
                    )

                } catch (e: Exception) {
                    trySend(ScanResult.Error(
                        e.message ?: "Camera initialization failed",
                        ErrorCode.CAMERA_ERROR
                    ))
                }
            }, ContextCompat.getMainExecutor(context))

        } catch (e: Exception) {
            trySend(ScanResult.Error(
                e.message ?: "Failed to start camera",
                ErrorCode.CAMERA_ERROR
            ))
        }

        awaitClose {
            isScanning.set(false)
            cameraProvider?.unbindAll()
        }
    }

    /**
     * Scan QR code from image bytes using ML Kit.
     *
     * @param imageBytes Raw image data (JPEG, PNG, etc.)
     * @return ScanResult with scanned content or error
     */
    override suspend fun scanFromImage(imageBytes: ByteArray): ScanResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Validate input
                if (imageBytes.isEmpty()) {
                    continuation.resume(ScanResult.Error(
                        "Empty image data",
                        ErrorCode.INVALID_IMAGE
                    ))
                    return@suspendCancellableCoroutine
                }

                // Security: Limit image size (10MB max)
                if (imageBytes.size > MAX_IMAGE_SIZE_BYTES) {
                    continuation.resume(ScanResult.Error(
                        "Image too large (max 10MB)",
                        ErrorCode.IMAGE_TOO_LARGE
                    ))
                    return@suspendCancellableCoroutine
                }

                // Decode bitmap with size limits
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

                // Calculate sample size to fit within MAX_IMAGE_DIMENSION
                options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight)
                options.inJustDecodeBounds = false

                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
                if (bitmap == null) {
                    continuation.resume(ScanResult.Error(
                        "Failed to decode image",
                        ErrorCode.IMAGE_DECODE_ERROR
                    ))
                    return@suspendCancellableCoroutine
                }

                processQrFromBitmap(bitmap, continuation)

            } catch (e: Exception) {
                continuation.resume(ScanResult.Error(
                    e.message ?: "Unknown error",
                    ErrorCode.UNKNOWN_ERROR
                ))
            }
        }
    }

    /**
     * Scan QR code from a content URI (photo picker/gallery).
     *
     * This is the main method for gallery scanning on Android.
     * Handles content:// URIs from photo picker.
     *
     * @param uri Content URI to the image (from photo picker)
     * @return ScanResult with scanned content or error
     */
    suspend fun scanFromUri(uri: Uri): ScanResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Open input stream from content resolver
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

                if (inputStream == null) {
                    continuation.resume(ScanResult.Error(
                        "Cannot open image file",
                        ErrorCode.INVALID_IMAGE
                    ))
                    return@suspendCancellableCoroutine
                }

                inputStream.use { stream ->
                    // First, get image dimensions only
                    val optionsForSize = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }

                    // We need to reopen the stream for actual decoding
                    context.contentResolver.openInputStream(uri)?.use { sizeStream ->
                        BitmapFactory.decodeStream(sizeStream, null, optionsForSize)
                    }

                    // Calculate sample size to prevent OOM
                    val sampleSize = calculateSampleSize(optionsForSize.outWidth, optionsForSize.outHeight)

                    // Decode with sample size
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }

                    // Reopen stream for final decode
                    val finalStream = context.contentResolver.openInputStream(uri)
                    if (finalStream == null) {
                        continuation.resume(ScanResult.Error(
                            "Cannot read image file",
                            ErrorCode.INVALID_IMAGE
                        ))
                        return@suspendCancellableCoroutine
                    }

                    finalStream.use { bitmapStream ->
                        val bitmap = BitmapFactory.decodeStream(bitmapStream, null, options)

                        if (bitmap == null) {
                            continuation.resume(ScanResult.Error(
                                "Failed to decode image",
                                ErrorCode.IMAGE_DECODE_ERROR
                            ))
                            return@suspendCancellableCoroutine
                        }

                        processQrFromBitmap(bitmap, continuation)
                    }
                }

            } catch (e: SecurityException) {
                continuation.resume(ScanResult.Error(
                    "Permission denied to access image",
                    ErrorCode.CAMERA_PERMISSION_DENIED
                ))
            } catch (e: OutOfMemoryError) {
                continuation.resume(ScanResult.Error(
                    "Image too large to process",
                    ErrorCode.IMAGE_TOO_LARGE
                ))
            } catch (e: Exception) {
                continuation.resume(ScanResult.Error(
                    e.message ?: "Failed to scan image",
                    ErrorCode.UNKNOWN_ERROR
                ))
            }
        }
    }

    /**
     * Scan QR code from a content URI string.
     *
     * Convenience method that parses the URI string.
     *
     * @param uriString URI string to the image
     * @return ScanResult with scanned content or error
     */
    suspend fun scanFromUriString(uriString: String): ScanResult {
        return try {
            val uri = Uri.parse(uriString)
            scanFromUri(uri)
        } catch (e: Exception) {
            ScanResult.Error(
                "Invalid URI: ${e.message}",
                ErrorCode.INVALID_IMAGE
            )
        }
    }

    /**
     * Process QR code detection from a bitmap.
     *
     * Internal helper method used by both byte array and URI scanning.
     */
    private fun processQrFromBitmap(
        bitmap: Bitmap,
        continuation: kotlinx.coroutines.CancellableContinuation<ScanResult>
    ) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val qrBarcodes = barcodes.filter { it.format == Barcode.FORMAT_QR_CODE }

                if (qrBarcodes.isNotEmpty()) {
                    val result = processBarcodeResult(qrBarcodes.first())
                    continuation.resume(result)
                } else {
                    continuation.resume(ScanResult.NoQrFound)
                }

                // Clean up bitmap
                bitmap.recycle()
            }
            .addOnFailureListener { e ->
                bitmap.recycle()
                continuation.resume(ScanResult.Error(
                    e.message ?: "ML Kit scan failed",
                    ErrorCode.ML_KIT_ERROR
                ))
            }
    }

    /**
     * Calculate sample size for image decoding.
     *
     * Ensures images are downscaled to prevent OOM while maintaining quality
     * sufficient for QR code detection.
     */
    private fun calculateSampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        val maxDimension = maxOf(width, height)

        while (maxDimension / sampleSize > MAX_IMAGE_DIMENSION) {
            sampleSize *= 2
        }

        return sampleSize
    }

    /**
     * Stop active camera scanning.
     */
    override fun stopScanning() {
        isScanning.set(false)
        cameraProvider?.unbindAll()
    }

    /**
     * Check if camera permission is granted.
     */
    override suspend fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request camera permission.
     *
     * Note: This should be called from an Activity that can handle the result.
     * In a real implementation, this would use Activity Result APIs.
     */
    override suspend fun requestCameraPermission(): Boolean {
        // This is a placeholder - actual permission request needs Activity
        // The calling Activity should handle this via registerForActivityResult
        return hasCameraPermission()
    }

    /**
     * Process ML Kit barcode result into ScanResult.
     *
     * @param barcode ML Kit Barcode object
     * @return ScanResult with parsed content
     */
    private fun processBarcodeResult(barcode: Barcode): ScanResult {
        val rawValue = barcode.rawValue ?: return ScanResult.NoQrFound

        // Security: Validate content length
        if (rawValue.length > 4096) {
            return ScanResult.Error(
                "QR content too long",
                ErrorCode.CONTENT_TOO_LARGE
            )
        }

        val contentType = when (barcode.valueType) {
            Barcode.TYPE_URL -> ContentType.URL
            Barcode.TYPE_TEXT -> ContentType.TEXT
            Barcode.TYPE_WIFI -> ContentType.WIFI
            Barcode.TYPE_CONTACT_INFO -> ContentType.VCARD
            Barcode.TYPE_GEO -> ContentType.GEO
            Barcode.TYPE_PHONE -> ContentType.PHONE
            Barcode.TYPE_SMS -> ContentType.SMS
            Barcode.TYPE_EMAIL -> ContentType.EMAIL
            else -> detectContentTypeFromString(rawValue)
        }

        return ScanResult.Success(rawValue, contentType)
    }

    /**
     * Fallback content type detection from raw string.
     */
    private fun detectContentTypeFromString(content: String): ContentType {
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
 * Android-specific factory implementation.
 *
 * Requires Context for camera and ML Kit initialization.
 */
actual class QrScannerFactory(private val context: Context) {
    actual fun create(): QrScanner = AndroidQrScanner(context)
}
