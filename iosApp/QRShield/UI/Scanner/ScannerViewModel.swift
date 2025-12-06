// UI/Scanner/ScannerViewModel.swift
// QR-SHIELD Scanner ViewModel - iOS 18+ / Swift 6 Optimized
//
// UPDATED: December 2024 - Using @Observable macro (iOS 17+)
// Bridges SwiftUI to the Kotlin Multiplatform IosQrScanner.
// Uses modern Swift Concurrency patterns for thread safety.

import Foundation
import AVFoundation
import SwiftUI
import Observation
// import common // Uncomment when KMP framework is linked

// MARK: - Modern Observable ViewModel (iOS 17+)

/// The iOS-native ViewModel using @Observable macro (modern replacement for ObservableObject)
/// This provides better performance with precise property-level observation
@Observable
@MainActor
final class ScannerViewModel {
    
    // MARK: - Observable State
    // No @Published needed with @Observable macro - all properties are automatically tracked
    
    var session: AVCaptureSession?
    var currentResult: RiskAssessmentMock?
    var isScanning = false
    var isAnalyzing = false
    var isFlashOn = false
    var errorMessage: String?
    var cameraPermissionStatus: CameraPermissionStatus = .unknown
    
    // MARK: - Private Properties
    
    private var captureSession: AVCaptureSession?
    private var videoOutput: AVCaptureMetadataOutput?
    private var metadataDelegate: QRCodeMetadataDelegate?
    
    // KMP Dependencies - Uncomment when framework is linked
    // private let scanner: QrScanner
    // private let engine: PhishingEngine
    
    // MARK: - Initialization
    
    init() {
        // KMP initialization - Uncomment when framework is linked
        // self.scanner = QrScannerFactory().create()
        // self.engine = PhishingEngine()
        
        Task {
            await checkCameraPermission()
        }
    }
    
    // MARK: - Camera Permission (iOS 18 Best Practice)
    
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
    
    // MARK: - Camera Setup (iOS 18 Enhanced Configuration)
    
    private func setupCamera() async {
        let session = AVCaptureSession()
        
        // iOS 18: Use beginConfiguration/commitConfiguration for atomic changes
        session.beginConfiguration()
        
        // Set high quality preset
        if session.canSetSessionPreset(.high) {
            session.sessionPreset = .high
        }
        
        // Get back camera with iOS 18 device discovery
        guard let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else {
            errorMessage = "No camera available"
            session.commitConfiguration()
            return
        }
        
        do {
            // Configure device for optimal QR scanning
            try videoDevice.lockForConfiguration()
            
            // iOS 18: Enable auto video frame rate for better low-light performance
            if videoDevice.isAutoVideoFrameRateEnabled {
                videoDevice.activeVideoMinFrameDuration = CMTime(value: 1, timescale: 30)
            }
            
            // Enable continuous auto-focus for QR codes
            if videoDevice.isFocusModeSupported(.continuousAutoFocus) {
                videoDevice.focusMode = .continuousAutoFocus
            }
            
            videoDevice.unlockForConfiguration()
            
            let videoInput = try AVCaptureDeviceInput(device: videoDevice)
            
            if session.canAddInput(videoInput) {
                session.addInput(videoInput)
            }
            
            // QR Code detection output with delegate
            let metadataOutput = AVCaptureMetadataOutput()
            if session.canAddOutput(metadataOutput) {
                session.addOutput(metadataOutput)
                
                // Set metadata types after adding output
                metadataOutput.metadataObjectTypes = [.qr, .aztec, .dataMatrix]
                
                // Create delegate for metadata handling
                let delegate = QRCodeMetadataDelegate { [weak self] code in
                    Task { @MainActor in
                        self?.handleScannedCode(code)
                    }
                }
                self.metadataDelegate = delegate
                metadataOutput.setMetadataObjectsDelegate(delegate, queue: .main)
            }
            
            session.commitConfiguration()
            
            self.captureSession = session
            self.session = session
            self.videoOutput = metadataOutput
            
        } catch {
            session.commitConfiguration()
            errorMessage = "Camera setup failed: \(error.localizedDescription)"
        }
    }
    
    // MARK: - Camera Controls
    
    func startCamera() {
        guard cameraPermissionStatus == .authorized else {
            Task { await checkCameraPermission() }
            return
        }
        
        startSession()
    }
    
    private func startSession() {
        guard let session = captureSession, !session.isRunning else { return }
        
        // iOS 18: Run on background queue to avoid blocking main thread
        Task.detached(priority: .userInitiated) {
            session.startRunning()
            
            await MainActor.run {
                self.isScanning = true
            }
        }
    }
    
    func stopCamera() {
        guard let session = captureSession, session.isRunning else { return }
        
        Task.detached(priority: .userInitiated) {
            session.stopRunning()
            
            await MainActor.run {
                self.isScanning = false
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
            device.torchMode = isFlashOn ? .off : .on
            isFlashOn.toggle()
            device.unlockForConfiguration()
        } catch {
            errorMessage = "Flash toggle failed: \(error.localizedDescription)"
        }
    }
    
    // MARK: - QR Code Handling
    
    private func handleScannedCode(_ code: String) {
        // Debounce rapid scans
        guard currentResult == nil && !isAnalyzing else { return }
        
        // Play haptic for scan detection
        let impact = UIImpactFeedbackGenerator(style: .medium)
        impact.impactOccurred()
        
        analyzeUrl(code)
    }
    
    // MARK: - Analysis
    
    func analyzeUrl(_ url: String) {
        isAnalyzing = true
        
        Task {
            // Simulate analysis delay
            // In production: let assessment = try await engine.analyze(url: url)
            try? await Task.sleep(for: .milliseconds(500))
            
            // Mock result for demo - Replace with KMP call
            let mockResult = createMockResult(for: url)
            
            withAnimation(.spring(response: 0.4, dampingFraction: 0.7)) {
                self.currentResult = mockResult
                self.isAnalyzing = false
            }
            
            // Haptic feedback based on verdict
            triggerHaptic(for: mockResult.verdict)
        }
    }
    
    func analyzeImage(_ image: UIImage) {
        isAnalyzing = true
        
        Task {
            // In production: Use Vision framework for QR detection
            try? await Task.sleep(for: .seconds(1))
            analyzeUrl("https://example.com/demo")
        }
    }
    
    func dismissResult() {
        withAnimation(.spring(response: 0.3)) {
            currentResult = nil
        }
    }
    
    // MARK: - Haptic Feedback (iOS 18 Enhanced)
    
    private func triggerHaptic(for verdict: VerdictMock) {
        // iOS 18: Prepare haptics ahead of time for better responsiveness
        let notificationGenerator = UINotificationFeedbackGenerator()
        notificationGenerator.prepare()
        
        switch verdict {
        case .safe:
            notificationGenerator.notificationOccurred(.success)
        case .suspicious:
            notificationGenerator.notificationOccurred(.warning)
        case .malicious:
            notificationGenerator.notificationOccurred(.error)
            // Add extra impact for malicious
            let impact = UIImpactFeedbackGenerator(style: .heavy)
            impact.impactOccurred(intensity: 1.0)
        case .unknown:
            let impact = UIImpactFeedbackGenerator(style: .light)
            impact.impactOccurred()
        }
    }
    
    // MARK: - Mock Data (Replace with KMP)
    
    private func createMockResult(for url: String) -> RiskAssessmentMock {
        let score: Int
        let verdict: VerdictMock
        var flags: [String] = []
        
        if url.contains("malware") || url.contains("phish") {
            score = 85
            verdict = .malicious
            flags = ["Known phishing domain", "Suspicious path patterns"]
        } else if url.contains("suspicious") || !url.contains("https") {
            score = 55
            verdict = .suspicious
            flags = ["HTTP instead of HTTPS"]
        } else {
            score = 15
            verdict = .safe
        }
        
        return RiskAssessmentMock(
            score: score,
            verdict: verdict,
            flags: flags,
            confidence: 0.87,
            url: url
        )
    }
}

// MARK: - QR Code Metadata Delegate

/// Separate delegate class for AVCaptureMetadataOutputObjectsDelegate
/// Required for Swift 6 concurrency compatibility
final class QRCodeMetadataDelegate: NSObject, AVCaptureMetadataOutputObjectsDelegate {
    private let onCodeScanned: (String) -> Void
    
    init(onCodeScanned: @escaping (String) -> Void) {
        self.onCodeScanned = onCodeScanned
        super.init()
    }
    
    nonisolated func metadataOutput(
        _ output: AVCaptureMetadataOutput,
        didOutput metadataObjects: [AVMetadataObject],
        from connection: AVCaptureConnection
    ) {
        guard let metadataObject = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
              let stringValue = metadataObject.stringValue else {
            return
        }
        
        onCodeScanned(stringValue)
    }
}

// MARK: - Camera Permission Status

enum CameraPermissionStatus {
    case unknown
    case notDetermined
    case authorized
    case denied
}

// MARK: - Mock Types (Replace with KMP imports)

/// Mock verdict type - Replace with common.Verdict
enum VerdictMock: String, CaseIterable {
    case safe = "SAFE"
    case suspicious = "SUSPICIOUS"
    case malicious = "MALICIOUS"
    case unknown = "UNKNOWN"
}

/// Mock risk assessment - Replace with common.RiskAssessment
struct RiskAssessmentMock: Identifiable {
    let id = UUID()
    let score: Int
    let verdict: VerdictMock
    let flags: [String]
    let confidence: Double
    let url: String
}
