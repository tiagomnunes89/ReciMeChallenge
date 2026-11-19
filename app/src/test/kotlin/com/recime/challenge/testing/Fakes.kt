package com.recime.challenge.testing

import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.model.RecipePage
import com.recime.challenge.domain.model.RecipeQuery
import com.recime.challenge.domain.repository.FavoritesRepository
import com.recime.challenge.domain.repository.RecipeRepository
import com.recime.challenge.ui.home.previewRecipes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

internal class FakeRecipeRepository(var recipes: List<Recipe> = previewRecipes) : RecipeRepository {
    var failure: Throwable? = null
    var searchFailure: Throwable? = null
    var pagesByCursor: Map<String?, RecipePage>? = null
    val queries = mutableListOf<RecipeQuery>()
    val pages = mutableListOf<Pair<String?, Int>>()

    override suspend fun searchRecipes(query: RecipeQuery, cursor: String?, limit: Int): RecipePage {
        queries += query
        pages += cursor to limit
        searchFailure?.let { throw it }
        return pagesByCursor?.getValue(cursor) ?: RecipePage(recipes = recipes, nextCursor = null)
    }

    override suspend fun getRecipe(id: String): Recipe {
        failure?.let { throw it }
        return recipes.first { it.id == id }
    }
}

internal class FakeFavoritesRepository(
    initialIds: Set<String> = emptySet(),
    private val recipes: List<Recipe> = previewRecipes,
) : FavoritesRepository {
    val favoriteIds = MutableStateFlow(initialIds)
    var observeFailure: Throwable? = null
    var toggleFailure: Throwable? = null
    var lastToggledId: String? = null
    var subscriptionCount = 0

    override fun observeFavoriteIds(): Flow<Set<String>> = flow {
        subscriptionCount++
        observeFailure?.let { throw it }
        emitAll(favoriteIds)
    }

    override fun observeFavoriteRecipes(): Flow<List<Recipe>> = flow {
        subscriptionCount++
        observeFailure?.let { throw it }
        emit(recipes.filter { it.id in favoriteIds.value })
    }

    override suspend fun toggleFavorite(id: String) {
        toggleFailure?.let { throw it }
        lastToggledId = id
        favoriteIds.value = if (id in favoriteIds.value) {
            favoriteIds.value - id
        } else {
            favoriteIds.value + id
        }
    }
}
