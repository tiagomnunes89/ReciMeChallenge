package com.recime.challenge.data.mapper

import com.recime.challenge.data.local.RecipeEntity
import com.recime.challenge.data.remote.model.RecipeDto
import com.recime.challenge.domain.model.DietaryAttribute
import com.recime.challenge.domain.model.Recipe

internal fun RecipeDto.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    servings = servings,
    ingredients = ingredients,
    instructions = instructions,
    dietaryAttributes = dietaryAttributes.toDietaryAttributes(),
    imageUrl = imageUrl,
)

internal fun RecipeDto.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    description = description,
    servings = servings,
    ingredients = ingredients,
    instructions = instructions,
    dietaryAttributes = dietaryAttributes,
    imageUrl = imageUrl,
)

internal fun RecipeEntity.toDomain(): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    servings = servings,
    ingredients = ingredients,
    instructions = instructions,
    dietaryAttributes = dietaryAttributes.toDietaryAttributes(),
    imageUrl = imageUrl,
)

private fun List<String>.toDietaryAttributes(): Set<DietaryAttribute> =
    mapNotNull { name -> DietaryAttribute.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }.toSet()
