package com.anshyeon.onoff.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Entity(tableName = "user")
@JsonClass(generateAdapter = true)
@Parcelize
data class User(
    @PrimaryKey val userId: String = "",
    @ColumnInfo(name = "nick_name") val nickName: String = "",
    val email: String = "",
    @ColumnInfo(name = "profile_uri") val profileUri: String? = null,
    @ColumnInfo(name = "profile_url") val profileUrl: String? = null
) : Parcelable