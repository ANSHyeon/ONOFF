package com.anshyeon.onoff.ui.infoInput

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.anshyeon.onoff.util.isValidNickname

class InfoInputViewModel : ViewModel() {

    private val _isValidInfo = MutableLiveData<Boolean>(false)
    val isValidInfo: LiveData<Boolean> = _isValidInfo

    fun isValidInfo(nickname: String) {
        _isValidInfo.value = isValidNickname(nickname)
    }

    companion object {

        fun provideFactory() = viewModelFactory {
            initializer {
                InfoInputViewModel()
            }
        }
    }
}