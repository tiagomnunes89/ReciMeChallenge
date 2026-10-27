package com.recime.challenge.data.repository

import androidx.room.withTransaction
import com.recime.challenge.data.local.FavoriteEntity
import com.recime.challenge.data.local.ReciMeDatabase
import com.recime.challenge.data.local.RecipeEntity
import com.recime.challenge.data.mapper.toDomain
import com.recime.challenge.domain.model.Recipe
import com.recime.challenge.domain.repository.FavoritesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal class RoomFavoritesRepository(
    private val database: ReciMeDatabase,
    private val currentTimeMillis: () -> Long,
) : FavoritesRepository {

    @Inject
    constructor(database: ReciMeDatabase) : this(database, System::currentTimeMillis)

    override fun observeFavoriteIds(): Flow<Set<String>> =
        database.favoriteDao().observeIds().map { it.toSet() }.distinctUntilChanged()

    override fun observeFavoriteRecipes(): Flow<List<Recipe>> =
        database.favoriteDao().observeRecipes()
            .map { entities -> entities.map(RecipeEntity::toDomain) }
            .distinctUntilChanged()

    override suspend fun toggleFavorite(id: String) {
        database.withTransaction {
            val favoriteDao = database.favoriteDao()
            if (favoriteDao.delete(id) == 0) favoriteDao.insert(FavoriteEntity(id, currentTimeMillis()))
        }
    }
}
