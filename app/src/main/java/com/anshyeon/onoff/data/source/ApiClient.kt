package com.anshyeon.onoff.data.source

import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.User
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

    @GET("chatRoom.json")
    suspend fun getChatRoom(
        @Query("auth") auth: String?
    ): Response<Map<String, ChatRoom>>
}