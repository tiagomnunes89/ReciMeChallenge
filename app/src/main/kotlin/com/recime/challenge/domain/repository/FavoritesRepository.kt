package com.recime.challenge.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavoriteIds(): Flow<Set<String>>

    suspend fun toggleFavorite(id: String)
}
