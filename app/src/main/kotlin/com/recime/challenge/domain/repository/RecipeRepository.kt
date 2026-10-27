package com.recime.challenge.domain.repository

import androidx.paging.PagingData
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipeNotFoundException
import com.recime.challenge.domain.model.RecipeQuery
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    /** Streams the recipes matching [query] page by page; filtering and paging happen on the server. */
    fun searchRecipes(query: RecipeQuery): Flow<PagingData<Recipe>>

    /** @throws RecipeNotFoundException when the recipe does not exist. */
    suspend fun getRecipe(id: String): Recipe

    suspend fun getRecipes(ids: Set<String>): List<Recipe>
}
