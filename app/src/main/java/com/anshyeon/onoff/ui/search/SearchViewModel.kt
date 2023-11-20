package com.anshyeon.onoff.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.Place
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
    private val placeRepository: PlaceRepository
) : ViewModel() {

    val searchedPlaceName = MutableStateFlow("")

    private val _snackBarText = MutableSharedFlow<Int>()
    val snackBarText = _snackBarText.asSharedFlow()

    private val _searchedPlace: MutableStateFlow<Place?> = MutableStateFlow(null)
    val searchedPlace: StateFlow<Place?> = _searchedPlace

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getSearchPlace() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = placeRepository.getSearchPlace(searchedPlaceName.value)
            result.onSuccess {
                try {
                    _searchedPlace.emit(it.documents.first())
                } catch (e: NoSuchElementException) {
                    _snackBarText.emit(R.string.error_message_search_place_name)
                }
            }.onError { code, message ->
                _snackBarText.emit(R.string.error_message_retry)
            }.onException {
                _snackBarText.emit(R.string.error_message_retry)
            }
            _isLoading.value = false
        }
    }
}
