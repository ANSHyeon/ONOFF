package com.anshyeon.onoff.ui.board

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.Post
import com.anshyeon.onoff.databinding.FragmentBoardBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.util.NetworkConnection
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
        binding.viewModel = viewModel
        setNetworkErrorBar()
    }

    private fun getPostList() {
        val adapter = BoardAdapter(this)
        binding.rvBoardList.adapter = adapter

        getCurrentPlaceInfo()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(
                Lifecycle.State.STARTED,
            ) {
                viewModel.currentPlaceInfo.collect {
                    it?.let { placeInfo ->
                        val location = placeInfo.region3depthName
                        setToolBar(location)
                        viewModel.getPostList(location)
                        viewModel.postList
                            .collect { postList ->
                                adapter.submitList(postList)
                                if (postList.isNotEmpty()) {
                                    binding.tvNothingPost.visibility = View.GONE
                                }
                            }
                    }
                }
            }

        }
    }

    private fun getCurrentPlaceInfo() {
        client.lastLocation.addOnSuccessListener { location ->
            viewModel.getCurrentPlaceInfo(
                location.latitude.toString(),
                location.longitude.toString()
            )
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

    private fun setNetworkErrorBar() {
        NetworkConnection(requireContext()).observe(viewLifecycleOwner) {
            val visibility = if (it) {
                getPostList()
                View.GONE
            } else {
                View.VISIBLE
            }
            binding.networkErrorBar.visibility = visibility
        }
    }

    override fun onPostClick(post: Post) {
        val action = BoardFragmentDirections.actionBoardToDetail(post)
        findNavController().navigate(action)
    }
}