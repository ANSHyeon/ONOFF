package com.anshyeon.onoff.ui.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.repository.AuthRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.anshyeon.onoff.util.isValidNickname
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val nickName = MutableStateFlow("")
    val profileUrl: MutableStateFlow<String?> = MutableStateFlow(null)
    private var imageUri: Uri? = null

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isUpdated: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isUpdated: StateFlow<Boolean> = _isUpdated

    fun updateUserInfo(location: String, userKey: String) {
        viewModelScope.launch {
            if (isValidNickname(nickName.value)) {
                _isLoading.value = true
                val result = authRepository.updateUser(nickName.value, imageUri, location, userKey)
                result.onSuccess {
                    _isUpdated.value = true
                    _isLoading.value = false
                }.onError { code, message ->
                    _isLoading.value = false
                    _isUpdated.value = false
                    _snackBarText.emit(R.string.error_message_retry)
                }.onException {
                    _isLoading.value = false
                    _isUpdated.value = false
                    _snackBarText.emit(R.string.error_message_retry)
                }

            } else {
                _snackBarText.emit(R.string.guide_message_invalid_nick_name)
            }
        }
    }

    fun setUserInfo(userNickName: String, userProfileUrl: String?) {
        nickName.value = userNickName
        profileUrl.value = userProfileUrl
    }

    fun updateProfileUri(uri: Uri?) {
        imageUri = uri
    }
}