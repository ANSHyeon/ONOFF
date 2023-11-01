package com.anshyeon.onoff.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.repository.ChatRoomRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: ChatRoomRepository) : ViewModel() {

    private val _chatRoomList = MutableSharedFlow<List<ChatRoom>>()
    val chatRoomList = _chatRoomList.asSharedFlow()

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun getChatRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getChatRoom()
            result.onSuccess {
                _chatRoomList.emit(it.values.toList())
            }.onError { code, message ->
                Log.d("HomeViewModel", "${code}---${message}")
            }.onException {
                Log.d("HomeViewModel", "$it")
            }
            _isLoading.value = false
        }
    }
}
