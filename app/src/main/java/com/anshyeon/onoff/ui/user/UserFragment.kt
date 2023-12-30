package com.anshyeon.onoff.ui.user

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentUserBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.util.NetworkConnection
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserFragment : BaseFragment<FragmentUserBinding>(R.layout.fragment_user) {

    private val viewModel by viewModels<UserViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        setNetworkErrorBar()
        setToolBar()
        setLogOutButtonClickListener()
        setSnackBarMessage()
    }

    private fun setNetworkErrorBar() {
        NetworkConnection(requireContext()).observe(viewLifecycleOwner) {
            viewModel.setConnectedState(it)
            if (it) {
                viewModel.getUserInfo()
                setUserInfo()
            }
        }
    }

    private fun setUserInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                launch {
                    viewModel.currentUserInfo.collect { user ->
                        viewModel.saveUserInLocal(user)
                    }
                }
                launch {
                    viewModel.isLogOut.collect {
                        if (it) {
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    private fun setToolBar() {
        binding.toolbarUser.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.user_app_bar_edit -> {
                    if (viewModel.isConnected) {
                        val nickname = viewModel.currentUserInfo.value.nickName
                        val profileUrl = viewModel.currentUserInfo.value.profileUrl ?: ""
                        val profileUri = viewModel.currentUserInfo.value.profileUri ?: ""
                        val action =
                            UserFragmentDirections.actionUserToEdit(
                                nickname,
                                profileUrl,
                                profileUri,
                                viewModel.userKey
                            )
                        findNavController().navigate(action)
                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.error_message_retry),
                            Snackbar.LENGTH_SHORT,
                        ).show()
                    }
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun setSnackBarMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.snackBarText.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                Snackbar.make(
                    binding.root,
                    getString(it),
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun setLogOutButtonClickListener() {
        binding.btnLogOut.setOnClickListener {
            if (viewModel.isConnected) {
                viewModel.logOut()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.error_message_retry),
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
        }
    }
}