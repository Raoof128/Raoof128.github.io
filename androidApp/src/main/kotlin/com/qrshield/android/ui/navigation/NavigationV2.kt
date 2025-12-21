/*
 * Copyright 2025-2026 QR-SHIELD Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.qrshield.android.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.qrshield.android.ui.screens.*
import com.qrshield.android.ui.theme.BackgroundDark
import com.qrshield.android.ui.theme.BrandPrimary
import com.qrshield.android.ui.theme.TextMuted

/**
 * Navigation routes for the QR-SHIELD app.
 * Includes all screens converted from HTML reference.
 */
object Routes {
    // Main tabs
    const val DASHBOARD = "dashboard"
    const val SCANNER = "scanner"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    
    // Feature screens
    const val SCAN_RESULT = "scan_result"
    const val ATTACK_BREAKDOWN = "attack_breakdown"
    const val EXPORT_REPORT = "export_report"
    
    // Trust & Security
    const val TRUST_CENTRE = "trust_centre"
    const val ALLOWLIST = "allowlist"
    const val BLOCKLIST = "blocklist"
    const val THREAT_DATABASE = "threat_database"
    const val HEURISTICS = "heuristics"
    
    // Learning & Training
    const val LEARNING_CENTRE = "learning_centre"
    const val BEAT_THE_BOT = "beat_the_bot"
    
    // Info
    const val OFFLINE_PRIVACY = "offline_privacy"
}

/**
 * Screen definitions for bottom navigation.
 */
sealed class Screen(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
) {
    data object Dashboard : Screen(
        route = Routes.DASHBOARD,
        titleResId = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        contentDescription = "Dashboard home screen"
    )

    data object Scanner : Screen(
        route = Routes.SCANNER,
        titleResId = R.string.nav_scan,
        selectedIcon = Icons.Filled.QrCodeScanner,
        unselectedIcon = Icons.Outlined.QrCodeScanner,
        contentDescription = "QR code scanner screen"
    )

    data object History : Screen(
        route = Routes.HISTORY,
        titleResId = R.string.nav_history,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        contentDescription = "Scan history screen"
    )

    data object Settings : Screen(
        route = Routes.SETTINGS,
        titleResId = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        contentDescription = "Settings screen"
    )

    companion object {
        val bottomNavItems = listOf(Dashboard, Scanner, History, Settings)
    }
}

/**
 * Main navigation scaffold with bottom navigation bar.
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
 * Bottom navigation bar with Material 3 styling.
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
            contentDescription = "Main navigation bar"
        }
    ) {
        Screen.bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.contentDescription
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
                    if (!selected) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
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
                )
            )
        }
    }
}

/**
 * Navigation host with all screen composables.
 */
@Composable
fun QRShieldNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
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
        // === MAIN TABS ===
        
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onScanClick = { navController.navigate(Routes.SCANNER) },
                onImportClick = { /* Handle import */ },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onViewAllScans = { navController.navigate(Routes.HISTORY) },
                onScanItemClick = { navController.navigate(Routes.SCAN_RESULT) },
                onToolClick = { tool ->
                    when (tool) {
                        "deep_analysis" -> navController.navigate(Routes.ATTACK_BREAKDOWN)
                        "report" -> navController.navigate(Routes.EXPORT_REPORT)
                        "whitelist" -> navController.navigate(Routes.ALLOWLIST)
                    }
                }
            )
        }

        composable(Routes.SCANNER) {
            ScannerScreen()
        }

        composable(Routes.HISTORY) {
            HistoryScreen()
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }

        // === SCAN RESULT SCREENS ===
        
        composable(Routes.SCAN_RESULT) {
            ScanResultScreen(
                onBackClick = { navController.popBackStack() },
                onShareClick = { navController.navigate(Routes.EXPORT_REPORT) },
                onBlockClick = { navController.navigate(Routes.BLOCKLIST) },
                onIgnoreClick = { navController.popBackStack() }
            )
        }

        composable(Routes.ATTACK_BREAKDOWN) {
            AttackBreakdownScreen(
                onBackClick = { navController.popBackStack() },
                onShareClick = { navController.navigate(Routes.EXPORT_REPORT) },
                onBlockDomain = { navController.navigate(Routes.BLOCKLIST) },
                onReportToIT = { /* Handle IT report */ }
            )
        }

        composable(Routes.EXPORT_REPORT) {
            ExportReportScreen(
                onBackClick = { navController.popBackStack() },
                onExport = { format ->
                    // Handle export
                    navController.popBackStack()
                }
            )
        }

        // === TRUST & SECURITY SCREENS ===
        
        composable(Routes.TRUST_CENTRE) {
            TrustCentreScreen(
                onBackClick = { navController.popBackStack() },
                onDoneClick = { navController.popBackStack() },
                onAllowlistClick = { navController.navigate(Routes.ALLOWLIST) },
                onBlocklistClick = { navController.navigate(Routes.BLOCKLIST) }
            )
        }

        composable(Routes.ALLOWLIST) {
            AllowlistScreen(
                onBackClick = { navController.popBackStack() },
                onAddClick = { /* Show add sheet */ },
                onImportClick = { /* Handle import */ },
                onDeleteItem = { /* Handle delete */ }
            )
        }

        composable(Routes.BLOCKLIST) {
            BlocklistScreen(
                onBackClick = { navController.popBackStack() },
                onAddClick = { /* Show add sheet */ },
                onImportClick = { /* Handle import */ },
                onDeleteItem = { /* Handle delete */ }
            )
        }

        composable(Routes.THREAT_DATABASE) {
            ThreatDatabaseScreen(
                onBackClick = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onCheckNow = { /* Handle check */ },
                onImportFile = { /* Handle import */ }
            )
        }

        composable(Routes.HEURISTICS) {
            HeuristicsScreen(
                onBackClick = { navController.popBackStack() },
                onAddRule = { /* Handle add rule */ },
                onRuleClick = { /* Handle rule click */ },
                onToggleRule = { _, _ -> /* Handle toggle */ }
            )
        }

        // === LEARNING & TRAINING SCREENS ===
        
        composable(Routes.LEARNING_CENTRE) {
            LearningCentreScreen(
                onBackClick = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onViewCertificate = { /* Handle certificate */ },
                onReadTip = { /* Handle tip */ },
                onModuleClick = { /* Handle module */ },
                onReportThreatClick = { /* Handle report */ }
            )
        }

        composable(Routes.BEAT_THE_BOT) {
            BeatTheBotScreen(
                onBackClick = { navController.popBackStack() },
                onEndSession = { navController.popBackStack() },
                onPhishingClick = { /* Handle phishing answer */ },
                onLegitimateClick = { /* Handle legitimate answer */ },
                onHintDismiss = { /* Dismiss hint */ }
            )
        }

        // === INFO SCREENS ===
        
        composable(Routes.OFFLINE_PRIVACY) {
            OfflinePrivacyScreen(
                onBackClick = { navController.popBackStack() },
                onLearnMoreClick = { /* Handle learn more */ }
            )
        }
    }
}
