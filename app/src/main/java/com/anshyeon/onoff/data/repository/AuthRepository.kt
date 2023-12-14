package com.anshyeon.onoff.data.repository

import android.net.Uri
import com.anshyeon.onoff.data.PreferenceManager
import com.anshyeon.onoff.data.local.dao.ChatRoomInfoDao
import com.anshyeon.onoff.data.local.dao.MessageDao
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.network.FireBaseApiClient
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.anshyeon.onoff.network.model.ApiResponse
import com.anshyeon.onoff.network.model.ApiResultException
import com.anshyeon.onoff.util.Constants
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val fireBaseApiClient: FireBaseApiClient,
    private val preferenceManager: PreferenceManager,
    private val userDataSource: UserDataSource,
    private val chatRoomInfoDao: ChatRoomInfoDao,
    private val messageDao: MessageDao,
) {

    fun getLocalIdToken(): String? {
        return preferenceManager.getString(Constants.KEY_GOOGLE_ID_TOKEN, "")
    }

    suspend fun saveIdToken() {
        preferenceManager.setGoogleIdToken(
            Constants.KEY_GOOGLE_ID_TOKEN,
            userDataSource.getIdToken()
        )
    }

    fun getLocalUserEmail(): String? {
        return preferenceManager.getString(Constants.KEY_USER_EMAIL, "")
    }

    fun getLocalUserNickName(): String? {
        return preferenceManager.getString(Constants.KEY_USER_NICKNAME, "")
    }

    fun getLocalUserProfileImage(): String? {
        return preferenceManager.getString(Constants.KEY_USER_PROFILE_URI, null)
    }

    fun saveUserEmail() {
        preferenceManager.setUserEmail(
            Constants.KEY_USER_EMAIL,
            userDataSource.getEmail()
        )
    }

    fun saveUserNickName(nickname: String) {
        preferenceManager.setUserNickName(
            Constants.KEY_USER_NICKNAME,
            nickname
        )
    }

    private fun saveUserProfileUri(profileUrl: String?) {
        preferenceManager.setUserImage(
            Constants.KEY_USER_PROFILE_URI,
            profileUrl
        )
    }

    fun saveUserInLocal(user: User) {
        saveUserNickName(user.nickName)
        saveUserEmail()
        saveUserProfileUri(user.profileUrl)
    }

    suspend fun createUser(
        nickname: String,
        uri: Uri?
    ): ApiResponse<Map<String, String>> {
        return try {
            val uriLocation = uri?.let { uploadImage(it) }
            val user = User(
                userId = userDataSource.getUid(),
                nickName = nickname,
                email = userDataSource.getEmail(),
                profileUri = uriLocation,
            )
            fireBaseApiClient.createUser(
                userDataSource.getIdToken(),
                user
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun getUser(): ApiResponse<Map<String, User>> {
        return try {
            fireBaseApiClient.getUser(
                userDataSource.getIdToken(),
                "\"${userDataSource.getUid()}\""
            )
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun updateUser(
        nickname: String,
        uri: Uri?,
        location: String,
        userKey: String
    ): ApiResponse<Map<String, String>> {
        return try {
            val uriLocation = uri?.let { uploadImage(it) } ?: location.ifBlank { null }
            val updates = mapOf(
                "nickName" to nickname,
                "profileUri" to uriLocation
            )
            fireBaseApiClient.updateUser(
                userKey,
                userDataSource.getIdToken(),
                updates
            ).onSuccess {
                saveUserInLocal(User(nickName = nickname, profileUrl = uriLocation))
            }.onError { code, message ->
                throw Exception()
            }.onException {
                throw Exception()
            }
        } catch (e: Exception) {
            ApiResultException(e)
        }
    }

    suspend fun logOut() {
        preferenceManager.setGoogleIdToken(Constants.KEY_GOOGLE_ID_TOKEN, "")
        preferenceManager.setUserNickName(Constants.KEY_USER_NICKNAME, "")
        preferenceManager.setUserEmail(Constants.KEY_USER_EMAIL, "")
        preferenceManager.setUserImage(Constants.KEY_USER_PROFILE_URI, "")
        chatRoomInfoDao.deleteAll()
        messageDao.deleteAll()
    }

    private suspend fun uploadImage(uri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val location = "images/${uri.lastPathSegment}_${System.currentTimeMillis()}"
        val imageRef = storageRef.child(location)
        imageRef.putFile(uri).await()
        return location
    }
}