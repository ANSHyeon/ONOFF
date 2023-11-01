package com.anshyeon.onoff.ui.infoInput

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
class InfoInputViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private var nickName: String? = null
    private var imageUri: Uri? = null

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isValidInfo: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isValidInfo: StateFlow<Boolean> = _isValidInfo

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSave: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isSave: StateFlow<Boolean> = _isSave

    fun updateNickName(text: String) {
        nickName = text
        _isValidInfo.value = isValidNickname(text)
    }

    fun updateProfileImage(uri: Uri?) {
        imageUri = uri
    }

    fun saveUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            nickName?.let { nickName ->
                val result = repository.createUser(nickName, imageUri)
                result.onSuccess {
                    repository.saveIdToken()
                    _isSave.value = true
                }.onError { code, message ->
                    _isLoading.value = false
                    _snackBarText.emit(R.string.error_message_retry)
                }.onException {
                    _isLoading.value = false
                    _snackBarText.emit(R.string.error_message_retry)
                }
            }
        }
    }
}