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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.qrshield.ui.game.BeatTheBotScreen
import platform.UIKit.UIViewController

/**
 * iOS Compose Interop - BeatTheBot Game UIViewController
 *
 * This function creates a UIViewController that hosts the BeatTheBotScreen
 * Compose component. It can be embedded in SwiftUI using UIViewControllerRepresentable.
 *
 * ## Usage in SwiftUI:
 *
 * ```swift
 * struct BeatTheBotGameView: UIViewControllerRepresentable {
 *     let onClose: () -> Void
 *
 *     func makeUIViewController(context: Context) -> UIViewController {
 *         return BeatTheBotViewControllerKt.BeatTheBotViewController(
 *             onClose: onClose
 *         )
 *     }
 *
 *     func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
 * }
 * ```
 *
 * ## Game Features
 *
 * The Beat the Bot game challenges users to:
 * - Submit adversarial URLs trying to bypass detection
 * - See real-time PhishingEngine verdicts
 * - Track their score against the bot
 * - Learn security concepts through play
 *
 * @param onClose Callback when user closes the game
 * @return UIViewController hosting the Compose game component
 */
fun BeatTheBotViewController(
    onClose: () -> Unit
): UIViewController = ComposeUIViewController {
    BeatTheBotWithCloseButton(onClose = onClose)
}

/**
 * Wrapper composable that adds a close button to BeatTheBotScreen
 * for iOS navigation integration.
 */
@Composable
private fun BeatTheBotWithCloseButton(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        BeatTheBotScreen()
        
        // Close button overlay
        FloatingActionButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = Color(0xFF334155)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close game",
                tint = Color.White
            )
        }
    }
}

