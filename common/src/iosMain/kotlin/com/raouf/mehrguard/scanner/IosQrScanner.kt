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
import com.raouf.mehrguard.model.ScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * iOS QR Scanner - Platform Layer Interface
 *
 * ARCHITECTURAL DESIGN:
 * =====================
 * This is the `actual` implementation of [QrScanner] for iOS. The camera scanning
 * is intentionally delegated to native Swift code (AVFoundation + Vision framework)
 * for these important reasons:
 *
 * 1. **Performance**: Native iOS camera APIs are optimized for real-time processing
 * 2. **Battery**: AVCaptureSession is hardware-accelerated on iOS
 * 3. **Best Practices**: Apple's Vision framework provides superior barcode detection
 * 4. **UI Integration**: SwiftUI camera views integrate seamlessly with iOS lifecycle
 *
 * THE REAL KMP VALUE:
 * ===================
 * The SHARED business logic lives in `commonMain`:
 * - [com.qrshield.core.PhishingEngine] - URL analysis
 * - [com.qrshield.engine.HeuristicsEngine] - 25+ security heuristics
 * - [com.qrshield.engine.BrandDetector] - 500+ brand fuzzy matching
 * - [com.qrshield.ml.LogisticRegressionModel] - ML scoring
 *
 * The Swift layer calls these Kotlin APIs directly via the KMP framework.
 * See: `iosApp/MehrGuard/Bridge/KMPBridge.swift` for the integration.
 *
 * @author QR-SHIELD Security Team
 * @since 1.0.0
 * @see com.qrshield.core.PhishingEngine
 */
class IosQrScanner : QrScanner {

    /**
     * Camera scanning is implemented in native SwiftUI (ScannerView.swift).
     * This returns an empty flow as the native layer handles the camera stream.
     *
     * The detected URL string is passed directly to [PhishingEngine.analyze()]
     * from Swift code, which calls the shared Kotlin implementation.
     */
    override fun scanFromCamera(): Flow<ScanResult> = flowOf()

    /**
     * Image scanning - native Vision framework handles QR detection.
     * Swift code extracts the URL and passes it to shared Kotlin analysis.
     */
    override suspend fun scanFromImage(imageBytes: ByteArray): ScanResult {
        // Native Vision framework used in Swift for image-based scanning
        return ScanResult.NoQrFound
    }

    /**
     * Stop scanning - handled by native AVCaptureSession.
     */
    override fun stopScanning() {
        // Native iOS session lifecycle managed in SwiftUI
    }

    /**
     * Camera permission is managed by native iOS Info.plist + system prompts.
     * The iOS app declares NSCameraUsageDescription in Info.plist.
     */
    override suspend fun hasCameraPermission(): Boolean = false

    /**
     * Camera permission request triggers native iOS permission dialog.
     * Managed in Swift with AVCaptureDevice.requestAccess().
     */
    override suspend fun requestCameraPermission(): Boolean = false
}

/**
 * iOS-specific factory for [QrScanner].
 *
 * This is the `actual` implementation of the `expect class QrScannerFactory`
 * declared in commonMain, demonstrating proper KMP expect/actual pattern usage.
 */
actual class QrScannerFactory {
    actual fun create(): QrScanner = IosQrScanner()
}

