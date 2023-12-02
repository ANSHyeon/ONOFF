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
import com.google.firebase.storage.FirebaseStorage
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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val chatRoomRepository: ChatRoomRepository
) : ViewModel() {

    val sendMessage = MutableStateFlow("")

    lateinit var messageList: StateFlow<List<Message>>
        private set

    private val _chatRoomKey: MutableStateFlow<String> = MutableStateFlow("")
    val chatRoomKey: StateFlow<String> = _chatRoomKey

    private val _currentUser: MutableStateFlow<User?> = MutableStateFlow(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getUser() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.getUser()
            result.onSuccess {
                _currentUser.value = it.values.first().profileUri?.let { uri ->
                    it.values.first().copy(
                        profileUrl = downloadImage(uri)
                    )
                } ?: it.values.first()


            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
            }
        }
    }

    private suspend fun downloadImage(location: String): String {
        val storageRef = FirebaseStorage.getInstance().reference
        return storageRef.child(location).downloadUrl.await().toString()
    }

    fun getMessage(chatRoomId: String) {
        messageList = transformMessageList(chatRoomId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private fun transformMessageList(chatRoomId: String): Flow<List<Message>> =
        chatRoomRepository.getMessage(
            chatRoomId,
            onComplete = {
                _isLoading.value = false
            },
            onError = {

            }
        ).map {
            it.sortedBy { message -> message.sendAt }
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

    fun createMessage(chatRoomId: String) {
        if (sendMessage.value.isNotBlank()) {
            viewModelScope.launch {
                _isLoading.value = true
                val currentTime = System.currentTimeMillis()
                val messageId = chatRoomId + (currentUser.value?.userId) + currentTime
                val message = Message(
                    messageId,
                    chatRoomId,
                    currentUser.value?.nickName ?: "",
                    currentUser.value?.email ?: "",
                    currentUser.value?.profileUri,
                    currentUser.value?.profileUrl,
                    sendMessage.value,
                    currentTime
                )
                sendMessage.value = ""
                val result = chatRoomRepository.createMessage(message)
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
}
