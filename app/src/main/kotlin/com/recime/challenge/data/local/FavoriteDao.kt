package com.recime.challenge.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FavoriteDao {

    @Query("SELECT recipe_id FROM favorites")
    fun observeIds(): Flow<List<String>>

    @Query(
        "SELECT recipes.* FROM favorites " +
            "INNER JOIN recipes ON recipes.id = favorites.recipe_id " +
            "ORDER BY favorites.favorited_at DESC",
    )
    fun observeRecipes(): Flow<List<RecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteEntity)

        @Query("DELETE FROM favorites WHERE recipe_id = :recipeId")
    suspend fun delete(recipeId: String): Int
}
