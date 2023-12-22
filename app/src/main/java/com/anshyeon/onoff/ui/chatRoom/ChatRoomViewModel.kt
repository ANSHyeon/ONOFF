package com.anshyeon.onoff.ui.chatRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.Message
import com.anshyeon.onoff.data.model.PlaceInfo
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.data.repository.AuthRepository
import com.anshyeon.onoff.data.repository.ChatRoomRepository
import com.anshyeon.onoff.data.repository.PlaceRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val placeRepository: PlaceRepository,
) : ViewModel() {

    val sendMessage = MutableStateFlow("")

    lateinit var localMessageList: StateFlow<List<Message>>
        private set

    lateinit var localUserList: StateFlow<List<User>>
        private set

    val memberIdList = mutableSetOf<String>()

    private val _chatRoomKey: MutableStateFlow<String> = MutableStateFlow("")
    val chatRoomKey: StateFlow<String> = _chatRoomKey

    private val _currentPlaceInfo: MutableStateFlow<PlaceInfo?> = MutableStateFlow(null)
    val currentPlaceInfo: StateFlow<PlaceInfo?> = _currentPlaceInfo

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isUserSaved: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isUserSaved: StateFlow<Boolean> = _isUserSaved

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setComplete() {
        _isLoading.value = false
    }

    fun getUserId(): String {
        return authRepository.getUserId()
    }

    fun getLocalMessage(chatRoomId: String) {
        localMessageList = transformLocalMessageList(chatRoomId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private fun transformLocalMessageList(chatRoomId: String): Flow<List<Message>> {
        return chatRoomRepository.getMessageListByRoom(
            chatRoomId,
            onComplete = {
                _isLoading.value = false
            }
        ).map {
            it.sortedBy { message -> message.sendAt }
        }
    }

    fun getLocalUser(userIdList: List<String>) {
        localUserList = transformLocalUserList(userIdList).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private fun transformLocalUserList(userIdList: List<String>): Flow<List<User>> {
        return chatRoomRepository.getUserListByRoom(
            userIdList,
            onComplete = {
            },
            onError = {
                _isLoading.value = false
            }
        )
    }

    fun getChatRoomOfPlace(placeName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = chatRoomRepository.getChatRoomOfPlace(placeName)
            result.onSuccess {
                if (it.isNotEmpty()) {
                    _chatRoomKey.value = it.keys.first()
                }
            }.onError { code, message ->
                _isLoading.value = false
            }.onException {
                _isLoading.value = false
            }
        }
    }

    fun getUserList(userList: List<String>) {
        viewModelScope.launch {
            _isUserSaved.value = false
            _isLoading.value = true
            val result = chatRoomRepository.getUserList(userList)
            result.onSuccess {
                memberIdList.addAll(userList)
                _isUserSaved.value = true
            }.onError { code, message ->
                _isUserSaved.value = false
                _isLoading.value = false
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _isUserSaved.value = false
                _isLoading.value = false
                _snackBarText.emit(R.string.error_message_retry)
            }
        }
    }

    fun createMessage(chatRoomId: String) {
        if (sendMessage.value.isNotBlank()) {
            viewModelScope.launch {
                _isLoading.value = true
                val message = sendMessage.value
                sendMessage.value = ""
                val result = chatRoomRepository.createMessage(chatRoomId, message)
                result.onSuccess {
                    updateChatRoom(chatRoomKey.value, chatRoomId)
                }.onError { code, message ->
                    _isLoading.value = false
                    _snackBarText.emit(R.string.error_message_retry)
                }.onException {
                    _isLoading.value = false
                    _snackBarText.emit(R.string.error_message_retry)
                }
            }
        }
    }

    private fun updateChatRoom(chatRoomKey: String, chatRoomId: String) {
        viewModelScope.launch {
            val result = chatRoomRepository.updateChatRoom(chatRoomKey)
            result.onSuccess {
                updateChatRoomInRoom(chatRoomId)
            }.onError { code, message ->
                _isLoading.value = false
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _isLoading.value = false
                _snackBarText.emit(R.string.error_message_retry)
            }
        }
    }

    private fun updateChatRoomInRoom(chatRoomId: String) {
        viewModelScope.launch {
            chatRoomRepository.updateChatRoomInRoom(chatRoomId)
            _isLoading.value = false
        }
    }

    fun insertMessage(message: Message) {
        viewModelScope.launch {
            _isLoading.value = true
            chatRoomRepository.insertMessage(message)
            _isLoading.value = false
        }
    }

    fun getCurrentPlaceInfo(latitude: String, longitude: String) {
        viewModelScope.launch {
            val result = placeRepository.getPlaceInfoByLocation(latitude, longitude)
            result.onSuccess {
                _currentPlaceInfo.value = it
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
                _isLoading.value = false
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
                _isLoading.value = false
            }
        }
    }
}