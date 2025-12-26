package com.qrshield.desktop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.theme.LocalStitchTokens

/**
 * Confirmation Dialog for destructive actions.
 * 
 * Matches Web app design patterns for:
 * - Clear scan history
 * - Reset settings
 * - Delete data
 */
@Composable
fun ConfirmationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isDangerous: Boolean = true,
    icon: String = "warning",
    language: AppLanguage
) {
    if (!isVisible) return

    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    val accentColor = if (isDangerous) colors.danger else colors.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.width(380.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.1f)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MaterialSymbol(
                            name = icon,
                            size = 24.sp,
                            color = accentColor
                        )
                    }
                }

                // Title
                Text(
                    text = t(title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain,
                    textAlign = TextAlign.Center
                )

                // Message
                Text(
                    text = t(message),
                    fontSize = 14.sp,
                    color = colors.textSub,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                HorizontalDivider(color = colors.border)

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.handCursor()
                    ) {
                        Text(
                            text = t(cancelText),
                            color = colors.textSub
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Confirm Button
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.handCursor()
                    ) {
                        MaterialSymbol(
                            name = if (isDangerous) "delete" else "check",
                            size = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t(confirmText),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
