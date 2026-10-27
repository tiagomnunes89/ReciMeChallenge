package com.recime.challenge.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = ButtonLabel,
    secondary = OrangePrimary,
    onSecondary = ButtonLabel,
    tertiary = OrangePrimary,
    background = ScreenBackground,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = SearchFill,
    onSurfaceVariant = TabInactive,
    outline = TabInactive,
    error = OrangePrimary,
    onError = ButtonLabel,
)

private val DarkColorScheme = darkColorScheme(
    primary = OrangeDark,
    onPrimary = Ink,
    secondary = OrangeDark,
    onSecondary = Ink,
    tertiary = OrangeDark,
    background = BackgroundDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = SearchFillDark,
    onSurfaceVariant = TabInactiveDark,
    outline = TabInactiveDark,
    error = OrangeDark,
    onError = Ink,
)

@Composable
fun ReciMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = ReciMeShapes,
        content = content,
    )
}

object ReciMeTokens {
    val orange: Color get() = OrangePrimary
    val searchFill: Color get() = SearchFill
    val cardShadow: Color get() = CardShadow
    val tabInactive: Color get() = TabInactive
    val inkMuted: Color get() = InkMuted
    val screenAlt: Color get() = ScreenBackgroundAlt
}
