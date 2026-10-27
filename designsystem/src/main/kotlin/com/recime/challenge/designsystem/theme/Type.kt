package com.recime.challenge.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val KitSans = FontFamily.SansSerif

internal val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = KitSans,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 41.sp,
        color = Ink,
    ),
    headlineMedium = TextStyle(
        fontFamily = KitSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = Ink,
    ),
    titleLarge = TextStyle(
        fontFamily = KitSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 22.sp,
        color = Ink.copy(alpha = 0.9f),
    ),
    titleMedium = TextStyle(
        fontFamily = KitSans,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 20.sp,
        color = OrangePrimary,
    ),
    bodyLarge = TextStyle(
        fontFamily = KitSans,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        color = Ink,
    ),
    bodyMedium = TextStyle(
        fontFamily = KitSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.3.sp,
        color = Ink.copy(alpha = 0.5f),
    ),
    labelLarge = TextStyle(
        fontFamily = KitSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 20.sp,
        color = ButtonLabel,
    ),
    labelMedium = TextStyle(
        fontFamily = KitSans,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        color = OrangePrimary,
    ),
)
