package com.anshyeon.onoff.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.PlaceInfo
import com.anshyeon.onoff.data.repository.ChatRoomRepository
import com.anshyeon.onoff.data.repository.PlaceRepository
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
    private val placeRepository: PlaceRepository,
    private val chatRoomRepository: ChatRoomRepository
) : ViewModel() {

    private val _markerList: MutableSet<Marker> = mutableSetOf()
    var selectedChatRoom: ChatRoom? = null

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _chatRoomList: MutableStateFlow<List<ChatRoom>> = MutableStateFlow(emptyList())
    val chatRoomList: StateFlow<List<ChatRoom>> = _chatRoomList

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _savedChatRoom: MutableStateFlow<ChatRoom?> = MutableStateFlow(null)
    val savedChatRoom: StateFlow<ChatRoom?> = _savedChatRoom

    private val _currentPlaceInfo: MutableStateFlow<PlaceInfo?> = MutableStateFlow(null)
    val currentPlaceInfo: StateFlow<PlaceInfo?> = _currentPlaceInfo

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

    fun getCurrentPlaceInfo(latitude: String, longitude: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = placeRepository.getPlaceInfoByLocation(latitude, longitude)
            result.onSuccess {
                _currentPlaceInfo.value = it
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
            }
            _isLoading.value = false
        }
    }

    fun reset() {
        _markerList.clear()
        _chatRoomList.value = emptyList()
        _savedChatRoom.value = null
        _isPermissionGranted.value = null
        _currentPlaceInfo.value = null
    }

    fun addMarker(marker: Marker) {
        _markerList.add(marker)
    }

    fun updateIsPermissionGranted(state: Boolean?) {
        viewModelScope.launch {
            _isPermissionGranted.value = state
        }
    }

    fun insertChatRoom(chatRoom: ChatRoom) {
        viewModelScope.launch {
            _isLoading.value = true
            chatRoomRepository.insertChatRoom(chatRoom)
            _isLoading.value = false
            _savedChatRoom.value = chatRoom
        }
    }
}