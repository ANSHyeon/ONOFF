package com.anshyeon.onoff.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.data.repository.AuthRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val nickname = getLocalUserNickName()
    private val email = getLocalUserEmail()
    private val profileImage = getLocalUserProfileImage()
    val user = User(nickName = nickname, email = email, profileUrl = profileImage)
    var userKey: String = ""
        private set

    var isConnected = false
        private set

    private val _currentUserInfo: MutableStateFlow<User> = MutableStateFlow(user)
    val currentUserInfo: StateFlow<User> = _currentUserInfo

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLogOut: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLogOut: StateFlow<Boolean> = _isLogOut

    fun getUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.getUser()
            result.onSuccess {
                if (it.values.isNotEmpty()) {
                    userKey = it.keys.first()
                    val user = it.values.first()
                    _currentUserInfo.value = user.copy(
                        profileUrl = user.profileUri?.let {
                            getDownloadUrl(user.profileUri)
                        }
                    )
                }
            }.onError { code, message ->
                _isLoading.value = false
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _isLoading.value = false
                _snackBarText.emit(R.string.error_message_retry)
            }
        }
    }

    fun setConnectedState(state: Boolean) {
        isConnected = state
    }

    fun saveUserInLocal(user: User) {
        authRepository.saveUserInLocal(user)
    }

    private fun getLocalUserNickName(): String {
        return authRepository.getLocalUserNickName()!!
    }

    private fun getLocalUserEmail(): String {
        return authRepository.getLocalUserEmail()!!
    }

    private fun getLocalUserProfileImage(): String? {
        return authRepository.getLocalUserProfileImage()
    }

    fun logOut() {
        viewModelScope.launch {
            authRepository.logOut()
            FirebaseAuth.getInstance().signOut()
            _isLogOut.value = true
        }
    }

    private suspend fun getDownloadUrl(location: String): String {
        return FirebaseStorage.getInstance()
            .getReference(location)
            .downloadUrl
            .await()
            .toString()
    }
}