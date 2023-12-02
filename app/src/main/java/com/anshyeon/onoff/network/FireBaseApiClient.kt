package com.anshyeon.onoff.network

import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.data.model.Post
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.network.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FireBaseApiClient {
    @POST("users.json")
    suspend fun createUser(
        @Query("auth") auth: String?,
        @Body user: User
    ): ApiResponse<Map<String, String>>

    @GET("users.json?orderBy=\"userId\"")
    suspend fun getUser(
        @Query("auth") auth: String?,
        @Query("equalTo") userId: String
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

    @POST("message.json")
    suspend fun createMessage(
        @Query("auth") auth: String?,
        @Body message: Message,
    ): ApiResponse<Map<String, String>>

    @GET("message.json?orderBy=\"chatRoomId\"")
    suspend fun getMessage(
        @Query("auth") auth: String?,
        @Query("equalTo") chatRoomId: String,
    ): ApiResponse<Map<String, Message>>

    @POST("post.json")
    suspend fun createPost(
        @Query("auth") auth: String?,
        @Body post: Post,
    ): ApiResponse<Map<String, String>>

    @GET("post.json?orderBy=\"location\"")
    suspend fun getPostList(
        @Query("auth") auth: String?,
        @Query("equalTo") location: String,
    ): ApiResponse<Map<String, Post>>
}