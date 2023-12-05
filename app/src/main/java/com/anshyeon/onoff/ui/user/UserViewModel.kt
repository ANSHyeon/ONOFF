package com.anshyeon.onoff.ui.user

import androidx.lifecycle.ViewModel
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val nickname = ""
    private val email = ""
    private val profileImage = ""
    val user = User(nickName = nickname, email = email, profileUrl = profileImage)

    private val _currentUserInfo: MutableStateFlow<User> = MutableStateFlow(user)
    val currentUserInfo: StateFlow<User> = _currentUserInfo

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
}