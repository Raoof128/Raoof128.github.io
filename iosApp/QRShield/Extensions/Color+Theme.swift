// Extensions/Color+Theme.swift
// QR-SHIELD Design System for iOS
//
// Based on UI_DESIGN_SYSTEM.md specifications
// Provides consistent theming across the iOS application

import SwiftUI

extension Color {
    // MARK: - Brand Colors
    
    /// Primary brand purple - #6C5CE7
    static let brandPrimary = Color(red: 0.42, green: 0.36, blue: 0.91)
    
    /// Secondary brand teal - #00CEC9
    static let brandSecondary = Color(red: 0.0, green: 0.81, blue: 0.79)
    
    // MARK: - Verdict Colors
    
    /// Safe verdict green - #00D68F
    static let verdictSafe = Color(red: 0.0, green: 0.84, blue: 0.56)
    
    /// Warning/Suspicious verdict amber - #FFAA00
    static let verdictWarning = Color(red: 1.0, green: 0.67, blue: 0.0)
    
    /// Danger/Malicious verdict red - #FF3D71
    static let verdictDanger = Color(red: 1.0, green: 0.24, blue: 0.44)
    
    /// Unknown verdict gray
    static let verdictUnknown = Color(red: 0.55, green: 0.58, blue: 0.62)
    
    // MARK: - Background Colors
    
    /// Dark background - #0D1117
    static let bgDark = Color(red: 0.05, green: 0.07, blue: 0.09)
    
    /// Card background - #161B22
    static let bgCard = Color(red: 0.09, green: 0.11, blue: 0.13)
    
    /// Surface background - #21262D
    static let bgSurface = Color(red: 0.13, green: 0.15, blue: 0.18)
    
    // MARK: - Text Colors
    
    /// Primary text color
    static let textPrimary = Color.white
    
    /// Secondary text color
    static let textSecondary = Color.white.opacity(0.7)
    
    /// Muted text color
    static let textMuted = Color.white.opacity(0.5)
}

// MARK: - Verdict Helpers

enum VerdictColor {
    case safe, suspicious, malicious, unknown
    
    var color: Color {
        switch self {
        case .safe: return .verdictSafe
        case .suspicious: return .verdictWarning
        case .malicious: return .verdictDanger
        case .unknown: return .verdictUnknown
        }
    }
    
    var iconName: String {
        switch self {
        case .safe: return "checkmark.shield.fill"
        case .suspicious: return "exclamationmark.shield.fill"
        case .malicious: return "xmark.shield.fill"
        case .unknown: return "questionmark.circle"
        }
    }
    
    var emoji: String {
        switch self {
        case .safe: return "✅"
        case .suspicious: return "⚠️"
        case .malicious: return "🚨"
        case .unknown: return "❓"
        }
    }
}

// MARK: - Gradient Helpers

extension LinearGradient {
    /// Primary brand gradient
    static let brandGradient = LinearGradient(
        gradient: Gradient(colors: [.brandPrimary, .brandSecondary]),
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
    
    /// Safe verdict gradient
    static let safeGradient = LinearGradient(
        gradient: Gradient(colors: [.verdictSafe, .verdictSafe.opacity(0.7)]),
        startPoint: .top,
        endPoint: .bottom
    )
    
    /// Danger verdict gradient  
    static let dangerGradient = LinearGradient(
        gradient: Gradient(colors: [.verdictDanger, .verdictDanger.opacity(0.7)]),
        startPoint: .top,
        endPoint: .bottom
    )
}
