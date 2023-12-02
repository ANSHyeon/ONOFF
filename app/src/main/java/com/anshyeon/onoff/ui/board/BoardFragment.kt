package com.anshyeon.onoff.ui.board

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.Post
import com.anshyeon.onoff.databinding.FragmentBoardBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class BoardFragment : BaseFragment<FragmentBoardBinding>(R.layout.fragment_board),
    OnPostClickListener {

    private val viewModel by viewModels<BoardViewModel>()
    private lateinit var client: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        getCurrentPlaceInfo()
    }

    private fun getCurrentPlaceInfo() {
        client.lastLocation.addOnSuccessListener { location ->
            viewModel.getCurrentPlaceInfo(
                location.latitude.toString(),
                location.longitude.toString()
            )
        }

        lifecycleScope.launch {
            viewModel.currentPlaceInfo.flowWithLifecycle(
                viewLifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            ).collect {
                it?.let { placeInfo ->
                    val location = placeInfo.region3depthName
                    setToolBar(location)
                    viewModel.getPostList(location)
                    setPostList()
                }
            }
        }
    }

    private fun setPostList() {
        val adapter = BoardAdapter(this)
        binding.rvBoardList.adapter = adapter
        lifecycleScope.launch {
            viewModel.postLsit
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { postList -> adapter.submitList(postList) }
        }
    }

    private fun setToolBar(address: String) {
        binding.toolbarBoard.title = address
        binding.toolbarBoard.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home_app_bar_add -> {
                    val action = BoardFragmentDirections.actionBoardToPost(address)
                    findNavController().navigate(action)
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    override fun onPostClick(post: Post) {
    }
}