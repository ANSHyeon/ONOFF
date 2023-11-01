package com.anshyeon.onoff.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.ChatRoom
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
class HomeViewModel @Inject constructor(private val repository: ChatRoomRepository) : ViewModel() {

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _chatRoomList = MutableSharedFlow<List<ChatRoom>>()
    val chatRoomList = _chatRoomList.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    fun getChatRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getChatRoom()
            result.onSuccess {
                _chatRoomList.emit(it.values.toList())
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                Log.d("HomeViewModel", "$it")
            }
            _isLoading.value = false
        }
    }
}
