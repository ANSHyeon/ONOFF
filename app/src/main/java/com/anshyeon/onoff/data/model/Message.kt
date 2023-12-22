package com.anshyeon.onoff.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "chat_message")
@JsonClass(generateAdapter = true)
data class Message(
    @PrimaryKey val messageId: String = "",
    @ColumnInfo(name = "chat_room_id") val chatRoomId: String = "",
    @ColumnInfo(name = "user_id") val userId: String = "",
    @ColumnInfo(name = "nick_name") val nickName: String? = null,
    val email: String? = null,
    @ColumnInfo(name = "profile_uri") val profileUri: String? = null,
    @ColumnInfo(name = "profile_url") val profileUrl: String? = null,
    val body: String = "",
    @ColumnInfo(name = "send_at") val sendAt: Long = 0,
)
