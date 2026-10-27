package com.recime.challenge.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
internal data class RecipeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val servings: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    @ColumnInfo(name = "dietary_attributes") val dietaryAttributes: List<String>,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
)
