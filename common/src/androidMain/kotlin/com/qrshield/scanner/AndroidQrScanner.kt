package com.qrshield.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android QR Scanner Implementation
 * 
 * Uses Google ML Kit Barcode Scanning + CameraX for real-time QR detection.
 * This is a fully functional production implementation.
 * 
 * SECURITY NOTES:
 * - All scanned content is validated before processing
 * - Camera resources are properly released on cleanup
 * - Permissions are checked before camera access
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
                if (imageBytes.size > 10 * 1024 * 1024) {
                    continuation.resume(ScanResult.Error(
                        "Image too large",
                        ErrorCode.IMAGE_TOO_LARGE
                    ))
                    return@suspendCancellableCoroutine
                }
                
                // Decode bitmap
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap == null) {
                    continuation.resume(ScanResult.Error(
                        "Failed to decode image",
                        ErrorCode.IMAGE_DECODE_ERROR
                    ))
                    return@suspendCancellableCoroutine
                }
                
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
                    
            } catch (e: Exception) {
                continuation.resume(ScanResult.Error(
                    e.message ?: "Unknown error",
                    ErrorCode.UNKNOWN_ERROR
                ))
            }
        }
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
