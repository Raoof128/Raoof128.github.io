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

package com.qrshield.desktop.navigation

import com.qrshield.desktop.model.AnalysisResult

/**
 * Navigation state for the desktop app sidebar-based navigation.
 * Matches the HTML design system with multiple distinct screens.
 */
sealed class Screen {
    data object Dashboard : Screen()
    data object Scanner : Screen()
    data object History : Screen()
    data object TrustCentre : Screen()
    data object Training : Screen()
    data object Settings : Screen()
    data object Export : Screen()
    data class Results(val result: AnalysisResult) : Screen()
}

/**
 * Navigation item data for sidebar rendering.
 */
data class NavItem(
    val screen: Screen,
    val icon: String,
    val label: String,
    val section: String // "Main Menu", "Security", "System"
)

/**
 * Sidebar navigation items matching HTML design.
 */
val mainNavItems = listOf(
    NavItem(Screen.Dashboard, "dashboard", "Dashboard", "Main Menu"),
    NavItem(Screen.Scanner, "qr_code_scanner", "Scan Monitor", "Main Menu"),
    NavItem(Screen.History, "history", "Scan History", "Main Menu"),
    NavItem(Screen.TrustCentre, "verified_user", "Safe List", "Main Menu"),
)

val systemNavItems = listOf(
    NavItem(Screen.Settings, "settings", "Settings", "System"),
    NavItem(Screen.Training, "school", "Training", "System"),
    NavItem(Screen.Export, "download", "Export", "System"),
)
