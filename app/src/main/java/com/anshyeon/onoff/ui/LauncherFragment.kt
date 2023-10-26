package com.anshyeon.onoff.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.data.source.repository.AuthRepository

class LauncherFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveToFirstScreen()
    }

    private fun moveToFirstScreen() {
        val action = if (AuthRepository().getCurrentUser() != null) {
            LauncherFragmentDirections.actionLauncherToHome()
        } else {
            LauncherFragmentDirections.actionLauncherToSignIn()
        }
        findNavController().navigate(action)
    }
}