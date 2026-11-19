package com.recime.challenge.data.remote.api

import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
import com.recime.challenge.domain.model.DEFAULT_PAGE_SIZE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeApi {

    @GET("recipes/search")
    suspend fun searchRecipes(
        @Query("q") text: String?,
        @Query("vegetarian") vegetarian: Boolean?,
        @Query("servings") servings: Int?,
        @Query("include_ingredients") includeIngredients: List<String>?,
        @Query("exclude_ingredients") excludeIngredients: List<String>?,
        @Query("instruction") instruction: String?,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = DEFAULT_PAGE_SIZE,
    ): RecipesResponseDto

    @GET("recipes/{id}")
    suspend fun getRecipe(@Path("id") id: String): RecipeDto
}
