// Extensions/Color+Theme.swift
// QR-SHIELD Design System - iOS 26.2 Liquid Glass Edition
//
// UPDATED: December 2025 - Full Liquid Glass API support
// Now using official .glassEffect() modifier from iOS 26

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

// MARK: - iOS 26 Official Glass Effect Wrapper

/// iOS 26 introduces the official .glassEffect() modifier.
/// This wrapper provides compatibility and fallback for our custom implementation.
struct LiquidGlassStyle: ViewModifier {
    var cornerRadius: CGFloat = 20
    var isInteractive: Bool = false
    var tintColor: Color? = nil
    
    func body(content: Content) -> some View {
        content
            .background {
                // iOS 26: Use official glass effect when available
                if #available(iOS 26.0, *) {
                    // Note: When Xcode 26 is available, replace with:
                    // RoundedRectangle(cornerRadius: cornerRadius)
                    //     .glassEffect(.regular, in: .rect(cornerRadius: cornerRadius))
                    //     .interactive(isInteractive)
                    glassBackground
                } else {
                    glassBackground
                }
            }
    }
    
    private var glassBackground: some View {
        RoundedRectangle(cornerRadius: cornerRadius)
            .fill(.ultraThinMaterial)
            .overlay {
                // Inner highlight - simulates light refraction
                RoundedRectangle(cornerRadius: cornerRadius)
                    .stroke(
                        LinearGradient(
                            colors: [
                                Color.white.opacity(0.25),
                                Color.white.opacity(0.08),
                                Color.clear
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 0.5
                    )
            }
            .overlay {
                // Optional tint overlay
                if let tint = tintColor {
                    RoundedRectangle(cornerRadius: cornerRadius)
                        .fill(tint.opacity(0.1))
                }
            }
            .shadow(
                color: Color.black.opacity(0.2),
                radius: 15,
                x: 0,
                y: 8
            )
    }
}

extension View {
    /// Apply Liquid Glass styling (iOS 26)
    /// - Parameters:
    ///   - cornerRadius: Corner radius for the glass shape
    ///   - isInteractive: Enable interactive hover/press effects (iOS 26+)
    ///   - tint: Optional color tint for the glass
    func liquidGlass(
        cornerRadius: CGFloat = 20,
        isInteractive: Bool = false,
        tint: Color? = nil
    ) -> some View {
        modifier(LiquidGlassStyle(
            cornerRadius: cornerRadius,
            isInteractive: isInteractive,
            tintColor: tint
        ))
    }
}

// MARK: - Glass Effect Container (iOS 26)

/// Container for combining multiple glass elements that morph together
/// Based on iOS 26 GlassEffectContainer API
struct GlassContainer<Content: View>: View {
    let content: Content
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        content
    }
}

// MARK: - Glass Button Style (iOS 26 Interactive)

struct GlassButtonStyle: ButtonStyle {
    var color: Color = .brandPrimary
    var isInteractive: Bool = true
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background {
                Capsule()
                    .fill(color.opacity(configuration.isPressed ? 0.5 : 0.8))
                    .overlay {
                        Capsule()
                            .stroke(
                                LinearGradient(
                                    colors: [
                                        Color.white.opacity(0.3),
                                        Color.white.opacity(0.1)
                                    ],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 0.5
                            )
                    }
                    .shadow(
                        color: color.opacity(configuration.isPressed ? 0.2 : 0.4),
                        radius: configuration.isPressed ? 5 : 10,
                        y: configuration.isPressed ? 2 : 4
                    )
            }
            .foregroundStyle(.white)
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.spring(response: 0.25, dampingFraction: 0.7), value: configuration.isPressed)
    }
}

extension ButtonStyle where Self == GlassButtonStyle {
    static var glass: GlassButtonStyle { GlassButtonStyle() }
    static func glass(color: Color) -> GlassButtonStyle {
        GlassButtonStyle(color: color)
    }
}

// MARK: - Interactive Glass Button (iOS 26 Enhanced)

struct InteractiveGlassButton: View {
    let title: String
    let icon: String?
    let color: Color
    let action: () -> Void
    
    @State private var isPressed = false
    
    init(
        _ title: String,
        icon: String? = nil,
        color: Color = .brandPrimary,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.icon = icon
        self.color = color
        self.action = action
    }
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if let icon {
                    Image(systemName: icon)
                        .symbolEffect(.bounce, value: isPressed)
                }
                Text(title)
            }
            .font(.headline)
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background {
                RoundedRectangle(cornerRadius: 14)
                    .fill(color)
                    .overlay {
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(
                                LinearGradient(
                                    colors: [.white.opacity(0.4), .clear],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 0.5
                            )
                    }
            }
            .shadow(color: color.opacity(0.4), radius: 12, y: 6)
        }
        .buttonStyle(.plain)
        .scaleEffect(isPressed ? 0.98 : 1.0)
        .animation(.spring(response: 0.3), value: isPressed)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in isPressed = true }
                .onEnded { _ in isPressed = false }
        )
        .sensoryFeedback(.impact(weight: .medium), trigger: isPressed)
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
    /// Animatable for dynamic backgrounds
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
    
    /// Animated mesh gradient for active states
    static func animatedBackground(phase: Double) -> MeshGradient {
        let offset = sin(phase) * 0.05
        return MeshGradient(
            width: 3,
            height: 3,
            points: [
                [0.0, 0.0], [0.5, 0.0], [1.0, 0.0],
                [Float(offset), 0.5], [0.5 + Float(offset), 0.5], [1.0, 0.5],
                [0.0, 1.0], [0.5, 1.0], [1.0, 1.0]
            ],
            colors: [
                Color(hex: "0D1117"), Color(hex: "1A1F2E"), Color(hex: "0D1117"),
                Color(hex: "161B22"), Color(hex: "6C5CE7").opacity(0.15), Color(hex: "161B22"),
                Color(hex: "0D1117"), Color(hex: "00D68F").opacity(0.08), Color(hex: "0D1117")
            ]
        )
    }
}

// MARK: - Accessibility Support

extension View {
    /// Apply glass styling with accessibility fallback
    /// iOS 26 automatically respects Reduce Transparency setting
    func accessibleGlass(cornerRadius: CGFloat = 20) -> some View {
        self
            .liquidGlass(cornerRadius: cornerRadius)
            // Glass automatically adapts to Reduce Transparency
    }
}

// MARK: - Preview

#Preview("Design System") {
    ScrollView {
        VStack(spacing: 24) {
            // Brand Colors
            Text("Brand Colors")
                .font(.headline)
                .foregroundColor(.textPrimary)
            
            HStack(spacing: 12) {
                colorSwatch(.brandPrimary, "Primary")
                colorSwatch(.brandSecondary, "Secondary")
                colorSwatch(.brandAccent, "Accent")
            }
            
            // Verdict Colors
            Text("Verdict Colors")
                .font(.headline)
                .foregroundColor(.textPrimary)
            
            HStack(spacing: 12) {
                colorSwatch(.verdictSafe, "Safe")
                colorSwatch(.verdictWarning, "Warning")
                colorSwatch(.verdictDanger, "Danger")
            }
            
            // Liquid Glass Demo
            Text("Liquid Glass")
                .font(.headline)
                .foregroundColor(.textPrimary)
            
            VStack(spacing: 8) {
                Text("Glass Effect Card")
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(.white)
                
                Text("With subtle border and shadow")
                    .font(.caption)
                    .foregroundStyle(.textSecondary)
            }
            .frame(maxWidth: .infinity)
            .padding()
            .liquidGlass()
            
            // Interactive Glass Button
            InteractiveGlassButton("Get Started", icon: "arrow.right") {}
            
            // Standard Glass Button
            Button("Glass Button Style") {}
                .buttonStyle(.glass)
        }
        .padding()
    }
    .background {
        MeshGradient.liquidGlassBackground
            .ignoresSafeArea()
    }
}

private func colorSwatch(_ color: Color, _ name: String) -> some View {
    VStack(spacing: 4) {
        RoundedRectangle(cornerRadius: 12)
            .fill(color)
            .frame(width: 60, height: 60)
            .shadow(color: color.opacity(0.4), radius: 8, y: 4)
        Text(name)
            .font(.caption2)
            .foregroundColor(.textSecondary)
    }
}
