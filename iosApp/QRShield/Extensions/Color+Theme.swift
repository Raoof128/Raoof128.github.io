// Extensions/Color+Theme.swift
// QR-SHIELD Design System - iOS 26 Liquid Glass Edition
//
// UPDATED: December 2025 - Full Liquid Glass support
// Integrates with Apple's new design language while maintaining brand identity

import SwiftUI

// MARK: - Brand Colors

extension Color {
    // Primary brand colors - refined for Liquid Glass
    static let brandPrimary = Color(hex: "6C5CE7")       // Purple
    static let brandSecondary = Color(hex: "00D68F")    // Cyan/Teal
    static let brandAccent = Color(hex: "A855F7")       // Light purple
    
    // Verdict colors - optimized for glass backgrounds
    static let verdictSafe = Color(hex: "00D68F")       // Green
    static let verdictWarning = Color(hex: "F5A623")    // Orange
    static let verdictDanger = Color(hex: "FF5252")     // Red
    static let verdictUnknown = Color(hex: "8B93A1")    // Gray
    
    // Background colors - Liquid Glass compatible
    static let bgDark = Color(hex: "0D1117")            // Deep dark
    static let bgCard = Color(hex: "161B22")            // Card background
    static let bgSurface = Color(hex: "21262D")         // Surface/elevated
    static let bgGlass = Color.white.opacity(0.05)      // Glass tint
    
    // Text colors
    static let textPrimary = Color.white
    static let textSecondary = Color(hex: "8B949E")
    static let textMuted = Color(hex: "6E7681")
}

// MARK: - Liquid Glass Colors (iOS 26)

extension Color {
    /// Liquid Glass tint - adapts to background
    static let liquidGlassTint = Color.white.opacity(0.08)
    
    /// Glass border for depth
    static let glassBorder = Color.white.opacity(0.12)
    
    /// Glass highlight for inner shine
    static let glassHighlight = Color.white.opacity(0.15)
    
    /// Glass shadow for depth
    static let glassShadow = Color.black.opacity(0.25)
}

// MARK: - Gradient Extensions

extension LinearGradient {
    static let brandGradient = LinearGradient(
        colors: [.brandPrimary, .brandSecondary],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    static let dangerGradient = LinearGradient(
        colors: [.verdictDanger, .verdictDanger.opacity(0.7)],
        startPoint: .top,
        endPoint: .bottom
    )
    
    static let safeGradient = LinearGradient(
        colors: [.verdictSafe, .verdictSafe.opacity(0.7)],
        startPoint: .top,
        endPoint: .bottom
    )
    
    // Liquid Glass gradient overlay
    static let liquidGlassOverlay = LinearGradient(
        colors: [
            Color.white.opacity(0.15),
            Color.white.opacity(0.05),
            Color.clear
        ],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}

// MARK: - Liquid Glass View Modifier (iOS 26)

struct LiquidGlassStyle: ViewModifier {
    var cornerRadius: CGFloat = 20
    var intensity: Double = 1.0
    
    func body(content: Content) -> some View {
        content
            .background {
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(.ultraThinMaterial)
                    .overlay {
                        // Inner highlight
                        RoundedRectangle(cornerRadius: cornerRadius)
                            .stroke(
                                LinearGradient(
                                    colors: [
                                        Color.white.opacity(0.2 * intensity),
                                        Color.white.opacity(0.05 * intensity),
                                        Color.clear
                                    ],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1
                            )
                    }
                    .shadow(
                        color: Color.black.opacity(0.15 * intensity),
                        radius: 10,
                        x: 0,
                        y: 5
                    )
            }
    }
}

extension View {
    /// Apply Liquid Glass styling (iOS 26)
    func liquidGlass(cornerRadius: CGFloat = 20, intensity: Double = 1.0) -> some View {
        modifier(LiquidGlassStyle(cornerRadius: cornerRadius, intensity: intensity))
    }
}

// MARK: - Glass Button Style (iOS 26)

struct GlassButtonStyle: ButtonStyle {
    var color: Color = .brandPrimary
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background {
                Capsule()
                    .fill(color.opacity(configuration.isPressed ? 0.6 : 0.8))
                    .overlay {
                        Capsule()
                            .stroke(Color.white.opacity(0.2), lineWidth: 1)
                    }
            }
            .foregroundStyle(.white)
            .scaleEffect(configuration.isPressed ? 0.96 : 1.0)
            .animation(.spring(response: 0.3), value: configuration.isPressed)
    }
}

extension ButtonStyle where Self == GlassButtonStyle {
    static var glass: GlassButtonStyle { GlassButtonStyle() }
    static func glass(color: Color) -> GlassButtonStyle {
        GlassButtonStyle(color: color)
    }
}

// MARK: - Verdict Color Helper

enum VerdictColor {
    case safe, warning, danger, unknown
    
    var color: Color {
        switch self {
        case .safe: return .verdictSafe
        case .warning: return .verdictWarning
        case .danger: return .verdictDanger
        case .unknown: return .verdictUnknown
        }
    }
    
    var gradient: LinearGradient {
        LinearGradient(
            colors: [color, color.opacity(0.7)],
            startPoint: .top,
            endPoint: .bottom
        )
    }
}

// MARK: - Hex Color Initializer

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - iOS 26 Mesh Gradient (Liquid Glass Effect)

extension MeshGradient {
    /// Creates a liquid glass mesh gradient background
    static var liquidGlassBackground: MeshGradient {
        MeshGradient(
            width: 3,
            height: 3,
            points: [
                [0.0, 0.0], [0.5, 0.0], [1.0, 0.0],
                [0.0, 0.5], [0.5, 0.5], [1.0, 0.5],
                [0.0, 1.0], [0.5, 1.0], [1.0, 1.0]
            ],
            colors: [
                Color(hex: "0D1117"), Color(hex: "1A1F2E"), Color(hex: "0D1117"),
                Color(hex: "161B22"), Color(hex: "6C5CE7").opacity(0.1), Color(hex: "161B22"),
                Color(hex: "0D1117"), Color(hex: "00D68F").opacity(0.05), Color(hex: "0D1117")
            ]
        )
    }
}

// MARK: - Preview

#Preview("Design System") {
    ScrollView {
        VStack(spacing: 24) {
            // Brand Colors
            HStack(spacing: 12) {
                colorSwatch(.brandPrimary, "Primary")
                colorSwatch(.brandSecondary, "Secondary")
                colorSwatch(.brandAccent, "Accent")
            }
            
            // Verdict Colors
            HStack(spacing: 12) {
                colorSwatch(.verdictSafe, "Safe")
                colorSwatch(.verdictWarning, "Warning")
                colorSwatch(.verdictDanger, "Danger")
            }
            
            // Liquid Glass Demo
            VStack {
                Text("Liquid Glass Effect")
                    .font(.headline)
                    .foregroundStyle(.white)
            }
            .frame(maxWidth: .infinity)
            .padding()
            .liquidGlass()
            
            // Glass Button
            Button("Glass Button") {}
                .buttonStyle(.glass)
        }
        .padding()
    }
    .background(Color.bgDark)
}

private func colorSwatch(_ color: Color, _ name: String) -> some View {
    VStack(spacing: 4) {
        RoundedRectangle(cornerRadius: 12)
            .fill(color)
            .frame(width: 60, height: 60)
        Text(name)
            .font(.caption2)
            .foregroundColor(.textSecondary)
    }
}
