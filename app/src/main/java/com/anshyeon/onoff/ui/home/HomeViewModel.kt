package com.anshyeon.onoff.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.repository.ChatRoomRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.naver.maps.map.overlay.Marker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val chatRoomRepository: ChatRoomRepository
) : ViewModel() {

    private val _markerList: MutableSet<Marker> = mutableSetOf()

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _chatRoomList: MutableStateFlow<List<ChatRoom>> = MutableStateFlow(emptyList())
    val chatRoomList: StateFlow<List<ChatRoom>> = _chatRoomList

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isPermissionGranted: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val isPermissionGranted: StateFlow<Boolean?> = _isPermissionGranted

    fun getChatRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = chatRoomRepository.getChatRoom()
            result.onSuccess {
                _chatRoomList.value = it.values.toList()
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
            }
            _isLoading.value = false
        }
    }

    fun addMarker(marker: Marker) {
        _markerList.add(marker)
    }

    fun removeMarkerOnMap() {
        _markerList.forEach {
            it.map = null
        }
        _markerList.clear()
    }

    fun updateIsPermissionGranted(state: Boolean) {
        viewModelScope.launch {
            _isPermissionGranted.value = state
        }
    }

    fun insertChatRoom(chatRoom: ChatRoom) {
        viewModelScope.launch {
            _isLoading.value = true
            chatRoomRepository.insertChatRoom(chatRoom)
            _isLoading.value = false
        }
    }
}
