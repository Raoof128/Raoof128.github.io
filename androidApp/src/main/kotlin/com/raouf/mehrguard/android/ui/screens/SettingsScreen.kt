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

package com.raouf.mehrguard.android.ui.screens

import com.raouf.mehrguard.android.ui.components.MehrGuardToggle

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raouf.mehrguard.android.BuildConfig
import com.raouf.mehrguard.android.R
import com.raouf.mehrguard.android.ui.theme.*
import com.raouf.mehrguard.verification.SystemIntegrityVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Settings Screen with app preferences and information.
 *
 * Features:
 * - Toggle settings for haptics, sounds, auto-scan
 * - Privacy controls
 * - App information
 * - Links to system settings
 * - Full accessibility support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel: com.raouf.mehrguard.ui.SharedViewModel = org.koin.compose.koinInject()
    val settings by viewModel.settings.collectAsState()

    // Developer Mode 7-Tap Counter
    var developerTapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    // System Integrity Verification State
    var isVerifying by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<SystemIntegrityVerifier.VerificationResult?>(null) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Derived states for UI
    val hapticEnabled = settings.isHapticEnabled
    val soundEnabled = settings.isSoundEnabled
    val autoScan = settings.isAutoScanEnabled
    val saveHistory = settings.isSaveHistoryEnabled
    val developerModeEnabled = settings.isDeveloperModeEnabled
    val notificationsEnabled = settings.isSecurityAlertsEnabled
    
    // Language picker dialog state
    var showLanguageDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics {
                contentDescription = context.getString(R.string.cd_settings_screen)
            },
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Title
        item {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_settings),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }

        // Quick Actions Section - Matches iOS SettingsView
        item {
            SettingsSection(title = stringResource(R.string.settings_quick_actions))
        }

        item {
            QuickActionRow(
                icon = Icons.Default.GppBad,
                iconBackgroundColor = VerdictDanger.copy(alpha = 0.15f),
                iconColor = VerdictDanger,
                title = stringResource(R.string.settings_threat_monitor),
                subtitle = stringResource(R.string.settings_threat_monitor_desc),
                onClick = { /* Navigate to Threat Monitor */ }
            )
        }

        item {
            QuickActionRow(
                icon = Icons.Default.VerifiedUser,
                iconBackgroundColor = VerdictSafe.copy(alpha = 0.15f),
                iconColor = VerdictSafe,
                title = stringResource(R.string.settings_trust_centre),
                subtitle = stringResource(R.string.settings_trust_centre_desc),
                onClick = { /* Navigate to Trust Centre */ }
            )
        }

        item {
            QuickActionRow(
                icon = Icons.Default.Description,
                iconBackgroundColor = BrandPrimary.copy(alpha = 0.15f),
                iconColor = BrandPrimary,
                title = stringResource(R.string.settings_export_report),
                subtitle = stringResource(R.string.settings_export_report_desc),
                onClick = { /* Navigate to Export Report */ }
            )
        }

        // Scanning Section
        item {
            SettingsSection(title = stringResource(R.string.settings_scanning))
        }

        item {
            SettingsToggle(
                icon = Icons.Default.QrCodeScanner,
                title = stringResource(R.string.settings_auto_scan),
                subtitle = stringResource(R.string.settings_auto_scan_desc),
                checked = autoScan,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings(settings.copy(isAutoScanEnabled = newValue))
                }
            )
        }

        item {
            SettingsToggle(
                icon = Icons.Default.Vibration,
                title = stringResource(R.string.settings_haptic),
                subtitle = stringResource(R.string.settings_haptic_desc),
                checked = hapticEnabled,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings(settings.copy(isHapticEnabled = newValue))
                }
            )
        }

        item {
            SettingsToggle(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = stringResource(R.string.settings_sound),
                subtitle = stringResource(R.string.settings_sound_desc),
                checked = soundEnabled,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings(settings.copy(isSoundEnabled = newValue))
                }
            )
        }

        // Notifications Section
        item {
            SettingsSection(title = stringResource(R.string.settings_notifications))
        }

        item {
            SettingsToggle(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.settings_security_alerts),
                subtitle = stringResource(R.string.settings_security_alerts_desc),
                checked = notificationsEnabled,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings(settings.copy(isSecurityAlertsEnabled = newValue))
                }
            )
        }

        item {
            SettingsClickable(
                icon = Icons.Default.NotificationsActive,
                title = stringResource(R.string.settings_system_notifications),
                subtitle = stringResource(R.string.settings_system_notifications_desc),
                onClick = {
                    val intent = Intent().apply {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.cd_open_system_settings),
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        // Appearance Section (iOS Parity)
        item {
            SettingsSection(title = stringResource(R.string.settings_appearance))
        }

        item {
            SettingsToggle(
                icon = Icons.Default.DarkMode,
                title = stringResource(R.string.settings_dark_mode),
                subtitle = stringResource(R.string.settings_dark_mode_desc),
                checked = settings.isDarkModeEnabled,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings(settings.copy(isDarkModeEnabled = newValue))
                }
            )
        }

        item {
            SettingsToggle(
                icon = Icons.Default.AutoAwesome,
                title = stringResource(R.string.settings_reduced_effects),
                subtitle = stringResource(R.string.settings_reduced_effects_desc),
                checked = settings.isReducedEffectsEnabled,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings(settings.copy(isReducedEffectsEnabled = newValue))
                }
            )
        }

        item {
            SettingsClickable(
                icon = Icons.Default.DisplaySettings,
                title = stringResource(R.string.settings_system_appearance),
                subtitle = stringResource(R.string.settings_system_appearance_desc),
                onClick = {
                    val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
                    context.startActivity(intent)
                },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.cd_open_system_settings),
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        // Language Selection
        item {
            // Get current app locale, default to English if using system default
            val appLocales = AppCompatDelegate.getApplicationLocales()
            val currentLanguageCode = if (appLocales.isEmpty) {
                // System default - treat as English or get actual system language
                context.resources.configuration.locales[0].language.takeIf { it in listOf("en", "de", "es", "fr", "it", "pt", "ru", "zh", "ja", "ko", "hi", "ar", "tr", "vi", "in", "id", "th") } ?: "en"
            } else {
                appLocales[0]?.language ?: "en"
            }
            
            // Show the language name directly (no "System Default" prefix)
            val currentLanguageName = when (currentLanguageCode) {
                "en" -> stringResource(R.string.language_english)
                "de" -> stringResource(R.string.language_german)
                "es" -> stringResource(R.string.language_spanish)
                "fr" -> stringResource(R.string.language_french)
                "it" -> stringResource(R.string.language_italian)
                "pt" -> stringResource(R.string.language_portuguese)
                "ru" -> stringResource(R.string.language_russian)
                "zh" -> stringResource(R.string.language_chinese)
                "ja" -> stringResource(R.string.language_japanese)
                "ko" -> stringResource(R.string.language_korean)
                "hi" -> stringResource(R.string.language_hindi)
                "ar" -> stringResource(R.string.language_arabic)
                "tr" -> stringResource(R.string.language_turkish)
                "vi" -> stringResource(R.string.language_vietnamese)
                "in", "id" -> stringResource(R.string.language_indonesian)
                "th" -> stringResource(R.string.language_thai)
                else -> stringResource(R.string.language_english)
            }
            
            SettingsClickable(
                icon = Icons.Default.Language,
                title = stringResource(R.string.settings_language),
                subtitle = currentLanguageName,
                onClick = { showLanguageDialog = true },
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        // Privacy Section
        item {
            SettingsSection(title = stringResource(R.string.settings_privacy))
        }

        item {
            SettingsToggle(
                icon = Icons.Default.History,
                title = stringResource(R.string.settings_save_history),
                subtitle = stringResource(R.string.settings_save_history_desc),
                checked = saveHistory,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings(settings.copy(isSaveHistoryEnabled = newValue))
                }
            )
        }

        // Aggressive Mode (URL Unshortener)
        item {
            SettingsToggle(
                icon = Icons.Default.LinkOff,
                title = stringResource(R.string.url_unshortener_title),
                subtitle = stringResource(R.string.url_unshortener_desc),
                checked = settings.isAggressiveModeEnabled,
                onCheckedChange = { newValue ->
                    viewModel.updateSettings(settings.copy(isAggressiveModeEnabled = newValue))
                }
            )
        }

        item {
            SettingsClickable(
                icon = Icons.Default.PrivacyTip,
                title = stringResource(R.string.settings_privacy_policy),
                subtitle = stringResource(R.string.settings_privacy_policy_desc),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://github.com/Raoof128/QDKMP-KotlinConf-2026-")
                    }
                    context.startActivity(intent)
                },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.cd_open_privacy_policy),
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        // Accessibility Section
        item {
            SettingsSection(title = stringResource(R.string.settings_accessibility))
        }

        item {
            SettingsClickable(
                icon = Icons.Default.Accessibility,
                title = stringResource(R.string.settings_accessibility_settings),
                subtitle = stringResource(R.string.settings_accessibility_settings_desc),
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.cd_open_accessibility),
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        // Developer Mode Section (only visible when enabled)
        if (developerModeEnabled) {
            item {
                SettingsSection(title = stringResource(R.string.dev_mode_section))
            }

            item {
                SettingsToggle(
                    icon = Icons.Default.BugReport,
                    title = stringResource(R.string.red_team_mode),
                    subtitle = stringResource(R.string.red_team_mode_desc),
                    checked = developerModeEnabled,
                    onCheckedChange = { newValue ->
                        viewModel.updateSettings(settings.copy(isDeveloperModeEnabled = newValue))
                        if (!newValue) {
                            Toast.makeText(context, context.getString(R.string.dev_mode_disabled_toast), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            item {
                // Warning card for developer mode
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF442222)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.red_team_warning_text),
                            color = Color(0xFFFFAAAA),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // About Section
        item {
            SettingsSection(title = stringResource(R.string.settings_about))
        }

        // Version info with 7-tap developer mode activation
        item {
            SettingsInfoClickable(
                icon = Icons.Default.Info,
                title = stringResource(R.string.settings_version),
                value = if (developerModeEnabled) {
                    "${BuildConfig.VERSION_NAME} ${stringResource(R.string.dev_mode_version_suffix)}"
                } else {
                    "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                },
                onClick = {
                    val currentTime = System.currentTimeMillis()

                    // Reset counter if more than 2 seconds between taps
                    if (currentTime - lastTapTime > 2000) {
                        developerTapCount = 0
                    }

                    lastTapTime = currentTime
                    developerTapCount++

                    when {
                        developerTapCount >= 7 -> {
                            // Toggle developer mode
                            val newDevMode = !settings.isDeveloperModeEnabled
                            viewModel.updateSettings(settings.copy(isDeveloperModeEnabled = newDevMode))
                            developerTapCount = 0

                            if (newDevMode) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.dev_mode_enabled_toast),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.dev_mode_disabled_toast),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        developerTapCount >= 4 -> {
                            val remaining = 7 - developerTapCount
                            val action = if (developerModeEnabled) context.getString(R.string.dev_mode_disable) else context.getString(R.string.dev_mode_enable)
                            Toast.makeText(
                                context,
                                context.getString(R.string.dev_mode_tap_hint, remaining, action),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }

        item {
            SettingsInfo(
                icon = Icons.Default.Build,
                title = stringResource(R.string.settings_build),
                value = stringResource(R.string.settings_build_fmt, Build.VERSION.RELEASE, Build.VERSION.SDK_INT)
            )
        }

        item {
            SettingsInfo(
                icon = Icons.Default.Memory,
                title = stringResource(R.string.settings_engine),
                value = stringResource(R.string.settings_engine_desc)
            )
        }

        // System Integrity Verification - "The Receipt"
        item {
            SettingsClickable(
                icon = Icons.Default.VerifiedUser,
                title = stringResource(R.string.verify_integrity_title),
                subtitle = if (isVerifying) stringResource(R.string.verify_integrity_desc_running) else stringResource(R.string.verify_integrity_desc_default),
                onClick = {
                    if (!isVerifying) {
                        isVerifying = true
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.Default) {
                                SystemIntegrityVerifier().verify()
                            }
                            verificationResult = result
                            isVerifying = false
                            showVerificationDialog = true
                        }
                    }
                },
                trailing = {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = BrandSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.cd_run_verification),
                            tint = BrandSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }

        item {
            SettingsClickable(
                icon = Icons.Default.Code,
                title = stringResource(R.string.settings_source_code),
                subtitle = stringResource(R.string.subtitle_github),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://github.com/Raoof128/QDKMP-KotlinConf-2026-")
                    }
                    context.startActivity(intent)
                },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.cd_open_github),
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        item {
            SettingsClickable(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                title = stringResource(R.string.settings_help),
                subtitle = stringResource(R.string.settings_help_desc),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://github.com/Raoof128/QDKMP-KotlinConf-2026-/issues")
                    }
                    context.startActivity(intent)
                },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = stringResource(R.string.cd_open_help),
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        // Credits Footer
        item {
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛡️",
                        fontSize = 40.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.about_app_desc),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.about_made_with_love),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Platform badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlatformBadge(stringResource(R.string.platform_android), BrandPrimary)
                    PlatformBadge(stringResource(R.string.platform_kotlin), BrandSecondary)
                }
            }
        }
    }
    
    // Verification Result Dialog
    if (showVerificationDialog && verificationResult != null) {
        val result = verificationResult!!
        AlertDialog(
            onDismissRequest = { showVerificationDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = {
                Icon(
                    imageVector = if (result.isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (result.isHealthy) VerdictSafe else VerdictWarning,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = if (result.isHealthy) stringResource(R.string.verification_dialog_healthy) else stringResource(R.string.verification_dialog_complete),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pass/Fail Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.isHealthy) VerdictSafe.copy(alpha = 0.15f) 
                                             else VerdictWarning.copy(alpha = 0.15f)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.verification_passed_tests, result.passed, result.totalTests),
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (result.isHealthy) VerdictSafe else VerdictWarning
                        )
                    }
                    
                    // Confusion Matrix
                    Text(stringResource(R.string.confusion_matrix), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricBox("TP", result.truePositives, VerdictSafe)
                        MetricBox("FP", result.falsePositives, VerdictDanger)
                        MetricBox("FN", result.falseNegatives, VerdictWarning)
                        MetricBox("TN", result.trueNegatives, VerdictSafe)
                    }
                    
                    HorizontalDivider(color = TextMuted.copy(alpha = 0.3f))
                    
                    // Metrics
                    Text(stringResource(R.string.metrics_title), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(R.string.metric_accuracy), fontSize = 11.sp, color = TextMuted)
                            Text("${String.format("%.1f", result.accuracy * 100)}%", 
                                fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text(stringResource(R.string.metric_precision), fontSize = 11.sp, color = TextMuted)
                            Text("${String.format("%.1f", result.precision * 100)}%",
                                fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text(stringResource(R.string.metric_recall), fontSize = 11.sp, color = TextMuted)
                            Text("${String.format("%.1f", result.recall * 100)}%",
                                fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text(stringResource(R.string.metric_f1_score), fontSize = 11.sp, color = TextMuted)
                            Text("${String.format("%.2f", result.f1Score)}",
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    
                    // Execution time
                    Text(
                        text = stringResource(R.string.execution_time_fmt, result.executionTimeMs),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVerificationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text(stringResource(R.string.action_close))
                }
            }
        )
    }
    
    // Language Selection Dialog
    if (showLanguageDialog) {
        // All 15 supported languages (no "System Default" - English is the default)
        val languages = listOf(
            "en" to stringResource(R.string.language_english),
            "de" to stringResource(R.string.language_german),
            "es" to stringResource(R.string.language_spanish),
            "fr" to stringResource(R.string.language_french),
            "it" to stringResource(R.string.language_italian),
            "pt" to stringResource(R.string.language_portuguese),
            "ru" to stringResource(R.string.language_russian),
            "zh" to stringResource(R.string.language_chinese),
            "ja" to stringResource(R.string.language_japanese),
            "ko" to stringResource(R.string.language_korean),
            "hi" to stringResource(R.string.language_hindi),
            "ar" to stringResource(R.string.language_arabic),
            "tr" to stringResource(R.string.language_turkish),
            "vi" to stringResource(R.string.language_vietnamese),
            "in" to stringResource(R.string.language_indonesian),
            "th" to stringResource(R.string.language_thai)
        )
        
        // Get current app locale (default to 'en' if system default)
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val currentLocale = if (appLocales.isEmpty) {
            "en" // Treat system default as English
        } else {
            appLocales[0]?.language ?: "en"
        }
        
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_select_language),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(languages.size) { index ->
                        val (code, name) = languages[index]
                        val isSelected = code == currentLocale || 
                            (code == "in" && currentLocale == "id") ||
                            (code == "en" && currentLocale == "en")
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Change app language directly
                                    val localeList = LocaleListCompat.forLanguageTags(code)
                                    AppCompatDelegate.setApplicationLocales(localeList)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) BrandPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = BrandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (index < languages.size - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

// =============================================================================
// SETTINGS COMPONENTS
// =============================================================================

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        color = BrandPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(top = 16.dp)
    )
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = "$title, ${if (checked) "enabled" else "disabled"}. $subtitle"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        MehrGuardToggle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsClickable(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = "$title. $subtitle. Tap to open."
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        trailing?.invoke()
    }
}

@Composable
private fun SettingsInfo(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = "$title: $value"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PlatformBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MehrGuardShapes.Card
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SettingsInfoClickable(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = "$title: $value. Tap to interact."
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

/**
 * Metric box for confusion matrix display.
 */
@Composable
private fun MetricBox(label: String, value: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Quick Action Row - Matches iOS Settings Quick Actions
 * Features icon with colored background, title, subtitle, and chevron
 */
@Composable
private fun QuickActionRow(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics {
                contentDescription = "$title. $subtitle. Tap to open."
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with colored background (matching iOS)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}
