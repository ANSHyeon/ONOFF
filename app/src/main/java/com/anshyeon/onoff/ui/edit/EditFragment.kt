package com.anshyeon.onoff.ui.edit

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentEditBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.ui.extensions.setClickEvent
import com.anshyeon.onoff.ui.extensions.showMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditFragment : BaseFragment<FragmentEditBinding>(R.layout.fragment_edit) {

    private val viewModel by viewModels<EditViewModel>()
    private val args: EditFragmentArgs by navArgs()
    private val getMultipleContents =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                binding.ivUserProfile.setImageURI(it)
                binding.ivUserProfile.visibility = View.VISIBLE
                viewModel.updateProfileUri(it)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        setDefaultUserInfo()
        setNavigationOnClickListener()
        setSnackBarMessage()
        observeIsUpdated()
        setImageSelectorClickListener()
        setSubmitButtonClickListener()
    }

    private fun setImageSelectorClickListener() {
        binding.ivUserProfileCameraBackground.setOnClickListener {
            getMultipleContents.launch("image/*")
        }
    }

    private fun setSubmitButtonClickListener() {
        binding.btnUserEdit.setClickEvent(viewLifecycleOwner.lifecycleScope) {
            viewModel.updateUserInfo(args.imageLocation, args.userKey)
        }
    }

    private fun observeIsUpdated() {
        lifecycleScope.launch {
            viewModel.isUpdated
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect {
                    if (it) {
                        findNavController().navigateUp()
                    }
                }
        }
    }

    private fun setDefaultUserInfo() {
        val profileUrl = args.profileUrl.ifBlank { null }
        viewModel.setUserInfo(args.nickName, profileUrl)
    }

    private fun setNavigationOnClickListener() {
        binding.toolbarEdit.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setSnackBarMessage() {
        lifecycleScope.launch {
            viewModel.snackBarText.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                binding.btnUserEdit.showMessage(it)
            }
        }
    }
}