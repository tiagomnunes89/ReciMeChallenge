package com.recime.challenge.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.recime.challenge.domain.usecase.ObserveFavoriteRecipesUseCase
import com.recime.challenge.ui.toUserMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val observeFavoriteRecipes: ObserveFavoriteRecipesUseCase,
) : ViewModel() {

    private val retryCount = MutableStateFlow(0)

    val uiState: StateFlow<FavoritesUiState> = retryCount
        .flatMapLatest { loadFavorites() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = FavoritesUiState.Loading,
        )

    fun onRetry() = retryCount.update { it + 1 }

    private fun loadFavorites(): Flow<FavoritesUiState> = observeFavoriteRecipes()
        .map<_, FavoritesUiState>(FavoritesUiState::Success)
        .onStart { emit(FavoritesUiState.Loading) }
        .catch { emit(FavoritesUiState.Error(it.toUserMessageRes())) }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
