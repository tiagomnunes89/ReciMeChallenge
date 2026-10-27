package com.recime.challenge.ui.home

import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.testing.FakeRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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

    private fun TestScope.settle() {
        advanceTimeBy(SETTLE_MS)
        runCurrent()
    }

    @Test
    fun `the initial catalog is requested without waiting for the debounce`() = runTest {
        createViewModel()
        runCurrent()

        assertEquals(listOf(RecipeQuery()), repository.queries)
    }

    @Test
    fun `filter changes are converted to a query`() = runTest {
        val viewModel = createViewModel()

        viewModel.onFiltersChange(
            SearchFilters(
                text = "pasta",
                vegetarianOnly = true,
                servings = "2",
                includeIngredients = "garlic, onion",
                excludeIngredients = " meat ,",
                instructionText = "boil",
            ),
        )
        settle()

        assertEquals(
            RecipeQuery(
                text = "pasta",
                vegetarianOnly = true,
                servings = 2,
                includeIngredients = listOf("garlic", "onion"),
                excludeIngredients = listOf("meat"),
                instructionContains = "boil",
            ),
            repository.queries.last(),
        )
    }

    @Test
    fun `typing quickly only searches for the latest text`() = runTest {
        val viewModel = createViewModel()

        listOf("p", "pa", "pas", "pasta").forEach { text ->
            viewModel.onFiltersChange(SearchFilters(text = text))
            advanceTimeBy(KEYSTROKE_INTERVAL_MS)
        }
        settle()

        assertEquals(listOf(RecipeQuery(), RecipeQuery(text = "pasta")), repository.queries)
    }

    @Test
    fun `clearing every filter searches again immediately`() = runTest {
        val viewModel = createViewModel()
        viewModel.onFiltersChange(SearchFilters(text = "pasta"))
        settle()

        viewModel.onFiltersChange(SearchFilters())
        runCurrent()

        assertEquals(RecipeQuery(), repository.queries.last())
    }

    @Test
    fun `filters are exposed immediately so text fields never lag behind the debounce`() = runTest {
        val viewModel = createViewModel()

        viewModel.onFiltersChange(SearchFilters(text = "pi"))

        assertEquals("pi", viewModel.filters.value.text)
    }

    @Test
    fun `servings input keeps only digits and limits the length`() = runTest {
        val viewModel = createViewModel()

        viewModel.onFiltersChange(SearchFilters(servings = "a1b2c3"))

        assertEquals("12", viewModel.filters.value.servings)
    }

    private companion object {
        const val SETTLE_MS = 400L
        const val KEYSTROKE_INTERVAL_MS = 100L
    }
}
