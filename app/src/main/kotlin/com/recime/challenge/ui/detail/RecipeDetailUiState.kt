package com.recime.challenge.ui.detail

import androidx.annotation.StringRes
import com.recime.challenge.domain.model.Recipe

sealed interface RecipeDetailUiState {
    data object Loading : RecipeDetailUiState
    data class Success(val recipe: Recipe, val isFavorite: Boolean = false) : RecipeDetailUiState
    data class Error(@StringRes val messageRes: Int) : RecipeDetailUiState
}
