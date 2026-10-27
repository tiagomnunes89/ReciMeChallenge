package com.recime.challenge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    recipeRepository: RecipeRepository,
) : ViewModel() {

    private val mutableFilters = MutableStateFlow(SearchFilters())

    /** What the user typed; updated on every keystroke so the text fields never lag. */
    val filters: StateFlow<SearchFilters> = mutableFilters.asStateFlow()

    // A new query cancels the in-flight pager, and `cachedIn` keeps the loaded pages alive across
    // configuration changes and back navigation, so returning from a detail restores the scroll position.
    val recipes: Flow<PagingData<Recipe>> = mutableFilters
        .map(SearchFilters::toQuery)
        .distinctUntilChanged()
        .debounce { query -> if (query == RecipeQuery()) 0L else SEARCH_DEBOUNCE_MS }
        .distinctUntilChanged()
        .flatMapLatest(recipeRepository::searchRecipes)
        .cachedIn(viewModelScope)

    fun onFiltersChange(newFilters: SearchFilters) {
        mutableFilters.update {
            newFilters.copy(servings = newFilters.servings.filter(Char::isDigit).take(MAX_SERVINGS_DIGITS))
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
        const val MAX_SERVINGS_DIGITS = 2
    }
}
