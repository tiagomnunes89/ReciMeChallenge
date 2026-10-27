package com.recime.challenge.domain.usecase

import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.usecase.ObserveFavoriteRecipesUseCase
import com.recime.challenge.testing.FakeFavoritesRepository
import com.recime.challenge.testing.FakeRecipeRepository
import com.recime.challenge.ui.home.previewRecipes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveFavoriteRecipesUseCaseTest {

    private val favorites = FakeFavoritesRepository()
    private val useCase = ObserveFavoriteRecipesUseCase(recipes = FakeRecipeRepository(), favorites = favorites)

    @Test
    fun `no favorites returns an empty list`() = runTest {
        assertEquals(emptyList<Recipe>(), useCase().first())
    }

    @Test
    fun `returns only the recipes whose id is favorited`() = runTest {
        val inputIds = previewRecipes.take(1).map { it.id }.toSet()
        favorites.favoriteIds.value = inputIds

        assertEquals(inputIds, useCase().first().map { it.id }.toSet())
    }
}
