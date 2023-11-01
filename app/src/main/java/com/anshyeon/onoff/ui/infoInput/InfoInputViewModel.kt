package com.anshyeon.onoff.ui.infoInput

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.data.repository.AuthRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.anshyeon.onoff.util.isValidNickname
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InfoInputViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    private val nickName = MutableLiveData<String>()
    private val imageUri = MutableLiveData<Uri>()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isValidInfo = MutableLiveData<Boolean>(false)
    val isValidInfo: LiveData<Boolean> = _isValidInfo

    private val _isSave = MutableLiveData<Boolean>(false)
    val isSave: LiveData<Boolean> = _isSave

    fun updateNickName(text: String) {
        nickName.value = text
        _isValidInfo.value = isValidNickname(text)
    }

    fun updateProfileImage(uri: Uri?) {
        uri?.let {
            imageUri.value = it
        }
    }

    fun saveUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            nickName.value?.let {
                val result = repository.createUser(it, imageUri.value)
                result.onSuccess {
                    repository.saveIdToken()
                    _isSave.value = true
                }.onError { code, message ->
                    Log.d("HomeViewModel", "${code}---${message}")
                }.onException {
                    Log.d("HomeViewModel", "$it")
                }
            }

        }
    }
}