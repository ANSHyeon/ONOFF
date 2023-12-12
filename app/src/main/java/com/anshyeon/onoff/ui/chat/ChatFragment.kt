package com.anshyeon.onoff.ui.chat

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
import com.anshyeon.onoff.util.NetworkConnection
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>(R.layout.fragment_chat),
    OnEnterButtonClickListener {

    private val viewModel by viewModels<ChatViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()
    }

    private fun setLayout() {
        binding.viewModel = viewModel
        setChatRoomList()
        setNetworkErrorBar()
    }

    private fun setNetworkErrorBar() {
        NetworkConnection(requireContext()).observe(viewLifecycleOwner) {
            binding.networkErrorBar.visibility = if (it) View.GONE else View.VISIBLE
        }
    }

    private fun setChatRoomList() {
        val adapter = ChatAdapter(this)
        binding.rvChatRoomList.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatRoomList
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .collect { chatRoomList -> adapter.submitList(chatRoomList) }
        }
    }

    override fun enterChatRoom(chatRoom: ChatRoom) {
        val action =
            ChatFragmentDirections.actionChatToChatRoom(
                chatRoom.placeName,
                chatRoom.chatRoomId,
                chatRoom.address,
                chatRoom.latitude,
                chatRoom.longitude
            )
        findNavController().navigate(action)
    }
}