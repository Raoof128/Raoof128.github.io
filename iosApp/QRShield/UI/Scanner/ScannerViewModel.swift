//
// Copyright 2024 QR-SHIELD Contributors
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
// QR-SHIELD Scanner ViewModel - iOS 17+ / Swift 6 Optimized
//
// UPDATED: December 2025 - iOS 17+ Compatible
// - Full @Observable macro support
// - Swift 6 strict concurrency compliance
// - Enhanced camera configuration
// - Comprehensive error handling

import Foundation
import AVFoundation
import SwiftUI
import Observation
#if canImport(common)
import common
#endif

// MARK: - iOS 17+ Observable ViewModel

/// The iOS-native ViewModel using @Observable macro
/// Provides precise property-level observation for optimal performance
@Observable
@MainActor
final class ScannerViewModel {
    
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
    
    // Swift 6: Dedicated queue for session operations
    private let sessionQueue = DispatchQueue(label: "com.qrshield.session", qos: .userInteractive)
    
    // Debounce configuration
    private let scanDebounceInterval: TimeInterval = 2.0
    
    // KMP Dependencies
    #if canImport(common)
    private let engine: PhishingEngine = PhishingEngine()
    #endif
    
    // MARK: - Initialization
    
    init() {
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
        case .notDetermined:
            cameraPermissionStatus = .notDetermined
            let granted = await AVCaptureDevice.requestAccess(for: .video)
            cameraPermissionStatus = granted ? .authorized : .denied
            if granted {
                await setupCamera()
            }
        case .denied, .restricted:
            cameraPermissionStatus = .denied
        @unknown default:
            cameraPermissionStatus = .unknown
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
            print("⚠️ QR-SHIELD: Using medium quality preset")
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
            print("❌ QR-SHIELD: No camera device found")
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
                print("❌ QR-SHIELD: Cannot add camera input")
                #endif
                return
            }
            session.addInput(videoInput)
            
            // Setup QR code detection
            let metadataOutput = AVCaptureMetadataOutput()
            guard session.canAddOutput(metadataOutput) else {
                errorMessage = "Cannot configure camera output. Please restart the app."
                #if DEBUG
                print("❌ QR-SHIELD: Cannot add metadata output")
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
                print("❌ QR-SHIELD: No supported metadata types")
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
            let metadataQueue = DispatchQueue(label: "com.qrshield.metadata", qos: .userInteractive)
            metadataOutput.setMetadataObjectsDelegate(delegate, queue: metadataQueue)
            
            // Set scanning rect for better performance
            // Focus on center of screen
            metadataOutput.rectOfInterest = CGRect(x: 0.2, y: 0.2, width: 0.6, height: 0.6)
            
            self.captureSession = session
            self.session = session
            self.videoOutput = metadataOutput
            
            #if DEBUG
            print("✅ QR-SHIELD: Camera setup complete")
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
            print("❌ QR-SHIELD: AVError - \(error.localizedDescription)")
            #endif
        } catch {
            errorMessage = "Camera setup failed: \(error.localizedDescription)"
            #if DEBUG
            print("❌ QR-SHIELD: Unknown error - \(error.localizedDescription)")
            #endif
        }
    }
    
    private func configureDevice(_ device: AVCaptureDevice) throws {
        try device.lockForConfiguration()
        defer { device.unlockForConfiguration() }
        
        // Enable continuous auto-focus
        if device.isFocusModeSupported(.continuousAutoFocus) {
            device.focusMode = .continuousAutoFocus
        }
        
        // Enable auto exposure
        if device.isExposureModeSupported(.continuousAutoExposure) {
            device.exposureMode = .continuousAutoExposure
        }
        
        // Optimize for low light (iOS 17+)
        if device.isLowLightBoostSupported {
            device.automaticallyEnablesLowLightBoostWhenAvailable = true
        }
        
        // Set frame rate for smooth scanning
        device.activeVideoMinFrameDuration = CMTime(value: 1, timescale: 30)
        device.activeVideoMaxFrameDuration = CMTime(value: 1, timescale: 30)
    }
    
    // MARK: - Camera Controls
    
    func startCamera() {
        guard cameraPermissionStatus == .authorized else {
            Task { await checkCameraPermission() }
            return
        }
        
        guard let session = captureSession, !session.isRunning else { return }
        
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
        } catch {
            errorMessage = "Flash toggle failed: \(error.localizedDescription)"
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
        
        lastScannedCode = code
        lastScanTime = Date()
        scanCount += 1
        
        // Haptic for scan detection
        triggerScanHaptic()
        
        analyzeUrl(code)
    }
    
    // MARK: - Analysis
    
    func analyzeUrl(_ url: String) {
        guard !isAnalyzing else { return }
        isAnalyzing = true
        
        Task { [weak self] in
            guard let self else { return }
            
            // Use actual KMP PhishingEngine when available
            #if canImport(common)
            let assessment = engine.analyze(url: url)
            let result = RiskAssessmentMock(
                score: Int(assessment.score),
                verdict: VerdictMock.from(assessment.verdict),
                flags: assessment.flags as? [String] ?? [],
                confidence: Double(assessment.confidence),
                url: url,
                scannedAt: Date()
            )
            #else
            // Fallback to mock for development without KMP framework
            try? await Task.sleep(for: .milliseconds(500))
            let result = createMockResult(for: url)
            #endif
            
            await MainActor.run {
                withAnimation(.spring(response: 0.4, dampingFraction: 0.75)) {
                    self.currentResult = result
                    self.isAnalyzing = false
                }
                
                // Save to history (respects saveHistory setting)
                HistoryStore.shared.addScan(result)
                
                self.triggerVerdictHaptic(for: result.verdict)
            }
        }
    }
    
    func analyzeImage(_ image: UIImage) {
        guard !isAnalyzing else { return }
        isAnalyzing = true
        
        Task { [weak self] in
            // In production: Use Vision framework for QR detection
            try? await Task.sleep(for: .seconds(1))
            
            await MainActor.run { [weak self] in
                // Simulate found QR code
                self?.analyzeUrl("https://example.com/from-image")
            }
        }
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
            // Double haptic for malicious
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                SettingsManager.shared.triggerHaptic(.heavy)
            }
        case .unknown:
            SettingsManager.shared.triggerHaptic(.light)
        }
    }
    
    // MARK: - Mock Data (Replace with KMP)
    
    private func createMockResult(for url: String) -> RiskAssessmentMock {
        let lowercaseUrl = url.lowercased()
        
        let score: Int
        let verdict: VerdictMock
        var flags: [String] = []
        
        if lowercaseUrl.contains("malware") || lowercaseUrl.contains("phish") ||
           lowercaseUrl.contains("hack") || lowercaseUrl.contains("scam") {
            score = 85 + Int.random(in: 0...10)
            verdict = .malicious
            flags = [
                "Known phishing domain",
                "Suspicious path patterns",
                "Domain impersonation detected"
            ]
        } else if lowercaseUrl.contains("suspicious") ||
                  !lowercaseUrl.hasPrefix("https") ||
                  lowercaseUrl.contains("login") {
            score = 45 + Int.random(in: 0...20)
            verdict = .suspicious
            flags = ["HTTP instead of HTTPS", "Login page detected"]
        } else {
            score = 5 + Int.random(in: 0...15)
            verdict = .safe
        }
        
        return RiskAssessmentMock(
            score: score,
            verdict: verdict,
            flags: flags,
            confidence: Double.random(in: 0.85...0.98),
            url: url,
            scannedAt: Date()
        )
    }
}

// MARK: - QR Code Metadata Delegate (Swift 6 Compliant)

/// Separate delegate class for AVCaptureMetadataOutputObjectsDelegate
/// Uses `nonisolated` for Swift 6 strict concurrency compliance
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
