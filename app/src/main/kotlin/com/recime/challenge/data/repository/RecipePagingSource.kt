package com.recime.challenge.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.recime.challenge.data.mapper.toDomain
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.data.remote.api.RecipeApi
import kotlinx.coroutines.CancellationException

/** Page-number keyed source; the API tells whether another page exists, so no total count is needed. */
internal class RecipePagingSource(
    private val api: RecipeApi,
    private val query: RecipeQuery,
) : PagingSource<Int, Recipe>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Recipe> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = api.searchRecipes(
                text = query.text.trim().ifEmpty { null },
                vegetarian = query.vegetarianOnly.takeIf { it },
                servings = query.servings,
                includeIngredients = query.includeIngredients.ifEmpty { null },
                excludeIngredients = query.excludeIngredients.ifEmpty { null },
                instruction = query.instructionContains.trim().ifEmpty { null },
                page = page,
                pageSize = params.loadSize,
            )
            LoadResult.Page(
                data = response.recipes.map { it.toDomain() },
                prevKey = (page - 1).takeIf { it >= FIRST_PAGE },
                nextKey = (page + 1).takeIf { response.hasNext },
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Recipe>): Int? =
        state.anchorPosition
            ?.let(state::closestPageToPosition)
            ?.let { page -> page.prevKey?.plus(1) ?: page.nextKey?.minus(1) }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
