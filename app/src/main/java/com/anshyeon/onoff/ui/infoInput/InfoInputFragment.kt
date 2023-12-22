package com.anshyeon.onoff.ui.infoInput

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentInfoInputBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.ui.extensions.showMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InfoInputFragment : BaseFragment<FragmentInfoInputBinding>(R.layout.fragment_info_input) {

    private val viewModel by viewModels<InfoInputViewModel>()

    private val getMultipleContents =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            binding.ivInfoInputProfile.setImageURI(it)
            viewModel.updateProfileImage(it)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        setSubmitButtonClickListener()
        setTextChangedListener()
        setImageSelectorClickListener()
        observeIsSave()
        setSnackBarMessage()
    }

    private fun setImageSelectorClickListener() {
        binding.ivInfoInputCamera.setOnClickListener {
            getMultipleContents.launch("image/*")
        }
    }

    private fun setSubmitButtonClickListener() {
        binding.btnSubmitUserInfo.setOnClickListener {
            viewModel.saveUserInfo()
        }
    }

    private fun setTextChangedListener() {
        binding.etInfoInputNickName.doAfterTextChanged {
            viewModel.updateNickName(it?.toString() ?: "")
        }
    }

    private fun observeIsSave() {
        lifecycleScope.launch {
            viewModel.isSaved
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect {
                    if (it) {
                        val action = InfoInputFragmentDirections.actionInfoInputToHome()
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
                binding.btnSubmitUserInfo.showMessage(it)
            }
        }
    }
}