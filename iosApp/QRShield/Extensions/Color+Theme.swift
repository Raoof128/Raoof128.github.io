// Extensions/Color+Theme.swift
// QR-SHIELD Design System - iOS 17+ Compatible
//
// UPDATED: December 2025
// - Fixed duplicate declarations
// - iOS 17+ compatibility (MeshGradient wrapped for iOS 18+)
// - Liquid Glass styling

import SwiftUI

// MARK: - Brand Colors

extension Color {
    // Primary brand colors
    static let brandPrimary = Color(hex: "6C5CE7")       // Purple
    static let brandSecondary = Color(hex: "00D68F")     // Cyan/Teal
    static let brandAccent = Color(hex: "A855F7")        // Light purple
    
    // Verdict colors
    static let verdictSafe = Color(hex: "00D68F")        // Green
    static let verdictWarning = Color(hex: "F5A623")     // Orange
    static let verdictDanger = Color(hex: "FF5252")      // Red
    static let verdictUnknown = Color(hex: "8B93A1")     // Gray
    
    // Background colors
    static let bgDark = Color(hex: "0D1117")             // Deep dark
    static let bgCard = Color(hex: "161B22")             // Card background
    static let bgSurface = Color(hex: "21262D")          // Surface/elevated
    static let bgGlass = Color.white.opacity(0.05)       // Glass tint
    
    // Text colors
    static let textPrimary = Color.white
    static let textSecondary = Color(hex: "8B949E")
    static let textMuted = Color(hex: "6E7681")
    
    // Liquid Glass colors
    static let liquidGlassTint = Color.white.opacity(0.08)
    static let glassBorder = Color.white.opacity(0.12)
    static let glassHighlight = Color.white.opacity(0.15)
    static let glassShadow = Color.black.opacity(0.25)
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

// MARK: - Brand Gradients

extension LinearGradient {
    static let brandGradient = LinearGradient(
        colors: [Color.brandPrimary, Color.brandSecondary],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    static let liquidGlassOverlay = LinearGradient(
        colors: [
            Color.white.opacity(0.2),
            Color.white.opacity(0.05),
            Color.clear
        ],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    static let glassBorderGradient = LinearGradient(
        colors: [
            Color.white.opacity(0.3),
            Color.white.opacity(0.1),
            Color.clear,
            Color.white.opacity(0.05)
        ],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}

// MARK: - Liquid Glass Background (iOS 17+ Compatible)

struct LiquidGlassBackground: View {
    var body: some View {
        ZStack {
            // Base dark gradient
            LinearGradient(
                colors: [
                    Color(hex: "0D1117"),
                    Color(hex: "161B22"),
                    Color(hex: "0D1117")
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            
            // Animated accent overlays
            Circle()
                .fill(Color.brandPrimary.opacity(0.15))
                .frame(width: 300, height: 300)
                .blur(radius: 100)
                .offset(x: -100, y: -200)
            
            Circle()
                .fill(Color.brandSecondary.opacity(0.1))
                .frame(width: 250, height: 250)
                .blur(radius: 80)
                .offset(x: 150, y: 300)
            
            Circle()
                .fill(Color.brandAccent.opacity(0.08))
                .frame(width: 200, height: 200)
                .blur(radius: 60)
                .offset(x: 100, y: -100)
        }
    }
}

// MARK: - Glass Background Extension

extension View {
    /// Applies a liquid glass background that works on iOS 17+
    @ViewBuilder
    func liquidGlassBackground() -> some View {
        self.background {
            LiquidGlassBackground()
                .ignoresSafeArea()
        }
    }
}

// MARK: - Liquid Glass View Modifier

struct LiquidGlassStyle: ViewModifier {
    var cornerRadius: CGFloat = 16
    var opacity: Double = 1.0
    
    func body(content: Content) -> some View {
        content
            .background {
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(.ultraThinMaterial)
                    .opacity(opacity)
            }
            .overlay {
                RoundedRectangle(cornerRadius: cornerRadius)
                    .stroke(
                        LinearGradient(
                            colors: [
                                Color.white.opacity(0.25),
                                Color.white.opacity(0.1),
                                Color.clear,
                                Color.white.opacity(0.05)
                            ],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        ),
                        lineWidth: 1
                    )
            }
            .shadow(color: Color.black.opacity(0.15), radius: 10, x: 0, y: 5)
    }
}

extension View {
    func liquidGlass(cornerRadius: CGFloat = 16, opacity: Double = 1.0) -> some View {
        modifier(LiquidGlassStyle(cornerRadius: cornerRadius, opacity: opacity))
    }
}

// MARK: - Glass Button Style

struct GlassButtonStyle: ButtonStyle {
    var color: Color = .brandPrimary
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background {
                RoundedRectangle(cornerRadius: 12)
                    .fill(color.opacity(configuration.isPressed ? 0.6 : 0.8))
                    .overlay {
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(Color.white.opacity(0.2), lineWidth: 1)
                    }
            }
            .foregroundColor(.white)
            .scaleEffect(configuration.isPressed ? 0.97 : 1.0)
            .animation(.spring(response: 0.3), value: configuration.isPressed)
    }
}

extension ButtonStyle where Self == GlassButtonStyle {
    static var glass: GlassButtonStyle { GlassButtonStyle() }
    static func glass(color: Color) -> GlassButtonStyle { GlassButtonStyle(color: color) }
}

// MARK: - Interactive Glass Button Component

struct InteractiveGlassButton: View {
    let title: String
    let icon: String
    var color: Color = .brandPrimary
    let action: () -> Void
    
    init(_ title: String, icon: String, color: Color = .brandPrimary, action: @escaping () -> Void) {
        self.title = title
        self.icon = icon
        self.color = color
        self.action = action
    }
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .symbolEffect(.bounce, value: UUID())
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
                                    colors: [.white.opacity(0.3), .clear],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1
                            )
                    }
            }
            .shadow(color: color.opacity(0.4), radius: 10, y: 4)
        }
        .sensoryFeedback(.impact(weight: .medium), trigger: UUID())
    }
}

// MARK: - Preview

#Preview("Design System") {
    VStack(spacing: 20) {
        Text("QR-SHIELD")
            .font(.largeTitle.bold())
            .foregroundStyle(LinearGradient.brandGradient)
        
        HStack(spacing: 16) {
            Circle().fill(Color.verdictSafe).frame(width: 40)
            Circle().fill(Color.verdictWarning).frame(width: 40)
            Circle().fill(Color.verdictDanger).frame(width: 40)
        }
        
        Text("Liquid Glass Card")
            .padding()
            .liquidGlass()
        
        InteractiveGlassButton("Get Started", icon: "arrow.right") {}
    }
    .padding()
    .liquidGlassBackground()
}

