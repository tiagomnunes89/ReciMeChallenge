package com.recime.challenge.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
    val screenPadding: Dp get() = 24.dp
    val screenBottomPadding: Dp get() = 32.dp
    val gridSpacing: Dp get() = 16.dp
    val gridRowSpacing: Dp get() = 24.dp
    val gridMinCellWidth: Dp get() = 160.dp
    val sectionSpacing: Dp get() = 24.dp
    val contentSpacing: Dp get() = 12.dp
    val inlinePadding: Dp get() = 8.dp
    val tightPadding: Dp get() = 4.dp
    val drawerTopSpacing: Dp get() = 32.dp
    val drawerHorizontalPadding: Dp get() = 28.dp
    val detailHorizontalPadding: Dp get() = 50.dp
    val detailImageSize: Dp get() = 241.dp
    val cardElevation: Dp get() = 24.dp
    val ingredientMarkerSize: Dp get() = 14.dp
    val ingredientMarkerStroke: Dp get() = 1.5.dp
}
