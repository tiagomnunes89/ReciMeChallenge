package com.recime.challenge.ui.favorites

import com.recime.challenge.R
import com.recime.challenge.testing.FakeFavoritesRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    private val favorites = FakeFavoritesRepository(previewRecipes.map { it.id }.toSet())

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.createViewModel(): FavoritesViewModel {
        val viewModel = FavoritesViewModel(favorites)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel
    }

    @Test
    fun `shows the favorite recipes`() = runTest {
        val viewModel = createViewModel()

        assertEquals(FavoritesUiState.Success(previewRecipes), viewModel.uiState.value)
    }

    @Test
    fun `no favorites expose an empty success state`() = runTest {
        favorites.favoriteIds.value = emptySet()
        val viewModel = createViewModel()

        assertEquals(FavoritesUiState.Success(emptyList()), viewModel.uiState.value)
    }

    @Test
    fun `repository failure exposes an error`() = runTest {
        favorites.observeFailure = IllegalStateException("favorites failed")
        val viewModel = createViewModel()

        assertEquals(FavoritesUiState.Error(R.string.common_error_generic), viewModel.uiState.value)
    }

    @Test
    fun `retry resubscribes after a failure`() = runTest {
        favorites.observeFailure = IllegalStateException("database")
        val viewModel = createViewModel()
        assertEquals(FavoritesUiState.Error(R.string.common_error_generic), viewModel.uiState.value)

        favorites.observeFailure = null
        viewModel.onRetry()

        assertEquals(FavoritesUiState.Success(previewRecipes), viewModel.uiState.value)
        assertEquals(2, favorites.subscriptionCount)
    }
}
