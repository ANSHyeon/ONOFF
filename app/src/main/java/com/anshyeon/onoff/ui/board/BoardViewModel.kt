package com.anshyeon.onoff.ui.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.Address
import com.anshyeon.onoff.data.model.Post
import com.anshyeon.onoff.data.repository.PlaceRepository
import com.anshyeon.onoff.data.repository.PostRepository
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
class BoardViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {

    lateinit var postLsit: StateFlow<List<Post>>

    private val _currentPlaceInfo: MutableStateFlow<Address?> = MutableStateFlow(null)
    val currentPlaceInfo: StateFlow<Address?> = _currentPlaceInfo

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getPostList(location: String) {
        postLsit = transformPostList(location).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private fun transformPostList(location: String): Flow<List<Post>> =
        postRepository.getPostList(
            location,
            onComplete = {

            },
            onError = {

            }
        ).map {
            it.sortedByDescending { post -> post.createdDate }
        }

    fun getCurrentPlaceInfo(latitude: String, longitude: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = placeRepository.getPlaceInfoByLocation(latitude, longitude)
            result.onSuccess {
                _currentPlaceInfo.value = it.documents.first().address
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
            }
            _isLoading.value = false
        }
    }
}