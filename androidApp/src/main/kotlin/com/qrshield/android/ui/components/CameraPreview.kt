package com.qrshield.android.ui.components

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Composable Camera Preview with ML Kit QR Scanning
 * 
 * Uses CameraX for viewfinder and ML Kit for barcode detection.
 * This is a high-performance, production-ready implementation.
 * 
 * @param modifier Modifier for the preview
 * @param scaleType How to scale the preview
 * @param cameraSelector Front or back camera
 * @param onQrCodeScanned Callback when QR code is detected
 * 
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onQrCodeScanned: ((String) -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    
    // Executor for image analysis
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Remember the PreviewView to prevent rebuilds on recomposition
    val previewView = remember {
        PreviewView(context).apply {
            this.scaleType = scaleType
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Enable hardware acceleration
            setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE)
        }
    }
    
    // Barcode scanner
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    
    // Track last scanned code for debouncing
    val lastScannedCode = remember { mutableMapOf<String, Long>() }
    val debounceMs = 2000L // Don't re-scan same code within 2 seconds

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
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
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                        @androidx.camera.core.ExperimentalGetImage
                        val mediaImage = imageProxy.image
                        
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        if (barcode.valueType == Barcode.TYPE_URL ||
                                            barcode.valueType == Barcode.TYPE_TEXT) {
                                            
                                            val content = barcode.rawValue ?: continue
                                            val now = System.currentTimeMillis()
                                            val lastScan = lastScannedCode[content] ?: 0L
                                            
                                            // Debounce: only notify if not recently scanned
                                            if (now - lastScan > debounceMs) {
                                                lastScannedCode[content] = now
                                                onQrCodeScanned?.invoke(content)
                                            }
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }
                }
            
            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()
                
                // Bind both preview and analysis to lifecycle
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                // Handle camera binding errors
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
        
        onDispose {
            // Shutdown executor when composable leaves
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}
