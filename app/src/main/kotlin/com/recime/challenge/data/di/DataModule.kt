package com.recime.challenge.data.di

import android.content.Context
import androidx.room.Room
import com.recime.challenge.data.local.ReciMeDatabase
import com.recime.challenge.data.local.RecipeLocalDataSource
import com.recime.challenge.data.local.RecipeSnapshotStore
import com.recime.challenge.data.repository.DefaultRecipeRepository
import com.recime.challenge.data.repository.RoomFavoritesRepository
import com.recime.challenge.domain.repository.FavoritesRepository
import com.recime.challenge.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: DefaultRecipeRepository): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: RoomFavoritesRepository): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindRecipeSnapshotStore(impl: RecipeLocalDataSource): RecipeSnapshotStore

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): ReciMeDatabase =
            Room.databaseBuilder(context, ReciMeDatabase::class.java, ReciMeDatabase.NAME)
                .build()
    }
}
