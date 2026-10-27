package com.recime.challenge.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecipesResponseDto(
    val recipes: List<RecipeDto>,
    @SerialName("has_next") val hasNext: Boolean = false,
)

@Serializable
data class RecipeDto(
    val id: String,
    val title: String,
    val description: String,
    val servings: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    @SerialName("dietary_attributes") val dietaryAttributes: List<String> = emptyList(),
    @SerialName("image_url") val imageUrl: String? = null,
)
