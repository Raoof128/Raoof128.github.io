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

// Models/MockTypes.swift
// QR-SHIELD Mock Types
//
// These mock types are used for UI development until KMP is integrated.
// Replace with actual KMP types (common.Verdict, common.RiskAssessment) when ready.

import Foundation

// MARK: - Verdict Mock

/// Mock verdict type - Replace with common.Verdict from KMP
enum VerdictMock: String, CaseIterable, Codable, Sendable {
    case safe = "SAFE"
    case suspicious = "SUSPICIOUS"
    case malicious = "MALICIOUS"
    case unknown = "UNKNOWN"
    
    var icon: String {
        switch self {
        case .safe: return "checkmark.shield.fill"
        case .suspicious: return "exclamationmark.shield.fill"
        case .malicious: return "xmark.shield.fill"
        case .unknown: return "questionmark.circle"
        }
    }
    
    var displayName: String {
        rawValue.capitalized
    }
    
    /// Bridge from KMP Verdict to VerdictMock
    /// Used when KMP framework is integrated
    #if canImport(common)
    static func from(_ verdict: common.Verdict) -> VerdictMock {
        switch verdict {
        case .safe: return .safe
        case .suspicious: return .suspicious
        case .malicious: return .malicious
        default: return .unknown
        }
    }
    #else
    /// Fallback for development without KMP
    static func from(_ verdict: Any) -> VerdictMock {
        return .unknown
    }
    #endif
}

// MARK: - Risk Assessment Mock

/// Mock risk assessment - Replace with common.RiskAssessment from KMP
struct RiskAssessmentMock: Identifiable, Sendable {
    let id: UUID
    let score: Int
    let verdict: VerdictMock
    let flags: [String]
    let confidence: Double
    let url: String
    let scannedAt: Date
    
    init(
        id: UUID = UUID(),
        score: Int,
        verdict: VerdictMock,
        flags: [String],
        confidence: Double,
        url: String,
        scannedAt: Date = Date()
    ) {
        self.id = id
        self.score = score
        self.verdict = verdict
        self.flags = flags
        self.confidence = confidence
        self.url = url
        self.scannedAt = scannedAt
    }
    
    var formattedDate: String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: scannedAt, relativeTo: Date())
    }
    
    var shortDate: String {
        let formatter = DateFormatter()
        formatter.dateStyle = .short
        formatter.timeStyle = .short
        return formatter.string(from: scannedAt)
    }
}

// MARK: - History Item Mock

/// Mock history item for history list
struct HistoryItemMock: Identifiable, Sendable, Hashable, Codable {
    let id: String
    let url: String
    let score: Int
    let verdict: VerdictMock
    let scannedAt: Date
    let domain: String
    
    init(
        id: String = UUID().uuidString,
        url: String,
        score: Int = 0,
        verdict: VerdictMock,
        scannedAt: Date = Date(),
        domain: String? = nil
    ) {
        self.id = id
        self.url = url
        self.score = score
        self.verdict = verdict
        self.scannedAt = scannedAt
        self.domain = domain ?? (URL(string: url)?.host ?? url)
    }
    
    var formattedDate: String {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .abbreviated
        return formatter.localizedString(for: scannedAt, relativeTo: Date())
    }
    
    // Hashable conformance
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
    
    static func == (lhs: HistoryItemMock, rhs: HistoryItemMock) -> Bool {
        lhs.id == rhs.id
    }
}
