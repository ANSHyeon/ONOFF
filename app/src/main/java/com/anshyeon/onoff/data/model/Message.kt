package com.anshyeon.onoff.data.model

import com.anshyeon.onoff.util.DateFormatText
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Message(
    val messageId: String,
    val sender: User,
    val body: String,
    val sendAt: String = DateFormatText.getCurrentTime(),
)
