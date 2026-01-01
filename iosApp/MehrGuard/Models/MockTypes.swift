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

#if os(iOS)
// Models/MockTypes.swift
// Mehr Guard Mock Types
//
// These mock types are used for UI development until KMP is integrated.
// Replace with actual KMP types (common.Verdict, common.RiskAssessment) when ready.

import Foundation
import SwiftUI

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
    
    /// Alias for formattedDate
    var relativeDate: String {
        formattedDate
    }
    
    // Hashable conformance
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
    
    static func == (lhs: HistoryItemMock, rhs: HistoryItemMock) -> Bool {
        lhs.id == rhs.id
    }
    
    /// Converts HistoryItemMock to RiskAssessmentMock for display in ScanResultView
    /// The flags will be re-analyzed by the engine when ScanResultView loads
    func toRiskAssessment() -> RiskAssessmentMock {
        RiskAssessmentMock(
            id: UUID(uuidString: id) ?? UUID(),
            score: score,
            verdict: verdict,
            flags: [],  // Will be populated by re-analysis in ScanResultView
            confidence: Double(score) / 100.0,
            url: url,
            scannedAt: scannedAt
        )
    }
}

// MARK: - Red Team Scenario

/// A red team test scenario with a description and malicious URL.
struct RedTeamScenario: Identifiable {
    let id: String
    let category: String
    let title: String
    let description: String
    let maliciousUrl: String
    let targetBrand: String?
    let expectedScore: ClosedRange<Int>
    
    init(
        id: String,
        category: String,
        title: String,
        description: String,
        maliciousUrl: String,
        targetBrand: String? = nil,
        expectedScore: ClosedRange<Int> = 50...100
    ) {
        self.id = id
        self.category = category
        self.title = title
        self.description = description
        self.maliciousUrl = maliciousUrl
        self.targetBrand = targetBrand
        self.expectedScore = expectedScore
    }
    
    /// Returns the emoji icon for this scenario's category
    var categoryIcon: String {
        switch category.lowercased() {
        case let c where c.contains("homograph"):
            return "🔤"
        case let c where c.contains("ip"):
            return "🔢"
        case let c where c.contains("tld"):
            return "🌐"
        case let c where c.contains("redirect"):
            return "↪️"
        case let c where c.contains("brand"):
            return "🏷️"
        case let c where c.contains("shortener"):
            return "🔗"
        case let c where c.contains("safe"):
            return "✅"
        default:
            return "⚠️"
        }
    }
    
    /// Returns the color for this scenario's category
    var categoryColor: Color {
        switch category.lowercased() {
        case let c where c.contains("homograph"):
            return .red
        case let c where c.contains("ip"):
            return .orange
        case let c where c.contains("tld"):
            return .pink
        case let c where c.contains("redirect"):
            return .purple
        case let c where c.contains("brand"):
            return .indigo
        case let c where c.contains("shortener"):
            return .cyan
        case let c where c.contains("safe"):
            return .green
        default:
            return Color(red: 1.0, green: 0.34, blue: 0.13)
        }
    }
}

// MARK: - Red Team Scenarios Repository

/// Repository of all red team scenarios for testing the detection engine.
enum RedTeamScenarios {
    
    /// All available red team scenarios for testing.
    /// Matches the Kotlin RedTeamScenarios exactly for cross-platform parity.
    static let scenarios: [RedTeamScenario] = [
        // HOMOGRAPH ATTACKS (Mixed Scripts)
        RedTeamScenario(
            id: "HG-001",
            category: "Homograph Attack",
            title: "Cyrillic 'а' in Apple",
            description: "Uses Cyrillic 'а' (U+0430) instead of Latin 'a' to impersonate Apple",
            maliciousUrl: "https://аpple.com/verify",
            targetBrand: "Apple",
            expectedScore: 70...100
        ),
        RedTeamScenario(
            id: "HG-002",
            category: "Homograph Attack",
            title: "Cyrillic in PayPal",
            description: "Uses Cyrillic 'р' and 'а' to impersonate PayPal",
            maliciousUrl: "https://раypal.com/login",
            targetBrand: "PayPal",
            expectedScore: 70...100
        ),
        RedTeamScenario(
            id: "HG-003",
            category: "Homograph Attack",
            title: "Cyrillic 'о' in Microsoft",
            description: "Uses Cyrillic 'о' (U+043E) to impersonate Microsoft",
            maliciousUrl: "https://micrоsоft.com/signin",
            targetBrand: "Microsoft",
            expectedScore: 70...100
        ),
        
        // IP OBFUSCATION
        RedTeamScenario(
            id: "IP-001",
            category: "IP Obfuscation",
            title: "Decimal IP Address",
            description: "Uses decimal encoding (3232235777) instead of dotted notation",
            maliciousUrl: "http://3232235777/malware",
            targetBrand: nil,
            expectedScore: 60...100
        ),
        RedTeamScenario(
            id: "IP-002",
            category: "IP Obfuscation",
            title: "Hexadecimal IP Address",
            description: "Uses hex encoding (0xC0A80101) to hide IP address",
            maliciousUrl: "http://0xC0A80101/payload",
            targetBrand: nil,
            expectedScore: 60...100
        ),
        RedTeamScenario(
            id: "IP-003",
            category: "IP Obfuscation",
            title: "Octal IP Address",
            description: "Uses octal notation (0300.0250.0001.0001) to obfuscate IP",
            maliciousUrl: "http://0300.0250.0001.0001/shell",
            targetBrand: nil,
            expectedScore: 50...100
        ),
        
        // SUSPICIOUS TLD (Known Phishing TLDs)
        RedTeamScenario(
            id: "TLD-001",
            category: "Suspicious TLD",
            title: "PayPal on .tk domain",
            description: ".tk is a free TLD commonly abused for phishing",
            maliciousUrl: "https://paypa1-secure.tk/login/verify",
            targetBrand: "PayPal",
            expectedScore: 70...100
        ),
        RedTeamScenario(
            id: "TLD-002",
            category: "Suspicious TLD",
            title: "Bank on .ml domain",
            description: ".ml is a free TLD commonly abused for phishing",
            maliciousUrl: "https://bank-secure.ml/verify",
            targetBrand: "Banking",
            expectedScore: 60...100
        ),
        RedTeamScenario(
            id: "TLD-003",
            category: "Suspicious TLD",
            title: "Amazon on .ga domain",
            description: ".ga is a free TLD commonly abused for phishing",
            maliciousUrl: "https://amazon-security.ga/giftcard",
            targetBrand: "Amazon",
            expectedScore: 60...100
        ),
        
        // NESTED REDIRECTS
        RedTeamScenario(
            id: "NR-001",
            category: "Nested Redirect",
            title: "URL in Query Parameter",
            description: "Embeds phishing URL in redirect parameter",
            maliciousUrl: "https://legit.com/redirect?url=https://phishing.tk/login",
            targetBrand: nil,
            expectedScore: 50...90
        ),
        RedTeamScenario(
            id: "NR-002",
            category: "Nested Redirect",
            title: "Encoded Nested URL",
            description: "URL-encoded malicious redirect destination",
            maliciousUrl: "https://legit.com/goto?next=https%3A%2F%2Fmalware.ml%2Fdownload",
            targetBrand: nil,
            expectedScore: 50...90
        ),
        
        // BRAND IMPERSONATION (Typosquatting)
        RedTeamScenario(
            id: "BI-001",
            category: "Brand Impersonation",
            title: "PayPal Typosquatting",
            description: "Uses '1' instead of 'l' in paypal (paypa1)",
            maliciousUrl: "https://paypa1.com/signin",
            targetBrand: "PayPal",
            expectedScore: 60...100
        ),
        RedTeamScenario(
            id: "BI-002",
            category: "Brand Impersonation",
            title: "Google Typosquatting",
            description: "Uses 'googIe' with capital I instead of 'l'",
            maliciousUrl: "https://googIe.com/account/verify",
            targetBrand: "Google",
            expectedScore: 50...90
        ),
        RedTeamScenario(
            id: "BI-003",
            category: "Brand Impersonation",
            title: "Netflix Subdomain Attack",
            description: "Uses netflix as subdomain of malicious domain",
            maliciousUrl: "https://netflix.secure-verify.com/billing",
            targetBrand: "Netflix",
            expectedScore: 50...90
        ),
        
        // URL SHORTENERS
        RedTeamScenario(
            id: "SH-001",
            category: "URL Shortener",
            title: "Bit.ly Shortened URL",
            description: "URL shorteners hide final destination, often used in phishing",
            maliciousUrl: "https://bit.ly/3xYz123",
            targetBrand: nil,
            expectedScore: 30...60
        ),
        RedTeamScenario(
            id: "SH-002",
            category: "URL Shortener",
            title: "TinyURL Shortened",
            description: "Another common shortener used to hide malicious destinations",
            maliciousUrl: "https://tinyurl.com/y2abc",
            targetBrand: nil,
            expectedScore: 30...60
        ),
        
        // SAFE CONTROL (Baseline for comparison)
        RedTeamScenario(
            id: "SAFE-001",
            category: "Safe (Control)",
            title: "Legitimate Google URL",
            description: "Baseline safe URL - should score low",
            maliciousUrl: "https://www.google.com",
            targetBrand: "Google",
            expectedScore: 0...30
        ),
        RedTeamScenario(
            id: "SAFE-002",
            category: "Safe (Control)",
            title: "Legitimate GitHub URL",
            description: "Baseline safe URL - should score low",
            maliciousUrl: "https://github.com/Raoof128/QDKMP-KotlinConf-2026-",
            targetBrand: "GitHub",
            expectedScore: 0...30
        )
    ]
}

#endif
