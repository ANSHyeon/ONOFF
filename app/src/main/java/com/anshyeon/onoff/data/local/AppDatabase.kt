package com.anshyeon.onoff.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anshyeon.onoff.data.local.dao.ChatRoomInfoDao
import com.anshyeon.onoff.data.local.dao.MessageDao
import com.anshyeon.onoff.data.local.dao.UserDao
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.Converters
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.data.model.User

@Database(entities = [Message::class, ChatRoom::class, User::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun chatRoomDao(): ChatRoomInfoDao
    abstract fun userDao(): UserDao
}