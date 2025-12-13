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

package com.qrshield.android.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.qrshield.android.BuildConfig
import com.qrshield.android.R
import com.qrshield.android.ui.theme.*

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
fun SettingsScreen() {
    val context = LocalContext.current
    val viewModel: com.qrshield.ui.SharedViewModel = org.koin.compose.koinInject()
    val settings by viewModel.settings.collectAsState()

    // Derived states for UI
    val hapticEnabled = settings.isHapticEnabled
    val soundEnabled = settings.isSoundEnabled
    val autoScan = settings.isAutoScanEnabled
    val saveHistory = settings.isSaveHistoryEnabled
    val notificationsEnabled = settings.isSecurityAlertsEnabled

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        Color(0xFF13171F)
                    )
                )
            )
            .semantics {
                contentDescription = "Settings screen with app preferences"
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
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
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
                        contentDescription = "Open system settings",
                        tint = BrandPrimary,
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
                        contentDescription = "Open privacy policy",
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
                        contentDescription = "Open accessibility settings",
                        tint = BrandPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        // About Section
        item {
            SettingsSection(title = stringResource(R.string.settings_about))
        }

        item {
            SettingsInfo(
                icon = Icons.Default.Info,
                title = stringResource(R.string.settings_version),
                value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )
        }

        item {
            SettingsInfo(
                icon = Icons.Default.Build,
                title = stringResource(R.string.settings_build),
                value = "Android ${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}"
            )
        }

        item {
            SettingsInfo(
                icon = Icons.Default.Memory,
                title = stringResource(R.string.settings_engine),
                value = "KMP PhishingEngine v1.0"
            )
        }

        item {
            SettingsClickable(
                icon = Icons.Default.Code,
                title = stringResource(R.string.settings_source_code),
                subtitle = "GitHub",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://github.com/Raoof128/QDKMP-KotlinConf-2026-")
                    }
                    context.startActivity(intent)
                },
                trailing = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open GitHub",
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
                        contentDescription = "Open help",
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
                    color = TextPrimary
                )

                Text(
                    text = "Kotlin Multiplatform QRishing Detector",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Made with ❤️ for KotlinConf 2026",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Platform badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlatformBadge("Android 16", BrandPrimary)
                    PlatformBadge("Kotlin 1.9.22", BrandSecondary)
                }
            }
        }
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
                color = TextPrimary,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BrandPrimary,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = BackgroundCard
            )
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
                color = TextPrimary,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = TextMuted,
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
            color = TextPrimary,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun PlatformBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp)
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
