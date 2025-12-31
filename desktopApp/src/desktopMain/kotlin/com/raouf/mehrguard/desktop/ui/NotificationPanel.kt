package com.raouf.mehrguard.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.raouf.mehrguard.desktop.i18n.AppLanguage
import com.raouf.mehrguard.desktop.i18n.DesktopStrings
import com.raouf.mehrguard.desktop.theme.LocalStitchTokens
import com.raouf.mehrguard.platform.PlatformTime

/**
 * Notification data class using Long timestamp (epoch millis)
 */
data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long, // epoch milliseconds
    val isRead: Boolean = false,
    val scanUrl: String? = null // URL of the associated scan for navigation
)

enum class NotificationType {
    SUCCESS, INFO, WARNING, ERROR
}

/**
 * Notification Panel Popup shown when clicking the notification bell icon.
 */
@Composable
fun NotificationPanel(
    visible: Boolean,
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit,
    onClearAll: () -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = -60, y = 50),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = modifier
                .width(340.dp)
                .shadow(16.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.backgroundAlt)
                        .border(1.dp, colors.border.copy(alpha = 0.3f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MaterialIconRound(name = "notifications", size = 18.sp, color = colors.primary)
                        Text(
                            text = t("Notifications"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMain
                        )
                        val unreadCount = notifications.count { !it.isRead }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(colors.primary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    if (notifications.any { !it.isRead }) {
                        Text(
                            text = t("Mark all read"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary,
                            modifier = Modifier
                                .clickable { onMarkAllRead() }
                                .focusable()
                        )
                    }
                }

                // Notification List
                if (notifications.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MaterialIconRound(name = "notifications_none", size = 40.sp, color = colors.textMuted)
                        Text(
                            text = t("No notifications"),
                            fontSize = 14.sp,
                            color = colors.textSub
                        )
                        Text(
                            text = t("You're all caught up!"),
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        notifications.forEach { notification ->
                            NotificationItem(
                                notification = notification,
                                onClick = { onNotificationClick(notification) },
                                language = language
                            )
                        }
                    }

                    // Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.backgroundAlt.copy(alpha = 0.5f))
                            .border(1.dp, colors.border.copy(alpha = 0.2f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = t("Clear all notifications"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSub,
                            modifier = Modifier
                                .clickable { onClearAll() }
                                .focusable()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: AppNotification,
    onClick: () -> Unit,
    language: AppLanguage
) {
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val colors = LocalStitchTokens.current.colors
    
    val (iconName, iconBg, iconColor) = when (notification.type) {
        NotificationType.SUCCESS -> Triple("check_circle", colors.success.copy(alpha = 0.1f), colors.success)
        NotificationType.INFO -> Triple("info", colors.primary.copy(alpha = 0.1f), colors.primary)
        NotificationType.WARNING -> Triple("warning", colors.warning.copy(alpha = 0.1f), colors.warning)
        NotificationType.ERROR -> Triple("error", colors.danger.copy(alpha = 0.1f), colors.danger)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) colors.primary.copy(alpha = 0.03f) else Color.Transparent)
            .clickable { onClick() }
            .focusable()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Unread indicator
        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.primary)
                    .align(Alignment.Top)
            )
        } else {
            Spacer(modifier = Modifier.width(6.dp))
        }

        // Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            MaterialIconRound(name = iconName, size = 18.sp, color = iconColor)
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = t(notification.title),
                fontSize = 13.sp,
                fontWeight = if (!notification.isRead) FontWeight.SemiBold else FontWeight.Medium,
                color = colors.textMain
            )
            Text(
                text = t(notification.message),
                fontSize = 12.sp,
                color = colors.textSub,
                lineHeight = 16.sp
            )
            Text(
                text = PlatformTime.formatRelativeTime(notification.timestamp),
                fontSize = 10.sp,
                color = colors.textMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
