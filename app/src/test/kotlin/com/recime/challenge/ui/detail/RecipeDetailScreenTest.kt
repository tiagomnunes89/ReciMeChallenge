package com.recime.challenge.ui.detail

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
class RecipeDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val recipe = previewRecipes.first()

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    private fun setScreen(
        uiState: RecipeDetailUiState,
        onBackClick: () -> Unit = {},
        onRetry: () -> Unit = {},
        onFavoriteClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            ReciMeTheme {
                RecipeDetailScreen(
                    uiState = uiState,
                    onBackClick = onBackClick,
                    onRetry = onRetry,
                    onFavoriteClick = onFavoriteClick,
                )
            }
        }
    }

    @Test
    fun `success shows the recipe content`() {
        setScreen(RecipeDetailUiState.Success(recipe))

        composeRule.onNodeWithText(recipe.title).assertIsDisplayed()
        composeRule.onNodeWithText("Serves 2").assertIsDisplayed()
        composeRule.onNodeWithText(recipe.description).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.detail_ingredients)).assertIsDisplayed()
        composeRule.onNodeWithText("garlic").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.detail_instructions)).assertIsDisplayed()
        composeRule.onNodeWithText("1. ${recipe.instructions.first()}").assertIsDisplayed()
    }

    @Test
    fun `favorite button offers to add when the recipe is not a favorite`() {
        var clicks = 0
        setScreen(RecipeDetailUiState.Success(recipe, isFavorite = false), onFavoriteClick = { clicks++ })

        composeRule.onNodeWithContentDescription("Add to favorites").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `favorite button offers to remove when the recipe is a favorite`() {
        var clicks = 0
        setScreen(RecipeDetailUiState.Success(recipe, isFavorite = true), onFavoriteClick = { clicks++ })

        composeRule.onNodeWithContentDescription("Remove from favorites").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `back button triggers the callback`() {
        var backs = 0
        setScreen(RecipeDetailUiState.Success(recipe), onBackClick = { backs++ })

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backs)
    }

    @Test
    fun `error shows its message and retry triggers the callback`() {
        var retries = 0
        setScreen(
            RecipeDetailUiState.Error(R.string.common_error_recipe_unavailable),
            onRetry = { retries++ },
        )

        composeRule.onNodeWithText(string(R.string.common_error_recipe_unavailable)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.common_try_again)).performClick()

        assertEquals(1, retries)
    }
}
