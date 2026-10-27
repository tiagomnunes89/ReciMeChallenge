package com.recime.challenge.data.repository

import androidx.paging.PagingSource.LoadResult
import androidx.paging.testing.TestPager
import com.recime.challenge.domain.model.DietaryAttribute
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipePagingSourceTest {

    private val dto = RecipeDto(
        id = "1",
        title = "Pasta",
        description = "Tasty",
        servings = 2,
        ingredients = listOf("spaghetti"),
        instructions = listOf("Boil"),
        dietaryAttributes = listOf("vegetarian", "unknown_attribute"),
        imageUrl = "https://cdn.example.com/pasta.jpg",
    )

    private fun pagerFor(api: FakeRecipeApi, query: RecipeQuery = RecipeQuery()) =
        TestPager(DefaultRecipeRepository.PAGING_CONFIG, RecipePagingSource(api, query))

    @Test
    fun `refresh maps DTOs to domain models ignoring unknown attributes`() = runTest {
        val api = FakeRecipeApi(listOf(RecipesResponseDto(listOf(dto))))

        val actualPage = pagerFor(api).refresh() as LoadResult.Page

        val actualRecipe = actualPage.data.single()
        assertEquals("Pasta", actualRecipe.title)
        assertEquals(setOf(DietaryAttribute.VEGETARIAN), actualRecipe.dietaryAttributes)
        assertEquals("https://cdn.example.com/pasta.jpg", actualRecipe.imageUrl)
    }

    @Test
    fun `append walks the pages until the API reports no more results`() = runTest {
        val api = FakeRecipeApi(
            listOf(
                RecipesResponseDto(listOf(dto), hasNext = true),
                RecipesResponseDto(listOf(dto.copy(id = "2")), hasNext = false),
            ),
        )
        val pager = pagerFor(api)

        val firstPage = pager.refresh() as LoadResult.Page
        val secondPage = pager.append() as LoadResult.Page

        assertNull(firstPage.prevKey)
        assertEquals(2, firstPage.nextKey)
        assertEquals(1, secondPage.prevKey)
        assertNull(secondPage.nextKey)
        assertEquals(listOf(1, 2), api.searchRequests.map { it.page })
        assertTrue(api.searchRequests.all { it.pageSize == DefaultRecipeRepository.PAGE_SIZE })
    }

    @Test
    fun `network failures become load errors instead of crashing`() = runTest {
        val expectedFailure = IOException("offline")
        val api = FakeRecipeApi(listOf(RecipesResponseDto(emptyList())), failure = expectedFailure)

        val actualResult = pagerFor(api).refresh()

        assertSame(expectedFailure, (actualResult as LoadResult.Error).throwable)
    }

    @Test
    fun `empty filters are omitted from the request`() = runTest {
        val api = FakeRecipeApi(listOf(RecipesResponseDto(emptyList())))

        pagerFor(api, RecipeQuery(text = "  ")).refresh()

        val actualRequest = api.searchRequests.single()
        assertNull(actualRequest.text)
        assertNull(actualRequest.vegetarian)
        assertNull(actualRequest.includeIngredients)
        assertNull(actualRequest.excludeIngredients)
        assertNull(actualRequest.instruction)
    }

    @Test
    fun `active filters are forwarded to the API`() = runTest {
        val api = FakeRecipeApi(listOf(RecipesResponseDto(emptyList())))
        val inputQuery = RecipeQuery(
            text = " pasta ",
            vegetarianOnly = true,
            servings = 2,
            includeIngredients = listOf("garlic"),
            excludeIngredients = listOf("meat"),
            instructionContains = "boil",
        )

        pagerFor(api, inputQuery).refresh()

        val actualRequest = api.searchRequests.single()
        assertEquals("pasta", actualRequest.text)
        assertEquals(true, actualRequest.vegetarian)
        assertEquals(2, actualRequest.servings)
        assertEquals(listOf("garlic"), actualRequest.includeIngredients)
        assertEquals(listOf("meat"), actualRequest.excludeIngredients)
        assertEquals("boil", actualRequest.instruction)
    }

    @Test
    fun `long scrolls keep memory bounded by dropping the pages far from the viewport`() = runTest {
        val totalPages = 30
        val pages = List(totalPages) { page ->
            val recipes = List(DefaultRecipeRepository.PAGE_SIZE) { dto.copy(id = "$page-$it") }
            RecipesResponseDto(recipes, hasNext = page < totalPages - 1)
        }
        val pager = pagerFor(FakeRecipeApi(pages))

        pager.refresh()
        repeat(totalPages - 1) { pager.append() }

        val loadedItems = pager.getPages().sumOf { it.data.size }
        assertTrue(loadedItems <= DefaultRecipeRepository.PAGING_CONFIG.maxSize)
    }
}
