package com.anshyeon.onoff.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "chat_room_info")
@JsonClass(generateAdapter = true)
data class ChatRoom(
    @PrimaryKey val chatRoomId: String,
    @ColumnInfo(name = "place_name") val placeName: String,
    val latitude: String,
    val longitude: String,
)