package com.anshyeon.onoff.network

import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.network.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FireBaseApiClient {
    @POST("users/{uid}.json")
    suspend fun createUser(
        @Path("uid") uid: String,
        @Query("auth") auth: String?,
        @Body user: User
    ): ApiResponse<Map<String, String>>

    @GET("users/{userId}.json")
    suspend fun getUser(
        @Path("userId") userId: String,
        @Query("auth") auth: String?
    ): ApiResponse<Map<String, User>>

    @GET("chatRoom.json")
    suspend fun getChatRoom(
        @Query("auth") auth: String?
    ): ApiResponse<Map<String, ChatRoom>>

    @GET("chatRoom.json")
    suspend fun getMessage(
        @Path("buildingName") buildingName: String,
        @Query("auth") auth: String?
    ): ApiResponse<Map<String, Message>>
}