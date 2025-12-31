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

// Extensions/Localization+Extension.swift
// String localization helpers for Mehr Guard iOS

import Foundation

extension String {
    /// Returns the localized version of this string key.
    /// Falls back to the key itself if no translation is found.
    var localized: String {
        NSLocalizedString(self, comment: "")
    }
    
    /// Returns the localized version with format arguments.
    func localized(_ args: CVarArg...) -> String {
        String(format: NSLocalizedString(self, comment: ""), arguments: args)
    }
}

// MARK: - Static Localization Keys

/// Type-safe localization keys for the app
enum L10n {
    // App General
    static var appName: String { "app.name".localized }
    static var appTagline: String { "app.tagline".localized }
    
    // Dashboard
    static var enterpriseBadge: String { "dashboard.enterprise_badge".localized }
    static var heroTagline: String { "dashboard.hero.tagline".localized }
    static var heroTagline2: String { "dashboard.hero.tagline2".localized }
    static var analyze: String { "dashboard.analyze".localized }
    static var scanQR: String { "dashboard.scan_qr".localized }
    static var importImage: String { "dashboard.import_image".localized }
    static var systemOptimal: String { "dashboard.system_optimal".localized }
    static var engineStatus: String { "dashboard.engine_status".localized }
    static var threatsBlocked: String { "dashboard.threats_blocked".localized }
    static var safeScans: String { "dashboard.safe_scans".localized }
    static var engineFeatures: String { "dashboard.engine_features".localized }
    static var recentScans: String { "dashboard.recent_scans".localized }
    static var viewAll: String { "dashboard.view_all".localized }
    static var noScansYet: String { "dashboard.no_scans_yet".localized }
    static var noScansMessage: String { "dashboard.no_scans_message".localized }
    static var threatDatabase: String { "dashboard.threat_database".localized }
    static var dbUpdated: String { "dashboard.db_updated".localized }
}
