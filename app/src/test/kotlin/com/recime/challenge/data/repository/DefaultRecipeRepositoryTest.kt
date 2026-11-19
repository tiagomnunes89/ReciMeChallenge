package com.recime.challenge.data.repository

import com.recime.challenge.data.local.RecipeEntity
import com.recime.challenge.data.local.RecipeLocalDataSource
import com.recime.challenge.data.local.RecipeSnapshotStore
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import com.recime.challenge.domain.model.RecipeNotFoundException
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.testing.createInMemoryDatabase
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

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
    private val database = createInMemoryDatabase()
    private val api = FakeRecipeApi(listOf(RecipesResponseDto(listOf(dto))))
    private val repository = DefaultRecipeRepository(api, RecipeLocalDataSource(database))

    @After
    fun tearDown() = database.close()

    @Test
    fun `search delegates filters and stores the returned page`() = runTest {
        val result = repository.searchRecipes(
            RecipeQuery(text = "pasta", vegetarianOnly = true, servings = 2),
            cursor = null,
            limit = 20,
        )

        assertEquals(listOf("1"), result.recipes.map { it.id })
        assertEquals("pasta", api.searchRequests.single().text)
        assertEquals(true, api.searchRequests.single().vegetarian)
        assertEquals(2, api.searchRequests.single().servings)
        assertEquals("Pasta", database.recipeDao().findById("1")?.title)
    }

    @Test
    fun `search propagates API failures`() = runTest {
        api.failure = IOException("offline")

        val failure = runCatching {
            repository.searchRecipes(RecipeQuery(), null, 20)
        }.exceptionOrNull()

        assertTrue(failure is IOException)
    }

    @Test
    fun `detail refreshes a saved snapshot from the network`() = runTest {
        repository.searchRecipes(RecipeQuery(), null, 20)
        api.pages = listOf(RecipesResponseDto(listOf(dto.copy(title = "Fresh Pasta"))))

        assertEquals("Fresh Pasta", repository.getRecipe("1").title)
        assertEquals("Fresh Pasta", database.recipeDao().findById("1")?.title)
    }

    @Test
    fun `detail falls back to the saved snapshot when the network is unavailable`() = runTest {
        repository.searchRecipes(RecipeQuery(), null, 20)
        api.failure = IOException("offline")

        assertEquals("Pasta", repository.getRecipe("1").title)
    }

    @Test
    fun `detail does not use stale snapshot when the remote recipe is missing`() = runTest {
        repository.searchRecipes(RecipeQuery(), null, 20)
        api.failure = HttpException(
            Response.error<Any>(
                404,
                "not found".toResponseBody(),
            ),
        )

        val failure = runCatching { repository.getRecipe("1") }.exceptionOrNull()

        assertTrue(failure is RecipeNotFoundException)
    }

    @Test
    fun `detail fetches and stores a recipe missing from the snapshot`() = runTest {
        assertEquals("Pasta", repository.getRecipe("1").title)
        assertEquals("Pasta", database.recipeDao().findById("1")?.title)
    }

    @Test
    fun `missing remote detail maps 404 to a domain exception`() = runTest {
        val failure = runCatching { repository.getRecipe("missing") }.exceptionOrNull()

        assertTrue(failure is RecipeNotFoundException)
    }

    @Test
    fun `search rethrows a canceled call`() = runTest {
        api.failure = IOException("Canceled")

        val failure: Throwable? = runCatching {
            repository.searchRecipes(RecipeQuery(), null, 20)
        }.exceptionOrNull()

        assertTrue(failure is CancellationException)
    }

    @Test
    fun `detail rethrows a canceled call`() = runTest {
        api.failure = IOException("Canceled")

        val failure: Throwable? = runCatching { repository.getRecipe("1") }.exceptionOrNull()

        assertTrue(failure is CancellationException)
    }

    @Test
    fun `search succeeds when snapshot persistence fails`() = runTest {
        val resilientRepository = DefaultRecipeRepository(
            api,
            failingSnapshotStore(IOException("cache unavailable")),
        )

        val result = resilientRepository.searchRecipes(RecipeQuery(), cursor = null, limit = 20)

        assertEquals(listOf("1"), result.recipes.map { it.id })
    }

    @Test
    fun `detail succeeds when snapshot persistence fails`() = runTest {
        val resilientRepository = DefaultRecipeRepository(
            api,
            failingSnapshotStore(IOException("cache unavailable")),
        )

        val result = resilientRepository.getRecipe("1")

        assertEquals("Pasta", result.title)
    }

    @Test
    fun `cache persistence rethrows cancellation`() = runTest {
        val cancellingRepository = DefaultRecipeRepository(
            api,
            failingSnapshotStore(CancellationException("cancelled")),
        )

        val failure: Throwable? = runCatching {
            cancellingRepository.searchRecipes(RecipeQuery(), cursor = null, limit = 20)
        }.exceptionOrNull()

        assertTrue(failure is CancellationException)
    }

    private fun failingSnapshotStore(failure: Throwable): RecipeSnapshotStore =
        FailingRecipeSnapshotStore(RecipeLocalDataSource(database), failure)
}

private class FailingRecipeSnapshotStore(
    private val delegate: RecipeSnapshotStore,
    private val failure: Throwable,
) : RecipeSnapshotStore {
    override suspend fun upsertAll(recipes: List<RecipeEntity>) {
        throw failure
    }

    override suspend fun findById(id: String): RecipeEntity? = delegate.findById(id)
}
