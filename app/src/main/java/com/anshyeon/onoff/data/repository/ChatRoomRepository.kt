package com.anshyeon.onoff.data.repository

import com.anshyeon.onoff.data.local.dao.ChatRoomInfoDao
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.network.FireBaseApiClient
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.anshyeon.onoff.network.model.ApiResponse
import com.anshyeon.onoff.network.model.ApiResultException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class ChatRoomRepository @Inject constructor(
    private val fireBaseApiClient: FireBaseApiClient,
    private val chatRoomInfoDao: ChatRoomInfoDao,
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

    fun getMessage(
        chatRoomId: String,
        onComplete: () -> Unit,
        onError: () -> Unit
    ): Flow<List<Message>> = flow {
        val response = fireBaseApiClient.getMessage(
            userDataSource.getIdToken(),
            "\"$chatRoomId\"",
        )
        response.onSuccess { data ->
            emit(
                data.filter { entry ->
                    entry.value.chatRoomId == chatRoomId
                }.map { it.value }
            )
        }.onError { code, message ->
            onError()
        }.onException {
            onError()
        }
    }.onCompletion {
        onComplete()
    }.flowOn(Dispatchers.Default)

    suspend fun insertChatRoom(chatRoom: ChatRoom) {
        chatRoomInfoDao.insert(chatRoom)
    }

    fun getChatRoomListByRoom(): Flow<List<ChatRoom>> {
        return chatRoomInfoDao.getAllChatRoomList()
    }
}