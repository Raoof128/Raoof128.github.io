/*
 * Copyright 2025-2026 Mehr Guard Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package com.raouf.mehrguard.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raouf.mehrguard.android.ui.theme.MehrGuardColors
import androidx.compose.ui.res.stringResource
import com.raouf.mehrguard.android.R

/**
 * Mehr Guard Top App Bar
 * Matches the HTML: sticky, backdrop blur effect, centered title
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MehrGuardTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = Icons.Default.Settings,
    actionText: String? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        actions = {
            if (onActionClick != null && actionText != null) {
                TextButton(onClick = onActionClick) {
                    Text(
                        text = actionText,
                        color = MehrGuardColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (onActionClick != null && actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = stringResource(R.string.cd_action),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
        ),
        modifier = modifier
    )
}

/**
 * Primary Action Button - Rounded, with shadow
 * Matches: bg-primary hover:bg-blue-700 rounded-full shadow-lg
 */
@Composable
fun MehrGuardPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(9999.dp),
                ambientColor = MehrGuardColors.Primary.copy(alpha = 0.3f),
                spotColor = MehrGuardColors.Primary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(9999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MehrGuardColors.Primary,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

/**
 * Secondary/Outline Button
 * Matches: border-2 border-primary text-primary rounded-full
 */
@Composable
fun MehrGuardSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(9999.dp),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(listOf(MehrGuardColors.Primary, MehrGuardColors.Primary))
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MehrGuardColors.Primary
        ),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

/**
 * Danger Button (Red)
 * Matches: bg-risk-high hover:bg-red-600 rounded-full
 */
@Composable
fun MehrGuardDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(9999.dp),
                ambientColor = MehrGuardColors.RiskDanger.copy(alpha = 0.3f),
                spotColor = MehrGuardColors.RiskDanger.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(9999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MehrGuardColors.RiskDanger,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

/**
 * Surface Card - Elevated card with border
 * Matches: bg-surface-light rounded-xl shadow-sm border border-gray-100
 */
@Composable
fun MehrGuardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Status Chip/Badge
 * Matches the verdict badges: bg-emerald-100 text-emerald-700 rounded-full
 */
@Composable
fun StatusChip(
    text: String,
    status: ChipStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ChipStatus.SAFE -> MehrGuardColors.Emerald50 to MehrGuardColors.Emerald600
        ChipStatus.WARNING -> MehrGuardColors.Orange50 to MehrGuardColors.Orange600
        ChipStatus.DANGER -> MehrGuardColors.Red50 to MehrGuardColors.Red600
        ChipStatus.INFO -> MehrGuardColors.Blue50 to MehrGuardColors.Blue600
        ChipStatus.NEUTRAL -> MehrGuardColors.Gray100 to MehrGuardColors.Gray600
        ChipStatus.IN_PROGRESS -> MehrGuardColors.Orange50 to MehrGuardColors.Orange600
        ChipStatus.NEW -> MehrGuardColors.Gray100 to MehrGuardColors.Gray600
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9999.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

enum class ChipStatus {
    SAFE, WARNING, DANGER, INFO, NEUTRAL, IN_PROGRESS, NEW
}

/**
 * Toggle Switch using Material 3 Switch component
 * Properly themed with Mehr Guard colors
 */
@Composable
fun MehrGuardToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = MehrGuardColors.Primary,
            checkedBorderColor = MehrGuardColors.Primary,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            disabledCheckedThumbColor = Color.White.copy(alpha = 0.6f),
            disabledCheckedTrackColor = MehrGuardColors.Primary.copy(alpha = 0.4f),
            disabledUncheckedThumbColor = Color.White.copy(alpha = 0.6f),
            disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    )
}

/**
 * Icon with circular background
 * Matches: flex size-10 items-center justify-center rounded-full bg-primary/10 text-primary
 */
@Composable
fun IconCircle(
    icon: ImageVector,
    backgroundColor: Color = MehrGuardColors.Primary.copy(alpha = 0.1f),
    iconColor: Color = MehrGuardColors.Primary,
    size: Dp = 40.dp,
    iconSize: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Feature List Item with icon
 * Matches the Privacy Architecture items in HTML
 */
@Composable
fun FeatureListItem(
    icon: ImageVector,
    title: String,
    description: String,
    iconBackgroundColor: Color = MehrGuardColors.Primary.copy(alpha = 0.1f),
    iconColor: Color = MehrGuardColors.Primary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        IconCircle(
            icon = icon,
            backgroundColor = iconBackgroundColor,
            iconColor = iconColor
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Segmented Button Control (for format selection, sensitivity, etc.)
 * Matches: flex h-12 items-center rounded-full bg-gray-200 p-1
 */
@Composable
fun <T> SegmentedButtonRow(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    iconProvider: ((T) -> ImageVector)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(9999.dp),
        color = MehrGuardColors.Gray200
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MehrGuardColors.Primary else Color.Transparent,
                    animationSpec = tween(200),
                    label = "segmentBgColor"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else MehrGuardColors.Gray500,
                    animationSpec = tween(200),
                    label = "segmentContentColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(9999.dp))
                        .background(backgroundColor)
                        .clickable { onOptionSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (iconProvider != null) {
                            Icon(
                                imageVector = iconProvider(option),
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = labelProvider(option),
                            color = contentColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Info Banner/Note
 * Matches: flex items-start gap-3 rounded-2xl bg-primary/10 border border-primary/20 p-3
 */
@Composable
fun InfoBanner(
    icon: ImageVector = Icons.Default.Info,
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MehrGuardColors.Primary.copy(alpha = 0.1f),
    borderColor: Color = MehrGuardColors.Primary.copy(alpha = 0.2f),
    iconColor: Color = MehrGuardColors.Primary,
    textColor: Color = MehrGuardColors.Primary.copy(alpha = 0.8f)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = textColor
        )
    }
}

/**
 * Circular Progress with percentage
 * Matches the SVG circular progress in Learning Centre
 */
@Composable
fun CircularProgressIndicatorWithPercentage(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 6.dp,
    backgroundColor: Color = MehrGuardColors.Gray200,
    progressColor: Color = MehrGuardColors.Primary
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor,
            strokeWidth = strokeWidth
        )
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = progressColor,
            strokeWidth = strokeWidth
        )
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

/**
 * URL Display Box (monospace, with copy button)
 */
@Composable
fun UrlDisplayBox(
    url: String,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = url,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onCopyClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.cd_copy_url),
                tint = MehrGuardColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Section Header with optional icon and action
 * Matches iOS pattern: Icon + "SECTION TITLE" in uppercase with brandPrimary color
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    uppercase: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MehrGuardColors.Primary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = if (uppercase) title.uppercase() else title,
                style = if (uppercase) {
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                } else {
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                },
                color = if (uppercase) MehrGuardColors.Primary else MaterialTheme.colorScheme.onBackground
            )
        }
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    color = MehrGuardColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Bottom Navigation Bar Item
 * Matches the three-tab navigation in HTML
 */
@Composable
fun MehrGuardBottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MehrGuardColors.Primary else MehrGuardColors.Gray400,
        animationSpec = tween(200),
        label = "navItemColor"
    )

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = contentColor
        )
    }
}
