// UI/Scanner/ScannerViewModel.swift
// QR-SHIELD Scanner ViewModel
//
// Bridges SwiftUI to the Kotlin Multiplatform IosQrScanner.
// Manages camera state, scanning flow, and phishing analysis.

import Foundation
import AVFoundation
import Combine
// import common // Uncomment when KMP framework is linked

/// The iOS-native ViewModel that bridges to Kotlin Multiplatform
@MainActor
class ScannerViewModel: ObservableObject {
    
    // MARK: - Published State
    
    @Published var session: AVCaptureSession?
    @Published var currentResult: RiskAssessmentMock? // Replace with common.RiskAssessment
    @Published var isScanning = false
    @Published var isAnalyzing = false
    @Published var isFlashOn = false
    @Published var errorMessage: String?
    
    // MARK: - Private Properties
    
    private var captureSession: AVCaptureSession?
    private var videoOutput: AVCaptureMetadataOutput?
    
    // KMP Dependencies - Uncomment when framework is linked
    // private let scanner: QrScanner
    // private let engine: PhishingEngine
    
    // MARK: - Initialization
    
    init() {
        // KMP initialization - Uncomment when framework is linked
        // self.scanner = QrScannerFactory().create()
        // self.engine = PhishingEngine()
        
        setupCamera()
    }
    
    // MARK: - Camera Setup
    
    private func setupCamera() {
        let session = AVCaptureSession()
        session.sessionPreset = .high
        
        // Get back camera
        guard let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else {
            errorMessage = "No camera available"
            return
        }
        
        do {
            let videoInput = try AVCaptureDeviceInput(device: videoDevice)
            
            if session.canAddInput(videoInput) {
                session.addInput(videoInput)
            }
            
            // QR Code detection output
            let metadataOutput = AVCaptureMetadataOutput()
            if session.canAddOutput(metadataOutput) {
                session.addOutput(metadataOutput)
                metadataOutput.metadataObjectTypes = [.qr]
                // Note: Delegate will be set when using KMP scanner
            }
            
            self.captureSession = session
            self.session = session
            
        } catch {
            errorMessage = "Camera setup failed: \(error.localizedDescription)"
        }
    }
    
    // MARK: - Camera Controls
    
    func startCamera() {
        guard let session = captureSession else { return }
        
        // Check permission
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            startSession()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                if granted {
                    Task { @MainActor in
                        self?.startSession()
                    }
                }
            }
        default:
            errorMessage = "Camera access denied"
        }
    }
    
    private func startSession() {
        guard let session = captureSession, !session.isRunning else { return }
        
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            session.startRunning()
            
            Task { @MainActor in
                self?.isScanning = true
            }
        }
    }
    
    func stopCamera() {
        guard let session = captureSession, session.isRunning else { return }
        
        DispatchQueue.global(qos: .userInitiated).async { [weak self] in
            session.stopRunning()
            
            Task { @MainActor in
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
            device.torchMode = isFlashOn ? .off : .on
            isFlashOn.toggle()
            device.unlockForConfiguration()
        } catch {
            print("Flash toggle failed: \(error)")
        }
    }
    
    // MARK: - Analysis
    
    func analyzeUrl(_ url: String) {
        isAnalyzing = true
        
        // Simulate analysis delay
        // In production, use: let assessment = engine.analyze(url: url)
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            guard let self = self else { return }
            
            // Mock result for demo - Replace with KMP call
            let mockResult = self.createMockResult(for: url)
            
            withAnimation(.spring(response: 0.4, dampingFraction: 0.7)) {
                self.currentResult = mockResult
                self.isAnalyzing = false
            }
            
            // Haptic feedback based on verdict
            self.triggerHaptic(for: mockResult.verdict)
        }
    }
    
    func analyzeImage(_ image: UIImage) {
        // In production, use KMP scanner.scanFromImage()
        isAnalyzing = true
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { [weak self] in
            // Mock: pretend we found a QR code
            self?.analyzeUrl("https://example.com/demo")
        }
    }
    
    func dismissResult() {
        withAnimation {
            currentResult = nil
        }
    }
    
    // MARK: - Haptic Feedback
    
    private func triggerHaptic(for verdict: VerdictMock) {
        let generator = UINotificationFeedbackGenerator()
        
        switch verdict {
        case .safe:
            generator.notificationOccurred(.success)
        case .suspicious:
            generator.notificationOccurred(.warning)
        case .malicious:
            generator.notificationOccurred(.error)
        case .unknown:
            let impact = UIImpactFeedbackGenerator(style: .light)
            impact.impactOccurred()
        }
    }
    
    // MARK: - Mock Data (Replace with KMP)
    
    private func createMockResult(for url: String) -> RiskAssessmentMock {
        // Simulate different verdicts based on URL
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

// MARK: - Mock Types (Replace with KMP imports)

/// Mock verdict type - Replace with common.Verdict
enum VerdictMock: String {
    case safe = "SAFE"
    case suspicious = "SUSPICIOUS"
    case malicious = "MALICIOUS"
    case unknown = "UNKNOWN"
}

/// Mock risk assessment - Replace with common.RiskAssessment
struct RiskAssessmentMock {
    let score: Int
    let verdict: VerdictMock
    let flags: [String]
    let confidence: Double
    let url: String
}
