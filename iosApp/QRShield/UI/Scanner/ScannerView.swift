// UI/Scanner/ScannerView.swift
// QR-SHIELD Main Scanner Interface
//
// The primary home screen with glassmorphic design and real-time scanning.
// Wraps the Kotlin Multiplatform IosQrScanner in a beautiful SwiftUI interface.

import SwiftUI
import AVFoundation

struct ScannerView: View {
    @StateObject private var viewModel = ScannerViewModel()
    @State private var showDetails = false
    @State private var showGalleryPicker = false
    
    var body: some View {
        ZStack {
            // 1. Full Screen Camera Preview
            CameraPreview(session: viewModel.session)
                .ignoresSafeArea()
            
            // 2. Gradient Overlay for Readability
            LinearGradient(
                gradient: Gradient(colors: [
                    Color.black.opacity(0.6),
                    Color.clear,
                    Color.clear,
                    Color.black.opacity(0.8)
                ]),
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            // 3. UI Overlay
            VStack(spacing: 0) {
                // Header Bar (Glassmorphic)
                headerBar
                    .padding(.horizontal)
                    .padding(.top, 8)
                
                Spacer()
                
                // Center: Scan Indicator or Result
                centerContent
                
                Spacer()
                
                // Bottom Controls
                controlBar
                    .padding(.bottom, 30)
            }
        }
        .sheet(isPresented: $showDetails) {
            if let result = viewModel.currentResult {
                DetailSheet(assessment: result)
            }
        }
        .sheet(isPresented: $showGalleryPicker) {
            ImagePicker(onImagePicked: viewModel.analyzeImage)
        }
        .onAppear {
            viewModel.startCamera()
        }
        .onDisappear {
            viewModel.stopCamera()
        }
    }
    
    // MARK: - Header Bar
    
    private var headerBar: some View {
        HStack {
            // Logo
            HStack(spacing: 8) {
                Image(systemName: "shield.fill")
                    .foregroundColor(.brandPrimary)
                    .font(.title2)
                
                Text("QR-SHIELD")
                    .font(.system(.headline, design: .rounded))
                    .fontWeight(.bold)
                    .foregroundColor(.white)
            }
            
            Spacer()
            
            // Status Indicator
            HStack(spacing: 4) {
                Circle()
                    .fill(viewModel.isScanning ? Color.verdictSafe : Color.gray)
                    .frame(width: 8, height: 8)
                
                Text(viewModel.isScanning ? "Scanning" : "Paused")
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }
            
            Spacer()
            
            // Flash Toggle
            Button(action: viewModel.toggleFlash) {
                Image(systemName: viewModel.isFlashOn ? "bolt.fill" : "bolt.slash")
                    .foregroundColor(viewModel.isFlashOn ? .yellow : .white.opacity(0.7))
                    .font(.title3)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(.ultraThinMaterial)
        .cornerRadius(16)
    }
    
    // MARK: - Center Content
    
    @ViewBuilder
    private var centerContent: some View {
        if let result = viewModel.currentResult {
            // Show Result Card
            ResultCard(assessment: result)
                .transition(.asymmetric(
                    insertion: .scale.combined(with: .opacity),
                    removal: .opacity
                ))
                .onTapGesture { showDetails = true }
        } else if viewModel.isAnalyzing {
            // Analyzing State
            VStack(spacing: 16) {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .brandPrimary))
                    .scaleEffect(1.5)
                
                Text("Analyzing...")
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
            }
            .padding(30)
            .background(.ultraThinMaterial)
            .cornerRadius(20)
        } else {
            // Scanning Target
            scanningIndicator
        }
    }
    
    private var scanningIndicator: some View {
        ZStack {
            // Outer glow
            Circle()
                .stroke(Color.brandPrimary.opacity(0.3), lineWidth: 2)
                .frame(width: 280, height: 280)
            
            // Main circle
            Circle()
                .strokeBorder(
                    LinearGradient.brandGradient,
                    lineWidth: 3
                )
                .frame(width: 250, height: 250)
            
            // Corner markers
            RoundedRectangle(cornerRadius: 4)
                .stroke(Color.brandPrimary, lineWidth: 4)
                .frame(width: 220, height: 220)
                .mask(
                    CornerMask()
                )
            
            // Center icon
            Image(systemName: "qrcode.viewfinder")
                .font(.system(size: 50))
                .foregroundColor(.white.opacity(0.5))
            
            // Scan text
            Text("Point at QR Code")
                .font(.caption)
                .foregroundColor(.textMuted)
                .offset(y: 140)
        }
        .shadow(color: .brandPrimary.opacity(0.4), radius: 20)
    }
    
    // MARK: - Control Bar
    
    private var controlBar: some View {
        HStack(spacing: 50) {
            // Gallery Button
            ControlButton(
                icon: "photo.on.rectangle",
                label: "Gallery"
            ) {
                showGalleryPicker = true
            }
            
            // Main Scan Button
            Button(action: viewModel.toggleScanning) {
                ZStack {
                    Circle()
                        .fill(LinearGradient.brandGradient)
                        .frame(width: 70, height: 70)
                        .shadow(color: .brandPrimary.opacity(0.5), radius: 10)
                    
                    Image(systemName: viewModel.isScanning ? "pause.fill" : "play.fill")
                        .font(.system(size: 28))
                        .foregroundColor(.white)
                }
            }
            
            // History Button
            NavigationLink(destination: HistoryView()) {
                ControlButton(icon: "clock.fill", label: "History") {}
            }
        }
    }
}

// MARK: - Control Button

struct ControlButton: View {
    let icon: String
    let label: String
    var action: () -> Void = {}
    
    var body: some View {
        Button(action: action) {
            VStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(.white)
                    .frame(width: 50, height: 50)
                    .background(.ultraThinMaterial)
                    .clipShape(Circle())
                
                Text(label)
                    .font(.caption2)
                    .foregroundColor(.textSecondary)
            }
        }
    }
}

// MARK: - Corner Mask for Scanning Indicator

struct CornerMask: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let cornerLength: CGFloat = 30
        
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

#Preview {
    ScannerView()
}
