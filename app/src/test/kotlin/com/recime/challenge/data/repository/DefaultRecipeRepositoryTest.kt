package com.recime.challenge.data.repository

import androidx.paging.testing.asSnapshot
import com.recime.challenge.domain.model.RecipeNotFoundException
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultRecipeRepositoryTest {

    private val dto = RecipeDto(
        id = "1",
        title = "Pasta",
        description = "Tasty",
        servings = 2,
        ingredients = listOf("spaghetti"),
        instructions = listOf("Boil"),
    )

    private val api = FakeRecipeApi(listOf(RecipesResponseDto(listOf(dto, dto.copy(id = "2")))))
    private val repository = DefaultRecipeRepository(api)

    @Test
    fun `searchRecipes streams the recipes returned by the API`() = runTest {
        val actualRecipes = repository.searchRecipes(RecipeQuery()).asSnapshot()

        assertEquals(listOf("1", "2"), actualRecipes.map { it.id })
    }

    @Test
    fun `getRecipe returns the mapped recipe`() = runTest {
        assertEquals("Pasta", repository.getRecipe("1").title)
    }

    @Test
    fun `getRecipe maps HTTP 404 to RecipeNotFoundException`() = runTest {
        val actualError = runCatching { repository.getRecipe("missing") }.exceptionOrNull()

        assertTrue(actualError is RecipeNotFoundException)
        assertEquals("missing", (actualError as RecipeNotFoundException).recipeId)
    }

    @Test
    fun `getRecipes returns the requested recipes`() = runTest {
        assertEquals(listOf("2"), repository.getRecipes(setOf("2")).map { it.id })
    }

    @Test
    fun `getRecipes skips the network call when there are no ids`() = runTest {
        assertEquals(emptyList<Any>(), repository.getRecipes(emptySet()))
        assertEquals(0, api.getRecipesCalls)
    }
}
