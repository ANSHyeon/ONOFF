package com.anshyeon.onoff.data.local.dao

import androidx.room.*
import com.anshyeon.onoff.data.model.ChatRoom

@Dao
interface ChatRoomInfoDao {

    @Query("SELECT * FROM chat_room_info")
    fun getAllChatRoomList(): List<ChatRoom>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatRoom: ChatRoom)
}