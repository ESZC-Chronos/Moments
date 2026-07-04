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
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Cozy Warm Theme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD97757), // Warm terracotta
    onPrimary = Color(0xFFFAF7F2),
    secondary = Color(0xFF8B9B7E), // Soft olive green
    onSecondary = Color(0xFFFAF7F2),
    background = Color(0xFF2C2825), // Soft dark brown
    onBackground = Color(0xFFE8E2D9),
    surface = Color(0xFF38332F),
    onSurface = Color(0xFFE8E2D9),
    surfaceVariant = Color(0xFF4A433D),
    onSurfaceVariant = Color(0xFFD97757),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFC06B4C), // Terracotta
    onPrimary = Color.White,
    secondary = Color(0xFF768668), // Soft olive green
    onSecondary = Color.White,
    background = Color(0xFFFAF7F2), // Warm beige
    onBackground = Color(0xFF3E3A39), // Charcoal brown
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF3E3A39),
    surfaceVariant = Color(0xFFEBE5DB), // Light taupe
    onSurfaceVariant = Color(0xFF8B7355),
    error = Color(0xFFB00020),
    onError = Color.White
)

val CozyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp)
)

@Composable
fun MomentsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CozyShapes,
        content = content
    )
}
