package com.recime.challenge.data.local

import javax.inject.Inject

internal class RecipeLocalDataSource @Inject constructor(
    private val database: ReciMeDatabase,
) : RecipeSnapshotStore {

    suspend fun upsert(recipe: RecipeEntity) {
        database.recipeDao().upsert(recipe)
    }

    override suspend fun upsertAll(recipes: List<RecipeEntity>) {
        database.recipeDao().upsertAll(recipes)
    }

    override suspend fun findById(id: String): RecipeEntity? =
        database.recipeDao().findById(id)

    suspend fun findAll(): List<RecipeEntity> =
        database.recipeDao().findAll()
}
