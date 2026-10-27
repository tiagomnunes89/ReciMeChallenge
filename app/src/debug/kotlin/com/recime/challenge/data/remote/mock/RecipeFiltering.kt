package com.recime.challenge.data.remote.mock

import com.recime.challenge.data.remote.model.RecipeDto

internal fun List<RecipeDto>.filterBy(
    text: String?,
    vegetarian: Boolean?,
    servings: Int?,
    includeIngredients: List<String>?,
    excludeIngredients: List<String>?,
    instruction: String?,
): List<RecipeDto> = filter { recipe ->
    val matchesText = text.isNullOrBlank() ||
        recipe.title.contains(text.trim(), ignoreCase = true) ||
        recipe.description.contains(text.trim(), ignoreCase = true)
    val matchesVegetarian = vegetarian != true ||
        recipe.dietaryAttributes.any { it.equals("vegetarian", ignoreCase = true) }
    val matchesServings = servings == null || recipe.servings == servings
    val matchesIncluded = includeIngredients.orEmpty().all { recipe.hasIngredient(it) }
    val matchesExcluded = excludeIngredients.orEmpty().none { recipe.hasIngredient(it) }
    val matchesInstruction = instruction.isNullOrBlank() ||
        recipe.instructions.any { it.contains(instruction.trim(), ignoreCase = true) }

    matchesText && matchesVegetarian && matchesServings &&
        matchesIncluded && matchesExcluded && matchesInstruction
}

private fun RecipeDto.hasIngredient(name: String): Boolean =
    name.isBlank() || ingredients.any { it.contains(name.trim(), ignoreCase = true) }
