package com.anshyeon.onoff.ui.edit

import androidx.lifecycle.ViewModel
import com.anshyeon.onoff.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val nickName = MutableStateFlow("")
    val profileUrl: MutableStateFlow<String?> = MutableStateFlow(null)

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setUserInfo(userNickName: String, userProfileUrl: String?) {
        nickName.value = userNickName
        profileUrl.value = userProfileUrl
    }
}