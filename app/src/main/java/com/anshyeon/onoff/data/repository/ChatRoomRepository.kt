package com.anshyeon.onoff.data.repository

import com.anshyeon.onoff.data.local.dao.ChatRoomInfoDao
import com.anshyeon.onoff.data.local.dao.MessageDao
import com.anshyeon.onoff.data.local.dao.UserDao
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.network.FireBaseApiClient
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.anshyeon.onoff.network.model.ApiResponse
import com.anshyeon.onoff.network.model.ApiResultException
import com.anshyeon.onoff.network.model.ApiResultSuccess
import com.anshyeon.onoff.util.DateFormatText
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRoomRepository @Inject constructor(
    private val fireBaseApiClient: FireBaseApiClient,
    private val chatRoomInfoDao: ChatRoomInfoDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val userDataSource: UserDataSource,
    private val authRepository: AuthRepository,
) {

    fun getChatRoom(
        onComplete: () -> Unit,
        onError: (message: String?) -> Unit
    ): Flow<Map<String, ChatRoom>> = flow {
        try {
            val response = fireBaseApiClient.getChatRoom(
                userDataSource.getIdToken()
            )
            response.onSuccess { data ->
                emit(data)
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
            insertChatRoom(chatRoom)
            ApiResultSuccess(mapOf())
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun addMemberToChatRoom(
        chatRoom: ChatRoom,
        chatRoomKey: String
    ): ApiResponse<Map<String, String>> {
        return try {
            val userId = userDataSource.getUid()
            val memberList = chatRoom.memberList.toMutableList()
            if (userId !in chatRoom.memberList) {
                memberList.add(userId)
                fireBaseApiClient.updateMember(
                    chatRoomKey,
                    userDataSource.getIdToken(),
                    mapOf("memberList" to memberList)
                )
            }
            ApiResultSuccess(mapOf())
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
        chatRoomId: String,
        sendMessage: String,
    ): ApiResponse<Map<String, String>> {
        return try {
            val messageId = chatRoomId + (userDataSource.getUid()) + DateFormatText.getCurrentDate()
            var user = User()
            val result = authRepository.getUser()
            result.onSuccess {
                user = it.values.first().profileUri?.let { uri ->
                    it.values.first().copy(
                        profileUrl = downloadImage(uri)
                    )
                } ?: it.values.first()
            }.onException {
                throw it
            }.onError { _, _ ->
                throw Exception()
            }
            val message = Message(
                messageId,
                chatRoomId,
                user.nickName,
                user.email,
                user.profileUri,
                user.profileUrl,
                sendMessage,
                System.currentTimeMillis()
            )
            fireBaseApiClient.createMessage(
                userDataSource.getIdToken(),
                message
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun getUserList(userList: List<String>): ApiResponse<Map<String, String>> {
        return try {
            userList.forEach { userId ->
                authRepository.getUserByUserId(userId)
                    .onSuccess { data ->
                        insertUser(data.values.first())
                    }.onException {
                        throw Exception()
                    }.onException {
                        throw Exception()
                    }
            }
            ApiResultSuccess(mapOf())
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

    private suspend fun insertUser(user: User) {
        userDao.insert(user)
    }

    private suspend fun downloadImage(location: String): String {
        val storageRef = FirebaseStorage.getInstance().reference
        return storageRef.child(location).downloadUrl.await().toString()
    }
}