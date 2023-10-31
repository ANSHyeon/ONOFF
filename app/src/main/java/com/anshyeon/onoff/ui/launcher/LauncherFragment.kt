package com.anshyeon.onoff.ui.launcher

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherFragment : Fragment() {

    private val viewModel by viewModels<LauncherViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveToFirstScreen()
    }

    private fun moveToFirstScreen() {
        val localGoogleIdToken = viewModel.getLocalGoogleIdToken()

        val action = if (localGoogleIdToken.isNotEmpty()) {
            LauncherFragmentDirections.actionLauncherToHome()
        } else {
            LauncherFragmentDirections.actionLauncherToSignIn()
        }
        findNavController().navigate(action)
    }
}