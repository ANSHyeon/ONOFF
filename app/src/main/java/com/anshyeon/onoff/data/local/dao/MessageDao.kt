package com.anshyeon.onoff.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.anshyeon.onoff.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM chat_message WHERE chat_room_id = :chatRoomId ORDER BY send_at DESC")
    fun getMessageListByChatRoomId(chatRoomId: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Query("DELETE FROM chat_message")
    suspend fun deleteAll()
}