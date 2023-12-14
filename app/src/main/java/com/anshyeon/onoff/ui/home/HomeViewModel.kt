package com.anshyeon.onoff.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.PlaceInfo
import com.anshyeon.onoff.data.repository.AuthRepository
import com.anshyeon.onoff.data.repository.ChatRoomRepository
import com.anshyeon.onoff.data.repository.PlaceRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import com.naver.maps.map.overlay.Marker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _markerList: MutableSet<Marker> = mutableSetOf()
    var selectedChatRoom: ChatRoom? = null

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    val chatRoomList: StateFlow<List<ChatRoom>> = transformChatRoomList().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _savedChatRoom: MutableStateFlow<ChatRoom?> = MutableStateFlow(null)
    val savedChatRoom: StateFlow<ChatRoom?> = _savedChatRoom

    private val _currentPlaceInfo: MutableStateFlow<PlaceInfo?> = MutableStateFlow(null)
    val currentPlaceInfo: StateFlow<PlaceInfo?> = _currentPlaceInfo

    private val _isPermissionGranted: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val isPermissionGranted: StateFlow<Boolean?> = _isPermissionGranted

    private fun transformChatRoomList(): Flow<List<ChatRoom>> =
        chatRoomRepository.getChatRoom(
            onComplete = {
                _isLoading.value = false
            },
            onError = {
                _isLoading.value = false
            }
        )

    fun getLocalGoogleIdToken(): String {
        return authRepository.getLocalIdToken() ?: ""
    }

    fun getCurrentPlaceInfo(latitude: String, longitude: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = placeRepository.getPlaceInfoByLocation(latitude, longitude)
            result.onSuccess {
                _currentPlaceInfo.value = it
                _isLoading.value = false
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
                _isLoading.value = false
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
                _isLoading.value = false
            }
        }
    }

    fun addMarker(marker: Marker) {
        _markerList.add(marker)
    }

    fun updateIsPermissionGranted(state: Boolean) {
        _isPermissionGranted.value = state
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