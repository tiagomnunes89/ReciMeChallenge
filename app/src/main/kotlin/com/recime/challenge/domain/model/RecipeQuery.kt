package com.recime.challenge.domain.model

data class RecipeQuery(
    val text: String = "",
    val vegetarianOnly: Boolean = false,
    val servings: Int? = null,
    val includeIngredients: List<String> = emptyList(),
    val excludeIngredients: List<String> = emptyList(),
    val instructionContains: String = "",
)
