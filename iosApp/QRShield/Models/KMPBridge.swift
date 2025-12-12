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

// KMPBridge.swift
// Demonstrates calling Kotlin Multiplatform code from SwiftUI
//
// This file provides the bridge between Swift and the KMP common module.
// When the common.framework is linked, uncomment the import and use real Kotlin classes.

import Foundation

#if canImport(common)
import common

/// Bridge to KMP HeuristicsEngine for URL analysis
@MainActor
class KMPAnalyzer: ObservableObject {
    private let heuristicsEngine = HeuristicsEngine()
    
    @Published var lastResult: AnalysisResult?
    @Published var isAnalyzing = false
    
    struct AnalysisResult: Identifiable {
        let id = UUID()
        let url: String
        let score: Int
        let verdict: String
        let flags: [String]
        let timestamp: Date
    }
    
    /// Analyze a URL using the KMP HeuristicsEngine
    func analyze(url: String) {
        isAnalyzing = true
        
        // Call Kotlin HeuristicsEngine.analyze()
        let result = heuristicsEngine.analyze(url: url)
        
        // Map Kotlin Result to Swift struct
        let flags = result.checks.compactMap { check -> String? in
            guard let hCheck = check as? HeuristicsEngine.HeuristicCheck else { return nil }
            return hCheck.triggered ? hCheck.name : nil
        }
        
        lastResult = AnalysisResult(
            url: url,
            score: Int(result.score),
            verdict: verdictString(from: result.score),
            flags: flags,
            timestamp: Date()
        )
        
        isAnalyzing = false
    }
    
    private func verdictString(from score: Int32) -> String {
        switch score {
        case 0..<31: return "SAFE"
        case 31..<71: return "SUSPICIOUS"
        default: return "MALICIOUS"
        }
    }
}

#else

/// Fallback mock implementation when common.framework is not linked
/// This allows the app to compile and run for UI development
@MainActor
class KMPAnalyzer: ObservableObject {
    @Published var lastResult: AnalysisResult?
    @Published var isAnalyzing = false
    
    struct AnalysisResult: Identifiable {
        let id = UUID()
        let url: String
        let score: Int
        let verdict: String
        let flags: [String]
        let timestamp: Date
    }
    
    /// Mock analysis - replace with real KMP when framework is linked
    func analyze(url: String) {
        isAnalyzing = true
        
        // Simulate analysis delay
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) { [weak self] in
            // Generate mock result based on URL characteristics
            let score: Int
            let verdict: String
            var flags: [String] = []
            
            if url.contains("paypa1") || url.contains(".tk") || url.contains(".ml") {
                score = 85
                verdict = "MALICIOUS"
                flags = ["BRAND_IMPERSONATION", "SUSPICIOUS_TLD", "TYPOSQUATTING"]
            } else if url.contains("bit.ly") || url.contains("tinyurl") || !url.hasPrefix("https") {
                score = 45
                verdict = "SUSPICIOUS"
                flags = ["URL_SHORTENER", "HTTP_NOT_HTTPS"]
            } else {
                score = 12
                verdict = "SAFE"
                flags = []
            }
            
            self?.lastResult = AnalysisResult(
                url: url,
                score: score,
                verdict: verdict,
                flags: flags,
                timestamp: Date()
            )
            self?.isAnalyzing = false
        }
    }
}

#endif
