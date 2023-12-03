package com.anshyeon.onoff.data.repository

import com.anshyeon.onoff.data.local.dao.ChatRoomInfoDao
import com.anshyeon.onoff.data.local.dao.MessageDao
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.network.FireBaseApiClient
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.anshyeon.onoff.network.model.ApiResponse
import com.anshyeon.onoff.network.model.ApiResultException
import com.anshyeon.onoff.util.DateFormatText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

class ChatRoomRepository @Inject constructor(
    private val fireBaseApiClient: FireBaseApiClient,
    private val chatRoomInfoDao: ChatRoomInfoDao,
    private val messageDao: MessageDao,
    private val userDataSource: UserDataSource,
) {

    fun getChatRoom(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<List<ChatRoom>> = flow {
        try {
            val response = fireBaseApiClient.getChatRoom(
                userDataSource.getIdToken()
            )
            response.onSuccess { data ->
                emit(data.map { entry ->
                    entry.value
                })
            }.onError { code, message ->
                onError("code: $code, message: $message")
            }.onException {
                onError(it.message)
            }
        } catch (e: Exception) {
            onError(e.message)
        }
    }.onCompletion {
        onComplete()
    }.flowOn(Dispatchers.Default)

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

    suspend fun updateChatRoom(
        chatRoomKey: String
    ): ApiResponse<Map<String, String>> {
        return try {
            fireBaseApiClient.updateChatRoom(
                chatRoomKey,
                userDataSource.getIdToken(),
                mapOf("lastMessageDate" to DateFormatText.getCurrentTime())
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun createMessage(
        message: Message
    ): ApiResponse<Map<String, String>> {
        return try {
            fireBaseApiClient.createMessage(
                userDataSource.getIdToken(),
                message
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
        try {
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
        } catch (e: Exception) {
            onError()
        }
    }.onCompletion {
        onComplete()
    }.flowOn(Dispatchers.Default)

    suspend fun insertChatRoom(chatRoom: ChatRoom) {
        chatRoomInfoDao.insert(chatRoom)
    }

    suspend fun updateChatRoomInRoom(chatRoomId: String) {
        chatRoomInfoDao.update(chatRoomId, DateFormatText.getCurrentTime())
    }

    fun getChatRoomListByRoom(): Flow<List<ChatRoom>> {
        return chatRoomInfoDao.getAllChatRoomList()
    }

    suspend fun insertMessage(message: Message) {
        messageDao.insert(message)
    }

    fun getMessageListByRoom(chatRoomId: String): Flow<List<Message>> {
        return messageDao.getMessageListByChatRoomId(chatRoomId)
    }
}