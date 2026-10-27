package com.recime.challenge.domain.model

class RecipeNotFoundException(val recipeId: String) : Exception("Recipe $recipeId was not found")
