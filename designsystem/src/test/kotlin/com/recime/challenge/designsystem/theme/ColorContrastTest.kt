package com.recime.challenge.designsystem.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorContrastTest {
    @Test
    fun `light error colors meet WCAG AA contrast`() {
        assertTrue(ErrorLight.contrastRatio(OnErrorLight) >= MinimumTextContrast)
        assertTrue(ErrorLight.contrastRatio(ScreenBackground) >= MinimumTextContrast)
    }

    @Test
    fun `dark error colors meet WCAG AA contrast`() {
        assertTrue(ErrorDark.contrastRatio(OnErrorDark) >= MinimumTextContrast)
    }

    private fun Color.contrastRatio(other: Color): Double {
        val firstLuminance = relativeLuminance()
        val secondLuminance = other.relativeLuminance()
        val lighter = maxOf(firstLuminance, secondLuminance)
        val darker = minOf(firstLuminance, secondLuminance)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun Color.relativeLuminance(): Double {
        fun channel(value: Float): Double {
            val normalized = value.toDouble()
            return if (normalized <= 0.03928) {
                normalized / 12.92
            } else {
                ((normalized + 0.055) / 1.055).pow(2.4)
            }
        }

        return (0.2126 * channel(red)) +
            (0.7152 * channel(green)) +
            (0.0722 * channel(blue))
    }

    private companion object {
        const val MinimumTextContrast = 4.5
    }
}
