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

package com.qrshield.desktop.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.qrshield.desktop.model.AnalysisResult
import com.qrshield.desktop.theme.DesktopColors
import com.qrshield.model.Verdict
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.imageio.ImageIO
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer

/**
 * Advanced Desktop-specific features matching mobile/web parity.
 *
 * Features:
 * - QR Code Image Upload & Decode
 * - Judge Mode Toggle
 * - Settings Dialog
 * - Share/Copy Result
 * - About Dialog
 *
 * @author QR-SHIELD Team
 * @since 1.1.4
 */

// ============================================
// QR IMAGE UPLOAD & DECODE
// ============================================

/**
 * Decode QR code from an image file using ZXing.
 */
fun decodeQrFromImage(file: File): String? {
    return try {
        val image = ImageIO.read(file) ?: return null
        val source = BufferedImageLuminanceSource(image)
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val result = MultiFormatReader().decode(bitmap)
        result.text
    } catch (e: NotFoundException) {
        null // No QR code found
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Open a file dialog to select an image containing a QR code.
 */
fun openImageFileDialog(): File? {
    val dialog = FileDialog(null as Frame?, "Select QR Code Image", FileDialog.LOAD)
    dialog.setFilenameFilter { _, name ->
        name.lowercase().endsWith(".png") ||
        name.lowercase().endsWith(".jpg") ||
        name.lowercase().endsWith(".jpeg") ||
        name.lowercase().endsWith(".gif") ||
        name.lowercase().endsWith(".bmp")
    }
    dialog.isVisible = true
    
    val directory = dialog.directory
    val file = dialog.file
    
    return if (directory != null && file != null) {
        File(directory, file)
    } else {
        null
    }
}

@Composable
fun UploadQrButton(
    onUrlDecoded: (String) -> Unit,
    onError: (String) -> Unit
) {
    Button(
        onClick = {
            val file = openImageFileDialog()
            if (file != null) {
                val decodedUrl = decodeQrFromImage(file)
                if (decodedUrl != null) {
                    onUrlDecoded(decodedUrl)
                } else {
                    onError("No QR code found in image")
                }
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = "📷", fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Upload QR Image")
    }
}

// ============================================
// JUDGE MODE
// ============================================

/**
 * Judge Mode state holder.
 */
object JudgeMode {
    var isEnabled by mutableStateOf(false)
}

@Composable
fun JudgeModeToggle() {
    val isEnabled = JudgeMode.isEnabled

    Surface(
        onClick = { JudgeMode.isEnabled = !JudgeMode.isEnabled },
        shape = RoundedCornerShape(12.dp),
        color = if (isEnabled) DesktopColors.VerdictMalicious.copy(alpha = 0.15f)
               else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isEnabled) DesktopColors.VerdictMalicious.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚖️", fontSize = 16.sp)
            Text(
                text = if (isEnabled) "Judge Mode ON" else "Judge Mode",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (isEnabled) DesktopColors.VerdictMalicious
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================
// SHARE/COPY RESULT
// ============================================

/**
 * Copy analysis result to clipboard.
 */
fun copyResultToClipboard(result: AnalysisResult): Boolean {
    return try {
        val text = buildString {
            appendLine("🛡️ QR-SHIELD Analysis Report")
            appendLine("═══════════════════════════")
            appendLine()
            appendLine("📎 URL: ${result.url}")
            appendLine("📊 Score: ${result.score}/100")
            appendLine("🎯 Verdict: ${result.verdict}")
            appendLine()
            if (result.flags.isNotEmpty()) {
                appendLine("⚠️ Risk Factors:")
                result.flags.forEach { flag ->
                    appendLine("  • $flag")
                }
            } else {
                appendLine("✅ No risk factors detected")
            }
            appendLine()
            appendLine("Analyzed by QR-SHIELD Desktop • ${java.time.LocalDateTime.now()}")
        }
        
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
        true
    } catch (e: Exception) {
        false
    }
}

@Composable
fun ShareResultButton(
    result: AnalysisResult,
    onCopied: () -> Unit
) {
    Button(
        onClick = {
            if (copyResultToClipboard(result)) {
                onCopied()
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = "📋", fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copy Report")
    }
}

// ============================================
// ABOUT DIALOG
// ============================================

@Composable
fun AboutDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.width(400.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        DesktopColors.BrandPrimary,
                                        DesktopColors.BrandSecondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🛡️", fontSize = 40.sp)
                    }

                    // Title
                    Text(
                        text = "QR-SHIELD",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Desktop Edition",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Info rows
                    AboutInfoRow("Version", "1.1.4")
                    AboutInfoRow("Build", "Desktop • JVM ${System.getProperty("java.version")}")
                    AboutInfoRow("Engine", "KMP PhishingEngine v1.0")
                    AboutInfoRow("Platform", System.getProperty("os.name"))

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    // Credits
                    Text(
                        text = "Made with ❤️ for KotlinConf 2026",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AboutBadge("Kotlin 1.9", DesktopColors.BrandPrimary)
                        AboutBadge("Compose MP", DesktopColors.BrandSecondary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Links
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(onClick = {
                            openUrl("https://github.com/Raoof128/QDKMP-KotlinConf-2026-")
                        }) {
                            Text("GitHub")
                        }
                        TextButton(onClick = {
                            openUrl("https://github.com/Raoof128/QDKMP-KotlinConf-2026-/issues")
                        }) {
                            Text("Report Issue")
                        }
                    }

                    // Close button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun AboutInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AboutBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

private fun openUrl(url: String) {
    try {
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().browse(java.net.URI(url))
        }
    } catch (e: Exception) {
        // Ignore
    }
}

// ============================================
// SETTINGS DIALOG
// ============================================

@Composable
fun SettingsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.width(420.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚙️ Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = onDismiss) {
                            Text("✕", fontSize = 18.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Appearance Section
                    SettingsSection(title = "Appearance", icon = "🎨")
                    
                    SettingsToggleRow(
                        icon = "🌙",
                        title = "Dark Mode",
                        subtitle = "Use dark color scheme",
                        isChecked = isDarkMode,
                        onCheckedChange = onDarkModeChange
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Privacy Section
                    SettingsSection(title = "Privacy", icon = "🔒")

                    Button(
                        onClick = {
                            onClearHistory()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🗑️ Clear All History")
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // About Section
                    SettingsSection(title = "About", icon = "ℹ️")

                    SettingsInfoRow("Version", "1.1.4")
                    SettingsInfoRow("Engine", "KMP PhishingEngine")
                    SettingsInfoRow("License", "Apache 2.0")

                    // Close
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, icon: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = DesktopColors.BrandPrimary
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: String,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
