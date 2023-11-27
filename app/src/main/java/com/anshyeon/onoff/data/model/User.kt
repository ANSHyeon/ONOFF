package com.anshyeon.onoff.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val userId: String,
    val nickName: String,
    val email: String,
    val profileUri: String? = null,
    val profileUrl: String? = null
) : Parcelable