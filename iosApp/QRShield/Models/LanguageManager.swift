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

// Models/LanguageManager.swift
// QR-SHIELD Language Manager - Runtime language switching

import Foundation
import SwiftUI
#if os(iOS)

/// Manages app language with runtime switching capability
@MainActor
final class LanguageManager: ObservableObject {
    static let shared = LanguageManager()
    
    /// Current language code
    @Published var currentLanguage: String {
        didSet {
            if oldValue != currentLanguage {
                applyLanguage(currentLanguage)
                UserDefaults.standard.set(currentLanguage, forKey: "selectedLanguage")
            }
        }
    }
    
    /// Custom bundle for localization
    private(set) var bundle: Bundle = .main
    
    private init() {
        // Load saved language or use system default
        let saved = UserDefaults.standard.string(forKey: "selectedLanguage") ?? "system"
        self.currentLanguage = saved
        applyLanguage(saved)
    }
    
    /// Apply a language change
    func applyLanguage(_ code: String) {
        let languageCode: String
        
        if code == "system" {
            // Use system language
            languageCode = Locale.preferredLanguages.first?.components(separatedBy: "-").first ?? "en"
        } else {
            languageCode = code
        }
        
        // Find the bundle for this language
        if let path = Bundle.main.path(forResource: languageCode, ofType: "lproj"),
           let langBundle = Bundle(path: path) {
            bundle = langBundle
        } else if let path = Bundle.main.path(forResource: "en", ofType: "lproj"),
                  let fallbackBundle = Bundle(path: path) {
            // Fallback to English
            bundle = fallbackBundle
        } else {
            bundle = .main
        }
        
        // Also set AppleLanguages for system integration
        if code == "system" {
            UserDefaults.standard.removeObject(forKey: "AppleLanguages")
        } else {
            UserDefaults.standard.set([code], forKey: "AppleLanguages")
        }
        
        #if DEBUG
        print("🌐 Language applied: \(code) -> bundle: \(bundle.bundlePath)")
        #endif
    }
    
    /// Get localized string using current language bundle
    nonisolated func localized(_ key: String) -> String {
        // Access bundle safely without MainActor isolation requirement
        Bundle.main.localizedString(forKey: key, value: nil, table: nil)
    }
    
    /// Get localized string with format arguments
    nonisolated func localized(_ key: String, _ args: CVarArg...) -> String {
        String(format: localized(key), arguments: args)
    }
}

#endif
