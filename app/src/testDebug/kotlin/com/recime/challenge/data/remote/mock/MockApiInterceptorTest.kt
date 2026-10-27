package com.recime.challenge.data.remote.mock

import com.recime.challenge.data.remote.api.RecipeApi
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import com.recime.challenge.data.local.ReciMeDatabase
import com.recime.challenge.data.local.RecipeLocalDataSource
import com.recime.challenge.data.repository.DefaultRecipeRepository
import com.recime.challenge.domain.model.RecipeNotFoundException
import com.recime.challenge.testing.createInMemoryDatabase
import java.net.HttpURLConnection
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Config(sdk = [35])
@RunWith(RobolectricTestRunner::class)
class MockApiInterceptorTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val database: ReciMeDatabase = createInMemoryDatabase()
    private val pasta = recipe("pasta", servings = 2, ingredients = listOf("spaghetti", "garlic"), vegetarian = true)
    private val salad = recipe("salad", servings = 4, ingredients = listOf("chicken breast", "lettuce"))
    private val soup = recipe("soup", servings = 4, ingredients = listOf("carrot", "garlic"), vegetarian = true)
    private val interceptor = MockApiInterceptor(
        recipes = { listOf(pasta, salad, soup) },
        json = json,
        latencyMillis = 0,
    )
    private val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
    private val api: RecipeApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RecipeApi::class.java)

    @After
    fun tearDown() = database.close()

    private suspend fun search(
        vegetarian: Boolean? = null,
        servings: Int? = null,
        include: List<String>? = null,
        exclude: List<String>? = null,
        cursor: String? = null,
        limit: Int = 20,
    ): RecipesResponseDto = api.searchRecipes(
        text = null,
        vegetarian = vegetarian,
        servings = servings,
        includeIngredients = include,
        excludeIngredients = exclude,
        instruction = null,
        cursor = cursor,
        limit = limit,
    )

    private fun RecipesResponseDto.ids(): List<String> = recipes.map(RecipeDto::id)

    @Test
    fun `search without filters returns the whole catalog`() = runTest {
        val actualIds = search().ids()

        assertEquals(listOf("pasta", "salad", "soup"), actualIds)
    }

    @Test
    fun `simulated catalog repeats recipes in an interleaved order`() {
        val actualIds = listOf(pasta, salad, soup)
            .simulateCatalog(size = 6)
            .map(RecipeDto::id)

        assertEquals(
            listOf("pasta", "salad", "soup", "pasta-1", "salad-1", "soup-1"),
            actualIds,
        )
    }

    @Test
    fun `search filters travel as query parameters and combine with AND`() = runTest {
        val actualIds = search(vegetarian = true, servings = 4, include = listOf("garlic", "carrot")).ids()

        assertEquals(listOf("soup"), actualIds)
    }

    @Test
    fun `search excludes ingredients sent as repeated parameters`() = runTest {
        val actualIds = search(exclude = listOf("chicken", "carrot")).ids()

        assertEquals(listOf("pasta"), actualIds)
    }

    @Test
    fun `search paginates with an opaque cursor`() = runTest {
        val firstPage = search(limit = 2)
        val lastPage = search(cursor = firstPage.nextCursor, limit = 2)

        assertEquals(listOf("pasta", "salad"), firstPage.ids())
        assertEquals("salad", firstPage.nextCursor)
        assertEquals(listOf("soup"), lastPage.ids())
        assertEquals(null, lastPage.nextCursor)
    }

    @Test
    fun `an unknown cursor returns items after it in id order`() = runTest {
        val actualResponse = search(cursor = "pasta", limit = 2)

        assertEquals(listOf("salad", "soup"), actualResponse.ids())
    }

    @Test
    fun `catalog writes between loads do not duplicate or skip items`() = runTest {
        val catalog = mutableListOf(pasta, salad, soup)
        val changingClient = OkHttpClient.Builder()
            .addInterceptor(MockApiInterceptor({ catalog }, json, latencyMillis = 0))
            .build()

        suspend fun load(cursor: String?): RecipesResponseDto = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(changingClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RecipeApi::class.java)
            .let { api -> api.searchRecipes(null, null, null, null, null, null, cursor, 2) }

        val firstPage = load(null)
        catalog += recipe("aardvark", 1, emptyList())
        catalog += recipe("zzz", 1, emptyList())
        val secondPage = load(firstPage.nextCursor)

        assertEquals(listOf("pasta", "salad"), firstPage.ids())
        assertEquals(listOf("soup", "aardvark"), secondPage.ids())
    }

    @Test
    fun `invalid limits return bad request`() {
        listOf("0", "-1", "101", "not-a-number").forEach { value ->
            val response = client.newCall(
                Request.Builder().url("${BASE_URL}recipes/search?limit=$value").build(),
            ).execute()
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code)
        }
    }

    @Test
    fun `get recipe returns the serialized recipe`() = runTest {
        val actualRecipe = api.getRecipe("salad")

        assertEquals(salad, actualRecipe)
    }

    @Test
    fun `get recipe fails with an HTTP 404 when the id is unknown`() = runTest {
        val actualFailure = runCatching { api.getRecipe("missing") }.exceptionOrNull()

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, (actualFailure as HttpException).code())
    }

    @Test
    fun `the repository turns the HTTP 404 into a domain exception`() = runTest {
        val repository = DefaultRecipeRepository(api, RecipeLocalDataSource(database.recipeDao()))

        val actualFailure = runCatching { repository.getRecipe("missing") }.exceptionOrNull()

        assertEquals("missing", (actualFailure as RecipeNotFoundException).recipeId)
    }

    @Test
    fun `unknown routes answer 404 and other methods answer 405`() {
        val unknownRequest = Request.Builder().url("${BASE_URL}unknown").build()
        val postRequest = Request.Builder()
            .url("${BASE_URL}recipes")
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()

        val unknownResponse = client.newCall(unknownRequest).execute()
        val postResponse = client.newCall(postRequest).execute()

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, unknownResponse.code)
        assertEquals(HttpURLConnection.HTTP_BAD_METHOD, postResponse.code)
    }

    private fun recipe(
        id: String,
        servings: Int,
        ingredients: List<String>,
        vegetarian: Boolean = false,
    ): RecipeDto = RecipeDto(
        id = id,
        title = id,
        description = "$id description",
        servings = servings,
        ingredients = ingredients,
        instructions = listOf("Cook the $id."),
        dietaryAttributes = if (vegetarian) listOf("vegetarian") else emptyList(),
    )

    private companion object {
        const val BASE_URL = "https://api.example.com/"
    }
}
