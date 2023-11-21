package com.anshyeon.onoff.ui.chatRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.data.repository.AuthRepository
import com.anshyeon.onoff.data.repository.ChatRoomRepository
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
class ChatRoomViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRoomRepository: ChatRoomRepository
) : ViewModel() {

    val sendMessage = MutableStateFlow("")
    var currentUser: User? = null
        private set

    private val _dummyMessageList: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    val dummyMessageList: StateFlow<List<Message>> = _dummyMessageList

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getUser() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.getUser()
            result.onSuccess {
                currentUser = it.values.first()
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
            }
            _isLoading.value = false
        }
    }

    fun getMessage(buildingName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = chatRoomRepository.getMessage(buildingName)
            result.onSuccess {
                _dummyMessageList.value = it.values.toList()
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
            }
            _isLoading.value = false
        }
    }
}
