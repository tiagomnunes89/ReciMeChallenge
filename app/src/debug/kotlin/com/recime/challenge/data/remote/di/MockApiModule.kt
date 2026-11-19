package com.recime.challenge.data.remote.di

import android.content.Context
import com.recime.challenge.data.remote.mock.AssetRecipeCatalog
import com.recime.challenge.data.remote.mock.MockApiInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
internal object MockApiModule {

    @Provides
    @Singleton
    @IntoSet
    fun provideMockApiInterceptor(@ApplicationContext context: Context, json: Json): Interceptor {
        val catalog = AssetRecipeCatalog(context, json)
        return MockApiInterceptor(recipes = catalog::recipes, json = json)
    }
}
