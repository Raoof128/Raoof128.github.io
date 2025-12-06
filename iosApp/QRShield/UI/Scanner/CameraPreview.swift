// UI/Scanner/CameraPreview.swift
// QR-SHIELD Native Camera Layer
//
// Bridges AVCaptureSession to SwiftUI for live camera preview.
// This component is the crucial glue between Kotlin/Native camera
// logic and the SwiftUI interface layer.

import SwiftUI
import AVFoundation

/// UIViewRepresentable wrapper for AVCaptureVideoPreviewLayer
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
        if let session = session, uiView.session !== session {
            uiView.session = session
        }
    }
}

/// The underlying UIView that holds the preview layer
class CameraPreviewView: UIView {
    
    private var previewLayer: AVCaptureVideoPreviewLayer?
    
    var session: AVCaptureSession? {
        didSet {
            setupPreviewLayer()
        }
    }
    
    override class var layerClass: AnyClass {
        return AVCaptureVideoPreviewLayer.self
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.frame = bounds
    }
    
    private func setupPreviewLayer() {
        // Remove old layer
        previewLayer?.removeFromSuperlayer()
        
        guard let session = session else { return }
        
        let layer = AVCaptureVideoPreviewLayer(session: session)
        layer.frame = bounds
        layer.videoGravity = .resizeAspectFill
        
        // Handle orientation
        if let connection = layer.connection {
            if connection.isVideoOrientationSupported {
                connection.videoOrientation = currentVideoOrientation()
            }
        }
        
        self.layer.insertSublayer(layer, at: 0)
        self.previewLayer = layer
    }
    
    private func currentVideoOrientation() -> AVCaptureVideoOrientation {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene else {
            return .portrait
        }
        
        switch windowScene.interfaceOrientation {
        case .portrait: return .portrait
        case .portraitUpsideDown: return .portraitUpsideDown
        case .landscapeLeft: return .landscapeLeft
        case .landscapeRight: return .landscapeRight
        default: return .portrait
        }
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
                    .foregroundColor(.gray)
                
                Text("Camera Preview")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
    }
}

#Preview {
    MockCameraPreview()
}
