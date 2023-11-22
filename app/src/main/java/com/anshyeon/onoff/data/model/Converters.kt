package com.anshyeon.onoff.data.model

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi

class Converters {

    private val moshi = Moshi.Builder().build()

    @TypeConverter
    fun messageToJson(message: Message): String {
        val jsonAdapter = moshi.adapter(Message::class.java)
        return jsonAdapter.toJson(message)
    }

    @TypeConverter
    fun messageFromJson(json: String): Message? {
        val jsonAdapter = moshi.adapter(Message::class.java)
        return jsonAdapter.fromJson(json)
    }

    @TypeConverter
    fun userToJson(user: User): String {
        val jsonAdapter = moshi.adapter(User::class.java)
        return jsonAdapter.toJson(user)
    }

    @TypeConverter
    fun userFromJson(json: String): User? {
        val jsonAdapter = moshi.adapter(User::class.java)
        return jsonAdapter.fromJson(json)
    }
}