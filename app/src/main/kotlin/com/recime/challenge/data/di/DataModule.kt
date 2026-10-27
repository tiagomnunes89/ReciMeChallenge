package com.recime.challenge.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.recime.challenge.data.local.ReciMeDatabase
import com.recime.challenge.data.local.RecipeDao
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

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): ReciMeDatabase =
            Room.databaseBuilder(context, ReciMeDatabase::class.java, ReciMeDatabase.NAME)
                .addMigrations(MIGRATION_1_2)
                .build()

        @Provides
        fun provideRecipeDao(database: ReciMeDatabase): RecipeDao =
            database.recipeDao()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS catalog_entries")
                db.execSQL("DROP TABLE IF EXISTS catalog_state")
            }
        }
    }
}
