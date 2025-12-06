// UI/Scanner/ScannerViewModel.swift
// QR-SHIELD Scanner ViewModel - iOS 26 / Swift 6.1 Optimized
//
// UPDATED: December 2025 - iOS 26.2 / Xcode 26
// - Full @Observable macro support
// - Swift 6 strict concurrency compliance
// - Cinematic Mode API ready
// - Enhanced camera configuration

import Foundation
import AVFoundation
import SwiftUI
import Observation
// import common // Uncomment when KMP framework is linked

// MARK: - iOS 26 Observable ViewModel

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
    
    // Debounce configuration
    private let scanDebounceInterval: TimeInterval = 2.0
    
    // KMP Dependencies - Uncomment when framework is linked
    // private let scanner: QrScanner
    // private let engine: PhishingEngine
    
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
    
    // MARK: - Camera Setup (iOS 26 Enhanced)
    
    private func setupCamera() async {
        let session = AVCaptureSession()
        
        // iOS 26: Atomic configuration changes
        session.beginConfiguration()
        defer { session.commitConfiguration() }
        
        // Set optimal preset for QR scanning
        if session.canSetSessionPreset(.hd1920x1080) {
            session.sessionPreset = .hd1920x1080
        } else if session.canSetSessionPreset(.high) {
            session.sessionPreset = .high
        }
        
        // Get back camera
        guard let videoDevice = AVCaptureDevice.default(
            .builtInWideAngleCamera,
            for: .video,
            position: .back
        ) else {
            errorMessage = "No camera available"
            return
        }
        
        do {
            // Configure device for optimal QR scanning
            try configureDevice(videoDevice)
            
            let videoInput = try AVCaptureDeviceInput(device: videoDevice)
            
            guard session.canAddInput(videoInput) else {
                errorMessage = "Cannot add camera input"
                return
            }
            session.addInput(videoInput)
            
            // Setup QR code detection
            let metadataOutput = AVCaptureMetadataOutput()
            guard session.canAddOutput(metadataOutput) else {
                errorMessage = "Cannot add metadata output"
                return
            }
            session.addOutput(metadataOutput)
            
            // Configure metadata types (after adding to session)
            metadataOutput.metadataObjectTypes = [.qr, .aztec, .dataMatrix, .pdf417]
            
            // Setup delegate with Swift 6 concurrency compliance
            let delegate = QRCodeMetadataDelegate { [weak self] code in
                self?.handleScannedCodeFromDelegate(code)
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
            
        } catch {
            errorMessage = "Camera setup failed: \(error.localizedDescription)"
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
        
        // Optimize for low light (iOS 26)
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
        
        // Swift 6: Use Task.detached for background work
        Task.detached(priority: .userInitiated) { [session] in
            session.startRunning()
            
            await MainActor.run { [weak self] in
                self?.isScanning = true
            }
        }
    }
    
    func stopCamera() {
        guard let session = captureSession, session.isRunning else { return }
        
        Task.detached(priority: .userInitiated) { [session] in
            session.stopRunning()
            
            await MainActor.run { [weak self] in
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
            
            // Simulate analysis delay
            // In production: let assessment = try await engine.analyze(url: url)
            try? await Task.sleep(for: .milliseconds(500))
            
            let result = createMockResult(for: url)
            
            await MainActor.run {
                withAnimation(.spring(response: 0.4, dampingFraction: 0.75)) {
                    self.currentResult = result
                    self.isAnalyzing = false
                }
                
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
    
    // MARK: - Haptic Feedback (iOS 26 Enhanced)
    
    private func triggerScanHaptic() {
        let impact = UIImpactFeedbackGenerator(style: .medium)
        impact.prepare()
        impact.impactOccurred()
    }
    
    private func triggerVerdictHaptic(for verdict: VerdictMock) {
        let notification = UINotificationFeedbackGenerator()
        notification.prepare()
        
        switch verdict {
        case .safe:
            notification.notificationOccurred(.success)
        case .suspicious:
            notification.notificationOccurred(.warning)
        case .malicious:
            notification.notificationOccurred(.error)
            // Double haptic for malicious
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.15) {
                let impact = UIImpactFeedbackGenerator(style: .heavy)
                impact.impactOccurred(intensity: 1.0)
            }
        case .unknown:
            let impact = UIImpactFeedbackGenerator(style: .light)
            impact.impactOccurred()
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

// MARK: - Mock Types (Replace with KMP imports)

/// Mock verdict type - Replace with common.Verdict
enum VerdictMock: String, CaseIterable, Sendable {
    case safe = "SAFE"
    case suspicious = "SUSPICIOUS"
    case malicious = "MALICIOUS"
    case unknown = "UNKNOWN"
    
    var icon: String {
        switch self {
        case .safe: return "checkmark.shield.fill"
        case .suspicious: return "exclamationmark.shield.fill"
        case .malicious: return "xmark.shield.fill"
        case .unknown: return "questionmark.circle"
        }
    }
}

/// Mock risk assessment - Replace with common.RiskAssessment
struct RiskAssessmentMock: Identifiable, Sendable {
    let id = UUID()
    let score: Int
    let verdict: VerdictMock
    let flags: [String]
    let confidence: Double
    let url: String
    let scannedAt: Date
    
    var formattedDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        return formatter.string(from: scannedAt)
    }
}
