package com.anshyeon.onoff.ui.signin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.data.repository.AuthRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val _hasUserInfo = MutableSharedFlow<Boolean>()
    val hasUserInfo = _hasUserInfo.asSharedFlow()

    private val _isSaveUserInfo = MutableSharedFlow<Boolean>()
    val isSaveUserInfo = _isSaveUserInfo.asSharedFlow()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun getUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getUser()
            result.onSuccess {
                _hasUserInfo.emit(it.isNotEmpty())
            }.onError { code, message ->
                Log.d("SignInViewModel", "${code}---${message}")
            }.onException {
                Log.d("SignInViewModel", "$it")
            }
        }
    }

    fun saveUserInfo() {
        viewModelScope.launch {
            repository.saveIdToken()
            _isSaveUserInfo.emit(true)
        }
    }
}