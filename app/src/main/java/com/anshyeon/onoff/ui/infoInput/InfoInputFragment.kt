package com.anshyeon.onoff.ui.infoInput

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentInfoInputBinding
import com.anshyeon.onoff.ui.BaseFragment

class InfoInputFragment : BaseFragment<FragmentInfoInputBinding>(R.layout.fragment_info_input) {

    private val viewModel by viewModels<InfoInputViewModel> {
        InfoInputViewModel.provideFactory()
    }

    private val getMultipleContents =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            binding.ivInfoInputProfile.setImageURI(it)
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
    }

    private fun setImageSelectorClickListener() {
        binding.ivInfoInputCamera.setOnClickListener {
            getMultipleContents.launch("image/*")
        }
    }

    private fun setSubmitButtonClickListener() {
        binding.btnSubmitUserInfo.setOnClickListener {
            val action = InfoInputFragmentDirections.actionInfoInputToHome()
            findNavController().navigate(action)
        }
    }

    private fun setTextChangedListener() {
        binding.etInfoInputNickName.doAfterTextChanged {
            viewModel.isValidInfo(it?.toString() ?: "")
        }
    }
}