package com.recime.challenge.ui.home

import androidx.paging.testing.asSnapshot
import com.recime.challenge.domain.model.DEFAULT_PAGE_SIZE
import com.recime.challenge.domain.model.RecipePage
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.testing.FakeRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun `filters are exposed immediately so text fields never lag behind the debounce`() = runTest {
        val viewModel = HomeViewModel(repository)

        viewModel.onFiltersChanged(SearchFilters(searchText = "pi"))

        assertEquals("pi", viewModel.filters.value.searchText)
    }

    @Test
    fun `servings input keeps only digits and limits the length`() = runTest {
        val viewModel = HomeViewModel(repository)

        viewModel.onFiltersChanged(SearchFilters(servings = "a1b2c3"))

        assertEquals("12", viewModel.filters.value.servings)
    }

    @Test
    fun `the first page is requested with the default query and page size`() = runTest {
        val viewModel = HomeViewModel(repository)

        val actualRecipes = viewModel.recipes.asSnapshot {}

        assertEquals(previewRecipes, actualRecipes)
        assertEquals(listOf(RecipeQuery()), repository.queries)
        assertEquals(listOf(null to DEFAULT_PAGE_SIZE), repository.pages)
    }

    @Test
    fun `the next page is requested with the cursor returned by the previous one`() = runTest {
        val firstRecipe = previewRecipes.first()
        val secondRecipe = previewRecipes.last()
        repository.pagesByCursor = mapOf(
            null to RecipePage(listOf(firstRecipe), nextCursor = "cursor-2"),
            "cursor-2" to RecipePage(listOf(secondRecipe), nextCursor = null),
        )
        val viewModel = HomeViewModel(repository)

        val actualRecipes = viewModel.recipes.asSnapshot {}

        assertEquals(listOf(firstRecipe, secondRecipe), actualRecipes)
        assertEquals(listOf<String?>(null, "cursor-2"), repository.pages.map { it.first })
    }

    @Test
    fun `changing the vegetarian tab queries the repository again with the filter`() = runTest {
        val viewModel = HomeViewModel(repository)
        viewModel.recipes.asSnapshot {}

        viewModel.onFiltersChanged(SearchFilters(vegetarianOnly = true))
        viewModel.recipes.asSnapshot {}

        assertEquals(RecipeQuery(vegetarianOnly = true), repository.queries.last())
    }
}
