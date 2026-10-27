package com.recime.challenge.data.remote.mock

import android.content.Context
import com.recime.challenge.common.dispatchers.DispatcherProvider
import com.recime.challenge.data.remote.api.RecipeApi
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

internal class MockRecipeApi @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
    private val dispatchers: DispatcherProvider,
) : RecipeApi {

    private val recipes: List<RecipeDto> by lazy {
        val text = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
        json.decodeFromString<RecipesResponseDto>(text).recipes.simulateCatalog(SIMULATED_CATALOG_SIZE)
    }

    override suspend fun searchRecipes(
        text: String?,
        vegetarian: Boolean?,
        servings: Int?,
        includeIngredients: List<String>?,
        excludeIngredients: List<String>?,
        instruction: String?,
        page: Int,
        pageSize: Int,
    ): RecipesResponseDto = withContext(dispatchers.io) {
        delay(NETWORK_LATENCY_MS)
        val filtered = recipes.filterBy(
            text,
            vegetarian,
            servings,
            includeIngredients,
            excludeIngredients,
            instruction,
        )
        val fromIndex = (((page - 1).coerceAtLeast(0).toLong() * pageSize)
            .coerceAtMost(filtered.size.toLong())).toInt()
        val toIndex = ((fromIndex.toLong() + pageSize.coerceAtLeast(1))
            .coerceAtMost(filtered.size.toLong())).toInt()
        RecipesResponseDto(
            recipes = filtered.subList(fromIndex, toIndex),
            hasNext = toIndex < filtered.size,
        )
    }

    override suspend fun getRecipe(id: String): RecipeDto = withContext(dispatchers.io) {
        delay(NETWORK_LATENCY_MS)
        recipes.firstOrNull { it.id == id }
            ?: throw HttpException(Response.error<Any>(404, "Recipe not found".toResponseBody()))
    }

    override suspend fun getRecipes(ids: List<String>): RecipesResponseDto = withContext(dispatchers.io) {
        delay(NETWORK_LATENCY_MS)
        RecipesResponseDto(recipes.filter { it.id in ids })
    }

    private companion object {
        const val ASSET_NAME = "recipes.json"
        const val NETWORK_LATENCY_MS = 250L

        // A real backend serves thousands of recipes; the mock multiplies the seed data to exercise paging.
        const val SIMULATED_CATALOG_SIZE = 2_000
    }
}
