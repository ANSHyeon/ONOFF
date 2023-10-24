package com.anshyeon.onoff.data.source

import com.anshyeon.onoff.BuildConfig
import com.anshyeon.onoff.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiClient {
    @POST("users/{uid}.json")
    suspend fun createUser(
        @Path("uid") uid: String,
        @Query("auth") auth: String?,
        @Body user: User
    ): Response<Map<String, String>>

    @GET("users/{userId}.json")
    suspend fun getUser(
        @Path("userId") userId: String,
        @Query("auth") auth: String?
    ): Response<Map<String, User>>

    companion object {

        fun create(): ApiClient {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.FIREBASE_REALTIME_DB_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(ApiClient::class.java)
        }
    }
}