/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qrshield.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.qrshield.model.RiskAssessment
import com.qrshield.ui.shared.ThreatRadar
import platform.UIKit.UIViewController

/**
 * iOS Compose Interop - ThreatRadar Visualization UIViewController
 *
 * This function creates a UIViewController that hosts the ThreatRadar
 * Compose component. It can be embedded in SwiftUI using UIViewControllerRepresentable.
 *
 * ## Usage in SwiftUI:
 *
 * ```swift
 * struct ThreatRadarView: UIViewControllerRepresentable {
 *     let assessment: RiskAssessment
 *
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         return ThreatRadarViewControllerKt.ThreatRadarViewController(
 *             assessment: assessment
 *         )
 *     }
 *
 *     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 * }
 * ```
 *
 * ## Visual Features
 *
 * The Threat Radar displays:
 * - Animated sweep effect like a radar screen
 * - Signal dots representing detected threats
 * - Pulsing effects for active threats
 * - Color-coded severity levels based on verdict
 *
 * @param assessment The risk assessment to visualize
 * @return UIViewController hosting the Compose radar component
 */
fun ThreatRadarViewController(
    assessment: RiskAssessment
): UIViewController = ComposeUIViewController {
    ThreatRadar(
        assessment = assessment
    )
}

