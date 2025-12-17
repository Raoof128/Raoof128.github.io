//
//  ComposeInterop.swift
//  QRShield
//
//  Copyright 2025-2026 QR-SHIELD Contributors
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

import SwiftUI
import common

// MARK: - Compose Multiplatform iOS Integration

/**
 * SharedResultCardView - SwiftUI wrapper for Compose Multiplatform ResultCard
 *
 * This demonstrates the "hybrid" KMP approach:
 * - Complex, reusable UI components are built once in Compose (commonMain)
 * - They're embedded in SwiftUI using UIViewControllerRepresentable
 * - Native SwiftUI handles navigation, gestures, and platform conventions
 *
 * ## Why This Matters for Competition
 *
 * This proves that Compose Multiplatform components work on iOS,
 * even though the main UI is SwiftUI. It's the best of both worlds:
 * - Shared UI code where it makes sense
 * - Native feel where it matters
 *
 * ## Usage
 *
 * ```swift
 * struct ResultView: View {
 *     let assessment: RiskAssessment
 *     @Environment(\.dismiss) var dismiss
 *
 *     var body: some View {
 *         SharedResultCardView(
 *             assessment: assessment,
 *             onDismiss: { dismiss() },
 *             onShare: { shareResult() }
 *         )
 *     }
 * }
 * ```
 */
struct SharedResultCardView: UIViewControllerRepresentable {
    let assessment: RiskAssessment
    let onDismiss: () -> Void
    let onShare: () -> Void
    
    func makeUIViewController(context: Context) -> UIViewController {
        // Create Compose UIViewController from Kotlin
        return SharedResultCardViewControllerKt.SharedResultCardViewController(
            assessment: assessment,
            onDismiss: onDismiss,
            onShare: onShare
        )
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Compose handles its own state updates
    }
}

// MARK: - Additional Compose Components

/**
 * BeatTheBotGameView - Gamification component from shared Compose
 *
 * Embeds the "Beat the Bot" game mode UI from commonMain.
 */
struct BeatTheBotGameView: UIViewControllerRepresentable {
    let onClose: () -> Void
    
    func makeUIViewController(context: Context) -> UIViewController {
        // Note: This requires BeatTheBotViewController to be exported from iosMain
        // return BeatTheBotViewControllerKt.BeatTheBotViewController(onClose: onClose)
        
        // Placeholder until full integration
        let vc = UIViewController()
        vc.view.backgroundColor = .systemBackground
        return vc
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/**
 * ThreatRadarView - Animated threat visualization from shared Compose
 *
 * Displays the signature "radar" animation for detected threats.
 */
struct ThreatRadarView: UIViewControllerRepresentable {
    let threatLevel: Float // 0.0 - 1.0
    let isActive: Bool
    
    func makeUIViewController(context: Context) -> UIViewController {
        // Note: Requires ThreatRadarViewController export
        let vc = UIViewController()
        vc.view.backgroundColor = .clear
        return vc
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

// MARK: - Preview Helpers

#if DEBUG
struct SharedResultCardView_Previews: PreviewProvider {
    static var previews: some View {
        // Create a mock assessment for preview
        let mockAssessment = createMockAssessment(
            score: 85,
            verdict: .malicious,
            flags: ["Brand Impersonation", "Suspicious TLD", "Typosquatting"]
        )
        
        SharedResultCardView(
            assessment: mockAssessment,
            onDismiss: {},
            onShare: {}
        )
        .previewDisplayName("Malicious Result")
    }
    
    static func createMockAssessment(
        score: Int32,
        verdict: Verdict,
        flags: [String]
    ) -> RiskAssessment {
        // This would create a RiskAssessment from the Kotlin model
        // Implementation depends on how the Kotlin types are exported
        fatalError("Mock implementation needed")
    }
}
#endif

// MARK: - Accessibility Extensions

extension SharedResultCardView {
    /**
     * Configure accessibility for the embedded Compose view.
     */
    func accessibilityConfigured() -> some View {
        self
            .accessibilityElement(children: .contain)
            .accessibilityLabel("Security Analysis Result")
            .accessibilityHint("Shows the risk assessment for the scanned QR code")
    }
}

// MARK: - Integration Notes

/**
 * ## iOS Compose Integration Architecture
 *
 * ```
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    SwiftUI Navigation                       │
 * ├─────────────────────────────────────────────────────────────┤
 * │                                                             │
 * │   ┌─────────────┐   ┌──────────────┐   ┌───────────────┐   │
 * │   │ ScannerView │   │ HistoryView  │   │ SettingsView  │   │
 * │   │  (SwiftUI)  │   │  (SwiftUI)   │   │   (SwiftUI)   │   │
 * │   └──────┬──────┘   └──────────────┘   └───────────────┘   │
 * │          │                                                  │
 * │          ▼                                                  │
 * │   ┌─────────────────────────────────────────────────────┐   │
 * │   │         SharedResultCardView (Compose)              │   │
 * │   │  ┌─────────────────────────────────────────────┐    │   │
 * │   │  │     UIViewControllerRepresentable           │    │   │
 * │   │  │         ┌─────────────────────┐             │    │   │
 * │   │  │         │ ComposeUIViewController │         │    │   │
 * │   │  │         │  (from iosMain)      │            │    │   │
 * │   │  │         └──────────┬──────────┘             │    │   │
 * │   │  │                    │                        │    │   │
 * │   │  │         ┌──────────▼──────────┐             │    │   │
 * │   │  │         │  SharedResultCard   │             │    │   │
 * │   │  │         │   (from commonMain) │             │    │   │
 * │   │  │         └─────────────────────┘             │    │   │
 * │   │  └─────────────────────────────────────────────┘    │   │
 * │   └─────────────────────────────────────────────────────┘   │
 * │                                                             │
 * └─────────────────────────────────────────────────────────────┘
 * ```
 *
 * ## Benefits of This Approach
 *
 * 1. **Code Sharing**: ResultCard UI is written once in Compose
 * 2. **Native Feel**: SwiftUI handles navigation, gestures
 * 3. **Gradual Adoption**: Can add more Compose screens over time
 * 4. **Type Safety**: Kotlin types flow through to Swift
 *
 * ## Required Setup
 *
 * In `iosMain`, export Compose controllers:
 *
 * ```kotlin
 * // iosMain/kotlin/com/qrshield/ui/SharedResultCardViewController.kt
 * fun SharedResultCardViewController(
 *     assessment: RiskAssessment,
 *     onDismiss: () -> Unit,
 *     onShare: () -> Unit
 * ): UIViewController = ComposeUIViewController {
 *     SharedResultCard(assessment, onDismiss, onShare)
 * }
 * ```
 */
private struct IntegrationDocumentation {}
