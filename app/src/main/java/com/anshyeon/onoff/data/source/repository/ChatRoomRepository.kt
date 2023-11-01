package com.anshyeon.onoff.data.source.repository

import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.source.ApiClient
import retrofit2.Response
import javax.inject.Inject

class ChatRoomRepository @Inject constructor(
    private val apiClient: ApiClient
) : BaseRepository() {

    suspend fun getChatRoom(
    ): Response<Map<String, ChatRoom>> {
        return apiClient.getChatRoom(
            getIdToken()
        )
    }
}