package com.recime.challenge.testing

import androidx.room.Room
import com.recime.challenge.data.local.ReciMeDatabase
import com.recime.challenge.data.local.RecipeEntity
import org.robolectric.RuntimeEnvironment

internal fun createInMemoryDatabase(): ReciMeDatabase =
    Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), ReciMeDatabase::class.java)
        .allowMainThreadQueries()
        .build()

internal fun recipeEntity(id: String, title: String = "Recipe $id"): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    description = "$title description",
    servings = 2,
    ingredients = listOf("ingredient"),
    instructions = listOf("Cook it."),
    dietaryAttributes = listOf("vegetarian"),
    imageUrl = null,
)
