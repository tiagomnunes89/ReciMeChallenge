package com.recime.challenge.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.recime.challenge.data.repository.DataStoreFavoritesRepository
import com.recime.challenge.data.repository.DefaultRecipeRepository
import com.recime.challenge.domain.repository.FavoritesRepository
import com.recime.challenge.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: DefaultRecipeRepository): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: DataStoreFavoritesRepository): FavoritesRepository

    companion object {
        @Provides
        @Singleton
        fun provideFavoritesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.favoritesDataStore
    }
}
