package com.recime.challenge

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        clearAppData()
        composeRule.activityRule.scenario.recreate()
        assertTextVisible(FIRST_RECIPE)
    }

    @Test
    fun recipeCardOpensDetailAndBackReturnsToHome() {
        clickText("Spaghetti Aglio e Olio")
        assertTextVisible("About")
        assertTextVisible("Ingredients")
        assertTextVisible("Instructions")

        composeRule.onNodeWithContentDescription("Back").performClick()
        assertTextVisible(FIRST_RECIPE)
    }

    @Test
    fun favoriteRecipeAppearsInFavoritesScreen() {
        clickText("Spaghetti Aglio e Olio")
        clickContentDescription("Add to favorites")
        assertContentDescriptionVisible("Remove from favorites")

        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.onNodeWithContentDescription("Menu").performClick()
        clickText("Favorites")

        assertTextVisible("Favorites")
        assertTextVisible("Spaghetti Aglio e Olio")
    }

    @Test
    fun vegetarianTabFiltersRecipes() {
        assertTextVisible(NON_VEGETARIAN_RECIPE)
        clickText("Vegetarian")
        composeRule.waitUntil(UI_TIMEOUT_MS) {
            composeRule.onAllNodesWithText(NON_VEGETARIAN_RECIPE).fetchSemanticsNodes().isEmpty()
        }
        assertTextVisible(FIRST_RECIPE)
        assertTextVisible("Veggie Buddha Bowl")
        assertTextVisible("Margherita Pizza")
        assertTextVisible("Creamy Mushroom Risotto")
    }

    @Test
    fun advancedFiltersCanBeOpenedAndClosed() {
        clickText("see more")
        assertTextVisible("Servings")
        assertTextVisible("Include ingredients")
        assertTextVisible("Exclude ingredients")
        assertTextVisible("Instructions contain")

        clickText("see less")
        assertTrue(
            composeRule.onAllNodesWithText("Include ingredients").fetchSemanticsNodes().isEmpty(),
        )
    }

    private fun clearAppData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeRule.runOnUiThread {
            context.filesDir.resolve("datastore/favorites.preferences_pb").delete()
        }
    }

    private fun clickText(text: String) {
        assertTextVisible(text)
        composeRule.onNodeWithText(text).performClick()
    }

    private fun clickContentDescription(description: String) {
        assertContentDescriptionVisible(description)
        composeRule.onNodeWithContentDescription(description).performClick()
    }

    private fun assertTextVisible(text: String) {
        composeRule.waitUntil(UI_TIMEOUT_MS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(text).assertIsDisplayed()
    }

    private fun assertContentDescriptionVisible(description: String) {
        composeRule.waitUntil(UI_TIMEOUT_MS) {
            composeRule.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    private companion object {
        const val UI_TIMEOUT_MS = 5_000L
        const val FIRST_RECIPE = "Spaghetti Aglio e Olio"
        const val NON_VEGETARIAN_RECIPE = "Chicken Caesar Salad"
    }
}
