package com.anshyeon.onoff.ui.signin

import android.app.Activity
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.BuildConfig
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentSignInBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.ui.extensions.showMessage
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInFragment : BaseFragment<FragmentSignInBinding>(R.layout.fragment_sign_in) {

    private val viewModel by viewModels<SignInViewModel>()
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signInLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oneTapClient = Identity.getSignInClient(requireActivity())
        signInRequest = provideSignInRequest()
        signInLauncher = getActivityResultLauncher()
        auth = Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        observeHasUserInfo()
        observeIsSaveUserInfo()
        setSnackBarMessage()
        binding.btnGoogleSignIn.setOnClickListener {
            signIn()
        }
    }

    private fun getActivityResultLauncher() = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val googleCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
                val idToken = googleCredential.googleIdToken
                when {
                    idToken != null -> {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnSuccessListener {
                                viewModel.getUserInfo()
                            }
                            .addOnFailureListener {
                                Log.w(
                                    "SignInActivity",
                                    "signInWithCredential:failure",
                                    it
                                )
                                binding.root.showMessage(R.string.message_sign_in_failure)
                            }
                    }

                    else -> {
                        Log.d("SignInActivity", "No ID token!")
                    }
                }
            } catch (e: ApiException) {
                Log.e("SingInActivity", "failure: ${e.message}")
            }
        }
    }

    private fun provideSignInRequest() = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()

    private fun signIn() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(requireActivity()) { result ->
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    signInLauncher.launch(intentSenderRequest)
                } catch (e: SendIntentException) {
                    Log.e("SignInActivity", "Couldn't start One Tap UI: " + e.localizedMessage)
                }
            }
            .addOnFailureListener(requireActivity()) { e ->
                Log.d("SignInActivity", "No saved credentials found: ${e.message}")
                binding.root.showMessage(R.string.message_sign_in_failure)
            }
    }

    private fun observeHasUserInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasUserInfo.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect { hasUserInfo ->
                if (hasUserInfo) {
                    viewModel.saveUserInfo()
                } else {
                    val action = SignInFragmentDirections.actionSignInToInfoInput()
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun observeIsSaveUserInfo() {
        lifecycleScope.launch {
            viewModel.isSaveUserInfo.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect { isSaveUserInfo ->
                if (isSaveUserInfo) {
                    val action = SignInFragmentDirections.actionSignInToHome()
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setSnackBarMessage() {
        lifecycleScope.launch {
            viewModel.snackBarText.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                binding.root.showMessage(it)
            }
        }
    }
}