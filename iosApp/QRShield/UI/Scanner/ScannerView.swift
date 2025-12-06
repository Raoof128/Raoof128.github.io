// UI/Scanner/ScannerView.swift
// QR-SHIELD Main Scanner Interface - iOS 18+ / SwiftUI 2024
//
// UPDATED: December 2024 - Modern SwiftUI patterns
// - Uses @State with @Observable (replaces @StateObject)
// - Enhanced animations with matchedTransitionSource
// - iOS 18 material effects and scroll behavior

import SwiftUI
import AVFoundation

struct ScannerView: View {
    // iOS 17+: Use @State with @Observable instead of @StateObject
    @State private var viewModel = ScannerViewModel()
    @State private var showDetails = false
    @State private var showGalleryPicker = false
    @State private var showPermissionAlert = false
    
    // Animation namespace for matched geometry transitions
    @Namespace private var animation
    
    var body: some View {
        ZStack {
            // 1. Full Screen Camera Preview
            CameraPreview(session: viewModel.session)
                .ignoresSafeArea()
            
            // 2. Gradient Overlay for Readability
            gradientOverlay
            
            // 3. Main Content
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
            
            // 4. Permission Overlay
            if viewModel.cameraPermissionStatus == .denied {
                permissionDeniedOverlay
            }
        }
        .sheet(isPresented: $showDetails) {
            if let result = viewModel.currentResult {
                DetailSheet(assessment: result)
                    .presentationDetents([.medium, .large])
                    .presentationDragIndicator(.visible)
                    .presentationBackground(.ultraThinMaterial)
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
        .onChange(of: viewModel.cameraPermissionStatus) { oldValue, newValue in
            showPermissionAlert = (newValue == .denied)
        }
        .alert("Camera Access Required", isPresented: $showPermissionAlert) {
            Button("Open Settings") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("QR-SHIELD needs camera access to scan QR codes. Please enable it in Settings.")
        }
    }
    
    // MARK: - Gradient Overlay
    
    private var gradientOverlay: some View {
        LinearGradient(
            gradient: Gradient(colors: [
                Color.black.opacity(0.7),
                Color.clear,
                Color.clear,
                Color.black.opacity(0.85)
            ]),
            startPoint: .top,
            endPoint: .bottom
        )
        .ignoresSafeArea()
    }
    
    // MARK: - Header Bar
    
    private var headerBar: some View {
        HStack {
            // Logo
            HStack(spacing: 8) {
                Image(systemName: "shield.fill")
                    .foregroundStyle(
                        LinearGradient(
                            colors: [.brandPrimary, .brandSecondary],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
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
                    // iOS 18: Pulsing animation for scanning state
                    .scaleEffect(viewModel.isScanning ? 1.2 : 1.0)
                    .animation(.easeInOut(duration: 0.8).repeatForever(autoreverses: true), value: viewModel.isScanning)
                
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
                    .contentTransition(.symbolEffect(.replace))
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 16))
    }
    
    // MARK: - Center Content
    
    @ViewBuilder
    private var centerContent: some View {
        if let result = viewModel.currentResult {
            // Show Result Card with matched geometry
            ResultCard(assessment: result)
                .matchedTransitionSource(id: "result", in: animation)
                .transition(.asymmetric(
                    insertion: .scale.combined(with: .opacity),
                    removal: .opacity
                ))
                .onTapGesture { showDetails = true }
                .contextMenu {
                    Button {
                        showDetails = true
                    } label: {
                        Label("View Details", systemImage: "doc.text.magnifyingglass")
                    }
                    
                    Button {
                        UIPasteboard.general.string = result.url
                    } label: {
                        Label("Copy URL", systemImage: "doc.on.doc")
                    }
                    
                    Button(role: .destructive) {
                        viewModel.dismissResult()
                    } label: {
                        Label("Dismiss", systemImage: "xmark")
                    }
                }
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
            .background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: 20))
            .transition(.scale.combined(with: .opacity))
        } else {
            // Scanning Target
            scanningIndicator
                .transition(.opacity)
        }
    }
    
    // MARK: - Scanning Indicator
    
    private var scanningIndicator: some View {
        ZStack {
            // Outer glow - iOS 18 enhanced animation
            Circle()
                .stroke(Color.brandPrimary.opacity(0.3), lineWidth: 2)
                .frame(width: 280, height: 280)
                .scaleEffect(viewModel.isScanning ? 1.1 : 1.0)
                .opacity(viewModel.isScanning ? 0.5 : 0.3)
                .animation(.easeInOut(duration: 1.5).repeatForever(autoreverses: true), value: viewModel.isScanning)
            
            // Main circle with gradient
            Circle()
                .strokeBorder(
                    LinearGradient(
                        colors: [.brandPrimary, .brandSecondary],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    lineWidth: 3
                )
                .frame(width: 250, height: 250)
            
            // Corner markers
            RoundedRectangle(cornerRadius: 4)
                .stroke(Color.brandPrimary, lineWidth: 4)
                .frame(width: 220, height: 220)
                .mask(CornerMask())
            
            // Center icon with SF Symbol animation
            Image(systemName: "qrcode.viewfinder")
                .font(.system(size: 50))
                .foregroundStyle(.linearGradient(
                    colors: [.white.opacity(0.5), .white.opacity(0.3)],
                    startPoint: .top,
                    endPoint: .bottom
                ))
                .symbolEffect(.pulse, isActive: viewModel.isScanning)
            
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
                        .fill(
                            LinearGradient(
                                colors: [.brandPrimary, .brandSecondary],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 70, height: 70)
                        .shadow(color: .brandPrimary.opacity(0.5), radius: 10)
                    
                    Image(systemName: viewModel.isScanning ? "pause.fill" : "play.fill")
                        .font(.system(size: 28))
                        .foregroundColor(.white)
                        .contentTransition(.symbolEffect(.replace))
                }
            }
            .sensoryFeedback(.impact(weight: .medium), trigger: viewModel.isScanning)
            
            // History Button
            NavigationLink(destination: HistoryView()) {
                VStack(spacing: 6) {
                    Image(systemName: "clock.fill")
                        .font(.title2)
                        .foregroundColor(.white)
                        .frame(width: 50, height: 50)
                        .background(.ultraThinMaterial, in: Circle())
                    
                    Text("History")
                        .font(.caption2)
                        .foregroundColor(.textSecondary)
                }
            }
        }
    }
    
    // MARK: - Permission Denied Overlay
    
    private var permissionDeniedOverlay: some View {
        VStack(spacing: 24) {
            Image(systemName: "camera.fill")
                .font(.system(size: 60))
                .foregroundColor(.textMuted)
            
            Text("Camera Access Required")
                .font(.title2.weight(.semibold))
                .foregroundColor(.textPrimary)
            
            Text("QR-SHIELD needs access to your camera to scan QR codes.")
                .font(.subheadline)
                .foregroundColor(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            Button("Open Settings") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            .font(.headline)
            .foregroundColor(.white)
            .padding(.horizontal, 32)
            .padding(.vertical, 14)
            .background(LinearGradient.brandGradient, in: Capsule())
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.bgDark.opacity(0.95))
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
                    .background(.ultraThinMaterial, in: Circle())
                
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

#Preview {
    NavigationStack {
        ScannerView()
    }
}
