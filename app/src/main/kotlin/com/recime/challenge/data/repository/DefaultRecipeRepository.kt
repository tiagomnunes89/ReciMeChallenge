package com.recime.challenge.data.repository

import com.recime.challenge.data.local.RecipeLocalDataSource
import com.recime.challenge.data.mapper.toDomain
import com.recime.challenge.data.mapper.toEntity
import com.recime.challenge.data.remote.api.RecipeApi
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.domain.model.DEFAULT_PAGE_SIZE
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipeNotFoundException
import com.recime.challenge.domain.model.RecipePage
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.domain.repository.RecipeRepository
import java.net.HttpURLConnection
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

internal class DefaultRecipeRepository @Inject constructor(
    private val api: RecipeApi,
    private val localDataSource: RecipeLocalDataSource,
) : RecipeRepository {

    override suspend fun searchRecipes(query: RecipeQuery, cursor: String?, limit: Int): RecipePage {
        val response = api.searchRecipes(
            text = query.text.trim().ifEmpty { null },
            vegetarian = query.vegetarianOnly.takeIf { it },
            servings = query.servings,
            includeIngredients = query.includeIngredients.ifEmpty { null },
            excludeIngredients = query.excludeIngredients.ifEmpty { null },
            instruction = query.instructionContains.trim().ifEmpty { null },
            cursor = cursor,
            limit = limit,
        )
        localDataSource.saveAll(response.recipes.map { it.toEntity() })
        return RecipePage(
            recipes = response.recipes.map { it.toDomain() },
            nextCursor = response.nextCursor,
        )
    }

    override suspend fun getRecipe(id: String): Recipe {
        val recipe: RecipeDto = try {
            api.getRecipe(id)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            return recoverRecipe(id, failure = exception)
        }
        localDataSource.save(recipe.toEntity())
        return recipe.toDomain()
    }

    private suspend fun recoverRecipe(id: String, failure: Exception): Recipe {
        if (failure.isNotFound()) throw RecipeNotFoundException(id)
        return localDataSource.findById(id)?.toDomain() ?: throw failure
    }

    private fun Exception.isNotFound(): Boolean =
        this is HttpException && code() == HttpURLConnection.HTTP_NOT_FOUND
}
