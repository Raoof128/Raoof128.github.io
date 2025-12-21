package com.qrshield.desktop.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

enum class IconFont {
    MaterialIcons,
    MaterialIconsRound,
    MaterialSymbolsOutlined
}

private fun iconForName(name: String): ImageVector? {
    return when (name) {
        "abc" -> Icons.Outlined.Abc
        "add" -> Icons.Outlined.Add
        "analytics" -> Icons.Outlined.Analytics
        "arrow_forward" -> Icons.Outlined.ArrowForward
        "block" -> Icons.Outlined.Block
        "bolt" -> Icons.Outlined.Bolt
        "calendar_today" -> Icons.Outlined.CalendarToday
        "center_focus_weak" -> Icons.Outlined.CenterFocusWeak
        "check" -> Icons.Outlined.Check
        "check_circle" -> Icons.Outlined.CheckCircle
        "chevron_left" -> Icons.Outlined.ChevronLeft
        "chevron_right" -> Icons.Outlined.ChevronRight
        "cloud_off" -> Icons.Outlined.CloudOff
        "code" -> Icons.Outlined.Code
        "code_off" -> Icons.Outlined.CodeOff
        "content_copy" -> Icons.Outlined.ContentCopy
        "data_object" -> Icons.Outlined.DataObject
        "dashboard" -> Icons.Outlined.Dashboard
        "database" -> Icons.Outlined.Storage
        "delete" -> Icons.Outlined.Delete
        "description" -> Icons.Outlined.Description
        "dns" -> Icons.Outlined.Dns
        "domain_verification" -> Icons.Outlined.DomainVerification
        "download" -> Icons.Outlined.Download
        "expand_less" -> Icons.Outlined.ExpandLess
        "expand_more" -> Icons.Outlined.ExpandMore
        "fact_check" -> Icons.Outlined.FactCheck
        "filter_list" -> Icons.Outlined.FilterList
        "flag" -> Icons.Outlined.Flag
        "flash_on" -> Icons.Outlined.FlashOn
        "gpp_bad" -> Icons.Outlined.GppBad
        "help_outline" -> Icons.Outlined.HelpOutline
        "history" -> Icons.Outlined.History
        "history_edu" -> Icons.Outlined.HistoryEdu
        "link" -> Icons.Outlined.Link
        "location_on" -> Icons.Outlined.LocationOn
        "lock" -> Icons.Outlined.Lock
        "logout" -> Icons.Outlined.Logout
        "manage_search" -> Icons.Outlined.ManageSearch
        "more_horiz" -> Icons.Outlined.MoreHoriz
        "notifications" -> Icons.Outlined.Notifications
        "open_in_new" -> Icons.Outlined.OpenInNew
        "person" -> Icons.Outlined.Person
        "picture_as_pdf" -> Icons.Outlined.PictureAsPdf
        "preview" -> Icons.Outlined.Preview
        "priority_high" -> Icons.Outlined.PriorityHigh
        "psychology" -> Icons.Outlined.Psychology
        "public" -> Icons.Outlined.Public
        "public_off" -> Icons.Outlined.PublicOff
        "qr_code_scanner" -> Icons.Outlined.QrCodeScanner
        "refresh" -> Icons.Outlined.Refresh
        "rule" -> Icons.Outlined.Rule
        "schedule" -> Icons.Outlined.Schedule
        "school" -> Icons.Outlined.School
        "science" -> Icons.Outlined.Science
        "search" -> Icons.Outlined.Search
        "security" -> Icons.Outlined.Security
        "security_update_good" -> Icons.Outlined.SecurityUpdateGood
        "settings" -> Icons.Outlined.Settings
        "shield" -> Icons.Outlined.Shield
        "shield_lock" -> Icons.Outlined.Shield
        "shuffle" -> Icons.Outlined.Shuffle
        "speed" -> Icons.Outlined.Speed
        "spellcheck" -> Icons.Outlined.Spellcheck
        "storage" -> Icons.Outlined.Storage
        "text_format" -> Icons.Outlined.TextFormat
        "thumb_up" -> Icons.Outlined.ThumbUp
        "timer" -> Icons.Outlined.Timer
        "tune" -> Icons.Outlined.Tune
        "upload_file" -> Icons.Outlined.UploadFile
        "verified" -> Icons.Outlined.Verified
        "verified_user" -> Icons.Outlined.VerifiedUser
        "videocam" -> Icons.Outlined.Videocam
        "videocam_off" -> Icons.Outlined.VideocamOff
        "visibility" -> Icons.Outlined.Visibility
        "visibility_off" -> Icons.Outlined.VisibilityOff
        "warning" -> Icons.Outlined.Warning
        "warning_amber" -> Icons.Outlined.WarningAmber
        "wifi_off" -> Icons.Outlined.WifiOff
        "zoom_in" -> Icons.Outlined.ZoomIn
        "zoom_out" -> Icons.Outlined.ZoomOut
        else -> null
    }
}

@Composable
fun IconText(
    name: String,
    font: IconFont,
    size: TextUnit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val icon = iconForName(name)
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = color,
            modifier = modifier.size(size.value.dp)
        )
    }
}

@Composable
fun MaterialIcon(
    name: String,
    size: TextUnit,
    color: Color,
    modifier: Modifier = Modifier
) {
    IconText(name = name, font = IconFont.MaterialIcons, size = size, color = color, modifier = modifier)
}

@Composable
fun MaterialIconRound(
    name: String,
    size: TextUnit,
    color: Color,
    modifier: Modifier = Modifier
) {
    IconText(name = name, font = IconFont.MaterialIconsRound, size = size, color = color, modifier = modifier)
}

@Composable
fun MaterialSymbol(
    name: String,
    size: TextUnit,
    color: Color,
    modifier: Modifier = Modifier
) {
    IconText(name = name, font = IconFont.MaterialSymbolsOutlined, size = size, color = color, modifier = modifier)
}
