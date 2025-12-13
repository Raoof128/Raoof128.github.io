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

package com.qrshield.android.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.qrshield.android.MainActivity
import com.qrshield.android.R

/**
 * QR-SHIELD Quick Scan Widget for Android 16
 *
 * Features:
 * - One-tap to open scanner
 * - Shows last scan result
 * - Material 3 Glance design
 * - Baseline Profile optimized
 */
class QRShieldWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(100.dp, 100.dp),
            DpSize(160.dp, 100.dp),
            DpSize(250.dp, 100.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QRShieldWidgetContent()
        }
    }

    @Composable
    private fun QRShieldWidgetContent() {
        val size = LocalSize.current

        GlanceTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColors.Background)
                    .appWidgetBackground()
                    .cornerRadius(24.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                when {
                    size.width >= 250.dp -> LargeWidgetLayout()
                    size.width >= 160.dp -> MediumWidgetLayout()
                    else -> SmallWidgetLayout()
                }
            }
        }
    }

    @Composable
    private fun SmallWidgetLayout() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🛡️",
                style = TextStyle(fontSize = 32.sp)
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Scan",
                style = TextStyle(
                    color = ColorProvider(WidgetColors.Primary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }

    @Composable
    private fun MediumWidgetLayout() {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🛡️",
                style = TextStyle(fontSize = 36.sp)
            )
            Spacer(modifier = GlanceModifier.width(12.dp))
            Column {
                Text(
                    text = "QR-SHIELD",
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.TextPrimary),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Tap to Scan",
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.Primary),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }

    @Composable
    private fun LargeWidgetLayout() {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Logo
            Box(
                modifier = GlanceModifier
                    .size(56.dp)
                    .background(WidgetColors.PrimaryContainer)
                    .cornerRadius(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛡️",
                    style = TextStyle(fontSize = 28.sp)
                )
            }

            Spacer(modifier = GlanceModifier.width(16.dp))

            // Center - Text
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = "QR-SHIELD",
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.TextPrimary),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Detect phishing in QR codes",
                    style = TextStyle(
                        color = ColorProvider(WidgetColors.TextSecondary),
                        fontSize = 12.sp
                    )
                )
            }

            // Right side - Action button
            Box(
                modifier = GlanceModifier
                    .size(44.dp)
                    .background(WidgetColors.Primary)
                    .cornerRadius(12.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📷",
                    style = TextStyle(fontSize = 20.sp)
                )
            }
        }
    }
}

/**
 * Widget color scheme matching the app theme.
 */
private object WidgetColors {
    val Background = Color(0xFF0D1117)
    val Primary = Color(0xFF6C5CE7)
    val PrimaryContainer = Color(0xFF2D2060)
    val TextPrimary = Color(0xFFF0F6FC)
    val TextSecondary = Color(0xFF8B949E)
}

/**
 * Widget receiver for system broadcasts.
 */
class QRShieldWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QRShieldWidget()
}

/**
 * Callback for widget actions.
 */
class ScanActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ACTION", "SCAN")
        }
        context.startActivity(intent)
    }
}
