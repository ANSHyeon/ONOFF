package com.anshyeon.onoff.data.repository

import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.network.ApiClient
import com.anshyeon.onoff.network.model.ApiResponse
import com.anshyeon.onoff.network.model.ApiResultException
import javax.inject.Inject

class ChatRoomRepository @Inject constructor(
    private val apiClient: ApiClient
) : BaseRepository() {

    suspend fun getChatRoom(
    ): ApiResponse<Map<String, ChatRoom>> {
        return try{
            apiClient.getChatRoom(
                getIdToken()
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }
}