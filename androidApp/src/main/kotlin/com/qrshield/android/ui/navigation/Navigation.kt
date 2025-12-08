/*
 * Copyright 2024 QR-SHIELD Contributors
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

package com.qrshield.android.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qrshield.android.R
import com.qrshield.android.ui.screens.HistoryScreen
import com.qrshield.android.ui.screens.ScannerScreen
import com.qrshield.android.ui.screens.SettingsScreen
import com.qrshield.android.ui.theme.BackgroundDark
import com.qrshield.android.ui.theme.BrandPrimary
import com.qrshield.android.ui.theme.TextMuted

/**
 * Navigation routes for the QR-SHIELD app.
 * 
 * Sealed class ensures type-safe navigation and compile-time verification.
 */
sealed class Screen(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
) {
    data object Scanner : Screen(
        route = "scanner",
        titleResId = R.string.nav_scan,
        selectedIcon = Icons.Filled.QrCodeScanner,
        unselectedIcon = Icons.Outlined.QrCodeScanner,
        contentDescription = "QR code scanner screen"
    )
    
    data object History : Screen(
        route = "history",
        titleResId = R.string.nav_history,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        contentDescription = "Scan history screen"
    )
    
    data object Settings : Screen(
        route = "settings",
        titleResId = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        contentDescription = "Settings screen"
    )
    
    companion object {
        val bottomNavItems = listOf(Scanner, History, Settings)
    }
}

/**
 * Main navigation scaffold with bottom navigation bar.
 * 
 * Features:
 * - Material 3 Navigation Bar with proper accessibility
 * - Haptic feedback on navigation
 * - Smooth animations between screens
 * - Android 16 predictive back gesture support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRShieldNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            QRShieldBottomNavBar(navController = navController)
        },
        containerColor = BackgroundDark
    ) { innerPadding ->
        QRShieldNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Bottom navigation bar with Material 3 styling and accessibility support.
 */
@Composable
fun QRShieldBottomNavBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar(
        containerColor = BackgroundDark.copy(alpha = 0.95f),
        contentColor = BrandPrimary,
        modifier = Modifier.semantics {
            contentDescription = "Main navigation bar with 3 tabs: Scan, History, and Settings"
        }
    ) {
        Screen.bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.contentDescription,
                        modifier = Modifier.semantics {
                            this.contentDescription = screen.contentDescription
                        }
                    )
                },
                label = {
                    Text(
                        text = stringResource(screen.titleResId),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                selected = selected,
                onClick = {
                    // Avoid re-navigation to current destination
                    if (!selected) {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination to avoid building up
                            // a large back stack
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandPrimary,
                    selectedTextColor = BrandPrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = BrandPrimary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.semantics {
                    contentDescription = if (selected) {
                        "${screen.contentDescription}, currently selected"
                    } else {
                        "${screen.contentDescription}, tap to navigate"
                    }
                }
            )
        }
    }
}

/**
 * Navigation host with screen composables and animations.
 */
@Composable
fun QRShieldNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Scanner.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Screen.Scanner.route) {
            ScannerScreen()
        }
        
        composable(Screen.History.route) {
            HistoryScreen()
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
