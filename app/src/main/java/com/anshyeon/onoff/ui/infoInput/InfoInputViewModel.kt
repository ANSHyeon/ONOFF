package com.anshyeon.onoff.ui.infoInput

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anshyeon.onoff.data.source.repository.AuthRepository
import com.anshyeon.onoff.util.isValidNickname
import kotlinx.coroutines.launch

class InfoInputViewModel(private val repository: AuthRepository) : ViewModel() {

    private val nickName = MutableLiveData<String>()
    private val imageUri = MutableLiveData<Uri>()

    private val _isValidInfo = MutableLiveData<Boolean>(false)
    val isValidInfo: LiveData<Boolean> = _isValidInfo

    private val _isSave = MutableLiveData<Boolean>(false)
    val isSave: LiveData<Boolean> = _isSave

    private val _isLogIn = MutableLiveData<Boolean>(false)
    val isLogIn: LiveData<Boolean> = _isLogIn

    fun updateNickName(text: String) {
        nickName.value = text
        _isValidInfo.value = isValidNickname(text)
    }

    fun updateProfileImage(uri: Uri?) {
        uri?.let {
            imageUri.value = it
        }
    }

    fun isLogIn() {
        viewModelScope.launch {
            if (repository.getUser().isSuccessful) {
                _isLogIn.value = true
            }
        }
    }

    fun saveUserInfo() {
        viewModelScope.launch {
            nickName.value?.let {
                repository.createUser(it, imageUri.value)
            }
            _isSave.value = true
        }
    }

    companion object {

        fun provideFactory(repository: AuthRepository) = viewModelFactory {
            initializer {
                InfoInputViewModel(repository)
            }
        }
    }
}