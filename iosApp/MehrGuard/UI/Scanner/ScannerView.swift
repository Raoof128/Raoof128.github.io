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

// UI/Scanner/ScannerView.swift
// Mehr Guard Main Scanner Interface - iOS 17+ Liquid Glass Edition
//
// UPDATED: December 2025 - iOS 17+ Features
// - Liquid Glass toolbars
// - Enhanced visual effects
// - GlassButtonStyle integration
// - Optimized rendering pipeline

import SwiftUI
import AVFoundation
#if os(iOS)

struct ScannerView: View {
    // iOS 17+: Use StateObject pattern with @Observable
    // This prevents the ViewModel from being recreated when switching tabs
    @State private var viewModel: ScannerViewModel
    @State private var showDetails = false
    @State private var showGalleryPicker = false
    @State private var showPermissionAlert = false
    
    // Settings
    @AppStorage("autoScan") private var autoScan = true
    @AppStorage("liquidGlassReduced") private var liquidGlassReduced = false
    @AppStorage("useDarkMode") private var useDarkMode = true
    @AppStorage("developerModeEnabled") private var developerModeEnabled = false
    
    // Environment
    @Environment(\.scenePhase) private var scenePhase
    
    // Animation namespace for transitions
    @Namespace private var animation
    
    // Initialize with a persistent view model
    init() {
        // Create the view model once and reuse it
        _viewModel = State(initialValue: ScannerViewModel.shared)
    }
    
    var body: some View {
        ZStack {
            // 1. Full Screen Camera Preview
            CameraPreview(session: viewModel.session)
                .ignoresSafeArea()
            
            // 2. Liquid Glass Gradient Overlay
            liquidGlassOverlay
            
            // 3. Main Content
            VStack(spacing: 0) {
                // RED TEAM SCENARIOS (Developer Mode Only)
                if developerModeEnabled {
                    RedTeamScenariosPanel { scenario in
                        // Bypass camera - feed malicious URL directly to analysis engine
                        SettingsManager.shared.triggerHaptic(.warning)
                        SettingsManager.shared.playSound(.scan)
                        viewModel.analyzeUrl(scenario.maliciousUrl)
                    }
                    .transition(.move(edge: .top).combined(with: .opacity))
                }
                
                // Header Bar (Liquid Glass)
                headerBar
                    .padding(.horizontal)
                    .padding(.top, 8)
                
                Spacer()
                
                // Center: Scan Indicator or Result
                centerContent
                
                Spacer()
                
                // Bottom Controls (Liquid Glass)
                controlBar
                    .padding(.bottom, 30)
            }
            
            // 4. Permission Overlay
            if viewModel.cameraPermissionStatus == .denied || viewModel.cameraPermissionStatus == .notDetermined {
                permissionDeniedOverlay
            }

            // 5. Error Banner
            if let error = viewModel.errorMessage {
                errorBanner(error)
                    .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .sheet(isPresented: $showDetails) {
            if let result = viewModel.currentResult {
                DetailSheet(assessment: result)
                    .presentationDetents([.medium, .large])
                    .presentationDragIndicator(.visible)
                    // iOS 17+: Glass background for sheets
                    .presentationBackground(.ultraThinMaterial)
                    .preferredColorScheme(useDarkMode ? .dark : .light)
            }
        }
        .sheet(isPresented: $showGalleryPicker) {
            ImagePicker(onImagePicked: viewModel.analyzeImage)
                .preferredColorScheme(useDarkMode ? .dark : .light)
        }
        .onAppear {
            // ViewModel handles auto-start in checkCameraPermission -> autoStartIfNeeded
            // Only start if already authorized and autoScan is enabled; otherwise wait for permission
            if ScannerViewModel.shared.cameraPermissionStatus == .authorized && autoScan {
                viewModel.startCamera()
            }
        }
        .onDisappear {
            viewModel.stopCamera()
        }
        .onChange(of: scenePhase) { oldValue, newValue in
            switch newValue {
            case .active:
                // Re-check permission when coming from background
                Task { await viewModel.checkCameraPermission() }
            case .inactive, .background:
                viewModel.stopCamera()
            @unknown default:
                break
            }
        }
        .onChange(of: autoScan) { _, newValue in
            if newValue && viewModel.cameraPermissionStatus == .authorized {
                viewModel.startCamera()
            } else if !newValue {
                viewModel.stopCamera()
            }
        }
        // iOS 17+: Toolbar with glass styling
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Text(String(format: "Scans: %d", viewModel.scanCount))
                    .font(.caption)
                    .foregroundColor(.textSecondary)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .liquidGlass(cornerRadius: 12)
            }
        }
    }

    // MARK: - Error Banner

    private func errorBanner(_ message: String) -> some View {
        VStack {
            HStack(spacing: 12) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .foregroundColor(.verdictWarning)
                Text(message)
                    .font(.footnote)
                    .foregroundColor(.textPrimary)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)
                Spacer(minLength: 8)
                Button {
                    withAnimation {
                        viewModel.errorMessage = nil
                    }
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(.textMuted)
                }
                .accessibilityLabel(Text(NSLocalizedString("accessibility.dismiss_error", comment: "")))
            }
            .padding(12)
            .background(.ultraThinMaterial, in: Capsule())
            .padding(.horizontal, 16)
            .shadow(color: .black.opacity(0.2), radius: 8, y: 4)

            Spacer()
        }
        .padding(.top, 20)
    }
    
    // MARK: - Liquid Glass Overlay (iOS 17+)
    
    private var liquidGlassOverlay: some View {
        Group {
            if liquidGlassReduced {
                // Simplified static gradient when effects are reduced
                LinearGradient(
                    colors: [
                        Color.black.opacity(0.75),
                        Color.black.opacity(0.4),
                        Color.clear
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                )
            } else if viewModel.isScanning {
                // Animated overlay ONLY when actively scanning (performance optimization)
                TimelineView(.animation(minimumInterval: 1.0/30.0)) { timeline in
                    let time = timeline.date.timeIntervalSinceReferenceDate
                    
                    ZStack {
                        LinearGradient(
                            gradient: Gradient(stops: [
                                .init(color: Color.black.opacity(0.8), location: 0),
                                .init(color: Color.black.opacity(0.3), location: 0.3 + sin(time * 0.5) * 0.05),
                                .init(color: Color.clear, location: 0.5 + cos(time * 0.5) * 0.05)
                            ]),
                            startPoint: .top,
                            endPoint: .center
                        )
                        
                        LinearGradient(
                            gradient: Gradient(stops: [
                                .init(color: Color.clear, location: 0.5),
                                .init(color: Color.black.opacity(0.5), location: 0.7 - cos(time * 0.4) * 0.05),
                                .init(color: Color.black.opacity(0.9), location: 1.0)
                            ]),
                            startPoint: .center,
                            endPoint: .bottom
                        )
                    }
                }
            } else {
                // Static overlay when paused (no animation overhead)
                ZStack {
                    LinearGradient(
                        gradient: Gradient(stops: [
                            .init(color: Color.black.opacity(0.8), location: 0),
                            .init(color: Color.black.opacity(0.3), location: 0.3),
                            .init(color: Color.clear, location: 0.5)
                        ]),
                        startPoint: .top,
                        endPoint: .center
                    )
                    
                    LinearGradient(
                        gradient: Gradient(stops: [
                            .init(color: Color.clear, location: 0.5),
                            .init(color: Color.black.opacity(0.5), location: 0.7),
                            .init(color: Color.black.opacity(0.9), location: 1.0)
                        ]),
                        startPoint: .center,
                        endPoint: .bottom
                    )
                }
            }
        }
        .ignoresSafeArea()
    }
    
    // MARK: - Header Bar (Liquid Glass iOS 17+)
    
    private var headerBar: some View {
        HStack {
            // Logo with gradient
            HStack(spacing: 8) {
                // Use custom asset if available, fallback to SF Symbol
                Image.forVerdict(.safe)
                    .renderingMode(.template)
                    .foregroundStyle(LinearGradient.brandGradient)
                    .font(.title2)
                    .symbolEffect(.pulse, isActive: viewModel.isScanning)
                
                Text(NSLocalizedString("app.name", comment: ""))
                    .font(.system(.headline, design: .rounded))
                    .fontWeight(.bold)
                    .foregroundColor(.white)
            }
            
            Spacer()
            
            // Status Indicator with glass pill
            HStack(spacing: 6) {
                Circle()
                    .fill(viewModel.isScanning ? Color.verdictSafe : Color.gray)
                    .frame(width: 8, height: 8)
                    .shadow(color: viewModel.isScanning ? .verdictSafe.opacity(0.5) : .clear, radius: 4)
                
                Text(viewModel.isScanning ? "Scanning" : "Paused")
                    .font(.caption)
                    .foregroundColor(.textSecondary)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(.ultraThinMaterial, in: Capsule())
            
            Spacer()
            
            // Flash Toggle with Liquid Glass
            Button(action: viewModel.toggleFlash) {
                Image(systemName: viewModel.isFlashOn ? "bolt.fill" : "bolt.slash")
                    .foregroundColor(viewModel.isFlashOn ? .yellow : .white.opacity(0.7))
                    .font(.title3)
                    .contentTransition(.symbolEffect(.replace))
            }
            .frame(width: 44, height: 44)
            .background(.ultraThinMaterial, in: Circle())
            .accessibilityLabel(Text(viewModel.isFlashOn ? "Flash on" : "Flash off"))
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .liquidGlass(cornerRadius: 20)
    }
    
    // MARK: - Center Content
    
    @ViewBuilder
    private var centerContent: some View {
        if let result = viewModel.currentResult {
            // Show Result Card with Liquid Glass
            ResultCard(assessment: result, onTap: { showDetails = true })
                .transition(.asymmetric(
                    insertion: .scale(scale: 0.9).combined(with: .opacity),
                    removal: .scale(scale: 0.95).combined(with: .opacity)
                ))
                .onTapGesture { showDetails = true }
                .contextMenu {
                    Button {
                        showDetails = true
                    } label: {
                        Label(
                            "View Details",
                            systemImage: "doc.text.magnifyingglass"
                        )
                    }
                    
                    Button {
                        UIPasteboard.general.string = result.url
                        SettingsManager.shared.triggerHaptic(.success)
                    } label: {
                        Label(
                            "Copy URL",
                            systemImage: "doc.on.doc"
                        )
                    }
                    
                    ShareLink(item: result.url) {
                        Label(
                            "Share",
                            systemImage: "square.and.arrow.up"
                        )
                    }
                    
                    Divider()
                    
                    Button(role: .destructive) {
                        viewModel.dismissResult()
                    } label: {
                        Label(
                            "Dismiss",
                            systemImage: "xmark"
                        )
                    }
                }
        } else if viewModel.isAnalyzing {
            // Analyzing State with Liquid Glass
            VStack(spacing: 16) {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .brandPrimary))
                    .scaleEffect(1.5)
                
                Text(NSLocalizedString("scanner.analyzing_dots", comment: ""))
                    .font(.subheadline)
                    .foregroundColor(.textSecondary)
            }
            .padding(30)
            .liquidGlass(cornerRadius: 24)
            .transition(.scale.combined(with: .opacity))
            // Breathing animation
            .phaseAnimator([0.98, 1.02]) { content, scale in
                content.scaleEffect(scale)
            } animation: { _ in
                .easeInOut(duration: 1.0)
            }
        } else {
            // Scanning Target with iOS 17+ effects
            scanningIndicator
                .transition(.opacity)
        }
    }
    
    // MARK: - Scanning Indicator (iOS 17+ Enhanced)
    
    private var scanningIndicator: some View {
        ZStack {
            // Outer glow rings with optimized animation
            if !liquidGlassReduced {
                ForEach(0..<3, id: \.self) { i in
                    Circle()
                        .stroke(Color.brandPrimary.opacity(0.2 - Double(i) * 0.05), lineWidth: 2)
                        .frame(width: CGFloat(280 + i * 20), height: CGFloat(280 + i * 20))
                        .scaleEffect(viewModel.isScanning ? 1.1 : 1.0)
                        .animation(
                            .easeInOut(duration: 1.5 + Double(i) * 0.2)
                            .repeatForever(autoreverses: true)
                            .delay(Double(i) * 0.1),
                            value: viewModel.isScanning
                        )
                }
            }
            
            // Main circle with Liquid Glass effect
            Circle()
                .fill(.ultraThinMaterial)
                .frame(width: 250, height: 250)
                .overlay {
                    Circle()
                        .strokeBorder(
                            LinearGradient(
                                colors: [
                                    .white.opacity(0.3),
                                    .brandPrimary.opacity(0.5),
                                    .brandSecondary.opacity(0.3),
                                    .white.opacity(0.1)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 3
                        )
                }
                .shadow(color: .brandPrimary.opacity(0.3), radius: 20)
                .accessibilityLabel(Text(NSLocalizedString("scanner.point_qr", comment: "")))
                .accessibilityHint(Text(NSLocalizedString("accessibility.toggle_scanning", comment: "")))
            
            // Corner markers
            RoundedRectangle(cornerRadius: 4)
                .stroke(Color.brandPrimary, lineWidth: 4)
                .frame(width: 200, height: 200)
                .mask(CornerMask())
            
            // Center icon with iOS 17+ symbol effects
            Image(systemName: "qrcode.viewfinder")
                .font(.system(size: 50))
                .foregroundStyle(
                    LinearGradient(
                        colors: [.white.opacity(0.6), .white.opacity(0.3)],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .symbolEffect(.variableColor.iterative, isActive: viewModel.isScanning)
            
            // Scan text
            Text(NSLocalizedString("scanner.point_qr", comment: ""))
                .font(.caption)
                .fontWeight(.medium)
                .foregroundColor(.textMuted)
                .offset(y: 145)
        }
    }
    
    // MARK: - Control Bar (Liquid Glass iOS 17+)
    
    private var controlBar: some View {
        HStack(spacing: 50) {
            // Gallery Button
            ControlButton(
                icon: "photo.on.rectangle",
                label: "Gallery"
            ) {
                showGalleryPicker = true
            }
            .accessibilityLabel(Text(NSLocalizedString("accessibility.gallery", comment: "")))
            
            // Main Scan Button with Liquid Glass
            Button(action: viewModel.toggleScanning) {
                ZStack {
                    // Glass background
                    Circle()
                        .fill(.ultraThinMaterial)
                        .frame(width: 80, height: 80)
                    
                    // Gradient overlay
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: [.brandPrimary.opacity(0.8), .brandSecondary.opacity(0.6)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 70, height: 70)
                    
                    // Inner highlight
                    Circle()
                        .stroke(LinearGradient.liquidGlassOverlay, lineWidth: 2)
                        .frame(width: 70, height: 70)
                    
                    Image(systemName: viewModel.isScanning ? "pause.fill" : "play.fill")
                        .font(.system(size: 28))
                        .foregroundColor(.white)
                        .contentTransition(.symbolEffect(.replace))
                }
                .shadow(color: .brandPrimary.opacity(0.4), radius: 15)
            }
            .accessibilityLabel(Text(NSLocalizedString("accessibility.toggle_scanning", comment: "")))
    
            
            // History Button
            NavigationLink(destination: HistoryView()) {
                VStack(spacing: 6) {
                    Image(systemName: "clock.fill")
                        .font(.title2)
                        .foregroundColor(.white)
                        .frame(width: 50, height: 50)
                        .background(.ultraThinMaterial, in: Circle())
                    
                    Text(NSLocalizedString("tab.history", comment: ""))
                        .font(.caption2)
                        .foregroundColor(.textSecondary)
                }
            }
            .accessibilityLabel(Text(NSLocalizedString("accessibility.open_history", comment: "")))
        }
        .padding(.horizontal, 40)
        .padding(.vertical, 20)
        .liquidGlass(cornerRadius: 30)
    }
    
    // MARK: - Permission Denied Overlay
    
    private var permissionDeniedOverlay: some View {
        VStack(spacing: 24) {
            Image(systemName: "camera.fill")
                .font(.system(size: 60))
                .foregroundColor(.textMuted)
                .symbolEffect(.pulse)
            
            Text(NSLocalizedString("scanner.camera_required_title", comment: ""))
                .font(.title2.weight(.semibold))
                .foregroundColor(.textPrimary)
            
            Text(NSLocalizedString("scanner.camera_required_message", comment: ""))
                .font(.subheadline)
                .foregroundColor(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            InteractiveGlassButton("Open Settings", icon: "gear") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            .padding(.horizontal, 40)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(.ultraThinMaterial)
    }
}

// MARK: - Control Button (Liquid Glass)

struct ControlButton: View {
    let icon: String
    let label: String
    var action: () -> Void = {}
    
    @State private var tapCount = 0
    
    var body: some View {
        Button {
            tapCount += 1
            action()
        } label: {
            VStack(spacing: 6) {
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(.white)
                    .frame(width: 50, height: 50)
                    .background(.ultraThinMaterial, in: Circle())
                    .overlay {
                        Circle()
                            .stroke(Color.white.opacity(0.1), lineWidth: 1)
                    }
                
                Text(label)
                    .font(.caption2)
                    .foregroundColor(.textSecondary)
            }
        }
        .sensoryFeedback(.impact(weight: .light), trigger: tapCount)
    }
}

// MARK: - Corner Mask

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

// MARK: - Red Team Scenarios Panel

/// Red Team Scenarios Panel - Shows attack scenarios for testing.
/// This panel appears at the top of the scanner screen when Developer Mode is enabled.
/// Clicking a scenario bypasses the camera and feeds the malicious URL directly to the engine.
struct RedTeamScenariosPanel: View {
    let onScenarioClick: (RedTeamScenario) -> Void
    
    var body: some View {
        VStack(spacing: 8) {
            // Header
            HStack {
                Text("🕵️")
                    .font(.title3)
                
                Text(NSLocalizedString("scanner.red_team_title", comment: ""))
                    .font(.subheadline.weight(.semibold))
                    .foregroundColor(Color(red: 1.0, green: 0.42, blue: 0.42))
                
                Spacer()
                
                Text(String(format: NSLocalizedString("scanner.red_team_attacks_fmt", comment: ""), RedTeamScenarios.scenarios.count))
                    .font(.caption2)
                    .foregroundColor(Color(red: 0.67, green: 0.47, blue: 0.47))
            }
            .padding(.horizontal, 16)
            
            // Scrollable Scenario Chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(RedTeamScenarios.scenarios) { scenario in
                        RedTeamScenarioChip(scenario: scenario) {
                            onScenarioClick(scenario)
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
            
            // Hint text
            Text(NSLocalizedString("scanner.red_team_hint", comment: ""))
                .font(.caption2)
                .foregroundColor(Color(red: 0.67, green: 0.47, blue: 0.47))
                .padding(.horizontal, 16)
        }
        .padding(.vertical, 12)
        .background(
            LinearGradient(
                colors: [
                    Color(red: 0.24, green: 0.08, blue: 0.08),
                    Color(red: 0.16, green: 0.06, blue: 0.06)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        )
    }
}

/// A single Red Team scenario chip.
struct RedTeamScenarioChip: View {
    let scenario: RedTeamScenario
    let onClick: () -> Void
    
    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 6) {
                Text(scenario.categoryIcon)
                    .font(.caption)
                
                Text(scenario.title)
                    .font(.caption2.weight(.medium))
                    .foregroundColor(.white)
                    .lineLimit(1)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(scenario.categoryColor.opacity(0.2), in: Capsule())
            .overlay(
                Capsule()
                    .stroke(scenario.categoryColor.opacity(0.5), lineWidth: 1)
            )
        }
        .accessibilityLabel("\(scenario.title). \(scenario.description). Tap to test.")
    }
}

#endif
