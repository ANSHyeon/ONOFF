package com.anshyeon.onoff.data.local.dao

import androidx.room.*
import com.anshyeon.onoff.data.model.ChatRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomInfoDao {

    @Query("SELECT * FROM chat_room_info")
    fun getAllChatRoomList(): Flow<List<ChatRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatRoom: ChatRoom)
}