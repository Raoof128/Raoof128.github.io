package com.qrshield.desktop.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.qrshield.data.ScanHistoryManager
import com.qrshield.desktop.SampleData
import com.qrshield.desktop.i18n.AppLanguage
import com.qrshield.desktop.i18n.DesktopStrings
import com.qrshield.desktop.theme.LocalStitchTokens

/**
 * Profile dropdown panel showing user info and quick stats
 */
@Composable
fun ProfileDropdown(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    userName: String,
    userRole: String,
    userInitials: String,
    historyStats: ScanHistoryManager.HistoryStatistics,
    onViewProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    language: AppLanguage
) {
    if (!isVisible) return
    
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    val userProfile = SampleData.userProfile
    
    Popup(
        alignment = Alignment.BottomStart,
        offset = IntOffset(0, -8),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with avatar and info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Column {
                        Text(
                            text = userName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMain
                        )
                        Text(
                            text = userRole,
                            fontSize = 12.sp,
                            color = colors.textSub
                        )
                    }
                }
                
                HorizontalDivider(color = colors.border, thickness = 1.dp)
                
                // Quick Stats
                Text(
                    text = t("Quick Stats"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    letterSpacing = 1.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip(
                        value = historyStats.totalScans.toString(),
                        label = t("Total"),
                        color = colors.primary
                    )
                    StatChip(
                        value = historyStats.safeCount.toString(),
                        label = t("Safe"),
                        color = colors.success
                    )
                    StatChip(
                        value = (historyStats.suspiciousCount + historyStats.maliciousCount).toString(),
                        label = t("Threats"),
                        color = colors.danger
                    )
                }
                
                HorizontalDivider(color = colors.border, thickness = 1.dp)
                
                // Menu Items
                ProfileMenuItem(
                    icon = "person",
                    label = t("View Profile"),
                    onClick = {
                        onDismiss()
                        onViewProfile()
                    }
                )
                
                ProfileMenuItem(
                    icon = "settings",
                    label = t("Settings"),
                    onClick = {
                        onDismiss()
                        onOpenSettings()
                    }
                )
                
                HorizontalDivider(color = colors.border, thickness = 1.dp)
                
                // Account plan badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.primary.copy(alpha = 0.1f))
                        .border(1.dp, colors.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialSymbol(
                        name = "workspace_premium",
                        size = 18.sp,
                        color = colors.primary
                    )
                    Column {
                        Text(
                            text = t("Enterprise Plan"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary
                        )
                        Text(
                            text = t("Unlimited scans"),
                            fontSize = 10.sp,
                            color = colors.textSub
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    value: String,
    label: String,
    color: Color
) {
    val colors = LocalStitchTokens.current.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = colors.textMuted
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    val colors = LocalStitchTokens.current.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .focusable()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialSymbol(
            name = icon,
            size = 18.sp,
            color = colors.textSub
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = colors.textMain
        )
    }
}
