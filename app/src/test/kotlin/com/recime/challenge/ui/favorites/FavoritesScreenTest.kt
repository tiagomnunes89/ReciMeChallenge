package com.recime.challenge.ui.favorites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.recime.challenge.designsystem.theme.ReciMeTheme
import com.recime.challenge.R
import com.recime.challenge.ui.home.previewRecipes
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config(sdk = [35], qualifiers = "w411dp-h1600dp")
@RunWith(RobolectricTestRunner::class)
class FavoritesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    private fun setScreen(
        uiState: FavoritesUiState,
        onBackClick: () -> Unit = {},
        onRecipeClick: (String) -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeRule.setContent {
            ReciMeTheme {
                FavoritesScreen(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onRecipeClick = onRecipeClick,
                    onRetry = onRetry,
                )
            }
        }
    }

    @Test
    fun `shows the saved recipes under the screen title`() {
        setScreen(FavoritesUiState.Success(previewRecipes))

        composeRule.onNodeWithText(string(R.string.favorites_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Spaghetti Aglio e Olio").assertIsDisplayed()
        composeRule.onNodeWithText("Chicken Caesar Salad").assertIsDisplayed()
    }

    @Test
    fun `shows the empty state when there are no favorites`() {
        setScreen(FavoritesUiState.Success(emptyList()))

        composeRule.onNodeWithText(string(R.string.favorites_empty_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.favorites_empty_message)).assertIsDisplayed()
    }

    @Test
    fun `error shows its message and retry triggers the callback`() {
        var retries = 0
        setScreen(FavoritesUiState.Error(R.string.common_error_generic), onRetry = { retries++ })

        composeRule.onNodeWithText(string(R.string.common_error_generic)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.common_try_again)).performClick()

        assertEquals(1, retries)
    }

    @Test
    fun `clicking a favorite reports its id`() {
        var clicked: String? = null
        setScreen(FavoritesUiState.Success(previewRecipes), onRecipeClick = { clicked = it })

        composeRule.onNodeWithText("Spaghetti Aglio e Olio").performClick()

        assertEquals("1", clicked)
    }

    @Test
    fun `back button triggers the callback`() {
        var backs = 0
        setScreen(FavoritesUiState.Success(previewRecipes), onBackClick = { backs++ })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backs)
    }
}
