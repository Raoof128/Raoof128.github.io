// Extensions/Assets+Extension.swift
// QR-SHIELD Asset Extensions - iOS 26
//
// CREATED: December 2025
// Provides type-safe access to all app assets with fallback support

import SwiftUI

// MARK: - Image Extensions

extension Image {
    
    // MARK: - Verdict Icons
    
    /// Safe verdict shield icon (green)
    static let shieldSafe = Image("ShieldSafe")
    
    /// Warning verdict shield icon (orange)
    static let shieldWarning = Image("ShieldWarning")
    
    /// Danger verdict shield icon (red)
    static let shieldDanger = Image("ShieldDanger")
    
    // MARK: - Navigation Icons
    
    /// History/clock icon for history tab
    static let iconHistory = Image("IconHistory")
    
    /// Settings/gear icon for settings tab
    static let iconSettings = Image("IconSettings")
    
    /// Gallery/photo icon for import button
    static let iconGallery = Image("IconGallery")
    
    // MARK: - Branding
    
    /// Main app logo
    static let brandingLogo = Image("BrandingLogo")
    
    /// Launch screen logo
    static let launchLogo = Image("LaunchLogo")
    
    /// Danger alert animation graphic
    static let dangerAlert = Image("DangerAlert")
    
    // MARK: - Onboarding
    
    static let onboardScan = Image("OnboardScan")
    static let onboardProtect = Image("OnboardProtect")
    static let onboardPrivacy = Image("OnboardPrivacy")
    
    // MARK: - Verdict Icon Helper
    
    /// Get the appropriate icon for a verdict
    static func forVerdict(_ verdict: VerdictMock) -> Image {
        switch verdict {
        case .safe:
            return shieldSafe
        case .suspicious:
            return shieldWarning
        case .malicious:
            return shieldDanger
        case .unknown:
            return Image(systemName: "questionmark.circle.fill")
        }
    }
    
    /// Get SF Symbol for a verdict (fallback)
    static func sfSymbolForVerdict(_ verdict: VerdictMock) -> Image {
        switch verdict {
        case .safe:
            return Image(systemName: "checkmark.shield.fill")
        case .suspicious:
            return Image(systemName: "exclamationmark.shield.fill")
        case .malicious:
            return Image(systemName: "xmark.shield.fill")
        case .unknown:
            return Image(systemName: "questionmark.circle.fill")
        }
    }
}

// MARK: - Safe Image Loading

extension Image {
    /// Load an asset image with SF Symbol fallback
    /// - Parameters:
    ///   - named: Asset catalog image name
    ///   - fallback: SF Symbol name to use if asset doesn't exist
    init(named: String, fallback: String) {
        if let uiImage = UIImage(named: named) {
            self.init(uiImage: uiImage)
        } else {
            self.init(systemName: fallback)
        }
    }
}

// MARK: - Color Extensions

extension Color {
    
    // MARK: - Asset Catalog Colors
    
    /// Accent color from asset catalog
    static let accent = Color("AccentColor")
    
    /// Safe verdict color from asset catalog
    static let verdictSafeAsset = Color("VerdictSafe")
    
    /// Warning verdict color from asset catalog
    static let verdictWarningAsset = Color("VerdictWarning")
    
    /// Danger verdict color from asset catalog
    static let verdictDangerAsset = Color("VerdictDanger")
    
    // MARK: - Verdict Color Helper
    
    /// Get the color for a verdict from asset catalog
    static func forVerdict(_ verdict: VerdictMock) -> Color {
        switch verdict {
        case .safe:
            return verdictSafeAsset
        case .suspicious:
            return verdictWarningAsset
        case .malicious:
            return verdictDangerAsset
        case .unknown:
            return .gray
        }
    }
}

// MARK: - Animated Verdict Icon View

/// Animated verdict icon with pulse effect for danger states
struct VerdictIcon: View {
    let verdict: VerdictMock
    let size: CGFloat
    var useSFSymbols: Bool = true
    
    @State private var isPulsing = false
    
    var body: some View {
        Group {
            if useSFSymbols {
                Image.sfSymbolForVerdict(verdict)
                    .font(.system(size: size))
            } else {
                Image.forVerdict(verdict)
                    .resizable()
                    .aspectRatio(contentMode: .fit)
                    .frame(width: size, height: size)
            }
        }
        .foregroundColor(color)
        .symbolEffect(.pulse, isActive: verdict == .malicious)
        .scaleEffect(isPulsing && verdict == .malicious ? 1.1 : 1.0)
        .animation(
            verdict == .malicious ?
                .easeInOut(duration: 0.6).repeatForever(autoreverses: true) : .default,
            value: isPulsing
        )
        .onAppear {
            if verdict == .malicious {
                isPulsing = true
            }
        }
        .sensoryFeedback(.impact(weight: verdict == .malicious ? .heavy : .light), trigger: verdict)
    }
    
    private var color: Color {
        Color.forVerdict(verdict)
    }
}

// MARK: - Animated Danger Background

/// Full-screen pulsing danger background for malicious detections
struct DangerBackground: View {
    @State private var opacity: Double = 0.0
    let isActive: Bool
    
    var body: some View {
        ZStack {
            // Red overlay
            Color.verdictDanger
                .opacity(opacity)
                .ignoresSafeArea()
            
            // Radial gradient for depth
            RadialGradient(
                colors: [
                    Color.verdictDanger.opacity(0.4),
                    Color.clear
                ],
                center: .center,
                startRadius: 50,
                endRadius: 400
            )
            .opacity(isActive ? 0.6 : 0)
            .ignoresSafeArea()
        }
        .animation(.easeInOut(duration: 0.3), value: isActive)
        .onChange(of: isActive) { _, newValue in
            if newValue {
                withAnimation(.easeInOut(duration: 0.5).repeatForever(autoreverses: true)) {
                    opacity = 0.2
                }
            } else {
                opacity = 0
            }
        }
    }
}

// MARK: - App Icon Preview (Debug)

/// Preview the app icon in different sizes
struct AppIconPreview: View {
    var body: some View {
        VStack(spacing: 20) {
            Text("App Icon Sizes")
                .font(.headline)
            
            HStack(spacing: 20) {
                iconPreview(size: 60, label: "@3x (60pt)")
                iconPreview(size: 40, label: "@2x (40pt)")
                iconPreview(size: 29, label: "Settings")
            }
        }
        .padding()
    }
    
    func iconPreview(size: CGFloat, label: String) -> some View {
        VStack(spacing: 8) {
            RoundedRectangle(cornerRadius: size * 0.2237)
                .fill(LinearGradient.brandGradient)
                .frame(width: size, height: size)
                .overlay {
                    Image(systemName: "shield.fill")
                        .font(.system(size: size * 0.5))
                        .foregroundColor(.white)
                }
            
            Text(label)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - Preview

#Preview("Asset Extensions") {
    VStack(spacing: 24) {
        HStack(spacing: 20) {
            VerdictIcon(verdict: .safe, size: 50)
            VerdictIcon(verdict: .suspicious, size: 50)
            VerdictIcon(verdict: .malicious, size: 50)
        }
        
        AppIconPreview()
    }
    .padding()
    .background(Color.bgDark)
}

#Preview("Danger Background") {
    ZStack {
        DangerBackground(isActive: true)
        
        VStack {
            VerdictIcon(verdict: .malicious, size: 80)
            Text("MALICIOUS DETECTED")
                .font(.title.bold())
                .foregroundColor(.white)
        }
    }
}
