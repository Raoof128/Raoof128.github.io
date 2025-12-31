//
// Copyright 2025-2026 Mehr Guard Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

// UI/Scanner/ScannerViewModel.swift
// Mehr Guard Scanner ViewModel - iOS 17+ / Swift 6 Optimized
//
// UPDATED: December 2025 - iOS 17+ Compatible
// - Full @Observable macro support
// - Swift 6 strict concurrency compliance
// - Enhanced camera configuration
// - Comprehensive error handling

import Foundation
@preconcurrency import AVFoundation
import SwiftUI
import Observation
#if canImport(common)
import common
#endif
#if os(iOS)

// MARK: - iOS 17+ Observable ViewModel

/// The iOS-native ViewModel using @Observable macro
/// Provides precise property-level observation for optimal performance
@available(iOS 17, *)
@Observable
@MainActor
final class ScannerViewModel {
    
    // MARK: - Singleton
    
    static let shared = ScannerViewModel()
    
    // MARK: - Observable State
    
    var session: AVCaptureSession?
    var currentResult: RiskAssessmentMock?
    var isScanning = false
    var isAnalyzing = false
    var isFlashOn = false
    var errorMessage: String?
    var cameraPermissionStatus: CameraPermissionStatus = .unknown
    var scanCount: Int = 0
    
    // MARK: - Private Properties
    
    private var captureSession: AVCaptureSession?
    private var videoOutput: AVCaptureMetadataOutput?
    private var metadataDelegate: QRCodeMetadataDelegate?
    private var lastScannedCode: String?
    private var lastScanTime: Date?
    private let imageScanner = QRImageScanner()
    private let maxUrlLength = 2048
    private let allowedSchemes: Set<String> = ["http", "https"]
    
    // Swift 6: Dedicated queue for session operations
    private let sessionQueue = DispatchQueue(label: "com.raouf.mehrguard.session", qos: .userInteractive)
    
    // Debounce configuration
    private let scanDebounceInterval: TimeInterval = 2.0
    
    // KMP Dependencies
    #if canImport(common)
    private let engine: PhishingEngine = PhishingEngine()
    #endif
    
    // MARK: - Initialization
    
    private init() {
        Task {
            await checkCameraPermission()
        }
    }
    
    deinit {
        // Cleanup is handled by ARC
    }
    
    // MARK: - Camera Permission
    
    func checkCameraPermission() async {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            cameraPermissionStatus = .authorized
            await setupCamera()
            autoStartIfNeeded()
        case .notDetermined:
            cameraPermissionStatus = .notDetermined
            let granted = await AVCaptureDevice.requestAccess(for: .video)
            cameraPermissionStatus = granted ? .authorized : .denied
            if granted {
                await setupCamera()
                autoStartIfNeeded()
            }
        case .denied, .restricted:
            cameraPermissionStatus = .denied
        @unknown default:
            cameraPermissionStatus = .unknown
        }
    }

    private func autoStartIfNeeded() {
        if SettingsManager.shared.autoScan {
            startCamera()
        }
    }
    
    // MARK: - Camera Setup (iOS 17+ Enhanced)
    
    private func setupCamera() async {
        let session = AVCaptureSession()
        
        // iOS 17+: Atomic configuration changes
        session.beginConfiguration()
        defer { session.commitConfiguration() }
        
        // Set optimal preset for QR scanning
        if session.canSetSessionPreset(.hd1920x1080) {
            session.sessionPreset = .hd1920x1080
        } else if session.canSetSessionPreset(.high) {
            session.sessionPreset = .high
        } else {
            session.sessionPreset = .medium
            #if DEBUG
            print("⚠️ Mehr Guard: Using medium quality preset")
            #endif
        }
        
        // Get back camera with fallback options
        var videoDevice: AVCaptureDevice?
        
        // Try wide angle camera first
        videoDevice = AVCaptureDevice.default(
            .builtInWideAngleCamera,
            for: .video,
            position: .back
        )
        
        // Fallback to any available back camera
        if videoDevice == nil {
            videoDevice = AVCaptureDevice.default(for: .video)
        }
        
        guard let device = videoDevice else {
            errorMessage = "No camera available on this device"
            cameraPermissionStatus = .denied
            #if DEBUG
            print("❌ Mehr Guard: No camera device found")
            #endif
            return
        }
        
        do {
            // Configure device for optimal QR scanning
            try configureDevice(device)
            
            let videoInput = try AVCaptureDeviceInput(device: device)
            
            guard session.canAddInput(videoInput) else {
                errorMessage = "Cannot configure camera input. The camera may be in use by another app."
                #if DEBUG
                print("❌ Mehr Guard: Cannot add camera input")
                #endif
                return
            }
            session.addInput(videoInput)
            
            // Setup QR code detection
            let metadataOutput = AVCaptureMetadataOutput()
            guard session.canAddOutput(metadataOutput) else {
                errorMessage = "Cannot configure camera output. Please restart the app."
                #if DEBUG
                print("❌ Mehr Guard: Cannot add metadata output")
                #endif
                return
            }
            session.addOutput(metadataOutput)
            
            // Configure metadata types (after adding to session)
            // Check which types are supported
            let supportedTypes: [AVMetadataObject.ObjectType] = [.qr, .aztec, .dataMatrix, .pdf417]
                .filter { metadataOutput.availableMetadataObjectTypes.contains($0) }
            
            if supportedTypes.isEmpty {
                errorMessage = "QR code scanning is not supported on this device"
                #if DEBUG
                print("❌ Mehr Guard: No supported metadata types")
                #endif
                return
            }
            
            metadataOutput.metadataObjectTypes = supportedTypes
            
            // Setup delegate with Swift 6 concurrency compliance
            let delegate = QRCodeMetadataDelegate { [weak self] code in
                Task { @MainActor in
                    self?.handleScannedCodeFromDelegate(code)
                }
            }
            self.metadataDelegate = delegate
            
            // Use dedicated queue for metadata processing
            let metadataQueue = DispatchQueue(label: "com.raouf.mehrguard.metadata", qos: .userInteractive)
            metadataOutput.setMetadataObjectsDelegate(delegate, queue: metadataQueue)
            
            // Set scanning rect for better performance
            // Focus on center of screen
            metadataOutput.rectOfInterest = CGRect(x: 0.2, y: 0.2, width: 0.6, height: 0.6)
            
            self.captureSession = session
            self.session = session
            self.videoOutput = metadataOutput
            
            #if DEBUG
            print("✅ Mehr Guard: Camera setup complete")
            #endif
            
        } catch let error as AVError {
            // Handle specific AVFoundation errors
            switch error.code {
            case .applicationIsNotAuthorizedToUseDevice:
                errorMessage = "Camera access denied. Please enable camera in Settings."
                cameraPermissionStatus = .denied
            case .deviceIsNotAvailableInBackground:
                errorMessage = "Camera cannot be used in background"
            case .sessionWasInterrupted:
                errorMessage = "Camera session was interrupted. Please try again."
            default:
                errorMessage = "Camera error: \(error.localizedDescription)"
            }
            #if DEBUG
            print("❌ Mehr Guard: AVError - \(error.localizedDescription)")
            #endif
        } catch {
            errorMessage = "Camera setup failed: \(error.localizedDescription)"
            #if DEBUG
            print("❌ Mehr Guard: Unknown error - \(error.localizedDescription)")
            #endif
        }
    }
    
    private func configureDevice(_ device: AVCaptureDevice) throws {
        try device.lockForConfiguration()
        defer { device.unlockForConfiguration() }
        
        // Enable continuous auto-focus for better QR detection
        if device.isFocusModeSupported(.continuousAutoFocus) {
            device.focusMode = .continuousAutoFocus
        }
        
        // Enable auto exposure for varying lighting conditions
        if device.isExposureModeSupported(.continuousAutoExposure) {
            device.exposureMode = .continuousAutoExposure
        }
        
        // iOS 17+: Optimize for low light conditions
        if device.isLowLightBoostSupported {
            device.automaticallyEnablesLowLightBoostWhenAvailable = true
        }
        
        // iOS 18+: Enable automatic frame rate adjustment based on lighting
        // This improves scanning in variable lighting without manual intervention
        if #available(iOS 18.0, *) {
            if device.responds(to: Selector(("isAutoVideoFrameRateEnabled"))) {
                // Note: Property availability varies by device
                // Keeping frame rate settings as fallback
            }
        }
        
        // Set target frame rate for smooth scanning (30fps is optimal for QR)
        // Using min/max duration for broader device compatibility
        let targetFrameRate = CMTime(value: 1, timescale: 30)
        if device.activeFormat.videoSupportedFrameRateRanges.contains(where: { $0.minFrameDuration <= targetFrameRate }) {
            device.activeVideoMinFrameDuration = targetFrameRate
            device.activeVideoMaxFrameDuration = targetFrameRate
        }
        
        #if DEBUG
        print("📷 Camera configured: focus=\(device.focusMode.rawValue), exposure=\(device.exposureMode.rawValue)")
        #endif
    }
    
    // MARK: - Camera Controls
    
    func startCamera() {
        guard cameraPermissionStatus == .authorized else {
            Task { await checkCameraPermission() }
            return
        }
        
        guard let session = captureSession else {
            Task { await checkCameraPermission() }
            return
        }
        
        guard !session.isRunning else { return }
        
        // Swift 6: Use dedicated queue for session work
        sessionQueue.async { [weak self] in
            session.startRunning()
            
            Task { @MainActor [weak self] in
                self?.isScanning = true
            }
        }
    }
    
    func stopCamera() {
        guard let session = captureSession, session.isRunning else { return }
        
        sessionQueue.async { [weak self] in
            session.stopRunning()
            
            Task { @MainActor [weak self] in
                self?.isScanning = false
            }
        }
    }
    
    func toggleScanning() {
        if isScanning {
            stopCamera()
        } else {
            startCamera()
        }
    }
    
    func toggleFlash() {
        guard let device = AVCaptureDevice.default(for: .video),
              device.hasTorch else { return }
        
        do {
            try device.lockForConfiguration()
            defer { device.unlockForConfiguration() }
            
            if isFlashOn {
                device.torchMode = .off
            } else {
                try device.setTorchModeOn(level: 0.8)
            }
            isFlashOn.toggle()
            SettingsManager.shared.triggerHaptic(.light)
        } catch {
            errorMessage = String(format: "Flash toggle failed: %@", error.localizedDescription)
            SettingsManager.shared.triggerHaptic(.warning)
        }
    }
    
    // MARK: - QR Code Handling (Swift 6 Concurrency Safe)
    
    /// Called from the metadata delegate on a background queue
    /// Uses Task to safely jump to MainActor
    private func handleScannedCodeFromDelegate(_ code: String) {
        Task { @MainActor [weak self] in
            self?.handleScannedCode(code)
        }
    }
    
    private func handleScannedCode(_ code: String) {
        // Debounce: Ignore same code within interval
        if code == lastScannedCode,
           let lastTime = lastScanTime,
           Date().timeIntervalSince(lastTime) < scanDebounceInterval {
            return
        }
        
        // Don't scan if already analyzing or showing result
        guard currentResult == nil && !isAnalyzing else { return }

        // Normalize and validate input early to avoid wasted work and UI churn
        guard let sanitized = sanitize(code) else {
            errorMessage = "Invalid or unsupported QR content."
            SettingsManager.shared.triggerHaptic(.warning)
            return
        }
        
        lastScannedCode = sanitized
        lastScanTime = Date()
        scanCount += 1
        
        // Haptic for scan detection
        triggerScanHaptic()
        
        analyzeUrl(sanitized)
    }
    
    // MARK: - Analysis
    
    func analyzeUrl(_ url: String) {
        guard !isAnalyzing else { return }
        isAnalyzing = true
        errorMessage = nil
        
        Task { [weak self] in
            guard let self else { return }
            
            // Small delay for UI feedback
            try? await Task.sleep(for: .milliseconds(400))
            
            // Use UnifiedAnalysisService for consistent analysis
            // This handles both KMP HeuristicsEngine and Swift fallback internally
            let result = await MainActor.run {
                UnifiedAnalysisService.shared.analyze(url: url)
            }
            
            await MainActor.run {
                withAnimation(.spring(response: 0.4, dampingFraction: 0.75)) {
                    self.currentResult = result
                    self.isAnalyzing = false
                }
                
                // Save to history (respects saveHistory setting)
                HistoryStore.shared.addScan(result)
                
                self.triggerVerdictHaptic(for: result.verdict)
                
                #if DEBUG
                print("🔍 [Scanner] Analysis complete via \(UnifiedAnalysisService.shared.lastEngineUsed)")
                #endif
            }
        }
    }
    
    func analyzeImage(_ image: UIImage) {
        guard !isAnalyzing else { return }
        isAnalyzing = true
        errorMessage = nil

        Task { [weak self] in
            guard let self else { return }

            do {
                let code = try await imageScanner.scanQRCode(from: image)

                await MainActor.run { [weak self] in
                    guard let self else { return }

                    if let code {
                        // Route through the same scan handling to keep counters/haptics consistent
                        self.isAnalyzing = false
                        self.handleScannedCode(code)
                    } else {
                        self.isAnalyzing = false
                        self.errorMessage = "No QR code found in the selected image."
                        SettingsManager.shared.triggerHaptic(.warning)
                    }
                }
            } catch {
                await MainActor.run { [weak self] in
                    guard let self else { return }
                    self.isAnalyzing = false
                    self.errorMessage = String(
                        format: "Image scan failed: %@",
                        error.localizedDescription
                    )
                    SettingsManager.shared.triggerHaptic(.error)
                }
            }
        }
    }

    /// Trim and validate QR payloads before analysis to avoid crashes and wasted work.
    /// Returns a sanitized string or nil if invalid.
    private func sanitize(_ raw: String) -> String? {
        let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return nil }

        // Prevent oversized payloads from exhausting resources
        guard trimmed.count <= maxUrlLength else { return nil }

        // Reject control/illegal characters to avoid parser oddities
        if trimmed.rangeOfCharacter(from: .controlCharacters) != nil {
            return nil
        }

        // Basic URL validity check; many malicious QR codes omit scheme, so accept host-only with implied https
        if let url = URL(string: trimmed), let scheme = url.scheme?.lowercased() {
            guard allowedSchemes.contains(scheme) else { return nil }
            return trimmed
        }

        // Try to prepend https if missing scheme and the host looks valid
        if let inferred = URL(string: "https://\(trimmed)"), inferred.host != nil {
            return inferred.absoluteString
        }

        return nil
    }
    
    func dismissResult() {
        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
            currentResult = nil
            lastScannedCode = nil
        }
    }
    
    // MARK: - Haptic Feedback (iOS 17+ Enhanced)
    
    private func triggerScanHaptic() {
        SettingsManager.shared.triggerHaptic(.medium)
        SettingsManager.shared.playSound(.scan)
    }
    
    private func triggerVerdictHaptic(for verdict: VerdictMock) {
        switch verdict {
        case .safe:
            SettingsManager.shared.triggerHaptic(.success)
            SettingsManager.shared.playSound(.success)
        case .suspicious:
            SettingsManager.shared.triggerHaptic(.warning)
            SettingsManager.shared.playSound(.warning)
        case .malicious:
            SettingsManager.shared.triggerHaptic(.error)
            SettingsManager.shared.playSound(.error)
            // Double haptic for malicious - using Task for Swift 6 compliance
            Task { @MainActor in
                try? await Task.sleep(for: .milliseconds(150))
                SettingsManager.shared.triggerHaptic(.heavy)
            }
        case .unknown:
            SettingsManager.shared.triggerHaptic(.light)
        }
    }
    
    // NOTE: createMockResult() was removed - all analysis now flows through
    // UnifiedAnalysisService which uses KMP HeuristicsEngine or Swift fallback
}

// MARK: - QR Code Metadata Delegate (Swift 6 Compliant)

/// Separate delegate class for AVCaptureMetadataOutputObjectsDelegate
/// Uses `nonisolated` for Swift 6 strict concurrency compliance.
///
/// Thread Safety Documentation (@unchecked Sendable justification):
/// - `onCodeScanned` is a `@Sendable` closure, making it safe to call from any thread
/// - This class stores no mutable state after initialization
/// - The delegate method is called by AVFoundation on a dedicated metadata queue
/// - All captured state inside the closure is either `Sendable` or managed via Task
final class QRCodeMetadataDelegate: NSObject, AVCaptureMetadataOutputObjectsDelegate, @unchecked Sendable {
    private let onCodeScanned: @Sendable (String) -> Void
    
    init(onCodeScanned: @escaping @Sendable (String) -> Void) {
        self.onCodeScanned = onCodeScanned
        super.init()
    }
    
    nonisolated func metadataOutput(
        _ output: AVCaptureMetadataOutput,
        didOutput metadataObjects: [AVMetadataObject],
        from connection: AVCaptureConnection
    ) {
        // Process on current queue, then dispatch result
        guard let metadataObject = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
              let stringValue = metadataObject.stringValue,
              !stringValue.isEmpty else {
            return
        }
        
        onCodeScanned(stringValue)
    }
}

// MARK: - Camera Permission Status

enum CameraPermissionStatus: String, Sendable {
    case unknown
    case notDetermined
    case authorized
    case denied
    
    var message: String {
        switch self {
        case .unknown:
            return "Checking camera access..."
        case .notDetermined:
            return "Camera access needed"
        case .authorized:
            return "Camera ready"
        case .denied:
            return "Camera access denied"
        }
    }
}

// NOTE: VerdictMock and RiskAssessmentMock are now defined in Models/MockTypes.swift

#endif
