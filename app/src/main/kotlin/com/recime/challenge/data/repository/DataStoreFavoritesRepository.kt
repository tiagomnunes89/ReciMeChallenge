package com.recime.challenge.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.recime.challenge.domain.repository.FavoritesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DataStoreFavoritesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : FavoritesRepository {

    override fun observeFavoriteIds(): Flow<Set<String>> =
        dataStore.data.map { it[FAVORITE_IDS] ?: emptySet() }

    override suspend fun toggleFavorite(id: String) {
        dataStore.edit { prefs ->
            val current = prefs[FAVORITE_IDS] ?: emptySet()
            prefs[FAVORITE_IDS] = if (id in current) current - id else current + id
        }
    }

    private companion object {
        val FAVORITE_IDS = stringSetPreferencesKey("favorite_recipe_ids")
    }
}
