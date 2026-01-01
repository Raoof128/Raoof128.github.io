/*
 * Copyright 2025-2026 Mehr Guard Contributors
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

package com.raouf.mehrguard.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.raouf.mehrguard.model.RiskAssessment
import com.raouf.mehrguard.ui.shared.SharedResultCard
import platform.UIKit.UIViewController

/**
 * iOS Compose Interop - SharedResultCard UIViewController
 *
 * This function creates a UIViewController that hosts the SharedResultCard
 * Compose component. It can be embedded in SwiftUI using UIViewControllerRepresentable.
 *
 * ## Usage in SwiftUI:
 *
 * ```swift
 * struct SharedResultCardView: UIViewControllerRepresentable {
 *     let assessment: RiskAssessment
 *     let onDismiss: () -> Void
 *     let onShare: () -> Void
 *
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         return SharedResultCardViewControllerKt.SharedResultCardViewController(
 *             assessment: assessment,
 *             onDismiss: onDismiss,
 *             onShare: onShare
 *         )
 *     }
 *
 *     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 * }
 * ```
 *
 * ## Why This Matters for KMP
 *
 * This demonstrates the "hybrid" approach to Compose Multiplatform on iOS:
 * - Complex, reusable UI components are built once in Compose
 * - They integrate seamlessly with native SwiftUI navigation
 * - Best of both worlds: shared code + native feel
 *
 * @param assessment The risk assessment to display
 * @param onDismiss Callback when user dismisses
 * @param onShare Callback when user shares
 * @return UIViewController hosting the Compose component
 */
fun SharedResultCardViewController(
    assessment: RiskAssessment,
    onDismiss: () -> Unit,
    onShare: () -> Unit
): UIViewController = ComposeUIViewController {
    SharedResultCard(
        assessment = assessment,
        onDismiss = onDismiss,
        onShare = onShare
    )
}
