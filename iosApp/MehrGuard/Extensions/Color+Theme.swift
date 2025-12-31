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

// Extensions/Color+Theme.swift
// Mehr Guard Design System - iOS 17+ Compatible
//
// UPDATED: December 2025
// - Fixed duplicate declarations
// - iOS 17+ compatibility (MeshGradient wrapped for iOS 18+)
// - Liquid Glass styling

import SwiftUI
#if os(iOS)

// MARK: - Brand Colors (Adaptive Light/Dark Mode)

extension Color {
    // Primary brand colors - Royal Blue from HTML designs
    static let brandPrimary = Color(hex: "2563EB")       // Royal Blue
    static let brandSecondary = Color(hex: "10B981")     // Emerald/Teal
    static let brandAccent = Color(hex: "8B5CF6")        // Violet
    
    // Verdict colors - iOS-style vibrant colors
    static let verdictSafe = Color(hex: "34C759")        // iOS Green
    static let verdictWarning = Color(hex: "FF9500")     // iOS Orange
    static let verdictDanger = Color(hex: "FF3B30")      // iOS Red
    static let verdictUnknown = Color(hex: "8E8E93")     // iOS Gray
    
    // MARK: - Adaptive Background Colors
    
    /// Main background - Light: soft blue-gray, Dark: deep navy
    static var bgMain: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color(hex: "0B1120"))  // Deep dark blue
                : UIColor(Color(hex: "F2F2F7"))  // iOS system gray 6
        })
    }
    
    /// Card/Panel background - Light: white with opacity, Dark: slate
    static var bgCard: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color(hex: "1E293B").opacity(0.7))  // Slate 800
                : UIColor(Color.white.opacity(0.7))           // White glass
        })
    }
    
    /// Surface background - Light: white, Dark: elevated dark
    static var bgSurface: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color(hex: "334155"))  // Slate 700
                : UIColor(Color.white.opacity(0.9))
        })
    }
    
    /// Glass tint for overlays
    static var bgGlass: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color.white.opacity(0.05))
                : UIColor(Color.white.opacity(0.6))
        })
    }
    
    // Legacy static backgrounds (for compatibility)
    static let bgDark = Color(hex: "0B1120")
    static let bgLight = Color(hex: "F2F2F7")
    
    // MARK: - Adaptive Text Colors
    
    /// Primary text - Light: dark slate, Dark: white
    static var textPrimary: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor.white
                : UIColor(Color(hex: "1C1C1E"))  // System label
        })
    }
    
    /// Secondary text - Light: gray, Dark: light gray
    static var textSecondary: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color(hex: "9CA3AF"))  // Gray 400
                : UIColor(Color(hex: "6B7280"))  // Gray 500
        })
    }
    
    /// Muted text - Light: lighter gray, Dark: dark gray
    static var textMuted: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color(hex: "6B7280"))  // Gray 500
                : UIColor(Color(hex: "9CA3AF"))  // Gray 400
        })
    }
    
    // MARK: - Glass Panel Colors (Adaptive)
    
    /// Glass border color
    static var glassBorder: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color.white.opacity(0.1))
                : UIColor(Color.white.opacity(0.6))
        })
    }
    
    /// Glass highlight
    static var glassHighlight: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color.white.opacity(0.15))
                : UIColor(Color.white.opacity(0.8))
        })
    }
    
    /// Glass shadow
    static var glassShadow: Color {
        Color(UIColor { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(Color.black.opacity(0.3))
                : UIColor(Color.black.opacity(0.08))
        })
    }
    
    // Legacy static values
    static let liquidGlassTint = Color.white.opacity(0.08)
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

// MARK: - Liquid Glass Background (iOS 17+ Compatible, Light/Dark Mode)

struct LiquidGlassBackground: View {
    @AppStorage("liquidGlassReduced") private var liquidGlassReduced = false
    @Environment(\.colorScheme) private var colorScheme
    
    var body: some View {
        ZStack {
            // Base gradient - adapts to color scheme
            if colorScheme == .dark {
                // Dark mode: Deep navy gradient
                LinearGradient(
                    colors: [
                        Color(hex: "0B1120"),
                        Color(hex: "1E293B"),
                        Color(hex: "0B1120")
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            } else {
                // Light mode: Soft blue-gray gradient matching HTML designs
                LinearGradient(
                    colors: [
                        Color(hex: "F0F4F8"),
                        Color(hex: "E5E7EB"),
                        Color(hex: "F3F4F6")
                    ],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            }
            
            // Only show animated effects if not reduced
            if !liquidGlassReduced {
                // Animated accent overlays - adjust opacity for light mode
                Circle()
                    .fill(Color.brandPrimary.opacity(colorScheme == .dark ? 0.15 : 0.12))
                    .frame(width: 300, height: 300)
                    .blur(radius: 100)
                    .offset(x: -100, y: -200)
                
                Circle()
                    .fill(Color.brandSecondary.opacity(colorScheme == .dark ? 0.1 : 0.08))
                    .frame(width: 250, height: 250)
                    .blur(radius: 80)
                    .offset(x: 150, y: 300)
                
                // Light mode: Add soft purple accent like HTML designs
                Circle()
                    .fill(Color(hex: colorScheme == .dark ? "8B5CF6" : "C4B5FD").opacity(colorScheme == .dark ? 0.08 : 0.3))
                    .frame(width: 200, height: 200)
                    .blur(radius: 60)
                    .offset(x: 100, y: -100)
            }
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

// MARK: - Liquid Glass View Modifier (Adaptive Light/Dark)

struct LiquidGlassStyle: ViewModifier {
    var cornerRadius: CGFloat = 16
    var opacity: Double = 1.0
    @AppStorage("liquidGlassReduced") private var liquidGlassReduced = false
    @Environment(\.colorScheme) private var colorScheme
    
    func body(content: Content) -> some View {
        content
            .background {
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(colorScheme == .dark ? .ultraThinMaterial : .regularMaterial)
                    .opacity(opacity)
            }
            .overlay {
                if !liquidGlassReduced {
                    RoundedRectangle(cornerRadius: cornerRadius)
                        .stroke(
                            LinearGradient(
                                colors: colorScheme == .dark
                                    ? [
                                        Color.white.opacity(0.25),
                                        Color.white.opacity(0.1),
                                        Color.clear,
                                        Color.white.opacity(0.05)
                                    ]
                                    : [
                                        Color.white.opacity(0.8),
                                        Color.white.opacity(0.5),
                                        Color.white.opacity(0.3),
                                        Color.white.opacity(0.6)
                                    ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 1
                        )
                } else {
                    RoundedRectangle(cornerRadius: cornerRadius)
                        .stroke(
                            colorScheme == .dark 
                                ? Color.white.opacity(0.1) 
                                : Color.black.opacity(0.05),
                            lineWidth: 0.5
                        )
                }
            }
            .shadow(
                color: liquidGlassReduced 
                    ? .clear 
                    : (colorScheme == .dark ? Color.black.opacity(0.2) : Color.black.opacity(0.06)),
                radius: liquidGlassReduced ? 0 : (colorScheme == .dark ? 10 : 8),
                x: 0,
                y: liquidGlassReduced ? 0 : (colorScheme == .dark ? 5 : 3)
            )
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
    
    @State private var tapCount = 0
    
    init(_ title: String, icon: String, color: Color = .brandPrimary, action: @escaping () -> Void) {
        self.title = title
        self.icon = icon
        self.color = color
        self.action = action
    }
    
    var body: some View {
        Button {
            tapCount += 1
            action()
        } label: {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .symbolEffect(.bounce, value: tapCount)
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
        .sensoryFeedback(.impact(weight: .medium), trigger: tapCount)
        .accessibilityLabel(Text(title))
    }
}

#endif
