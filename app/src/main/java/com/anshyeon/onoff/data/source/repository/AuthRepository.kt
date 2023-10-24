package com.anshyeon.onoff.data.source.repository

import android.net.Uri
import com.anshyeon.onoff.OnOffApplication
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.data.source.ApiClient
import com.anshyeon.onoff.util.Constants
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import retrofit2.Response

class AuthRepository(private val apiClient: ApiClient) {

    private fun getCurrentUser(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    private fun getEmail(): String {
        return getCurrentUser()?.email ?: ""
    }

    private fun getUid(): String {
        return getCurrentUser()?.uid ?: ""
    }

    private suspend fun getIdToken(): String {
        return getCurrentUser()?.getIdToken(true)?.await()?.token ?: ""
    }

    suspend fun createUser(
        nickname: String,
        uri: Uri?
    ): Response<Map<String, String>> {
        val uriLocation = uri?.let { uploadImage(it) }
        val user = User(
            nickName = nickname,
            email = getEmail(),
            profileUri = uriLocation,
        )
        OnOffApplication.preferencesManager.setGoogleIdToken(
            Constants.KEY_GOOGLE_ID_TOKEN,
            getIdToken()
        )
        return apiClient.createUser(
            getUid(),
            getIdToken(),
            user
        )
    }

    private suspend fun uploadImage(uri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val location = "images/${uri.lastPathSegment}_${System.currentTimeMillis()}"
        val imageRef = storageRef.child(location)
        imageRef.putFile(uri).await()
        return location
    }

    suspend fun getUser(
    ): Response<Map<String, User>> {
        return apiClient.getUser(getUid(), getIdToken())
    }
}