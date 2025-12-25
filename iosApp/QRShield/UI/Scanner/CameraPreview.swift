//
// Copyright 2025-2026 QR-SHIELD Contributors
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

// UI/Scanner/CameraPreview.swift
// QR-SHIELD Native Camera Layer - iOS 17+ Edition
//
// UPDATED: December 2025 - iOS 17+
// - videoRotationAngle for modern orientation handling
// - Improved lifecycle management
// - Swift 6 concurrency compliance

import SwiftUI
import AVFoundation
#if os(iOS)

// MARK: - Camera Preview (SwiftUI Wrapper)

/// UIViewRepresentable wrapper for AVCaptureVideoPreviewLayer
/// Bridges AVFoundation camera to SwiftUI with proper lifecycle
struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession?
    
    func makeUIView(context: Context) -> CameraPreviewView {
        let view = CameraPreviewView()
        view.backgroundColor = .black
        
        // Set session immediately if available
        if let session {
            view.session = session
        }
        
        return view
    }
    
    func updateUIView(_ uiView: CameraPreviewView, context: Context) {
        // Only update if session changed
        if uiView.session !== session {
            uiView.session = session
        }
    }
    
    static func dismantleUIView(_ uiView: CameraPreviewView, coordinator: ()) {
        // Clean up resources
        uiView.cleanup()
    }
}

// MARK: - Camera Preview View (UIKit)

/// The underlying UIView that holds the AVCaptureVideoPreviewLayer
/// Note: Do NOT use @MainActor here - UIKit views in UIViewRepresentable
/// must allow AVFoundation to update the preview layer from its capture queues
final class CameraPreviewView: UIView {
    
    // MARK: - Properties
    
    private var previewLayer: AVCaptureVideoPreviewLayer?
    private var orientationObserver: NSObjectProtocol?
    
    var session: AVCaptureSession? {
        didSet {
            guard session !== oldValue else { return }
            setupPreviewLayer()
        }
    }
    
    // MARK: - Lifecycle
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupView()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupView()
    }
    
    private func setupView() {
        backgroundColor = .black
        clipsToBounds = true
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        // Update layer frame
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer?.frame = bounds
        CATransaction.commit()
        
        // Update rotation
        updateVideoRotation()
    }
    
    override func didMoveToWindow() {
        super.didMoveToWindow()
        
        if window != nil {
            startObservingOrientation()
        } else {
            stopObservingOrientation()
        }
    }
    
    // MARK: - Preview Layer Setup
    
    private func setupPreviewLayer() {
        // Remove existing layer
        previewLayer?.removeFromSuperlayer()
        previewLayer = nil
        
        guard let session else { return }
        
        let layer = AVCaptureVideoPreviewLayer(session: session)
        layer.frame = bounds
        layer.videoGravity = .resizeAspectFill
        layer.contentsGravity = .resizeAspectFill
        
        // Set initial rotation
        updateVideoRotation(for: layer)
        
        // Insert at bottom so overlays appear on top
        self.layer.insertSublayer(layer, at: 0)
        self.previewLayer = layer
    }
    
    // MARK: - Orientation Handling (iOS 17+)
    
    private func startObservingOrientation() {
        orientationObserver = NotificationCenter.default.addObserver(
            forName: UIDevice.orientationDidChangeNotification,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            // UIView methods are MainActor isolated, so call on main
            Task { @MainActor in
                self?.updateVideoRotation()
            }
        }
    }
    
    private func stopObservingOrientation() {
        if let observer = orientationObserver {
            NotificationCenter.default.removeObserver(observer)
            orientationObserver = nil
        }
    }
    
    private func updateVideoRotation() {
        guard let previewLayer else { return }
        updateVideoRotation(for: previewLayer)
    }
    
    private func updateVideoRotation(for layer: AVCaptureVideoPreviewLayer) {
        guard let connection = layer.connection else { return }
        
        // iOS 17+: Use videoRotationAngle instead of deprecated videoOrientation
        let angle = currentRotationAngle()
        
        if connection.isVideoRotationAngleSupported(angle) {
            // Apply rotation without animation
            CATransaction.begin()
            CATransaction.setDisableActions(true)
            connection.videoRotationAngle = angle
            CATransaction.commit()
        }
    }
    
    /// Get rotation angle based on current device orientation
    private func currentRotationAngle() -> CGFloat {
        // Get interface orientation from window scene
        guard let windowScene = window?.windowScene else {
            return 90 // Default portrait
        }
        
        switch windowScene.interfaceOrientation {
        case .portrait:
            return 90
        case .portraitUpsideDown:
            return 270
        case .landscapeLeft:
            return 180
        case .landscapeRight:
            return 0
        default:
            return 90
        }
    }
    
    // MARK: - Cleanup
    
    func cleanup() {
        stopObservingOrientation()
        previewLayer?.removeFromSuperlayer()
        previewLayer = nil
        session = nil
    }
    
    deinit {
        // NotificationCenter observer is removed automatically when the object deallocates
        // previewLayer cleanup is handled by ARC
    }
}

// MARK: - Scanning Overlay View

/// Animated overlay for the scanning area
struct ScanningOverlay: View {
    let isScanning: Bool
    let verdict: VerdictMock?
    
    @State private var animationPhase: CGFloat = 0
    
    var body: some View {
        GeometryReader { geometry in
            ZStack {
                // Scan line animation
                if isScanning && verdict == nil {
                    scanLine(in: geometry.size)
                }
                
                // Corner markers
                cornerMarkers
                    .stroke(markerColor, lineWidth: 4)
                    .frame(width: 250, height: 250)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
    
    private var markerColor: Color {
        guard let verdict else { return .brandPrimary }
        
        switch verdict {
        case .safe: return .verdictSafe
        case .suspicious: return .verdictWarning
        case .malicious: return .verdictDanger
        case .unknown: return .brandPrimary
        }
    }
    
    private func scanLine(in size: CGSize) -> some View {
        Rectangle()
            .fill(
                LinearGradient(
                    colors: [.clear, .brandPrimary.opacity(0.5), .clear],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .frame(width: 200, height: 2)
            .offset(y: sin(animationPhase) * 100)
            .onAppear {
                withAnimation(.easeInOut(duration: 1.5).repeatForever(autoreverses: true)) {
                    animationPhase = .pi
                }
            }
    }
    
    private var cornerMarkers: some Shape {
        CornerMarkerShape()
    }
}

/// Shape for corner markers
struct CornerMarkerShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let cornerLength: CGFloat = 35
        
        // Top Left
        path.move(to: CGPoint(x: 0, y: cornerLength))
        path.addLine(to: CGPoint(x: 0, y: 0))
        path.addLine(to: CGPoint(x: cornerLength, y: 0))
        
        // Top Right
        path.move(to: CGPoint(x: rect.width - cornerLength, y: 0))
        path.addLine(to: CGPoint(x: rect.width, y: 0))
        path.addLine(to: CGPoint(x: rect.width, y: cornerLength))
        
        // Bottom Right
        path.move(to: CGPoint(x: rect.width, y: rect.height - cornerLength))
        path.addLine(to: CGPoint(x: rect.width, y: rect.height))
        path.addLine(to: CGPoint(x: rect.width - cornerLength, y: rect.height))
        
        // Bottom Left
        path.move(to: CGPoint(x: cornerLength, y: rect.height))
        path.addLine(to: CGPoint(x: 0, y: rect.height))
        path.addLine(to: CGPoint(x: 0, y: rect.height - cornerLength))
        
        return path
    }
}

// MARK: - Mock Camera Preview (for SwiftUI Previews)

struct MockCameraPreview: View {
    var body: some View {
        ZStack {
            // Dark background
            Color.black
            
            // Simulated camera view
            VStack(spacing: 24) {
                ZStack {
                    Circle()
                        .fill(.ultraThinMaterial)
                        .frame(width: 100, height: 100)
                    
                    Image(systemName: "camera.fill")
                        .font(.system(size: 40))
                        .foregroundStyle(
                            LinearGradient(
                                colors: [.gray, .gray.opacity(0.5)],
                                startPoint: .top,
                                endPoint: .bottom
                            )
                        )
                        .symbolEffect(.pulse)
                }
                
                Text(NSLocalizedString("scanner.camera_preview", comment: ""))
                    .font(.caption)
                    .foregroundColor(.gray)
                
                // Simulated scan area
                RoundedRectangle(cornerRadius: 12)
                    .strokeBorder(
                        style: StrokeStyle(lineWidth: 2, dash: [10])
                    )
                    .foregroundColor(.gray.opacity(0.5))
                    .frame(width: 200, height: 200)
            }
        }
    }
}

// MARK: - Preview

#Preview("Camera Preview") {
    ZStack {
        MockCameraPreview()
        
        ScanningOverlay(isScanning: true, verdict: nil)
    }
}

#Preview("Scan Complete") {
    ZStack {
        MockCameraPreview()
        
        ScanningOverlay(isScanning: false, verdict: .safe)
    }
}

#endif
