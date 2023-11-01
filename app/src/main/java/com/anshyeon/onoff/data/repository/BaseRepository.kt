package com.anshyeon.onoff.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

open class BaseRepository {
    fun getCurrentUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    suspend fun getIdToken(): String {
        return getCurrentUser()?.getIdToken(true)?.await()?.token ?: ""
    }
}