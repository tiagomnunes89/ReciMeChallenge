package com.recime.challenge.ui.home

import com.recime.challenge.testing.FakeRecipeRepository
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
class HomeViewModelTest {

    private val repository = FakeRecipeRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.createViewModel(): HomeViewModel {
        val viewModel = HomeViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.recipes.collect {} }
        return viewModel
    }

    @Test
    fun `filters are exposed immediately so text fields never lag behind the debounce`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(HomeEvent.FiltersChanged(SearchFilters(text = "pi")))

        assertEquals("pi", viewModel.filters.value.text)
    }

    @Test
    fun `servings input keeps only digits and limits the length`() = runTest {
        val viewModel = createViewModel()

        viewModel.onEvent(HomeEvent.FiltersChanged(SearchFilters(servings = "a1b2c3")))

        assertEquals("12", viewModel.filters.value.servings)
    }
}
