@file:Suppress("DEPRECATION") // painterResource - migration to Compose Resources planned

package com.raouf.mehrguard.desktop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raouf.mehrguard.desktop.i18n.AppLanguage
import com.raouf.mehrguard.desktop.i18n.DesktopStringKey
import com.raouf.mehrguard.desktop.i18n.DesktopStrings
import com.raouf.mehrguard.desktop.navigation.AppScreen
import com.raouf.mehrguard.desktop.theme.LocalStitchTokens

@Composable
fun AppSidebar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    language: AppLanguage,
    onProfileClick: (() -> Unit)? = null,
    userName: String = "Security Analyst",
    userRole: String = "Offline Operations",
    userInitials: String = "SA",
    modifier: Modifier = Modifier
) {
    val tokens = LocalStitchTokens.current
    val colors = tokens.colors
    val spacing = tokens.spacing
    val radius = tokens.radius
    val t = { text: String -> DesktopStrings.translate(text, language) }
    val activeScreen = when (currentScreen) {
        AppScreen.ResultSafe,
        AppScreen.ResultSuspicious,
        AppScreen.ResultDangerous,
        AppScreen.ResultDangerousAlt -> AppScreen.LiveScan
        else -> currentScreen
    }

    Column(
        modifier = modifier
            .width(256.dp)
            .fillMaxHeight()
            .background(colors.surface)
            .border(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .border(1.dp, colors.border)
                .padding(horizontal = spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Image(
                painter = painterResource("assets/app-icon.png"),
                contentDescription = "QR-SHIELD Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = DesktopStrings.text(DesktopStringKey.AppName, language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = spacing.md, vertical = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            SectionLabel(DesktopStrings.text(DesktopStringKey.MenuMain, language), colors.textMuted)
            SidebarItem(
                label = DesktopStrings.text(DesktopStringKey.NavDashboard, language),
                icon = "dashboard",
                active = activeScreen == AppScreen.Dashboard,
                colors = colors,
                radius = radius,
                onClick = { onNavigate(AppScreen.Dashboard) }
            )
            SidebarItem(
                label = DesktopStrings.text(DesktopStringKey.NavScanMonitor, language),
                icon = "qr_code_scanner",
                active = activeScreen == AppScreen.LiveScan,
                colors = colors,
                radius = radius,
                onClick = { onNavigate(AppScreen.LiveScan) }
            )
            SidebarItem(
                label = DesktopStrings.text(DesktopStringKey.NavScanHistory, language),
                icon = "history",
                active = activeScreen == AppScreen.ScanHistory,
                colors = colors,
                radius = radius,
                onClick = { onNavigate(AppScreen.ScanHistory) }
            )

            Spacer(modifier = Modifier.height(spacing.lg))
            SectionLabel(DesktopStrings.text(DesktopStringKey.MenuSecurity, language), colors.textMuted)
            SidebarItem(
                label = DesktopStrings.text(DesktopStringKey.NavTrustCentre, language),
                icon = "verified_user",
                active = activeScreen == AppScreen.TrustCentre,
                colors = colors,
                radius = radius,
                onClick = { onNavigate(AppScreen.TrustCentre) }
            )
            SidebarItem(
                label = DesktopStrings.text(DesktopStringKey.NavReports, language),
                icon = "description",
                active = activeScreen == AppScreen.ReportsExport,
                colors = colors,
                radius = radius,
                onClick = { onNavigate(AppScreen.ReportsExport) }
            )

            Spacer(modifier = Modifier.height(spacing.lg))
            SectionLabel(DesktopStrings.text(DesktopStringKey.MenuSystem, language), colors.textMuted)
            SidebarItem(
                label = DesktopStrings.text(DesktopStringKey.NavTraining, language),
                icon = "sports_esports",
                active = activeScreen == AppScreen.Training,
                colors = colors,
                radius = radius,
                onClick = { onNavigate(AppScreen.Training) }
            )
            SidebarItem(
                label = DesktopStrings.text(DesktopStringKey.NavSettings, language),
                icon = "settings",
                active = activeScreen == AppScreen.TrustCentreAlt,
                colors = colors,
                radius = radius,
                onClick = { onNavigate(AppScreen.TrustCentreAlt) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(radius.md))
                .background(colors.primary.copy(alpha = 0.05f))
                .border(1.dp, colors.border, RoundedCornerShape(radius.md))
                .clickable(enabled = onProfileClick != null) { onProfileClick?.invoke() }
                .focusable()
                .handCursor()
                .padding(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitials,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textMain
                )
                Text(
                    text = userRole,
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
            MaterialIconRound(name = "expand_more", size = 18.sp, color = colors.textMuted)
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
private fun SidebarItem(
    label: String,
    icon: String,
    active: Boolean,
    colors: com.raouf.mehrguard.desktop.theme.ColorTokens,
    radius: com.raouf.mehrguard.desktop.theme.RadiusTokens,
    onClick: () -> Unit
) {
    val bg = if (active) colors.primary.copy(alpha = 0.1f) else Color.Transparent
    val border = if (active) colors.primary.copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (active) colors.primary else colors.textSub

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.sm))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(radius.sm))
            .clickable { onClick() }
            .focusable()
            .handCursor()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MaterialIconRound(name = icon, size = 18.sp, color = textColor)
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
    }
}
