package com.recime.challenge.domain.di

import com.recime.challenge.domain.repository.FavoritesRepository
import com.recime.challenge.domain.repository.RecipeRepository
import com.recime.challenge.domain.usecase.ObserveFavoriteRecipesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DomainModule {

    @Provides
    fun provideObserveFavoriteRecipesUseCase(
        recipes: RecipeRepository,
        favorites: FavoritesRepository,
    ) = ObserveFavoriteRecipesUseCase(recipes, favorites)
}
