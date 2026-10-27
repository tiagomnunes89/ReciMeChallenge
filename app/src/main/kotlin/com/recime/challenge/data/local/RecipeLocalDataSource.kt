package com.recime.challenge.data.local

import javax.inject.Inject

internal class RecipeLocalDataSource @Inject constructor(
    private val recipeDao: RecipeDao,
) {

    suspend fun save(recipe: RecipeEntity) {
        recipeDao.upsert(recipe)
    }

    suspend fun saveAll(recipes: List<RecipeEntity>) {
        recipeDao.upsertAll(recipes)
    }

    suspend fun findById(id: String): RecipeEntity? =
        recipeDao.findById(id)
}
