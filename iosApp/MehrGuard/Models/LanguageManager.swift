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

// Models/LanguageManager.swift
// Mehr Guard Language Manager - Runtime language switching
//
// iOS Language Architecture:
// - NSLocalizedString uses Bundle.main which cannot be changed at runtime
// - Setting AppleLanguages requires app restart to take effect
// - For runtime switching, we store preference and prompt restart
// - The app respects iOS system language settings by default

import Foundation
import SwiftUI
#if os(iOS)

/// Manages app language with system integration
/// Note: iOS language changes require app restart to fully take effect
@MainActor
final class LanguageManager: ObservableObject {
    static let shared = LanguageManager()
    
    /// Current language code stored in UserDefaults
    @Published var currentLanguage: String {
        didSet {
            if oldValue != currentLanguage {
                saveLanguagePreference(currentLanguage)
            }
        }
    }
    
    /// Whether a language change is pending (requires restart)
    @Published var pendingRestart: Bool = false
    
    private init() {
        // Load saved language or use system default
        let saved = UserDefaults.standard.string(forKey: "selectedLanguage") ?? "system"
        self.currentLanguage = saved
        
        // Apply AppleLanguages on init for next launch
        applyAppleLanguages(saved)
    }
    
    /// Save language preference and set AppleLanguages for next launch
    private func saveLanguagePreference(_ code: String) {
        UserDefaults.standard.set(code, forKey: "selectedLanguage")
        applyAppleLanguages(code)
        pendingRestart = true
        
        #if DEBUG
        print("🌐 Language preference saved: \(code)")
        print("🌐 App restart required for changes to take effect")
        #endif
    }
    
    /// Set AppleLanguages UserDefaults key for iOS to use on next launch
    private func applyAppleLanguages(_ code: String) {
        if code == "system" {
            // Remove override to use system language
            UserDefaults.standard.removeObject(forKey: "AppleLanguages")
        } else {
            // Set specific language for next launch
            UserDefaults.standard.set([code], forKey: "AppleLanguages")
        }
        UserDefaults.standard.synchronize()
    }
    
    /// Get display name for a language code
    func displayName(for code: String) -> String {
        if code == "system" {
            return NSLocalizedString("settings.language_system", comment: "System Default")
        }
        let locale = Locale(identifier: code)
        return locale.localizedString(forIdentifier: code) ?? code
    }
    
    /// Check if the current system language matches our preference
    var isLanguageApplied: Bool {
        if currentLanguage == "system" {
            return true
        }
        let systemLang = Locale.preferredLanguages.first?.components(separatedBy: "-").first ?? "en"
        return systemLang == currentLanguage
    }
}

#endif
