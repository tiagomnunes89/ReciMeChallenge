package com.recime.challenge.ui.home

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.domain.repository.RecipeRepository
import kotlinx.coroutines.CancellationException

internal class HomePagingSource(
    private val repository: RecipeRepository,
    private val query: RecipeQuery,
) : PagingSource<String, Recipe>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Recipe> {
        return try {
            val result = repository.searchRecipes(query, params.key, params.loadSize)
            LoadResult.Page(
                data = result.recipes,
                prevKey = null,
                nextKey = result.nextCursor,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Recipe>): String? = null
}
