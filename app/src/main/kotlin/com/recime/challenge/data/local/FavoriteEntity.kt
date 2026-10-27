package com.recime.challenge.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
internal data class FavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "favorited_at") val favoritedAt: Long,
)
