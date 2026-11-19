package com.recime.challenge.ui.favorites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.recime.challenge.R
import com.recime.challenge.designsystem.theme.ReciMeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesErrorUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun errorStateIsVisibleInLightTheme() {
        composeRule.setContent {
            ReciMeTheme {
                errorScreen()
            }
        }

        assertErrorContent()
    }

    @Test
    fun errorStateIsVisibleInDarkTheme() {
        composeRule.setContent {
            ReciMeTheme(darkTheme = true) {
                errorScreen()
            }
        }

        assertErrorContent()
    }

    @androidx.compose.runtime.Composable
    private fun errorScreen() {
        FavoritesScreen(
            uiState = FavoritesUiState.Error(R.string.common_error_generic),
            onBackClick = {},
            onRecipeClick = {},
            onRetry = {},
        )
    }

    private fun assertErrorContent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithText(context.getString(R.string.common_error_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.common_error_generic)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.common_try_again)).assertIsDisplayed()
    }
}
