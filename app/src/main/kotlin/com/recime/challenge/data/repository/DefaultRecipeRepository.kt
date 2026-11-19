package com.recime.challenge.data.repository

import android.util.Log
import com.recime.challenge.BuildConfig
import com.recime.challenge.data.local.RecipeEntity
import com.recime.challenge.data.local.RecipeSnapshotStore
import com.recime.challenge.data.mapper.toDomain
import com.recime.challenge.data.mapper.toEntity
import com.recime.challenge.data.remote.api.RecipeApi
import com.recime.challenge.data.remote.model.RecipesResponseDto
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipeNotFoundException
import com.recime.challenge.domain.model.RecipePage
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.domain.model.rethrowIfCancellation
import com.recime.challenge.domain.repository.RecipeRepository
import java.net.HttpURLConnection
import javax.inject.Inject
import retrofit2.HttpException

internal class DefaultRecipeRepository @Inject constructor(
    private val api: RecipeApi,
    private val snapshotStore: RecipeSnapshotStore,
) : RecipeRepository {

    override suspend fun searchRecipes(query: RecipeQuery, cursor: String?, limit: Int): RecipePage {
        val response: RecipesResponseDto = fetchSearch(query, cursor, limit)
        val recipes: List<Recipe> = response.recipes.map { it.toDomain() }
        persistSnapshotsSafely(response.recipes.map { it.toEntity() })
        return RecipePage(
            recipes = recipes,
            nextCursor = response.nextCursor,
        )
    }

    override suspend fun getRecipe(id: String): Recipe {
        return try {
            api.getRecipe(id).also { persistSnapshotsSafely(listOf(it.toEntity())) }.toDomain()
        } catch (exception: HttpException) {
            if (exception.code() == HttpURLConnection.HTTP_NOT_FOUND) throw RecipeNotFoundException(id)
            if (isTransientHttpFailure(exception)) {
                findCachedRecipe(id)?.let { return it }
            }
            throw exception
        } catch (exception: Exception) {
            exception.rethrowIfCancellation()
            findCachedRecipe(id)?.let { return it }
            throw exception
        }
    }

    private suspend fun findCachedRecipe(id: String): Recipe? =
        try {
            snapshotStore.findById(id)?.toDomain()
        } catch (exception: Exception) {
            exception.rethrowIfCancellation()
            null
        }

    private fun isTransientHttpFailure(exception: HttpException): Boolean =
        exception.code() == HttpURLConnection.HTTP_CLIENT_TIMEOUT ||
            exception.code() == HTTP_TOO_MANY_REQUESTS ||
            exception.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR

    private companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
    }

    private suspend fun fetchSearch(query: RecipeQuery, cursor: String?, limit: Int): RecipesResponseDto =
        try {
            api.searchRecipes(
                text = query.text.takeIf(String::isNotBlank),
                vegetarian = query.vegetarianOnly.takeIf { it },
                servings = query.servings,
                includeIngredients = query.includeIngredients.takeIf(List<String>::isNotEmpty),
                excludeIngredients = query.excludeIngredients.takeIf(List<String>::isNotEmpty),
                instruction = query.instructionContains.takeIf(String::isNotBlank),
                cursor = cursor,
                limit = limit,
            )
        } catch (exception: Exception) {
            exception.rethrowIfCancellation()
            throw exception
        }

    private suspend fun persistSnapshotsSafely(recipes: List<RecipeEntity>) {
        try {
            snapshotStore.upsertAll(recipes)
        } catch (exception: Exception) {
            exception.rethrowIfCancellation()
            if (BuildConfig.DEBUG) {
                Log.w("DefaultRecipeRepository", "Unable to persist recipe snapshot", exception)
            }
        }
    }
}
