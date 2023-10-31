package com.anshyeon.onoff.ui.launcher

import androidx.lifecycle.ViewModel
import com.anshyeon.onoff.data.source.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    fun getLocalGoogleIdToken(): String {
        return repository.getLocalIdToken()
    }
}