package com.recime.challenge.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.recime.challenge.domain.model.DEFAULT_PAGE_SIZE
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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

    val filters: StateFlow<SearchFilters> = mutableFilters.asStateFlow()

    val recipes = mutableFilters
        .map(SearchFilters::toQuery)
        .distinctUntilChanged()
        .debounce { query -> if (query == RecipeQuery()) 0L else SEARCH_DEBOUNCE_MS }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            Pager(
                config = PAGING_CONFIG,
                pagingSourceFactory = { HomePagingSource(recipeRepository, query) },
            ).flow
        }
        .cachedIn(viewModelScope)

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.FiltersChanged -> mutableFilters.update {
                event.filters.copy(servings = event.filters.servings.filter(Char::isDigit).take(MAX_SERVINGS_DIGITS))
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
        const val MAX_SERVINGS_DIGITS = 2
        val PAGING_CONFIG = PagingConfig(
            pageSize = DEFAULT_PAGE_SIZE,
            initialLoadSize = DEFAULT_PAGE_SIZE,
            prefetchDistance = DEFAULT_PAGE_SIZE / 2,
            enablePlaceholders = false,
        )
    }
}
