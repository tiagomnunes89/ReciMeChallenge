package com.recime.challenge.domain.model

data class RecipePage(
    val recipes: List<Recipe>,
    val nextCursor: String?,
)
