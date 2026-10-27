package com.recime.challenge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [RecipeEntity::class, FavoriteEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(StringListConverter::class)
internal abstract class ReciMeDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val NAME = "recime.db"
    }
}
