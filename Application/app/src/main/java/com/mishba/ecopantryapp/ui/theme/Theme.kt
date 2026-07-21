package com.mishba.ecopantryapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mishba.ecopantryapp.data.AppDataStore
import com.mishba.ecopantryapp.model.LightOrDarkMode
import kotlinx.coroutines.flow.filterNotNull

private val DarkColorScheme = darkColorScheme(
    primary = EcoGreen80,
    secondary = EcoGreenGrey80,
    tertiary = EcoAmber80,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color(0xFF0B3313),
    onSecondary = Color(0xFF0B3313),
    onBackground = Color(0xFFE1E4DE),
    onSurface = Color(0xFFE1E4DE),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFA8F5AE),
    surfaceVariant = Color(0xFF2B302B),
    onSurfaceVariant = Color(0xFFC0C9BD)
)

private val LightColorScheme = lightColorScheme(
    primary = EcoGreen40,
    secondary = EcoGreenGrey40,
    tertiary = EcoAmber40,
    background = Color(0xFFF6FAF3),
    surface = Color(0xFFF6FAF3),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF1A1C18),
    onSurface = Color(0xFF1A1C18),
    primaryContainer = Color(0xFFB9F6C1),
    onPrimaryContainer = Color(0xFF00210A),
    surfaceVariant = Color(0xFFDDE5D8),
    onSurfaceVariant = Color(0xFF424940)
)

@Composable
fun EcoPantryTheme(
    isSystemInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lightOrDarkMode by AppDataStore(context)
        .lightOrDarkModeFlow()
        .filterNotNull()
        .collectAsStateWithLifecycle(LightOrDarkMode.System)

    val useDark = when (lightOrDarkMode) {
        LightOrDarkMode.System -> isSystemInDarkTheme
        LightOrDarkMode.Light  -> false
        LightOrDarkMode.Dark   -> true
    }

    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
