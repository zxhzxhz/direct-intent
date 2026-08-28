package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MiuiLightColorScheme = lightColorScheme(
    primary = HyperOSBlue,
    onPrimary = Color.White,
    primaryContainer = HyperOSBlueContainer,
    onPrimaryContainer = HyperOSOnBlueContainer,
    secondary = HyperOSOrange,
    onSecondary = Color.White,
    secondaryContainer = HyperOSOrangeContainer,
    onSecondaryContainer = Color(0xFF7C2D12),
    tertiary = HyperOSPurple,
    onTertiary = Color.White,
    tertiaryContainer = HyperOSPurpleContainer,
    onTertiaryContainer = Color(0xFF4C1D95),
    background = MiuiBgLight,
    onBackground = MiuiTextPrimaryLight,
    surface = MiuiCardLight,
    onSurface = MiuiTextPrimaryLight,
    surfaceVariant = Color(0xFFF0F2F6),
    onSurfaceVariant = MiuiTextSecondaryLight,
    outline = MiuiCardBorderLight,
    error = HyperOSRed,
    errorContainer = HyperOSRedContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val MiuiDarkColorScheme = darkColorScheme(
    primary = HyperOSBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF00388A),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = HyperOSOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Color(0xFFFFDBC8),
    tertiary = HyperOSPurple,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFEDE9FE),
    background = MiuiBgDark,
    onBackground = MiuiTextPrimaryDark,
    surface = MiuiCardDark,
    onSurface = MiuiTextPrimaryDark,
    surfaceVariant = Color(0xFF23262F),
    onSurfaceVariant = MiuiTextSecondaryDark,
    outline = MiuiCardBorderDark,
    error = HyperOSRed,
    errorContainer = Color(0xFF7F1D1D),
    onError = Color.White,
    onErrorContainer = Color(0xFFFEECEE)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MiuiDarkColorScheme
        else -> MiuiLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
