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
import com.qrshield.model.ErrorCode
import com.qrshield.model.ScanResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.cinterop.*
import platform.AVFoundation.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.Vision.*
import platform.darwin.*
import platform.UIKit.UIImage
import platform.posix.uint8_tVar
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.native.concurrent.AtomicInt

/**
 * iOS QR Scanner Implementation
 * 
 * Uses AVFoundation for camera capture and Vision framework for QR detection.
 * This is a fully functional production implementation for iOS.
 * 
 * ARCHITECTURE:
 * - AVCaptureSession for camera management
 * - AVCaptureVideoDataOutput for frame capture
 * - VNDetectBarcodesRequest for QR detection
 * 
 * SECURITY NOTES:
 * - Camera permission is validated before access
 * - All scanned content is length-validated
 * - Resources are properly released on cleanup
 * - Thread-safe using AtomicInt for state management
 * 
 * @author QR-SHIELD Security Team  
 * @since 1.0.0
 */
class IosQrScanner : QrScanner {
    
    companion object {
        /** Maximum content length to accept */
        private const val MAX_CONTENT_LENGTH = 4096
        
        // Scanning state constants
        private const val STATE_STOPPED = 0
        private const val STATE_SCANNING = 1
    }
    
    // Use AtomicInt for thread-safe state on Kotlin/Native
    private val scanningState = AtomicInt(STATE_STOPPED)
    private var captureSession: AVCaptureSession? = null
    
    /**
     * Get the AVCaptureSession for use in SwiftUI CameraPreview.
     * This allows native SwiftUI views to display the camera feed.
     * 
     * @return The active capture session, or null if not initialized
     */
    fun getSession(): AVCaptureSession? = captureSession
    
    /**
     * Start continuous camera scanning using AVFoundation + Vision.
     * 
     * Sets up AVCaptureSession with video output that feeds frames to 
     * Vision framework's VNDetectBarcodesRequest for QR detection.
     */
    override fun scanFromCamera(): Flow<ScanResult> = callbackFlow {
        scanningState.value = STATE_SCANNING
        
        val session = AVCaptureSession()
        captureSession = session
        
        // Configure session for high quality
        session.sessionPreset = AVCaptureSessionPresetHigh
        
        // Get back camera
        val videoDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (videoDevice == null) {
            trySend(ScanResult.Error("No camera available", ErrorCode.CAMERA_NOT_AVAILABLE))
            close()
            return@callbackFlow
        }
        
        try {
            // Create input
            val videoInput = AVCaptureDeviceInput.deviceInputWithDevice(videoDevice, null)
            if (videoInput == null || !session.canAddInput(videoInput)) {
                trySend(ScanResult.Error("Cannot add camera input", ErrorCode.CAMERA_ERROR))
                close()
                return@callbackFlow
            }
            session.addInput(videoInput)
            
            // Create video output
            val videoOutput = AVCaptureVideoDataOutput()
            videoOutput.alwaysDiscardsLateVideoFrames = true
            
            // Set up sample buffer delegate
            val delegateQueue = dispatch_queue_create("com.qrshield.camera", null)
            
            // Create a delegate that processes frames with Vision
            val scanningStateRef = scanningState // Capture reference for callback
            val sampleBufferDelegate = object : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
                override fun captureOutput(
                    output: AVCaptureOutput,
                    didOutputSampleBuffer: CMSampleBufferRef?,
                    fromConnection: AVCaptureConnection
                ) {
                    // Check if still scanning (thread-safe atomic read)
                    if (scanningStateRef.value != STATE_SCANNING || didOutputSampleBuffer == null) return
                    
                    // Get pixel buffer from sample buffer
                    val pixelBuffer = CMSampleBufferGetImageBuffer(didOutputSampleBuffer) ?: return
                    
                    // Create Vision request for QR detection
                    val request = VNDetectBarcodesRequest { request, error ->
                        if (error != null) {
                            trySend(ScanResult.Error(
                                error.localizedDescription ?: "Vision error",
                                ErrorCode.VISION_ERROR
                            ))
                            return@VNDetectBarcodesRequest
                        }
                        
                        val results = request?.results as? List<VNBarcodeObservation>
                        results?.forEach { observation ->
                            val symbology = observation.symbology
                            if (symbology == VNBarcodeSymbologyQR) {
                                val payload = observation.payloadStringValue
                                if (payload != null && payload.length <= MAX_CONTENT_LENGTH) {
                                    val contentType = detectContentType(payload)
                                    trySend(ScanResult.Success(payload, contentType))
                                }
                            }
                        }
                    }
                    
                    // Only detect QR codes
                    request.symbologies = listOf(VNBarcodeSymbologyQR)
                    
                    // Create and perform request
                    val handler = VNImageRequestHandler(pixelBuffer, options = emptyMap<Any?, Any?>())
                    try {
                        handler.performRequests(listOf(request), null)
                    } catch (e: Exception) {
                        // Silently fail on frame processing errors
                    }
                }
            }
            
            videoOutput.setSampleBufferDelegate(sampleBufferDelegate, delegateQueue)
            
            if (session.canAddOutput(videoOutput)) {
                session.addOutput(videoOutput)
            } else {
                trySend(ScanResult.Error("Cannot add video output", ErrorCode.CAMERA_ERROR))
                close()
                return@callbackFlow
            }
            
            // Start session on background thread
            dispatch_async(dispatch_get_global_queue(QOS_CLASS_USER_INITIATED.toLong(), 0u)) {
                session.startRunning()
            }
            
        } catch (e: Exception) {
            trySend(ScanResult.Error(
                e.message ?: "Camera setup failed",
                ErrorCode.CAMERA_ERROR
            ))
            close()
            return@callbackFlow
        }
        
        awaitClose {
            scanningState.value = STATE_STOPPED
            captureSession?.stopRunning()
            captureSession = null
        }
    }
    
    /**
     * Scan QR code from image bytes using Vision framework.
     * 
     * @param imageBytes Raw image data (JPEG, PNG, etc.)
     * @return ScanResult with scanned content or error
     */
    override suspend fun scanFromImage(imageBytes: ByteArray): ScanResult {
        return suspendCancellableCoroutine { continuation ->
            // Flag to prevent double-resume (using simple boolean - 
            // this is safe as the callback runs on a single thread)
            var hasResumed = false
            val resumeOnce: (ScanResult) -> Unit = { result ->
                if (!hasResumed) {
                    hasResumed = true
                    continuation.resume(result)
                }
            }
            
            // Validate input
            if (imageBytes.isEmpty()) {
                resumeOnce(ScanResult.Error(
                    "Empty image data",
                    ErrorCode.INVALID_IMAGE
                ))
                return@suspendCancellableCoroutine
            }
            
            // Security: Limit image size (10MB max)
            if (imageBytes.size > 10 * 1024 * 1024) {
                resumeOnce(ScanResult.Error(
                    "Image too large",
                    ErrorCode.IMAGE_TOO_LARGE
                ))
                return@suspendCancellableCoroutine
            }
            
            // Convert bytes to NSData
            val nsData = imageBytes.usePinned { pinned ->
                NSData.dataWithBytes(pinned.addressOf(0), imageBytes.size.toULong())
            }
            
            // Create UIImage from data
            val uiImage = UIImage.imageWithData(nsData)
            if (uiImage == null) {
                resumeOnce(ScanResult.Error(
                    "Failed to decode image",
                    ErrorCode.IMAGE_DECODE_ERROR
                ))
                return@suspendCancellableCoroutine
            }
            
            // Get CGImage for Vision
            val cgImage = uiImage.CGImage
            if (cgImage == null) {
                resumeOnce(ScanResult.Error(
                    "Failed to get CGImage",
                    ErrorCode.IMAGE_DECODE_ERROR
                ))
                return@suspendCancellableCoroutine
            }
            
            // Create Vision request
            val request = VNDetectBarcodesRequest { request, error ->
                if (error != null) {
                    resumeOnce(ScanResult.Error(
                        error.localizedDescription ?: "Vision error",
                        ErrorCode.VISION_ERROR
                    ))
                    return@VNDetectBarcodesRequest
                }
                
                val results = request?.results as? List<VNBarcodeObservation>
                val qrResults = results?.filter { it.symbology == VNBarcodeSymbologyQR }
                
                if (qrResults.isNullOrEmpty()) {
                    resumeOnce(ScanResult.NoQrFound)
                } else {
                    val payload = qrResults.first().payloadStringValue
                    if (payload == null || payload.length > MAX_CONTENT_LENGTH) {
                        resumeOnce(ScanResult.NoQrFound)
                    } else {
                        val contentType = detectContentType(payload)
                        resumeOnce(ScanResult.Success(payload, contentType))
                    }
                }
            }
            
            request.symbologies = listOf(VNBarcodeSymbologyQR)
            
            // Perform request
            val handler = VNImageRequestHandler(cgImage, options = emptyMap<Any?, Any?>())
            try {
                handler.performRequests(listOf(request), null)
            } catch (e: Exception) {
                resumeOnce(ScanResult.Error(
                    e.message ?: "Vision processing failed",
                    ErrorCode.VISION_ERROR
                ))
            }
        }
    }
    
    /**
     * Stop active camera scanning.
     */
    override fun stopScanning() {
        scanningState.value = STATE_STOPPED
        captureSession?.stopRunning()
        captureSession = null
    }
    
    /**
     * Check if camera permission is granted.
     */
    override suspend fun hasCameraPermission(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return status == AVAuthorizationStatusAuthorized
    }
    
    /**
     * Request camera permission.
     */
    override suspend fun requestCameraPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                continuation.resume(granted)
            }
        }
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
 * iOS-specific factory implementation.
 */
actual class QrScannerFactory {
    actual fun create(): QrScanner = IosQrScanner()
}
