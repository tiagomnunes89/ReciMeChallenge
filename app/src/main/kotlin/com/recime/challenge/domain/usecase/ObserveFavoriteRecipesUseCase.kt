package com.recime.challenge.domain.usecase

import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.repository.FavoritesRepository
import com.recime.challenge.domain.repository.RecipeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/** Combines two repositories (persisted ids + remote recipes), which is why it earns a use case. */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveFavoriteRecipesUseCase(
    private val recipes: RecipeRepository,
    private val favorites: FavoritesRepository,
) {
    operator fun invoke(): Flow<List<Recipe>> =
        favorites.observeFavoriteIds().flatMapLatest { ids ->
            flow { emit(if (ids.isEmpty()) emptyList() else recipes.getRecipes(ids)) }
        }
}
