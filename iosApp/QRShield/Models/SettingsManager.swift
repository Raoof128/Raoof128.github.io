//
// Copyright 2024 QR-SHIELD Contributors
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

// Models/SettingsManager.swift
// QR-SHIELD Settings Manager - Centralized Settings Access
//
// Provides a centralized way to access user preferences throughout the app.
// All settings are persisted via UserDefaults through @AppStorage.

#if os(iOS)
import Foundation
import SwiftUI
import AVFoundation

// MARK: - Settings Manager

/// Singleton manager for app-wide settings access
/// Uses @Observable for SwiftUI integration
@available(iOS 17, *)
@Observable
@MainActor
final class SettingsManager {
    
    // MARK: - Singleton
    
    static let shared = SettingsManager()
    
    // MARK: - Settings Keys
    
    private enum Keys {
        static let hapticEnabled = "hapticEnabled"
        static let soundEnabled = "soundEnabled"
        static let autoScan = "autoScan"
        static let saveHistory = "saveHistory"
        static let liquidGlassReduced = "liquidGlassReduced"
        static let notificationsEnabled = "notificationsEnabled"
        static let useDarkMode = "useDarkMode"
    }
    
    // MARK: - Settings Properties
    
    var hapticEnabled: Bool {
        get { UserDefaults.standard.bool(forKey: Keys.hapticEnabled) }
        set { UserDefaults.standard.set(newValue, forKey: Keys.hapticEnabled) }
    }
    
    var soundEnabled: Bool {
        get { UserDefaults.standard.bool(forKey: Keys.soundEnabled) }
        set { UserDefaults.standard.set(newValue, forKey: Keys.soundEnabled) }
    }
    
    var autoScan: Bool {
        get { UserDefaults.standard.bool(forKey: Keys.autoScan) }
        set { UserDefaults.standard.set(newValue, forKey: Keys.autoScan) }
    }
    
    var saveHistory: Bool {
        get { UserDefaults.standard.bool(forKey: Keys.saveHistory) }
        set { UserDefaults.standard.set(newValue, forKey: Keys.saveHistory) }
    }
    
    var liquidGlassReduced: Bool {
        get { UserDefaults.standard.bool(forKey: Keys.liquidGlassReduced) }
        set { UserDefaults.standard.set(newValue, forKey: Keys.liquidGlassReduced) }
    }
    
    var notificationsEnabled: Bool {
        get { UserDefaults.standard.bool(forKey: Keys.notificationsEnabled) }
        set { UserDefaults.standard.set(newValue, forKey: Keys.notificationsEnabled) }
    }
    
    var useDarkMode: Bool {
        get { UserDefaults.standard.bool(forKey: Keys.useDarkMode) }
        set { UserDefaults.standard.set(newValue, forKey: Keys.useDarkMode) }
    }
    
    // MARK: - Initialization
    
    private init() {
        // Register default values
        UserDefaults.standard.register(defaults: [
            Keys.hapticEnabled: true,
            Keys.soundEnabled: true,
            Keys.autoScan: true,
            Keys.saveHistory: true,
            Keys.liquidGlassReduced: false,
            Keys.notificationsEnabled: true,
            Keys.useDarkMode: true
        ])
    }
    
    // MARK: - Haptic Feedback
    
    /// Trigger haptic feedback if enabled in settings
    func triggerHaptic(_ type: HapticType) {
        guard hapticEnabled else { return }
        
        switch type {
        case .light:
            let generator = UIImpactFeedbackGenerator(style: .light)
            generator.prepare()
            generator.impactOccurred()
        case .medium:
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.prepare()
            generator.impactOccurred()
        case .heavy:
            let generator = UIImpactFeedbackGenerator(style: .heavy)
            generator.prepare()
            generator.impactOccurred()
        case .success:
            let generator = UINotificationFeedbackGenerator()
            generator.prepare()
            generator.notificationOccurred(.success)
        case .warning:
            let generator = UINotificationFeedbackGenerator()
            generator.prepare()
            generator.notificationOccurred(.warning)
        case .error:
            let generator = UINotificationFeedbackGenerator()
            generator.prepare()
            generator.notificationOccurred(.error)
        case .selection:
            let generator = UISelectionFeedbackGenerator()
            generator.prepare()
            generator.selectionChanged()
        }
    }
    
    // MARK: - Sound Feedback
    
    /// Play sound feedback if enabled in settings
    func playSound(_ type: SoundType) {
        guard soundEnabled else { return }
        
        // Use system sounds
        switch type {
        case .scan:
            AudioServicesPlaySystemSound(1057) // Keyboard tap
        case .success:
            AudioServicesPlaySystemSound(1025) // Success sound
        case .warning:
            AudioServicesPlaySystemSound(1053) // Alert sound
        case .error:
            AudioServicesPlaySystemSound(1073) // Error sound
        }
    }
    
    // MARK: - Notifications
    
    /// Request notification permission if enabled
    func requestNotificationPermission() async -> Bool {
        guard notificationsEnabled else { return false }
        
        let center = UNUserNotificationCenter.current()
        do {
            return try await center.requestAuthorization(options: [.alert, .badge, .sound])
        } catch {
            #if DEBUG
            print("❌ Failed to request notification permission: \(error)")
            #endif
            return false
        }
    }
    
    /// Show local notification for security alert
    func showSecurityNotification(title: String, body: String, verdict: VerdictMock) async {
        guard notificationsEnabled else { return }
        
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        
        // Add category based on verdict
        switch verdict {
        case .malicious:
            content.categoryIdentifier = "MALICIOUS_ALERT"
        case .suspicious:
            content.categoryIdentifier = "SUSPICIOUS_ALERT"
        default:
            content.categoryIdentifier = "GENERAL_ALERT"
        }
        
        let request = UNNotificationRequest(
            identifier: UUID().uuidString,
            content: content,
            trigger: nil
        )
        
        do {
            try await UNUserNotificationCenter.current().add(request)
        } catch {
            #if DEBUG
            print("❌ Failed to send notification: \(error)")
            #endif
        }
    }
}

// MARK: - Haptic Types

enum HapticType {
    case light
    case medium
    case heavy
    case success
    case warning
    case error
    case selection
}

// MARK: - Sound Types

enum SoundType {
    case scan
    case success
    case warning
    case error
}

// MARK: - Import AudioToolbox for system sounds

import AudioToolbox
import UserNotifications
#endif
