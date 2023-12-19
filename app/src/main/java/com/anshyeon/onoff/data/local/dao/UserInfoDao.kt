package com.anshyeon.onoff.data.local.dao

import androidx.room.*
import com.anshyeon.onoff.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user WHERE userId = :userId")
    fun getUserListByUserId(userId: String): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("UPDATE user SET profile_uri = :profileUri, profile_url = :profileUrl WHERE userId = :userId")
    suspend fun update(userId: String, profileUri: String, profileUrl: String)

    @Query("DELETE FROM user")
    suspend fun deleteAll()
}