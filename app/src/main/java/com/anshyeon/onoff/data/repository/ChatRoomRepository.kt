package com.anshyeon.onoff.data.repository

import com.anshyeon.onoff.data.local.AppDatabase
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.network.FireBaseApiClient
import com.anshyeon.onoff.network.model.ApiResponse
import com.anshyeon.onoff.network.model.ApiResultException
import javax.inject.Inject

class ChatRoomRepository @Inject constructor(
    private val fireBaseApiClient: FireBaseApiClient,
    private val database: AppDatabase,
    private val userDataSource: UserDataSource,
) {

    suspend fun getChatRoom(
    ): ApiResponse<Map<String, ChatRoom>> {
        return try {
            fireBaseApiClient.getChatRoom(
                userDataSource.getIdToken()
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun getChatRoomOfPlace(
        placeName: String
    ): ApiResponse<Map<String, ChatRoom>> {
        return try {
            fireBaseApiClient.getChatRoomOfPlace(
                userDataSource.getIdToken(),
                "\"$placeName\""
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun createChatRoom(
        chatRoom: ChatRoom
    ): ApiResponse<Map<String, String>> {
        return try {
            fireBaseApiClient.createChatRoom(
                userDataSource.getIdToken(),
                chatRoom
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun getMessage(chatRoomId: String): ApiResponse<Map<String, Message>> {
        return try {
            fireBaseApiClient.getMessage(
                userDataSource.getIdToken(),
                "\"$chatRoomId\"",
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun insertChatRoom(chatRoom: ChatRoom) {
        database.chatRoomDao().insert(chatRoom)
    }
}