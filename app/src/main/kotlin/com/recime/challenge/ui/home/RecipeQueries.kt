package com.recime.challenge.ui.home

import com.recime.challenge.domain.model.RecipeQuery
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

internal const val SEARCH_DEBOUNCE_MS = 300L

@OptIn(FlowPreview::class)
internal fun Flow<SearchFilters>.toDebouncedQueries(): Flow<RecipeQuery> =
    map(SearchFilters::toQuery)
        .distinctUntilChanged()
        .scan(QueryTransition(RecipeQuery(), RecipeQuery())) { transition: QueryTransition, query: RecipeQuery ->
            QueryTransition(previous = transition.current, current = query)
        }
        .debounce { transition: QueryTransition -> transition.debounceMillis() }
        .map { transition: QueryTransition -> transition.current }
        .distinctUntilChanged()

private data class QueryTransition(val previous: RecipeQuery, val current: RecipeQuery) {

    fun debounceMillis(): Long = if (isImmediate()) 0L else SEARCH_DEBOUNCE_MS

    private fun isImmediate(): Boolean =
        current == RecipeQuery() || previous.copy(vegetarianOnly = current.vegetarianOnly) == current
}
