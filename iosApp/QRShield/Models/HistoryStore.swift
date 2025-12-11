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

// Models/HistoryStore.swift
// QR-SHIELD History Storage - Persistent Scan History
//
// Provides persistent storage for scan history using UserDefaults.
// Respects the "saveHistory" user preference.

#if os(iOS)
import Foundation
import SwiftUI

// MARK: - History Store

/// Singleton manager for scan history persistence
@available(iOS 17, *)
@Observable
@MainActor
final class HistoryStore {
    
    // MARK: - Singleton
    
    static let shared = HistoryStore()
    
    // MARK: - Storage Key
    
    private let storageKey = "scanHistory"
    
    // MARK: - History Data
    
    private(set) var items: [HistoryItemMock] = []
    
    // MARK: - Initialization
    
    private init() {
        loadHistory()
    }
    
    // MARK: - Public Methods
    
    /// Add a new scan result to history (if saving is enabled)
    func addScan(_ assessment: RiskAssessmentMock) {
        guard SettingsManager.shared.saveHistory else {
            #if DEBUG
            print("📋 History save disabled - not saving scan")
            #endif
            return
        }
        
        let item = HistoryItemMock(
            id: UUID().uuidString,
            url: assessment.url,
            score: assessment.score,
            verdict: assessment.verdict,
            scannedAt: assessment.scannedAt
        )
        
        // Add to beginning (most recent first)
        items.insert(item, at: 0)
        
        // Limit to 100 items
        if items.count > 100 {
            items = Array(items.prefix(100))
        }
        
        saveHistory()
        
        #if DEBUG
        print("📋 Saved scan to history: \(assessment.url)")
        #endif
    }
    
    /// Remove a specific item from history
    func delete(_ item: HistoryItemMock) {
        items.removeAll { $0.id == item.id }
        saveHistory()
        SettingsManager.shared.triggerHaptic(.light)
    }
    
    /// Clear all history
    func clearAll() {
        items = []  // Explicit assignment to trigger @Observable
        saveHistory()
        SettingsManager.shared.triggerHaptic(.warning)
        
        #if DEBUG
        print("📋 Cleared all history - count now: \(items.count)")
        #endif
    }
    
    /// Get history count
    var count: Int {
        items.count
    }
    
    /// Check if history is empty
    var isEmpty: Bool {
        items.isEmpty
    }
    
    // MARK: - Filtering & Sorting
    
    /// Filter history by verdict
    func filtered(by verdict: VerdictMock?) -> [HistoryItemMock] {
        guard let verdict = verdict else { return items }
        return items.filter { $0.verdict == verdict }
    }
    
    /// Search history by URL
    func search(query: String) -> [HistoryItemMock] {
        guard !query.isEmpty else { return items }
        return items.filter { $0.url.localizedCaseInsensitiveContains(query) }
    }
    
    /// Sort by date (newest first)
    func sortedByDate() -> [HistoryItemMock] {
        items.sorted { $0.scannedAt > $1.scannedAt }
    }
    
    /// Sort by risk score (highest first)
    func sortedByRisk() -> [HistoryItemMock] {
        items.sorted { $0.score > $1.score }
    }
    
    // MARK: - Export
    
    /// Export history as JSON string
    func exportAsJSON() -> String? {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = .prettyPrinted
        
        guard let data = try? encoder.encode(items) else { return nil }
        return String(data: data, encoding: .utf8)
    }
    
    /// Export history as shareable text
    func exportAsText() -> String {
        var text = "🛡️ QR-SHIELD Scan History\n"
        text += "Exported: \(Date().formatted())\n"
        text += "Total Scans: \(items.count)\n"
        text += String(repeating: "-", count: 40) + "\n\n"
        
        for item in items {
            text += "[\(item.verdict.rawValue)] \(item.url)\n"
            text += "  Risk Score: \(item.score)/100\n"
            text += "  Scanned: \(item.formattedDate)\n\n"
        }
        
        return text
    }
    
    // MARK: - Persistence
    
    private func loadHistory() {
        guard let data = UserDefaults.standard.data(forKey: storageKey) else {
            // For production, start with an empty history. In DEBUG builds we seed mock data.
            #if DEBUG
            loadMockData()
            #else
            items = []
            #endif
            return
        }
        
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        
        if let decoded = try? decoder.decode([HistoryItemMock].self, from: data) {
            items = decoded
            #if DEBUG
            print("📋 Loaded \(items.count) history items")
            #endif
        } else {
            #if DEBUG
            loadMockData()
            #else
            items = []
            #endif
        }
    }
    
    private func saveHistory() {
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        
        if let data = try? encoder.encode(items) {
            UserDefaults.standard.set(data, forKey: storageKey)
        }
    }
    
    private func loadMockData() {
        items = [
            HistoryItemMock(id: "1", url: "https://google.com", score: 12, verdict: .safe, scannedAt: Date()),
            HistoryItemMock(id: "2", url: "https://suspicious-link.xyz", score: 55, verdict: .suspicious, scannedAt: Date().addingTimeInterval(-3600)),
            HistoryItemMock(id: "3", url: "https://apple.com/store", score: 8, verdict: .safe, scannedAt: Date().addingTimeInterval(-7200)),
            HistoryItemMock(id: "4", url: "https://phishing-attack.com/login", score: 92, verdict: .malicious, scannedAt: Date().addingTimeInterval(-86400)),
            HistoryItemMock(id: "5", url: "https://amazon.com/product", score: 5, verdict: .safe, scannedAt: Date().addingTimeInterval(-172800)),
            HistoryItemMock(id: "6", url: "http://bank-verify.com/secure", score: 78, verdict: .malicious, scannedAt: Date().addingTimeInterval(-259200))
        ]
        saveHistory()
    }
}

#endif
