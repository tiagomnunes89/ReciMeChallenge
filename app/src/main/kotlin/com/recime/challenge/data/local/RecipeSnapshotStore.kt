package com.recime.challenge.data.local

internal interface RecipeSnapshotStore {
    suspend fun upsertAll(recipes: List<RecipeEntity>)

    suspend fun findById(id: String): RecipeEntity?
}
