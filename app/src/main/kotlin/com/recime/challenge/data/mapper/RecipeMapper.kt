package com.recime.challenge.data.mapper

import com.recime.challenge.domain.model.DietaryAttribute
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.data.remote.model.RecipeDto

internal fun RecipeDto.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    servings = servings,
    ingredients = ingredients,
    instructions = instructions,
    dietaryAttributes = dietaryAttributes
        .mapNotNull { name -> DietaryAttribute.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }
        .toSet(),
    imageUrl = imageUrl,
)
