package com.anshyeon.onoff.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.repository.AuthRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _hasUserInfo: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val hasUserInfo: StateFlow<Boolean?> = _hasUserInfo

    private val _isSaveUserInfo: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isSaveUserInfo: StateFlow<Boolean> = _isSaveUserInfo

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getUser()
            result.onSuccess {
                _hasUserInfo.value = it.values.isNotEmpty()
            }.onError { code, message ->
                _hasUserInfo.value = false
                _isLoading.value = false
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _hasUserInfo.value = false
                _isLoading.value = false
                _snackBarText.emit(R.string.error_message_retry)
            }
        }
    }

    fun updateSnackBarMessage(message: Int) {
        viewModelScope.launch {
            _snackBarText.emit(message)
        }
    }

    fun saveUserInfo() {
        viewModelScope.launch {
            repository.saveIdToken()
            _isSaveUserInfo.value = true
        }
    }
}