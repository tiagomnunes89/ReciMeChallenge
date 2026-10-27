package com.recime.challenge.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
internal interface RecipeDao {

    @Upsert
    suspend fun upsert(recipe: RecipeEntity)

    @Upsert
    suspend fun upsertAll(recipes: List<RecipeEntity>)

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun findById(id: String): RecipeEntity?
}
