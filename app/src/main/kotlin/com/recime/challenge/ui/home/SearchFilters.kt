package com.recime.challenge.ui.home

import com.recime.challenge.domain.model.RecipeQuery

data class SearchFilters(
    val searchText: String = "",
    val vegetarianOnly: Boolean = false,
    val servings: String = "",
    val includeIngredients: String = "",
    val excludeIngredients: String = "",
    val instructionContains: String = "",
) {
    fun toQuery(): RecipeQuery = RecipeQuery(
        text = searchText,
        vegetarianOnly = vegetarianOnly,
        servings = servings.toIntOrNull(),
        includeIngredients = includeIngredients.toIngredientList(),
        excludeIngredients = excludeIngredients.toIngredientList(),
        instructionContains = instructionContains,
    )
}

private fun String.toIngredientList(): List<String> =
    split(',').map { it.trim() }.filter { it.isNotEmpty() }
