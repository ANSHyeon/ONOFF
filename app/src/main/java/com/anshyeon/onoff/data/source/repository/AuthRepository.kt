package com.anshyeon.onoff.data.source.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthRepository {

    fun getCurrentUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }
}