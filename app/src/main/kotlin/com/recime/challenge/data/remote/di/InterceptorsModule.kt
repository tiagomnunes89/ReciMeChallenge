package com.recime.challenge.data.remote.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import okhttp3.Interceptor

@Module
@InstallIn(SingletonComponent::class)
internal abstract class InterceptorsModule {

    @Multibinds
    abstract fun bindInterceptors(): Set<Interceptor>
}
