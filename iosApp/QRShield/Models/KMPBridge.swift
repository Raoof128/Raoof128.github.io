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

// KMPBridge.swift
// Demonstrates calling Kotlin Multiplatform code from SwiftUI
//
// This file provides the bridge between Swift and the KMP common module.
// When the common.framework is linked, uncomment the import and use real Kotlin classes.

import Foundation

#if canImport(common)
import common

/// Bridge to KMP HeuristicsEngine for URL analysis
///
/// ## Memory Safety Notes
/// - `HeuristicsEngine` is a stateless Kotlin class with no internal mutable state.
/// - It is safe to hold a strong reference as it doesn't capture callbacks.
/// - The engine is thread-safe and can be called from any thread.
/// - No retain cycles are possible because HeuristicsEngine doesn't hold
///   references back to Swift objects.
/// - This class is `@MainActor` isolated to ensure UI updates are on main thread.
@MainActor
class KMPAnalyzer: ObservableObject {
    // HeuristicsEngine is stateless - safe to hold strong reference
    // It's a pure function wrapper: analyze(url) -> Result
    // No callbacks, no delegates, no retain cycle risk
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

// ============================================================================
// ⚠️ KMP FRAMEWORK NOT LINKED - EXPLICIT FAILURE MODE
// ============================================================================
//
// This fallback exists ONLY to allow the project to compile without the
// common.framework. In production, the KMP framework MUST be linked.
//
// To build with the real KMP framework:
// 1. Run: ./gradlew :common:linkDebugFrameworkIosArm64
// 2. Ensure Frameworks/common.framework exists
// 3. Rebuild the iOS app in Xcode
//
// This is NOT a mock - it shows an error state to make clear that
// real analysis requires the shared Kotlin module.
// ============================================================================

import SwiftUI

/// Stub implementation when common.framework is not linked.
/// Shows explicit error state - NOT a functional mock.
@MainActor
class KMPAnalyzer: ObservableObject {
    @Published var lastResult: AnalysisResult?
    @Published var isAnalyzing = false
    @Published var isKMPAvailable = false  // Always false in stub mode
    
    struct AnalysisResult: Identifiable {
        let id = UUID()
        let url: String
        let score: Int
        let verdict: String
        let flags: [String]
        let timestamp: Date
    }
    
    init() {
        // Log warning on initialization (debug only)
        #if DEBUG
        print("⚠️ [QR-SHIELD] KMP common.framework NOT LINKED")
        print("⚠️ [QR-SHIELD] Analysis will show error state")
        print("⚠️ [QR-SHIELD] Run: ./gradlew :common:linkDebugFrameworkIosArm64")
        #endif
    }
    
    /// Analysis is unavailable without KMP framework.
    /// Returns an error result instead of mocking.
    func analyze(url: String) {
        isAnalyzing = true
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) { [weak self] in
            // Return explicit error result - NOT a mock
            self?.lastResult = AnalysisResult(
                url: url,
                score: -1,  // Invalid score indicates error
                verdict: "ERROR",
                flags: [
                    "⚠️ KMP Framework Not Linked",
                    "Run: ./gradlew :common:linkDebugFrameworkIosArm64",
                    "Then rebuild the iOS app in Xcode"
                ],
                timestamp: Date()
            )
            self?.isAnalyzing = false
        }
    }
}

#endif
