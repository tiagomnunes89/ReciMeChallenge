package com.recime.challenge.ui.home

import com.recime.challenge.domain.model.RecipeQuery

data class SearchFilters(
    val text: String = "",
    val vegetarianOnly: Boolean = false,
    val servings: String = "",
    val includeIngredients: String = "",
    val excludeIngredients: String = "",
    val instructionText: String = "",
) {
    fun toQuery(): RecipeQuery = RecipeQuery(
        text = text,
        vegetarianOnly = vegetarianOnly,
        servings = servings.toIntOrNull(),
        includeIngredients = includeIngredients.toIngredientList(),
        excludeIngredients = excludeIngredients.toIngredientList(),
        instructionContains = instructionText,
    )
}

private fun String.toIngredientList(): List<String> =
    split(',').map { it.trim() }.filter { it.isNotEmpty() }
