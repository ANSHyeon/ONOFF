package com.anshyeon.onoff.di

import android.content.Context
import androidx.room.Room
import com.anshyeon.onoff.data.local.AppDatabase
import com.anshyeon.onoff.data.local.dao.ChatRoomInfoDao
import com.anshyeon.onoff.data.local.dao.MessageDao
import com.anshyeon.onoff.data.local.dao.UserDao
import com.anshyeon.onoff.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            Constants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providerChatPreviewDao(appDatabase: AppDatabase): ChatRoomInfoDao {
        return appDatabase.chatRoomDao()
    }

    @Provides
    fun providerMessageDao(appDatabase: AppDatabase): MessageDao {
        return appDatabase.messageDao()
    }

    @Provides
    fun providerUserInfoDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }
}