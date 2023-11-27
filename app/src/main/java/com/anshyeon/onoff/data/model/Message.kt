package com.anshyeon.onoff.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anshyeon.onoff.util.DateFormatText
import com.squareup.moshi.JsonClass

@Entity(tableName = "chat_message")
@JsonClass(generateAdapter = true)
data class Message(
    @PrimaryKey val messageId: String,
    @ColumnInfo(name = "chat_room_id") val chatRoomId: String,
    val sender: User,
    val body: String,
    @ColumnInfo(name = "send_at") val sendAt: String = DateFormatText.getCurrentTime(),
)
