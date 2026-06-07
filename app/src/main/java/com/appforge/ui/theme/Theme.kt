package com.appforge.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Blue800,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Blue400,
    secondary = Purple600,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = Purple400,
    tertiary = Gold600,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    background = LightSurface,
    surface = LightSurface,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFF3F0EF)
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue400,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = Blue800,
    secondary = Purple400,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = Purple600,
    tertiary = Gold400,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    background = DarkSurface,
    surface = DarkSurface,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2C2C2E)
)

@Composable
fun AppForgeTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppForgeTypography,
        shapes = AppForgeShapes,
        content = content
    )
}
