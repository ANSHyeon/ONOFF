package com.anshyeon.onoff.ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anshyeon.onoff.R
import com.anshyeon.onoff.data.model.ChatRoom
import com.anshyeon.onoff.databinding.FragmentChatBinding
import com.anshyeon.onoff.ui.BaseFragment
import com.anshyeon.onoff.util.SamePlaceChecker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>(R.layout.fragment_chat),
    OnEnterButtonClickListener {

    private val viewModel by viewModels<ChatViewModel>()
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
        setChatRoomList()
    }

    private fun setChatRoomList() {
        val adapter = ChatAdapter(this)
        binding.rvChatRoomList.adapter = adapter
        lifecycleScope.launch {
            viewModel.chatRoomList
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { chatRoomList -> adapter.submitList(chatRoomList) }
        }
    }

    override fun enterChatRoom(chatRoom: ChatRoom) {
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
                    if (SamePlaceChecker.isSamePlace(placeInfo, chatRoom)) {
                        val action =
                            ChatFragmentDirections.actionChatToChatRoom(
                                chatRoom.placeName,
                                chatRoom.chatRoomId
                            )
                        findNavController().navigate(action)

                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.error_message_not_same_place),
                            Snackbar.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }
    }
}