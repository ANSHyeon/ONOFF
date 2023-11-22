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

    @POST("chatRoom.json")
    suspend fun createChatRoom(
        @Query("auth") auth: String?,
        @Body chatRoom: ChatRoom
    ): ApiResponse<Map<String, String>>

    @GET("chatRoom.json")
    suspend fun getChatRoom(
        @Query("auth") auth: String?
    ): ApiResponse<Map<String, ChatRoom>>

    @GET("chatRoom.json?orderBy=\"placeName\"")
    suspend fun getChatRoomOfPlace(
        @Query("auth") auth: String?,
        @Query("equalTo") placeName: String,
    ): ApiResponse<Map<String, ChatRoom>>

    @GET("message.json?orderBy=\"chatRoomId\"")
    suspend fun getMessage(
        @Query("auth") auth: String?,
        @Query("equalTo") chatRoomId: String,
    ): ApiResponse<Map<String, Message>>
}