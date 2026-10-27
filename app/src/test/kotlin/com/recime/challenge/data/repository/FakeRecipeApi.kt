package com.recime.challenge.data.repository

import com.recime.challenge.data.remote.api.RecipeApi
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

internal class FakeRecipeApi(
    private val pages: List<RecipesResponseDto>,
    var failure: Throwable? = null,
) : RecipeApi {

    data class SearchRequest(
        val text: String?,
        val vegetarian: Boolean?,
        val servings: Int?,
        val includeIngredients: List<String>?,
        val excludeIngredients: List<String>?,
        val instruction: String?,
        val cursor: String?,
        val limit: Int,
    )

    val searchRequests = mutableListOf<SearchRequest>()

    override suspend fun searchRecipes(
        text: String?,
        vegetarian: Boolean?,
        servings: Int?,
        includeIngredients: List<String>?,
        excludeIngredients: List<String>?,
        instruction: String?,
        cursor: String?,
        limit: Int,
    ): RecipesResponseDto {
        searchRequests += SearchRequest(
            text, vegetarian, servings, includeIngredients, excludeIngredients, instruction, cursor, limit,
        )
        failure?.let { throw it }
        return pages.firstOrNull { it.nextCursor == cursor } ?: pages.first()
    }

    override suspend fun getRecipe(id: String): RecipeDto {
        failure?.let { throw it }
        return pages.flatMap { it.recipes }.firstOrNull { it.id == id }
            ?: throw HttpException(Response.error<Any>(404, "not found".toResponseBody()))
    }
}
