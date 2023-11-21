package com.anshyeon.onoff.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatRoom(
    val chatRoomId: String,
    val latitude: String,
    val longitude: String,
)