package com.anshyeon.onoff.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Entity(tableName = "chat_room_info")
@JsonClass(generateAdapter = true)
@Parcelize
data class ChatRoom(
    @PrimaryKey val chatRoomId: String,
    @ColumnInfo(name = "place_name") val placeName: String,
    val address: String,
    val latitude: String,
    val longitude: String,
    val lastMessageDate: String,
    val memberList: List<String>,
) : Parcelable