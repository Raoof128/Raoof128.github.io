package com.qrshield.desktop.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.CallSplit
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.Rule
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.CallSplit
import androidx.compose.material.icons.automirrored.rounded.FactCheck
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ManageSearch
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Rule
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
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

private fun iconForName(name: String, font: IconFont): ImageVector? {
    val useRounded = font == IconFont.MaterialIconsRound
    return when (name) {
        "abc" -> if (useRounded) Icons.Rounded.Abc else Icons.Outlined.Abc
        "account_circle" -> if (useRounded) Icons.Rounded.AccountCircle else Icons.Outlined.AccountCircle
        "add" -> if (useRounded) Icons.Rounded.Add else Icons.Outlined.Add
        "add_photo_alternate" -> if (useRounded) Icons.Rounded.AddPhotoAlternate else Icons.Outlined.AddPhotoAlternate
        "analytics" -> if (useRounded) Icons.Rounded.Analytics else Icons.Outlined.Analytics
        "alt_route" -> if (useRounded) Icons.AutoMirrored.Rounded.AltRoute else Icons.AutoMirrored.Outlined.AltRoute
        "arrow_forward" -> if (useRounded) Icons.AutoMirrored.Rounded.ArrowForward else Icons.AutoMirrored.Outlined.ArrowForward
        "block" -> if (useRounded) Icons.Rounded.Block else Icons.Outlined.Block
        "bolt" -> if (useRounded) Icons.Rounded.Bolt else Icons.Outlined.Bolt
        "calendar_today" -> if (useRounded) Icons.Rounded.CalendarToday else Icons.Outlined.CalendarToday
        "call_split" -> if (useRounded) Icons.AutoMirrored.Rounded.CallSplit else Icons.AutoMirrored.Outlined.CallSplit
        "center_focus_weak" -> if (useRounded) Icons.Rounded.CenterFocusWeak else Icons.Outlined.CenterFocusWeak
        "check" -> if (useRounded) Icons.Rounded.Check else Icons.Outlined.Check
        "check_circle" -> if (useRounded) Icons.Rounded.CheckCircle else Icons.Outlined.CheckCircle
        "chevron_left" -> if (useRounded) Icons.Rounded.ChevronLeft else Icons.Outlined.ChevronLeft
        "chevron_right" -> if (useRounded) Icons.Rounded.ChevronRight else Icons.Outlined.ChevronRight
        "cloud_off" -> if (useRounded) Icons.Rounded.CloudOff else Icons.Outlined.CloudOff
        "code" -> if (useRounded) Icons.Rounded.Code else Icons.Outlined.Code
        "code_off" -> if (useRounded) Icons.Rounded.CodeOff else Icons.Outlined.CodeOff
        "coffee" -> if (useRounded) Icons.Rounded.Coffee else Icons.Outlined.Coffee
        "bug_report" -> if (useRounded) Icons.Rounded.BugReport else Icons.Outlined.BugReport
        "content_paste" -> if (useRounded) Icons.Rounded.ContentPaste else Icons.Outlined.ContentPaste
        "content_copy" -> if (useRounded) Icons.Rounded.ContentCopy else Icons.Outlined.ContentCopy
        "data_object" -> if (useRounded) Icons.Rounded.DataObject else Icons.Outlined.DataObject
        "dashboard" -> if (useRounded) Icons.Rounded.Dashboard else Icons.Outlined.Dashboard
        "database" -> if (useRounded) Icons.Rounded.Storage else Icons.Outlined.Storage
        "delete" -> if (useRounded) Icons.Rounded.Delete else Icons.Outlined.Delete
        "description" -> if (useRounded) Icons.Rounded.Description else Icons.Outlined.Description
        "dns" -> if (useRounded) Icons.Rounded.Dns else Icons.Outlined.Dns
        "domain_verification" -> if (useRounded) Icons.Rounded.DomainVerification else Icons.Outlined.DomainVerification
        "download" -> if (useRounded) Icons.Rounded.Download else Icons.Outlined.Download
        "dark_mode" -> if (useRounded) Icons.Rounded.DarkMode else Icons.Outlined.DarkMode
        "light_mode" -> if (useRounded) Icons.Rounded.LightMode else Icons.Outlined.LightMode
        "error" -> if (useRounded) Icons.Rounded.Error else Icons.Outlined.Error
        "edit" -> if (useRounded) Icons.Rounded.Edit else Icons.Outlined.Edit
        "expand_less" -> if (useRounded) Icons.Rounded.ExpandLess else Icons.Outlined.ExpandLess
        "expand_more" -> if (useRounded) Icons.Rounded.ExpandMore else Icons.Outlined.ExpandMore
        "fact_check" -> if (useRounded) Icons.AutoMirrored.Rounded.FactCheck else Icons.AutoMirrored.Outlined.FactCheck
        "filter_list" -> if (useRounded) Icons.Rounded.FilterList else Icons.Outlined.FilterList
        "flag" -> if (useRounded) Icons.Rounded.Flag else Icons.Outlined.Flag
        "flash_on" -> if (useRounded) Icons.Rounded.FlashOn else Icons.Outlined.FlashOn
        "gpp_bad" -> if (useRounded) Icons.Rounded.GppBad else Icons.Outlined.GppBad
        "help_outline" -> if (useRounded) Icons.AutoMirrored.Rounded.HelpOutline else Icons.AutoMirrored.Outlined.HelpOutline
        "history" -> if (useRounded) Icons.Rounded.History else Icons.Outlined.History
        "history_edu" -> if (useRounded) Icons.Rounded.HistoryEdu else Icons.Outlined.HistoryEdu
        "javascript" -> if (useRounded) Icons.Rounded.Javascript else Icons.Outlined.Javascript
        "link" -> if (useRounded) Icons.Rounded.Link else Icons.Outlined.Link
        "location_on" -> if (useRounded) Icons.Rounded.LocationOn else Icons.Outlined.LocationOn
        "lock" -> if (useRounded) Icons.Rounded.Lock else Icons.Outlined.Lock
        "logout" -> if (useRounded) Icons.AutoMirrored.Rounded.Logout else Icons.AutoMirrored.Outlined.Logout
        "manage_search" -> if (useRounded) Icons.AutoMirrored.Rounded.ManageSearch else Icons.AutoMirrored.Outlined.ManageSearch
        "more_horiz" -> if (useRounded) Icons.Rounded.MoreHoriz else Icons.Outlined.MoreHoriz
        "notifications" -> if (useRounded) Icons.Rounded.Notifications else Icons.Outlined.Notifications
        "open_in_new" -> if (useRounded) Icons.AutoMirrored.Rounded.OpenInNew else Icons.AutoMirrored.Outlined.OpenInNew
        "person" -> if (useRounded) Icons.Rounded.Person else Icons.Outlined.Person
        "picture_as_pdf" -> if (useRounded) Icons.Rounded.PictureAsPdf else Icons.Outlined.PictureAsPdf
        "preview" -> if (useRounded) Icons.Rounded.Preview else Icons.Outlined.Preview
        "priority_high" -> if (useRounded) Icons.Rounded.PriorityHigh else Icons.Outlined.PriorityHigh
        "psychology" -> if (useRounded) Icons.Rounded.Psychology else Icons.Outlined.Psychology
        "public" -> if (useRounded) Icons.Rounded.Public else Icons.Outlined.Public
        "public_off" -> if (useRounded) Icons.Rounded.PublicOff else Icons.Outlined.PublicOff
        "qr_code_scanner" -> if (useRounded) Icons.Rounded.QrCodeScanner else Icons.Outlined.QrCodeScanner
        "refresh" -> if (useRounded) Icons.Rounded.Refresh else Icons.Outlined.Refresh
        "rule" -> if (useRounded) Icons.AutoMirrored.Rounded.Rule else Icons.AutoMirrored.Outlined.Rule
        "schedule" -> if (useRounded) Icons.Rounded.Schedule else Icons.Outlined.Schedule
        "school" -> if (useRounded) Icons.Rounded.School else Icons.Outlined.School
        "science" -> if (useRounded) Icons.Rounded.Science else Icons.Outlined.Science
        "search" -> if (useRounded) Icons.Rounded.Search else Icons.Outlined.Search
        "security" -> if (useRounded) Icons.Rounded.Security else Icons.Outlined.Security
        "security_update_good" -> if (useRounded) Icons.Rounded.SecurityUpdateGood else Icons.Outlined.SecurityUpdateGood
        "settings" -> if (useRounded) Icons.Rounded.Settings else Icons.Outlined.Settings
        "share" -> if (useRounded) Icons.Rounded.Share else Icons.Outlined.Share
        "shield" -> if (useRounded) Icons.Rounded.Shield else Icons.Outlined.Shield
        "shield_lock" -> if (useRounded) Icons.Rounded.Shield else Icons.Outlined.Shield
        "shuffle" -> if (useRounded) Icons.Rounded.Shuffle else Icons.Outlined.Shuffle
        "speed" -> if (useRounded) Icons.Rounded.Speed else Icons.Outlined.Speed
        "spellcheck" -> if (useRounded) Icons.Rounded.Spellcheck else Icons.Outlined.Spellcheck
        "storage" -> if (useRounded) Icons.Rounded.Storage else Icons.Outlined.Storage
        "text_format" -> if (useRounded) Icons.Rounded.TextFormat else Icons.Outlined.TextFormat
        "thumb_up" -> if (useRounded) Icons.Rounded.ThumbUp else Icons.Outlined.ThumbUp
        "timer" -> if (useRounded) Icons.Rounded.Timer else Icons.Outlined.Timer
        "tune" -> if (useRounded) Icons.Rounded.Tune else Icons.Outlined.Tune
        "upload_file" -> if (useRounded) Icons.Rounded.UploadFile else Icons.Outlined.UploadFile
        "verified" -> if (useRounded) Icons.Rounded.Verified else Icons.Outlined.Verified
        "verified_user" -> if (useRounded) Icons.Rounded.VerifiedUser else Icons.Outlined.VerifiedUser
        "videocam" -> if (useRounded) Icons.Rounded.Videocam else Icons.Outlined.Videocam
        "videocam_off" -> if (useRounded) Icons.Rounded.VideocamOff else Icons.Outlined.VideocamOff
        "visibility" -> if (useRounded) Icons.Rounded.Visibility else Icons.Outlined.Visibility
        "visibility_off" -> if (useRounded) Icons.Rounded.VisibilityOff else Icons.Outlined.VisibilityOff
        "warning" -> if (useRounded) Icons.Rounded.Warning else Icons.Outlined.Warning
        "warning_amber" -> if (useRounded) Icons.Rounded.WarningAmber else Icons.Outlined.WarningAmber
        "wifi_off" -> if (useRounded) Icons.Rounded.WifiOff else Icons.Outlined.WifiOff
        "zoom_in" -> if (useRounded) Icons.Rounded.ZoomIn else Icons.Outlined.ZoomIn
        "zoom_out" -> if (useRounded) Icons.Rounded.ZoomOut else Icons.Outlined.ZoomOut
        "sports_esports" -> if (useRounded) Icons.Rounded.SportsEsports else Icons.Outlined.SportsEsports
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
    val icon = iconForName(name, font)
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
