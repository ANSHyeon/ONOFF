package com.anshyeon.onoff.di

import com.anshyeon.onoff.BuildConfig
import com.anshyeon.onoff.network.ApiCallAdapterFactory
import com.anshyeon.onoff.network.FireBaseApiClient
import com.anshyeon.onoff.network.KakaoLocalApiClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FireBaseRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KakaoLocalRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    @FireBaseRetrofit
    @Singleton
    @Provides
    fun provideFireBaseRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.FIREBASE_REALTIME_DB_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ApiCallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideFireBaseClient(@FireBaseRetrofit retrofit: Retrofit): FireBaseApiClient {
        return retrofit.create(FireBaseApiClient::class.java)
    }

    @KakaoLocalRetrofit
    @Singleton
    @Provides
    fun provideKakaoLocalRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.KAKAO_LOCAL_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(ApiCallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideKakaoLocalClient(@KakaoLocalRetrofit retrofit: Retrofit): KakaoLocalApiClient {
        return retrofit.create(KakaoLocalApiClient::class.java)
    }
}