package com.recime.challenge.data.remote.api

import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.data.remote.model.RecipesResponseDto
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
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): RecipesResponseDto

    @GET("recipes")
    suspend fun getRecipes(@Query("ids") ids: List<String>): RecipesResponseDto

    @GET("recipes/{id}")
    suspend fun getRecipe(@Path("id") id: String): RecipeDto
}
