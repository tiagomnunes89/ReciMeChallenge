package com.recime.challenge.ui.favorites

import androidx.annotation.StringRes
import com.recime.challenge.domain.model.Recipe

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState
    data class Success(val recipes: List<Recipe>) : FavoritesUiState
    data class Error(@StringRes val messageRes: Int) : FavoritesUiState
}
