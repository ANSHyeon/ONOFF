package com.anshyeon.onoff.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.data.model.Place
import com.anshyeon.onoff.data.model.PlaceInfo
import com.anshyeon.onoff.data.repository.ChatRoomRepository
import com.anshyeon.onoff.data.repository.PlaceRepository
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
class SearchViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val chatRoomRepository: ChatRoomRepository
) : ViewModel() {

    val searchedPlaceName = MutableStateFlow("")
    private var chatRoomKey = ""

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _searchedPlace: MutableStateFlow<Place?> = MutableStateFlow(null)
    val searchedPlace: StateFlow<Place?> = _searchedPlace

    private val _placeChatRoom: MutableStateFlow<ChatRoom?> = MutableStateFlow(null)
    val placeChatRoom: StateFlow<ChatRoom?> = _placeChatRoom

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaved: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private val _currentPlaceInfo: MutableStateFlow<PlaceInfo?> = MutableStateFlow(null)
    val currentPlaceInfo: StateFlow<PlaceInfo?> = _currentPlaceInfo

    fun getSearchPlace() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = placeRepository.getSearchPlace(searchedPlaceName.value)
            result.onSuccess {
                try {
                    _searchedPlace.value = it.documents.first()
                    getChatRoomOfPlace(it.documents.first())
                } catch (e: NoSuchElementException) {
                    _snackBarText.emit(R.string.error_message_search_place_name)
                }
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

    private fun getChatRoomOfPlace(place: Place) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = chatRoomRepository.getChatRoomOfPlace(place.placeName)
            result.onSuccess {
                _isLoading.value = false
                if (it.isNotEmpty()) {
                    chatRoomKey = it.keys.first()
                    _placeChatRoom.value = it.values.first()
                }
            }.onError { code, message ->
                _isLoading.value = false
            }.onException {
                _isLoading.value = false
            }
        }
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

    private fun insertChatRoom(chatRoom: ChatRoom) {
        _isSaved.value = false
        viewModelScope.launch {
            _isLoading.value = true
            chatRoomRepository.insertChatRoom(chatRoom)
            _isLoading.value = false
            _isSaved.value = true
        }
    }

    fun createChatRoom(searchedPlace: Place) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = chatRoomRepository.createChatRoom(searchedPlace)
            result.onSuccess {
                _isLoading.value = false
                _isSaved.value = true
            }.onError { code, message ->
                _isLoading.value = false
                _isSaved.value = false
            }.onException {
                _isLoading.value = false
                _isSaved.value = false
            }
        }
    }

    fun addMemberToChatRoom(chatRoom: ChatRoom) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = chatRoomRepository.addMemberToChatRoom(chatRoom, chatRoomKey)
            result.onSuccess {
                _isLoading.value = false
                insertChatRoom(chatRoom)
            }.onError { code, message ->
                _isLoading.value = false
            }.onException {
                _isLoading.value = false
            }
        }
    }
}