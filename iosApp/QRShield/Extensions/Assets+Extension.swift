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

// Extensions/Assets+Extension.swift
// QR-SHIELD Asset Extensions - iOS 17+ Compatible
//
// UPDATED: December 2025
// - Removed duplicate color declarations
// - All assets use SF Symbol fallbacks
// - VerdictIcon component with animations

#if os(iOS)
import SwiftUI

// MARK: - Image Extensions

extension Image {
    
    // MARK: - Verdict Icons (SF Symbol based)
    
    /// Safe verdict shield icon
    static let shieldSafe = Image(systemName: "checkmark.shield.fill")
    
    /// Warning verdict shield icon
    static let shieldWarning = Image(systemName: "exclamationmark.shield.fill")
    
    /// Danger verdict shield icon
    static let shieldDanger = Image(systemName: "xmark.shield.fill")
    
    // MARK: - Navigation Icons
    
    /// History icon for history tab
    static let iconHistory = Image(systemName: "clock.fill")
    
    /// Settings icon for settings tab
    static let iconSettings = Image(systemName: "gearshape.fill")
    
    /// Gallery icon for import button
    static let iconGallery = Image(systemName: "photo.on.rectangle")
    
    // MARK: - Branding
    
    /// Main app logo - uses custom PNG from Assets
    static let brandingLogo = Image("Logo")
    
    /// Launch screen logo - uses custom PNG from Assets
    static let launchLogo = Image("Logo")
    
    // MARK: - Verdict Icon Helper
    
    /// Get the appropriate SF Symbol for a verdict
    static func forVerdict(_ verdict: VerdictMock) -> Image {
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
    
    /// Alias for forVerdict
    static func sfSymbolForVerdict(_ verdict: VerdictMock) -> Image {
        forVerdict(verdict)
    }
}

// MARK: - Color Verdict Helper

extension Color {
    /// Get the color for a verdict
    static func forVerdict(_ verdict: VerdictMock) -> Color {
        switch verdict {
        case .safe:
            return .verdictSafe
        case .suspicious:
            return .verdictWarning
        case .malicious:
            return .verdictDanger
        case .unknown:
            return .gray
        }
    }
}

// MARK: - VerdictIcon Component

/// Animated verdict icon with pulse effect for danger states
/// Respects system Reduce Motion setting per HIG accessibility guidelines
struct VerdictIcon: View {
    let verdict: VerdictMock
    let size: CGFloat
    var useSFSymbols: Bool = true
    
    @State private var isPulsing = false
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    
    private var color: Color {
        Color.forVerdict(verdict)
    }
    
    var body: some View {
        Image.forVerdict(verdict)
            .font(.system(size: size))
            .foregroundColor(color)
            .symbolEffect(.pulse, isActive: verdict == .malicious && !reduceMotion)
            .scaleEffect(isPulsing && verdict == .malicious && !reduceMotion ? 1.1 : 1.0)
            .animation(
                verdict == .malicious && !reduceMotion ?
                    .easeInOut(duration: 0.6).repeatForever(autoreverses: true) : .default,
                value: isPulsing
            )
            .onAppear {
                if verdict == .malicious && !reduceMotion {
                    isPulsing = true
                }
            }
            .sensoryFeedback(.impact(weight: verdict == .malicious ? .heavy : .light), trigger: verdict)
            .accessibilityLabel(verdict.rawValue)
    }
}

// MARK: - Danger Background

/// Full-screen pulsing danger background for malicious detections
/// Respects system Reduce Motion setting per HIG accessibility guidelines
struct DangerBackground: View {
    @State private var opacity: Double = 0.0
    let isActive: Bool
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    
    var body: some View {
        ZStack {
            // Red overlay - static when Reduce Motion is enabled
            Color.verdictDanger
                .opacity(reduceMotion ? (isActive ? 0.15 : 0) : opacity)
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
        .animation(reduceMotion ? .none : .easeInOut(duration: 0.3), value: isActive)
        .onChange(of: isActive) { _, newValue in
            // Skip animation if Reduce Motion is enabled
            guard !reduceMotion else { return }
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

#endif
