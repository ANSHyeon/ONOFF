package com.anshyeon.onoff.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.OnOffApplication
import com.anshyeon.onoff.util.Constants

class LauncherFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveToFirstScreen()
    }

    private fun moveToFirstScreen() {
        val localGoogleIdToken =
            OnOffApplication.preferencesManager.getString(Constants.KEY_GOOGLE_ID_TOKEN, "")

        val action = if (localGoogleIdToken.isNotEmpty()) {
            LauncherFragmentDirections.actionLauncherToHome()
        } else {
            LauncherFragmentDirections.actionLauncherToSignIn()
        }
        findNavController().navigate(action)
    }
}