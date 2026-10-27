package com.recime.challenge.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val servings: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val dietaryAttributes: Set<DietaryAttribute>,
    val imageUrl: String? = null,
)

enum class DietaryAttribute { VEGETARIAN, VEGAN, GLUTEN_FREE }
