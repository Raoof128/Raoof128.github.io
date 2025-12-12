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

import com.qrshield.model.ContentType
import com.qrshield.model.ErrorCode
import com.qrshield.model.ScanResult
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.await
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.ImageData
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import kotlin.js.Promise
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * External declaration for jsQR library.
 * 
 * jsQR is a pure JavaScript QR code reading library.
 * Must be included via <script src="https://cdn.jsdelivr.net/npm/jsqr/dist/jsQR.min.js"></script>
 */
@JsModule("jsqr")
@JsNonModule
external fun jsQR(data: dynamic, width: Int, height: Int, options: dynamic = definedExternally): JsQRResult?

external interface JsQRResult {
    val data: String
    val binaryData: dynamic
    val location: dynamic
}

/**
 * Web QR Scanner Implementation
 * 
 * Uses jsQR library and MediaDevices API for browser-based QR scanning.
 * Works in modern browsers (Chrome, Firefox, Safari, Edge).
 * 
 * ARCHITECTURE:
 * - navigator.mediaDevices.getUserMedia for camera access
 * - Canvas API for frame capture
 * - jsQR library for QR decoding
 * 
 * SECURITY NOTES:
 * - HTTPS required for camera access in production
 * - Content length is validated
 * - Media streams are properly stopped on cleanup
 * 
 * REQUIREMENTS:
 * - Include jsQR library in HTML: 
 *   <script src="https://cdn.jsdelivr.net/npm/jsqr/dist/jsQR.min.js"></script>
 * - HTTPS connection (localhost allowed for development)
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
class WebQrScanner : QrScanner {
    
    companion object {
        /** Maximum content length to accept */
        private const val MAX_CONTENT_LENGTH = 4096
        
        /** Frame analysis interval in milliseconds */
        private const val FRAME_DELAY_MS = 100L
        
        /** Camera resolution constraints */
        private const val VIDEO_WIDTH = 1280
        private const val VIDEO_HEIGHT = 720
    }
    
    private var isScanning = false
    private var mediaStream: MediaStream? = null
    
    /**
     * Start continuous camera scanning using MediaDevices API.
     * 
     * Creates a video element, captures frames to canvas, and
     * processes them with jsQR for QR code detection.
     */
    override fun scanFromCamera(): Flow<ScanResult> = callbackFlow {
        isScanning = true
        
        try {
            // Security check: Camera access requires HTTPS in production
            val isSecure = window.location.protocol == "https:" || 
                          window.location.hostname == "localhost" ||
                          window.location.hostname == "127.0.0.1"
            
            if (!isSecure) {
                trySend(ScanResult.Error(
                    "Camera access requires a secure connection (HTTPS). " +
                    "Please use HTTPS or access via localhost for development.",
                    ErrorCode.CAMERA_NOT_AVAILABLE
                ))
                close()
                return@callbackFlow
            }
            
            // Check if getUserMedia is available
            val mediaDevices = window.navigator.asDynamic().mediaDevices
            if (mediaDevices == null) {
                trySend(ScanResult.Error(
                    "Camera API not available. Please use a modern browser " +
                    "(Chrome, Firefox, Safari, Edge) with camera support.",
                    ErrorCode.CAMERA_NOT_AVAILABLE
                ))
                close()
                return@callbackFlow
            }
            
            // Request camera access
            val constraints = js("{video: {facingMode: 'environment', width: {ideal: $VIDEO_WIDTH}, height: {ideal: $VIDEO_HEIGHT}}}")
            
            val streamPromise: Promise<MediaStream> = mediaDevices.getUserMedia(constraints).unsafeCast<Promise<MediaStream>>()
            
            mediaStream = streamPromise.await()
            
            val stream = mediaStream
            if (stream == null) {
                trySend(ScanResult.Error(
                    "Failed to get camera stream",
                    ErrorCode.CAMERA_ERROR
                ))
                close()
                return@callbackFlow
            }
            
            // Create video element
            val video = document.createElement("video") as HTMLVideoElement
            video.srcObject = stream
            video.setAttribute("playsinline", "true") // Required for iOS
            video.play()
            
            // Wait for video to be ready
            while (video.readyState != HTMLVideoElement.HAVE_ENOUGH_DATA) {
                delay(100)
            }
            
            // Create canvas for frame capture
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            canvas.width = video.videoWidth
            canvas.height = video.videoHeight
            val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
            
            // Scanning loop
            while (isScanning) {
                try {
                    // Draw current frame to canvas
                    ctx.drawImage(video, 0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
                    
                    // Get image data
                    val imageData = ctx.getImageData(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
                    
                    // Process with jsQR
                    val result = processImageData(imageData)
                    if (result is ScanResult.Success) {
                        trySend(result)
                    }
                    
                } catch (e: Exception) {
                    // Continue on frame processing errors
                }
                
                delay(FRAME_DELAY_MS)
            }
            
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("NotAllowed") == true -> "Camera permission denied"
                e.message?.contains("NotFound") == true -> "No camera found"
                e.message?.contains("NotReadable") == true -> "Camera is in use by another app"
                else -> e.message ?: "Camera error"
            }
            
            trySend(ScanResult.Error(errorMessage, ErrorCode.CAMERA_ERROR))
        }
        
        awaitClose {
            isScanning = false
            stopMediaStream()
        }
    }
    
    /**
     * Scan QR code from image bytes.
     * 
     * @param imageBytes Raw image data
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
                
                // Security: Limit image size
                if (imageBytes.size > 10 * 1024 * 1024) {
                    continuation.resume(ScanResult.Error(
                        "Image too large",
                        ErrorCode.IMAGE_TOO_LARGE
                    ))
                    return@suspendCancellableCoroutine
                }
                
                // Convert to Blob and create image
                val blob = js("new Blob([new Uint8Array(imageBytes)], {type: 'image/png'})")
                val url = js("URL.createObjectURL(blob)") as String
                
                val img = js("new Image()").unsafeCast<dynamic>()
                
                img.onload = {
                    try {
                        val canvas = document.createElement("canvas") as HTMLCanvasElement
                        canvas.width = (img.width as Number).toInt()
                        canvas.height = (img.height as Number).toInt()
                        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
                        
                        ctx.drawImage(img, 0.0, 0.0)
                        
                        val imageData = ctx.getImageData(
                            0.0, 0.0, 
                            canvas.width.toDouble(), 
                            canvas.height.toDouble()
                        )
                        
                        val result = processImageData(imageData)
                        continuation.resume(result)
                        
                        // Clean up
                        js("URL.revokeObjectURL(url)")
                        
                    } catch (e: Exception) {
                        continuation.resume(ScanResult.Error(
                            e.message ?: "Failed to process image",
                            ErrorCode.DECODE_ERROR
                        ))
                    }
                }
                
                img.onerror = {
                    js("URL.revokeObjectURL(url)")
                    continuation.resume(ScanResult.Error(
                        "Failed to load image",
                        ErrorCode.IMAGE_DECODE_ERROR
                    ))
                }
                
                img.src = url
                
            } catch (e: Exception) {
                continuation.resume(ScanResult.Error(
                    e.message ?: "Unknown error",
                    ErrorCode.UNKNOWN_ERROR
                ))
            }
        }
    }
    
    /**
     * Process ImageData with jsQR library.
     */
    private fun processImageData(imageData: ImageData): ScanResult {
        return try {
            val result = jsQR(
                imageData.data,
                imageData.width,
                imageData.height,
                js("{inversionAttempts: 'dontInvert'}")
            )
            
            if (result != null) {
                val content = result.data
                
                // Security: Validate content length
                if (content.length > MAX_CONTENT_LENGTH) {
                    return ScanResult.Error(
                        "QR content too long",
                        ErrorCode.CONTENT_TOO_LARGE
                    )
                }
                
                val contentType = detectContentType(content)
                ScanResult.Success(content, contentType)
            } else {
                ScanResult.NoQrFound
            }
            
        } catch (e: Exception) {
            // jsQR may not be loaded
            if (js("typeof jsQR === 'undefined'") as Boolean) {
                ScanResult.Error(
                    "jsQR library not loaded. Add: <script src=\"https://cdn.jsdelivr.net/npm/jsqr/dist/jsQR.min.js\"></script>",
                    ErrorCode.LIBRARY_NOT_LOADED
                )
            } else {
                ScanResult.Error(
                    e.message ?: "QR decode failed",
                    ErrorCode.DECODE_ERROR
                )
            }
        }
    }
    
    /**
     * Stop active camera scanning.
     */
    override fun stopScanning() {
        isScanning = false
        stopMediaStream()
    }
    
    /**
     * Stop and clean up media stream.
     */
    private fun stopMediaStream() {
        mediaStream?.getTracks()?.forEach { track ->
            track.asDynamic().stop()
        }
        mediaStream = null
    }
    
    /**
     * Check if camera permission is granted.
     */
    override suspend fun hasCameraPermission(): Boolean {
        return try {
            val permissions = window.navigator.asDynamic().permissions
            if (permissions == null) {
                // Permissions API not available, assume permission needed
                false
            } else {
                val result = (permissions.query(js("{name: 'camera'}")) as Promise<dynamic>).await()
                result.state == "granted"
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Request camera permission.
     * 
     * In browsers, permission is requested when getUserMedia is called.
     */
    override suspend fun requestCameraPermission(): Boolean {
        return try {
            val mediaDevices = window.navigator.asDynamic().mediaDevices ?: return false
            val constraints = js("{video: true}")
            val stream = (mediaDevices.getUserMedia(constraints) as Promise<MediaStream>).await()
            
            // Stop the test stream immediately
            stream.getTracks().forEach { track -> 
                track.asDynamic().stop() 
            }
            
            true
        } catch (e: Exception) {
            false
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
 * Web-specific factory implementation.
 */
actual class QrScannerFactory {
    actual fun create(): QrScanner = WebQrScanner()
}
