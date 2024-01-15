package com.anshyeon.onoff.data.dataSource

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserDataSource @Inject constructor() {
    private fun getCurrentUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    suspend fun getIdToken(): String {
        return getCurrentUser()?.getIdToken(true)?.await()?.token ?: ""
    }

    fun getEmail(): String {
        return getCurrentUser()?.email ?: ""
    }

    fun getUid(): String {
        return getCurrentUser()?.uid ?: ""
    }
}