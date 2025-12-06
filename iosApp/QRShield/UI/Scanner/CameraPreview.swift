// UI/Scanner/CameraPreview.swift
// QR-SHIELD Native Camera Layer - iOS 18+ Optimized
//
// UPDATED: December 2024
// - Uses videoRotationAngle (iOS 17+) instead of deprecated videoOrientation
// - Improved orientation handling
// - Better memory management

import SwiftUI
import AVFoundation

/// UIViewRepresentable wrapper for AVCaptureVideoPreviewLayer
/// Bridges AVFoundation camera to SwiftUI
struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession?
    
    func makeUIView(context: Context) -> CameraPreviewView {
        let view = CameraPreviewView()
        view.backgroundColor = .black
        
        if let session = session {
            view.session = session
        }
        
        return view
    }
    
    func updateUIView(_ uiView: CameraPreviewView, context: Context) {
        if let session = session {
            if uiView.session !== session {
                uiView.session = session
            }
        }
    }
    
    static func dismantleUIView(_ uiView: CameraPreviewView, coordinator: ()) {
        // Clean up when view is removed
        uiView.session = nil
    }
}

/// The underlying UIView that holds the preview layer
final class CameraPreviewView: UIView {
    
    private var previewLayer: AVCaptureVideoPreviewLayer?
    
    var session: AVCaptureSession? {
        didSet {
            guard session !== oldValue else { return }
            setupPreviewLayer()
        }
    }
    
    // Use preview layer as the view's backing layer for better performance
    override class var layerClass: AnyClass {
        AVCaptureVideoPreviewLayer.self
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        // Update frame
        previewLayer?.frame = bounds
        
        // Update rotation for current orientation
        updateVideoRotation()
    }
    
    override func didMoveToWindow() {
        super.didMoveToWindow()
        
        // Observe orientation changes
        if window != nil {
            NotificationCenter.default.addObserver(
                self,
                selector: #selector(orientationDidChange),
                name: UIDevice.orientationDidChangeNotification,
                object: nil
            )
        } else {
            NotificationCenter.default.removeObserver(
                self,
                name: UIDevice.orientationDidChangeNotification,
                object: nil
            )
        }
    }
    
    @objc private func orientationDidChange() {
        updateVideoRotation()
    }
    
    private func setupPreviewLayer() {
        // Remove old layer
        previewLayer?.removeFromSuperlayer()
        previewLayer = nil
        
        guard let session = session else { return }
        
        let layer = AVCaptureVideoPreviewLayer(session: session)
        layer.frame = bounds
        layer.videoGravity = .resizeAspectFill
        
        // Set initial rotation
        updateVideoRotation(for: layer)
        
        self.layer.insertSublayer(layer, at: 0)
        self.previewLayer = layer
    }
    
    private func updateVideoRotation() {
        guard let previewLayer = previewLayer else { return }
        updateVideoRotation(for: previewLayer)
    }
    
    private func updateVideoRotation(for layer: AVCaptureVideoPreviewLayer) {
        guard let connection = layer.connection else { return }
        
        // iOS 17+: Use videoRotationAngle instead of deprecated videoOrientation
        if #available(iOS 17.0, *) {
            let angle = currentRotationAngle()
            if connection.isVideoRotationAngleSupported(angle) {
                connection.videoRotationAngle = angle
            }
        } else {
            // Fallback for iOS 16 and earlier
            if connection.isVideoOrientationSupported {
                connection.videoOrientation = currentVideoOrientation()
            }
        }
    }
    
    // iOS 17+: Get rotation angle based on device orientation
    @available(iOS 17.0, *)
    private func currentRotationAngle() -> CGFloat {
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
    
    // Legacy orientation support
    private func currentVideoOrientation() -> AVCaptureVideoOrientation {
        guard let windowScene = window?.windowScene else {
            return .portrait
        }
        
        switch windowScene.interfaceOrientation {
        case .portrait:
            return .portrait
        case .portraitUpsideDown:
            return .portraitUpsideDown
        case .landscapeLeft:
            return .landscapeLeft
        case .landscapeRight:
            return .landscapeRight
        default:
            return .portrait
        }
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }
}

// MARK: - Mock Camera Preview for SwiftUI Previews

struct MockCameraPreview: View {
    var body: some View {
        ZStack {
            Color.black
            
            VStack(spacing: 20) {
                Image(systemName: "camera.fill")
                    .font(.system(size: 60))
                    .foregroundStyle(.linearGradient(
                        colors: [.gray, .gray.opacity(0.5)],
                        startPoint: .top,
                        endPoint: .bottom
                    ))
                
                Text("Camera Preview")
                    .font(.caption)
                    .foregroundColor(.gray)
                
                // Simulated scan area
                RoundedRectangle(cornerRadius: 12)
                    .strokeBorder(style: StrokeStyle(lineWidth: 2, dash: [10]))
                    .foregroundColor(.gray.opacity(0.5))
                    .frame(width: 200, height: 200)
            }
        }
    }
}

#Preview {
    MockCameraPreview()
}
