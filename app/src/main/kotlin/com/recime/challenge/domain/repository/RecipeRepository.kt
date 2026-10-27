package com.recime.challenge.domain.repository

import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipePage
import com.recime.challenge.domain.model.RecipeQuery

interface RecipeRepository {

    suspend fun searchRecipes(query: RecipeQuery, cursor: String?, limit: Int): RecipePage

    suspend fun getRecipe(id: String): Recipe
}
