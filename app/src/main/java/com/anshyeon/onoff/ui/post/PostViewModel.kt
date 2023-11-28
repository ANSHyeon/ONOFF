package com.anshyeon.onoff.ui.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.ImageContent
import com.anshyeon.onoff.data.model.User
import com.anshyeon.onoff.data.repository.AuthRepository
import com.anshyeon.onoff.data.repository.PostRepository
import com.anshyeon.onoff.network.extentions.onError
import com.anshyeon.onoff.network.extentions.onException
import com.anshyeon.onoff.network.extentions.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val title = MutableStateFlow("")
    val body = MutableStateFlow("")

    private var imageList: List<ImageContent> = emptyList()

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    fun updateImageList(items: List<ImageContent>) {
        imageList = items
    }

    fun submitPost(location: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.getUser()
            result.onSuccess {
                createPost(it.values.first(), location)
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
            }
            _isLoading.value = false
        }
    }

    private fun createPost(user: User, location: String) {
        if (!isPostContentEmpty()) {
            viewModelScope.launch {
                _isLoading.value = true
                val result = postRepository.createPost(
                    title.value,
                    body.value,
                    location,
                    user,
                    imageList
                )
                result.onSuccess {
                    _isSaved.value = true
                }.onError { code, message ->
                    _snackBarText.emit(R.string.error_message_retry)
                }.onException {
                    _snackBarText.emit(R.string.error_message_retry)
                }
                _isLoading.value = false
            }
        } else {
            _isSaved.value = false
        }
    }

    private fun isPostContentEmpty(): Boolean {
        return title.value.isBlank() || body.value.isBlank()
    }
}