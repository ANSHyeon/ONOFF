package com.anshyeon.onoff.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.source.repository.ChatRoomRepository
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
            val response = repository.getChatRoom().body()?.values
            response?.toList()?.let {
                _chatRoomList.emit(it)
            }
            _isLoading.value = false
        }
    }
}
