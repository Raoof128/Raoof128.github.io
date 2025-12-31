package com.raouf.mehrguard.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.raouf.mehrguard.desktop.i18n.AppLanguage
import com.raouf.mehrguard.desktop.i18n.DesktopStrings
import com.raouf.mehrguard.desktop.theme.LocalStitchTokens

/**
 * Help Dialog showing keyboard shortcuts and app info.
 * Matches webapp's showHelpModal() in shared-ui.js
 */
@Composable
fun HelpDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    language: AppLanguage
) {
    if (!isVisible) return

    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.width(480.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MaterialSymbol(name = "help", size = 24.sp, color = colors.primary)
                        Text(
                            t("Help & Keyboard Shortcuts"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        MaterialSymbol(name = "close", size = 20.sp, color = colors.textSub)
                    }
                }

                // Keyboard Shortcuts Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        t("Keyboard Shortcuts"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMain
                    )
                    Text(
                        t("Press these keys when not typing in an input field:"),
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShortcutRow(label = t("Start Scanner"), key = "S", colors = colors)
                        ShortcutRow(label = t("Import Image"), key = "Cmd+I", colors = colors)
                        ShortcutRow(label = t("Navigate to Dashboard"), key = "D", colors = colors)
                        ShortcutRow(label = t("Scan History"), key = "H", colors = colors)
                        ShortcutRow(label = t("Trust Centre / Allow List"), key = "T", colors = colors)
                        ShortcutRow(label = t("Beat the Bot Game"), key = "G", colors = colors)
                        ShortcutRow(label = t("Paste & Analyze URL"), key = "Cmd+V", colors = colors)
                        ShortcutRow(label = t("Start Scan (in URL field)"), key = "Enter", colors = colors)
                        ShortcutRow(label = t("Close Menu / Modal"), key = "Escape", colors = colors)
                        ShortcutRow(label = t("Show Help"), key = "?", colors = colors)
                    }
                }

                HorizontalDivider(color = colors.border)

                // About Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        t("About Mehr Guard"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMain
                    )
                    Text(
                        t("Enterprise-grade QR code security with 100% offline analysis. Your data never leaves your device. All threat detection is performed locally using our advanced phishing detection engine."),
                        fontSize = 13.sp,
                        color = colors.textSub,
                        lineHeight = 20.sp
                    )
                }

                // Version info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.success)
                    )
                    Text(
                        t("Version 2025.12.29 • Offline Ready"),
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ShortcutRow(
    label: String,
    key: String,
    colors: com.raouf.mehrguard.desktop.theme.ColorTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = colors.textSub
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(colors.backgroundAlt)
                .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                key,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textMain
            )
        }
    }
}
