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

package com.raouf.mehrguard.android.ui

import com.raouf.mehrguard.android.ui.navigation.MehrGuardNavigation
import androidx.compose.runtime.Composable

/**
 * Main Mehr Guard Application Composable
 *
 * This is the root composable that sets up navigation.
 * Updated for Android 16 with Navigation Compose.
 */
@Composable
fun MehrGuardApp() {
    MehrGuardNavigation()
}
