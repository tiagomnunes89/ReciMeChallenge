package com.recime.challenge.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.recime.challenge.R
import com.recime.challenge.testing.FakeFavoritesRepository
import com.recime.challenge.testing.FakeRecipeRepository
import com.recime.challenge.ui.home.previewRecipes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class RecipeDetailViewModelTest {

    private val recipe = previewRecipes.first()
    private val recipes = FakeRecipeRepository()
    private val favorites = FakeFavoritesRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun createViewModel(): RecipeDetailViewModel =
        RecipeDetailViewModel(
            recipes = recipes,
            favorites = favorites,
            savedStateHandle = SavedStateHandle(
                mapOf("recipeId" to recipe.id),
            ),
        )

    private fun TestScope.collectState(viewModel: RecipeDetailViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    @Test
    fun `starts loading and then shows recipe favorite state`() = runTest {
        favorites.favoriteIds.value = setOf(recipe.id)
        val viewModel = createViewModel()
        collectState(viewModel)

        assertEquals(RecipeDetailUiState.Success(recipe, isFavorite = true), viewModel.uiState.value)
    }

    @Test
    fun `shows not favorite when recipe is not favorited`() = runTest {
        val viewModel = createViewModel()
        collectState(viewModel)

        assertEquals(RecipeDetailUiState.Success(recipe, isFavorite = false), viewModel.uiState.value)
    }

    @Test
    fun `recipe failure exposes an error`() = runTest {
        recipes.failure = IllegalStateException("missing")
        val viewModel = createViewModel()
        collectState(viewModel)

        assertEquals(
            RecipeDetailUiState.Error(R.string.common_error_generic),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `favorite observation failure exposes an error`() = runTest {
        favorites.observeFailure = IllegalStateException("favorites failed")
        val viewModel = createViewModel()
        collectState(viewModel)

        assertEquals(
            RecipeDetailUiState.Error(R.string.common_error_generic),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `retry resubscribes after a recipe failure`() = runTest {
        recipes.failure = IllegalStateException("temporary")
        val viewModel = createViewModel()
        collectState(viewModel)
        assertEquals(RecipeDetailUiState.Error(R.string.common_error_generic), viewModel.uiState.value)

        recipes.failure = null
        viewModel.onRetry()

        assertEquals(RecipeDetailUiState.Success(recipe), viewModel.uiState.value)
    }

    @Test
    fun `favorite action failure exposes an error`() = runTest {
        favorites.toggleFailure = IllegalStateException("write failed")
        val viewModel = createViewModel()
        collectState(viewModel)

        viewModel.onFavoriteClick()

        assertEquals(
            RecipeDetailUiState.Error(R.string.common_error_generic),
            viewModel.uiState.value,
        )
    }

    @Test
    fun `favorite action succeeds with the recipe id`() = runTest {
        val viewModel = createViewModel()
        collectState(viewModel)

        viewModel.onFavoriteClick()

        assertEquals(recipe.id, favorites.lastToggledId)
        assertEquals(RecipeDetailUiState.Success(recipe), viewModel.uiState.value)
    }
}
