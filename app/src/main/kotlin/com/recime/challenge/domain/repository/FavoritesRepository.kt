package com.recime.challenge.domain.repository

import com.recime.challenge.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteIds(): Flow<Set<String>>

        fun observeFavoriteRecipes(): Flow<List<Recipe>>

        suspend fun toggleFavorite(id: String)
}
