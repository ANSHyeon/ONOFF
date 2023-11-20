package com.anshyeon.onoff.data.repository

import android.net.Uri
import com.anshyeon.onoff.data.PreferenceManager
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.network.FireBaseApiClient
import com.anshyeon.onoff.network.model.ApiResponse
import com.anshyeon.onoff.network.model.ApiResultException
import com.anshyeon.onoff.util.Constants
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val fireBaseApiClient: FireBaseApiClient,
    private val preferenceManager: PreferenceManager
) : BaseRepository() {

    private fun getEmail(): String {
        return getCurrentUser()?.email ?: ""
    }

    private fun getUid(): String {
        return getCurrentUser()?.uid ?: ""
    }

    fun getLocalIdToken(): String {
        return preferenceManager.getString(Constants.KEY_GOOGLE_ID_TOKEN, "")
    }

    suspend fun saveIdToken() {
        preferenceManager.setGoogleIdToken(
            Constants.KEY_GOOGLE_ID_TOKEN,
            getIdToken()
        )
    }

    suspend fun createUser(
        nickname: String,
        uri: Uri?
    ): ApiResponse<Map<String, String>> {
        return try {
            val uriLocation = uri?.let { uploadImage(it) }
            val user = User(
                nickName = nickname,
                email = getEmail(),
                profileUri = uriLocation,
            )
            fireBaseApiClient.createUser(
                getUid(),
                getIdToken(),
                user
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun getUser(
    ): ApiResponse<Map<String, User>> {
        return try {
            fireBaseApiClient.getUser(getUid(), getIdToken())
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    private suspend fun uploadImage(uri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val location = "images/${uri.lastPathSegment}_${System.currentTimeMillis()}"
        val imageRef = storageRef.child(location)
        imageRef.putFile(uri).await()
        return location
    }
}