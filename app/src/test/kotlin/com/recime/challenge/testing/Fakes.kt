package com.recime.challenge.testing

import androidx.paging.PagingData
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.domain.repository.FavoritesRepository
import com.recime.challenge.domain.repository.RecipeRepository
import com.recime.challenge.ui.home.previewRecipes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

internal class FakeRecipeRepository(var recipes: List<Recipe> = previewRecipes) : RecipeRepository {
    var failure: Throwable? = null
    val queries = mutableListOf<RecipeQuery>()

    override fun searchRecipes(query: RecipeQuery): Flow<PagingData<Recipe>> {
        queries += query
        return flowOf(PagingData.from(recipes))
    }

    override suspend fun getRecipe(id: String): Recipe {
        failure?.let { throw it }
        return recipes.first { it.id == id }
    }

    override suspend fun getRecipes(ids: Set<String>): List<Recipe> {
        failure?.let { throw it }
        return recipes.filter { it.id in ids }
    }
}

internal class FakeFavoritesRepository(initialIds: Set<String> = emptySet()) : FavoritesRepository {
    val favoriteIds = MutableStateFlow(initialIds)
    var observeFailure: Throwable? = null
    var toggleFailure: Throwable? = null
    var lastToggledId: String? = null
    var subscriptionCount = 0

    override fun observeFavoriteIds(): Flow<Set<String>> = flow {
        subscriptionCount++
        observeFailure?.let { throw it }
        emit(favoriteIds.value)
    }

    override suspend fun toggleFavorite(id: String) {
        toggleFailure?.let { throw it }
        lastToggledId = id
    }
}
