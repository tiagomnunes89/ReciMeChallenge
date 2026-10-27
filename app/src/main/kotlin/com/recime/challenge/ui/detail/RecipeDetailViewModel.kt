package com.recime.challenge.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.repository.FavoritesRepository
import com.recime.challenge.domain.repository.RecipeRepository
import com.recime.challenge.ui.navigation.RecipeDetailDestination
import com.recime.challenge.ui.toUserMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipes: RecipeRepository,
    private val favorites: FavoritesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recipeId = savedStateHandle.toRoute<RecipeDetailDestination>().recipeId
    private val retryCount = MutableStateFlow(0)
    private val favoriteActionError = MutableStateFlow<Int?>(null)

    private val contentState: StateFlow<RecipeDetailUiState> = retryCount
        .flatMapLatest { loadRecipe() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecipeDetailUiState.Loading,
        )

    val uiState: StateFlow<RecipeDetailUiState> = combine(
        contentState,
        favoriteActionError,
    ) { state, error ->
        error?.let(RecipeDetailUiState::Error) ?: state
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecipeDetailUiState.Loading,
    )

    fun onRetry() {
        favoriteActionError.value = null
        retryCount.update { it + 1 }
    }

    fun onFavoriteClick() {
        viewModelScope.launch {
            try {
                favorites.toggleFavorite(recipeId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                favoriteActionError.value = exception.toUserMessageRes()
            }
        }
    }

    private fun Recipe.toUiState(isFavorite: Boolean): RecipeDetailUiState =
        RecipeDetailUiState.Success(this, isFavorite)

    private fun loadRecipe(): Flow<RecipeDetailUiState> = flow {
        val recipe = recipes.getRecipe(recipeId)
        emitAll(favorites.observeFavoriteIds().map { ids -> recipe.toUiState(isFavorite = recipeId in ids) })
    }
        .distinctUntilChanged()
        .onStart { emit(RecipeDetailUiState.Loading) }
        .catch { emit(RecipeDetailUiState.Error(it.toUserMessageRes())) }
}
