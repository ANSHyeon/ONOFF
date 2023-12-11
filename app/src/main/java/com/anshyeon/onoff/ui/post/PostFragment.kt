package com.anshyeon.onoff.ui.post

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import com.anshyeon.onoff.R
import com.anshyeon.onoff.databinding.FragmentPostBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.ui.extensions.setClickEvent
import com.anshyeon.onoff.ui.extensions.showMessage
import com.anshyeon.onoff.util.DateFormatText
import com.anshyeon.onoff.util.NetworkConnection
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostFragment : BaseFragment<FragmentPostBinding>(R.layout.fragment_post),
    PhotoSelectorOnclickListener, PhotoRemoverOnclickListener {

    private val viewModel by viewModels<PostViewModel>()
    private val args: PostFragmentArgs by navArgs()

    private val imageHeaderAdapter = PostPhotoSelectorAdapter(this)
    private val imageCountLimit = 10
    private val imageListAdapter = PostSelectedPhotoAdapter(imageCountLimit, this)

    private val getMultipleContents =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            addImageList(it)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        binding.location = args.location
        setToolbar()
        setImageList()
        setComplete()
        setNetworkErrorBar()
        setSubmitButton()
        setSnackBarMessage()
    }

    private fun setToolbar() {
        binding.toolbarPost.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setImageList() {
        imageHeaderAdapter.setImageHeader(imageCountLimit)
        binding.rvPost.adapter = ConcatAdapter(imageHeaderAdapter, imageListAdapter)
    }

    private fun setComplete() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSaved.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                if (it) {
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onImageContentRequest() {
        getMultipleContents.launch("image/*")
    }

    override fun removeImage(position: Int) {
        imageListAdapter.removeImage(position)
        imageHeaderAdapter.updateImageHeader(imageListAdapter.itemCount)
    }

    private fun addImageList(uriList: List<Uri>) {
        for (uri in uriList) {
            val fileName = getFileName(uri)
            if (fileName.isNotEmpty()) {
                val newFileName = getString(
                    R.string.format_file_name,
                    DateFormatText.getFileNameFormat(),
                    fileName
                )
                imageListAdapter.addImage(uri, newFileName)
            }
        }
        viewModel.updateImageList(imageListAdapter.getItems())
        imageHeaderAdapter.updateImageHeader(imageListAdapter.itemCount)
    }

    private fun getFileName(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: ""
    }

    private fun setNetworkErrorBar() {
        NetworkConnection(requireContext()).observe(viewLifecycleOwner) {
            val visibility = if (it) {
                View.GONE
            } else {
                View.VISIBLE
            }
            binding.networkErrorBar.visibility = visibility
        }
    }

    private fun setSubmitButton() {
        binding.btnPostSend.setClickEvent(viewLifecycleOwner.lifecycleScope) {
            viewModel.submitPost(args.location)
        }
    }

    private fun setSnackBarMessage() {
        lifecycleScope.launch {
            viewModel.snackBarText.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                binding.btnPostSend.showMessage(it)
            }
        }
    }
}