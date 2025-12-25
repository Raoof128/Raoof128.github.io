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

@file:Suppress("DEPRECATION") // LocalLifecycleOwner deprecated in Compose 1.6+, migrate when lifecycle 2.8.0+ is adopted

package com.qrshield.android.ui.components

import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import com.qrshield.android.R
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Enhanced Camera Preview with ML Kit QR Scanning
 *
 * Android 16 Features:
 * - CameraX 1.4.0 with improved stability
 * - Optimized frame analysis with ML Kit 17.3.0
 * - Thread-safe scanning with atomic flag
 * - Memory-efficient debouncing
 * - Full accessibility support with TalkBack
 * - Fixed processing flag reset bug
 *
 * @param modifier Modifier for the preview
 * @param scaleType How to scale the preview
 * @param cameraSelector Front or back camera
 * @param onQrCodeScanned Callback when QR code is detected
 * @param onCameraError Callback when camera error occurs
 *
 * @author QR-SHIELD Security Team
 * @since 1.1.0
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onQrCodeScanned: ((String) -> Unit)? = null,
    onCameraError: ((String) -> Unit)? = null
) {
    // Deprecated in Compose 1.6+, migrate when lifecycle 2.8.0+ is adopted
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // State for camera
    var camera by remember { mutableStateOf<Camera?>(null) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    // Thread-safe flag to prevent multiple simultaneous callbacks
    val isProcessing = remember { AtomicBoolean(false) }

    // Executor for image analysis (single thread for sequential processing)
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    // Remember the PreviewView to prevent rebuilds on recomposition
    val previewView = remember {
        PreviewView(context).apply {
            this.scaleType = scaleType
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Use PERFORMANCE mode for faster rendering
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            // Accessibility: Set content description for TalkBack
            contentDescription = context.getString(
                android.R.string.untitled
            ).let { "QR code scanner camera preview. Point your camera at a QR code to scan it." }
        }
    }

    // Barcode scanner (ML Kit 17.3.0)
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    // Debouncing for scanned codes (prevent duplicate callbacks)
    val lastScannedCode = remember { mutableMapOf<String, Long>() }
    val debounceMs = 2000L // Don't re-scan same code within 2 seconds

    DisposableEffect(lifecycleOwner, cameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Preview use case
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // Image analysis use case for QR scanning
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                            // Skip if already processing to prevent callback spam
                            if (isProcessing.get()) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            @Suppress("UnsafeOptInUsageError")
                            val mediaImage = imageProxy.image

                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        for (barcode in barcodes) {
                                            // Support multiple barcode formats
                                            when (barcode.format) {
                                                Barcode.FORMAT_QR_CODE,
                                                Barcode.FORMAT_DATA_MATRIX,
                                                Barcode.FORMAT_AZTEC -> {
                                                    val content = barcode.rawValue
                                                    if (content != null && content.isNotBlank()) {
                                                        val now = System.currentTimeMillis()
                                                        val lastScan = lastScannedCode[content] ?: 0L

                                                        // Debounce: only notify if not recently scanned
                                                        if (now - lastScan > debounceMs) {
                                                            // Set processing flag before callback
                                                            if (isProcessing.compareAndSet(false, true)) {
                                                                lastScannedCode[content] = now

                                                                try {
                                                                    onQrCodeScanned?.invoke(content)
                                                                } finally {
                                                                    // FIX: Always reset the processing flag after callback
                                                                    // This was the critical bug causing scanning to stop
                                                                    isProcessing.set(false)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                else -> { /* Ignore other formats */ }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        // Reset processing flag on error
                                        isProcessing.set(false)
                                        cameraError = "Scan failed: ${e.message}"
                                        onCameraError?.invoke(cameraError!!)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind preview and analysis to lifecycle
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

                // Clear any previous errors on successful binding
                cameraError = null

            } catch (exc: Exception) {
                // Log and report camera binding errors
                val errorMessage = when (exc) {
                    is SecurityException -> "Camera permission denied"
                    is IllegalStateException -> "Camera is in use by another app"
                    is IllegalArgumentException -> "Invalid camera configuration"
                    else -> "Camera initialization failed: ${exc.message}"
                }
                cameraError = errorMessage
                onCameraError?.invoke(errorMessage)
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            // Shutdown executor when composable leaves
            try {
                analysisExecutor.shutdown()
            } catch (e: Exception) {
                // Ignore shutdown errors
            }

            // Clear the debounce cache
            lastScannedCode.clear()

            // Reset processing flag
            isProcessing.set(false)
        }
    }

    val cameraPreviewDesc = stringResource(R.string.cd_camera_preview)
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                // Accessibility: Describe the camera preview for TalkBack
                contentDescription = cameraPreviewDesc
            }
    ) {
        // Camera preview layer
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )
    }
}
