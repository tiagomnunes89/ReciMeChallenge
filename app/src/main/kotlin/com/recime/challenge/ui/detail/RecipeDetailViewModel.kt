package com.recime.challenge.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.recime.challenge.BuildConfig
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.rethrowIfCancellation
import com.recime.challenge.domain.repository.FavoritesRepository
import com.recime.challenge.domain.repository.RecipeRepository
import com.recime.challenge.ui.navigation.RecipeDetailDestination
import com.recime.challenge.ui.toUserMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipes: RecipeRepository,
    private val favoritesRepository: FavoritesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recipeId = savedStateHandle.toRoute<RecipeDetailDestination>().recipeId
    private val retryCount = MutableStateFlow(0)
    private val favoriteOverride = MutableStateFlow<Boolean?>(null)
    private val _favoriteActionErrors = MutableSharedFlow<Int>(extraBufferCapacity = 1)

    val favoriteActionErrors = _favoriteActionErrors.asSharedFlow()

    val uiState: StateFlow<RecipeDetailUiState> = retryCount
        .flatMapLatest { loadRecipe() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecipeDetailUiState.Loading,
        )

    fun onRetry() = retryCount.update { it + 1 }

    fun onFavoriteClick() {
        viewModelScope.launch {
            val isFavorite = (uiState.value as? RecipeDetailUiState.Success)?.isFavorite == true
            favoriteOverride.value = !isFavorite
            try {
                favoritesRepository.toggleFavorite(recipeId)
                favoriteOverride.value = null
            } catch (exception: CancellationException) {
                favoriteOverride.value = null
                throw exception
            } catch (exception: Exception) {
                favoriteOverride.value = null
                if (BuildConfig.DEBUG) {
                    Log.e("RecipeDetailViewModel", "Unable to toggle favorite", exception)
                }
                _favoriteActionErrors.emit(exception.toUserMessageRes())
            }
        }
    }

    private fun Recipe.toUiState(isFavorite: Boolean): RecipeDetailUiState =
        RecipeDetailUiState.Success(this, isFavorite)

    private fun loadRecipe(): Flow<RecipeDetailUiState> = flow {
        val recipe = recipes.getRecipe(recipeId)
        emitAll(
            combine(
                favoritesRepository.observeFavoriteIds(),
                favoriteOverride,
            ) { ids, override ->
                recipe.toUiState(isFavorite = override ?: (recipeId in ids))
            },
        )
    }
        .distinctUntilChanged()
        .onStart { emit(RecipeDetailUiState.Loading) }
        .catch { cause: Throwable ->
            cause.rethrowIfCancellation()
            emit(RecipeDetailUiState.Error(cause.toUserMessageRes()))
        }
}
