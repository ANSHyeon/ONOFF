package com.anshyeon.onoff.data.repository

import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.network.ApiClient
import com.anshyeon.onoff.network.model.ApiResponse
import javax.inject.Inject

class ChatRoomRepository @Inject constructor(
    private val apiClient: ApiClient
) : BaseRepository() {

    suspend fun getChatRoom(
    ): ApiResponse<Map<String, ChatRoom>> {
        return apiClient.getChatRoom(
            getIdToken()
        )
    }
}