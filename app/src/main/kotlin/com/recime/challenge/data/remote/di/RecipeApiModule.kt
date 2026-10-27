package com.recime.challenge.data.remote.di

import com.recime.challenge.data.remote.api.RecipeApi
import com.recime.challenge.data.remote.mock.MockRecipeApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RecipeApiModule {

    @Binds
    @Singleton
    abstract fun bindRecipeApi(impl: MockRecipeApi): RecipeApi
}
