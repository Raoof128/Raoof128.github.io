package com.qrshield.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// QR-SHIELD Brand Colors
private val Purple80 = Color(0xFF6C5CE7)
private val PurpleGrey80 = Color(0xFFCCC2DC)
private val Teal80 = Color(0xFF00CEC9)

private val Purple40 = Color(0xFF5B4DCF)
private val PurpleGrey40 = Color(0xFF625b71)
private val Teal40 = Color(0xFF00B5B0)

// Semantic Colors
val SafeGreen = Color(0xFF00D68F)
val WarningAmber = Color(0xFFFFAA00)
val DangerRed = Color(0xFFFF3D71)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = Teal80,
    tertiary = WarningAmber,
    background = Color(0xFF0D1117),
    surface = Color(0xFF161B22),
    error = DangerRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFF0F6FC),
    onSurface = Color(0xFFF0F6FC),
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = Teal40,
    tertiary = WarningAmber,
    background = Color(0xFFF6F8FA),
    surface = Color.White,
    error = DangerRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF24292F),
    onSurface = Color(0xFF24292F),
    onError = Color.White
)

@Composable
fun QRShieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

val Typography = Typography()
