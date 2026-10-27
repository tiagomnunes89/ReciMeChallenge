package com.recime.challenge.data.repository

import com.recime.challenge.data.local.ReciMeDatabase
import com.recime.challenge.data.local.RecipeLocalDataSource
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import com.recime.challenge.domain.model.DEFAULT_PAGE_SIZE
import com.recime.challenge.domain.model.RecipeNotFoundException
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.testing.createInMemoryDatabase
import com.recime.challenge.testing.recipeEntity
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class DefaultRecipeRepositoryTest {

    private val dto = RecipeDto(
        id = "1",
        title = "Pasta",
        description = "Tasty",
        servings = 2,
        ingredients = listOf("spaghetti"),
        instructions = listOf("Boil"),
    )

    private val database: ReciMeDatabase = createInMemoryDatabase()
    private val api = FakeRecipeApi(listOf(RecipesResponseDto(listOf(dto, dto.copy(id = "2")))))
    private val repository = DefaultRecipeRepository(api, RecipeLocalDataSource(database.recipeDao()))

    @After
    fun tearDown() = database.close()

    @Test
    fun `the repository returns a mapped page and caches its recipes`() = runTest {
        val actualRecipes = repository.searchRecipes(
            query = RecipeQuery(),
            cursor = null,
            limit = DEFAULT_PAGE_SIZE,
        )

        assertEquals(listOf("1", "2"), actualRecipes.recipes.map { it.id })
        assertNull(actualRecipes.nextCursor)
        assertEquals("Pasta", database.recipeDao().findById("1")?.title)
    }

    @Test
    fun `blank text is omitted from the API request`() = runTest {
        repository.searchRecipes(
            query = RecipeQuery(text = "  "),
            cursor = null,
            limit = DEFAULT_PAGE_SIZE,
        )

        assertNull(api.searchRequests.single().text)
        assertEquals("Pasta", database.recipeDao().findById("1")?.title)
    }

    @Test
    fun `a filtered search is sent to the API and cached`() = runTest {
        val actualRecipes = repository.searchRecipes(
            query = RecipeQuery(text = "pasta"),
            cursor = null,
            limit = DEFAULT_PAGE_SIZE,
        )

        assertEquals(listOf("1", "2"), actualRecipes.recipes.map { it.id })
        assertEquals("pasta", api.searchRequests.single().text)
        assertEquals("Pasta", database.recipeDao().findById("1")?.title)
    }

    @Test
    fun `getRecipe returns the mapped recipe and caches it`() = runTest {
        assertEquals("Pasta", repository.getRecipe("1").title)
        assertEquals("Pasta", database.recipeDao().findById("1")?.title)
    }

    @Test
    fun `getRecipe serves the cached copy when the API cannot be reached`() = runTest {
        repository.getRecipe("1")
        api.failure = IOException("offline")

        val actualRecipe = repository.getRecipe("1")

        assertEquals("Pasta", actualRecipe.title)
    }

    @Test
    fun `getRecipe rethrows the failure when nothing is cached`() = runTest {
        api.failure = IOException("offline")

        val actualError = runCatching { repository.getRecipe("1") }.exceptionOrNull()

        assertTrue(actualError is IOException)
    }

    @Test
    fun `getRecipe maps HTTP 404 to RecipeNotFoundException`() = runTest {
        val actualError = runCatching { repository.getRecipe("missing") }.exceptionOrNull()

        assertTrue(actualError is RecipeNotFoundException)
        assertEquals("missing", (actualError as RecipeNotFoundException).recipeId)
    }

    @Test
    fun `a recipe the server no longer has is not served from the cache`() = runTest {
        database.recipeDao().upsert(recipeEntity("gone"))

        val actualError = runCatching { repository.getRecipe("gone") }.exceptionOrNull()

        assertTrue(actualError is RecipeNotFoundException)
    }
}
