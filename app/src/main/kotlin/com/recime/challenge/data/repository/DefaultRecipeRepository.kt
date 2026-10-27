package com.recime.challenge.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.recime.challenge.data.mapper.toDomain
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipeNotFoundException
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.domain.repository.RecipeRepository
import com.recime.challenge.data.remote.api.RecipeApi
import java.net.HttpURLConnection
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

internal class DefaultRecipeRepository @Inject constructor(
    private val api: RecipeApi,
) : RecipeRepository {

    override fun searchRecipes(query: RecipeQuery): Flow<PagingData<Recipe>> =
        Pager(config = PAGING_CONFIG) { RecipePagingSource(api, query) }.flow

    override suspend fun getRecipe(id: String): Recipe =
        try {
            api.getRecipe(id).toDomain()
        } catch (exception: HttpException) {
            if (exception.code() == HttpURLConnection.HTTP_NOT_FOUND) throw RecipeNotFoundException(id)
            throw exception
        }

    override suspend fun getRecipes(ids: Set<String>): List<Recipe> =
        if (ids.isEmpty()) emptyList() else api.getRecipes(ids.toList()).recipes.map { it.toDomain() }

    internal companion object {
        const val PAGE_SIZE = 20

        val PAGING_CONFIG = PagingConfig(
            pageSize = PAGE_SIZE,
            // Page-number keys require every load, including the first, to request exactly one page.
            initialLoadSize = PAGE_SIZE,
            prefetchDistance = PAGE_SIZE / 2,
            enablePlaceholders = false,
            // Bounds memory on very long scrolls: pages far from the viewport are dropped and reloaded on demand.
            maxSize = PAGE_SIZE * MAX_PAGES_IN_MEMORY,
        )

        private const val MAX_PAGES_IN_MEMORY = 10
    }
}
