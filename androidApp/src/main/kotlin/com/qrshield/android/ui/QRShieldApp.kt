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

package com.qrshield.android.ui

import com.qrshield.android.ui.navigation.QRShieldNavigation
import androidx.compose.runtime.Composable

/**
 * Main QR-SHIELD Application Composable
 *
 * This is the root composable that sets up navigation.
 * Updated for Android 16 with Navigation Compose.
 */
@Composable
fun QRShieldApp() {
    QRShieldNavigation()
}
