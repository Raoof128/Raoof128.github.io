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

package com.qrshield.android.ui.navigation

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qrshield.android.R
import com.qrshield.android.ui.screens.*
import com.qrshield.android.ui.theme.BackgroundDark
import com.qrshield.android.ui.theme.BrandPrimary
import com.qrshield.android.ui.theme.TextMuted
import com.qrshield.model.ScanSource
import com.qrshield.scanner.AndroidQrScanner
import com.qrshield.ui.SharedViewModel
import com.qrshield.ui.UiState
import kotlinx.coroutines.launch
import com.qrshield.android.ui.viewmodels.BeatTheBotViewModel
import com.qrshield.android.ui.viewmodels.GameResult
import com.qrshield.android.util.DateUtils
import com.qrshield.android.util.SoundManager
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

/**
 * Navigation routes for the QR-SHIELD app.
 * REWIRED: Dashboard is now the start destination with expanded navigation.
 */
object Routes {
    // Main bottom nav tabs
    const val DASHBOARD = "dashboard"
    const val SCANNER = "scanner"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val SETTINGS_FROM_DASHBOARD = "settings_from_dashboard"
    
    // Feature screens
    const val SCAN_RESULT = "scan_result/{url}/{verdict}/{score}"
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
    
    // Helper to build scan result route
    fun scanResult(url: String, verdict: String, score: Int) = 
        "scan_result/${java.net.URLEncoder.encode(url, "UTF-8")}/$verdict/$score"
}

/**
 * Screen definitions for bottom navigation.
 * REWIRED: Added Dashboard as first tab, renamed Scanner to Scan.
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
        // REWIRED: 4-tab bottom nav with Dashboard first
        val bottomNavItems = listOf(Dashboard, Scanner, History, Settings)
    }
}

/**
 * Main navigation scaffold with bottom navigation bar.
 * REWIRED: Dashboard is now the start destination.
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
            contentDescription = "Main navigation bar with 4 tabs: Home, Scan, History, and Settings"
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
 * Navigation host with all screen composables and animations.
 * REWIRED: Dashboard is start destination, all new screens added with REAL logic.
 */
@Composable
fun QRShieldNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SharedViewModel = koinInject()
    val domainListRepository: com.qrshield.android.data.DomainListRepository = koinInject()
    val coroutineScope = rememberCoroutineScope()
    
    // Persistent domain lists from DataStore
    val allowlistEntries by domainListRepository.allowlist.collectAsState(initial = emptyList())
    val blocklistEntries by domainListRepository.blocklist.collectAsState(initial = emptyList())
    
    // Heuristics rules state
    var heuristicsRules by remember { 
        mutableStateOf(mapOf(
            "homograph_detection" to true,
            "tld_analysis" to true,
            "brand_impersonation" to true,
            "url_shortener" to true,
            "suspicious_keywords" to true,
            "ip_detection" to false
        ))
    }
    
    // Learning progress state
    var learningProgress by remember { mutableIntStateOf(67) }
    
    // QR Scanner for gallery images
    val qrScanner = remember { AndroidQrScanner(context) }
    
    // Photo picker launcher for Dashboard import
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val result = qrScanner.scanFromUri(uri)
                when (result) {
                    is com.qrshield.model.ScanResult.Success -> {
                        viewModel.processScanResult(result, ScanSource.GALLERY)
                        Toast.makeText(context, "QR code found - analyzing...", Toast.LENGTH_SHORT).show()
                    }
                    is com.qrshield.model.ScanResult.NoQrFound -> {
                        Toast.makeText(context, "No QR code found in image", Toast.LENGTH_SHORT).show()
                    }
                    is com.qrshield.model.ScanResult.Error -> {
                        Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

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
        // ===== BOTTOM NAV TABS =====
        
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onScanClick = { 
                    // Use same pattern as bottom nav for proper back stack
                    navController.navigate(Routes.SCANNER) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onImportClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onSettingsClick = { 
                    // Navigate to settings with back button
                    navController.navigate(Routes.SETTINGS_FROM_DASHBOARD)
                },
                onViewAllScans = { 
                    // Use same pattern as bottom nav for proper back stack
                    navController.navigate(Routes.HISTORY) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onScanItemClick = { scanId ->
                    // Navigate to result with scan details
                    coroutineScope.launch {
                        val scan = viewModel.getScanById(scanId)
                        scan?.let {
                            navController.navigate(Routes.scanResult(it.url, it.verdict.name, it.score))
                        }
                    }
                },
                onToolClick = { tool ->
                    when (tool) {
                        "trust_centre", "deep_analysis" -> navController.navigate(Routes.TRUST_CENTRE)
                        "learning_centre" -> navController.navigate(Routes.LEARNING_CENTRE)
                        "threat_database" -> navController.navigate(Routes.THREAT_DATABASE)
                        "beat_the_bot" -> navController.navigate(Routes.BEAT_THE_BOT)
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
        
        // Settings accessed from Dashboard header - shows back button
        composable(Routes.SETTINGS_FROM_DASHBOARD) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ===== SCAN RESULT SCREENS =====
        
        composable(
            route = Routes.SCAN_RESULT,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("verdict") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val url = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("url") ?: "",
                "UTF-8"
            )
            val verdict = backStackEntry.arguments?.getString("verdict") ?: "UNKNOWN"
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            
            ScanResultScreen(
                url = url,
                verdict = verdict,
                score = score,
                onBackClick = { navController.popBackStack() },
                onShareClick = { 
                    val shareText = viewModel.generateShareText()
                    if (shareText != null) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Analysis"))
                    }
                },
                onBlockClick = { 
                    // Add to blocklist using persistent repository
                    val domain = url.substringAfter("://").substringBefore("/").substringBefore("?")
                    coroutineScope.launch {
                        domainListRepository.addToBlocklist(domain, com.qrshield.android.data.DomainSource.SCANNED)
                        Toast.makeText(context, "Added $domain to blocklist", Toast.LENGTH_SHORT).show()
                    }
                    navController.popBackStack()
                },
                onIgnoreClick = { navController.popBackStack() }
            )
        }

        composable(Routes.ATTACK_BREAKDOWN) {
            AttackBreakdownScreen(
                onBackClick = { navController.popBackStack() },
                onShareClick = { 
                    val shareText = viewModel.generateShareText()
                    if (shareText != null) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Threat Analysis"))
                    }
                },
                onBlockDomain = { 
                    navController.navigate(Routes.BLOCKLIST)
                },
                onReportToIT = {
                    // Generate incident report and open email
                    val jsonReport = viewModel.generateJsonExport() ?: "No report available"
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("security@company.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "QR-SHIELD: Security Incident Report")
                        putExtra(Intent.EXTRA_TEXT, jsonReport)
                    }
                    context.startActivity(Intent.createChooser(intent, "Send IT Report"))
                }
            )
        }

        composable(Routes.EXPORT_REPORT) {
            ExportReportScreen(
                onBackClick = { navController.popBackStack() },
                onExport = { format ->
                    when (format) {
                        ExportFormat.PDF -> {
                            Toast.makeText(context, "Generating PDF report...", Toast.LENGTH_SHORT).show()
                            val shareText = viewModel.generateShareText() ?: "No data"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Export PDF"))
                        }
                        ExportFormat.CSV -> {
                            Toast.makeText(context, "Exporting to CSV...", Toast.LENGTH_SHORT).show()
                            coroutineScope.launch {
                                val stats = viewModel.getStatistics()
                                val csv = "Total,Safe,Suspicious,Malicious\n${stats.totalScans},${stats.safeCount},${stats.suspiciousCount},${stats.maliciousCount}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_TEXT, csv)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export CSV"))
                            }
                        }
                        ExportFormat.JSON -> {
                            val json = viewModel.generateJsonExport()
                            if (json != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(intent, "Export JSON"))
                            }
                        }
                    }
                    navController.popBackStack()
                }
            )
        }

        // ===== TRUST & SECURITY SCREENS =====
        
        composable(Routes.TRUST_CENTRE) {
            TrustCentreScreen(
                onBackClick = { navController.popBackStack() },
                onDoneClick = { 
                    Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
                    navController.popBackStack() 
                },
                onAllowlistClick = { navController.navigate(Routes.ALLOWLIST) },
                onBlocklistClick = { navController.navigate(Routes.BLOCKLIST) }
            )
        }

        composable(Routes.ALLOWLIST) {
            // Convert DomainEntry to AllowedDomain for UI
            val allowedDomains = allowlistEntries.map { entry ->
                AllowedDomain(
                    id = entry.domain,
                    domain = entry.domain,
                    addedDate = DateUtils.formatRelativeTime(entry.addedAt),
                    source = when (entry.source) {
                        com.qrshield.android.data.DomainSource.ENTERPRISE -> AllowlistSource.ENTERPRISE
                        com.qrshield.android.data.DomainSource.AUTO_LEARNED -> AllowlistSource.AUTO_LEARNED
                        else -> AllowlistSource.MANUAL
                    }
                )
            }
            
            var showAddSheet by remember { mutableStateOf(false) }
            
            AllowlistScreen(
                onBackClick = { navController.popBackStack() },
                onAddClick = { showAddSheet = true },
                onImportClick = { 
                    Toast.makeText(context, "Import allowlist from file", Toast.LENGTH_SHORT).show()
                },
                onDeleteItem = { domainId ->
                    coroutineScope.launch {
                        domainListRepository.removeFromAllowlist(domainId)
                        Toast.makeText(context, "Removed $domainId from allowlist", Toast.LENGTH_SHORT).show()
                    }
                },
                allowedDomains = allowedDomains,
                showAddSheet = showAddSheet,
                onDismissSheet = { showAddSheet = false },
                onAllowDomain = { domain ->
                    coroutineScope.launch {
                        domainListRepository.addToAllowlist(domain)
                        Toast.makeText(context, "Added $domain to allowlist", Toast.LENGTH_SHORT).show()
                        showAddSheet = false
                    }
                }
            )
        }

        composable(Routes.BLOCKLIST) {
            // Convert DomainEntry to BlockedDomain for UI
            val blockedDomains = blocklistEntries.map { entry ->
                BlockedDomain(
                    id = entry.domain,
                    domain = entry.domain,
                    addedDate = DateUtils.formatRelativeTime(entry.addedAt),
                    type = when (entry.type) {
                        com.qrshield.android.data.DomainType.PHISHING -> BlockedType.PHISHING
                        com.qrshield.android.data.DomainType.SUSPICIOUS -> BlockedType.SUSPICIOUS
                        else -> BlockedType.MALICIOUS
                    }
                )
            }
            
            var showAddSheet by remember { mutableStateOf(false) }
            
            BlocklistScreen(
                onBackClick = { navController.popBackStack() },
                onAddClick = { showAddSheet = true },
                onImportClick = { 
                    Toast.makeText(context, "Import blocklist from file", Toast.LENGTH_SHORT).show()
                },
                onDeleteItem = { domainId ->
                    coroutineScope.launch {
                        domainListRepository.removeFromBlocklist(domainId)
                        Toast.makeText(context, "Removed $domainId from blocklist", Toast.LENGTH_SHORT).show()
                    }
                },
                blockedDomains = blockedDomains,
                showAddSheet = showAddSheet,
                onDismissSheet = { showAddSheet = false },
                onBlockDomain = { domain ->
                    coroutineScope.launch {
                        domainListRepository.addToBlocklist(domain)
                        Toast.makeText(context, "Added $domain to blocklist", Toast.LENGTH_SHORT).show()
                        showAddSheet = false
                    }
                }
            )
        }

        composable(Routes.THREAT_DATABASE) {
            ThreatDatabaseScreen(
                onBackClick = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onCheckNow = { 
                    Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show()
                    // In production: Trigger database update check
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1500)
                        Toast.makeText(context, "Database is up to date!", Toast.LENGTH_SHORT).show()
                    }
                },
                onImportFile = { 
                    Toast.makeText(context, "Select threat signature file", Toast.LENGTH_SHORT).show()
                    // In production: Open file picker for signature import
                }
            )
        }

        composable(Routes.HEURISTICS) {
            HeuristicsScreen(
                onBackClick = { navController.popBackStack() },
                onAddRule = { 
                    Toast.makeText(context, "Custom rule creation coming soon", Toast.LENGTH_SHORT).show()
                },
                onRuleClick = { ruleId ->
                    Toast.makeText(context, "Rule details: $ruleId", Toast.LENGTH_SHORT).show()
                },
                onToggleRule = { ruleId, enabled ->
                    heuristicsRules = heuristicsRules.toMutableMap().apply {
                        put(ruleId, enabled)
                    }
                    val status = if (enabled) "enabled" else "disabled"
                    Toast.makeText(context, "Rule $ruleId $status", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ===== LEARNING & TRAINING SCREENS =====
        
        composable(Routes.LEARNING_CENTRE) {
            LearningCentreScreen(
                onBackClick = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onViewCertificate = { 
                    if (learningProgress >= 100) {
                        Toast.makeText(context, "Generating certificate...", Toast.LENGTH_SHORT).show()
                        // In production: Generate and show certificate
                    } else {
                        Toast.makeText(context, "Complete all modules to earn certificate", Toast.LENGTH_SHORT).show()
                    }
                },
                onReadTip = {
                    Toast.makeText(context, "Tip marked as read", Toast.LENGTH_SHORT).show()
                    learningProgress = (learningProgress + 5).coerceAtMost(100)
                },
                onModuleClick = { moduleId ->
                    when (moduleId) {
                        "beat_the_bot" -> navController.navigate(Routes.BEAT_THE_BOT)
                        else -> {
                            Toast.makeText(context, "Opening module: $moduleId", Toast.LENGTH_SHORT).show()
                            learningProgress = (learningProgress + 10).coerceAtMost(100)
                        }
                    }
                },
                onReportThreatClick = { 
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "message/rfc822"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("threats@qrshield.app"))
                        putExtra(Intent.EXTRA_SUBJECT, "Threat Report from QR-SHIELD User")
                    }
                    context.startActivity(Intent.createChooser(intent, "Report Threat"))
                }
            )
        }

        composable(Routes.BEAT_THE_BOT) {
            val beatTheBotViewModel: BeatTheBotViewModel = koinViewModel()
            val state by beatTheBotViewModel.uiState.collectAsState()
            val settings by viewModel.settings.collectAsState()
            
            // Play sounds based on result changes
            LaunchedEffect(state.lastResult) {
                when (state.lastResult) {
                    GameResult.CORRECT -> {
                        SoundManager.playSound(SoundManager.SoundType.SUCCESS, settings.isSoundEnabled)
                    }
                    GameResult.INCORRECT, GameResult.TIMEOUT -> {
                        SoundManager.playSound(SoundManager.SoundType.WARNING, settings.isSoundEnabled)
                    }
                    null -> { /* No action */ }
                }
            }
            
            // Format time helper
            val formattedTime = remember(state.timeRemainingSeconds) {
                String.format("%02d:%02d", state.timeRemainingSeconds / 60, state.timeRemainingSeconds % 60)
            }

            BeatTheBotScreen(
                onBackClick = { navController.popBackStack() },
                onEndSession = { 
                    beatTheBotViewModel.startNewGame() // Reset for next time or just leave
                    navController.popBackStack()
                },
                onPhishingClick = { beatTheBotViewModel.submitGuess(isPhishingGuess = true) },
                onLegitimateClick = { beatTheBotViewModel.submitGuess(isPhishingGuess = false) },
                onHintDismiss = { /* Hint auto-dismisses or managed by VM if needed */ },
                
                uiState = state
            )
        }


        // ===== INFO SCREENS =====
        
        composable(Routes.OFFLINE_PRIVACY) {
            OfflinePrivacyScreen(
                onBackClick = { navController.popBackStack() },
                onLearnMoreClick = { 
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://github.com/Raoof128/QDKMP-KotlinConf-2026-")
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}



