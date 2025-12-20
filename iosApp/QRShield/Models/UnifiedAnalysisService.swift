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

// Services/UnifiedAnalysisService.swift
// QR-SHIELD Unified Analysis Engine
//
// This service provides URL analysis using:
// 1. KMP HeuristicsEngine (when common.framework is linked)
// 2. Swift fallback engine (when KMP is not available)
//
// This dual approach ensures:
// - Full KMP integration for competition judging
// - Works in development without building Kotlin

import Foundation
import SwiftUI

#if canImport(common)
import common
#endif

#if os(iOS)

// MARK: - Unified Analysis Service

@MainActor
class UnifiedAnalysisService: ObservableObject {
    static let shared = UnifiedAnalysisService()
    
    @Published var isKMPAvailable: Bool = false
    @Published var lastEngineUsed: String = "None"
    
    #if canImport(common)
    private let kmpEngine = HeuristicsEngine()
    #endif
    
    private init() {
        #if canImport(common)
        isKMPAvailable = true
        print("✅ [QR-SHIELD] KMP HeuristicsEngine loaded")
        #else
        isKMPAvailable = false
        print("⚠️ [QR-SHIELD] Using Swift fallback engine")
        #endif
    }
    
    // MARK: - Unified Analysis
    
    /// Analyze a URL using the best available engine
    /// Returns a RiskAssessmentMock for UI compatibility
    func analyze(url: String) -> RiskAssessmentMock {
        #if canImport(common)
        return analyzeWithKMP(url: url)
        #else
        return analyzeWithSwift(url: url)
        #endif
    }
    
    // MARK: - KMP Engine (Kotlin Multiplatform)
    
    #if canImport(common)
    private func analyzeWithKMP(url: String) -> RiskAssessmentMock {
        lastEngineUsed = "KMP HeuristicsEngine"
        
        // Call Kotlin engine
        let result = kmpEngine.analyze(url: url)
        
        // Extract triggered flags
        let flags = result.checks.compactMap { check -> String? in
            guard let hCheck = check as? HeuristicsEngine.HeuristicCheck else { return nil }
            return hCheck.triggered ? hCheck.name : nil
        }
        
        // Map Kotlin score to verdict
        let score = Int(result.score)
        let verdict: VerdictMock
        if score >= 71 {
            verdict = .malicious
        } else if score >= 31 {
            verdict = .suspicious
        } else {
            verdict = .safe
        }
        
        return RiskAssessmentMock(
            score: score,
            verdict: verdict,
            flags: flags.isEmpty ? ["Analyzed by KMP Engine"] : flags,
            confidence: Double(100 - abs(50 - score)) / 100.0,
            url: url
        )
    }
    #endif
    
    // MARK: - Swift Fallback Engine
    
    private func analyzeWithSwift(url: String) -> RiskAssessmentMock {
        lastEngineUsed = "Swift Fallback Engine"
        
        var score = 0
        var flags: [String] = []
        let lowercasedUrl = url.lowercased()
        
        // Normalize URL
        var normalizedUrl = lowercasedUrl
        if !normalizedUrl.hasPrefix("http://") && !normalizedUrl.hasPrefix("https://") {
            normalizedUrl = "https://" + normalizedUrl
        }
        
        guard let urlComponents = URLComponents(string: normalizedUrl),
              let host = urlComponents.host else {
            // Invalid URL format
            return RiskAssessmentMock(
                score: 50,
                verdict: .suspicious,
                flags: ["Invalid URL Format"],
                confidence: 0.7,
                url: url
            )
        }
        
        // ============================================
        // TRUSTED DOMAINS - known safe domains
        // ============================================
        let trustedDomains = [
            "google.com", "www.google.com", "google.com.au",
            "apple.com", "www.apple.com",
            "microsoft.com", "www.microsoft.com", "account.microsoft.com",
            "github.com", "www.github.com",
            "amazon.com", "www.amazon.com",
            "paypal.com", "www.paypal.com",
            "facebook.com", "www.facebook.com",
            "twitter.com", "www.twitter.com", "x.com",
            "youtube.com", "www.youtube.com",
            "netflix.com", "www.netflix.com",
            "linkedin.com", "www.linkedin.com",
            "instagram.com", "www.instagram.com",
            "wikipedia.org", "en.wikipedia.org",
            "reddit.com", "www.reddit.com",
            "stackoverflow.com", "www.stackoverflow.com"
        ]
        
        // Check if it's a trusted domain
        let isTrusted = trustedDomains.contains(host) || trustedDomains.contains { host.hasSuffix(".\($0)") }
        
        if isTrusted {
            // Trusted domain - low risk
            score = 5
            flags.append("Verified Domain")
        } else {
            // Start with base score for unknown domains
            score = 25
            
            // ============================================
            // SUSPICIOUS PATTERNS
            // ============================================
            
            // Login/verify keywords
            if url.contains("login") || url.contains("signin") || url.contains("verify") || url.contains("account") {
                score += 15
                flags.append("Login/Verify Keywords")
            }
            
            // Urgency language
            if url.contains("secure") || url.contains("alert") || url.contains("urgent") || url.contains("suspended") {
                score += 25
                flags.append("Urgency Language")
            }
            
            // ============================================
            // HOMOGRAPH DETECTION
            // ============================================
            let homographPatterns = [
                "paypa1", "paypal1", "paypai", "paypall",
                "amaz0n", "amazom", "arnazon",
                "g00gle", "googie", "go0gle",
                "faceb00k", "facebok", "facebo0k",
                "micros0ft", "mircosoft", "micr0soft",
                "app1e", "appie", "apple1",
                "netf1ix", "netfiix", "n3tflix",
                "bank0f", "bankof-", "bank-of"
            ]
            
            for pattern in homographPatterns {
                if url.contains(pattern) {
                    score += 40
                    flags.append("Homograph Attack")
                    break
                }
            }
            
            // ============================================
            // HIGH-RISK TLDs
            // ============================================
            let highRiskTLDs = [".tk", ".ml", ".ga", ".cf", ".gq"]
            let mediumRiskTLDs = [".work", ".click", ".xyz", ".top", ".buzz"]
            
            for tld in highRiskTLDs {
                if host.hasSuffix(tld) {
                    score += 50
                    flags.append("High-Risk Free TLD")
                    break
                }
            }
            
            for tld in mediumRiskTLDs {
                if host.hasSuffix(tld) {
                    score += 25
                    flags.append("Suspicious TLD")
                    break
                }
            }
            
            // ============================================
            // BRAND IMPERSONATION
            // ============================================
            let brandNames = ["paypal", "amazon", "google", "apple", "microsoft", "facebook", "netflix", "bank"]
            for brand in brandNames {
                if host.hasPrefix("\(brand).") || host.hasPrefix("\(brand)-") {
                    if !host.hasSuffix("\(brand).com") && !host.hasSuffix("\(brand).net") {
                        score += 40
                        flags.append("Brand Impersonation")
                        break
                    }
                }
            }
            
            // ============================================
            // STRUCTURAL CHECKS
            // ============================================
            
            // Long subdomain chains
            let components = host.components(separatedBy: ".")
            if components.count > 4 {
                score += 20
                flags.append("Complex Domain Structure")
            }
            
            // IP address in URL
            let ipPattern = try? NSRegularExpression(pattern: "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")
            if let ipPattern = ipPattern, ipPattern.firstMatch(in: url, range: NSRange(url.startIndex..., in: url)) != nil {
                score += 45
                flags.append("IP Address URL")
            }
            
            // Excessive hyphens
            if host.filter({ $0 == "-" }).count > 2 {
                score += 15
                flags.append("Excessive Hyphens")
            }
            
            // ============================================
            // @ SYMBOL - CREDENTIAL THEFT
            // ============================================
            if url.contains("@") {
                score += 55
                flags.append("Credential Theft Attempt")
            }
            
            // ============================================
            // TYPOSQUATTING
            // ============================================
            let typosquattingPatterns = [
                "googl.", "gogle.", "goolge.", "gooogle.", "g00gle.", "googel.",
                "appple.", "aple.", "aplle.", "app1e.",
                "amazn.", "amzon.", "amazom.", "anazon.",
                "paypa.", "paypall.", "payypal.", "pyppal.",
                "microsof.", "mircosoft.", "microsofl.",
                "facebok.", "facbook.", "faceboo.",
                "netfllx.", "netfiix.", "neflix.",
                "bankk.", "bamk."
            ]
            
            for pattern in typosquattingPatterns {
                if host.contains(pattern) {
                    score += 50
                    flags.append("Typosquatting")
                    break
                }
            }
        }
        
        // ============================================
        // DETERMINE VERDICT
        // ============================================
        let verdict: VerdictMock
        if score >= 60 {
            verdict = .malicious
        } else if score >= 35 {
            verdict = .suspicious
        } else {
            verdict = .safe
            if flags.isEmpty {
                flags.append("No Threats Detected")
            }
        }
        
        return RiskAssessmentMock(
            score: min(score, 100),
            verdict: verdict,
            flags: flags,
            confidence: calculateConfidence(score: score, flagCount: flags.count),
            url: url
        )
    }
    
    // MARK: - Helpers
    
    private func calculateConfidence(score: Int, flagCount: Int) -> Double {
        // Higher confidence when score is extreme and multiple flags found
        let scoreConfidence = score > 70 || score < 20 ? 0.9 : 0.7
        let flagBonus = min(Double(flagCount) * 0.05, 0.1)
        return min(scoreConfidence + flagBonus, 0.98)
    }
}

// MARK: - Engine Info View Component

struct EngineInfoBadge: View {
    @StateObject private var service = UnifiedAnalysisService.shared
    
    var body: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(service.isKMPAvailable ? Color.verdictSafe : Color.verdictWarning)
                .frame(width: 6, height: 6)
            
            Text(service.isKMPAvailable ? "KMP Engine" : "Swift Engine")
                .font(.caption2.weight(.medium))
                .foregroundColor(.textSecondary)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4)
        .background(.ultraThinMaterial, in: Capsule())
    }
}

#endif
